package com.thenewkenya.ingrediet.feature.recipe

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.AddShoppingCart
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.PlaylistAddCheck
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Water
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.thenewkenya.ingrediet.data.model.DetailedRecipe
import com.thenewkenya.ingrediet.data.model.IngredientItem
import com.thenewkenya.ingrediet.ui.theme.Primary
import kotlinx.coroutines.delay


// CompositionLocal for RecipeDetailViewModel
val LocalRecipeDetailViewModel = compositionLocalOf<RecipeDetailViewModel> {
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
    
    // Collect snackbar messages
    LaunchedEffect(viewModel) {
        viewModel.snackbarMessage.collect { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
        }
    }
    
    // Observe auth error
    val authError by viewModel.authError.collectAsState()
    
    LaunchedEffect(recipeId) {
        viewModel.loadRecipe(recipeId)
    }
    
    // Initialize servings when recipe is loaded
    LaunchedEffect(recipe) {
        recipe?.let {
            viewModel.updateServings(it.servings)
        }
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(32.dp)
                            ),
                        shape = RoundedCornerShape(32.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        tonalElevation = 0.dp
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
                                        .padding(4.dp)
                                        .height(36.dp),
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (selected) {
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                    } else {
                                        Color.Transparent
                                    },
                                    onClick = { selectedTabIndex = index }
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = title,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = if (selected) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            },
                                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                            fontSize = 13.sp
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
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 1.dp
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Step by Step Instructions",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                recipe.instructions.forEachIndexed { index, instruction ->
                                    if (index > 0) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                    }
                                    InstructionItem(index + 1, instruction)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
                1 -> {
                    // Ingredients tab
                    item {
                        val viewModel = LocalRecipeDetailViewModel.current
                        IngredientsSection(
                            ingredients = recipe.ingredients,
                            servings = recipe.servings,
                            onAddAllToShoppingList = { viewModel.addAllIngredientsToShoppingList() }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
                2 -> {
                    // Nutrition tab
                    item {
                        NutritionSection(recipe)
                        Spacer(modifier = Modifier.height(16.dp))
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
        val context = LocalContext.current
        var isLoading by remember { mutableStateOf(true) }
        var cals by remember { mutableStateOf(0) }
        var prot by remember { mutableStateOf(0f) }
        var carbs by remember { mutableStateOf(0f) }
        var fat by remember { mutableStateOf(0f) }
        var fiber by remember { mutableStateOf<Float?>(null) }
        var sugar by remember { mutableStateOf<Float?>(null) }

        LaunchedEffect(recipe.id) {
            isLoading = true
            try {
                val repo = com.thenewkenya.ingrediet.data.repository.NutritionRepository(context)
                fun factors(name: String, desc: String?): Triple<Float, Float, Float> {
                    val txt = (name + " " + (desc ?: "")).lowercase()
                    return when {
                        txt.contains("fried") || txt.contains("fry") -> Triple(1.0f, 0.95f, 1.15f)
                        txt.contains("roast") || txt.contains("baked") -> Triple(1.0f, 1.05f, 1.05f)
                        txt.contains("boil") || txt.contains("simmer") -> Triple(0.95f, 1.0f, 1.0f)
                        else -> Triple(1.0f, 1.0f, 1.0f)
                    }
                }
                val (carbFactor, proteinFactor, fatFactor) = factors(recipe.name, recipe.description)
                var sumCals = 0
                var sumProt = 0f
                var sumCarb = 0f
                var sumFat = 0f
                var sumFiber: Float? = null
                var sumSugar: Float? = null

                for (ing in recipe.ingredients) {
                    val off = repo.getNutritionByName(ing.name) ?: continue
                    val grams = com.thenewkenya.ingrediet.feature.recipe.UnitConversion.toGrams(ing.quantity, ing.unit, ing.name)
                    val totals = com.thenewkenya.ingrediet.feature.recipe.NutritionMath.totalForWeight(off.per100g, grams)
                    sumCals += totals.calories
                    sumProt += totals.protein * proteinFactor
                    sumCarb += totals.carbs * carbFactor
                    sumFat += totals.fat * fatFactor
                    totals.fiber?.let { sumFiber = (sumFiber ?: 0f) + it }
                    totals.sugar?.let { sumSugar = (sumSugar ?: 0f) + it }
                }
                cals = sumCals
                prot = sumProt
                carbs = sumCarb
                fat = sumFat
                fiber = sumFiber
                sugar = sumSugar
            } finally {
                isLoading = false
            }
        }
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

        if (isLoading) {
            // Skeleton placeholders (no zero values shown)
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(96.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(96.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(96.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(96.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    )
                }
            }
        } else {
            // Nutrition Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NutritionCard(
                    title = "Calories",
                    value = cals.toString(),
                    unit = "kcal",
                    modifier = Modifier.weight(1f)
                )
                NutritionCard(
                    title = "Protein",
                    value = "${prot.toInt()}",
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
                    value = "${carbs.toInt()}",
                    unit = "g",
                    modifier = Modifier.weight(1f)
                )
                NutritionCard(
                    title = "Fat",
                    value = "${fat.toInt()}",
                    unit = "g",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Additional nutrition info if available
        if (!isLoading) fiber?.let { fiberVal ->
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val fiberStr = fiberVal.toInt().toString()
                NutritionCard(
                    title = "Fiber",
                    value = fiberStr,
                    unit = "g",
                    modifier = Modifier.weight(1f)
                )
                sugar?.let { sVal ->
                    val sugarStr = sVal.toInt().toString()
                    NutritionCard(title = "Sugar", value = sugarStr, unit = "g", modifier = Modifier.weight(1f))
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
private fun IngredientsSection(
    ingredients: List<IngredientItem>,
    servings: Int,
    onAddAllToShoppingList: () -> Unit
) {
    val viewModel = LocalRecipeDetailViewModel.current
    val currentServings by viewModel.servings.collectAsState()
    val selectedIngredients by viewModel.selectedIngredients.collectAsState()
    
    // Shared state for showing checkboxes across all ingredients
    var showCheckboxes by remember { mutableStateOf(false) }
    
    // Hide checkboxes when no ingredients are selected
    LaunchedEffect(selectedIngredients) {
        if (selectedIngredients.isEmpty() && showCheckboxes) {
            // Add a small delay to ensure the animation completes smoothly
            delay(300)
            showCheckboxes = false
        }
    }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(
                start = 20.dp,
                end = 20.dp,
                top = 24.dp,
                bottom = 16.dp
            )
        ) {
            // Title row with Ingredients heading
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ingredients",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                // Servings adjuster in the top row with clear label
                Text(
                    text = "Servings: $servings",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Remove the standalone servings adjuster surface and keep only a small spacer
            Spacer(modifier = Modifier.height(16.dp))
            
            // Add selected ingredients button if any are selected
            if (selectedIngredients.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = { viewModel.addSelectedIngredientsToShoppingList() },
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AddShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Selected (${selectedIngredients.size})")
                    }
                }
            }
            
            // Ingredients list
            ingredients.forEach { ingredient ->
                val isSelected = ingredient.id in selectedIngredients
                val originalQuantity = ingredient.quantity
                val adjustedQuantity = originalQuantity
                
                IngredientItem(
                    ingredient = ingredient.copy(quantity = adjustedQuantity),
                    isSelected = isSelected,
                    showCheckbox = showCheckboxes,
                    onToggleSelection = { viewModel.toggleIngredientSelection(ingredient.id) },
                    onLongPress = { showCheckboxes = !showCheckboxes },
                    onAddToShoppingList = { viewModel.addIngredientToShoppingList(ingredient.copy(quantity = adjustedQuantity)) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun IngredientItem(
    ingredient: IngredientItem,
    isSelected: Boolean = false,
    showCheckbox: Boolean = false,
    onToggleSelection: () -> Unit = {},
    onLongPress: () -> Unit = {},
    onAddToShoppingList: () -> Unit = {}
) {
    // Add state for showing ingredient details bottom sheet
    var showIngredientDetails by remember { mutableStateOf(false) }
    
    // Add animating color transition
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        },
        animationSpec = tween(durationMillis = 300),
        label = "backgroundColor"
    )
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { onLongPress() },
                    onTap = { if (!showCheckbox) showIngredientDetails = true }
                )
            },
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = 8.dp,
                    end = 12.dp,
                    top = 12.dp,
                    bottom = 12.dp
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Selection checkbox (shown when showCheckbox is true)
            AnimatedVisibility(
                visible = showCheckbox,
                enter = fadeIn(animationSpec = tween(300)) + expandHorizontally(
                    animationSpec = tween(300),
                    expandFrom = Alignment.Start
                ),
                exit = fadeOut(animationSpec = tween(200)) + shrinkHorizontally(
                    animationSpec = tween(200),
                    shrinkTowards = Alignment.Start
                )
            ) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggleSelection() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        uncheckedColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
            
            // Ingredient image
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(ingredient.imageUrl ?: "")
                    .crossfade(true)
                    .build(),
                contentDescription = ingredient.name,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop
            )
            
            // Ingredient name
            Text(
                text = formatIngredientPhrase(ingredient.quantity, ingredient.unit, ingredient.name),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Add to shopping list button
            FilledIconButton(
                onClick = onAddToShoppingList,
                modifier = Modifier.size(36.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.AddShoppingCart,
                    contentDescription = "Add ${ingredient.name} to shopping list",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
    
    // Show ingredient details bottom sheet when clicked
    if (showIngredientDetails) {
        IngredientDetailsBottomSheet(
            ingredient = ingredient,
            onDismiss = { showIngredientDetails = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IngredientDetailsBottomSheet(
    ingredient: IngredientItem,
    onDismiss: () -> Unit
) {
    val bottomSheetState = rememberModalBottomSheetState()
    val scrollState = rememberScrollState()
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = bottomSheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(16.dp)
                .padding(bottom = 24.dp), // Extra padding for bottom gesture area
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with ingredient name
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = ingredient.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
                // Nutritional info
                InfoSection(
                    title = "Nutritional Information",
                    content = {
                        val context = LocalContext.current
                        var isLoading by remember { mutableStateOf(true) }
                        var calories by remember { mutableStateOf(0) }
                        var protein by remember { mutableStateOf(0f) }
                        var carbs by remember { mutableStateOf(0f) }
                        var fat by remember { mutableStateOf(0f) }
                        var offImageUrl by remember { mutableStateOf<String?>(null) }

                        LaunchedEffect(ingredient.name, ingredient.quantity, ingredient.unit) {
                            isLoading = true
                            val repo = com.thenewkenya.ingrediet.data.repository.NutritionRepository(context)
                            val nutrition = repo.getNutritionByName(ingredient.name)
                            if (nutrition != null) {
                                offImageUrl = nutrition.imageUrl
                                val grams = com.thenewkenya.ingrediet.feature.recipe.UnitConversion.toGrams(
                                    ingredient.quantity, ingredient.unit, ingredient.name
                                )
                                val totals = com.thenewkenya.ingrediet.feature.recipe.NutritionMath.totalForWeight(nutrition.per100g, grams)
                                calories = totals.calories
                                protein = totals.protein
                                carbs = totals.carbs
                                fat = totals.fat
                            }
                            isLoading = false
                        }

                        // If OFF provides an image, swap the header image
                        if (offImageUrl != null) {
                            // This is a no-op here since header already rendered; in a fuller impl
                            // we would hoist state to swap the AsyncImage model.
                        }

                        if (isLoading) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                NutrientItem(
                                    name = "Calories",
                                    value = "$calories kcal",
                                    icon = Icons.Outlined.LocalFireDepartment
                                )
                                NutrientItem(
                                    name = "Protein",
                                    value = "${protein.toInt()}g",
                                    icon = Icons.Outlined.FitnessCenter
                                )
                                NutrientItem(
                                    name = "Carbs",
                                    value = "${carbs.toInt()}g",
                                    icon = Icons.Outlined.Speed
                                )
                                NutrientItem(
                                    name = "Fat",
                                    value = "${fat.toInt()}g",
                                    icon = Icons.Outlined.Water
                                )
                            }
                        }
                    }
                )
                
                // Benefits
                InfoSection(
                    title = "Health Benefits",
                    content = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val nameLower = ingredient.name.lowercase()
                            val lines = when {
                                nameLower.contains("tomato") -> listOf(
                                    "Rich in lycopene, an antioxidant supporting heart health",
                                    "Provides vitamin C for immunity",
                                    "Low in calories and hydrating"
                                )
                                nameLower.contains("onion") -> listOf(
                                    "Contains quercetin which may reduce inflammation",
                                    "Source of vitamin C and fiber",
                                    "Adds prebiotics that support gut health"
                                )
                                nameLower.contains("garlic") -> listOf(
                                    "May support heart health",
                                    "Contains allicin with antibacterial properties",
                                    "Flavorful way to reduce salt usage"
                                )
                                else -> listOf(
                                    "Provides essential vitamins and minerals",
                                    "May support overall wellbeing",
                                    "Versatile addition to balanced meals"
                                )
                            }
                            lines.forEach { BenefitItem(text = it) }
                        }
                    }
                )
                
                // Description
                InfoSection(
                    title = "About ${ingredient.name}",
                    content = {
                        val nameLower = ingredient.name.lowercase()
                        val desc = when {
                            nameLower.contains("tomato") -> "Tomatoes are juicy fruits used as vegetables, known for their bright flavor and lycopene content."
                            nameLower.contains("onion") -> "Onions are aromatic bulbs that form the base of many dishes, offering sweetness when cooked."
                            nameLower.contains("garlic") -> "Garlic is a pungent bulb used worldwide to add depth and aroma to dishes."
                            else -> "A versatile ingredient commonly used in a variety of cuisines."
                        }
                        Text(desc, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                )
                
                // Store and use
                InfoSection(
                    title = "Storage & Usage",
                    content = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val nameLower = ingredient.name.lowercase()
                            val storage = when {
                                nameLower.contains("tomato") -> "Store at room temperature away from direct sun; refrigerate only when fully ripe to extend life."
                                nameLower.contains("onion") -> "Store in a cool, dry, ventilated place; keep away from potatoes."
                                nameLower.contains("garlic") -> "Keep whole bulbs in a cool, dry place; avoid airtight containers."
                                else -> "Store in a cool, dry place or refrigerate per freshness."
                            }
                            val tip = when {
                                nameLower.contains("tomato") -> "Roast to concentrate sweetness; avoid overcooking to keep texture."
                                nameLower.contains("onion") -> "Sauté low and slow for sweetness; caramelize for deeper flavor."
                                nameLower.contains("garlic") -> "Add minced garlic toward the end to prevent burning and bitterness."
                                else -> "Adjust seasoning and doneness to taste for best results."
                            }
                            val pairs = when {
                                nameLower.contains("tomato") -> "Basil, garlic, olive oil, mozzarella"
                                nameLower.contains("onion") -> "Garlic, thyme, butter, beef"
                                nameLower.contains("garlic") -> "Olive oil, lemon, parsley, chili"
                                else -> "Herbs, spices, olive oil, citrus"
                            }
                            UsageItem(title = "Storage", description = storage)
                            UsageItem(title = "Cooking Tips", description = tip)
                            UsageItem(title = "Pairs Well With", description = pairs)
                        }
                    }
                )
        }
    }
}

@Composable
private fun InfoSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        HorizontalDivider(
            modifier = Modifier.padding(bottom = 8.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )
        content()
    }
}

@Composable
private fun NutrientItem(
    name: String,
    value: String,
    icon: ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Primary,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun BenefitItem(
    text: String
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.Check,
            contentDescription = null,
            tint = Primary,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun UsageItem(
    title: String,
    description: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Helper functions to generate dummy content
private fun generateDescription(ingredientName: String): String {
    return "The $ingredientName is a versatile and nutritious ingredient commonly used in various cuisines around the world. It's known for its unique flavor profile and health benefits. Rich in essential vitamins and minerals, $ingredientName can be prepared in numerous ways to enhance your meals."
}

private fun generateBenefit(ingredientName: String, index: Int): String {
    return when (index) {
        1 -> "Contains essential vitamins and minerals that support overall health."
        2 -> "Rich in antioxidants that help fight inflammation and boost immunity."
        3 -> "Provides dietary fiber that supports digestive health and helps maintain healthy cholesterol levels."
        else -> "Good source of nutrients that contribute to a balanced diet."
    }
}

private fun generateStorage(ingredientName: String): String {
    return "Store $ingredientName in a cool, dry place or refrigerate to maintain freshness. Always check for signs of spoilage before use."
}

private fun generateCookingTip(ingredientName: String): String {
    return "For best results, $ingredientName should be prepared just before cooking to preserve its flavor and nutritional value."
}

private fun generatePairings(ingredientName: String): String {
    return "Garlic, onions, olive oil, lemon juice, black pepper, and fresh herbs."
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
