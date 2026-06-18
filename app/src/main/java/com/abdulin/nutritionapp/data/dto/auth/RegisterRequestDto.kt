package com.abdulin.nutritionapp.data.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequestDto(
    val email: String,
    val password: String,
    val firstName: String? = null,
    val lastName: String? = null
)
