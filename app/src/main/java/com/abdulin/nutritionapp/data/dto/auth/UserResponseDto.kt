package com.abdulin.nutritionapp.data.dto.auth

import com.abdulin.nutritionapp.data.dto.common.LongListOrEmptySerializer
import com.abdulin.nutritionapp.data.dto.common.StringListOrEmptySerializer
import kotlinx.serialization.Serializable

@Serializable
data class UserResponseDto(
    val userId: Int,
    val publicId: String? = null,
    val firstName: String,
    val lastName: String? = null,
    val email: String? = null,
    val birthDate: String? = null,
    val gender: String? = null,
    val heightCm: Double? = null,
    val weightKg: Double? = null,
    val targetWeightKg: Double? = null,
    val activityLevel: String? = null,
    val dietType: String? = null,
    val goalTypeId: Long? = null,
    @Serializable(with = StringListOrEmptySerializer::class)
    val allergies: List<String> = emptyList(),
    @Serializable(with = LongListOrEmptySerializer::class)
    val excludedProductsIds: List<Long> = emptyList(),
    @Serializable(with = StringListOrEmptySerializer::class)
    val favoriteCuisines: List<String> = emptyList(),
    @Serializable(with = StringListOrEmptySerializer::class)
    val dislikedCuisines: List<String> = emptyList(),
    val onboardingCompleted: Boolean? = null
)
