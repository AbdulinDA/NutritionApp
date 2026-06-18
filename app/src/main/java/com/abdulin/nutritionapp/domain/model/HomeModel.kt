package com.abdulin.nutritionapp.domain.model

data class HomeModel(
    val calories: Double,
    val protein: Double,
    val fat: Double,
    val carbs: Double,
    val targetCalories: Double = 2000.0,
    val targetProtein: Double = 150.0,
    val targetFat: Double = 70.0,
    val targetCarbs: Double = 250.0,
    val water: Int,
    val targetWater: Int = 2000,
    val weight: Double,
    val streak: Int = 0,
    val todayMeals: List<FoodDiaryEntry> = emptyList(),
    val recommendations: List<RecipeShortModel> = emptyList(),
    val aiMessage: String? = null
)

data class RecipeShortModel(
    val id: Long,
    val title: String,
    val imageUrl: String?,
    val cuisineType: String? = null,
    val servingsCount: Int? = null,
    val totalCalories: Double = 0.0,
    val totalProtein: Double = 0.0,
    val totalFat: Double = 0.0,
    val totalCarbs: Double = 0.0,
    val nutritionCalculationStatus: String? = null,
    val recommendationImpressionId: Long? = null,
    val recommendationReason: String? = null
)
