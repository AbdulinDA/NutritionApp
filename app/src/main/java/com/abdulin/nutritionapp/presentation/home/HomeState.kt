package com.abdulin.nutritionapp.presentation.home

import com.abdulin.nutritionapp.domain.model.HomeData

data class HomeState(
    val homeData: HomeData? = null,

    val isLoading: Boolean,

    val error: String? = null
)