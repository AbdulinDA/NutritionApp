package com.abdulin.nutritionapp.data.dto.analytics

import kotlinx.serialization.Serializable

@Serializable
data class MealPlanReportDto(
    val generatedDays: Int = 0,
    val totalGeneratedMeals: Int = 0,
    val averageMealsPerDay: Double = 0.0,
    val mealLoggingRate: Double = 0.0,
    val mealReplacementRate: Double = 0.0,
    val replacementReasons: List<MealPlanReportSliceDto> = emptyList(),
    val cuisines: List<MealPlanReportSliceDto> = emptyList(),
    val repeatedRecipes: List<MealPlanReportSliceDto> = emptyList(),
    val mealTypePreferences: List<MealPlanPreferenceProfileDto> = emptyList(),
    val decisionSignals: List<MealPlanDecisionSignalDto> = emptyList(),
    val summaryInsights: List<MealPlanInsightDto> = emptyList(),
    val loggedCount: Int = 0,
    val replacedCount: Int = 0,
    val removedCount: Int = 0,
    val dislikedCount: Int = 0,
    val pinnedCount: Int = 0,
    val manualAddedCount: Int = 0
)

@Serializable
data class MealPlanReportSliceDto(
    val key: String = "",
    val label: String = "",
    val count: Int = 0,
    val share: Double = 0.0
)

@Serializable
data class MealPlanPreferenceProfileDto(
    val mealType: String = "",
    val dominantReason: String = "",
    val confidence: Double? = 0.0,
    val learnedSignalStrength: Double? = null,
    val preferredCookTimeMin: Double? = null,
    val preferredCalories: Double? = null,
    val preferredProtein: Double? = null
)

@Serializable
data class MealPlanDecisionSignalDto(
    val mealType: String = "",
    val signalKey: String = "",
    val signalLabel: String = "",
    val source: String = "",
    val evidenceCount: Int = 0,
    val confidence: Double? = 0.0,
    val learnedSignalStrength: Double? = null,
    val preferredCookTimeMin: Double? = null,
    val preferredCalories: Double? = null,
    val preferredProtein: Double? = null,
    val explanation: String = ""
)

@Serializable
data class MealPlanInsightDto(
    val key: String = "",
    val title: String = "",
    val description: String = "",
    val score: Double? = null
)
