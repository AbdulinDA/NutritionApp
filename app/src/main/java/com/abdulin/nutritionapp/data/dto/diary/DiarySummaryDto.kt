package com.abdulin.nutritionapp.data.dto.diary

import kotlinx.serialization.Serializable

@Serializable
data class DiarySummaryDto(

    val totalCalories: Double,

    val totalProtein: Double,

    val totalFat: Double,

    val totalCarbs: Double
)