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
                // Separate diet type from allergies
                val knownDietTypes = listOf("vegetarian", "vegan", "low-carb", "high-protein", "keto", "paleo", "mediterranean")
                val dietType = dietaryPreferences.find { pref -> 
                    knownDietTypes.contains(pref.lowercase().replace("-", "").replace(" ", ""))
                }?.lowercase()?.replace("-", "")?.replace(" ", "") ?: "balanced"
                
                val allergies = dietaryPreferences.filter { pref -> 
                    !knownDietTypes.contains(pref.lowercase().replace("-", "").replace(" ", ""))
                }
                
                Log.d(TAG, "Fetching recipes with diet type: $dietType, allergies to avoid: $allergies")
                
                // Start with a larger pool of recipes
                val recipeDtos = try {
                    if (dietType != "balanced") {
                        // Try with diet filtering first, but be more lenient
                        val dietFiltered = supabase.from("recipes")
                            .select {
                                filter {
                                    or {
                                        ilike("tags", "%$dietType%")
                                        ilike("cuisine_type", "%$dietType%")
                                        ilike("description", "%$dietType%")
                                    }
                                }
                                limit(count.toLong() * 3) // Get even more for filtering
                            }
                            .decodeList<RecipeDto>()
                        
                        // If diet filtering returns too few results, fallback to all recipes
                        if (dietFiltered.size < count) {
                            Log.d(TAG, "Diet filtering returned only ${dietFiltered.size} recipes, fetching more...")
                            supabase.from("recipes")
                                .select {
                                    limit(count.toLong() * 4) // Get many more for allergy filtering
                                }
                                .decodeList<RecipeDto>()
                        } else {
                            dietFiltered
                        }
                    } else {
                        // Get random recipes
                        supabase.from("recipes")
                            .select {
                                limit(count.toLong() * 4) // Get many more for allergy filtering
                            }
                            .decodeList<RecipeDto>()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error fetching recipes from Supabase: ${e.message}", e)
                    // Try fallback without filtering
                    supabase.from("recipes")
                        .select {
                            limit(count.toLong() * 4)
                        }
                        .decodeList<RecipeDto>()
                }
                
                // Filter out recipes containing allergens
                val filteredRecipes = if (allergies.isNotEmpty()) {
                    Log.d(TAG, "Filtering out recipes with allergies: $allergies")
                    recipeDtos.filter { recipe ->
                        val recipeText = "${recipe.name} ${recipe.description} ${recipe.ingredients ?: ""} ${recipe.tags ?: ""}".lowercase()
                        
                        // Check if recipe contains any allergens
                        val containsAllergen = allergies.any { allergen ->
                            when (allergen.lowercase()) {
                                "gluten" -> recipeText.contains("wheat") || recipeText.contains("flour") || 
                                           recipeText.contains("bread") || recipeText.contains("pasta") ||
                                           recipeText.contains("gluten")
                                "nuts" -> recipeText.contains("nuts") || recipeText.contains("almonds") || 
                                         recipeText.contains("peanuts") || recipeText.contains("cashews") ||
                                         recipeText.contains("walnuts") || recipeText.contains("pecans")
                                "dairy" -> recipeText.contains("milk") || recipeText.contains("cheese") || 
                                          recipeText.contains("butter") || recipeText.contains("cream") ||
                                          recipeText.contains("yogurt") || recipeText.contains("dairy")
                                "eggs" -> recipeText.contains("egg") || recipeText.contains("eggs")
                                "shellfish" -> recipeText.contains("shrimp") || recipeText.contains("crab") || 
                                              recipeText.contains("lobster") || recipeText.contains("shellfish")
                                "fish" -> recipeText.contains("fish") || recipeText.contains("salmon") || 
                                         recipeText.contains("tuna") || recipeText.contains("cod")
                                "soy" -> recipeText.contains("soy") || recipeText.contains("tofu")
                                else -> recipeText.contains(allergen.lowercase())
                            }
                        }
                        !containsAllergen // Keep recipes that don't contain allergens
                    }
                } else {
                    recipeDtos
                }
                
                Log.d(TAG, "After allergy filtering: ${filteredRecipes.size} recipes available (from ${recipeDtos.size} original)")
                
                // Convert to DetailedRecipe objects
                val recipes = filteredRecipes
                    .shuffled() // Randomize order
                    .take(count.coerceAtMost(filteredRecipes.size)) // Take as many as we have up to count
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