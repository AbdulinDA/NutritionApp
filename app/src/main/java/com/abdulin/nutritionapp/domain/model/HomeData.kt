package com.abdulin.nutritionapp.domain.model

data class HomeData(

    val calories: Double,
    val protein: Double,
    val fat: Double,
    val carbs: Double,

    val waterMl: Int,
    val currentWeight: Double,

    val meals: List<FoodEntry>,
    val recommendations: List<Recipe>
)

data class FoodEntry(
    val name: String,
    val calories: Double
)

data class Recipe(
    val id: Long,
    val name: String,
    val imageUrl: String?
)