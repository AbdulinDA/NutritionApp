package com.abdulin.nutritionapp.data.repository

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.abdulin.nutritionapp.R
import com.abdulin.nutritionapp.core.network.safeApiCall
import com.abdulin.nutritionapp.core.utils.Resource
import com.abdulin.nutritionapp.data.dto.diary.CreateDiaryEntryDto
import com.abdulin.nutritionapp.data.dto.diary.CreateFoodDiaryRequestDto
import com.abdulin.nutritionapp.data.dto.diary.UpdateDiaryEntryWeightRequestDto
import com.abdulin.nutritionapp.data.local.dao.DiaryDao
import com.abdulin.nutritionapp.data.local.dao.SavedMealTemplateDao
import com.abdulin.nutritionapp.data.local.entity.DiaryEntity
import com.abdulin.nutritionapp.data.mapper.toDomain
import com.abdulin.nutritionapp.data.mapper.toEntity
import com.abdulin.nutritionapp.data.mapper.toSavedMealTemplateEntity
import com.abdulin.nutritionapp.data.remote.NutritionApi
import com.abdulin.nutritionapp.data.worker.DiarySyncWorker
import com.abdulin.nutritionapp.domain.model.DiarySummary
import com.abdulin.nutritionapp.domain.model.FoodDiaryEntry
import com.abdulin.nutritionapp.domain.model.SavedMealTemplate
import com.abdulin.nutritionapp.domain.repository.FoodDiaryRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.JsonElement
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class FoodDiaryRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: NutritionApi,
    private val diaryDao: DiaryDao,
    private val savedMealTemplateDao: SavedMealTemplateDao,
    private val workManager: WorkManager
) : FoodDiaryRepository {

    override suspend fun getDiaryByDate(date: String): Resource<List<FoodDiaryEntry>> {
        val localEntries = diaryDao.getDiaryByDate(date).first()
        val response = safeApiCall { api.getDiary(date) }

        return when (response) {
            is Resource.Success -> {
                val remoteEntries = response.data ?: emptyList()
                val unsyncedEntries = localEntries.filter { !it.isSynced }

                diaryDao.clearDate(date)
                diaryDao.insertEntries(remoteEntries.map { it.toEntity(date) })
                if (unsyncedEntries.isNotEmpty()) {
                    diaryDao.insertEntries(unsyncedEntries)
                }

                val merged = (remoteEntries.map { it.toDomain() } + unsyncedEntries.map { it.toDomain() })
                    .sortedBy { it.consumedAt }
                Resource.Success(merged)
            }

            is Resource.Error -> {
                if (localEntries.isNotEmpty()) {
                    Resource.Success(localEntries.map { it.toDomain() }.sortedBy { it.consumedAt })
                } else {
                    Resource.Error(response.message ?: context.getString(R.string.diary_error_load))
                }
            }

            is Resource.Loading -> Resource.Loading()
        }
    }

    override suspend fun getSummary(date: String): Resource<DiarySummary> {
        val response = safeApiCall { api.getDiarySummary(date) }
        return when (response) {
            is Resource.Success -> Resource.Success(response.data!!.toDomain())
            is Resource.Error -> Resource.Error(response.message ?: context.getString(R.string.diary_error_load_summary))
            is Resource.Loading -> Resource.Loading()
        }
    }

    override suspend fun addFood(request: CreateFoodDiaryRequestDto): Resource<Unit> {
        val localId = System.currentTimeMillis()
        val pendingEntity = request.toEntity(localId)
        diaryDao.insertEntry(pendingEntity)

        val createDto = request.toCreateDiaryEntryDto()
        return when (val response = safeApiCall { api.addDiaryEntry(createDto) }) {
            is Resource.Success -> {
                val syncedEntry = response.data!!.toEntity(request.entryDate)
                diaryDao.deleteEntry(pendingEntity)
                diaryDao.insertEntry(syncedEntry)
                Resource.Success(Unit)
            }

            is Resource.Error -> {
                scheduleSync()
                Resource.Error(response.message ?: context.getString(R.string.diary_error_add_entry), Unit)
            }

            is Resource.Loading -> Resource.Loading()
        }
    }

    override suspend fun deleteDiaryEntry(id: Long): Resource<Unit> {
        val existingEntry = diaryDao.getUnsyncedEntries().firstOrNull { it.id == id }
        return when (val result = safeApiCall<JsonElement> { api.deleteDiaryEntry(id) }) {
            is Resource.Success -> {
                if (existingEntry != null) {
                    diaryDao.deleteEntry(existingEntry)
                } else {
                    deleteLocalEntry(id)
                }
                Resource.Success(Unit)
            }

            is Resource.Error -> Resource.Error(result.message ?: context.getString(R.string.diary_error_delete_entry))
            is Resource.Loading -> Resource.Loading()
        }
    }

    override suspend fun updateDiaryEntry(id: Long, weightGrams: Double): Resource<Unit> {
        return when (val result = safeApiCall { api.updateDiaryEntry(id, UpdateDiaryEntryWeightRequestDto(weightGrams)) }) {
            is Resource.Success -> {
                val updatedEntry = result.data!!.toEntity(result.data.entryDate ?: "")
                deleteLocalEntry(id)
                diaryDao.insertEntry(updatedEntry)
                Resource.Success(Unit)
            }

            is Resource.Error -> Resource.Error(result.message ?: context.getString(R.string.diary_error_update_entry))
            is Resource.Loading -> Resource.Loading()
        }
    }

    override fun getAllLoggedDates(): Flow<List<String>> = diaryDao.getAllLoggedDates()

    override suspend fun getEntriesBetweenDates(startDate: String, endDate: String): List<FoodDiaryEntry> {
        return diaryDao.getEntriesBetweenDates(startDate, endDate).map { it.toDomain() }
    }

    override fun observeSavedMealTemplates(): Flow<List<SavedMealTemplate>> {
        return savedMealTemplateDao.observeTemplates().map { templates -> templates.map { it.toDomain() } }
    }

    override suspend fun saveMealTemplate(templateName: String, request: CreateFoodDiaryRequestDto): Resource<Unit> {
        val now = System.currentTimeMillis()
        savedMealTemplateDao.insertTemplate(
            request.toSavedMealTemplateEntity(
                id = now,
                templateName = templateName.trim(),
                createdAt = now
            )
        )
        return Resource.Success(Unit)
    }

    override suspend fun deleteSavedMealTemplate(id: Long): Resource<Unit> {
        savedMealTemplateDao.deleteTemplate(id)
        return Resource.Success(Unit)
    }

    private suspend fun deleteLocalEntry(id: Long) {
        val dates = diaryDao.getAllLoggedDates().first()
        for (date in dates) {
            val entries = diaryDao.getDiaryByDate(date).first()
            entries.firstOrNull { it.id == id }?.let {
                diaryDao.deleteEntry(it)
                return
            }
        }
    }

    private fun scheduleSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<DiarySyncWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
            .build()

        workManager.enqueueUniqueWork(
            "diary_sync",
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )
    }

    private fun CreateFoodDiaryRequestDto.toCreateDiaryEntryDto(): CreateDiaryEntryDto {
        return CreateDiaryEntryDto(
            mealType = mealType,
            source = source,
            productId = productId,
            recipeId = recipeId,
            sideDishRecipeId = sideDishRecipeId,
            sideDishPortionMultiplier = sideDishPortionMultiplier,
            planRecipeId = planRecipeId,
            customName = customName,
            calories = calories,
            protein = protein,
            fat = fat,
            carbs = carbs,
            weightGrams = weightGrams,
            entryDate = entryDate,
            consumedAt = consumedAt,
            idempotencyKey = idempotencyKey
        )
    }
}
