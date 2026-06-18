package com.abdulin.nutritionapp.domain.repository

import com.abdulin.nutritionapp.core.utils.Resource
import com.abdulin.nutritionapp.domain.model.PantryItemModel

interface PantryRepository {
    suspend fun getPantry(): Resource<List<PantryItemModel>>
    suspend fun addPantryItem(productId: Long): Resource<PantryItemModel>
    suspend fun removePantryItem(pantryItemId: Long): Resource<Unit>
}
