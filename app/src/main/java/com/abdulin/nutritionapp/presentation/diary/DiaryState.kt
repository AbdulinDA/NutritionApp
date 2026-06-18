package com.abdulin.nutritionapp.presentation.diary

import com.abdulin.nutritionapp.domain.model.FoodDiaryEntryModel

data class DiaryState(

    val isLoading: Boolean = false,

    val entries: List<FoodDiaryEntryModel> = emptyList(),

    val error: String? = null
)