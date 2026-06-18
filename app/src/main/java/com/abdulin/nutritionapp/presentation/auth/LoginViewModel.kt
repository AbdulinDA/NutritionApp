package com.abdulin.nutritionapp.presentation.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdulin.nutritionapp.R
import com.abdulin.nutritionapp.core.utils.Resource
import com.abdulin.nutritionapp.domain.usecase.GetMyProfileUseCase
import com.abdulin.nutritionapp.domain.usecase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val loginUseCase: LoginUseCase,
    private val getMyProfileUseCase: GetMyProfileUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()

    fun onEmailChange(email: String) {
        _state.value = _state.value.copy(email = email, emailError = null, error = null)
    }

    fun onPasswordChange(password: String) {
        _state.value = _state.value.copy(password = password, passwordError = null, error = null)
    }

    fun login() {
        val email = _state.value.email.trim()
        val password = _state.value.password

        val emailError = when {
            email.isBlank() -> context.getString(R.string.login_error_email_required)
            !email.contains("@") -> context.getString(R.string.login_error_email_invalid)
            else -> null
        }
        val passwordError = when {
            password.isBlank() -> context.getString(R.string.login_error_password_required)
            password.length < 8 -> context.getString(R.string.login_error_password_invalid)
            else -> null
        }

        if (emailError != null || passwordError != null) {
            _state.value = _state.value.copy(
                emailError = emailError,
                passwordError = passwordError,
                error = null,
                isLoading = false
            )
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            val result = loginUseCase(
                email = email,
                password = password
            )

            when (result) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        nextDestination = resolveNextDestination()
                    )
                }

                is Resource.Error -> {
                    val message = result.message?.let { msg ->
                        if (msg.contains("verify your email", ignoreCase = true)) {
                            context.getString(R.string.login_error_email_not_verified)
                        } else {
                            msg
                        }
                    }
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = message
                    )
                }

                is Resource.Loading -> {
                    _state.value = _state.value.copy(isLoading = true)
                }
            }
        }
    }

    private suspend fun resolveNextDestination(): String {
        return when (val profileResult = getMyProfileUseCase()) {
            is Resource.Success -> {
                if (profileResult.data?.onboardingCompleted == true) "main" else "onboarding"
            }
            else -> "main"
        }
    }
}
