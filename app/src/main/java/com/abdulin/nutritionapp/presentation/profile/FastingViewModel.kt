package com.abdulin.nutritionapp.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.abdulin.nutritionapp.data.fasting.FastingReminderWorker
import com.abdulin.nutritionapp.data.fasting.FastingRepository
import com.abdulin.nutritionapp.data.fasting.FastingState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class FastingViewModel @Inject constructor(
    private val fastingRepository: FastingRepository,
    private val workManager: WorkManager
) : ViewModel() {
    val state: StateFlow<FastingState> = fastingRepository.fastingState.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        FastingState()
    )

    fun setTargetHours(hours: Int) {
        viewModelScope.launch { fastingRepository.setTargetHours(hours) }
    }

    fun start() {
        val current = state.value
        viewModelScope.launch {
            startFastingSession(current.targetHours)
        }
    }

    fun stop() {
        viewModelScope.launch {
            stopFastingSession(state.value)
        }
    }

    suspend fun startFastingSession(targetHours: Int = state.value.targetHours) {
        fastingRepository.startFasting(System.currentTimeMillis(), targetHours)
        scheduleReminder(targetHours)
    }

    suspend fun stopFastingSession(current: FastingState = state.value) {
        val elapsed = System.currentTimeMillis() - current.startMillis
        fastingRepository.stopFasting(markCompleted = elapsed >= current.targetMillis)
        workManager.cancelUniqueWork(WORK_NAME)
    }

    private fun scheduleReminder(targetHours: Int) {
        val request = OneTimeWorkRequestBuilder<FastingReminderWorker>()
            .setInitialDelay(targetHours.toLong(), TimeUnit.HOURS)
            .build()
        workManager.enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, request)
    }

    private companion object {
        const val WORK_NAME = "fasting_finish_reminder"
    }
}
