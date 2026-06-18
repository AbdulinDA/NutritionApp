package com.abdulin.nutritionapp.data.dto.mealplan

import kotlinx.serialization.Serializable

@Serializable
data class ReplaceMealInPlanRequestDto(
    val reason: String? = null
)
