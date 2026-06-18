package com.abdulin.nutritionapp.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.abdulin.nutritionapp.presentation.splash.SplashViewModel

@Composable
fun SplashScreen(
    viewModel: SplashViewModel = hiltViewModel(),
    onGoHome: () -> Unit,
    onGoLogin: () -> Unit
) {

    val state by viewModel.startDestination.collectAsState()

    LaunchedEffect(state) {
        when (state) {
            "main" -> onGoHome()
            "home" -> onGoHome()
            "login" -> onGoLogin()
        }
    }

    Box(modifier = Modifier.fillMaxSize())
}
