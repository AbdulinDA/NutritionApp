package com.abdulin.nutritionapp.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.abdulin.nutritionapp.core.utils.Resource
import com.abdulin.nutritionapp.data.fasting.FastingReminderWorker
import com.abdulin.nutritionapp.data.fasting.FastingState
import com.abdulin.nutritionapp.data.fasting.FastingRepository
import com.abdulin.nutritionapp.data.health.HealthConnectManager
import com.abdulin.nutritionapp.domain.model.FoodDiaryEntry
import com.abdulin.nutritionapp.domain.model.HomeModel
import com.abdulin.nutritionapp.domain.repository.FoodDiaryRepository
import com.abdulin.nutritionapp.domain.usecase.AiAdvice
import com.abdulin.nutritionapp.domain.usecase.CoachContext
import com.abdulin.nutritionapp.domain.usecase.CalculateNutritionTargetsUseCase
import com.abdulin.nutritionapp.domain.usecase.CalculateStreakUseCase
import com.abdulin.nutritionapp.domain.usecase.GetAiAdviceUseCase
import com.abdulin.nutritionapp.domain.usecase.GetHomeDataUseCase
import com.abdulin.nutritionapp.domain.usecase.GetMyProfileUseCase
import com.abdulin.nutritionapp.domain.usecase.LogWaterUseCase
import com.abdulin.nutritionapp.domain.usecase.UpdateWeightUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val homeData: Resource<HomeModel> = Resource.Loading(),
    val advice: AiAdvice? = null,
    val steps: Long = 0,
    val activeCalories: Double = 0.0,
    val sleepMinutes: Long = 0,
    val hasHealthPermissions: Boolean = false,
    val fastingState: FastingState = FastingState()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getHomeDataUseCase: GetHomeDataUseCase,
    private val getMyProfileUseCase: GetMyProfileUseCase,
    private val calculateTargetsUseCase: CalculateNutritionTargetsUseCase,
    private val logWaterUseCase: LogWaterUseCase,
    private val updateWeightUseCase: UpdateWeightUseCase,
    private val getAiAdviceUseCase: GetAiAdviceUseCase,
    private val calculateStreakUseCase: CalculateStreakUseCase,
    private val diaryRepository: FoodDiaryRepository,
    private val healthConnectManager: HealthConnectManager,
    private val fastingRepository: FastingRepository,
    private val workManager: WorkManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    val healthPermissions get() = healthConnectManager.permissions

    init {
        observeFasting()
        loadHome()
    }

    fun loadHome(showLoading: Boolean = true) {
        viewModelScope.launch {
            if (showLoading || _uiState.value.homeData !is Resource.Success) {
                _uiState.update { it.copy(homeData = Resource.Loading()) }
            }

            val hasPermissions = healthConnectManager.hasAllPermissions()
            val steps = if (hasPermissions) healthConnectManager.readStepsForToday() else 0L
            val activeCals = if (hasPermissions) {
                healthConnectManager.readActiveCaloriesForToday()
            } else {
                0.0
            }
            val sleepMinutes = if (hasPermissions) {
                healthConnectManager.readSleepMinutesLastNight()
            } else {
                0L
            }

            val homeResult = getHomeDataUseCase()
            val profileResult = getMyProfileUseCase()
            val loggedDates = diaryRepository.getAllLoggedDates().first()
            val currentStreak = calculateStreakUseCase(loggedDates)
            val recentEntriesByDate = loadRecentEntriesByDate()

            if (homeResult is Resource.Success && profileResult is Resource.Success) {
                val baseTargets = calculateTargetsUseCase(profileResult.data!!)

                val updatedData = homeResult.data!!.copy(
                    targetCalories = baseTargets.calories + activeCals,
                    targetProtein = baseTargets.protein,
                    targetFat = baseTargets.fat,
                    targetCarbs = baseTargets.carbs + (activeCals / 4),
                    targetWater = baseTargets.water + ((steps / 2000) * 250).toInt(),
                    streak = currentStreak
                )

                _uiState.update {
                    it.copy(
                        homeData = Resource.Success(updatedData),
                        advice = getAiAdviceUseCase(
                            updatedData,
                            CoachContext(
                                steps = steps,
                                activeCalories = activeCals,
                                sleepMinutes = sleepMinutes,
                                fastingState = _uiState.value.fastingState,
                                recentEntriesByDate = recentEntriesByDate
                            )
                        ),
                        steps = steps,
                        activeCalories = activeCals,
                        sleepMinutes = sleepMinutes,
                        hasHealthPermissions = hasPermissions
                    )
                }
            } else {
                _uiState.update {
                    it.copy(homeData = homeResult, hasHealthPermissions = hasPermissions)
                }
            }
        }
    }

    private fun observeFasting() {
        viewModelScope.launch {
            fastingRepository.fastingState.collect { fastingState ->
                _uiState.update { it.copy(fastingState = fastingState) }
                if (_uiState.value.homeData is Resource.Success) {
                    loadHome(showLoading = false)
                }
            }
        }
    }

    private suspend fun loadRecentEntriesByDate(): Map<String, List<FoodDiaryEntry>> {
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        val end = LocalDate.now()
        val start = end.minusDays(2)
        val entries = diaryRepository.getEntriesBetweenDates(start.format(formatter), end.format(formatter))
        val fallbackDate = end.format(formatter)
        return entries.groupBy { entry ->
            entry.consumedAt
                .substringBefore('T')
                .takeIf { candidate -> Regex("""\d{4}-\d{2}-\d{2}""").matches(candidate) }
                ?: fallbackDate
        }
    }

    fun logWater(amountMl: Int) {
        viewModelScope.launch {
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val result = logWaterUseCase(amountMl, dateStr)
            if (result is Resource.Success) {
                _uiState.update { current ->
                    val currentHome = (current.homeData as? Resource.Success)?.data
                    if (currentHome != null) {
                        current.copy(
                            homeData = Resource.Success(
                                currentHome.copy(water = currentHome.water + amountMl)
                            )
                        )
                    } else {
                        current
                    }
                }
                loadHome(showLoading = false)
            }
        }
    }

    fun updateWeight(weightKg: Double) {
        viewModelScope.launch {
            val result = updateWeightUseCase(weightKg)
            if (result is Resource.Success) {
                _uiState.update { current ->
                    val currentHome = (current.homeData as? Resource.Success)?.data
                    if (currentHome != null) {
                        current.copy(
                            homeData = Resource.Success(currentHome.copy(weight = weightKg))
                        )
                    } else {
                        current
                    }
                }
                loadHome(showLoading = false)
            }
        }
    }

    fun toggleFasting() {
        viewModelScope.launch {
            val current = _uiState.value.fastingState
            if (current.isActive) {
                val elapsed = System.currentTimeMillis() - current.startMillis
                fastingRepository.stopFasting(markCompleted = elapsed >= current.targetMillis)
                workManager.cancelUniqueWork(FASTING_WORK_NAME)
            } else {
                fastingRepository.startFasting(System.currentTimeMillis(), current.targetHours)
                val request = OneTimeWorkRequestBuilder<FastingReminderWorker>()
                    .setInitialDelay(current.targetHours.toLong(), TimeUnit.HOURS)
                    .build()
                workManager.enqueueUniqueWork(FASTING_WORK_NAME, ExistingWorkPolicy.REPLACE, request)
            }
        }
    }

    companion object {
        private const val FASTING_WORK_NAME = "fasting_finish_reminder"
    }
}
