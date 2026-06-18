package com.abdulin.nutritionapp.data.repository

import android.content.Context
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.abdulin.nutritionapp.R
import com.abdulin.nutritionapp.core.network.safeApiCall
import com.abdulin.nutritionapp.core.utils.Resource
import com.abdulin.nutritionapp.data.dto.common.PageResponse
import com.abdulin.nutritionapp.data.dto.product.ProductDto
import com.abdulin.nutritionapp.data.local.dao.DiaryDao
import com.abdulin.nutritionapp.data.local.dao.ProductUsage
import com.abdulin.nutritionapp.data.paging.ProductPagingSource
import com.abdulin.nutritionapp.data.remote.NutritionApi
import com.abdulin.nutritionapp.domain.model.ProductModel
import com.abdulin.nutritionapp.domain.repository.ProductRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.JsonElement
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val api: NutritionApi,
    private val diaryDao: DiaryDao,
    @ApplicationContext private val context: Context
) : ProductRepository {

    override fun searchProductsPaging(query: String): Flow<PagingData<ProductModel>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                ProductPagingSource(
                    api = api,
                    query = query,
                    fallbackErrorMessage = context.getString(R.string.network_unknown_server_error)
                )
            }
        ).flow.map { pagingData ->
            pagingData.map { it.toDomain() }
        }
    }

    override suspend fun searchProducts(
        query: String,
        page: Int,
        size: Int
    ): Resource<PageResponse<ProductModel>> {
        val result = safeApiCall<PageResponse<ProductDto>> {
            api.searchProducts(query, page, size)
        }

        return when (result) {
            is Resource.Success -> {
                val pageResponse = result.data ?: PageResponse(
                    content = emptyList(),
                    page = page,
                    size = size,
                    totalElements = 0,
                    totalPages = 0,
                    last = true
                )
                Resource.Success(
                    PageResponse(
                        content = pageResponse.content.map { it.toDomain() },
                        page = pageResponse.page,
                        size = pageResponse.size,
                        totalElements = pageResponse.totalElements,
                        totalPages = pageResponse.totalPages,
                        last = pageResponse.last
                    )
                )
            }
            is Resource.Error -> Resource.Error(
                result.message ?: context.getString(R.string.products_error_search)
            )
            is Resource.Loading -> Resource.Loading()
        }
    }

    override suspend fun getProductByBarcode(barcode: String): Resource<ProductModel> {
        val result = safeApiCall<ProductDto> {
            api.getProductByBarcode(barcode)
        }
        return when (result) {
            is Resource.Success -> Resource.Success(result.data!!.toDomain())
            is Resource.Error -> Resource.Error(
                result.message ?: context.getString(R.string.products_not_found_short)
            )
            is Resource.Loading -> Resource.Loading()
        }
    }

    override suspend fun toggleFavorite(productId: Long): Resource<Unit> {
        val result = safeApiCall<JsonElement> {
            api.toggleFavoriteProduct(productId)
        }
        return when (result) {
            is Resource.Success -> Resource.Success(Unit)
            is Resource.Error -> Resource.Error(
                result.message ?: context.getString(R.string.products_error_toggle_favorite)
            )
            is Resource.Loading -> Resource.Loading()
        }
    }

    override suspend fun getFrequentProducts(limit: Int): List<ProductUsage> {
        return diaryDao.getMostFrequentProducts(limit)
    }

    override suspend fun getFrequentProductsByMealType(mealType: String, limit: Int): List<ProductUsage> {
        return diaryDao.getFrequentProductsByMealType(mealType, limit)
    }
}

fun ProductDto.toDomain() = ProductModel(
    id = id ?: 0L,
    name = name ?: "",
    brand = brand,
    calories = calories ?: 0.0,
    protein = protein ?: 0.0,
    fat = fat ?: 0.0,
    carbs = carbs ?: 0.0,
    imageUrl = imageUrl
)
