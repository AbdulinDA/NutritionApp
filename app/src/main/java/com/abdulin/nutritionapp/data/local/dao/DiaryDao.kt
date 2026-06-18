package com.abdulin.nutritionapp.data.local.dao

import androidx.room.*
import com.abdulin.nutritionapp.data.local.entity.DiaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryDao {
    @Query("SELECT * FROM diary_entries WHERE entryDate = :date ORDER BY consumedAt ASC")
    fun getDiaryByDate(date: String): Flow<List<DiaryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: DiaryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(entries: List<DiaryEntity>)

    @Delete
    suspend fun deleteEntry(entry: DiaryEntity)

    @Query("DELETE FROM diary_entries WHERE entryDate = :date")
    suspend fun clearDate(date: String)

    @Query("SELECT * FROM diary_entries WHERE isSynced = 0")
    suspend fun getUnsyncedEntries(): List<DiaryEntity>

    @Query("UPDATE diary_entries SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: Long)

    @Query("""
        SELECT productId, productName, COUNT(*) as usageCount 
        FROM diary_entries 
        WHERE productId IS NOT NULL 
        GROUP BY productId, productName 
        ORDER BY usageCount DESC 
        LIMIT :limit
    """)
    suspend fun getMostFrequentProducts(limit: Int): List<ProductUsage>

    @Query("""
        SELECT productId, productName, COUNT(*) as usageCount 
        FROM diary_entries 
        WHERE productId IS NOT NULL AND mealType = :mealType 
        GROUP BY productId, productName 
        ORDER BY usageCount DESC 
        LIMIT :limit
    """)
    suspend fun getFrequentProductsByMealType(mealType: String, limit: Int): List<ProductUsage>

    @Query("SELECT DISTINCT entryDate FROM diary_entries ORDER BY entryDate DESC")
    fun getAllLoggedDates(): Flow<List<String>>

    @Query("SELECT * FROM diary_entries WHERE entryDate BETWEEN :startDate AND :endDate")
    suspend fun getEntriesBetweenDates(startDate: String, endDate: String): List<DiaryEntity>
}

data class ProductUsage(
    val productId: Long,
    val productName: String,
    val usageCount: Int
)
