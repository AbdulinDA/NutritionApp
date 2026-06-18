package com.abdulin.nutritionapp.data.repository

import android.content.Context
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.filter
import androidx.paging.map
import com.abdulin.nutritionapp.R
import com.abdulin.nutritionapp.core.network.safeApiCall
import com.abdulin.nutritionapp.core.utils.shouldHideRecipeFromApp
import com.abdulin.nutritionapp.core.utils.Resource
import com.abdulin.nutritionapp.data.dto.recipe.ProductMatchRequestDto
import com.abdulin.nutritionapp.data.mapper.toDomain
import com.abdulin.nutritionapp.data.paging.RecipePagingSource
import com.abdulin.nutritionapp.data.remote.RecommendationFeedbackRequestDto
import com.abdulin.nutritionapp.domain.model.ProductMatchRecipeModel
import com.abdulin.nutritionapp.domain.model.RecommendedRecipeModel
import com.abdulin.nutritionapp.domain.model.RecipeCompositionModel
import com.abdulin.nutritionapp.domain.model.RecipeModel
import com.abdulin.nutritionapp.domain.repository.RecipeRepository
import com.abdulin.nutritionapp.data.remote.NutritionApi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import javax.inject.Inject

class RecipeRepositoryImpl @Inject constructor(
    private val api: NutritionApi,
    @ApplicationContext private val context: Context
) : RecipeRepository {

    override fun searchRecipesPaging(
        mealType: String?,
        maxTime: Int?,
        query: String?
    ): Flow<PagingData<RecipeModel>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                RecipePagingSource(
                    api = api,
                    query = query?.trim()?.takeIf { it.isNotEmpty() },
                    mealType = mealType,
                    maxTime = maxTime,
                    fallbackErrorMessage = context.getString(R.string.network_unknown_server_error)
                )
            }
        ).flow.map { pagingData ->
            pagingData
                .map { it.toDomain() }
                .filter { !shouldHideRecipeFromApp(it) }
        }
    }

    override suspend fun searchRecipes(
        mealType: String?,
        maxTime: Int?,
        query: String?
    ): Resource<List<RecipeModel>> {
        val result = safeApiCall {
            api.searchRecipes(mealType, maxTime, query)
        }
        return when (result) {
            is Resource.Success -> Resource.Success(
                result.data
                    ?.map { it.toDomain() }
                    ?.filterNot(::shouldHideRecipeFromApp)
                    ?: emptyList()
            )
            is Resource.Error -> Resource.Error(result.message ?: "Failed to find recipes")
            is Resource.Loading -> Resource.Loading()
        }
    }

    override suspend fun getRecipeById(id: Long): Resource<RecipeModel> {
        val result = safeApiCall { api.getRecipeById(id) }
        return when (result) {
            is Resource.Success -> {
                val recipe = result.data!!.toDomain()
                if (shouldHideRecipeFromApp(recipe)) {
                    Resource.Error("Recipe is unavailable")
                } else {
                    Resource.Success(recipe)
                }
            }
            is Resource.Error -> Resource.Error(result.message ?: "Recipe not found")
            is Resource.Loading -> Resource.Loading()
        }
    }

    override suspend fun getRecipeComposition(
        recipeId: Long,
        sideDishRecipeId: Long?
    ): Resource<RecipeCompositionModel> {
        val result = safeApiCall { api.getRecipeComposition(recipeId, sideDishRecipeId) }
        return when (result) {
            is Resource.Success -> {
                val composition = result.data?.toDomain()
                if (composition != null) {
                    Resource.Success(composition)
                } else {
                    Resource.Error("Failed to build recipe composition")
                }
            }
            is Resource.Error -> Resource.Error(result.message ?: "Composition is unavailable")
            is Resource.Loading -> Resource.Loading()
        }
    }

    override suspend fun getSmartRecommendations(
        limit: Int,
        mealType: String?,
        variant: String?
    ): Resource<List<RecommendedRecipeModel>> {
        val result = safeApiCall { api.getSmartRecommendations(limit, mealType) }
        return when (result) {
            is Resource.Success -> {
                val recipes = result.data
                    ?.mapNotNull { recommendation ->
                        recommendation.recipe?.let { recipe ->
                            val resolvedId = recipe.id ?: recommendation.recipeId
                            val mappedRecipe = recipe.copy(id = resolvedId).toDomain()
                            if (shouldHideRecipeFromApp(mappedRecipe)) {
                                null
                            } else {
                                RecommendedRecipeModel(
                                    recipe = mappedRecipe,
                                    impressionId = recommendation.impressionId,
                                    reason = recommendation.reason,
                                    score = recommendation.score,
                                    ruleScore = recommendation.ruleScore,
                                    mlScore = recommendation.mlScore,
                                    experimentVariant = recommendation.experimentVariant,
                                    explanationTags = recommendation.explanationTags
                                )
                            }
                        }
                    }
                    ?: emptyList()
                Resource.Success(recipes)
            }
            is Resource.Error -> Resource.Error(result.message ?: "Failed to get recommendations")
            is Resource.Loading -> Resource.Loading()
        }
    }

    override suspend fun getRecommendationsByProducts(
        productIds: List<Long>,
        mealType: String?,
        limit: Int,
        maxMissingIngredients: Int
    ): Resource<List<ProductMatchRecipeModel>> {
        val result = safeApiCall {
            api.getRecommendationsByProducts(
                ProductMatchRequestDto(
                    productIds = productIds,
                    mealType = mealType,
                    limit = limit,
                    maxMissingIngredients = maxMissingIngredients
                )
            )
        }
        return when (result) {
            is Resource.Success -> Resource.Success(
                result.data
                    .orEmpty()
                    .mapNotNull { it.toDomain() }
                    .filterNot { shouldHideRecipeFromApp(it.recipe) }
            )
            is Resource.Error -> Resource.Error(result.message ?: "Failed to get recipes by products")
            is Resource.Loading -> Resource.Loading()
        }
    }

    override suspend fun sendRecommendationFeedback(
        recipeId: Long,
        eventType: String,
        impressionId: Long?,
        metadata: Map<String, String>
    ): Resource<Unit> {
        return try {
            api.sendRecommendationFeedback(
                RecommendationFeedbackRequestDto(
                    recipeId = recipeId,
                    eventType = eventType,
                    impressionId = impressionId,
                    metadataJson = metadata.takeIf { it.isNotEmpty() }?.let { Json.encodeToString(it) }
                )
            )
            Resource.Success(Unit)
        } catch (error: Exception) {
            Resource.Error(error.message ?: "Failed to send recommendation feedback")
        }
    }
}
