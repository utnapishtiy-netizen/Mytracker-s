package com.example.pixeltracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EInkLightColorScheme = lightColorScheme(
    primary = Color.Black,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E0E0),
    onPrimaryContainer = Color.Black,
    secondary = Color(0xFF424242),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEEEEEE),
    onSecondaryContainer = Color.Black,
    tertiary = Color(0xFF616161),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE0E0E0),
    onTertiaryContainer = Color.Black,
    background = Color(0xFFF5F5F5),
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    surfaceVariant = Color(0xFFEBEBEB),
    onSurfaceVariant = Color(0xFF333333),
    outline = Color(0xFF9E9E9E),
    error = Color.Black,
    onError = Color.White
)

private val EInkDarkColorScheme = darkColorScheme(
    primary = Color.White,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF2C2C2C),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFFB0B0B0),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF1F1F1F),
    onSecondaryContainer = Color.White,
    tertiary = Color(0xFF9E9E9E),
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF2C2C2C),
    onTertiaryContainer = Color.White,
    background = Color.Black,
    onBackground = Color.White,
    surface = Color(0xFF121212),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF1E1E1E),
    onSurfaceVariant = Color(0xFFCCCCCC),
    outline = Color(0xFF616161),
    error = Color.White,
    onError = Color.Black
)

@Composable
fun PixelTrackerTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) EInkDarkColorScheme else EInkLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
