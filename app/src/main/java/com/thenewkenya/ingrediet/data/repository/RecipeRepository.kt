package com.thenewkenya.ingrediet.data.repository

import android.util.Log
import com.thenewkenya.ingrediet.data.model.DetailedRecipe
import com.thenewkenya.ingrediet.data.model.IngredientItem
import com.thenewkenya.ingrediet.data.model.NutritionFacts
import com.thenewkenya.ingrediet.data.network.supabase
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class RecipeRepository {

    suspend fun getRecipeDetails(recipeId: Int): Flow<Result<DetailedRecipe>> = flow {
        try {
            // In the real implementation, fetch this from Supabase
            // For now, we'll use a mock implementation
            val recipe = getMockRecipe(recipeId)
            emit(Result.success(recipe))

            // Example Supabase implementation (commented out)
            /*
            val response = supabase.from("recipes")
                .select {
                    filter { eq("id", recipeId) }
                }
                .decodeSingle<RecipeDto>()

            // Convert DTO to domain model
            val recipe = mapRecipeDtoToDetailedRecipe(response)
            emit(Result.success(recipe))
            */
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Error fetching recipe details", e)
            emit(Result.failure(e))
        }
    }

    suspend fun toggleFavorite(recipeId: Int, isFavorite: Boolean): Flow<Result<Boolean>> = flow {
        try {
            // In the real implementation, update this in Supabase
            // For now, we'll simulate success
            emit(Result.success(true))

            // Example Supabase implementation (commented out)
            /*
            supabase.from("user_favorites")
                .upsert(
                    {
                        set("user_id", supabase.auth.currentUserOrNull()?.id ?: "")
                        set("recipe_id", recipeId)
                        set("is_favorite", isFavorite)
                    }
                )
            emit(Result.success(true))
            */
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Error toggling favorite", e)
            emit(Result.failure(e))
        }
    }

    // Mock implementation for preview purposes
    private fun getMockRecipe(recipeId: Int): DetailedRecipe {
        return DetailedRecipe(
            id = recipeId,
            name = "Avocado Toast with Poached Egg",
            description = "A nutritious breakfast that's quick to prepare and packed with healthy fats and protein.",
            imageUrl = "https://images.unsplash.com/photo-1525351484163-7529414344d8?ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&ixlib=rb-1.2.1&auto=format&fit=crop&w=1000&q=80",
            preparationTime = 5,
            cookingTime = 10,
            servings = 1,
            difficulty = "Easy",
            ingredients = listOf(
                IngredientItem(1, "Whole grain bread", 1f, "slice"),
                IngredientItem(2, "Ripe avocado", 0.5f, "medium"),
                IngredientItem(3, "Egg", 1f, "large"),
                IngredientItem(4, "Salt", 0.25f, "tsp"),
                IngredientItem(5, "Black pepper", 0.25f, "tsp"),
                IngredientItem(6, "Lemon juice", 1f, "tsp"),
                IngredientItem(7, "Red pepper flakes", 0.25f, "tsp"),
                IngredientItem(8, "Olive oil", 1f, "tsp")
            ),
            instructions = listOf(
                "Toast the bread slice until golden and crispy.",
                "Cut the avocado in half, remove the pit, and scoop out the flesh into a bowl.",
                "Mash the avocado with a fork, add salt, pepper, and lemon juice. Mix well.",
                "Bring a small pot of water to a gentle simmer. Add a dash of vinegar.",
                "Crack the egg into a small bowl, then gently slide it into the simmering water.",
                "Poach for 3-4 minutes for a runny yolk.",
                "Spread the mashed avocado on the toast.",
                "Remove the poached egg with a slotted spoon, drain, and place on top of the avocado.",
                "Sprinkle with red pepper flakes, a drizzle of olive oil, and extra salt and pepper to taste."
            ),
            nutritionFacts = NutritionFacts(
                calories = 320,
                protein = 14.5f,
                carbs = 25.0f,
                fat = 18.5f,
                fiber = 6.0f,
                sugar = 2.5f
            ),
            tags = listOf("Breakfast", "High Protein", "Vegetarian", "Quick"),
            isFavorite = false
        )
    }

    // DTO and mapping logic would go here in the real implementation
}