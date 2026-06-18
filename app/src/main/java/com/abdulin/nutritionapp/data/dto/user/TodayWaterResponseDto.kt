package com.abdulin.nutritionapp.data.dto.user

import kotlinx.serialization.Serializable

@Serializable
data class TodayWaterResponseDto(
    val amountMl: Int = 0
)
