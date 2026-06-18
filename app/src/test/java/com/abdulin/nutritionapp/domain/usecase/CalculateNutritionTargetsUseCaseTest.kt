package com.abdulin.nutritionapp.domain.usecase

import com.abdulin.nutritionapp.data.dto.auth.UserResponseDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculateNutritionTargetsUseCaseTest {

    private val useCase = CalculateNutritionTargetsUseCase()

    @Test
    fun `uses active multiplier for active users`() {
        val user = baseUser(activityLevel = "active")

        val targets = useCase(user)

        assertEquals(2620.99, targets.calories, 0.01)
        assertEquals(2096, targets.water)
    }

    @Test
    fun `applies calorie deficit when target weight is lower`() {
        val user = baseUser(targetWeightKg = 55.0)

        val targets = useCase(user)

        assertTrue(targets.calories < 1800.0)
        assertEquals(1585.53, targets.calories, 0.01)
    }

    private fun baseUser(
        activityLevel: String = "moderate",
        targetWeightKg: Double = 65.0
    ) = UserResponseDto(
        userId = 1,
        firstName = "Test",
        lastName = "User",
        email = "test@example.com",
        birthDate = "1995-01-01",
        gender = "female",
        heightCm = 170.0,
        weightKg = 59.9,
        targetWeightKg = targetWeightKg,
        activityLevel = activityLevel,
        dietType = "CLASSIC"
    )
}
