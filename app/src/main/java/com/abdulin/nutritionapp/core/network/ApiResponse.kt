package com.abdulin.nutritionapp.core.network

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val error: ApiError? = null,
    val data: T? = null
)