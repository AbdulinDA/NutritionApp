package com.abdulin.nutritionapp.data.dto.user

import kotlinx.serialization.Serializable

@Serializable
data class UpdateMyProfileRequestDto(
    val firstName: String? = null,
    val lastName: String? = null,
    val birthDate: String? = null,
    val heightCm: Double? = null,
    val targetWeightKg: Double? = null,
    val activityLevel: String? = null,
    val dietType: String? = null,
    val goalTypeId: Long? = null,
    val gender: String? = null,
    val allergies: List<String>? = null,
    val excludedProductsIds: List<Long>? = null,
    val favoriteCuisines: List<String>? = null,
    val dislikedCuisines: List<String>? = null,
    val onboardingCompleted: Boolean? = null
)
