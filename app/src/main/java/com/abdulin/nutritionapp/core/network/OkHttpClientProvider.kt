package com.abdulin.nutritionapp.core.network

import com.abdulin.nutritionapp.BuildConfig
import okhttp3.Authenticator
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

object OkHttpClientProvider {

    private const val CONNECT_TIMEOUT_SECONDS = 20L
    private const val READ_TIMEOUT_SECONDS = 30L
    private const val WRITE_TIMEOUT_SECONDS = 30L

    fun createBasic(): OkHttpClient {
        return baseBuilder().build()
    }

    fun create(
        tokenRefreshManager: TokenRefreshManager,
        authenticator: Authenticator
    ): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.ENABLE_NETWORK_LOGS) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
            redactHeader("Authorization")
        }

        val builder = baseBuilder()
            .addInterceptor(AuthInterceptor(tokenRefreshManager))
            .addInterceptor(logging)
            .authenticator(authenticator)

        if (!BuildConfig.DEBUG) {
            val certificatePinner = CertificatePinner.Builder()
                .add(BuildConfig.API_DOMAIN, BuildConfig.API_CERT_PIN)
                .build()
            builder.certificatePinner(certificatePinner)
        }

        return builder.build()
    }

    private fun baseBuilder(): OkHttpClient.Builder {
        return OkHttpClient.Builder()
            .addInterceptor(RetryInterceptor())
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
    }
}
