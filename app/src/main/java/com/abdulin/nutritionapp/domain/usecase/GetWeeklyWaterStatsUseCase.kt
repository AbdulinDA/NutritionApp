package com.abdulin.nutritionapp.domain.usecase

import com.abdulin.nutritionapp.data.local.dao.WaterStat
import com.abdulin.nutritionapp.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetWeeklyWaterStatsUseCase @Inject constructor(
    private val repository: UserRepository
) {
    operator fun invoke(): Flow<List<WaterStat>> {
        return repository.getWeeklyWaterStats()
    }
}
