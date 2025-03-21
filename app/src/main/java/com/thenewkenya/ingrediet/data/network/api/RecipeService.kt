package com.thenewkenya.ingrediet.data.network.api

import android.content.Context
import android.util.Log
import com.thenewkenya.ingrediet.data.model.DetailedRecipe
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/**
 * Unified service that combines recipes from both TheMealDB and Spoonacular APIs
 */
class RecipeService(private val context: Context) {
    private val mealDbService = TheMealDbService()
    private val spoonacularService = SpoonacularService(context)

    init {
        // Check for saved API limit state in preferences
        val prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(context)
        val apiLimitTimestamp = prefs.getLong(SpoonacularService.API_LIMIT_TIMESTAMP_KEY, 0L)
        
        // If it's been more than 24 hours since the API limit was reached, reset it
        if (apiLimitTimestamp > 0) {
            val now = System.currentTimeMillis()
            val timeSinceLimit = now - apiLimitTimestamp
            
            if (timeSinceLimit > SpoonacularService.API_LIMIT_DURATION_MS) {
                spoonacularService.resetApiLimitStatus()
                Log.d("RecipeService", "Reset Spoonacular API limit status (24 hours passed)")
            } else {
                Log.d("RecipeService", "Spoonacular API limit still active (${timeSinceLimit/1000/60/60} hours remaining)")
            }
        }
    }

    /**
     * Search for recipes across both APIs
     * @param query Search query
     * @return Flow of DetailedRecipe lists from both APIs
     */
    fun searchRecipes(query: String): Flow<List<DetailedRecipe>> = flow {
        Log.d("RecipeService", "Searching recipes with query: $query")
        
        var isCancelled = false
        
        try {
            // Get results from TheMealDB
            val mealDbResults = try {
                val result = mealDbService.searchMealsByName(query)
                if (result.isEmpty()) {
                    Log.d("RecipeService", "No results from TheMealDB")
                }
                result
            } catch (e: Exception) {
                // Check specifically for cancellation
                if (e is kotlinx.coroutines.CancellationException ||
                    e.message?.contains("composition") == true ||
                    e.cause is kotlinx.coroutines.CancellationException) {
                    Log.d("RecipeService", "TheMealDB search cancelled")
                    isCancelled = true
                    emptyList<DetailedRecipe>()
                } else {
                    Log.e("RecipeService", "Error from TheMealDB: ${e.message}")
                    emptyList<DetailedRecipe>()
                }
            }
            
            // Stop processing if cancelled
            if (isCancelled) {
                Log.d("RecipeService", "Search operation cancelled after TheMealDB fetch")
                return@flow
            }
            
            // Get results from Spoonacular
            val spoonacularResults = try {
                val result = spoonacularService.getRandomRecipes(10).first()
                if (result.isEmpty()) {
                    Log.d("RecipeService", "No results from Spoonacular")
                }
                result
            } catch (e: Exception) {
                // Check specifically for cancellation
                if (e is kotlinx.coroutines.CancellationException ||
                    e.message?.contains("composition") == true ||
                    e.cause is kotlinx.coroutines.CancellationException) {
                    Log.d("RecipeService", "Spoonacular search cancelled")
                    isCancelled = true
                    emptyList<DetailedRecipe>()
                } else {
                    Log.e("RecipeService", "Error from Spoonacular: ${e.message}")
                    emptyList<DetailedRecipe>()
                }
            }
            
            // Stop processing if cancelled
            if (isCancelled) {
                Log.d("RecipeService", "Search operation cancelled after Spoonacular fetch")
                return@flow
            }

            // Combine results
            val combinedResults = mealDbResults + spoonacularResults
            
            Log.d("RecipeService", "Combined ${mealDbResults.size} TheMealDB results with ${spoonacularResults.size} Spoonacular results")
            
            // Only emit if not cancelled
            if (!isCancelled) {
                emit(combinedResults)
            }
        } catch (e: Exception) {
            // Specifically handle cancellation without logging errors or emitting values
            if (e is kotlinx.coroutines.CancellationException || 
                e.message?.contains("composition") == true || 
                e.cause is kotlinx.coroutines.CancellationException) {
                Log.d("RecipeService", "Search operation cancelled normally")
                // Don't emit anything
            } else {
                Log.e("RecipeService", "Error searching recipes: ${e.message}")
                emit(emptyList())
            }
        }
    }

    /**
     * Get random recipes
     * @param count Number of recipes to get (default: 5)
     * @return Flow of random recipes
     */
    fun getRandomRecipes(count: Int = 5): Flow<List<DetailedRecipe>> = flow {
        Log.d("RecipeService", "Getting $count random recipes")
        
        // We'll collect results in this list
        val combinedResults = mutableListOf<DetailedRecipe>()
        var isCancelled = false
        
        // Track seen IDs to avoid duplicates
        val seenIds = mutableSetOf<Int>()
        
        // Wrap all operations in a single try block
        try {
            // We'll try up to 2x the requested count to account for potential nulls
            for (i in 1..(count * 2)) {
                // Break early if we have enough recipes or if cancelled
                if (isCancelled || combinedResults.size >= count) {
                    break
                }
                
                try {
                    // Try to get a random meal - this shouldn't throw cancellation exceptions now
                    // since we've updated TheMealDbService to handle those gracefully
                    val meal = mealDbService.getRandomMeal()
                    
                    if (meal != null && meal.id > 0) {
                        // Make sure we don't add duplicates
                        if (meal.id !in seenIds) {
                            Log.d("RecipeService", "Got random meal from TheMealDB: ${meal.name}")
                            combinedResults.add(meal)
                            seenIds.add(meal.id)
                        } else {
                            Log.d("RecipeService", "Skipping duplicate meal: ${meal.name}")
                        }
                    }
                } catch (e: Exception) {
                    // Check if this is a cancellation and break the loop
                    if (e is kotlinx.coroutines.CancellationException ||
                        e.message?.contains("composition") == true ||
                        e.cause is kotlinx.coroutines.CancellationException) {
                        Log.d("RecipeService", "Random recipe fetch cancelled")
                        isCancelled = true
                        // Do NOT throw or emit here - just break the loop
                        break
                    }
                    
                    // Log other errors but continue trying
                    Log.e("RecipeService", "Error getting a single random meal: ${e.message}")
                    // Continue with the next recipe
                }
                
                // Small delay to avoid hammering the API
                kotlinx.coroutines.delay(200) // 200ms delay between requests
            }
            
            // Log the result status
            if (combinedResults.isEmpty() && !isCancelled) {
                Log.d("RecipeService", "No random meals returned from TheMealDB")
            } else if (!isCancelled) {
                Log.d("RecipeService", "Got ${combinedResults.size} random meals from TheMealDB")
            }
            
            // Always emit exactly once, at the end of the flow
            // This ensures we never violate flow transparency by emitting after a flow abortion
            if (!isCancelled) {
                // Even if we don't have the requested number, still emit what we have
                emit(combinedResults)
            } else {
                Log.d("RecipeService", "Random recipes operation cancelled normally")
                // We don't emit anything in the cancellation case
            }
        } catch (e: Exception) {
            // Specifically handle cancellation without logging errors or emitting values
            if (e is kotlinx.coroutines.CancellationException || 
                e.message?.contains("composition") == true || 
                e.cause is kotlinx.coroutines.CancellationException) {
                Log.d("RecipeService", "Random recipes operation cancelled normally")
                // We don't emit anything or throw here
            } else {
                // For other errors, log and emit an empty list
                Log.e("RecipeService", "Error in getRandomRecipes: ${e.message}")
                emit(emptyList())
            }
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
                "spoonacular" -> {
                    try {
                        spoonacularService.getRecipeById(id.toInt()).first()
                    } catch (e: Exception) {
                        Log.e("RecipeService", "Error getting Spoonacular recipe: ${e.message}")
                        null
                    }
                }
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
        var isCancelled = false

        try {
            // Get recipes by category from MealDB
            val mealDbResults = try {
                mealDbService.filterByCategory(category).first()
            } catch (e: Exception) {
                // Check specifically for cancellation
                if (e is kotlinx.coroutines.CancellationException ||
                    e.message?.contains("composition") == true ||
                    e.cause is kotlinx.coroutines.CancellationException) {
                    Log.d("RecipeService", "MealDB category fetch cancelled")
                    isCancelled = true
                    emptyList<DetailedRecipe>()
                } else {
                    Log.e("RecipeService", "Error from TheMealDB category: ${e.message}")
                    emptyList<DetailedRecipe>()
                }
            }
            
            // Stop processing if cancelled
            if (isCancelled) {
                Log.d("RecipeService", "Category search operation cancelled after MealDB fetch")
                return@flow
            }
            
            // For Spoonacular, we'll search using the category as a query
            val spoonacularResults = try {
                spoonacularService.searchRecipes(category).first()
            } catch (e: Exception) {
                // Check specifically for cancellation
                if (e is kotlinx.coroutines.CancellationException ||
                    e.message?.contains("composition") == true ||
                    e.cause is kotlinx.coroutines.CancellationException) {
                    Log.d("RecipeService", "Spoonacular category fetch cancelled")
                    isCancelled = true
                    emptyList<DetailedRecipe>()
                } else {
                    Log.e("RecipeService", "Error from Spoonacular search: ${e.message}")
                    emptyList<DetailedRecipe>()
                }
            }

            // Stop processing if cancelled
            if (isCancelled) {
                Log.d("RecipeService", "Category search operation cancelled after Spoonacular fetch")
                return@flow
            }

            // Combine results
            val combinedResults = mealDbResults + spoonacularResults
            
            // Only emit if not cancelled
            if (!isCancelled) {
                emit(combinedResults)
            }
        } catch (e: Exception) {
            // Specifically handle cancellation without logging errors or emitting values
            if (e is kotlinx.coroutines.CancellationException || 
                e.message?.contains("composition") == true || 
                e.cause is kotlinx.coroutines.CancellationException) {
                Log.d("RecipeService", "Category search operation cancelled normally")
                // Don't emit anything
            } else {
                Log.e("RecipeService", "Error getting recipes by category: ${e.message}")
                emit(emptyList())
            }
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
