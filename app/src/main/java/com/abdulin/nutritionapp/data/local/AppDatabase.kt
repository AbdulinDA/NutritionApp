package com.abdulin.nutritionapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.abdulin.nutritionapp.data.local.dao.DiaryDao
import com.abdulin.nutritionapp.data.local.dao.MealPlanDao
import com.abdulin.nutritionapp.data.local.dao.SavedMealTemplateDao
import com.abdulin.nutritionapp.data.local.dao.WaterDao
import com.abdulin.nutritionapp.data.local.dao.WeightDao
import com.abdulin.nutritionapp.data.local.entity.DiaryEntity
import com.abdulin.nutritionapp.data.local.entity.MealPlanEntity
import com.abdulin.nutritionapp.data.local.entity.SavedMealTemplateEntity
import com.abdulin.nutritionapp.data.local.entity.WaterEntity
import com.abdulin.nutritionapp.data.local.entity.WeightEntity

@Database(
    entities = [WaterEntity::class, WeightEntity::class, DiaryEntity::class, MealPlanEntity::class, SavedMealTemplateEntity::class],
    version = 5,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun waterDao(): WaterDao
    abstract fun weightDao(): WeightDao
    abstract fun diaryDao(): DiaryDao
    abstract fun mealPlanDao(): MealPlanDao
    abstract fun savedMealTemplateDao(): SavedMealTemplateDao
}
