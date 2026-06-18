package com.abdulin.nutritionapp.presentation.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdulin.nutritionapp.R
import com.abdulin.nutritionapp.core.utils.Resource
import com.abdulin.nutritionapp.data.dto.auth.UserResponseDto
import com.abdulin.nutritionapp.domain.repository.UserRepository
import com.abdulin.nutritionapp.domain.usecase.CalculateNutritionTargetsUseCase
import com.abdulin.nutritionapp.domain.usecase.GetMyProfileUseCase
import com.abdulin.nutritionapp.domain.usecase.NutritionTargets
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private val OnboardingAvailableCuisines = listOf(
    "RUSSIAN",
    "ITALIAN",
    "ASIAN",
    "MEDITERRANEAN",
    "MEXICAN",
    "GEORGIAN"
)

private val OnboardingCommonAllergies = listOf(
    "Milk",
    "Eggs",
    "Gluten",
    "Nuts",
    "Peanuts",
    "Fish",
    "Seafood",
    "Soy"
)

data class OnboardingState(
    val currentStep: Int = 0,
    val gender: String = "MALE",
    val height: Double = 175.0,
    val weight: Double = 75.0,
    val targetWeight: Double = 70.0,
    val activityLevel: String = "MODERATE",
    val dietType: String = "CLASSIC",
    val allergies: Set<String> = emptySet(),
    val favoriteCuisines: Set<String> = emptySet(),
    val dislikedCuisines: Set<String> = emptySet(),
    val birthDate: String = "1995-01-01",
    val calculatedTargets: NutritionTargets? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isCompleted: Boolean = false,
    val profileLoaded: Boolean = false,
    val currentUser: UserResponseDto? = null
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val getMyProfileUseCase: GetMyProfileUseCase,
    private val userRepository: UserRepository,
    private val calculateTargetsUseCase: CalculateNutritionTargetsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state = _state.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = getMyProfileUseCase()) {
                is Resource.Success -> {
                    val user = result.data
                    if (user == null) {
                        _state.update { it.copy(isLoading = false, error = context.getString(R.string.repository_error_load_profile)) }
                        return@launch
                    }
                    _state.value = OnboardingState(
                        gender = normalizeUiGender(user.gender),
                        height = user.heightCm ?: 175.0,
                        weight = user.weightKg ?: 75.0,
                        targetWeight = user.targetWeightKg ?: user.weightKg ?: 75.0,
                        activityLevel = normalizeUiActivity(user.activityLevel),
                        dietType = user.dietType ?: "CLASSIC",
                        allergies = user.allergies.toSet(),
                        favoriteCuisines = user.favoriteCuisines.toSet(),
                        dislikedCuisines = user.dislikedCuisines.toSet(),
                        birthDate = user.birthDate ?: "1995-01-01",
                        isLoading = false,
                        profileLoaded = true,
                        currentUser = user
                    )
                    updateCalculatedTargets()
                }
                is Resource.Error -> _state.update {
                    it.copy(isLoading = false, error = result.message ?: context.getString(R.string.repository_error_load_profile))
                }
                is Resource.Loading -> _state.update { it.copy(isLoading = true) }
            }
        }
    }

    fun nextStep() {
        if (_state.value.currentStep < 4) {
            _state.update { it.copy(currentStep = it.currentStep + 1, error = null) }
            updateCalculatedTargets()
        }
    }

    fun prevStep() {
        if (_state.value.currentStep > 0) {
            _state.update { it.copy(currentStep = it.currentStep - 1, error = null) }
        }
    }

    fun updateData(
        birthDate: String? = null,
        gender: String? = null,
        height: Double? = null,
        weight: Double? = null,
        targetWeight: Double? = null,
        activityLevel: String? = null,
        dietType: String? = null
    ) {
        _state.update {
            it.copy(
                birthDate = birthDate ?: it.birthDate,
                gender = gender ?: it.gender,
                height = height ?: it.height,
                weight = weight ?: it.weight,
                targetWeight = targetWeight ?: it.targetWeight,
                activityLevel = activityLevel ?: it.activityLevel,
                dietType = dietType ?: it.dietType,
                error = null
            )
        }
        updateCalculatedTargets()
    }

    fun toggleFavoriteCuisine(cuisine: String) {
        if (!OnboardingAvailableCuisines.contains(cuisine)) return
        _state.update { state ->
            val updated = state.favoriteCuisines.toMutableSet()
            if (!updated.add(cuisine)) updated.remove(cuisine)
            state.copy(
                favoriteCuisines = updated,
                dislikedCuisines = state.dislikedCuisines.filterNot { it.equals(cuisine, ignoreCase = true) }.toSet(),
                error = null
            )
        }
    }

    fun toggleAllergy(allergy: String) {
        if (!OnboardingCommonAllergies.contains(allergy)) return
        _state.update { state ->
            val updated = state.allergies.toMutableSet()
            if (!updated.add(allergy)) updated.remove(allergy)
            state.copy(allergies = updated, error = null)
        }
    }

    fun addCustomAllergy(value: String) {
        val normalized = value.trim()
        if (normalized.isBlank()) return
        _state.update { state ->
            val updated = state.allergies.toMutableSet()
            val existing = updated.firstOrNull { it.equals(normalized, ignoreCase = true) }
            if (existing == null) updated.add(normalized)
            state.copy(allergies = updated, error = null)
        }
    }

    fun removeAllergy(value: String) {
        _state.update { state ->
            state.copy(allergies = state.allergies.filterNot { it.equals(value, ignoreCase = true) }.toSet(), error = null)
        }
    }

    fun toggleDislikedCuisine(cuisine: String) {
        if (!OnboardingAvailableCuisines.contains(cuisine)) return
        _state.update { state ->
            val updated = state.dislikedCuisines.toMutableSet()
            if (!updated.add(cuisine)) updated.remove(cuisine)
            state.copy(
                dislikedCuisines = updated,
                favoriteCuisines = state.favoriteCuisines.filterNot { it.equals(cuisine, ignoreCase = true) }.toSet(),
                error = null
            )
        }
    }

    fun completeOnboarding() {
        val snapshot = _state.value
        val currentUser = snapshot.currentUser
        if (currentUser == null) {
            _state.update { it.copy(error = context.getString(R.string.repository_error_load_profile)) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (
                val result = userRepository.completeOnboarding(
                    currentUser = currentUser,
                    firstName = currentUser.firstName.ifBlank { context.getString(R.string.auth_default_first_name) },
                    lastName = currentUser.lastName.orEmpty().ifBlank { context.getString(R.string.auth_default_last_name) },
                    birthDate = snapshot.birthDate,
                    gender = snapshot.gender,
                    heightCm = snapshot.height,
                    weightKg = snapshot.weight,
                    targetWeightKg = snapshot.targetWeight,
                    activityLevel = snapshot.activityLevel,
                    dietType = snapshot.dietType,
                    allergies = snapshot.allergies.toList(),
                    favoriteCuisines = snapshot.favoriteCuisines.toList(),
                    dislikedCuisines = snapshot.dislikedCuisines.toList()
                )
            ) {
                is Resource.Success -> _state.update {
                    it.copy(isLoading = false, isCompleted = true, currentUser = result.data ?: currentUser)
                }
                is Resource.Error -> _state.update { it.copy(isLoading = false, error = result.message) }
                is Resource.Loading -> _state.update { it.copy(isLoading = true) }
            }
        }
    }

    private fun updateCalculatedTargets() {
        val s = _state.value
        val resolvedFirstName = s.currentUser?.firstName?.ifBlank { context.getString(R.string.auth_default_first_name) }
            ?: context.getString(R.string.auth_default_first_name)
        val resolvedLastName = s.currentUser?.lastName.orEmpty().ifBlank { context.getString(R.string.auth_default_last_name) }

        val mockUser = UserResponseDto(
            userId = s.currentUser?.userId ?: 0,
            email = s.currentUser?.email.orEmpty(),
            firstName = resolvedFirstName,
            lastName = resolvedLastName,
            birthDate = s.birthDate,
            gender = s.gender,
            heightCm = s.height,
            weightKg = s.weight,
            targetWeightKg = s.targetWeight,
            activityLevel = s.activityLevel,
            dietType = s.dietType
        )
        _state.update { it.copy(calculatedTargets = calculateTargetsUseCase(mockUser)) }
    }

    private fun normalizeUiGender(value: String?): String {
        return when (value?.trim()?.uppercase()) {
            "FEMALE", "F" -> "FEMALE"
            else -> "MALE"
        }
    }

    private fun normalizeUiActivity(value: String?): String {
        return when (value?.trim()?.uppercase()?.replace('-', '_')) {
            "SEDENTARY" -> "SEDENTARY"
            "LIGHT" -> "LIGHT"
            "ACTIVE" -> "ACTIVE"
            "VERY_ACTIVE" -> "VERY_ACTIVE"
            else -> "MODERATE"
        }
    }
}
