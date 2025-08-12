package com.thenewkenya.ingrediet.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.FoodBank
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Typography
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(navController: NavController) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    LaunchedEffect(Unit) { NotificationUtils.ensureChannels(context) }
    
    // Check current notification permission status
    var notifPermissionGranted by remember { 
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else {
                true // No runtime permission needed on older Android versions
            }
        )
    }
    
    // Main notification toggle - initialize based on permission status
    var masterNotificationsEnabled by remember { mutableStateOf(notifPermissionGranted) }
    
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        notifPermissionGranted = granted
        if (granted) {
            masterNotificationsEnabled = true
        }
    }
    
    // Automatically request permission when screen opens if not granted
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notifPermissionGranted) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    
    // Individual notification toggles - initialize based on permission status
    var mealPlanRemindersEnabled by remember { mutableStateOf(notifPermissionGranted) }
    var recipeRecommendationsEnabled by remember { mutableStateOf(notifPermissionGranted) }
    var shoppingListRemindersEnabled by remember { mutableStateOf(notifPermissionGranted) }
    var appUpdatesEnabled by remember { mutableStateOf(notifPermissionGranted) }
    var promotionsEnabled by remember { mutableStateOf(false) }

    // Schedules state
    var breakfastTime by remember { mutableStateOf(Pair(8, 0)) }
    var lunchTime by remember { mutableStateOf(Pair(13, 0)) }
    var dinnerTime by remember { mutableStateOf(Pair(19, 0)) }
    var shoppingDay by remember { mutableStateOf(Calendar.SATURDAY) }
    var shoppingTime by remember { mutableStateOf(Pair(17, 0)) }
    var recipesDay by remember { mutableStateOf(Calendar.MONDAY) }
    var recipesTime by remember { mutableStateOf(Pair(9, 0)) }
    var goalsTime by remember { mutableStateOf(Pair(20, 30)) }
    var hydrationEnabled by remember { mutableStateOf(notifPermissionGranted) }
    val hydrationTimes = remember { listOf(Pair(9,0), Pair(12,0), Pair(15,0), Pair(18,0)) }

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
            
            // Permission status card
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notifPermissionGranted) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = colors.errorContainer.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.NotificationsOff,
                            contentDescription = null,
                            tint = colors.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Notification Permission Required",
                                style = typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = colors.onErrorContainer
                            )
                            Text(
                                text = "Grant permission to receive notifications",
                                style = typography.bodySmall,
                                color = colors.onErrorContainer.copy(alpha = 0.8f)
                            )
                        }
                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Grant")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
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
                        if (it && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notifPermissionGranted) {
                            // Request permission first
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            masterNotificationsEnabled = it
                        }
                        
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
                        onCheckedChange = { 
                            mealPlanRemindersEnabled = it
                            if (it && masterNotificationsEnabled) {
                                // Auto-schedule when enabled
                                NotificationScheduler.scheduleMealReminder(context, breakfastTime.first, breakfastTime.second, "Breakfast")
                                NotificationScheduler.scheduleMealReminder(context, lunchTime.first, lunchTime.second, "Lunch")
                                NotificationScheduler.scheduleMealReminder(context, dinnerTime.first, dinnerTime.second, "Dinner")
                            }
                        },
                        enabled = masterNotificationsEnabled
                    )

                    if (mealPlanRemindersEnabled && masterNotificationsEnabled) {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            MealTimeSelector(
                                mealName = "Breakfast",
                                time = breakfastTime,
                                onTimeChange = { hour, minute ->
                                    breakfastTime = Pair(hour, minute)
                                    NotificationScheduler.scheduleMealReminder(context, hour, minute, "Breakfast")
                                },
                                colors = colors,
                                typography = typography
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            MealTimeSelector(
                                mealName = "Lunch",
                                time = lunchTime,
                                onTimeChange = { hour, minute ->
                                    lunchTime = Pair(hour, minute)
                                    NotificationScheduler.scheduleMealReminder(context, hour, minute, "Lunch")
                                },
                                colors = colors,
                                typography = typography
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            MealTimeSelector(
                                mealName = "Dinner",
                                time = dinnerTime,
                                onTimeChange = { hour, minute ->
                                    dinnerTime = Pair(hour, minute)
                                    NotificationScheduler.scheduleMealReminder(context, hour, minute, "Dinner")
                                },
                                colors = colors,
                                typography = typography
                            )
                        }
                    }
                    
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
                    if (recipeRecommendationsEnabled && masterNotificationsEnabled) {
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("Weekly on ${dayName(recipesDay)} at %02d:%02d".format(recipesTime.first, recipesTime.second), modifier = Modifier.weight(1f))
                        }
                    }
                    
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
                        onCheckedChange = { 
                            shoppingListRemindersEnabled = it
                            if (it && masterNotificationsEnabled) {
                                // Auto-schedule when enabled
                                NotificationScheduler.scheduleShoppingReminder(context, shoppingDay, shoppingTime.first, shoppingTime.second)
                            }
                        },
                        enabled = masterNotificationsEnabled
                    )
                    if (shoppingListRemindersEnabled && masterNotificationsEnabled) {
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("${dayName(shoppingDay)} at %02d:%02d".format(shoppingTime.first, shoppingTime.second), modifier = Modifier.weight(1f))
                        }
                    }
                    
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
                    // Goals
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color = colors.outlineVariant.copy(alpha = 0.5f)
                    )
                    NotificationToggleItem(
                        title = "Goals & Streaks",
                        description = "Reminders to hit calorie/macro targets",
                        icon = Icons.Outlined.Notifications,
                        isChecked = masterNotificationsEnabled,
                        onCheckedChange = { /* use master */ },
                        enabled = masterNotificationsEnabled
                    )
                    if (masterNotificationsEnabled) {
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("Daily at %02d:%02d".format(goalsTime.first, goalsTime.second), modifier = Modifier.weight(1f))
                        }
                    }
                    // Hydration
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color = colors.outlineVariant.copy(alpha = 0.5f)
                    )
                    NotificationToggleItem(
                        title = "Hydration",
                        description = "Reminders to drink water",
                        icon = Icons.Outlined.Notifications,
                        isChecked = hydrationEnabled && masterNotificationsEnabled,
                        onCheckedChange = { 
                            hydrationEnabled = it
                            if (it && masterNotificationsEnabled) {
                                // Auto-schedule when enabled
                                hydrationTimes.forEach { (h,m) -> 
                                    NotificationScheduler.scheduleHydrationReminder(context, h, m) 
                                }
                            }
                        },
                        enabled = masterNotificationsEnabled
                    )
                    if (hydrationEnabled && masterNotificationsEnabled) {
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("Default times: ${hydrationTimes.joinToString { "%02d:%02d".format(it.first, it.second) }}", modifier = Modifier.weight(1f))
                        }
                    }
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

private fun dayName(day: Int): String = when(day) {
    Calendar.MONDAY -> "Monday"
    Calendar.TUESDAY -> "Tuesday"
    Calendar.WEDNESDAY -> "Wednesday"
    Calendar.THURSDAY -> "Thursday"
    Calendar.FRIDAY -> "Friday"
    Calendar.SATURDAY -> "Saturday"
    Calendar.SUNDAY -> "Sunday"
    else -> ""
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealTimeSelector(
    mealName: String,
    time: Pair<Int, Int>,
    onTimeChange: (Int, Int) -> Unit,
    colors: ColorScheme,
    typography: Typography
) {
    var showTimePicker by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = mealName,
            style = typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = colors.onSurface
        )
        
        OutlinedButton(
            onClick = { showTimePicker = true },
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = colors.primary
            ),
            border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.5f))
        ) {
            Icon(
                imageVector = Icons.Outlined.Schedule,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "%02d:%02d".format(time.first, time.second),
                style = typography.labelLarge.copy(fontWeight = FontWeight.Medium)
            )
        }
    }
    
    if (showTimePicker) {
        TimePickerDialog(
            initialTime = time,
            onTimeSelected = { hour, minute ->
                onTimeChange(hour, minute)
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false },
            colors = colors,
            typography = typography
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    initialTime: Pair<Int, Int>,
    onTimeSelected: (Int, Int) -> Unit,
    onDismiss: () -> Unit,
    colors: ColorScheme,
    typography: Typography
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialTime.first,
        initialMinute = initialTime.second,
        is24Hour = true
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select Time",
                style = typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold)
            )
        },
        text = {
            TimePicker(
                state = timePickerState,
                colors = TimePickerDefaults.colors(
                    clockDialColor = colors.surfaceVariant,
                    clockDialSelectedContentColor = colors.onPrimary,
                    clockDialUnselectedContentColor = colors.onSurfaceVariant,
                    selectorColor = colors.primary,
                    containerColor = colors.surface,
                    periodSelectorBorderColor = colors.outline,
                    timeSelectorSelectedContainerColor = colors.primary,
                    timeSelectorUnselectedContainerColor = colors.surfaceVariant,
                    timeSelectorSelectedContentColor = colors.onPrimary,
                    timeSelectorUnselectedContentColor = colors.onSurfaceVariant
                )
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onTimeSelected(timePickerState.hour, timePickerState.minute)
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Set Time")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = colors.surface
    )
} 