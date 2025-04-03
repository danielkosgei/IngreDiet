package com.thenewkenya.ingrediet.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.FoodBank
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(navController: NavController) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val scrollState = rememberScrollState()
    
    // Main notification toggle
    var masterNotificationsEnabled by remember { mutableStateOf(true) }
    
    // Individual notification toggles
    var mealPlanRemindersEnabled by remember { mutableStateOf(true) }
    var recipeRecommendationsEnabled by remember { mutableStateOf(true) }
    var shoppingListRemindersEnabled by remember { mutableStateOf(true) }
    var appUpdatesEnabled by remember { mutableStateOf(true) }
    var promotionsEnabled by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Notifications",
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
            
            // Master notifications toggle
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                NotificationToggleItem(
                    title = "All Notifications",
                    description = "Enable or disable all notifications",
                    icon = Icons.Outlined.NotificationsActive,
                    isChecked = masterNotificationsEnabled,
                    onCheckedChange = { 
                        masterNotificationsEnabled = it
                        // If master is disabled, disable all, otherwise leave them as is
                        if (!it) {
                            mealPlanRemindersEnabled = false
                            recipeRecommendationsEnabled = false
                            shoppingListRemindersEnabled = false
                            appUpdatesEnabled = false
                            promotionsEnabled = false
                        }
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Individual notification settings
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 8.dp)
                ) {
                    Text(
                        text = "Notification Settings",
                        style = typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    NotificationToggleItem(
                        title = "Meal Plan Reminders",
                        description = "Get reminders for your planned meals",
                        icon = Icons.Outlined.Restaurant,
                        isChecked = mealPlanRemindersEnabled && masterNotificationsEnabled,
                        onCheckedChange = { mealPlanRemindersEnabled = it },
                        enabled = masterNotificationsEnabled
                    )
                    
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color = colors.outlineVariant.copy(alpha = 0.5f)
                    )
                    
                    NotificationToggleItem(
                        title = "Recipe Recommendations",
                        description = "Get personalized recipe suggestions",
                        icon = Icons.Outlined.FoodBank,
                        isChecked = recipeRecommendationsEnabled && masterNotificationsEnabled,
                        onCheckedChange = { recipeRecommendationsEnabled = it },
                        enabled = masterNotificationsEnabled
                    )
                    
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color = colors.outlineVariant.copy(alpha = 0.5f)
                    )
                    
                    NotificationToggleItem(
                        title = "Shopping List Reminders",
                        description = "Get reminders about your shopping list",
                        icon = Icons.Outlined.ShoppingCart,
                        isChecked = shoppingListRemindersEnabled && masterNotificationsEnabled,
                        onCheckedChange = { shoppingListRemindersEnabled = it },
                        enabled = masterNotificationsEnabled
                    )
                    
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color = colors.outlineVariant.copy(alpha = 0.5f)
                    )
                    
                    NotificationToggleItem(
                        title = "App Updates",
                        description = "Get notified about new app features",
                        icon = Icons.Outlined.NewReleases,
                        isChecked = appUpdatesEnabled && masterNotificationsEnabled,
                        onCheckedChange = { appUpdatesEnabled = it },
                        enabled = masterNotificationsEnabled
                    )
                    
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color = colors.outlineVariant.copy(alpha = 0.5f)
                    )
                    
                    NotificationToggleItem(
                        title = "Promotions & News",
                        description = "Get promotional offers and news",
                        icon = Icons.Outlined.Campaign,
                        isChecked = promotionsEnabled && masterNotificationsEnabled,
                        onCheckedChange = { promotionsEnabled = it },
                        enabled = masterNotificationsEnabled
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun NotificationToggleItem(
    title: String,
    description: String,
    icon: ImageVector,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) colors.primary else colors.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(24.dp)
        )
        
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = title,
                style = typography.bodyLarge,
                color = if (enabled) colors.onSurface else colors.onSurfaceVariant.copy(alpha = 0.5f)
            )
            
            Text(
                text = description,
                style = typography.bodySmall,
                color = colors.onSurfaceVariant.copy(alpha = if (enabled) 0.7f else 0.5f)
            )
        }
        
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
} 