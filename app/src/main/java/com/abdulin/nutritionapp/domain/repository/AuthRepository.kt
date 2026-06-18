package com.abdulin.nutritionapp.domain.repository

import com.abdulin.nutritionapp.core.utils.Resource
import com.abdulin.nutritionapp.data.dto.auth.MessageResponseDto
import com.abdulin.nutritionapp.data.dto.auth.RegisterRequestDto

interface AuthRepository {

    suspend fun login(
        email: String,
        password: String
    ): Resource<Unit>

    suspend fun register(
        request: RegisterRequestDto
    ): Resource<MessageResponseDto>

    suspend fun resendVerification(email: String): Resource<MessageResponseDto>

    suspend fun logout(): Resource<Unit>
}
