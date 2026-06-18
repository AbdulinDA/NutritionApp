package com.abdulin.nutritionapp.core.utils

fun String.toServerGender(): String {
    return when (trim().uppercase()) {
        "MALE" -> "male"
        "FEMALE" -> "female"
        else -> trim().lowercase()
    }
}

fun String.toServerActivityLevel(): String {
    return when (trim().uppercase()) {
        "SEDENTARY" -> "sedentary"
        "LIGHT" -> "light"
        "MODERATE" -> "moderate"
        "ACTIVE" -> "active"
        "VERY_ACTIVE" -> "very_active"
        "EXTRA_ACTIVE" -> "very_active"
        else -> trim().lowercase()
    }
}

fun String.toUiActivityLevel(): String {
    return when (trim().lowercase()) {
        "sedentary" -> "SEDENTARY"
        "light" -> "LIGHT"
        "moderate" -> "MODERATE"
        "active" -> "ACTIVE"
        "very_active" -> "VERY_ACTIVE"
        else -> trim().uppercase()
    }
}
