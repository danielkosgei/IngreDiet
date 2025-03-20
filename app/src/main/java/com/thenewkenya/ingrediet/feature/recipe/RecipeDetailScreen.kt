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
    recipeId: Int,
    viewModel: RecipeDetailViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val recipe by viewModel.recipe.collectAsState()
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    LaunchedEffect(recipeId) {
        viewModel.loadRecipe(recipeId)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
        topBar = {
            TopAppBar(
                title = { /* Empty title to center the layout */ },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                            contentDescription = "Back",
                            tint = colors.onBackground
                        )
                    }
                },
                actions = {
                    recipe?.let {
                        IconButton(onClick = { viewModel.toggleFavorite() }) {
                            Icon(
                                imageVector = if (it.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = if (it.isFavorite) "Remove from favorites" else "Add to favorites",
                                tint = if (it.isFavorite) colors.error else colors.onBackground
                            )
                        }
                    }
                    IconButton(onClick = { /* Share recipe */ }) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "Share",
                            tint = colors.onBackground
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
                .padding(paddingValues)
        ) {
            when (uiState) {
                is RecipeDetailUiState.Loading -> {
                    CircularProgressIndicator(
                        color = colors.primary,
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
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { navController.navigateUp() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.surfaceVariant
                                )
                            ) {
                                Text("Go Back")
                            }
                            Button(
                                onClick = { viewModel.loadRecipe(recipeId) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.primary
                                )
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }

                is RecipeDetailUiState.Success -> {
                    recipe?.let { recipeData ->
                        RecipeDetailContent(recipe = recipeData)
                    } ?: run {
                        Text(
                            text = "Recipe data is missing",
                            color = colors.error,
                            modifier = Modifier.align(Alignment.Center)
                        )
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
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // Recipe Image
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(recipe.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = recipe.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    colors.background
                                )
                            )
                        )
                )
            }
        }

        // Recipe Info
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = recipe.name,
                    style = typography.headlineMedium,
                    color = colors.onBackground,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    RecipeMetricItem(
                        icon = Icons.Filled.Timer,
                        label = "Time",
                        value = recipe.cookingTime.toString()
                    )
                    RecipeMetricItem(
                        icon = Icons.Filled.Person,
                        label = "Servings",
                        value = recipe.servings.toString()
                    )
                    RecipeMetricItem(
                        icon = Icons.Filled.Fireplace,
                        label = "Calories",
                        value = recipe.nutritionFacts.getFormattedCalories()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Description
                Text(
                    text = "Description",
                    style = typography.titleLarge,
                    color = colors.onBackground,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = recipe.description,
                    style = typography.bodyLarge,
                    color = colors.onBackground.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Ingredients
                Text(
                    text = "Ingredients",
                    style = typography.titleLarge,
                    color = colors.onBackground,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                recipe.ingredients.forEach { ingredient ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = false,
                            onCheckedChange = { /* Handle checkbox state */ },
                            colors = CheckboxDefaults.colors(
                                checkedColor = colors.primary,
                                uncheckedColor = colors.onSurfaceVariant
                            )
                        )
                        Text(
                            text = "${ingredient.quantity} ${ingredient.unit} ${ingredient.name}",
                            style = typography.bodyLarge,
                            color = colors.onBackground,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

                // Add to Shopping List Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = { /* Add all to shopping list */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ShoppingCart,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add to Shopping List")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Instructions Section
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    "Instructions",
                    style = typography.titleLarge,
                    color = colors.onSurface,
                    fontWeight = FontWeight.Bold
                )

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
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Restaurant,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Start Cooking",
                    modifier = Modifier.padding(vertical = 8.dp),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
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
