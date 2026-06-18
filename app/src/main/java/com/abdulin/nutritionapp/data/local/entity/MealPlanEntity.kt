package com.abdulin.nutritionapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meal_plans")
data class MealPlanEntity(
    @PrimaryKey val id: Long,
    val planJson: String, // Храним всю структуру MealPlanResponseDto как JSON
    val createdAt: Long = System.currentTimeMillis()
)
