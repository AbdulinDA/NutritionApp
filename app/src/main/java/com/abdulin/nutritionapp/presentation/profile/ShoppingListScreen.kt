package com.abdulin.nutritionapp.presentation.profile

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.abdulin.nutritionapp.R
import com.abdulin.nutritionapp.domain.model.ShoppingListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    viewModel: ShoppingListViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onNavigateToGeneratePlan: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var isAddDialogOpen by remember { mutableStateOf(false) }
    val handleBack = {
        viewModel.persistCheckedItems()
        onBack()
    }

    BackHandler(onBack = handleBack)

    DisposableEffect(Unit) {
        onDispose {
            viewModel.persistCheckedItems()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.shopping_list_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = handleBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.onboarding_back))
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.persistCheckedItems()
                        viewModel.loadShoppingList()
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.shopping_list_refresh))
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
            val contentModifier = Modifier.fillMaxWidth(if (maxWidth > 600.dp) 0.7f else 1f)

            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.error != null) {
                ShoppingListStateCard(
                    modifier = contentModifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    title = stringResource(R.string.shopping_list_error_title),
                    subtitle = state.error ?: stringResource(R.string.shopping_list_error_subtitle),
                    primaryAction = stringResource(R.string.shopping_list_refresh),
                    onPrimaryAction = viewModel::loadShoppingList,
                    secondaryAction = stringResource(R.string.shopping_list_generate_plan),
                    onSecondaryAction = onNavigateToGeneratePlan
                )
            } else if (state.items.isEmpty()) {
                ShoppingListStateCard(
                    modifier = contentModifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    title = stringResource(R.string.shopping_list_empty_title),
                    subtitle = stringResource(R.string.shopping_list_empty_subtitle),
                    primaryAction = stringResource(R.string.shopping_list_generate_plan),
                    onPrimaryAction = onNavigateToGeneratePlan
                )
            } else {
                val groupedItems = state.items.groupBy {
                    it.category.takeIf(String::isNotBlank) ?: stringResource(R.string.shopping_list_category_fallback)
                }

                LazyColumn(
                    modifier = contentModifier.fillMaxHeight(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        ShoppingListSummaryCard(
                            itemsCount = state.items.size,
                            checkedCount = state.pendingPurchasedCount,
                            atHomeCount = state.atHomeCount,
                            hiddenCount = state.hiddenCount,
                            onMarkPurchased = viewModel::persistCheckedItems,
                            onMarkAtHome = viewModel::markCheckedItemsAtHome,
                            onAddItem = { isAddDialogOpen = true },
                            hasSelection = state.checkedItems.isNotEmpty()
                        )
                    }

                    groupedItems.forEach { (category, items) ->
                        item {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        items(items) { item ->
                            val itemKey = item.purchaseKey()
                            val isChecked = state.checkedItems.contains(itemKey)
                            ShoppingItemRow(
                                item = item,
                                isChecked = isChecked,
                                onToggle = { viewModel.toggleItem(itemKey) },
                                onHide = { viewModel.hideItem(itemKey) }
                            )
                        }
                    }
                    item { Spacer(modifier = Modifier.height(32.dp)) }
                }
            }
        }
    }

    if (isAddDialogOpen) {
        AddShoppingItemDialog(
            onDismiss = { isAddDialogOpen = false },
            onConfirm = { name, quantity, unit, category ->
                if (viewModel.addManualItem(name, quantity, unit, category)) {
                    isAddDialogOpen = false
                }
            }
        )
    }
}

private fun ShoppingListItem.purchaseKey(): String {
    return listOf(
        productId.toString(),
        productName.trim().lowercase(),
        category.trim().lowercase(),
        totalQuantity.toString(),
        unit.trim().lowercase()
    ).joinToString("|")
}

@Composable
private fun ShoppingListSummaryCard(
    itemsCount: Int,
    checkedCount: Int,
    atHomeCount: Int,
    hiddenCount: Int,
    onMarkPurchased: () -> Unit,
    onMarkAtHome: () -> Unit,
    onAddItem: () -> Unit,
    hasSelection: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.shopping_list_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.surface) {
                    Text(
                        text = stringResource(R.string.shopping_list_items_count, itemsCount),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.surface) {
                    Text(
                        text = stringResource(R.string.shopping_list_checked_count, checkedCount),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.surface) {
                    Text(
                        text = stringResource(R.string.shopping_list_at_home_count, atHomeCount),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.surface) {
                    Text(
                        text = stringResource(R.string.shopping_list_hidden_count, hiddenCount),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalButton(onClick = onAddItem, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.shopping_list_add_item))
                }
                FilledTonalButton(
                    onClick = onMarkAtHome,
                    enabled = hasSelection,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.shopping_list_mark_at_home))
                }
            }
            Button(
                onClick = onMarkPurchased,
                enabled = hasSelection,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.shopping_list_mark_purchased))
            }
        }
    }
}

@Composable
private fun ShoppingListStateCard(
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
fun ShoppingItemRow(
    item: ShoppingListItem,
    isChecked: Boolean,
    onToggle: () -> Unit,
    onHide: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        colors = CardDefaults.cardColors(
            containerColor = if (isChecked) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (!isChecked) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isChecked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (isChecked) Color(0xFF4CAF50) else MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = item.productName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    textDecoration = if (isChecked) TextDecoration.LineThrough else null,
                    color = if (isChecked) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${item.totalQuantity} ${item.unit}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                if (item.isManual) {
                    Text(
                        text = stringResource(R.string.shopping_list_manual_badge),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                TextButton(
                    onClick = onHide,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(stringResource(R.string.shopping_list_hide_item))
                }
            }
        }
    }
}

@Composable
private fun AddShoppingItemDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("шт") }
    var category by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.shopping_list_add_item)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.shopping_list_item_name)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text(stringResource(R.string.shopping_list_item_quantity)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text(stringResource(R.string.shopping_list_item_unit)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text(stringResource(R.string.shopping_list_item_category)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name, quantity, unit, category) }) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}
