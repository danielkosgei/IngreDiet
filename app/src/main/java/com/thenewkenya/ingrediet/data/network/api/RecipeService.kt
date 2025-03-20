package com.thenewkenya.ingrediet.data.network.api

import android.content.Context
import android.util.Log
import com.thenewkenya.ingrediet.data.model.DetailedRecipe
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Unified service that combines recipes from both TheMealDB and Spoonacular APIs
 */
class RecipeService {
    private val mealDbService = TheMealDbService()
    private val spoonacularService = SpoonacularService()

    /**
     * Search for recipes across both APIs
     * @param query Search query
     * @return Flow of DetailedRecipe lists from both APIs
     */
    fun searchRecipes(query: String): Flow<List<DetailedRecipe>> = flow {
        try {
            coroutineScope {
                // Search both APIs concurrently
                val mealDbDeferred = async { mealDbService.searchMealsByName(query) }
                val spoonacularDeferred = async { spoonacularService.searchRecipes(query) }

                // Combine results
                val combinedResults = mealDbDeferred.await() + spoonacularDeferred.await()
                emit(combinedResults)
            }
        } catch (e: Exception) {
            Log.e("RecipeService", "Error searching recipes: ${e.message}", e)
            emit(emptyList())
        }
    }

    /**
     * Get random recipes from both APIs
     * @param count Number of recipes to fetch from each API
     * @return Flow of DetailedRecipe lists
     */
    fun getRandomRecipes(count: Int = 5): Flow<List<DetailedRecipe>> = flow {
        try {
            coroutineScope {
                // Get random recipes from both APIs concurrently
                val mealDbDeferred = async {
                    (1..count).mapNotNull { mealDbService.getRandomMeal() }
                }
                val spoonacularDeferred = async {
                    spoonacularService.getRandomRecipes(count)
                }

                // Combine results
                val combinedResults = mealDbDeferred.await() + spoonacularDeferred.await()
                emit(combinedResults)
            }
        } catch (e: Exception) {
            Log.e("RecipeService", "Error getting random recipes: ${e.message}", e)
            emit(emptyList())
        }
    }

    /**
     * Get recipe details by ID and source
     * @param id Recipe ID
     * @param source Source API ("mealdb" or "spoonacular")
     * @return DetailedRecipe if found, null otherwise
     */
    suspend fun getRecipeById(id: String, source: String): DetailedRecipe? {
        return try {
            when (source.lowercase()) {
                "mealdb" -> mealDbService.getMealById(id)
                "spoonacular" -> spoonacularService.getRecipeById(id.toInt())
                else -> null
            }
        } catch (e: Exception) {
            Log.e("RecipeService", "Error getting recipe details: ${e.message}", e)
            null
        }
    }

    /**
     * Get recipes by category
     * @param category Category name
     * @return Flow of DetailedRecipe lists
     */
    fun getRecipesByCategory(category: String): Flow<List<DetailedRecipe>> = flow {
        try {
            coroutineScope {
                // Get recipes by category from MealDB
                val mealDbDeferred = async { mealDbService.filterByCategory(category) }
                
                // For Spoonacular, we'll search using the category as a query
                val spoonacularDeferred = async { spoonacularService.searchRecipes(category) }

                // Combine results
                val combinedResults = mealDbDeferred.await() + spoonacularDeferred.await()
                emit(combinedResults)
            }
        } catch (e: Exception) {
            Log.e("RecipeService", "Error getting recipes by category: ${e.message}", e)
            emit(emptyList())
        }
    }

    /**
     * Get all available categories
     * @return Flow of category names
     */
    fun getCategories(): Flow<List<String>> = flow {
        try {
            val categories = mealDbService.getCategories()
            emit(categories)
        } catch (e: Exception) {
            Log.e("RecipeService", "Error getting categories: ${e.message}", e)
            emit(emptyList())
        }
    }
}
