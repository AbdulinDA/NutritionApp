package com.abdulin.nutritionapp.presentation.diary

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.abdulin.nutritionapp.R
import com.abdulin.nutritionapp.core.utils.Resource
import com.abdulin.nutritionapp.core.utils.labelRes
import com.abdulin.nutritionapp.data.dto.diary.CreateFoodDiaryRequestDto
import com.abdulin.nutritionapp.domain.model.MealType
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.delay

private enum class AddMode { PRODUCT, RECIPE, MANUAL }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFoodScreen(
    viewModel: AddFoodViewModel = hiltViewModel(),
    initialProductId: String? = null,
    initialProductName: String? = null,
    initialRecipeId: String? = null,
    initialRecipeName: String? = null,
    onNavigateToSearch: () -> Unit,
    onNavigateToRecipeSearch: () -> Unit,
    onBack: () -> Unit
) {
    var productId by rememberSaveable { mutableStateOf(initialProductId ?: "") }
    var productName by rememberSaveable { mutableStateOf(initialProductName ?: "") }
    var recipeId by rememberSaveable { mutableStateOf(initialRecipeId ?: "") }
    var recipeName by rememberSaveable { mutableStateOf(initialRecipeName ?: "") }
    var weight by rememberSaveable { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var mode by rememberSaveable { mutableStateOf(AddMode.PRODUCT) }
    var customName by rememberSaveable { mutableStateOf("") }
    var calories by rememberSaveable { mutableStateOf("") }
    var protein by rememberSaveable { mutableStateOf("") }
    var fat by rememberSaveable { mutableStateOf("") }
    var carbs by rememberSaveable { mutableStateOf("") }
    var submitAttempted by rememberSaveable { mutableStateOf(false) }
    var submissionStarted by rememberSaveable { mutableStateOf(false) }
    var showSaveTemplateDialog by rememberSaveable { mutableStateOf(false) }
    var templateName by rememberSaveable { mutableStateOf("") }

    val mealTypes = MealType.entries
    var selectedMealType by rememberSaveable { mutableStateOf(mealTypes[0]) }

    val uiState by viewModel.state.collectAsState()
    val submissionState = uiState.submissionState
    val defaultProductName = stringResource(R.string.add_food_product)
    val defaultRecipeName = stringResource(R.string.nav_recipes)
    val defaultSavedProductTemplateName = stringResource(R.string.saved_meal_default_product_name)
    val defaultSavedRecipeTemplateName = stringResource(R.string.saved_meal_default_recipe_name)
    val defaultSavedCustomTemplateName = stringResource(R.string.saved_meal_default_custom_name)
    val parsedWeight = weight.toDoubleOrNull()
    val parsedCalories = calories.toDoubleOrNull()
    val parsedProtein = protein.toDoubleOrNull() ?: 0.0
    val parsedFat = fat.toDoubleOrNull() ?: 0.0
    val parsedCarbs = carbs.toDoubleOrNull() ?: 0.0
    val isWeightValid = parsedWeight != null && parsedWeight > 0.0
    val isProductValid = productId.isNotBlank()
    val isRecipeValid = recipeId.isNotBlank()
    val isCustomNameValid = customName.trim().isNotBlank()
    val isCaloriesValid = parsedCalories != null && parsedCalories > 0.0
    val canSubmit = submissionState !is Resource.Loading && when (mode) {
        AddMode.PRODUCT -> isProductValid && isWeightValid
        AddMode.RECIPE -> isRecipeValid
        AddMode.MANUAL -> isCustomNameValid && isCaloriesValid && isWeightValid
    }
    val currentRequest = remember(
        mode,
        productId,
        productName,
        recipeId,
        recipeName,
        customName,
        parsedCalories,
        parsedProtein,
        parsedFat,
        parsedCarbs,
        parsedWeight,
        selectedMealType
    ) {
        when (mode) {
            AddMode.PRODUCT -> parsedWeight?.takeIf { productId.isNotBlank() }?.let {
                CreateFoodDiaryRequestDto(
                    mealType = selectedMealType.name,
                    source = "PRODUCT",
                    productId = productId.toLongOrNull(),
                    customName = productName.ifBlank { null },
                    weightGrams = it,
                    entryDate = "template",
                    consumedAt = "template"
                )
            }
            AddMode.RECIPE -> recipeId.toLongOrNull()?.let {
                CreateFoodDiaryRequestDto(
                    mealType = selectedMealType.name,
                    source = "RECIPE",
                    recipeId = it,
                    customName = recipeName.ifBlank { null },
                    weightGrams = 300.0,
                    entryDate = "template",
                    consumedAt = "template"
                )
            }
            AddMode.MANUAL -> if (isCustomNameValid && isCaloriesValid && isWeightValid) {
                CreateFoodDiaryRequestDto(
                    mealType = selectedMealType.name,
                    source = "CUSTOM",
                    customName = customName.trim(),
                    calories = parsedCalories,
                    protein = parsedProtein,
                    fat = parsedFat,
                    carbs = parsedCarbs,
                    weightGrams = parsedWeight ?: 0.0,
                    entryDate = "template",
                    consumedAt = "template"
                )
            } else null
        }
    }
    val showProductError = mode == AddMode.PRODUCT && submitAttempted && !isProductValid
    val showRecipeError = mode == AddMode.RECIPE && submitAttempted && !isRecipeValid
    val showNameError = mode == AddMode.MANUAL && customName.isNotEmpty() && !isCustomNameValid
    val showCaloriesError = mode == AddMode.MANUAL && calories.isNotEmpty() && !isCaloriesValid
    val showWeightError = mode != AddMode.RECIPE && (weight.isNotEmpty() || submitAttempted) && !isWeightValid

    LaunchedEffect(initialProductId, initialProductName) {
        if (initialProductId != null) {
            productId = initialProductId
            mode = AddMode.PRODUCT
        }
        if (initialProductName != null) productName = initialProductName
    }

    LaunchedEffect(initialRecipeId, initialRecipeName) {
        if (initialRecipeId != null) {
            recipeId = initialRecipeId
            mode = AddMode.RECIPE
        }
        if (initialRecipeName != null) recipeName = initialRecipeName
    }

    LaunchedEffect(submissionState, submissionStarted) {
        if (submissionStarted && submissionState is Resource.Success && submissionState.data != null) {
            delay(800)
            submissionStarted = false
            viewModel.resetState()
            onBack()
        }
    }

    LaunchedEffect(uiState.templateMessage) {
        if (uiState.templateMessage != null) {
            delay(1800)
            viewModel.clearTemplateMessage()
        }
    }

    if (showSaveTemplateDialog) {
        AlertDialog(
            onDismissRequest = { showSaveTemplateDialog = false },
            title = { Text(stringResource(R.string.saved_meal_dialog_title)) },
            text = {
                OutlinedTextField(
                    value = templateName,
                    onValueChange = { templateName = it },
                    label = { Text(stringResource(R.string.saved_meal_name_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val request = currentRequest ?: return@TextButton
                        viewModel.saveCurrentAsTemplate(templateName, request)
                        showSaveTemplateDialog = false
                        templateName = ""
                    }
                ) {
                    Text(stringResource(R.string.saved_meal_save_action))
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveTemplateDialog = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        when (mode) {
                            AddMode.PRODUCT -> stringResource(R.string.add_food_title)
                            AddMode.RECIPE -> stringResource(R.string.add_food_recipe_title)
                            AddMode.MANUAL -> stringResource(R.string.add_food_custom_title)
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
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
            if (uiState.savedMeals.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.saved_meals_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.saved_meals_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(uiState.savedMeals.size) { index ->
                        val template = uiState.savedMeals[index]
                        SavedMealTemplateCard(
                            templateName = template.templateName,
                            mealTypeLabel = stringResource(
                                MealType.valueOf(template.mealType).labelRes()
                            ),
                            onUse = {
                                submissionStarted = true
                                viewModel.addSavedMeal(template)
                            },
                            onDelete = { viewModel.deleteSavedMealTemplate(template.id) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = mode == AddMode.PRODUCT,
                    onClick = {
                        mode = AddMode.PRODUCT
                        submitAttempted = false
                    },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
                ) {
                    Text(stringResource(R.string.add_food_product_mode))
                }
                SegmentedButton(
                    selected = mode == AddMode.RECIPE,
                    onClick = {
                        mode = AddMode.RECIPE
                        submitAttempted = false
                    },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
                ) {
                    Text(stringResource(R.string.add_food_recipe_mode))
                }
                SegmentedButton(
                    selected = mode == AddMode.MANUAL,
                    onClick = {
                        mode = AddMode.MANUAL
                        submitAttempted = false
                    },
                    shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
                ) {
                    Text(stringResource(R.string.add_food_manual_mode))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = stringResource(selectedMealType.labelRes()),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.add_food_meal_type)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    mealTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(stringResource(type.labelRes())) },
                            onClick = {
                                selectedMealType = type
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (mode == AddMode.PRODUCT) {
                FilledTonalButton(
                    onClick = onNavigateToSearch,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.search_products_title))
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = productName,
                    onValueChange = { productName = it },
                    label = { Text(stringResource(R.string.add_food_product)) },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        if (productId.isNotBlank()) {
                            TextButton(
                                onClick = {
                                    productId = ""
                                    productName = ""
                                    submitAttempted = false
                                }
                            ) {
                                Text(stringResource(R.string.clear))
                            }
                        }
                    },
                    isError = showProductError,
                    supportingText = {
                        when {
                            showProductError -> Text(stringResource(R.string.add_food_error_product))
                            productId.isBlank() -> Text(stringResource(R.string.add_food_product_placeholder))
                            else -> Text(stringResource(R.string.add_food_submit))
                        }
                    },
                    placeholder = { Text(stringResource(R.string.add_food_product_placeholder)) }
                )
            } else if (mode == AddMode.RECIPE) {
                FilledTonalButton(
                    onClick = onNavigateToRecipeSearch,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.add_food_search_recipes))
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = recipeName,
                    onValueChange = { recipeName = it },
                    label = { Text(stringResource(R.string.nav_recipes)) },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        if (recipeId.isNotBlank()) {
                            TextButton(
                                onClick = {
                                    recipeId = ""
                                    recipeName = ""
                                    submitAttempted = false
                                }
                            ) {
                                Text(stringResource(R.string.clear))
                            }
                        }
                    },
                    isError = showRecipeError,
                    supportingText = {
                        when {
                            showRecipeError -> Text(stringResource(R.string.add_food_error_recipe))
                            recipeId.isBlank() -> Text(stringResource(R.string.add_food_recipe_placeholder))
                            else -> Text(stringResource(R.string.add_food_recipe_no_weight))
                        }
                    },
                    placeholder = { Text(stringResource(R.string.add_food_recipe_placeholder)) }
                )
            } else {
                OutlinedTextField(
                    value = customName,
                    onValueChange = { customName = it },
                    label = { Text(stringResource(R.string.add_food_custom_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    isError = showNameError,
                    supportingText = {
                        if (showNameError) {
                            Text(stringResource(R.string.add_food_error_name))
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                Text(stringResource(R.string.add_food_nutrition_title), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = calories,
                        onValueChange = { calories = it },
                        label = { Text(stringResource(R.string.add_food_calories)) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        isError = showCaloriesError,
                        supportingText = {
                            if (showCaloriesError) {
                                Text(stringResource(R.string.add_food_error_calories))
                            }
                        }
                    )
                    OutlinedTextField(
                        value = protein,
                        onValueChange = { protein = it },
                        label = { Text(stringResource(R.string.add_food_protein)) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = fat,
                        onValueChange = { fat = it },
                        label = { Text(stringResource(R.string.add_food_fat)) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = carbs,
                        onValueChange = { carbs = it },
                        label = { Text(stringResource(R.string.add_food_carbs)) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            if (mode != AddMode.RECIPE) {
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = {
                        Text(
                            if (mode == AddMode.MANUAL) stringResource(R.string.add_food_portion_weight)
                            else stringResource(R.string.add_food_weight)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    isError = showWeightError,
                    supportingText = {
                        if (showWeightError) {
                            Text(stringResource(R.string.add_food_error_weight))
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedButton(
                onClick = {
                    templateName = when (mode) {
                        AddMode.PRODUCT -> productName.ifBlank { defaultSavedProductTemplateName }
                        AddMode.RECIPE -> recipeName.ifBlank { defaultSavedRecipeTemplateName }
                        AddMode.MANUAL -> customName.ifBlank { defaultSavedCustomTemplateName }
                    }
                    showSaveTemplateDialog = true
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = currentRequest != null && submissionState !is Resource.Loading,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.BookmarkAdd, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.saved_meal_save_button))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    submitAttempted = true
                    if (!canSubmit) return@Button
                    submissionStarted = true

                    if (mode == AddMode.MANUAL) {
                        viewModel.addCustomFood(
                            name = customName.trim(),
                            calories = parsedCalories ?: return@Button,
                            protein = parsedProtein,
                            fat = parsedFat,
                            carbs = parsedCarbs,
                            weight = parsedWeight ?: return@Button,
                            mealType = selectedMealType.name
                        )
                    } else if (mode == AddMode.PRODUCT) {
                        viewModel.addFood(
                            productId = productId.toLongOrNull() ?: 0,
                            productName = productName.ifBlank { defaultProductName },
                            weight = parsedWeight ?: return@Button,
                            mealType = selectedMealType.name
                        )
                    } else {
                        viewModel.addRecipe(
                            recipeId = recipeId.toLongOrNull() ?: 0L,
                            recipeName = recipeName.ifBlank { defaultRecipeName },
                            mealType = selectedMealType.name
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = canSubmit
            ) {
                if (submissionState is Resource.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                } else if (submissionState is Resource.Success && submissionState.data != null) {
                    Text(stringResource(R.string.add_food_done))
                } else {
                    Text(stringResource(R.string.add_food_submit), fontWeight = FontWeight.Bold)
                }
            }

            if (submissionState is Resource.Error) {
                Text(
                    text = submissionState.message ?: stringResource(R.string.add_food_error_save),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 12.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            uiState.templateMessage?.let { message ->
                Text(
                    text = when (message) {
                        "template_saved" -> stringResource(R.string.saved_meal_saved)
                        "template_deleted" -> stringResource(R.string.saved_meal_deleted)
                        "empty_template_name" -> stringResource(R.string.saved_meal_empty_name)
                        else -> stringResource(R.string.saved_meal_save_failed)
                    },
                    color = if (message == "template_saved" || message == "template_deleted") {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                    modifier = Modifier.padding(top = 12.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun SavedMealTemplateCard(
    templateName: String,
    mealTypeLabel: String,
    onUse: () -> Unit,
    onDelete: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.width(220.dp),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = templateName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = mealTypeLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalButton(onClick = onUse, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.FlashOn, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.saved_meal_use_action))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = stringResource(R.string.saved_meal_delete_action))
                }
            }
        }
    }
}
