package com.abdulin.nutritionapp.presentation.analytics

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdulin.nutritionapp.R
import com.abdulin.nutritionapp.core.utils.Resource
import com.abdulin.nutritionapp.core.utils.TokenManager
import com.abdulin.nutritionapp.domain.model.MealPlanReportModel
import com.abdulin.nutritionapp.domain.model.NutritionQualityReport
import com.abdulin.nutritionapp.domain.model.RecommendationReportModel
import com.abdulin.nutritionapp.domain.repository.AnalyticsRepository
import com.abdulin.nutritionapp.domain.usecase.GetDiarySummaryUseCase
import com.abdulin.nutritionapp.domain.usecase.GetNutritionQualityUseCase
import com.abdulin.nutritionapp.domain.usecase.GetWeeklyWaterStatsUseCase
import com.abdulin.nutritionapp.domain.usecase.GetWeightHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

data class AnalyticsUiState(
    val caloriesStats: List<Pair<String, Double>> = emptyList(),
    val waterStats: List<Pair<String, Int>> = emptyList(),
    val weightStats: List<Pair<String, Double>> = emptyList(),
    val qualityReport: NutritionQualityReport? = null,
    val recommendationReport: RecommendationReportModel? = null,
    val mealPlanReport: MealPlanReportModel? = null,
    val overviewCards: List<AnalyticsOverviewCard> = emptyList(),
    val learnedInsights: List<String> = emptyList(),
    val preferenceProfile: AnalyticsPreferenceProfile = AnalyticsPreferenceProfile(),
    val behaviorSignals: List<AnalyticsBehaviorSignal> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val aiInsight: String = "…",
    val selectedTab: AnalyticsTab = AnalyticsTab.QUALITY
)

enum class AnalyticsTab {
    QUALITY, PLAN, CALORIES, WATER, WEIGHT
}

data class AnalyticsOverviewCard(
    val title: String,
    val value: String,
    val subtitle: String
)

data class AnalyticsPreferenceProfile(
    val favoriteCuisines: List<String> = emptyList(),
    val dislikedCuisines: List<String> = emptyList(),
    val allergyProducts: List<String> = emptyList(),
    val excludedProducts: List<String> = emptyList(),
    val favoriteProductsCount: Int = 0,
    val excludedProductsCount: Int = 0,
    val allergyProductsCount: Int = 0
)

data class AnalyticsBehaviorSignal(
    val title: String,
    val subtitle: String
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val analyticsRepository: AnalyticsRepository,
    private val getDiarySummaryUseCase: GetDiarySummaryUseCase,
    private val getWeightHistoryUseCase: GetWeightHistoryUseCase,
    private val getWeeklyWaterStatsUseCase: GetWeeklyWaterStatsUseCase,
    private val getNutritionQualityUseCase: GetNutritionQualityUseCase,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(AnalyticsTab.QUALITY)
    private val _isLoading = MutableStateFlow(false)
    private val _caloriesStats = MutableStateFlow<List<Pair<String, Double>>>(emptyList())
    private val _qualityReport = MutableStateFlow<NutritionQualityReport?>(null)
    private val _recommendationReport = MutableStateFlow<RecommendationReportModel?>(null)
    private val _mealPlanReport = MutableStateFlow<MealPlanReportModel?>(null)
    private val weightHistory = getWeightHistoryUseCase()
    private val waterHistory = getWeeklyWaterStatsUseCase()
    private val baseState = combine(
        combine(
            _selectedTab,
            _isLoading,
            _caloriesStats,
            _qualityReport,
            _recommendationReport
        ) { tab, loading, calories, quality, recommendationReport ->
            BaseAnalyticsState(
                selectedTab = tab,
                isLoading = loading,
                caloriesStats = calories,
                qualityReport = quality,
                recommendationReport = recommendationReport,
                mealPlanReport = null
            )
        },
        _mealPlanReport
    ) { base, mealPlanReport ->
        BaseAnalyticsState(
            selectedTab = base.selectedTab,
            isLoading = base.isLoading,
            caloriesStats = base.caloriesStats,
            qualityReport = base.qualityReport,
            recommendationReport = base.recommendationReport,
            mealPlanReport = mealPlanReport
        )
    }
    private val preferenceProfile = combine(
        tokenManager.favoriteProducts,
        tokenManager.excludedProducts,
        tokenManager.allergyProducts,
        tokenManager.favoriteCuisines,
        tokenManager.dislikedCuisines
    ) { favoriteProducts, excludedProducts, allergyProducts, favoriteCuisines, dislikedCuisines ->
        AnalyticsPreferenceProfile(
            favoriteCuisines = favoriteCuisines,
            dislikedCuisines = dislikedCuisines,
            allergyProducts = allergyProducts.map { it.name }.distinctBy { it.lowercase() }.sortedBy { it.lowercase() },
            excludedProducts = excludedProducts.map { it.name }.distinctBy { it.lowercase() }.sortedBy { it.lowercase() },
            favoriteProductsCount = favoriteProducts.size,
            excludedProductsCount = excludedProducts.size,
            allergyProductsCount = allergyProducts.size
        )
    }

    val state: StateFlow<AnalyticsUiState> = combine(
        baseState,
        weightHistory,
        waterHistory,
        preferenceProfile
    ) { base, weightLogs, waterLogs, preferenceProfile ->
        AnalyticsUiState(
            selectedTab = base.selectedTab,
            isLoading = base.isLoading,
            caloriesStats = base.caloriesStats,
            qualityReport = base.qualityReport,
            recommendationReport = base.recommendationReport,
            mealPlanReport = base.mealPlanReport,
            overviewCards = buildOverviewCards(
                caloriesStats = base.caloriesStats,
                waterStats = waterLogs.map { it.date to it.total }.reversed(),
                weightStats = weightLogs.take(7).map { it.date to it.weightKg }.reversed(),
                qualityReport = base.qualityReport,
                mealPlanReport = base.mealPlanReport
            ),
            learnedInsights = buildLearnedInsights(base.mealPlanReport),
            preferenceProfile = preferenceProfile,
            behaviorSignals = buildBehaviorSignals(base.mealPlanReport, preferenceProfile),
            weightStats = weightLogs.take(7).map { it.date to it.weightKg }.reversed(),
            waterStats = waterLogs.map { it.date to it.total }.reversed(),
            aiInsight = base.qualityReport?.verdict ?: generateInsight(base.caloriesStats),
            error = null
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AnalyticsUiState(isLoading = true))

    init {
        loadData()
    }

    fun onTabSelected(tab: AnalyticsTab) {
        _selectedTab.value = tab
    }

    fun refresh() {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true

            _qualityReport.value = getNutritionQualityUseCase()
            _recommendationReport.value = when (val reportResult = analyticsRepository.getRecommendationReport()) {
                is Resource.Success -> reportResult.data
                else -> null
            }
            _mealPlanReport.value = when (val reportResult = analyticsRepository.getMealPlanReport()) {
                is Resource.Success -> reportResult.data
                else -> null
            }

            val calendar = Calendar.getInstance()
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val stats = mutableListOf<Pair<String, Double>>()
            repeat(7) {
                val date = sdf.format(calendar.time)
                val result = getDiarySummaryUseCase(date)
                if (result is Resource.Success) {
                    stats.add(date to (result.data?.totalCalories ?: 0.0))
                }
                calendar.add(Calendar.DAY_OF_YEAR, -1)
            }
            _caloriesStats.value = stats.reversed()
            _isLoading.value = false
        }
    }

    private fun generateInsight(calories: List<Pair<String, Double>>): String {
        if (calories.isEmpty() || calories.all { it.second == 0.0 }) {
            return context.getString(R.string.analytics_insight_start_logging)
        }
        val avgCalories = calories.filter { it.second > 0 }.map { it.second }.average()
        return if (avgCalories > 2500) {
            context.getString(R.string.analytics_insight_high_calories)
        } else {
            context.getString(R.string.analytics_insight_stable)
        }
    }

    private fun buildOverviewCards(
        caloriesStats: List<Pair<String, Double>>,
        waterStats: List<Pair<String, Int>>,
        weightStats: List<Pair<String, Double>>,
        qualityReport: NutritionQualityReport?,
        mealPlanReport: MealPlanReportModel?
    ): List<AnalyticsOverviewCard> {
        val avgCalories = caloriesStats
            .map { it.second }
            .filter { it > 0.0 }
            .average()
            .takeIf { !it.isNaN() }
            ?.toInt()
            ?: 0
        val avgWater = waterStats
            .map { it.second }
            .filter { it > 0 }
            .average()
            .takeIf { !it.isNaN() }
            ?.toInt()
            ?: 0
        val currentWeight = weightStats.lastOrNull()?.second
        val planAdherence = mealPlanReport?.mealTypePreferences
            ?.maxOfOrNull { it.confidence }
            ?.let { formatPercent(it) }
            ?: context.getString(R.string.analytics_value_no_data)
        val qualityValue = qualityReport?.healthIndex?.toString()
            ?: context.getString(R.string.analytics_value_no_data)

        return listOf(
            AnalyticsOverviewCard(
                title = context.getString(R.string.home_calories),
                value = if (avgCalories > 0) "$avgCalories ${context.getString(R.string.unit_kcal)}" else context.getString(R.string.analytics_value_no_data),
                subtitle = context.getString(R.string.analytics_overview_calories_subtitle)
            ),
            AnalyticsOverviewCard(
                title = context.getString(R.string.home_water),
                value = if (avgWater > 0) "$avgWater ${context.getString(R.string.unit_ml)}" else context.getString(R.string.analytics_value_no_data),
                subtitle = context.getString(R.string.analytics_overview_water_subtitle)
            ),
            AnalyticsOverviewCard(
                title = context.getString(R.string.home_weight),
                value = currentWeight?.let { "$it ${context.getString(R.string.unit_kg)}" }
                    ?: context.getString(R.string.analytics_value_no_data),
                subtitle = context.getString(R.string.analytics_overview_weight_subtitle)
            ),
            AnalyticsOverviewCard(
                title = context.getString(R.string.analytics_health_index),
                value = qualityValue,
                subtitle = context.getString(R.string.analytics_overview_quality_subtitle)
            ),
            AnalyticsOverviewCard(
                title = context.getString(R.string.profile_meal_plan),
                value = planAdherence,
                subtitle = context.getString(R.string.analytics_overview_plan_subtitle)
            ),
            AnalyticsOverviewCard(
                title = context.getString(R.string.analytics_meal_plan_logging_rate),
                value = mealPlanReport?.let { formatPercent(it.mealLoggingRate) }
                    ?: context.getString(R.string.analytics_value_no_data),
                subtitle = context.getString(R.string.analytics_meal_plan_logging_rate_subtitle)
            ),
            AnalyticsOverviewCard(
                title = context.getString(R.string.analytics_meal_plan_replacement_rate),
                value = mealPlanReport?.let { formatPercent(it.mealReplacementRate) }
                    ?: context.getString(R.string.analytics_value_no_data),
                subtitle = context.getString(R.string.analytics_meal_plan_replacement_rate_subtitle)
            )
        )
    }

    private fun buildLearnedInsights(mealPlanReport: MealPlanReportModel?): List<String> {
        if (mealPlanReport == null) {
            return emptyList()
        }

        if (mealPlanReport.summaryInsights.isNotEmpty()) {
            return mealPlanReport.summaryInsights.mapNotNull { insight ->
                when {
                    insight.title.isNotBlank() && insight.description.isNotBlank() ->
                        "${insight.title}. ${insight.description}"
                    insight.description.isNotBlank() -> insight.description
                    insight.title.isNotBlank() -> insight.title
                    else -> null
                }
            }
        }

        return mealPlanReport.mealTypePreferences.mapNotNull { preference ->
            val mealLabel = preference.mealType.lowercase().replaceFirstChar { it.uppercase() }
            val confidenceText = formatPercent(preference.confidence)
            when {
                preference.preferredCookTimeMin != null && preference.preferredCookTimeMin > 0 ->
                    context.getString(
                        R.string.analytics_learned_cook_time,
                        mealLabel,
                        preference.preferredCookTimeMin.toInt(),
                        confidenceText
                    )
                preference.preferredProtein != null && preference.preferredProtein > 0 ->
                    context.getString(
                        R.string.analytics_learned_protein,
                        mealLabel,
                        preference.preferredProtein.toInt(),
                        confidenceText
                    )
                preference.preferredCalories != null && preference.preferredCalories > 0 ->
                    context.getString(
                        R.string.analytics_learned_calories,
                        mealLabel,
                        preference.preferredCalories.toInt(),
                        confidenceText
                    )
                preference.dominantReason.isNotBlank() ->
                    context.getString(
                        R.string.analytics_learned_reason,
                        mealLabel,
                        preference.dominantReason,
                        confidenceText
                    )
                else -> null
            }
        }
    }

    private fun buildBehaviorSignals(
        mealPlanReport: MealPlanReportModel?,
        preferenceProfile: AnalyticsPreferenceProfile
    ): List<AnalyticsBehaviorSignal> {
        val signals = mutableListOf<AnalyticsBehaviorSignal>()

        if (preferenceProfile.favoriteCuisines.isNotEmpty()) {
            signals += AnalyticsBehaviorSignal(
                title = context.getString(R.string.analytics_signal_cuisines_title),
                subtitle = context.getString(
                    R.string.analytics_signal_cuisines_subtitle,
                    preferenceProfile.favoriteCuisines.take(3).joinToString(),
                    preferenceProfile.favoriteCuisines.size
                )
            )
        }

        if (preferenceProfile.dislikedCuisines.isNotEmpty()) {
            signals += AnalyticsBehaviorSignal(
                title = context.getString(R.string.analytics_signal_disliked_cuisines_title),
                subtitle = context.getString(
                    R.string.analytics_signal_disliked_cuisines_subtitle,
                    preferenceProfile.dislikedCuisines.take(3).joinToString(),
                    preferenceProfile.dislikedCuisines.size
                )
            )
        }

        if (preferenceProfile.allergyProducts.isNotEmpty()) {
            signals += AnalyticsBehaviorSignal(
                title = context.getString(R.string.analytics_signal_allergies_title),
                subtitle = context.getString(
                    R.string.analytics_signal_allergies_subtitle,
                    preferenceProfile.allergyProducts.take(3).joinToString(),
                    preferenceProfile.allergyProducts.size
                )
            )
        }

        if (preferenceProfile.favoriteProductsCount > 0) {
            signals += AnalyticsBehaviorSignal(
                title = context.getString(R.string.analytics_signal_favorites_title),
                subtitle = context.getString(
                    R.string.analytics_signal_favorites_subtitle,
                    preferenceProfile.favoriteProductsCount
                )
            )
        }

        if (preferenceProfile.excludedProductsCount > 0 || preferenceProfile.allergyProductsCount > 0) {
            signals += AnalyticsBehaviorSignal(
                title = context.getString(R.string.analytics_signal_filters_title),
                subtitle = context.getString(
                    R.string.analytics_signal_filters_subtitle,
                    preferenceProfile.excludedProductsCount,
                    preferenceProfile.allergyProductsCount
                )
            )
        }

        if (mealPlanReport == null) {
            return signals
        }

        mealPlanReport.replacementReasons.firstOrNull()?.let { topReason ->
            signals += AnalyticsBehaviorSignal(
                title = context.getString(R.string.analytics_signal_replacements_title),
                subtitle = context.getString(
                    R.string.analytics_signal_replacements_subtitle,
                    topReason.label.ifBlank { topReason.key },
                    topReason.count
                )
            )
        }

        if (mealPlanReport.pinnedCount > 0 || mealPlanReport.manualAddedCount > 0) {
            signals += AnalyticsBehaviorSignal(
                title = context.getString(R.string.analytics_signal_manual_control_title),
                subtitle = context.getString(
                    R.string.analytics_signal_manual_control_subtitle,
                    mealPlanReport.pinnedCount,
                    mealPlanReport.manualAddedCount
                )
            )
        }

        mealPlanReport.repeatedRecipes.firstOrNull()?.let { repeated ->
            signals += AnalyticsBehaviorSignal(
                title = context.getString(R.string.analytics_signal_repeats_title),
                subtitle = context.getString(
                    R.string.analytics_signal_repeats_subtitle,
                    repeated.label.ifBlank { repeated.key },
                    repeated.count
                )
            )
        }

        return signals
    }

    private fun formatPercent(value: Double): String = "${(value * 100).toInt()}%"

    private data class BaseAnalyticsState(
        val selectedTab: AnalyticsTab,
        val isLoading: Boolean,
        val caloriesStats: List<Pair<String, Double>>,
        val qualityReport: NutritionQualityReport?,
        val recommendationReport: RecommendationReportModel?,
        val mealPlanReport: MealPlanReportModel?
    )
}
