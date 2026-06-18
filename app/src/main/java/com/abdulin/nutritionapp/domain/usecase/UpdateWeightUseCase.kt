package com.abdulin.nutritionapp.domain.usecase

import com.abdulin.nutritionapp.core.utils.Resource
import com.abdulin.nutritionapp.data.health.HealthConnectManager
import com.abdulin.nutritionapp.domain.repository.UserRepository
import javax.inject.Inject

/**
 * Use case для обновления веса пользователя с синхронизацией в Health Connect.
 */
class UpdateWeightUseCase @Inject constructor(
    private val repository: UserRepository,
    private val healthConnectManager: HealthConnectManager
) {
    suspend operator fun invoke(weightKg: Double): Resource<Unit> {
        // 1. Обновляем на нашем сервере
        val result = repository.updateWeight(weightKg)
        
        // 2. Если успешно, дублируем запись в Google Health
        if (result is Resource.Success) {
            healthConnectManager.writeWeight(weightKg)
        }
        
        return result
    }
}
