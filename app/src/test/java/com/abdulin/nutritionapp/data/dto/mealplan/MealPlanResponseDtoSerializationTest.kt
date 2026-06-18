package com.abdulin.nutritionapp.data.dto.mealplan

import com.abdulin.nutritionapp.core.network.ApiResponse
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MealPlanResponseDtoSerializationTest {

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        coerceInputValues = true
    }

    @Test
    fun `deserializes null recipe collections in generated meal plan`() {
        val payload = """
            {
              "success": true,
              "data": {
                "id": 10,
                "days": [
                  {
                    "date": "2026-06-16",
                    "meals": [
                      {
                        "planRecipeId": 100,
                        "mealType": "BREAKFAST",
                        "portionSize": 1.0,
                        "recipe": {
                          "recipeId": 5,
                          "recipeName": "Омлет",
                          "recommendedSideDishes": null,
                          "ingredients": null,
                          "totalTimeMin": 40,
                          "calories": 320
                        }
                      }
                    ]
                  }
                ]
              }
            }
        """.trimIndent()

        val response = json.decodeFromString<ApiResponse<MealPlanResponseDto>>(payload)
        val recipe = response.data?.days?.firstOrNull()?.meals?.firstOrNull()?.recipe

        assertEquals("Омлет", recipe?.title)
        assertTrue(recipe?.recommendedSideDishes.isNullOrEmpty())
        assertTrue(recipe?.ingredients.isNullOrEmpty())
    }
}
