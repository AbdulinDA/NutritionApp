package com.abdulin.nutritionapp.presentation.recipe

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.abdulin.nutritionapp.R
import com.abdulin.nutritionapp.domain.model.RecommendedRecipeModel
import com.abdulin.nutritionapp.domain.model.RecipeModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeSearchScreen(
    viewModel: RecipeSearchViewModel = hiltViewModel(),
    onRecipeClick: (Long, String?, String?, Long?, Double?) -> Unit,
    onRecipeSelected: ((RecipeModel) -> Unit)? = null,
    onGeneratePlanClick: () -> Unit,
    bottomBarPadding: androidx.compose.ui.unit.Dp = 0.dp,
    initialMealTypeFilter: String? = null,
    onBack: (() -> Unit)? = null
) {
    val state by viewModel.state.collectAsState()
    val recipes = viewModel.recipesPagingData.collectAsLazyPagingItems()
    val categories = listOf<String?>(null, "BREAKFAST", "LUNCH", "DINNER", "SNACK")
    val nutritionFilters = listOf(
        NutritionFilter.ALL,
        NutritionFilter.COMPLETE,
        NutritionFilter.ESTIMATED,
        NutritionFilter.REQUIRES_SIDE_DISH,
        NutritionFilter.INCOMPLETE
    )

    LaunchedEffect(initialMealTypeFilter) {
        if (initialMealTypeFilter != null) {
            viewModel.applyInitialMealType(initialMealTypeFilter)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.onboarding_back)
                            )
                        }
                    }
                },
                title = {
                    OutlinedTextField(
                        value = state.query,
                        onValueChange = viewModel::onQueryChange,
                        placeholder = { Text(stringResource(R.string.recipes_search_placeholder)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 8.dp),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null
                            )
                        },
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium
                    )
                },
                actions = {
                    IconButton(onClick = onGeneratePlanClick) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = stringResource(R.string.recipes_generate_plan),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(bottom = bottomBarPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            val contentModifier = Modifier.fillMaxWidth(if (maxWidth > 600.dp) 0.8f else 1f)

            Column(modifier = contentModifier) {
                LazyRow(
                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        FilterChip(
                            selected = state.selectedMealType == category,
                            onClick = { viewModel.onMealTypeChange(category) },
                            label = {
                                val label = when (category) {
                                    null -> stringResource(R.string.recipes_filter_all)
                                    "BREAKFAST" -> stringResource(R.string.diary_meal_breakfast)
                                    "LUNCH" -> stringResource(R.string.diary_meal_lunch)
                                    "DINNER" -> stringResource(R.string.diary_meal_dinner)
                                    else -> stringResource(R.string.diary_meal_snack)
                                }
                                Text(label)
                            }
                        )
                    }
                }

                LazyRow(
                    modifier = Modifier.padding(bottom = 8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(nutritionFilters) { filter ->
                        FilterChip(
                            selected = state.selectedNutritionFilter == filter,
                            onClick = { viewModel.onNutritionFilterChange(filter) },
                            label = {
                                Text(
                                    when (filter) {
                                        NutritionFilter.ALL -> stringResource(R.string.recipe_filter_quality_all)
                                        NutritionFilter.COMPLETE -> stringResource(R.string.recipe_filter_quality_complete)
                                        NutritionFilter.ESTIMATED -> stringResource(R.string.recipe_filter_quality_estimated)
                                        NutritionFilter.REQUIRES_SIDE_DISH -> stringResource(R.string.recipe_filter_quality_side_dish)
                                        NutritionFilter.INCOMPLETE -> stringResource(R.string.recipe_filter_quality_incomplete)
                                    }
                                )
                            }
                        )
                    }
                }

                if (state.isLoading || recipes.loadState.refresh is LoadState.Loading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                item {
                    SmartPlanCard(onGeneratePlanClick = onGeneratePlanClick)
                }

                    if (state.query.isBlank()) {
                        item {
                            Text(
                                text = stringResource(R.string.recipes_browse_hint),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    val pagingError = (recipes.loadState.refresh as? LoadState.Error)?.error?.message
                        ?: (recipes.loadState.append as? LoadState.Error)?.error?.message

                    if (state.error != null && recipes.itemCount == 0 && recipes.loadState.refresh !is LoadState.Loading) {
                        item {
                            RecipesStateCard(
                                title = stringResource(R.string.recipes_error_title),
                                subtitle = state.error ?: stringResource(R.string.recipes_error_subtitle),
                                actionLabel = stringResource(R.string.retry),
                                onAction = {
                                    viewModel.retry()
                                    recipes.retry()
                                }
                            )
                        }
                    } else if (pagingError != null && state.recommendedRecipes.isEmpty() && recipes.itemCount == 0 && recipes.loadState.refresh !is LoadState.Loading) {
                        item {
                            RecipesStateCard(
                                title = stringResource(R.string.recipes_error_title),
                                subtitle = pagingError,
                                actionLabel = stringResource(R.string.retry),
                                onAction = {
                                    viewModel.retry()
                                    recipes.retry()
                                }
                            )
                        }
                    } else if (recipes.itemCount == 0 && state.recommendedRecipes.isEmpty() && !state.isLoading && recipes.loadState.refresh !is LoadState.Loading) {
                        item {
                            RecipesStateCard(
                                title = stringResource(R.string.recipes_empty_title),
                                subtitle = stringResource(R.string.recipes_empty_subtitle),
                                actionLabel = stringResource(R.string.recipes_plan_cta),
                                onAction = onGeneratePlanClick
                            )
                        }
                    }

                    if (state.recommendedRecipes.isNotEmpty()) {
                        item {
                            SectionHeader(
                                icon = Icons.Default.AutoAwesome,
                                title = stringResource(R.string.recipes_personalized_title)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.recipes_personalized_subtitle),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        items(state.recommendedRecipes) { recommendation ->
                            RecommendedRecipeCard(
                                recommendation = recommendation,
                                onClick = {
                                    onRecipeSelected?.invoke(recommendation.recipe)
                                        ?: onRecipeClick(
                                            recommendation.recipe.id,
                                            "smart_recommendation",
                                            recommendation.reason,
                                            recommendation.impressionId,
                                            null
                                        )
                                }
                            )
                        }
                    }

                    if (state.selectedMealType != null && state.recommendedRecipes.isEmpty() && recipes.itemCount > 0) {
                        item {
                            Text(
                                text = stringResource(R.string.recipes_regular_results_hint),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    if (state.recommendedRecipes.isNotEmpty() && recipes.itemCount > 0) {
                        item {
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.recipes_more_results_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.recipes_more_results_subtitle),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    items(recipes.itemCount) { index ->
                        val recipe = recipes[index] ?: return@items
                        RecipeCard(
                            recipe = recipe,
                            onClick = {
                                onRecipeSelected?.invoke(recipe)
                                    ?: onRecipeClick(recipe.id, null, null, null, null)
                            }
                        )
                    }

                    if (recipes.loadState.append is LoadState.Loading) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                LinearProgressIndicator(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                )
                            }
                        }
                    }

                    val appendError = recipes.loadState.append as? LoadState.Error
                    if (appendError != null) {
                        item {
                            RecipesStateCard(
                                title = stringResource(R.string.recipes_error_title),
                                subtitle = appendError.error.message ?: stringResource(R.string.recipes_error_subtitle),
                                actionLabel = stringResource(R.string.retry),
                                onAction = recipes::retry
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SmartPlanCard(onGeneratePlanClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.recipes_smart_plan_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = stringResource(R.string.recipes_smart_plan_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FilledTonalButton(onClick = onGeneratePlanClick) {
                Text(stringResource(R.string.recipes_plan_cta))
            }
        }
    }
}

@Composable
private fun SectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
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
private fun RecipesStateCard(
    title: String,
    subtitle: String,
    actionLabel: String,
    onAction: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
fun RecipeCard(recipe: RecipeModel, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            RecipeImage(
                imageUrl = recipe.imageUrl,
                contentDescription = recipe.title,
                recipeTitle = recipe.title,
                mealType = recipe.mealType,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                RecipeNutritionStatusBadges(recipe)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${recipe.totalCalories.toInt()} ${stringResource(R.string.unit_kcal)}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "${recipe.cookTimeMin} ${stringResource(R.string.recipe_cook_time)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
fun RecommendedRecipeCard(
    recommendation: RecommendedRecipeModel,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
        )
    ) {
        Column {
            RecipeImage(
                imageUrl = recommendation.recipe.imageUrl,
                contentDescription = recommendation.recipe.title,
                recipeTitle = recommendation.recipe.title,
                mealType = recommendation.recipe.mealType,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AssistChip(
                            onClick = onClick,
                            label = { Text(stringResource(R.string.recipes_personalized_badge)) }
                        )
                    }
                    recommendation.score?.let { score ->
                        Text(
                            text = stringResource(R.string.recipes_match_score, score.toInt()),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = recommendation.recipe.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                RecipeNutritionStatusBadges(recommendation.recipe)
                recommendation.reason?.takeIf { it.isNotBlank() }?.let { reason ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = reason,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (recommendation.explanationTags.isNotEmpty()) {
                    val localizedTags = recommendation.explanationTags
                        .take(2)
                        .map { tag ->
                            tag.toReadableTagRes()?.let { stringResource(it) }
                                ?: tag.replace('_', ' ').replaceFirstChar { it.titlecase(Locale.ROOT) }
                        }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = localizedTags.joinToString(" • "),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${recommendation.recipe.totalCalories.toInt()} ${stringResource(R.string.unit_kcal)}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "${recommendation.recipe.cookTimeMin} ${stringResource(R.string.recipe_cook_time)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
private fun RecipeNutritionStatusBadges(recipe: RecipeModel) {
    val status = recipe.nutritionCalculationStatus
        ?.trim()
        ?.uppercase(Locale.ROOT)
        .orEmpty()
    val badges = buildList {
        when (status) {
            "ESTIMATED" -> add(
                Triple(
                    stringResource(R.string.recipe_nutrition_status_estimated_short),
                    MaterialTheme.colorScheme.tertiaryContainer,
                    MaterialTheme.colorScheme.onTertiaryContainer
                )
            )
            "REQUIRES_SIDE_DISH" -> add(
                Triple(
                    stringResource(R.string.recipe_nutrition_status_side_dish_short),
                    MaterialTheme.colorScheme.primaryContainer,
                    MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
            "INCOMPLETE" -> add(
                Triple(
                    stringResource(R.string.recipe_nutrition_status_incomplete_short),
                    MaterialTheme.colorScheme.errorContainer,
                    MaterialTheme.colorScheme.onErrorContainer
                )
            )
        }
        if (recipe.requiresSideDish && status != "REQUIRES_SIDE_DISH") {
            add(
                Triple(
                    stringResource(R.string.recipe_requires_side_dish_short),
                    MaterialTheme.colorScheme.secondaryContainer,
                    MaterialTheme.colorScheme.onSecondaryContainer
                )
            )
        }
    }

    if (badges.isEmpty()) return

    Spacer(modifier = Modifier.height(8.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        badges.forEach { (label, containerColor, contentColor) ->
            Surface(
                color = containerColor,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = label,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = contentColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

private fun String.toReadableTagRes(): Int? {
    return when (this.lowercase(Locale.ROOT)) {
        "protein_gap" -> R.string.recipe_tag_protein_gap
        "goal_fit" -> R.string.recipe_tag_goal_fit
        "calorie_fit" -> R.string.recipe_tag_calorie_fit
        "favorite_cuisine" -> R.string.recipe_tag_favorite_cuisine
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


