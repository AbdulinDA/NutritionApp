package com.abdulin.nutritionapp.presentation.recipe

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.abdulin.nutritionapp.R
import com.abdulin.nutritionapp.core.utils.labelRes
import com.abdulin.nutritionapp.domain.model.MealPlanDayModel
import com.abdulin.nutritionapp.domain.model.MealPlanPreferenceProfileModel
import com.abdulin.nutritionapp.domain.model.MealPlanMealExplanationModel
import com.abdulin.nutritionapp.domain.model.MealPlanMealModel
import com.abdulin.nutritionapp.domain.model.MealPlanReportModel
import com.abdulin.nutritionapp.domain.model.MealType
import com.abdulin.nutritionapp.domain.model.RecipeCompositionModel
import androidx.compose.foundation.layout.BoxWithConstraints
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealPlanScreen(
    viewModel: MealPlanViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onNavigateToRecipe: (Long, String?, String?, Long?, Double?) -> Unit,
    onNavigateToShoppingList: () -> Unit,
    onNavigateToGeneratePlan: () -> Unit,
    onAddMealToSlot: (String, String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var replacementMeal by remember { mutableStateOf<MealPlanMealModel?>(null) }
    val planDays = state.plan?.days.orEmpty()
    val totalMeals = planDays.sumOf { it.meals.size }
    val nutritionStatusCounts = planDays
        .flatMap { it.meals }
        .mapNotNull { meal ->
            meal.recipe.nutritionCalculationStatus
                ?.trim()
                ?.uppercase()
                ?.takeIf { it.isNotBlank() }
        }
        .groupingBy { it }
        .eachCount()

    val loggedMessage = stringResource(R.string.meal_plan_logged_success)
    val logFailedMessage = stringResource(R.string.meal_plan_logged_error)
    val removedMessage = stringResource(R.string.meal_plan_removed_success)
    val replacedMessage = stringResource(R.string.meal_plan_replaced_success)
    val dislikedMessage = stringResource(R.string.meal_plan_disliked_success)
    val pinToggledMessage = stringResource(R.string.meal_plan_pin_toggled_success)
    val actionFailedMessage = stringResource(R.string.meal_plan_action_failed)
    val replannedMessage = stringResource(R.string.meal_plan_replanned_success)
    val loggedReplanFailedMessage = stringResource(R.string.meal_plan_logged_replan_failed)
    val daySlotsFilledMessage = stringResource(R.string.meal_plan_day_slots_filled_success)
    val dayRebuiltMessage = stringResource(R.string.meal_plan_day_rebuilt_success)
    val upcomingDaysReplannedMessage = stringResource(R.string.meal_plan_upcoming_days_replanned_success)
    val dayRepeatedMessage = stringResource(R.string.meal_plan_day_repeated_success)
    val compositionFailedMessage = stringResource(R.string.meal_plan_composition_error)
    val explanationFailedMessage = stringResource(R.string.meal_plan_explanation_error)

    LaunchedEffect(state.userMessage) {
        val message = when (state.userMessage) {
            "meal_logged" -> loggedMessage
            "meal_logged_replan_failed" -> state.error ?: loggedReplanFailedMessage
            "meal_log_failed" -> state.error ?: logFailedMessage
            "meal_removed" -> removedMessage
            "meal_replaced" -> replacedMessage
            "meal_disliked" -> dislikedMessage
            "meal_pin_toggled" -> pinToggledMessage
            "day_replanned" -> replannedMessage
            "day_replan_failed" -> state.error ?: actionFailedMessage
            "day_slots_filled" -> daySlotsFilledMessage
            "day_slots_fill_failed" -> state.error ?: actionFailedMessage
            "day_rebuilt" -> dayRebuiltMessage
            "day_rebuild_failed" -> state.error ?: actionFailedMessage
            "upcoming_days_replanned" -> upcomingDaysReplannedMessage
            "upcoming_days_replan_failed" -> state.error ?: actionFailedMessage
            "day_repeated_to_next" -> dayRepeatedMessage
            "day_repeat_failed" -> state.error ?: actionFailedMessage
            "meal_composition_failed" -> state.compositionError ?: compositionFailedMessage
            "meal_explanation_failed" -> state.explanationError ?: explanationFailedMessage
            "meal_action_failed" -> state.error ?: actionFailedMessage
            else -> null
        }
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.meal_plan_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.onboarding_back))
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::refreshLatestPlan) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.meal_plan_refresh))
                    }
                }
            )
        }
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            val contentModifier = Modifier.fillMaxWidth(if (maxWidth > 600.dp) 0.76f else 1f)
            if (state.isLoading && state.plan == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.error != null && state.plan == null) {
                MealPlanStateCard(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    title = stringResource(R.string.meal_plan_error_title),
                    subtitle = state.error ?: stringResource(R.string.meal_plan_error_subtitle),
                    primaryAction = stringResource(R.string.meal_plan_refresh),
                    onPrimaryAction = viewModel::refreshLatestPlan,
                    secondaryAction = stringResource(R.string.meal_plan_generate_new),
                    onSecondaryAction = onNavigateToGeneratePlan
                )
            } else if (planDays.isEmpty()) {
                MealPlanStateCard(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    title = stringResource(R.string.meal_plan_empty_title),
                    subtitle = stringResource(R.string.meal_plan_empty_subtitle),
                    primaryAction = stringResource(R.string.meal_plan_generate_new),
                    onPrimaryAction = onNavigateToGeneratePlan
                )
            } else {
                LazyColumn(
                    modifier = contentModifier.fillMaxHeight(),
                    contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    item {
                        MealPlanSummaryCard(
                            daysCount = planDays.size,
                            mealsCount = totalMeals,
                            nutritionStatusCounts = nutritionStatusCounts,
                            onOpenShoppingList = onNavigateToShoppingList,
                            onGenerateNew = onNavigateToGeneratePlan,
                            onReplanRemaining = viewModel::replanRemainingDay,
                            onReplanUpcomingDays = viewModel::replanUpcomingDays,
                            isUpdatingUpcomingDays = state.isUpdatingUpcomingDays
                        )
                    }

                    items(
                        items = planDays,
                        key = { day -> day.date }
                    ) { day ->
                        MealPlanDaySection(
                            day = day,
                            onLogMeal = { meal ->
                                viewModel.logMealFromPlan(
                                    mealType = meal.mealType.name,
                                    recipe = meal.recipe,
                                    planRecipeId = meal.planRecipeId
                                )
                            },
                            loggingRecipeIds = state.loggingRecipeIds,
                            updatingMealIds = state.updatingMealIds,
                            onRemoveMeal = { meal -> viewModel.removeMealFromPlan(meal.planRecipeId) },
                            onReplaceMeal = { meal -> replacementMeal = meal },
                            onDislikeMeal = { meal -> viewModel.dislikeMealFromPlan(meal.planRecipeId) },
                            onTogglePin = { meal -> viewModel.toggleMealPin(meal.planRecipeId) },
                            onShowComposition = { meal -> viewModel.loadMealComposition(meal) },
                            onAddMealToSlot = { date, mealType -> onAddMealToSlot(date, mealType) },
                            onFillEmptySlots = { date -> viewModel.fillEmptySlotsForDay(date) },
                            onReplanDay = { date -> viewModel.replanDay(date) },
                            onRepeatToNextDay = { date -> viewModel.repeatDayToNext(date) },
                            isUpdatingDay = state.updatingDayDates.contains(day.date),
                            loadingCompositionMealIds = state.loadingCompositionMealIds,
                            onRecipeClick = onNavigateToRecipe
                        )
                    }
                }
            }
        }
    }

    replacementMeal?.let { meal ->
        ReplaceMealReasonDialog(
            mealTitle = meal.recipe.title,
            onDismiss = { replacementMeal = null },
            onReasonSelected = { reason ->
                replacementMeal = null
                viewModel.replaceMealInPlan(meal.planRecipeId, reason.code)
            }
        )
    }

    val selectedComposition = state.selectedMealComposition
    val selectedCompositionMeal = state.selectedMealForComposition
    if (selectedComposition != null && selectedCompositionMeal != null) {
        MealPlanCompositionSheet(
            meal = selectedCompositionMeal,
            composition = selectedComposition,
            explanation = state.selectedMealExplanation,
            mealPlanReport = state.mealPlanReport,
            onDismiss = viewModel::dismissMealComposition
        )
    }
}

@Composable
private fun MealPlanSummaryCard(
    daysCount: Int,
    mealsCount: Int,
    nutritionStatusCounts: Map<String, Int>,
    onOpenShoppingList: () -> Unit,
    onGenerateNew: () -> Unit,
    onReplanRemaining: () -> Unit,
    onReplanUpcomingDays: () -> Unit,
    isUpdatingUpcomingDays: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = stringResource(R.string.meal_plan_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.surface) {
                    Text(
                        text = stringResource(R.string.meal_plan_summary_days, daysCount),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.surface) {
                    Text(
                        text = stringResource(R.string.meal_plan_summary_meals, mealsCount),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Text(
                text = stringResource(R.string.meal_plan_summary_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            MealPlanNutritionStatusOverview(nutritionStatusCounts)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilledTonalButton(onClick = onOpenShoppingList, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(stringResource(R.string.meal_plan_open_shopping_list))
                }
                Button(onClick = onGenerateNew, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Autorenew, contentDescription = null)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(stringResource(R.string.meal_plan_generate_new))
                }
            }
            FilledTonalButton(onClick = onReplanRemaining, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.size(8.dp))
                Text(stringResource(R.string.meal_plan_replan_remaining))
            }
            FilledTonalButton(
                onClick = onReplanUpcomingDays,
                enabled = !isUpdatingUpcomingDays,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isUpdatingUpcomingDays) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Autorenew, contentDescription = null)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(stringResource(R.string.meal_plan_replan_upcoming_days))
                }
            }
        }
    }
}

@Composable
private fun MealPlanNutritionStatusOverview(nutritionStatusCounts: Map<String, Int>) {
    if (nutritionStatusCounts.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.meal_plan_nutrition_quality_title),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "COMPLETE" to stringResource(R.string.recipe_filter_quality_complete),
                "ESTIMATED" to stringResource(R.string.recipe_filter_quality_estimated),
                "REQUIRES_SIDE_DISH" to stringResource(R.string.recipe_filter_quality_side_dish),
                "INCOMPLETE" to stringResource(R.string.recipe_filter_quality_incomplete)
            ).forEach { (status, label) ->
                val count = nutritionStatusCounts[status] ?: return@forEach
                val (containerColor, contentColor) = when (status) {
                    "COMPLETE" -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
                    "ESTIMATED" -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
                    "REQUIRES_SIDE_DISH" -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
                    else -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = containerColor
                ) {
                    Text(
                        text = stringResource(R.string.meal_plan_nutrition_quality_chip, label, count),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        color = contentColor,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun MealPlanStateCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    primaryAction: String,
    onPrimaryAction: () -> Unit,
    secondaryAction: String? = null,
    onSecondaryAction: (() -> Unit)? = null
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
            FilledTonalButton(onClick = onPrimaryAction) {
                Text(primaryAction)
            }
            if (secondaryAction != null && onSecondaryAction != null) {
                Button(onClick = onSecondaryAction) {
                    Text(secondaryAction)
                }
            }
        }
    }
}

@Composable
fun MealPlanDaySection(
    day: MealPlanDayModel,
    onLogMeal: (MealPlanMealModel) -> Unit,
    loggingRecipeIds: Set<Long>,
    updatingMealIds: Set<Long>,
    onRemoveMeal: (MealPlanMealModel) -> Unit,
    onReplaceMeal: (MealPlanMealModel) -> Unit,
    onDislikeMeal: (MealPlanMealModel) -> Unit,
    onTogglePin: (MealPlanMealModel) -> Unit,
    onShowComposition: (MealPlanMealModel) -> Unit,
    onAddMealToSlot: (String, String) -> Unit,
    onFillEmptySlots: (String) -> Unit,
    onReplanDay: (String) -> Unit,
    onRepeatToNextDay: (String) -> Unit,
    isUpdatingDay: Boolean,
    loadingCompositionMealIds: Set<Long>,
    onRecipeClick: (Long, String?, String?, Long?, Double?) -> Unit
) {
    val occupiedMealTypes = day.meals.map { it.mealType }.toSet()
    val missingMealTypes = MealType.entries.filterNot(occupiedMealTypes::contains)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = day.date,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalButton(
                    onClick = { onReplanDay(day.date) },
                    enabled = !isUpdatingDay
                ) {
                    if (isUpdatingDay) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Autorenew, contentDescription = null)
                        Spacer(modifier = Modifier.size(6.dp))
                        Text(stringResource(R.string.meal_plan_replan_day_cta))
                    }
                }
                FilledTonalButton(
                    onClick = { onRepeatToNextDay(day.date) },
                    enabled = !isUpdatingDay
                ) {
                    Icon(Icons.Default.Autorenew, contentDescription = null)
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(stringResource(R.string.meal_plan_repeat_day_cta))
                }
                if (missingMealTypes.isNotEmpty()) {
                    FilledTonalButton(
                        onClick = { onFillEmptySlots(day.date) },
                        enabled = !isUpdatingDay
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.size(6.dp))
                        Text(stringResource(R.string.meal_plan_fill_day_cta))
                    }
                }
            }
        }
        if (day.meals.isEmpty()) {
            Text(
                text = stringResource(R.string.meal_plan_day_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            day.meals.forEach { meal ->
                MealPlanItem(
                    meal = meal,
                    isLogging = loggingRecipeIds.contains(meal.recipe.id),
                    isUpdating = updatingMealIds.contains(meal.planRecipeId),
                    onLogClick = { onLogMeal(meal) },
                    onRemoveClick = { onRemoveMeal(meal) },
                    onReplaceClick = { onReplaceMeal(meal) },
                    onDislikeClick = { onDislikeMeal(meal) },
                    onTogglePinClick = { onTogglePin(meal) },
                    onShowCompositionClick = { onShowComposition(meal) },
                    isLoadingComposition = loadingCompositionMealIds.contains(meal.planRecipeId),
                    onClick = {
                        onRecipeClick(
                            meal.recipe.id,
                            "meal_plan",
                            meal.selectionReason,
                            meal.recommendationImpressionId,
                            plannedServingsValue(meal)
                        )
                    }
                )
            }
            missingMealTypes.forEach { missingMealType ->
                    EmptyMealSlotCard(
                        mealType = missingMealType,
                        onAddClick = { onAddMealToSlot(day.date, missingMealType.name) }
                    )
            }
        }
    }
}

@Composable
fun MealPlanItem(
    meal: MealPlanMealModel,
    isLogging: Boolean,
    isUpdating: Boolean,
    onLogClick: () -> Unit,
    onRemoveClick: () -> Unit,
    onReplaceClick: () -> Unit,
    onDislikeClick: () -> Unit,
    onTogglePinClick: () -> Unit,
    onShowCompositionClick: () -> Unit,
    isLoadingComposition: Boolean,
    onClick: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    if (!isUpdating) onRemoveClick()
                    false
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    if (!isUpdating) onDislikeClick()
                    false
                }
                SwipeToDismissBoxValue.Settled -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            MealPlanSwipeBackground(
                dismissDirection = dismissState.dismissDirection,
                isUpdating = isUpdating
            )
        }
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            onClick = onClick
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RecipeImage(
                        imageUrl = meal.recipe.imageUrl,
                        contentDescription = null,
                        recipeTitle = meal.recipe.title,
                        mealType = meal.mealType.name,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = stringResource(meal.mealType.labelRes()),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = meal.recipe.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${formatDecimal(plannedMealCalories(meal))} ${stringResource(R.string.unit_kcal)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        meal.recipe.cuisineType?.takeIf { it.isNotBlank() }?.let { cuisine ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = mealCuisineLabel(cuisine),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                        plannedServingsLabel(meal)?.let { portionLabel ->
                            Text(
                                text = portionLabel,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        if (meal.isPinned) {
                            Text(
                                text = stringResource(R.string.meal_plan_pinned_badge),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                        if (meal.isMealPrepCarryover) {
                            Text(
                                text = stringResource(R.string.meal_plan_meal_prep_badge),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        meal.selectionReason?.takeIf { it.isNotBlank() }?.let { reason ->
                            Text(
                                text = reason,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = onReplaceClick,
                        enabled = !isUpdating,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Autorenew, contentDescription = null)
                        Spacer(modifier = Modifier.size(6.dp))
                        Text(stringResource(R.string.meal_plan_replace_cta))
                    }
                    Button(
                        onClick = onLogClick,
                        enabled = !isLogging && !isUpdating,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isLogging) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(
                                imageVector = Icons.Default.Restaurant,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                            Text(stringResource(R.string.meal_plan_log_cta))
                        }
                    }
                }
                FilledTonalButton(
                    onClick = onShowCompositionClick,
                    enabled = !isUpdating && !isLoadingComposition,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isLoadingComposition) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Info, contentDescription = null)
                        Spacer(modifier = Modifier.size(6.dp))
                        Text(stringResource(R.string.meal_plan_composition_cta))
                    }
                }
                FilledTonalButton(
                    onClick = onTogglePinClick,
                    enabled = !isUpdating,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PushPin, contentDescription = null)
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        stringResource(
                            if (meal.isPinned) {
                                R.string.meal_plan_unpin_cta
                            } else {
                                R.string.meal_plan_pin_cta
                            }
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun MealPlanSwipeBackground(
    dismissDirection: SwipeToDismissBoxValue,
    isUpdating: Boolean
) {
    val isRemove = dismissDirection == SwipeToDismissBoxValue.StartToEnd
    val background = if (isRemove) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.tertiaryContainer
    }
    val icon = if (isRemove) Icons.Default.DeleteOutline else Icons.Default.Block
    val label = if (isRemove) {
        stringResource(R.string.meal_plan_remove_cta)
    } else {
        stringResource(R.string.meal_plan_dislike_cta)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = background,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (isRemove) Arrangement.Start else Arrangement.End
        ) {
            if (isUpdating) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            } else {
                Icon(icon, contentDescription = null)
            }
            Spacer(modifier = Modifier.size(8.dp))
            Text(label, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun EmptyMealSlotCard(
    mealType: MealType,
    onAddClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        onClick = onAddClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(mealType.labelRes()),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.meal_plan_empty_slot_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            FilledTonalButton(onClick = onAddClick) {
                Icon(Icons.Default.Restaurant, contentDescription = null)
                Spacer(modifier = Modifier.size(6.dp))
                Text(stringResource(R.string.meal_plan_add_slot_cta))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MealPlanCompositionSheet(
    meal: MealPlanMealModel,
    composition: RecipeCompositionModel,
    explanation: MealPlanMealExplanationModel?,
    mealPlanReport: MealPlanReportModel?,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(start = 20.dp, top = 8.dp, end = 20.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = meal.recipe.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(meal.mealType.labelRes()),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(
                            R.string.meal_plan_composition_subtitle,
                            formatDecimal(composition.portionMultiplier ?: meal.portionSize)
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            item {
                NutritionFactsCard(composition = composition)
            }
            mealPlanReport
                ?.mealTypePreferences
                ?.firstOrNull { it.mealType.equals(meal.mealType.name, ignoreCase = true) && it.confidence >= 0.15 }
                ?.let { preference ->
                    item {
                        MealPlanHabitHintCard(
                            mealType = meal.mealType,
                            preference = preference
                        )
                    }
                }
            explanation?.let { explanationModel ->
                item {
                    MealPlanExplanationCard(explanation = explanationModel)
                }
            }
            composition.calculationFormula?.takeIf { it.isNotBlank() }?.let { formula ->
                item {
                    InfoBlock(
                        title = stringResource(R.string.meal_plan_composition_formula_title),
                        body = formula
                    )
                }
            }
            composition.nutritionNotes?.takeIf { it.isNotBlank() }?.let { notes ->
                item {
                    InfoBlock(
                        title = stringResource(R.string.meal_plan_composition_notes_title),
                        body = notes
                    )
                }
            }
            if (composition.ingredientBreakdown.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.meal_plan_composition_ingredients_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                items(composition.ingredientBreakdown) { ingredient ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = ingredient.productName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            ingredient.quantity?.let { quantity ->
                                Text(
                                    text = stringResource(
                                        R.string.meal_plan_composition_ingredient_quantity,
                                        formatDecimal(quantity),
                                        ingredient.measurementUnitCode.orEmpty()
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = stringResource(
                                    R.string.meal_plan_composition_ingredient_macros,
                                    formatMacro(ingredient.ingredientCalories),
                                    formatMacro(ingredient.ingredientProtein),
                                    formatMacro(ingredient.ingredientFat),
                                    formatMacro(ingredient.ingredientCarbs)
                                ),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MealPlanHabitHintCard(
    mealType: MealType,
    preference: MealPlanPreferenceProfileModel
) {
    val mealLabel = stringResource(mealType.labelRes())
    val insight = buildMealPlanHabitInsight(mealLabel, preference)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.meal_plan_habit_hint_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = insight,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MealPlanExplanationCard(explanation: MealPlanMealExplanationModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.meal_plan_explanation_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            explanation.reason?.takeIf { it.isNotBlank() }?.let { reason ->
                Text(
                    text = reason,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            explanation.preferenceReason?.takeIf { it.isNotBlank() }?.let { preferenceReason ->
                Text(
                    text = stringResource(
                        R.string.meal_plan_explanation_preference_reason,
                        localizePreferenceReason(preferenceReason)
                    ),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            val metaParts = buildList {
                explanation.coveragePercent?.let {
                    add(stringResource(R.string.meal_plan_explanation_coverage, formatDecimal(it)))
                }
                explanation.matchedSelectedProductCount?.let { matched ->
                    val selected = explanation.selectedProductCount ?: 0
                    add(stringResource(R.string.meal_plan_explanation_selected_match, matched, selected))
                }
                explanation.pantryProductCount?.let {
                    add(stringResource(R.string.meal_plan_explanation_pantry_count, it))
                }
            }
            if (metaParts.isNotEmpty()) {
                Text(
                    text = metaParts.joinToString(" • "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            val scoreParts = buildList {
                explanation.finalScore?.let { add(stringResource(R.string.meal_plan_explanation_final_score, formatDecimal(it))) }
                explanation.ruleScore?.let { add(stringResource(R.string.meal_plan_explanation_rule_score, formatDecimal(it))) }
                explanation.mlScore?.let { add(stringResource(R.string.meal_plan_explanation_ml_score, formatDecimal(it))) }
            }
            if (scoreParts.isNotEmpty()) {
                Text(
                    text = scoreParts.joinToString(" • "),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (explanation.tags.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    explanation.tags.take(4).forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = readableMealPlanTag(tag),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NutritionFactsCard(composition: RecipeCompositionModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.meal_plan_composition_totals_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(
                    R.string.meal_plan_composition_totals_macros,
                    formatMacro(composition.totalCalories),
                    formatMacro(composition.totalProtein),
                    formatMacro(composition.totalFat),
                    formatMacro(composition.totalCarbs)
                ),
                style = MaterialTheme.typography.bodyMedium
            )
            composition.totalPortionWeightG?.let { weight ->
                Text(
                    text = stringResource(R.string.meal_plan_composition_weight, formatDecimal(weight)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            composition.plannedServingsCount?.let { servings ->
                Text(
                    text = stringResource(R.string.meal_plan_composition_servings, formatDecimal(servings)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            composition.caloriesPerServing?.let {
                Text(
                    text = stringResource(
                        R.string.meal_plan_composition_per_serving,
                        formatMacro(it),
                        formatMacro(composition.proteinPerServing),
                        formatMacro(composition.fatPerServing),
                        formatMacro(composition.carbsPerServing)
                    ),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            composition.caloriesPer100g?.let {
                Text(
                    text = stringResource(
                        R.string.meal_plan_composition_per_100g,
                        formatMacro(it),
                        formatMacro(composition.proteinPer100g),
                        formatMacro(composition.fatPer100g),
                        formatMacro(composition.carbsPer100g)
                    ),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun InfoBlock(title: String, body: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatPortionSize(portionSize: Double): String {
    val rounded = kotlin.math.round(portionSize * 100.0) / 100.0
    return if (rounded % 1.0 == 0.0) {
        rounded.toInt().toString()
    } else {
        rounded.toString()
    }
}

@Composable
private fun plannedServingsLabel(meal: MealPlanMealModel): String? {
    val plannedServings = plannedServingsValue(meal)
    return when {
        plannedServings != null -> stringResource(
            R.string.meal_plan_servings_count,
            formatDecimal(plannedServings)
        )
        meal.portionSize != 1.0 -> stringResource(
            R.string.meal_plan_portion_size,
            formatPortionSize(meal.portionSize)
        )
        else -> null
    }
}

private fun plannedServingsValue(meal: MealPlanMealModel): Double? {
    val baseServings = meal.recipe.servingsCount
    return baseServings?.let { servings ->
        (servings * meal.portionSize).coerceAtLeast(0.1)
    }
}

private fun plannedMealCalories(meal: MealPlanMealModel): Double {
    return (meal.recipe.totalCalories * meal.portionSize).coerceAtLeast(0.0)
}

private fun formatDecimal(value: Double): String {
    val rounded = (value * 10.0).roundToInt() / 10.0
    return if (rounded % 1.0 == 0.0) {
        rounded.toInt().toString()
    } else {
        rounded.toString()
    }
}

private fun formatMacro(value: Double?): String = formatDecimal(value ?: 0.0)

@Composable
private fun readableMealPlanTag(tag: String): String {
    return tag.toReadableTagRes()?.let { stringResource(it) }
        ?: tag.replace('_', ' ').replaceFirstChar { it.titlecase(Locale.ROOT) }
}

@Composable
private fun localizePreferenceReason(reason: String): String {
    return when (reason.lowercase(Locale.ROOT)) {
        "faster" -> stringResource(R.string.meal_plan_replace_reason_faster)
        "higher_protein" -> stringResource(R.string.meal_plan_replace_reason_higher_protein)
        "from_home" -> stringResource(R.string.meal_plan_replace_reason_from_home)
        "lighter" -> stringResource(R.string.meal_plan_replace_reason_lighter)
        "lower_calories" -> stringResource(R.string.meal_plan_replace_reason_lower_calories)
        else -> reason.replace('_', ' ').replaceFirstChar { it.titlecase(Locale.ROOT) }
    }
}

private fun String.toReadableTagRes(): Int? {
    return when (this.lowercase(Locale.ROOT)) {
        "protein_gap" -> R.string.recipe_tag_protein_gap
        "goal_fit" -> R.string.recipe_tag_goal_fit
        "calorie_fit" -> R.string.recipe_tag_calorie_fit
        "favorite_cuisine" -> R.string.recipe_tag_favorite_cuisine
        "selected_cuisine" -> R.string.recipe_tag_favorite_cuisine
        "favorite_recipe" -> R.string.recipe_tag_favorite_recipe
        "favorite_ingredients" -> R.string.recipe_tag_favorite_ingredients
        "viewed_before" -> R.string.recipe_tag_viewed_before
        "logged_before" -> R.string.recipe_tag_logged_before
        "recommendation_interest" -> R.string.recipe_tag_recommendation_interest
        "timing_fit" -> R.string.recipe_tag_timing_fit
        "novelty" -> R.string.recipe_tag_novelty
        else -> null
    }
}

@Composable
private fun buildMealPlanHabitInsight(
    mealLabel: String,
    preference: MealPlanPreferenceProfileModel
): String {
    val confidence = formatPercent(preference.confidence)
    return when {
        preference.preferredCookTimeMin != null -> stringResource(
            R.string.meal_plan_habit_hint_cook_time,
            mealLabel.lowercase(Locale.ROOT),
            preference.preferredCookTimeMin.roundToInt(),
            confidence
        )
        preference.preferredProtein != null -> stringResource(
            R.string.meal_plan_habit_hint_protein,
            mealLabel.lowercase(Locale.ROOT),
            preference.preferredProtein.roundToInt(),
            confidence
        )
        preference.preferredCalories != null -> stringResource(
            R.string.meal_plan_habit_hint_calories,
            mealLabel.lowercase(Locale.ROOT),
            preference.preferredCalories.roundToInt(),
            confidence
        )
        preference.dominantReason.isNotBlank() -> stringResource(
            R.string.meal_plan_habit_hint_reason,
            mealLabel.lowercase(Locale.ROOT),
            localizePreferenceReason(preference.dominantReason),
            confidence
        )
        else -> stringResource(R.string.meal_plan_habit_hint_generic, mealLabel.lowercase(Locale.ROOT))
    }
}

private fun formatPercent(value: Double): String = "${(value * 100).roundToInt()}%"

@Composable
private fun mealCuisineLabel(cuisine: String): String {
    val russian = stringResource(R.string.cuisine_russian)
    val italian = stringResource(R.string.cuisine_italian)
    val asian = stringResource(R.string.cuisine_asian)
    val mediterranean = stringResource(R.string.cuisine_mediterranean)
    val mexican = stringResource(R.string.cuisine_mexican)
    val georgian = stringResource(R.string.cuisine_georgian)
    val order = listOf("RUSSIAN", "ITALIAN", "ASIAN", "MEDITERRANEAN", "MEXICAN", "GEORGIAN")

    return cuisine
        .split(',', '/', ';', '|')
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .distinctBy { it.uppercase(Locale.ROOT) }
        .sortedWith(
            compareBy<String> { token ->
                order.indexOf(token.uppercase(Locale.ROOT)).let { index ->
                    if (index >= 0) index else Int.MAX_VALUE
                }
            }.thenBy { it.uppercase(Locale.ROOT) }
        )
        .joinToString(" ? ") { token ->
            when (token.uppercase(Locale.ROOT)) {
                "RUSSIAN" -> russian
                "ITALIAN" -> italian
                "ASIAN" -> asian
                "MEDITERRANEAN" -> mediterranean
                "MEXICAN" -> mexican
                "GEORGIAN" -> georgian
                else -> token.replaceFirstChar { it.titlecase(Locale.ROOT) }
            }
        }
}

private enum class ReplaceMealReason(
    val code: String,
    val labelRes: Int
) {
    FASTER("faster", R.string.meal_plan_replace_reason_faster),
    HIGHER_PROTEIN("higher_protein", R.string.meal_plan_replace_reason_higher_protein),
    FROM_HOME("from_home", R.string.meal_plan_replace_reason_from_home),
    LIGHTER("lighter", R.string.meal_plan_replace_reason_lighter),
    LOWER_CALORIES("lower_calories", R.string.meal_plan_replace_reason_lower_calories)
}

@Composable
private fun ReplaceMealReasonDialog(
    mealTitle: String,
    onDismiss: () -> Unit,
    onReasonSelected: (ReplaceMealReason) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.meal_plan_replace_dialog_title))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = mealTitle,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                ReplaceMealReason.entries.forEach { reason ->
                    FilledTonalButton(
                        onClick = { onReasonSelected(reason) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(reason.labelRes),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}
