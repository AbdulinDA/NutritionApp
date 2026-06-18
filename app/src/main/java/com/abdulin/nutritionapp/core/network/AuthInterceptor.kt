package com.abdulin.nutritionapp.core.network

import android.util.Log
import com.abdulin.nutritionapp.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val tokenRefreshManager: TokenRefreshManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val token = tokenRefreshManager.currentAccessToken()

        if (BuildConfig.ENABLE_NETWORK_LOGS) {
            Log.d("AuthInterceptor", "Intercepting request: ${request.url}")
        }

        val authorizedRequest = if (token != null) {
            if (BuildConfig.ENABLE_NETWORK_LOGS) {
                Log.d("AuthInterceptor", "Adding token to request")
            }
            tokenRefreshManager.attachToken(request, token)
        } else {
            if (BuildConfig.ENABLE_NETWORK_LOGS) {
                Log.d("AuthInterceptor", "No token found for request")
            }
            request
        }

        return chain.proceed(authorizedRequest)
    }
}
