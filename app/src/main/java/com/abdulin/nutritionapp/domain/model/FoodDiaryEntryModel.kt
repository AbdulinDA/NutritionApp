package com.abdulin.nutritionapp.domain.model

data class FoodDiaryEntryModel(

    val id: Long,

    val mealType: String,

    val productName: String,

    val calories: Double,

    val protein: Double,

    val fat: Double,

    val carbs: Double,

    val weightGrams: Double,

    val consumedAt: String
)