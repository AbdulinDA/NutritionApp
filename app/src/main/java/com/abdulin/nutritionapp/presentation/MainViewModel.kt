package com.abdulin.nutritionapp.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdulin.nutritionapp.core.utils.TokenManager
import com.abdulin.nutritionapp.core.utils.Resource
import com.abdulin.nutritionapp.domain.usecase.GetMyProfileUseCase
import com.abdulin.nutritionapp.ui.theme.AppThemePreset
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val getMyProfileUseCase: GetMyProfileUseCase
) : ViewModel() {

    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination = _startDestination.asStateFlow()

    val currentTheme = tokenManager.themePresetOrdinal
        .map { ordinal -> AppThemePreset.entries.getOrElse(ordinal) { AppThemePreset.NATURE_GREEN } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppThemePreset.NATURE_GREEN)

    init {
        checkAuth()
    }

    private fun checkAuth() {
        tokenManager.peekAccessToken()?.let {
            resolveStartDestination()
            return
        }

        viewModelScope.launch {
            val token = tokenManager.getAccessToken()
            if (token != null) {
                resolveStartDestination()
            } else {
                _startDestination.value = "login"
            }
        }
    }

    private fun resolveStartDestination() {
        viewModelScope.launch {
            _startDestination.value = when (val profileResult = getMyProfileUseCase()) {
                is Resource.Success -> {
                    if (profileResult.data?.onboardingCompleted == true) "main" else "onboarding"
                }
                else -> "main"
            }
        }
    }
}
