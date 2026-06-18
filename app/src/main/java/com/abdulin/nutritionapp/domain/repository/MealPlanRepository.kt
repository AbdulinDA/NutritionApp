package com.abdulin.nutritionapp.domain.repository

import com.abdulin.nutritionapp.core.utils.Resource
import com.abdulin.nutritionapp.data.dto.mealplan.MealPlanRequestDto
import com.abdulin.nutritionapp.domain.model.MealPlanMealExplanationModel
import com.abdulin.nutritionapp.domain.model.MealPlanModel
import com.abdulin.nutritionapp.domain.model.RecipeCompositionModel
import com.abdulin.nutritionapp.domain.model.ShoppingListItem
import kotlinx.coroutines.flow.Flow

interface MealPlanRepository {
    suspend fun generateMealPlan(request: MealPlanRequestDto): Resource<MealPlanModel>
    suspend fun refreshLatestPlan(): Resource<MealPlanModel>
    suspend fun removeMealFromPlan(planRecipeId: Long): Resource<MealPlanModel>
    suspend fun replaceMealInPlan(planRecipeId: Long, reason: String): Resource<MealPlanModel>
    suspend fun dislikeMealFromPlan(planRecipeId: Long): Resource<MealPlanModel>
    suspend fun toggleMealPin(planRecipeId: Long): Resource<MealPlanModel>
    suspend fun addRecipeToMealPlan(recipeId: Long, planDate: String, mealType: String, portionSize: Double = 1.0): Resource<MealPlanModel>
    suspend fun replanRemainingDay(): Resource<MealPlanModel>
    suspend fun fillEmptySlotsForDay(planDate: String): Resource<MealPlanModel>
    suspend fun replanDay(planDate: String): Resource<MealPlanModel>
    suspend fun replanUpcomingDays(days: Int): Resource<MealPlanModel>
    suspend fun repeatDayToNext(planDate: String): Resource<MealPlanModel>
    suspend fun getMealComposition(planRecipeId: Long): Resource<RecipeCompositionModel>
    suspend fun getMealExplanation(planRecipeId: Long): Resource<MealPlanMealExplanationModel>
    suspend fun getShoppingList(planId: Long): Resource<List<ShoppingListItem>>
    suspend fun getLatestShoppingList(): Resource<List<ShoppingListItem>>
    fun getLatestPlan(): Flow<MealPlanModel?>
}
