package com.thenewkenya.ingrediet.feature.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
    val tabs = listOf("All", "Meals", "Shopping", "Recipes", "Goals", "Hydration")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", style = typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        NotificationStore.markAllRead(context)
                        all = NotificationStore.getAll(context)
                    }) { Text("Mark all read") }
                }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { idx, label ->
                    Tab(
                        selected = selectedTab == idx,
                        onClick = { selectedTab = idx },
                        text = { Text(label) }
                    )
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
                }
            }
            if (filtered.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No notifications", color = colors.onSurfaceVariant)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(12.dp)) {
                    items(filtered) { entry ->
                        NotificationRow(entry = entry, onClick = {
                            NotificationStore.markRead(context, entry.id, true)
                            all = NotificationStore.getAll(context)
                        })
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationRow(entry: NotificationEntry, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val bg = if (entry.isRead) colors.surface else colors.surfaceVariant
    Card(colors = CardDefaults.cardColors(containerColor = bg), modifier = Modifier.fillMaxWidth().clickable { onClick() }) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            val icon = when (entry.type) {
                "meal" -> Icons.Outlined.Restaurant
                "shopping" -> Icons.Outlined.ShoppingCart
                "recipes" -> Icons.Outlined.Campaign
                "goals" -> Icons.Outlined.Notifications
                "hydration" -> Icons.Outlined.Notifications
                else -> Icons.Outlined.Notifications
            }
            Icon(icon, contentDescription = null, tint = colors.primary)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(entry.title, style = typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = colors.onSurface)
                Spacer(Modifier.height(2.dp))
                Text(entry.message, style = typography.bodySmall, color = colors.onSurfaceVariant)
            }
        }
    }
} 