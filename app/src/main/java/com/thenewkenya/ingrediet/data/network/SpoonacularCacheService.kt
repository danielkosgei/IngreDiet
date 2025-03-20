package com.thenewkenya.ingrediet.data.network

import android.content.Context
import android.util.Log
import com.thenewkenya.ingrediet.data.model.DetailedRecipe
import com.thenewkenya.ingrediet.data.network.api.SpoonacularService
import com.thenewkenya.ingrediet.data.repository.RecipeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicInteger

/**
 * Service for caching Spoonacular API data to Supabase to reduce API usage
 */
class SpoonacularCacheService(private val context: Context) {
    private val spoonacularService = SpoonacularService()
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
            Log.d("SpoonacularCacheService", "Reset daily API request count")
        }, System.currentTimeMillis() + CACHE_REFRESH_INTERVAL)
    }
    
    /**
     * Search and cache recipes from Spoonacular
     * @param query Search query
     * @param maxResults Maximum number of results to return
     * @return Flow of DetailedRecipe lists
     */
    fun searchAndCacheRecipes(query: String, maxResults: Int = 10): Flow<List<DetailedRecipe>> = flow {
        try {
            // Check if we're under the request limit
            if (dailyRequestCount.get() >= MAX_DAILY_REQUESTS) {
                Log.d("SpoonacularCacheService", "Daily API request limit reached")
                // Fallback to cached results only
                val cachedResults = getCachedResults(query)
                emit(cachedResults)
                return@flow
            }
            
            // Increment the request counter
            dailyRequestCount.incrementAndGet()
            
            // Get recipes from Spoonacular
            val recipes = spoonacularService.searchRecipes(query)
            
            // Store recipes in Supabase and local cache
            val cachedRecipes = mutableListOf<DetailedRecipe>()
            for (recipe in recipes.take(maxResults)) {
                cacheRecipe(recipe)
                cachedRecipes.add(recipe)
            }
            
            emit(cachedRecipes)
        } catch (e: Exception) {
            Log.e("SpoonacularCacheService", "Error searching and caching recipes: ${e.message}", e)
            // Fallback to cached results
            val cachedResults = getCachedResults(query)
            emit(cachedResults)
        }
    }
    
    /**
     * Get random recipes and cache them
     * @param count Number of recipes to fetch
     * @return Flow of DetailedRecipe lists
     */
    fun getAndCacheRandomRecipes(count: Int = 10): Flow<List<DetailedRecipe>> = flow {
        try {
            // Check if we're under the request limit
            if (dailyRequestCount.get() >= MAX_DAILY_REQUESTS) {
                Log.d("SpoonacularCacheService", "Daily API request limit reached")
                // Fallback to cached results only
                val cachedResults = getCachedRandomRecipes(count)
                emit(cachedResults)
                return@flow
            }
            
            // Check if we need to refresh the cache
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastCacheRefreshTime > CACHE_REFRESH_INTERVAL) {
                // Increment the request counter
                dailyRequestCount.incrementAndGet()
                
                // Get random recipes from Spoonacular
                val recipes = spoonacularService.getRandomRecipes(count)
                
                // Store recipes in Supabase and local cache
                val cachedRecipes = mutableListOf<DetailedRecipe>()
                for (recipe in recipes) {
                    cacheRecipe(recipe)
                    cachedRecipes.add(recipe)
                }
                
                lastCacheRefreshTime = currentTime
                emit(cachedRecipes)
            } else {
                // Use cached random recipes
                val cachedResults = getCachedRandomRecipes(count)
                emit(cachedResults)
            }
        } catch (e: Exception) {
            Log.e("SpoonacularCacheService", "Error getting and caching random recipes: ${e.message}", e)
            // Fallback to cached results
            val cachedResults = getCachedRandomRecipes(count)
            emit(cachedResults)
        }
    }
    
    /**
     * Get recipe details by ID and cache it
     * @param id Recipe ID
     * @return DetailedRecipe if found, null otherwise
     */
    suspend fun getAndCacheRecipeById(id: Int): DetailedRecipe? {
        return withContext(Dispatchers.IO) {
            try {
                // Check if recipe is in local cache first
                val cachedRecipe = cacheManager.getCachedRecipe(id)
                if (cachedRecipe != null) {
                    Log.d("SpoonacularCacheService", "Found recipe $id in local cache")
                    return@withContext cachedRecipe
                }
                
                // If not in cache, check if we're under the request limit
                if (dailyRequestCount.get() >= MAX_DAILY_REQUESTS) {
                    Log.d("SpoonacularCacheService", "Daily API request limit reached")
                    return@withContext null
                }
                
                // Increment the request counter
                dailyRequestCount.incrementAndGet()
                
                // Get recipe from Spoonacular
                val recipe = spoonacularService.getRecipeById(id)
                
                // Store recipe in Supabase and local cache
                if (recipe != null) {
                    cacheRecipe(recipe)
                }
                
                return@withContext recipe
            } catch (e: Exception) {
                Log.e("SpoonacularCacheService", "Error getting and caching recipe: ${e.message}", e)
                return@withContext null
            }
        }
    }
    
    /**
     * Cache a recipe in both local storage and Supabase
     */
    private suspend fun cacheRecipe(recipe: DetailedRecipe) {
        withContext(Dispatchers.IO) {
            try {
                // Store in local cache first
                cacheManager.cacheRecipe(recipe)
                
                // Then store in Supabase
                try {
                    recipeRepository.storeRecipeInSupabase(recipe)
                } catch (e: Exception) {
                    Log.e("SpoonacularCacheService", "Error storing recipe in Supabase: ${e.message}", e)
                    // Continue even if Supabase storage fails
                }
                
                Log.d("SpoonacularCacheService", "Successfully cached recipe ${recipe.id}")
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
} 