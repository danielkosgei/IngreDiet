package com.thenewkenya.ingrediet.feature.navigation

import android.content.Context
import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.FreeBreakfast
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
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
import com.thenewkenya.ingrediet.data.repository.ProfileRepository
import com.thenewkenya.ingrediet.data.model.Profile
import com.thenewkenya.ingrediet.feature.mealplanner.MealPlannerViewModel
import com.thenewkenya.ingrediet.feature.shopping.ShoppingListViewModel
import com.thenewkenya.ingrediet.feature.shopping.ShoppingItem
import com.thenewkenya.ingrediet.feature.mealplanner.MealPlanItem
import com.thenewkenya.ingrediet.feature.mealplanner.MealTime
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.DayOfWeek
import java.util.Date
import java.util.Locale
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.layout.heightIn
import com.thenewkenya.ingrediet.feature.components.EnhancedMealPreviewCard

data class Recipe(
    val id: Int,
    val name: String,
    val imageRes: Int,
    val calories: Int,
    val time: String,
    val category: String
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
    val profileRepository = remember { ProfileRepository() }
    val coroutineScope = rememberCoroutineScope()
    val user = supabase.auth.currentUserOrNull()
    val focusManager = LocalFocusManager.current
    
    // Load user profile data for profile image and name
    var userProfile by remember { mutableStateOf<Profile?>(null) }
    
    LaunchedEffect(user?.id) {
        if (user != null) {
            profileRepository.getProfile().collect { result ->
                result.fold(
                    onSuccess = { profile ->
                        userProfile = profile
                        Log.d("HomeScreen", "Profile loaded: ${profile.firstName} ${profile.lastName}, image: ${profile.profileImageUrl}")
                    },
                    onFailure = { error ->
                        Log.e("HomeScreen", "Failed to load profile: ${error.message}")
                    }
                )
            }
        }
    }

    // Notification permission handling
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            // Auto-schedule default notifications when permission granted
            com.thenewkenya.ingrediet.feature.profile.NotificationScheduler.scheduleAllDefaults(context)
        }
    }

    // Request notification permission on first home screen visit
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context, 
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            
            if (!hasPermission) {
                // Request permission for notifications
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                // Permission already granted, schedule notifications if not already done
                com.thenewkenya.ingrediet.feature.profile.NotificationScheduler.scheduleAllDefaults(context)
            }
        } else {
            // On older Android versions, no permission needed - just schedule
            com.thenewkenya.ingrediet.feature.profile.NotificationScheduler.scheduleAllDefaults(context)
        }
    }

    var isSigningOut by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All Recipes") }
    var isSearchExpanded by remember { mutableStateOf(false) }

    // Recipes state
    var recipes by remember { mutableStateOf<List<RecipeRepository.RecipeListItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var hasInitiallyLoaded by remember { mutableStateOf(false) }

    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    // Remove hardcoded colors and use theme colors
    val BackgroundColor = colors.background
    val AccentGreen = colors.primary
    val CardBackground = colors.surface
    val TextPrimary = colors.onBackground
    val TextSecondary = colors.onBackground.copy(alpha = 0.7f)
    val SurfaceLight = colors.surfaceVariant

    // Filter suggestions based on input
    var filteredSuggestions by remember { mutableStateOf<List<String>>(listOf()) }

    // Simplified function that searches by recipe name, category, and dietary info
    fun filterSuggestions(query: String, allRecipes: List<RecipeRepository.RecipeListItem>) {
        if (query.isBlank()) {
            Log.d("SearchDebug", "Query is blank, clearing suggestions")
            filteredSuggestions = emptyList()
            return
        }

        val lowercaseQuery = query.lowercase().trim()
        val matchedRecipes = mutableSetOf<String>()
        
        Log.d("SearchDebug", "Searching for: '$lowercaseQuery' in ${allRecipes.size} recipes")
        
        if (allRecipes.isEmpty()) {
            Log.d("SearchDebug", "Recipe list is empty!")
            return
        }
        
        // Map of special terms to look for in various places
        val specialTerms = mapOf(
            "dessert" to listOf("dessert", "sweet", "cake", "pie", "cookie"),
            "breakfast" to listOf("breakfast", "morning", "brunch"),
            "vegan" to listOf("vegan", "plant-based"),
            "vegetarian" to listOf("vegetarian", "veggie", "no meat")
        )
        
        // Check if query matches any special terms
        val matchingTerms = specialTerms.filter { (key, synonyms) ->
            lowercaseQuery.contains(key) || synonyms.any { lowercaseQuery.contains(it) }
        }
        
        if (matchingTerms.isNotEmpty()) {
            Log.d("SearchDebug", "Query matches special terms: ${matchingTerms.keys}")
        }

        allRecipes.forEach { recipe ->
            var matched = false
            
            // Check recipe name
            if (recipe.name.lowercase().contains(lowercaseQuery)) {
                matchedRecipes.add(recipe.name)
                matched = true
                Log.d("SearchDebug", "Name match: ${recipe.name}")
                return@forEach // No need to check further for this recipe
            }
            
            // Check recipe category directly
            if (recipe.category.lowercase().contains(lowercaseQuery)) {
                matchedRecipes.add(recipe.name)
                matched = true
                Log.d("SearchDebug", "Category match: ${recipe.name} (${recipe.category})")
                return@forEach
            }
            
            // Check for special term matches in category
            if (matchingTerms.isNotEmpty()) {
                for ((term, synonyms) in matchingTerms) {
                    if (recipe.category.lowercase().contains(term) || 
                        synonyms.any { recipe.category.lowercase().contains(it) }) {
                        matchedRecipes.add(recipe.name)
                        matched = true
                        Log.d("SearchDebug", "Special term match in category: ${recipe.name} (term: $term)")
                        return@forEach
                    }
                }
            }
            
            // Check dietary tags
            val dietaryTags = recipe.dietaryInfo.filter { it.isNotBlank() }
            if (dietaryTags.isNotEmpty()) {
                Log.d("SearchDebug", "Checking dietary tags for ${recipe.name}: ${dietaryTags.joinToString()}")
                
                // Direct tag match
                for (tag in dietaryTags) {
                    if (tag.lowercase().contains(lowercaseQuery) || 
                        lowercaseQuery.contains(tag.lowercase())) {
                        matchedRecipes.add(recipe.name)
                        Log.d("SearchDebug", "Tag direct match: ${recipe.name} (tag: $tag)")
                        matched = true
                        break
                    }
                }
                
                // Special term match in tags
                if (!matched && matchingTerms.isNotEmpty()) {
                    for ((term, synonyms) in matchingTerms) {
                        val foundMatch = dietaryTags.any { tag ->
                            tag.lowercase().contains(term) || 
                            synonyms.any { syn -> tag.lowercase().contains(syn) }
                        }
                        
                        if (foundMatch) {
                            matchedRecipes.add(recipe.name)
                            Log.d("SearchDebug", "Special term match in tags: ${recipe.name} (term: $term)")
                            matched = true
                            break
                        }
                    }
                }
            }
        }

        filteredSuggestions = matchedRecipes.toList().sorted()
        Log.d("SearchDebug", "Final filtered suggestions: ${filteredSuggestions.joinToString()}")
    }

    // Fetch recipes when search query or category changes
    LaunchedEffect(searchQuery, selectedCategory) {
        if (searchQuery.isNotEmpty()) {
            isLoading = true
            errorMessage = null
            hasInitiallyLoaded = true
            
            try {
                // Use coroutineScope.launch to collect the flow properly
                coroutineScope.launch {
                    try {
                        recipeRepository.getRecipes(
                            query = searchQuery,
                            category = selectedCategory.takeIf { it != "All Recipes" }
                        ).collect { result ->
                            if (isActive) { // Check if still active before updating state
                                isLoading = false
                                result.fold(
                                    onSuccess = { recipesList -> 
                                        recipes = recipesList
                                        // Update filtered suggestions based on loaded recipes
                                        filterSuggestions(searchQuery, recipes)
                                        Log.d("HomeScreen", "Loaded ${recipes.size} recipes")
                                        Log.d("SearchDebug", "Recipe categories: ${recipes.map { it.category }.distinct()}")
                                        Log.d("SearchDebug", "Recipe tags: ${recipes.flatMap { it.dietaryInfo }.distinct()}")
                                    },
                                    onFailure = { error -> 
                                        if (error is kotlinx.coroutines.CancellationException ||
                                            error.message?.contains("composition") == true ||
                                            error.cause is kotlinx.coroutines.CancellationException) {
                                            Log.d("HomeScreen", "Recipe loading cancelled - screen likely left composition")
                                        } else {
                                            errorMessage = error.message ?: "Failed to load recipes"
                                            Log.e("HomeScreen", "Error loading recipes", error)
                                        }
                                    }
                                )
                            }
                        }
                    } catch (e: Exception) {
                        // Only update UI state if still active and not a cancellation
                        if (isActive && e !is kotlinx.coroutines.CancellationException && 
                            e.message?.contains("composition") != true &&
                            e.cause !is kotlinx.coroutines.CancellationException) {
                            isLoading = false
                            errorMessage = e.message ?: "An unexpected error occurred"
                            Log.e("HomeScreen", "Error loading recipes", e)
                        } else {
                            Log.d("HomeScreen", "Recipe loading cancelled: ${e.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                // Handle exceptions outside of the coroutine scope
                if (e !is kotlinx.coroutines.CancellationException && 
                    e.message?.contains("composition") != true &&
                    e.cause !is kotlinx.coroutines.CancellationException) {
                    isLoading = false
                    errorMessage = e.message ?: "An unexpected error occurred"
                    Log.e("HomeScreen", "Error launching recipe loader", e)
                } else {
                    Log.d("HomeScreen", "Recipe loading cancelled during launch: ${e.message}")
                }
            }
        } else {
            // Reset states when search query is empty
            isLoading = false
            errorMessage = null
            recipes = emptyList()
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
        Column(
            modifier = Modifier
                .background(BackgroundColor)
                .fillMaxSize()
        ) {
            // Sticky header
            HomeHeader(
                navController = navController,
                userName = when {
                    userProfile?.firstName?.isNotEmpty() == true || userProfile?.lastName?.isNotEmpty() == true -> 
                        "${userProfile?.firstName ?: ""} ${userProfile?.lastName ?: ""}".trim()
                    user?.email?.substringBefore('@')?.isNotEmpty() == true -> 
                        user.email!!.substringBefore('@')
                    else -> "Guest"
                },
                userPhotoUrl = userProfile?.profileImageUrl?.ifEmpty { getUserPhotoUrl(user) } ?: getUserPhotoUrl(user),
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                focusManager = focusManager,
                colors = colors,
                typography = typography,
                recipes = recipes,
                filteredSuggestions = filteredSuggestions,
                onUpdateFilteredSuggestions = { query -> 
                    filterSuggestions(query, recipes)
                }
            )
            
            // Scrollable content
            LazyColumn(
                modifier = Modifier
                    .background(BackgroundColor)
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(
                    top = 0.dp,
                    start = 0.dp,
                    end = 0.dp,
                    bottom = 16.dp
                )
            ) {
                // Recipe of the Day section
                item {
                    RecipeOfTheDaySection(
                        navController = navController,
                        colors = colors,
                        typography = typography,
                        recipeRepository = recipeRepository
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
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
                    
                    // Add today's meal preview card
                    item {
                        com.thenewkenya.ingrediet.feature.components.EnhancedMealPreviewCard(
                            navController = navController,
                            colors = colors
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                    
                    // Add shopping list preview
                    item {
                        Text(
                            text = "Shopping List",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier
                                .padding(horizontal = 24.dp)
                                .padding(bottom = 16.dp)
                        )
                        
                        ShoppingListPreview(
                            navController = navController,
                            colors = colors,
                            TextPrimary = TextPrimary,
                            TextSecondary = TextSecondary,
                            CardBackground = CardBackground
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                    
                    // Loading and error states only shown during search
                    if (isLoading) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
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
                                    .height(100.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = errorMessage ?: "Unknown error",
                                    color = colors.error
                                )
                            }
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
    typography: androidx.compose.material3.Typography,
    recipes: List<RecipeRepository.RecipeListItem>,
    filteredSuggestions: List<String>,
    onUpdateFilteredSuggestions: (String) -> Unit
) {
    // Common search suggestions
    val searchSuggestions = remember {
        listOf(
            "Breakfast", "Lunch", "Dinner", 
            "Vegetarian", "Vegan", "Gluten-Free", 
            "Low Carb", "High Protein", "Keto", 
            "Quick & Easy", "Desserts", "Snacks",
            "Italian", "Mexican", "Asian", "Mediterranean",
            "Soup", "Salad", "Seafood", "Chicken"
        )
    }
    
    var showSuggestions by remember { mutableStateOf(false) }
    var isSearchExpanded by remember { mutableStateOf(false) }
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = colors.background,
        shadowElevation = if (isSearchExpanded) 2.dp else 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            if (isSearchExpanded) {
                // Search bar replaces profile info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Search field takes most space
                    val localSearchQuery = remember(searchQuery) { mutableStateOf(searchQuery) }
                    val scope = rememberCoroutineScope()
                    
                    OutlinedTextField(
                        value = localSearchQuery.value,
                        onValueChange = { newValue ->
                            localSearchQuery.value = newValue
                            showSuggestions = newValue.isNotEmpty()
                            onUpdateFilteredSuggestions(newValue)
                            scope.launch {
                                onSearchQueryChange(newValue)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        placeholder = { 
                            Text(
                                "Search recipes...",
                                color = colors.onSurface.copy(alpha = 0.6f)
                            )
                        },
                        leadingIcon = { 
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = colors.primary
                            )
                        },
                        trailingIcon = {
                            if (localSearchQuery.value.isNotEmpty()) {
                                IconButton(onClick = { 
                                    localSearchQuery.value = ""
                                    showSuggestions = false
                                    scope.launch {
                                        onSearchQueryChange("")
                                    }
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear search",
                                        tint = colors.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = colors.surface,
                            unfocusedContainerColor = colors.surface,
                            focusedIndicatorColor = colors.primary,
                            unfocusedIndicatorColor = colors.outline.copy(alpha = 0.5f),
                            cursorColor = colors.primary
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                focusManager.clearFocus()
                                showSuggestions = false
                            }
                        )
                    )
                    
                    // Close search icon - Rounded rectangle style with error color
                    IconButton(
                        onClick = { 
                            isSearchExpanded = false
                            onSearchQueryChange("")
                            showSuggestions = false
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.errorContainer.copy(alpha = 0.3f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close search",
                            tint = colors.error,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    
                    // Notification icon - Circular style with different color
                    IconButton(
                        onClick = { navController.navigate("inbox/notifications") },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(colors.tertiaryContainer.copy(alpha = 0.4f))
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Notifications,
                            contentDescription = "Notifications",
                            tint = colors.tertiary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            } else {
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
                    val ctx = LocalContext.current
                    val currentUserId = supabase.auth.currentUserOrNull()?.id
                    val prefs = remember { ctx.getSharedPreferences("ingrediet_prefs", android.content.Context.MODE_PRIVATE) }
                    var isReturning by remember(currentUserId) {
                        mutableStateOf(currentUserId?.let { prefs.getBoolean("seen_user_$it", false) } ?: false)
                    }
                    LaunchedEffect(currentUserId) {
                        if (currentUserId != null && !prefs.getBoolean("seen_user_$currentUserId", false)) {
                            prefs.edit().putBoolean("seen_user_$currentUserId", true).apply()
                            isReturning = false
                        } else if (currentUserId != null) {
                            isReturning = true
                        }
                    }
                    Text(
                        text = userName,
                        style = typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = colors.onBackground
                    )
                    Text(
                        text = if (isReturning) "Welcome back" else "Welcome",
                        style = typography.bodyLarge,
                        color = colors.onBackground.copy(alpha = 0.7f)
                    )
                }
            }
            
                            Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                        // Search icon - Rounded rectangle style
                        IconButton(
                            onClick = { 
                                isSearchExpanded = true
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.primaryContainer.copy(alpha = 0.3f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = colors.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        
                        // Notification icon - Circular style with different color
                        IconButton(
                            onClick = { navController.navigate("inbox/notifications") },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(colors.tertiaryContainer.copy(alpha = 0.4f))
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Notifications,
                                contentDescription = "Notifications",
                                tint = colors.tertiary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
        }
        
        // Search suggestions (only show when search is expanded)
        if (isSearchExpanded) {
            AnimatedVisibility(
                visible = showSuggestions && searchQuery.isNotEmpty(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colors.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 2.dp
                    )
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        if (filteredSuggestions.isEmpty()) {
                            // Show a hint when no matching suggestions
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Try searching for a recipe, ingredient, or cuisine type",
                                    style = typography.bodySmall,
                                    color = colors.onSurfaceVariant.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            // Show filtered suggestions
                            LazyColumn(
                                modifier = Modifier.heightIn(max = 200.dp)
                            ) {
                                items(filteredSuggestions) { suggestion ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                onSearchQueryChange(suggestion)
                                                showSuggestions = false
                                                focusManager.clearFocus()
                                            }
                                            .padding(horizontal = 8.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = null,
                                            tint = colors.onSurfaceVariant.copy(alpha = 0.7f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = suggestion,
                                            style = typography.bodyMedium,
                                            color = colors.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
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
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Search Results",
            style = MaterialTheme.typography.titleLarge.copy(
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier
                .padding(start = 8.dp, bottom = 12.dp, top = 8.dp)
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
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = TextSecondary.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No recipes found for '$searchQuery'",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Try adjusting your search terms or browse categories",
                        color = TextSecondary.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Display the found recipes with improved spacing
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                recipes.forEach { recipe ->
                    RecipeCard(recipe = recipe, navController = navController)
                }
                // Add some bottom padding
                Spacer(modifier = Modifier.height(16.dp))
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
fun RecipeCard(recipe: RecipeRepository.RecipeListItem, navController: NavController) {
    val colors = MaterialTheme.colorScheme
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable {
                Log.d("RecipeCard", "Navigating to recipe ${recipe.id}")
                navController.navigate("recipe/${recipe.id}")
            },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surfaceVariant.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Recipe image as squircle (rounded square)
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                if (recipe.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(recipe.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(20.dp))
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Restaurant,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            // Recipe details
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Recipe name
                    Text(
                        text = recipe.name,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Quick info in a compact format
                    Text(
                        text = recipe.time.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Category with subtle indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(colors.primary)
                    )
                    Text(
                        text = recipe.category,
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Tags row with scrolling
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Display calories
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(colors.tertiary.copy(alpha = 0.1f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${recipe.calories} kcal",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.tertiary
                        )
                    }
                    
                    // Display dietary info tags - show up to 4 tags
                    val tagsToShow = mutableListOf<String>()
                    
                    // Get filtered tags
                    recipe.dietaryInfo.forEach { tag ->
                        val cleanTag = tag.trim()
                        if (cleanTag.isNotEmpty() && !tagsToShow.contains(cleanTag) && 
                            !recipe.category.equals(cleanTag, ignoreCase = true) &&
                            tagsToShow.size < 4) {
                            tagsToShow.add(cleanTag)
                        }
                    }
                    
                    // Show all valid tags in pills
                    tagsToShow.forEach { tag ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(colors.secondary.copy(alpha = 0.1f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = tag.capitalizeFirst(),
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.onSecondaryContainer
                            )
                        }
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

@Composable
fun ShoppingListPreview(
    navController: NavController,
    colors: ColorScheme,
    TextPrimary: Color,
    TextSecondary: Color,
    CardBackground: Color
) {
    val context = LocalContext.current
    val shoppingListViewModel = remember { 
        ShoppingListViewModel(
            context = context
        )
    }
    
    // Get shopping list items
    val items by shoppingListViewModel.items.collectAsState(initial = emptyList())
    val isLoading by shoppingListViewModel.isLoading.collectAsState(initial = true)
    val displayItems = items.take(5)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clickable { navController.navigate("shopping") },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            if (isLoading) {
                // Show loading skeleton
                ShoppingListSkeleton(TextSecondary)
            } else if (items.isEmpty()) {
                // Show empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = null,
                            tint = TextSecondary.copy(alpha = 0.5f),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Your shopping list is empty",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        TextButton(
                            onClick = { navController.navigate("shopping") },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = colors.primary
                            )
                        ) {
                            Text("Add items")
                        }
                    }
                }
            } else {
                // Display items
                displayItems.forEachIndexed { index, item ->
                    if (index > 0) {
                        Divider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = TextSecondary.copy(alpha = 0.1f)
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = item.isChecked,
                            onCheckedChange = { shoppingListViewModel.toggleItem(item.id) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = colors.primary,
                                uncheckedColor = TextSecondary
                            )
                        )
                        
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextPrimary,
                                textDecoration = if (item.isChecked) {
                                    androidx.compose.ui.text.style.TextDecoration.LineThrough
                                } else {
                                    null
                                }
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                // If there are more items than we show
                if (items.size > 5) {
                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = TextSecondary.copy(alpha = 0.1f)
                    )
                    
                    Text(
                        text = "+${items.size - 5} more items",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = colors.primary
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun ShoppingListSkeleton(textSecondary: Color) {
    val shimmerColor = textSecondary.copy(alpha = 0.2f)
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer alpha"
    )
    
    Column(modifier = Modifier.fillMaxWidth()) {
        repeat(5) { index ->
            if (index > 0) {
                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .padding(vertical = 8.dp)
                        .background(shimmerColor.copy(alpha = 0.5f))
                )
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Checkbox placeholder
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(
                            shimmerColor.copy(alpha = alpha),
                            RoundedCornerShape(4.dp)
                        )
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Text placeholder
                Box(
                    modifier = Modifier
                        .height(16.dp)
                        .width(150.dp + (index * 20).dp)
                        .background(
                            shimmerColor.copy(alpha = alpha),
                            RoundedCornerShape(4.dp)
                        )
                )
            }
        }
    }
}

@Composable
private fun RecipeOfTheDaySection(
    navController: NavController,
    colors: androidx.compose.material3.ColorScheme,
    typography: androidx.compose.material3.Typography,
    recipeRepository: RecipeRepository
) {
    val context = LocalContext.current
    val profileRepository = remember { ProfileRepository() }
    var recipeOfTheDay by remember { mutableStateOf<RecipeRepository.RecipeListItem?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var hasLoaded by remember { mutableStateOf(false) }
    
    // Check if we have a cached recipe for today first
    val today = remember { 
        java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()) 
    }
    
    // Only fetch if we haven't loaded today's recipe yet
    LaunchedEffect(today) {
        if (hasLoaded) return@LaunchedEffect
        
        try {
            // Check cache first
            val prefs = context.getSharedPreferences("recipe_of_day", Context.MODE_PRIVATE)
            val cachedDate = prefs.getString("last_recipe_date", "")
            val cachedRecipeId = prefs.getString("last_recipe_id", "")
            val cachedRecipeName = prefs.getString("cached_recipe_name", "")
            val cachedRecipeCategory = prefs.getString("cached_recipe_category", "")
            val cachedRecipeCalories = prefs.getInt("cached_recipe_calories", 0)
            val cachedRecipeTime = prefs.getInt("cached_recipe_time", 0)
            val cachedRecipeImageUrl = prefs.getString("cached_recipe_image_url", "")
            val cachedRecipeDescription = prefs.getString("cached_recipe_description", "")
            
            if (cachedDate == today && cachedRecipeId?.isNotEmpty() == true && cachedRecipeName?.isNotEmpty() == true) {
                // Use cached recipe
                recipeOfTheDay = RecipeRepository.RecipeListItem(
                    id = cachedRecipeId,
                    name = cachedRecipeName,
                    description = cachedRecipeDescription ?: "",
                    imageUrl = cachedRecipeImageUrl ?: "",
                    time = cachedRecipeTime,
                    calories = cachedRecipeCalories,
                    recipeId = cachedRecipeId,
                    category = cachedRecipeCategory ?: "",
                    dietaryInfo = emptyList()
                )
                isLoading = false
                hasLoaded = true
                Log.d("RecipeOfTheDay", "Using cached recipe: $cachedRecipeName")
                return@LaunchedEffect
            }
            
            // If no valid cache, fetch new recipe with timeout
            Log.d("RecipeOfTheDay", "Starting recipe fetch...")
            
            // Add a timeout mechanism
            val timeoutJob = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                kotlinx.coroutines.delay(10000) // 10 second timeout
                if (isLoading) {
                    Log.w("RecipeOfTheDay", "Profile loading timed out, using fallback")
                    getRandomRecipeOfTheDay(recipeRepository, context) { recipe ->
                        recipeOfTheDay = recipe
                        isLoading = false
                        hasLoaded = true
                    }
                }
            }
            
            // If no valid cache, fetch new recipe
            profileRepository.getProfile().collect { profileResult ->
                timeoutJob.cancel() // Cancel timeout if we get a result
                profileResult.fold(
                    onSuccess = { profile ->
                        // Get recipes that match user preferences
                        getPersonalizedRecipeOfTheDay(
                            recipeRepository = recipeRepository,
                            context = context,
                            profile = profile
                        ) { recipe ->
                            recipeOfTheDay = recipe
                            isLoading = false
                            hasLoaded = true
                            
                            // Cache the recipe details for fast loading
                            recipe?.let { r ->
                                prefs.edit()
                                    .putString("cached_recipe_name", r.name)
                                    .putString("cached_recipe_category", r.category)
                                    .putInt("cached_recipe_calories", r.calories)
                                    .putInt("cached_recipe_time", r.time)
                                    .putString("cached_recipe_image_url", r.imageUrl)
                                    .putString("cached_recipe_description", r.description)
                                    .apply()
                            }
                        }
                    },
                    onFailure = { 
                        // If profile loading fails, fallback to random recipe
                        Log.w("RecipeOfTheDay", "Could not load user profile, using random recipe")
                        getRandomRecipeOfTheDay(recipeRepository, context) { recipe ->
                            recipeOfTheDay = recipe
                            isLoading = false
                            hasLoaded = true
                            
                            // Cache the fallback recipe too
                            recipe?.let { r ->
                                prefs.edit()
                                    .putString("cached_recipe_name", r.name)
                                    .putString("cached_recipe_category", r.category)
                                    .putInt("cached_recipe_calories", r.calories)
                                    .putInt("cached_recipe_time", r.time)
                                    .putString("cached_recipe_image_url", r.imageUrl)
                                    .putString("cached_recipe_description", r.description)
                                    .apply()
                            }
                        }
                    }
                )
            }
        } catch (e: Exception) {
            isLoading = false
            hasLoaded = true
            Log.e("RecipeOfTheDay", "Error loading recipe of the day", e)
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // Section header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recipe of the Day",
                style = typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = colors.onBackground
            )
            
            TextButton(
                onClick = { /* Navigate to all recipes */ }
            ) {
                Text(
                    text = "See All",
                    color = colors.primary,
                    style = typography.bodyMedium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Featured recipe card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clickable { 
                    val recipe = recipeOfTheDay
                    if (recipe != null) {
                        navController.navigate("recipe/${recipe.id}")
                    }
                },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = colors.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            )
        ) {
            if (isLoading) {
                // Loading state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = colors.primary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Loading recipe...",
                            style = typography.bodyMedium,
                            color = colors.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                val recipe = recipeOfTheDay
                if (recipe != null) {
                    // Real recipe data
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Recipe image background
                        AsyncImage(
                            model = recipe.imageUrl.ifBlank { 
                                "https://images.unsplash.com/photo-1565299624946-b28f40a0ca4b?w=800" 
                            },
                            contentDescription = "Recipe of the day: ${recipe.name}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    
                    // Gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.7f)
                                    )
                                )
                            )
                    )
                    
                                            // Recipe info
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        ) {
                            Text(
                                text = recipe.name,
                            style = typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = "Cooking time",
                                    tint = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${recipe.time} min",
                                    style = typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = "Calories",
                                    tint = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${recipe.calories} cal",
                                    style = typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                    
                    // Category badge
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = colors.primary.copy(alpha = 0.9f)
                    ) {
                        Text(
                            text = recipe.category,
                            style = typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                } else {
                    // Error state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Restaurant,
                                contentDescription = "No recipe",
                                tint = colors.onSurface.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No recipe available",
                                style = typography.bodyMedium,
                                color = colors.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Get a personalized recipe of the day based on user preferences and allergies
 */
private suspend fun getPersonalizedRecipeOfTheDay(
    recipeRepository: RecipeRepository,
    context: Context,
    profile: Profile,
    onResult: (RecipeRepository.RecipeListItem?) -> Unit
) {
    try {
        val prefs = context.getSharedPreferences("recipe_of_day", Context.MODE_PRIVATE)
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        val lastRecipeId = prefs.getString("last_recipe_id", "")
        val lastRecipeDate = prefs.getString("last_recipe_date", "")
        
        // If we already have a recipe for today, try to use it
        if (lastRecipeDate == today && lastRecipeId?.isNotEmpty() == true) {
            // Try to get the same recipe as yesterday if it's still valid
            recipeRepository.getRecipeDetails(lastRecipeId).collect { result ->
                result.fold(
                    onSuccess = { recipe ->
                        if (isRecipeCompatibleWithProfile(recipe, profile)) {
                            onResult(RecipeRepository.RecipeListItem.fromDetailedRecipe(recipe))
                            return@collect
                        }
                    },
                    onFailure = { /* Continue to get new recipe */ }
                )
                
                // If cached recipe failed or incompatible, get a new one
                getNewPersonalizedRecipe(recipeRepository, context, profile, lastRecipeId, onResult)
            }
        } else {
            // Get a new recipe for today
            getNewPersonalizedRecipe(recipeRepository, context, profile, lastRecipeId, onResult)
        }
        
    } catch (e: Exception) {
        Log.e("RecipeOfTheDay", "Error getting personalized recipe", e)
        onResult(null)
    }
}

/**
 * Get a new personalized recipe that hasn't been shown recently
 */
private suspend fun getNewPersonalizedRecipe(
    recipeRepository: RecipeRepository,
    context: Context,
    profile: Profile,
    excludeRecipeId: String?,
    onResult: (RecipeRepository.RecipeListItem?) -> Unit
) {
    // Get random recipes and filter them
    recipeRepository.getRandomRecipes(20).collect { result ->
        result.fold(
            onSuccess = { recipes ->
                val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                
                // Filter recipes based on user preferences and allergies
                val compatibleRecipes = recipes.filter { recipe ->
                    recipe.id != excludeRecipeId && isRecipeCompatibleWithProfile(recipe, profile)
                }
                
                if (compatibleRecipes.isNotEmpty()) {
                    val selectedRecipe = compatibleRecipes.random()
                    val recipeListItem = RecipeRepository.RecipeListItem.fromDetailedRecipe(selectedRecipe)
                    
                    // Cache the selection for today
                    val prefs = context.getSharedPreferences("recipe_of_day", Context.MODE_PRIVATE)
                    prefs.edit()
                        .putString("last_recipe_id", selectedRecipe.id)
                        .putString("last_recipe_date", today)
                        .apply()
                    
                    onResult(recipeListItem)
                } else {
                    // If no compatible recipes, fallback to any random recipe
                    Log.w("RecipeOfTheDay", "No compatible recipes found, using fallback")
                    getRandomRecipeOfTheDay(recipeRepository, context, onResult)
                }
            },
            onFailure = { 
                Log.e("RecipeOfTheDay", "Failed to get personalized recipes", it)
                onResult(null)
            }
        )
    }
}

/**
 * Check if a recipe is compatible with user's dietary preferences, allergies, and health goals
 */
private fun isRecipeCompatibleWithProfile(recipe: com.thenewkenya.ingrediet.data.model.DetailedRecipe, profile: Profile): Boolean {
    val recipeName = recipe.name.lowercase()
    val recipeDescription = recipe.description.lowercase()
    val recipeCategory = recipe.category.lowercase()
    val recipeTags = recipe.tags.map { it.lowercase() }
    val recipeIngredients = recipe.ingredients.map { it.name.lowercase() }
    
    // Check for allergies - if user has allergies, exclude recipes with those ingredients
    for (allergy in profile.allergies) {
        val allergyLower = allergy.lowercase()
        
        // Check in ingredients
        if (recipeIngredients.any { it.contains(allergyLower) }) {
            Log.d("RecipeOfTheDay", "Recipe ${recipe.name} excluded due to allergy: $allergy")
            return false
        }
        
        // Check in recipe name and description
        if (recipeName.contains(allergyLower) || recipeDescription.contains(allergyLower)) {
            Log.d("RecipeOfTheDay", "Recipe ${recipe.name} excluded due to allergy in name/description: $allergy")
            return false
        }
    }
    
    // Check health conditions for recipe compatibility
    for (condition in profile.healthConditions) {
        when (condition.lowercase()) {
            "diabetes" -> {
                // Avoid high-sugar recipes for diabetic users
                if (recipeTags.any { it.contains("dessert") || it.contains("sweet") || it.contains("sugar") } ||
                    recipeName.contains("cake") || recipeName.contains("cookie") || recipeName.contains("candy")) {
                    Log.d("RecipeOfTheDay", "Recipe ${recipe.name} excluded due to diabetes - high sugar content")
                    return false
                }
            }
            "hypertension", "high blood pressure" -> {
                // Avoid high-sodium recipes for users with hypertension
                if (recipeIngredients.any { it.contains("salt") || it.contains("sodium") || it.contains("pickle") } ||
                    recipeTags.any { it.contains("salty") || it.contains("processed") }) {
                    Log.d("RecipeOfTheDay", "Recipe ${recipe.name} excluded due to hypertension - high sodium content")
                    return false
                }
            }
            "heart disease" -> {
                // Prefer heart-healthy recipes
                if (recipeIngredients.any { it.contains("fried") || it.contains("butter") } ||
                    recipeTags.any { it.contains("fried") || it.contains("fatty") }) {
                    Log.d("RecipeOfTheDay", "Recipe ${recipe.name} excluded due to heart disease - high fat content")
                    return false
                }
            }
        }
    }
    
    // Check dietary preferences - if user has specific dietary preferences, try to match them
    if (profile.dietaryPreferences.isNotEmpty()) {
        val hasMatchingPreference = profile.dietaryPreferences.any { preference ->
            val prefLower = preference.lowercase()
            
            // Check if recipe tags match dietary preferences
            recipeTags.any { it.contains(prefLower) } ||
            recipeCategory.contains(prefLower) ||
            recipeName.contains(prefLower) ||
            recipeDescription.contains(prefLower)
        }
        
        // If user has specific preferences but recipe doesn't match any, lower priority but don't exclude
        if (!hasMatchingPreference) {
            Log.d("RecipeOfTheDay", "Recipe ${recipe.name} doesn't match preferences but allowed")
        }
    }
    
    // Health goal-based recommendations
    if (profile.healthGoals.isNotEmpty()) {
        val hasHealthMatch = profile.healthGoals.any { goal ->
            when (goal.lowercase()) {
                "weight loss" -> {
                    // Prefer low-calorie recipes for weight loss
                    recipe.nutritionFacts.calories < 400 ||
                    recipeTags.any { it.contains("low-calorie") || it.contains("light") || it.contains("salad") }
                }
                "muscle gain", "muscle building" -> {
                    // Prefer high-protein recipes for muscle building
                    recipe.nutritionFacts.protein > 20 ||
                    recipeIngredients.any { it.contains("chicken") || it.contains("fish") || it.contains("egg") || it.contains("beans") } ||
                    recipeTags.any { it.contains("protein") || it.contains("muscle") }
                }
                "heart health" -> {
                    // Prefer heart-healthy recipes
                    recipeTags.any { it.contains("heart-healthy") || it.contains("omega") } ||
                    recipeIngredients.any { it.contains("salmon") || it.contains("avocado") || it.contains("olive oil") }
                }
                "better digestion" -> {
                    // Prefer fiber-rich and probiotic recipes
                    recipeTags.any { it.contains("fiber") || it.contains("probiotic") } ||
                    recipeIngredients.any { it.contains("yogurt") || it.contains("vegetables") || it.contains("whole grain") }
                }
                else -> false
            }
        }
        
        if (hasHealthMatch) {
            Log.d("RecipeOfTheDay", "Recipe ${recipe.name} prioritized for health goals")
        }
    }
    
    // BMI-based calorie recommendations
    val bmi = profile.bmi
    if (bmi != null) {
        when {
            bmi < 18.5 -> {
                // Underweight - prefer higher calorie recipes
                if (recipe.nutritionFacts.calories < 300) {
                    Log.d("RecipeOfTheDay", "Recipe ${recipe.name} deprioritized for underweight user - too low calorie")
                }
            }
            bmi > 25.0 -> {
                // Overweight - prefer lower calorie recipes
                if (recipe.nutritionFacts.calories > 600) {
                    Log.d("RecipeOfTheDay", "Recipe ${recipe.name} deprioritized for overweight user - too high calorie")
                }
            }
        }
    }
    
    return true
}

/**
 * Fallback function to get any random recipe
 */
private suspend fun getRandomRecipeOfTheDay(
    recipeRepository: RecipeRepository,
    context: Context,
    onResult: (RecipeRepository.RecipeListItem?) -> Unit
) {
    recipeRepository.getRandomRecipes(1).collect { result ->
        result.fold(
            onSuccess = { recipes ->
                if (recipes.isNotEmpty()) {
                    val detailedRecipe = recipes.first()
                    onResult(RecipeRepository.RecipeListItem.fromDetailedRecipe(detailedRecipe))
                } else {
                    onResult(null)
                }
            },
            onFailure = { 
                Log.e("RecipeOfTheDay", "Failed to load fallback recipe", it)
                onResult(null)
            }
        )
    }
}


