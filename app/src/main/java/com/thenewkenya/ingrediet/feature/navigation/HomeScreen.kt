package com.thenewkenya.ingrediet.feature.navigation

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import com.thenewkenya.ingrediet.feature.components.NutritionItem
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.outlined.Add
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.ErrorResult
import coil3.request.SuccessResult
import com.thenewkenya.ingrediet.R
import com.thenewkenya.ingrediet.data.network.AuthManager
import com.thenewkenya.ingrediet.data.network.supabase
import com.thenewkenya.ingrediet.data.repository.RecipeRepository

import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.CompositionLocalProvider

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
    val icon: ImageVector,
    val gradient: Brush
)

// Define a local composition provider for NavController
val LocalNavController = staticCompositionLocalOf<NavController> {
    error("No NavController provided")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(navController: NavController) {
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }
    val recipeRepository = remember { RecipeRepository(context) }
    val coroutineScope = rememberCoroutineScope()
    val user = supabase.auth.currentUserOrNull()
    val focusManager = LocalFocusManager.current

    var isSigningOut by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All Recipes") }
    var isSearchExpanded by remember { mutableStateOf(false) }

    // Recipes state
    var recipes by remember { mutableStateOf<List<RecipeRepository.RecipeListItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    // Remove hardcoded colors and use theme colors
    val BackgroundColor = colors.background
    val AccentGreen = colors.primary
    val CardBackground = colors.surface
    val TextPrimary = colors.onBackground
    val TextSecondary = colors.onBackground.copy(alpha = 0.7f)
    val SurfaceLight = colors.surfaceVariant

    // Categories with updated design using theme colors
    val categories = listOf(
        Category(
            1,
            "Calories",
            Icons.Filled.LocalDining,
            Brush.verticalGradient(listOf(colors.primary.copy(alpha = 0.1f), colors.primary.copy(alpha = 0.05f)))
        ),
        Category(
            2,
            "Water",
            Icons.Filled.WaterDrop,
            Brush.verticalGradient(listOf(colors.primary.copy(alpha = 0.1f), colors.primary.copy(alpha = 0.05f)))
        ),
        Category(
            3,
            "Sleep",
            Icons.Filled.Timer,
            Brush.verticalGradient(listOf(colors.primary.copy(alpha = 0.1f), colors.primary.copy(alpha = 0.05f)))
        ),
        Category(
            4,
            "Training",
            Icons.Filled.FitnessCenter,
            Brush.verticalGradient(listOf(colors.primary.copy(alpha = 0.1f), colors.primary.copy(alpha = 0.05f)))
        )
    )

    // Fetch recipes when search query or category changes
    LaunchedEffect(searchQuery, selectedCategory) {
        isLoading = true
        errorMessage = null
        
        try {
            // Use coroutineScope.launch to collect the flow properly
            recipeRepository.getRecipes(
                query = searchQuery.takeIf { it.isNotEmpty() },
                category = selectedCategory.takeIf { it != "All Recipes" }
            ).collect { result ->
                isLoading = false
                result.fold(
                    onSuccess = { recipesList -> 
                        recipes = recipesList.sortedByDescending { it.rating }
                        Log.d("HomeScreen", "Loaded ${recipes.size} recipes")
                    },
                    onFailure = { error -> 
                        errorMessage = error.message ?: "Failed to load recipes"
                        Log.e("HomeScreen", "Error loading recipes", error)
                    }
                )
            }
        } catch (e: Exception) {
            isLoading = false
            errorMessage = e.message ?: "An unexpected error occurred"
            Log.e("HomeScreen", "Error loading recipes", e)
        }
    }

    if (isSigningOut) {
        SignOutDialog(
            onConfirm = {
                coroutineScope.launch {
                    authManager.signOut()
                    navController.navigate("auth") {
                        popUpTo(0)
                    }
                }
            },
            onDismiss = { isSigningOut = false }
        )
    }

    // Provide the navController to the composition
    CompositionLocalProvider(LocalNavController provides navController) {
        Box(
            modifier = Modifier
                .background(BackgroundColor)
                .fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .background(BackgroundColor)
                    .fillMaxSize(),
                contentPadding = PaddingValues(
                    top = 24.dp,
                    start = 0.dp,
                    end = 0.dp,
                    bottom = 16.dp
                )
            ) {
                // Header section
                item {
                    HomeHeader(
                        navController = navController,
                        userName = user?.email?.substringBefore('@') ?: "Guest",
                        userPhotoUrl = getUserPhotoUrl(user),
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        focusManager = focusManager,
                        colors = colors,
                        typography = typography
                    )
                }

                // Stats Grid
                item {
                    StatsGrid(
                        categories = categories,
                        AccentGreen = AccentGreen,
                        CardBackground = CardBackground,
                        TextPrimary = TextPrimary,
                        TextSecondary = TextSecondary
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }

                // Search results section (shown if search query is not empty)
                if (searchQuery.isNotEmpty()) {
                    item {
                        SearchResultsSection(
                            searchQuery = searchQuery,
                            isLoading = isLoading,
                            errorMessage = errorMessage,
                            recipes = recipes,
                            colors = colors,
                            TextPrimary = TextPrimary,
                            TextSecondary = TextSecondary
                        )
                    }
                }
                
                // Today's Meals Section (only shown if not searching)
                if (searchQuery.isEmpty()) {
                    item {
                        Text(
                            text = "Today's Meals",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier
                                .padding(horizontal = 24.dp)
                                .padding(bottom = 16.dp)
                        )
                    }
                    
                    // Display the recipes in the LazyColumn
                    if (isLoading) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = colors.primary)
                            }
                        }
                    } else if (errorMessage != null) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = errorMessage ?: "Unknown error",
                                    color = colors.error
                                )
                            }
                        }
                    } else if (recipes.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No recipes available. Try searching for something!",
                                    color = TextSecondary
                                )
                            }
                        }
                    } else {
                        items(recipes) { recipe ->
                            RecipeCard(recipe = recipe, navController = navController)
                        }
                    }
                }
            }
        }
    }
}

private fun getUserPhotoUrl(user: io.github.jan.supabase.auth.user.UserInfo?): String? {
    if (user == null) return null
    
    try {
        // Approach 1: Direct access with type cast
        val metadata = user.userMetadata
        var userPhotoUrl = metadata?.get("avatar_url") as? String
        
        // Approach 2: If that fails, try to get it from raw JSON
        if (userPhotoUrl == null && metadata != null) {
            val jsonMetadata = metadata.toString()
            Log.d("UserProfile", "Raw metadata JSON: $jsonMetadata")
            
            // Look for avatar_url pattern in the JSON string
            val regex = "\"avatar_url\"\\s*:\\s*\"([^\"]+)\"".toRegex()
            val matchResult = regex.find(jsonMetadata)
            userPhotoUrl = matchResult?.groupValues?.getOrNull(1)
        }
        
        Log.d("UserProfile", "Extracted photo URL: $userPhotoUrl")
        return userPhotoUrl
    } catch (e: Exception) {
        Log.e("UserProfile", "Error extracting profile image URL", e)
        return null
    }
}

@Composable
private fun HomeHeader(
    navController: NavController,
    userName: String,
    userPhotoUrl: String?,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    focusManager: FocusManager,
    colors: androidx.compose.material3.ColorScheme,
    typography: androidx.compose.material3.Typography
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        // Profile section with updated design
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (userPhotoUrl != null) {
                    // User avatar
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .clickable { navController.navigate("profile") }
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(userPhotoUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Profile picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    // Default profile icon
                    IconButton(
                        onClick = { navController.navigate("profile") },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(colors.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AccountCircle,
                            contentDescription = "Profile",
                            tint = colors.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                Column {
                    Text(
                        text = "Hi $userName!",
                        style = typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = colors.onBackground
                    )
                    Text(
                        text = "Your boards looks so good",
                        style = typography.bodyLarge,
                        color = colors.onBackground.copy(alpha = 0.7f)
                    )
                }
            }
            
            // Notification icon
            IconButton(
                onClick = { /* Handle notifications */ },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(colors.surfaceVariant)
            ) {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = "Notifications",
                    tint = colors.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        // Search bar below profile section
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            placeholder = { Text("Search recipes...") },
            leadingIcon = { 
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search"
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear search"
                        )
                    }
                }
            },
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = colors.surfaceVariant,
                unfocusedContainerColor = colors.surfaceVariant,
                disabledContainerColor = colors.surfaceVariant,
                focusedIndicatorColor = colors.primary,
                unfocusedIndicatorColor = Color.Transparent
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    // Handle search action
                    focusManager.clearFocus()
                    if (searchQuery.isNotEmpty()) {
                        navController.navigate("search/$searchQuery")
                    }
                }
            )
        )
    }
}

@Composable
private fun StatsGrid(
    categories: List<Category>,
    AccentGreen: Color,
    CardBackground: Color,
    TextPrimary: Color,
    TextSecondary: Color
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(categories) { category ->
            Card(
                modifier = Modifier
                    .width(160.dp)
                    .height(120.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = null,
                        tint = AccentGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        when (category.name) {
                            "Calories" -> Text(
                                text = "1480 kcal",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = TextSecondary
                                )
                            )
                            "Water" -> Text(
                                text = "1.5L",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = TextSecondary
                                )
                            )
                            "Sleep" -> Text(
                                text = "7.5h",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = TextSecondary
                                )
                            )
                            "Training" -> Text(
                                text = "5500 steps",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = TextSecondary
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultsSection(
    searchQuery: String,
    isLoading: Boolean,
    errorMessage: String?,
    recipes: List<RecipeRepository.RecipeListItem>,
    colors: androidx.compose.material3.ColorScheme,
    TextPrimary: Color,
    TextSecondary: Color
) {
    val navController = LocalNavController.current
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = "Search Results",
            style = MaterialTheme.typography.titleLarge.copy(
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = colors.primary)
            }
        } else if (errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = errorMessage,
                    color = colors.error
                )
            }
        } else if (recipes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No recipes found for '$searchQuery'",
                    color = TextSecondary
                )
            }
        } else {
            // Display the found recipes
            recipes.forEach { recipe ->
                RecipeCard(recipe = recipe, navController = navController)
            }
        }
    }
}

@Composable
fun SignOutDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Sign Out",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Text(
                text = "Are you sure you want to sign out?",
                style = MaterialTheme.typography.bodyLarge
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Sign Out")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text("Cancel")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    )
}

@Composable
fun CategoryItem(
    category: Category,
    isSelected: Boolean,
    onCategorySelected: (String) -> Unit
) {
    val categoryColors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Box(
        modifier = Modifier
            .width(120.dp)
            .height(100.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(category.gradient)
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
                    .background(categoryColors.onPrimary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = null,
                    tint = categoryColors.onPrimary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = category.name,
                color = categoryColors.onPrimary,
                style = typography.bodyMedium,
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
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(140.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable {
                Log.d("RecipeCard", "Navigating to recipe ${recipe.id}")
                navController.navigate("recipe/${recipe.id}")
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Recipe Image with gradient overlay
            Box(
                modifier = Modifier
                    .width(140.dp)
                    .fillMaxHeight()
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    var isLoading by remember { mutableStateOf(true) }
                    var isError by remember { mutableStateOf(false) }
                    
                    if (recipe.imageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(recipe.imageUrl)
                                .crossfade(true)
                                .listener(object : ImageRequest.Listener {
                                    override fun onStart(request: ImageRequest) {
                                        isLoading = true
                                    }
                                    override fun onSuccess(request: ImageRequest, result: SuccessResult) {
                                        isLoading = false
                                    }
                                    override fun onError(request: ImageRequest, result: ErrorResult) {
                                        isLoading = false
                                        isError = true
                                    }
                                })
                                .build(),
                            contentDescription = "Recipe image for ${recipe.name}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Restaurant,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    if (isError) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Restaurant,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    if (isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                // Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.3f)
                                )
                            )
                        )
                )
            }
            
            // Recipe Details
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 12.dp, horizontal = 4.dp)
            ) {
                Text(
                    text = recipe.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                // Category and dietary info
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    // Category tag
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = recipe.category,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    
                    // Display dietary info
                    recipe.dietaryInfo.take(3).forEach { info ->
                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = info,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Metadata row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Time
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Timer,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = recipe.time,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    
                    // Calories
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocalDining,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "${recipe.calories} kcal",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

// String extension to capitalize first letter
fun String.capitalizeFirst(): String {
    return this.lowercase().replaceFirstChar { it.uppercase() }
}
