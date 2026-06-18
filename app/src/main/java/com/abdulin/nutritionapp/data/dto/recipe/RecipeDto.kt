package com.abdulin.nutritionapp.data.dto.recipe

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class RecipeDto(
    @SerialName("recipeId")
    @JsonNames("id")
    val id: Long? = null,
    val publicId: String? = null,
    @SerialName("recipeName")
    @JsonNames("title")
    val title: String? = null,
    val description: String? = null,
    val imageUrl: String? = null,
    val cookTimeMin: Int? = null,
    val prepTimeMin: Int? = null,
    val difficultLevel: String? = null,
    val servingsCount: Int? = null,
    val portionWeightG: Double? = null,
    val totalCalories: Double? = null,
    val totalProtein: Double? = null,
    val totalFat: Double? = null,
    val totalCarbs: Double? = null,
    val instruction: String? = null,
    val cuisineType: String? = null,
    val mealType: String? = null,
    val recipeRole: String? = null,
    val requiresSideDish: Boolean? = null,
    val hiddenFromCatalog: Boolean? = null,
    val nutritionCalculationStatus: String? = null,
    val nutritionNotes: String? = null,
    val recommendedSideDishes: List<RecipeSideDishDto>? = emptyList(),
    val ingredients: List<RecipeIngredientDto>? = emptyList()
)

@Serializable
data class RecipeSideDishDto(
    val recipeSideDishId: Long? = null,
    val sideDishRecipeId: Long? = null,
    val sideDishRecipeName: String? = null,
    val sideDishRecipeRole: String? = null,
    val portionMultiplier: Double? = null,
    val notes: String? = null
)

@Serializable
data class RecipeCompositionDto(
    val mainRecipe: RecipeDto? = null,
    val sideDishRecipe: RecipeDto? = null,
    val sideDishIncludedInNutrition: Boolean? = null,
    val sideDishPortionMultiplier: Double? = null,
    val nutritionCalculationStatus: String? = null,
    val nutritionNotes: String? = null,
    val calculationFormula: String? = null,
    val totalCalories: Double? = null,
    val totalProtein: Double? = null,
    val totalFat: Double? = null,
    val totalCarbs: Double? = null,
    val totalPortionWeightG: Double? = null,
    val servingsCount: Int? = null,
    val caloriesPerServing: Double? = null,
    val proteinPerServing: Double? = null,
    val fatPerServing: Double? = null,
    val carbsPerServing: Double? = null,
    val caloriesPer100g: Double? = null,
    val proteinPer100g: Double? = null,
    val fatPer100g: Double? = null,
    val carbsPer100g: Double? = null,
    val ingredientBreakdown: List<RecipeNutritionIngredientBreakdownDto>? = emptyList(),
    val nutritionNote: String? = null,
    val planRecipeId: Long? = null,
    val portionMultiplier: Double? = null,
    val plannedServingsCount: Double? = null
)

@Serializable
data class RecipeNutritionIngredientBreakdownDto(
    val ingredientId: Long? = null,
    val sourceType: String? = null,
    val sourceRecipeName: String? = null,
    val appliedMultiplier: Double? = null,
    val productId: Long? = null,
    val productName: String? = null,
    val quantity: Double? = null,
    val measurementUnitCode: String? = null,
    val required: Boolean? = null,
    val productCaloriesPer100g: Double? = null,
    val productProteinPer100g: Double? = null,
    val productFatPer100g: Double? = null,
    val productCarbsPer100g: Double? = null,
    val ingredientCalories: Double? = null,
    val ingredientProtein: Double? = null,
    val ingredientFat: Double? = null,
    val ingredientCarbs: Double? = null,
    val calculationBasis: String? = null
)

@Serializable
data class RecipeIngredientDto(
    val ingredientId: Long? = null,
    val productId: Long? = null,
    val productName: String? = null,
    val quantity: Double? = null,
    val measurementUnitCode: String? = null,
    val notes: String? = null,
    val isRequired: Boolean? = null,
    val calories: Double? = null,
    val protein: Double? = null,
    val fat: Double? = null,
    val carbs: Double? = null
)
