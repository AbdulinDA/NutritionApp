package com.abdulin.nutritionapp.domain.repository

import androidx.paging.PagingData
import com.abdulin.nutritionapp.core.utils.Resource
import com.abdulin.nutritionapp.data.dto.common.PageResponse
import com.abdulin.nutritionapp.domain.model.ProductModel
import com.abdulin.nutritionapp.data.local.dao.ProductUsage
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun searchProductsPaging(query: String): Flow<PagingData<ProductModel>>
    
    // Standard search for cases where paging isn't needed (like alternatives)
    suspend fun searchProducts(
        query: String,
        page: Int,
        size: Int
    ): Resource<PageResponse<ProductModel>>

    suspend fun getProductByBarcode(barcode: String): Resource<ProductModel>
    suspend fun toggleFavorite(productId: Long): Resource<Unit>
    suspend fun getFrequentProducts(limit: Int): List<ProductUsage>
    suspend fun getFrequentProductsByMealType(mealType: String, limit: Int): List<ProductUsage>
}
