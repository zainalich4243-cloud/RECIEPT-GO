package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = NavyPrimary,
    onPrimary = Color.White,
    primaryContainer = Slate100,
    onPrimaryContainer = Slate900,
    secondary = TealAccent,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCCFBF1),
    onSecondaryContainer = Color(0xFF115E59),
    tertiary = BlueAccent,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFDBEAFE),
    onTertiaryContainer = Color(0xFF1E40AF),
    background = Slate50,
    onBackground = Slate900,
    surface = Color.White,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate600,
    outline = Slate300,
    outlineVariant = Slate200,
    error = RoseError,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF94A3B8),
    onPrimary = Slate900,
    primaryContainer = Slate800,
    onPrimaryContainer = Color.White,
    secondary = TealLight,
    onSecondary = Slate900,
    background = Color(0xFF0B0F19),
    surface = Color(0xFF111827),
    onSurface = Color(0xFFF8FAFC),
    outline = Slate700
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Business apps prefer crisp light contrast
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
