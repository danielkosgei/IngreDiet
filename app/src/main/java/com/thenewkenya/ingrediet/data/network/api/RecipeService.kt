package com.thenewkenya.ingrediet.data.network.api

import android.content.Context
import android.util.Log
import com.thenewkenya.ingrediet.data.model.DetailedRecipe
import com.thenewkenya.ingrediet.data.model.KenyanRecipe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/**
 * Unified service for recipe operations using IngreDiet API
 */
class RecipeService(private val context: Context) {
    private val edgeFunctionService = IngreDietService(context)
    private val TAG = "RecipeService"

    /**
     * Get the IngreDietService instance
     * @return IngreDietService instance
     */
    fun getIngreDietService(): IngreDietService {
        return edgeFunctionService
    }

    /**
     * Get recipes with pagination
     * @param limit Maximum number of recipes to return
     * @return Flow of DetailedRecipe lists
     */
    fun getRecipes(limit: Int = 20): Flow<List<DetailedRecipe>> = flow {
        Log.d(TAG, "Getting general recipes, limit: $limit")
        
        try {
            val results = edgeFunctionService.getRecipes(limit).catch { e ->
                // Check for cancellation
                if (e is kotlinx.coroutines.CancellationException) {
                    Log.d(TAG, "Recipe fetch cancelled")
                    throw e
                }
                
                Log.e(TAG, "Error getting recipes: ${e.message}", e)
                emit(emptyList<DetailedRecipe>())
            }.first()
            
            Log.d(TAG, "Found ${results.size} recipes")
            emit(results)
        } catch (e: Exception) {
            // Specifically handle cancellation without logging errors or emitting values
            if (e is kotlinx.coroutines.CancellationException ||
                e.message?.contains("composition") == true ||
                e.cause is kotlinx.coroutines.CancellationException) {
                Log.d(TAG, "Recipe fetch operation cancelled normally")
                // Don't emit anything for cancellation
            } else {
                Log.e(TAG, "Error getting recipes: ${e.message}", e)
                emit(emptyList())
            }
        }
    }

    /**
     * Search for recipes
     * @param query Search query
     * @return Flow of DetailedRecipe lists
     */
    fun searchRecipes(query: String): Flow<List<DetailedRecipe>> = flow {
        Log.d(TAG, "Searching recipes with query: $query")
        
        try {
            val results = edgeFunctionService.searchRecipes(query).catch { e ->
                // Check for cancellation
                if (e is kotlinx.coroutines.CancellationException) {
                    Log.d(TAG, "Recipe search cancelled")
                    throw e
                }
                
                Log.e(TAG, "Error searching recipes: ${e.message}", e)
                emit(emptyList<DetailedRecipe>())
            }.first()
            
            Log.d(TAG, "Found ${results.size} recipes for query: $query")
            emit(results)
        } catch (e: Exception) {
            // Specifically handle cancellation without logging errors or emitting values
            if (e is kotlinx.coroutines.CancellationException || 
                e.message?.contains("composition") == true || 
                e.cause is kotlinx.coroutines.CancellationException) {
                Log.d(TAG, "Search operation cancelled normally")
                // Don't emit anything for cancellation
            } else {
                Log.e(TAG, "Error searching recipes: ${e.message}", e)
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
        Log.d(TAG, "Getting $count random recipes")
        
        try {
            val results = edgeFunctionService.getRandomRecipes(count).catch { e ->
                // Check for cancellation
                if (e is kotlinx.coroutines.CancellationException) {
                    Log.d(TAG, "Random recipe fetch cancelled")
                    throw e
                }
                
                Log.e(TAG, "Error getting random recipes: ${e.message}", e)
                emit(emptyList<DetailedRecipe>())
            }.first()
            
            Log.d(TAG, "Found ${results.size} random recipes")
            emit(results)
        } catch (e: Exception) {
            // Specifically handle cancellation without logging errors or emitting values
            if (e is kotlinx.coroutines.CancellationException || 
                e.message?.contains("composition") == true || 
                e.cause is kotlinx.coroutines.CancellationException) {
                Log.d(TAG, "Random recipes operation cancelled normally")
                // Don't emit anything for cancellation
            } else {
                Log.e(TAG, "Error getting random recipes: ${e.message}", e)
                emit(emptyList())
            }
        }
    }

    /**
     * Get recipe details by ID and source
     * @param id Recipe ID
     * @param source Source API (optional - kept for backward compatibility)
     * @return DetailedRecipe if found, null otherwise
     */
    suspend fun getRecipeById(id: String, source: String? = null): DetailedRecipe? {
        return try {
            // Source parameter is ignored as we're now using a single source
            edgeFunctionService.getRecipeById(id).catch { e ->
                // Check for cancellation
                if (e is kotlinx.coroutines.CancellationException) {
                    Log.d(TAG, "Recipe details fetch cancelled")
                    throw e
                }
                
                Log.e(TAG, "Error getting recipe details: ${e.message}", e)
                emit(null)
            }.first()
        } catch (e: Exception) {
            // Check for cancellation
            if (e is kotlinx.coroutines.CancellationException || 
                e.message?.contains("composition") == true || 
                e.cause is kotlinx.coroutines.CancellationException) {
                Log.d(TAG, "Recipe details operation cancelled normally")
                null
            } else {
                Log.e(TAG, "Error getting recipe details: ${e.message}", e)
                null
            }
        }
    }

    /**
     * Get recipes by category
     * @param category Category name
     * @return Flow of DetailedRecipe lists
     */
    fun getRecipesByCategory(category: String): Flow<List<DetailedRecipe>> = flow {
        Log.d(TAG, "Getting recipes by category: $category")
        
        try {
            // Using search function with category as query
            val results = edgeFunctionService.searchRecipes(category).catch { e ->
                // Check for cancellation
                if (e is kotlinx.coroutines.CancellationException) {
                    Log.d(TAG, "Category search cancelled")
                    throw e
                }
                
                Log.e(TAG, "Error getting recipes by category: ${e.message}", e)
                emit(emptyList<DetailedRecipe>())
            }.first()
            
            Log.d(TAG, "Found ${results.size} recipes for category: $category")
            emit(results)
            } catch (e: Exception) {
            // Specifically handle cancellation without logging errors or emitting values
                if (e is kotlinx.coroutines.CancellationException ||
                    e.message?.contains("composition") == true ||
                    e.cause is kotlinx.coroutines.CancellationException) {
                Log.d(TAG, "Category search operation cancelled normally")
                // Don't emit anything for cancellation
                } else {
                Log.e(TAG, "Error getting recipes by category: ${e.message}", e)
                emit(emptyList())
            }
        }
    }

    /**
     * Get recipes by ingredients
     * @param ingredients List of ingredient names
     * @param limit Maximum number of recipes to return
     * @return Flow of DetailedRecipe lists
     */
    fun getRecipesByIngredients(ingredients: List<String>, limit: Int = 10): Flow<List<DetailedRecipe>> = flow {
        Log.d(TAG, "Getting recipes by ingredients: $ingredients")
        
        try {
            val results = edgeFunctionService.getRecipesByIngredients(ingredients, limit).catch { e ->
                // Check for cancellation
                if (e is kotlinx.coroutines.CancellationException) {
                    Log.d(TAG, "Ingredients search cancelled")
                    throw e
                }
                
                Log.e(TAG, "Error getting recipes by ingredients: ${e.message}", e)
                emit(emptyList<DetailedRecipe>())
            }.first()
            
            Log.d(TAG, "Found ${results.size} recipes for ingredients: $ingredients")
            emit(results)
        } catch (e: Exception) {
            // Specifically handle cancellation without logging errors or emitting values
            if (e is kotlinx.coroutines.CancellationException || 
                e.message?.contains("composition") == true || 
                e.cause is kotlinx.coroutines.CancellationException) {
                Log.d(TAG, "Ingredients search operation cancelled normally")
                // Don't emit anything for cancellation
            } else {
                Log.e(TAG, "Error getting recipes by ingredients: ${e.message}", e)
                emit(emptyList())
            }
        }
    }

    /**
     * Get Kenyan recipes
     * @param limit Maximum number of recipes to return
     * @return Flow of KenyanRecipe lists
     */
    fun getKenyanRecipes(limit: Int = 10): Flow<List<KenyanRecipe>> = flow {
        Log.d(TAG, "Getting Kenyan recipes, limit: $limit")
        
        try {
            val results = edgeFunctionService.getKenyanRecipes(limit).catch { e ->
                // Check for cancellation
                if (e is kotlinx.coroutines.CancellationException) {
                    Log.d(TAG, "Kenyan recipes fetch cancelled")
                    throw e
                }
                
                Log.e(TAG, "Error getting Kenyan recipes: ${e.message}", e)
                emit(emptyList<KenyanRecipe>())
            }.first()
            
            Log.d(TAG, "Found ${results.size} Kenyan recipes")
            emit(results)
            } catch (e: Exception) {
            // Specifically handle cancellation without logging errors or emitting values
                if (e is kotlinx.coroutines.CancellationException ||
                    e.message?.contains("composition") == true ||
                    e.cause is kotlinx.coroutines.CancellationException) {
                Log.d(TAG, "Kenyan recipes operation cancelled normally")
                // Don't emit anything for cancellation
                } else {
                Log.e(TAG, "Error getting Kenyan recipes: ${e.message}", e)
                emit(emptyList())
            }
        }
    }

    /**
     * Get Kenyan recipe by ID
     * @param id Recipe ID
     * @return KenyanRecipe if found, null otherwise
     */
    suspend fun getKenyanRecipeById(id: String): KenyanRecipe? {
        return try {
            Log.d(TAG, "Getting Kenyan recipe details with ID: $id")
            edgeFunctionService.getKenyanRecipeById(id)
        } catch (e: Exception) {
            // Check for cancellation
            if (e is kotlinx.coroutines.CancellationException || 
                e.message?.contains("composition") == true || 
                e.cause is kotlinx.coroutines.CancellationException) {
                Log.d(TAG, "Kenyan recipe details operation cancelled normally")
                null
            } else {
                Log.e(TAG, "Error getting Kenyan recipe details: ${e.message}", e)
                null
            }
        }
    }

    /**
     * Get Kenyan recipes by region
     * @param region Region name
     * @param limit Maximum number of recipes to return
     * @return Flow of KenyanRecipe lists
     */
    fun getKenyanRecipesByRegion(region: String, limit: Int = 10): Flow<List<KenyanRecipe>> = flow {
        Log.d(TAG, "Getting Kenyan recipes by region: $region, limit: $limit")
        
        try {
            val results = edgeFunctionService.getKenyanRecipesByRegion(region, limit).catch { e ->
                // Check for cancellation
                if (e is kotlinx.coroutines.CancellationException) {
                    Log.d(TAG, "Kenyan recipes by region fetch cancelled")
                    throw e
                }
                
                Log.e(TAG, "Error getting Kenyan recipes by region: ${e.message}", e)
                emit(emptyList<KenyanRecipe>())
            }.first()
            
            Log.d(TAG, "Found ${results.size} Kenyan recipes for region: $region")
            emit(results)
        } catch (e: Exception) {
            // Specifically handle cancellation without logging errors or emitting values
            if (e is kotlinx.coroutines.CancellationException || 
                e.message?.contains("composition") == true || 
                e.cause is kotlinx.coroutines.CancellationException) {
                Log.d(TAG, "Kenyan recipes by region operation cancelled normally")
                // Don't emit anything for cancellation
            } else {
                Log.e(TAG, "Error getting Kenyan recipes by region: ${e.message}", e)
                emit(emptyList())
            }
        }
    }

    /**
     * Search Kenyan recipes
     * @param query Search query
     * @param limit Maximum number of recipes to return
     * @return Flow of KenyanRecipe lists
     */
    fun searchKenyanRecipes(query: String, limit: Int = 10): Flow<List<KenyanRecipe>> = flow {
        Log.d(TAG, "Searching Kenyan recipes with query: $query, limit: $limit")
        
        try {
            val results = edgeFunctionService.searchKenyanRecipes(query, limit).catch { e ->
                // Check for cancellation
                if (e is kotlinx.coroutines.CancellationException) {
                    Log.d(TAG, "Kenyan recipe search cancelled")
                    throw e
                }
                
                Log.e(TAG, "Error searching Kenyan recipes: ${e.message}", e)
                emit(emptyList<KenyanRecipe>())
            }.first()
            
            Log.d(TAG, "Found ${results.size} Kenyan recipes for query: $query")
            emit(results)
        } catch (e: Exception) {
            // Specifically handle cancellation without logging errors or emitting values
            if (e is kotlinx.coroutines.CancellationException || 
                e.message?.contains("composition") == true || 
                e.cause is kotlinx.coroutines.CancellationException) {
                Log.d(TAG, "Kenyan recipe search operation cancelled normally")
                // Don't emit anything for cancellation
            } else {
                Log.e(TAG, "Error searching Kenyan recipes: ${e.message}", e)
            emit(emptyList())
            }
        }
    }
}
