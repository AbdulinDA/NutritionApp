package com.abdulin.nutritionapp.data.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class TokenRefreshResponseDto(
    val accessToken: String,
    val refreshToken: String,
    val type: String = "Bearer"
)
