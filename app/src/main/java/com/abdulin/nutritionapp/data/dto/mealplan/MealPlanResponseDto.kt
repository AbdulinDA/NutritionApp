package com.abdulin.nutritionapp.data.dto.mealplan

import com.abdulin.nutritionapp.data.dto.recipe.RecipeDto
import kotlinx.serialization.Serializable

@Serializable
data class MealPlanResponseDto(
    val id: Long? = null,
    val publicId: String? = null,
    val householdSize: Int = 1,
    val mealPrepServings: Int = 1,
    val days: List<MealPlanDayDto>
)

@Serializable
data class MealPlanDayDto(
    val date: String,
    val meals: List<MealPlanEntryDto>
)

@Serializable
data class MealPlanEntryDto(
    val planRecipeId: Long? = null,
    val mealType: String,
    val recipe: RecipeDto,
    val portionSize: Double = 1.0,
    val recommendationImpressionId: Long? = null,
    val selectionReason: String? = null,
    val pinned: Boolean = false,
    val mealPrepCarryover: Boolean = false
)
