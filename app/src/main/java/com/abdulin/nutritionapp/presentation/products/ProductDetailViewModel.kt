package com.abdulin.nutritionapp.presentation.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdulin.nutritionapp.core.utils.TokenManager
import com.abdulin.nutritionapp.core.utils.Resource
import com.abdulin.nutritionapp.domain.model.ProductModel
import com.abdulin.nutritionapp.domain.repository.ProductRepository
import com.abdulin.nutritionapp.domain.usecase.GetProductAlternativesUseCase
import com.abdulin.nutritionapp.domain.usecase.ToggleFavoriteProductUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProductDetailUiState(
    val product: ProductModel? = null,
    val alternatives: List<ProductModel> = emptyList(),
    val isFavorite: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val getAlternativesUseCase: GetProductAlternativesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteProductUseCase,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _state = MutableStateFlow(ProductDetailUiState())
    val state = _state.asStateFlow()

    fun init(product: ProductModel, isFavorite: Boolean = false) {
        _state.update { it.copy(product = product, isFavorite = isFavorite) }
        viewModelScope.launch {
            _state.update { it.copy(isFavorite = tokenManager.isFavoriteProduct(product.id)) }
        }
        loadAlternatives(product)
    }

    private fun loadAlternatives(product: ProductModel) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val result = getAlternativesUseCase(product)
            if (result is Resource.Success) {
                _state.update { it.copy(alternatives = result.data ?: emptyList(), isLoading = false) }
            } else {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun toggleFavorite() {
        val product = _state.value.product ?: return
        viewModelScope.launch {
            val wasFavorite = _state.value.isFavorite
            val result = toggleFavoriteUseCase(product.id)
            if (result is Resource.Success || wasFavorite) {
                val isFavorite = tokenManager.toggleFavoriteProductId(product.id, product.name)
                _state.update { it.copy(isFavorite = isFavorite) }
            }
        }
    }
}
