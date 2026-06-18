package com.abdulin.nutritionapp.domain.usecase

import com.abdulin.nutritionapp.domain.repository.HomeRepository
import javax.inject.Inject

class GetHomeDataUseCase @Inject constructor(
    private val repository: HomeRepository
) {

    suspend operator fun invoke() =
        repository.getHomeData()
}