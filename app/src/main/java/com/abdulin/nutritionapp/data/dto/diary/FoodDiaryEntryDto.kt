package com.abdulin.nutritionapp.data.dto.diary

import kotlinx.serialization.Serializable

@Serializable
data class FoodDiaryEntryDto(
    val id: Long,
    val mealType: String,
    val source: String,
    val consumedAt: String,
    val entryDate: String? = null,
    val productId: Long? = null,
    val productName: String? = null,
    val recipeId: Long? = null,
    val recipeName: String? = null,
    val customName: String? = null,
    val imageUrl: String? = null,
    val weightGrams: Double,
    val calories: Double? = 0.0,
    val protein: Double? = 0.0,
    val fat: Double? = 0.0,
    val carbs: Double? = 0.0
)
