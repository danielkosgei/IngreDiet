package com.thenewkenya.ingrediet.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Updated color definitions for a harmonious health and wellness theme
val PrimaryColor = Color(0xFF4CAF50) // Soft muted green
val SecondaryColor = Color(0xFF64B5F6) // Gentle blue
val BackgroundColor = Color(0xFFF5F5F5) // Light neutral
val SurfaceColor = Color(0xFFFFFFFF) // Very light gray
val TextColor = Color(0xFF424242) // Dark gray text

// Dark Theme Colors
val DarkPrimaryColor = Color(0xFF388E3C) // Darker muted green
val DarkSecondaryColor = Color(0xFF42A5F5) // Darker gentle blue
val DarkBackgroundColor = Color(0xFF121212) // Dark background
val DarkSurfaceColor = Color(0xFF1E1E1E) // Dark surface
val DarkTextColor = Color(0xFFFFFFFF) // White text

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimaryColor,
    secondary = DarkSecondaryColor,
    tertiary = Color(0xFF00796B), // DarkTertiary
    background = DarkBackgroundColor,
    surface = DarkSurfaceColor,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = DarkTextColor,
    onSurface = DarkTextColor,
    error = Color(0xFFB00020), // DarkError
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryColor,
    secondary = SecondaryColor,
    tertiary = Color(0xFF009688), // Tertiary
    background = BackgroundColor,
    surface = SurfaceColor,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = TextColor,
    onSurface = TextColor,
    error = Color(0xFFB00020), // Error
    onError = Color.White
)

@Composable
fun IngreDietTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}