package com.abdulin.nutritionapp.data.fasting

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.fastingDataStore by preferencesDataStore(name = "fasting_prefs")

data class FastingHistoryEntry(
    val completedAtMillis: Long,
    val durationMillis: Long,
    val targetHours: Int
)

data class FastingState(
    val startMillis: Long = 0L,
    val targetHours: Int = 16,
    val completedCount: Int = 0,
    val recentHistory: List<FastingHistoryEntry> = emptyList()
) {
    val isActive: Boolean get() = startMillis > 0L
    val targetMillis: Long get() = targetHours * 60L * 60L * 1000L
    val lastCompletedFast: FastingHistoryEntry? get() = recentHistory.maxByOrNull { it.completedAtMillis }
    val averageCompletedMillis: Long
        get() = if (recentHistory.isEmpty()) 0L else recentHistory.sumOf { it.durationMillis } / recentHistory.size
    val longestCompletedMillis: Long get() = recentHistory.maxOfOrNull { it.durationMillis } ?: 0L
}

@Singleton
class FastingRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val maxHistoryEntries = 12
    private val startMillisKey = longPreferencesKey("fasting_start_millis")
    private val targetHoursKey = intPreferencesKey("fasting_target_hours")
    private val completedCountKey = intPreferencesKey("fasting_completed_count")
    private val historyKey = stringSetPreferencesKey("fasting_history_entries")

    val fastingState: Flow<FastingState> = context.fastingDataStore.data.map { prefs ->
        FastingState(
            startMillis = prefs[startMillisKey] ?: 0L,
            targetHours = prefs[targetHoursKey] ?: 16,
            completedCount = prefs[completedCountKey] ?: 0,
            recentHistory = decodeHistory(prefs[historyKey])
        )
    }

    suspend fun setTargetHours(hours: Int) {
        context.fastingDataStore.edit { prefs ->
            prefs[targetHoursKey] = hours.coerceIn(8, 24)
        }
    }

    suspend fun startFasting(startMillis: Long, targetHours: Int) {
        context.fastingDataStore.edit { prefs ->
            prefs[startMillisKey] = startMillis
            prefs[targetHoursKey] = targetHours.coerceIn(8, 24)
        }
    }

    suspend fun stopFasting(markCompleted: Boolean) {
        context.fastingDataStore.edit { prefs ->
            val startMillis = prefs[startMillisKey] ?: 0L
            val targetHours = prefs[targetHoursKey] ?: 16
            prefs[startMillisKey] = 0L
            if (markCompleted) {
                prefs[completedCountKey] = (prefs[completedCountKey] ?: 0) + 1
                val durationMillis = (System.currentTimeMillis() - startMillis).coerceAtLeast(0L)
                val updatedHistory = (
                    decodeHistory(prefs[historyKey]) +
                        FastingHistoryEntry(
                            completedAtMillis = System.currentTimeMillis(),
                            durationMillis = durationMillis,
                            targetHours = targetHours
                        )
                    )
                    .sortedByDescending { it.completedAtMillis }
                    .take(maxHistoryEntries)
                prefs[historyKey] = encodeHistory(updatedHistory)
            }
        }
    }

    private fun decodeHistory(rawValues: Set<String>?): List<FastingHistoryEntry> {
        return rawValues
            .orEmpty()
            .mapNotNull { entry ->
                val parts = entry.split("|")
                if (parts.size != 3) return@mapNotNull null
                val completedAt = parts[0].toLongOrNull() ?: return@mapNotNull null
                val durationMillis = parts[1].toLongOrNull() ?: return@mapNotNull null
                val targetHours = parts[2].toIntOrNull() ?: return@mapNotNull null
                FastingHistoryEntry(
                    completedAtMillis = completedAt,
                    durationMillis = durationMillis,
                    targetHours = targetHours
                )
            }
            .sortedByDescending { it.completedAtMillis }
    }

    private fun encodeHistory(history: List<FastingHistoryEntry>): Set<String> {
        return history.map { entry ->
            "${entry.completedAtMillis}|${entry.durationMillis}|${entry.targetHours}"
        }.toSet()
    }
}
