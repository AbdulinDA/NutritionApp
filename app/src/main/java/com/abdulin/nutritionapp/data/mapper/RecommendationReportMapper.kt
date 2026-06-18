package com.abdulin.nutritionapp.data.mapper

import com.abdulin.nutritionapp.data.dto.analytics.RecommendationReportDto
import com.abdulin.nutritionapp.domain.model.RecommendationReportModel
import com.abdulin.nutritionapp.domain.model.RecommendationReportSliceModel
import com.abdulin.nutritionapp.domain.model.RecommendationTopRecipeModel

fun RecommendationReportDto.toDomain(): RecommendationReportModel {
    return RecommendationReportModel(
        totalImpressions = totalImpressions,
        totalOpened = totalOpened,
        totalLogged = totalLogged,
        openRate = openRate,
        logRate = logRate,
        byContext = byContext.map {
            RecommendationReportSliceModel(
                key = it.key,
                impressions = it.impressions,
                opened = it.opened,
                logged = it.logged,
                openRate = it.openRate,
                logRate = it.logRate
            )
        },
        byVariant = byVariant.map {
            RecommendationReportSliceModel(
                key = it.key,
                impressions = it.impressions,
                opened = it.opened,
                logged = it.logged,
                openRate = it.openRate,
                logRate = it.logRate
            )
        },
        topRecipes = topRecipes.map {
            RecommendationTopRecipeModel(
                recipeId = it.recipeId,
                recipeName = it.recipeName,
                impressions = it.impressions,
                opened = it.opened,
                logged = it.logged,
                openRate = it.openRate,
                logRate = it.logRate
            )
        }
    )
}
