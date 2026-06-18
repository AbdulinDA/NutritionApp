package com.abdulin.nutritionapp.domain.usecase

import com.abdulin.nutritionapp.domain.repository.ProductRepository
import javax.inject.Inject

class ToggleFavoriteProductUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(productId: Long) = repository.toggleFavorite(productId)
}
