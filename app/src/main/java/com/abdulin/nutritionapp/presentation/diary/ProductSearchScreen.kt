package com.abdulin.nutritionapp.presentation.diary

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.abdulin.nutritionapp.R
import com.abdulin.nutritionapp.domain.model.ProductModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductSearchScreen(
    viewModel: ProductSearchViewModel = hiltViewModel(),
    navController: NavController? = null,
    onProductSelected: (ProductModel) -> Unit,
    onNavigateToScanner: () -> Unit,
    onBack: () -> Unit,
    showBackButton: Boolean = true,
    bottomBarPadding: androidx.compose.ui.unit.Dp = 0.dp
) {
    val state by viewModel.state.collectAsState()
    val products = viewModel.productsPagingData.collectAsLazyPagingItems()
    val savedStateHandle = navController?.currentBackStackEntry?.savedStateHandle

    val scannedBarcode by savedStateHandle?.getStateFlow<String?>("scanned_barcode", null)
        ?.collectAsState() ?: remember { mutableStateOf(null) }

    LaunchedEffect(scannedBarcode) {
        scannedBarcode?.let { barcode ->
            viewModel.searchByBarcode(barcode)
            savedStateHandle?.remove<String>("scanned_barcode")
        }
    }

    LaunchedEffect(state.scannedProduct) {
        state.scannedProduct?.let { product ->
            onProductSelected(product)
            viewModel.clearScannedProduct()
        }
    }

    val isRefreshError = products.loadState.refresh is LoadState.Error
    val refreshErrorMessage = (products.loadState.refresh as? LoadState.Error)?.error?.localizedMessage

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.search_products_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    if (showBackButton) {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.onboarding_back)
                            )
                        }
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
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = {
                        viewModel.onSearchQueryChange(it)
                        if (state.error != null) viewModel.clearError()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 16.dp),
                    placeholder = { Text(stringResource(R.string.search_hint)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(onClick = onNavigateToScanner) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                        Text(
                            stringResource(R.string.search_barcode_hint),
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }
                }

                if (state.isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, top = 4.dp, end = 16.dp, bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (state.searchQuery.isBlank()) {
                        item {
                            Text(
                                text = stringResource(R.string.search_frequent),
                                color = MaterialTheme.colorScheme.outline,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        item {
                            Text(
                                text = stringResource(R.string.products_search_subtitle),
                                color = MaterialTheme.colorScheme.outline,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    if (state.error != null && products.itemCount == 0 && !state.isLoading) {
                        item {
                            val isBarcodeFailure = state.lastScannedBarcode != null
                            SearchStateCard(
                                title = stringResource(
                                    if (isBarcodeFailure) R.string.products_scan_not_found_title
                                    else R.string.products_error_title
                                ),
                                subtitle = stringResource(
                                    if (isBarcodeFailure) R.string.products_scan_not_found_subtitle
                                    else R.string.products_error_subtitle
                                ),
                                primaryAction = stringResource(
                                    if (isBarcodeFailure) R.string.products_retry_scan else R.string.retry
                                ),
                                onPrimaryAction = {
                                    if (isBarcodeFailure) onNavigateToScanner else viewModel.clearError()
                                },
                                secondaryAction = stringResource(R.string.products_manual_search),
                                onSecondaryAction = {
                                    viewModel.clearError()
                                }
                            )
                        }
                    } else if (isRefreshError && products.itemCount == 0 && !state.isLoading) {
                        item {
                            SearchStateCard(
                                title = stringResource(R.string.products_error_title),
                                subtitle = refreshErrorMessage ?: stringResource(R.string.products_error_subtitle),
                                primaryAction = stringResource(R.string.retry),
                                onPrimaryAction = { products.retry() },
                                secondaryAction = stringResource(R.string.search_barcode_hint),
                                onSecondaryAction = onNavigateToScanner
                            )
                        }
                    } else if (products.loadState.refresh is LoadState.NotLoading && products.itemCount == 0 && !state.isLoading) {
                        item {
                            SearchStateCard(
                                title = stringResource(R.string.products_empty_title),
                                subtitle = stringResource(R.string.products_empty_subtitle),
                                primaryAction = stringResource(R.string.search_barcode_hint),
                                onPrimaryAction = onNavigateToScanner,
                                secondaryAction = stringResource(R.string.products_manual_search),
                                onSecondaryAction = viewModel::clearError
                            )
                        }
                    }

                    items(
                        count = products.itemCount,
                        key = products.itemKey { it.id }
                    ) { index ->
                        val product = products[index] ?: return@items
                        ProductSearchItem(
                            product = product,
                            isFavorite = state.favoritesIds.contains(product.id),
                            onClick = {
                                viewModel.selectProduct(product)
                                onProductSelected(product)
                            },
                            onToggleFavorite = { viewModel.toggleFavorite(product.id, product.name) }
                        )
                    }

                    when {
                        products.loadState.refresh is LoadState.Loading -> {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }

                        products.loadState.append is LoadState.Loading -> {
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
    }
}

@Composable
private fun SearchStateCard(
    title: String,
    subtitle: String,
    primaryAction: String,
    onPrimaryAction: () -> Unit,
    secondaryAction: String,
    onSecondaryAction: () -> Unit
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
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalButton(onClick = onPrimaryAction) {
                    Text(primaryAction)
                }
                Button(onClick = onSecondaryAction) {
                    Text(secondaryAction)
                }
            }
        }
    }
}

@Composable
private fun ProductSearchItem(
    product: ProductModel,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp)
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

                product.brand?.takeIf { it.isNotBlank() }?.let { brand ->
                    Text(
                        text = brand,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val proteinShort = stringResource(R.string.macros_protein).take(1)
                    val fatShort = stringResource(R.string.macros_fat).take(1)
                    val carbsShort = stringResource(R.string.macros_carbs).take(1)
                    NutritionBadge(
                        text = "${product.calories.toInt()} ${stringResource(R.string.unit_kcal)}",
                        background = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    NutritionBadge(
                        text = "$proteinShort ${product.protein.toInt()}",
                        background = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    NutritionBadge(
                        text = "$fatShort ${product.fat.toInt()}",
                        background = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    NutritionBadge(
                        text = "$carbsShort ${product.carbs.toInt()}",
                        background = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    tint = if (isFavorite) Color(0xFFE53935) else MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun NutritionBadge(
    text: String,
    background: Color,
    contentColor: Color
) {
    Surface(
        color = background,
        contentColor = contentColor,
        shape = RoundedCornerShape(999.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )
    }
}
