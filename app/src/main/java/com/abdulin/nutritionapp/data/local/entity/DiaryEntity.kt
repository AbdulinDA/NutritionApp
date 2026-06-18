package com.abdulin.nutritionapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diary_entries")
data class DiaryEntity(
    @PrimaryKey val id: Long,
    val mealType: String,
    val source: String,
    val consumedAt: String,
    val productId: Long? = null,
    val recipeId: Long? = null,
    val productName: String,
    val weightGrams: Double,
    val calories: Double,
    val protein: Double = 0.0,
    val fat: Double = 0.0,
    val carbs: Double = 0.0,
    val entryDate: String, // yyyy-MM-dd
    val isSynced: Boolean = true
)
