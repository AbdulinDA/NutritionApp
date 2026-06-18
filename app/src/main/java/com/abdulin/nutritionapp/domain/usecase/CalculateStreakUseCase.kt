package com.abdulin.nutritionapp.domain.usecase

import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class CalculateStreakUseCase @Inject constructor() {
    operator fun invoke(dates: List<String>): Int {
        if (dates.isEmpty()) return 0

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sortedDates = dates.mapNotNull { 
            try { sdf.parse(it) } catch (e: Exception) { null } 
        }.distinct().sortedDescending()

        if (sortedDates.isEmpty()) return 0

        val calendar = Calendar.getInstance()
        val today = calendar.time
        
        // Normalize today to start of day
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val normalizedToday = calendar.time

        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val normalizedYesterday = calendar.time

        var currentStreak = 0
        var expectedDate = normalizedToday

        // If today is not in the list, check if yesterday is. 
        // If yesterday is also not there, streak is 0.
        val firstDate = sortedDates[0]
        if (firstDate < normalizedYesterday) return 0
        
        if (firstDate < normalizedToday) {
            expectedDate = normalizedYesterday
        }

        for (date in sortedDates) {
            if (date == expectedDate) {
                currentStreak++
                calendar.time = expectedDate
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                expectedDate = calendar.time
            } else if (date < expectedDate) {
                break
            }
        }

        return currentStreak
    }
}
