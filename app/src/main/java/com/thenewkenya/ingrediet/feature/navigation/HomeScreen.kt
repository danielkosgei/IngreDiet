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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WaterDrop

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
import androidx.compose.material3.NavigationBar
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
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.thenewkenya.ingrediet.R
import com.thenewkenya.ingrediet.data.network.AuthManager
import com.thenewkenya.ingrediet.data.network.supabase
import com.thenewkenya.ingrediet.data.repository.RecipeRepository
import com.thenewkenya.ingrediet.ui.components.BottomNavItem
import com.thenewkenya.ingrediet.ui.components.GlassBottomBar
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

@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }
    val recipeRepository = remember { RecipeRepository() }
    val coroutineScope = rememberCoroutineScope()
    val user = supabase.auth.currentUserOrNull()
    var isSigningOut by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All Recipes") }

    // States for recipe data
    var recipes by remember { mutableStateOf<List<RecipeRepository.RecipeListItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Sample categories data
    val categories = listOf(
        Category(
            1, "All Recipes",
            { Icon(Icons.Filled.RestaurantMenu, contentDescription = "All Recipes", tint = Color.White) },
            Brush.horizontalGradient(colors = listOf(teal, teal.copy(alpha = 0.7f)))
        ),
        Category(
            2, "Fitness",
            { Icon(Icons.Filled.FitnessCenter, contentDescription = "Fitness", tint = Color.White) },
            Brush.horizontalGradient(colors = listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0)))
        ),
        Category(
            3, "Vegetarian",
            { Icon(Icons.Filled.Eco, contentDescription = "Vegetarian", tint = Color.White) },
            Brush.horizontalGradient(colors = listOf(Color(0xFF56ab2f), Color(0xFFa8e063)))
        ),
        Category(
            4, "Hydration",
            { Icon(Icons.Filled.WaterDrop, contentDescription = "Hydration", tint = Color.White) },
            Brush.horizontalGradient(colors = listOf(Color(0xFF2193b0), Color(0xFF6dd5ed)))
        )
    )

    // Navigation items
    val items = listOf(
        Triple(Icons.Outlined.Home, Icons.Filled.Home, "Home"),
        Triple(Icons.Outlined.Favorite, Icons.Filled.Favorite, "Favorites"),
        Triple(Icons.Outlined.FitnessCenter, Icons.Filled.FitnessCenter, "My Plan"),
        Triple(Icons.Outlined.AccountCircle, Icons.Filled.AccountCircle, "Profile")
    )

    // Load recipes when search query or category changes
    LaunchedEffect(searchQuery, selectedCategory) {
        isLoading = true
        errorMessage = null

        coroutineScope.launch {
            try {
                recipeRepository.getRecipes(
                    query = searchQuery.takeIf { it.isNotEmpty() },
                    category = selectedCategory.takeIf { it != "All Recipes" }
                ).collect { result ->
                    isLoading = false
                    result.fold(
                        onSuccess = { recipesList ->
                            recipes = recipesList
                        },
                        onFailure = { error ->
                            Log.e("HomeScreen", "Error loading recipes", error)
                            errorMessage = error.message ?: "Failed to load recipes"
                        }
                    )
                }
            } catch (e: Exception) {
                isLoading = false
                Log.e("HomeScreen", "Exception loading recipes", e)
                errorMessage = e.message ?: "An unexpected error occurred"
            }
        }
    }

    // Handle signout in a LaunchedEffect
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
                NavigationBar(
                    containerColor = darkGray
                ) {
                    items.forEachIndexed { index, (outlinedIcon, filledIcon, label) ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (selectedItem == index) filledIcon else outlinedIcon,
                                    contentDescription = label
                                )
                            },
                            label = { Text(label) },
                            selected = selectedItem == index,
                            onClick = {
                                selectedItem = index
                                if (index == 3) { // Profile
                                    navController.navigate("profile")
                                }
                            }
                        )
                    }
                }
            }
        ) { paddingValues ->
            // Main content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(black)
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Top Bar with user greeting and logout
                item {
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
                                Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "Logout",
                                tint = Color.White
                            )
                        }
                    }
                }

                // Search Bar
                item {
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
                }

                // Daily Summary Card
                item {
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
                }

                // Categories
                item {
                    Column {
                        Text(
                            text = "Browse Categories",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(categories) { category ->
                                CategoryItem(
                                    category = category,
                                    isSelected = selectedCategory == category.name,
                                    onCategorySelected = {
                                        selectedCategory = it
                                    }
                                )
                            }
                        }
                    }
                }

                // Today's Recommendation
                item {
                    Column {
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

                        if (isLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = teal)
                            }
                        } else if (errorMessage != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = errorMessage ?: "Unknown error",
                                    color = Color.Red,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else if (recipes.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No recipes found",
                                    color = Color.White.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(recipes) { recipe ->
                                    RecipeCard(
                                        recipe = recipe,
                                        navController = navController
                                    )
                                }
                            }
                        }
                    }
                }

                // Add Ingredients Button
                item {
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
                progress = { progress },
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
fun CategoryItem(
    category: Category,
    isSelected: Boolean,
    onCategorySelected: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .width(120.dp)
            .height(100.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                category.gradient
            )
            .clickable { onCategorySelected(category.name) }
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
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun RecipeCard(recipe: RecipeRepository.RecipeListItem, navController: NavController) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .clickable { navController.navigate("recipe/${recipe.id}") },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = darkGray
        )
    ) {
        Column {
            // Image loading with Coil
            if (recipe.imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(recipe.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = recipe.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .background(teal.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Restaurant,
                        contentDescription = "Recipe Image",
                        modifier = Modifier.size(48.dp),
                        tint = Color.White.copy(alpha = 0.7f)
                    )
                }
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
        if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault())
        else it.toString()
    }
}