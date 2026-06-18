package com.abdulin.nutritionapp.data.dto.analytics

import com.abdulin.nutritionapp.core.network.ApiResponse
import com.abdulin.nutritionapp.data.mapper.toDomain
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class MealPlanReportDtoSerializationTest {

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        coerceInputValues = true
    }

    @Test
    fun `deserializes null decision signal confidence`() {
        val payload = """
            {
              "success": true,
              "data": {
                "generatedDays": 3,
                "decisionSignals": [
                  {
                    "mealType": "BREAKFAST",
                    "signalKey": "cook_time",
                    "signalLabel": "Fast breakfast",
                    "source": "behavior",
                    "evidenceCount": 7,
                    "confidence": null,
                    "learnedSignalStrength": 7.2
                  }
                ]
              }
            }
        """.trimIndent()

        val response = json.decodeFromString<ApiResponse<MealPlanReportDto>>(payload)
        val report = response.data!!.toDomain()

        assertEquals(0.0, report.decisionSignals.first().confidence, 0.0)
    }
}
