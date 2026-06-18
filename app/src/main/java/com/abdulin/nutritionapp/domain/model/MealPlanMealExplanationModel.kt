package com.abdulin.nutritionapp.domain.model

data class MealPlanMealExplanationModel(
    val planRecipeId: Long? = null,
    val recommendationImpressionId: Long? = null,
    val mealType: String? = null,
    val reason: String? = null,
    val preferenceReason: String? = null,
    val finalScore: Double? = null,
    val ruleScore: Double? = null,
    val mlScore: Double? = null,
    val coveragePercent: Double? = null,
    val selectedProductCount: Int? = null,
    val matchedSelectedProductCount: Int? = null,
    val pantryProductCount: Int? = null,
    val tags: List<String> = emptyList()
)
