package com.abdulin.nutritionapp.presentation.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdulin.nutritionapp.core.utils.Resource
import com.abdulin.nutritionapp.data.dto.diary.CreateFoodDiaryRequestDto
import com.abdulin.nutritionapp.domain.model.MealPlanModel
import com.abdulin.nutritionapp.domain.model.MealPlanReportModel
import com.abdulin.nutritionapp.domain.model.MealPlanMealModel
import com.abdulin.nutritionapp.domain.model.RecipeCompositionModel
import com.abdulin.nutritionapp.domain.model.MealPlanMealExplanationModel
import com.abdulin.nutritionapp.domain.model.RecipeShortModel
import com.abdulin.nutritionapp.domain.repository.AnalyticsRepository
import com.abdulin.nutritionapp.domain.repository.FoodDiaryRepository
import com.abdulin.nutritionapp.domain.repository.MealPlanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class MealPlanUiState(
    val plan: MealPlanModel? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val logSuccess: Boolean = false,
    val loggingRecipeIds: Set<Long> = emptySet(),
    val updatingMealIds: Set<Long> = emptySet(),
    val updatingDayDates: Set<String> = emptySet(),
    val isUpdatingUpcomingDays: Boolean = false,
    val loadingCompositionMealIds: Set<Long> = emptySet(),
    val selectedMealComposition: RecipeCompositionModel? = null,
    val selectedMealExplanation: MealPlanMealExplanationModel? = null,
    val selectedMealForComposition: MealPlanMealModel? = null,
    val mealPlanReport: MealPlanReportModel? = null,
    val compositionError: String? = null,
    val explanationError: String? = null,
    val userMessage: String? = null
)

@HiltViewModel
class MealPlanViewModel @Inject constructor(
    private val mealPlanRepository: MealPlanRepository,
    private val diaryRepository: FoodDiaryRepository,
    private val analyticsRepository: AnalyticsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MealPlanUiState())
    val state = _uiState.asStateFlow()

    init {
        observeLatestPlan()
        refreshLatestPlan()
        loadMealPlanReport()
    }

    private fun observeLatestPlan() {
        _uiState.update { it.copy(isLoading = true) }
        mealPlanRepository.getLatestPlan()
            .onEach { plan ->
                _uiState.update { it.copy(plan = plan, isLoading = false) }
            }
            .catch { e ->
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    fun refreshLatestPlan() {
        viewModelScope.launch {
            when (val result = mealPlanRepository.refreshLatestPlan()) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            plan = result.data,
                            error = null,
                            isLoading = false
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update { current ->
                        if (current.plan == null || isMissingPlanMessage(result.message)) {
                            current.copy(plan = null, error = result.message, isLoading = false)
                        } else {
                            current.copy(error = null, isLoading = false)
                        }
                    }
                }
                is Resource.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun removeMealFromPlan(planRecipeId: Long) {
        mutateMealPlan(planRecipeId, "meal_removed") {
            mealPlanRepository.removeMealFromPlan(planRecipeId)
        }
    }

    fun replaceMealInPlan(planRecipeId: Long, reason: String) {
        mutateMealPlan(planRecipeId, "meal_replaced") {
            mealPlanRepository.replaceMealInPlan(planRecipeId, reason)
        }
    }

    fun dislikeMealFromPlan(planRecipeId: Long) {
        mutateMealPlan(planRecipeId, "meal_disliked") {
            mealPlanRepository.dislikeMealFromPlan(planRecipeId)
        }
    }

    fun toggleMealPin(planRecipeId: Long) {
        mutateMealPlan(planRecipeId, "meal_pin_toggled") {
            mealPlanRepository.toggleMealPin(planRecipeId)
        }
    }

    fun replanRemainingDay() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, userMessage = null) }
            when (val result = mealPlanRepository.replanRemainingDay()) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            plan = result.data?.takeIf { plan -> plan.days.isNotEmpty() },
                            isLoading = false,
                            error = null,
                            userMessage = "day_replanned"
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message,
                            userMessage = "day_replan_failed"
                        )
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun fillEmptySlotsForDay(planDate: String) {
        if (_uiState.value.updatingDayDates.contains(planDate)) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    updatingDayDates = it.updatingDayDates + planDate,
                    error = null,
                    userMessage = null
                )
            }
            when (val result = mealPlanRepository.fillEmptySlotsForDay(planDate)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            plan = result.data?.takeIf { plan -> plan.days.isNotEmpty() },
                            updatingDayDates = it.updatingDayDates - planDate,
                            error = null,
                            userMessage = "day_slots_filled"
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            updatingDayDates = it.updatingDayDates - planDate,
                            error = result.message,
                            userMessage = "day_slots_fill_failed"
                        )
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun replanDay(planDate: String) {
        if (_uiState.value.updatingDayDates.contains(planDate)) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    updatingDayDates = it.updatingDayDates + planDate,
                    error = null,
                    userMessage = null
                )
            }
            when (val result = mealPlanRepository.replanDay(planDate)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            plan = result.data?.takeIf { plan -> plan.days.isNotEmpty() },
                            updatingDayDates = it.updatingDayDates - planDate,
                            error = null,
                            userMessage = "day_rebuilt"
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            updatingDayDates = it.updatingDayDates - planDate,
                            error = result.message,
                            userMessage = "day_rebuild_failed"
                        )
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun replanUpcomingDays(days: Int = 3) {
        if (_uiState.value.isUpdatingUpcomingDays) return

        viewModelScope.launch {
            _uiState.update { it.copy(isUpdatingUpcomingDays = true, error = null, userMessage = null) }
            when (val result = mealPlanRepository.replanUpcomingDays(days)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            plan = result.data?.takeIf { plan -> plan.days.isNotEmpty() },
                            isUpdatingUpcomingDays = false,
                            error = null,
                            userMessage = "upcoming_days_replanned"
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isUpdatingUpcomingDays = false,
                            error = result.message,
                            userMessage = "upcoming_days_replan_failed"
                        )
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun repeatDayToNext(planDate: String) {
        if (_uiState.value.updatingDayDates.contains(planDate)) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    updatingDayDates = it.updatingDayDates + planDate,
                    error = null,
                    userMessage = null
                )
            }
            when (val result = mealPlanRepository.repeatDayToNext(planDate)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            plan = result.data?.takeIf { plan -> plan.days.isNotEmpty() },
                            updatingDayDates = it.updatingDayDates - planDate,
                            error = null,
                            userMessage = "day_repeated_to_next"
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            updatingDayDates = it.updatingDayDates - planDate,
                            error = result.message,
                            userMessage = "day_repeat_failed"
                        )
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun logMealFromPlan(mealType: String, recipe: RecipeShortModel, planRecipeId: Long) {
        if (_uiState.value.loggingRecipeIds.contains(recipe.id)) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    loggingRecipeIds = it.loggingRecipeIds + recipe.id,
                    error = null,
                    userMessage = null
                )
            }
            val now = Date()
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now)
            val timeStr = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(now)

            val result = diaryRepository.addFood(
                CreateFoodDiaryRequestDto(
                    mealType = mealType,
                    source = "RECIPE",
                    recipeId = recipe.id,
                    planRecipeId = planRecipeId,
                    customName = recipe.title,
                    calories = recipe.totalCalories,
                    protein = recipe.totalProtein,
                    fat = recipe.totalFat,
                    carbs = recipe.totalCarbs,
                    weightGrams = 300.0,
                    entryDate = dateStr,
                    consumedAt = timeStr
                )
            )
            if (result is Resource.Success) {
                when (val replanResult = mealPlanRepository.replanRemainingDay()) {
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                plan = replanResult.data?.takeIf { plan -> plan.days.isNotEmpty() },
                                logSuccess = true,
                                loggingRecipeIds = it.loggingRecipeIds - recipe.id,
                                userMessage = "meal_logged"
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                logSuccess = true,
                                loggingRecipeIds = it.loggingRecipeIds - recipe.id,
                                error = replanResult.message,
                                userMessage = "meal_logged_replan_failed"
                            )
                        }
                    }
                    is Resource.Loading -> {
                        _uiState.update {
                            it.copy(
                                logSuccess = true,
                                loggingRecipeIds = it.loggingRecipeIds - recipe.id,
                                userMessage = "meal_logged"
                            )
                        }
                    }
                }
            } else {
                _uiState.update {
                    it.copy(
                        error = result.message,
                        loggingRecipeIds = it.loggingRecipeIds - recipe.id,
                        userMessage = "meal_log_failed"
                    )
                }
            }
        }
    }

    fun resetLogSuccess() {
        _uiState.update { it.copy(logSuccess = false) }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    private fun loadMealPlanReport() {
        viewModelScope.launch {
            when (val result = analyticsRepository.getMealPlanReport()) {
                is Resource.Success -> _uiState.update { it.copy(mealPlanReport = result.data) }
                is Resource.Error -> Unit
                is Resource.Loading -> Unit
            }
        }
    }

    fun loadMealComposition(meal: MealPlanMealModel) {
        if (_uiState.value.loadingCompositionMealIds.contains(meal.planRecipeId)) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    loadingCompositionMealIds = it.loadingCompositionMealIds + meal.planRecipeId,
                    compositionError = null,
                    explanationError = null
                )
            }
            val compositionResult = mealPlanRepository.getMealComposition(meal.planRecipeId)
            val explanationResult = mealPlanRepository.getMealExplanation(meal.planRecipeId)
            when (compositionResult) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            loadingCompositionMealIds = it.loadingCompositionMealIds - meal.planRecipeId,
                            selectedMealComposition = compositionResult.data,
                            selectedMealExplanation = (explanationResult as? Resource.Success)?.data,
                            selectedMealForComposition = meal,
                            compositionError = null,
                            explanationError = (explanationResult as? Resource.Error)?.message
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            loadingCompositionMealIds = it.loadingCompositionMealIds - meal.planRecipeId,
                            compositionError = compositionResult.message,
                            explanationError = (explanationResult as? Resource.Error)?.message,
                            userMessage = "meal_composition_failed"
                        )
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun dismissMealComposition() {
        _uiState.update {
            it.copy(
                selectedMealComposition = null,
                selectedMealExplanation = null,
                selectedMealForComposition = null,
                compositionError = null,
                explanationError = null
            )
        }
    }

    private fun mutateMealPlan(
        planRecipeId: Long,
        successMessage: String,
        action: suspend () -> Resource<MealPlanModel>
    ) {
        if (_uiState.value.updatingMealIds.contains(planRecipeId)) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    updatingMealIds = it.updatingMealIds + planRecipeId,
                    error = null,
                    userMessage = null
                )
            }
            when (val result = action()) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            plan = result.data?.takeIf { plan -> plan.days.isNotEmpty() },
                            updatingMealIds = it.updatingMealIds - planRecipeId,
                            error = null,
                            userMessage = successMessage
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            updatingMealIds = it.updatingMealIds - planRecipeId,
                            error = result.message,
                            userMessage = "meal_action_failed"
                        )
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    private fun isMissingPlanMessage(message: String?): Boolean {
        val normalized = message?.lowercase().orEmpty()
        return normalized.contains("meal plan not found")
                || normalized.contains("план питания не найден")
                || normalized.contains("not found")
    }
}
