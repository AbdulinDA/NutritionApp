package com.abdulin.nutritionapp.domain.model

data class ProductMatchRecipeModel(
    val recipe: RecipeModel,
    val impressionId: Long? = null,
    val score: Double? = null,
    val ruleScore: Double? = null,
    val mlScore: Double? = null,
    val reason: String? = null,
    val experimentVariant: String? = null,
    val explanationTags: List<String> = emptyList(),
    val coveragePercent: Double? = null,
    val matchedProductIds: List<Long> = emptyList(),
    val missingIngredients: List<MissingIngredientModel> = emptyList()
)

data class MissingIngredientModel(
    val productId: Long? = null,
    val productName: String? = null,
    val quantity: Double? = null,
    val unitCode: String? = null
)
