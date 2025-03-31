package com.thenewkenya.ingrediet.data.repository

import android.content.Context
import android.util.Log
import com.thenewkenya.ingrediet.data.model.DetailedRecipe
import com.thenewkenya.ingrediet.data.model.IngredientItem
import com.thenewkenya.ingrediet.data.model.NutritionFacts
import com.thenewkenya.ingrediet.data.model.Recipe
import com.thenewkenya.ingrediet.data.network.CacheManager
import com.thenewkenya.ingrediet.data.network.RecipeCacheService
import com.thenewkenya.ingrediet.data.network.api.KenyanFoodsService
import com.thenewkenya.ingrediet.data.network.api.OpenFoodFactsService
import com.thenewkenya.ingrediet.data.network.api.RecipeService
import com.thenewkenya.ingrediet.data.network.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlin.collections.*
import java.util.concurrent.TimeUnit
import java.time.DayOfWeek
import kotlin.random.Random
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.CancellationException

class RecipeRepository(context: Context) {
    private val cacheManager = CacheManager(context)
    private val recipeService = RecipeService(context)
    private val openFoodFactsService = OpenFoodFactsService()
    private val kenyanFoodsService = KenyanFoodsService()
    private val recipeCacheService by lazy { RecipeCacheService(context) }
    
    // Schedule periodic cache cleanup
    init {
        // Run cache cleanup once a day, but not blocking the main thread
        val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())
        mainHandler.postDelayed({
            // Use a proper coroutine scope for cleanup instead of runBlocking
            GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    cacheManager.cleanupCache()
                } catch (e: Exception) {
                    Log.e("RecipeRepository", "Error cleaning up cache: ${e.message}", e)
                }
            }
        }, TimeUnit.DAYS.toMillis(1))
    }

    suspend fun validateRecipeData(recipeId: Int): Map<String, Boolean> {
        val result = mutableMapOf<String, Boolean>()

        try {
            // Log the process
            Log.d("RecipeRepository", "Validating recipe data for ID: $recipeId")

            // Check if recipe exists
            val recipeExists = try {
                val recipeList = supabase.from("recipes")
                    .select(columns = Columns.list("id")) {
                        filter { eq("id", recipeId) }
                    }
                    .decodeList<IdDto>() // Use our serializable DTO

                val exists = recipeList.isNotEmpty()
                Log.d("RecipeRepository", "Recipe $recipeId exists check: exists = $exists")
                exists
            } catch (e: Exception) {
                Log.e("RecipeRepository", "Error checking if recipe exists: ${e.message}", e)
                false
            }
            result["recipe_exists"] = recipeExists

            // Only check other data if recipe exists
            if (recipeExists) {
                // Check if recipe_ingredients entries exist
                val ingredientsExist = try {
                    val ingredientsList = supabase.from("recipe_ingredients")
                        .select(columns = Columns.list("id")) {
                            filter { eq("recipe_id", recipeId) }
                        }
                        .decodeList<IdDto>() // Use our serializable DTO

                    val exists = ingredientsList.isNotEmpty()
                    Log.d("RecipeRepository", "Recipe $recipeId ingredients check: exists = $exists")
                    exists
                } catch (e: Exception) {
                    Log.e("RecipeRepository", "Error checking ingredients: ${e.message}", e)
                    false
                }
                result["ingredients_exist"] = ingredientsExist

                // Check nutrition
                val nutritionExists = try {
                    val nutritionList = supabase.from("recipe_nutrition")
                        .select(columns = Columns.list("id")) {
                            filter { eq("recipe_id", recipeId) }
                        }
                        .decodeList<IdDto>() // Use our serializable DTO

                    val exists = nutritionList.isNotEmpty()
                    Log.d("RecipeRepository", "Recipe $recipeId nutrition check: exists = $exists")
                    exists
                } catch (e: Exception) {
                    Log.e("RecipeRepository", "Error checking nutrition: ${e.message}", e)
                    false
                }
                result["nutrition_exists"] = nutritionExists

                // Check instructions
                val instructionsExist = try {
                    val instructionsList = supabase.from("recipe_instructions")
                        .select(columns = Columns.list("id")) {
                            filter { eq("recipe_id", recipeId) }
                        }
                        .decodeList<IdDto>() // Use our serializable DTO

                    val exists = instructionsList.isNotEmpty()
                    Log.d("RecipeRepository", "Recipe $recipeId instructions check: exists = $exists")
                    exists
                } catch (e: Exception) {
                    Log.e("RecipeRepository", "Error checking instructions: ${e.message}", e)
                    false
                }
                result["instructions_exist"] = instructionsExist
            } else {
                // If recipe doesn't exist, other data doesn't exist either
                result["ingredients_exist"] = false
                result["nutrition_exists"] = false
                result["instructions_exist"] = false
            }

            Log.d("RecipeRepository", "Validation results for recipe $recipeId: $result")
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

    suspend fun getRecipeDetails(recipeId: Int): Flow<Result<DetailedRecipe>> = flow {
        Log.d("RecipeRepository", "Fetching recipe details for ID: $recipeId")
        
        // Check if recipe is in local cache first
        val cachedRecipe = cacheManager.getCachedRecipe(recipeId)
        if (cachedRecipe != null) {
            Log.d("RecipeRepository", "Found recipe $recipeId in local cache")
            emit(Result.success(cachedRecipe))
            return@flow
        }

        // If not in cache, try the IngreDiet API through RecipeCacheService
        Log.d("RecipeRepository", "Recipe not in cache, trying IngreDiet API via RecipeCacheService")
        
        val recipe = recipeCacheService.getAndCacheRecipeById(recipeId)
        
        if (recipe != null) {
            Log.d("RecipeRepository", "Found recipe $recipeId in Edge Function")
            emit(Result.success(recipe))
        } else {
            // If not found in Edge Function, try the recipe service directly
            val serviceRecipe = recipeService.getRecipeById(recipeId.toString())
            
            if (serviceRecipe != null) {
                Log.d("RecipeRepository", "Found recipe $recipeId in Recipe Service")
                // Cache the recipe locally
                cacheManager.cacheRecipe(serviceRecipe)
                emit(Result.success(serviceRecipe))
            } else {
                // If still not found, return a failure
                Log.e("RecipeRepository", "Recipe $recipeId not found anywhere")
                emit(Result.failure(NoSuchElementException("Recipe not found")))
            }
        }
    }
    
    /**
     * Store a recipe from API in Supabase for future use
     * This is now a no-op to avoid unnecessary Supabase traffic
     */
    suspend fun storeRecipeInSupabase(recipe: DetailedRecipe) {
        // No-op - we no longer store recipes in Supabase to reduce network traffic
        Log.d("RecipeRepository", "Recipe caching to Supabase disabled")
    }
    
    /**
     * Search for recipes using IngreDiet API
     * @param query Optional search query (or null for random recipes)
     * @param limit Maximum number of recipes to return
     * @return Flow of search results
     */
    suspend fun searchRecipes(query: String?, limit: Int = 10): Flow<Result<List<DetailedRecipe>>> = flow {
        try {
            Log.d("RecipeRepository", "Searching recipes with query: $query, limit: $limit")
            
            // Try to get recipes from local cache first
            var recipesFound = false
            val cachedResults = if (query != null) {
                cacheManager.getCachedRecipesByQuery(query)
            } else {
                cacheManager.getRandomCachedRecipes(limit)
            }
            
            if (cachedResults.isNotEmpty()) {
                Log.d("RecipeRepository", "Found ${cachedResults.size} recipes in local cache")
                emit(Result.success(cachedResults))
                recipesFound = true
            }
            
            // Then use the recipe service which now uses the IngreDiet API
            try {
                val results = if (query != null) {
                    recipeService.searchRecipes(query)
                        .catch { e ->
                            if (e is CancellationException) {
                                Log.d("RecipeRepository", "Recipe search cancelled")
                                throw e
                            } else {
                                Log.e("RecipeRepository", "Error searching recipes: ${e.message}", e)
                                // Don't emit from catch to avoid flow transparency violation
                            }
                        }
                        .first()
                } else {
                    recipeService.getRandomRecipes(limit)
                        .catch { e ->
                            if (e is CancellationException) {
                                Log.d("RecipeRepository", "Random recipe fetch cancelled")
                                throw e
                            } else {
                                Log.e("RecipeRepository", "Error getting random recipes: ${e.message}", e)
                                // Don't emit from catch to avoid flow transparency violation
                            }
                        }
                        .first()
                }
                
                if (results.isNotEmpty()) {
                    Log.d("RecipeRepository", "Found ${results.size} recipes from IngreDiet API")
                    
                    // Cache the results
                    results.forEach { recipe ->
                        cacheManager.cacheRecipe(recipe)
                    }
                    
                    // Emit the new results
                    emit(Result.success(results))
                    recipesFound = true
                } else if (!recipesFound) {
                    // Only emit empty list if we haven't already emitted cached results
                    Log.d("RecipeRepository", "No recipes found from any source")
                    emit(Result.success(emptyList()))
                }
            } catch (e: CancellationException) {
                Log.d("RecipeRepository", "Recipe search was cancelled")
                throw e
            } catch (e: Exception) {
                Log.e("RecipeRepository", "Error in searchRecipes API call: ${e.message}", e)
                // Only emit if we haven't already emitted cached results
                if (!recipesFound) {
                    emit(Result.success(emptyList()))
                }
            }
        } catch (e: CancellationException) {
            Log.d("RecipeRepository", "Recipe search was cancelled")
            throw e
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Error in searchRecipes: ${e.message}", e)
            emit(Result.failure(e))
        }
    }
    
    /**
     * Get ingredient details by name
     * First checks Supabase, then Open Food Facts API
     */
    suspend fun getIngredientByName(name: String): Flow<Result<IngredientItem>> = flow {
        try {
            Log.d("RecipeRepository", "Searching for ingredient: $name")
            
            // First check Supabase
            val ingredientList = supabase.from("ingredients")
                .select() {
                    filter { ilike("name", "%$name%") }
                    limit(1)
                }
                .decodeList<IngredientDto>()
                
            if (ingredientList.isNotEmpty()) {
                Log.d("RecipeRepository", "Found ingredient in Supabase")
                val ingredientData = ingredientList.first()
                
                // Create a basic ingredient item
                val ingredient = IngredientItem(
                    id = ingredientData.id,
                    name = ingredientData.name,
                    quantity = 1f,
                    unit = "unit",
                    imageUrl = ""
                )
                
                emit(Result.success(ingredient))
                return@flow
            }
            
            // If not in Supabase, search Open Food Facts API
            val apiResults = openFoodFactsService.searchIngredients(name)
            
            if (apiResults.isNotEmpty()) {
                Log.d("RecipeRepository", "Found ingredient in Open Food Facts API")
                val ingredient = apiResults.first()
                
                // Cache the ingredient
                cacheManager.cacheIngredient(ingredient)
                
                // Store in Supabase for future use
                try {
                    supabase.from("ingredients")
                        .insert(
                            IngredientDto(
                                id = ingredient.id,
                                name = ingredient.name
                            )
                        )
                } catch (e: Exception) {
                    Log.e("RecipeRepository", "Error storing ingredient in Supabase: ${e.message}", e)
                    // Continue even if Supabase storage fails
                }
                
                emit(Result.success(ingredient))
            } else {
                // If not found anywhere, create a basic ingredient
                val basicIngredient = IngredientItem(
                    id = name.hashCode(),
                    name = name,
                    quantity = 1f,
                    unit = "unit"
                )
                
                emit(Result.success(basicIngredient))
            }
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Error searching for ingredient: ${e.message}", e)
            emit(Result.failure(e))
        }
    }

    suspend fun toggleFavorite(recipeId: Int, isFavorite: Boolean): Flow<Result<Boolean>> = flow {
        try {
            val currentUser = supabase.auth.currentUserOrNull()?.id ?: run {
                emit(Result.failure(Exception("User not authenticated")))
                return@flow
            }

            if (isFavorite) {
                // Add to favorites - use the object-based insert
                val favoriteData = UserFavoriteDto(
                    user_id = currentUser,
                    recipe_id = recipeId
                )

                supabase.from("user_favorites")
                    .insert(favoriteData)
            } else {
                // Remove from favorites
                supabase.from("user_favorites")
                    .delete {
                        filter {
                            eq("user_id", currentUser)
                            eq("recipe_id", recipeId)
                        }
                    }
            }

            emit(Result.success(true))
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Error toggling favorite", e)
            emit(Result.failure(e))
        }
    }

    /**
     * Get recipes for browsing (now using Supabase Edge Function)
     */
    suspend fun getRecipes(category: String? = null, limit: Int = 20, tags: List<String>? = null, query: String? = null): Flow<Result<List<RecipeListItem>>> = flow {
        Log.d("RecipeRepository", "Getting recipes: category=$category, tags=$tags, query=$query, limit=$limit")
        
        try {
            // Collect all recipes here
            val combinedRecipes = mutableListOf<RecipeListItem>()
            
            // First try to get recipes from the Edge Function via recipe service
            try {
                val recipeFlow = if (query != null) {
                    recipeService.searchRecipes(query)
                } else if (category != null) {
                    recipeService.getRecipesByCategory(category)
                } else if (tags != null && tags.isNotEmpty()) {
                    // Create an ingredients search if the tags are ingredient names
                    recipeService.getRecipesByIngredients(tags)
                } else {
                    recipeService.getRandomRecipes(limit)
                }
                
                recipeFlow.catch { e ->
                    Log.e("RecipeRepository", "Error getting recipes from Edge Function: ${e.message}", e)
                }.collect { recipes ->
                    recipes.forEach { recipe ->
                        // Add to combined results if not already there
                        if (combinedRecipes.none { it.id == recipe.id }) {
                            combinedRecipes.add(
                                RecipeListItem(
                                    id = recipe.id,
                                    name = recipe.name,
                                    imageUrl = recipe.imageUrl,
                                    time = "${recipe.preparationTime + recipe.cookingTime} min",
                                    calories = recipe.nutritionFacts.calories,
                                    category = recipe.category.ifEmpty { recipe.tags.firstOrNull() ?: "" },
                                    rating = recipe.rating,
                                    dietaryInfo = recipe.dietaryInfo.ifEmpty { recipe.tags }
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                // Check for cancellation
                if (e is kotlinx.coroutines.CancellationException) throw e
                
                Log.e("RecipeRepository", "Error getting recipes: ${e.message}", e)
            }
            
            if (combinedRecipes.isNotEmpty()) {
                Log.d("RecipeRepository", "Found ${combinedRecipes.size} recipes")
                emit(Result.success(combinedRecipes.take(limit)))
            } else {
                // Return at least an empty list
                Log.d("RecipeRepository", "No recipes found, returning empty list")
                emit(Result.success(emptyList()))
            }
        } catch (e: Exception) {
            // Check for cancellation
            if (e is kotlinx.coroutines.CancellationException) {
                Log.d("RecipeRepository", "getRecipes was cancelled")
                throw e
            }
            
            Log.e("RecipeRepository", "Error in getRecipes: ${e.message}", e)
            emit(Result.failure(e))
        }
    }

    // DTO classes for deserialization
    @Serializable
    private data class RecipeDto(
        val id: Int,
        val name: String,
        val description: String?,
        @SerialName("image_url") val image_url: String?,
        @SerialName("preparation_time") val preparation_time: Int?,
        @SerialName("cooking_time") val cooking_time: Int?,
        val servings: Int?,
        val difficulty: String?,
        val tags: List<String>?
    )

    @Serializable
    data class RecipeListItem(
        val id: Int,
        val name: String,
        val imageUrl: String,
        val time: String,
        val calories: Int,
        val category: String,
        val rating: Float = 0f,
        val dietaryInfo: List<String> = emptyList()
    )

    @Serializable
    private data class RecipeIngredientDto(
        val id: Int,
        @SerialName("recipe_id") val recipe_id: Int,
        @SerialName("ingredient_id") val ingredient_id: Int,
        val quantity: Float,
        val unit: String,
        //val ingredients: IngredientDto
    )

    @Serializable
    private data class IngredientDto(
        val id: Int,
        val name: String
    )

    @Serializable
    private data class RecipeInstructionDto(
        val id: Int,
        @SerialName("recipe_id") val recipe_id: Int,
        @SerialName("step_number") val step_number: Int,
        val instruction: String
    )

    @Serializable
    private data class RecipeNutritionDto(
        val id: Int,
        @SerialName("recipe_id") val recipe_id: Int,
        val calories: Int?,
        val protein: Float?,
        val carbs: Float?,
        val fat: Float?,
        val fiber: Float?,
        val sugar: Float?
    )

    @Serializable
    private data class RecipeListItemDto(
        val id: Int,
        val name: String,
        @SerialName("image_url") val imageUrl: String,
        @SerialName("preparation_time") val preparationTime: Int,
        @SerialName("cooking_time") val cookingTime: Int,
        val difficulty: String,
        val tags: List<String>
    )

    @Serializable
    private data class RecipeNutritionSimpleDto(
        @SerialName("recipe_id") val recipe_id: Int,
        val calories: Int
    )

    @Serializable
    private data class UserFavoriteDto(
        @SerialName("user_id") val user_id: String,
        @SerialName("recipe_id") val recipe_id: Int
    )

    @Serializable
    private data class NutritiondataDto(
        val calories: Int? = null
    )

    @Serializable
    private data class FavoriteDto(
        val id: Int
    )

    @Serializable
    private data class IdDto(
        val id: Int? = null
    )
    
    @Serializable
    private data class RecipeNameDto(
        val name: String
    )
    
    /**
     * Get search suggestions based on a query string
     * Returns a list of recipe name suggestions that match the query
     */
    suspend fun getSearchSuggestions(query: String): List<String> {
        if (query.length < 2) return emptyList()
        
        val suggestions = mutableListOf<String>()
        
        try {
            // First get suggestions from Supabase
            try {
                val supabaseResults = supabase.from("recipes")
                    .select(columns = Columns.list("name")) {
                        filter { ilike("name", "%$query%") }
                        limit(5)
                    }
                    .decodeList<RecipeNameDto>()
                    
                suggestions.addAll(supabaseResults.map { it.name })
            } catch (e: Exception) {
                Log.e("RecipeRepository", "Error getting suggestions from Supabase: ${e.message}", e)
                // Continue even if Supabase fails
            }
            
            // Get Kenyan food suggestions
            if (suggestions.size < 5) {
                try {
                    val kenyanSuggestions = kenyanFoodsService.getKenyanFoodSuggestions(query)
                        .take(5 - suggestions.size)
                    suggestions.addAll(kenyanSuggestions)
                } catch (e: Exception) {
                    Log.e("RecipeRepository", "Error getting Kenyan food suggestions: ${e.message}", e)
                    // Continue even if Kenyan foods service fails
                }
            }
            
            // If we still have fewer than 5 suggestions, try the international API
            if (suggestions.size < 5) {
                try {
                    val apiResults = recipeService.searchRecipes(query).first()
                    val apiSuggestions = apiResults.map { it.name }.take(5 - suggestions.size)
                    suggestions.addAll(apiSuggestions)
                } catch (e: Exception) {
                    Log.e("RecipeRepository", "Error getting suggestions from API: ${e.message}", e)
                }
            }
            
            // Add some common suggestions if we still have room
            if (suggestions.size < 5) {
                val commonSuggestions = listOf(
                    "Pasta", "Pizza", "Pancakes", "Chicken", "Beef", "Vegetarian",
                    "Salad", "Soup", "Breakfast", "Dessert", "Quick meal", "Healthy"
                ).filter { it.contains(query, ignoreCase = true) }
                    .take(5 - suggestions.size)
                
                suggestions.addAll(commonSuggestions)
            }
            
            return suggestions.distinct().take(5)
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Error getting search suggestions: ${e.message}", e)
            return emptyList()
        }
    }

    suspend fun getFavoriteRecipes(): List<DetailedRecipe> {
        try {
            // Get the current user
            val currentUser = supabase.auth.currentUserOrNull()
            if (currentUser == null) {
                Log.d("RecipeRepository", "No user logged in, returning empty favorites list")
                return emptyList()
            }

            // Fetch favorite recipes from Supabase
            val favoriteRecipes = supabase.from("user_favorites")
                .select() {
                    filter { eq("user_id", currentUser.id) }
                }
                .decodeList<FavoriteRecipeDto>()

            // Get the full recipe details for each favorite
            return favoriteRecipes.mapNotNull { favorite ->
                getRecipeDetails(favorite.recipeId).first().getOrNull()
            }
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Error fetching favorite recipes: ${e.message}", e)
            return emptyList()
        }
    }

    suspend fun addToFavorites(recipeId: Int) {
        try {
            val currentUser = supabase.auth.currentUserOrNull()
            if (currentUser == null) {
                Log.d("RecipeRepository", "Cannot add to favorites - no user logged in")
                return
            }

            supabase.from("user_favorites")
                .insert(FavoriteRecipeDto(
                    id = 0, // Will be auto-assigned
                    userId = currentUser.id,
                    recipeId = recipeId
                ))
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Error adding recipe to favorites: ${e.message}", e)
        }
    }

    suspend fun removeFromFavorites(recipeId: Int) {
        try {
            val currentUser = supabase.auth.currentUserOrNull()
            if (currentUser == null) {
                Log.d("RecipeRepository", "Cannot remove from favorites - no user logged in")
                return
            }

            supabase.from("user_favorites")
                .delete {
                    filter {
                        eq("user_id", currentUser.id)
                        eq("recipe_id", recipeId)
                    }
                }
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Error removing recipe from favorites: ${e.message}", e)
        }
    }

    @Serializable
    private data class FavoriteRecipeDto(
        val id: Int,
        @SerialName("user_id") val userId: String,
        @SerialName("recipe_id") val recipeId: Int
    )

    /**
     * Get random recipes from various sources
     * @param count Number of recipes to fetch
     */
    suspend fun getRandomRecipes(count: Int = 10): Flow<Result<List<DetailedRecipe>>> = flow {
        Log.d("RecipeRepository", "Fetching $count random recipes")
        
        try {
            // Try to get recipes from local cache first
            var recipesFound = false
            val cachedRecipes = cacheManager.getRandomCachedRecipes(count)
            
            if (cachedRecipes.isNotEmpty()) {
                Log.d("RecipeRepository", "Found ${cachedRecipes.size} random recipes in local cache")
                emit(Result.success(cachedRecipes))
                recipesFound = true
            }
            
            // Then try IngreDiet API through the cache service
            try {
                val ingreDietRecipes = recipeCacheService.getAndCacheRandomRecipes(count)
                    .catch { e ->
                        // Handle cancellation explicitly
                        if (e is CancellationException) {
                            Log.d("RecipeRepository", "Random recipe fetch cancelled")
                            throw e
                        } else {
                            Log.e("RecipeRepository", "Error fetching random recipes from API: ${e.message}", e)
                            // Don't emit here to avoid transparency violations
                        }
                    }
                    .first()
                
                if (ingreDietRecipes.isNotEmpty()) {
                    Log.d("RecipeRepository", "Found ${ingreDietRecipes.size} random recipes from API")
                    emit(Result.success(ingreDietRecipes))
                    recipesFound = true
                } else {
                    Log.d("RecipeRepository", "No recipes returned from API")
                }
            } catch (e: CancellationException) {
                Log.d("RecipeRepository", "Random recipe fetch was cancelled")
                throw e
            } catch (e: Exception) {
                Log.e("RecipeRepository", "Error fetching random recipes from API", e)
                // Don't emit here if we've already emitted something
            }
            
            // If we didn't find any recipes from any source, emit an empty list
            if (!recipesFound) {
                Log.d("RecipeRepository", "No random recipes found from any source")
                emit(Result.success(emptyList()))
            }
        } catch (e: CancellationException) {
            Log.d("RecipeRepository", "Random recipe fetch was cancelled")
            throw e
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Error in getRandomRecipes", e)
            emit(Result.failure(e))
        }
    }

    /**
     * Generates a meal plan based on user preferences
     * @param calorieTarget Daily calorie target
     * @param dietType Diet preference (e.g., "Low-carb", "High-protein", etc.)
     * @param days List of days to generate meal plan for (defaults to all days of the week)
     * @param allergies List of allergies/restrictions to avoid
     * @return Flow with the generated meal plan as a Map of DayOfWeek to List of RecipeMealItem
     */
    fun generateMealPlan(
        calorieTarget: Int,
        dietType: String,
        days: List<DayOfWeek> = DayOfWeek.values().toList(),
        allergies: List<String> = emptyList()
    ): Flow<Result<Map<DayOfWeek, List<RecipeMealItem>>>> = flow {
        Log.d("RecipeRepository", "Generating meal plan with diet: $dietType, calories: $calorieTarget")
        val mealPlan = mutableMapOf<DayOfWeek, MutableList<RecipeMealItem>>()
        
        // Initialize empty meal lists for each day
        days.forEach { day ->
            mealPlan[day] = mutableListOf()
        }
        
        // Define meal distribution by percentage of daily calories
        val mealDistribution = mapOf(
            "Breakfast" to 0.25f,
            "Lunch" to 0.35f,
            "Dinner" to 0.35f,
            "Snacks" to 0.05f
        )
        
        // Get recipes that match the diet type
        val dietQuery = when (dietType.lowercase()) {
            "low-carb" -> "low carb"
            "high-protein" -> "high protein"
            "vegetarian" -> "vegetarian"
            "vegan" -> "vegan"
            else -> "" // balanced diet doesn't need a specific query
        }
        
        // Get recipes that match the diet type and avoid allergies
        var query = dietQuery
        if (allergies.isNotEmpty()) {
            // Add allergy exclusions to the query
            allergies.forEach { allergy ->
                query += " -${allergy}"
            }
        }
        
        // Build the collection of recipes we can use for the meal plan
        val breakfastRecipes = mutableListOf<DetailedRecipe>()
        val lunchRecipes = mutableListOf<DetailedRecipe>()
        val dinnerRecipes = mutableListOf<DetailedRecipe>()
        val snackRecipes = mutableListOf<DetailedRecipe>()
        
        // Run recipe searches one after another since we're in a Flow
        // Get breakfast recipes
        val breakfastQuery = if (query.isNotEmpty()) "$query breakfast" else "breakfast"
        try {
            searchRecipes(breakfastQuery, limit = 30)
                .catch { e ->
                    Log.e("RecipeRepository", "Error searching breakfast recipes: ${e.message}")
                    Result.success(emptyList<DetailedRecipe>())
                }
                .collect { result ->
                    result.getOrNull()?.let { recipes ->
                        val filteredRecipes = recipes.filter {
                            // Apply calorie filter to find appropriate recipes
                            val targetCalories = (calorieTarget * mealDistribution["Breakfast"]!!)
                            it.nutritionFacts.calories in (targetCalories * 0.6f).toInt()..(targetCalories * 1.4f).toInt()
                        }
                        breakfastRecipes.addAll(filteredRecipes)
                    }
                }
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Error searching breakfast recipes: ${e.message}")
            // Continue with other meal types
        }
        
        // Get lunch recipes
        val lunchQuery = if (query.isNotEmpty()) "$query lunch" else "lunch"
        try {
            searchRecipes(lunchQuery, limit = 30)
                .catch { e ->
                    Log.e("RecipeRepository", "Error searching lunch recipes: ${e.message}")
                    Result.success(emptyList<DetailedRecipe>())
                }
                .collect { result ->
                    result.getOrNull()?.let { recipes ->
                        val filteredRecipes = recipes.filter {
                            // Apply calorie filter to find appropriate recipes
                            val targetCalories = (calorieTarget * mealDistribution["Lunch"]!!)
                            it.nutritionFacts.calories in (targetCalories * 0.6f).toInt()..(targetCalories * 1.4f).toInt()
                        }
                        lunchRecipes.addAll(filteredRecipes)
                    }
                }
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Error searching lunch recipes: ${e.message}")
            // Continue with other meal types
        }
        
        // Get dinner recipes
        val dinnerQuery = if (query.isNotEmpty()) "$query dinner" else "dinner"
        try {
            searchRecipes(dinnerQuery, limit = 30)
                .catch { e ->
                    Log.e("RecipeRepository", "Error searching dinner recipes: ${e.message}")
                    Result.success(emptyList<DetailedRecipe>())
                }
                .collect { result ->
                    result.getOrNull()?.let { recipes ->
                        val filteredRecipes = recipes.filter {
                            // Apply calorie filter to find appropriate recipes
                            val targetCalories = (calorieTarget * mealDistribution["Dinner"]!!)
                            it.nutritionFacts.calories in (targetCalories * 0.6f).toInt()..(targetCalories * 1.4f).toInt()
                        }
                        dinnerRecipes.addAll(filteredRecipes)
                    }
                }
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Error searching dinner recipes: ${e.message}")
            // Continue with other meal types
        }
        
        // Get snack recipes
        val snackQuery = if (query.isNotEmpty()) "$query snack" else "snack"
        try {
            searchRecipes(snackQuery, limit = 20)
                .catch { e ->
                    Log.e("RecipeRepository", "Error searching snack recipes: ${e.message}")
                    Result.success(emptyList<DetailedRecipe>())
                }
                .collect { result ->
                    result.getOrNull()?.let { recipes ->
                        val filteredRecipes = recipes.filter {
                            // Apply calorie filter to find appropriate recipes
                            val targetCalories = (calorieTarget * mealDistribution["Snacks"]!!)
                            it.nutritionFacts.calories in (targetCalories * 0.5f).toInt()..(targetCalories * 1.5f).toInt()
                        }
                        snackRecipes.addAll(filteredRecipes)
                    }
                }
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Error searching snack recipes: ${e.message}")
            // Continue with other meal types
        }
        
        // Get random recipes for additional variety
        try {
            getRandomRecipes(40)
                .catch { e ->
                    Log.e("RecipeRepository", "Error getting random recipes: ${e.message}")
                    Result.success(emptyList<DetailedRecipe>())
                }
                .collect { result ->
                    result.getOrNull()?.let { recipes ->
                        // Add to breakfast recipes
                        breakfastRecipes.addAll(recipes.filter { 
                            it.name.contains("breakfast", ignoreCase = true) && 
                            !breakfastRecipes.any { existing -> existing.id == it.id }
                        })
                        
                        // Add to lunch recipes
                        lunchRecipes.addAll(recipes.filter { 
                            !it.name.contains("breakfast", ignoreCase = true) && 
                            !lunchRecipes.any { existing -> existing.id == it.id }
                        })
                        
                        // Add to dinner recipes
                        dinnerRecipes.addAll(recipes.filter { 
                            !it.name.contains("breakfast", ignoreCase = true) && 
                            !dinnerRecipes.any { existing -> existing.id == it.id }
                        })
                        
                        // Add to snack recipes
                        snackRecipes.addAll(recipes.filter { 
                            it.name.length < 30 && 
                            !snackRecipes.any { existing -> existing.id == it.id }
                        })
                    }
                }
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Error getting random recipes: ${e.message}")
            // Continue with what we have
        }
        
        // Log recipe counts for debugging
        Log.d("RecipeRepository", "Recipe counts - Breakfast: ${breakfastRecipes.size}, Lunch: ${lunchRecipes.size}, Dinner: ${dinnerRecipes.size}, Snacks: ${snackRecipes.size}")
        
        // If we still don't have enough recipes, use what we have
        if (breakfastRecipes.isEmpty()) {
            Log.w("RecipeRepository", "No breakfast recipes found, using fallback")
            // Use lunch recipes as fallback if needed
            breakfastRecipes.addAll(lunchRecipes.take(days.size.coerceAtMost(lunchRecipes.size)))
        }
        
        if (lunchRecipes.isEmpty()) {
            Log.w("RecipeRepository", "No lunch recipes found, using fallback")
            // Use dinner recipes as fallback if needed
            lunchRecipes.addAll(dinnerRecipes.take(days.size.coerceAtMost(dinnerRecipes.size)))
        }
        
        if (dinnerRecipes.isEmpty()) {
            Log.w("RecipeRepository", "No dinner recipes found, using fallback")
            // Use lunch recipes as fallback if needed
            dinnerRecipes.addAll(lunchRecipes.take(days.size.coerceAtMost(lunchRecipes.size)))
        }
        
        // Assign recipes to each day of the week
        for (day in days) {
            // Create a list to track all recipe IDs used for this day to avoid duplicates
            val usedRecipeIds = mutableSetOf<Int>()
            
            // Breakfast
            if (breakfastRecipes.isNotEmpty()) {
                // Find breakfast recipes not already used
                val validBreakfastRecipes = breakfastRecipes.filter { it.id !in usedRecipeIds }
                val breakfast = if (validBreakfastRecipes.isNotEmpty()) {
                    validBreakfastRecipes.random()
                } else {
                    breakfastRecipes.random()
                }
                
                // Add this recipe to the used set
                usedRecipeIds.add(breakfast.id)
                
                // Remove this recipe from our list to avoid reuse across days
                breakfastRecipes.remove(breakfast)
                
                mealPlan[day]!!.add(RecipeMealItem(
                    id = "${day.name}_breakfast",
                    name = "Breakfast", 
                    description = breakfast.name,
                    recipeId = breakfast.id,
                    calories = breakfast.nutritionFacts.calories,
                    time = "Breakfast",
                    imageUrl = breakfast.imageUrl
                ))
            }
            
            // Lunch
            if (lunchRecipes.isNotEmpty()) {
                // Find lunch recipes not already used
                val validLunchRecipes = lunchRecipes.filter { it.id !in usedRecipeIds }
                val lunch = if (validLunchRecipes.isNotEmpty()) {
                    validLunchRecipes.random()
                } else {
                    lunchRecipes.random()
                }
                
                // Add this recipe to the used set
                usedRecipeIds.add(lunch.id)
                
                // Remove this recipe from our list to avoid reuse across days
                lunchRecipes.remove(lunch)
                
                mealPlan[day]!!.add(RecipeMealItem(
                    id = "${day.name}_lunch",
                    name = "Lunch", 
                    description = lunch.name,
                    recipeId = lunch.id,
                    calories = lunch.nutritionFacts.calories,
                    time = "Lunch",
                    imageUrl = lunch.imageUrl
                ))
            }
            
            // Dinner
            if (dinnerRecipes.isNotEmpty()) {
                // Find dinner recipes not already used
                val validDinnerRecipes = dinnerRecipes.filter { it.id !in usedRecipeIds }
                val dinner = if (validDinnerRecipes.isNotEmpty()) {
                    validDinnerRecipes.random()
                } else {
                    dinnerRecipes.random()
                }
                
                // Add this recipe to the used set
                usedRecipeIds.add(dinner.id)
                
                // Remove this recipe from our list to avoid reuse across days
                dinnerRecipes.remove(dinner)
                
                mealPlan[day]!!.add(RecipeMealItem(
                    id = "${day.name}_dinner",
                    name = "Dinner", 
                    description = dinner.name,
                    recipeId = dinner.id,
                    calories = dinner.nutritionFacts.calories,
                    time = "Dinner",
                    imageUrl = dinner.imageUrl
                ))
            }
            
            // Snack
            if (snackRecipes.isNotEmpty()) {
                // Find snack recipes not already used
                val validSnackRecipes = snackRecipes.filter { it.id !in usedRecipeIds }
                val snack = if (validSnackRecipes.isNotEmpty()) {
                    validSnackRecipes.random()
                } else {
                    snackRecipes.random()
                }
                
                // Add this recipe to the used set
                usedRecipeIds.add(snack.id)
                
                // Remove this recipe from our list to avoid reuse across days
                snackRecipes.remove(snack)
                
                mealPlan[day]!!.add(RecipeMealItem(
                    id = "${day.name}_snack",
                    name = "Snacks", 
                    description = snack.name,
                    recipeId = snack.id,
                    calories = snack.nutritionFacts.calories,
                    time = "Snacks",
                    imageUrl = snack.imageUrl
                ))
            }
        }
        
        emit(Result.success(mealPlan))
    }.catch { e ->
        Log.e("RecipeRepository", "Error generating meal plan: ${e.message}", e)
        emit(Result.failure(e))
    }

    /**
     * Data class for items in the meal plan
     */
    data class RecipeMealItem(
        val id: String,
        val name: String,
        val description: String,
        val recipeId: Int,
        val calories: Int,
        val time: String,
        val imageUrl: String? = null
    )

    /**
     * Checks if API request returned a payment required error and handles it
     */
    private fun handlePaymentRequiredErrors(searchResult: Result<List<DetailedRecipe>>, query: String): Result<List<DetailedRecipe>> {
        searchResult.onFailure { error ->
            if (error.message?.contains("402") == true || error.message?.contains("Payment Required") == true) {
                Log.w("RecipeRepository", "API payment limit reached, using cached data: ${error.message}")
                
                // Try to get cached results instead
                try {
                    val cachedRecipes = cacheManager.getCachedRecipesByQuery(query)
                    if (cachedRecipes.isNotEmpty()) {
                        Log.d("RecipeRepository", "Using ${cachedRecipes.size} cached recipes as fallback")
                        return Result.success(cachedRecipes)
                    }
                } catch (e: Exception) {
                    Log.e("RecipeRepository", "Error getting cached recipes: ${e.message}")
                }
            }
        }
        
        return searchResult
    }

    /**
     * Get a random recipe to use as fallback when API fails
     */
    private fun getFallbackRecipe(): DetailedRecipe {
        // Create a basic fallback recipe
        return DetailedRecipe(
            id = 0,
            name = "Default Recipe",
            description = "A simple healthy meal",
            imageUrl = "",
            preparationTime = 15,
            cookingTime = 25,
            servings = 4,
            difficulty = "Medium",
            ingredients = listOf(
                IngredientItem(
                    id = 1,
                    name = "Ingredient",
                    quantity = 1f,
                    unit = "cup"
                )
            ),
            instructions = listOf("Prepare ingredients", "Cook until done", "Serve hot"),
            nutritionFacts = NutritionFacts(
                calories = 400,
                protein = 20f,
                carbs = 40f,
                fat = 15f
            ),
            tags = listOf("Balanced", "Healthy"),
            isFavorite = false
        )
    }

    /**
     * Get recipes by category
     * @param category Category to filter by
     * @param limit Maximum number of results to return (default: 10)
     * @return Flow of recipes in the category
     */
    fun getRecipesByCategory(category: String, limit: Int = 10): Flow<Result<List<DetailedRecipe>>> = flow {
        Log.d("RecipeRepository", "Fetching recipes by category: $category, limit: $limit")
        
        val results = mutableListOf<DetailedRecipe>()
        var emitFallbackOnError = true
        
        try {
            // First try to get recipes from Spoonacular
            try {
                val spoonacularRecipes = try {
                    recipeCacheService.searchAndCacheRecipes(category, limit)
                        .catch { e ->
                            // Check for cancellation first
                            if (e is kotlinx.coroutines.CancellationException) throw e
                            
                            // Handle Spoonacular errors gracefully without emitting
                            if (e.message?.contains("402") == true || e.message?.contains("Payment Required") == true) {
                                Log.w("RecipeRepository", "Spoonacular payment limit reached: ${e.message}")
                            } else {
                                Log.e("RecipeRepository", "Error fetching recipes by category from Spoonacular: ${e.message}")
                            }
                            // Don't emit from within catch block
                        }
                        .first() as? List<DetailedRecipe> ?: emptyList()
                } catch (e: Exception) {
                    // Check for cancellation first
                    if (e is kotlinx.coroutines.CancellationException) throw e
                    
                    Log.e("RecipeRepository", "Error fetching recipes by category from Spoonacular: ${e.message}")
                    emptyList<DetailedRecipe>()
                }
                
                if (spoonacularRecipes.isNotEmpty()) {
                    Log.d("RecipeRepository", "Got ${spoonacularRecipes.size} recipes from Spoonacular category search")
                    results.addAll(spoonacularRecipes)
                } else {
                    Log.d("RecipeRepository", "No recipes from Spoonacular category search")
                }
            } catch (e: Exception) {
                // Check for cancellation first
                if (e is kotlinx.coroutines.CancellationException) throw e
                
                Log.e("RecipeRepository", "Error fetching Spoonacular category recipes: ${e.message}")
                // Continue to other sources
            }
            
            // Then try to get recipes from TheMealDB
            try {
                val mealDbRecipes = try {
                    recipeService.getRecipesByCategory(category)
                        .catch { e ->
                            // Check for cancellation first
                            if (e is kotlinx.coroutines.CancellationException) throw e
                            
                            Log.e("RecipeRepository", "Error in TheMealDB category flow: ${e.message}")
                            // Don't emit from within catch block
                        }
                        .first() as? List<DetailedRecipe> ?: emptyList()
                } catch (e: Exception) {
                    // Check for cancellation first
                    if (e is kotlinx.coroutines.CancellationException) throw e
                    
                    Log.e("RecipeRepository", "Error fetching recipes by category from TheMealDB: ${e.message}")
                    emptyList<DetailedRecipe>()
                }
                
                if (mealDbRecipes.isNotEmpty()) {
                    Log.d("RecipeRepository", "Got ${mealDbRecipes.size} recipes from TheMealDB category search")
                    
                    // Add unique recipes from TheMealDB
                    mealDbRecipes.forEach { recipe ->
                        if (results.none { it.id == recipe.id }) {
                            results.add(recipe)
                        }
                    }
                } else {
                    Log.d("RecipeRepository", "No recipes from TheMealDB category search")
                }
            } catch (e: Exception) {
                // Check for cancellation first
                if (e is kotlinx.coroutines.CancellationException) throw e
                
                Log.e("RecipeRepository", "Error fetching TheMealDB category recipes: ${e.message}")
                // Continue with existing results
            }
            
            // If no recipes found, add a fallback recipe
            if (results.isEmpty()) {
                Log.d("RecipeRepository", "No category results found, using fallback")
                results.add(getFallbackRecipe())
            }
            
            Log.d("RecipeRepository", "Returning ${results.size} category results")
            emit(Result.success(results.take(limit)))
            emitFallbackOnError = false // We've successfully emitted results
        } catch (e: Exception) {
            // Don't try to handle cancellation, just let it propagate
            if (e is kotlinx.coroutines.CancellationException) {
                Log.d("RecipeRepository", "Category request was cancelled: ${e.message}")
                throw e // Rethrow cancellation exception to properly cancel the flow
            }
            
            Log.e("RecipeRepository", "Error fetching recipes by category: ${e.message}", e)
            
            // Only emit a fallback if we haven't already emitted something
            if (emitFallbackOnError) {
                // Final fallback - always return at least one recipe
                val fallbackResult = listOf(getFallbackRecipe())
                emit(Result.success(fallbackResult))
            }
        }
    }
}