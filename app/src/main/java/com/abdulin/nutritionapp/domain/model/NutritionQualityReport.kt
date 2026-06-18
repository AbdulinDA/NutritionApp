package com.abdulin.nutritionapp.domain.model

data class NutritionQualityReport(
    val healthIndex: Int, // 0-100
    val verdict: String,
    val recommendations: List<String>,
    val metrics: List<QualityMetric>
)

data class QualityMetric(
    val label: String,
    val value: Double,
    val target: Double,
    val status: MetricStatus
)

enum class MetricStatus {
    GOOD, WARNING, CRITICAL
}
