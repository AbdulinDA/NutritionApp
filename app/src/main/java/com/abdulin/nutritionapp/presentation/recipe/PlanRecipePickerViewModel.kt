package com.abdulin.nutritionapp.presentation.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdulin.nutritionapp.core.utils.Resource
import com.abdulin.nutritionapp.domain.repository.MealPlanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlanRecipePickerUiState(
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class PlanRecipePickerViewModel @Inject constructor(
    private val mealPlanRepository: MealPlanRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PlanRecipePickerUiState())
    val state = _state.asStateFlow()

    fun addRecipeToPlan(recipeId: Long, planDate: String, mealType: String) {
        if (_state.value.isSaving) return

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null, isSaved = false) }
            when (val result = mealPlanRepository.addRecipeToMealPlan(recipeId, planDate, mealType)) {
                is Resource.Success -> {
                    _state.update { it.copy(isSaving = false, isSaved = true, error = null) }
                }
                is Resource.Error -> {
                    _state.update { it.copy(isSaving = false, error = result.message, isSaved = false) }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun consumeResult() {
        _state.update { it.copy(isSaved = false, error = null) }
    }
}
