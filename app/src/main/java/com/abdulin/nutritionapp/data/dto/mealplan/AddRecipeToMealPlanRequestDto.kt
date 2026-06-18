package com.abdulin.nutritionapp.data.dto.mealplan

import kotlinx.serialization.Serializable

@Serializable
data class AddRecipeToMealPlanRequestDto(
    val recipeId: Long,
    val planDate: String,
    val mealType: String,
    val portionSize: Double = 1.0
)
