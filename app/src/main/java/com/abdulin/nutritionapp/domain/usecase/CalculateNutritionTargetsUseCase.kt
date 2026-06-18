package com.abdulin.nutritionapp.domain.usecase

import com.abdulin.nutritionapp.core.utils.normalizeUiActivityLevel
import com.abdulin.nutritionapp.data.dto.auth.UserResponseDto
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class NutritionTargets(
    val calories: Double,
    val protein: Double,
    val fat: Double,
    val carbs: Double,
    val water: Int
)

class CalculateNutritionTargetsUseCase @Inject constructor() {
    operator fun invoke(user: UserResponseDto): NutritionTargets {
        val weight = user.weightKg ?: 75.0
        val height = user.heightCm ?: 170.0
        val age = calculateAge(user.birthDate) ?: 30
        val isMale = user.gender?.uppercase() == "MALE"
        
        // Mifflin-St Jeor Equation
        val bmr = if (isMale) {
            10 * weight + 6.25 * height - 5 * age + 5
        } else {
            10 * weight + 6.25 * height - 5 * age - 161
        }
        
        val activityFactor = when ((user.activityLevel ?: "MODERATE").normalizeUiActivityLevel()) {
            "SEDENTARY" -> 1.2
            "LIGHT" -> 1.375
            "MODERATE" -> 1.55
            "ACTIVE" -> 1.725
            "VERY_ACTIVE" -> 1.725
            else -> 1.2
        }
        
        var targetCalories = bmr * activityFactor
        
        // Adjust for target weight
        val targetWeight = user.targetWeightKg ?: weight
        if (targetWeight < weight) {
            targetCalories -= 500 // Deficit for weight loss
        } else if (targetWeight > weight) {
            targetCalories += 300 // Surplus for muscle gain
        }

        // Macros calculation (Example: 30% Protein, 30% Fat, 40% Carbs)
        val protein = (targetCalories * 0.30) / 4
        val fat = (targetCalories * 0.30) / 9
        val carbs = (targetCalories * 0.40) / 4
        
        // Water: 35ml per kg of weight
        val water = (weight * 35).toInt()
        
        return NutritionTargets(
            calories = targetCalories,
            protein = protein,
            fat = fat,
            carbs = carbs,
            water = water
        )
    }

    private fun calculateAge(birthDate: String?): Int? {
        if (birthDate == null) return null
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val birth = sdf.parse(birthDate) ?: return null
            val today = Calendar.getInstance()
            val birthCalendar = Calendar.getInstance().apply { time = birth }
            var age = today.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR)
            if (today.get(Calendar.DAY_OF_YEAR) < birthCalendar.get(Calendar.DAY_OF_YEAR)) {
                age--
            }
            age
        } catch (e: Exception) {
            null
        }
    }
}
