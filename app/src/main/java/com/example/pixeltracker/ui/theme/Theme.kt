package com.example.pixeltracker.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    // Ваши темные цвета по умолчанию (при желании)
)

private val LightColorScheme = lightColorScheme(
    // Ваши светлые цвета по умолчанию (при желании)
)

@Composable
fun PixelTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // Определяет, включена ли темная тема в системе
    dynamicColor: Boolean = true, // Динамические цвета Material You (Android 12+)
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // Если устройство поддерживает Material You (Android 12+), берем системные цвета
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
