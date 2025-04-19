package com.thenewkenya.ingrediet.feature.recipe

import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.thenewkenya.ingrediet.data.model.DetailedRecipe
import com.thenewkenya.ingrediet.data.model.IngredientItem
import com.thenewkenya.ingrediet.ui.theme.Primary
import kotlin.math.max
import kotlin.math.min
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.TextButton

// CompositionLocal for RecipeDetailViewModel
val LocalRecipeDetailViewModel = staticCompositionLocalOf<RecipeDetailViewModel> {
    error("No RecipeDetailViewModel provided")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    navController: NavController,
    recipeId: String,
    viewModel: RecipeDetailViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val recipe by viewModel.recipe.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Observe auth error
    val authError by viewModel.authError.collectAsState()
    
    LaunchedEffect(recipeId) {
        viewModel.loadRecipe(recipeId)
    }

    CompositionLocalProvider(LocalRecipeDetailViewModel provides viewModel) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            floatingActionButton = {
                if (uiState is RecipeDetailUiState.Success && recipe != null) {
                    FloatingActionButtons(recipe!!)
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                when (uiState) {
                    is RecipeDetailUiState.Loading -> LoadingState()
                    is RecipeDetailUiState.Error -> ErrorState((uiState as RecipeDetailUiState.Error).message)
                    is RecipeDetailUiState.Success -> {
                        recipe?.let { recipeData ->
                            RecipeContent(
                                recipe = recipeData,
                                onBackPress = { navController.navigateUp() }
                            )
                        }
                    }
                }
            }
        }
    }

    // Show login dialog if auth error occurs
    if (authError != null) {
        LoginRequiredDialog(
            message = authError ?: "Please log in to continue",
            onDismiss = { viewModel.clearAuthError() },
            onLogin = { 
                viewModel.clearAuthError()
                navController.navigate("login")
            }
        )
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = Primary,
            modifier = Modifier.size(48.dp)
        )
    }
}

@Composable
private fun ErrorState(message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Error,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecipeContent(
    recipe: DetailedRecipe,
    onBackPress: () -> Unit
) {
    val scrollState = rememberLazyListState()
    val headerHeight = 300.dp
    val headerHeightPx = with(LocalDensity.current) { headerHeight.toPx() }
    val scrollOffset = remember { mutableStateOf(0f) }

    // Update scroll offset based on first visible item
    val firstVisibleItemIndex = scrollState.firstVisibleItemIndex
    val firstVisibleItemScrollOffset = scrollState.firstVisibleItemScrollOffset
    scrollOffset.value = if (firstVisibleItemIndex == 0) {
        (firstVisibleItemScrollOffset / headerHeightPx).coerceIn(0f, 1f)
    } else {
        1f
    }

    Box(modifier = Modifier.fillMaxSize()) {
        var selectedTabIndex by remember { mutableStateOf(0) }
        val tabs = listOf("Instructions", "Ingredients", "Nutrition")

        LazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Header(
                    recipe = recipe,
                    headerHeight = headerHeight,
                    scrollOffset = scrollOffset.value
                )
            }
            
            // Quick Info
            item {
                Spacer(modifier = Modifier.height(24.dp))
                QuickInfoSection(recipe)
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Tabs
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        tonalElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(4.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            tabs.forEachIndexed { index, title ->
                                val selected = selectedTabIndex == index
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(4.dp),
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (selected) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        Color.Transparent
                                    },
                                    onClick = { selectedTabIndex = index }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .padding(vertical = 8.dp, horizontal = 12.dp)
                                            .fillMaxWidth(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = title,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = if (selected) {
                                                MaterialTheme.colorScheme.onPrimaryContainer
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            },
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Tab content
            when (selectedTabIndex) {
                0 -> {
                    // Instructions tab
                    item {
                        Text(
                            text = "Step by Step Instructions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    items(recipe.instructions.withIndex().toList()) { (index, instruction) ->
                        InstructionItem(index + 1, instruction)
                    }
                }
                1 -> {
                    // Ingredients tab
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Ingredients",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "${recipe.servings} servings",
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                    items(recipe.ingredients) { ingredient ->
                        IngredientItem(ingredient)
                    }
                }
                2 -> {
                    // Nutrition tab
                    item {
                        NutritionSection(recipe)
                    }
                }
            }

            // Add bottom spacing for FAB
            item {
                Spacer(modifier = Modifier.height(88.dp))
            }
        }

        // Fixed top bar that stays on top
        TopBarOverlay(
            recipe = recipe,
            scrollOffset = scrollOffset.value,
            onBackPress = onBackPress
        )
    }
}

@Composable
private fun Header(
    recipe: DetailedRecipe,
    headerHeight: Dp,
    scrollOffset: Float
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(headerHeight)
    ) {
        // Recipe Image with parallax effect
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(recipe.imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = recipe.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = 1f - (scrollOffset * 0.5f)
                    translationY = scrollOffset * 50
                }
        )
        
        // Gradient overlay - now extends beyond visible area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationY = scrollOffset * 50 // Match image parallax
                }
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.7f),
                            Color.Black.copy(alpha = 0.3f),
                            Color.Black.copy(alpha = 0.3f),
                            Color.Black.copy(alpha = 0.7f),
                        ),
                        startY = -headerHeight.value * 0.5f, // Start gradient above visible area
                        endY = headerHeight.value * 1.5f // End gradient below visible area
                    )
                )
        )
        
        // Recipe title overlay at the bottom of the header
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
                .graphicsLayer {
                    alpha = (1f - scrollOffset).coerceIn(0f, 1f)
                }
        ) {
            Text(
                text = recipe.name,
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = recipe.rating.toString(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun TopBarOverlay(
    recipe: DetailedRecipe,
    scrollOffset: Float,
    onBackPress: () -> Unit
) {
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val surfaceColor = MaterialTheme.colorScheme.surface.copy(alpha = scrollOffset)
    
    // Get the ViewModel
    val viewModel = LocalRecipeDetailViewModel.current
    
    Column(modifier = Modifier.fillMaxWidth()) {
        // Solid status bar background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(statusBarHeight)
                .background(surfaceColor)
        )
        
        // Top bar content
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            color = surfaceColor
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp)
            ) {
                // Back button
                IconButton(
                    onClick = onBackPress,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = if (scrollOffset > 0.5f) MaterialTheme.colorScheme.onSurface else Color.White
                    )
                }
                
                // Title - only show when scrolled
                Text(
                    text = recipe.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 56.dp)
                        .graphicsLayer {
                            alpha = scrollOffset
                        }
                )
                
                // Action buttons
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Like button
                    IconButton(onClick = { viewModel.toggleFavorite() }) {
                        Icon(
                            imageVector = if (recipe.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = if (recipe.isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = if (recipe.isFavorite) MaterialTheme.colorScheme.error 
                                  else if (scrollOffset > 0.5f) MaterialTheme.colorScheme.onSurface 
                                  else Color.White
                        )
                    }
                    
                    // Share button
                    IconButton(onClick = { viewModel.shareRecipe() }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share recipe",
                            tint = if (scrollOffset > 0.5f) MaterialTheme.colorScheme.onSurface else Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickInfoSection(recipe: DetailedRecipe) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            InfoItem(
                icon = Icons.Outlined.Timer,
                label = "Prep",
                value = "${recipe.preparationTime}m"
            )
            InfoItem(
                icon = Icons.Outlined.LocalFireDepartment,
                label = "Cook",
                value = "${recipe.cookingTime}m"
            )
            InfoItem(
                icon = Icons.Outlined.Restaurant,
                label = "Serves",
                value = "${recipe.servings}"
            )
            InfoItem(
                icon = Icons.Outlined.Speed,
                label = "Level",
                value = recipe.difficulty
            )
        }
    }
}

@Composable
private fun InfoItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun NutritionSection(recipe: DetailedRecipe) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Description Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "About this Recipe",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = recipe.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Nutrition Facts
        Text(
            text = "Nutrition Facts",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Nutrition Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            NutritionCard(
                title = "Calories",
                value = recipe.nutritionFacts.getFormattedCalories(),
                unit = "kcal",
                modifier = Modifier.weight(1f)
            )
            NutritionCard(
                title = "Protein",
                value = recipe.nutritionFacts.getFormattedProtein(),
                unit = "g",
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            NutritionCard(
                title = "Carbs",
                value = recipe.nutritionFacts.getFormattedCarbs(),
                unit = "g",
                modifier = Modifier.weight(1f)
            )
            NutritionCard(
                title = "Fat",
                value = recipe.nutritionFacts.getFormattedFat(),
                unit = "g",
                modifier = Modifier.weight(1f)
            )
        }

        // Additional nutrition info if available
        recipe.nutritionFacts.fiber?.let { fiber ->
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                recipe.nutritionFacts.getFormattedFiber()?.let { fiberStr ->
                    NutritionCard(
                        title = "Fiber",
                        value = fiberStr,
                        unit = "g",
                        modifier = Modifier.weight(1f)
                    )
                }
                recipe.nutritionFacts.getFormattedSugar()?.let { sugarStr ->
                    NutritionCard(
                        title = "Sugar",
                        value = sugarStr,
                        unit = "g",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun NutritionCard(
    title: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = unit,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun IngredientItem(ingredient: IngredientItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.ShoppingCart,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(16.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ingredient.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Text(
                text = "${ingredient.quantity} ${ingredient.unit}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InstructionItem(
    stepNumber: Int,
    instruction: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stepNumber.toString(),
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = instruction,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun FloatingActionButtons(recipe: DetailedRecipe) {
    ExtendedFloatingActionButton(
        onClick = { /* TODO: Start cooking mode */ },
        containerColor = Primary,
        icon = {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "Start cooking",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        },
        text = {
            Text(
                text = "Start Cooking",
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Medium
            )
        }
    )
}

@Composable
private fun TopAppBar(
    title: @Composable () -> Unit,
    navigationIcon: @Composable () -> Unit,
    actions: @Composable () -> Unit
) {
    TopAppBar(
        title = title,
        navigationIcon = navigationIcon,
        actions = actions
    )
}

@Composable
private fun LoginRequiredDialog(
    message: String,
    onDismiss: () -> Unit,
    onLogin: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Authentication Required") },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onLogin) {
                Text("Log In")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
