package com.abdulin.nutritionapp.presentation.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TextButton
import androidx.compose.material3.AssistChip
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.abdulin.nutritionapp.R
import com.abdulin.nutritionapp.core.utils.SupportedUiActivityLevels
import com.abdulin.nutritionapp.core.utils.toActivityLabelRes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = hiltViewModel(),
    onFinish: () -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.isCompleted) {
        if (state.isCompleted) {
            onFinish()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.onboarding_profile_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (state.currentStep > 0) {
                                viewModel.prevStep()
                            } else {
                                onBack()
                            }
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.onboarding_back))
                    }
                }
            )
        }
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(if (maxWidth > 600.dp) 0.7f else 1f)
                    .padding(16.dp)
                    .imePadding()
            ) {
                LinearProgressIndicator(
                    progress = { (state.currentStep + 1) / 5f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                    ) {
                        AnimatedContent(
                            targetState = state.currentStep,
                            transitionSpec = {
                                if (targetState > initialState) {
                                    (slideInHorizontally { it } + fadeIn())
                                        .togetherWith(slideOutHorizontally { -it } + fadeOut())
                                } else {
                                    (slideInHorizontally { -it } + fadeIn())
                                        .togetherWith(slideOutHorizontally { it } + fadeOut())
                                }
                            },
                            label = "OnboardingStep"
                        ) { step ->
                            when (step) {
                                0 -> StepPersonalParams(state, viewModel)
                                1 -> StepActivityDiet(state, viewModel)
                                2 -> StepFoodPreferences(state, viewModel)
                                3 -> StepGoals(state, viewModel)
                                else -> StepSummary(state)
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                if (state.error != null) {
                    Text(
                        text = state.error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Button(
                    onClick = {
                        if (state.currentStep < 4) viewModel.nextStep() else viewModel.completeOnboarding()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = isStepValid(state) && !state.isLoading,
                    shape = MaterialTheme.shapes.large
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text(
                            if (state.currentStep < 4) stringResource(R.string.onboarding_continue)
                            else stringResource(R.string.onboarding_finish),
                            fontWeight = FontWeight.Bold
                        )
                        if (state.currentStep < 4) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun isStepValid(state: OnboardingState): Boolean {
    return when (state.currentStep) {
        0 -> state.birthDate.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) &&
            state.height >= 100 &&
            state.weight >= 30
        1 -> true
        2 -> true
        3 -> state.targetWeight >= 30
        else -> true
    }
}

@Composable
private fun StepPersonalParams(state: OnboardingState, viewModel: OnboardingViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(stringResource(R.string.onboarding_step_params), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            stringResource(R.string.onboarding_params_intro),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )

        Text(stringResource(R.string.onboarding_gender), style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            FilterChip(
                selected = state.gender == "MALE",
                onClick = { viewModel.updateData(gender = "MALE") },
                label = { Text(stringResource(R.string.onboarding_male)) }
            )
            FilterChip(
                selected = state.gender == "FEMALE",
                onClick = { viewModel.updateData(gender = "FEMALE") },
                label = { Text(stringResource(R.string.onboarding_female)) }
            )
        }

        OutlinedTextField(
            value = state.birthDate,
            onValueChange = { viewModel.updateData(birthDate = it) },
            label = { Text(stringResource(R.string.onboarding_birth_date_hint)) },
            supportingText = {
                Text(
                    if (state.birthDate.isNotBlank() && !state.birthDate.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
                        stringResource(R.string.register_error_birth_date)
                    } else {
                        stringResource(R.string.onboarding_birth_date_format)
                    }
                )
            },
            modifier = Modifier.fillMaxWidth(),
            isError = state.birthDate.isNotBlank() && !state.birthDate.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))
        )
        OutlinedTextField(
            value = state.height.toString(),
            onValueChange = { viewModel.updateData(height = it.toDoubleOrNull() ?: 0.0) },
            label = { Text(stringResource(R.string.onboarding_height_hint)) },
            modifier = Modifier.fillMaxWidth(),
            isError = state.height > 0 && state.height < 100,
            supportingText = {
                if (state.height > 0 && state.height < 100) {
                    Text(stringResource(R.string.onboarding_error_height))
                }
            }
        )
        OutlinedTextField(
            value = state.weight.toString(),
            onValueChange = { viewModel.updateData(weight = it.toDoubleOrNull() ?: 0.0) },
            label = { Text(stringResource(R.string.onboarding_current_weight_hint)) },
            modifier = Modifier.fillMaxWidth(),
            isError = state.weight > 0 && state.weight < 30,
            supportingText = {
                if (state.weight > 0 && state.weight < 30) {
                    Text(stringResource(R.string.onboarding_error_weight))
                }
            }
        )
    }
}

@Composable
private fun StepActivityDiet(state: OnboardingState, viewModel: OnboardingViewModel) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(stringResource(R.string.onboarding_step_activity), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            stringResource(R.string.onboarding_activity_intro),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )

        Text(stringResource(R.string.onboarding_activity_label), style = MaterialTheme.typography.titleMedium)
        SupportedUiActivityLevels.forEach { value ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = state.activityLevel == value,
                        onClick = { viewModel.updateData(activityLevel = value) },
                        role = Role.RadioButton
                    )
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = state.activityLevel == value, onClick = null)
                Text(
                    stringResource(value.toActivityLabelRes()),
                    modifier = Modifier.padding(start = 12.dp)
                )
            }
        }

        Text(stringResource(R.string.onboarding_diet_label), style = MaterialTheme.typography.titleMedium)
        Column(Modifier.selectableGroup()) {
            listOf(
                "CLASSIC" to R.string.diet_classic_description,
                "VEGETARIAN" to R.string.diet_vegetarian_description,
                "KETO" to R.string.diet_keto_description
            ).forEach { (value, labelRes) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = state.dietType == value,
                            onClick = { viewModel.updateData(dietType = value) },
                            role = Role.RadioButton
                        )
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = state.dietType == value, onClick = null)
                    Text(stringResource(labelRes), modifier = Modifier.padding(start = 12.dp))
                }
            }
        }
    }
}

@Composable
private fun StepFoodPreferences(state: OnboardingState, viewModel: OnboardingViewModel) {
    var customRestriction by remember { mutableStateOf("") }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            stringResource(R.string.onboarding_step_preferences),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            stringResource(R.string.onboarding_preferences_intro),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )

        Text(
            stringResource(R.string.onboarding_allergies_title),
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            stringResource(R.string.onboarding_allergies_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Milk", "Eggs", "Gluten", "Nuts", "Peanuts", "Fish", "Seafood", "Soy").forEach { allergy ->
                FilterChip(
                    selected = state.allergies.contains(allergy),
                    onClick = { viewModel.toggleAllergy(allergy) },
                    label = { Text(allergy) }
                )
            }
        }
        OutlinedTextField(
            value = customRestriction,
            onValueChange = { customRestriction = it },
            label = { Text(stringResource(R.string.onboarding_custom_restriction_label)) },
            placeholder = { Text(stringResource(R.string.onboarding_custom_restriction_placeholder)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        TextButton(
            onClick = {
                viewModel.addCustomAllergy(customRestriction)
                customRestriction = ""
            },
            enabled = customRestriction.trim().isNotBlank()
        ) {
            Text(stringResource(R.string.onboarding_add_custom_restriction))
        }
        if (state.allergies.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.allergies.sorted().forEach { allergy ->
                    AssistChip(
                        onClick = { viewModel.removeAllergy(allergy) },
                        label = { Text(allergy) },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null
                            )
                        }
                    )
                }
            }
        }

        Text(stringResource(R.string.onboarding_cuisine_title), style = MaterialTheme.typography.titleMedium)
        Text(
            stringResource(R.string.onboarding_cuisine_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("RUSSIAN", "ITALIAN", "ASIAN", "MEDITERRANEAN", "MEXICAN", "GEORGIAN").forEach { cuisine ->
                FilterChip(
                    selected = state.favoriteCuisines.contains(cuisine),
                    onClick = { viewModel.toggleFavoriteCuisine(cuisine) },
                    label = { Text(cuisineLabel(cuisine)) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            stringResource(R.string.onboarding_disliked_cuisine_title),
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            stringResource(R.string.onboarding_disliked_cuisine_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("RUSSIAN", "ITALIAN", "ASIAN", "MEDITERRANEAN", "MEXICAN", "GEORGIAN").forEach { cuisine ->
                FilterChip(
                    selected = state.dislikedCuisines.contains(cuisine),
                    onClick = { viewModel.toggleDislikedCuisine(cuisine) },
                    label = { Text(cuisineLabel(cuisine)) }
                )
            }
        }
    }
}

@Composable
private fun StepGoals(state: OnboardingState, viewModel: OnboardingViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(stringResource(R.string.onboarding_step_goals), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            stringResource(R.string.onboarding_goal_intro),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
        OutlinedTextField(
            value = state.targetWeight.toString(),
            onValueChange = { viewModel.updateData(targetWeight = it.toDoubleOrNull() ?: 0.0) },
            label = { Text(stringResource(R.string.onboarding_goal_weight_hint)) },
            modifier = Modifier.fillMaxWidth(),
            isError = state.targetWeight > 0 && state.targetWeight < 30,
            supportingText = {
                if (state.targetWeight > 0 && state.targetWeight < 30) {
                    Text(stringResource(R.string.onboarding_error_target_weight))
                }
            }
        )

        val goalLabel = when {
            state.targetWeight < state.weight -> stringResource(R.string.onboarding_goal_loss_summary)
            state.targetWeight > state.weight -> stringResource(R.string.onboarding_goal_gain_summary)
            else -> stringResource(R.string.onboarding_goal_maintain_summary)
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Text(
                text = goalLabel,
                modifier = Modifier.padding(20.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun StepSummary(state: OnboardingState) {
    val favoriteCuisineLabels = state.favoriteCuisines.map { cuisineLabel(it) }
    val dislikedCuisineLabels = state.dislikedCuisines.map { cuisineLabel(it) }
    val allergyLabels = state.allergies.toList().sorted()

    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.onboarding_step_summary), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(
            stringResource(R.string.onboarding_summary_intro),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )

        state.calculatedTargets?.let { targets ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${targets.calories.toInt()}",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = stringResource(R.string.onboarding_kcal_per_day),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        SummaryMini(stringResource(R.string.macros_protein), "${targets.protein.toInt()} ${stringResource(R.string.unit_g)}")
                        SummaryMini(stringResource(R.string.macros_fat), "${targets.fat.toInt()} ${stringResource(R.string.unit_g)}")
                        SummaryMini(stringResource(R.string.macros_carbs), "${targets.carbs.toInt()} ${stringResource(R.string.unit_g)}")
                    }
                }
            }
            if (state.allergies.isNotEmpty() || state.favoriteCuisines.isNotEmpty() || state.dislikedCuisines.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.onboarding_summary_preferences_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (allergyLabels.isNotEmpty()) {
                            Text(
                                text = stringResource(
                                    R.string.onboarding_summary_allergies,
                                    allergyLabels.joinToString()
                                ),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        if (state.favoriteCuisines.isNotEmpty()) {
                            Text(
                                text = stringResource(
                                    R.string.onboarding_summary_favorite_cuisines,
                                    favoriteCuisineLabels.joinToString()
                                ),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        if (state.dislikedCuisines.isNotEmpty()) {
                            Text(
                                text = stringResource(
                                    R.string.onboarding_summary_disliked_cuisines,
                                    dislikedCuisineLabels.joinToString()
                                ),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.onboarding_auto_open_profile))
            }
        }
    }
}

@Composable
private fun SummaryMini(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun cuisineLabel(cuisine: String): String {
    return when (cuisine.uppercase()) {
        "RUSSIAN" -> stringResource(R.string.cuisine_russian)
        "ITALIAN" -> stringResource(R.string.cuisine_italian)
        "ASIAN" -> stringResource(R.string.cuisine_asian)
        "MEDITERRANEAN" -> stringResource(R.string.cuisine_mediterranean)
        "MEXICAN" -> stringResource(R.string.cuisine_mexican)
        "GEORGIAN" -> stringResource(R.string.cuisine_georgian)
        else -> cuisine
    }
}
