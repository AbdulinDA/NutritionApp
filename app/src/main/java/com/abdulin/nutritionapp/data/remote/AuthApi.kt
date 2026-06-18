package com.abdulin.nutritionapp.data.remote

import com.abdulin.nutritionapp.core.network.ApiResponse
import com.abdulin.nutritionapp.data.dto.auth.LoginRequestDto
import com.abdulin.nutritionapp.data.dto.auth.LoginResponseDto
import com.abdulin.nutritionapp.data.dto.auth.RegisterRequestDto
import com.abdulin.nutritionapp.data.dto.auth.TokenRefreshRequestDto
import com.abdulin.nutritionapp.data.dto.auth.TokenRefreshResponseDto
import com.abdulin.nutritionapp.data.dto.auth.MessageResponseDto
import com.abdulin.nutritionapp.data.dto.auth.ResendVerificationRequestDto
import com.abdulin.nutritionapp.data.dto.auth.UserResponseDto
import kotlinx.serialization.json.JsonElement
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("api/v1/auth/login")
    suspend fun login(
        @Body request: LoginRequestDto
    ): Response<ApiResponse<LoginResponseDto>>

    @POST("api/v1/auth/register")
    suspend fun register(
        @Body request: RegisterRequestDto
    ): Response<ApiResponse<MessageResponseDto>>

    @POST("api/v1/auth/resend-verification")
    suspend fun resendVerification(
        @Body request: ResendVerificationRequestDto
    ): Response<ApiResponse<MessageResponseDto>>

    @POST("api/v1/auth/refresh")
    suspend fun refreshToken(
        @Body request: TokenRefreshRequestDto
    ): Response<ApiResponse<TokenRefreshResponseDto>>

    @POST("api/v1/auth/logout")
    suspend fun logout(): Response<ApiResponse<JsonElement>>
}
