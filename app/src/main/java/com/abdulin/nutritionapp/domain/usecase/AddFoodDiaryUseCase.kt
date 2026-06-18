package com.abdulin.nutritionapp.domain.usecase

import com.abdulin.nutritionapp.data.dto.diary.CreateFoodDiaryRequestDto
import com.abdulin.nutritionapp.domain.repository.FoodDiaryRepository
import javax.inject.Inject

class AddFoodDiaryUseCase @Inject constructor(
    private val repository: FoodDiaryRepository
) {

    // operator позволяет вызывать use case как функцию:
    // addFoodDiaryUseCase(...)
    suspend operator fun invoke(
        request: CreateFoodDiaryRequestDto
    ) = repository.addFood(request)
}