package com.thenewkenya.ingrediet.data.mealplan

import android.util.Log
import com.thenewkenya.ingrediet.data.model.DetailedRecipe
import com.thenewkenya.ingrediet.data.model.RecipeDto
import com.thenewkenya.ingrediet.data.network.supabase
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * A standalone module for meal plan generation that directly uses Supabase SDK
 */
class MealPlanGenerator {
    companion object {
        private const val TAG = "MealPlanGenerator"
        
        /**
         * Generates a meal plan with specific parameters using direct Supabase SDK calls
         * 
         * @param calorieTarget The target calorie intake per day
         * @param days The number of days to generate a plan for
         * @param dietaryPreferences Optional dietary preferences/restrictions
         * @return A Result containing either the meal plan or an exception
         */
        suspend fun generateMealPlan(
            calorieTarget: Int = 2000,
            days: Int = 7,
            dietaryPreferences: List<String> = emptyList()
        ): Result<Map<String, List<DetailedRecipe>>> = withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Generating meal plan for $days days with $calorieTarget calories")
                
                // Get recipes directly from Supabase
                val recipes = fetchRecipesFromSupabase(
                    count = days * 3, 
                    dietaryPreferences = dietaryPreferences
                )
                
                if (recipes.isEmpty()) {
                    Log.e(TAG, "No recipes available for meal plan")
                    return@withContext Result.failure(IllegalStateException("No recipes available for meal plan"))
                }
                
                // Create the meal plan
                val mealPlan = HashMap<String, List<DetailedRecipe>>()
                
                // Distribute recipes by day
                for (day in 1..days) {
                    val dayKey = "Day $day"
                    val dayRecipes = if (recipes.size >= 3) {
                        recipes.shuffled().take(3)
                    } else {
                        recipes.toList()
                    }
                    
                    mealPlan[dayKey] = dayRecipes
                }
                
                Log.d(TAG, "Successfully generated meal plan with ${mealPlan.size} days")
                Result.success(mealPlan)
            } catch (e: Exception) {
                Log.e(TAG, "Error generating meal plan: ${e.message}", e)
                Result.failure(e)
            }
        }
        
        /**
         * Fetches recipes directly from Supabase without using Repository
         */
        private suspend fun fetchRecipesFromSupabase(
            count: Int, 
            dietaryPreferences: List<String>
        ): List<DetailedRecipe> = withContext(Dispatchers.IO) {
            try {
                // Build query based on dietary preferences if provided
                val dietType = if (dietaryPreferences.isNotEmpty()) {
                    dietaryPreferences.first().lowercase()
                } else {
                    "balanced" 
                }
                
                Log.d(TAG, "Fetching recipes with diet type: $dietType")
                
                // Direct Supabase query to get recipes
                val recipeDtos = try {
                    if (dietType != "balanced") {
                        supabase.from("recipes")
                            .select {
                                filter {
                                    ilike("tags", "%$dietType%")
                                }
                                limit(count.toLong() * 2) // Get more than we need for variety
                            }
                            .decodeList<RecipeDto>()
                    } else {
                        // Just get random recipes if no specific diet
                        supabase.from("recipes")
                            .select {
                                limit(count.toLong() * 2)
                            }
                            .decodeList<RecipeDto>()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error fetching recipes from Supabase: ${e.message}", e)
                    // Try fallback without filtering
                    supabase.from("recipes")
                        .select {
                            limit(count.toLong() * 2)
                        }
                        .decodeList<RecipeDto>()
                }
                
                // Convert to DetailedRecipe objects
                val recipes = recipeDtos
                    .shuffled() // Randomize order
                    .take(count.coerceAtMost(recipeDtos.size)) // Take as many as we have up to count
                    .mapNotNull { dto ->
                        try {
                            dto.toDetailedRecipe()
                        } catch (e: Exception) {
                            Log.e(TAG, "Error converting RecipeDto: ${e.message}")
                            null
                        }
                    }
                
                Log.d(TAG, "Fetched ${recipes.size} recipes for meal plan")
                recipes
            } catch (e: Exception) {
                Log.e(TAG, "Error in fetchRecipesFromSupabase: ${e.message}", e)
                emptyList()
            }
        }
    }
} 