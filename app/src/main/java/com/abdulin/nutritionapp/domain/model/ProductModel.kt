package com.abdulin.nutritionapp.domain.model

data class ProductModel(
    val id: Long,
    val name: String,
    val brand: String?,
    val calories: Double,
    val protein: Double,
    val fat: Double,
    val carbs: Double,
    val imageUrl: String?
)
