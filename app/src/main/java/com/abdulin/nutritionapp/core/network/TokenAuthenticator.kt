package com.abdulin.nutritionapp.core.network

import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject

class TokenAuthenticator @Inject constructor(
    private val tokenRefreshManager: TokenRefreshManager
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.code != 401 ||
            response.request.header("X-Token-Retry") == "true" ||
            responseCount(response) >= MAX_AUTH_RETRIES
        ) {
            return null
        }

        val refreshedToken = tokenRefreshManager.refreshIfNeeded(response) ?: return null
        return tokenRefreshManager.attachToken(
            response.request.newBuilder()
                .header("X-Token-Retry", "true")
                .build(),
            refreshedToken
        )
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var priorResponse = response.priorResponse
        while (priorResponse != null) {
            count++
            priorResponse = priorResponse.priorResponse
        }
        return count
    }

    private companion object {
        private const val MAX_AUTH_RETRIES = 2
    }
}
