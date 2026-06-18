package com.abdulin.nutritionapp.domain.model

data class RecommendationReportModel(
    val totalImpressions: Int = 0,
    val totalOpened: Int = 0,
    val totalLogged: Int = 0,
    val openRate: Double = 0.0,
    val logRate: Double = 0.0,
    val byContext: List<RecommendationReportSliceModel> = emptyList(),
    val byVariant: List<RecommendationReportSliceModel> = emptyList(),
    val topRecipes: List<RecommendationTopRecipeModel> = emptyList()
)

data class RecommendationReportSliceModel(
    val key: String,
    val impressions: Int = 0,
    val opened: Int = 0,
    val logged: Int = 0,
    val openRate: Double = 0.0,
    val logRate: Double = 0.0
)

data class RecommendationTopRecipeModel(
    val recipeId: Long,
    val recipeName: String,
    val impressions: Int = 0,
    val opened: Int = 0,
    val logged: Int = 0,
    val openRate: Double = 0.0,
    val logRate: Double = 0.0
)
