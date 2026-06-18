package com.abdulin.nutritionapp.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdulin.nutritionapp.core.utils.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FavoritesUiState(
    val products: List<FoodPreferenceProduct> = emptyList(),
    val recipes: List<FavoriteRecipeItem> = emptyList()
)

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _state = MutableStateFlow(FavoritesUiState())
    val state = _state.asStateFlow()

    init {
        combine(
            tokenManager.favoriteProducts,
            tokenManager.favoriteRecipes
        ) { products, recipes -> products to recipes }
            .onEach { (products, recipes) ->
                _state.update { it.copy(products = products, recipes = recipes) }
            }
            .launchIn(viewModelScope)
    }

    fun removeFavoriteProduct(product: FoodPreferenceProduct) {
        viewModelScope.launch {
            tokenManager.toggleFavoriteProductId(product.id, product.name)
        }
    }

    fun removeFavoriteRecipe(recipe: FavoriteRecipeItem) {
        viewModelScope.launch {
            tokenManager.toggleFavoriteRecipeId(recipe.id, recipe.title)
        }
    }
}
