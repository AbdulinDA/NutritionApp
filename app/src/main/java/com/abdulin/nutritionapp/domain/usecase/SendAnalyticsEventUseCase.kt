package com.abdulin.nutritionapp.domain.usecase

import com.abdulin.nutritionapp.domain.repository.AnalyticsRepository
import javax.inject.Inject

class SendAnalyticsEventUseCase @Inject constructor(
    private val repository: AnalyticsRepository
) {
    suspend operator fun invoke(
        eventType: String,
        entityType: String? = null,
        entityId: String? = null,
        metadata: Map<String, String> = emptyMap()
    ) = repository.sendEvent(eventType, entityType, entityId, metadata)
}
