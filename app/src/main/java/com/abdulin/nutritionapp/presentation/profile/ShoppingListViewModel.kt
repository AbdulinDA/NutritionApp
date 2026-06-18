package com.abdulin.nutritionapp.presentation.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdulin.nutritionapp.R
import com.abdulin.nutritionapp.core.utils.Resource
import com.abdulin.nutritionapp.core.utils.TokenManager
import com.abdulin.nutritionapp.domain.model.ShoppingListItem
import com.abdulin.nutritionapp.domain.repository.MealPlanRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ShoppingListUiState(
    val items: List<ShoppingListItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val checkedItems: Set<String> = emptySet(),
    val pendingPurchasedCount: Int = 0,
    val atHomeCount: Int = 0,
    val hiddenCount: Int = 0,
    val planId: Long = 0L
)

@HiltViewModel
class ShoppingListViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: MealPlanRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private companion object {
        const val LATEST_SHOPPING_LIST_KEY = -1L
    }

    private val _state = MutableStateFlow(ShoppingListUiState())
    val state = _state.asStateFlow()

    init {
        loadShoppingList()
    }

    fun loadShoppingList() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            val latestResult = repository.getLatestShoppingList()
            val planId = tokenManager.lastMealPlanId.first() ?: 0L
            val result = if (latestResult is Resource.Success && !latestResult.data.isNullOrEmpty()) {
                latestResult
            } else {
                if (planId == 0L) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = context.getString(R.string.shopping_list_plan_not_found)
                    )
                    return@launch
                }
                repository.getShoppingList(planId)
            }

            when (result) {
                is Resource.Success -> {
                    val effectivePlanId = if (planId > 0L) planId else LATEST_SHOPPING_LIST_KEY
                    val purchasedKeys = if (effectivePlanId != 0L) {
                        tokenManager.getPurchasedShoppingItemKeys(effectivePlanId)
                    } else {
                        emptySet()
                    }
                    val atHomeKeys = if (effectivePlanId != 0L) {
                        tokenManager.getAtHomeShoppingItemKeys(effectivePlanId)
                    } else {
                        emptySet()
                    }
                    val hiddenKeys = if (effectivePlanId != 0L) {
                        tokenManager.getHiddenShoppingItemKeys(effectivePlanId)
                    } else {
                        emptySet()
                    }
                    val manualItems = if (effectivePlanId != 0L) {
                        tokenManager.getManualShoppingItems(effectivePlanId)
                    } else {
                        emptyList()
                    }
                    val mergedItems = ((result.data ?: emptyList()) + manualItems)
                        .distinctBy { it.purchaseKey() }
                    val visibleItems = (result.data ?: emptyList()).filterNot { purchasedKeys.contains(it.purchaseKey()) }
                    val checkedKeys = if (effectivePlanId != 0L) {
                        tokenManager.getCheckedShoppingItemKeys(effectivePlanId)
                            .intersect(mergedItems.mapTo(mutableSetOf()) { it.purchaseKey() })
                    } else {
                        emptySet()
                    }
                    val filteredItems = mergedItems.filterNot { item ->
                        val key = item.purchaseKey()
                        key in purchasedKeys || key in atHomeKeys || key in hiddenKeys
                    }
                    _state.value = _state.value.copy(
                        items = filteredItems,
                        isLoading = false,
                        checkedItems = checkedKeys,
                        pendingPurchasedCount = checkedKeys.size,
                        atHomeCount = atHomeKeys.size,
                        hiddenCount = hiddenKeys.size,
                        planId = effectivePlanId
                    )
                }

                is Resource.Error -> {
                    _state.value = _state.value.copy(
                        error = result.message,
                        isLoading = false
                    )
                }

                is Resource.Loading -> {
                    _state.value = _state.value.copy(isLoading = true)
                }
            }
        }
    }

    fun toggleItem(itemKey: String) {
        val currentState = _state.value
        if (currentState.planId == 0L) return

        val updatedChecked = currentState.checkedItems.toMutableSet().apply {
            if (!add(itemKey)) {
                remove(itemKey)
            }
        }
        _state.value = currentState.copy(
            checkedItems = updatedChecked,
            pendingPurchasedCount = updatedChecked.size
        )
        viewModelScope.launch {
            tokenManager.saveCheckedShoppingItemKeys(currentState.planId, updatedChecked)
        }
    }

    fun persistCheckedItems() {
        val currentState = _state.value
        if (currentState.planId == 0L || currentState.checkedItems.isEmpty()) return

        viewModelScope.launch {
            tokenManager.markShoppingItemKeysPurchased(currentState.planId, currentState.checkedItems)
            _state.value = currentState.copy(
                items = currentState.items.filterNot { it.purchaseKey() in currentState.checkedItems },
                checkedItems = emptySet(),
                pendingPurchasedCount = 0
            )
        }
    }

    fun markCheckedItemsAtHome() {
        val currentState = _state.value
        if (currentState.planId == 0L || currentState.checkedItems.isEmpty()) return

        viewModelScope.launch {
            tokenManager.markShoppingItemKeysAtHome(currentState.planId, currentState.checkedItems)
            _state.value = currentState.copy(
                items = currentState.items.filterNot { it.purchaseKey() in currentState.checkedItems },
                checkedItems = emptySet(),
                pendingPurchasedCount = 0,
                atHomeCount = currentState.atHomeCount + currentState.checkedItems.size
            )
        }
    }

    fun hideItem(itemKey: String) {
        val currentState = _state.value
        if (currentState.planId == 0L) return

        viewModelScope.launch {
            tokenManager.hideShoppingItemKeys(currentState.planId, setOf(itemKey))
            _state.value = currentState.copy(
                items = currentState.items.filterNot { it.purchaseKey() == itemKey },
                checkedItems = currentState.checkedItems - itemKey,
                pendingPurchasedCount = (currentState.checkedItems - itemKey).size,
                hiddenCount = currentState.hiddenCount + 1
            )
        }
    }

    fun addManualItem(
        name: String,
        quantityText: String,
        unit: String,
        category: String
    ): Boolean {
        val currentState = _state.value
        if (currentState.planId == 0L) return false

        val normalizedName = name.trim()
        val normalizedCategory = category.trim().ifBlank {
            context.getString(R.string.shopping_list_category_fallback)
        }
        val quantity = quantityText.replace(',', '.').toDoubleOrNull()
        if (normalizedName.isBlank() || quantity == null || quantity <= 0.0 || unit.isBlank()) {
            return false
        }

        val item = ShoppingListItem(
            productId = -System.currentTimeMillis(),
            productName = normalizedName,
            category = normalizedCategory,
            totalQuantity = quantity,
            unit = unit.trim(),
            isManual = true
        )
        viewModelScope.launch {
            tokenManager.saveManualShoppingItem(currentState.planId, item)
            _state.value = currentState.copy(
                items = (currentState.items + item).sortedWith(compareBy<ShoppingListItem> { it.category }.thenBy { it.productName })
            )
        }
        return true
    }
}

private fun ShoppingListItem.purchaseKey(): String {
    return listOf(
        productId.toString(),
        productName.trim().lowercase(),
        category.trim().lowercase(),
        totalQuantity.toString(),
        unit.trim().lowercase()
    ).joinToString("|")
}
