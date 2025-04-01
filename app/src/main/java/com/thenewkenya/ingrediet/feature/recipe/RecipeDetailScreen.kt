package com.thenewkenya.ingrediet.feature.recipe

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Fireplace
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import com.thenewkenya.ingrediet.feature.components.NutritionItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.thenewkenya.ingrediet.data.model.DetailedRecipe
import com.thenewkenya.ingrediet.data.model.IngredientItem
import com.thenewkenya.ingrediet.data.repository.RecipeRepository
import androidx.compose.foundation.clickable

@Composable
fun IngredientListItem(ingredient: IngredientItem) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = false,
            onCheckedChange = { /* Toggle ingredient checked state */ },
            colors = CheckboxDefaults.colors(
                checkedColor = colors.primary,
                uncheckedColor = colors.onSurface.copy(alpha = 0.6f)
            )
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column {
            Text(
                text = ingredient.name,
                style = typography.bodyLarge,
                color = colors.onSurface
            )

            Row {
                Text(
                    text = "${ingredient.quantity} ${ingredient.unit}",
                    style = typography.bodyMedium,
                    color = colors.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun InstructionStep(stepNumber: Int, instruction: String) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Step number circle
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(colors.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber.toString(),
                color = colors.onPrimary,
                style = typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Instruction text
        Text(
            text = instruction,
            style = typography.bodyLarge,
            color = colors.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun getProgressColor(progress: Float): Color {
    val colors = MaterialTheme.colorScheme
    return when {
        progress < 0.25f -> colors.primary
        progress < 0.5f -> colors.secondary
        progress < 0.75f -> colors.tertiary
        else -> colors.error
    }
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
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    
    // Track the selected tab
    val tabs = listOf("Recipe", "Ingredients")
    var selectedTabIndex by remember { mutableStateOf(0) }

    // For snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    val addToShoppingListResult by viewModel.addToShoppingListResult.collectAsState()
    
    // Show snackbar when add to shopping list result changes
    LaunchedEffect(addToShoppingListResult) {
        addToShoppingListResult?.let { result ->
            val snackbarMessage = when (result) {
                is AddToShoppingListResult.Success -> 
                    "Added ${result.count} ingredients to shopping list"
                is AddToShoppingListResult.PartialSuccess -> 
                    "Added ${result.successCount} out of ${result.totalCount} ingredients"
                is AddToShoppingListResult.Error -> 
                    "Error: ${result.message}"
            }
            
            snackbarHostState.showSnackbar(
                message = snackbarMessage,
                duration = SnackbarDuration.Short
            )
            
            // Reset the result after showing the snackbar
            viewModel.resetAddToShoppingListResult()
        }
    }

    // Use theme colors instead of hardcoded colors
    val backgroundColor = colors.background
    val accentColor = colors.primary
    val cardBackground = colors.surface
    val textPrimary = colors.onBackground
    val textSecondary = colors.onBackground.copy(alpha = 0.7f)

    LaunchedEffect(recipeId) {
        viewModel.loadRecipe(recipeId)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        containerColor = backgroundColor,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(backgroundColor)
        ) {
            when (uiState) {
                is RecipeDetailUiState.Loading -> {
                    CircularProgressIndicator(
                        color = accentColor,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is RecipeDetailUiState.Error -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Error,
                            contentDescription = null,
                            tint = colors.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = (uiState as RecipeDetailUiState.Error).message,
                            style = typography.bodyLarge,
                            color = colors.error
                        )
                        Button(
                            onClick = { navController.navigateUp() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = accentColor
                            )
                        ) {
                            Text("Go Back")
                        }
                    }
                }

                is RecipeDetailUiState.Success -> {
                    recipe?.let { recipeData ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(backgroundColor)
                        ) {
                            // Recipe Image with back button and bookmark
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp)
                            ) {
                                // Recipe Image
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(recipeData.imageUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Image of ${recipeData.name}",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                
                                // Dark overlay on the image
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    backgroundColor.copy(alpha = 0.3f),
                                                    backgroundColor.copy(alpha = 0.7f)
                                                )
                                            )
                                        )
                                )
                                
                                // Back button
                                IconButton(
                                    onClick = { navController.navigateUp() },
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(colors.surfaceVariant.copy(alpha = 0.5f))
                                        .align(Alignment.TopStart)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                                        contentDescription = "Back",
                                        tint = textPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                
                                // Bookmark button
                                IconButton(
                                    onClick = { viewModel.toggleFavorite() },
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(colors.surfaceVariant.copy(alpha = 0.5f))
                                        .align(Alignment.TopEnd)
                                ) {
                                    Icon(
                                        imageVector = if (recipeData.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                        contentDescription = if (recipeData.isFavorite) "Remove from favorites" else "Add to favorites",
                                        tint = if (recipeData.isFavorite) colors.error else textPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            // Recipe title and details
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            ) {
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Recipe name
                                Text(
                                    text = recipeData.name,
                                    style = typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = textPrimary
                                    )
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                // Rating
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Stars,
                                        contentDescription = null,
                                        tint = accentColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "4.8",
                                        style = typography.bodyMedium,
                                        color = textPrimary
                                    )
                                    Text(
                                        text = "(352)",
                                        style = typography.bodyMedium,
                                        color = textSecondary
                                    )
                                }
                                
                                // Time, calories, serves
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                ) {
                                    RecipeInfoItem(
                                        icon = Icons.Filled.Timer,
                                        value = "${recipeData.preparationTime + recipeData.cookingTime} min",
                                        label = "Time",
                                        textPrimary = textPrimary,
                                        textSecondary = textSecondary
                                    )
                                    
                                    RecipeInfoItem(
                                        icon = Icons.Filled.Fireplace,
                                        value = "${recipeData.nutritionFacts.calories} kcal",
                                        label = "Calories",
                                        textPrimary = textPrimary,
                                        textSecondary = textSecondary
                                    )
                                    
                                    RecipeInfoItem(
                                        icon = Icons.Filled.Person,
                                        value = "${recipeData.servings} serves",
                                        label = "Serves",
                                        textPrimary = textPrimary,
                                        textSecondary = textSecondary
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Tabs
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp)
                                ) {
                                    tabs.forEachIndexed { index, tab ->
                                        TabButton(
                                            text = tab,
                                            isSelected = selectedTabIndex == index,
                                            onClick = { selectedTabIndex = index },
                                            modifier = Modifier.weight(1f),
                                            selectedColor = textPrimary,
                                            unselectedColor = textSecondary,
                                            indicatorColor = accentColor
                                        )
                                    }
                                }
                            }
                            
                            // Tab content
                            when (selectedTabIndex) {
                                0 -> { // Recipe tab
                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f)
                                            .padding(horizontal = 16.dp)
                                    ) {
                                        item {
                                            Text(
                                                text = "Recipe",
                                                style = typography.titleLarge.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = textPrimary
                                                ),
                                                modifier = Modifier.padding(vertical = 16.dp)
                                            )
                                        }
                                        
                                        itemsIndexed(recipeData.instructions) { index, instruction ->
                                            RecipeStep(
                                                stepNumber = index + 1, 
                                                instruction = instruction,
                                                accentColor = accentColor,
                                                textColor = textPrimary
                                            )
                                            if (index < recipeData.instructions.size - 1) {
                                                Spacer(modifier = Modifier.height(16.dp))
                                            }
                                        }
                                        
                                        item {
                                            Spacer(modifier = Modifier.height(100.dp))
                                        }
                                    }
                                }
                                1 -> { // Ingredients tab
                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f)
                                            .padding(horizontal = 16.dp)
                                    ) {
                                        item {
                                            Text(
                                                text = "Ingredients",
                                                style = typography.titleLarge.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = textPrimary
                                                ),
                                                modifier = Modifier.padding(vertical = 16.dp)
                                            )
                                        }
                                        
                                        items(recipeData.ingredients) { ingredient ->
                                            IngredientRow(
                                                ingredient = ingredient,
                                                cardBackground = cardBackground,
                                                textPrimary = textPrimary,
                                                textSecondary = textSecondary
                                            )
                                            if (ingredient != recipeData.ingredients.last()) {
                                                HorizontalDivider(
                                                    color = colors.surfaceVariant,
                                                    modifier = Modifier.padding(vertical = 8.dp)
                                                )
                                            }
                                        }
                                        
                                        item {
                                            Spacer(modifier = Modifier.height(100.dp))
                                        }
                                    }
                                }
                            }
                            
                            // Action button
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                val isAddingToShoppingList by viewModel.isAddingToShoppingList.collectAsState()
                                
                                Button(
                                    onClick = { 
                                        if (selectedTabIndex == 0) {
                                            /* Handle Start Cooking action */ 
                                        } else {
                                            viewModel.addIngredientsToShoppingList()
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    shape = RoundedCornerShape(28.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = accentColor
                                    ),
                                    enabled = !isAddingToShoppingList
                                ) {
                                    if (selectedTabIndex == 0) {
                                        Text(
                                            text = "Start Cooking",
                                            style = typography.bodyLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = colors.onPrimary
                                            )
                                        )
                                    } else {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (isAddingToShoppingList) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(20.dp),
                                                    strokeWidth = 2.dp,
                                                    color = colors.onPrimary
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Filled.ShoppingCart,
                                                    contentDescription = null,
                                                    tint = colors.onPrimary
                                                )
                                            }
                                            Text(
                                                text = if (isAddingToShoppingList) "Adding..." else "Add to Shopping List",
                                                style = typography.bodyLarge.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = colors.onPrimary
                                                )
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

@Composable
fun RecipeInfoItem(
    icon: ImageVector,
    value: String,
    label: String,
    textPrimary: Color,
    textSecondary: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = textPrimary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color = textPrimary
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = textSecondary
        )
    }
}

@Composable
fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedColor: Color,
    unselectedColor: Color,
    indicatorColor: Color
) {
    val textColor = if (isSelected) selectedColor else unselectedColor
    val indicator = if (isSelected) indicatorColor else Color.Transparent
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            ),
            color = textColor
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .height(3.dp)
                .width(40.dp)
                .background(indicator, RoundedCornerShape(1.5.dp))
        )
    }
}

@Composable
fun RecipeStep(
    stepNumber: Int, 
    instruction: String,
    accentColor: Color,
    textColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(accentColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber.toString(),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
        
        Text(
            text = instruction,
            style = MaterialTheme.typography.bodyLarge,
            color = textColor,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun IngredientRow(
    ingredient: IngredientItem,
    cardBackground: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Ingredient icon or image could go here
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(cardBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Restaurant,
                contentDescription = null,
                tint = textPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = ingredient.name,
                style = MaterialTheme.typography.bodyLarge,
                color = textPrimary
            )
        }
        
        Text(
            text = "${ingredient.quantity} ${ingredient.unit}",
            style = MaterialTheme.typography.bodyMedium,
            color = textSecondary
        )
    }
}

@Composable
fun RecipeMetricItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = typography.titleMedium,
            color = colors.onBackground,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = typography.bodySmall,
            color = colors.onBackground.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun NutritionFactsCard(nutritionFacts: com.thenewkenya.ingrediet.data.model.NutritionFacts) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Nutrition Facts",
                style = typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Display nutrition facts with circular progress indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                NutritionItem(
                    title = "Calories",
                    value = nutritionFacts.getFormattedCalories(),
                    target = "2000",
                    progress = nutritionFacts.getCaloriesProgress(),
                    color = getProgressColor(nutritionFacts.getCaloriesProgress())
                )
                NutritionItem(
                    title = "Protein",
                    value = nutritionFacts.getFormattedProtein(),
                    target = "50g",
                    progress = nutritionFacts.getProteinProgress(),
                    color = getProgressColor(nutritionFacts.getProteinProgress())
                )
                NutritionItem(
                    title = "Carbs",
                    value = nutritionFacts.getFormattedCarbs(),
                    target = "300g",
                    progress = nutritionFacts.getCarbsProgress(),
                    color = getProgressColor(nutritionFacts.getCarbsProgress())
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Additional nutrition info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                NutritionItem(
                    title = "Fat",
                    value = nutritionFacts.getFormattedFat(),
                    target = "65g",
                    progress = nutritionFacts.getFatProgress(),
                    color = getProgressColor(nutritionFacts.getFatProgress())
                )
                nutritionFacts.getFormattedFiber()?.let { fiberValue ->
                    NutritionItem(
                        title = "Fiber",
                        value = fiberValue,
                        target = "25g",
                        progress = nutritionFacts.getFiberProgress(),
                        color = getProgressColor(nutritionFacts.getFiberProgress())
                    )
                }
                nutritionFacts.getFormattedSugar()?.let { sugarValue ->
                    NutritionItem(
                        title = "Sugar",
                        value = sugarValue,
                        target = "25g",
                        progress = nutritionFacts.getSugarProgress(),
                        color = getProgressColor(nutritionFacts.getSugarProgress())
                    )
                }
            }
        }
    }
}
