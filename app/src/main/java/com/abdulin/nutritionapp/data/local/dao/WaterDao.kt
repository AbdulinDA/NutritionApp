package com.abdulin.nutritionapp.data.local.dao

import androidx.room.*
import com.abdulin.nutritionapp.data.local.entity.WaterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterDao {
    @Query("SELECT * FROM water_logs WHERE date = :date")
    fun getWaterLogsByDate(date: String): Flow<List<WaterEntity>>

    @Query("SELECT SUM(amountMl) FROM water_logs WHERE date = :date")
    fun getTotalWaterByDate(date: String): Flow<Int?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaterLog(waterLog: WaterEntity)

    @Query("SELECT date, SUM(amountMl) as total FROM water_logs GROUP BY date ORDER BY date DESC LIMIT 7")
    fun getWeeklyWaterStats(): Flow<List<WaterStat>>
}

data class WaterStat(
    val date: String,
    val total: Int
)
