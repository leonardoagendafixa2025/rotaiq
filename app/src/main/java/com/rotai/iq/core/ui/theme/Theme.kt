package com.rotai.iq.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = BrandPrimary,
    secondary = BrandSecondary,
    background = CockpitBackground,
    surface = CockpitSurface,
    surfaceVariant = CockpitSurfaceVariant,
    onPrimary = CockpitBackground,
    onSecondary = CockpitBackground,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outline = CockpitBorder
)

@Composable
fun RotaIQTheme(
    content: @Composable () -> Unit
) {
    // ROTA IQ é nativamente cockpit dark para máxima visibilidade e ergonomia ao volante
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
