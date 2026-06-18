package com.abdulin.nutritionapp.domain.repository

import com.abdulin.nutritionapp.core.utils.Resource
import com.abdulin.nutritionapp.data.dto.diary.CreateFoodDiaryRequestDto
import com.abdulin.nutritionapp.domain.model.DiarySummary
import com.abdulin.nutritionapp.domain.model.FoodDiaryEntry
import com.abdulin.nutritionapp.domain.model.SavedMealTemplate
import kotlinx.coroutines.flow.Flow

interface FoodDiaryRepository {

    /**
     * Получить дневник за дату
     */
    suspend fun getDiaryByDate(
        date: String
    ): Resource<List<FoodDiaryEntry>>

    /**
     * Получить сводку КБЖУ
     */
    suspend fun getSummary(
        date: String
    ): Resource<DiarySummary>

    /**
     * Добавить запись в дневник
     */
    suspend fun addFood(
        request: CreateFoodDiaryRequestDto
    ): Resource<Unit>

    /**
     * Удалить запись из дневника
     */
    suspend fun deleteDiaryEntry(
        id: Long
    ): Resource<Unit>

    /**
     * Обновить вес записи в дневнике
     */
    suspend fun updateDiaryEntry(
        id: Long,
        weightGrams: Double
    ): Resource<Unit>

    /**
     * Получить все даты, когда были записи (для расчета страйков)
     */
    fun getAllLoggedDates(): Flow<List<String>>

    /**
     * Получить записи между датами (для глубокого анализа)
     */
    suspend fun getEntriesBetweenDates(startDate: String, endDate: String): List<FoodDiaryEntry>

    fun observeSavedMealTemplates(): Flow<List<SavedMealTemplate>>

    suspend fun saveMealTemplate(templateName: String, request: CreateFoodDiaryRequestDto): Resource<Unit>

    suspend fun deleteSavedMealTemplate(id: Long): Resource<Unit>
}
