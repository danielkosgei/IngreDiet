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
import java.util.concurrent.TimeUnit

/**
 * Manages caching of API responses to reduce external API calls and Supabase storage usage
 */
class CacheManager(context: Context) {
    companion object {
        private const val PREFS_NAME = "ingrediet_cache"
        private const val RECIPE_PREFIX = "recipe_"
        private const val INGREDIENT_PREFIX = "ingredient_"
        private const val LAST_USED_PREFIX = "last_used_"
        private const val CACHE_EXPIRY_DAYS = 7 // Default cache expiry in days
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
     * Get a cached recipe by ID
     * @return The cached recipe or null if not found or expired
     */
    suspend fun getCachedRecipe(recipeId: Int): DetailedRecipe? = withContext(Dispatchers.IO) {
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
    suspend fun getCachedIngredient(ingredientId: Int): IngredientItem? = withContext(Dispatchers.IO) {
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
    fun isRecipeCached(recipeId: Int): Boolean {
        val recipeKey = "$RECIPE_PREFIX$recipeId"
        return prefs.contains(recipeKey) && !isCacheExpired(recipeKey)
    }
    
    /**
     * Check if an ingredient exists in the cache and is not expired
     */
    fun isIngredientCached(ingredientId: Int): Boolean {
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
}
