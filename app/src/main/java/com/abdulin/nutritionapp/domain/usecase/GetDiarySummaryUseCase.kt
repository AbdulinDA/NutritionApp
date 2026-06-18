package com.abdulin.nutritionapp.domain.usecase

import com.abdulin.nutritionapp.domain.repository.FoodDiaryRepository
import javax.inject.Inject

class GetDiarySummaryUseCase @Inject constructor(
    private val repository: FoodDiaryRepository
) {

    suspend operator fun invoke(
        date: String
    ) = repository.getSummary(date)
}