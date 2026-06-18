package com.abdulin.nutritionapp.data.mapper

import com.abdulin.nutritionapp.data.dto.analytics.MealPlanPreferenceProfileDto
import com.abdulin.nutritionapp.data.dto.analytics.MealPlanDecisionSignalDto
import com.abdulin.nutritionapp.data.dto.analytics.MealPlanInsightDto
import com.abdulin.nutritionapp.data.dto.analytics.MealPlanReportDto
import com.abdulin.nutritionapp.data.dto.analytics.MealPlanReportSliceDto
import com.abdulin.nutritionapp.domain.model.MealPlanDecisionSignalModel
import com.abdulin.nutritionapp.domain.model.MealPlanInsightModel
import com.abdulin.nutritionapp.domain.model.MealPlanPreferenceProfileModel
import com.abdulin.nutritionapp.domain.model.MealPlanReportModel
import com.abdulin.nutritionapp.domain.model.MealPlanReportSliceModel

fun MealPlanReportDto.toDomain(): MealPlanReportModel {
    return MealPlanReportModel(
        generatedDays = generatedDays,
        totalGeneratedMeals = totalGeneratedMeals,
        averageMealsPerDay = averageMealsPerDay,
        mealLoggingRate = mealLoggingRate,
        mealReplacementRate = mealReplacementRate,
        replacementReasons = replacementReasons.map { it.toDomain() },
        cuisines = cuisines.map { it.toDomain() },
        repeatedRecipes = repeatedRecipes.map { it.toDomain() },
        mealTypePreferences = mealTypePreferences.map { it.toDomain() },
        decisionSignals = decisionSignals.map { it.toDomain() },
        summaryInsights = summaryInsights.map { it.toDomain() },
        loggedCount = loggedCount,
        replacedCount = replacedCount,
        removedCount = removedCount,
        dislikedCount = dislikedCount,
        pinnedCount = pinnedCount,
        manualAddedCount = manualAddedCount
    )
}

private fun MealPlanReportSliceDto.toDomain(): MealPlanReportSliceModel {
    return MealPlanReportSliceModel(
        key = key,
        label = label,
        count = count,
        share = share
    )
}

private fun MealPlanPreferenceProfileDto.toDomain(): MealPlanPreferenceProfileModel {
    return MealPlanPreferenceProfileModel(
        mealType = mealType,
        dominantReason = dominantReason,
        confidence = confidence ?: 0.0,
        learnedSignalStrength = learnedSignalStrength,
        preferredCookTimeMin = preferredCookTimeMin,
        preferredCalories = preferredCalories,
        preferredProtein = preferredProtein
    )
}

private fun MealPlanDecisionSignalDto.toDomain(): MealPlanDecisionSignalModel {
    return MealPlanDecisionSignalModel(
        mealType = mealType,
        signalKey = signalKey,
        signalLabel = signalLabel,
        source = source,
        evidenceCount = evidenceCount,
        confidence = confidence ?: 0.0,
        learnedSignalStrength = learnedSignalStrength,
        preferredCookTimeMin = preferredCookTimeMin,
        preferredCalories = preferredCalories,
        preferredProtein = preferredProtein,
        explanation = explanation
    )
}

private fun MealPlanInsightDto.toDomain(): MealPlanInsightModel {
    return MealPlanInsightModel(
        key = key,
        title = title,
        description = description,
        score = score
    )
}
