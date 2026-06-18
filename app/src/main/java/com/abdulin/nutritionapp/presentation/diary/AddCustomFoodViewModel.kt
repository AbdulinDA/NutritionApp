package com.abdulin.nutritionapp.presentation.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdulin.nutritionapp.core.utils.Resource
import com.abdulin.nutritionapp.data.dto.diary.CreateFoodDiaryRequestDto
import com.abdulin.nutritionapp.domain.repository.FoodDiaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AddCustomFoodViewModel @Inject constructor(
    private val repository: FoodDiaryRepository
) : ViewModel() {

    private val _state = MutableStateFlow<Resource<Unit>>(Resource.Success(Unit))
    val state = _state.asStateFlow()

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
            _state.value = Resource.Loading()
            val now = Date()
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now)
            val timeStr = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(now)

            val result = repository.addFood(
                CreateFoodDiaryRequestDto(
                    mealType = mealType,
                    source = "CUSTOM",
                    customName = name,
                    calories = calories,
                    protein = protein,
                    fat = fat,
                    carbs = carbs,
                    weightGrams = weight,
                    entryDate = dateStr,
                    consumedAt = timeStr
                )
            )
            _state.value = result
        }
    }
}
