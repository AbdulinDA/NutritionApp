package com.abdulin.nutritionapp.data.mapper

import com.abdulin.nutritionapp.core.utils.localizeMeasurementUnit
import com.abdulin.nutritionapp.data.dto.recipe.MissingIngredientDto
import com.abdulin.nutritionapp.data.dto.recipe.ProductMatchRecommendationDto
import com.abdulin.nutritionapp.data.dto.recipe.RecipeCompositionDto
import com.abdulin.nutritionapp.data.dto.recipe.RecipeDto
import com.abdulin.nutritionapp.data.dto.recipe.RecipeIngredientDto
import com.abdulin.nutritionapp.data.dto.recipe.RecipeSideDishDto
import com.abdulin.nutritionapp.domain.model.MissingIngredientModel
import com.abdulin.nutritionapp.domain.model.ProductMatchRecipeModel
import com.abdulin.nutritionapp.domain.model.RecipeCompositionModel
import com.abdulin.nutritionapp.domain.model.RecipeIngredientModel
import com.abdulin.nutritionapp.domain.model.RecipeModel
import com.abdulin.nutritionapp.domain.model.RecipeNutritionIngredientBreakdownModel
import com.abdulin.nutritionapp.domain.model.RecipeSideDishModel
import java.util.Locale

fun RecipeDto.toDomain(): RecipeModel {
    val parsedIngredientLines = extractIngredientLines(description)
    return RecipeModel(
        id = id ?: 0L,
        title = title?.takeIf { it.isNotBlank() } ?: "-",
        description = description,
        imageUrl = imageUrl,
        cookTimeMin = cookTimeMin ?: 0,
        prepTimeMin = prepTimeMin,
        difficultLevel = difficultLevel,
        servingsCount = servingsCount,
        portionWeightG = portionWeightG,
        totalCalories = totalCalories ?: 0.0,
        totalProtein = totalProtein ?: 0.0,
        totalFat = totalFat ?: 0.0,
        totalCarbs = totalCarbs ?: 0.0,
        instruction = instruction,
        cuisineType = cuisineType,
        mealType = mealType,
        recipeRole = recipeRole,
        requiresSideDish = requiresSideDish == true,
        hiddenFromCatalog = hiddenFromCatalog == true,
        nutritionCalculationStatus = nutritionCalculationStatus,
        nutritionNotes = nutritionNotes,
        recommendedSideDishes = recommendedSideDishes.orEmpty().map { it.toDomain() },
        ingredients = ingredients.orEmpty().mapIndexed { index, ingredient ->
            ingredient.toDomain(parsedIngredientLines.getOrNull(index))
        }
    )
}

fun RecipeSideDishDto.toDomain() = RecipeSideDishModel(
    recipeSideDishId = recipeSideDishId,
    sideDishRecipeId = sideDishRecipeId ?: 0L,
    sideDishRecipeName = sideDishRecipeName?.takeIf { it.isNotBlank() } ?: "-",
    sideDishRecipeRole = sideDishRecipeRole,
    portionMultiplier = portionMultiplier,
    notes = notes
)

fun RecipeCompositionDto.toDomain(): RecipeCompositionModel? {
    val main = mainRecipe?.toDomain() ?: return null
    return RecipeCompositionModel(
        mainRecipe = main,
        sideDishRecipe = sideDishRecipe?.toDomain(),
        sideDishIncludedInNutrition = sideDishIncludedInNutrition == true,
        sideDishPortionMultiplier = sideDishPortionMultiplier,
        totalCalories = totalCalories ?: main.totalCalories,
        totalProtein = totalProtein ?: main.totalProtein,
        totalFat = totalFat ?: main.totalFat,
        totalCarbs = totalCarbs ?: main.totalCarbs,
        totalPortionWeightG = totalPortionWeightG,
        servingsCount = servingsCount ?: main.servingsCount,
        nutritionNote = nutritionNote,
        nutritionCalculationStatus = nutritionCalculationStatus,
        nutritionNotes = nutritionNotes,
        calculationFormula = calculationFormula,
        caloriesPerServing = caloriesPerServing,
        proteinPerServing = proteinPerServing,
        fatPerServing = fatPerServing,
        carbsPerServing = carbsPerServing,
        caloriesPer100g = caloriesPer100g,
        proteinPer100g = proteinPer100g,
        fatPer100g = fatPer100g,
        carbsPer100g = carbsPer100g,
        ingredientBreakdown = ingredientBreakdown.orEmpty().map { it.toDomain() },
        planRecipeId = planRecipeId,
        portionMultiplier = portionMultiplier,
        plannedServingsCount = plannedServingsCount
    )
}

fun com.abdulin.nutritionapp.data.dto.recipe.RecipeNutritionIngredientBreakdownDto.toDomain() =
    RecipeNutritionIngredientBreakdownModel(
        ingredientId = ingredientId,
        sourceType = sourceType,
        sourceRecipeName = sourceRecipeName,
        appliedMultiplier = appliedMultiplier,
        productId = productId,
        productName = productName?.takeIf { it.isNotBlank() } ?: "-",
        quantity = quantity,
        measurementUnitCode = measurementUnitCode,
        required = required,
        productCaloriesPer100g = productCaloriesPer100g,
        productProteinPer100g = productProteinPer100g,
        productFatPer100g = productFatPer100g,
        productCarbsPer100g = productCarbsPer100g,
        ingredientCalories = ingredientCalories,
        ingredientProtein = ingredientProtein,
        ingredientFat = ingredientFat,
        ingredientCarbs = ingredientCarbs,
        calculationBasis = calculationBasis
    )

fun RecipeIngredientDto.toDomain(parsedLine: String? = null) = RecipeIngredientModel(
    id = ingredientId ?: 0L,
    productId = productId,
    name = parsedLine?.let(::extractIngredientName)
        ?.takeIf { it.isNotBlank() }
        ?: productName?.takeIf { it.isNotBlank() }
        ?: "-",
    quantity = quantity,
    unit = localizeMeasurementUnit(measurementUnitCode),
    notes = notes?.takeIf { it.isNotBlank() }
        ?: parsedLine?.let(::extractIngredientNote),
    calories = calories ?: 0.0,
    protein = protein ?: 0.0,
    fat = fat ?: 0.0,
    carbs = carbs ?: 0.0
)

fun ProductMatchRecommendationDto.toDomain(): ProductMatchRecipeModel? {
    val recipeDto = recipe ?: return null
    return ProductMatchRecipeModel(
        recipe = recipeDto.toDomain(),
        impressionId = impressionId,
        score = score,
        ruleScore = ruleScore,
        mlScore = mlScore,
        reason = reason,
        experimentVariant = experimentVariant,
        explanationTags = explanationTags,
        coveragePercent = coveragePercent,
        matchedProductIds = matchedProductIds,
        missingIngredients = missingIngredients.map { it.toDomain() }
    )
}

fun MissingIngredientDto.toDomain() = MissingIngredientModel(
    productId = productId,
    productName = productName,
    quantity = quantity,
    unitCode = unitCode
)

private fun extractIngredientLines(description: String?): List<String> {
    if (description.isNullOrBlank()) return emptyList()

    val lines = description.lines()
    val startIndex = lines.indexOfFirst { it.trim().lowercase(Locale.ROOT).startsWith("ингредиенты") }
    if (startIndex < 0) return emptyList()

    return lines
        .drop(startIndex + 1)
        .map { it.trim() }
        .takeWhile { it.startsWith("-") || it.startsWith("•") }
        .map { it.removePrefix("-").removePrefix("•").trim() }
        .filter { it.isNotBlank() }
}

private fun extractIngredientName(rawLine: String): String {
    val lineWithoutParentheses = rawLine.substringBefore(" (").trim()
    val withoutTaste = lineWithoutParentheses
        .substringBefore(" - по вкусу", lineWithoutParentheses)
        .substringBefore(" по вкусу", lineWithoutParentheses)
        .trim()
    val firstDigitIndex = withoutTaste.indexOfFirst { it.isDigit() }
    val candidate = if (firstDigitIndex > 0) {
        withoutTaste.substring(0, firstDigitIndex).trim()
    } else {
        withoutTaste
    }
    return candidate.trim().trimEnd(',', '-', ' ')
}

private fun extractIngredientNote(rawLine: String): String? {
    val parenthesisStart = rawLine.indexOf('(')
    val parenthesisEnd = rawLine.indexOf(')')
    if (parenthesisStart >= 0 && parenthesisEnd > parenthesisStart) {
        return rawLine.substring(parenthesisStart + 1, parenthesisEnd).trim().takeIf { it.isNotBlank() }
    }
    return null
}
