package com.abdulin.nutritionapp.domain.usecase

import android.content.Context
import com.abdulin.nutritionapp.R
import com.abdulin.nutritionapp.core.utils.Resource
import com.abdulin.nutritionapp.domain.model.ProductModel
import com.abdulin.nutritionapp.domain.repository.ProductRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class GetProductAlternativesUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: ProductRepository
) {
    suspend operator fun invoke(product: ProductModel): Resource<List<ProductModel>> {
        val searchResult = repository.searchProducts(product.name.split(" ").first(), 0, 20)

        return when (searchResult) {
            is Resource.Success -> {
                val alternatives = searchResult.data?.content?.filter {
                    it.id != product.id && it.calories < product.calories
                }?.sortedBy { it.calories } ?: emptyList()
                Resource.Success(alternatives.take(3))
            }

            is Resource.Error -> Resource.Error(searchResult.message ?: context.getString(R.string.product_alternatives_error))
            is Resource.Loading -> Resource.Loading()
        }
    }
}
