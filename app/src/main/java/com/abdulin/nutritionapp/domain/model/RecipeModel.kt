package com.abdulin.nutritionapp.domain.model

data class RecipeModel(
    val id: Long,
    val title: String,
    val description: String? = null,
    val imageUrl: String? = null,
    val cookTimeMin: Int,
    val prepTimeMin: Int? = null,
    val difficultLevel: String? = null,
    val servingsCount: Int? = null,
    val portionWeightG: Double? = null,
    val totalCalories: Double,
    val totalProtein: Double,
    val totalFat: Double,
    val totalCarbs: Double,
    val instruction: String? = null,
    val cuisineType: String? = null,
    val mealType: String? = null,
    val recipeRole: String? = null,
    val requiresSideDish: Boolean = false,
    val hiddenFromCatalog: Boolean = false,
    val nutritionCalculationStatus: String? = null,
    val nutritionNotes: String? = null,
    val recommendedSideDishes: List<RecipeSideDishModel> = emptyList(),
    val ingredients: List<RecipeIngredientModel> = emptyList()
)

data class RecipeSideDishModel(
    val recipeSideDishId: Long? = null,
    val sideDishRecipeId: Long,
    val sideDishRecipeName: String,
    val sideDishRecipeRole: String? = null,
    val portionMultiplier: Double? = null,
    val notes: String? = null
)

data class RecipeCompositionModel(
    val mainRecipe: RecipeModel,
    val sideDishRecipe: RecipeModel? = null,
    val sideDishIncludedInNutrition: Boolean = false,
    val sideDishPortionMultiplier: Double? = null,
    val totalCalories: Double,
    val totalProtein: Double,
    val totalFat: Double,
    val totalCarbs: Double,
    val totalPortionWeightG: Double? = null,
    val servingsCount: Int? = null,
    val nutritionNote: String? = null,
    val nutritionCalculationStatus: String? = null,
    val nutritionNotes: String? = null,
    val calculationFormula: String? = null,
    val caloriesPerServing: Double? = null,
    val proteinPerServing: Double? = null,
    val fatPerServing: Double? = null,
    val carbsPerServing: Double? = null,
    val caloriesPer100g: Double? = null,
    val proteinPer100g: Double? = null,
    val fatPer100g: Double? = null,
    val carbsPer100g: Double? = null,
    val ingredientBreakdown: List<RecipeNutritionIngredientBreakdownModel> = emptyList(),
    val planRecipeId: Long? = null,
    val portionMultiplier: Double? = null,
    val plannedServingsCount: Double? = null
)

data class RecipeNutritionIngredientBreakdownModel(
    val ingredientId: Long? = null,
    val sourceType: String? = null,
    val sourceRecipeName: String? = null,
    val appliedMultiplier: Double? = null,
    val productId: Long? = null,
    val productName: String,
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

data class RecipeIngredientModel(
    val id: Long,
    val productId: Long?,
    val name: String,
    val quantity: Double?,
    val unit: String?,
    val notes: String?,
    val calories: Double,
    val protein: Double,
    val fat: Double,
    val carbs: Double
)
