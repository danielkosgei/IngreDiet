package com.thenewkenya.ingrediet.data.network

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.thenewkenya.ingrediet.data.model.DetailedRecipe
import com.thenewkenya.ingrediet.data.model.IngredientItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Manages caching of API responses to reduce external API calls and Supabase storage usage
 */
class CacheManager(private val context: Context) {
    companion object {
        private const val PREFS_NAME = "ingrediet_cache"
        private const val RECIPE_PREFIX = "recipe_"
        private const val INGREDIENT_PREFIX = "ingredient_"
        private const val LAST_USED_PREFIX = "last_used_"
        private const val CACHE_EXPIRY_DAYS = 7 // Default cache expiry in days
        private const val RECIPE_CACHE_DIR = "recipe_cache"
    }
    
    private val json = Json { ignoreUnknownKeys = true }
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    /**
     * Cache a recipe in local storage
     */
    suspend fun cacheRecipe(recipe: DetailedRecipe?) = withContext(Dispatchers.IO) {
        try {
            val recipeJson = json.encodeToString(recipe)
            if (recipe != null) {
                prefs.edit()
                    .putString("$RECIPE_PREFIX${recipe.id}", recipeJson)
                    .putLong("$LAST_USED_PREFIX$RECIPE_PREFIX${recipe.id}", System.currentTimeMillis())
                    .apply()
            }
            Log.d("CacheManager", "Cached recipe: ${recipe?.id}")
        } catch (e: Exception) {
            Log.e("CacheManager", "Error caching recipe: ${e.message}", e)
        }
    }
    
    /**
     * Cache a recipe synchronously (non-suspending version)
     * For use in non-suspending contexts like callbacks
     */
    fun cacheRecipeSync(recipe: DetailedRecipe?) {
        if (recipe == null) return
        try {
            val recipeKey = "$RECIPE_PREFIX${recipe.id}"
            prefs.edit()
                .putString(recipeKey, json.encodeToString(recipe))
                .putLong("$LAST_USED_PREFIX$recipeKey", System.currentTimeMillis())
                .apply()
                
            Log.d("CacheManager", "Cached recipe: ${recipe.id} - ${recipe.name}")
        } catch (e: Exception) {
            Log.e("CacheManager", "Error caching recipe: ${e.message}", e)
        }
    }
    
    /**
     * Get a cached recipe by its ID
     * @param recipeId The recipe ID
     * @return The cached DetailedRecipe or null if not found/expired
     */
    suspend fun getCachedRecipe(recipeId: String): DetailedRecipe? = withContext(Dispatchers.IO) {
        try {
            val recipeKey = "$RECIPE_PREFIX$recipeId"
            val recipeJson = prefs.getString(recipeKey, null) ?: return@withContext null
            
            // Update last used timestamp
            prefs.edit()
                .putLong("$LAST_USED_PREFIX$recipeKey", System.currentTimeMillis())
                .apply()
                
            return@withContext json.decodeFromString<DetailedRecipe>(recipeJson)
        } catch (e: Exception) {
            Log.e("CacheManager", "Error getting cached recipe: ${e.message}", e)
            return@withContext null
        }
    }
    
    /**
     * Get a cached recipe by ID (non-suspending version)
     * For use in non-suspending contexts like callbacks
     * @return The cached recipe or null if not found or expired
     */
    fun getCachedRecipeSync(recipeId: String): DetailedRecipe? {
        try {
            val recipeKey = "$RECIPE_PREFIX$recipeId"
            val recipeJson = prefs.getString(recipeKey, null) ?: return null
            
            // Update last used timestamp
            prefs.edit()
                .putLong("$LAST_USED_PREFIX$recipeKey", System.currentTimeMillis())
                .apply()
                
            return json.decodeFromString<DetailedRecipe>(recipeJson)
        } catch (e: Exception) {
            Log.e("CacheManager", "Error getting cached recipe: ${e.message}", e)
            return null
        }
    }
    
    /**
     * Cache an ingredient in local storage
     */
    suspend fun cacheIngredient(ingredient: IngredientItem) = withContext(Dispatchers.IO) {
        try {
            val ingredientJson = json.encodeToString(ingredient)
            prefs.edit()
                .putString("$INGREDIENT_PREFIX${ingredient.id}", ingredientJson)
                .putLong("$LAST_USED_PREFIX$INGREDIENT_PREFIX${ingredient.id}", System.currentTimeMillis())
                .apply()
            Log.d("CacheManager", "Cached ingredient: ${ingredient.id}")
        } catch (e: Exception) {
            Log.e("CacheManager", "Error caching ingredient: ${e.message}", e)
        }
    }
    
    /**
     * Get a cached ingredient by ID
     * @return The cached ingredient or null if not found or expired
     */
    suspend fun getCachedIngredient(ingredientId: String): IngredientItem? = withContext(Dispatchers.IO) {
        try {
            val ingredientKey = "$INGREDIENT_PREFIX$ingredientId"
            val ingredientJson = prefs.getString(ingredientKey, null) ?: return@withContext null
            
            // Update last used timestamp
            prefs.edit()
                .putLong("$LAST_USED_PREFIX$ingredientKey", System.currentTimeMillis())
                .apply()
                
            return@withContext json.decodeFromString<IngredientItem>(ingredientJson)
        } catch (e: Exception) {
            Log.e("CacheManager", "Error getting cached ingredient: ${e.message}", e)
            return@withContext null
        }
    }
    
    /**
     * Check if a recipe exists in the cache and is not expired
     */
    fun isRecipeCached(recipeId: String): Boolean {
        val recipeKey = "$RECIPE_PREFIX$recipeId"
        return prefs.contains(recipeKey) && !isCacheExpired(recipeKey)
    }
    
    /**
     * Check if an ingredient exists in the cache and is not expired
     */
    fun isIngredientCached(ingredientId: String): Boolean {
        val ingredientKey = "$INGREDIENT_PREFIX$ingredientId"
        return prefs.contains(ingredientKey) && !isCacheExpired(ingredientKey)
    }
    
    /**
     * Check if a cached item is expired
     */
    private fun isCacheExpired(key: String): Boolean {
        val lastUsedKey = "$LAST_USED_PREFIX$key"
        val lastUsedTime = prefs.getLong(lastUsedKey, 0)
        if (lastUsedTime == 0L) return true
        
        val currentTime = System.currentTimeMillis()
        val expiryTime = TimeUnit.DAYS.toMillis(CACHE_EXPIRY_DAYS.toLong())
        
        return currentTime - lastUsedTime > expiryTime
    }
    
    /**
     * Clean up expired or unused cache entries
     * This should be called periodically to free up space
     */
    suspend fun cleanupCache() = withContext(Dispatchers.IO) {
        try {
            val currentTime = System.currentTimeMillis()
            val expiryTime = TimeUnit.DAYS.toMillis(CACHE_EXPIRY_DAYS.toLong())
            val editor = prefs.edit()
            var cleanedCount = 0
            
            // Get all keys
            val allEntries = prefs.all
            
            // Check each key for expiration
            for (entry in allEntries) {
                val key = entry.key
                
                // Skip non-cache entries
                if (!key.startsWith(RECIPE_PREFIX) && !key.startsWith(INGREDIENT_PREFIX)) continue
                
                // Check if expired
                val lastUsedKey = "$LAST_USED_PREFIX$key"
                val lastUsedTime = prefs.getLong(lastUsedKey, 0)
                
                if (lastUsedTime == 0L || currentTime - lastUsedTime > expiryTime) {
                    // Remove expired entry
                    editor.remove(key)
                    editor.remove(lastUsedKey)
                    cleanedCount++
                }
            }
            
            editor.apply()
            Log.d("CacheManager", "Cleaned up $cleanedCount expired cache entries")
        } catch (e: Exception) {
            Log.e("CacheManager", "Error cleaning up cache: ${e.message}", e)
        }
    }

    /**
     * Get cached recipes that match a query
     * @param query The search query to match against recipe names
     * @return List of matching recipes from cache
     */
    fun getCachedRecipesByQuery(query: String): List<DetailedRecipe> {
        val cachedRecipes = getRecipesFromCache()
        
        // If there's no actual query, return a subset of all recipes
        if (query.isBlank()) {
            return cachedRecipes.take(10)
        }
        
        // Otherwise filter by the query term
        return cachedRecipes.filter {
            it.name.contains(query, ignoreCase = true)
        }
    }

    /**
     * Get a random selection of cached recipes
     * @param count Number of recipes to return
     * @return List of random recipes from cache
     */
    fun getRandomCachedRecipes(count: Int): List<DetailedRecipe> {
        val cachedRecipes = getRecipesFromCache()
        
        // If we have fewer recipes than requested, return all of them
        if (cachedRecipes.size <= count) {
            return cachedRecipes
        }
        
        // Otherwise return a random subset
        return cachedRecipes.shuffled().take(count)
    }

    /**
     * Get all recipes from the cache
     * @return List of all cached recipes
     */
    private fun getRecipesFromCache(): List<DetailedRecipe> {
        val cachedRecipes = mutableListOf<DetailedRecipe>()
        
        try {
            val parentDir = context.cacheDir
            val cacheDir = File(parentDir.path, RECIPE_CACHE_DIR)
            if (!cacheDir.exists()) {
                return emptyList()
            }
            
            val files = cacheDir.listFiles()
            files?.forEach { file ->
                try {
                    val content = file.readText()
                    val recipe = json.decodeFromString<DetailedRecipe>(content)
                    cachedRecipes.add(recipe)
                } catch (e: Exception) {
                    Log.e("CacheManager", "Error reading cached recipe: ${e.message}")
                    // Continue with other files
                }
            }
        } catch (e: Exception) {
            Log.e("CacheManager", "Error getting recipes from cache: ${e.message}")
        }
        
        return cachedRecipes
    }

    private fun getRecipeCacheFile(recipeId: String): File {
        val cacheDir = context.cacheDir
        // Ensure directory exists
        val recipeCacheDir = File(cacheDir, RECIPE_CACHE_DIR)
        recipeCacheDir.mkdirs()
        return File(recipeCacheDir, "$RECIPE_PREFIX$recipeId")
    }

    private suspend fun loadRecipeFromCache(file: File): DetailedRecipe? {
        if (!file.exists()) return null
        
        val content = file.readText()
        return json.decodeFromString<DetailedRecipe>(content)
    }
}
