package com.abdulin.nutritionapp.data.local.dao

import androidx.room.*
import com.abdulin.nutritionapp.data.local.entity.MealPlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MealPlanDao {
    @Query("SELECT * FROM meal_plans ORDER BY createdAt DESC LIMIT 1")
    fun getLatestPlan(): Flow<MealPlanEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: MealPlanEntity)

    @Query("DELETE FROM meal_plans")
    suspend fun clearPlans()
}
