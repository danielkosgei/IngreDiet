package com.thenewkenya.ingrediet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.thenewkenya.ingrediet.ui.theme.IngreDietTheme
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable


val supabase = createSupabaseClient(
    supabaseUrl = BuildConfig.SUPABASE_URL,
    supabaseKey = BuildConfig.SUPABASE_ANON_KEY,
) {
    install(Postgrest)
}

@Serializable
data class Country(
    val id: Int,
    val name: String,
)



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

@Composable
fun CountriesList() {
    var countries by remember { mutableStateOf<List<Country>>(listOf()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                countries = supabase.from("countries").select().decodeList<Country>()
                errorMessage = null
            } catch (e: Exception) {
                errorMessage = "Failed to load data: ${e.message}"
            }
        }
    }

    if (errorMessage != null) {
        Text(
            text = errorMessage!!,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(16.dp)
        )
    } else {
        Column {
            var newCountry by remember { mutableStateOf("") }
            val composableScope = rememberCoroutineScope()
            LazyColumn {
                items(countries, key = { country -> country.id }) { country ->
                    Text(country.name, modifier = Modifier.padding(8.dp).animateItem())
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(value = newCountry, onValueChange = { newCountry = it },
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = {
                    composableScope.launch(Dispatchers.IO) {
                        val country = supabase.from("countries").insert(mapOf("name" to newCountry)) {
                            select()
                            single()
                        }.decodeAs<Country>()
                        countries = countries + country
                        newCountry = ""
                    }
                }) {
                    Text("Add")
                }
            }


        }
    }
}
