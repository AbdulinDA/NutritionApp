package com.abdulin.nutritionapp.domain.repository

import com.abdulin.nutritionapp.core.utils.Resource
import com.abdulin.nutritionapp.data.dto.auth.UserResponseDto
import com.abdulin.nutritionapp.data.local.dao.WaterStat
import com.abdulin.nutritionapp.data.local.entity.WeightEntity
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun getMyProfile(): Resource<UserResponseDto>
    suspend fun updateProfile(
        currentUser: UserResponseDto,
        firstName: String,
        lastName: String,
        weightKg: Double,
        targetWeightKg: Double,
        goalTypeId: Long?,
        activityLevel: String
    ): Resource<UserResponseDto>
    suspend fun syncFoodPreferences(
        currentUser: UserResponseDto,
        allergies: List<String>,
        excludedProductsIds: List<Long>,
        favoriteCuisines: List<String>,
        dislikedCuisines: List<String>
    ): Resource<UserResponseDto>
    suspend fun completeOnboarding(
        currentUser: UserResponseDto,
        firstName: String,
        lastName: String,
        birthDate: String,
        gender: String,
        heightCm: Double,
        weightKg: Double,
        targetWeightKg: Double,
        activityLevel: String,
        dietType: String,
        allergies: List<String>,
        favoriteCuisines: List<String>,
        dislikedCuisines: List<String>
    ): Resource<UserResponseDto>
    suspend fun updateWeight(weightKg: Double): Resource<Unit>
    suspend fun logWeightHistory(weightKg: Double, date: String): Resource<Unit>
    suspend fun logWater(amountMl: Int, date: String): Resource<Unit>
    suspend fun getWaterToday(): Resource<Int>
    
    // Local data for Analytics
    fun getWeightHistory(): Flow<List<WeightEntity>>
    fun getWeeklyWaterStats(): Flow<List<WaterStat>>
}
