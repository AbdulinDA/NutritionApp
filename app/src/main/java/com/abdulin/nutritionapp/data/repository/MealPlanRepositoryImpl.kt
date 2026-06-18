package com.abdulin.nutritionapp.data.repository

import android.content.Context
import com.abdulin.nutritionapp.R
import com.abdulin.nutritionapp.core.network.safeApiCall
import com.abdulin.nutritionapp.core.utils.Resource
import com.abdulin.nutritionapp.core.utils.TokenManager
import com.abdulin.nutritionapp.data.dto.mealplan.AddRecipeToMealPlanRequestDto
import com.abdulin.nutritionapp.data.dto.mealplan.MealPlanRequestDto
import com.abdulin.nutritionapp.data.dto.mealplan.MealPlanResponseDto
import com.abdulin.nutritionapp.data.dto.mealplan.ReplaceMealInPlanRequestDto
import com.abdulin.nutritionapp.data.local.dao.MealPlanDao
import com.abdulin.nutritionapp.data.mapper.toDomain
import com.abdulin.nutritionapp.data.mapper.toEntity
import com.abdulin.nutritionapp.data.remote.NutritionApi
import com.abdulin.nutritionapp.domain.model.MealPlanMealExplanationModel
import com.abdulin.nutritionapp.domain.model.MealPlanModel
import com.abdulin.nutritionapp.domain.model.RecipeCompositionModel
import com.abdulin.nutritionapp.domain.model.ShoppingListItem
import com.abdulin.nutritionapp.domain.repository.MealPlanRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeParseException
import javax.inject.Inject

class MealPlanRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: NutritionApi,
    private val mealPlanDao: MealPlanDao,
    private val tokenManager: TokenManager
) : MealPlanRepository {

    override suspend fun generateMealPlan(request: MealPlanRequestDto): Resource<MealPlanModel> {
        val response = safeApiCall { api.generateMealPlan(request) }

        return when (response) {
            is Resource.Success -> {
                val data = response.data!!
                savePlanLocally(data)
                data.id?.let { tokenManager.saveLastMealPlanId(it) }
                Resource.Success(data.toDomain())
            }

            is Resource.Error -> Resource.Error(response.message ?: context.getString(R.string.meal_plan_error_generate))
            is Resource.Loading -> Resource.Loading()
        }
    }

    override suspend fun refreshLatestPlan(): Resource<MealPlanModel> {
        val response = safeApiCall { api.getLatestMealPlan() }

        return when (response) {
            is Resource.Success -> {
                val data = response.data!!
                savePlanLocally(data)
                data.id?.let { tokenManager.saveLastMealPlanId(it) }
                Resource.Success(data.toDomain())
            }

            is Resource.Error -> {
                if (isPlanMissingError(response.message)) {
                    clearLocalPlan()
                }
                Resource.Error(response.message ?: context.getString(R.string.meal_plan_error_load))
            }
            is Resource.Loading -> Resource.Loading()
        }
    }

    override suspend fun removeMealFromPlan(planRecipeId: Long): Resource<MealPlanModel> {
        val response = safeApiCall { api.removeMealFromPlan(planRecipeId) }
        return persistLatestPlanResponse(response, R.string.meal_plan_error_load)
    }

    override suspend fun replaceMealInPlan(planRecipeId: Long, reason: String): Resource<MealPlanModel> {
        val response = safeApiCall {
            api.replaceMealInPlan(
                planRecipeId = planRecipeId,
                request = ReplaceMealInPlanRequestDto(reason = reason)
            )
        }
        return persistLatestPlanResponse(response, R.string.meal_plan_error_load)
    }

    override suspend fun dislikeMealFromPlan(planRecipeId: Long): Resource<MealPlanModel> {
        val response = safeApiCall { api.dislikeMealFromPlan(planRecipeId) }
        return persistLatestPlanResponse(response, R.string.meal_plan_error_load)
    }

    override suspend fun toggleMealPin(planRecipeId: Long): Resource<MealPlanModel> {
        val response = safeApiCall { api.toggleMealPin(planRecipeId) }
        return persistLatestPlanResponse(response, R.string.meal_plan_error_load)
    }

    override suspend fun addRecipeToMealPlan(
        recipeId: Long,
        planDate: String,
        mealType: String,
        portionSize: Double
    ): Resource<MealPlanModel> {
        val response = safeApiCall {
            api.addRecipeToMealPlan(
                AddRecipeToMealPlanRequestDto(
                    recipeId = recipeId,
                    planDate = planDate,
                    mealType = mealType,
                    portionSize = portionSize
                )
            )
        }
        return persistLatestPlanResponse(response, R.string.meal_plan_error_load)
    }

    override suspend fun replanRemainingDay(): Resource<MealPlanModel> {
        val response = safeApiCall { api.replanRemainingDay() }
        return persistLatestPlanResponse(response, R.string.meal_plan_error_load)
    }

    override suspend fun fillEmptySlotsForDay(planDate: String): Resource<MealPlanModel> {
        val response = safeApiCall { api.fillEmptySlotsForDay(planDate) }
        return persistLatestPlanResponse(response, R.string.meal_plan_error_load)
    }

    override suspend fun replanDay(planDate: String): Resource<MealPlanModel> {
        val response = safeApiCall { api.replanDay(planDate) }
        return persistLatestPlanResponse(response, R.string.meal_plan_error_load)
    }

    override suspend fun replanUpcomingDays(days: Int): Resource<MealPlanModel> {
        val response = safeApiCall { api.replanUpcomingDays(days) }
        return persistLatestPlanResponse(response, R.string.meal_plan_error_load)
    }

    override suspend fun repeatDayToNext(planDate: String): Resource<MealPlanModel> {
        val response = safeApiCall { api.repeatDayToNext(planDate) }
        return persistLatestPlanResponse(response, R.string.meal_plan_error_load)
    }

    override suspend fun getMealComposition(planRecipeId: Long): Resource<RecipeCompositionModel> {
        val response = safeApiCall { api.getMealPlanMealComposition(planRecipeId) }
        return when (response) {
            is Resource.Success -> {
                val composition = response.data?.toDomain()
                if (composition != null) {
                    Resource.Success(composition)
                } else {
                    Resource.Error(context.getString(R.string.meal_plan_composition_error))
                }
            }
            is Resource.Error -> Resource.Error(response.message ?: context.getString(R.string.meal_plan_composition_error))
            is Resource.Loading -> Resource.Loading()
        }
    }

    override suspend fun getMealExplanation(planRecipeId: Long): Resource<MealPlanMealExplanationModel> {
        val response = safeApiCall { api.getMealPlanMealExplanation(planRecipeId) }
        return when (response) {
            is Resource.Success -> {
                val explanation = response.data?.toDomain()
                if (explanation != null) {
                    Resource.Success(explanation)
                } else {
                    Resource.Error(context.getString(R.string.meal_plan_explanation_error))
                }
            }
            is Resource.Error -> Resource.Error(response.message ?: context.getString(R.string.meal_plan_explanation_error))
            is Resource.Loading -> Resource.Loading()
        }
    }

    override suspend fun getShoppingList(planId: Long): Resource<List<ShoppingListItem>> {
        val response = safeApiCall { api.getShoppingList(planId) }
        return when (response) {
            is Resource.Success -> Resource.Success(response.data?.map { it.toDomain() } ?: emptyList())
            is Resource.Error -> Resource.Error(response.message ?: context.getString(R.string.meal_plan_error_load_shopping_list))
            is Resource.Loading -> Resource.Loading()
        }
    }

    override suspend fun getLatestShoppingList(): Resource<List<ShoppingListItem>> {
        val response = safeApiCall { api.getLatestShoppingList() }
        return when (response) {
            is Resource.Success -> Resource.Success(response.data?.map { it.toDomain() } ?: emptyList())
            is Resource.Error -> Resource.Error(response.message ?: context.getString(R.string.meal_plan_error_load_shopping_list))
            is Resource.Loading -> Resource.Loading()
        }
    }

    override fun getLatestPlan(): Flow<MealPlanModel?> {
        return mealPlanDao.getLatestPlan().map { entity ->
            val plan = entity?.toDomain()
            if (plan == null || isPlanExpired(plan)) {
                null
            } else {
                plan
            }
        }
    }

    private suspend fun savePlanLocally(plan: MealPlanResponseDto) {
        mealPlanDao.clearPlans()
        mealPlanDao.insertPlan(plan.toEntity())
    }

    private suspend fun clearLocalPlan() {
        mealPlanDao.clearPlans()
    }

    private suspend fun persistLatestPlanResponse(
        response: Resource<MealPlanResponseDto>,
        errorResId: Int
    ): Resource<MealPlanModel> {
        return when (response) {
            is Resource.Success -> {
                val data = response.data!!
                if (data.days.isEmpty()) {
                    clearLocalPlan()
                    Resource.Success(MealPlanModel(id = 0L, days = emptyList()))
                } else {
                    savePlanLocally(data)
                    data.id?.let { tokenManager.saveLastMealPlanId(it) }
                    Resource.Success(data.toDomain())
                }
            }
            is Resource.Error -> {
                if (isPlanMissingError(response.message)) {
                    clearLocalPlan()
                }
                Resource.Error(response.message ?: context.getString(errorResId))
            }
            is Resource.Loading -> Resource.Loading()
        }
    }

    private fun isPlanExpired(plan: MealPlanModel): Boolean {
        val latestDate = plan.days.maxOfOrNull { day ->
            try {
                LocalDate.parse(day.date)
            } catch (_: DateTimeParseException) {
                LocalDate.MIN
            }
        } ?: return true
        return latestDate.isBefore(LocalDate.now())
    }

    private fun isPlanMissingError(message: String?): Boolean {
        val normalized = message?.lowercase().orEmpty()
        return normalized.contains("meal plan not found")
                || normalized.contains("план питания не найден")
                || normalized.contains("not found")
    }
}
