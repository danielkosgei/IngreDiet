package com.thenewkenya.ingrediet.feature.profile

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.SettingsBrightness
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.thenewkenya.ingrediet.ui.theme.*

enum class ThemeMode {
    // System mode options
    SYSTEM, LIGHT, DARK,
    
    // Color theme options
    DEFAULT_THEME, PINK_THEME;
    
    companion object {
        // For backwards compatibility
        fun fromLegacy(mode: String): ThemeMode {
            return when (mode) {
                "SYSTEM_LIGHT", "SYSTEM_DARK" -> SYSTEM
                "WELLNESS_LIGHT" -> LIGHT
                "WELLNESS_DARK" -> DARK
                "PINK_LIGHT", "PINK_DARK" -> PINK_THEME
                "CYBERPUNK_LIGHT", "CYBERPUNK_DARK" -> DEFAULT_THEME
                else -> DEFAULT_THEME
            }
        }
    }
}

data class ColorPalette(
    val primary: Color,
    val secondary: Color,
    val tertiary: Color
)

data class ThemeOption(
    val mode: ThemeMode,
    val name: String,
    val description: String,
    val icon: ImageVector,
    val colorPalette: ColorPalette? = null
)

object ThemePreferences {
    private const val PREFS_NAME = "theme_preferences"
    private const val THEME_MODE_KEY = "theme_mode"
    private const val COLOR_THEME_KEY = "color_theme"
    
    // Mutable state for reactive theme changes
    private var _currentThemeMode = mutableStateOf(ThemeMode.SYSTEM)
    private var _currentColorTheme = mutableStateOf(ThemeMode.DEFAULT_THEME)
    val currentThemeMode = _currentThemeMode
    val currentColorTheme = _currentColorTheme
    
    fun getThemeMode(context: Context): ThemeMode {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val mode = prefs.getString(THEME_MODE_KEY, ThemeMode.SYSTEM.name)
        val themeMode = try {
            ThemeMode.valueOf(mode ?: ThemeMode.SYSTEM.name)
        } catch (e: IllegalArgumentException) {
            ThemeMode.fromLegacy(mode ?: "SYSTEM")
        }
        _currentThemeMode.value = themeMode
        return themeMode
    }
    
    fun getColorTheme(context: Context): ThemeMode {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val theme = prefs.getString(COLOR_THEME_KEY, ThemeMode.DEFAULT_THEME.name)
        val colorTheme = try {
            ThemeMode.valueOf(theme ?: ThemeMode.DEFAULT_THEME.name)
        } catch (e: IllegalArgumentException) {
            ThemeMode.fromLegacy(theme ?: "DEFAULT_THEME")
        }
        _currentColorTheme.value = colorTheme
        return colorTheme
    }
    
    fun setThemeMode(context: Context, themeMode: ThemeMode) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(THEME_MODE_KEY, themeMode.name).apply()
        _currentThemeMode.value = themeMode
    }
    
    fun setColorTheme(context: Context, colorTheme: ThemeMode) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(COLOR_THEME_KEY, colorTheme.name).apply()
        _currentColorTheme.value = colorTheme
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceScreen(navController: NavController) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    
    var selectedThemeMode by remember { mutableStateOf(ThemeMode.SYSTEM) }
    var selectedColorTheme by remember { mutableStateOf(ThemeMode.DEFAULT_THEME) }
    
    // Load current preferences
    LaunchedEffect(Unit) {
        selectedThemeMode = ThemePreferences.getThemeMode(context)
        selectedColorTheme = ThemePreferences.getColorTheme(context)
    }
    
    val systemModeOptions = listOf(
        ThemeOption(
            mode = ThemeMode.SYSTEM,
            name = "System",
            description = "Follow device settings",
            icon = Icons.Outlined.SettingsBrightness
        ),
        ThemeOption(
            mode = ThemeMode.LIGHT,
            name = "Light",
            description = "Always use light theme",
            icon = Icons.Outlined.LightMode
        ),
        ThemeOption(
            mode = ThemeMode.DARK,
            name = "Dark",
            description = "Always use dark theme",
            icon = Icons.Outlined.DarkMode
        )
    )
    
    val colorThemeOptions = listOf(
        ThemeOption(
            mode = ThemeMode.DEFAULT_THEME,
            name = "Default",
            description = "Wellness green theme",
            icon = Icons.Outlined.LightMode,
            colorPalette = ColorPalette(Primary, Secondary, Tertiary)
        ),
        ThemeOption(
            mode = ThemeMode.PINK_THEME,
            name = "Pink & Purple",
            description = "Elegant pink and purple tones",
            icon = Icons.Outlined.LightMode,
            colorPalette = ColorPalette(PinkPrimary, PinkSecondary, PinkTertiary)
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Appearance",
                        style = typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = "Back",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // System Default Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "System Default",
                        style = typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Column(
                        modifier = Modifier.selectableGroup()
                    ) {
                        systemModeOptions.forEachIndexed { index, option ->
                            ThemeOptionItem(
                                option = option,
                                isSelected = selectedThemeMode == option.mode,
                                onSelected = { 
                                    selectedThemeMode = option.mode
                                    ThemePreferences.setThemeMode(context, option.mode)
                                }
                            )
                            
                            if (index < systemModeOptions.size - 1) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    thickness = 0.5.dp,
                                    color = colors.outlineVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Color Themes Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Themes",
                        style = typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Column(
                        modifier = Modifier.selectableGroup()
                    ) {
                        colorThemeOptions.forEachIndexed { index, option ->
                            ColorThemeOptionItem(
                                option = option,
                                isSelected = selectedColorTheme == option.mode,
                                onSelected = { 
                                    selectedColorTheme = option.mode
                                    ThemePreferences.setColorTheme(context, option.mode)
                                }
                            )
                            
                            if (index < colorThemeOptions.size - 1) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    thickness = 0.5.dp,
                                    color = colors.outlineVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ThemeOptionItem(
    option: ThemeOption,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .selectable(
                selected = isSelected,
                onClick = onSelected,
                role = Role.RadioButton
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = option.icon,
            contentDescription = null,
            tint = if (isSelected) colors.primary else colors.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = option.name,
                style = typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
            )
            Text(
                text = option.description,
                style = typography.bodySmall,
                color = colors.onSurfaceVariant
            )
        }
        
        RadioButton(
            selected = isSelected,
            onClick = null
        )
    }
}

@Composable
fun ColorThemeOptionItem(
    option: ThemeOption,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .selectable(
                selected = isSelected,
                onClick = onSelected,
                role = Role.RadioButton
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Simple color palette preview
        option.colorPalette?.let { palette ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(palette.primary)
                )
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(palette.secondary)
                )
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(palette.tertiary)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = option.name,
                style = typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
            )
            Text(
                text = option.description,
                style = typography.bodySmall,
                color = colors.onSurfaceVariant
            )
        }
        
        RadioButton(
            selected = isSelected,
            onClick = null
        )
    }
} 