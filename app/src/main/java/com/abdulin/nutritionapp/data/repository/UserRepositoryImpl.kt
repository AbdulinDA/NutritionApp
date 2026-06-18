package com.abdulin.nutritionapp.data.repository

import android.content.Context
import com.abdulin.nutritionapp.R
import com.abdulin.nutritionapp.core.network.safeApiCall
import com.abdulin.nutritionapp.core.utils.Resource
import com.abdulin.nutritionapp.core.utils.TokenManager
import com.abdulin.nutritionapp.core.utils.toServerActivityLevel
import com.abdulin.nutritionapp.core.utils.toServerGender
import com.abdulin.nutritionapp.data.dto.auth.UserResponseDto
import com.abdulin.nutritionapp.data.dto.user.TodayWaterResponseDto
import com.abdulin.nutritionapp.data.dto.user.UpdateMyProfileRequestDto
import com.abdulin.nutritionapp.data.dto.user.UpdateWeightRequestDto
import com.abdulin.nutritionapp.data.dto.user.WaterLogRequestDto
import com.abdulin.nutritionapp.data.dto.user.WeightLogRequestDto
import com.abdulin.nutritionapp.data.local.dao.WaterDao
import com.abdulin.nutritionapp.data.local.dao.WaterStat
import com.abdulin.nutritionapp.data.local.dao.WeightDao
import com.abdulin.nutritionapp.data.local.entity.WaterEntity
import com.abdulin.nutritionapp.data.local.entity.WeightEntity
import com.abdulin.nutritionapp.data.remote.NutritionApi
import com.abdulin.nutritionapp.domain.repository.UserRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.JsonElement
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: NutritionApi,
    private val tokenManager: TokenManager,
    private val waterDao: WaterDao,
    private val weightDao: WeightDao
) : UserRepository {

    private fun UserResponseDto.serverActivityLevelOrDefault(): String =
        (activityLevel ?: "MODERATE").toServerActivityLevel()

    private fun mergePreferenceEcho(
        profile: UserResponseDto,
        requestedAllergies: List<String>? = null,
        requestedExcludedProductsIds: List<Long>? = null,
        requestedFavoriteCuisines: List<String>? = null,
        requestedDislikedCuisines: List<String>? = null
    ): UserResponseDto {
        val normalizedAllergies = requestedAllergies
            ?.mapNotNull { it.trim().takeIf(String::isNotBlank) }
            ?.distinctBy { it.lowercase() }
            .orEmpty()
        val normalizedExcludedIds = requestedExcludedProductsIds
            ?.distinct()
            .orEmpty()
        val normalizedFavoriteCuisines = requestedFavoriteCuisines
            ?.mapNotNull { it.trim().takeIf(String::isNotBlank) }
            ?.distinctBy { it.lowercase() }
            .orEmpty()
        val normalizedDislikedCuisines = requestedDislikedCuisines
            ?.mapNotNull { it.trim().takeIf(String::isNotBlank) }
            ?.distinctBy { it.lowercase() }
            ?.filterNot { disliked ->
                normalizedFavoriteCuisines.any { it.equals(disliked, ignoreCase = true) }
            }
            .orEmpty()

        return profile.copy(
            allergies = if (profile.allergies.isEmpty() && normalizedAllergies.isNotEmpty()) {
                normalizedAllergies
            } else {
                profile.allergies
            },
            excludedProductsIds = if (profile.excludedProductsIds.isEmpty() && normalizedExcludedIds.isNotEmpty()) {
                normalizedExcludedIds
            } else {
                profile.excludedProductsIds
            },
            favoriteCuisines = if (profile.favoriteCuisines.isEmpty() && normalizedFavoriteCuisines.isNotEmpty()) {
                normalizedFavoriteCuisines
            } else {
                profile.favoriteCuisines
            },
            dislikedCuisines = if (profile.dislikedCuisines.isEmpty() && normalizedDislikedCuisines.isNotEmpty()) {
                normalizedDislikedCuisines
            } else {
                profile.dislikedCuisines
            }
        )
    }

    override suspend fun getMyProfile(): Resource<UserResponseDto> {
        val result = safeApiCall<UserResponseDto> {
            api.getMyProfile()
        }
        return when (result) {
            is Resource.Success -> {
                val profile = mergePreferenceEcho(result.data!!)
                tokenManager.syncFoodPreferencesFromProfile(profile)
                Resource.Success(profile)
            }
            is Resource.Error -> Resource.Error(result.message ?: context.getString(R.string.repository_error_load_profile))
            is Resource.Loading -> Resource.Loading()
        }
    }

    override suspend fun updateProfile(
        currentUser: UserResponseDto,
        firstName: String,
        lastName: String,
        weightKg: Double,
        targetWeightKg: Double,
        goalTypeId: Long?,
        activityLevel: String
    ): Resource<UserResponseDto> {
        val normalizedFirstName = firstName.trim()
        val normalizedLastName = lastName.trim().ifBlank { currentUser.lastName.orEmpty() }
        val isProfileChanged =
            currentUser.firstName != normalizedFirstName ||
                currentUser.lastName.orEmpty() != normalizedLastName ||
                currentUser.activityLevel != activityLevel ||
                currentUser.targetWeightKg != targetWeightKg ||
                currentUser.goalTypeId != goalTypeId
        val isWeightChanged = currentUser.weightKg != weightKg

        if (!isProfileChanged && !isWeightChanged) {
            return Resource.Success(currentUser)
        }

        val profileRequest = UpdateMyProfileRequestDto(
            firstName = normalizedFirstName,
            lastName = normalizedLastName,
            birthDate = currentUser.birthDate,
            heightCm = currentUser.heightCm,
            gender = currentUser.gender?.toServerGender(),
            activityLevel = activityLevel.toServerActivityLevel(),
            targetWeightKg = targetWeightKg,
            dietType = currentUser.dietType,
            goalTypeId = goalTypeId,
            allergies = currentUser.allergies,
            excludedProductsIds = currentUser.excludedProductsIds,
            favoriteCuisines = currentUser.favoriteCuisines,
            dislikedCuisines = currentUser.dislikedCuisines,
            onboardingCompleted = currentUser.onboardingCompleted
        )

        if (isProfileChanged) {
            val profileResult = safeApiCall<UserResponseDto> {
                api.updateMyProfile(profileRequest)
            }

            if (profileResult is Resource.Error) {
                return Resource.Error(profileResult.message ?: context.getString(R.string.repository_error_update_profile))
            }
        }

        if (isWeightChanged) {
            val weightResult = safeApiCall<JsonElement> {
                api.updateCurrentWeight(UpdateWeightRequestDto(weight = weightKg))
            }
            if (weightResult is Resource.Error) {
                return Resource.Error(weightResult.message ?: context.getString(R.string.repository_error_update_weight))
            }
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            weightDao.insertWeightLog(WeightEntity(weightKg = weightKg, date = dateStr))
        }

        val refreshedProfile = safeApiCall<UserResponseDto> {
            api.getMyProfile()
        }

        return when (refreshedProfile) {
            is Resource.Success -> {
                val profile = refreshedProfile.data!!
                tokenManager.syncFoodPreferencesFromProfile(profile)
                Resource.Success(profile)
            }
            is Resource.Error -> Resource.Error(refreshedProfile.message ?: context.getString(R.string.repository_error_refresh_profile))
            is Resource.Loading -> Resource.Loading()
        }
    }

    override suspend fun syncFoodPreferences(
        currentUser: UserResponseDto,
        allergies: List<String>,
        excludedProductsIds: List<Long>,
        favoriteCuisines: List<String>,
        dislikedCuisines: List<String>
    ): Resource<UserResponseDto> {
        val normalizedAllergies = allergies
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }
        val normalizedExcludedProductsIds = excludedProductsIds.distinct()
        val normalizedFavoriteCuisines = favoriteCuisines
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }
        val normalizedDislikedCuisines = dislikedCuisines
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }
            .filterNot { disliked ->
                normalizedFavoriteCuisines.any { it.equals(disliked, ignoreCase = true) }
            }

        val profileRequest = UpdateMyProfileRequestDto(
            firstName = currentUser.firstName,
            lastName = currentUser.lastName,
            birthDate = currentUser.birthDate,
            heightCm = currentUser.heightCm,
            gender = currentUser.gender?.toServerGender(),
            activityLevel = currentUser.serverActivityLevelOrDefault(),
            targetWeightKg = currentUser.targetWeightKg,
            dietType = currentUser.dietType,
            goalTypeId = currentUser.goalTypeId,
            allergies = normalizedAllergies,
            excludedProductsIds = normalizedExcludedProductsIds,
            favoriteCuisines = normalizedFavoriteCuisines,
            dislikedCuisines = normalizedDislikedCuisines,
            onboardingCompleted = currentUser.onboardingCompleted
        )

        val result = safeApiCall<UserResponseDto> {
            api.updateMyProfile(profileRequest)
        }

        return when (result) {
            is Resource.Success -> {
                val profile = mergePreferenceEcho(
                    profile = result.data!!,
                    requestedAllergies = normalizedAllergies,
                    requestedExcludedProductsIds = normalizedExcludedProductsIds,
                    requestedFavoriteCuisines = normalizedFavoriteCuisines,
                    requestedDislikedCuisines = normalizedDislikedCuisines
                )
                tokenManager.syncFoodPreferencesFromProfile(profile)
                Resource.Success(profile)
            }
            is Resource.Error -> Resource.Error(result.message ?: context.getString(R.string.repository_error_update_profile))
            is Resource.Loading -> Resource.Loading()
        }
    }

    override suspend fun completeOnboarding(
        currentUser: UserResponseDto,
        firstName: String,
        lastName: String,
        birthDate: String,
        gender: String,
        heightCm: Double,
        weightKg: Double,
        targetWeightKg: Double,
        activityLevel: String,
        dietType: String,
        allergies: List<String>,
        favoriteCuisines: List<String>,
        dislikedCuisines: List<String>
    ): Resource<UserResponseDto> {
        val normalizedAllergies = allergies
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }
        val normalizedFavoriteCuisines = favoriteCuisines
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }
        val normalizedDislikedCuisines = dislikedCuisines
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }
            .filterNot { disliked ->
                normalizedFavoriteCuisines.any { it.equals(disliked, ignoreCase = true) }
            }

        val profileRequest = UpdateMyProfileRequestDto(
            firstName = firstName.trim(),
            lastName = lastName.trim(),
            birthDate = birthDate,
            heightCm = heightCm,
            targetWeightKg = targetWeightKg,
            activityLevel = activityLevel.toServerActivityLevel(),
            dietType = dietType,
            goalTypeId = currentUser.goalTypeId,
            gender = gender.toServerGender(),
            allergies = normalizedAllergies,
            excludedProductsIds = currentUser.excludedProductsIds,
            favoriteCuisines = normalizedFavoriteCuisines,
            dislikedCuisines = normalizedDislikedCuisines,
            onboardingCompleted = true
        )

        val profileResult = safeApiCall<UserResponseDto> {
            api.updateMyProfile(profileRequest)
        }
        if (profileResult is Resource.Error) {
            return Resource.Error(profileResult.message ?: context.getString(R.string.repository_error_update_profile))
        }

        val weightResult = safeApiCall<JsonElement> {
            api.updateCurrentWeight(UpdateWeightRequestDto(weight = weightKg))
        }
        if (weightResult is Resource.Error) {
            return Resource.Error(weightResult.message ?: context.getString(R.string.repository_error_update_weight))
        }

        val refreshedProfile = safeApiCall<UserResponseDto> {
            api.getMyProfile()
        }

        return when (refreshedProfile) {
            is Resource.Success -> {
                val profile = mergePreferenceEcho(
                    profile = refreshedProfile.data!!,
                    requestedAllergies = normalizedAllergies,
                    requestedFavoriteCuisines = normalizedFavoriteCuisines,
                    requestedDislikedCuisines = normalizedDislikedCuisines
                )
                tokenManager.syncFoodPreferencesFromProfile(profile)
                Resource.Success(profile)
            }
            is Resource.Error -> Resource.Error(refreshedProfile.message ?: context.getString(R.string.repository_error_refresh_profile))
            is Resource.Loading -> Resource.Loading()
        }
    }

    override suspend fun updateWeight(weightKg: Double): Resource<Unit> {
        val result = safeApiCall<JsonElement> {
            api.updateCurrentWeight(UpdateWeightRequestDto(weight = weightKg))
        }

        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        weightDao.insertWeightLog(WeightEntity(weightKg = weightKg, date = dateStr))

        return when (result) {
            is Resource.Success -> Resource.Success(Unit)
            is Resource.Error -> Resource.Error(result.message ?: context.getString(R.string.repository_error_update_weight))
            is Resource.Loading -> Resource.Loading()
        }
    }

    override suspend fun logWeightHistory(weightKg: Double, date: String): Resource<Unit> {
        val result = safeApiCall<JsonElement> {
            api.logWeight(WeightLogRequestDto(weight = weightKg, date = date))
        }

        weightDao.insertWeightLog(WeightEntity(weightKg = weightKg, date = date))

        return when (result) {
            is Resource.Success -> Resource.Success(Unit)
            is Resource.Error -> Resource.Error(result.message ?: context.getString(R.string.repository_error_log_weight_history))
            is Resource.Loading -> Resource.Loading()
        }
    }

    override suspend fun logWater(amountMl: Int, date: String): Resource<Unit> {
        val result = safeApiCall<JsonElement> {
            api.logWater(WaterLogRequestDto(amount = amountMl, date = date))
        }

        waterDao.insertWaterLog(WaterEntity(amountMl = amountMl, date = date))

        return when (result) {
            is Resource.Success -> Resource.Success(Unit)
            is Resource.Error -> Resource.Error(result.message ?: context.getString(R.string.repository_error_log_water))
            is Resource.Loading -> Resource.Loading()
        }
    }

    override suspend fun getWaterToday(): Resource<Int> {
        val result = safeApiCall<TodayWaterResponseDto> {
            api.getWaterToday()
        }
        return when (result) {
            is Resource.Success -> Resource.Success(result.data?.amountMl ?: 0)
            is Resource.Error -> Resource.Error(result.message ?: context.getString(R.string.repository_error_get_water))
            is Resource.Loading -> Resource.Loading()
        }
    }

    override fun getWeightHistory(): Flow<List<WeightEntity>> = weightDao.getAllWeightLogs()

    override fun getWeeklyWaterStats(): Flow<List<WaterStat>> = waterDao.getWeeklyWaterStats()
}
