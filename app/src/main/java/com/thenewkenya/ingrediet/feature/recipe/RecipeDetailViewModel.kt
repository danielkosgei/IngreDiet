package com.thenewkenya.ingrediet.feature.recipe

import android.adservices.adid.AdId
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thenewkenya.ingrediet.data.model.DetailedRecipe
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
            recipeRepository.getRecipeDetails(recipeId).collect { result ->
                result.fold(
                    onSuccess = { recipe ->
                        _recipe.value = recipe
                        _uiState.value = RecipeDetailUiState.Success
                    },
                    onFailure = { error ->
                        _uiState.value = RecipeDetailUiState.Error(error.message ?: "Unknown error")
                    }
                )
            }
        }
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