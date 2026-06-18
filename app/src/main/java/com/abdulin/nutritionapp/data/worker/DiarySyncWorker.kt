package com.abdulin.nutritionapp.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.abdulin.nutritionapp.data.dto.diary.CreateDiaryEntryDto
import com.abdulin.nutritionapp.data.local.dao.DiaryDao
import com.abdulin.nutritionapp.data.remote.NutritionApi
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class DiarySyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val api: NutritionApi,
    private val diaryDao: DiaryDao
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val unsyncedEntries = diaryDao.getUnsyncedEntries()

        if (unsyncedEntries.isEmpty()) return@withContext Result.success()

        var hasError = false
        unsyncedEntries.forEach { entry ->
            try {
                val dto = CreateDiaryEntryDto(
                    mealType = entry.mealType,
                    source = entry.source,
                    productId = if (entry.source == "PRODUCT") entry.productId else null,
                    recipeId = if (entry.source == "RECIPE") entry.recipeId else null,
                    customName = if (entry.source != "PRODUCT") entry.productName else null,
                    calories = entry.calories,
                    protein = entry.protein,
                    fat = entry.fat,
                    carbs = entry.carbs,
                    weightGrams = entry.weightGrams,
                    entryDate = entry.entryDate,
                    consumedAt = entry.consumedAt
                )

                val response = api.addDiaryEntry(dto)
                if (response.isSuccessful && response.body()?.success == true) {
                    diaryDao.markAsSynced(entry.id)
                } else {
                    hasError = true
                }
            } catch (_: Exception) {
                hasError = true
            }
        }

        if (hasError) Result.retry() else Result.success()
    }
}
