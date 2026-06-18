package com.abdulin.nutritionapp.domain.model

data class RecommendedRecipeModel(
    val recipe: RecipeModel,
    val impressionId: Long? = null,
    val reason: String? = null,
    val score: Double? = null,
    val ruleScore: Double? = null,
    val mlScore: Double? = null,
    val experimentVariant: String? = null,
    val explanationTags: List<String> = emptyList()
)
