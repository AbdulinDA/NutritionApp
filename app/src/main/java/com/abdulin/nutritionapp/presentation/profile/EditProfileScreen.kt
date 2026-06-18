package com.abdulin.nutritionapp.presentation.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.abdulin.nutritionapp.R
import com.abdulin.nutritionapp.core.utils.SupportedUiActivityLevels
import com.abdulin.nutritionapp.core.utils.normalizeUiActivityLevel
import com.abdulin.nutritionapp.core.utils.toActivityLabelRes

private const val GOAL_LOSS_ID = 1L
private const val GOAL_GAIN_ID = 2L
private const val GOAL_MAINTAIN_ID = 3L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    viewModel: EditProfileViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onOpenFoodPreferences: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var targetWeight by remember { mutableStateOf("") }
    var activityLevel by remember { mutableStateOf("MODERATE") }
    var goalTypeId by remember { mutableStateOf<Long?>(null) }

    val activityLevels = SupportedUiActivityLevels
    val goalOptions = listOf(
        GOAL_LOSS_ID to stringResource(R.string.goal_loss),
        GOAL_GAIN_ID to stringResource(R.string.goal_gain),
        GOAL_MAINTAIN_ID to stringResource(R.string.goal_maintain)
    )
    val trimmedFirstName = firstName.trim()
    val parsedWeight = weight.toDoubleOrNull()
    val parsedTargetWeight = targetWeight.toDoubleOrNull()
    val isFirstNameValid = trimmedFirstName.isNotEmpty()
    val isWeightValid = parsedWeight != null && parsedWeight > 0.0
    val isTargetWeightValid = parsedTargetWeight != null && parsedTargetWeight > 0.0
    val isFormValid = isFirstNameValid && isWeightValid && isTargetWeightValid
    val hasChanges by remember(
        state.user,
        firstName,
        lastName,
        weight,
        targetWeight,
        goalTypeId,
        activityLevel
    ) {
        derivedStateOf {
            state.user?.let { user ->
                user.firstName != trimmedFirstName ||
                user.lastName.orEmpty() != lastName.trim() ||
                user.weightKg != parsedWeight ||
                user.targetWeightKg != parsedTargetWeight ||
                user.goalTypeId != goalTypeId ||
                (user.activityLevel ?: "MODERATE").normalizeUiActivityLevel() != activityLevel
            } ?: false
        }
    }

    fun submitProfile() {
        if (!isFormValid) return
        val validWeight = parsedWeight
        val validTargetWeight = parsedTargetWeight
        viewModel.updateProfile(
            firstName = trimmedFirstName,
            lastName = lastName.trim(),
            weight = validWeight ?: return,
            targetWeight = validTargetWeight ?: return,
            goalTypeId = goalTypeId,
            activityLevel = activityLevel
        )
    }

    LaunchedEffect(state.user) {
        state.user?.let {
            firstName = it.firstName
            lastName = it.lastName.orEmpty()
            weight = it.weightKg?.toString().orEmpty()
            targetWeight = it.targetWeightKg?.toString().orEmpty()
            goalTypeId = it.goalTypeId ?: deriveGoalTypeId(it.weightKg ?: 0.0, it.targetWeightKg ?: it.weightKg ?: 0.0)
            activityLevel = (it.activityLevel ?: "MODERATE").normalizeUiActivityLevel()
        }
    }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            onBack()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.profile_edit), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.onboarding_back)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = ::submitProfile,
                        enabled = !state.isLoading && isFormValid && hasChanges
                    ) {
                        Icon(Icons.Default.Check, contentDescription = stringResource(R.string.save))
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
            val maxWidth = maxWidth
            if (state.isLoading && state.user == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(if (maxWidth > 600.dp) 0.7f else 1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = { Text(stringResource(R.string.onboarding_name_hint)) },
                        modifier = Modifier.fillMaxWidth(),
                        isError = !isFirstNameValid,
                        supportingText = {
                            if (!isFirstNameValid) {
                                Text(stringResource(R.string.profile_error_first_name_required))
                            }
                        }
                    )

                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = { Text(stringResource(R.string.profile_last_name)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = weight,
                        onValueChange = { weight = it },
                        label = { Text(stringResource(R.string.onboarding_weight)) },
                        modifier = Modifier.fillMaxWidth(),
                        isError = !isWeightValid,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        ),
                        supportingText = {
                            if (!isWeightValid) {
                                Text(stringResource(R.string.profile_error_weight_invalid))
                            }
                        }
                    )

                    OutlinedTextField(
                        value = targetWeight,
                        onValueChange = { targetWeight = it },
                        label = { Text(stringResource(R.string.onboarding_target_weight)) },
                        modifier = Modifier.fillMaxWidth(),
                        isError = !isTargetWeightValid,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        ),
                        supportingText = {
                            if (!isTargetWeightValid) {
                                Text(stringResource(R.string.profile_error_target_weight_invalid))
                            } else {
                                Text(stringResource(R.string.profile_target_weight_edit_hint))
                            }
                        }
                    )

                    Text(
                        stringResource(R.string.onboarding_step_goals),
                        style = MaterialTheme.typography.titleSmall
                    )
                    goalOptions.forEach { (goalId, goalLabel) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = goalTypeId == goalId,
                                onClick = { goalTypeId = goalId }
                            )
                            Text(
                                text = goalLabel,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }

                    Button(
                        onClick = onOpenFoodPreferences,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.settings_food_preferences_title))
                    }

                    Text(
                        stringResource(R.string.onboarding_activity_label),
                        style = MaterialTheme.typography.titleSmall
                    )
                    activityLevels.forEach { level ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = activityLevel == level,
                                onClick = { activityLevel = level }
                            )
                            Text(
                                text = stringResource(level.toActivityLabelRes()),
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }

                    if (state.error != null) {
                        Text(
                            text = state.error!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = ::submitProfile,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !state.isLoading && isFormValid && hasChanges
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(stringResource(R.string.save))
                        }
                    }
                }
            }
        }
    }
}

private fun deriveGoalTypeId(currentWeight: Double, targetWeight: Double): Long {
    return when {
        targetWeight < currentWeight -> GOAL_LOSS_ID
        targetWeight > currentWeight -> GOAL_GAIN_ID
        else -> GOAL_MAINTAIN_ID
    }
}
