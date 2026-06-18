package com.abdulin.nutritionapp.presentation.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.abdulin.nutritionapp.R
import com.abdulin.nutritionapp.core.utils.labelRes
import com.abdulin.nutritionapp.domain.model.MealPlanDecisionSignalModel
import com.abdulin.nutritionapp.domain.model.MealType
import com.abdulin.nutritionapp.domain.model.MealPlanReportModel
import com.abdulin.nutritionapp.domain.model.MetricStatus
import com.abdulin.nutritionapp.domain.model.NutritionQualityReport
import com.abdulin.nutritionapp.domain.model.RecommendationReportModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.analytics_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.onboarding_back))
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::refresh) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.analytics_refresh))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            PrimaryTabRow(selectedTabIndex = state.selectedTab.ordinal) {
                AnalyticsTab.entries.forEach { tab ->
                    Tab(
                        selected = state.selectedTab == tab,
                        onClick = { viewModel.onTabSelected(tab) },
                        text = {
                            Text(
                                text = when (tab) {
                                    AnalyticsTab.QUALITY -> stringResource(R.string.analytics_tab_quality)
                                    AnalyticsTab.PLAN -> stringResource(R.string.analytics_tab_plan)
                                    AnalyticsTab.CALORIES -> stringResource(R.string.analytics_tab_calories)
                                    AnalyticsTab.WATER -> stringResource(R.string.analytics_tab_water)
                                    AnalyticsTab.WEIGHT -> stringResource(R.string.analytics_tab_weight)
                                }
                            )
                        }
                    )
                }
            }

            BoxWithConstraints(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                val contentModifier = Modifier.fillMaxWidth(if (maxWidth > 600.dp) 0.72f else 1f)

                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (state.error != null) {
                    AnalyticsStateCard(
                        modifier = contentModifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        title = stringResource(R.string.analytics_error_title),
                        subtitle = state.error ?: stringResource(R.string.analytics_error_subtitle),
                        actionLabel = stringResource(R.string.retry),
                        onAction = viewModel::refresh
                    )
                } else {
                    Column(
                        modifier = contentModifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        AnalyticsOverviewSection(state)
                        AiInsightSection(state.aiInsight)

                        when (state.selectedTab) {
                            AnalyticsTab.QUALITY -> {
                                QualityReportSection(state.qualityReport)
                                RecommendationReportSection(state.recommendationReport)
                            }
                            AnalyticsTab.PLAN -> {
                                MealPlanReportSection(
                                    report = state.mealPlanReport,
                                    learnedInsights = state.learnedInsights,
                                    preferenceProfile = state.preferenceProfile,
                                    behaviorSignals = state.behaviorSignals
                                )
                            }

                            else -> {
                                val chartTitle = when (state.selectedTab) {
                                    AnalyticsTab.CALORIES -> stringResource(R.string.home_calories)
                                    AnalyticsTab.PLAN -> stringResource(R.string.analytics_tab_plan)
                                    AnalyticsTab.WATER -> stringResource(R.string.home_water)
                                    AnalyticsTab.WEIGHT -> stringResource(R.string.home_weight)
                                    else -> ""
                                }

                                Text(
                                    chartTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )

                                val chartData = when (state.selectedTab) {
                                    AnalyticsTab.CALORIES -> state.caloriesStats.map { it.second.toFloat() }
                                    AnalyticsTab.PLAN -> emptyList()
                                    AnalyticsTab.WATER -> state.waterStats.map { it.second.toFloat() }
                                    AnalyticsTab.WEIGHT -> state.weightStats.map { it.second.toFloat() }
                                    else -> emptyList()
                                }

                                if (chartData.isNotEmpty()) {
                                    SimpleLineChart(
                                        data = chartData,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp)
                                    )
                                } else {
                                    AnalyticsStateCard(
                                        title = stringResource(R.string.analytics_empty_chart_title),
                                        subtitle = stringResource(R.string.analytics_empty_chart_subtitle),
                                        actionLabel = stringResource(R.string.home_log_meal),
                                        onAction = {}
                                    )
                                }

                                AnalyticsSummaryCard(state)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalyticsStateCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    actionLabel: String,
    onAction: () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center
            )
            Button(onClick = onAction) {
                Text(actionLabel)
            }
        }
    }
}

@Composable
fun QualityReportSection(report: NutritionQualityReport?) {
    if (report == null) return

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stringResource(R.string.analytics_health_index), style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${report.healthIndex}",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(stringResource(R.string.analytics_out_of_100), style = MaterialTheme.typography.bodyMedium)
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    stringResource(R.string.analytics_summary_quality_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    stringResource(R.string.analytics_summary_quality_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(report.verdict, style = MaterialTheme.typography.bodyMedium)
            }
        }

        Text(stringResource(R.string.analytics_key_metrics), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        report.metrics.forEach { metric ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when (metric.status) {
                            MetricStatus.GOOD -> Icons.Default.CheckCircle
                            MetricStatus.WARNING -> Icons.Default.Warning
                            MetricStatus.CRITICAL -> Icons.Default.Error
                        },
                        contentDescription = null,
                        tint = when (metric.status) {
                            MetricStatus.GOOD -> Color(0xFF4CAF50)
                            MetricStatus.WARNING -> Color(0xFFFFC107)
                            MetricStatus.CRITICAL -> Color(0xFFF44336)
                        }
                    )
                    Spacer(modifier = Modifier.weight(0.08f))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(metric.label, fontWeight = FontWeight.Bold)
                        LinearProgressIndicator(
                            progress = { (metric.value / metric.target).toFloat().coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .padding(vertical = 4.dp),
                            color = when (metric.status) {
                                MetricStatus.GOOD -> Color(0xFF4CAF50)
                                MetricStatus.WARNING -> Color(0xFFFFC107)
                                MetricStatus.CRITICAL -> Color(0xFFF44336)
                            }
                        )
                    }
                }
            }
        }

        Text(stringResource(R.string.analytics_recommendations_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        if (report.recommendations.isEmpty()) {
            Text(
                text = stringResource(R.string.analytics_no_recommendations),
                color = MaterialTheme.colorScheme.outline
            )
        } else {
            report.recommendations.forEach { recommendation ->
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Text(
                        text = recommendation,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun RecommendationReportSection(report: RecommendationReportModel?) {
    if (report == null) return

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            stringResource(R.string.analytics_reco_report_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        RecommendationInsightCard(report)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RecommendationMetricCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.analytics_reco_impressions),
                value = report.totalImpressions.toString()
            )
            RecommendationMetricCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.analytics_reco_open_rate),
                value = formatPercent(report.openRate)
            )
            RecommendationMetricCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.analytics_reco_log_rate),
                value = formatPercent(report.logRate)
            )
        }

        RecommendationSliceCard(
            title = stringResource(R.string.analytics_reco_by_context),
            rows = report.byContext.map {
                RecommendationSliceRow(
                    label = it.key,
                    impressions = it.impressions,
                    openRate = it.openRate,
                    logRate = it.logRate
                )
            }
        )

        RecommendationSliceCard(
            title = stringResource(R.string.analytics_reco_by_variant),
            rows = report.byVariant.map {
                RecommendationSliceRow(
                    label = it.key,
                    impressions = it.impressions,
                    openRate = it.openRate,
                    logRate = it.logRate
                )
            }
        )

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    stringResource(R.string.analytics_reco_top_recipes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (report.topRecipes.isEmpty()) {
                    Text(
                        text = stringResource(R.string.analytics_reco_no_data),
                        color = MaterialTheme.colorScheme.outline
                    )
                } else {
                    report.topRecipes.take(5).forEach { recipe ->
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(recipe.recipeName, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = stringResource(
                                    R.string.analytics_reco_recipe_meta,
                                    recipe.impressions,
                                    formatPercent(recipe.openRate),
                                    formatPercent(recipe.logRate)
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MealPlanReportSection(
    report: MealPlanReportModel?,
    learnedInsights: List<String>,
    preferenceProfile: AnalyticsPreferenceProfile,
    behaviorSignals: List<AnalyticsBehaviorSignal>
) {
    if (report == null) return

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            stringResource(R.string.analytics_meal_plan_report_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        MealPlanInsightCard(report)

        PlannerInputSection(preferenceProfile)

        BehaviorSignalsSection(behaviorSignals)

        MealTypeDecisionSignalsSection(
            signals = report.decisionSignals,
            preferences = report.mealTypePreferences
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RecommendationMetricCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.analytics_meal_plan_days),
                value = report.generatedDays.toString()
            )
            RecommendationMetricCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.analytics_meal_plan_avg_meals),
                value = String.format(Locale.getDefault(), "%.1f", report.averageMealsPerDay)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RecommendationMetricCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.analytics_plan_logged),
                value = report.loggedCount.toString()
            )
            RecommendationMetricCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.analytics_plan_replaced),
                value = report.replacedCount.toString()
            )
            RecommendationMetricCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.analytics_plan_pinned),
                value = report.pinnedCount.toString()
            )
        }

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.analytics_plan_actions_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.analytics_plan_actions_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
                SummaryRow(
                    label = stringResource(R.string.analytics_plan_logged),
                    value = report.loggedCount.toString()
                )
                SummaryRow(
                    label = stringResource(R.string.analytics_plan_replaced),
                    value = report.replacedCount.toString()
                )
                SummaryRow(
                    label = stringResource(R.string.analytics_plan_removed),
                    value = report.removedCount.toString()
                )
                SummaryRow(
                    label = stringResource(R.string.analytics_plan_disliked),
                    value = report.dislikedCount.toString()
                )
                SummaryRow(
                    label = stringResource(R.string.analytics_plan_pinned),
                    value = report.pinnedCount.toString()
                )
                SummaryRow(
                    label = stringResource(R.string.analytics_plan_manual_added),
                    value = report.manualAddedCount.toString()
                )
            }
        }

        MealPlanSliceCard(
            title = stringResource(R.string.analytics_meal_plan_replacement_reasons),
            rows = report.replacementReasons
        )

        MealPlanSliceCard(
            title = stringResource(R.string.analytics_meal_plan_cuisines),
            rows = report.cuisines
        )

        MealPlanSliceCard(
            title = stringResource(R.string.analytics_meal_plan_repeated_recipes),
            rows = report.repeatedRecipes
        )

        LearnedInsightsSection(learnedInsights)
    }
}

@Composable
private fun MealTypeDecisionSignalsSection(
    signals: List<MealPlanDecisionSignalModel>,
    preferences: List<com.abdulin.nutritionapp.domain.model.MealPlanPreferenceProfileModel>
) {
    if (signals.isEmpty() && preferences.isEmpty()) return

    val groupedSignals = signals
        .groupBy { it.mealType.ifBlank { "UNKNOWN" } }
        .toMutableMap()

    preferences.forEach { preference ->
        groupedSignals.putIfAbsent(preference.mealType.ifBlank { "UNKNOWN" }, emptyList())
    }

    val grouped = groupedSignals
        .toSortedMap()

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.analytics_slot_signals_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.analytics_slot_signals_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
            grouped.forEach { (mealTypeKey, mealSignals) ->
                val preference = preferences.firstOrNull { it.mealType.equals(mealTypeKey, ignoreCase = true) }
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = mealTypeTitle(mealTypeKey),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        val chips = slotSignalChips(mealSignals, preference)
                        if (chips.isNotEmpty()) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                chips.forEach { chip ->
                                    SlotSignalChip(chip)
                                }
                            }
                        }
                        if (mealSignals.isNotEmpty()) {
                            mealSignals
                                .sortedByDescending { it.confidence }
                                .take(3)
                                .forEach { signal ->
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            text = signal.signalLabel.ifBlank {
                                                stringResource(R.string.analytics_slot_signals_default_label)
                                            },
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = signal.explanation.takeIf { it.isNotBlank() }
                                                ?: stringResource(
                                                    R.string.analytics_slot_signals_meta,
                                                    signal.evidenceCount,
                                                    formatPercent(signal.confidence)
                                                ),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                        } else if (preference != null) {
                            val summary = buildMealTypePreferenceSummary(preference)
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = stringResource(R.string.analytics_slot_signals_default_label),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = summary,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private enum class SlotSignalChipTone {
    SPEED,
    PROTEIN,
    CALORIES,
    HOME,
    LIGHT
}

private data class SlotSignalChipModel(
    val label: String,
    val tone: SlotSignalChipTone
)

@Composable
private fun SlotSignalChip(chip: SlotSignalChipModel) {
    val (containerColor, contentColor) = when (chip.tone) {
        SlotSignalChipTone.SPEED ->
            MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        SlotSignalChipTone.PROTEIN ->
            MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        SlotSignalChipTone.CALORIES ->
            MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        SlotSignalChipTone.HOME ->
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.75f) to MaterialTheme.colorScheme.onSecondaryContainer
        SlotSignalChipTone.LIGHT ->
            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.8f) to MaterialTheme.colorScheme.onTertiaryContainer
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = containerColor
    ) {
        Text(
            text = chip.label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun PlannerInputSection(profile: AnalyticsPreferenceProfile) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.analytics_inputs_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.analytics_inputs_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
            SummaryRow(
                label = stringResource(R.string.analytics_inputs_favorite_products),
                value = profile.favoriteProductsCount.toString()
            )
            SummaryRow(
                label = stringResource(R.string.analytics_inputs_allergy_products),
                value = profile.allergyProductsCount.toString()
            )
            if (profile.allergyProducts.isNotEmpty()) {
                Text(
                    text = profile.allergyProducts.joinToString(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            SummaryRow(
                label = stringResource(R.string.analytics_inputs_excluded_products),
                value = profile.excludedProductsCount.toString()
            )
            if (profile.excludedProducts.isNotEmpty()) {
                Text(
                    text = profile.excludedProducts.joinToString(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            SummaryRow(
                label = stringResource(R.string.analytics_inputs_favorite_cuisines),
                value = profile.favoriteCuisines.size.toString()
            )
            if (profile.favoriteCuisines.isNotEmpty()) {
                Text(
                    text = profile.favoriteCuisines.joinToString(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            SummaryRow(
                label = stringResource(R.string.analytics_inputs_disliked_cuisines),
                value = profile.dislikedCuisines.size.toString()
            )
            if (profile.dislikedCuisines.isNotEmpty()) {
                Text(
                    text = profile.dislikedCuisines.joinToString(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun BehaviorSignalsSection(signals: List<AnalyticsBehaviorSignal>) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.analytics_signals_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.analytics_signals_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
            if (signals.isEmpty()) {
                Text(
                    text = stringResource(R.string.analytics_signals_empty),
                    color = MaterialTheme.colorScheme.outline
                )
            } else {
                signals.forEach { signal ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = signal.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = signal.subtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalyticsOverviewSection(state: AnalyticsUiState) {
    if (state.overviewCards.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.analytics_overview_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(R.string.analytics_overview_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
        state.overviewCards.chunked(2).forEach { rowCards ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowCards.forEach { card ->
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = card.title,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                text = card.value,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = card.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
                if (rowCards.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun LearnedInsightsSection(insights: List<String>) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.analytics_learned_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.analytics_learned_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
            if (insights.isEmpty()) {
                Text(
                    text = stringResource(R.string.analytics_learned_empty),
                    color = MaterialTheme.colorScheme.outline
                )
            } else {
                insights.forEach { insight ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
                        )
                    ) {
                        Text(
                            text = insight,
                            modifier = Modifier.padding(14.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MealPlanInsightCard(report: MealPlanReportModel) {
    val insightTitle = report.summaryInsights.firstOrNull()?.title
        ?.takeIf { it.isNotBlank() }
        ?: stringResource(R.string.analytics_meal_plan_insight_title)
    val insightText = report.summaryInsights.firstOrNull()?.description
        ?.takeIf { it.isNotBlank() }
        ?: when {
            report.generatedDays < 3 ->
                stringResource(R.string.analytics_meal_plan_insight_not_enough_data)
            report.replacedCount + report.removedCount + report.dislikedCount > report.loggedCount ->
                stringResource(R.string.analytics_meal_plan_insight_adjustments)
            report.repeatedRecipes.isNotEmpty() ->
                stringResource(R.string.analytics_meal_plan_insight_repeats)
            report.replacementReasons.isNotEmpty() ->
                stringResource(R.string.analytics_meal_plan_insight_learning)
            else ->
                stringResource(R.string.analytics_meal_plan_insight_stable)
        }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = insightTitle,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(text = insightText, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun MealPlanSliceCard(
    title: String,
    rows: List<com.abdulin.nutritionapp.domain.model.MealPlanReportSliceModel>
) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (rows.isEmpty()) {
                Text(
                    text = stringResource(R.string.analytics_reco_no_data),
                    color = MaterialTheme.colorScheme.outline
                )
            } else {
                rows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = row.label.ifBlank { row.key },
                            modifier = Modifier.weight(1f),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = stringResource(
                                R.string.analytics_meal_plan_slice_meta,
                                row.count,
                                formatPercent(row.share)
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecommendationInsightCard(report: RecommendationReportModel) {
    val insightText = when {
        report.totalImpressions < 10 ->
            stringResource(R.string.analytics_reco_insight_not_enough_data)
        report.logRate >= 0.18 ->
            stringResource(R.string.analytics_reco_insight_strong)
        report.openRate >= 0.35 ->
            stringResource(R.string.analytics_reco_insight_promising)
        else ->
            stringResource(R.string.analytics_reco_insight_needs_tuning)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = stringResource(R.string.analytics_reco_insight_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = insightText,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun mealTypeTitle(mealTypeKey: String): String {
    return runCatching { MealType.valueOf(mealTypeKey.uppercase(Locale.ROOT)) }
        .map { stringResource(it.labelRes()) }
        .getOrElse { mealTypeKey.lowercase().replaceFirstChar { char -> char.uppercase() } }
}

@Composable
private fun slotSignalChips(
    signals: List<MealPlanDecisionSignalModel>,
    preference: com.abdulin.nutritionapp.domain.model.MealPlanPreferenceProfileModel?
): List<SlotSignalChipModel> {
    val chips = linkedMapOf<String, SlotSignalChipModel>()

    fun addChip(key: String, label: String, tone: SlotSignalChipTone) {
        chips.putIfAbsent(key, SlotSignalChipModel(label, tone))
    }

    signals.forEach { signal ->
        when {
            signal.signalKey.contains("cook", ignoreCase = true)
                    || signal.signalKey.contains("fast", ignoreCase = true)
                    || signal.signalLabel.contains("fast", ignoreCase = true) ->
                addChip("speed", stringResource(R.string.analytics_slot_chip_speed), SlotSignalChipTone.SPEED)
            signal.signalKey.contains("protein", ignoreCase = true)
                    || signal.signalLabel.contains("protein", ignoreCase = true) ->
                addChip("protein", stringResource(R.string.analytics_slot_chip_protein), SlotSignalChipTone.PROTEIN)
            signal.signalKey.contains("calorie", ignoreCase = true)
                    || signal.signalLabel.contains("calorie", ignoreCase = true) ->
                addChip("calories", stringResource(R.string.analytics_slot_chip_calories), SlotSignalChipTone.CALORIES)
            signal.signalKey.contains("home", ignoreCase = true)
                    || signal.signalLabel.contains("home", ignoreCase = true) ->
                addChip("home", stringResource(R.string.analytics_slot_chip_home), SlotSignalChipTone.HOME)
            signal.signalKey.contains("light", ignoreCase = true)
                    || signal.signalLabel.contains("light", ignoreCase = true) ->
                addChip("light", stringResource(R.string.analytics_slot_chip_light), SlotSignalChipTone.LIGHT)
        }
    }

    preference?.let {
        when {
            it.preferredCookTimeMin != null -> addChip(
                "speed",
                stringResource(R.string.analytics_slot_chip_speed),
                SlotSignalChipTone.SPEED
            )
            it.preferredProtein != null -> addChip(
                "protein",
                stringResource(R.string.analytics_slot_chip_protein),
                SlotSignalChipTone.PROTEIN
            )
            it.preferredCalories != null -> addChip(
                "calories",
                stringResource(R.string.analytics_slot_chip_calories),
                SlotSignalChipTone.CALORIES
            )
        }
        when (it.dominantReason.lowercase(Locale.ROOT)) {
            "faster" -> addChip("speed", stringResource(R.string.analytics_slot_chip_speed), SlotSignalChipTone.SPEED)
            "higher_protein" -> addChip("protein", stringResource(R.string.analytics_slot_chip_protein), SlotSignalChipTone.PROTEIN)
            "lower_calories" -> addChip("calories", stringResource(R.string.analytics_slot_chip_calories), SlotSignalChipTone.CALORIES)
            "from_home" -> addChip("home", stringResource(R.string.analytics_slot_chip_home), SlotSignalChipTone.HOME)
            "lighter" -> addChip("light", stringResource(R.string.analytics_slot_chip_light), SlotSignalChipTone.LIGHT)
        }
    }

    return chips.values.toList()
}

@Composable
private fun buildMealTypePreferenceSummary(
    preference: com.abdulin.nutritionapp.domain.model.MealPlanPreferenceProfileModel
): String {
    val mealLabel = preference.mealType.lowercase().replaceFirstChar { it.uppercase() }
    val confidence = formatPercent(preference.confidence)
    return when {
        preference.preferredCookTimeMin != null && preference.preferredCookTimeMin > 0 ->
            stringResource(
                R.string.analytics_signal_meal_speed_subtitle,
                mealLabel,
                preference.preferredCookTimeMin.toInt(),
                confidence
            )
        preference.preferredProtein != null && preference.preferredProtein > 0 ->
            stringResource(
                R.string.analytics_signal_meal_protein_subtitle,
                mealLabel,
                preference.preferredProtein.toInt(),
                confidence
            )
        preference.preferredCalories != null && preference.preferredCalories > 0 ->
            stringResource(
                R.string.analytics_signal_meal_calories_subtitle,
                mealLabel,
                preference.preferredCalories.toInt(),
                confidence
            )
        preference.dominantReason.isNotBlank() ->
            stringResource(
                R.string.analytics_signal_meal_reason_subtitle,
                mealLabel,
                preference.dominantReason,
                confidence
            )
        else -> stringResource(R.string.analytics_reco_no_data)
    }
}

@Composable
private fun RecommendationMetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String
) {
    Card(modifier = modifier.widthIn(min = 0.dp), shape = RoundedCornerShape(16.dp)) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

private data class RecommendationSliceRow(
    val label: String,
    val impressions: Int,
    val openRate: Double,
    val logRate: Double
)

@Composable
private fun RecommendationSliceCard(
    title: String,
    rows: List<RecommendationSliceRow>
) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (rows.isEmpty()) {
                Text(
                    text = stringResource(R.string.analytics_reco_no_data),
                    color = MaterialTheme.colorScheme.outline
                )
            } else {
                rows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = row.label,
                            modifier = Modifier.weight(1f),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = stringResource(
                                R.string.analytics_reco_slice_meta,
                                row.impressions,
                                formatPercent(row.openRate),
                                formatPercent(row.logRate)
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
    }
}

private fun formatPercent(value: Double): String = "${(value * 100).toInt()}%"

@Composable
fun AiInsightSection(insight: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary
            ) {
                Text(
                    stringResource(R.string.ai_badge),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            Spacer(modifier = Modifier.weight(0.08f))
            Column {
                Text(
                    stringResource(R.string.analytics_ai_insight),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(insight, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun SimpleLineChart(data: List<Float>, modifier: Modifier = Modifier) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier) {
        if (data.size < 2) return@Canvas

        val maxVal = data.maxOrNull()?.takeIf { it > 0 } ?: 1f
        val minVal = data.minOrNull() ?: 0f
        val range = (maxVal - minVal).coerceAtLeast(1f)

        val width = size.width
        val height = size.height
        val stepX = width / (data.size - 1)

        val path = Path()
        data.forEachIndexed { index, value ->
            val x = index * stepX
            val y = height - ((value - minVal) / range * height * 0.8f + height * 0.1f)
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        drawPath(
            path = path,
            color = primaryColor,
            style = Stroke(width = 3.dp.toPx())
        )

        data.forEachIndexed { index, value ->
            val x = index * stepX
            val y = height - ((value - minVal) / range * height * 0.8f + height * 0.1f)
            drawCircle(
                color = primaryColor,
                radius = 4.dp.toPx(),
                center = Offset(x, y)
            )
        }
    }
}

@Composable
fun AnalyticsSummaryCard(state: AnalyticsUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.analytics_summary), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            when (state.selectedTab) {
                AnalyticsTab.CALORIES -> {
                    val avg = if (state.caloriesStats.isNotEmpty()) state.caloriesStats.map { it.second }.average().toInt() else 0
                    SummaryRow(stringResource(R.string.analytics_summary_average), "$avg ${stringResource(R.string.unit_kcal)}")
                }

                AnalyticsTab.WATER -> {
                    val avg = if (state.waterStats.isNotEmpty()) state.waterStats.map { it.second }.average().toInt() else 0
                    SummaryRow(stringResource(R.string.analytics_summary_average), "$avg ${stringResource(R.string.unit_ml)}")
                }

                AnalyticsTab.WEIGHT -> {
                    val last = state.weightStats.lastOrNull()?.second ?: 0.0
                    SummaryRow(stringResource(R.string.analytics_summary_current_weight), "$last ${stringResource(R.string.unit_kg)}")
                }

                else -> Unit
            }

            val activeDays = state.caloriesStats.count { it.second > 0.0 }
            SummaryRow(stringResource(R.string.analytics_summary_weekly_streak), "$activeDays / 7")
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.outline)
        Text(value, fontWeight = FontWeight.Bold)
    }
}
