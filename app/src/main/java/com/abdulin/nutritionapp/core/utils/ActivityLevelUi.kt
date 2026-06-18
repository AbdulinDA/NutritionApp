package com.abdulin.nutritionapp.core.utils

import androidx.annotation.StringRes
import com.abdulin.nutritionapp.R

val SupportedUiActivityLevels = listOf(
    "SEDENTARY",
    "LIGHT",
    "MODERATE",
    "ACTIVE",
    "VERY_ACTIVE"
)

fun String.normalizeUiActivityLevel(): String {
    return when (trim().uppercase()) {
        "SEDENTARY" -> "SEDENTARY"
        "LIGHT" -> "LIGHT"
        "MODERATE" -> "MODERATE"
        "ACTIVE" -> "ACTIVE"
        "VERY_ACTIVE", "EXTRA_ACTIVE" -> "VERY_ACTIVE"
        else -> toUiActivityLevel()
    }
}

@StringRes
fun String.toActivityLabelRes(): Int {
    return when (normalizeUiActivityLevel()) {
        "SEDENTARY" -> R.string.activity_sedentary
        "LIGHT" -> R.string.activity_light
        "MODERATE" -> R.string.activity_moderate
        "ACTIVE" -> R.string.activity_active
        "VERY_ACTIVE" -> R.string.activity_very_active
        else -> R.string.activity_moderate
    }
}
