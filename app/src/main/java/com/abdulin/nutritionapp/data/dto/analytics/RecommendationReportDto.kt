package com.abdulin.nutritionapp.data.dto.analytics

import kotlinx.serialization.Serializable

@Serializable
data class RecommendationReportDto(
    val totalImpressions: Int = 0,
    val totalOpened: Int = 0,
    val totalLogged: Int = 0,
    val openRate: Double = 0.0,
    val logRate: Double = 0.0,
    val byContext: List<RecommendationReportSliceDto> = emptyList(),
    val byVariant: List<RecommendationReportSliceDto> = emptyList(),
    val topRecipes: List<RecommendationTopRecipeDto> = emptyList()
)

@Serializable
data class RecommendationReportSliceDto(
    val key: String,
    val impressions: Int = 0,
    val opened: Int = 0,
    val logged: Int = 0,
    val openRate: Double = 0.0,
    val logRate: Double = 0.0
)

@Serializable
data class RecommendationTopRecipeDto(
    val recipeId: Long,
    val recipeName: String,
    val impressions: Int = 0,
    val opened: Int = 0,
    val logged: Int = 0,
    val openRate: Double = 0.0,
    val logRate: Double = 0.0
)
