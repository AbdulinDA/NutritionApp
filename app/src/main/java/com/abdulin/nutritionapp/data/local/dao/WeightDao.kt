package com.abdulin.nutritionapp.data.local.dao

import androidx.room.*
import com.abdulin.nutritionapp.data.local.entity.WeightEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightDao {
    @Query("SELECT * FROM weight_logs ORDER BY date DESC")
    fun getAllWeightLogs(): Flow<List<WeightEntity>>

    @Query("SELECT * FROM weight_logs WHERE date = :date LIMIT 1")
    suspend fun getWeightByDate(date: String): WeightEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeightLog(weightLog: WeightEntity)

    @Query("SELECT * FROM weight_logs ORDER BY date DESC LIMIT 7")
    fun getLastSevenWeightLogs(): Flow<List<WeightEntity>>
}
