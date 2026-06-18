package com.abdulin.nutritionapp.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.abdulin.nutritionapp.R
import com.abdulin.nutritionapp.ui.theme.AppThemePreset
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSettingsScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.settings_app_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
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
                .padding(padding)
        ) {
            val contentWidth = if (maxWidth > 600.dp) 0.6f else 1f

            Column(
                modifier = Modifier
                    .fillMaxWidth(contentWidth)
                    .padding(16.dp)
                    .align(Alignment.Center),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.profile_theme),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(AppThemePreset.entries) { preset ->
                        ThemeCircle(
                            preset = preset,
                            isSelected = state.currentThemeOrdinal == preset.ordinal,
                            onClick = { viewModel.setTheme(preset) }
                        )
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Language,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.size(16.dp))
                        Text(
                            text = stringResource(R.string.settings_language_title),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = stringResource(
                            R.string.settings_language_current,
                            currentLanguageLabel(state.currentLocaleCode)
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        LanguageChip(
                            label = stringResource(R.string.settings_language_option_system),
                            selected = state.currentLocaleCode.isNullOrBlank(),
                            onClick = { viewModel.setLanguage(null) }
                        )
                        LanguageChip(
                            label = stringResource(R.string.settings_language_option_ru),
                            selected = state.currentLocaleCode == "ru",
                            onClick = { viewModel.setLanguage("ru") }
                        )
                        LanguageChip(
                            label = stringResource(R.string.settings_language_option_en),
                            selected = state.currentLocaleCode == "en",
                            onClick = { viewModel.setLanguage("en") }
                        )
                    }
                }
            }

            }
        }
    }
}

@Composable
private fun currentLanguageLabel(localeCode: String?): String {
    val effectiveCode = localeCode ?: Locale.getDefault().language
    return when (effectiveCode) {
        "ru" -> stringResource(R.string.language_russian)
        "en" -> stringResource(R.string.language_english)
        else -> Locale.getDefault().displayLanguage.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }
    }
}

@Composable
private fun LanguageChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) }
    )
}

@Composable
private fun ThemeCircle(
    preset: AppThemePreset,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val color = when (preset) {
        AppThemePreset.NATURE_GREEN -> Color(0xFF00897B)
        AppThemePreset.OCEAN_BLUE -> Color(0xFF1976D2)
        AppThemePreset.SUNSET_ORANGE -> Color(0xFFF57C00)
        AppThemePreset.OLED_BLACK -> Color(0xFF000000)
        AppThemePreset.LAVENDER -> Color(0xFF7C4DFF)
        AppThemePreset.BERRY_PINK -> Color(0xFFE91E63)
        AppThemePreset.FOREST_DARK -> Color(0xFF388E3C)
        AppThemePreset.SAND_EARTH -> Color(0xFF8D6E63)
        AppThemePreset.MIDNIGHT_NAVY -> Color(0xFF3F51B5)
        AppThemePreset.CORAL_HEALTH -> Color(0xFFFF6D3A)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(color)
                .border(
                    width = if (isSelected) 3.dp else 0.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = preset.displayName,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
