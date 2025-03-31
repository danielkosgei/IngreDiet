package com.thenewkenya.ingrediet.feature.create

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thenewkenya.ingrediet.SupabaseApplication
import com.thenewkenya.ingrediet.data.model.DetailedRecipe
import com.thenewkenya.ingrediet.data.network.api.IngreDietService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CreateRecipeViewModel(
    private val context: Context = SupabaseApplication.instance
) : ViewModel() {
    private val TAG = "CreateRecipeVM"
    private val edgeFunctionService = IngreDietService(context)
    
    private val _recipeName = MutableStateFlow("")
    val recipeName: StateFlow<String> = _recipeName.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _cookingTime = MutableStateFlow("")
    val cookingTime: StateFlow<String> = _cookingTime.asStateFlow()

    private val _calories = MutableStateFlow("")
    val calories: StateFlow<String> = _calories.asStateFlow()

    private val _ingredients = MutableStateFlow<List<String>>(emptyList())
    val ingredients: StateFlow<List<String>> = _ingredients.asStateFlow()

    private val _instructions = MutableStateFlow<List<String>>(emptyList())
    val instructions: StateFlow<List<String>> = _instructions.asStateFlow()
    
    // New state for ingredient search
    private val _currentIngredient = MutableStateFlow("")
    val currentIngredient: StateFlow<String> = _currentIngredient.asStateFlow()
    
    private val _matchingRecipes = MutableStateFlow<List<DetailedRecipe>>(emptyList())
    val matchingRecipes: StateFlow<List<DetailedRecipe>> = _matchingRecipes.asStateFlow()
    
    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    fun updateRecipeName(name: String) {
        _recipeName.value = name
    }

    fun updateDescription(desc: String) {
        _description.value = desc
    }

    fun updateCookingTime(time: String) {
        _cookingTime.value = time
    }

    fun updateCalories(cal: String) {
        _calories.value = cal
    }

    fun addIngredient() {
        _ingredients.value = _ingredients.value + ""
    }

    fun removeIngredient(ingredient: String) {
        _ingredients.value = _ingredients.value - ingredient
    }

    fun addInstruction() {
        _instructions.value = _instructions.value + ""
    }

    fun removeInstruction(instruction: String) {
        _instructions.value = _instructions.value - instruction
    }
    
    // New functions for ingredient search
    fun updateCurrentIngredient(ingredient: String) {
        _currentIngredient.value = ingredient
    }
    
    fun addIngredientForSearch() {
        val ingredient = _currentIngredient.value.trim()
        if (ingredient.isNotEmpty() && !_ingredients.value.contains(ingredient)) {
            _ingredients.value = _ingredients.value + ingredient
            _currentIngredient.value = ""
        }
    }
    
    fun searchRecipesByIngredients() {
        if (_ingredients.value.isEmpty()) {
            return
        }
        
        viewModelScope.launch {
            try {
                _isSearching.value = true
                
                Log.d(TAG, "Searching for recipes with ingredients: ${_ingredients.value}")
                
                edgeFunctionService.getRecipesByIngredients(_ingredients.value).collect { recipes ->
                    _matchingRecipes.value = recipes
                    _isSearching.value = false
                    
                    Log.d(TAG, "Found ${recipes.size} recipes matching the ingredient list")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error searching for recipes by ingredients", e)
                _isSearching.value = false
            }
        }
    }
    
    fun useRecipeAsTemplate(recipe: DetailedRecipe) {
        _recipeName.value = recipe.name
        _description.value = recipe.description
        _cookingTime.value = recipe.cookingTime.toString()
        _calories.value = recipe.nutritionFacts.calories.toString()
        _ingredients.value = recipe.ingredients.map { "${it.quantity} ${it.unit} ${it.name}" }
        _instructions.value = recipe.instructions
    }

    fun saveRecipe() {
        viewModelScope.launch {
            // TODO: Implement recipe saving logic
            // This should:
            // 1. Validate all fields
            // 2. Create a Recipe object
            // 3. Save to the repository
            // 4. Navigate back to the previous screen
        }
    }
} 