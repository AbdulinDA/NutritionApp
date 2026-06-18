package com.abdulin.nutritionapp.presentation.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdulin.nutritionapp.core.utils.Resource
import com.abdulin.nutritionapp.data.dto.diary.CreateFoodDiaryRequestDto
import com.abdulin.nutritionapp.domain.model.SavedMealTemplate
import com.abdulin.nutritionapp.domain.repository.FoodDiaryRepository
import com.abdulin.nutritionapp.domain.usecase.AddFoodDiaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class AddFoodUiState(
    val submissionState: Resource<Unit> = Resource.Success(Unit),
    val savedMeals: List<SavedMealTemplate> = emptyList(),
    val templateMessage: String? = null
)

@HiltViewModel
class AddFoodViewModel @Inject constructor(
    private val addFoodDiaryUseCase: AddFoodDiaryUseCase,
    private val foodDiaryRepository: FoodDiaryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AddFoodUiState())
    val state = _state.asStateFlow()

    init {
        foodDiaryRepository.observeSavedMealTemplates()
            .onEach { templates ->
                _state.update { it.copy(savedMeals = templates) }
            }
            .launchIn(viewModelScope)
    }

    fun addFood(
        productId: Long,
        productName: String,
        weight: Double,
        mealType: String
    ) {
        viewModelScope.launch {
            _state.update { it.copy(submissionState = Resource.Loading(), templateMessage = null) }

            val request = buildRequest(
                mealType = mealType,
                source = "PRODUCT",
                productId = productId,
                customName = productName,
                weightGrams = weight
            )

            _state.update { it.copy(submissionState = addFoodDiaryUseCase(request)) }
        }
    }

    fun addRecipe(
        recipeId: Long,
        recipeName: String,
        mealType: String
    ) {
        viewModelScope.launch {
            _state.update { it.copy(submissionState = Resource.Loading(), templateMessage = null) }

            val request = buildRequest(
                mealType = mealType,
                source = "RECIPE",
                recipeId = recipeId,
                customName = recipeName,
                weightGrams = 300.0
            )

            _state.update { it.copy(submissionState = addFoodDiaryUseCase(request)) }
        }
    }

    fun addCustomFood(
        name: String,
        calories: Double,
        protein: Double,
        fat: Double,
        carbs: Double,
        weight: Double,
        mealType: String
    ) {
        viewModelScope.launch {
            _state.update { it.copy(submissionState = Resource.Loading(), templateMessage = null) }

            val request = buildRequest(
                mealType = mealType,
                source = "CUSTOM",
                customName = name,
                calories = calories,
                protein = protein,
                fat = fat,
                carbs = carbs,
                weightGrams = weight
            )

            _state.update { it.copy(submissionState = addFoodDiaryUseCase(request)) }
        }
    }

    fun addSavedMeal(template: SavedMealTemplate) {
        viewModelScope.launch {
            _state.update { it.copy(submissionState = Resource.Loading(), templateMessage = null) }
            val request = buildRequest(
                mealType = template.mealType,
                source = template.source,
                productId = template.productId,
                recipeId = template.recipeId,
                customName = template.customName,
                calories = template.calories,
                protein = template.protein,
                fat = template.fat,
                carbs = template.carbs,
                weightGrams = template.weightGrams
            )
            _state.update { it.copy(submissionState = addFoodDiaryUseCase(request)) }
        }
    }

    fun saveCurrentAsTemplate(templateName: String, request: CreateFoodDiaryRequestDto) {
        if (templateName.isBlank()) {
            _state.update { it.copy(templateMessage = "empty_template_name") }
            return
        }
        viewModelScope.launch {
            when (foodDiaryRepository.saveMealTemplate(templateName, request)) {
                is Resource.Success -> _state.update {
                    it.copy(templateMessage = "template_saved")
                }
                is Resource.Error -> _state.update {
                    it.copy(templateMessage = "template_save_failed")
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun deleteSavedMealTemplate(id: Long) {
        viewModelScope.launch {
            when (foodDiaryRepository.deleteSavedMealTemplate(id)) {
                is Resource.Success -> _state.update { it.copy(templateMessage = "template_deleted") }
                is Resource.Error -> _state.update { it.copy(templateMessage = "template_delete_failed") }
                is Resource.Loading -> Unit
            }
        }
    }

    fun clearTemplateMessage() {
        _state.update { it.copy(templateMessage = null) }
    }

    fun resetState() {
        _state.update { it.copy(submissionState = Resource.Success(Unit), templateMessage = null) }
    }

    private fun buildRequest(
        mealType: String,
        source: String,
        productId: Long? = null,
        recipeId: Long? = null,
        customName: String? = null,
        calories: Double? = null,
        protein: Double? = null,
        fat: Double? = null,
        carbs: Double? = null,
        weightGrams: Double
    ): CreateFoodDiaryRequestDto {
        val now = Date()
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now)
        val dateTimeStr = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(now)

        return CreateFoodDiaryRequestDto(
            mealType = mealType,
            source = source,
            productId = productId,
            recipeId = recipeId,
            customName = customName,
            calories = calories,
            protein = protein,
            fat = fat,
            carbs = carbs,
            weightGrams = weightGrams,
            entryDate = dateStr,
            consumedAt = dateTimeStr
        )
    }
}
