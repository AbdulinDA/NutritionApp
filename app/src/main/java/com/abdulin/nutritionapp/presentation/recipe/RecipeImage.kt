package com.abdulin.nutritionapp.presentation.recipe

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BakeryDining
import androidx.compose.material.icons.filled.BreakfastDining
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.EggAlt
import androidx.compose.material.icons.filled.Icecream
import androidx.compose.material.icons.filled.LocalPizza
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.RiceBowl
import androidx.compose.material.icons.filled.SetMeal
import androidx.compose.material.icons.filled.SoupKitchen
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.abdulin.nutritionapp.BuildConfig
import java.util.Locale
import kotlin.math.absoluteValue

@Composable
fun RecipeImage(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    recipeTitle: String? = null,
    mealType: String? = null
) {
    val context = LocalContext.current
    val resolvedImageUrl = remember(imageUrl) {
        imageUrl
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let { url ->
                if (url.startsWith("http://") || url.startsWith("https://")) {
                    url
                } else {
                    BuildConfig.API_BASE_URL.trimEnd('/') + "/" + url.trimStart('/')
                }
            }
    }
    var showFallback by remember(resolvedImageUrl) { mutableStateOf(resolvedImageUrl == null) }

    Box(modifier = modifier) {
        if (!showFallback) {
            val request = remember(resolvedImageUrl) {
                ImageRequest.Builder(context)
                    .data(resolvedImageUrl)
                    .crossfade(true)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .networkCachePolicy(CachePolicy.ENABLED)
                    .size(720)
                    .build()
            }
            SubcomposeAsyncImage(
                model = request,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale,
                loading = {
                    UniqueDishIllustration(
                        title = recipeTitle ?: contentDescription,
                        mealType = mealType,
                        modifier = Modifier.fillMaxSize()
                    )
                },
                onError = { showFallback = true }
            )
        }

        if (showFallback) {
            UniqueDishIllustration(
                title = recipeTitle ?: contentDescription,
                mealType = mealType,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun UniqueDishIllustration(
    title: String?,
    mealType: String?,
    modifier: Modifier = Modifier
) {
    val style = dishStyle(title, mealType)

    Box(
        modifier = modifier
            .background(Brush.linearGradient(style.background))
            .clip(RoundedCornerShape(0.dp))
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 16.dp)
                .size(style.topOrbSize)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.12f))
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 18.dp)
                .size(style.sideOrbSize)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.10f))
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 26.dp, bottom = 26.dp)
                .size(style.bottomOrbSize)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.10f))
        )

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                color = Color.White.copy(alpha = 0.14f),
                shape = CircleShape
            ) {
                Icon(
                    imageVector = style.icon,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(22.dp)
                        .size(style.iconSize),
                    tint = Color.White
                )
            }
        }
    }
}

private fun dishStyle(title: String?, mealType: String?): DishStyle {
    val seed = stableSeed(title, mealType)
    val palette = palettes[seed % palettes.size]
    val icon = icons[seed % icons.size]

    return DishStyle(
        background = palette,
        icon = icon,
        topOrbSize = (78 + seed % 26).dp,
        sideOrbSize = (48 + (seed / 3) % 24).dp,
        bottomOrbSize = (34 + (seed / 5) % 22).dp,
        iconSize = (56 + (seed / 7) % 16).dp
    )
}

private fun keywordTags(title: String?): List<String> {
    val normalized = title
        .orEmpty()
        .lowercase(Locale.ROOT)
        .replace(Regex("\\s+"), " ")

    val tags = mutableListOf<String>()
    if (normalized.contains("soup") || normalized.contains("суп")) tags += "Soup"
    if (normalized.contains("salad") || normalized.contains("салат")) tags += "Salad"
    if (normalized.contains("rice") || normalized.contains("плов")) tags += "Rice"
    if (normalized.contains("pasta") || normalized.contains("карбонара")) tags += "Pasta"
    if (normalized.contains("egg") || normalized.contains("омлет")) tags += "Eggs"
    if (normalized.contains("fish") || normalized.contains("рыб")) tags += "Fish"
    if (normalized.contains("chicken") || normalized.contains("кур")) tags += "Chicken"
    if (normalized.contains("dessert") || normalized.contains("ягод") || normalized.contains("smoothie")) tags += "Sweet"
    if (tags.isEmpty() && normalized.isNotBlank()) {
        tags += normalized.split(" ").first().replaceFirstChar { it.uppercase(Locale.ROOT) }
    }
    return tags
}

private fun stableSeed(title: String?, mealType: String?): Int {
    return "${title.orEmpty()}|${mealType.orEmpty()}"
        .lowercase(Locale.ROOT)
        .hashCode()
        .absoluteValue
}

private data class DishStyle(
    val background: List<Color>,
    val icon: ImageVector,
    val topOrbSize: androidx.compose.ui.unit.Dp,
    val sideOrbSize: androidx.compose.ui.unit.Dp,
    val bottomOrbSize: androidx.compose.ui.unit.Dp,
    val iconSize: androidx.compose.ui.unit.Dp
)

private val icons = listOf(
    Icons.Default.BreakfastDining,
    Icons.Default.LunchDining,
    Icons.Default.DinnerDining,
    Icons.Default.Restaurant,
    Icons.Default.SoupKitchen,
    Icons.Default.RiceBowl,
    Icons.Default.LocalPizza,
    Icons.Default.EggAlt,
    Icons.Default.BakeryDining,
    Icons.Default.Icecream,
    Icons.Default.SetMeal
)

private val palettes = listOf(
    listOf(Color(0xFFEF5350), Color(0xFF8E24AA)),
    listOf(Color(0xFFFFB74D), Color(0xFFF4511E)),
    listOf(Color(0xFF4DB6AC), Color(0xFF00695C)),
    listOf(Color(0xFF64B5F6), Color(0xFF1E88E5)),
    listOf(Color(0xFF81C784), Color(0xFF2E7D32)),
    listOf(Color(0xFF9575CD), Color(0xFF5E35B1)),
    listOf(Color(0xFFFF8A65), Color(0xFFD84315)),
    listOf(Color(0xFF4FC3F7), Color(0xFF0288D1)),
    listOf(Color(0xFFFFD54F), Color(0xFFF9A825)),
    listOf(Color(0xFF90A4AE), Color(0xFF455A64)),
    listOf(Color(0xFFA1887F), Color(0xFF6D4C41))
)
