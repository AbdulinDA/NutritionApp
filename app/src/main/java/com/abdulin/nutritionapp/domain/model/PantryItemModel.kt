package com.abdulin.nutritionapp.domain.model

data class PantryItemModel(
    val pantryItemId: Long,
    val productId: Long,
    val productName: String,
    val productCategory: String? = null,
    val quantity: Double,
    val unitCode: String,
    val quantityGrams: Double? = null,
    val expiresAt: String? = null,
    val source: String? = null
)
