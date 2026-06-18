package com.abdulin.nutritionapp.data.dto.home

import com.abdulin.nutritionapp.data.dto.diary.FoodDiaryEntryDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HomeResponseDto(
    val generatedAt: String,
    val cacheTtl: Int,
    val macros: MacrosDto,
    val waterProgress: Int,
    val currentWeight: Double? = null,
    val todayMeals: List<FoodDiaryEntryDto> = emptyList(),
    val recommendations: List<HomeRecommendationDto> = emptyList(),
    val aiMessage: String? = null
)

@Serializable
data class MacrosDto(
    val totalCalories: Double,
    val totalProtein: Double,
    val totalFat: Double,
    val totalCarbs: Double
)

@Serializable
data class HomeRecommendationDto(
    val recipeId: Long? = null,
    val impressionId: Long? = null,
    val reason: String? = null,
    val experimentVariant: String? = null,
    val score: Double? = null,
    val recipe: RecipeShortDto? = null
)

@Serializable
data class RecipeShortDto(
    @SerialName("recipeId")
    val id: Long? = null,
    @SerialName("recipeName")
    val title: String? = null,
    val imageUrl: String? = null,
    val totalCalories: Double? = null,
    val totalProtein: Double? = null,
    val totalFat: Double? = null,
    val totalCarbs: Double? = null
)
