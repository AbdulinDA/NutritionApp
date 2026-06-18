package com.abdulin.nutritionapp.core.security

import com.abdulin.nutritionapp.core.utils.TokenManager
import com.abdulin.nutritionapp.data.local.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionCleaner @Inject constructor(
    private val tokenManager: TokenManager,
    private val appDatabase: AppDatabase
) {
    suspend fun clearSession() = withContext(Dispatchers.IO) {
        tokenManager.clearTokens()
        appDatabase.clearAllTables()
    }
}
