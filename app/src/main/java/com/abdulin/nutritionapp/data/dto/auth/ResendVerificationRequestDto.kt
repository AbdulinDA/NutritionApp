package com.abdulin.nutritionapp.data.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class ResendVerificationRequestDto(
    val email: String
)
