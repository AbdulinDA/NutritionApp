package com.abdulin.nutritionapp.data.dto.user

import kotlinx.serialization.Serializable

@Serializable
data class UpdateProfileRequestDto(
    val email: String,
    val password: String? = null,
    val firstName: String,
    val lastName: String,
    val birthDate: String? = null,
    val gender: String? = null,
    val heightCm: Double? = null,
    val weightKg: Double? = null,
    val activityLevel: String? = null,
    val targetWeightKg: Double? = null,
    val dietType: String? = null
)
