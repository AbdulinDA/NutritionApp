package com.abdulin.nutritionapp.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdulin.nutritionapp.R
import com.abdulin.nutritionapp.core.utils.Resource
import com.abdulin.nutritionapp.data.dto.auth.RegisterRequestDto
import com.abdulin.nutritionapp.domain.usecase.GetMyProfileUseCase
import com.abdulin.nutritionapp.domain.usecase.LoginUseCase
import com.abdulin.nutritionapp.domain.usecase.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val registerUseCase: RegisterUseCase,
    private val loginUseCase: LoginUseCase,
    private val getMyProfileUseCase: GetMyProfileUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<Resource<String>?>(null)
    val state = _state.asStateFlow()

    fun register(
        email: String,
        password: String
    ) {
        viewModelScope.launch {
            _state.value = Resource.Loading()
            val request = RegisterRequestDto(
                email = email,
                password = password
            )
            when (val registerResult = registerUseCase(request)) {
                is Resource.Success -> {
                    when (val loginResult = loginUseCase(email = email, password = password)) {
                        is Resource.Success -> {
                            val destination = resolveNextDestination()
                            _state.value = Resource.Success(destination)
                        }
                        is Resource.Error -> {
                            _state.value = Resource.Error(
                                loginResult.message ?: context.getString(R.string.onboarding_error_auto_login)
                            )
                        }
                        is Resource.Loading -> {
                            _state.value = Resource.Loading()
                        }
                    }
                }
                is Resource.Error -> _state.value = Resource.Error(registerResult.message ?: "Registration failed")
                is Resource.Loading -> _state.value = Resource.Loading()
            }
        }
    }

    private suspend fun resolveNextDestination(): String {
        return when (val profileResult = getMyProfileUseCase()) {
            is Resource.Success -> {
                if (profileResult.data?.onboardingCompleted == true) "main" else "onboarding"
            }
            else -> "onboarding"
        }
    }
}
