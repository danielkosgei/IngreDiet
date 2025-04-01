package com.thenewkenya.ingrediet.feature.recipe

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.thenewkenya.ingrediet.data.repository.RecipeRepository
import com.thenewkenya.ingrediet.data.repository.ShoppingListRepository

class RecipeDetailViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecipeDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecipeDetailViewModel(
                RecipeRepository(context),
                ShoppingListRepository(context),
                context
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 