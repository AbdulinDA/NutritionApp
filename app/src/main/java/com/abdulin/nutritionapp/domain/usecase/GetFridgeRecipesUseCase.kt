package com.abdulin.nutritionapp.domain.usecase

import com.abdulin.nutritionapp.core.utils.Resource
import com.abdulin.nutritionapp.domain.model.PantryItemModel
import com.abdulin.nutritionapp.domain.model.RecipeModel
import com.abdulin.nutritionapp.domain.repository.PantryRepository
import com.abdulin.nutritionapp.domain.repository.ProductRepository
import com.abdulin.nutritionapp.domain.repository.RecipeRepository
import javax.inject.Inject

/**
 * Use case для получения рекомендаций рецептов на основе продуктов, 
 * которые пользователь чаще всего использует ("из холодильника").
 */
class GetFridgeRecipesUseCase @Inject constructor(
    private val pantryRepository: PantryRepository,
    private val productRepository: ProductRepository,
    private val recipeRepository: RecipeRepository
) {
    suspend operator fun invoke(): Resource<List<RecipeModel>> {
        when (val pantryResult = pantryRepository.getPantry()) {
            is Resource.Success -> {
                val pantryItems = pantryResult.data.orEmpty()
                if (pantryItems.isNotEmpty()) {
                    return loadRecipesByPantryItems(pantryItems)
                }
            }
            is Resource.Error -> Unit
            is Resource.Loading -> Unit
        }

        val frequentProducts = productRepository.getFrequentProducts(3)
        if (frequentProducts.isEmpty()) {
            return Resource.Error("Not enough pantry or diary data to suggest recipes")
        }

        val mainIngredient = frequentProducts.first().productName
        return recipeRepository.searchRecipes(query = mainIngredient)
    }

    private suspend fun loadRecipesByPantryItems(
        pantryItems: List<PantryItemModel>
    ): Resource<List<RecipeModel>> {
        return when (
            val recommendations = recipeRepository.getRecommendationsByProducts(
                productIds = pantryItems.map { it.productId },
                limit = 10,
                maxMissingIngredients = 2
            )
        ) {
            is Resource.Success -> Resource.Success(recommendations.data.orEmpty().map { it.recipe })
            is Resource.Error -> Resource.Error(recommendations.message ?: "Failed to get pantry recipes")
            is Resource.Loading -> Resource.Loading()
        }
    }
}
