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
import com.thenewkenya.ingrediet.R
import androidx.compose.ui.res.painterResource
import com.thenewkenya.ingrediet.ui.theme.Error
import com.thenewkenya.ingrediet.ui.theme.Primary
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.zIndex

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
    
    // Dark colors from the image
    val backgroundColor = Color(0xFF111111)
    val cardBackgroundColor = Color(0xFF1D1D1D)
    val accentColor = Primary // Using your app's primary color for accents
    val textColor = Color.White
    val textSecondaryColor = Color.White.copy(alpha = 0.7f)

    // State for bottom sheets
    var recipeSheetState by remember { mutableStateOf(BottomSheetState.Collapsed) }
    var ingredientsSheetState by remember { mutableStateOf(BottomSheetState.Collapsed) }

    LaunchedEffect(recipeId) {
        viewModel.loadRecipe(recipeId)
    }

    Scaffold(
        containerColor = backgroundColor,
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Error,
                            contentDescription = null,
                            tint = Error,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = (uiState as RecipeDetailUiState.Error).message,
                            color = textColor,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
                is RecipeDetailUiState.Success -> {
                    recipe?.let { recipeData ->
                        // Main content
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(backgroundColor)
                        ) {
                            // Top Navigation Bar
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Back button
                                IconButton(
                                    onClick = { navController.navigateUp() }
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                                        contentDescription = "Back",
                                        tint = textColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.weight(1f))
                                
                                // Save button
                                IconButton(
                                    onClick = { /* Add to favorites */ }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FavoriteBorder,
                                        contentDescription = "Save recipe",
                                        tint = textColor
                                    )
                                }
                            }
                            
                            // Recipe Title
                            Text(
                                text = "Mexican potatoes",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = textColor,
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 4.dp)
                            )
                            
                            // Rating
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = Error, // Using error color as red heart
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "4.63",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = textColor
                                )
                                Text(
                                    text = " (271)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = textSecondaryColor
                                )
                            }
                            
                            // Food Image (with transparent background)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                // Food image
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(recipeData.imageUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Recipe image",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .size(240.dp)
                                )
                            }
                            
                            // Spacer to push sheets to the bottom
                            Spacer(modifier = Modifier.weight(1f))
                        }
                        
                        // Recipe Bottom Sheet - Taller
                        BottomSheet(
                            state = recipeSheetState,
                            onStateChange = { recipeSheetState = it },
                            backgroundColor = cardBackgroundColor,
                            contentColor = textColor,
                            peekHeight = 160.dp,
                            initialHeightFraction = 0.4f,
                            zIndex = 1f,
                            sheetTitle = "Recipe Steps",
                            sheetIcon = Icons.Default.Stars,
                            indicatorColor = accentColor
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                // Handle and header are now in the BottomSheet composable
                                
                                // Recipe or Ingredients Label
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Recipe",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = textColor
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .width(24.dp)
                                                .height(2.dp)
                                                .background(color = accentColor)
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.weight(1f))
                                    
                                    // Dot indicators
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable {
                                            // Switch to ingredients
                                            ingredientsSheetState = BottomSheetState.Expanded
                                        }
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(accentColor)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(textSecondaryColor.copy(alpha = 0.3f))
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Recipe Steps
                                LazyColumn {
                                    // Step 1
                                    item {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(bottom = 16.dp)
                                        ) {
                                            // Step number circle
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(CircleShape)
                                                    .background(accentColor),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "1",
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = textColor
                                                )
                                            }
                                            
                                            Spacer(modifier = Modifier.width(12.dp))
                                            
                                            // Step description
                                            Text(
                                                text = recipeData.instructions.firstOrNull() ?: "",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }
                                    
                                    // Display additional steps if available
                                    if (recipeData.instructions.size > 1) {
                                        itemsIndexed(recipeData.instructions.drop(1)) { index, instruction ->
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(bottom = 16.dp)
                                            ) {
                                                // Step number circle
                                                Box(
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .clip(CircleShape)
                                                        .background(accentColor),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = "${index + 2}",
                                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                        color = textColor
                                                    )
                                                }
                                                
                                                Spacer(modifier = Modifier.width(12.dp))
                                                
                                                // Step description
                                                Text(
                                                    text = instruction,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        // Ingredients Bottom Sheet - On top
                        BottomSheet(
                            state = ingredientsSheetState,
                            onStateChange = { ingredientsSheetState = it },
                            backgroundColor = Color(0xFF2A3439), // Slightly different color for ingredients
                            contentColor = textColor,
                            peekHeight = 120.dp,
                            initialHeightFraction = 0.3f,
                            zIndex = 2f,
                            sheetTitle = "Ingredients",
                            sheetIcon = Icons.Default.Restaurant,
                            indicatorColor = Primary // Use Primary color for ingredients
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                // Handle and header are now in the BottomSheet composable
                                
                                // Recipe or Ingredients Label
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Ingredients",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = textColor
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .width(24.dp)
                                                .height(2.dp)
                                                .background(color = Primary)
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.weight(1f))
                                    
                                    // Dot indicators
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable {
                                            // Switch to recipe
                                            recipeSheetState = BottomSheetState.Expanded
                                        }
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(textSecondaryColor.copy(alpha = 0.3f))
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(Primary)
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Ingredients List
                                LazyColumn {
                                    items(recipeData.ingredients) { ingredient ->
                                        ModernIngredientRow(
                                            ingredient = ingredient,
                                            textPrimary = textColor,
                                            textSecondary = textSecondaryColor,
                                            backgroundColor = backgroundColor,
                                            accentColor = Primary
                                        )
                                        
                                        if (ingredient != recipeData.ingredients.last()) {
                                            HorizontalDivider(
                                                color = textSecondaryColor.copy(alpha = 0.2f),
                                                modifier = Modifier.padding(vertical = 8.dp)
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

// Bottom sheet state
enum class BottomSheetState {
    Collapsed,
    Expanded
}

@Composable
fun BottomSheet(
    state: BottomSheetState,
    onStateChange: (BottomSheetState) -> Unit,
    backgroundColor: Color,
    contentColor: Color,
    peekHeight: Dp,
    zIndex: Float = 1f,
    initialHeightFraction: Float = 0.25f,
    sheetTitle: String,
    sheetIcon: ImageVector,
    indicatorColor: Color,
    content: @Composable () -> Unit
) {
    var sheetHeightFraction by remember { mutableStateOf(
        if (state == BottomSheetState.Expanded) 0.8f else initialHeightFraction
    )}
    
    // Minimum height to ensure sheet always remains visible
    val minHeightFraction = 0.08f
    
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Semi-transparent background for expanded sheet
        if (state == BottomSheetState.Expanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .zIndex(zIndex - 0.1f)
                    .clickable { onStateChange(BottomSheetState.Collapsed) }
            )
        }
        
        // Bottom sheet
        Card(
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor,
                contentColor = contentColor
            ),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp,
                pressedElevation = 8.dp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(sheetHeightFraction)
                .zIndex(zIndex)
                .draggable(
                    orientation = Orientation.Vertical,
                    state = rememberDraggableState { delta ->
                        // Continuous adjustment of the sheet height
                        val newHeight = sheetHeightFraction - (delta / 1000f)
                        // Ensure sheet always remains visible with minimum height
                        sheetHeightFraction = newHeight.coerceIn(minHeightFraction, 0.9f)

                        // Update state based on height threshold
                        val currentState = if (sheetHeightFraction > 0.5f) {
                            BottomSheetState.Expanded
                        } else {
                            BottomSheetState.Collapsed
                        }

                        // Only notify state changes when crossing threshold
                        if (currentState != state) {
                            onStateChange(currentState)
                        }
                    },
                    onDragStopped = {
                        // No snapping effect - keep the sheet at its current position
                        // Just update the state accordingly
                        val finalState = if (sheetHeightFraction > 0.5f) {
                            BottomSheetState.Expanded
                        } else {
                            BottomSheetState.Collapsed
                        }
                        onStateChange(finalState)
                    }
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                // Sheet handle and title
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(backgroundColor.copy(alpha = 0.9f))
                        .padding(top = 12.dp, bottom = 8.dp)
                ) {
                    // Pill-shaped handle
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .background(indicatorColor.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Sheet title with icon
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Icon(
                            imageVector = sheetIcon,
                            contentDescription = null,
                            tint = indicatorColor,
                            modifier = Modifier.size(20.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = sheetTitle,
                            style = MaterialTheme.typography.titleSmall,
                            color = indicatorColor
                        )
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        // Drag indicator
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(indicatorColor.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Drag",
                                style = MaterialTheme.typography.bodySmall,
                                color = indicatorColor
                            )
                        }
                    }
                }
                
                // Divider
                HorizontalDivider(
                    color = indicatorColor.copy(alpha = 0.2f),
                    thickness = 1.dp
                )
                
                // Content
                content()
            }
        }
    }
}

@Composable
fun IngredientIconCircle(
    backgroundColor: Color,
    tint: Color,
    icon: ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor
            ),
            shape = CircleShape,
            modifier = Modifier.size(40.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun ModernIngredientRow(
    ingredient: IngredientItem,
    textPrimary: Color,
    textSecondary: Color,
    backgroundColor: Color,
    accentColor: Color = textPrimary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Ingredient icon circle
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Restaurant,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Ingredient name and amount
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = ingredient.name,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = textPrimary
            )
            
            Text(
                text = "${ingredient.quantity} ${ingredient.unit}",
                style = MaterialTheme.typography.bodySmall,
                color = textSecondary
            )
        }
        
        // Optional amount pill
        if (ingredient.quantity.isNaN()) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = accentColor.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(50),
            ) {
                Text(
                    text = ingredient.unit,
                    style = MaterialTheme.typography.bodySmall,
                    color = accentColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

// Use drawable resources for ingredient icons
@Composable
fun IngredientIcon(
    ingredientName: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    // Will be replaced with actual ingredient images in the future
    // For now, use a simple icon representation
    val icon = when {
        ingredientName.contains("potato", ignoreCase = true) -> Icons.Default.Restaurant
        ingredientName.contains("tomato", ignoreCase = true) -> Icons.Default.Restaurant
        ingredientName.contains("herb", ignoreCase = true) || 
        ingredientName.contains("cilantro", ignoreCase = true) ||
        ingredientName.contains("parsley", ignoreCase = true) -> Icons.Default.Restaurant
        ingredientName.contains("meat", ignoreCase = true) ||
        ingredientName.contains("beef", ignoreCase = true) ||
        ingredientName.contains("chicken", ignoreCase = true) -> Icons.Default.Restaurant
        else -> Icons.Default.Restaurant
    }
    
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = tint,
        modifier = modifier
    )
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
