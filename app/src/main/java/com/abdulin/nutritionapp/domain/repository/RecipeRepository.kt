package com.abdulin.nutritionapp.domain.repository

import androidx.paging.PagingData
import com.abdulin.nutritionapp.core.utils.Resource
import com.abdulin.nutritionapp.domain.model.ProductMatchRecipeModel
import com.abdulin.nutritionapp.domain.model.RecommendedRecipeModel
import com.abdulin.nutritionapp.domain.model.RecipeCompositionModel
import com.abdulin.nutritionapp.domain.model.RecipeModel
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {
    fun searchRecipesPaging(
        mealType: String? = null,
        maxTime: Int? = null,
        query: String? = null
    ): Flow<PagingData<RecipeModel>>

    suspend fun searchRecipes(
        mealType: String? = null,
        maxTime: Int? = null,
        query: String? = null
    ): Resource<List<RecipeModel>>

    suspend fun getRecipeById(id: Long): Resource<RecipeModel>

    suspend fun getRecipeComposition(
        recipeId: Long,
        sideDishRecipeId: Long? = null
    ): Resource<RecipeCompositionModel>

    suspend fun getSmartRecommendations(
        limit: Int,
        mealType: String?,
        variant: String? = null
    ): Resource<List<RecommendedRecipeModel>>

    suspend fun getRecommendationsByProducts(
        productIds: List<Long>,
        mealType: String? = null,
        limit: Int = 10,
        maxMissingIngredients: Int = 2
    ): Resource<List<ProductMatchRecipeModel>>

    suspend fun sendRecommendationFeedback(
        recipeId: Long,
        eventType: String,
        impressionId: Long? = null,
        metadata: Map<String, String> = emptyMap()
    ): Resource<Unit>
}
