package com.abdulin.nutritionapp.domain.usecase

import com.abdulin.nutritionapp.data.local.entity.WeightEntity
import com.abdulin.nutritionapp.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetWeightHistoryUseCase @Inject constructor(
    private val repository: UserRepository
) {
    operator fun invoke(): Flow<List<WeightEntity>> {
        return repository.getWeightHistory()
    }
}
