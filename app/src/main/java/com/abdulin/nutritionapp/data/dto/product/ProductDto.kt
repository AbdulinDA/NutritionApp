package com.abdulin.nutritionapp.data.dto.product

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProductDto(
    @SerialName("productId")
    val id: Long? = null,
    val publicId: String? = null,
    @SerialName("productName")
    val name: String? = null,
    val brand: String? = null,
    @SerialName("caloriesPer100g")
    val calories: Double? = null,
    @SerialName("proteinPer100g")
    val protein: Double? = null,
    @SerialName("fatPer100g")
    val fat: Double? = null,
    @SerialName("carbsPer100g")
    val carbs: Double? = null,
    val barcode: String? = null,
    val imageUrl: String? = null,
    val category: String? = null
)
