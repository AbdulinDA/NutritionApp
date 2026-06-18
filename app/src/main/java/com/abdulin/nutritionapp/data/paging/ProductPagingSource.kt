package com.abdulin.nutritionapp.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.abdulin.nutritionapp.data.dto.common.PageResponse
import com.abdulin.nutritionapp.data.dto.product.ProductDto
import com.abdulin.nutritionapp.data.remote.NutritionApi
import java.io.IOException

class ProductPagingSource(
    private val api: NutritionApi,
    private val query: String,
    private val fallbackErrorMessage: String
) : PagingSource<Int, ProductDto>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ProductDto> {
        val page = params.key ?: 0
        return try {
            val response = if (query.isBlank()) {
                api.getProducts(page = page, size = params.loadSize)
            } else {
                api.searchProducts(query = query, page = page, size = params.loadSize)
            }
            val body = response.body()

            if (response.isSuccessful && body != null && body.success) {
                val data = body.data ?: PageResponse(
                    content = emptyList(),
                    page = page,
                    size = params.loadSize,
                    totalElements = 0,
                    totalPages = 0,
                    last = true
                )
                LoadResult.Page(
                    data = data.content,
                    prevKey = if (page == 0) null else page - 1,
                    nextKey = if (data.last) null else page + 1
                )
            } else {
                LoadResult.Error(IOException(body?.error?.message ?: fallbackErrorMessage))
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, ProductDto>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}
