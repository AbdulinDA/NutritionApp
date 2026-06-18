package com.abdulin.nutritionapp.core.network

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import kotlin.math.min

class RetryInterceptor(
    private val maxRetries: Int = 2,
    private val initialBackoffMs: Long = 250L
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var lastException: IOException? = null

        repeat(maxRetries + 1) { attempt ->
            try {
                return chain.proceed(request)
            } catch (exception: IOException) {
                lastException = exception
                if (!shouldRetry(request.method, attempt)) {
                    throw exception
                }

                val backoffMs = min(initialBackoffMs * (1L shl attempt), 2_000L)
                Thread.sleep(backoffMs)
            }
        }

        throw lastException ?: IOException("Request failed after retries.")
    }

    private fun shouldRetry(method: String, attempt: Int): Boolean {
        return attempt < maxRetries && method.uppercase() in SAFE_METHODS
    }

    private companion object {
        private val SAFE_METHODS = setOf("GET", "HEAD", "OPTIONS")
    }
}
