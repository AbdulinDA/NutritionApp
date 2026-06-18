package com.abdulin.nutritionapp.data.dto.pantry

import kotlinx.serialization.Serializable

@Serializable
data class PantryItemDto(
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

@Serializable
data class PantryItemRequestDto(
    val productId: Long,
    val quantity: Double,
    val unitCode: String = "item",
    val quantityGrams: Double? = null,
    val expiresAt: String? = null,
    val source: String = "MANUAL"
)
