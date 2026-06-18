package com.abdulin.nutritionapp.presentation.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdulin.nutritionapp.core.utils.TokenManager
import com.abdulin.nutritionapp.core.utils.Resource
import com.abdulin.nutritionapp.data.dto.diary.CreateFoodDiaryRequestDto
import com.abdulin.nutritionapp.domain.model.RecipeCompositionModel
import com.abdulin.nutritionapp.domain.model.RecipeModel
import com.abdulin.nutritionapp.domain.repository.AnalyticsRepository
import com.abdulin.nutritionapp.domain.repository.FoodDiaryRepository
import com.abdulin.nutritionapp.domain.repository.MealPlanRepository
import com.abdulin.nutritionapp.domain.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class RecipeDetailUiState(
    val recipe: RecipeModel? = null,
    val composition: RecipeCompositionModel? = null,
    val selectedSideDishId: Long? = null,
    val isLoading: Boolean = false,
    val isCompositionLoading: Boolean = false,
    val error: String? = null,
    val compositionError: String? = null,
    val recommendationSource: String? = null,
    val recommendationReason: String? = null,
    val recommendationImpressionId: Long? = null,
    val isFavorite: Boolean = false
)

@HiltViewModel
class RecipeDetailViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val diaryRepository: FoodDiaryRepository,
    private val mealPlanRepository: MealPlanRepository,
    private val analyticsRepository: AnalyticsRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _state = MutableStateFlow(RecipeDetailUiState())
    val state = _state.asStateFlow()

    private val _addState = MutableStateFlow<Resource<Unit>>(Resource.Success(Unit))
    val addState = _addState.asStateFlow()

    private val _planAddState = MutableStateFlow<Resource<Unit>>(Resource.Success(Unit))
    val planAddState = _planAddState.asStateFlow()

    fun loadRecipe(
        id: Long,
        recommendationSource: String? = null,
        recommendationReason: String? = null,
        recommendationImpressionId: Long? = null
    ) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    recommendationSource = recommendationSource,
                    recommendationReason = recommendationReason,
                    recommendationImpressionId = recommendationImpressionId
                )
            }
            val result = recipeRepository.getRecipeById(id)
            if (result is Resource.Success) {
                val recipe = result.data
                
                // Рассчитываем автоматические порции на основе калорий
                _state.update {
                    it.copy(
                        recipe = recipe,
                        composition = null,
                        selectedSideDishId = null,
                        isLoading = false,
                        isCompositionLoading = false,
                        compositionError = null,
                        isFavorite = tokenManager.isFavoriteRecipe(id)
                    )
                }

                recipe?.let { loadedRecipe ->
                    loadComposition(loadedRecipe.id, null)
                }

                analyticsRepository.sendEvent(
                    eventType = "RECIPE_VIEWED",
                    entityType = "RECIPE",
                    entityId = id.toString(),
                    metadata = buildMap {
                        put("title", result.data?.title ?: "")
                        recommendationSource?.let { put("source", it) }
                        recommendationReason?.takeIf { it.isNotBlank() }?.let { put("reason", it) }
                    }
                )
                if (!recommendationSource.isNullOrBlank()) {
                    recipeRepository.sendRecommendationFeedback(
                        recipeId = id,
                        eventType = "OPENED",
                        impressionId = recommendationImpressionId,
                        metadata = buildMap {
                            put("source", recommendationSource)
                            recommendationReason?.takeIf { it.isNotBlank() }?.let { put("reason", it) }
                        }
                    )
                    analyticsRepository.sendEvent(
                        eventType = "RECIPE_RECOMMENDATION_OPENED",
                        entityType = "RECIPE",
                        entityId = id.toString(),
                        metadata = buildMap {
                            put("title", result.data?.title ?: "")
                            put("source", recommendationSource)
                            recommendationReason?.takeIf { it.isNotBlank() }?.let { put("reason", it) }
                        }
                    )
                }
            } else {
                _state.update { it.copy(error = result.message, isLoading = false) }
            }
        }
    }

    fun selectSideDish(sideDishRecipeId: Long?) {
        val recipeId = _state.value.recipe?.id ?: return
        _state.update { it.copy(selectedSideDishId = sideDishRecipeId) }
        loadComposition(recipeId, sideDishRecipeId)
    }

    private fun loadComposition(recipeId: Long, sideDishRecipeId: Long?) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isCompositionLoading = true,
                    compositionError = null
                )
            }
            when (val result = recipeRepository.getRecipeComposition(recipeId, sideDishRecipeId)) {
                is Resource.Success -> {
                    _state.update {
                        it.copy(
                            composition = result.data,
                            isCompositionLoading = false,
                            compositionError = null
                        )
                    }
                }
                is Resource.Error -> {
                    _state.update {
                        it.copy(
                            isCompositionLoading = false,
                            compositionError = result.message
                        )
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun addToDiary(mealType: String, weightGrams: Double) {
        val currentState = _state.value
        val recipe = currentState.recipe ?: return
        val composition = currentState.composition
        viewModelScope.launch {
            _addState.value = Resource.Loading()
            val now = Date()
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now)
            val timeStr = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(now)
            val selectedSideDishId = currentState.selectedSideDishId
            val effectiveTitle = if (selectedSideDishId != null && composition?.sideDishRecipe != null) {
                "${recipe.title} + ${composition.sideDishRecipe.title}"
            } else {
                recipe.title
            }

            val result = diaryRepository.addFood(
                CreateFoodDiaryRequestDto(
                    mealType = mealType,
                    source = "RECIPE",
                    recipeId = recipe.id,
                    sideDishRecipeId = selectedSideDishId,
                    sideDishPortionMultiplier = composition?.sideDishPortionMultiplier,
                    customName = effectiveTitle,
                    calories = recipe.totalCalories,
                    protein = recipe.totalProtein,
                    fat = recipe.totalFat,
                    carbs = recipe.totalCarbs,
                    weightGrams = weightGrams,
                    entryDate = dateStr,
                    consumedAt = timeStr
                )
            )
            _addState.value = result
            if (result is Resource.Success) {
                recipeRepository.sendRecommendationFeedback(
                    recipeId = recipe.id,
                    eventType = "LOGGED",
                    impressionId = currentState.recommendationImpressionId,
                    metadata = buildMap {
                        currentState.recommendationSource?.let { put("source", it) }
                        currentState.recommendationReason?.takeIf { it.isNotBlank() }?.let { put("reason", it) }
                        put("mealType", mealType)
                    }
                )
                analyticsRepository.sendEvent(
                    eventType = "RECIPE_LOGGED",
                    entityType = "RECIPE",
                    entityId = recipe.id.toString(),
                    metadata = buildMap {
                        currentState.recommendationSource?.let { put("source", it) }
                        currentState.recommendationReason?.takeIf { it.isNotBlank() }?.let { put("reason", it) }
                        put("mealType", mealType)
                    }
                )
            }
        }
    }

    fun resetAddState() {
        _addState.value = Resource.Success(Unit)
    }

    fun addToMealPlan(planDate: String, mealType: String, portionSize: Double) {
        val currentState = _state.value
        val recipe = currentState.recipe ?: return
        viewModelScope.launch {
            _planAddState.value = Resource.Loading()
            val result = mealPlanRepository.addRecipeToMealPlan(
                recipeId = recipe.id,
                planDate = planDate,
                mealType = mealType,
                portionSize = portionSize
            )
            _planAddState.value = when (result) {
                is Resource.Success -> Resource.Success(Unit)
                is Resource.Error -> Resource.Error(result.message ?: "Could not add recipe to meal plan")
                is Resource.Loading -> Resource.Loading()
            }
            if (result is Resource.Success) {
                recipeRepository.sendRecommendationFeedback(
                    recipeId = recipe.id,
                    eventType = "PLANNED",
                    impressionId = currentState.recommendationImpressionId,
                    metadata = buildMap {
                        currentState.recommendationSource?.let { put("source", it) }
                        currentState.recommendationReason?.takeIf { it.isNotBlank() }?.let { put("reason", it) }
                        put("mealType", mealType)
                        put("planDate", planDate)
                    }
                )
                analyticsRepository.sendEvent(
                    eventType = "RECIPE_PLANNED",
                    entityType = "RECIPE",
                    entityId = recipe.id.toString(),
                    metadata = buildMap {
                        currentState.recommendationSource?.let { put("source", it) }
                        currentState.recommendationReason?.takeIf { it.isNotBlank() }?.let { put("reason", it) }
                        put("mealType", mealType)
                        put("planDate", planDate)
                    }
                )
            }
        }
    }

    fun resetPlanAddState() {
        _planAddState.value = Resource.Success(Unit)
    }

    fun toggleFavorite() {
        val recipe = _state.value.recipe ?: return
        viewModelScope.launch {
            val isFavorite = tokenManager.toggleFavoriteRecipeId(recipe.id, recipe.title)
            _state.update { it.copy(isFavorite = isFavorite) }
        }
    }

    /**
     * Рассчитывает предполагаемое количество порций на основе калорий.
     * - < 300 ккал → 1 порция
     * - 300-600 ккал → 2 порции
     * - 600-900 ккал → 3 порции
     * - > 900 ккал → 4 порции
     */
    fun calculateSuggestedServings(totalCalories: Double): Int {
        return when {
            totalCalories < 300 -> 1
            totalCalories < 600 -> 2
            totalCalories < 900 -> 3
            else -> 4
        }
    }

    /**
     * Рассчитывает допустимый диапазон порций на основе калорий.
     * - < 300 ккал → только 1 порция
     * - 300-600 ккал → 1-2 порции
     * - 600-900 ккал → 1-3 порции
     * - > 900 ккал → 1-4 порции
     */
    fun calculateServingsRange(totalCalories: Double): IntRange {
        return when {
            totalCalories < 300 -> 1..1
            totalCalories < 600 -> 1..2
            totalCalories < 900 -> 1..3
            else -> 1..4
        }
    }
}
