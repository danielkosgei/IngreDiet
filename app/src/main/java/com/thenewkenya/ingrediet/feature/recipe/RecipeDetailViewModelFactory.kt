package com.thenewkenya.ingrediet.feature.recipe

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class RecipeDetailViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecipeDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecipeDetailViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 