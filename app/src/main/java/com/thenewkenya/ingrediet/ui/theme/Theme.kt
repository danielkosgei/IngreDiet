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
import com.thenewkenya.ingrediet.feature.profile.ThemeMode

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
    primaryContainer = ButtonSecondaryLight,
    onPrimaryContainer = TextPrimary,
    secondary = Secondary,
    onSecondary = Color.White,
    secondaryContainer = SurfaceLight,
    onSecondaryContainer = TextPrimary,
    tertiary = Tertiary,
    onTertiary = Color.White,
    tertiaryContainer = CardLight,
    onTertiaryContainer = TextPrimary,
    background = Background,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = NavBarColor,
    onSurfaceVariant = TextSecondary,
    outline = CardBorderLight,
    outlineVariant = DividerLight,
    scrim = OverlayLight,
    error = Error,
    onError = Color.White,
    errorContainer = Color(0xFFFFF3F3),  // Light Alert Container
    onErrorContainer = Error
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = Color.White,
    primaryContainer = ButtonSecondaryDark,
    onPrimaryContainer = DarkTextPrimary,
    secondary = DarkSecondary,
    onSecondary = Color.White,
    secondaryContainer = DarkSurfaceLight,
    onSecondaryContainer = DarkTextPrimary,
    tertiary = Tertiary,
    onTertiary = Color.White,
    tertiaryContainer = CardDark,
    onTertiaryContainer = DarkTextPrimary,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkNavBarColor,
    onSurfaceVariant = DarkTextSecondary,
    outline = CardBorderDark,
    outlineVariant = DividerDark,
    scrim = OverlayDark,
    error = Error,
    onError = Color.White,
    errorContainer = Color(0xFF2C1F1F),  // Dark Alert Container
    onErrorContainer = Error
)

@Composable
fun IngreDietTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    
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