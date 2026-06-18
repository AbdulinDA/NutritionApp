package com.abdulin.nutritionapp.presentation.health

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdulin.nutritionapp.data.health.HealthConnectManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HealthConnectUiState(
    val isAvailable: Boolean = false,
    val hasPermissions: Boolean = false,
    val steps: Long = 0,
    val activeCalories: Double = 0.0,
    val latestWeightKg: Double? = null,
    val sleepMinutes: Long = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class HealthConnectViewModel @Inject constructor(
    private val healthConnectManager: HealthConnectManager
) : ViewModel() {
    private val _state = MutableStateFlow(HealthConnectUiState())
    val state = _state.asStateFlow()

    val permissions get() = healthConnectManager.permissions

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val available = healthConnectManager.isHealthConnectAvailable()
            val hasPermissions = available && healthConnectManager.hasAllPermissions()
            _state.update {
                it.copy(
                    isAvailable = available,
                    hasPermissions = hasPermissions,
                    steps = if (hasPermissions) healthConnectManager.readStepsForToday() else 0,
                    activeCalories = if (hasPermissions) healthConnectManager.readActiveCaloriesForToday() else 0.0,
                    latestWeightKg = if (hasPermissions) healthConnectManager.readLatestWeightKg() else null,
                    sleepMinutes = if (hasPermissions) healthConnectManager.readSleepMinutesLastNight() else 0,
                    isLoading = false
                )
            }
        }
    }
}
