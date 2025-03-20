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

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    primaryContainer = SurfaceLight,
    onPrimaryContainer = TextPrimary,
    secondary = Secondary,
    onSecondary = Color.White,
    secondaryContainer = SurfaceLight,
    onSecondaryContainer = TextPrimary,
    tertiary = Primary,
    onTertiary = Color.White,
    tertiaryContainer = SurfaceLight,
    onTertiaryContainer = TextPrimary,
    background = Background,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = NavBarColor,
    onSurfaceVariant = NavBarIconInactive,
    error = Error,
    onError = Color.White,
    errorContainer = Error.copy(alpha = 0.1f),
    onErrorContainer = Error
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = Color.White,
    primaryContainer = DarkSurfaceLight,
    onPrimaryContainer = DarkTextPrimary,
    secondary = DarkSecondary,
    onSecondary = Color.White,
    secondaryContainer = DarkSurfaceLight,
    onSecondaryContainer = DarkTextPrimary,
    tertiary = DarkPrimary,
    onTertiary = Color.White,
    tertiaryContainer = DarkSurfaceLight,
    onTertiaryContainer = DarkTextPrimary,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkNavBarColor,
    onSurfaceVariant = DarkNavBarIconInactive,
    error = Error,
    onError = Color.White,
    errorContainer = Error.copy(alpha = 0.1f),
    onErrorContainer = Error
)

@Composable
fun IngreDietTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}