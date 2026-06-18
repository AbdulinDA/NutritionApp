package com.abdulin.nutritionapp.domain.usecase

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class CalculateStreakUseCaseTest {

    private val useCase = CalculateStreakUseCase()
    private val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    @Test
    fun `returns streak when today and previous days are present`() {
        val dates = listOf(dayOffset(0), dayOffset(-1), dayOffset(-2))

        val result = useCase(dates)

        assertEquals(3, result)
    }

    @Test
    fun `returns streak from yesterday when today is missing`() {
        val dates = listOf(dayOffset(-1), dayOffset(-2))

        val result = useCase(dates)

        assertEquals(2, result)
    }

    @Test
    fun `returns zero when last completed day is older than yesterday`() {
        val dates = listOf(dayOffset(-2), dayOffset(-3))

        val result = useCase(dates)

        assertEquals(0, result)
    }

    private fun dayOffset(offset: Int): String {
        val calendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, offset)
        }
        return formatter.format(calendar.time)
    }
}
