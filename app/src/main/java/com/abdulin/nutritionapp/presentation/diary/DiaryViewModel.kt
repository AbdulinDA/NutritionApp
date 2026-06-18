package com.abdulin.nutritionapp.presentation.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdulin.nutritionapp.core.utils.Resource
import com.abdulin.nutritionapp.domain.model.DiarySummary
import com.abdulin.nutritionapp.domain.model.FoodDiaryEntry
import com.abdulin.nutritionapp.domain.usecase.GetFoodDiaryUseCase
import com.abdulin.nutritionapp.domain.usecase.GetDiarySummaryUseCase
import com.abdulin.nutritionapp.domain.repository.FoodDiaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class DiaryUiState(
    val entries: List<FoodDiaryEntry> = emptyList(),
    val summary: DiarySummary? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val userMessage: String? = null,
    val selectedDate: String = ""
)

@HiltViewModel
class DiaryViewModel @Inject constructor(
    private val getFoodDiaryUseCase: GetFoodDiaryUseCase,
    private val getDiarySummaryUseCase: GetDiarySummaryUseCase,
    private val repository: FoodDiaryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DiaryUiState(
        selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    ))
    val state = _state.asStateFlow()

    init {
        loadDiary()
    }

    fun loadDiary() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val selectedDate = _state.value.selectedDate
            when (val result = getFoodDiaryUseCase(selectedDate)) {
                is Resource.Success -> {
                    val summaryResult = getDiarySummaryUseCase(selectedDate)
                    val summary = (summaryResult as? Resource.Success)?.data
                    _state.update {
                        it.copy(
                            entries = result.data ?: emptyList(),
                            summary = summary,
                            isLoading = false
                        )
                    }
                }
                is Resource.Error -> {
                    _state.update { it.copy(error = result.message, summary = null, isLoading = false) }
                }
                is Resource.Loading -> {
                    _state.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun deleteEntry(id: Long) {
        viewModelScope.launch {
            val result = repository.deleteDiaryEntry(id)
            when (result) {
                is Resource.Success -> {
                    _state.update {
                        it.copy(
                            entries = it.entries.filterNot { entry -> entry.id == id },
                            userMessage = "delete_success"
                        )
                    }
                    loadDiary()
                }
                is Resource.Error -> {
                    _state.update { it.copy(userMessage = result.message ?: "delete_error") }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun clearUserMessage() {
        _state.update { it.copy(userMessage = null) }
    }

    fun changeDate(offset: Int) {
        val calendar = Calendar.getInstance()
        val current = try {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(_state.value.selectedDate) ?: Date()
        } catch (e: Exception) {
            Date()
        }
        calendar.time = current
        calendar.add(Calendar.DAY_OF_YEAR, offset)
        val newDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        _state.update { it.copy(selectedDate = newDate) }
        loadDiary()
    }
}
