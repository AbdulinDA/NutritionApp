package com.abdulin.nutritionapp.data.repository

import android.content.Context
import com.abdulin.nutritionapp.R
import com.abdulin.nutritionapp.core.network.safeApiCall
import com.abdulin.nutritionapp.core.utils.Resource
import com.abdulin.nutritionapp.data.dto.pantry.PantryItemDto
import com.abdulin.nutritionapp.data.dto.pantry.PantryItemRequestDto
import com.abdulin.nutritionapp.data.remote.NutritionApi
import com.abdulin.nutritionapp.domain.model.PantryItemModel
import com.abdulin.nutritionapp.domain.repository.PantryRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.JsonElement
import javax.inject.Inject

class PantryRepositoryImpl @Inject constructor(
    private val api: NutritionApi,
    @ApplicationContext private val context: Context
) : PantryRepository {

    override suspend fun getPantry(): Resource<List<PantryItemModel>> {
        return when (val result = safeApiCall<List<PantryItemDto>> { api.getPantry() }) {
            is Resource.Success -> Resource.Success(result.data.orEmpty().map { it.toDomain() })
            is Resource.Error -> Resource.Error(result.message ?: context.getString(R.string.fridge_error_load))
            is Resource.Loading -> Resource.Loading()
        }
    }

    override suspend fun addPantryItem(productId: Long): Resource<PantryItemModel> {
        val request = PantryItemRequestDto(productId = productId, quantity = 1.0)
        return when (val result = safeApiCall<PantryItemDto> { api.addPantryItem(request) }) {
            is Resource.Success -> Resource.Success(result.data!!.toDomain())
            is Resource.Error -> Resource.Error(result.message ?: context.getString(R.string.fridge_error_update))
            is Resource.Loading -> Resource.Loading()
        }
    }

    override suspend fun removePantryItem(pantryItemId: Long): Resource<Unit> {
        return when (val result = safeApiCall<JsonElement> { api.deletePantryItem(pantryItemId) }) {
            is Resource.Success -> Resource.Success(Unit)
            is Resource.Error -> Resource.Error(result.message ?: context.getString(R.string.fridge_error_update))
            is Resource.Loading -> Resource.Loading()
        }
    }

    private fun PantryItemDto.toDomain() = PantryItemModel(
        pantryItemId = pantryItemId,
        productId = productId,
        productName = productName,
        productCategory = productCategory,
        quantity = quantity,
        unitCode = unitCode,
        quantityGrams = quantityGrams,
        expiresAt = expiresAt,
        source = source
    )
}
