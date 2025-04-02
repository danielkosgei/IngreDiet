package com.thenewkenya.ingrediet.feature.kenyan

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thenewkenya.ingrediet.IngreDietApplication
import com.thenewkenya.ingrediet.data.model.KenyanRecipe
import com.thenewkenya.ingrediet.data.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * ViewModel for Kenyan recipes
 */
class KenyanRecipesViewModel(
    private val context: Context = IngreDietApplication.instance
) : ViewModel() {
    private val TAG = "KenyanRecipesVM"
    private val recipeRepository = RecipeRepository(context)
    
    // UI state
    private val _kenyanRecipes = MutableStateFlow<List<KenyanRecipe>>(emptyList())
    val kenyanRecipes: StateFlow<List<KenyanRecipe>> = _kenyanRecipes.asStateFlow()
    
    private val _selectedRegion = MutableStateFlow<String?>(null)
    val selectedRegion: StateFlow<String?> = _selectedRegion.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _selectedRecipe = MutableStateFlow<KenyanRecipe?>(null)
    val selectedRecipe: StateFlow<KenyanRecipe?> = _selectedRecipe.asStateFlow()
    
    // List of Kenyan regions
    val regions = listOf(
        "All Regions",
        "Central",
        "Coastal",
        "Eastern",
        "Nairobi",
        "Northeastern",
        "Nyanza",
        "Rift Valley",
        "Western"
    )
    
    /**
     * Load Kenyan recipes when the ViewModel is initialized
     */
    init {
        loadKenyanRecipes()
    }
    
    /**
     * Load Kenyan recipes
     */
    fun loadKenyanRecipes() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            recipeRepository.getKenyanRecipes()
                .catch { e ->
                    Log.e(TAG, "Error loading Kenyan recipes: ${e.message}", e)
                    _errorMessage.value = "Failed to load Kenyan recipes: ${e.message}"
                    _isLoading.value = false
                }
                .collect { result ->
                    result.fold(
                        onSuccess = { recipes ->
                            _kenyanRecipes.value = recipes
                            _isLoading.value = false
                        },
                        onFailure = { e ->
                            Log.e(TAG, "Error loading Kenyan recipes: ${e.message}", e)
                            _errorMessage.value = "Failed to load Kenyan recipes: ${e.message}"
                            _isLoading.value = false
                        }
                    )
                }
        }
    }
    
    /**
     * Set selected region and filter recipes
     */
    fun selectRegion(region: String) {
        viewModelScope.launch {
            if (region == "All Regions") {
                _selectedRegion.value = null
                loadKenyanRecipes()
                return@launch
            }
            
            _selectedRegion.value = region
            _isLoading.value = true
            _errorMessage.value = null
            
            recipeRepository.getKenyanRecipesByRegion(region)
                .catch { e ->
                    Log.e(TAG, "Error loading Kenyan recipes by region: ${e.message}", e)
                    _errorMessage.value = "Failed to load Kenyan recipes: ${e.message}"
                    _isLoading.value = false
                }
                .collect { result ->
                    result.fold(
                        onSuccess = { recipes ->
                            _kenyanRecipes.value = recipes
                            _isLoading.value = false
                        },
                        onFailure = { e ->
                            Log.e(TAG, "Error loading Kenyan recipes by region: ${e.message}", e)
                            _errorMessage.value = "Failed to load Kenyan recipes: ${e.message}"
                            _isLoading.value = false
                        }
                    )
                }
        }
    }
    
    /**
     * Search Kenyan recipes
     */
    fun searchRecipes(query: String) {
        _searchQuery.value = query
        
        if (query.isBlank()) {
            // If query is blank, reset to default view based on selected region
            if (_selectedRegion.value != null) {
                selectRegion(_selectedRegion.value!!)
            } else {
                loadKenyanRecipes()
            }
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            recipeRepository.searchKenyanRecipes(query)
                .catch { e ->
                    Log.e(TAG, "Error searching Kenyan recipes: ${e.message}", e)
                    _errorMessage.value = "Failed to search Kenyan recipes: ${e.message}"
                    _isLoading.value = false
                }
                .collect { result ->
                    result.fold(
                        onSuccess = { recipes ->
                            _kenyanRecipes.value = recipes
                            _isLoading.value = false
                        },
                        onFailure = { e ->
                            Log.e(TAG, "Error searching Kenyan recipes: ${e.message}", e)
                            _errorMessage.value = "Failed to search Kenyan recipes: ${e.message}"
                            _isLoading.value = false
                        }
                    )
                }
        }
    }
    
    /**
     * Get recipe details by ID
     */
    fun getRecipeById(recipeId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            recipeRepository.getKenyanRecipeById(recipeId)
                .catch { e ->
                    Log.e(TAG, "Error loading Kenyan recipe: ${e.message}", e)
                    _errorMessage.value = "Failed to load recipe details: ${e.message}"
                    _isLoading.value = false
                }
                .collect { result ->
                    result.fold(
                        onSuccess = { recipe ->
                            _selectedRecipe.value = recipe
                            _isLoading.value = false
                        },
                        onFailure = { e ->
                            Log.e(TAG, "Error loading Kenyan recipe: ${e.message}", e)
                            _errorMessage.value = "Failed to load recipe details: ${e.message}"
                            _isLoading.value = false
                        }
                    )
                }
        }
    }
    
    /**
     * Clear the selected recipe
     */
    fun clearSelectedRecipe() {
        _selectedRecipe.value = null
    }
    
    /**
     * Clear error message
     */
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}