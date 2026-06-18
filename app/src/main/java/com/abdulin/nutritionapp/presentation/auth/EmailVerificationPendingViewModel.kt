package com.abdulin.nutritionapp.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdulin.nutritionapp.core.utils.Resource
import com.abdulin.nutritionapp.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EmailVerificationPendingState(
    val isSending: Boolean = false,
    val message: String? = null,
    val error: String? = null
)

@HiltViewModel
class EmailVerificationPendingViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(EmailVerificationPendingState())
    val state = _state.asStateFlow()

    fun resend(email: String) {
        if (email.isBlank()) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isSending = true, message = null, error = null)
            when (val result = authRepository.resendVerification(email)) {
                is Resource.Success -> _state.value = EmailVerificationPendingState(
                    isSending = false,
                    message = result.data?.message
                )
                is Resource.Error -> _state.value = EmailVerificationPendingState(
                    isSending = false,
                    error = result.message
                )
                is Resource.Loading -> _state.value = _state.value.copy(isSending = true)
            }
        }
    }
}
