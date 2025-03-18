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
import androidx.compose.runtime.remember
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
    return when {
        progress < 0.25f -> MaterialTheme.colorScheme.primary
        progress < 0.5f -> MaterialTheme.colorScheme.secondary
        progress < 0.75f -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.error
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(navController: NavController, recipeId: Int) {
    val context = LocalContext.current
    val recipeRepository = remember { RecipeRepository(context) }
    val viewModel = remember { RecipeDetailViewModel(recipeRepository) }

    // Load the recipe when the screen is first displayed
    LaunchedEffect(recipeId) {
        Log.d("RecipeDetailScreen", "Loading recipe with ID: $recipeId")
        viewModel.loadRecipe(recipeId)
    }

    val uiState by viewModel.uiState.collectAsState()
    val recipe by viewModel.recipe.collectAsState()

    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val errorColor = MaterialTheme.colorScheme.error
    val buttonColor = MaterialTheme.colorScheme.primary

    Scaffold(
        topBar = {
            TopAppBar(
                title = { /* Empty title to center the layout */ },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                            contentDescription = "Back",
                            tint = textColor
                        )
                    }
                },
                actions = {
                    recipe?.let {
                        IconButton(onClick = { viewModel.toggleFavorite() }) {
                            Icon(
                                imageVector = if (it.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = if (it.isFavorite) "Remove from favorites" else "Add to favorites",
                                tint = if (it.isFavorite) errorColor else textColor
                            )
                        }
                    }
                    IconButton(onClick = { /* Share recipe */ }) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "Share",
                            tint = textColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(paddingValues)
        ) {
            when (uiState) {
                is RecipeDetailUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = buttonColor)
                    }
                }

                is RecipeDetailUiState.Error -> {
                    val errorMessage = (uiState as RecipeDetailUiState.Error).message
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Error,
                                contentDescription = null,
                                tint = errorColor,
                                modifier = Modifier.size(64.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = if (errorMessage.contains("not exist")) "Recipe Not Found" else "Error Loading Recipe",
                                style = MaterialTheme.typography.headlineMedium,
                                color = textColor
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = errorMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = textColor.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Button(
                                    onClick = { navController.navigateUp() },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Text("Go Back")
                                }
                                Button(
                                    onClick = { viewModel.loadRecipe(recipeId) },
                                    colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
                                ) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                }

                is RecipeDetailUiState.Success -> {
                    recipe?.let { recipeData ->
                        RecipeDetailContent(recipe = recipeData)
                    } ?: run {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Recipe data is missing",
                                color = errorColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecipeDetailContent(recipe: DetailedRecipe) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(colors.background)
    ) {
        // Recipe Image
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            ) {
                if (recipe.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(recipe.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = recipe.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(colors.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Restaurant,
                            contentDescription = null,
                            tint = colors.onSurfaceVariant,
                            modifier = Modifier.size(80.dp)
                        )
                    }
                }

                // Gradient overlay for better text visibility
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    colors.surface.copy(alpha = 0.7f)
                                ),
                                startY = 0f,
                                endY = 500f
                            )
                        )
                )

                // Recipe title
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = recipe.name,
                        style = typography.headlineMedium,
                        color = colors.onSurface,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Additional Tags (excluding category and cuisine type)
                    val additionalTags = recipe.tags.filter { tag ->
                        tag != recipe.category && tag != recipe.cuisineType
                    }
                    if (additionalTags.isNotEmpty()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            additionalTags.take(3).forEach { tag ->
                                SuggestionChip(
                                    onClick = { /* Navigate to tag */ },
                                    label = { Text(tag, color = colors.onPrimary) },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = colors.primary.copy(alpha = 0.7f)
                                    )
                                )
                            }

                            if (additionalTags.size > 3) {
                                Text(
                                    text = "+${additionalTags.size - 3} more",
                                    color = colors.onSurfaceVariant,
                                    modifier = Modifier.align(Alignment.CenterVertically)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Recipe Info
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(recipe.description, style = typography.bodyLarge, color = colors.onSurface.copy(alpha = 0.9f))

                Spacer(modifier = Modifier.height(16.dp))

                // Recipe metrics
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    RecipeMetricItem(Icons.Filled.Timer, "Prep Time", "${recipe.preparationTime} min")
                    RecipeMetricItem(Icons.Filled.Fireplace, "Cook Time", "${recipe.cookingTime} min")
                    RecipeMetricItem(Icons.Filled.Person, "Servings", recipe.servings.toString())
                    RecipeMetricItem(Icons.Filled.Stars, "Difficulty", recipe.difficulty)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Category and cuisine type
                if (recipe.category.isNotEmpty() || recipe.cuisineType.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (recipe.category.isNotEmpty()) {
                            RecipeMetricItem(
                                icon = Icons.Filled.Restaurant,
                                label = "Category",
                                value = recipe.category
                            )
                        }
                        if (recipe.cuisineType.isNotEmpty()) {
                            RecipeMetricItem(
                                icon = Icons.Filled.Share,
                                label = "Cuisine",
                                value = recipe.cuisineType
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("Nutrition Facts", style = typography.titleLarge, color = colors.onSurface, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(8.dp))

                NutritionFactsCard(nutritionFacts = recipe.nutritionFacts)

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Ingredients Section
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text("Ingredients", style = typography.titleLarge, color = colors.onSurface, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("For ${recipe.servings} serving${if (recipe.servings > 1) "s" else ""}",
                            color = colors.onSurfaceVariant,
                            style = typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        recipe.ingredients.forEach { ingredient ->
                            IngredientListItem(ingredient)
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = colors.onSurface.copy(alpha = 0.1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { /* Add all to shopping list */ },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Icon(imageVector = Icons.Filled.ShoppingCart, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add to Shopping List")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Instructions Section
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text("Instructions", style = typography.titleLarge, color = colors.onSurface, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Instruction steps
        itemsIndexed(recipe.instructions) { index, instruction ->
            InstructionStep(stepNumber = index + 1, instruction = instruction)

            if (index < recipe.instructions.size - 1) {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Bottom padding + Start Cooking Button
        item {
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { /* Start cooking mode */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Filled.Restaurant, contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start Cooking", modifier = Modifier.padding(vertical = 8.dp), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}


@Composable
fun RecipeMetricItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    val textColor = MaterialTheme.colorScheme.onBackground
    val secondaryTextColor = textColor.copy(alpha = 0.7f)
    val iconColor = MaterialTheme.colorScheme.primary

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = textColor,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = secondaryTextColor
        )
    }
}

@Composable
fun NutritionFactsCard(nutritionFacts: com.thenewkenya.ingrediet.data.model.NutritionFacts) {
    val cardBackground = MaterialTheme.colorScheme.surfaceVariant
    val textColor = MaterialTheme.colorScheme.onSurface



    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Nutrition Facts",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = textColor
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
