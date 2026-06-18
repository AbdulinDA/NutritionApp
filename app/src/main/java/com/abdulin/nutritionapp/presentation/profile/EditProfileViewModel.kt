package com.abdulin.nutritionapp.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdulin.nutritionapp.R
import com.abdulin.nutritionapp.core.utils.Resource
import com.abdulin.nutritionapp.core.utils.toUiActivityLevel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import com.abdulin.nutritionapp.data.dto.auth.UserResponseDto
import com.abdulin.nutritionapp.domain.repository.UserRepository
import com.abdulin.nutritionapp.domain.usecase.GetMyProfileUseCase
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditProfileUiState(
    val user: UserResponseDto? = null,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getMyProfileUseCase: GetMyProfileUseCase,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(EditProfileUiState())
    val state = _state.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val result = getMyProfileUseCase()
            if (result is Resource.Success) {
                val user = result.data?.copy(
                    activityLevel = result.data.activityLevel?.toUiActivityLevel() ?: "MODERATE"
                )
                _state.update { it.copy(user = user, isLoading = false) }
            } else {
                _state.update { it.copy(error = result.message, isLoading = false) }
            }
        }
    }

    fun updateProfile(
        firstName: String,
        lastName: String,
        weight: Double,
        targetWeight: Double,
        goalTypeId: Long?,
        activityLevel: String
    ) {
        if (_state.value.isLoading) {
            return
        }

        viewModelScope.launch {
            val currentUser = _state.value.user
            if (currentUser == null) {
                _state.update { it.copy(error = context.getString(R.string.profile_error_profile_not_loaded)) }
                return@launch
            }

            _state.update { it.copy(isLoading = true, error = null) }
            val result = userRepository.updateProfile(
                currentUser = currentUser,
                firstName = firstName,
                lastName = lastName,
                weightKg = weight,
                targetWeightKg = targetWeight,
                goalTypeId = goalTypeId,
                activityLevel = activityLevel
            )

            if (result is Resource.Success) {
                val updatedUser = result.data?.copy(
                    activityLevel = result.data.activityLevel?.toUiActivityLevel() ?: "MODERATE"
                )
                _state.update {
                    it.copy(
                        user = updatedUser,
                        isSaved = true,
                        isLoading = false,
                        error = null
                    )
                }
            } else {
                _state.update { it.copy(error = result.message, isLoading = false) }
            }
        }
    }
}
