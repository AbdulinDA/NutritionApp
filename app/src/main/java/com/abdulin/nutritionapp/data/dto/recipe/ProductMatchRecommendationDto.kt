package com.abdulin.nutritionapp.data.dto.recipe

import kotlinx.serialization.Serializable

@Serializable
data class ProductMatchRequestDto(
    val productIds: List<Long>,
    val mealType: String? = null,
    val limit: Int = 10,
    val maxMissingIngredients: Int = 2
)

@Serializable
data class ProductMatchRecommendationDto(
    val impressionId: Long? = null,
    val recipeId: Long? = null,
    val score: Double? = null,
    val ruleScore: Double? = null,
    val mlScore: Double? = null,
    val reason: String? = null,
    val experimentVariant: String? = null,
    val explanationTags: List<String> = emptyList(),
    val recipe: RecipeDto? = null,
    val coveragePercent: Double? = null,
    val matchedProductIds: List<Long> = emptyList(),
    val missingIngredients: List<MissingIngredientDto> = emptyList()
)

@Serializable
data class MissingIngredientDto(
    val productId: Long? = null,
    val productName: String? = null,
    val quantity: Double? = null,
    val unitCode: String? = null
)
