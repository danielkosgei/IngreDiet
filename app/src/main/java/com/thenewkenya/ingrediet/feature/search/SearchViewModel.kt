package com.thenewkenya.ingrediet.feature.search

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.thenewkenya.ingrediet.data.model.DetailedRecipe
import com.thenewkenya.ingrediet.data.model.Recipe
import com.thenewkenya.ingrediet.data.repository.RecipeRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class SearchViewModel(
    context: Context
) : ViewModel() {
    private val repository = RecipeRepository(context)
    private val TAG = "SearchViewModel"
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<DetailedRecipe>>(emptyList())
    val searchResults: StateFlow<List<DetailedRecipe>> = _searchResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        setupSearchDebounce()
    }
    
    @OptIn(FlowPreview::class)
    private fun setupSearchDebounce() {
        // Search after user stops typing for 500ms
        _searchQuery
            .debounce(500)
            .onEach { query ->
                if (query.length >= 2 || query.isEmpty()) {
                    searchRecipes(query)
                }
            }
            .launchIn(viewModelScope)
    }
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun searchRecipes(query: String) {
        _isLoading.value = true
        _error.value = null
        
        viewModelScope.launch {
            try {
                Log.d(TAG, "Searching for recipes with query: '$query'")
                
                repository.searchRecipesFromDatabase(query).collect { result ->
                    result.fold(
                        onSuccess = { recipes ->
                            _searchResults.value = recipes
                            _isLoading.value = false
                            Log.d(TAG, "Found ${recipes.size} recipes in database")
                        },
                        onFailure = { error ->
                            _error.value = "Failed to search recipes: ${error.message}"
                            _isLoading.value = false
                            Log.e(TAG, "Error searching recipes", error)
                        }
                    )
                }
            } catch (e: Exception) {
                _error.value = "Failed to search recipes: ${e.message}"
                _isLoading.value = false
                Log.e(TAG, "Error searching recipes", e)
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
    
    /**
     * Factory for creating SearchViewModel instances with the correct context
     */
    class SearchViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
                return SearchViewModel(context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
} 