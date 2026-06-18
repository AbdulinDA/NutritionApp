package com.abdulin.nutritionapp.domain.model


/**
 * Сводка КБЖУ за день
 */
data class DiarySummary(

    val totalCalories: Double,

    val totalProtein: Double,

    val totalFat: Double,

    val totalCarbs: Double
)