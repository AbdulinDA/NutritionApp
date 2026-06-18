package com.abdulin.nutritionapp.core.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.abdulin.nutritionapp.BuildConfig
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit

object RetrofitProvider {

    fun create(okHttpClient: OkHttpClient): Retrofit {

        val json = Json {

            ignoreUnknownKeys = true
            explicitNulls = false
            prettyPrint = true
        }

        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(
                json.asConverterFactory(
                    "application/json".toMediaType()
                )
            )
            .build()
    }

}
