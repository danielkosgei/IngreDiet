package com.thenewkenya.ingrediet.feature.favorites

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.thenewkenya.ingrediet.data.model.DetailedRecipe
import com.thenewkenya.ingrediet.data.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class FavoritesViewModel(context: Context) : ViewModel() {
    private val repository = RecipeRepository(context)
    
    private val _favorites = MutableStateFlow<List<DetailedRecipe>>(emptyList())
    val favorites: StateFlow<List<DetailedRecipe>> = _favorites.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        loadFavorites()
    }
    
    private fun loadFavorites() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                repository.getFavoriteRecipes().collect { result ->
                    result.fold(
                        onSuccess = { recipes ->
                            _favorites.value = recipes
                        },
                        onFailure = { e ->
                            _error.value = e.message
                            _favorites.value = emptyList()
                        }
                    )
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = e.message
                _favorites.value = emptyList()
                _isLoading.value = false
            }
        }
    }
    
    fun refreshFavorites() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                // Add a small delay to ensure the loading indicator is visible
                // This is better for UX in case the data loads too quickly
                delay(300)
                
                repository.getFavoriteRecipes().collect { result ->
                    result.fold(
                        onSuccess = { recipes ->
                            _favorites.value = recipes
                            _error.value = null
                        },
                        onFailure = { e ->
                            _error.value = e.message ?: "Failed to load favorites"
                            // Keep existing favorites in case of error to provide a better UX
                            if (_favorites.value.isEmpty()) {
                                _favorites.value = emptyList()
                            }
                        }
                    )
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "An unexpected error occurred"
                // Keep existing favorites in case of error
                if (_favorites.value.isEmpty()) {
                    _favorites.value = emptyList()
                }
                _isLoading.value = false
            }
        }
    }
}

class FavoritesViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavoritesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FavoritesViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 