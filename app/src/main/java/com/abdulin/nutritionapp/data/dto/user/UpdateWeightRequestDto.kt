package com.abdulin.nutritionapp.data.dto.user

import kotlinx.serialization.Serializable

@Serializable
data class UpdateWeightRequestDto(
    val weight: Double // Изменено с weightKg для соответствия серверу
)
