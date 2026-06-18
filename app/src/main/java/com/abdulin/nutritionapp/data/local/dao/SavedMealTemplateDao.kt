package com.abdulin.nutritionapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.abdulin.nutritionapp.data.local.entity.SavedMealTemplateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedMealTemplateDao {

    @Query("SELECT * FROM saved_meal_templates ORDER BY createdAt DESC")
    fun observeTemplates(): Flow<List<SavedMealTemplateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: SavedMealTemplateEntity)

    @Query("DELETE FROM saved_meal_templates WHERE id = :id")
    suspend fun deleteTemplate(id: Long)
}
