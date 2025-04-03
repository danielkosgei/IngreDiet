package com.thenewkenya.ingrediet.feature.profile

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LocalDining
import androidx.compose.material.icons.outlined.MonitorWeight
import androidx.compose.material.icons.outlined.NoFood
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val scrollState = rememberScrollState()
    
    // Define settings states
    var notificationsEnabled by remember { mutableStateOf(true) }
    var darkModeEnabled by remember { mutableStateOf(false) }
    var biometricAuthEnabled by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.navigateUp() },
                        modifier = Modifier
                            .padding(8.dp)
                            .size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background,
                    titleContentColor = colors.onBackground
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Notifications Settings
            SettingsCategoryHeader(title = "Notifications")
            
            SettingsToggleItem(
                title = "Push Notifications",
                subtitle = "Get notified about meal plans and recipe suggestions",
                icon = Icons.Outlined.Notifications,
                iconTint = colors.primary,
                isChecked = notificationsEnabled,
                onCheckedChange = { notificationsEnabled = it }
            )
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = colors.outlineVariant.copy(alpha = 0.3f)
            )
            
            // Appearance Settings
            SettingsCategoryHeader(title = "Appearance")
            
            SettingsToggleItem(
                title = "Dark Mode",
                subtitle = "Toggle between light and dark themes",
                icon = Icons.Outlined.DarkMode,
                iconTint = colors.secondary,
                isChecked = darkModeEnabled,
                onCheckedChange = { darkModeEnabled = it }
            )
            
            SettingsItem(
                title = "Language",
                subtitle = "English (US)",
                icon = Icons.Outlined.Language,
                iconTint = colors.tertiary,
                onClick = { /* Language settings */ }
            )
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = colors.outlineVariant.copy(alpha = 0.3f)
            )
            
            // Diet & Nutrition Settings
            SettingsCategoryHeader(title = "Diet & Nutrition")
            
            SettingsItem(
                title = "Dietary Preferences",
                subtitle = "Vegetarian, Vegan, Keto, etc.",
                icon = Icons.Outlined.LocalDining,
                iconTint = colors.tertiary,
                onClick = { navController.navigate("profile/diet-preferences") }
            )
            
            SettingsItem(
                title = "Allergies & Restrictions",
                subtitle = "Manage food allergies and restrictions",
                icon = Icons.Outlined.NoFood,
                iconTint = colors.error,
                onClick = { navController.navigate("profile/allergies") }
            )
            
            SettingsItem(
                title = "Nutrition Goals",
                subtitle = "Calorie targets and macro preferences",
                icon = Icons.Outlined.MonitorWeight,
                iconTint = colors.primary,
                onClick = { navController.navigate("profile/nutrition-goals?isOnboarding=false") }
            )
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = colors.outlineVariant.copy(alpha = 0.3f)
            )
            
            // Privacy & Security Settings
            SettingsCategoryHeader(title = "Privacy & Security")
            
            SettingsToggleItem(
                title = "Biometric Authentication",
                subtitle = "Use fingerprint or face ID to secure the app",
                icon = Icons.Outlined.Security,
                iconTint = colors.error,
                isChecked = biometricAuthEnabled,
                onCheckedChange = { biometricAuthEnabled = it }
            )
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = colors.outlineVariant.copy(alpha = 0.3f)
            )
            
            // App Preferences
            SettingsCategoryHeader(title = "App Preferences")
            
            SettingsItem(
                title = "Measurement Units",
                subtitle = "Metric (g, ml, °C)",
                icon = Icons.Outlined.Tune,
                iconTint = colors.primary,
                onClick = { /* Measurement units settings */ }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun SettingsCategoryHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun SettingsItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon with colorful background
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = iconTint.copy(alpha = 0.1f)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Title and subtitle
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = colors.onBackground
            )
            
            Text(
                text = subtitle,
                style = typography.bodySmall,
                color = colors.onSurfaceVariant
            )
        }
        
        // Trailing content (can be customized)
        IconButton(onClick = onClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Navigate",
                modifier = Modifier.size(20.dp),
                tint = colors.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SettingsToggleItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon with colorful background
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = iconTint.copy(alpha = 0.1f)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Title and subtitle
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = colors.onBackground
            )
            
            Text(
                text = subtitle,
                style = typography.bodySmall,
                color = colors.onSurfaceVariant
            )
        }
        
        // Switch
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            thumbContent = if (isChecked) {
                {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(SwitchDefaults.IconSize),
                    )
                }
            } else {
                null
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.primary,
                checkedTrackColor = colors.primaryContainer,
                checkedIconColor = colors.onPrimary,
                uncheckedThumbColor = colors.outline,
                uncheckedTrackColor = colors.surfaceVariant
            )
        )
    }
} 