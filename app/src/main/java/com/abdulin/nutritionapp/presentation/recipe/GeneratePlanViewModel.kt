package com.abdulin.nutritionapp.presentation.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdulin.nutritionapp.core.utils.Resource
import com.abdulin.nutritionapp.data.dto.mealplan.MealPlanRequestDto
import com.abdulin.nutritionapp.domain.model.MealType
import com.abdulin.nutritionapp.domain.model.PantryItemModel
import com.abdulin.nutritionapp.domain.repository.MealPlanRepository
import com.abdulin.nutritionapp.domain.repository.PantryRepository
import com.abdulin.nutritionapp.domain.usecase.CalculateNutritionTargetsUseCase
import com.abdulin.nutritionapp.domain.usecase.GetMyProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class GeneratePlanUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isGenerated: Boolean = false,
    val days: Int = 3,
    val dietType: String = "CLASSIC",
    val dailyCalories: Int = 2000,
    val goalTypeId: Long? = null,
    val startDate: LocalDate = LocalDate.now(),
    val mealPrepServings: Int = 1,
    val selectedMealPrepMealTypes: Set<MealType> = setOf(MealType.LUNCH, MealType.DINNER),
    val pantryProducts: List<PantryItemModel> = emptyList(),
    val selectedProductIds: Set<Long> = emptySet(),
    val availableCuisines: List<String> = listOf("RUSSIAN", "ITALIAN", "ASIAN", "MEDITERRANEAN", "MEXICAN", "GEORGIAN"),
    val selectedPreferredCuisines: Set<String> = emptySet()
)

@HiltViewModel
class GeneratePlanViewModel @Inject constructor(
    private val mealPlanRepository: MealPlanRepository,
    private val pantryRepository: PantryRepository,
    private val getMyProfileUseCase: GetMyProfileUseCase,
    private val calculateNutritionTargetsUseCase: CalculateNutritionTargetsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(GeneratePlanUiState())
    val state = _state.asStateFlow()

    init {
        loadPlanDefaults()
        loadPantryProducts()
    }

    fun updateDays(days: Int) {
        _state.update { it.copy(days = days) }
    }

    fun updateDiet(diet: String) {
        _state.update { it.copy(dietType = diet) }
    }

    fun updateStartDate(startDate: LocalDate) {
        _state.update { it.copy(startDate = startDate) }
    }

    fun updateMealPrepServings(servings: Int) {
        _state.update { it.copy(mealPrepServings = servings.coerceAtLeast(1)) }
    }

    fun toggleMealPrepMealType(mealType: MealType) {
        _state.update { state ->
            val updated = state.selectedMealPrepMealTypes.toMutableSet()
            if (!updated.add(mealType)) {
                updated.remove(mealType)
            }
            state.copy(
                selectedMealPrepMealTypes = if (updated.isEmpty()) {
                    setOf(MealType.LUNCH, MealType.DINNER)
                } else {
                    updated
                }
            )
        }
    }

    fun toggleProduct(productId: Long) {
        _state.update { state ->
            val updated = state.selectedProductIds.toMutableSet()
            if (!updated.add(productId)) {
                updated.remove(productId)
            }
            state.copy(selectedProductIds = updated)
        }
    }

    fun togglePreferredCuisine(cuisine: String) {
        _state.update { state ->
            val updated = state.selectedPreferredCuisines.toMutableSet()
            if (!updated.add(cuisine)) {
                updated.remove(cuisine)
            }
            state.copy(selectedPreferredCuisines = updated)
        }
    }

    private fun loadPlanDefaults() {
        viewModelScope.launch {
            val profileResult = getMyProfileUseCase()
            if (profileResult !is Resource.Success) {
                return@launch
            }

            val profile = profileResult.data ?: return@launch
            val targets = calculateNutritionTargetsUseCase(profile)
            _state.update {
                val preferredCuisineDefaults = profile.favoriteCuisines
                    .map { cuisine -> cuisine.trim().uppercase() }
                    .filter { cuisine -> it.availableCuisines.contains(cuisine) }
                    .toSet()
                it.copy(
                    dietType = profile.dietType ?: it.dietType,
                    dailyCalories = targets.calories.toInt(),
                    goalTypeId = profile.goalTypeId,
                    selectedPreferredCuisines = if (it.selectedPreferredCuisines.isEmpty()) {
                        preferredCuisineDefaults
                    } else {
                        it.selectedPreferredCuisines
                    }
                )
            }
        }
    }

    private fun loadPantryProducts() {
        viewModelScope.launch {
            when (val result = pantryRepository.getPantry()) {
                is Resource.Success -> {
                    val products = result.data.orEmpty()
                    _state.update {
                        it.copy(
                            pantryProducts = products,
                            selectedProductIds = products.map(PantryItemModel::productId).toSet()
                        )
                    }
                }
                is Resource.Error -> Unit
                is Resource.Loading -> Unit
            }
        }
    }

    fun generatePlan() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            val request = MealPlanRequestDto(
                days = _state.value.days,
                dailyCalories = _state.value.dailyCalories,
                goalTypeId = _state.value.goalTypeId,
                startDate = _state.value.startDate.toString(),
                householdSize = 1,
                mealPrepServings = _state.value.mealPrepServings,
                mealPrepMealTypes = _state.value.selectedMealPrepMealTypes.map { it.name },
                selectedProductIds = _state.value.selectedProductIds.toList(),
                preferredCuisines = _state.value.selectedPreferredCuisines.toList()
            )

            val result = mealPlanRepository.generateMealPlan(request)
            when (result) {
                is Resource.Success -> {
                    _state.update { it.copy(isLoading = false, isGenerated = true) }
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
}
