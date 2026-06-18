package com.abdulin.nutritionapp.presentation.profile

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdulin.nutritionapp.R
import com.abdulin.nutritionapp.core.utils.TokenManager
import com.abdulin.nutritionapp.data.dto.auth.UserResponseDto
import com.abdulin.nutritionapp.data.remote.NutritionApi
import com.abdulin.nutritionapp.domain.repository.AuthRepository
import com.abdulin.nutritionapp.ui.theme.AppThemePreset
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val user: UserResponseDto? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedOut: Boolean = false,
    val currentThemeOrdinal: Int = 0,
    val currentLocaleCode: String? = null,
    val favoriteProductsCount: Int = 0,
    val excludedProductsCount: Int = 0,
    val allergyProductsCount: Int = 0,
    val favoriteCuisinesCount: Int = 0,
    val dislikedCuisinesCount: Int = 0
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: NutritionApi,
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state = _state.asStateFlow()

    init {
        loadProfile()
        observeTheme()
        observeLocale()
        observeFoodPreferences()
    }

    private fun observeTheme() {
        tokenManager.themePresetOrdinal
            .onEach { ordinal ->
                _state.update { it.copy(currentThemeOrdinal = ordinal) }
            }
            .launchIn(viewModelScope)
    }

    private fun observeLocale() {
        tokenManager.appLocaleCode
            .onEach { localeCode ->
                _state.update { it.copy(currentLocaleCode = localeCode) }
            }
            .launchIn(viewModelScope)
    }

    private fun observeFoodPreferences() {
        tokenManager.excludedProducts
            .onEach { products ->
                _state.update { it.copy(excludedProductsCount = products.size) }
            }
            .launchIn(viewModelScope)

        tokenManager.favoriteProducts
            .onEach { products ->
                _state.update { it.copy(favoriteProductsCount = products.size) }
            }
            .launchIn(viewModelScope)

        tokenManager.allergyProducts
            .onEach { products ->
                _state.update { it.copy(allergyProductsCount = products.size) }
            }
            .launchIn(viewModelScope)

        tokenManager.favoriteCuisines
            .onEach { cuisines ->
                _state.update { it.copy(favoriteCuisinesCount = cuisines.size) }
            }
            .launchIn(viewModelScope)

        tokenManager.dislikedCuisines
            .onEach { cuisines ->
                _state.update { it.copy(dislikedCuisinesCount = cuisines.size) }
            }
            .launchIn(viewModelScope)
    }

    fun setTheme(preset: AppThemePreset) {
        viewModelScope.launch {
            tokenManager.saveThemePreset(preset.ordinal)
        }
    }

    fun setLanguage(localeCode: String?) {
        viewModelScope.launch {
            tokenManager.saveAppLocaleCode(localeCode)
            val locales = if (localeCode.isNullOrBlank()) {
                LocaleListCompat.getEmptyLocaleList()
            } else {
                LocaleListCompat.forLanguageTags(localeCode)
            }
            AppCompatDelegate.setApplicationLocales(locales)
        }
    }

    fun loadProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val response = api.getMyProfile()
                if (response.isSuccessful && response.body()?.success == true) {
                    response.body()?.data?.let { tokenManager.syncFoodPreferencesFromProfile(it) }
                    _state.update { it.copy(user = response.body()?.data, isLoading = false) }
                } else {
                    _state.update {
                        it.copy(
                            error = context.getString(R.string.profile_error_load),
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        error = e.message ?: context.getString(R.string.error_unknown),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _state.update { it.copy(isLoggedOut = true) }
        }
    }
}
