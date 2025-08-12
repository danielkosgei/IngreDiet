package com.thenewkenya.ingrediet

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.thenewkenya.ingrediet.data.network.AuthManager
import com.thenewkenya.ingrediet.data.network.LocalSupabase
import com.thenewkenya.ingrediet.data.network.SessionManager
import com.thenewkenya.ingrediet.data.network.RecipeCacheService
import com.thenewkenya.ingrediet.data.network.supabase
import com.thenewkenya.ingrediet.data.repository.RecipeRepository
import com.thenewkenya.ingrediet.feature.navigation.LoadingScreen
import com.thenewkenya.ingrediet.feature.authentication.LoginScreen
import com.thenewkenya.ingrediet.feature.authentication.RegisterScreen
import com.thenewkenya.ingrediet.feature.navigation.HomeScreenContent
import com.thenewkenya.ingrediet.feature.profile.ProfileScreen
import com.thenewkenya.ingrediet.feature.recipe.RecipeDetailScreen
import com.thenewkenya.ingrediet.feature.search.SearchScreen
import com.thenewkenya.ingrediet.feature.favorites.FavoritesScreen
import com.thenewkenya.ingrediet.feature.mealplanner.MealPlannerScreen
import com.thenewkenya.ingrediet.feature.create.CreateRecipeScreen
import com.thenewkenya.ingrediet.feature.favorites.FavoritesViewModelSimpleFactory
import com.thenewkenya.ingrediet.feature.shopping.ShoppingListScreen
import com.thenewkenya.ingrediet.feature.search.IngredientSearchScreen
import com.thenewkenya.ingrediet.feature.profile.AccountScreen
import com.thenewkenya.ingrediet.feature.profile.SettingsScreen
import com.thenewkenya.ingrediet.feature.profile.SupportScreen
import com.thenewkenya.ingrediet.feature.profile.DietaryPreferencesScreen
import com.thenewkenya.ingrediet.feature.profile.AllergiesScreen
import com.thenewkenya.ingrediet.feature.profile.NutritionGoalsScreen
import com.thenewkenya.ingrediet.feature.profile.AppearanceScreen
import com.thenewkenya.ingrediet.feature.profile.NotificationsScreen
import com.thenewkenya.ingrediet.feature.profile.PrivacySecurityScreen
import com.thenewkenya.ingrediet.ui.theme.IngreDietTheme
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable edge-to-edge display
        enableEdgeToEdge()
        
        // Make system bars (status and navigation) draw over our app
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        val sessionManager = SessionManager(applicationContext)
        val authManager = AuthManager(applicationContext)
        val recipeCacheService = RecipeCacheService(applicationContext)
        
        // Preload a few recipes in the background
        preloadRecipes()
        
        setContent {
            // Initialize theme preferences and observe changes reactively
            val themeMode by com.thenewkenya.ingrediet.feature.profile.ThemePreferences.currentThemeMode
            
            // Initialize themes on first load
            LaunchedEffect(Unit) {
                com.thenewkenya.ingrediet.feature.profile.ThemePreferences.getThemeMode(this@MainActivity)
            }
            
            IngreDietTheme(themeMode = themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CompositionLocalProvider(
                        LocalSupabase provides supabase,
                        LocalSessionManager provides sessionManager,
                        LocalAuthManager provides authManager,
                        LocalRecipeCacheService provides recipeCacheService
                    ) {
                        AppNavigation()
                    }
                }
            }
        }
    }
    
    /**
     * Preload a few random recipes to ensure we always have content available
     * This runs in the background and doesn't block app startup
     */
    private fun preloadRecipes() {
        Log.d("MainActivity", "Starting recipe preloading")
        
        // Use a coroutine without lifecycleScope
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val recipeRepository = RecipeRepository(applicationContext)
                
                // Get recipes from local cache first, with a short timeout
                withTimeoutOrNull(5000) {
                    recipeRepository.getRandomRecipes(10).collect { result ->
                        result.onSuccess { recipes ->
                            Log.d("MainActivity", "Preloaded ${recipes.size} recipes")
                        }
                        result.onFailure { error ->
                            Log.e("MainActivity", "Error preloading recipes", error)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error preloading recipes", e)
            }
        }
    }
}

val LocalSessionManager = staticCompositionLocalOf<SessionManager> {
    error("No SessionManager provided")
}

val LocalAuthManager = staticCompositionLocalOf<AuthManager> {
    error("No AuthManager provided")
}

val LocalRecipeCacheService = staticCompositionLocalOf<RecipeCacheService> {
    error("No RecipeCacheService provided")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()
    var startDestination by remember { mutableStateOf("splash") }
    var isInitializing by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val sessionManager = LocalSessionManager.current
    val authManager = LocalAuthManager.current
    val supabase = LocalSupabase.current

    // Check for session directly from SharedPreferences
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            // Show splash screen during initialization
            isInitializing = true
            // Check if we have a stored session before trying to restore
            val hasSession = sessionManager.hasValidSession()
            Log.d("AppNavigation", "Has stored session: $hasSession")

            if (hasSession) {
                // Only try to restore if we have something to restore
                val sessionRestored = authManager.restoreSession()
                Log.d("AppNavigation", "Session restored: $sessionRestored")

                if (sessionRestored) {
                    startDestination = "home"
                } else {
                    startDestination = "login"
                }
            } else {
                startDestination = "login"
            }

            // Initialization complete
            isInitializing = false

            // Now monitor the actual session status
            supabase.auth.sessionStatus.collect { status ->
                Log.d("AppNavigation", "Session status update: $status")
                startDestination = when (status) {
                    is SessionStatus.Authenticated -> {
                        Log.d("AppNavigation", "User authenticated: ${status.session.user?.email}")
                        // Save session when authenticated
                        authManager.saveCurrentSession()
                        "home"
                    }
                    is SessionStatus.NotAuthenticated -> {
                        Log.d("AppNavigation", "User not authenticated")
                        "login"
                    }
                    else -> {
                        Log.d("AppNavigation", "Auth status loading")
                        if (isInitializing) "splash" else if (sessionManager.hasValidSession()) "home" else "login"
                    }
                }
            }
        }
    }

    if (isInitializing) {
        SplashScreen(isLoading = true)
    } else {
        val isMainRoute = remember { mutableStateOf(false) }
        val currentRoute = currentRoute(navController)
        
        // Check if current route is a main route that should show the bottom nav
        val mainRoutes = listOf("home", "mealplanner", "create", "shopping", "profile")
        isMainRoute.value = mainRoutes.contains(currentRoute)
        
        // Selected nav item based on current route
        val selectedNavItem = when (currentRoute) {
            "home" -> 0
            "mealplanner" -> 1
            "create" -> 2
            "shopping" -> 3
            "profile" -> 4
            else -> 0
        }
        
        Scaffold(
            bottomBar = {
                if (isMainRoute.value) {
                    MainBottomNavigation(navController, selectedNavItem)
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController, 
                startDestination = startDestination,
                modifier = Modifier.padding(
                    bottom = if (isMainRoute.value) innerPadding.calculateBottomPadding() else 0.dp
                )
            ) {
                composable("login") { LoginScreen(navController) }
                composable("register") { RegisterScreen(navController) }
                composable("forgot_password") { com.thenewkenya.ingrediet.feature.authentication.ForgotPasswordScreen(navController) }
                composable("home") { HomeScreenContent(navController) }
                composable("loading") { LoadingScreen() }
                composable("splash") { SplashScreen() }
                composable("profile") { ProfileScreen(navController) }
                composable("search") { SearchScreen(navController) }
                composable("favorites") { 
                    val context = LocalContext.current
                    val repository = com.thenewkenya.ingrediet.data.repository.FavoritesRepository.getInstance(context)
                    val factory = FavoritesViewModelSimpleFactory(repository)
                    FavoritesScreen(navController, factory) 
                }
                composable("mealplanner") { MealPlannerScreen(navController) }
                composable("create") { CreateRecipeScreen(navController) }
                composable("shopping") { ShoppingListScreen(navController) }
                composable("ingredient-search") { IngredientSearchScreen(navController) }
                
                // Profile section routes
                composable("profile/settings") { SettingsScreen(navController) }
                composable("profile/support") { SupportScreen(navController) }
                composable("profile/account") { AccountScreen(navController) }
                composable("profile/edit") { ProfileScreen(navController, true) }
                composable("profile/appearance") { AppearanceScreen(navController) }
                composable("profile/notifications") { NotificationsScreen(navController) }
                composable("profile/privacy") { PrivacySecurityScreen(navController) }
                composable("privacy_policy") { com.thenewkenya.ingrediet.feature.profile.PrivacyPolicyScreen(navController) }
                composable("inbox/notifications") { com.thenewkenya.ingrediet.feature.notifications.NotificationsInboxScreen(navController) }
                
                // Diet & Nutrition routes
                composable("profile/diet-preferences") { DietaryPreferencesScreen(navController) }
                composable("profile/allergies") { AllergiesScreen(navController) }
                composable("profile/nutrition-goals?isOnboarding={isOnboarding}") { backStackEntry ->
                    val isOnboarding = backStackEntry.arguments?.getString("isOnboarding")?.toBoolean() ?: false
                    NutritionGoalsScreen(navController, isOnboarding)
                }

                // Recipe details route with parameter
                composable(
                    route = "recipe/{recipeId}",
                    arguments = listOf(
                        navArgument("recipeId") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val recipeId = backStackEntry.arguments?.getString("recipeId") ?: ""
                    val context = LocalContext.current
                    val viewModel = viewModel<com.thenewkenya.ingrediet.feature.recipe.RecipeDetailViewModel>(
                        factory = com.thenewkenya.ingrediet.feature.recipe.RecipeDetailViewModelFactory(context)
                    )
                    RecipeDetailScreen(
                        navController = navController, 
                        recipeId = recipeId,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
fun MainBottomNavigation(navController: NavController, selectedIndex: Int) {
    val colors = MaterialTheme.colorScheme
    
    // Navigation items
    val navItems = listOf(
        Triple(Icons.Filled.Home, Icons.Outlined.Home, "Home"),
        Triple(Icons.Filled.RestaurantMenu, Icons.Outlined.RestaurantMenu, "Meal Planner"),
        Triple(Icons.Filled.Add, Icons.Outlined.Add, "Create"),
        Triple(Icons.Filled.ShoppingCart, Icons.Outlined.ShoppingCart, "Shopping"),
        Triple(Icons.Filled.Person, Icons.Outlined.Person, "Profile")
    )
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surfaceVariant)
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
                                imageVector = if (selectedIndex == index) selectedIcon else unselectedIcon,
                                contentDescription = label,
                                modifier = Modifier.size(24.dp),
                                tint = if (selectedIndex == index) 
                                    colors.primary
                                else colors.onSurfaceVariant
                            )
                            if (selectedIndex == index) {
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
                    selected = selectedIndex == index,
                    onClick = {
                        when (index) {
                            0 -> navController.navigate("home") {
                                popUpTo("home") { inclusive = true }
                            }
                            1 -> navController.navigate("mealplanner") {
                                popUpTo("home")
                            }
                            2 -> navController.navigate("create") {
                                popUpTo("home")
                            }
                            3 -> navController.navigate("shopping") {
                                popUpTo("home")
                            }
                            4 -> navController.navigate("profile") {
                                popUpTo("home")
                            }
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

@Composable
fun currentRoute(navController: NavController): String {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route ?: ""
}

@Composable
fun Gradient() {
    val colors = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        colors.primary,
                        colors.secondary,
                        colors.background
                    )
                )
            )
    )
}

@Composable
fun SplashScreen(
    isLoading: Boolean = false
) {
    val colors = MaterialTheme.colorScheme // Access current theme colors

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = colors.background // Use dynamic background color
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // App logo can go here
                Text(
                    text = "IngreDiet",
                    style = MaterialTheme.typography.headlineLarge,
                    color = colors.onBackground, // Ensure readability
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (isLoading) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = colors.primary, // Use primary color for the loader
                            modifier = Modifier.size(48.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Restoring session...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onBackground.copy(alpha = 0.7f) // Adjusted for readability
                        )
                    }
                }
            }
        }
    }
}