package com.abdulin.nutritionapp.data.dto.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeightLogRequestDto(
    @SerialName("weightKg")
    val weight: Double,
    @SerialName("measuredAt")
    val date: String
)
