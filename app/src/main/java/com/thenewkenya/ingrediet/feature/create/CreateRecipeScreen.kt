package com.thenewkenya.ingrediet.feature.create

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

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
            Icon(Icons.Default.Delete, contentDescription = "Delete instruction")
        }
    }
} 