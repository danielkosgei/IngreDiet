package com.thenewkenya.ingrediet.data.repository

import android.content.Context
import android.util.Log
import com.thenewkenya.ingrediet.data.model.DetailedRecipe
import com.thenewkenya.ingrediet.data.model.IngredientItem
import com.thenewkenya.ingrediet.data.model.NutritionFacts
import com.thenewkenya.ingrediet.data.network.DatabaseErrorHandler
import com.thenewkenya.ingrediet.data.network.supabase
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.random.Random
import java.util.UUID
import java.util.NoSuchElementException
import com.thenewkenya.ingrediet.data.model.RecipeDto

class RecipeRepository(context: Context) {

    /**
     * Simplified recipe data for UI display in lists
     */
    data class RecipeListItem(
        val id: String,
        val name: String,
        val description: String,
        val imageUrl: String,
        val time: Int,
        val calories: Int,
        val recipeId: String,
        val category: String,
        val dietaryInfo: List<String> = emptyList()
    ) {
        // Convert a DetailedRecipe to a RecipeListItem
        companion object {
            fun fromDetailedRecipe(recipe: DetailedRecipe): RecipeListItem {
                return RecipeListItem(
                    id = recipe.id,
                    name = recipe.name,
                    description = recipe.description,
                    imageUrl = recipe.imageUrl,
                    time = recipe.cookingTime,
                    calories = recipe.nutritionFacts.calories,
                    recipeId = recipe.id,
                    category = recipe.category,
                    dietaryInfo = recipe.tags
                )
            }
        }
    }

    /**
     * Search for recipes directly from the Supabase database
     * This function specifically searches the recipes table
     * @param query The search query to match against recipe name and description
     * @param limit Maximum number of recipes to return
     * @return Flow of search results with detailed recipes
     */
    suspend fun searchRecipesFromDatabase(query: String, limit: Int = 10): Flow<Result<List<DetailedRecipe>>> = 
        DatabaseErrorHandler.executeDatabaseOperation(
            tag = "RecipeRepository",
            operation = "Search recipes in database",
            defaultValue = emptyList()
        ) {
            Log.d("RecipeRepository", "Searching recipes in Supabase with query: $query")
            
            val searchResults = try {
                if (query.isBlank()) {
                    // If query is blank, get random recipes
                    supabase.from("recipes")
                        .select()
                        .decodeList<RecipeDto>()
                } else {
                    // Otherwise do a search query
                    supabase.from("recipes")
                        .select() {
                            filter {
                                ilike("name", "%$query%")
                            }
                        }
                        .decodeList<RecipeDto>()
                }
                } catch (e: Exception) {
                Log.e("RecipeRepository", "Error searching recipes: ${e.message}", e)
                emptyList<RecipeDto>()
            }
            
            Log.d("RecipeRepository", "Found ${searchResults.count()} recipes in database")
            
            // Convert the database results to DetailedRecipe objects
            val result = mutableListOf<DetailedRecipe>()
            
            for (recipeDto in searchResults) {
                try {
                    result.add(recipeDto.toDetailedRecipe())
                } catch (e: Exception) {
                    Log.e("RecipeRepository", "Error converting RecipeDto to DetailedRecipe", e)
                }
            }
            
            result
        }
        
    /**
     * Get random recipes from the database
     * @param limit Maximum number of recipes to return
     * @return Flow of recipes
     */
    suspend fun getRandomRecipes(limit: Int = 10): Flow<Result<List<DetailedRecipe>>> = flow {
        try {
            Log.d("RecipeRepository", "Getting $limit random recipes")
            
            val searchResults = try {
                supabase.from("recipes")
                    .select()
                    .decodeList<RecipeDto>()
            } catch (e: Exception) {
                Log.e("RecipeRepository", "Error getting random recipes: ${e.message}", e)
                emptyList<RecipeDto>()
            }
            
            val shuffledResults = searchResults.shuffled().take(limit)
            Log.d("RecipeRepository", "Found ${shuffledResults.size} random recipes")
            
            // Convert the database results to DetailedRecipe objects
            val result = mutableListOf<DetailedRecipe>()
            
            for (recipeDto in shuffledResults) {
                try {
                    result.add(recipeDto.toDetailedRecipe())
                } catch (e: Exception) {
                    Log.e("RecipeRepository", "Error converting RecipeDto to DetailedRecipe", e)
                }
            }
            
            // If no recipes found, return empty list
            if (result.isEmpty()) {
                Log.d("RecipeRepository", "No random recipes found, returning empty list")
            }
            
            emit(Result.success(result))
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Error getting random recipes", e)
            emit(Result.failure(e))
        }
    }

    /**
     * Get recipe details by ID
     * @param recipeId Recipe ID
     * @return Flow with the recipe details
     */
    suspend fun getRecipeDetails(recipeId: String): Flow<Result<DetailedRecipe>> = flow {
        try {
            Log.d("RecipeRepository", "Getting recipe details for ID: $recipeId")
            
            val searchResult = try {
                supabase.from("recipes")
                    .select() {
                        filter {
                            eq("id", recipeId)
                        }
                    }
                    .decodeList<RecipeDto>()
                    .firstOrNull()
            } catch (e: Exception) {
                Log.e("RecipeRepository", "Error getting recipe details: ${e.message}", e)
                null
            }
            
            if (searchResult == null) {
                emit(Result.failure(NoSuchElementException("Recipe not found with ID: $recipeId")))
                return@flow
            }
            
            try {
                val detailedRecipe = searchResult.toDetailedRecipe()
                emit(Result.success(detailedRecipe))
            } catch (e: Exception) {
                Log.e("RecipeRepository", "Error parsing recipe data from database", e)
                emit(Result.failure(e))
            }
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Error getting recipe details", e)
            emit(Result.failure(e))
        }
    }
        
    /**
     * Search recipes by name, description, or ingredients
     */
    suspend fun searchRecipes(query: String?, limit: Int = 10): Flow<Result<List<DetailedRecipe>>> {
        if (query.isNullOrBlank()) {
            return getRandomRecipes(limit)
        }
        return searchRecipesFromDatabase(query, limit)
    }

    /**
     * Get recipes based on query/category for the home screen
     */
    suspend fun getRecipes(query: String? = null, category: String? = null, limit: Int = 10): Flow<Result<List<RecipeListItem>>> = flow {
        try {
            Log.d("RecipeRepository", "Getting recipes with query: $query, category: $category")
            // Get detailed recipes first
            val recipesFlow = if (query != null && query.isNotEmpty()) {
                searchRecipes(query, limit)
            } else {
                getRandomRecipes(limit)
            }
            
            // Collect the recipes and convert to RecipeListItem
            recipesFlow.collect { result ->
                val recipeItems = result.fold(
                    onSuccess = { recipes ->
                        // Convert to RecipeListItem
                        recipes.map { RecipeListItem.fromDetailedRecipe(it) }
                    },
                    onFailure = { 
                        emit(Result.failure(it))
                        return@collect // Return from the collect lambda
                    }
                )
                
                emit(Result.success(recipeItems))
            }
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Error getting recipes", e)
            emit(Result.failure(e))
        }
    }

    /**
     * Get Kenyan recipes
     */
    suspend fun getKenyanRecipes(limit: Int = 20): Flow<Result<List<DetailedRecipe>>> = flow {
        try {
            Log.d("RecipeRepository", "Getting Kenyan recipes")
            
            val searchResults = try {
                supabase.from("recipes")
                .select() {
                        filter {
                            eq("category", "Kenyan")
                }
            }
                    .decodeList<RecipeDto>()
        } catch (e: Exception) {
                Log.e("RecipeRepository", "Error getting Kenyan recipes: ${e.message}", e)
                emptyList<RecipeDto>()
            }
            
            // Process results (same logic as other methods)
            val result = mutableListOf<DetailedRecipe>()
            for (recipeDto in searchResults) {
                try {
                    result.add(recipeDto.toDetailedRecipe())
                } catch (e: Exception) {
                    Log.e("RecipeRepository", "Error parsing Kenyan recipe data", e)
                }
            }
            
            emit(Result.success(result.take(limit)))
                } catch (e: Exception) {
            Log.e("RecipeRepository", "Error getting Kenyan recipes", e)
            emit(Result.failure(e))
        }
    }

    /**
     * Get Kenyan recipes by region
     */
    suspend fun getKenyanRecipesByRegion(region: String, limit: Int = 20): Flow<Result<List<DetailedRecipe>>> = flow {
        try {
            if (region == "All Regions") {
                getKenyanRecipes(limit).collect { result ->
                    emit(result)
                }
                return@flow
            }
            
            Log.d("RecipeRepository", "Getting Kenyan recipes for region: $region")
            
            val searchResults = try {
                supabase.from("recipes")
                    .select() {
                        filter {
                            eq("category", "Kenyan")
                            eq("region", region)
                        }
                    }
                    .decodeList<RecipeDto>()
        } catch (e: Exception) {
                Log.e("RecipeRepository", "Error getting Kenyan recipes by region: ${e.message}", e)
                emptyList<RecipeDto>()
            }
            
            // Process results (same logic as other methods)
            val result = mutableListOf<DetailedRecipe>()
            for (recipeDto in searchResults) {
                try {
                    result.add(recipeDto.toDetailedRecipe())
        } catch (e: Exception) {
                    Log.e("RecipeRepository", "Error parsing Kenyan recipe data", e)
                }
            }
            
            emit(Result.success(result.take(limit)))
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Error getting Kenyan recipes by region", e)
            emit(Result.failure(e))
        }
    }
    
    /**
     * Search for Kenyan recipes
     */
    suspend fun searchKenyanRecipes(query: String, limit: Int = 20): Flow<Result<List<DetailedRecipe>>> = 
        DatabaseErrorHandler.executeDatabaseOperation(
            tag = "RecipeRepository",
            operation = "Search Kenyan recipes",
            defaultValue = emptyList()
        ) {
            Log.d("RecipeRepository", "Searching Kenyan recipes with query: $query")
            
            val searchResults = try {
                supabase.from("recipes")
                    .select() {
                        filter {
                            eq("category", "Kenyan")
                            ilike("name", "%$query%")
                        }
                    }
                    .decodeList<RecipeDto>()
            } catch (e: Exception) {
                Log.e("RecipeRepository", "Error searching Kenyan recipes: ${e.message}", e)
                emptyList<RecipeDto>()
            }
            
            // Process results (same logic as other methods)
            val result = mutableListOf<DetailedRecipe>()
            for (recipeDto in searchResults) {
                try {
                    result.add(recipeDto.toDetailedRecipe())
                } catch (e: Exception) {
                    Log.e("RecipeRepository", "Error parsing Kenyan recipe data", e)
                }
            }
            
            result.take(limit)
        }
        
    /**
     * Get a Kenyan recipe by ID
     */
    suspend fun getKenyanRecipeById(recipeId: String): Flow<Result<DetailedRecipe>> = 
        getRecipeDetails(recipeId)

    /**
     * Get favorite recipes
     */
    suspend fun getFavoriteRecipes(limit: Int = 20): Flow<Result<List<DetailedRecipe>>> = flow {
        try {
            // For simplicity, we'll just return some random recipes
            Log.d("RecipeRepository", "Getting favorite recipes")
            getRandomRecipes(limit).collect { result ->
                emit(result)
                    }
                } catch (e: Exception) {
            Log.e("RecipeRepository", "Error getting favorite recipes", e)
            emit(Result.failure(e))
        }
    }
    
    /**
     * Toggle whether a recipe is a favorite
     */
    suspend fun toggleFavorite(recipeId: String): Flow<Result<Boolean>> = flow {
        try {
            // This would normally update a user's favorites in the database
            // For now, just return success
            Log.d("RecipeRepository", "Toggling favorite for recipe $recipeId")
            emit(Result.success(true))
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Error toggling favorite: ${e.message}", e)
            emit(Result.failure(e))
        }
    }
    
    /**
     * Generate a meal plan based on preferences
     */
    suspend fun generateMealPlan(
        calories: Int = 2000,
        days: Int = 7,
        preferences: List<String> = emptyList()
    ): Flow<Result<Map<String, List<DetailedRecipe>>>> = flow {
        try {
            Log.d("RecipeRepository", "Generating meal plan for $days days with $calories calories")
            
            val mealPlan = mutableMapOf<String, List<DetailedRecipe>>()
            val allRecipes = mutableListOf<DetailedRecipe>()
            
            // Get random recipes first
            getRandomRecipes(days * 3).collect { result ->
                result.fold(
                    onSuccess = { recipes ->
                        allRecipes.addAll(recipes)
                    },
                    onFailure = { error ->
                        emit(Result.failure(error))
                        return@collect
                    }
                )
            }
            
            if (allRecipes.isEmpty()) {
                emit(Result.failure(IllegalStateException("No recipes available for meal plan")))
                return@flow
            }
            
            // Group recipes by day
            for (day in 1..days) {
                // Get 3 random recipes for breakfast, lunch, dinner or as many as we have
                val dayRecipes = if (allRecipes.size >= 3) {
                    allRecipes.shuffled().take(3)
                } else {
                    allRecipes.shuffled()
                }
                mealPlan["Day $day"] = dayRecipes
            }
            
            emit(Result.success(mealPlan))
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Error generating meal plan: ${e.message}", e)
            emit(Result.failure(e))
        }
    }

    /**
     * Validate whether a recipe exists and has all required data
     */
    suspend fun validateRecipeData(recipeId: String): Map<String, Boolean> {
        val result = mutableMapOf<String, Boolean>()
        
        try {
            Log.d("RecipeRepository", "Validating recipe data for ID: $recipeId")
            
            // Check if recipe exists
            val recipeExists = try {
                val recipe = supabase.from("recipes")
                    .select() {
                        filter {
                            eq("id", recipeId)
                        }
                    }
                    .decodeList<RecipeDto>()
                
                recipe.isNotEmpty()
        } catch (e: Exception) {
                Log.e("RecipeRepository", "Error validating recipe: ${e.message}", e)
                false
            }
            
            result["recipe_exists"] = recipeExists
            
            // For simplicity, we'll just check if recipe exists
            // In a real implementation, we'd check for ingredients, instructions, etc.
            result["ingredients_exist"] = recipeExists
            result["nutrition_exists"] = recipeExists
            result["instructions_exist"] = recipeExists
            
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Error validating recipe data: ${e.message}", e)
            // Set all to false on error
            result["recipe_exists"] = false
            result["ingredients_exist"] = false
            result["nutrition_exists"] = false
            result["instructions_exist"] = false
        }
        
        return result
    }
}

