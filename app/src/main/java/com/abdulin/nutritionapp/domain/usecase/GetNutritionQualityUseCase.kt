package com.abdulin.nutritionapp.domain.usecase

import android.content.Context
import com.abdulin.nutritionapp.R
import com.abdulin.nutritionapp.domain.model.MetricStatus
import com.abdulin.nutritionapp.domain.model.NutritionQualityReport
import com.abdulin.nutritionapp.domain.model.QualityMetric
import com.abdulin.nutritionapp.domain.repository.FoodDiaryRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

class GetNutritionQualityUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: FoodDiaryRepository
) {
    suspend operator fun invoke(): NutritionQualityReport {
        val calendar = Calendar.getInstance()
        val endDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val startDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

        val entries = repository.getEntriesBetweenDates(startDate, endDate)

        if (entries.isEmpty()) {
            return NutritionQualityReport(
                healthIndex = 0,
                verdict = context.getString(R.string.analytics_quality_verdict_no_data),
                recommendations = listOf(context.getString(R.string.analytics_quality_recommendation_start_logging)),
                metrics = emptyList()
            )
        }

        val totalProtein = entries.sumOf { it.protein }
        val processedFoodRatio = entries.count {
            val name = it.productName.lowercase()
            name.contains("бургер") || name.contains("cola") || name.contains("кола") || name.contains("burger")
        }.toDouble() / entries.size

        var score = 100
        val recommendations = mutableListOf<String>()
        val metrics = mutableListOf<QualityMetric>()

        val averageProtein = totalProtein / entries.size
        val proteinStatus = if (averageProtein > 20) {
            MetricStatus.GOOD
        } else {
            score -= 15
            recommendations.add(context.getString(R.string.analytics_quality_recommendation_more_protein))
            MetricStatus.WARNING
        }
        metrics.add(
            QualityMetric(
                context.getString(R.string.analytics_quality_metric_protein),
                averageProtein,
                100.0,
                proteinStatus
            )
        )

        val processedStatus = if (processedFoodRatio < 0.2) {
            MetricStatus.GOOD
        } else {
            score -= 20
            recommendations.add(context.getString(R.string.analytics_quality_recommendation_less_processed))
            MetricStatus.CRITICAL
        }
        metrics.add(
            QualityMetric(
                context.getString(R.string.analytics_quality_metric_naturalness),
                (1 - processedFoodRatio) * 100,
                80.0,
                processedStatus
            )
        )

        val verdict = when {
            score > 80 -> context.getString(R.string.analytics_quality_verdict_great)
            score > 50 -> context.getString(R.string.analytics_quality_verdict_good)
            else -> context.getString(R.string.analytics_quality_verdict_needs_work)
        }

        return NutritionQualityReport(
            healthIndex = score.coerceIn(0, 100),
            verdict = verdict,
            recommendations = recommendations,
            metrics = metrics
        )
    }
}
