package com.abdulin.nutritionapp.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme

enum class AppThemePreset(val displayName: String) {
    DEMO("Демо"),
    CLASSIC("Классика"),
    OCEAN("Океан"),
    FOREST("Лес"),
    SUNSET("Закат"),
    LAVENDER("Лаванда"),
    CORAL("Коралл"),
    SLATE("Серый"),
    BERRY("Ягодный"),
    AMOLED("AMOLED")
}

fun AppThemePreset.toColorScheme(isDark: Boolean): ColorScheme {
    return when (this) {
        AppThemePreset.DEMO -> if (isDark) demoDarkScheme() else demoLightScheme()
        AppThemePreset.CLASSIC -> if (isDark) classicDarkScheme() else classicLightScheme()
        AppThemePreset.OCEAN -> if (isDark) oceanDarkScheme() else oceanLightScheme()
        AppThemePreset.FOREST -> if (isDark) forestDarkScheme() else forestLightScheme()
        AppThemePreset.SUNSET -> if (isDark) sunsetDarkScheme() else sunsetLightScheme()
        AppThemePreset.LAVENDER -> if (isDark) lavenderDarkScheme() else lavenderLightScheme()
        AppThemePreset.CORAL -> if (isDark) coralDarkScheme() else coralLightScheme()
        AppThemePreset.SLATE -> if (isDark) slateDarkScheme() else slateLightScheme()
        AppThemePreset.BERRY -> if (isDark) berryDarkScheme() else berryLightScheme()
        AppThemePreset.AMOLED -> amoledScheme()
    }
}

private fun demoLightScheme() = lightColorScheme(
    primary = DemoPrimary, onPrimary = DemoOnPrimary,
    primaryContainer = DemoPrimaryContainer, onPrimaryContainer = DemoOnPrimaryContainer,
    secondary = DemoSecondary, onSecondary = DemoOnSecondary,
    secondaryContainer = DemoSecondaryContainer, onSecondaryContainer = DemoOnSecondaryContainer,
    tertiary = DemoTertiary, onTertiary = DemoOnTertiary,
    tertiaryContainer = DemoTertiaryContainer, onTertiaryContainer = DemoOnTertiaryContainer,
    background = DemoBackground, onBackground = DemoOnBackground,
    surface = DemoSurface, onSurface = DemoOnSurface,
    surfaceVariant = DemoSurfaceVariant, onSurfaceVariant = DemoOnSurfaceVariant,
    error = DemoError, onError = DemoOnError,
    errorContainer = DemoErrorContainer, onErrorContainer = DemoOnErrorContainer,
    outline = DemoOutline
)

private fun demoDarkScheme() = darkColorScheme(
    primary = DemoPrimaryDark, onPrimary = DemoOnPrimaryDark,
    primaryContainer = DemoPrimaryContainerDark, onPrimaryContainer = DemoOnPrimaryContainerDark,
    secondary = DemoSecondaryDark, onSecondary = DemoOnSecondaryDark,
    secondaryContainer = DemoSecondaryContainerDark, onSecondaryContainer = DemoOnSecondaryContainerDark,
    tertiary = DemoTertiaryDark, onTertiary = DemoOnTertiaryDark,
    tertiaryContainer = DemoTertiaryContainerDark, onTertiaryContainer = DemoOnTertiaryContainerDark,
    background = DemoBackgroundDark, onBackground = DemoOnBackgroundDark,
    surface = DemoSurfaceDark, onSurface = DemoOnSurfaceDark,
    surfaceVariant = DemoSurfaceVariantDark, onSurfaceVariant = DemoOnSurfaceVariantDark,
    error = DemoErrorDark, onError = DemoOnErrorDark,
    errorContainer = DemoErrorContainerDark, onErrorContainer = DemoOnErrorContainerDark,
    outline = DemoOutlineDark
)

private fun classicLightScheme() = lightColorScheme(
    primary = ClassicPrimary, onPrimary = ClassicOnPrimary,
    primaryContainer = ClassicPrimaryContainer, onPrimaryContainer = ClassicOnPrimaryContainer,
    secondary = ClassicSecondary, onSecondary = ClassicOnSecondary,
    secondaryContainer = ClassicSecondaryContainer, onSecondaryContainer = ClassicOnSecondaryContainer,
    tertiary = ClassicTertiary, onTertiary = ClassicOnTertiary,
    tertiaryContainer = ClassicTertiaryContainer, onTertiaryContainer = ClassicOnTertiaryContainer,
    background = ClassicBackground, onBackground = ClassicOnBackground,
    surface = ClassicSurface, onSurface = ClassicOnSurface,
    surfaceVariant = ClassicSurfaceVariant, onSurfaceVariant = ClassicOnSurfaceVariant,
    error = ClassicError, onError = ClassicOnError,
    errorContainer = ClassicErrorContainer, onErrorContainer = ClassicOnErrorContainer,
    outline = ClassicOutline
)

private fun classicDarkScheme() = darkColorScheme(
    primary = ClassicPrimaryDark, onPrimary = ClassicOnPrimaryDark,
    primaryContainer = ClassicPrimaryContainerDark, onPrimaryContainer = ClassicOnPrimaryContainerDark,
    secondary = ClassicSecondaryDark, onSecondary = ClassicOnSecondaryDark,
    secondaryContainer = ClassicSecondaryContainerDark, onSecondaryContainer = ClassicOnSecondaryContainerDark,
    tertiary = ClassicTertiaryDark, onTertiary = ClassicOnTertiaryDark,
    tertiaryContainer = ClassicTertiaryContainerDark, onTertiaryContainer = ClassicOnTertiaryContainerDark,
    background = ClassicBackgroundDark, onBackground = ClassicOnBackgroundDark,
    surface = ClassicSurfaceDark, onSurface = ClassicOnSurfaceDark,
    surfaceVariant = ClassicSurfaceVariantDark, onSurfaceVariant = ClassicOnSurfaceVariantDark,
    error = ClassicErrorDark, onError = ClassicOnErrorDark,
    errorContainer = ClassicErrorContainerDark, onErrorContainer = ClassicOnErrorContainerDark,
    outline = ClassicOutlineDark
)

private fun oceanLightScheme() = lightColorScheme(
    primary = OceanPrimary, onPrimary = OceanOnPrimary,
    primaryContainer = OceanPrimaryContainer, onPrimaryContainer = OceanOnPrimaryContainer,
    secondary = OceanSecondary, onSecondary = OceanOnSecondary,
    secondaryContainer = OceanSecondaryContainer, onSecondaryContainer = OceanOnSecondaryContainer,
    tertiary = OceanTertiary, onTertiary = OceanOnTertiary,
    tertiaryContainer = OceanTertiaryContainer, onTertiaryContainer = OceanOnTertiaryContainer,
    background = OceanBackground, onBackground = OceanOnBackground,
    surface = OceanSurface, onSurface = OceanOnSurface,
    surfaceVariant = OceanSurfaceVariant, onSurfaceVariant = OceanOnSurfaceVariant,
    error = OceanError, onError = OceanOnError,
    errorContainer = OceanErrorContainer, onErrorContainer = OceanOnErrorContainer,
    outline = OceanOutline
)

private fun oceanDarkScheme() = darkColorScheme(
    primary = OceanPrimaryDark, onPrimary = OceanOnPrimaryDark,
    primaryContainer = OceanPrimaryContainerDark, onPrimaryContainer = OceanOnPrimaryContainerDark,
    secondary = OceanSecondaryDark, onSecondary = OceanOnSecondaryDark,
    secondaryContainer = OceanSecondaryContainerDark, onSecondaryContainer = OceanOnSecondaryContainerDark,
    tertiary = OceanTertiaryDark, onTertiary = OceanOnTertiaryDark,
    tertiaryContainer = OceanTertiaryContainerDark, onTertiaryContainer = OceanOnTertiaryContainerDark,
    background = OceanBackgroundDark, onBackground = OceanOnBackgroundDark,
    surface = OceanSurfaceDark, onSurface = OceanOnSurfaceDark,
    surfaceVariant = OceanSurfaceVariantDark, onSurfaceVariant = OceanOnSurfaceVariantDark,
    error = OceanErrorDark, onError = OceanOnErrorDark,
    errorContainer = OceanErrorContainerDark, onErrorContainer = OceanOnErrorContainerDark,
    outline = OceanOutlineDark
)

private fun forestLightScheme() = lightColorScheme(
    primary = ForestPrimary, onPrimary = ForestOnPrimary,
    primaryContainer = ForestPrimaryContainer, onPrimaryContainer = ForestOnPrimaryContainer,
    secondary = ForestSecondary, onSecondary = ForestOnSecondary,
    secondaryContainer = ForestSecondaryContainer, onSecondaryContainer = ForestOnSecondaryContainer,
    tertiary = ForestTertiary, onTertiary = ForestOnTertiary,
    tertiaryContainer = ForestTertiaryContainer, onTertiaryContainer = ForestOnTertiaryContainer,
    background = ForestBackground, onBackground = ForestOnBackground,
    surface = ForestSurface, onSurface = ForestOnSurface,
    surfaceVariant = ForestSurfaceVariant, onSurfaceVariant = ForestOnSurfaceVariant,
    error = ForestError, onError = ForestOnError,
    errorContainer = ForestErrorContainer, onErrorContainer = ForestOnErrorContainer,
    outline = ForestOutline
)

private fun forestDarkScheme() = darkColorScheme(
    primary = ForestPrimaryDark, onPrimary = ForestOnPrimaryDark,
    primaryContainer = ForestPrimaryContainerDark, onPrimaryContainer = ForestOnPrimaryContainerDark,
    secondary = ForestSecondaryDark, onSecondary = ForestOnSecondaryDark,
    secondaryContainer = ForestSecondaryContainerDark, onSecondaryContainer = ForestOnSecondaryContainerDark,
    tertiary = ForestTertiaryDark, onTertiary = ForestOnTertiaryDark,
    tertiaryContainer = ForestTertiaryContainerDark, onTertiaryContainer = ForestOnTertiaryContainerDark,
    background = ForestBackgroundDark, onBackground = ForestOnBackgroundDark,
    surface = ForestSurfaceDark, onSurface = ForestOnSurfaceDark,
    surfaceVariant = ForestSurfaceVariantDark, onSurfaceVariant = ForestOnSurfaceVariantDark,
    error = ForestErrorDark, onError = ForestOnErrorDark,
    errorContainer = ForestErrorContainerDark, onErrorContainer = ForestOnErrorContainerDark,
    outline = ForestOutlineDark
)

private fun sunsetLightScheme() = lightColorScheme(
    primary = SunsetPrimary, onPrimary = SunsetOnPrimary,
    primaryContainer = SunsetPrimaryContainer, onPrimaryContainer = SunsetOnPrimaryContainer,
    secondary = SunsetSecondary, onSecondary = SunsetOnSecondary,
    secondaryContainer = SunsetSecondaryContainer, onSecondaryContainer = SunsetOnSecondaryContainer,
    tertiary = SunsetTertiary, onTertiary = SunsetOnTertiary,
    tertiaryContainer = SunsetTertiaryContainer, onTertiaryContainer = SunsetOnTertiaryContainer,
    background = SunsetBackground, onBackground = SunsetOnBackground,
    surface = SunsetSurface, onSurface = SunsetOnSurface,
    surfaceVariant = SunsetSurfaceVariant, onSurfaceVariant = SunsetOnSurfaceVariant,
    error = SunsetError, onError = SunsetOnError,
    errorContainer = SunsetErrorContainer, onErrorContainer = SunsetOnErrorContainer,
    outline = SunsetOutline
)

private fun sunsetDarkScheme() = darkColorScheme(
    primary = SunsetPrimaryDark, onPrimary = SunsetOnPrimaryDark,
    primaryContainer = SunsetPrimaryContainerDark, onPrimaryContainer = SunsetOnPrimaryContainerDark,
    secondary = SunsetSecondaryDark, onSecondary = SunsetOnSecondaryDark,
    secondaryContainer = SunsetSecondaryContainerDark, onSecondaryContainer = SunsetOnSecondaryContainerDark,
    tertiary = SunsetTertiaryDark, onTertiary = SunsetOnTertiaryDark,
    tertiaryContainer = SunsetTertiaryContainerDark, onTertiaryContainer = SunsetOnTertiaryContainerDark,
    background = SunsetBackgroundDark, onBackground = SunsetOnBackgroundDark,
    surface = SunsetSurfaceDark, onSurface = SunsetOnSurfaceDark,
    surfaceVariant = SunsetSurfaceVariantDark, onSurfaceVariant = SunsetOnSurfaceVariantDark,
    error = SunsetErrorDark, onError = SunsetOnErrorDark,
    errorContainer = SunsetErrorContainerDark, onErrorContainer = SunsetOnErrorContainerDark,
    outline = SunsetOutlineDark
)

private fun lavenderLightScheme() = lightColorScheme(
    primary = LavenderPrimary, onPrimary = LavenderOnPrimary,
    primaryContainer = LavenderPrimaryContainer, onPrimaryContainer = LavenderOnPrimaryContainer,
    secondary = LavenderSecondary, onSecondary = LavenderOnSecondary,
    secondaryContainer = LavenderSecondaryContainer, onSecondaryContainer = LavenderOnSecondaryContainer,
    tertiary = LavenderTertiary, onTertiary = LavenderOnTertiary,
    tertiaryContainer = LavenderTertiaryContainer, onTertiaryContainer = LavenderOnTertiaryContainer,
    background = LavenderBackground, onBackground = LavenderOnBackground,
    surface = LavenderSurface, onSurface = LavenderOnSurface,
    surfaceVariant = LavenderSurfaceVariant, onSurfaceVariant = LavenderOnSurfaceVariant,
    error = LavenderError, onError = LavenderOnError,
    errorContainer = LavenderErrorContainer, onErrorContainer = LavenderOnErrorContainer,
    outline = LavenderOutline
)

private fun lavenderDarkScheme() = darkColorScheme(
    primary = LavenderPrimaryDark, onPrimary = LavenderOnPrimaryDark,
    primaryContainer = LavenderPrimaryContainerDark, onPrimaryContainer = LavenderOnPrimaryContainerDark,
    secondary = LavenderSecondaryDark, onSecondary = LavenderOnSecondaryDark,
    secondaryContainer = LavenderSecondaryContainerDark, onSecondaryContainer = LavenderOnSecondaryContainerDark,
    tertiary = LavenderTertiaryDark, onTertiary = LavenderOnTertiaryDark,
    tertiaryContainer = LavenderTertiaryContainerDark, onTertiaryContainer = LavenderOnTertiaryContainerDark,
    background = LavenderBackgroundDark, onBackground = LavenderOnBackgroundDark,
    surface = LavenderSurfaceDark, onSurface = LavenderOnSurfaceDark,
    surfaceVariant = LavenderSurfaceVariantDark, onSurfaceVariant = LavenderOnSurfaceVariantDark,
    error = LavenderErrorDark, onError = LavenderOnErrorDark,
    errorContainer = LavenderErrorContainerDark, onErrorContainer = LavenderOnErrorContainerDark,
    outline = LavenderOutlineDark
)

private fun coralLightScheme() = lightColorScheme(
    primary = CoralPrimary, onPrimary = CoralOnPrimary,
    primaryContainer = CoralPrimaryContainer, onPrimaryContainer = CoralOnPrimaryContainer,
    secondary = CoralSecondary, onSecondary = CoralOnSecondary,
    secondaryContainer = CoralSecondaryContainer, onSecondaryContainer = CoralOnSecondaryContainer,
    tertiary = CoralTertiary, onTertiary = CoralOnTertiary,
    tertiaryContainer = CoralTertiaryContainer, onTertiaryContainer = CoralOnTertiaryContainer,
    background = CoralBackground, onBackground = CoralOnBackground,
    surface = CoralSurface, onSurface = CoralOnSurface,
    surfaceVariant = CoralSurfaceVariant, onSurfaceVariant = CoralOnSurfaceVariant,
    error = CoralError, onError = CoralOnError,
    errorContainer = CoralErrorContainer, onErrorContainer = CoralOnErrorContainer,
    outline = CoralOutline
)

private fun coralDarkScheme() = darkColorScheme(
    primary = CoralPrimaryDark, onPrimary = CoralOnPrimaryDark,
    primaryContainer = CoralPrimaryContainerDark, onPrimaryContainer = CoralOnPrimaryContainerDark,
    secondary = CoralSecondaryDark, onSecondary = CoralOnSecondaryDark,
    secondaryContainer = CoralSecondaryContainerDark, onSecondaryContainer = CoralOnSecondaryContainerDark,
    tertiary = CoralTertiaryDark, onTertiary = CoralOnTertiaryDark,
    tertiaryContainer = CoralTertiaryContainerDark, onTertiaryContainer = CoralOnTertiaryContainerDark,
    background = CoralBackgroundDark, onBackground = CoralOnBackgroundDark,
    surface = CoralSurfaceDark, onSurface = CoralOnSurfaceDark,
    surfaceVariant = CoralSurfaceVariantDark, onSurfaceVariant = CoralOnSurfaceVariantDark,
    error = CoralErrorDark, onError = CoralOnErrorDark,
    errorContainer = CoralErrorContainerDark, onErrorContainer = CoralOnErrorContainerDark,
    outline = CoralOutlineDark
)

private fun slateLightScheme() = lightColorScheme(
    primary = SlatePrimary, onPrimary = SlateOnPrimary,
    primaryContainer = SlatePrimaryContainer, onPrimaryContainer = SlateOnPrimaryContainer,
    secondary = SlateSecondary, onSecondary = SlateOnSecondary,
    secondaryContainer = SlateSecondaryContainer, onSecondaryContainer = SlateOnSecondaryContainer,
    tertiary = SlateTertiary, onTertiary = SlateOnTertiary,
    tertiaryContainer = SlateTertiaryContainer, onTertiaryContainer = SlateOnTertiaryContainer,
    background = SlateBackground, onBackground = SlateOnBackground,
    surface = SlateSurface, onSurface = SlateOnSurface,
    surfaceVariant = SlateSurfaceVariant, onSurfaceVariant = SlateOnSurfaceVariant,
    error = SlateError, onError = SlateOnError,
    errorContainer = SlateErrorContainer, onErrorContainer = SlateOnErrorContainer,
    outline = SlateOutline
)

private fun slateDarkScheme() = darkColorScheme(
    primary = SlatePrimaryDark, onPrimary = SlateOnPrimaryDark,
    primaryContainer = SlatePrimaryContainerDark, onPrimaryContainer = SlateOnPrimaryContainerDark,
    secondary = SlateSecondaryDark, onSecondary = SlateOnSecondaryDark,
    secondaryContainer = SlateSecondaryContainerDark, onSecondaryContainer = SlateOnSecondaryContainerDark,
    tertiary = SlateTertiaryDark, onTertiary = SlateOnTertiaryDark,
    tertiaryContainer = SlateTertiaryContainerDark, onTertiaryContainer = SlateOnTertiaryContainerDark,
    background = SlateBackgroundDark, onBackground = SlateOnBackgroundDark,
    surface = SlateSurfaceDark, onSurface = SlateOnSurfaceDark,
    surfaceVariant = SlateSurfaceVariantDark, onSurfaceVariant = SlateOnSurfaceVariantDark,
    error = SlateErrorDark, onError = SlateOnErrorDark,
    errorContainer = SlateErrorContainerDark, onErrorContainer = SlateOnErrorContainerDark,
    outline = SlateOutlineDark
)

private fun berryLightScheme() = lightColorScheme(
    primary = BerryPrimary, onPrimary = BerryOnPrimary,
    primaryContainer = BerryPrimaryContainer, onPrimaryContainer = BerryOnPrimaryContainer,
    secondary = BerrySecondary, onSecondary = BerryOnSecondary,
    secondaryContainer = BerrySecondaryContainer, onSecondaryContainer = BerryOnSecondaryContainer,
    tertiary = BerryTertiary, onTertiary = BerryOnTertiary,
    tertiaryContainer = BerryTertiaryContainer, onTertiaryContainer = BerryOnTertiaryContainer,
    background = BerryBackground, onBackground = BerryOnBackground,
    surface = BerrySurface, onSurface = BerryOnSurface,
    surfaceVariant = BerrySurfaceVariant, onSurfaceVariant = BerryOnSurfaceVariant,
    error = BerryError, onError = BerryOnError,
    errorContainer = BerryErrorContainer, onErrorContainer = BerryOnErrorContainer,
    outline = BerryOutline
)

private fun berryDarkScheme() = darkColorScheme(
    primary = BerryPrimaryDark, onPrimary = BerryOnPrimaryDark,
    primaryContainer = BerryPrimaryContainerDark, onPrimaryContainer = BerryOnPrimaryContainerDark,
    secondary = BerrySecondaryDark, onSecondary = BerryOnSecondaryDark,
    secondaryContainer = BerrySecondaryContainerDark, onSecondaryContainer = BerryOnSecondaryContainerDark,
    tertiary = BerryTertiaryDark, onTertiary = BerryOnTertiaryDark,
    tertiaryContainer = BerryTertiaryContainerDark, onTertiaryContainer = BerryOnTertiaryContainerDark,
    background = BerryBackgroundDark, onBackground = BerryOnBackgroundDark,
    surface = BerrySurfaceDark, onSurface = BerryOnSurfaceDark,
    surfaceVariant = BerrySurfaceVariantDark, onSurfaceVariant = BerryOnSurfaceVariantDark,
    error = BerryErrorDark, onError = BerryOnErrorDark,
    errorContainer = BerryErrorContainerDark, onErrorContainer = BerryOnErrorContainerDark,
    outline = BerryOutlineDark
)

private fun amoledScheme() = darkColorScheme(
    primary = AmoledPrimary, onPrimary = AmoledOnPrimary,
    primaryContainer = AmoledPrimaryContainer, onPrimaryContainer = AmoledOnPrimaryContainer,
    secondary = AmoledSecondary, onSecondary = AmoledOnSecondary,
    secondaryContainer = AmoledSecondaryContainer, onSecondaryContainer = AmoledOnSecondaryContainer,
    tertiary = AmoledTertiary, onTertiary = AmoledOnTertiary,
    tertiaryContainer = AmoledTertiaryContainer, onTertiaryContainer = AmoledOnTertiaryContainer,
    background = AmoledBackground, onBackground = AmoledOnBackground,
    surface = AmoledSurface, onSurface = AmoledOnSurface,
    surfaceVariant = AmoledSurfaceVariant, onSurfaceVariant = AmoledOnSurfaceVariant,
    error = AmoledError, onError = AmoledOnError,
    errorContainer = AmoledErrorContainer, onErrorContainer = AmoledOnErrorContainer,
    outline = AmoledOutline
)
