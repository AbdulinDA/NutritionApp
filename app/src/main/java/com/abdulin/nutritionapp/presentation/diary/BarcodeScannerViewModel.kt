package com.abdulin.nutritionapp.presentation.diary

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdulin.nutritionapp.R
import com.abdulin.nutritionapp.core.utils.Resource
import com.abdulin.nutritionapp.domain.model.ProductModel
import com.abdulin.nutritionapp.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BarcodeScannerState(
    val scannedProduct: ProductModel? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class BarcodeScannerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _state = MutableStateFlow(BarcodeScannerState())
    val state = _state.asStateFlow()

    fun onBarcodeDetected(barcode: String) {
        if (_state.value.isLoading) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val result = productRepository.getProductByBarcode(barcode)

            when (result) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        scannedProduct = result.data,
                        isLoading = false
                    )
                }

                is Resource.Error -> {
                    _state.value = _state.value.copy(
                        error = result.message ?: context.getString(R.string.products_not_found_short),
                        isLoading = false
                    )
                }

                is Resource.Loading -> {
                    _state.value = _state.value.copy(isLoading = true)
                }
            }
        }
    }

    fun resetError() {
        _state.value = _state.value.copy(error = null)
    }
}
