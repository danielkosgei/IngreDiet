package com.thenewkenya.ingrediet.feature.favorites

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.*
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.thenewkenya.ingrediet.feature.components.RecipeCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    navController: NavController,
    viewModel: FavoritesViewModel = viewModel(
        factory = FavoritesViewModelFactory(LocalContext.current)
    )
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val coroutineScope = rememberCoroutineScope()

    // Collect state values
    val favorites by viewModel.favorites.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    // Local UI state
    var isRefreshing by remember { mutableStateOf(false) }
    var showContent by remember { mutableStateOf(false) }
    
    // Animation effects
    LaunchedEffect(Unit) {
        delay(100) // Small delay for better animation
        showContent = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "My Favorites",
                        style = typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold)
                    ) 
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.navigateUp() },
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Back",
                            tint = colors.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.surface,
                    titleContentColor = colors.onSurface
                ),
                actions = {
                    IconButton(
                        onClick = { 
                            isRefreshing = true
                            coroutineScope.launch {
                                viewModel.refreshFavorites()
                                delay(1000) // Give some visual feedback
                                isRefreshing = false
                            }
                        },
                        enabled = !isLoading && !isRefreshing,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(40.dp)
                    ) {
                        if (isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = colors.primary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh favorites",
                                tint = colors.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        AnimatedVisibility(
            visible = showContent,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(durationMillis = 500)
            ) + fadeIn(animationSpec = tween(durationMillis = 500)),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(colors.background)
            ) {
                when {
                    isLoading && favorites.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                CircularProgressIndicator(
                                    color = colors.primary,
                                    modifier = Modifier.size(48.dp),
                                    strokeWidth = 4.dp
                                )
                                Text(
                                    text = "Loading your favorites...",
                                    style = typography.bodyLarge,
                                    color = colors.onBackground.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                    
                    error != null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                modifier = Modifier
                                    .padding(24.dp)
                                    .fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = colors.errorContainer
                                ),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = 4.dp
                                )
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(16.dp),
                                    modifier = Modifier.padding(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Error,
                                        contentDescription = null,
                                        tint = colors.error,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    
                                    Text(
                                        text = "Unable to load your favorites",
                                        style = typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = colors.onErrorContainer,
                                        textAlign = TextAlign.Center
                                    )
                                    
                                    Text(
                                        text = error ?: "An error occurred while retrieving your favorite recipes.",
                                        style = typography.bodyMedium,
                                        color = colors.onErrorContainer.copy(alpha = 0.8f),
                                        textAlign = TextAlign.Center
                                    )
                                    
                                    Button(
                                        onClick = { viewModel.refreshFavorites() },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = colors.error,
                                            contentColor = colors.onError
                                        ),
                                        modifier = Modifier
                                            .padding(top = 8.dp)
                                            .height(48.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Try Again")
                                    }
                                }
                            }
                        }
                    }
                    
                    favorites.isEmpty() -> {
                        EmptyFavoritesView(navController, colors, typography)
                    }
                    
                    else -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp, top = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${favorites.size} Recipe${if (favorites.size != 1) "s" else ""}",
                                    style = typography.titleMedium,
                                    color = colors.onBackground.copy(alpha = 0.7f)
                                )
                                
                                if (isRefreshing) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp,
                                            color = colors.primary
                                        )
                                        Text(
                                            text = "Refreshing...",
                                            style = typography.bodySmall,
                                            color = colors.primary
                                        )
                                    }
                                }
                            }
                            
                            LazyVerticalStaggeredGrid(
                                columns = StaggeredGridCells.Fixed(2),
                                contentPadding = PaddingValues(bottom = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalItemSpacing = 16.dp,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(favorites) { recipe ->
                                    RecipeCardEnhanced(recipe = recipe.toRecipe()) {
                                        navController.navigate("recipe/${recipe.id}")
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
fun RecipeCardEnhanced(
    recipe: com.thenewkenya.ingrediet.data.model.Recipe,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp,
            pressedElevation = 4.dp
        )
    ) {
        Column {
            // Recipe Image without gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                // Image
                coil3.compose.AsyncImage(
                    model = recipe.imageUrl,
                    contentDescription = recipe.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
                
                // Favorite icon
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(32.dp)
                        .clip(RoundedCornerShape(50))
                        .background(colors.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = colors.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Recipe name and info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = recipe.name,
                    style = typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestaurantMenu,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${recipe.cookingTime} min",
                            style = typography.bodySmall,
                            color = colors.onSurfaceVariant
                        )
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestaurantMenu,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${recipe.nutritionFacts.calories} cal",
                            style = typography.bodySmall,
                            color = colors.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyFavoritesView(
    navController: NavController,
    colors: ColorScheme,
    typography: Typography
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(0.85f),
            colors = CardDefaults.cardColors(
                containerColor = colors.surface
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(48.dp)
                )
                
                Text(
                    text = "No Favorites Yet",
                    style = typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = colors.onSurface,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "Save your favorite recipes for quick access and meal planning.",
                    style = typography.bodyLarge,
                    color = colors.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                
                Button(
                    onClick = { navController.navigate("home") },
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        contentColor = colors.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.RestaurantMenu,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Browse Recipes",
                        style = typography.titleSmall
                    )
                }
            }
        }
    }
} 