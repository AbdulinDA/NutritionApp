package com.abdulin.nutritionapp.core.network

import com.abdulin.nutritionapp.core.security.SessionCleaner
import com.abdulin.nutritionapp.core.utils.TokenManager
import com.abdulin.nutritionapp.data.dto.auth.TokenRefreshRequestDto
import com.abdulin.nutritionapp.data.dto.auth.TokenRefreshResponseDto
import com.abdulin.nutritionapp.data.remote.AuthApi
import kotlinx.coroutines.runBlocking
import okhttp3.Request
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class TokenRefreshManager @Inject constructor(
    private val tokenManager: TokenManager,
    private val sessionCleaner: SessionCleaner,
    private val authApiProvider: Provider<AuthApi>
) {
    fun currentAccessToken(): String? = runBlocking { tokenManager.getAccessToken() }

    fun attachToken(request: Request, token: String): Request {
        return request.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
    }

    fun refreshIfNeeded(response: Response): String? {
        val refreshToken = runBlocking { tokenManager.getRefreshToken() } ?: return null

        synchronized(this) {
            val currentToken = runBlocking { tokenManager.getAccessToken() }
            val requestToken = response.request.header("Authorization")?.removePrefix("Bearer ")

            if (currentToken != null && currentToken != requestToken) {
                return currentToken
            }

            val newTokens = when (val result = refreshTokens(refreshToken)) {
                is RefreshResult.Success -> result.tokens
                RefreshResult.InvalidSession -> {
                    runBlocking { sessionCleaner.clearSession() }
                    return null
                }
                RefreshResult.TemporaryFailure -> return null
            }

            runBlocking {
                tokenManager.saveTokens(newTokens.accessToken, newTokens.refreshToken)
            }
            return newTokens.accessToken
        }
    }

    private fun refreshTokens(refreshToken: String): RefreshResult {
        return runBlocking {
            try {
                val response = authApiProvider.get().refreshToken(TokenRefreshRequestDto(refreshToken))
                if (response.isSuccessful && response.body()?.success == true) {
                    response.body()?.data?.let { RefreshResult.Success(it) }
                        ?: RefreshResult.TemporaryFailure
                } else if (response.code() == 401 || response.code() == 403) {
                    RefreshResult.InvalidSession
                } else {
                    RefreshResult.TemporaryFailure
                }
            } catch (_: Exception) {
                RefreshResult.TemporaryFailure
            }
        }
    }

    private sealed interface RefreshResult {
        data class Success(val tokens: TokenRefreshResponseDto) : RefreshResult
        data object InvalidSession : RefreshResult
        data object TemporaryFailure : RefreshResult
    }
}
