package com.thenewkenya.ingrediet.feature.recipe

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thenewkenya.ingrediet.data.model.DetailedRecipe
import com.thenewkenya.ingrediet.data.model.IngredientItem
import com.thenewkenya.ingrediet.data.model.NutritionFacts
import com.thenewkenya.ingrediet.data.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecipeDetailViewModel(
    private val recipeRepository: RecipeRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<RecipeDetailUiState>(RecipeDetailUiState.Loading)
    val uiState: StateFlow<RecipeDetailUiState> = _uiState.asStateFlow()

    private val _recipe = MutableStateFlow<DetailedRecipe?>(null)
    val recipe: StateFlow<DetailedRecipe?> = _recipe.asStateFlow()

    fun loadRecipe(recipeId: Int) {
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


    private fun createSampleRecipe(recipeId: Int): DetailedRecipe {
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
                IngredientItem(id = 1, name = "Avocado", quantity = 1f, unit = "whole"),
                IngredientItem(id = 2, name = "Eggs", quantity = 2f, unit = "large"),
                IngredientItem(id = 3, name = "Spinach", quantity = 2f, unit = "cups"),
                IngredientItem(id = 4, name = "Cherry Tomatoes", quantity = 0.5f, unit = "cup"),
                IngredientItem(id = 5, name = "Quinoa", quantity = 0.5f, unit = "cup")
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
                recipeRepository.toggleFavorite(currentRecipe.id, !currentRecipe.isFavorite)
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
}

sealed class RecipeDetailUiState {
    data object Loading : RecipeDetailUiState()
    data object Success : RecipeDetailUiState()
    data class Error(val message: String) : RecipeDetailUiState()
}