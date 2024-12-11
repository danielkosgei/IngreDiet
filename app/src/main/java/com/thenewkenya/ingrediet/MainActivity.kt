package com.thenewkenya.ingrediet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.thenewkenya.ingrediet.ui.theme.IngreDietTheme
import dagger.hilt.android.AndroidEntryPoint


data class BottomNavItem(
    val title: String,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val hasNews: Boolean,
    val badges: Int
)

val bottomNavItems = listOf(
    BottomNavItem("Home", "home", Icons.Filled.Home, Icons.Outlined.Home, false, 0),
    BottomNavItem("Recipes", "recipes", Icons.Filled.DateRange, Icons.Outlined.DateRange, false, 0),
    BottomNavItem("Notifications", "notifications", Icons.Filled.Notifications, Icons.Outlined.Notifications, false, 3),
    BottomNavItem("Profile", "profile", Icons.Filled.Person, Icons.Outlined.Person, false, 8)
)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IngreDietTheme {
                val navController = rememberNavController()
                var selected by remember { mutableIntStateOf(0) }

                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            bottomNavItems.forEachIndexed { index, bottomNavItem ->
                                NavigationBarItem(
                                    selected = index == selected,
                                    onClick = {
                                        selected = index
                                        navController.navigate(bottomNavItem.route) {
                                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = {
                                        BadgedBox(
                                            badge = {
                                                if (bottomNavItem.badges != 0) {
                                                    Badge { Text(text = bottomNavItem.badges.toString()) }
                                                } else if (bottomNavItem.hasNews) {
                                                    Badge()
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = if (index == selected) bottomNavItem.selectedIcon else bottomNavItem.unselectedIcon,
                                                contentDescription = bottomNavItem.title
                                            )
                                        }
                                    },
                                    label = { Text(text = bottomNavItem.title) }
                                )
                            }
                        }
                    },

                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = bottomNavItems[0].route,
                        Modifier.padding(innerPadding)
                    ) {
                        composable("home") { HomeScreen(navController = navController) }
                        composable("recipes") { RecipesScreen() }
                        composable("create") { CreateScreen(navController = navController) }
                        composable("notifications") { NotificationsScreen() }
                        composable("profile") { ProfileScreen() }
                    }
                }
            }
        }
    }
}


