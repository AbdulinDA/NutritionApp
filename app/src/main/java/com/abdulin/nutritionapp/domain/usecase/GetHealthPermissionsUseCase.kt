package com.abdulin.nutritionapp.domain.usecase

import com.abdulin.nutritionapp.data.health.HealthConnectManager
import javax.inject.Inject

class GetHealthPermissionsUseCase @Inject constructor(
    private val healthConnectManager: HealthConnectManager
) {
    fun getPermissions() = healthConnectManager.permissions

    suspend fun hasPermissions() = healthConnectManager.hasAllPermissions()
    
    fun isAvailable() = healthConnectManager.isHealthConnectAvailable()
}
