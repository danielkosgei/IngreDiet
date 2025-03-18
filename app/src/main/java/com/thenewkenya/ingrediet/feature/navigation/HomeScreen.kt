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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
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

    // Recipes state
    var recipes by remember { mutableStateOf<List<RecipeRepository.RecipeListItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val colors = MaterialTheme.colorScheme

    val GradientStart = colors.primary
    val GradientEnd = colors.primaryContainer
    val AccentBlue = Color(0xFF8E2DE2)
    val AccentGreen = Color(0xFF56ab2f)
    val AccentYellow = Color(0xFFf7dc6f)
    val AccentRed = Color(0xFFff3d71)
    val Primary = colors.primary
    val Secondary = colors.secondary
    val Tertiary = colors.tertiary

    // Define categories
    val categories = listOf(
        Category(
            1,
            "All Recipes",
            Icons.Filled.RestaurantMenu,
            Brush.horizontalGradient(listOf(GradientStart, GradientEnd))
        ),
        Category(
            2,
            "Fitness",
            Icons.Filled.FitnessCenter,
            Brush.horizontalGradient(listOf(AccentBlue, Primary))
        ),
        Category(
            3,
            "Vegetarian",
            Icons.Filled.Eco,
            Brush.horizontalGradient(listOf(AccentGreen, Primary))
        ),
        Category(
            4,
            "Quick & Easy",
            Icons.Filled.Timer,
            Brush.horizontalGradient(listOf(AccentYellow, Secondary))
        ),
        Category(
            5,
            "Trending",
            Icons.Filled.Whatshot,
            Brush.horizontalGradient(listOf(AccentRed, Tertiary))
        )
    )

    // Fetch recipes when search query or category changes
    LaunchedEffect(searchQuery, selectedCategory) {
        isLoading = true
        errorMessage = null
        try {
            recipeRepository.getRecipes(
                query = searchQuery.takeIf { it.isNotEmpty() },
                category = selectedCategory.takeIf { it != "All Recipes" }
            ).collect { result ->
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
        } catch (e: Exception) {
            isLoading = false
            errorMessage = e.message ?: "An unexpected error occurred"
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

    Box(modifier = Modifier.fillMaxSize()) {
        // Main content
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Profile section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Hello,",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = user?.email?.substringBefore('@') ?: "Guest",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(onClick = { /* Handle notification click */ }) {
                            Icon(
                                imageVector = Icons.Filled.Notifications,
                                contentDescription = "Notifications",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Search bar with suggestions
                    var tempSearchQuery by remember { mutableStateOf("") }
                    var showSuggestions by remember { mutableStateOf(false) }
                    var searchSuggestions by remember { mutableStateOf<List<String>>(emptyList()) }
                    
                    // Get the current context for repository access
                    val currentContext = LocalContext.current
                    
                    // Function to get search suggestions safely without composable dependencies
                    suspend fun getSuggestions(appContext: Context, query: String): List<String> {
                        return try {
                            val recipeRepo = RecipeRepository(appContext)
                            recipeRepo.getSearchSuggestions(query)
                        } catch (e: Exception) {
                            Log.e("HomeScreen", "Error getting search suggestions: ${e.message}", e)
                            // Fallback to basic filtering if repository fails
                            // Include Kenyan foods in the suggestions
                            val allSuggestions = listOf(
                                // International foods
                                "Pasta", "Pizza", "Pancakes", "Chicken", "Beef", "Vegetarian",
                                "Salad", "Soup", "Breakfast", "Dessert", "Quick meal", "Healthy",
                                
                                // Kenyan foods
                                "Ugali", "Nyama Choma", "Sukuma Wiki", "Githeri", "Pilau", 
                                "Chapati", "Mandazi", "Mukimo", "Irio", "Matoke", "Bhajia",
                                "Samosa", "Kachumbari", "Mutura", "Mahindi Choma", "Mahamri",
                                "Viazi Karai", "Mbaazi", "Uji", "Omena", "Tilapia", "Mursik"
                            )
                            allSuggestions.filter { 
                                it.contains(query, ignoreCase = true) 
                            }.take(5)
                        }
                    }
                    
                    // Effect to update suggestions when search query changes
                    LaunchedEffect(tempSearchQuery) {
                        if (tempSearchQuery.length >= 2) {
                            // Get suggestions from the helper function, passing context explicitly
                            searchSuggestions = getSuggestions(currentContext, tempSearchQuery)
                            showSuggestions = searchSuggestions.isNotEmpty()
                        } else {
                            searchSuggestions = emptyList()
                            showSuggestions = false
                        }
                    }
                    
                    Box(modifier = Modifier.fillMaxWidth()) {
                        TextField(
                            value = tempSearchQuery,
                            onValueChange = { tempSearchQuery = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp)),
                            placeholder = { Text("Search recipes...") },
                            leadingIcon = { 
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = {
                                searchQuery = tempSearchQuery
                                showSuggestions = false
                            })
                        )
                        
                        // Suggestions dropdown
                        if (showSuggestions) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 60.dp) // Position below search field
                                    .fillMaxWidth()
                                    .align(Alignment.TopStart)
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .shadow(4.dp, RoundedCornerShape(8.dp)),
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surface
                                ) {
                                    Column(modifier = Modifier.heightIn(max = 200.dp)) {
                                        searchSuggestions.forEach { suggestion ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        tempSearchQuery = suggestion
                                                        searchQuery = suggestion
                                                        showSuggestions = false
                                                    }
                                                    .padding(16.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Search,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text(
                                                    text = suggestion,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Categories
                    Text(
                        text = "Categories",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Categories list
            item {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        CategoryItem(
                            category = category,
                            isSelected = category.name == selectedCategory,
                            onCategorySelected = { selectedCategory = it }
                        )
                    }
                }
            }

            // Recipes grid
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            } else if (errorMessage != null) {
                item {
                    Text(
                        text = errorMessage ?: "An error occurred",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                items(recipes) { recipe ->
                    RecipeCard(recipe = recipe, navController = navController)
                }
            }
        }

        // Updated navigation bar to be more distinguishing
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp) // Adjusted padding
                .navigationBarsPadding()
                .background(MaterialTheme.colorScheme.tertiary .copy(alpha = 1f), shape = RoundedCornerShape(16.dp)) // Slightly darker earthy tone
                .clickable(enabled = false) {} // Prevent click-through
                .height(60.dp), // Set a moderate height for the Row
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(
                Icons.Outlined.Home to "Home",
                Icons.Outlined.FitnessCenter to "Fitness",
                Icons.Outlined.Notifications to "Notifications",
                Icons.Outlined.AccountCircle to "Profile"
            ).forEachIndexed { index, (icon, label) ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { 
                            if (index == 3) navController.navigate("profile") 
                        }
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(24.dp), // Updated icon size
                        tint = Color.White // Set icon color to white
                    )
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White // Set text color to white
                    )
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
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    .padding(vertical = 12.dp)
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
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
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
                            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
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

@Composable
fun NavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val navColors = MaterialTheme.colorScheme
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) navColors.primary else navColors.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) navColors.primary else navColors.onSurface.copy(alpha = 0.7f)
        )
    }
}

// String extension to capitalize first letter
fun String.capitalizeFirst(): String {
    return this.lowercase().replaceFirstChar { it.uppercase() }
}
