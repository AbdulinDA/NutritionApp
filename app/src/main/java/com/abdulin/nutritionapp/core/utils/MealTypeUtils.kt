package com.abdulin.nutritionapp.core.utils

import com.abdulin.nutritionapp.R
import com.abdulin.nutritionapp.domain.model.MealType

fun MealType.labelRes(): Int {
    return when (this) {
        MealType.BREAKFAST -> R.string.diary_meal_breakfast
        MealType.LUNCH -> R.string.diary_meal_lunch
        MealType.DINNER -> R.string.diary_meal_dinner
        MealType.SNACK -> R.string.diary_meal_snack
    }
}
