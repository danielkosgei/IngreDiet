package com.thenewkenya.ingrediet.data.repository

import android.content.Context
import android.util.Log
import com.thenewkenya.ingrediet.data.model.DetailedRecipe
import com.thenewkenya.ingrediet.data.model.IngredientItem
import com.thenewkenya.ingrediet.data.model.NutritionFacts
import com.thenewkenya.ingrediet.data.network.CacheManager
import com.thenewkenya.ingrediet.data.network.api.KenyanFoodsService
import com.thenewkenya.ingrediet.data.network.api.OpenFoodFactsService
import com.thenewkenya.ingrediet.data.network.api.TheMealDbService
import com.thenewkenya.ingrediet.data.network.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.filter.PostgrestFilterBuilder
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import java.util.concurrent.TimeUnit

class RecipeRepository(context: Context) {
    private val cacheManager = CacheManager(context)
    private val mealDbService = TheMealDbService()
    private val openFoodFactsService = OpenFoodFactsService()
    private val kenyanFoodsService = KenyanFoodsService()
    
    // Schedule periodic cache cleanup
    init {
        // Run cache cleanup once a day
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            kotlinx.coroutines.runBlocking {
                cacheManager.cleanupCache()
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
        try {
            Log.d("RecipeRepository", "Fetching recipe details for ID: $recipeId")
            
            // Check if recipe is in local cache first
            val cachedRecipe = cacheManager.getCachedRecipe(recipeId)
            if (cachedRecipe != null) {
                Log.d("RecipeRepository", "Found recipe $recipeId in local cache")
                emit(Result.success(cachedRecipe))
                return@flow
            }

            // If not in cache, fetch from Supabase
            Log.d("RecipeRepository", "Recipe not in cache, fetching from Supabase")
            val recipeList = supabase.from("recipes")
                .select() {
                    filter { eq("id", recipeId) }
                }
                .decodeList<RecipeDto>()

            if (recipeList.isEmpty()) {
                // Not in Supabase, try API
                Log.d("RecipeRepository", "Recipe not in Supabase, trying API")
                val apiRecipe = mealDbService.getMealById(recipeId.toString())
                
                if (apiRecipe != null) {
                    Log.d("RecipeRepository", "Found recipe $recipeId in API")
                    // Cache the recipe
                    cacheManager.cacheRecipe(apiRecipe)
                    
                    // Optionally store in Supabase for future use
                    try {
                        storeRecipeInSupabase(apiRecipe)
                    } catch (e: Exception) {
                        Log.e("RecipeRepository", "Error storing API recipe in Supabase: ${e.message}", e)
                        // Continue even if Supabase storage fails
                    }
                    
                    emit(Result.success(apiRecipe))
                } else {
                    emit(Result.failure(Exception("Recipe not found in any source")))
                }
                return@flow
            }

            val recipeResponse = recipeList.first()

            // 2. Fetch ingredients
            val ingredients = supabase.from("recipe_ingredients")
                .select() {
                    filter { eq("recipe_id", recipeId) }
                }
                .decodeList<RecipeIngredientDto>()

            Log.d("RecipeRepository", "Found ${ingredients.size} ingredients for recipe $recipeId")

            // 3. Fetch ingredient details
            val ingredientDetails = mutableListOf<IngredientItem>()

            for (ingredient in ingredients) {
                try {
                    val ingredientDataList = supabase.from("ingredients")
                        .select() {
                            filter { eq("id", ingredient.ingredient_id) }
                        }
                        .decodeList<IngredientDto>()

                    if (ingredientDataList.isNotEmpty()) {
                        val ingredientData = ingredientDataList.first()
                        ingredientDetails.add(
                            IngredientItem(
                                id = ingredient.ingredient_id,
                                name = ingredientData.name,
                                quantity = ingredient.quantity,
                                unit = ingredient.unit
                            )
                        )
                    }
                } catch (e: Exception) {
                    Log.e("RecipeRepository", "Error fetching ingredient ${ingredient.ingredient_id}: ${e.message}", e)
                }
            }

            // 4. Fetch instructions
            val instructions = supabase.from("recipe_instructions")
                .select() {
                    filter { eq("recipe_id", recipeId) }
                    order("step_number", Order.ASCENDING)
                }
                .decodeList<RecipeInstructionDto>()
                .map { it.instruction }

            // 5. Fetch nutrition facts
            val nutritionList = supabase.from("recipe_nutrition")
                .select() {
                    filter { eq("recipe_id", recipeId) }
                }
                .decodeList<RecipeNutritionDto>()

            val nutritionResponse = if (nutritionList.isNotEmpty()) {
                nutritionList.first()
            } else {
                // Default nutrition data
                RecipeNutritionDto(
                    id = 0,
                    recipe_id = recipeId,
                    calories = 0,
                    protein = 0f,
                    carbs = 0f,
                    fat = 0f,
                    fiber = null,
                    sugar = null
                )
            }

            // 6. Check favorite status
            val isFavorite = try {
                val currentUser = supabase.auth.currentUserOrNull()?.id
                if (currentUser != null) {
                    // Use the proper DTO class for deserialization
                    val favoritesList = supabase.from("user_favorites")
                        .select(columns = Columns.list("id")) {
                            filter {
                                eq("user_id", currentUser)
                                eq("recipe_id", recipeId)
                            }
                        }
                        .decodeList<FavoriteDto>()

                    favoritesList.isNotEmpty()
                } else {
                    // User not logged in, can't have favorites
                    false
                }
            } catch (e: Exception) {
                Log.e("RecipeRepository", "Error checking favorite status: ${e.message}", e)
                false
            }

            // Create the final recipe object
            val detailedRecipe = DetailedRecipe(
                id = recipeResponse.id,
                name = recipeResponse.name,
                description = recipeResponse.description ?: "",
                imageUrl = recipeResponse.image_url ?: "",
                preparationTime = recipeResponse.preparation_time ?: 0,
                cookingTime = recipeResponse.cooking_time ?: 0,
                servings = recipeResponse.servings ?: 0,
                difficulty = recipeResponse.difficulty ?: "Medium",
                ingredients = ingredientDetails,
                instructions = instructions,
                nutritionFacts = NutritionFacts(
                    calories = nutritionResponse.calories ?: 0,
                    protein = nutritionResponse.protein ?: 0f,
                    carbs = nutritionResponse.carbs ?: 0f,
                    fat = nutritionResponse.fat ?: 0f,
                    fiber = nutritionResponse.fiber,
                    sugar = nutritionResponse.sugar
                ),
                tags = recipeResponse.tags ?: emptyList(),
                isFavorite = isFavorite
            )
            
            // Cache the recipe for future use
            cacheManager.cacheRecipe(detailedRecipe)

            emit(Result.success(detailedRecipe))
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Error in getRecipeDetails: ${e.message}", e)
            emit(Result.failure(e))
        }
    }
    
    /**
     * Store a recipe from API in Supabase for future use
     */
    private suspend fun storeRecipeInSupabase(recipe: DetailedRecipe) {
        try {
            // Check if user is authenticated
            val currentUser = supabase.auth.currentUserOrNull()
            if (currentUser == null) {
                Log.d("RecipeRepository", "Not storing recipe in Supabase - user not authenticated")
                return
            }
            
            Log.d("RecipeRepository", "Storing recipe ${recipe.id} in Supabase")
            
            // 1. Store basic recipe data
            val recipeDto = RecipeDto(
                id = recipe.id,
                name = recipe.name,
                description = recipe.description,
                image_url = recipe.imageUrl,
                preparation_time = recipe.preparationTime,
                cooking_time = recipe.cookingTime,
                servings = recipe.servings,
                difficulty = recipe.difficulty,
                tags = recipe.tags
            )
            
            supabase.from("recipes").upsert(recipeDto)
            
            // 2. Store ingredients
            recipe.ingredients.forEach { ingredient ->
                // First ensure the ingredient exists
                val ingredientDto = IngredientDto(
                    id = ingredient.id,
                    name = ingredient.name
                )
                
                supabase.from("ingredients").upsert(ingredientDto)
                
                // Then link to recipe
                val recipeIngredientDto = RecipeIngredientDto(
                    id = 0, // Will be auto-assigned
                    recipe_id = recipe.id,
                    ingredient_id = ingredient.id,
                    quantity = ingredient.quantity,
                    unit = ingredient.unit
                )
                
                supabase.from("recipe_ingredients").upsert(recipeIngredientDto)
            }
            
            // 3. Store instructions
            recipe.instructions.forEachIndexed { index, instruction ->
                val instructionDto = RecipeInstructionDto(
                    id = 0, // Will be auto-assigned
                    recipe_id = recipe.id,
                    step_number = index + 1,
                    instruction = instruction
                )
                
                supabase.from("recipe_instructions").upsert(instructionDto)
            }
            
            // 4. Store nutrition facts
            val nutritionDto = RecipeNutritionDto(
                id = 0, // Will be auto-assigned
                recipe_id = recipe.id,
                calories = recipe.nutritionFacts.calories,
                protein = recipe.nutritionFacts.protein,
                carbs = recipe.nutritionFacts.carbs,
                fat = recipe.nutritionFacts.fat,
                fiber = recipe.nutritionFacts.fiber,
                sugar = recipe.nutritionFacts.sugar
            )
            
            supabase.from("recipe_nutrition").upsert(nutritionDto)
            
            Log.d("RecipeRepository", "Successfully stored recipe ${recipe.id} in Supabase")
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Error storing recipe in Supabase: ${e.message}", e)
            throw e
        }
    }
    
    /**
     * Search for recipes by name
     * Combines results from Supabase and TheMealDB API
     */
    suspend fun searchRecipes(query: String): Flow<Result<List<DetailedRecipe>>> = flow {
        try {
            Log.d("RecipeRepository", "Searching for recipes with query: $query")
            val results = mutableListOf<DetailedRecipe>()
            
            // First search in Supabase
            try {
                val supabaseResults = supabase.from("recipes")
                    .select() {
                        filter { ilike("name", "%$query%") }
                        limit(10)
                    }
                    .decodeList<RecipeDto>()
                    
                Log.d("RecipeRepository", "Found ${supabaseResults.size} recipes in Supabase")
                
                // Fetch full details for each recipe
                supabaseResults.forEach { recipeDto ->
                    getRecipeDetails(recipeDto.id).collect { result ->
                        result.onSuccess { recipe ->
                            results.add(recipe)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("RecipeRepository", "Error searching Supabase: ${e.message}", e)
                // Continue with API search even if Supabase search fails
            }
            
            // Then search in TheMealDB API
            try {
                val apiResults = mealDbService.searchMealsByName(query)
                Log.d("RecipeRepository", "Found ${apiResults.size} recipes in API")
                
                // Cache API results
                apiResults.forEach { recipe ->
                    cacheManager.cacheRecipe(recipe)
                    
                    // Only add if not already in results
                    if (results.none { it.id == recipe.id }) {
                        results.add(recipe)
                    }
                }
            } catch (e: Exception) {
                Log.e("RecipeRepository", "Error searching API: ${e.message}", e)
                // Continue with existing results even if API search fails
            }
            
            emit(Result.success(results))
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

    suspend fun getRecipes(
        query: String? = null,
        category: String? = null,
        limit: Int = 10
    ): Flow<Result<List<RecipeListItem>>> = flow {
        try {
            val combinedRecipes = mutableListOf<RecipeListItem>()
            var apiRecipesCount = 0
            
            // First try to get recipes from Supabase
            try {
                Log.d("RecipeRepository", "Fetching recipes from Supabase")
                val recipes = supabase.from("recipes")
                    .select(columns = Columns.list("id, name, image_url, preparation_time, cooking_time, difficulty, tags")) {
                        // Apply filters if provided
                        filter {
                            if (!query.isNullOrEmpty()) {
                                ilike("name", "%$query%")
                            }

                            if (!category.isNullOrEmpty() && category.lowercase() != "all recipes") {
                                contains("tags", arrayOf(category).toList())
                            }
                        }

                        limit(limit.toLong())
                        order("id", Order.DESCENDING)
                    }
                    .decodeList<RecipeListItemDto>()

                // Map DTOs to domain objects
                val recipeItems = recipes.map { dto ->
                    RecipeListItem(
                        id = dto.id,
                        name = dto.name,
                        imageUrl = dto.imageUrl,
                        time = "${dto.preparationTime + dto.cookingTime} min",
                        calories = 0, // We'll need to fetch this from nutrition facts table
                        category = dto.tags.first(),
                        rating = 0f, // Default rating since we don't have it in the database yet
                        dietaryInfo = dto.tags.drop(1) // Use remaining tags as dietary info
                    )
                }
                
                combinedRecipes.addAll(recipeItems)
                Log.d("RecipeRepository", "Found ${recipeItems.size} recipes in Supabase")
            } catch (e: Exception) {
                Log.e("RecipeRepository", "Error fetching from Supabase: ${e.message}", e)
                // Continue to API even if Supabase fetch fails
            }
            
            // Always search the API if a query is provided to get the most comprehensive results
            // Also search if we need more recipes to meet the limit
            if (combinedRecipes.size < limit || query != null) {
                try {
                    Log.d("RecipeRepository", "Fetching recipes from API")
                    val apiQuery = query ?: ""  // Use empty string if query is null
                    
                    // Search TheMealDB API
                    val apiRecipes = mealDbService.searchMealsByName(apiQuery)
                    Log.d("RecipeRepository", "Found ${apiRecipes.size} recipes from TheMealDB API")
                    
                    // Convert API recipes to RecipeListItems
                    val apiRecipeItems = apiRecipes.map { recipe ->
                        RecipeListItem(
                            id = recipe.id,
                            name = recipe.name,
                            imageUrl = recipe.imageUrl,
                            time = "${recipe.preparationTime + recipe.cookingTime} min",
                            calories = recipe.nutritionFacts.calories,
                            category = recipe.tags.firstOrNull() ?: "",
                            rating = 4.5f,  // Default rating for API recipes
                            dietaryInfo = recipe.tags
                        )
                    }
                    
                    // Filter out duplicates (recipes that are already in the list from Supabase)
                    val newApiRecipes = apiRecipeItems.filter { apiRecipe ->
                        combinedRecipes.none { it.id == apiRecipe.id }
                    }
                    
                    // Add new API recipes to the combined list
                    combinedRecipes.addAll(newApiRecipes)
                    apiRecipesCount = newApiRecipes.size
                    Log.d("RecipeRepository", "Added $apiRecipesCount new recipes from API")
                    
                    // Cache the first few recipes immediately if they match the search query closely
                    // This improves user experience for future searches
                    if (!query.isNullOrEmpty() && apiRecipes.isNotEmpty()) {
                        val recipesToCache = apiRecipes.take(3).filter { recipe ->
                            recipe.name.contains(query, ignoreCase = true)
                        }
                        
                        if (recipesToCache.isNotEmpty()) {
                            Log.d("RecipeRepository", "Found ${recipesToCache.size} highly relevant recipes to cache")
                            
                            // Mark these recipes for caching when accessed
                            // We'll store the recipe IDs to prioritize caching when they're viewed
                            val recipeIds = recipesToCache.map { it.id }
                            Log.d("RecipeRepository", "Marked recipe IDs for priority caching: $recipeIds")
                            
                            // The actual caching will happen when the recipe details are accessed
                            // This avoids suspension function calls in this flow context
                        }
                    }
                } catch (e: Exception) {
                    Log.e("RecipeRepository", "Error fetching from API: ${e.message}", e)
                    // Continue with existing Supabase results even if API fetch fails
                }
            }
            
            // Limit the final list to the requested size
            val finalRecipes = combinedRecipes.take(limit)
            Log.d("RecipeRepository", "Returning ${finalRecipes.size} recipes (${finalRecipes.size - apiRecipesCount} from Supabase, $apiRecipesCount from API)")
            
            emit(Result.success(finalRecipes))
        } catch (e: Exception) {
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
                    val apiResults = mealDbService.searchMealsByName(query)
                    // API results are already DetailedRecipe objects, so we need to extract the name
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
}