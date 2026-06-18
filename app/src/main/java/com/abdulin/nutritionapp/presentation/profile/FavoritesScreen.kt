package com.abdulin.nutritionapp.presentation.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.abdulin.nutritionapp.R

private enum class FavoritesMode { PRODUCTS, RECIPES }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onOpenRecipe: (Long) -> Unit
) {
    val state by viewModel.state.collectAsState()
    var mode by rememberSaveable { mutableStateOf(FavoritesMode.PRODUCTS) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.favorites_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
            val contentModifier = Modifier.fillMaxWidth(if (maxWidth > 600.dp) 0.78f else 1f)

            LazyColumn(
                modifier = contentModifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = mode == FavoritesMode.PRODUCTS,
                            onClick = { mode = FavoritesMode.PRODUCTS },
                            label = { Text(stringResource(R.string.nav_products)) },
                            leadingIcon = { Icon(Icons.Default.ShoppingCart, null) }
                        )
                        FilterChip(
                            selected = mode == FavoritesMode.RECIPES,
                            onClick = { mode = FavoritesMode.RECIPES },
                            label = { Text(stringResource(R.string.nav_recipes)) },
                            leadingIcon = { Icon(Icons.Default.RestaurantMenu, null) }
                        )
                    }
                }

                if (mode == FavoritesMode.PRODUCTS) {
                    if (state.products.isEmpty()) {
                        item { EmptyStateCard(stringResource(R.string.favorites_products_empty)) }
                    }
                    items(state.products, key = { it.id }) { product ->
                        FavoriteRow(
                            title = product.name,
                            onClick = null,
                            onRemove = { viewModel.removeFavoriteProduct(product) }
                        )
                    }
                } else {
                    if (state.recipes.isEmpty()) {
                        item { EmptyStateCard(stringResource(R.string.favorites_recipes_empty)) }
                    }
                    items(state.recipes, key = { it.id }) { recipe ->
                        FavoriteRow(
                            title = recipe.title,
                            onClick = { onOpenRecipe(recipe.id) },
                            onRemove = { viewModel.removeFavoriteRecipe(recipe) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoriteRow(
    title: String,
    onClick: (() -> Unit)?,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .let { modifier -> if (onClick != null) modifier.clickable { onClick() } else modifier },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            AssistChip(
                onClick = onRemove,
                label = { Text(stringResource(R.string.food_preferences_remove)) },
                trailingIcon = { Icon(Icons.Default.Close, null) }
            )
        }
    }
}

@Composable
private fun EmptyStateCard(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(18.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
    }
}
