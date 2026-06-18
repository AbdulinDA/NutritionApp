package com.abdulin.nutritionapp.data.dto.auth

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UserResponseDtoSerializationTest {

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        coerceInputValues = true
    }

    @Test
    fun `deserializes null preference lists as empty`() {
        val payload = """
            {
              "userId": 1,
              "firstName": "Ivan",
              "weightKg": 82.0,
              "targetWeightKg": 78.0,
              "activityLevel": "moderate",
              "allergies": null,
              "excludedProductsIds": null,
              "favoriteCuisines": null
            }
        """.trimIndent()

        val dto = json.decodeFromString<UserResponseDto>(payload)

        assertTrue(dto.allergies.isEmpty())
        assertTrue(dto.excludedProductsIds.isEmpty())
        assertTrue(dto.favoriteCuisines.isEmpty())
    }

    @Test
    fun `keeps non null preference lists intact`() {
        val payload = """
            {
              "userId": 2,
              "firstName": "Anna",
              "weightKg": 60.0,
              "targetWeightKg": 58.0,
              "activityLevel": "light",
              "allergies": ["nuts"],
              "excludedProductsIds": [5, 7],
              "favoriteCuisines": ["italian", "asian"]
            }
        """.trimIndent()

        val dto = json.decodeFromString<UserResponseDto>(payload)

        assertEquals(listOf("nuts"), dto.allergies)
        assertEquals(listOf(5L, 7L), dto.excludedProductsIds)
        assertEquals(listOf("italian", "asian"), dto.favoriteCuisines)
    }
}
