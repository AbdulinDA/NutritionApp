package com.abdulin.nutritionapp.data.mapper

import com.abdulin.nutritionapp.data.dto.diary.CreateFoodDiaryRequestDto
import com.abdulin.nutritionapp.data.local.entity.SavedMealTemplateEntity
import com.abdulin.nutritionapp.domain.model.SavedMealTemplate

fun SavedMealTemplateEntity.toDomain(): SavedMealTemplate {
    return SavedMealTemplate(
        id = id,
        templateName = templateName,
        mealType = mealType,
        source = source,
        productId = productId,
        recipeId = recipeId,
        customName = customName,
        calories = calories,
        protein = protein,
        fat = fat,
        carbs = carbs,
        weightGrams = weightGrams
    )
}

fun CreateFoodDiaryRequestDto.toSavedMealTemplateEntity(
    id: Long,
    templateName: String,
    createdAt: Long
): SavedMealTemplateEntity {
    return SavedMealTemplateEntity(
        id = id,
        templateName = templateName,
        mealType = mealType,
        source = source,
        productId = productId,
        recipeId = recipeId,
        customName = customName,
        calories = calories,
        protein = protein,
        fat = fat,
        carbs = carbs,
        weightGrams = weightGrams,
        createdAt = createdAt
    )
}
