package com.abdulin.nutritionapp.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.abdulin.nutritionapp.core.utils.TokenManager
import com.abdulin.nutritionapp.core.utils.Resource
import com.abdulin.nutritionapp.domain.model.PantryItemModel
import com.abdulin.nutritionapp.domain.model.ProductModel
import com.abdulin.nutritionapp.domain.repository.PantryRepository
import com.abdulin.nutritionapp.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
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

data class FridgeUiState(
    val searchQuery: String = "",
    val fridgeProducts: List<PantryItemModel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class FridgeViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val pantryRepository: PantryRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _state = MutableStateFlow(FridgeUiState())
    val state = _state.asStateFlow()

    private val searchQuery = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val productsPagingData: Flow<PagingData<ProductModel>> = searchQuery
        .debounce(250)
        .flatMapLatest(productRepository::searchProductsPaging)
        .cachedIn(viewModelScope)

    init {
        refreshPantry()
    }

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
        _state.update { it.copy(searchQuery = query) }
    }

    fun refreshPantry() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = pantryRepository.getPantry()) {
                is Resource.Success -> {
                    _state.update {
                        it.copy(
                            fridgeProducts = result.data.orEmpty(),
                            isLoading = false,
                            error = null
                        )
                    }
                }
                is Resource.Error -> {
                    _state.update { it.copy(isLoading = false, error = result.message) }
                }
                is Resource.Loading -> {
                    _state.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun toggleFridgeProduct(product: FoodPreferenceProduct) {
        viewModelScope.launch {
            val existing = _state.value.fridgeProducts.firstOrNull { it.productId == product.id }
            val result = if (existing != null) {
                pantryRepository.removePantryItem(existing.pantryItemId)
            } else {
                pantryRepository.addPantryItem(product.id)
            }

            when (result) {
                is Resource.Success -> {
                    tokenManager.toggleFridgeProduct(product)
                    refreshPantry()
                }
                is Resource.Error -> {
                    _state.update { it.copy(error = result.message) }
                }
                is Resource.Loading -> {
                    _state.update { it.copy(isLoading = true) }
                }
            }
        }
    }
}
