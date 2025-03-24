package com.thenewkenya.ingrediet.feature.search

import android.util.Log
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
fun IngredientSearchScreen(
    navController: NavController,
    viewModel: IngredientSearchViewModel = remember { IngredientSearchViewModel() }
) {
    val ingredients by viewModel.ingredients.collectAsState()
    val currentIngredient by viewModel.currentIngredient.collectAsState()
    val matchingRecipes by viewModel.matchingRecipes.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Find Recipes by Ingredients") },
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
            // Current ingredient input field with Add button
            item {
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
                        onClick = { viewModel.addIngredient() },
                        modifier = Modifier
                            .padding(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add ingredient")
                    }
                }
            }
            
            // Ingredient list display
            item {
                Text(
                    text = "Your ingredients:",
                    style = typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }
            
            // List of added ingredients with delete buttons
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
                    onClick = { viewModel.searchRecipes() },
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
            
            // Results heading
            if (matchingRecipes.isNotEmpty()) {
                item {
                    Text(
                        text = "Recipes you can make:",
                        style = typography.titleMedium,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }
                
                // Recipe results
                items(matchingRecipes) { recipe ->
                    RecipeCard(
                        recipe = recipe.toRecipe(),
                        onClick = {
                            // Navigate to recipe details
                            navController.navigate("recipe/${recipe.id}")
                        }
                    )
                }
            } else if (!isSearching && ingredients.isNotEmpty()) {
                item {
                    Text(
                        text = "No recipes found for these ingredients. Try adding more ingredients.",
                        style = typography.bodyMedium,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
        }
    }
} 