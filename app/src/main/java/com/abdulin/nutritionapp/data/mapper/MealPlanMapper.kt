package com.abdulin.nutritionapp.data.mapper

import com.abdulin.nutritionapp.core.utils.localizeMeasurementUnit
import com.abdulin.nutritionapp.data.dto.mealplan.MealPlanMealExplanationDto
import com.abdulin.nutritionapp.data.dto.mealplan.MealPlanResponseDto
import com.abdulin.nutritionapp.data.dto.mealplan.ShoppingListItemDto
import com.abdulin.nutritionapp.data.local.entity.MealPlanEntity
import com.abdulin.nutritionapp.domain.model.MealPlanDayModel
import com.abdulin.nutritionapp.domain.model.MealPlanMealExplanationModel
import com.abdulin.nutritionapp.domain.model.MealPlanMealModel
import com.abdulin.nutritionapp.domain.model.MealPlanModel
import com.abdulin.nutritionapp.domain.model.MealType
import com.abdulin.nutritionapp.domain.model.RecipeShortModel
import com.abdulin.nutritionapp.domain.model.ShoppingListItem
import kotlinx.serialization.json.Json

fun MealPlanResponseDto.toDomain(): MealPlanModel {
    return MealPlanModel(
        id = id ?: 0L,
        householdSize = householdSize,
        mealPrepServings = mealPrepServings,
        days = days.map { dayDto ->
            MealPlanDayModel(
                date = dayDto.date,
                meals = dayDto.meals.map { mealDto ->
                    MealPlanMealModel(
                        planRecipeId = mealDto.planRecipeId ?: 0L,
                        mealType = MealType.valueOf(mealDto.mealType),
                        recipe = RecipeShortModel(
                            id = mealDto.recipe.id ?: 0L,
                            title = mealDto.recipe.title ?: "",
                            imageUrl = mealDto.recipe.imageUrl,
                            cuisineType = mealDto.recipe.cuisineType,
                            servingsCount = mealDto.recipe.servingsCount,
                            totalCalories = mealDto.recipe.totalCalories ?: 0.0,
                            totalProtein = mealDto.recipe.totalProtein ?: 0.0,
                            totalFat = mealDto.recipe.totalFat ?: 0.0,
                            totalCarbs = mealDto.recipe.totalCarbs ?: 0.0,
                            nutritionCalculationStatus = mealDto.recipe.nutritionCalculationStatus
                        ),
                        portionSize = mealDto.portionSize,
                        recommendationImpressionId = mealDto.recommendationImpressionId,
                        selectionReason = mealDto.selectionReason,
                        isPinned = mealDto.pinned,
                        isMealPrepCarryover = mealDto.mealPrepCarryover
                    )
                }
            )
        }
    )
}

fun MealPlanResponseDto.toEntity(): MealPlanEntity {
    return MealPlanEntity(
        id = id ?: System.currentTimeMillis(),
        planJson = Json.encodeToString(MealPlanResponseDto.serializer(), this)
    )
}

fun ShoppingListItemDto.toDomain(): ShoppingListItem {
    return ShoppingListItem(
        productId = productId,
        productName = productName,
        category = category,
        totalQuantity = totalQuantity,
        unit = localizeMeasurementUnit(unit) ?: "г",
        isManual = false
    )
}

fun MealPlanEntity.toDomain(): MealPlanModel {
    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    return json.decodeFromString(MealPlanResponseDto.serializer(), planJson).toDomain()
}

fun MealPlanMealExplanationDto.toDomain(): MealPlanMealExplanationModel {
    return MealPlanMealExplanationModel(
        planRecipeId = planRecipeId,
        recommendationImpressionId = recommendationImpressionId,
        mealType = mealType,
        reason = reason,
        preferenceReason = preferenceReason,
        finalScore = finalScore,
        ruleScore = ruleScore,
        mlScore = mlScore,
        coveragePercent = coveragePercent,
        selectedProductCount = selectedProductCount,
        matchedSelectedProductCount = matchedSelectedProductCount,
        pantryProductCount = pantryProductCount,
        tags = tags
    )
}
