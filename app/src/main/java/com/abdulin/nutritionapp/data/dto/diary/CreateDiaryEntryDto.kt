package com.abdulin.nutritionapp.data.dto.diary

import kotlinx.serialization.Serializable

@Serializable
data class CreateDiaryEntryDto(
    val mealType: String,
    val source: String,
    val productId: Long? = null,
    val recipeId: Long? = null,
    val sideDishRecipeId: Long? = null,
    val sideDishPortionMultiplier: Double? = null,
    val planRecipeId: Long? = null,
    val customName: String? = null,
    val calories: Double? = null,
    val protein: Double? = null,
    val fat: Double? = null,
    val carbs: Double? = null,
    val weightGrams: Double,
    val entryDate: String,
    val consumedAt: String,
    val idempotencyKey: String? = null
)
