package com.abdulin.nutritionapp.domain.model

data class MealPlanReportModel(
    val generatedDays: Int = 0,
    val totalGeneratedMeals: Int = 0,
    val averageMealsPerDay: Double = 0.0,
    val mealLoggingRate: Double = 0.0,
    val mealReplacementRate: Double = 0.0,
    val replacementReasons: List<MealPlanReportSliceModel> = emptyList(),
    val cuisines: List<MealPlanReportSliceModel> = emptyList(),
    val repeatedRecipes: List<MealPlanReportSliceModel> = emptyList(),
    val mealTypePreferences: List<MealPlanPreferenceProfileModel> = emptyList(),
    val decisionSignals: List<MealPlanDecisionSignalModel> = emptyList(),
    val summaryInsights: List<MealPlanInsightModel> = emptyList(),
    val loggedCount: Int = 0,
    val replacedCount: Int = 0,
    val removedCount: Int = 0,
    val dislikedCount: Int = 0,
    val pinnedCount: Int = 0,
    val manualAddedCount: Int = 0
)

data class MealPlanReportSliceModel(
    val key: String = "",
    val label: String = "",
    val count: Int = 0,
    val share: Double = 0.0
)

data class MealPlanPreferenceProfileModel(
    val mealType: String = "",
    val dominantReason: String = "",
    val confidence: Double = 0.0,
    val learnedSignalStrength: Double? = null,
    val preferredCookTimeMin: Double? = null,
    val preferredCalories: Double? = null,
    val preferredProtein: Double? = null
)

data class MealPlanDecisionSignalModel(
    val mealType: String = "",
    val signalKey: String = "",
    val signalLabel: String = "",
    val source: String = "",
    val evidenceCount: Int = 0,
    val confidence: Double = 0.0,
    val learnedSignalStrength: Double? = null,
    val preferredCookTimeMin: Double? = null,
    val preferredCalories: Double? = null,
    val preferredProtein: Double? = null,
    val explanation: String = ""
)

data class MealPlanInsightModel(
    val key: String = "",
    val title: String = "",
    val description: String = "",
    val score: Double? = null
)
