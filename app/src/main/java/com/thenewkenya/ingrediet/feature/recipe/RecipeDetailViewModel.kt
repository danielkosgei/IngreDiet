package com.thenewkenya.ingrediet.feature.recipe

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thenewkenya.ingrediet.data.model.DetailedRecipe
import com.thenewkenya.ingrediet.data.model.IngredientItem
import com.thenewkenya.ingrediet.data.model.NutritionFacts
import com.thenewkenya.ingrediet.data.repository.RecipeRepository
import com.thenewkenya.ingrediet.data.repository.ShoppingListRepository
import com.thenewkenya.ingrediet.feature.shopping.ShoppingItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID
import androidx.lifecycle.ViewModelProvider

class RecipeDetailViewModel(
    private val recipeRepository: RecipeRepository,
    private val shoppingListRepository: ShoppingListRepository,
    private val context: Context
) : ViewModel() {
    private val _uiState = MutableStateFlow<RecipeDetailUiState>(RecipeDetailUiState.Loading)
    val uiState: StateFlow<RecipeDetailUiState> = _uiState.asStateFlow()

    private val _recipe = MutableStateFlow<DetailedRecipe?>(null)
    val recipe: StateFlow<DetailedRecipe?> = _recipe.asStateFlow()

    private val _isAddingToShoppingList = MutableStateFlow(false)
    val isAddingToShoppingList: StateFlow<Boolean> = _isAddingToShoppingList.asStateFlow()

    private val _addToShoppingListResult = MutableStateFlow<AddToShoppingListResult?>(null)
    val addToShoppingListResult: StateFlow<AddToShoppingListResult?> = _addToShoppingListResult.asStateFlow()

    fun loadRecipe(recipeId: String) {
        viewModelScope.launch {
            _uiState.value = RecipeDetailUiState.Loading
            Log.d("RecipeDetailViewModel", "Started loading recipe: $recipeId")

            try {
                // Check data integrity first
                val dataValidation = recipeRepository.validateRecipeData(recipeId)
                Log.d("RecipeDetailViewModel", "Recipe data validation: $dataValidation")

                // Even if some parts of the recipe data are missing, try to load what we can
                if (!dataValidation.getOrDefault("recipe_exists", false)) {
                    Log.w("RecipeDetailViewModel", "Recipe $recipeId doesn't seem to exist, but we'll try to load anyway")
                }

                // Log warnings about missing data but continue
                if (!dataValidation.getOrDefault("ingredients_exist", false)) {
                    Log.w("RecipeDetailViewModel", "Recipe $recipeId has no ingredients in the database")
                }

                if (!dataValidation.getOrDefault("nutrition_exists", false)) {
                    Log.w("RecipeDetailViewModel", "Recipe $recipeId has no nutrition data in the database")
                }

                if (!dataValidation.getOrDefault("instructions_exist", false)) {
                    Log.w("RecipeDetailViewModel", "Recipe $recipeId has no instructions in the database")
                }

                // Continue with normal loading
                recipeRepository.getRecipeDetails(recipeId).collect { result ->
                    result.fold(
                        onSuccess = { recipe ->
                            _recipe.value = recipe
                            _uiState.value = RecipeDetailUiState.Success
                        },
                        onFailure = { error ->
                            Log.e("RecipeDetailViewModel", "Error loading recipe: ${error.message}", error)
                            _uiState.value = RecipeDetailUiState.Error(error.message ?: "Failed to load recipe details")
                        }
                    )
                }
            } catch (e: Exception) {
                Log.e("RecipeDetailViewModel", "Exception in loadRecipe", e)
                _uiState.value = RecipeDetailUiState.Error("An unexpected error occurred: ${e.message ?: "Unknown error"}")
            }
        }
    }


    private fun createSampleRecipe(recipeId: String): DetailedRecipe {
        return DetailedRecipe(
            id = recipeId,
            name = "Sample Vegetarian Breakfast Bowl",
            description = "A nutritious bowl packed with protein and fiber to start your day right. This is sample data for development.",
            imageUrl = "https://images.unsplash.com/photo-1494859802809-d069c3b71a8a?q=80&w=1470&fmt=auto",
            preparationTime = 15,
            cookingTime = 30,
            servings = 2,
            difficulty = "Medium",
            ingredients = listOf(
                IngredientItem(id = "1", name = "Avocado", quantity = 1f, unit = "whole"),
                IngredientItem(id = "2", name = "Eggs", quantity = 2f, unit = "large"),
                IngredientItem(id = "3", name = "Spinach", quantity = 2f, unit = "cups"),
                IngredientItem(id = "4", name = "Cherry Tomatoes", quantity = 0.5f, unit = "cup"),
                IngredientItem(id = "5", name = "Quinoa", quantity = 0.5f, unit = "cup")
            ),
            instructions = listOf(
                "Cook quinoa according to package instructions and set aside.",
                "In a pan, sauté spinach with a little olive oil until wilted.",
                "Fry or poach eggs to your preference.",
                "Slice the avocado and halve the cherry tomatoes.",
                "Assemble the bowl: quinoa at the bottom, topped with spinach, eggs, avocado, and tomatoes.",
                "Season with salt, pepper, and a drizzle of olive oil if desired."
            ),
            nutritionFacts = NutritionFacts(
                calories = 450,
                protein = 22.5f,
                carbs = 40.2f,
                fat = 23.8f,
                fiber = 12.3f,
                sugar = 3.8f
            ),
            tags = listOf("vegetarian", "breakfast", "healthy"),
            isFavorite = false
        )
    }

    fun toggleFavorite() {
        _recipe.value?.let { currentRecipe ->
            viewModelScope.launch {
                recipeRepository.toggleFavorite(currentRecipe.id)
                    .collect { result ->
                        result.fold(
                            onSuccess = { success ->
                                if (success) {
                                    _recipe.value = currentRecipe.copy(isFavorite = !currentRecipe.isFavorite)
                                }
                            },
                            onFailure = { /* Handle error */ }
                        )
                    }
            }
        }
    }

    fun addIngredientsToShoppingList() {
        val currentRecipe = _recipe.value ?: return
        
        viewModelScope.launch {
            _isAddingToShoppingList.value = true
            
            try {
                // Map ingredients to shopping items
                val shoppingItems = currentRecipe.ingredients.map { ingredient ->
                    val formattedQuantity = formatQuantity(ingredient.quantity, ingredient.unit)
                    val category = mapIngredientToCategory(ingredient.name)
                    
                    ShoppingItem(
                        id = UUID.randomUUID().toString(),
                        name = "${ingredient.name} ($formattedQuantity)",
                        category = category,
                        isChecked = false
                    )
                }
                
                var successCount = 0
                
                // Add each ingredient to shopping list
                shoppingItems.forEach { item ->
                    shoppingListRepository.addShoppingItem(item)
                        .collectLatest { result ->
                            result.fold(
                                onSuccess = { successCount++ },
                                onFailure = { error ->
                                    Log.e("RecipeDetailViewModel", "Failed to add item ${item.name}: ${error.message}")
                                }
                            )
                        }
                }
                
                // Set result based on success count
                _addToShoppingListResult.value = if (successCount == shoppingItems.size) {
                    AddToShoppingListResult.Success(shoppingItems.size)
                } else if (successCount > 0) {
                    AddToShoppingListResult.PartialSuccess(successCount, shoppingItems.size)
                } else {
                    AddToShoppingListResult.Error("Failed to add ingredients to shopping list")
                }
                
            } catch (e: Exception) {
                Log.e("RecipeDetailViewModel", "Error adding to shopping list", e)
                _addToShoppingListResult.value = AddToShoppingListResult.Error(e.message ?: "Unknown error occurred")
            } finally {
                _isAddingToShoppingList.value = false
            }
        }
    }
    
    // Reset the add to shopping list result (e.g., after showing a message to the user)
    fun resetAddToShoppingListResult() {
        _addToShoppingListResult.value = null
    }
    
    /**
     * Format the quantity and unit for display
     */
    private fun formatQuantity(quantity: Float, unit: String): String {
        // Format whole numbers without decimal point
        val formattedQuantity = if (quantity == quantity.toInt().toFloat()) {
            quantity.toInt().toString()
        } else {
            quantity.toString()
        }
        
        return "$formattedQuantity $unit"
    }
    
    /**
     * Map ingredient name to an appropriate category
     */
    private fun mapIngredientToCategory(name: String): String {
        val lowerName = name.lowercase()
        
        return when {
            // Dairy
            lowerName.contains("milk") || lowerName.contains("cheese") || 
            lowerName.contains("yogurt") || lowerName.contains("cream") || 
            lowerName.contains("butter") -> "Dairy"
            
            // Produce
            lowerName.contains("apple") || lowerName.contains("banana") || 
            lowerName.contains("orange") || lowerName.contains("pepper") || 
            lowerName.contains("tomato") || lowerName.contains("potato") || 
            lowerName.contains("onion") || lowerName.contains("garlic") || 
            lowerName.contains("lettuce") || lowerName.contains("spinach") || 
            lowerName.contains("broccoli") || lowerName.contains("carrot") ||
            lowerName.contains("berry") || lowerName.contains("fruit") ||
            lowerName.contains("vegetable") -> "Produce"
            
            // Meat
            lowerName.contains("beef") || lowerName.contains("chicken") || 
            lowerName.contains("pork") || lowerName.contains("lamb") || 
            lowerName.contains("sausage") || lowerName.contains("meat") ||
            lowerName.contains("bacon") || lowerName.contains("steak") -> "Meat"
            
            // Bakery
            lowerName.contains("bread") || lowerName.contains("bun") || 
            lowerName.contains("roll") || lowerName.contains("cake") || 
            lowerName.contains("cookie") || lowerName.contains("flour") -> "Bakery"
            
            // Spices and condiments
            lowerName.contains("salt") || lowerName.contains("pepper") || 
            lowerName.contains("spice") || lowerName.contains("sauce") || 
            lowerName.contains("oil") || lowerName.contains("vinegar") || 
            lowerName.contains("herb") -> "Spices"
            
            // Grains
            lowerName.contains("rice") || lowerName.contains("pasta") || 
            lowerName.contains("cereal") || lowerName.contains("grain") ||
            lowerName.contains("quinoa") || lowerName.contains("oat") -> "Grains"
            
            // Default category
            else -> "General"
        }
    }

    fun shareRecipe() {
        _recipe.value?.let { recipe ->
            val shareText = buildString {
                appendLine("Check out this recipe from IngreDiet!")
                appendLine()
                appendLine(recipe.name)
                appendLine()
                appendLine(recipe.description)
                appendLine()
                appendLine("Preparation Time: ${recipe.preparationTime} minutes")
                appendLine("Cooking Time: ${recipe.cookingTime} minutes")
                appendLine("Servings: ${recipe.servings}")
                appendLine("Difficulty: ${recipe.difficulty}")
                appendLine()
                appendLine("Ingredients:")
                recipe.ingredients.forEach { ingredient ->
                    appendLine("- ${formatQuantity(ingredient.quantity, ingredient.unit)} ${ingredient.name}")
                }
                appendLine()
                appendLine("Instructions:")
                recipe.instructions.forEachIndexed { index, instruction ->
                    appendLine("${index + 1}. $instruction")
                }
                appendLine()
                appendLine("Nutrition Facts:")
                appendLine("Calories: ${recipe.nutritionFacts.calories}")
                appendLine("Protein: ${recipe.nutritionFacts.protein}g")
                appendLine("Carbs: ${recipe.nutritionFacts.carbs}g")
                appendLine("Fat: ${recipe.nutritionFacts.fat}g")
            }
            
            val intent = android.content.Intent().apply {
                action = android.content.Intent.ACTION_SEND
                putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                type = "text/plain"
            }
            
            val shareIntent = android.content.Intent.createChooser(intent, null)
            context.startActivity(shareIntent)
        }
    }
}

sealed class RecipeDetailUiState {
    data object Loading : RecipeDetailUiState()
    data object Success : RecipeDetailUiState()
    data class Error(val message: String) : RecipeDetailUiState()
}

sealed class AddToShoppingListResult {
    data class Success(val count: Int) : AddToShoppingListResult()
    data class PartialSuccess(val successCount: Int, val totalCount: Int) : AddToShoppingListResult()
    data class Error(val message: String) : AddToShoppingListResult()
}