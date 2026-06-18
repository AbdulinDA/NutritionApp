package com.abdulin.nutritionapp.domain.model

data class MealPlanModel(
    val id: Long,
    val householdSize: Int = 1,
    val mealPrepServings: Int = 1,
    val days: List<MealPlanDayModel>
)

data class MealPlanDayModel(
    val date: String,
    val meals: List<MealPlanMealModel>
)

data class MealPlanMealModel(
    val planRecipeId: Long,
    val mealType: MealType,
    val recipe: RecipeShortModel,
    val portionSize: Double = 1.0,
    val recommendationImpressionId: Long? = null,
    val selectionReason: String? = null,
    val isPinned: Boolean = false,
    val isMealPrepCarryover: Boolean = false
)

data class ShoppingListItem(
    val productId: Long,
    val productName: String,
    val category: String,
    val totalQuantity: Double,
    val unit: String,
    val isManual: Boolean = false
)
