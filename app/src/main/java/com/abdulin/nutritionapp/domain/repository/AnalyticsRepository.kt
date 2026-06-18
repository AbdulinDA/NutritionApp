package com.abdulin.nutritionapp.domain.repository

import com.abdulin.nutritionapp.core.utils.Resource
import com.abdulin.nutritionapp.data.remote.AnalyticsEventDto
import com.abdulin.nutritionapp.domain.model.MealPlanReportModel
import com.abdulin.nutritionapp.domain.model.RecommendationReportModel

interface AnalyticsRepository {
    suspend fun sendEvent(
        eventType: String,
        entityType: String? = null,
        entityId: String? = null,
        metadata: Map<String, String> = emptyMap()
    )

    suspend fun getRecommendationReport(): Resource<RecommendationReportModel>

    suspend fun getMealPlanReport(): Resource<MealPlanReportModel>
}
