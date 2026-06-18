package com.abdulin.nutritionapp.data.dto.mealplan

import kotlinx.serialization.Serializable

@Serializable
data class MealPlanRequestDto(
    val days: Int,
    val dailyCalories: Int,
    val goalTypeId: Long? = null,
    val startDate: String? = null,
    val householdSize: Int = 1,
    val mealPrepServings: Int = 1,
    val mealPrepMealTypes: List<String> = emptyList(),
    val selectedProductIds: List<Long> = emptyList(),
    val preferredCuisines: List<String> = emptyList()
)
