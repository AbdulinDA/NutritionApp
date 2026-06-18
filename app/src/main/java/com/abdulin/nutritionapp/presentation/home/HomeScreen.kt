package com.abdulin.nutritionapp.presentation.home

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.PermissionController
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.abdulin.nutritionapp.R
import com.abdulin.nutritionapp.core.utils.labelRes
import com.abdulin.nutritionapp.core.utils.Resource
import com.abdulin.nutritionapp.domain.model.FoodDiaryEntry
import com.abdulin.nutritionapp.domain.model.HomeModel
import com.abdulin.nutritionapp.domain.model.MealType
import com.abdulin.nutritionapp.domain.usecase.AdviceType
import com.abdulin.nutritionapp.domain.usecase.AiAdvice
import com.abdulin.nutritionapp.presentation.recipe.RecipeImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToDiary: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToFridge: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToRecipe: (Long, String?, String?, Long?, Double?) -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToFasting: () -> Unit,
    bottomBarPadding: androidx.compose.ui.unit.Dp = 0.dp
) {
    val uiState by viewModel.uiState.collectAsState()
    var showWaterDialog by remember { mutableStateOf(false) }
    var showWeightDialog by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current

    val permissionsLauncher = rememberLauncherForActivityResult(
        PermissionController.createRequestPermissionResultContract()
    ) { granted ->
        if (granted.isNotEmpty()) {
            viewModel.loadHome()
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadHome(showLoading = false)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.nav_home),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (uiState.homeData is Resource.Success) {
                            val streak = uiState.homeData.data?.streak ?: 0
                            if (streak > 0) {
                                Spacer(modifier = Modifier.width(10.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.tertiaryContainer,
                                    shape = RoundedCornerShape(999.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.LocalFireDepartment,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = streak.toString(),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::loadHome, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.home_refresh))
                    }
                    IconButton(onClick = onNavigateToAnalytics, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = stringResource(R.string.profile_analytics))
                    }
                    IconButton(onClick = onNavigateToProfile, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Default.Person, contentDescription = stringResource(R.string.nav_profile))
                    }
                }
            )
        }
    ) { paddingValues ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(bottom = bottomBarPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            val contentModifier = Modifier.fillMaxWidth(if (maxWidth > 600.dp) 0.72f else 1f)

            when (val homeData = uiState.homeData) {
                is Resource.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is Resource.Error -> {
                    HomeStateCard(
                        modifier = contentModifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        title = stringResource(R.string.home_error_title),
                        subtitle = homeData.message ?: stringResource(R.string.home_error_subtitle),
                        actionLabel = stringResource(R.string.retry),
                        onAction = viewModel::loadHome
                    )
                }
                is Resource.Success -> {
                    val data = homeData.data!!
                    LazyColumn(
                        modifier = contentModifier.fillMaxHeight(),
                        contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            NutritionSummaryCard(data)
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                QuickLinkCard(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Default.Kitchen,
                                    title = stringResource(R.string.fridge_title),
                                    subtitle = stringResource(R.string.fridge_home_subtitle),
                                    onClick = onNavigateToFridge
                                )
                                QuickLinkCard(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Default.Star,
                                    title = stringResource(R.string.favorites_title),
                                    subtitle = stringResource(R.string.favorites_home_subtitle),
                                    onClick = onNavigateToFavorites
                                )
                            }
                        }

                        item {
                            FastingStatusCard(
                                fastingState = uiState.fastingState,
                                onOpen = onNavigateToFasting,
                                onToggle = viewModel::toggleFasting
                            )
                        }

                        item {
                            uiState.advice?.let { advice ->
                                AiAssistantCard(advice, onNavigateToAnalytics)
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                WaterCard(
                                    current = data.water,
                                    target = data.targetWater,
                                    onAddClick = { viewModel.logWater(250) },
                                    onMoreClick = { showWaterDialog = true }
                                )
                                WeightCard(
                                    weight = data.weight,
                                    onClick = { showWeightDialog = true }
                                )
                            }
                        }

                        item {
                            SectionTitle(stringResource(R.string.home_meals))
                        }

                        if (data.todayMeals.isEmpty()) {
                            item {
                                HomeStateCard(
                                    title = stringResource(R.string.home_meals_empty_title),
                                    subtitle = stringResource(R.string.home_meals_empty_subtitle),
                                    actionLabel = stringResource(R.string.home_log_meal),
                                    onAction = onNavigateToDiary
                                )
                            }
                        } else {
                            val mealsByType = data.todayMeals.groupBy { it.mealType }
                            MealType.entries.forEach { type ->
                                val meals = mealsByType[type] ?: emptyList()
                                item {
                                    MealSection(type, meals, onNavigateToDiary)
                                }
                            }
                        }

                        item {
                            SectionTitle(stringResource(R.string.home_recommendations))
                        }

                        if (data.recommendations.isEmpty()) {
                            item {
                                HomeStateCard(
                                    title = stringResource(R.string.home_recommendations),
                                    subtitle = stringResource(R.string.home_recommendations_empty),
                                    actionLabel = stringResource(R.string.profile_analytics),
                                    onAction = onNavigateToAnalytics
                                )
                            }
                        } else {
                            items(data.recommendations.size) { index ->
                                val recipe = data.recommendations[index]
                                RecipeCard(
                                    recipe,
                                    onClick = {
                                        onNavigateToRecipe(
                                            recipe.id,
                                            "home_recommendation",
                                            recipe.recommendationReason ?: data.aiMessage,
                                            recipe.recommendationImpressionId,
                                            null
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showWaterDialog) {
        QuickAmountDialog(
            title = stringResource(R.string.home_add_water),
            label = stringResource(R.string.home_water),
            unitSuffix = stringResource(R.string.unit_ml),
            initialValue = "250",
            onDismiss = { showWaterDialog = false },
            onConfirm = { value ->
                viewModel.logWater(value.toInt())
                showWaterDialog = false
            }
        )
    }

    if (showWeightDialog) {
        QuickAmountDialog(
            title = stringResource(R.string.home_update_weight),
            label = stringResource(R.string.home_weight),
            unitSuffix = stringResource(R.string.unit_kg),
            initialValue = "",
            onDismiss = { showWeightDialog = false },
            onConfirm = { value ->
                viewModel.updateWeight(value.toDouble())
                showWeightDialog = false
            }
        )
    }
}

@Composable
private fun QuickLinkCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(120.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
    )
}

@Composable
private fun HomeStateCard(
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
            FilledTonalButton(onClick = onAction) {
                Text(actionLabel)
            }
        }
    }
}

@Composable
fun RecipeCard(recipe: com.abdulin.nutritionapp.domain.model.RecipeShortModel, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            RecipeImage(
                imageUrl = recipe.imageUrl,
                contentDescription = null,
                recipeTitle = recipe.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${recipe.totalCalories.toInt()} ${stringResource(R.string.unit_kcal)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
fun NutritionSummaryCard(data: HomeModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(stringResource(R.string.home_calories), style = MaterialTheme.typography.labelLarge)
                    Text(
                        "${data.calories.toInt()} / ${data.targetCalories.toInt()} ${stringResource(R.string.unit_kcal)}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                CircularProgressIndicator(
                    progress = { (data.calories / data.targetCalories).toFloat().coerceIn(0f, 1f) },
                    modifier = Modifier.size(48.dp),
                    strokeWidth = 6.dp,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MacroIndicator(stringResource(R.string.home_protein), data.protein, data.targetProtein, MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                MacroIndicator(stringResource(R.string.home_fat), data.fat, data.targetFat, MaterialTheme.colorScheme.tertiary, Modifier.weight(1f))
                MacroIndicator(stringResource(R.string.home_carbs), data.carbs, data.targetCarbs, MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun MacroIndicator(label: String, value: Double, target: Double, color: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelSmall)
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { (value / target).toFloat().coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )
        Text(
            "${value.toInt()} / ${target.toInt()}${stringResource(R.string.unit_g)}",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ActivityQuickStats(steps: Long, calories: Double, sleepMinutes: Long) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(stringResource(R.string.home_steps), style = MaterialTheme.typography.labelSmall)
                Text(steps.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
        Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(stringResource(R.string.home_activity), style = MaterialTheme.typography.labelSmall)
                Text("${calories.toInt()} ${stringResource(R.string.unit_kcal)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
        Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(stringResource(R.string.health_connect_sleep), style = MaterialTheme.typography.labelSmall)
                Text(formatSleepSummary(sleepMinutes), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun FastingStatusCard(
    fastingState: com.abdulin.nutritionapp.data.fasting.FastingState,
    onOpen: () -> Unit,
    onToggle: () -> Unit
) {
    val elapsedMillis = if (fastingState.isActive) {
        (System.currentTimeMillis() - fastingState.startMillis).coerceAtLeast(0L)
    } else {
        0L
    }
    val remainingMillis = (fastingState.targetMillis - elapsedMillis).coerceAtLeast(0L)
    val progress = if (fastingState.isActive) {
        (elapsedMillis.toFloat() / fastingState.targetMillis).coerceIn(0f, 1f)
    } else {
        0f
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.fasting_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    if (fastingState.isActive) formatDurationHome(remainingMillis) else "${fastingState.targetHours}:${24 - fastingState.targetHours}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = if (fastingState.isActive) stringResource(R.string.fasting_remaining) else stringResource(R.string.fasting_ready),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
            fastingState.lastCompletedFast?.let { lastFast ->
                Text(
                    text = stringResource(
                        R.string.fasting_last_completed_summary,
                        formatDurationHome(lastFast.durationMillis),
                        lastFast.targetHours
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilledTonalButton(
                    onClick = onToggle,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(if (fastingState.isActive) R.string.fasting_stop else R.string.fasting_start))
                }
                TextButton(
                    onClick = onOpen,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.fasting_open_tracker))
                }
            }
        }
    }
}

private fun formatDurationHome(millis: Long): String {
    val totalMinutes = millis / 60_000L
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return "%02d:%02d".format(hours, minutes)
}

private fun formatSleepSummary(minutes: Long): String {
    if (minutes <= 0) return "--"
    val hours = minutes / 60
    val rest = minutes % 60
    return if (rest == 0L) "${hours}h" else "${hours}h ${rest}m"
}

@Composable
fun WaterCard(current: Int, target: Int, onAddClick: () -> Unit, onMoreClick: () -> Unit) {
    Card(
        modifier = Modifier
            .height(140.dp)
            .width(152.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.WaterDrop, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                IconButton(onClick = onAddClick, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
            Column {
                Text("${current} ${stringResource(R.string.unit_ml)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.home_water_goal, target), style = MaterialTheme.typography.bodySmall)
            }
            Text(
                stringResource(R.string.home_add_water),
                modifier = Modifier.clickable { onMoreClick() },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun WeightCard(weight: Double, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .height(140.dp)
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(Icons.Default.MonitorWeight, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
            Column {
                Text("${weight} ${stringResource(R.string.unit_kg)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.home_weight), style = MaterialTheme.typography.bodySmall)
            }
            Text(
                stringResource(R.string.home_weight_history),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
fun MealSection(type: MealType, meals: List<FoodDiaryEntry>, onAddClick: () -> Unit) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val title = when (type) {
                MealType.BREAKFAST -> stringResource(R.string.diary_meal_breakfast)
                MealType.LUNCH -> stringResource(R.string.diary_meal_lunch)
                MealType.DINNER -> stringResource(R.string.diary_meal_dinner)
                else -> stringResource(R.string.diary_meal_snack)
            }
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            IconButton(onClick = onAddClick, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
        }

        if (meals.isEmpty()) {
            Text(
                stringResource(R.string.home_empty_meal),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        } else {
            meals.forEach { meal ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RecipeImage(
                        imageUrl = meal.imageUrl,
                        contentDescription = meal.productName,
                        recipeTitle = meal.productName,
                        mealType = meal.mealType.name,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(14.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            meal.productName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            stringResource(meal.mealType.labelRes()),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    Text(
                        "${meal.calories.toInt()} ${stringResource(R.string.unit_kcal)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        HorizontalDivider(
            modifier = Modifier.padding(top = 8.dp),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

@Composable
fun HealthPermissionsBanner(onConnectClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.DirectionsRun, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.home_health_sync_title), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.home_health_sync_subtitle), style = MaterialTheme.typography.bodySmall)
            }
            TextButton(onClick = onConnectClick) {
                Text(stringResource(R.string.home_health_sync_action))
            }
        }
    }
}

@Composable
fun AiAssistantCard(advice: AiAdvice, onDetailClick: () -> Unit) {
    val backgroundColor = when (advice.type) {
        AdviceType.WARNING -> MaterialTheme.colorScheme.errorContainer
        AdviceType.SUCCESS -> MaterialTheme.colorScheme.tertiaryContainer
        AdviceType.CALORIES -> MaterialTheme.colorScheme.primaryContainer
        AdviceType.INFO -> MaterialTheme.colorScheme.secondaryContainer
    }

    val icon = when (advice.type) {
        AdviceType.WARNING -> Icons.Default.Warning
        AdviceType.SUCCESS -> Icons.Default.LocalFireDepartment
        AdviceType.CALORIES -> Icons.Default.LocalFireDepartment
        AdviceType.INFO -> Icons.Default.AutoAwesome
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(16.dp),
        onClick = onDetailClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
            ) {
                Icon(icon, contentDescription = null, modifier = Modifier.padding(8.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(advice.title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Text(advice.message, style = MaterialTheme.typography.bodyMedium)
                advice.actionText?.let {
                    Text(
                        "$it >",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickAmountDialog(
    title: String,
    label: String,
    unitSuffix: String,
    initialValue: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var value by remember { mutableStateOf(initialValue) }
    val parsed = value.toDoubleOrNull()
    val isValid = parsed != null && parsed > 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                label = { Text(label) },
                suffix = { Text(unitSuffix) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                isError = value.isNotBlank() && !isValid
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(value) }, enabled = isValid) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
