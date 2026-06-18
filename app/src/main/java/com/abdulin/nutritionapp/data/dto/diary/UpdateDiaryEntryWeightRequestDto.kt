package com.abdulin.nutritionapp.data.dto.diary

import kotlinx.serialization.Serializable

@Serializable
data class UpdateDiaryEntryWeightRequestDto(
    val weightGrams: Double
)
