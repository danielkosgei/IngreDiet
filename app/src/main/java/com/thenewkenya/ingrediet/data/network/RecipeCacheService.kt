package com.thenewkenya.ingrediet.data.network

import android.content.Context
import android.util.Log
import com.thenewkenya.ingrediet.data.model.DetailedRecipe
import com.thenewkenya.ingrediet.data.network.api.IngreDietService
import com.thenewkenya.ingrediet.data.repository.RecipeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.LinkedHashSet
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.CancellationException

/**
 * Service for caching recipe data from IngreDiet API
 */
class RecipeCacheService(private val context: Context) {
    private val TAG = "RecipeCacheService"
    private val ingreDietService = IngreDietService(context)
    private val cacheManager = CacheManager(context)
    
    /**
     * Search for recipes and cache the results
     * @param query The search query
     * @param limit Maximum number of recipes to return
     * @return Flow of cached and new recipes matching the query
     */
    suspend fun searchAndCacheRecipes(query: String, limit: Int = 10): Flow<List<DetailedRecipe>> = flow {
        Log.d(TAG, "Searching and caching recipes for query: $query")
        
        try {
            // First return cached results while we fetch new ones
            val cachedResults = cacheManager.getCachedRecipesByQuery(query)
            if (cachedResults.isNotEmpty()) {
                Log.d(TAG, "Found ${cachedResults.size} cached results for query: $query")
                emit(cachedResults)
            } else {
                // Emit empty list so collectors have something to start with
                Log.d(TAG, "No cached results for query: $query")
                emit(emptyList())
            }
            
            // Then get new results from the IngreDiet API
            try {
                ingreDietService.searchRecipes(query, limit)
                    .catch { e -> 
                        // Check if this is just a Flow abort exception (normal cancellation)
                        if (e.message?.contains("Flow was aborted") == true || 
                            e::class.java.name.contains("AbortFlowException")) {
                            Log.d(TAG, "Recipe search flow completed normally")
                            return@catch  // Just return, don't emit or rethrow
                        }
                        
                        // Handle specific cancellation explicitly
                        if (e is CancellationException) {
                            Log.d(TAG, "Recipe search cancelled normally")
                            throw e // Re-throw cancellation to preserve flow collection cancellation
                        } else {
                            Log.e(TAG, "Error fetching recipes from API: ${e.message}", e)
                            // Don't emit again here to avoid flow transparency violation
                        }
                    }
                    .collect { recipes ->
                        if (recipes.isNotEmpty()) {
                            Log.d(TAG, "Caching ${recipes.size} new recipes for query: $query")
                            // Cache each recipe individually
                            cacheRecipesInBackground(recipes)
                            
                            // Combine with cached results and emit
                            val combinedResults = LinkedHashSet<DetailedRecipe>()
                            combinedResults.addAll(cachedResults)
                            combinedResults.addAll(recipes)
                            
                            emit(combinedResults.toList().take(limit))
                        }
                    }
            } catch (e: Exception) {
                // Check for abort exceptions
                if (e.message?.contains("Flow was aborted") == true || 
                    e::class.java.name.contains("AbortFlowException")) {
                    Log.d(TAG, "Cache service flow completed normally")
                    return@flow
                }
                
                // Just re-throw cancellation exceptions
                if (e is CancellationException) {
                    Log.d(TAG, "Recipe search cancelled")
                    throw e
                }
                Log.e(TAG, "Error fetching recipes from API: ${e.message}", e)
                // Don't emit again here to avoid flow transparency violation
            }
        } catch (e: CancellationException) {
            // Just re-throw cancellation exceptions
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error searching and caching recipes: ${e.message}", e)
            // Only emit if we haven't already emitted something
            if (!cacheManager.getCachedRecipesByQuery(query).isNotEmpty()) {
                emit(emptyList())
            }
        }
    }
    
    /**
     * Get and cache a specific recipe by ID
     * @param recipeId The recipe ID
     * @return The cached recipe or null if not found
     */
    suspend fun getAndCacheRecipeById(recipeId: String): DetailedRecipe? {
        try {
            Log.d(TAG, "Getting and caching recipe ID: $recipeId")
            
            // First check our local cache
            val cachedRecipe = cacheManager.getCachedRecipe(recipeId)
            if (cachedRecipe != null) {
                Log.d(TAG, "Found recipe $recipeId in cache")
                return cachedRecipe
            }
            
            // Then get from the IngreDiet API and cache
            var fetchedRecipe: DetailedRecipe? = null
            try {
                ingreDietService.getRecipeById(recipeId)
                    .catch { e ->
                        if (e is CancellationException) {
                            Log.d(TAG, "Recipe detail fetch cancelled")
                            throw e
                        } else {
                            Log.e(TAG, "Error fetching recipe from API: ${e.message}", e)
                        }
                    }
                    .collect { recipe ->
                        fetchedRecipe = recipe
                    }
                
                if (fetchedRecipe != null) {
                    cacheManager.cacheRecipe(fetchedRecipe!!)
                    Log.d(TAG, "Successfully cached recipe $recipeId locally")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching recipe from API: ${e.message}", e)
            }
            
            return fetchedRecipe
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error getting and caching recipe: ${e.message}", e)
            return null
        }
    }
    
    /**
     * Get and cache random recipes
     * @param count The number of recipes to fetch
     * @return Flow of random recipes
     */
    suspend fun getAndCacheRandomRecipes(count: Int = 10): Flow<List<DetailedRecipe>> = flow {
        Log.d(TAG, "Fetching and caching random recipes")
        
        try {
            // First emit cached random recipes while we fetch new ones
            val cachedRandomRecipes = cacheManager.getRandomCachedRecipes(count)
            if (cachedRandomRecipes.isNotEmpty()) {
                Log.d(TAG, "Found ${cachedRandomRecipes.size} cached random recipes")
                emit(cachedRandomRecipes)
            } else {
                // Always emit at least an empty list if no cached recipes
                Log.d(TAG, "No cached random recipes found")
                emit(emptyList())
            }
            
            // Then get new ones from IngreDiet API
            try {
                ingreDietService.getRandomRecipes(count)
                    .catch { e ->
                        // Check if this is just a Flow abort exception (normal cancellation)
                        if (e.message?.contains("Flow was aborted") == true || 
                            e::class.java.name.contains("AbortFlowException")) {
                            Log.d(TAG, "Random recipes flow completed normally")
                            return@catch  // Just return, don't emit or rethrow
                        }
                        
                        if (e is CancellationException) {
                            Log.d(TAG, "Random recipes fetch cancelled")
                            throw e
                        } else {
                            Log.e(TAG, "Error fetching recipes from IngreDiet API: ${e.message}", e)
                            // Don't emit here to avoid flow transparency violation
                        }
                    }
                    .collect { recipes ->
                        if (recipes.isNotEmpty()) {
                            Log.d(TAG, "Caching ${recipes.size} new random recipes")
                            // Cache recipes in background
                            cacheRecipesInBackground(recipes)
                            
                            // If we found new recipes not in our initial cached results, emit them
                            val combinedResults = LinkedHashSet<DetailedRecipe>()
                            combinedResults.addAll(cachedRandomRecipes)
                            combinedResults.addAll(recipes)
                            
                            emit(combinedResults.toList().take(count))
                        }
                    }
            } catch (e: Exception) {
                // Check for abort exceptions
                if (e.message?.contains("Flow was aborted") == true || 
                    e::class.java.name.contains("AbortFlowException")) {
                    Log.d(TAG, "Random recipes cache service flow completed normally")
                    return@flow
                }
                
                if (e is CancellationException) {
                    Log.d(TAG, "Random recipes fetch cancelled")
                    throw e
                }
                Log.e(TAG, "Error fetching recipes from IngreDiet API: ${e.message}", e)
                // No need to re-emit as we've already emitted cached results
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching and caching random recipes: ${e.message}", e)
            // Only emit if we haven't emitted anything yet
            if (cacheManager.getRandomCachedRecipes(count).isEmpty()) {
                emit(emptyList())
            }
        }
    }
    
    /**
     * Cache a recipe in local storage
     */
    private suspend fun cacheRecipe(recipe: DetailedRecipe) {
        withContext(Dispatchers.IO) {
            try {
                // Store in local cache
                cacheManager.cacheRecipe(recipe)
                Log.d(TAG, "Successfully cached recipe ${recipe.id} locally")
            } catch (e: Exception) {
                Log.e(TAG, "Error caching recipe: ${e.message}", e)
            }
        }
    }
    
    /**
     * Helper method to cache recipes in the background without blocking
     */
    private fun cacheRecipesInBackground(recipes: List<DetailedRecipe>) {
        GlobalScope.launch(Dispatchers.IO) {
            for (recipe in recipes) {
                try {
                    cacheRecipe(recipe)
                } catch (e: Exception) {
                    Log.e(TAG, "Error caching recipe: ${e.message}", e)
                }
            }
        }
    }
} 