package com.abdulin.nutritionapp.core.network

import kotlinx.serialization.Serializable

@Serializable
data class ApiError(
    val code: String,
    val message: String
)