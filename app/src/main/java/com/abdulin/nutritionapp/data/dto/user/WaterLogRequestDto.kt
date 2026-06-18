package com.abdulin.nutritionapp.data.dto.user

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class WaterLogRequestDto(
    @SerialName("amountMl")
    val amount: Int,
    @SerialName("entryDate")
    val date: String
)
