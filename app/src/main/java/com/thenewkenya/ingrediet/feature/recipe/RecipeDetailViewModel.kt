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
import kotlinx.coroutines.async

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

    // Add a state for authentication errors
    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    fun loadRecipe(recipeId: String) {
        viewModelScope.launch {
            _uiState.value = RecipeDetailUiState.Loading
            Log.d("RecipeDetailViewModel", "Started loading recipe: $recipeId")

            try {
                // Create coroutines for parallel requests
                val recipeDeferred = viewModelScope.async {
                    var result: Result<DetailedRecipe>? = null
                    recipeRepository.getRecipeDetails(recipeId).collect { 
                        result = it
                    }
                    return@async result
                }

                val favoriteStatusDeferred = viewModelScope.async {
                    var result: Result<Boolean>? = null
                    recipeRepository.isRecipeFavorite(recipeId).collect {
                        result = it
                    }
                    return@async result
                }

                // Wait for both requests to complete
                val recipeResult = recipeDeferred.await()
                val favoriteResult = favoriteStatusDeferred.await()

                if (recipeResult == null) {
                    _uiState.value = RecipeDetailUiState.Error("Failed to load recipe details")
                    return@launch
                }

                // Process recipe result
                when {
                    recipeResult.isSuccess -> {
                        val recipe = recipeResult.getOrNull()!!
                        // Apply favorite status if available
                        val isFavorite = favoriteResult?.getOrNull() ?: false
                        _recipe.value = recipe.copy(isFavorite = isFavorite)
                        _uiState.value = RecipeDetailUiState.Success
                    }
                    recipeResult.isFailure -> {
                        val error = recipeResult.exceptionOrNull()
                        Log.e("RecipeDetailViewModel", "Error loading recipe: ${error?.message}", error)
                        _uiState.value = RecipeDetailUiState.Error(error?.message ?: "Failed to load recipe details")
                    }
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
            // Clear any previous auth errors
            _authError.value = null
            
            // Optimistically update UI immediately for better UX
            val newFavoriteStatus = !currentRecipe.isFavorite
            _recipe.value = currentRecipe.copy(isFavorite = newFavoriteStatus)
            
            // Log what's happening
            val action = if (currentRecipe.isFavorite) "Removing from" else "Adding to"
            Log.d("RecipeDetailViewModel", "$action favorites: ${currentRecipe.id}")
            
            // Start background operation
            viewModelScope.launch {
                try {
                    var apiCallSuccessful = false
                    
                    recipeRepository.toggleFavorite(currentRecipe.id)
                        .collect { result ->
                            when {
                                result.isSuccess -> {
                                    val success = result.getOrNull() ?: false
                                    apiCallSuccessful = success
                                    if (success) {
                                        val actionCompleted = if (newFavoriteStatus) "added to" else "removed from"
                                        Log.d("RecipeDetailViewModel", "Recipe ${currentRecipe.id} successfully $actionCompleted favorites")
                                    } else {
                                        Log.w("RecipeDetailViewModel", "Toggle favorite returned false")
                                        // Revert UI state if API returns false
                                        _recipe.value = currentRecipe
                                    }
                                }
                                result.isFailure -> {
                                    val error = result.exceptionOrNull()
                                    Log.e("RecipeDetailViewModel", "Error toggling favorite: ${error?.message}", error)
                                    
                                    // Check if it's an authentication error
                                    if (error?.message?.contains("must be logged in", ignoreCase = true) == true ||
                                        error?.message?.contains("not authenticated", ignoreCase = true) == true) {
                                        _authError.value = "Please log in to save favorites"
                                    }
                                    
                                    // Revert UI state on error
                                    _recipe.value = currentRecipe
                                }
                            }
                        }
                } catch (e: Exception) {
                    Log.e("RecipeDetailViewModel", "Exception toggling favorite", e)
                    
                    // Check if it's an authentication error
                    if (e.message?.contains("must be logged in", ignoreCase = true) == true ||
                        e.message?.contains("not authenticated", ignoreCase = true) == true) {
                        _authError.value = "Please log in to save favorites"
                    }
                    
                    // Revert UI state on exception
                    _recipe.value = currentRecipe
                }
            }
        }
    }

    // Add a method to clear the auth error
    fun clearAuthError() {
        _authError.value = null
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