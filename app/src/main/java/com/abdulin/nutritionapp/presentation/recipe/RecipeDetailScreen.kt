package com.abdulin.nutritionapp.presentation.recipe

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.abdulin.nutritionapp.R
import com.abdulin.nutritionapp.core.utils.Resource
import com.abdulin.nutritionapp.core.utils.labelRes
import com.abdulin.nutritionapp.core.utils.localizeMeasurementUnit
import com.abdulin.nutritionapp.domain.model.MealType
import com.abdulin.nutritionapp.domain.model.RecipeCompositionModel
import com.abdulin.nutritionapp.domain.model.RecipeIngredientModel
import com.abdulin.nutritionapp.domain.model.RecipeModel
import com.abdulin.nutritionapp.domain.model.RecipeNutritionIngredientBreakdownModel
import com.abdulin.nutritionapp.domain.model.RecipeSideDishModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    recipeId: Long,
    recommendationSource: String?,
    recommendationReason: String?,
    recommendationImpressionId: Long?,
    initialServings: Double? = null,
    viewModel: RecipeDetailViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onAddtoDiary: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val addState by viewModel.addState.collectAsState()
    val planAddState by viewModel.planAddState.collectAsState()

    var showMealTypeDialog by remember { mutableStateOf(false) }
    var showPlanDialog by remember { mutableStateOf(false) }
    var addRequested by remember { mutableStateOf(false) }
    var planAddRequested by remember { mutableStateOf(false) }
    var selectedDiaryPortionMultiplier by remember { mutableStateOf(1.0) }
    var manualDiaryWeight by remember { mutableStateOf("") }
    var selectedPlanMealType by remember { mutableStateOf(MealType.DINNER) }
    var selectedPlanDate by remember { mutableStateOf(LocalDate.now()) }
    
    val servingsBase = (state.composition?.servingsCount ?: state.recipe?.servingsCount ?: viewModel.calculateSuggestedServings(state.recipe?.totalCalories ?: 0.0)).coerceAtLeast(1)
    var selectedServings by remember(recipeId, servingsBase, state.composition?.sideDishRecipe?.id, initialServings) {
        mutableStateOf((initialServings ?: 1.0).coerceIn(0.25, 99.0))
    }
    var selectedPlanServings by remember(recipeId, servingsBase, state.composition?.sideDishRecipe?.id, initialServings) {
        mutableStateOf((initialServings ?: 1.0).coerceIn(0.25, 99.0))
    }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val recipeAddedMessage = stringResource(R.string.recipe_added_to_diary)
    val recipeAddFailedMessage = stringResource(R.string.recipe_add_failed)
    val recipePlannedMessage = stringResource(R.string.recipe_added_to_plan)
    val recipePlanFailedMessage = stringResource(R.string.recipe_add_to_plan_failed)
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMM") }
    val planningDates = remember { List(7) { LocalDate.now().plusDays(it.toLong()) } }

    LaunchedEffect(recipeId, recommendationSource, recommendationReason, recommendationImpressionId) {
        viewModel.loadRecipe(
            id = recipeId,
            recommendationSource = recommendationSource,
            recommendationReason = recommendationReason,
            recommendationImpressionId = recommendationImpressionId
        )
    }

    LaunchedEffect(addState) {
        if (!addRequested) return@LaunchedEffect
        when (val result = addState) {
            is Resource.Success -> {
                snackbarHostState.showSnackbar(recipeAddedMessage)
                addRequested = false
                viewModel.resetAddState()
                onAddtoDiary()
            }
            is Resource.Error -> {
                snackbarHostState.showSnackbar(result.message ?: recipeAddFailedMessage)
                addRequested = false
                viewModel.resetAddState()
            }
            is Resource.Loading -> Unit
        }
    }

    LaunchedEffect(planAddState) {
        if (!planAddRequested) return@LaunchedEffect
        when (val result = planAddState) {
            is Resource.Success -> {
                snackbarHostState.showSnackbar(recipePlannedMessage)
                planAddRequested = false
                viewModel.resetPlanAddState()
            }
            is Resource.Error -> {
                snackbarHostState.showSnackbar(result.message ?: recipePlanFailedMessage)
                planAddRequested = false
                viewModel.resetPlanAddState()
            }
            is Resource.Loading -> Unit
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.recipe_detail_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.onboarding_back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::toggleFavorite) {
                        Icon(
                            imageVector = if (state.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = stringResource(R.string.product_detail_favorite),
                            tint = if (state.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (state.recipe != null) {
                FloatingActionButton(
                    onClick = {
                        if (addState !is Resource.Loading) {
                            selectedDiaryPortionMultiplier = 1.0
                            manualDiaryWeight = ""
                            showMealTypeDialog = true
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    if (addState is Resource.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = stringResource(R.string.recipe_add_to_diary)
                        )
                    }
                }
            }
        }
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            val isWide = maxWidth > 600.dp
            val contentModifier = Modifier.fillMaxWidth(if (isWide) 0.7f else 1f)

            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.error != null) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = state.error!!, color = MaterialTheme.colorScheme.error)
                    Button(
                        onClick = {
                            viewModel.loadRecipe(
                                id = recipeId,
                                recommendationSource = recommendationSource,
                                recommendationReason = recommendationReason,
                                recommendationImpressionId = recommendationImpressionId
                            )
                        },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(stringResource(R.string.retry))
                    }
                }
            } else if (state.recipe != null) {
                val recipe = state.recipe!!
                Column(
                    modifier = contentModifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    RecipeImage(
                        imageUrl = recipe.imageUrl,
                        contentDescription = recipe.title,
                        recipeTitle = recipe.title,
                        mealType = recipe.mealType,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (isWide) 400.dp else 250.dp)
                            .clip(
                                RoundedCornerShape(
                                    bottomStart = 24.dp,
                                    bottomEnd = 24.dp
                                )
                            ),
                        contentScale = ContentScale.Crop
                    )

                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = recipe.title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        FilledTonalButton(
                            onClick = {
                                selectedPlanDate = LocalDate.now()
                                selectedPlanMealType = recipe.mealType
                                    ?.uppercase(Locale.ROOT)
                                    ?.let { value -> MealType.entries.find { it.name == value } }
                                    ?: MealType.DINNER
                                selectedPlanServings = selectedServings
                                showPlanDialog = true
                            },
                            enabled = planAddState !is Resource.Loading
                        ) {
                            if (planAddState is Resource.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.size(8.dp))
                                Text(stringResource(R.string.recipe_add_to_plan))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${recipe.cookTimeMin} ${stringResource(R.string.recipe_cook_time)}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "${recipe.totalCalories.toInt()} ${stringResource(R.string.unit_kcal)}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                        recipe.servingsCount?.let { servings ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.recipe_servings_count, servings),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        recipe.portionWeightG?.let { portionWeight ->
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.recipe_portion_weight, portionWeight.asIntString()),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                        if (!recommendationSource.isNullOrBlank()) {
                            RecommendationInsightCard(
                                source = recommendationSource,
                                reason = recommendationReason
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                        }

                        Text(
                            text = stringResource(R.string.recipe_nutrition_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            MacroInfo(
                                stringResource(R.string.home_protein),
                                "${recipe.totalProtein.toInt()}${stringResource(R.string.unit_g)}",
                                Modifier.weight(1f)
                            )
                            MacroInfo(
                                stringResource(R.string.home_fat),
                                "${recipe.totalFat.toInt()}${stringResource(R.string.unit_g)}",
                                Modifier.weight(1f)
                            )
                            MacroInfo(
                                stringResource(R.string.home_carbs),
                                "${recipe.totalCarbs.toInt()}${stringResource(R.string.unit_g)}",
                                Modifier.weight(1f)
                            )
                        }

                        if (recipe.requiresSideDish || recipe.recommendedSideDishes.isNotEmpty() || state.composition != null) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                            NutritionCompositionSection(
                                recipe = recipe,
                                composition = state.composition,
                                selectedSideDishId = state.selectedSideDishId,
                                isLoading = state.isCompositionLoading,
                                compositionError = state.compositionError,
                                onSelectSideDish = viewModel::selectSideDish
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                        NutritionBreakdownSection(
                            recipe = recipe,
                            composition = state.composition,
                            viewModel = viewModel,
                            selectedServings = selectedServings,
                            onServingsChange = { selectedServings = it }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                        
                        IngredientsSection(
                            recipe = recipe,
                            composition = state.composition,
                            selectedServings = selectedServings
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                        
                        Text(
                            text = stringResource(R.string.recipe_instruction),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        RecipeInstructionSection(instruction = recipe.instruction)

                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }

            if (addState is Resource.Error) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.resetAddState() }) {
                            Text(stringResource(R.string.ok))
                        }
                    }
                ) {
                    Text((addState as Resource.Error).message ?: stringResource(R.string.recipe_add_failed))
                }
            }
        }
    }

    if (showMealTypeDialog) {
        val recipe = state.recipe
        val defaultPortionWeight = recipe?.portionWeightG ?: 300.0
        val effectiveWeight = manualDiaryWeight.toDoubleOrNull()?.takeIf { it > 0 }
            ?: defaultPortionWeight * selectedDiaryPortionMultiplier
        AlertDialog(
            onDismissRequest = { showMealTypeDialog = false },
            title = { Text(stringResource(R.string.home_meals)) },
            text = {
                Column {
                    Text(
                        text = stringResource(R.string.recipe_diary_portion_prompt),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(0.5, 1.0, 2.0).forEach { multiplier ->
                            FilterChip(
                                selected = manualDiaryWeight.isBlank() && selectedDiaryPortionMultiplier == multiplier,
                                onClick = {
                                    selectedDiaryPortionMultiplier = multiplier
                                    manualDiaryWeight = ""
                                },
                                label = {
                                    Text(stringResource(R.string.recipe_portion_multiplier_label, formatPortionMultiplier(multiplier)))
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = manualDiaryWeight,
                        onValueChange = { manualDiaryWeight = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.recipe_diary_manual_grams)) },
                        supportingText = {
                            Text(stringResource(R.string.recipe_diary_manual_grams_hint))
                        },
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(
                            R.string.recipe_diary_weight_preview,
                            effectiveWeight.asIntString()
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    MealType.entries.forEach { type ->
                        ListItem(
                            headlineContent = { Text(stringResource(type.labelRes())) },
                            modifier = Modifier.clickable {
                                addRequested = true
                                viewModel.addToDiary(type.name, effectiveWeight)
                                showMealTypeDialog = false
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMealTypeDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showPlanDialog) {
        val perServingCalories = (state.composition?.caloriesPerServing
            ?: ((state.composition?.totalCalories ?: state.recipe?.totalCalories ?: 0.0) / servingsBase.toDouble()))
        val normalizedPlanServings = selectedPlanServings.coerceIn(0.25, 99.0)
        val planPortionSize = (normalizedPlanServings / servingsBase.toDouble()).coerceIn(0.25, 8.0)
        AlertDialog(
            onDismissRequest = { showPlanDialog = false },
            title = { Text(stringResource(R.string.recipe_add_to_plan)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.generate_plan_start_date_title),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            planningDates.forEach { date ->
                                FilterChip(
                                    selected = selectedPlanDate == date,
                                    onClick = { selectedPlanDate = date },
                                    label = { Text(date.format(dateFormatter)) }
                                )
                            }
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.home_meals),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MealType.entries.forEach { type ->
                                FilterChip(
                                    selected = selectedPlanMealType == type,
                                    onClick = { selectedPlanMealType = type },
                                    label = { Text(stringResource(type.labelRes())) }
                                )
                            }
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.recipe_selected_servings_title),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilledTonalButton(
                                onClick = {
                                    selectedPlanServings = (selectedPlanServings - 0.5).coerceAtLeast(0.25)
                                }
                            ) {
                                Text("-")
                            }
                            OutlinedTextField(
                                value = formatPortionMultiplier(normalizedPlanServings),
                                onValueChange = { value ->
                                    val filtered = value.filter { it.isDigit() || it == '.' || it == ',' }.replace(',', '.')
                                    val parsed = filtered.toDoubleOrNull()
                                    if (parsed != null) {
                                        selectedPlanServings = parsed.coerceIn(0.25, 99.0)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )
                            FilledTonalButton(
                                onClick = {
                                    selectedPlanServings = (selectedPlanServings + 0.5).coerceAtMost(99.0)
                                }
                            ) {
                                Text("+")
                            }
                        }
                        Text(
                            text = stringResource(
                                R.string.recipe_serving_explanation,
                                formatPortionMultiplier(normalizedPlanServings),
                                servingsBase
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${formatCompactDecimal(perServingCalories * normalizedPlanServings)} ${stringResource(R.string.unit_kcal)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        planAddRequested = true
                        viewModel.addToMealPlan(
                            planDate = selectedPlanDate.toString(),
                            mealType = selectedPlanMealType.name,
                            portionSize = planPortionSize
                        )
                        showPlanDialog = false
                    }
                ) {
                    Text(stringResource(R.string.recipe_add_to_plan_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showPlanDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }
}

@Composable
private fun NutritionQualityStatusSection(recipe: RecipeModel) {
    var expanded by remember { mutableStateOf(true) }
    
    val normalizedStatus = recipe.nutritionCalculationStatus
        ?.trim()
        ?.uppercase(Locale.ROOT)
        .orEmpty()
    if (normalizedStatus.isBlank() && recipe.nutritionNotes.isNullOrBlank()) {
        return
    }

    // Заголовок с кнопкой раскрытия
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.recipe_nutrition_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = { expanded = !expanded }) {
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (expanded) "Скрыть" else "Показать"
            )
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
    
    // Контент сворачивается/разворачивается
    if (expanded) {
        val (label, containerColor, contentColor) = when (normalizedStatus) {
            "COMPLETE" -> Triple(
                stringResource(R.string.recipe_nutrition_status_complete),
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
                MaterialTheme.colorScheme.onSecondaryContainer
            )
            "ESTIMATED" -> Triple(
                stringResource(R.string.recipe_nutrition_status_estimated),
                MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.65f),
                MaterialTheme.colorScheme.onTertiaryContainer
            )
            "REQUIRES_SIDE_DISH" -> Triple(
                stringResource(R.string.recipe_nutrition_status_side_dish),
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                MaterialTheme.colorScheme.onPrimaryContainer
            )
            "INCOMPLETE" -> Triple(
                stringResource(R.string.recipe_nutrition_status_incomplete),
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
                MaterialTheme.colorScheme.onErrorContainer
            )
            else -> Triple(
                stringResource(R.string.recipe_nutrition_status_unknown),
                MaterialTheme.colorScheme.surfaceContainerHigh,
                MaterialTheme.colorScheme.onSurface
            )
        }

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = containerColor
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor
                )
                recipe.nutritionNotes?.takeIf { it.isNotBlank() }?.let { note ->
                    Text(
                        text = note,
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentColor
                    )
                }
            }
        }
    }
}

@Composable
private fun RecommendationInsightCard(
    source: String,
    reason: String?
) {
    Snackbar {
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = stringResource(R.string.recipe_recommendation_title),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = when (source) {
                    "home_recommendation" -> stringResource(R.string.recipe_recommendation_home_source)
                    "meal_plan" -> stringResource(R.string.recipe_recommendation_plan_source)
                    else -> stringResource(R.string.recipe_recommendation_smart_source)
                }
            )
            reason?.takeIf { it.isNotBlank() }?.let {
                Text(text = it)
            }
        }
    }
}



@Composable
fun MacroInfo(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.outline
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun NutritionCompositionSection(
    recipe: RecipeModel,
    composition: RecipeCompositionModel?,
    selectedSideDishId: Long?,
    isLoading: Boolean,
    compositionError: String?,
    onSelectSideDish: (Long?) -> Unit
) {
    var expanded by remember { mutableStateOf(true) }
    
    val effectiveComposition = composition ?: RecipeCompositionModel(
        mainRecipe = recipe,
        totalCalories = recipe.totalCalories,
        totalProtein = recipe.totalProtein,
        totalFat = recipe.totalFat,
        totalCarbs = recipe.totalCarbs,
        servingsCount = recipe.servingsCount
    )

    // Заголовок с кнопкой раскрытия
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.recipe_composition_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = { expanded = !expanded }) {
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (expanded) "Скрыть" else "Показать"
            )
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
    
    // Контент сворачивается/разворачивается
    if (expanded) {
        Text(
            text = stringResource(R.string.recipe_composition_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (recipe.recommendedSideDishes.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.recipe_side_dishes_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedSideDishId == null,
                    onClick = { onSelectSideDish(null) },
                    label = { Text(stringResource(R.string.recipe_side_dish_not_selected)) }
                )
                recipe.recommendedSideDishes.forEach { sideDish ->
                    FilterChip(
                        selected = selectedSideDishId == sideDish.sideDishRecipeId,
                        onClick = { onSelectSideDish(sideDish.sideDishRecipeId) },
                        label = { Text(sideDish.sideDishRecipeName) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        compositionError?.takeIf { it.isNotBlank() }?.let { error ->
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Text(
            text = buildCompositionSummary(
                composition = effectiveComposition,
                selectedSideDish = recipe.recommendedSideDishes.firstOrNull { it.sideDishRecipeId == selectedSideDishId }
            ),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MacroInfo(
                stringResource(R.string.recipe_total_kcal_short),
                effectiveComposition.totalCalories.asKcalString(),
                Modifier.weight(1f)
            )
            MacroInfo(
                stringResource(R.string.home_protein),
                effectiveComposition.totalProtein.asGramString(),
                Modifier.weight(1f)
            )
            MacroInfo(
                stringResource(R.string.home_fat),
                effectiveComposition.totalFat.asGramString(),
                Modifier.weight(1f)
            )
            MacroInfo(
                stringResource(R.string.home_carbs),
                effectiveComposition.totalCarbs.asGramString(),
                Modifier.weight(1f)
            )
        }

        effectiveComposition.totalPortionWeightG?.let { weight ->
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.recipe_total_portion_weight, weight.asIntString()),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        effectiveComposition.servingsCount?.let { servings ->
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.recipe_composition_servings, servings),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        effectiveComposition.nutritionNote?.takeIf { it.isNotBlank() }?.let { note ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = note,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun buildCompositionSummary(
    composition: RecipeCompositionModel,
    selectedSideDish: RecipeSideDishModel?
): String {
    val mainTitle = composition.mainRecipe.title
    val sideDishTitle = composition.sideDishRecipe?.title ?: selectedSideDish?.sideDishRecipeName
    return when {
        composition.sideDishIncludedInNutrition && !sideDishTitle.isNullOrBlank() ->
            stringResource(R.string.recipe_composition_with_side_dish, mainTitle, sideDishTitle)
        !sideDishTitle.isNullOrBlank() ->
            stringResource(R.string.recipe_composition_without_side_dish_nutrition, mainTitle, sideDishTitle)
        else ->
            stringResource(R.string.recipe_composition_base_only, mainTitle)
    }
}

private fun Double.asIntString(): String = formatCompactDecimal(this)

private fun Double.asGramString(): String = "${formatCompactDecimal(this)} g"

private fun Double.asKcalString(): String = "${formatCompactDecimal(this)} kcal"

private fun formatCompactDecimal(value: Double): String {
    val rounded = kotlin.math.round(value * 10.0) / 10.0
    return if (rounded % 1.0 == 0.0) {
        rounded.toInt().toString()
    } else {
        String.format(Locale.getDefault(), "%.1f", rounded)
    }
}

private fun formatPortionMultiplier(multiplier: Double): String {
    return if (multiplier % 1.0 == 0.0) {
        multiplier.toInt().toString()
    } else {
        String.format(Locale.getDefault(), "%.1f", multiplier)
    }
}

private fun resolveDisplayUnit(productName: String?, rawUnit: String): String {
    val localizedUnit = localizeMeasurementUnit(rawUnit).orEmpty()
    val normalizedName = productName.orEmpty().lowercase(Locale.ROOT)
    val liquidKeywords = listOf(
        "масло", "oil", "молоко", "milk", "кефир", "сливки", "cream",
        "йогурт", "yogurt", "вода", "water", "бульон", "juice", "сок",
        "уксус", "vinegar", "соус", "sauce", "soy", "сироп"
    )
    val looksLikeLiquid = liquidKeywords.any { normalizedName.contains(it) }
    return if (looksLikeLiquid && localizedUnit in setOf("шт", "pcs")) {
        localizeMeasurementUnit("ml").orEmpty()
    } else {
        localizedUnit.ifBlank { rawUnit }
    }
}

@Composable
private fun NutritionBreakdownSection(
    recipe: RecipeModel,
    composition: RecipeCompositionModel?,
    viewModel: RecipeDetailViewModel,
    selectedServings: Double,
    onServingsChange: (Double) -> Unit
) {
    val servingsBase = (composition?.servingsCount ?: recipe.servingsCount ?: viewModel.calculateSuggestedServings(recipe.totalCalories)).coerceAtLeast(1)
    var detailsExpanded by remember { mutableStateOf(false) }
    val perServingCalories = composition?.caloriesPerServing ?: (composition?.totalCalories ?: recipe.totalCalories) / servingsBase
    val perServingProtein = composition?.proteinPerServing ?: (composition?.totalProtein ?: recipe.totalProtein) / servingsBase
    val perServingFat = composition?.fatPerServing ?: (composition?.totalFat ?: recipe.totalFat) / servingsBase
    val perServingCarbs = composition?.carbsPerServing ?: (composition?.totalCarbs ?: recipe.totalCarbs) / servingsBase
    val normalizedStatus = recipe.nutritionCalculationStatus
        ?.trim()
        ?.uppercase(Locale.ROOT)
        .orEmpty()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { detailsExpanded = !detailsExpanded }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.recipe_nutrition_breakdown_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Icon(
            imageVector = if (detailsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = null
        )
    }

    if (detailsExpanded) {
        if (normalizedStatus.isNotBlank() || !recipe.nutritionNotes.isNullOrBlank()) {
            val (label, containerColor, contentColor) = when (normalizedStatus) {
                "COMPLETE" -> Triple(
                    stringResource(R.string.recipe_nutrition_status_complete),
                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
                    MaterialTheme.colorScheme.onSecondaryContainer
                )
                "ESTIMATED" -> Triple(
                    stringResource(R.string.recipe_nutrition_status_estimated),
                    MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.65f),
                    MaterialTheme.colorScheme.onTertiaryContainer
                )
                "REQUIRES_SIDE_DISH" -> Triple(
                    stringResource(R.string.recipe_nutrition_status_side_dish),
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    MaterialTheme.colorScheme.onPrimaryContainer
                )
                "INCOMPLETE" -> Triple(
                    stringResource(R.string.recipe_nutrition_status_incomplete),
                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
                    MaterialTheme.colorScheme.onErrorContainer
                )
                else -> Triple(
                    stringResource(R.string.recipe_nutrition_status_unknown),
                    MaterialTheme.colorScheme.surfaceContainerHigh,
                    MaterialTheme.colorScheme.onSurface
                )
            }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = containerColor
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = contentColor
                    )
                    recipe.nutritionNotes?.takeIf { it.isNotBlank() }?.let { note ->
                        Text(
                            text = note,
                            style = MaterialTheme.typography.bodyMedium,
                            color = contentColor
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        Text(
            text = stringResource(
                R.string.recipe_servings_auto_calculated,
                servingsBase
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(
                R.string.recipe_servings_calculated_from,
                (perServingCalories * selectedServings).toInt(),
                perServingCalories.toInt()
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))
        FormulaLine(
            label = stringResource(R.string.recipe_formula_total_kcal),
            value = stringResource(R.string.recipe_formula_total_kcal_value)
        )
        FormulaLine(
            label = stringResource(R.string.recipe_formula_per_serving),
            value = stringResource(R.string.recipe_formula_per_serving_value, servingsBase)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FormulaSummaryRow(
                    label = stringResource(R.string.recipe_formula_total_recipe),
                    value = "${formatCompactDecimal(composition?.totalCalories ?: recipe.totalCalories)} ${stringResource(R.string.unit_kcal)}"
                )
                FormulaSummaryRow(
                    label = stringResource(R.string.recipe_formula_one_serving),
                    value = "${formatCompactDecimal(perServingCalories)} ${stringResource(R.string.unit_kcal)}"
                )
                FormulaSummaryRow(
                    label = stringResource(R.string.recipe_formula_selected_servings),
                    value = "${formatCompactDecimal(perServingCalories * selectedServings)} ${stringResource(R.string.unit_kcal)}"
                )
            }
        }
        composition?.calculationFormula?.takeIf { it.isNotBlank() }?.let { formula ->
            Spacer(modifier = Modifier.height(12.dp))
            CalculationDetailsCard(
                title = stringResource(R.string.recipe_formula_details_title),
                lines = normalizeCalculationText(formula)
            )
        }
        composition?.ingredientBreakdown?.takeIf { it.isNotEmpty() }?.let { breakdown ->
            Spacer(modifier = Modifier.height(12.dp))
            IngredientCalculationSection(
                ingredients = breakdown,
                multiplier = selectedServings / servingsBase.toDouble()
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = stringResource(R.string.recipe_selected_servings_title),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold
    )
    Spacer(modifier = Modifier.height(8.dp))
    
    // Отображаем только допустимые варианты порций (ограниченные servingsRange)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilledTonalButton(
            onClick = {
                onServingsChange((selectedServings - 0.5).coerceAtLeast(0.25))
            }
        ) {
            Text("-")
        }
        OutlinedTextField(
            value = formatPortionMultiplier(selectedServings),
            onValueChange = { value ->
                val newValue = value.filter { it.isDigit() || it == '.' || it == ',' }
                val newServings = newValue.toDoubleOrNull()?.coerceIn(0.25, 99.0) ?: selectedServings
                onServingsChange(newServings)
            },
            modifier = Modifier.weight(1f),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )
        FilledTonalButton(
            onClick = {
                onServingsChange((selectedServings + 0.5).coerceIn(0.25, 99.0))
            }
        ) {
            Text("+")
        }
    }

    Spacer(modifier = Modifier.height(12.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        MacroInfo(
            stringResource(R.string.recipe_total_kcal_short),
            (perServingCalories * selectedServings).asKcalString(),
            Modifier.weight(1f)
        )
        MacroInfo(
            stringResource(R.string.home_protein),
            (perServingProtein * selectedServings).asGramString(),
            Modifier.weight(1f)
        )
        MacroInfo(
            stringResource(R.string.home_fat),
            (perServingFat * selectedServings).asGramString(),
            Modifier.weight(1f)
        )
        MacroInfo(
            stringResource(R.string.home_carbs),
            (perServingCarbs * selectedServings).asGramString(),
            Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(12.dp))
    Text(
        text = stringResource(
            R.string.recipe_serving_explanation,
            formatPortionMultiplier(selectedServings),
            servingsBase
        ),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun IngredientsSection(
    recipe: RecipeModel,
    composition: RecipeCompositionModel?,
    selectedServings: Double
) {
    val servingsBase = (composition?.servingsCount ?: recipe.servingsCount ?: 1).coerceAtLeast(1)
    val ingredientMultiplier = (selectedServings / servingsBase.toDouble()).coerceIn(0.05, 99.0)
    Text(
        text = stringResource(R.string.recipe_ingredients_title),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(16.dp))
    
    if (composition?.ingredientBreakdown != null && composition.ingredientBreakdown.isNotEmpty()) {
        composition.ingredientBreakdown.forEach { ingredient ->
            IngredientRow(
                ingredient = ingredient,
                multiplier = ingredientMultiplier
            )
            if (ingredient != composition.ingredientBreakdown.last()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    } else if (recipe.ingredients.isNotEmpty()) {
        recipe.ingredients.forEach { ingredient ->
            IngredientRow(
                ingredient = ingredient,
                multiplier = ingredientMultiplier
            )
            if (ingredient != recipe.ingredients.last()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    } else {
        Text(
            text = stringResource(R.string.recipe_ingredients_not_available),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun IngredientRow(
    ingredient: RecipeNutritionIngredientBreakdownModel,
    multiplier: Double
) {
    val scaledCalories = (ingredient.ingredientCalories ?: 0.0) * multiplier
    val scaledProtein = (ingredient.ingredientProtein ?: 0.0) * multiplier
    val scaledFat = (ingredient.ingredientFat ?: 0.0) * multiplier
    val scaledCarbs = (ingredient.ingredientCarbs ?: 0.0) * multiplier
    val amountText = formatIngredientAmount(
        quantity = ingredient.quantity,
        unit = ingredient.measurementUnitCode,
        multiplier = multiplier,
        productName = ingredient.productName
    )

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = ingredient.productName,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = scaledCalories.asKcalString(),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            if (amountText.isNotBlank()) {
                Text(
                    text = amountText,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text = stringResource(
                    R.string.recipe_ingredient_macros,
                    scaledProtein.asIntString(),
                    scaledFat.asIntString(),
                    scaledCarbs.asIntString()
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun IngredientRow(
    ingredient: RecipeIngredientModel,
    multiplier: Double
) {
    val scaledCalories = ingredient.calories * multiplier
    val scaledProtein = ingredient.protein * multiplier
    val scaledFat = ingredient.fat * multiplier
    val scaledCarbs = ingredient.carbs * multiplier
    val amountText = formatIngredientAmount(
        quantity = ingredient.quantity,
        unit = ingredient.unit,
        multiplier = multiplier,
        productName = ingredient.name
    )

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = ingredient.name,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = scaledCalories.asKcalString(),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            if (amountText.isNotBlank()) {
                Text(
                    text = amountText,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text = stringResource(
                    R.string.recipe_ingredient_macros,
                    scaledProtein.asIntString(),
                    scaledFat.asIntString(),
                    scaledCarbs.asIntString()
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatIngredientAmount(
    quantity: Double?,
    unit: String?,
    multiplier: Double,
    productName: String
): String {
    if (quantity == null && unit == null) return ""
    val scaledQuantity = quantity?.times(multiplier)
    val formattedQuantity = if (scaledQuantity != null) {
        if (scaledQuantity % 1.0 == 0.0) {
            scaledQuantity.toInt().toString()
        } else {
            String.format(Locale.getDefault(), "%.2f", scaledQuantity).removeSuffix("0").removeSuffix(".")
        }
    } else {
        ""
    }
    val formattedUnit = unit?.let { resolveDisplayUnit(productName, it) }
    return listOf(formattedQuantity, formattedUnit)
        .filter { !it.isNullOrBlank() }
        .joinToString(" ")
}

@Composable
private fun RecipeInstructionSection(instruction: String?) {
    val steps = remember(instruction) { parseInstructionSteps(instruction) }
    if (steps.isEmpty()) {
        Text(
            text = stringResource(R.string.recipe_unknown_instruction),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        steps.forEachIndexed { index, step ->
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "${index + 1}",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Text(
                        text = step,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun FormulaSummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun CalculationDetailsCard(title: String, lines: List<String>) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            lines.forEach { line ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = line,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun IngredientCalculationSection(
    ingredients: List<RecipeNutritionIngredientBreakdownModel>,
    multiplier: Double
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Вклад ингредиентов",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            ingredients.forEachIndexed { index, ingredient ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = ingredient.productName,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${formatCompactDecimal((ingredient.ingredientCalories ?: 0.0) * multiplier)} ${stringResource(R.string.unit_kcal)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    val quantityText = ingredient.quantity?.let { quantity ->
                        val scaledQuantity = quantity * multiplier
                        val unit = ingredient.measurementUnitCode?.let { resolveDisplayUnit(ingredient.productName, it) }.orEmpty()
                        "${formatCompactDecimal(scaledQuantity)} $unit".trim()
                    }
                    if (!quantityText.isNullOrBlank()) {
                        Text(
                            text = quantityText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "Б: ${formatCompactDecimal((ingredient.ingredientProtein ?: 0.0) * multiplier)} г  •  Ж: ${formatCompactDecimal((ingredient.ingredientFat ?: 0.0) * multiplier)} г  •  У: ${formatCompactDecimal((ingredient.ingredientCarbs ?: 0.0) * multiplier)} г",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    ingredient.calculationBasis?.takeIf { it.isNotBlank() }?.let { basis ->
                        Text(
                            text = basis,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (index != ingredients.lastIndex) {
                    HorizontalDivider()
                }
            }
        }
    }
}

private fun parseInstructionSteps(instruction: String?): List<String> {
    val raw = instruction
        ?.replace("\r", "\n")
        ?.replace(Regex("\\n+"), "\n")
        ?.trim()
        .orEmpty()
    if (raw.isBlank()) return emptyList()

    val numbered = Regex("""(?:^|\n)\s*(?:Шаг\s*)?\d+[.)]\s*""", RegexOption.IGNORE_CASE)
    if (numbered.containsMatchIn(raw)) {
        return numbered
            .split(raw)
            .map { it.trim(' ', '\n', ';') }
            .filter { it.isNotBlank() }
    }

    return raw
        .split('\n')
        .flatMap { block -> block.split(Regex("""(?<=[.!?])\s+(?=[А-ЯA-Z0-9])""")) }
        .map { it.trim() }
        .filter { it.isNotBlank() }
}

private fun normalizeCalculationText(formula: String): List<String> {
    return formula
        .replace("\r", "\n")
        .split('\n')
        .flatMap { line ->
            line.split(Regex("""(?<=[.!?;])\s+(?=[А-ЯA-Z0-9])"""))
        }
        .map { it.trim().trimStart('•', '-', '–') }
        .filter { it.isNotBlank() }
}

@Composable
private fun FormulaLine(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}
