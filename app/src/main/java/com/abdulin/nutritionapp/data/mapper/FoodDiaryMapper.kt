package com.abdulin.nutritionapp.data.mapper

import com.abdulin.nutritionapp.data.dto.diary.DiarySummaryDto
import com.abdulin.nutritionapp.data.dto.diary.FoodDiaryEntryDto
import com.abdulin.nutritionapp.data.dto.diary.CreateFoodDiaryRequestDto
import com.abdulin.nutritionapp.data.local.entity.DiaryEntity
import com.abdulin.nutritionapp.domain.model.DiarySummary
import com.abdulin.nutritionapp.domain.model.FoodDiaryEntry
import com.abdulin.nutritionapp.domain.model.MealType

fun FoodDiaryEntryDto.toDomain(): FoodDiaryEntry {
    val displayName = productName ?: recipeName ?: customName ?: "Без названия"
    return FoodDiaryEntry(
        id = id,
        mealType = try { MealType.valueOf(mealType) } catch(e: Exception) { MealType.BREAKFAST },
        productName = displayName,
        imageUrl = imageUrl,
        weightGrams = weightGrams,
        calories = calories ?: 0.0,
        protein = protein ?: 0.0,
        fat = fat ?: 0.0,
        carbs = carbs ?: 0.0,
        consumedAt = consumedAt
    )
}

fun DiaryEntity.toDomain(): FoodDiaryEntry {
    return FoodDiaryEntry(
        id = id,
        mealType = try { MealType.valueOf(mealType) } catch(e: Exception) { MealType.BREAKFAST },
        productName = productName,
        imageUrl = null,
        weightGrams = weightGrams,
        calories = calories,
        protein = protein,
        fat = fat,
        carbs = carbs,
        consumedAt = consumedAt
    )
}

fun FoodDiaryEntryDto.toEntity(date: String): DiaryEntity {
    val displayName = productName ?: recipeName ?: customName ?: "Без названия"
    return DiaryEntity(
        id = id,
        mealType = mealType,
        source = source,
        consumedAt = consumedAt,
        productId = productId,
        recipeId = recipeId,
        productName = displayName,
        weightGrams = weightGrams,
        calories = calories ?: 0.0,
        protein = protein ?: 0.0,
        fat = fat ?: 0.0,
        carbs = carbs ?: 0.0,
        entryDate = date,
        isSynced = true
    )
}

fun CreateFoodDiaryRequestDto.toEntity(id: Long): DiaryEntity {
    return DiaryEntity(
        id = id,
        mealType = mealType,
        source = source,
        consumedAt = consumedAt,
        productId = productId,
        recipeId = recipeId,
        productName = customName ?: "Новая запись",
        weightGrams = weightGrams,
        calories = calories ?: 0.0,
        protein = protein ?: 0.0,
        fat = fat ?: 0.0,
        carbs = carbs ?: 0.0,
        entryDate = entryDate,
        isSynced = false
    )
}

fun DiarySummaryDto.toDomain(): DiarySummary {
    return DiarySummary(
        totalCalories = totalCalories,
        totalProtein = totalProtein,
        totalFat = totalFat,
        totalCarbs = totalCarbs
    )
}
