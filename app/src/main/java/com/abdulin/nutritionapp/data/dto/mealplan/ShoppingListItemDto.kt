package com.abdulin.nutritionapp.data.dto.mealplan

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ShoppingListItemDto(
    val productId: Long,
    val productName: String,
    val category: String,
    val totalQuantity: Double,
    @SerialName("measurementUnitCode")
    val unit: String? = null
)
