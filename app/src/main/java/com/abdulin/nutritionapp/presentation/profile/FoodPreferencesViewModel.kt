package com.abdulin.nutritionapp.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.abdulin.nutritionapp.core.utils.TokenManager
import com.abdulin.nutritionapp.core.utils.Resource
import com.abdulin.nutritionapp.data.dto.auth.UserResponseDto
import com.abdulin.nutritionapp.domain.model.ProductModel
import com.abdulin.nutritionapp.domain.repository.ProductRepository
import com.abdulin.nutritionapp.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FoodPreferencesUiState(
    val searchQuery: String = "",
    val favoriteProducts: List<FoodPreferenceProduct> = emptyList(),
    val excludedProducts: List<FoodPreferenceProduct> = emptyList(),
    val allergyProducts: List<FoodPreferenceProduct> = emptyList(),
    val favoriteCuisines: List<String> = emptyList(),
    val dislikedCuisines: List<String> = emptyList(),
    val currentUser: UserResponseDto? = null,
    val isSyncing: Boolean = false,
    val error: String? = null
)

private data class FoodPreferenceSnapshot(
    val favoriteProducts: List<FoodPreferenceProduct>,
    val excludedProducts: List<FoodPreferenceProduct>,
    val allergyProducts: List<FoodPreferenceProduct>,
    val favoriteCuisines: List<String>,
    val dislikedCuisines: List<String>
)

@HiltViewModel
class FoodPreferencesViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _state = MutableStateFlow(FoodPreferencesUiState())
    val state = _state.asStateFlow()

    private val searchQuery = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val productsPagingData: Flow<PagingData<ProductModel>> = searchQuery
        .debounce(250)
        .flatMapLatest(productRepository::searchProductsPaging)
        .cachedIn(viewModelScope)

    init {
        loadProfile()

        combine(
            tokenManager.favoriteProducts,
            tokenManager.excludedProducts,
            tokenManager.allergyProducts,
            tokenManager.favoriteCuisines,
            tokenManager.dislikedCuisines
        ) { favoriteProducts, excludedProducts, allergyProducts, favoriteCuisines, dislikedCuisines ->
            FoodPreferenceSnapshot(
                favoriteProducts = favoriteProducts,
                excludedProducts = excludedProducts,
                allergyProducts = allergyProducts,
                favoriteCuisines = favoriteCuisines,
                dislikedCuisines = dislikedCuisines
            )
        }.onEach { snapshot ->
            _state.update {
                it.copy(
                    favoriteProducts = snapshot.favoriteProducts,
                    excludedProducts = snapshot.excludedProducts,
                    allergyProducts = snapshot.allergyProducts,
                    favoriteCuisines = snapshot.favoriteCuisines,
                    dislikedCuisines = snapshot.dislikedCuisines
                )
            }
        }.launchIn(viewModelScope)
    }

    private fun loadProfile() {
        viewModelScope.launch {
            when (val result = userRepository.getMyProfile()) {
                is Resource.Success -> {
                    _state.update { it.copy(currentUser = result.data, error = null) }
                }
                is Resource.Error -> {
                    _state.update { it.copy(error = result.message) }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
        _state.update { it.copy(searchQuery = query) }
    }

    fun toggleExcludedProduct(product: FoodPreferenceProduct) {
        viewModelScope.launch {
            tokenManager.toggleExcludedProduct(product)
            syncFoodPreferences()
        }
    }

    fun toggleFavoriteProduct(product: FoodPreferenceProduct) {
        viewModelScope.launch {
            val wasFavorite = tokenManager.favoriteProductIds.first().contains(product.id)
            val result = productRepository.toggleFavorite(product.id)
            if (result is Resource.Success || wasFavorite) {
                tokenManager.toggleFavoriteProductId(product.id, product.name)
            } else if (result is Resource.Error) {
                _state.update { it.copy(error = result.message) }
            }
        }
    }

    fun toggleAllergyProduct(product: FoodPreferenceProduct) {
        viewModelScope.launch {
            tokenManager.toggleAllergyProduct(product)
            syncFoodPreferences()
        }
    }

    fun toggleFavoriteCuisine(cuisine: String) {
        viewModelScope.launch {
            tokenManager.toggleFavoriteCuisine(cuisine)
            syncFoodPreferences()
        }
    }

    fun toggleDislikedCuisine(cuisine: String) {
        viewModelScope.launch {
            tokenManager.toggleDislikedCuisine(cuisine)
            syncFoodPreferences()
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    private suspend fun syncFoodPreferences() {
        val currentUser = _state.value.currentUser ?: return
        _state.update { it.copy(isSyncing = true, error = null) }

        val allergyNames = tokenManager.allergyProducts.first()
            .map { it.name.trim() }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }
        val excludedIds = tokenManager.excludedProducts.first()
            .map { it.id }
            .distinct()
        val favoriteCuisines = tokenManager.favoriteCuisines.first()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }
        val dislikedCuisines = tokenManager.dislikedCuisines.first()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }
            .filterNot { disliked -> favoriteCuisines.any { it.equals(disliked, ignoreCase = true) } }

        when (
            val result = userRepository.syncFoodPreferences(
                currentUser = currentUser,
                allergies = allergyNames,
                excludedProductsIds = excludedIds,
                favoriteCuisines = favoriteCuisines,
                dislikedCuisines = dislikedCuisines
            )
        ) {
            is Resource.Success -> {
                _state.update {
                    it.copy(
                        currentUser = result.data,
                        isSyncing = false,
                        error = null
                    )
                }
            }
            is Resource.Error -> {
                _state.update {
                    it.copy(
                        isSyncing = false,
                        error = result.message
                    )
                }
            }
            is Resource.Loading -> Unit
        }
    }
}
