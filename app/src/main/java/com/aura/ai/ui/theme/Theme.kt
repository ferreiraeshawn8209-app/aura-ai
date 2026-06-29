package com.aura.ai.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val AuraDarkColorScheme = darkColorScheme(
    primary = AuraPurple,
    onPrimary = AuraUserText,
    primaryContainer = AuraPurpleDark,
    onPrimaryContainer = AuraOnSurface,
    secondary = AuraAccent,
    onSecondary = AuraBackground,
    background = AuraBackground,
    onBackground = AuraOnSurface,
    surface = AuraSurface,
    onSurface = AuraOnSurface,
    surfaceVariant = AuraSurfaceVariant,
    onSurfaceVariant = AuraOnSurface,
    error = AuraError
)

@Composable
fun AuraTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AuraDarkColorScheme,
        typography = AuraTypography,
        content = content
    )
}
