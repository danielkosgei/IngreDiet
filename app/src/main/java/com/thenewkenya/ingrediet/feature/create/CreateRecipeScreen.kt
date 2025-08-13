package com.thenewkenya.ingrediet.feature.create

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Kitchen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.thenewkenya.ingrediet.data.model.DetailedRecipe
import com.thenewkenya.ingrediet.feature.components.RecipeCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRecipeScreen(
    navController: NavController,
    viewModel: CreateRecipeViewModel = remember { CreateRecipeViewModel() }
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val focusManager = LocalFocusManager.current

    // Collect state values
    val currentIngredient by viewModel.currentIngredient.collectAsState()
    val ingredients by viewModel.ingredients.collectAsState()
    val matchingRecipes by viewModel.matchingRecipes.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val hasSearched by viewModel.hasSearched.collectAsState()
    val searchError by viewModel.searchError.collectAsState()
    val needsMoreIngredients by viewModel.needsMoreIngredients.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Recipe Finder",
                        style = typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.surface,
                    titleContentColor = colors.onSurface
                )
            )
        },
        containerColor = colors.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header Section with illustration
            HeaderSection(
                colors = colors,
                typography = typography,
                hasIngredients = ingredients.isNotEmpty(),
                isSearching = isSearching,
                hasSearched = hasSearched,
                needsMoreIngredients = needsMoreIngredients
            )
            
            // Main Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Ingredient Input Section
                item {
                    IngredientInputSection(
                        currentIngredient = currentIngredient,
                        onIngredientChange = viewModel::updateCurrentIngredient,
                        onAddIngredient = {
                            viewModel.addIngredientForSearch()
                            focusManager.clearFocus()
                        },
                        colors = colors,
                        typography = typography
                    )
                }
                
                // Selected Ingredients
                if (ingredients.isNotEmpty()) {
                    item {
                        SelectedIngredientsSection(
                            ingredients = ingredients,
                            onRemoveIngredient = viewModel::removeIngredient,
                            colors = colors,
                            typography = typography
                        )
                    }
                }
                
                // Recipe Results
                if (ingredients.isNotEmpty()) {
                    item {
                        RecipeResultsSection(
                            matchingRecipes = matchingRecipes,
                            isSearching = isSearching,
                            hasSearched = hasSearched,
                            searchError = searchError,
                            needsMoreIngredients = needsMoreIngredients,
                            ingredientCount = ingredients.size,
                            navController = navController,
                            colors = colors,
                            typography = typography
                        )
                    }
                }
                
                // Empty State
                if (ingredients.isEmpty()) {
                    item {
                        EmptyStateSection(
                            colors = colors,
                            typography = typography
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderSection(
    colors: androidx.compose.material3.ColorScheme,
    typography: androidx.compose.material3.Typography,
    hasIngredients: Boolean,
    isSearching: Boolean,
    hasSearched: Boolean,
    needsMoreIngredients: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                containerColor = when {
                    isSearching -> colors.primaryContainer.copy(alpha = 0.4f)
                    needsMoreIngredients -> colors.tertiaryContainer.copy(alpha = 0.3f)
                    hasIngredients -> colors.secondaryContainer.copy(alpha = 0.3f)
                    else -> colors.primaryContainer.copy(alpha = 0.3f)
                }
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Text(
                        text = when {
                            isSearching -> "Searching for recipes..."
                            needsMoreIngredients -> "Add one more ingredient"
                            hasIngredients && hasSearched -> "Great! Let's find recipes"
                            hasIngredients -> "Great! Let's find recipes"
                            else -> "What's in your kitchen?"
                        },
                        style = typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Text(
                        text = when {
                            isSearching -> "Looking for recipes that match your ingredients and preferences..."
                            needsMoreIngredients -> "We need at least 2 ingredients to find the best recipe matches for you"
                            hasIngredients && hasSearched -> "Here are personalized recipes you can make with your ingredients"
                            hasIngredients -> "Here are recipes you can make with your ingredients"
                            else -> "Add ingredients you have and discover recipes you can cook right now"
                        },
                        style = typography.bodyMedium,
                        color = colors.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isSearching -> colors.primary.copy(alpha = 0.2f)
                            needsMoreIngredients -> colors.tertiary.copy(alpha = 0.15f)
                            hasIngredients -> colors.secondary.copy(alpha = 0.1f)
                            else -> colors.primary.copy(alpha = 0.1f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSearching) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = colors.primary,
                        strokeWidth = 3.dp
                    )
                } else {
                    Icon(
                        imageVector = when {
                            needsMoreIngredients -> Icons.Default.Add
                            hasIngredients -> Icons.Default.RestaurantMenu
                            else -> Icons.Outlined.Kitchen
                        },
                        contentDescription = null,
                        tint = when {
                            needsMoreIngredients -> colors.tertiary
                            hasIngredients -> colors.secondary
                            else -> colors.primary
                        },
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun IngredientInputSection(
    currentIngredient: String,
    onIngredientChange: (String) -> Unit,
    onAddIngredient: () -> Unit,
    colors: androidx.compose.material3.ColorScheme,
    typography: androidx.compose.material3.Typography
) {
    Column {
        Text(
            text = "Add Your Ingredients",
            style = typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = colors.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = currentIngredient,
                onValueChange = onIngredientChange,
                placeholder = { 
                    Text(
                        "e.g., chicken, tomatoes, rice...",
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
                    if (currentIngredient.isNotEmpty()) {
                        IconButton(onClick = { onIngredientChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = colors.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = colors.outline.copy(alpha = 0.5f),
                    focusedContainerColor = colors.surface,
                    unfocusedContainerColor = colors.surface
                ),
                singleLine = true
            )
            
            FilledTonalButton(
                onClick = onAddIngredient,
                enabled = currentIngredient.isNotBlank(),
                modifier = Modifier.height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = colors.primaryContainer,
                    contentColor = colors.onPrimaryContainer
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add")
            }
        }
    }
}

@Composable
private fun SelectedIngredientsSection(
    ingredients: List<String>,
    onRemoveIngredient: (String) -> Unit,
    colors: androidx.compose.material3.ColorScheme,
    typography: androidx.compose.material3.Typography
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Your Ingredients",
                style = typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface
            )
            Text(
                text = "${ingredients.size} ingredient${if (ingredients.size != 1) "s" else ""}",
                style = typography.bodyMedium,
                color = colors.primary,
                fontWeight = FontWeight.Medium
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(ingredients) { ingredient ->
                IngredientChip(
                    ingredient = ingredient,
                    onRemove = { onRemoveIngredient(ingredient) },
                    colors = colors
                )
            }
        }
    }
}

@Composable
private fun IngredientChip(
    ingredient: String,
    onRemove: () -> Unit,
    colors: androidx.compose.material3.ColorScheme
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = colors.secondaryContainer,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = ingredient,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSecondaryContainer,
                fontWeight = FontWeight.Medium
            )
            
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(colors.onSecondaryContainer.copy(alpha = 0.2f))
                    .clickable { onRemove() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = colors.onSecondaryContainer,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

@Composable
private fun RecipeResultsSection(
    matchingRecipes: List<DetailedRecipe>,
    isSearching: Boolean,
    hasSearched: Boolean,
    searchError: String?,
    needsMoreIngredients: Boolean,
    ingredientCount: Int,
    navController: NavController,
    colors: androidx.compose.material3.ColorScheme,
    typography: androidx.compose.material3.Typography
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recipe Suggestions",
                style = typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface
            )
            
            if (!isSearching && matchingRecipes.isNotEmpty()) {
                Text(
                    text = "${matchingRecipes.size} recipe${if (matchingRecipes.size != 1) "s" else ""} found",
                    style = typography.bodyMedium,
                    color = colors.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
                when {
            needsMoreIngredients -> {
                // Waiting for more ingredients
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colors.tertiaryContainer.copy(alpha = 0.2f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(colors.tertiary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = colors.tertiary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Almost there!",
                            style = typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.onSurface
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Add one more ingredient to start finding amazing recipes that match what you have in your kitchen.",
                            style = typography.bodyMedium,
                            color = colors.onSurface.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Suggestion chips for common ingredients
                        Text(
                            text = "Popular additions:",
                            style = typography.labelMedium,
                            color = colors.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(listOf("Salt", "Pepper", "Oil", "Garlic", "Onion")) { ingredient ->
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = colors.tertiaryContainer.copy(alpha = 0.5f),
                                    modifier = Modifier.clip(RoundedCornerShape(16.dp))
                                ) {
                                    Text(
                                        text = ingredient,
                                        style = typography.bodySmall,
                                        color = colors.onTertiaryContainer,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            isSearching -> {
                // Loading State with message
                Column {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = colors.primaryContainer.copy(alpha = 0.2f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = colors.primary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Finding recipes...",
                                    style = typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.onSurface
                                )
                                Text(
                                    text = "Checking your preferences and allergies",
                                    style = typography.bodySmall,
                                    color = colors.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Shimmer placeholders
                    repeat(3) {
                        ShimmerRecipeCard(colors = colors)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
            
            searchError != null -> {
                // Error State
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colors.errorContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = null,
                            tint = colors.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Search Error",
                            style = typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = searchError,
                            style = typography.bodyMedium,
                            color = colors.onErrorContainer.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            
            matchingRecipes.isNotEmpty() -> {
                // Recipe Results with smooth animations
                matchingRecipes.forEachIndexed { index, recipe ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = tween(300, delayMillis = index * 50)) + 
                                expandVertically(animationSpec = tween(300, delayMillis = index * 50))
                    ) {
                        RecipeCard(
                            recipe = recipe.toRecipe(),
                            onClick = { navController.navigate("recipe/${recipe.id}") }
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                // Success message at the end
                if (matchingRecipes.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = colors.secondaryContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "✨ All recipes are from your database and personalized based on your preferences and dietary restrictions",
                                style = typography.bodySmall,
                                color = colors.onSecondaryContainer.copy(alpha = 0.8f),
                                modifier = Modifier.padding(16.dp),
                                textAlign = TextAlign.Center
                            )
                            
                            if (matchingRecipes.any { it.cuisineType == "Kenyan" }) {
                                Text(
                                    text = "🇰🇪 Includes authentic Kenyan recipes",
                                    style = typography.labelSmall,
                                    color = colors.primary.copy(alpha = 0.8f),
                                    modifier = Modifier.padding(top = 4.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
            
            hasSearched && matchingRecipes.isEmpty() -> {
                // No Results (only show if we've actually searched)
                NoResultsCard(
                    ingredientCount = ingredientCount,
                    colors = colors,
                    typography = typography
                )
            }
        }
    }
}

@Composable
private fun ShimmerRecipeCard(
    colors: androidx.compose.material3.ColorScheme
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surfaceVariant.copy(alpha = 0.6f))
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(colors.surfaceVariant.copy(alpha = 0.6f))
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(colors.surfaceVariant.copy(alpha = 0.4f))
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(colors.surfaceVariant.copy(alpha = 0.4f))
                )
            }
        }
    }
}

@Composable
private fun NoResultsCard(
    ingredientCount: Int,
    colors: androidx.compose.material3.ColorScheme,
    typography: androidx.compose.material3.Typography
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = colors.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "No recipes found",
                style = typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Try adding more ingredients or different combinations to find recipes you can make.",
                style = typography.bodyMedium,
                color = colors.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun EmptyStateSection(
    colors: androidx.compose.material3.ColorScheme,
    typography: androidx.compose.material3.Typography
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.Kitchen,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(64.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Ready to cook?",
                style = typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Start by adding ingredients you have in your kitchen. We'll suggest delicious recipes you can make right now!",
                style = typography.bodyLarge,
                color = colors.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Example ingredients
            Text(
                text = "Popular ingredients:",
                style = typography.labelMedium,
                color = colors.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(listOf("Chicken", "Rice", "Tomatoes", "Onions", "Garlic")) { ingredient ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = colors.primaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.clip(RoundedCornerShape(16.dp))
                    ) {
                        Text(
                            text = ingredient,
                            style = typography.bodySmall,
                            color = colors.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
} 