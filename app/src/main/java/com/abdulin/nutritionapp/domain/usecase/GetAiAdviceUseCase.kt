package com.abdulin.nutritionapp.domain.usecase

import android.content.Context
import com.abdulin.nutritionapp.R
import com.abdulin.nutritionapp.data.fasting.FastingState
import com.abdulin.nutritionapp.domain.model.FoodDiaryEntry
import com.abdulin.nutritionapp.domain.model.HomeModel
import com.abdulin.nutritionapp.domain.model.MealType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

data class AiAdvice(
    val title: String,
    val message: String,
    val actionText: String? = null,
    val type: AdviceType = AdviceType.INFO
)

enum class AdviceType {
    INFO, WARNING, SUCCESS, CALORIES
}

data class CoachContext(
    val steps: Long = 0,
    val activeCalories: Double = 0.0,
    val sleepMinutes: Long = 0,
    val fastingState: FastingState = FastingState(),
    val recentEntriesByDate: Map<String, List<FoodDiaryEntry>> = emptyMap()
)

class GetAiAdviceUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    operator fun invoke(homeData: HomeModel, coachContext: CoachContext = CoachContext()): AiAdvice {
        val targetCalories = homeData.targetCalories.coerceAtLeast(1.0)
        val remainingCalories = targetCalories - homeData.calories
        val serverInsight = homeData.aiMessage?.takeIf { it.isNotBlank() }
        val recentProteinStreak = countRecentProteinGapDays(coachContext.recentEntriesByDate, homeData.targetProtein)
        val heavyDinnerDays = countHeavyDinnerDays(coachContext.recentEntriesByDate)
        val recentFastingWins = countRecentCompletedFasts(coachContext.fastingState)
        val fastingConsistency = isFastingConsistent(coachContext.fastingState)
        val fastingUnderTarget = isFastingUnderTarget(coachContext.fastingState)
        val fastingHours = if (coachContext.fastingState.isActive) {
            ((System.currentTimeMillis() - coachContext.fastingState.startMillis) / 3_600_000L).coerceAtLeast(0)
        } else {
            0
        }

        return when {
            coachContext.sleepMinutes in 1..359 -> AiAdvice(
                title = context.getString(R.string.ai_advice_sleep_title),
                message = context.getString(R.string.ai_advice_sleep_message),
                actionText = context.getString(R.string.ai_advice_sleep_action),
                type = AdviceType.WARNING
            )

            recentProteinStreak >= 3 -> AiAdvice(
                title = context.getString(R.string.ai_advice_protein_streak_title),
                message = context.getString(R.string.ai_advice_protein_streak_message),
                actionText = context.getString(R.string.ai_advice_protein_streak_action),
                type = AdviceType.INFO
            )

            fastingConsistency && recentFastingWins >= 3 -> AiAdvice(
                title = context.getString(R.string.ai_advice_fasting_consistency_title),
                message = context.getString(R.string.ai_advice_fasting_consistency_message),
                actionText = context.getString(R.string.ai_advice_fasting_consistency_action),
                type = AdviceType.SUCCESS
            )

            fastingUnderTarget && !coachContext.fastingState.isActive -> AiAdvice(
                title = context.getString(R.string.ai_advice_fasting_adjust_title),
                message = context.getString(R.string.ai_advice_fasting_adjust_message),
                actionText = context.getString(R.string.ai_advice_fasting_adjust_action),
                type = AdviceType.INFO
            )

            heavyDinnerDays >= 2 -> AiAdvice(
                title = context.getString(R.string.ai_advice_dinner_title),
                message = context.getString(R.string.ai_advice_dinner_message),
                actionText = context.getString(R.string.ai_advice_dinner_action),
                type = AdviceType.CALORIES
            )

            coachContext.activeCalories >= 450.0 && remainingCalories > 250.0 -> AiAdvice(
                title = context.getString(R.string.ai_advice_activity_title),
                message = context.getString(R.string.ai_advice_activity_message, coachContext.activeCalories.toInt()),
                actionText = context.getString(R.string.ai_advice_activity_action),
                type = AdviceType.SUCCESS
            )

            coachContext.fastingState.isActive && fastingHours >= coachContext.fastingState.targetHours - 1L -> AiAdvice(
                title = context.getString(R.string.ai_advice_fasting_title),
                message = context.getString(R.string.ai_advice_fasting_message),
                actionText = context.getString(R.string.ai_advice_fasting_action),
                type = AdviceType.INFO
            )

            homeData.water < 1000 -> AiAdvice(
                title = context.getString(R.string.ai_advice_hydration_title),
                message = context.getString(R.string.ai_advice_hydration_message),
                actionText = context.getString(R.string.ai_advice_hydration_action),
                type = AdviceType.WARNING
            )

            homeData.protein < 50 && homeData.calories > 1000 -> AiAdvice(
                title = context.getString(R.string.ai_advice_protein_title),
                message = context.getString(R.string.ai_advice_protein_message),
                actionText = context.getString(R.string.ai_advice_protein_action),
                type = AdviceType.INFO
            )

            remainingCalories in 1.0..300.0 -> AiAdvice(
                title = context.getString(R.string.ai_advice_calories_title),
                message = context.getString(R.string.ai_advice_calories_message, remainingCalories.toInt()),
                actionText = context.getString(R.string.ai_advice_calories_action),
                type = AdviceType.CALORIES
            )

            remainingCalories <= 0 -> AiAdvice(
                title = context.getString(R.string.ai_advice_limit_title),
                message = context.getString(R.string.ai_advice_limit_message),
                type = AdviceType.WARNING
            )

            serverInsight != null && homeData.recommendations.isNotEmpty() -> AiAdvice(
                title = context.getString(R.string.ai_advice_personal_title),
                message = serverInsight,
                actionText = context.getString(R.string.ai_advice_personal_action),
                type = AdviceType.INFO
            )

            else -> AiAdvice(
                title = context.getString(R.string.ai_advice_progress_title),
                message = serverInsight ?: context.getString(R.string.ai_advice_progress_message),
                actionText = context.getString(R.string.ai_advice_progress_action),
                type = AdviceType.SUCCESS
            )
        }
    }

    private fun countRecentProteinGapDays(
        recentEntriesByDate: Map<String, List<FoodDiaryEntry>>,
        targetProtein: Double
    ): Int {
        if (recentEntriesByDate.isEmpty()) return 0
        val threshold = (targetProtein * 0.75).coerceAtLeast(50.0)
        return recentEntriesByDate.values.count { entries ->
            entries.sumOf { it.protein } < threshold
        }
    }

    private fun countHeavyDinnerDays(recentEntriesByDate: Map<String, List<FoodDiaryEntry>>): Int {
        return recentEntriesByDate.values.count { entries ->
            val totalCalories = entries.sumOf { it.calories }.coerceAtLeast(1.0)
            val dinnerCalories = entries
                .filter { it.mealType == MealType.DINNER }
                .sumOf { it.calories }
            dinnerCalories / totalCalories >= 0.45
        }
    }

    private fun countRecentCompletedFasts(fastingState: FastingState): Int {
        val sevenDaysAgo = System.currentTimeMillis() - 7L * 24L * 60L * 60L * 1000L
        return fastingState.recentHistory.count { it.completedAtMillis >= sevenDaysAgo }
    }

    private fun isFastingConsistent(fastingState: FastingState): Boolean {
        if (fastingState.recentHistory.size < 3) return false
        val targetMillis = fastingState.targetMillis
        return fastingState.recentHistory
            .take(3)
            .all { historyEntry -> historyEntry.durationMillis >= targetMillis * 0.9 }
    }

    private fun isFastingUnderTarget(fastingState: FastingState): Boolean {
        if (fastingState.recentHistory.size < 2) return false
        val targetMillis = fastingState.targetMillis
        val recentAverage = fastingState.recentHistory
            .take(3)
            .map { it.durationMillis }
            .average()
        return recentAverage in 1.0..(targetMillis * 0.75)
    }
}
