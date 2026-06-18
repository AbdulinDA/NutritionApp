package com.abdulin.nutritionapp.core

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.abdulin.nutritionapp.BuildConfig
import com.abdulin.nutritionapp.core.utils.TokenManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class NutritionApp : Application(), Configuration.Provider {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        applySavedLocale()
        appScope.launch {
            TokenManager(applicationContext).normalizeStoredFoodPreferences()
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(
                if (BuildConfig.DEBUG) Log.DEBUG else Log.ERROR
            )
            .build()

    private fun applySavedLocale() {
        val localeCode = TokenManager(applicationContext).peekSavedAppLocaleCode()
        val locales = if (localeCode.isNullOrBlank()) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(localeCode)
        }
        AppCompatDelegate.setApplicationLocales(locales)
    }
}
