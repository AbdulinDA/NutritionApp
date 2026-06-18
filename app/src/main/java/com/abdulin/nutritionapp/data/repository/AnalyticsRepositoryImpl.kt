package com.abdulin.nutritionapp.data.repository

import android.content.Context
import android.util.Log
import com.abdulin.nutritionapp.R
import com.abdulin.nutritionapp.core.network.safeApiCall
import com.abdulin.nutritionapp.core.utils.Resource
import com.abdulin.nutritionapp.data.dto.analytics.MealPlanReportDto
import com.abdulin.nutritionapp.data.dto.analytics.RecommendationReportDto
import com.abdulin.nutritionapp.data.mapper.toDomain
import com.abdulin.nutritionapp.data.remote.AnalyticsEventDto
import com.abdulin.nutritionapp.data.remote.NutritionApi
import com.abdulin.nutritionapp.domain.model.MealPlanReportModel
import com.abdulin.nutritionapp.domain.model.RecommendationReportModel
import com.abdulin.nutritionapp.domain.repository.AnalyticsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import javax.inject.Inject

class AnalyticsRepositoryImpl @Inject constructor(
    private val api: NutritionApi,
    @ApplicationContext private val context: Context
) : AnalyticsRepository {
    override suspend fun sendEvent(
        eventType: String,
        entityType: String?,
        entityId: String?,
        metadata: Map<String, String>
    ) {
        runCatching {
            api.sendAnalyticsEvent(
                AnalyticsEventDto(
                    eventType = eventType,
                    entityType = entityType,
                    entityId = entityId,
                    metadata = metadata.takeIf { it.isNotEmpty() }?.let { Json.encodeToString(it) }
                )
            )
        }.onFailure { error ->
            Log.w("AnalyticsRepository", "Failed to send analytics event: $eventType", error)
        }
    }

    override suspend fun getRecommendationReport(): Resource<RecommendationReportModel> {
        val result = safeApiCall<RecommendationReportDto> {
            api.getRecommendationReport()
        }

        return when (result) {
            is Resource.Success -> Resource.Success(result.data!!.toDomain())
            is Resource.Error -> Resource.Error(result.message ?: context.getString(R.string.error_unknown))
            is Resource.Loading -> Resource.Loading()
        }
    }

    override suspend fun getMealPlanReport(): Resource<MealPlanReportModel> {
        val result = safeApiCall<MealPlanReportDto> {
            api.getMealPlanReport()
        }

        return when (result) {
            is Resource.Success -> Resource.Success(result.data!!.toDomain())
            is Resource.Error -> Resource.Error(result.message ?: context.getString(R.string.error_unknown))
            is Resource.Loading -> Resource.Loading()
        }
    }
}
