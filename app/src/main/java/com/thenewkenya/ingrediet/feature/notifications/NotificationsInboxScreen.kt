package com.thenewkenya.ingrediet.feature.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsInboxScreen(navController: NavController) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val context = LocalContext.current
    var all by remember { mutableStateOf(NotificationStore.getAll(context)) }
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf(
        NotificationTab("All", Icons.Outlined.Notifications),
        NotificationTab("Meals", Icons.Outlined.Restaurant),
        NotificationTab("Shopping", Icons.Outlined.ShoppingCart),
        NotificationTab("Recipes", Icons.Outlined.Campaign),
        NotificationTab("Goals", Icons.Outlined.Notifications),
        NotificationTab("Hydration", Icons.Outlined.Notifications)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            text = "Notifications", 
                            style = typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        val unreadCount = all.count { !it.isRead }
                        if (unreadCount > 0) {
                            Text(
                                text = "$unreadCount unread",
                                style = typography.bodySmall,
                                color = colors.primary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (all.any { !it.isRead }) {
                        TextButton(
                            onClick = {
                                NotificationStore.markAllRead(context)
                                all = NotificationStore.getAll(context)
                            }
                        ) { 
                            Text(
                                "Mark all read",
                                style = typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                            ) 
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.surface,
                    titleContentColor = colors.onSurface
                )
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .background(colors.background)
        ) {
            // Enhanced Tab Section
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .border(1.dp, colors.outline.copy(alpha = 0.12f), RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                color = colors.surface
            ) {
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = colors.primary,
                    indicator = { tabPositions ->
                        if (selectedTab < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                height = 3.dp,
                                color = colors.primary
                            )
                        }
                    },
                    divider = { }
                ) {
                    tabs.forEachIndexed { idx, tab ->
                        Tab(
                            selected = selectedTab == idx,
                            onClick = { selectedTab = idx },
                            modifier = Modifier.padding(vertical = 12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = if (selectedTab == idx) colors.primary else colors.onSurfaceVariant
                                )
                                Text(
                                    text = tab.label,
                                    style = typography.labelLarge.copy(
                                        fontWeight = if (selectedTab == idx) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (selectedTab == idx) colors.primary else colors.onSurfaceVariant
                                )
                                // Show count badge for each category
                                val categoryCount = when (idx) {
                                    1 -> all.count { it.type == "meal" && !it.isRead }
                                    2 -> all.count { it.type == "shopping" && !it.isRead }
                                    3 -> all.count { it.type == "recipes" && !it.isRead }
                                    4 -> all.count { it.type == "goals" && !it.isRead }
                                    5 -> all.count { it.type == "hydration" && !it.isRead }
                                    else -> all.count { !it.isRead }
                                }
                                if (categoryCount > 0) {
                                                        Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(colors.primary.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                            .border(1.dp, colors.primary.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                                                                Text(
                            text = if (categoryCount > 9) "9+" else categoryCount.toString(),
                            style = typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = colors.primary
                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            val filtered = remember(all, selectedTab) {
                when (selectedTab) {
                    1 -> all.filter { it.type == "meal" }
                    2 -> all.filter { it.type == "shopping" }
                    3 -> all.filter { it.type == "recipes" }
                    4 -> all.filter { it.type == "goals" }
                    5 -> all.filter { it.type == "hydration" }
                    else -> all
                }.sortedByDescending { it.timestamp } // Most recent first
            }

            if (filtered.isEmpty()) {
                EmptyNotificationsState(selectedTab = selectedTab, tabName = tabs[selectedTab].label)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(filtered) { entry ->
                        EnhancedNotificationCard(
                            entry = entry,
                            onClick = {
                                NotificationStore.markRead(context, entry.id, true)
                                all = NotificationStore.getAll(context)
                            }
                        )
                    }
                }
            }
        }
    }
}

// Data class for tab structure
private data class NotificationTab(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
private fun EnhancedNotificationCard(entry: NotificationEntry, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (entry.isRead) colors.outline.copy(alpha = 0.12f) else colors.primary.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() },
        color = if (entry.isRead) colors.surface else colors.primaryContainer.copy(alpha = 0.05f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Enhanced icon with background
            val (icon, iconColor) = when (entry.type) {
                "meal" -> Icons.Outlined.Restaurant to Color(0xFF4CAF50)
                "shopping" -> Icons.Outlined.ShoppingCart to Color(0xFF2196F3)
                "recipes" -> Icons.Outlined.Campaign to Color(0xFFFF9800)
                "goals" -> Icons.Outlined.Notifications to Color(0xFF9C27B0)
                "hydration" -> Icons.Outlined.Notifications to Color(0xFF00BCD4)
                else -> Icons.Outlined.Notifications to colors.primary
            }
            
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(iconColor.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                    .border(1.dp, iconColor.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = iconColor
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Content column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Title and timestamp row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = entry.title,
                        style = typography.titleMedium.copy(
                            fontWeight = if (entry.isRead) FontWeight.Medium else FontWeight.Bold
                        ),
                        color = colors.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Text(
                        text = formatRelativeTime(entry.timestamp),
                        style = typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }
                
                // Message
                Text(
                    text = entry.message,
                    style = typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                    lineHeight = typography.bodyMedium.lineHeight * 1.3
                )
                
                // Category tag
                Surface(
                    color = iconColor.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .wrapContentWidth()
                        .border(1.dp, iconColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                ) {
                    Text(
                        text = entry.type.replaceFirstChar { it.uppercase() },
                        style = typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = iconColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            // Unread indicator
            if (!entry.isRead) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(colors.primary, androidx.compose.foundation.shape.CircleShape)
                )
            }
        }
    }
}

@Composable
private fun EmptyNotificationsState(selectedTab: Int, tabName: String) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            // Empty state icon
            val emptyIcon = when (selectedTab) {
                1 -> Icons.Outlined.Restaurant
                2 -> Icons.Outlined.ShoppingCart
                3 -> Icons.Outlined.Campaign
                4 -> Icons.Outlined.Notifications
                5 -> Icons.Outlined.Notifications
                else -> Icons.Outlined.Notifications
            }
            
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(colors.surfaceVariant.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                    .border(1.dp, colors.outline.copy(alpha = 0.15f), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = emptyIcon,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = colors.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            
            Text(
                text = if (selectedTab == 0) "No notifications yet" else "No $tabName notifications",
                style = typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = colors.onSurface
            )
            
            Text(
                text = when (selectedTab) {
                    1 -> "Meal reminders and suggestions will appear here"
                    2 -> "Shopping list updates will appear here"
                    3 -> "Recipe recommendations will appear here"
                    4 -> "Goal updates and achievements will appear here"
                    5 -> "Hydration reminders will appear here"
                    else -> "You're all caught up! Notifications will appear here when you receive them."
                },
                style = typography.bodyMedium,
                color = colors.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

// Helper function to format relative time
private fun formatRelativeTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000}m ago"
        diff < 86400_000 -> "${diff / 3600_000}h ago"
        diff < 604800_000 -> "${diff / 86400_000}d ago"
        else -> "1w+ ago"
    }
} 