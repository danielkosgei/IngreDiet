package com.thenewkenya.ingrediet.feature.search

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.thenewkenya.ingrediet.data.model.Recipe
import com.thenewkenya.ingrediet.data.repository.RecipeRepository
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
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Recipe>>(emptyList())
    val searchResults: StateFlow<List<Recipe>> = _searchResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        // Observe search query changes and perform search
        searchQuery
            .debounce(300) // Wait 300ms after last input before searching
            .onEach { query ->
                try {
                    _isLoading.value = true
                    _error.value = null
                    
                    if (query.isBlank()) {
                        _searchResults.value = emptyList()
                        return@onEach
                    }
                    
                    // Launch a coroutine to handle the Flow
                    viewModelScope.launch {
                        repository.searchRecipes(query)
                            .collect { result ->
                                result.fold(
                                    onSuccess = { detailedRecipes ->
                                        _searchResults.value = detailedRecipes.map { it.toRecipe() }
                                    },
                                    onFailure = { e ->
                                        _error.value = e.message
                                        _searchResults.value = emptyList()
                                    }
                                )
                            }
                    }
                } catch (e: Exception) {
                    _error.value = e.message
                    _searchResults.value = emptyList()
                } finally {
                    _isLoading.value = false
                }
            }
            .launchIn(viewModelScope)
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
}

class SearchViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SearchViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 