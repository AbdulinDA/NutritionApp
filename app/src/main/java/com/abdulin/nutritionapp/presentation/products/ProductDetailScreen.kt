package com.abdulin.nutritionapp.presentation.products

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.abdulin.nutritionapp.R
import com.abdulin.nutritionapp.domain.model.ProductModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    product: ProductModel,
    viewModel: ProductDetailViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onAlternativeClick: (ProductModel) -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(product) {
        viewModel.init(product, false)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.product_detail_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.onboarding_back))
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFavorite() }) {
                        Icon(
                            imageVector = if (state.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = stringResource(R.string.product_detail_favorite),
                            tint = if (state.isFavorite) Color.Red else MaterialTheme.colorScheme.onSurface
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
            val contentModifier = Modifier.fillMaxWidth(if (maxWidth > 600.dp) 0.75f else 1f)

            Column(
                modifier = contentModifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Fit
                )

                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    product.brand?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    AiProductAnalysisCard(product)

                    Spacer(modifier = Modifier.height(24.dp))

                    if (state.isLoading) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    AlternativesSection(state.alternatives, onAlternativeClick)

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        stringResource(R.string.product_detail_nutrition_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    NutritionRow(
                        stringResource(R.string.home_calories),
                        "${formatNutritionValue(product.calories)} ${stringResource(R.string.unit_kcal)}",
                        (product.calories / 800.0).toFloat(),
                        Color(0xFFFF9800)
                    )
                    NutritionRow(
                        stringResource(R.string.home_protein),
                        "${formatNutritionValue(product.protein)} ${stringResource(R.string.unit_g)}",
                        (product.protein / 50.0).toFloat(),
                        Color(0xFF4CAF50)
                    )
                    NutritionRow(
                        stringResource(R.string.home_fat),
                        "${formatNutritionValue(product.fat)} ${stringResource(R.string.unit_g)}",
                        (product.fat / 50.0).toFloat(),
                        Color(0xFFFFC107)
                    )
                    NutritionRow(
                        stringResource(R.string.home_carbs),
                        "${formatNutritionValue(product.carbs)} ${stringResource(R.string.unit_g)}",
                        (product.carbs / 100.0).toFloat(),
                        Color(0xFF2196F3)
                    )
                }
            }
        }
    }
}

@Composable
fun AlternativesSection(alternatives: List<ProductModel>, onAlternativeClick: (ProductModel) -> Unit) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Lightbulb,
                contentDescription = null,
                tint = Color(0xFFFFC107),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                stringResource(R.string.product_detail_alternatives_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (alternatives.isEmpty()) {
            Text(
                text = stringResource(R.string.product_detail_alternatives_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(alternatives) { alt ->
                    Card(
                        modifier = Modifier
                            .width(160.dp)
                            .clickable { onAlternativeClick(alt) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            AsyncImage(
                                model = alt.imageUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(80.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                alt.name,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                "${formatNutritionValue(alt.calories)} ${stringResource(R.string.unit_kcal)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatNutritionValue(value: Double): String {
    val rounded = kotlin.math.round(value * 10.0) / 10.0
    return if (rounded % 1.0 == 0.0) {
        rounded.toInt().toString()
    } else {
        String.format(Locale.getDefault(), "%.1f", rounded)
    }
}

@Composable
fun AiProductAnalysisCard(product: ProductModel) {
    val analysis = generateAiAnalysis(product)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = analysis.color.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, analysis.color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Info, contentDescription = null, tint = analysis.color)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    stringResource(R.string.product_detail_analysis_title),
                    style = MaterialTheme.typography.labelLarge,
                    color = analysis.color,
                    fontWeight = FontWeight.Bold
                )
                Text(analysis.text, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

private data class ProductAnalysis(val text: String, val color: Color)

@Composable
private fun generateAiAnalysis(product: ProductModel): ProductAnalysis {
    return when {
        product.protein > 15 -> ProductAnalysis(
            stringResource(R.string.product_detail_analysis_high_protein),
            Color(0xFF4CAF50)
        )
        product.carbs > 40 && product.calories > 300 -> ProductAnalysis(
            stringResource(R.string.product_detail_analysis_high_calorie),
            Color(0xFFFF9800)
        )
        product.fat > 20 -> ProductAnalysis(
            stringResource(R.string.product_detail_analysis_high_fat),
            Color(0xFFE91E63)
        )
        else -> ProductAnalysis(
            stringResource(R.string.product_detail_analysis_balanced),
            Color(0xFF2196F3)
        )
    }
}

@Composable
fun NutritionRow(label: String, value: String, progress: Float, color: Color) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )
    }
}
