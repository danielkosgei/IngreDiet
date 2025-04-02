package com.thenewkenya.ingrediet.feature.search

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thenewkenya.ingrediet.IngreDietApplication
import com.thenewkenya.ingrediet.data.model.DetailedRecipe
import com.thenewkenya.ingrediet.data.network.api.IngreDietService
import com.thenewkenya.ingrediet.data.network.RecipeCacheService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class IngredientSearchViewModel(
    private val context: Context = IngreDietApplication.instance
) : ViewModel() {
    
    private val TAG = "IngredientSearchVM"
    
    // Services
    private val edgeFunctionService = IngreDietService(context)
    private val cacheService = RecipeCacheService(context)
    
    // UI state
    private val _ingredients = MutableStateFlow<List<String>>(emptyList())
    val ingredients: StateFlow<List<String>> = _ingredients.asStateFlow()
    
    private val _currentIngredient = MutableStateFlow("")
    val currentIngredient: StateFlow<String> = _currentIngredient.asStateFlow()
    
    private val _matchingRecipes = MutableStateFlow<List<DetailedRecipe>>(emptyList())
    val matchingRecipes: StateFlow<List<DetailedRecipe>> = _matchingRecipes.asStateFlow()
    
    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()
    
    /**
     * Update the current ingredient input
     */
    fun updateCurrentIngredient(ingredient: String) {
        _currentIngredient.value = ingredient
    }
    
    /**
     * Add the current ingredient to the list if it's not empty
     */
    fun addIngredient() {
        val ingredient = _currentIngredient.value.trim()
        if (ingredient.isNotEmpty() && !_ingredients.value.contains(ingredient)) {
            _ingredients.value = _ingredients.value + ingredient
            _currentIngredient.value = ""
        }
    }
    
    /**
     * Remove an ingredient from the list
     */
    fun removeIngredient(ingredient: String) {
        _ingredients.value = _ingredients.value - ingredient
    }
    
    /**
     * Search for recipes that match the current ingredient list
     */
    fun searchRecipes() {
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
} 