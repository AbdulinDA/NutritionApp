package com.abdulin.nutritionapp.core.network

import retrofit2.Response
import retrofit2.http.GET

interface ApiService {

    @GET("home")
    suspend fun getHomeData(): Response<String>
}