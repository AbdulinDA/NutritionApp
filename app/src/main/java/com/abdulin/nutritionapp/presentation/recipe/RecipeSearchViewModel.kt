package com.abdulin.nutritionapp.presentation.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import com.abdulin.nutritionapp.core.utils.Resource
import com.abdulin.nutritionapp.domain.model.RecommendedRecipeModel
import com.abdulin.nutritionapp.domain.model.RecipeModel
import com.abdulin.nutritionapp.domain.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecipeSearchUiState(
    val query: String = "",
    val recommendedRecipes: List<RecommendedRecipeModel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedMealType: String? = null,
    val selectedNutritionFilter: NutritionFilter = NutritionFilter.ALL
)

enum class NutritionFilter {
    ALL,
    COMPLETE,
    ESTIMATED,
    REQUIRES_SIDE_DISH,
    INCOMPLETE
}

private data class RecipeSearchParams(
    val query: String,
    val mealType: String?,
    val nutritionFilter: NutritionFilter
)

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class RecipeSearchViewModel @Inject constructor(
    private val repository: RecipeRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RecipeSearchUiState())
    val state = _state.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _selectedMealType = MutableStateFlow<String?>(null)
    private val _selectedNutritionFilter = MutableStateFlow(NutritionFilter.ALL)

    val recipesPagingData: Flow<PagingData<RecipeModel>> = combine(
        _searchQuery
            .debounce(400L)
            .map(String::trim)
            .distinctUntilChanged(),
        _selectedMealType,
        _selectedNutritionFilter
    ) { query, mealType, nutritionFilter ->
        RecipeSearchParams(
            query = query,
            mealType = mealType,
            nutritionFilter = nutritionFilter
        )
    }
        .distinctUntilChanged()
        .flatMapLatest { params ->
            repository.searchRecipesPaging(
                query = params.query,
                mealType = params.mealType
            ).map { pagingData ->
                if (params.nutritionFilter == NutritionFilter.ALL) {
                    pagingData
                } else {
                    pagingData.filter { recipe ->
                        matchesNutritionFilter(recipe, params.nutritionFilter)
                    }
                }
            }
        }
        .cachedIn(viewModelScope)

    init {
        observeBrowseRecommendations()
    }

    fun retry() {
        viewModelScope.launch {
            loadBrowseRecommendations(
                query = _searchQuery.value.trim(),
                mealType = _selectedMealType.value
            )
        }
    }

    fun onQueryChange(newQuery: String) {
        _state.update { it.copy(query = newQuery) }
        _searchQuery.value = newQuery
    }

    fun onMealTypeChange(mealType: String?) {
        val newMealType = if (_state.value.selectedMealType == mealType) null else mealType
        _state.update { it.copy(selectedMealType = newMealType) }
        _selectedMealType.value = newMealType
    }

    fun onNutritionFilterChange(filter: NutritionFilter) {
        val selectedFilter = if (_state.value.selectedNutritionFilter == filter) {
            NutritionFilter.ALL
        } else {
            filter
        }
        _state.update { it.copy(selectedNutritionFilter = selectedFilter) }
        _selectedNutritionFilter.value = selectedFilter
        if (_searchQuery.value.isBlank()) {
            retry()
        }
    }

    fun applyInitialMealType(mealType: String?) {
        _state.update { it.copy(selectedMealType = mealType) }
        _selectedMealType.value = mealType
    }

    private fun observeBrowseRecommendations() {
        combine(
            _searchQuery
                .debounce(400L)
                .map(String::trim)
                .distinctUntilChanged(),
            _selectedMealType
        ) { query, mealType -> query to mealType }
            .distinctUntilChanged()
            .onEach { (query, mealType) ->
                loadBrowseRecommendations(query, mealType)
            }
            .launchIn(viewModelScope)
    }

    private suspend fun loadBrowseRecommendations(query: String, mealType: String?) {
        if (query.isNotBlank()) {
            _state.update {
                it.copy(
                    recommendedRecipes = emptyList(),
                    isLoading = false,
                    error = null
                )
            }
            return
        }

        _state.update { it.copy(isLoading = true, error = null) }
        when (val recommendations = repository.getSmartRecommendations(limit = 10, mealType = mealType)) {
            is Resource.Success -> {
                _state.update {
                    it.copy(
                        recommendedRecipes = recommendations.data.orEmpty().filter {
                            _selectedNutritionFilter.value == NutritionFilter.ALL ||
                                matchesNutritionFilter(it.recipe, _selectedNutritionFilter.value)
                        },
                        isLoading = false,
                        error = null
                    )
                }
            }
            is Resource.Error -> {
                _state.update {
                    it.copy(
                        recommendedRecipes = emptyList(),
                        isLoading = false,
                        error = recommendations.message
                    )
                }
            }
            is Resource.Loading -> {
                _state.update { it.copy(isLoading = true) }
            }
        }
    }

    private fun matchesNutritionFilter(recipe: RecipeModel, filter: NutritionFilter): Boolean {
        val status = recipe.nutritionCalculationStatus?.trim()?.uppercase().orEmpty()
        return when (filter) {
            NutritionFilter.ALL -> true
            NutritionFilter.COMPLETE -> status == "COMPLETE"
            NutritionFilter.ESTIMATED -> status == "ESTIMATED"
            NutritionFilter.REQUIRES_SIDE_DISH -> status == "REQUIRES_SIDE_DISH"
            NutritionFilter.INCOMPLETE -> status == "INCOMPLETE"
        }
    }
}
