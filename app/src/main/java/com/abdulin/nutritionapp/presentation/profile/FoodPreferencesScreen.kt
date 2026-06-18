package com.abdulin.nutritionapp.presentation.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HeartBroken
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.NoFood
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.abdulin.nutritionapp.R
import com.abdulin.nutritionapp.domain.model.ProductModel

private enum class FoodPreferenceMode {
    FAVORITE,
    EXCLUDED,
    ALLERGY,
    CUISINE,
    DISLIKED_CUISINE
}

private val FavoriteCuisineOptions = listOf(
    "RUSSIAN",
    "ITALIAN",
    "ASIAN",
    "MEDITERRANEAN",
    "MEXICAN",
    "GEORGIAN"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodPreferencesScreen(
    viewModel: FoodPreferencesViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onOpenFridge: (() -> Unit)? = null
) {
    val state by viewModel.state.collectAsState()
    val products = viewModel.productsPagingData.collectAsLazyPagingItems()
    var mode by rememberSaveable { mutableStateOf(FoodPreferenceMode.EXCLUDED) }

    val selectedProducts = when (mode) {
        FoodPreferenceMode.FAVORITE -> state.favoriteProducts
        FoodPreferenceMode.EXCLUDED -> state.excludedProducts
        FoodPreferenceMode.ALLERGY -> state.allergyProducts
        FoodPreferenceMode.CUISINE -> emptyList()
        FoodPreferenceMode.DISLIKED_CUISINE -> emptyList()
    }.distinctBy { it.name.trim().lowercase() }
    val selectedIds = remember(selectedProducts) { selectedProducts.map { it.id }.toSet() }
    val selectedNames = remember(selectedProducts) { selectedProducts.map { it.name.trim().lowercase() }.toSet() }
    val selectedCuisines = state.favoriteCuisines
    val selectedDislikedCuisines = state.dislikedCuisines

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.food_preferences_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.onboarding_back)
                        )
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
            val contentModifier = Modifier.fillMaxWidth(if (maxWidth > 600.dp) 0.82f else 1f)

            LazyColumn(
                modifier = contentModifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = mode == FoodPreferenceMode.FAVORITE,
                                    onClick = { mode = FoodPreferenceMode.FAVORITE },
                                    label = { Text(stringResource(R.string.food_preferences_mode_favorite)) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                )
                                FilterChip(
                                    selected = mode == FoodPreferenceMode.EXCLUDED,
                                    onClick = { mode = FoodPreferenceMode.EXCLUDED },
                                    label = { Text(stringResource(R.string.food_preferences_mode_excluded)) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.NoFood,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                )
                                FilterChip(
                                    selected = mode == FoodPreferenceMode.ALLERGY,
                                    onClick = { mode = FoodPreferenceMode.ALLERGY },
                                    label = { Text(stringResource(R.string.food_preferences_mode_allergy)) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.HeartBroken,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                )
                                FilterChip(
                                    selected = mode == FoodPreferenceMode.CUISINE,
                                    onClick = { mode = FoodPreferenceMode.CUISINE },
                                    label = { Text(stringResource(R.string.food_preferences_mode_cuisine)) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Public,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                )
                                FilterChip(
                                    selected = mode == FoodPreferenceMode.DISLIKED_CUISINE,
                                    onClick = { mode = FoodPreferenceMode.DISLIKED_CUISINE },
                                    label = { Text(stringResource(R.string.food_preferences_mode_disliked_cuisine)) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                )
                            }
                            Text(
                                text = stringResource(R.string.food_preferences_local_hint),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = stringResource(modeDescriptionRes(mode)),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (onOpenFridge != null) {
                                FilledTonalButton(
                                    onClick = onOpenFridge,
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Kitchen,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = stringResource(R.string.food_preferences_open_fridge),
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                if (state.isSyncing) {
                    item {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }

                state.error?.let { errorMessage ->
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = errorMessage,
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                IconButton(onClick = viewModel::clearError) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = stringResource(R.string.food_preferences_remove),
                                        tint = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }
                }

                if (mode != FoodPreferenceMode.CUISINE && mode != FoodPreferenceMode.DISLIKED_CUISINE) {
                    item {
                        OutlinedTextField(
                            value = state.searchQuery,
                            onValueChange = viewModel::onSearchQueryChange,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            placeholder = { Text(stringResource(R.string.food_preferences_search_placeholder)) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
                        )
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = stringResource(
                                if (mode == FoodPreferenceMode.CUISINE || mode == FoodPreferenceMode.DISLIKED_CUISINE) {
                                    R.string.food_preferences_selected_cuisines_title
                                } else {
                                    R.string.food_preferences_selected_title
                                }
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (mode == FoodPreferenceMode.CUISINE && selectedCuisines.isNotEmpty()) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                selectedCuisines.forEach { cuisine ->
                                    AssistChip(
                                        onClick = { viewModel.toggleFavoriteCuisine(cuisine) },
                                        label = { Text(cuisineLabel(cuisine)) },
                                        trailingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = stringResource(R.string.food_preferences_remove),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    )
                                }
                            }
                        } else if (mode == FoodPreferenceMode.DISLIKED_CUISINE && selectedDislikedCuisines.isNotEmpty()) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                selectedDislikedCuisines.forEach { cuisine ->
                                    AssistChip(
                                        onClick = { viewModel.toggleDislikedCuisine(cuisine) },
                                        label = { Text(cuisineLabel(cuisine)) },
                                        trailingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = stringResource(R.string.food_preferences_remove),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    )
                                }
                            }
                        } else if (selectedProducts.isEmpty()) {
                            Text(
                                text = stringResource(
                                    when (mode) {
                                        FoodPreferenceMode.FAVORITE -> R.string.food_preferences_empty_favorite
                                        FoodPreferenceMode.EXCLUDED -> R.string.food_preferences_empty_excluded
                                        FoodPreferenceMode.ALLERGY -> R.string.food_preferences_empty_allergy
                                        FoodPreferenceMode.CUISINE -> R.string.food_preferences_empty_cuisine
                                        FoodPreferenceMode.DISLIKED_CUISINE -> R.string.food_preferences_empty_disliked_cuisine
                                    }
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        } else {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(selectedProducts, key = { it.id }) { product ->
                                    AssistChip(
                                        onClick = {
                                            toggleModeProduct(
                                                mode = mode,
                                                product = product,
                                                onToggleFavorite = viewModel::toggleFavoriteProduct,
                                                onToggleExcluded = viewModel::toggleExcludedProduct,
                                                onToggleAllergy = viewModel::toggleAllergyProduct
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = product.name,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        },
                                        trailingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = stringResource(R.string.food_preferences_remove),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = stringResource(
                            if (mode == FoodPreferenceMode.CUISINE || mode == FoodPreferenceMode.DISLIKED_CUISINE) {
                                R.string.food_preferences_cuisine_catalog_title
                            } else {
                                R.string.food_preferences_results_title
                            }
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (mode == FoodPreferenceMode.CUISINE || mode == FoodPreferenceMode.DISLIKED_CUISINE) {
                    item {
                        Text(
                            text = stringResource(
                                if (mode == FoodPreferenceMode.CUISINE) {
                                    R.string.food_preferences_cuisine_hint
                                } else {
                                    R.string.food_preferences_disliked_cuisine_hint
                                }
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    item {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FavoriteCuisineOptions.forEach { cuisine ->
                                FilterChip(
                                    selected = if (mode == FoodPreferenceMode.CUISINE) {
                                        selectedCuisines.any { it.equals(cuisine, ignoreCase = true) }
                                    } else {
                                        selectedDislikedCuisines.any { it.equals(cuisine, ignoreCase = true) }
                                    },
                                    onClick = {
                                        if (mode == FoodPreferenceMode.CUISINE) {
                                            viewModel.toggleFavoriteCuisine(cuisine)
                                        } else {
                                            viewModel.toggleDislikedCuisine(cuisine)
                                        }
                                    },
                                    label = { Text(cuisineLabel(cuisine)) }
                                )
                            }
                        }
                    }
                }

                if (mode != FoodPreferenceMode.CUISINE && mode != FoodPreferenceMode.DISLIKED_CUISINE && products.loadState.refresh is LoadState.Loading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }

                if (mode != FoodPreferenceMode.CUISINE && mode != FoodPreferenceMode.DISLIKED_CUISINE) {
                    items(
                        count = products.itemCount,
                        key = products.itemKey { it.id }
                    ) { index ->
                        val product = products[index] ?: return@items
                        FoodPreferenceProductItem(
                            product = product,
                            selected = if (mode == FoodPreferenceMode.EXCLUDED) {
                                selectedIds.contains(product.id)
                            } else {
                                selectedIds.contains(product.id) || selectedNames.contains(product.name.trim().lowercase())
                            },
                            mode = mode,
                            onClick = {
                                toggleModeProduct(
                                    mode = mode,
                                    product = FoodPreferenceProduct(product.id, product.name),
                                    onToggleFavorite = viewModel::toggleFavoriteProduct,
                                    onToggleExcluded = viewModel::toggleExcludedProduct,
                                    onToggleAllergy = viewModel::toggleAllergyProduct
                                )
                            }
                        )
                    }
                }

                if (mode != FoodPreferenceMode.CUISINE && mode != FoodPreferenceMode.DISLIKED_CUISINE && products.loadState.append is LoadState.Loading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FoodPreferenceProductItem(
    product: ProductModel,
    selected: Boolean,
    mode: FoodPreferenceMode,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f)
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (selected) {
                        stringResource(R.string.food_preferences_remove)
                    } else {
                        stringResource(
                            when (mode) {
                                FoodPreferenceMode.FAVORITE -> R.string.food_preferences_add_favorite
                                FoodPreferenceMode.EXCLUDED -> R.string.food_preferences_add_excluded
                                FoodPreferenceMode.ALLERGY -> R.string.food_preferences_add_allergy
                                FoodPreferenceMode.CUISINE -> R.string.food_preferences_add_favorite
                                FoodPreferenceMode.DISLIKED_CUISINE -> R.string.food_preferences_add_excluded
                            }
                        )
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Icon(
                imageVector = if (selected) {
                    Icons.Default.CheckCircle
                } else if (mode == FoodPreferenceMode.EXCLUDED) {
                    Icons.Default.NoFood
                } else {
                    when (mode) {
                        FoodPreferenceMode.FAVORITE -> Icons.Default.Star
                        FoodPreferenceMode.EXCLUDED -> Icons.Default.NoFood
                        FoodPreferenceMode.ALLERGY -> Icons.Default.HeartBroken
                        FoodPreferenceMode.CUISINE -> Icons.Default.Public
                        FoodPreferenceMode.DISLIKED_CUISINE -> Icons.Default.Close
                    }
                },
                contentDescription = null,
                tint = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline
                }
            )
        }
    }
}

private fun toggleModeProduct(
    mode: FoodPreferenceMode,
    product: FoodPreferenceProduct,
    onToggleFavorite: (FoodPreferenceProduct) -> Unit,
    onToggleExcluded: (FoodPreferenceProduct) -> Unit,
    onToggleAllergy: (FoodPreferenceProduct) -> Unit
) {
    when (mode) {
        FoodPreferenceMode.FAVORITE -> onToggleFavorite(product)
        FoodPreferenceMode.EXCLUDED -> onToggleExcluded(product)
        FoodPreferenceMode.ALLERGY -> onToggleAllergy(product)
        FoodPreferenceMode.CUISINE -> Unit
        FoodPreferenceMode.DISLIKED_CUISINE -> Unit
    }
}

private fun modeDescriptionRes(mode: FoodPreferenceMode): Int {
    return when (mode) {
        FoodPreferenceMode.FAVORITE -> R.string.food_preferences_favorite_hint
        FoodPreferenceMode.EXCLUDED -> R.string.food_preferences_excluded_hint
        FoodPreferenceMode.ALLERGY -> R.string.food_preferences_allergy_hint
        FoodPreferenceMode.CUISINE -> R.string.food_preferences_cuisine_hint
        FoodPreferenceMode.DISLIKED_CUISINE -> R.string.food_preferences_disliked_cuisine_hint
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
