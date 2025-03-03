package com.thenewkenya.ingrediet.feature.navigation

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Search

import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.thenewkenya.ingrediet.R
import com.thenewkenya.ingrediet.data.network.AuthManager
import com.thenewkenya.ingrediet.data.network.supabase
import com.thenewkenya.ingrediet.ui.theme.black
import com.thenewkenya.ingrediet.ui.theme.darkGray
import com.thenewkenya.ingrediet.ui.theme.darkTeal
import com.thenewkenya.ingrediet.ui.theme.teal
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Recipe(
    val id: Int,
    val name: String,
    val imageRes: Int,
    val calories: Int,
    val time: String,
    val category: String
)

data class Category(
    val id: Int,
    val name: String,
    val icon: @Composable () -> Unit,
    val gradient: Brush
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }
    val coroutineScope = rememberCoroutineScope()
    val user = supabase.auth.currentUserOrNull()
    var isSigningOut by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    // Sample data
    val recipes = listOf(
        Recipe(1, "Healthy Avocado Toast", R.drawable.ic_google, 320, "15 min", "Breakfast"),
        Recipe(2, "Protein Smoothie Bowl", R.drawable.ic_google, 450, "10 min", "Breakfast"),
        Recipe(3, "Quinoa Veggie Salad", R.drawable.ic_google, 380, "20 min", "Lunch"),
        Recipe(4, "Grilled Chicken with Veggies", R.drawable.ic_google, 520, "30 min", "Dinner")
    )

    val categories = listOf(
        Category(
            1, "All Recipes",
            { Icon(Icons.Filled.LocalDining, contentDescription = "All Recipes", tint = Color.White) },
            Brush.horizontalGradient(colors = listOf(teal, darkTeal))
        ),
        Category(
            2, "Fitness",
            { Icon(Icons.Filled.FitnessCenter, contentDescription = "Fitness", tint = Color.White) },
            Brush.horizontalGradient(colors = listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0)))
        ),
        Category(
            3, "Vegetarian",
            { Icon(Icons.Filled.Favorite, contentDescription = "Vegetarian", tint = Color.White) },
            Brush.horizontalGradient(colors = listOf(Color(0xFFFF416C), Color(0xFFFF4B2B)))
        ),
        Category(
            4, "Hydration",
            { Icon(Icons.Filled.LocalDining, contentDescription = "Hydration", tint = Color.White) },
            Brush.horizontalGradient(colors = listOf(Color(0xFF2193b0), Color(0xFF6dd5ed)))
        )
    )

    // Handle signout in a Launched Effect
    LaunchedEffect(isSigningOut) {
        if (isSigningOut) {
            authManager.signOut()
            navController.navigate("login") {
                popUpTo("home") { inclusive = true }
            }
            isSigningOut = false
        }
    }

    if (user == null) {
        LoadingScreen()
        LaunchedEffect(Unit) {
            authManager.signOut()
            navController.navigate("login") {
                popUpTo("home") { inclusive = true }
            }
        }
    } else {
        Scaffold(
            bottomBar = {
                BottomAppBar(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = darkGray,
                    contentColor = Color.White
                ) {
                    NavigationBarItem(
                        selected = selectedItem == 0,
                        onClick = { selectedItem = 0 },
                        icon = { Icon(Icons.Outlined.Home, contentDescription = "Home") },
                        label = { Text("Home") }
                    )
                    NavigationBarItem(
                        selected = selectedItem == 1,
                        onClick = { selectedItem = 1 },
                        icon = { Icon(Icons.Outlined.Favorite, contentDescription = "Favorites") },
                        label = { Text("Favorites") }
                    )
                    NavigationBarItem(
                        selected = selectedItem == 2,
                        onClick = { selectedItem = 2 },
                        icon = {
                            BadgedBox(badge = { Badge { Text("2") } }) {
                                Icon(Icons.Outlined.FitnessCenter, contentDescription = "My Plan")
                            }
                        },
                        label = { Text("My Plan") }
                    )
                    NavigationBarItem(
                        selected = selectedItem == 3,
                        onClick = {
                            selectedItem = 3
                            navController.navigate("profile")
                        },
                        icon = { Icon(Icons.Outlined.AccountCircle, contentDescription = "Profile") },
                        label = { Text("Profile") }
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(black)
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Top Bar with user greeting and logout
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Hello,",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Text(
                                text = user.email?.substringBefore('@')?.capitalize() ?: "User",
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(
                            onClick = { isSigningOut = true }
                        ) {
                            Icon(
                                Icons.Filled.Logout,
                                contentDescription = "Logout",
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Search Bar
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search for recipes or ingredients") },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Search,
                                contentDescription = "Search",
                                tint = Color.White.copy(alpha = 0.7f)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = darkGray,
                            focusedContainerColor = darkGray,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Daily Summary Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = darkGray
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Today's Summary",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                NutritionItem(
                                    title = "Calories",
                                    value = "1450",
                                    target = "2000",
                                    progress = 0.72f
                                )
                                NutritionItem(
                                    title = "Protein",
                                    value = "65g",
                                    target = "80g",
                                    progress = 0.81f
                                )
                                NutritionItem(
                                    title = "Water",
                                    value = "1.2L",
                                    target = "2.5L",
                                    progress = 0.48f
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = { /* TODO: Navigate to daily summary */ },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = teal
                                ),
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("View Details")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Categories
                    Text(
                        text = "Browse Categories",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(categories) { category ->
                            CategoryItem(category = category)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Today's Recommendation
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recommended for You",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "See All",
                            style = MaterialTheme.typography.bodyMedium,
                            color = teal,
                            modifier = Modifier.clickable { /* TODO: Navigate to all recommendations */ }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(recipes) { recipe ->
                            RecipeCard(recipe = recipe)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Add Ingredients Button
                    Button(
                        onClick = { /* TODO: Navigate to add ingredients screen */ },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = teal
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Filled.AddCircle,
                            contentDescription = "Add",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Add Ingredients From Your Kitchen",
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun NutritionItem(title: String, value: String, target: String, progress: Float) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(80.dp)
                .padding(8.dp)
        ) {
            CircularProgressIndicator(
                progress = progress,
                color = teal,
                strokeWidth = 4.dp,
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.7f)
        )
        Text(
            text = "/$target",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun CategoryItem(category: Category) {
    Box(
        modifier = Modifier
            .width(120.dp)
            .height(100.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(category.gradient)
            .clickable { /* TODO: Navigate to category */ }
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                category.icon()
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = category.name,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun RecipeCard(recipe: Recipe) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .clickable { /* TODO: Navigate to recipe details */ },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = darkGray
        )
    ) {
        Column {
            // Use a placeholder image or the actual resource
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(teal.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.LocalDining,
                    contentDescription = "Recipe Image",
                    modifier = Modifier.size(48.dp),
                    tint = Color.White.copy(alpha = 0.7f)
                )
            }

            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = recipe.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${recipe.calories} kcal",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "•",
                        color = Color.White.copy(alpha = 0.5f)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = recipe.time,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = recipe.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = teal,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// String extension to capitalize first letter
fun String.capitalize(): String {
    return this.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.getDefault())
        else it.toString()
    }
}