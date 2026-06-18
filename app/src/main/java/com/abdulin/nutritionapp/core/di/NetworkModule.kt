package com.abdulin.nutritionapp.core.di

import com.abdulin.nutritionapp.core.network.OkHttpClientProvider
import com.abdulin.nutritionapp.core.network.RetrofitProvider
import com.abdulin.nutritionapp.core.network.TokenAuthenticator
import com.abdulin.nutritionapp.core.network.TokenRefreshManager
import com.abdulin.nutritionapp.data.remote.AuthApi
import com.abdulin.nutritionapp.data.remote.NutritionApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthenticatedRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BasicRetrofit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideTokenAuthenticator(
        tokenRefreshManager: TokenRefreshManager
    ): TokenAuthenticator {
        return TokenAuthenticator(tokenRefreshManager)
    }

    @Provides
    @Singleton
    @BasicRetrofit
    fun provideBasicOkHttpClient(): OkHttpClient {
        return OkHttpClientProvider.createBasic()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        tokenRefreshManager: TokenRefreshManager,
        authenticator: TokenAuthenticator
    ): OkHttpClient {
        return OkHttpClientProvider.create(tokenRefreshManager, authenticator)
    }

    @Provides
    @Singleton
    @BasicRetrofit
    fun provideBasicRetrofit(@BasicRetrofit okHttpClient: OkHttpClient): Retrofit {
        return RetrofitProvider.create(okHttpClient)
    }

    @Provides
    @Singleton
    @AuthenticatedRetrofit
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return RetrofitProvider.create(okHttpClient)
    }

    @Provides
    @Singleton
    fun provideAuthApi(@BasicRetrofit retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }

    @Provides
    @Singleton
    fun provideNutritionApi(@AuthenticatedRetrofit retrofit: Retrofit): NutritionApi {
        return retrofit.create(NutritionApi::class.java)
    }

}
