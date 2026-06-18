package com.abdulin.nutritionapp.core.utils

import java.util.Locale

fun localizeMeasurementUnit(
    unit: String?,
    locale: Locale = Locale.getDefault()
): String? {
    val raw = unit?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    val normalized = raw.lowercase(Locale.ROOT)
    val isRussian = locale.language.equals("ru", ignoreCase = true)

    return when (normalized) {
        "g", "gram", "grams", "гр", "г" -> if (isRussian) "г" else "g"
        "kg", "kilogram", "kilograms", "кг" -> if (isRussian) "кг" else "kg"
        "ml", "мл" -> if (isRussian) "мл" else "ml"
        "l", "lt", "liter", "liters", "л" -> if (isRussian) "л" else "l"
        "piece", "pieces", "pc", "pcs", "шт", "шт.", "штука", "штуки", "штук" -> if (isRussian) "шт" else "pcs"
        "cup", "cups", "стакан", "стакана", "стаканов" -> if (isRussian) "стак." else "cup"
        "tbsp", "ст. л.", "ст л", "столовая ложка", "столовые ложки" -> if (isRussian) "ст. л." else "tbsp"
        "tsp", "ч. л.", "ч л", "чайная ложка", "чайные ложки" -> if (isRussian) "ч. л." else "tsp"
        else -> raw
    }
}
