package com.thenewkenya.ingrediet.data.repository

import android.content.Context
import android.util.Log
import com.thenewkenya.ingrediet.data.model.FavoriteRecipe
import com.thenewkenya.ingrediet.data.network.SessionManager
import com.thenewkenya.ingrediet.data.network.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.PostgrestRequestBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * Repository for managing favorite recipes.
 */
class FavoritesRepository private constructor(context: Context) {
    private val sessionManager = SessionManager(context)
    
    // Singleton pattern implementation
    companion object {
        @Volatile
        private var instance: FavoritesRepository? = null
        private const val TAG = "FavoritesRepository"
        
        fun getInstance(context: Context): FavoritesRepository {
            return instance ?: synchronized(this) {
                instance ?: FavoritesRepository(context).also { instance = it }
            }
        }
    }
    
    @Serializable
    private data class UserFavoriteDto(
        val user_id: String,
        val recipe_id: String,
        val recipes: RecipeDto? = null
    )
    
    @Serializable
    private data class RecipeDto(
        val id: String,
        val name: String,
        val description: String? = "",
        val image_url: String? = "",
        val ingredients: JsonObject? = null,
        val instructions: String? = "",
        val category: String? = "",
        val recipe_nutrition: List<NutritionDto>? = null
    )
    
    @Serializable
    private data class NutritionDto(
        val calories: Int? = 0,
        val protein: Float? = 0f,
        val carbs: Float? = 0f,
        val fat: Float? = 0f
    )
    
    /**
     * Gets all favorite recipes for the current user.
     */
    suspend fun getFavoriteRecipes(): List<FavoriteRecipe> = withContext(Dispatchers.IO) {
        try {
            val userId = sessionManager.getCurrentUserId()
            if (userId == null) {
                Log.w(TAG, "No user ID found, returning empty favorites list")
                return@withContext emptyList()
            }

            // Fetch favorites with recipe details using a single nested query
            val favorites = supabase.from("user_favorites")
                .select(Columns.raw("*, recipes(*, recipe_nutrition(*))")) {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<UserFavoriteDto>()

            Log.d(TAG, "Fetched ${favorites.size} favorites for user $userId")

            // Map to FavoriteRecipe objects
            return@withContext favorites.mapNotNull { dto ->
                dto.recipes?.let { recipe ->
                    FavoriteRecipe(
                        id = recipe.id,
                        name = recipe.name,
                        description = recipe.description ?: "",
                        imageUrl = recipe.image_url ?: "",
                        cookingTime = 30, // Default value since it's not in the schema
                        nutritionFacts = recipe.recipe_nutrition?.firstOrNull()?.let { nutrition ->
                            FavoriteRecipe.NutritionFacts(
                                calories = nutrition.calories ?: 0,
                                protein = nutrition.protein?.toInt() ?: 0,
                                carbs = nutrition.carbs?.toInt() ?: 0,
                                fat = nutrition.fat?.toInt() ?: 0
                            )
                        } ?: FavoriteRecipe.NutritionFacts(0, 0, 0, 0)
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching favorites: ${e.message}", e)
            throw e
        }
    }
} 