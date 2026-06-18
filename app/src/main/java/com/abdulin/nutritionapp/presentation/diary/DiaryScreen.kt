package com.abdulin.nutritionapp.presentation.diary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.LifecycleEventObserver
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.abdulin.nutritionapp.R
import com.abdulin.nutritionapp.domain.model.DiarySummary
import com.abdulin.nutritionapp.domain.model.FoodDiaryEntry
import com.abdulin.nutritionapp.domain.model.MealType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryScreen(
    viewModel: DiaryViewModel = hiltViewModel(),
    onNavigateToAddFood: () -> Unit,
    onBack: () -> Unit,
    showBackButton: Boolean = true,
    bottomBarPadding: androidx.compose.ui.unit.Dp = 0.dp
) {
    val state by viewModel.state.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val snackbarHostState = remember { SnackbarHostState() }
    val deleteSuccessMessage = stringResource(R.string.diary_delete_success)
    val deleteErrorMessage = stringResource(R.string.diary_delete_error)

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadDiary()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(state.userMessage) {
        val message = when (state.userMessage) {
            "delete_success" -> deleteSuccessMessage
            "delete_error" -> deleteErrorMessage
            null -> null
            else -> state.userMessage
        }
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.diary_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    if (showBackButton) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.onboarding_back))
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddFood) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.diary_add_food))
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        // Адаптивная верстка: центрируем контент на больших экранах
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(bottom = bottomBarPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            val maxWidth = maxWidth
            Column(
                modifier = Modifier.fillMaxWidth(if (maxWidth > 600.dp) 0.7f else 1f)
            ) {
                DateSelector(
                    date = state.selectedDate,
                    onPrevious = { viewModel.changeDate(-1) },
                    onNext = { viewModel.changeDate(1) }
                )

                if (state.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (state.error != null) {
                    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                        EmptyDiaryState(
                            title = state.error!!,
                            subtitle = stringResource(R.string.error_unknown),
                            actionLabel = stringResource(R.string.diary_retry),
                            onAction = viewModel::loadDiary
                        )
                    }
                } else {
                    if (state.entries.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                            EmptyDiaryState(
                                title = stringResource(R.string.diary_empty_title),
                                subtitle = stringResource(R.string.diary_empty_subtitle),
                                actionLabel = stringResource(R.string.diary_add_food),
                                onAction = onNavigateToAddFood
                            )
                        }
                    } else {
                        val mealsByType = state.entries.groupBy { it.mealType }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                state.summary?.let { DiarySummaryCard(it) }
                            }

                            MealType.entries.forEach { type ->
                                val meals = mealsByType[type].orEmpty()
                                if (meals.isNotEmpty()) {
                                    item {
                                        MealGroupHeader(type)
                                    }
                                    items(meals) { meal ->
                                        DiaryEntryItem(
                                            entry = meal,
                                            onDelete = { viewModel.deleteEntry(meal.id) }
                                        )
                                    }
                                }
                            }

                            item { Spacer(modifier = Modifier.height(80.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DiarySummaryCard(summary: DiarySummary) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.diary_summary_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MacroBadge("${summary.totalCalories.toInt()} ${stringResource(R.string.unit_kcal)}", MaterialTheme.colorScheme.primary)
                MacroBadge("${stringResource(R.string.macros_protein)} ${summary.totalProtein.toInt()} ${stringResource(R.string.unit_g)}", Color(0xFF4CAF50))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MacroBadge("${stringResource(R.string.macros_fat)} ${summary.totalFat.toInt()} ${stringResource(R.string.unit_g)}", Color(0xFFFFC107))
                MacroBadge("${stringResource(R.string.macros_carbs)} ${summary.totalCarbs.toInt()} ${stringResource(R.string.unit_g)}", Color(0xFF2196F3))
            }
        }
    }
}

@Composable
private fun EmptyDiaryState(
    title: String,
    subtitle: String,
    actionLabel: String,
    onAction: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
        Button(onClick = onAction) {
            Text(actionLabel)
        }
    }
}

@Composable
fun DateSelector(
    date: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrevious) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            }
            Text(
                text = date,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onNext) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }
        }
    }
}

@Composable
fun MealGroupHeader(type: MealType) {
    val title = when(type) {
        MealType.BREAKFAST -> stringResource(R.string.diary_meal_breakfast)
        MealType.LUNCH -> stringResource(R.string.diary_meal_lunch)
        MealType.DINNER -> stringResource(R.string.diary_meal_dinner)
        else -> stringResource(R.string.diary_meal_snack)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val color = when(type) {
            MealType.BREAKFAST -> Color(0xFFFFA726)
            MealType.LUNCH -> Color(0xFF66BB6A)
            MealType.DINNER -> Color(0xFF5C6BC0)
            MealType.SNACK -> Color(0xFFAB47BC)
        }
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun DiaryEntryItem(
    entry: FoodDiaryEntry,
    onDelete: () -> Unit
) {
    val consumedTime = entry.consumedAt
        .substringAfter('T', "")
        .take(5)
        .ifBlank { stringResource(R.string.diary_time_unknown) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.productName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${entry.weightGrams.toInt()} ${stringResource(R.string.unit_g)} • ${entry.calories.toInt()} ${stringResource(R.string.unit_kcal)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = consumedTime,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Row(modifier = Modifier.padding(top = 8.dp)) {
                    MacroBadge(stringResource(R.string.home_protein).take(1) + ": ${entry.protein.toInt()}", Color(0xFF4CAF50))
                    Spacer(modifier = Modifier.width(4.dp))
                    MacroBadge(stringResource(R.string.home_fat).take(1) + ": ${entry.fat.toInt()}", Color(0xFFFFC107))
                    Spacer(modifier = Modifier.width(4.dp))
                    MacroBadge(stringResource(R.string.home_carbs).take(1) + ": ${entry.carbs.toInt()}", Color(0xFF2196F3))
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun MacroBadge(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}
