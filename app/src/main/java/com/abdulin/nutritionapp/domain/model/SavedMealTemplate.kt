package com.abdulin.nutritionapp.domain.model

data class SavedMealTemplate(
    val id: Long,
    val templateName: String,
    val mealType: String,
    val source: String,
    val productId: Long? = null,
    val recipeId: Long? = null,
    val customName: String? = null,
    val calories: Double? = null,
    val protein: Double? = null,
    val fat: Double? = null,
    val carbs: Double? = null,
    val weightGrams: Double
)
