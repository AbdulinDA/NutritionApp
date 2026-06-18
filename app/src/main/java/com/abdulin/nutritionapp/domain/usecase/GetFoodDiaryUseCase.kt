package com.abdulin.nutritionapp.domain.usecase

import com.abdulin.nutritionapp.domain.repository.FoodDiaryRepository
import javax.inject.Inject

/**
 * UseCase получения дневника
 */
class GetFoodDiaryUseCase @Inject constructor(
    private val repository: FoodDiaryRepository
) {

    suspend operator fun invoke(
        date: String
    ) = repository.getDiaryByDate(date)
}