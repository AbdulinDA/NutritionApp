package com.abdulin.nutritionapp.presentation.recipe

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.abdulin.nutritionapp.R
import androidx.compose.ui.res.stringResource
import com.abdulin.nutritionapp.core.utils.labelRes
import com.abdulin.nutritionapp.domain.model.MealType
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneratePlanScreen(
    viewModel: GeneratePlanViewModel = hiltViewModel(),
    onPlanGenerated: () -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val dietTypes = listOf("CLASSIC", "VEGETARIAN", "KETO", "PALEO")
    val dayOptions = listOf(1, 3, 7)
    val mealPrepMealTypes = listOf(MealType.LUNCH, MealType.DINNER, MealType.SNACK, MealType.BREAKFAST)
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = state.startDate
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    )

    LaunchedEffect(state.isGenerated) {
        if (state.isGenerated) {
            onPlanGenerated()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.generate_plan_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.onboarding_back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = stringResource(R.string.generate_plan_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.generate_plan_days_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                dayOptions.forEach { days ->
                    FilterChip(
                        selected = state.days == days,
                        onClick = { viewModel.updateDays(days) },
                        label = { Text("$days ${getDaysText(days)}") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.generate_plan_start_date_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            FilledTonalButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    stringResource(
                        R.string.generate_plan_start_date_value,
                        state.startDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                    )
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.generate_plan_meal_prep_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.generate_plan_meal_prep_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            PortionStepper(
                value = state.mealPrepServings,
                onValueChange = viewModel::updateMealPrepServings
            )

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.generate_plan_meal_prep_meals_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.generate_plan_meal_prep_meals_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                mealPrepMealTypes.forEach { mealType ->
                    FilterChip(
                        selected = state.selectedMealPrepMealTypes.contains(mealType),
                        onClick = { viewModel.toggleMealPrepMealType(mealType) },
                        label = { Text(stringResource(mealType.labelRes())) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.generate_plan_diet_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Column(Modifier.selectableGroup()) {
                dietTypes.forEach { diet ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .selectable(
                                selected = (state.dietType == diet),
                                onClick = { viewModel.updateDiet(diet) },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (state.dietType == diet),
                            onClick = null
                        )
                        Text(
                            text = dietLabel(diet),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.generate_plan_cuisine_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.generate_plan_cuisine_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            state.selectedPreferredCuisines
                .takeIf { it.isNotEmpty() }
                ?.let { selectedCuisines ->
                    val selectedCuisineLabels = selectedCuisines.map { cuisine -> cuisineLabel(cuisine) }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(
                            R.string.generate_plan_cuisine_selected_summary,
                            selectedCuisineLabels.joinToString(", ")
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.availableCuisines.forEach { cuisine ->
                    FilterChip(
                        selected = state.selectedPreferredCuisines.contains(cuisine),
                        onClick = { viewModel.togglePreferredCuisine(cuisine) },
                        label = { Text(cuisineLabel(cuisine)) }
                    )
                }
            }

            if (state.pantryProducts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Продукты для персонализации плана",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "План и рекомендации будут опираться на отмеченные продукты.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.pantryProducts.forEach { product ->
                        FilterChip(
                            selected = state.selectedProductIds.contains(product.productId),
                            onClick = { viewModel.toggleProduct(product.productId) },
                            label = { Text(product.productName) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { viewModel.generatePlan() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(stringResource(R.string.generate_plan_submit), fontWeight = FontWeight.Bold)
                }
            }

            if (state.error != null) {
                Text(
                    text = state.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 16.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selectedMillis ->
                            val selectedDate = Instant.ofEpochMilli(selectedMillis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            if (!selectedDate.isBefore(LocalDate.now())) {
                                viewModel.updateStartDate(selectedDate)
                            }
                        }
                        showDatePicker = false
                    }
                ) {
                    Text(stringResource(R.string.generate_plan_date_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun PortionStepper(
    value: Int,
    onValueChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedIconButton(
            onClick = { onValueChange((value - 1).coerceAtLeast(1)) },
            enabled = value > 1
        ) {
            Icon(Icons.Default.Remove, contentDescription = null)
        }
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(
                    if (value == 1) R.string.generate_plan_meal_prep_single else R.string.generate_plan_meal_prep_multiple,
                    value
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        OutlinedIconButton(
            onClick = { onValueChange((value + 1).coerceAtMost(7)) },
            enabled = value < 7
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
        }
    }
}

@Composable
private fun dietLabel(diet: String): String {
    return when (diet) {
        "VEGETARIAN" -> stringResource(R.string.diet_vegetarian)
        "KETO" -> stringResource(R.string.diet_keto)
        "PALEO" -> stringResource(R.string.diet_paleo)
        else -> stringResource(R.string.diet_classic)
    }
}

@Composable
private fun cuisineLabel(cuisine: String): String {
    return localizedCuisineLabel(cuisine)
}

@Composable
private fun localizedCuisineLabel(cuisine: String): String {
    return when (cuisine) {
        "RUSSIAN" -> stringResource(R.string.cuisine_russian)
        "ITALIAN" -> stringResource(R.string.cuisine_italian)
        "ASIAN" -> stringResource(R.string.cuisine_asian)
        "MEDITERRANEAN" -> stringResource(R.string.cuisine_mediterranean)
        "MEXICAN" -> stringResource(R.string.cuisine_mexican)
        "GEORGIAN" -> stringResource(R.string.cuisine_georgian)
        else -> cuisine
    }
}

@Composable
private fun getDaysText(days: Int): String {
    return when (days) {
        1 -> stringResource(R.string.generate_plan_day_single)
        3 -> stringResource(R.string.generate_plan_day_few)
        else -> stringResource(R.string.generate_plan_day_many)
    }
}
