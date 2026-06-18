package com.abdulin.nutritionapp.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.abdulin.nutritionapp.core.worker.ReminderManager
import com.abdulin.nutritionapp.presentation.navigation.AppNavigation
import com.abdulin.nutritionapp.ui.theme.NutritionAppTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @Inject
    lateinit var reminderManager: ReminderManager

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition {
            viewModel.startDestination.value == null
        }

        setContent {
            val startDestination by viewModel.startDestination.collectAsState()
            val currentTheme by viewModel.currentTheme.collectAsState()

            NutritionAppTheme(preset = currentTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (startDestination != null) {
                        AppNavigation(startDestination = startDestination!!)
                    }
                }
            }
        }

        window.decorView.post {
            lifecycleScope.launchWhenStarted {
                reminderManager.scheduleAllReminders()
            }
        }
    }
}
