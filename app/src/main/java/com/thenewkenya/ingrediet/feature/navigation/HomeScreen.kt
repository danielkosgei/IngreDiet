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

@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }
    val recipeRepository = remember { RecipeRepository(context) }
    val coroutineScope = rememberCoroutineScope()
    val user = supabase.auth.currentUserOrNull()

    var isSigningOut by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All Recipes") }
    var selectedNavItem by remember { mutableStateOf(0) }
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

    // Navigation items
    val navItems = listOf(
        Triple(Icons.Filled.Home, Icons.Outlined.Home, "Home"),
        Triple(Icons.Filled.RestaurantMenu, Icons.Outlined.RestaurantMenu, "Meal Planner"),
        Triple(Icons.Filled.Add, Icons.Outlined.Add, "Create"),
        Triple(Icons.Filled.ShoppingCart, Icons.Outlined.ShoppingCart, "Shopping"),
        Triple(Icons.Filled.AccountCircle, Icons.Outlined.AccountCircle, "Profile")
    )

    // Fetch recipes when search query or category changes
    LaunchedEffect(searchQuery, selectedCategory) {
        isLoading = true
        errorMessage = null
        
        try {
            // Create a new coroutine for each collection to prevent cancellation issues
            val result = recipeRepository.getRecipes(
                query = searchQuery.takeIf { it.isNotEmpty() },
                category = selectedCategory.takeIf { it != "All Recipes" }
            ).collect { result ->
                // Ensure we're still active before updating state
                if (isActive) {
                    isLoading = false
                    result.fold(
                        onSuccess = { recipesList -> 
                            recipes = recipesList.sortedByDescending { it.rating }
                        },
                        onFailure = { error -> 
                            errorMessage = error.message ?: "Failed to load recipes" 
                        }
                    )
                }
            }
        } catch (e: Exception) {
            // Only update state if the coroutine is still active
            if (isActive) {
                isLoading = false
                errorMessage = e.message ?: "An unexpected error occurred"
                Log.e("HomeScreen", "Error loading recipes", e)
            }
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

    Scaffold(
        modifier = Modifier.background(BackgroundColor),
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .background(
                        colors.surfaceVariant,
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    )
            ) {
                NavigationBar(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = Color.Transparent,
                    tonalElevation = 0.dp
                ) {
                    navItems.forEachIndexed { index, (selectedIcon, unselectedIcon, label) ->
                        NavigationBarItem(
                            icon = {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = if (selectedNavItem == index) selectedIcon else unselectedIcon,
                                        contentDescription = label,
                                        modifier = Modifier.size(24.dp),
                                        tint = if (selectedNavItem == index) 
                                            colors.primary
                                        else colors.onSurfaceVariant
                                    )
                                    if (selectedNavItem == index) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .background(
                                                    colors.primary,
                                                    CircleShape
                                                )
                                        )
                                    }
                                }
                            },
                            label = null,
                            selected = selectedNavItem == index,
                            onClick = {
                                selectedNavItem = index
                                when (index) {
                                    0 -> { /* Home - already on home screen */ }
                                    1 -> navController.navigate("mealplanner")
                                    2 -> navController.navigate("create")
                                    3 -> navController.navigate("shopping")
                                    4 -> navController.navigate("profile")
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = colors.primary,
                                unselectedIconColor = colors.onSurfaceVariant,
                                indicatorColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .background(BackgroundColor)
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // Header section
            item {
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
                        if (!isSearchExpanded) {
                            Column {
                                Text(
                                    text = "Hi ${user?.email?.substringBefore('@') ?: "Guest"}!",
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
                        
                        // Action buttons or Search bar
                        if (isSearchExpanded) {
                            // Expanded search bar
                            TextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                placeholder = { Text("Search recipes...") },
                                leadingIcon = { 
                                    Icon(
                                        imageVector = Icons.Filled.Search,
                                        contentDescription = "Search",
                                        tint = colors.primary
                                    )
                                },
                                trailingIcon = {
                                    IconButton(onClick = { 
                                        isSearchExpanded = false
                                        searchQuery = ""
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Close Search",
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = colors.surfaceVariant,
                                    unfocusedContainerColor = colors.surfaceVariant,
                                    disabledContainerColor = colors.surfaceVariant,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(
                                    onSearch = {
                                        // Just leave the keyboard handling to the system
                                        // No need to manually dismiss the keyboard
                                    }
                                )
                            )
                        } else {
                            // Action buttons
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Search button
                                IconButton(
                                    onClick = { isSearchExpanded = true },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(colors.surfaceVariant)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Search,
                                        contentDescription = "Search",
                                        tint = colors.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                
                                // Favorites button
                                IconButton(
                                    onClick = { navController.navigate("favorites") },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(colors.surfaceVariant)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Favorite,
                                        contentDescription = "Favorites",
                                        tint = colors.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                
                                // Profile button
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
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Stats Grid
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
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

                    Spacer(modifier = Modifier.height(32.dp))

                    // Search results section (shown if search query is not empty)
                    if (searchQuery.isNotEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
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
                                        text = errorMessage ?: "Error loading recipes",
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
                            }
                        }
                    }
                    
                    // Today's Meals Section (only shown if not searching)
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "Today's Meals",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                }
            }

            // Recipes list with updated design
            if (searchQuery.isEmpty()) {
                // Show regular recipe list when not searching
                items(recipes) { recipe ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp)
                            .height(80.dp)
                            .clickable { navController.navigate("recipe/${recipe.id}") },
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Recipe image
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SurfaceLight)
                                ) {
                                    AsyncImage(
                                        model = recipe.imageUrl,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                // Recipe details
                                Column {
                                    Text(
                                        text = recipe.name,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                    Text(
                                        text = "${recipe.calories} kcal",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = TextSecondary
                                        )
                                    )
                                }
                            }
                            // Arrow indicator
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "View Recipe",
                                tint = AccentGreen,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            } else if (!isLoading && errorMessage == null && recipes.isNotEmpty()) {
                // Show search results when searching and there are results
                items(recipes) { recipe ->
                    RecipeCard(recipe = recipe, navController = navController)
                }
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
