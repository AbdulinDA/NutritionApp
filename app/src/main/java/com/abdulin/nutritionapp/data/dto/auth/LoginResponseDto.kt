package com.abdulin.nutritionapp.data.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponseDto (
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val id: Long,
    val publicId: String? = null,
    val email: String,
    val role: String,
    val type: String = "Bearer"
)
