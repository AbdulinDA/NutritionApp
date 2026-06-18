package com.abdulin.nutritionapp.data.mapper

import com.abdulin.nutritionapp.core.utils.shouldHideRecipeTitleFromApp
import com.abdulin.nutritionapp.data.dto.home.HomeResponseDto
import com.abdulin.nutritionapp.domain.model.HomeModel
import com.abdulin.nutritionapp.domain.model.RecipeShortModel

fun HomeResponseDto.toDomain(): HomeModel {
    val resolvedWeight = currentWeight ?: 0.0
    return HomeModel(
        calories = macros.totalCalories,
        protein = macros.totalProtein,
        fat = macros.totalFat,
        carbs = macros.totalCarbs,
        water = waterProgress,
        weight = resolvedWeight,
        todayMeals = todayMeals.map { it.toDomain() },
        aiMessage = aiMessage,
        recommendations = recommendations.mapNotNull {
            val recipe = it.recipe
            val id = recipe?.id
            val title = recipe?.title
            if (id != null && title != null && !shouldHideRecipeTitleFromApp(title)) {
                RecipeShortModel(
                    id = id,
                    title = title,
                    imageUrl = recipe.imageUrl,
                    totalCalories = recipe.totalCalories ?: 0.0,
                    totalProtein = recipe.totalProtein ?: 0.0,
                    totalFat = recipe.totalFat ?: 0.0,
                    totalCarbs = recipe.totalCarbs ?: 0.0,
                    recommendationImpressionId = it.impressionId,
                    recommendationReason = it.reason
                )
            } else {
                null
            }
        }
    )
}
