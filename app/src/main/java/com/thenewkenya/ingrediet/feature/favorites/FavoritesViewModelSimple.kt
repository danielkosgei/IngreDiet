package com.thenewkenya.ingrediet.feature.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.thenewkenya.ingrediet.data.model.FavoriteRecipe
import com.thenewkenya.ingrediet.data.repository.FavoritesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * A simplified ViewModel for managing favorite recipes.
 */
class FavoritesViewModelSimple(
    private val repository: FavoritesRepository
) : ViewModel() {
    
    // State management using StateFlow
    private val _favorites = MutableStateFlow<List<FavoriteRecipe>>(emptyList())
    val favorites: StateFlow<List<FavoriteRecipe>> = _favorites.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        // Load favorites when the ViewModel is created
        refreshFavorites()
    }
    
    /**
     * Fetches favorite recipes from the repository.
     */
    fun refreshFavorites() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val result = repository.getFavoriteRecipes()
                _favorites.value = result
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load favorite recipes"
            } finally {
                _isLoading.value = false
            }
        }
    }
}

/**
 * Factory for creating FavoritesViewModelSimple instances.
 */
class FavoritesViewModelSimpleFactory(
    private val repository: FavoritesRepository
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavoritesViewModelSimple::class.java)) {
            return FavoritesViewModelSimple(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
} 