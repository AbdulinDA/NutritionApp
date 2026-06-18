package com.abdulin.nutritionapp.domain.usecase

import android.content.Context
import com.abdulin.nutritionapp.R
import com.abdulin.nutritionapp.domain.model.HomeModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class GetAiAdviceUseCaseTest {

    private val context = mock<Context>()
    private val useCase = GetAiAdviceUseCase(context)

    init {
        whenever(context.getString(R.string.ai_advice_hydration_title)).thenReturn("Гидратация")
        whenever(context.getString(R.string.ai_advice_hydration_message)).thenReturn("Пора попить воды")
        whenever(context.getString(R.string.ai_advice_hydration_action)).thenReturn("Выпить 250 мл")
        whenever(context.getString(R.string.ai_advice_protein_title)).thenReturn("Нужен белок")
        whenever(context.getString(R.string.ai_advice_protein_message)).thenReturn("Доберите белок")
        whenever(context.getString(R.string.ai_advice_protein_action)).thenReturn("Подобрать блюдо")
        whenever(context.getString(R.string.ai_advice_calories_title)).thenReturn("Почти норма")
        whenever(context.getString(eq(R.string.ai_advice_calories_message), any())).thenAnswer {
            "Осталось ${it.arguments[1]} ккал"
        }
        whenever(context.getString(R.string.ai_advice_calories_action)).thenReturn("Подобрать ужин")
        whenever(context.getString(R.string.ai_advice_limit_title)).thenReturn("Лимит превышен")
        whenever(context.getString(R.string.ai_advice_limit_message)).thenReturn("Лучше выбрать что-то полегче")
        whenever(context.getString(R.string.ai_advice_personal_title)).thenReturn("Персональный совет")
        whenever(context.getString(R.string.ai_advice_personal_action)).thenReturn("Открыть рецепт")
        whenever(context.getString(R.string.ai_advice_progress_title)).thenReturn("Все идет хорошо")
        whenever(context.getString(R.string.ai_advice_progress_message)).thenReturn("Продолжайте в том же духе")
        whenever(context.getString(R.string.ai_advice_progress_action)).thenReturn("Посмотреть рекомендации")
    }

    @Test
    fun `prioritizes hydration advice when water is low`() {
        val advice = useCase(home(water = 750))

        assertEquals("Гидратация", advice.title)
        assertEquals(AdviceType.WARNING, advice.type)
        assertEquals("Выпить 250 мл", advice.actionText)
    }

    @Test
    fun `uses target calories instead of hardcoded limit`() {
        val advice = useCase(
            home(
                calories = 2150.0,
                targetCalories = 2300.0,
                water = 1800
            )
        )

        assertEquals("Почти норма", advice.title)
        assertEquals(AdviceType.CALORIES, advice.type)
        assertTrue(advice.message.contains("150"))
    }

    @Test
    fun `warns about calorie overage when limit is exceeded`() {
        val advice = useCase(
            home(
                calories = 2400.0,
                targetCalories = 2200.0,
                water = 1800,
                protein = 120.0
            )
        )

        assertEquals("Лимит превышен", advice.title)
        assertEquals(AdviceType.WARNING, advice.type)
    }

    private fun home(
        calories: Double = 1200.0,
        protein: Double = 90.0,
        targetCalories: Double = 2000.0,
        water: Int = 1500
    ) = HomeModel(
        calories = calories,
        protein = protein,
        fat = 50.0,
        carbs = 120.0,
        targetCalories = targetCalories,
        targetProtein = 140.0,
        targetFat = 70.0,
        targetCarbs = 230.0,
        water = water,
        targetWater = 2200,
        weight = 72.0
    )
}
