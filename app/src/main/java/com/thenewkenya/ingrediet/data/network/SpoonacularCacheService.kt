package com.thenewkenya.ingrediet.data.network

import android.content.Context
import android.util.Log
import com.thenewkenya.ingrediet.data.model.DetailedRecipe
import com.thenewkenya.ingrediet.data.network.api.SpoonacularService
import com.thenewkenya.ingrediet.data.repository.RecipeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.util.concurrent.atomic.AtomicInteger

/**
 * Service for caching Spoonacular API data to Supabase to reduce API usage
 */
class SpoonacularCacheService(private val context: Context) {
    private val TAG = "SpoonacularCacheService"
    private val spoonacularService = SpoonacularService(context)
    private val cacheManager = CacheManager(context)
    private val recipeRepository = RecipeRepository(context)
    
    // Track API usage to avoid hitting limits
    private val dailyRequestCount = AtomicInteger(0)
    private val MAX_DAILY_REQUESTS = 150 // Adjust based on your API plan
    
    // Keep track of the last cache refresh
    private var lastCacheRefreshTime = 0L
    private val CACHE_REFRESH_INTERVAL = 24 * 60 * 60 * 1000 // 24 hours in milliseconds
    
    init {
        // Reset request count every 24 hours
        android.os.Handler(android.os.Looper.getMainLooper()).postAtTime({
            dailyRequestCount.set(0)
            Log.d(TAG, "Reset daily API request count")
        }, System.currentTimeMillis() + CACHE_REFRESH_INTERVAL)
    }
    
    /**
     * Search for recipes and cache the results
     * @param query The search query
     * @param limit Maximum number of recipes to return
     * @return Flow of cached and new recipes matching the query
     */
    suspend fun searchAndCacheRecipes(query: String, limit: Int = 10): Flow<List<DetailedRecipe>> = flow {
        try {
            Log.d(TAG, "Searching and caching recipes for query: $query")
            
            // First return cached results while we fetch new ones
            val cachedResults = cacheManager.getCachedRecipesByQuery(query)
            if (cachedResults.isNotEmpty()) {
                Log.d(TAG, "Found ${cachedResults.size} cached results for query: $query")
                emit(cachedResults)
            }
            
            // If we've hit our API limit, just stop here with cached results
            if (spoonacularService.shouldUseFallbackOnly()) {
                Log.d(TAG, "API limit reached, using only cached results for query: $query")
                return@flow
            }
            
            // Then get new results from the API
            val recipesFlow = spoonacularService.searchRecipes(query, limit)
            recipesFlow.collect { recipes ->
                if (recipes.isNotEmpty()) {
                    Log.d(TAG, "Caching ${recipes.size} new recipes for query: $query")
                    // Cache each recipe individually
                    recipes.forEach { recipe ->
                        cacheManager.cacheRecipe(recipe)
                        Log.d(TAG, "Cached recipe: ${recipe.id}")
                    }
                    
                    // If we found new recipes not in our initial cached results, emit them
                    val combinedResults = LinkedHashSet<DetailedRecipe>()
                    combinedResults.addAll(cachedResults)
                    combinedResults.addAll(recipes)
                    
                    if (combinedResults.size > cachedResults.size) {
                        emit(combinedResults.toList().take(limit))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error searching and caching recipes: ${e.message}", e)
            // Just emit whatever cached results we have
            val cachedResults = cacheManager.getCachedRecipesByQuery(query)
            emit(cachedResults)
        }
    }
    
    /**
     * Get and cache a specific recipe by ID
     * @param recipeId The recipe ID
     * @return The cached recipe or null if not found
     */
    suspend fun getAndCacheRecipeById(recipeId: Int): DetailedRecipe? {
        try {
            Log.d(TAG, "Getting and caching recipe ID: $recipeId")
            
            // First check our local cache
            val cachedRecipe = cacheManager.getCachedRecipe(recipeId)
            if (cachedRecipe != null) {
                Log.d(TAG, "Found recipe $recipeId in cache")
                return cachedRecipe
            }
            
            // If we've hit our API limit, just return null
            if (spoonacularService.shouldUseFallbackOnly()) {
                Log.d(TAG, "API limit reached, cannot fetch new recipe: $recipeId")
                return null
            }
            
            // Then get from the API and cache
            var fetchedRecipe: DetailedRecipe? = null
            spoonacularService.getRecipeById(recipeId).collect { recipe ->
                fetchedRecipe = recipe
            }
            
            if (fetchedRecipe != null) {
                cacheManager.cacheRecipe(fetchedRecipe!!)
                Log.d(TAG, "Successfully cached recipe $recipeId locally")
            }
            
            return fetchedRecipe
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
        try {
            Log.d(TAG, "Fetching and caching random recipes")
            
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
            
            // If we've hit our API limit, just stop here with cached results
            if (spoonacularService.shouldUseFallbackOnly()) {
                Log.d(TAG, "API limit reached, using only cached random recipes")
                return@flow
            }
            
            // Then get new ones from API
            try {
                val recipesFlow = spoonacularService.getRandomRecipes(count)
                recipesFlow.collect { recipes ->
                    if (recipes.isNotEmpty()) {
                        Log.d(TAG, "Caching ${recipes.size} new random recipes")
                        // Cache each recipe individually
                        recipes.forEach { recipe ->
                            cacheManager.cacheRecipe(recipe)
                        }
                        
                        // If we found new recipes not in our initial cached results, emit them
                        val combinedResults = LinkedHashSet<DetailedRecipe>()
                        combinedResults.addAll(cachedRandomRecipes)
                        combinedResults.addAll(recipes)
                        
                        emit(combinedResults.toList().take(count))
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching recipes from Spoonacular API: ${e.message}")
                // No need to re-emit as we've already emitted cached results
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching and caching random recipes: ${e.message}", e)
            // Always emit something to prevent "Expected at least one element" errors
            emit(emptyList())
        }
    }
    
    /**
     * Cache a recipe in local storage only (no longer using Supabase)
     */
    private suspend fun cacheRecipe(recipe: DetailedRecipe) {
        withContext(Dispatchers.IO) {
            try {
                // Store in local cache only
                cacheManager.cacheRecipe(recipe)
                Log.d("SpoonacularCacheService", "Successfully cached recipe ${recipe.id} locally")
            } catch (e: Exception) {
                Log.e("SpoonacularCacheService", "Error caching recipe: ${e.message}", e)
            }
        }
    }
    
    /**
     * Get cached results for a query
     */
    private suspend fun getCachedResults(query: String): List<DetailedRecipe> {
        return withContext(Dispatchers.IO) {
            val results = mutableListOf<DetailedRecipe>()
            
            try {
                recipeRepository.searchRecipes(query).collect { result ->
                    result.onSuccess { recipes ->
                        results.addAll(recipes)
                    }
                }
            } catch (e: Exception) {
                Log.e("SpoonacularCacheService", "Error getting cached results: ${e.message}", e)
            }
            
            return@withContext results
        }
    }
    
    /**
     * Get cached random recipes
     */
    private suspend fun getCachedRandomRecipes(count: Int): List<DetailedRecipe> {
        return withContext(Dispatchers.IO) {
            val results = mutableListOf<DetailedRecipe>()
            
            try {
                // Get random recipes from Supabase based on recent creation date
                recipeRepository.getRecipes(limit = count).collect { result ->
                    result.onSuccess { recipes ->
                        recipes.forEach { recipeItem ->
                            recipeRepository.getRecipeDetails(recipeItem.id).collect { detailResult ->
                                detailResult.onSuccess { recipe ->
                                    results.add(recipe)
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("SpoonacularCacheService", "Error getting cached random recipes: ${e.message}", e)
            }
            
            return@withContext results
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
                    Log.e("SpoonacularCacheService", "Error caching recipe: ${e.message}", e)
                }
            }
        }
    }
    
    /**
     * Helper method to refresh the cache in the background
     */
    private fun refreshCacheInBackground(count: Int) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                if (dailyRequestCount.get() < MAX_DAILY_REQUESTS) {
                    dailyRequestCount.incrementAndGet()
                    val recipesFlow = spoonacularService.getRandomRecipes(count)
                    recipesFlow.collect { recipes ->
                        for (recipe in recipes) {
                            try {
                                cacheRecipe(recipe)
                            } catch (e: Exception) {
                                Log.e("SpoonacularCacheService", "Error caching recipe in background: ${e.message}", e)
                            }
                        }
                    }
                    lastCacheRefreshTime = System.currentTimeMillis()
                }
            } catch (e: Exception) {
                Log.e("SpoonacularCacheService", "Error refreshing cache in background: ${e.message}", e)
            }
        }
    }
} 