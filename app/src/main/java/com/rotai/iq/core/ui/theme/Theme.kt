package com.rotai.iq.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = RotaOrangePrimary,
    onPrimary = RotaBlack,
    primaryContainer = RotaCardElevated,
    onPrimaryContainer = RotaTextPrimary,
    secondary = RotaOrangeLight,
    onSecondary = RotaBlack,
    background = RotaBlack,
    onBackground = RotaTextPrimary,
    surface = RotaDarkCanvas,
    onSurface = RotaTextPrimary,
    surfaceVariant = RotaCardBackground,
    onSurfaceVariant = RotaTextSecondary,
    outline = RotaBorderSubtle,
    outlineVariant = RotaBorderMedium
)

@Composable
fun RotaIQTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        shapes = RotaShapes,
        content = content
    )
}
