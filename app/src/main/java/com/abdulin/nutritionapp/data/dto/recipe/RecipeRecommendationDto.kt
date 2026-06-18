package com.abdulin.nutritionapp.data.dto.recipe

import kotlinx.serialization.Serializable

@Serializable
data class RecipeRecommendationDto(
    val impressionId: Long? = null,
    val recipeId: Long? = null,
    val score: Double? = null,
    val ruleScore: Double? = null,
    val mlScore: Double? = null,
    val reason: String? = null,
    val experimentVariant: String? = null,
    val explanationTags: List<String> = emptyList(),
    val recipe: RecipeDto? = null
)
