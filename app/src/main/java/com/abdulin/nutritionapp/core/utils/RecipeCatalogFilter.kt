package com.abdulin.nutritionapp.core.utils

import com.abdulin.nutritionapp.domain.model.RecipeModel
import java.util.Locale

private val componentKeywords = listOf(
    "крем",
    "соус",
    "маринад",
    "тесто",
    "заготовк",
    "заправк",
    "глазур",
    "основа для",
    "cream",
    "frosting",
    "sauce",
    "marinade",
    "dough",
    "batter",
    "dressing",
    "base for"
)

fun shouldHideRecipeFromApp(recipe: RecipeModel): Boolean {
    val role = recipe.recipeRole?.trim()?.uppercase(Locale.ROOT).orEmpty()
    val nutritionStatus = recipe.nutritionCalculationStatus?.trim()?.uppercase(Locale.ROOT).orEmpty()
    val title = recipe.title.lowercase(Locale.ROOT)

    return recipe.hiddenFromCatalog ||
        role == "COMPONENT" ||
        nutritionStatus == "INCOMPLETE" ||
        componentKeywords.any(title::contains)
}

fun shouldHideRecipeTitleFromApp(title: String?): Boolean {
    val normalized = title?.trim()?.lowercase(Locale.ROOT).orEmpty()
    if (normalized.isBlank()) return true
    return componentKeywords.any(normalized::contains)
}
