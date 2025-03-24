package com.thenewkenya.ingrediet.feature.create

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    // Collect state values
    val recipeName by viewModel.recipeName.collectAsState()
    val description by viewModel.description.collectAsState()
    val cookingTime by viewModel.cookingTime.collectAsState()
    val calories by viewModel.calories.collectAsState()
    val ingredients by viewModel.ingredients.collectAsState()
    val instructions by viewModel.instructions.collectAsState()
    val currentIngredient by viewModel.currentIngredient.collectAsState()
    val matchingRecipes by viewModel.matchingRecipes.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Recipe") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Ingredient Search Section
            item {
                Text(
                    text = "Find Recipes by Ingredients",
                    style = typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = currentIngredient,
                        onValueChange = viewModel::updateCurrentIngredient,
                        label = { Text("Add an ingredient") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { viewModel.addIngredientForSearch() },
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add ingredient")
                    }
                }
            }
            
            // List of search ingredients
            items(ingredients) { ingredient ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = ingredient,
                        style = typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { viewModel.removeIngredient(ingredient) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove ingredient")
                    }
                }
            }
            
            // Search button
            item {
                Button(
                    onClick = { viewModel.searchRecipesByIngredients() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = ingredients.isNotEmpty() && !isSearching
                ) {
                    Icon(Icons.Default.Search, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Find Recipes")
                }
            }
            
            // Loading indicator
            if (isSearching) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            
            // Matching recipes
            if (matchingRecipes.isNotEmpty()) {
                item {
                    Text(
                        text = "Found Recipes:",
                        style = typography.titleMedium,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }
                
                items(matchingRecipes) { recipe ->
                    RecipeCard(
                        recipe = recipe.toRecipe(),
                        onClick = { viewModel.useRecipeAsTemplate(recipe) }
                    )
                }
            }
            
            // Divider
            item {
                Divider(modifier = Modifier.padding(vertical = 16.dp))
            }
            
            // Manual Recipe Creation Section
            item {
                Text(
                    text = "Create Recipe Manually",
                    style = typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            // Recipe Name
            item {
                OutlinedTextField(
                    value = recipeName,
                    onValueChange = viewModel::updateRecipeName,
                    label = { Text("Recipe Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Description
            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = viewModel::updateDescription,
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }

            // Cooking Time
            item {
                OutlinedTextField(
                    value = cookingTime,
                    onValueChange = viewModel::updateCookingTime,
                    label = { Text("Cooking Time (minutes)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Calories
            item {
                OutlinedTextField(
                    value = calories,
                    onValueChange = viewModel::updateCalories,
                    label = { Text("Calories") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Ingredients Section
            item {
                Text(
                    text = "Ingredients",
                    style = typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
                
            // Ingredients List
            items(ingredients) { ingredient ->
                IngredientItem(
                    ingredient = ingredient,
                    onDelete = { viewModel.removeIngredient(ingredient) }
                )
            }

            item {
                // Add Ingredient Button
                OutlinedButton(
                    onClick = viewModel::addIngredient,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Ingredient")
                }
            }

            // Instructions Section
            item {
                Text(
                    text = "Instructions",
                    style = typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
                
            // Instructions List
            items(instructions) { instruction ->
                InstructionItem(
                    instruction = instruction,
                    onDelete = { viewModel.removeInstruction(instruction) }
                )
            }

            item {
                // Add Instruction Button
                OutlinedButton(
                    onClick = viewModel::addInstruction,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Instruction")
                }
            }

            // Save Button
            item {
                Button(
                    onClick = { viewModel.saveRecipe() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Recipe")
                }
            }
        }
    }
}

@Composable
fun IngredientItem(
    ingredient: String,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = ingredient,
            onValueChange = { /* TODO: Update ingredient */ },
            modifier = Modifier.weight(1f),
            placeholder = { Text("Enter ingredient") }
        )
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Delete ingredient")
        }
    }
}

@Composable
fun InstructionItem(
    instruction: String,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = instruction,
            onValueChange = { /* TODO: Update instruction */ },
            modifier = Modifier.weight(1f),
            placeholder = { Text("Enter instruction") },
            minLines = 2
        )
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Delete ingredient")
        }
    }
} 