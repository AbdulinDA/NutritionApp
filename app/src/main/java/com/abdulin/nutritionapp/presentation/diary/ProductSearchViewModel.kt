package com.abdulin.nutritionapp.presentation.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.abdulin.nutritionapp.core.utils.Resource
import com.abdulin.nutritionapp.core.utils.TokenManager
import com.abdulin.nutritionapp.domain.model.ProductModel
import com.abdulin.nutritionapp.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProductSearchUiState(
    val searchQuery: String = "",
    val selectedProduct: ProductModel? = null,
    val scannedProduct: ProductModel? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val favoritesIds: Set<Long> = emptySet(),
    val lastScannedBarcode: String? = null
)

@HiltViewModel
class ProductSearchViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _state = MutableStateFlow(ProductSearchUiState())
    val state = _state.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val productsPagingData: Flow<PagingData<ProductModel>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            productRepository.searchProductsPaging(query)
        }
        .cachedIn(viewModelScope)

    init {
        tokenManager.favoriteProductIds
            .onEach { favorites ->
                _state.update { it.copy(favoritesIds = favorites) }
            }
            .launchIn(viewModelScope)
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        _state.update { it.copy(searchQuery = query) }
    }

    fun searchByBarcode(barcode: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, lastScannedBarcode = barcode) }
            val result = productRepository.getProductByBarcode(barcode)
            when (result) {
                is Resource.Success -> {
                    _state.update { it.copy(scannedProduct = result.data, isLoading = false) }
                }
                is Resource.Error -> {
                    _state.update { it.copy(error = result.message, isLoading = false) }
                }
                else -> Unit
            }
        }
    }

    fun retryLastBarcode() {
        val barcode = _state.value.lastScannedBarcode ?: return
        searchByBarcode(barcode)
    }

    fun toggleFavorite(productId: Long, productName: String? = null) {
        viewModelScope.launch {
            val wasFavorite = _state.value.favoritesIds.contains(productId)
            val result = productRepository.toggleFavorite(productId)
            if (result is Resource.Success || wasFavorite) {
                tokenManager.toggleFavoriteProductId(productId, productName)
            }
        }
    }

    fun selectProduct(product: ProductModel) {
        _state.update { it.copy(selectedProduct = product) }
    }

    fun clearScannedProduct() {
        _state.update { it.copy(scannedProduct = null) }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
