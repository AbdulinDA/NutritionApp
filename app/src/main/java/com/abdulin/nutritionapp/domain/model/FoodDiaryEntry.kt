package com.abdulin.nutritionapp.domain.model

data class FoodDiaryEntry(
    val id: Long,
    val mealType: MealType,
    val productName: String,
    val imageUrl: String? = null,
    val weightGrams: Double,
    val calories: Double,
    val protein: Double,
    val fat: Double,
    val carbs: Double,
    val consumedAt: String
)
