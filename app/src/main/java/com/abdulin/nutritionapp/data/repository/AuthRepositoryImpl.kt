package com.abdulin.nutritionapp.data.repository

import android.content.Context
import com.abdulin.nutritionapp.R
import com.abdulin.nutritionapp.core.network.safeApiCall
import com.abdulin.nutritionapp.core.security.SessionCleaner
import com.abdulin.nutritionapp.core.utils.Resource
import com.abdulin.nutritionapp.core.utils.TokenManager
import com.abdulin.nutritionapp.data.dto.auth.LoginRequestDto
import com.abdulin.nutritionapp.data.dto.auth.MessageResponseDto
import com.abdulin.nutritionapp.data.dto.auth.RegisterRequestDto
import com.abdulin.nutritionapp.data.dto.auth.ResendVerificationRequestDto
import com.abdulin.nutritionapp.data.remote.AuthApi
import com.abdulin.nutritionapp.domain.repository.AuthRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.JsonElement
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi,
    private val tokenManager: TokenManager,
    private val sessionCleaner: SessionCleaner,
    @ApplicationContext private val context: Context
) : AuthRepository {

    private fun String.containsAny(vararg values: String): Boolean = values.any { contains(it) }

    private fun mapRegisterError(message: String?): String {
        val normalized = message?.trim().orEmpty()
        val lower = normalized.lowercase()
        return when {
            lower.containsAny(
                "too many requests",
                "rate limited",
                "retry after"
            ) -> context.getString(R.string.auth_error_rate_limited)

            lower.containsAny(
                "already exists",
                "already exist",
                "resource already exists",
                "conflict",
                "электронной почтой уже существует",
                "с такой эл. почтой уже существует",
                "email already exists"
            ) -> context.getString(R.string.register_error_email_exists)

            lower.containsAny(
                "email is invalid",
                "valid email",
                "email is required",
                "{email=",
                "email="
            ) -> context.getString(R.string.register_error_email)

            lower.containsAny(
                "password is required",
                "password must be between",
                "password must contain",
                "{password=",
                "password="
            ) -> context.getString(R.string.register_error_password)

            lower.containsAny(
                "first name",
                "{firstname=",
                "{first_name=",
                "{firstname",
                "firstname=",
                "first name is required"
            ) -> context.getString(R.string.register_error_first_name)

            lower.containsAny(
                "validation_error",
                "invalid request parameters",
                "request could not be processed",
                "bad_request"
            ) -> context.getString(R.string.register_error_generic)

            else -> normalized.ifBlank { context.getString(R.string.register_error_generic) }
        }
    }

    private fun mapLoginError(message: String?): String {
        val normalized = message?.trim().orEmpty()
        val lower = normalized.lowercase()
        return when {
            lower.containsAny(
                "verify your email",
                "email is not verified",
                "account is not verified",
                "account inactive"
            ) -> context.getString(R.string.login_error_email_not_verified)

            lower.containsAny(
                "invalid email or password",
                "invalid credentials",
                "bad credentials"
            ) -> context.getString(R.string.login_error_invalid_credentials)

            lower.containsAny(
                "too many login attempts",
                "retry after",
                "rate limited",
                "too many requests"
            ) -> context.getString(R.string.auth_error_rate_limited)

            lower.containsAny(
                "email is required",
                "{email=",
                "email=",
                "valid email"
            ) -> context.getString(R.string.login_error_email_invalid)

            lower.containsAny(
                "password is required",
                "{password=",
                "password="
            ) -> context.getString(R.string.login_error_password_required)

            else -> normalized.ifBlank { context.getString(R.string.auth_error_login) }
        }
    }

    override suspend fun login(email: String, password: String): Resource<Unit> {
        val result = safeApiCall {
            api.login(LoginRequestDto(email, password))
        }

        return when (result) {
            is Resource.Success -> {
                val loginData = result.data!!
                tokenManager.saveTokens(
                    access = loginData.accessToken,
                    refresh = loginData.refreshToken
                )
                Resource.Success(Unit)
            }
            is Resource.Error -> Resource.Error(mapLoginError(result.message))
            is Resource.Loading -> Resource.Loading()
        }
    }

    override suspend fun register(request: RegisterRequestDto): Resource<MessageResponseDto> {
        val result = safeApiCall {
            api.register(request)
        }
        return when (result) {
            is Resource.Success -> Resource.Success(result.data!!)
            is Resource.Error -> Resource.Error(mapRegisterError(result.message))
            is Resource.Loading -> Resource.Loading()
        }
    }

    override suspend fun resendVerification(email: String): Resource<MessageResponseDto> {
        val result = safeApiCall {
            api.resendVerification(ResendVerificationRequestDto(email.trim()))
        }
        return when (result) {
            is Resource.Success -> Resource.Success(result.data!!)
            is Resource.Error -> Resource.Error(mapRegisterError(result.message))
            is Resource.Loading -> Resource.Loading()
        }
    }

    override suspend fun logout(): Resource<Unit> {
        val result = safeApiCall<JsonElement> {
            api.logout()
        }
        sessionCleaner.clearSession()
        return when (result) {
            is Resource.Success -> Resource.Success(Unit)
            is Resource.Error -> Resource.Error(
                result.message ?: context.getString(R.string.auth_error_logout)
            )
            is Resource.Loading -> Resource.Loading()
        }
    }
}
