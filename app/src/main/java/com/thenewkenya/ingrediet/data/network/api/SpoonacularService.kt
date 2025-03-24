package com.thenewkenya.ingrediet.data.network.api

import android.util.Log
import com.thenewkenya.ingrediet.data.model.DetailedRecipe
import com.thenewkenya.ingrediet.data.model.IngredientItem
import com.thenewkenya.ingrediet.data.model.NutritionFacts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.client.plugins.*
import io.ktor.util.*
import android.preference.PreferenceManager

/**
 * Service class for interacting with Spoonacular API
 */
class SpoonacularService(private val context: android.content.Context) {
    private val TAG = "SpoonacularService"
    private val json = Json { ignoreUnknownKeys = true }
    private val baseUrl = "https://api.spoonacular.com"
    private val apiKey = "8633f6e288b441c48e43a51c3c968d78"
    
    // Constants
    companion object {
        const val TAG = "SpoonacularService"
        const val API_LIMIT_TIMESTAMP_KEY = "spoonacular_api_limit_timestamp"
        const val API_LIMIT_DURATION_MS = 24 * 60 * 60 * 1000L // 24 hours in milliseconds
    }
    
    // Track if API limit has been reached
    private val _apiLimitReached = mutableStateOf(false)
    private var _lastApiLimitReachedTime: Long = 0L
    
    // Public state for API limit
    val apiLimitReached: State<Boolean> = _apiLimitReached
    
    // Create Ktor client
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        // Set timeout using HttpTimeout feature or handle manually with HttpURLConnection
    }

    // Simplified method using HttpURLConnection for direct HTTP calls
    private suspend fun makeApiCall(endpoint: String, params: Map<String, String> = emptyMap()): String {
        return withContext(Dispatchers.IO) {
            // If API limit already reached, don't even try to make the call
            if (_apiLimitReached.value) {
                Log.d(TAG, "API limit already reached, skipping call to endpoint: $endpoint")
                throw ApiLimitExceededException("API limit reached (cached state)")
            }
            
            val urlBuilder = StringBuilder("$baseUrl/$endpoint?apiKey=$apiKey")
            
            params.forEach { (key, value) ->
                urlBuilder.append("&$key=$value")
            }
            
            val url = URL(urlBuilder.toString())
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 10000 // 10 seconds
            connection.readTimeout = 10000 // 10 seconds
            
            try {
                val responseCode = connection.responseCode
                Log.d(TAG, "API call to $endpoint returned code: $responseCode")
                
                if (responseCode == 200) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    Log.d(TAG, "Successfully received data from Spoonacular for endpoint: $endpoint")
                    return@withContext response
                } else if (responseCode == 402) {
                    _apiLimitReached.value = true
                    // Store the timestamp for when we reached the limit
                    storeLimitReachedTimestamp()
                    Log.e(TAG, "API limit reached (402 Payment Required) for endpoint: $endpoint")
                    throw ApiLimitExceededException("API limit reached (402 Payment Required)")
                } else {
                    Log.e(TAG, "API error: $responseCode for endpoint: $endpoint")
                    throw ApiException("API error: $responseCode for endpoint: $endpoint")
                }
            } catch (e: Exception) {
                if (e is ApiLimitExceededException) {
                    throw e
                }
                
                if (e is java.net.SocketTimeoutException) {
                    Log.e(TAG, "Timeout connecting to Spoonacular API for endpoint: $endpoint", e)
                    throw ApiTimeoutException("Connection timed out for endpoint: $endpoint")
                }
                
                Log.e(TAG, "Error making API call to endpoint: $endpoint", e)
                throw e
            } finally {
                connection.disconnect()
            }
        }
    }

    /**
     * Store timestamp when API limit was reached
     */
    private fun storeLimitReachedTimestamp() {
        // We use the system time to track when the limit was reached
        val currentTime = System.currentTimeMillis()
        _lastApiLimitReachedTime = currentTime
        
        // Store the timestamp in preferences for app restarts
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit().putLong(API_LIMIT_TIMESTAMP_KEY, currentTime).apply()
    }

    /**
     * Custom exceptions for API errors
     */
    class ApiException(message: String) : Exception(message)
    class ApiLimitExceededException(message: String) : Exception(message)
    class ApiTimeoutException(message: String) : Exception(message)

    /**
     * Search for recipes by query
     */
    suspend fun searchRecipes(query: String, limit: Int = 10): Flow<List<DetailedRecipe>> = flow {
        // Early check for API limit to avoid unnecessary work
        if (_apiLimitReached.value) {
            Log.d(TAG, "API limit reached, skipping Spoonacular search for query: $query")
            emit(emptyList())
            return@flow
        }

        // Skip empty queries
        if (query.isBlank()) {
            Log.d(TAG, "Empty query, fetching random recipes instead")
            getRandomRecipes(limit).collect { recipes -> 
                emit(recipes)
            }
            return@flow
        }

        Log.d(TAG, "Searching Spoonacular for: $query")
        val endpoint = "recipes/complexSearch"
        
        val params = mapOf(
            "query" to query,
            "number" to limit.toString(),
            "addRecipeInformation" to "true",
            "fillIngredients" to "true",
            "addRecipeNutrition" to "true"
        )
        
        val recipes = try {
            val responseJson = makeApiCall(endpoint, params)
            val searchResponse = json.decodeFromString<SpoonacularSearchResponse>(responseJson)
            searchResponse.results.map { result ->
                mapToDetailedRecipe(result)
            }
        } catch (e: Exception) {
            Log.e(TAG, "API error during search: ${e.message}")
            
            // Set API limit flag if appropriate
            if (e is ApiLimitExceededException || e.message?.contains("402") == true) {
                _apiLimitReached.value = true
                Log.d(TAG, "API limit reached, skipping Spoonacular search")
            }
            
            // For cancellation, don't emit - just return
            if (e is kotlinx.coroutines.CancellationException || 
                e.message?.contains("composition") == true || 
                e.cause is kotlinx.coroutines.CancellationException) {
                Log.d(TAG, "Search operation cancelled normally")
                return@flow
            }
            
            // For other errors, use an empty list
            emptyList()
        }
        
        // Only emit once, at the end
        emit(recipes)
    }

    /**
     * Search for recipes by ingredient list
     */
    suspend fun searchRecipesByIngredients(ingredients: List<String>, limit: Int = 10): Flow<List<DetailedRecipe>> = flow {
        // Early check for API limit to avoid unnecessary work
        if (_apiLimitReached.value) {
            Log.d(TAG, "API limit reached, skipping Spoonacular search by ingredients")
            emit(emptyList())
            return@flow
        }

        // Skip empty ingredient lists
        if (ingredients.isEmpty()) {
            Log.d(TAG, "Empty ingredient list, fetching random recipes instead")
            getRandomRecipes(limit).collect { recipes -> 
                emit(recipes)
            }
            return@flow
        }

        // Join the ingredients into a comma-separated string
        val ingredientsString = ingredients.joinToString(",")
        
        Log.d(TAG, "Searching Spoonacular for recipes with ingredients: $ingredientsString")
        val endpoint = "recipes/findByIngredients"
        
        val params = mapOf(
            "ingredients" to ingredientsString,
            "number" to limit.toString(),
            "ranking" to "1", // 1 = maximize used ingredients, 2 = minimize missing ingredients
            "ignorePantry" to "false"
        )
        
        try {
            val responseJson = makeApiCall(endpoint, params)
            val recipeResults = json.decodeFromString<List<SpoonacularIngredientSearchResult>>(responseJson)
            
            // For each recipe ID found, get the full recipe details
            val detailedRecipes = mutableListOf<DetailedRecipe>()
            
            for (result in recipeResults) {
                // Get full recipe details for each result
                getRecipeById(result.id).collect { recipe ->
                    if (recipe != null) {
                        detailedRecipes.add(recipe)
                    }
                }
            }
            
            emit(detailedRecipes)
            
        } catch (e: Exception) {
            Log.e(TAG, "API error during search by ingredients: ${e.message}")
            
            // Set API limit flag if appropriate
            if (e is ApiLimitExceededException || e.message?.contains("402") == true) {
                _apiLimitReached.value = true
                Log.d(TAG, "API limit reached, skipping search by ingredients")
            }
            
            // For cancellation, don't emit - just return
            if (e is kotlinx.coroutines.CancellationException || 
                e.message?.contains("composition") == true || 
                e.cause is kotlinx.coroutines.CancellationException) {
                Log.d(TAG, "Search by ingredients operation cancelled normally")
                return@flow
            }
            
            // For other errors, use an empty list
            emit(emptyList())
        }
    }

    /**
     * Get random recipes
     */
    suspend fun getRandomRecipes(limit: Int = 10): Flow<List<DetailedRecipe>> = flow {
        // Early check for API limit to avoid unnecessary work
        if (_apiLimitReached.value) {
            Log.d(TAG, "API limit reached, skipping Spoonacular random recipes")
            emit(emptyList())
            return@flow
        }

        Log.d(TAG, "Fetching random recipes from Spoonacular")
        val endpoint = "recipes/random"
        
        val params = mapOf(
            "number" to limit.toString(),
            "tags" to ""
        )
        
        val recipes = try {
            val responseJson = makeApiCall(endpoint, params)
            val randomResponse = json.decodeFromString<SpoonacularRandomResponse>(responseJson)
            randomResponse.recipes.map { recipe ->
                mapRandomToDetailedRecipe(recipe)
            }
        } catch (e: Exception) {
            Log.e(TAG, "API error during random recipes: ${e.message}")
            
            // Set API limit flag if appropriate
            if (e is ApiLimitExceededException || e.message?.contains("402") == true) {
                _apiLimitReached.value = true
                Log.d(TAG, "API limit reached, skipping Spoonacular random recipes")
            }
            
            // For cancellation, don't emit - just return
            if (e is kotlinx.coroutines.CancellationException || 
                e.message?.contains("composition") == true || 
                e.cause is kotlinx.coroutines.CancellationException) {
                Log.d(TAG, "Random recipes operation cancelled normally")
                return@flow
            }
            
            // For other errors, use an empty list
            emptyList()
        }
        
        // Only emit once, at the end
        emit(recipes)
    }

    /**
     * Get recipe by ID
     */
    suspend fun getRecipeById(id: Int): Flow<DetailedRecipe?> = flow {
        // Early check for API limit to avoid unnecessary work
        if (_apiLimitReached.value) {
            Log.d(TAG, "API limit reached, skipping Spoonacular recipe details for id: $id")
            emit(null)
            return@flow
        }

        Log.d(TAG, "Fetching recipe details from Spoonacular for ID: $id")
        val endpoint = "recipes/$id/information"
        
        val params = mapOf(
            "includeNutrition" to "true"
        )
        
        val recipe = try {
            val responseJson = makeApiCall(endpoint, params)
            val recipeResponse = json.decodeFromString<SpoonacularRecipeResponse>(responseJson)
            mapRecipeToDetailedRecipe(recipeResponse)
        } catch (e: Exception) {
            Log.e(TAG, "API error during recipe details: ${e.message}")
            
            // Set API limit flag if appropriate
            if (e is ApiLimitExceededException || e.message?.contains("402") == true) {
                _apiLimitReached.value = true
                Log.d(TAG, "API limit reached, skipping recipe details")
            }
            
            // For cancellation, don't emit - just return
            if (e is kotlinx.coroutines.CancellationException || 
                e.message?.contains("composition") == true || 
                e.cause is kotlinx.coroutines.CancellationException) {
                Log.d(TAG, "Recipe details operation cancelled normally")
                return@flow
            }
            
            // For other errors, return null
            null
        }
        
        // Only emit once, at the end
        emit(recipe)
    }

    /**
     * Provide fallback recipes when API calls fail
     */
    private fun getFallbackRecipes(): List<DetailedRecipe> {
        // Create some mock recipes as fallback
        return listOf(
            DetailedRecipe(
                id = 1001,
                name = "Pasta Primavera",
                description = "A delicious pasta dish with fresh vegetables.",
                imageUrl = "https://via.placeholder.com/300?text=Pasta+Primavera",
                preparationTime = 15,
                cookingTime = 20,
                servings = 4,
                difficulty = "Medium",
                ingredients = listOf(
                    IngredientItem(1, "Pasta", 250f, "g"),
                    IngredientItem(2, "Bell Peppers", 2f, ""),
                    IngredientItem(3, "Zucchini", 1f, ""),
                    IngredientItem(4, "Olive Oil", 2f, "tbsp")
                ),
                instructions = listOf(
                    "Boil pasta according to package instructions.",
                    "Sauté vegetables in olive oil.",
                    "Combine and serve."
                ),
                nutritionFacts = NutritionFacts(350, 10f, 50f, 8f),
                tags = listOf("Italian", "Vegetarian", "Pasta"),
                category = "Main Course",
                author = "Sample Author",
                dateAdded = "2025-03-20",
                cuisineType = "Italian"
            ),
            DetailedRecipe(
                id = 1002,
                name = "Chicken Stir Fry",
                description = "Quick and easy chicken stir fry with vegetables.",
                imageUrl = "https://via.placeholder.com/300?text=Chicken+Stir+Fry",
                preparationTime = 10,
                cookingTime = 15,
                servings = 3,
                difficulty = "Easy",
                ingredients = listOf(
                    IngredientItem(5, "Chicken Breast", 300f, "g"),
                    IngredientItem(6, "Mixed Vegetables", 2f, "cups"),
                    IngredientItem(7, "Soy Sauce", 3f, "tbsp"),
                    IngredientItem(8, "Garlic", 2f, "cloves")
                ),
                instructions = listOf(
                    "Cut chicken into strips.",
                    "Stir fry chicken until cooked.",
                    "Add vegetables and sauce, cook until tender."
                ),
                nutritionFacts = NutritionFacts(320, 30f, 15f, 12f),
                tags = listOf("Asian", "Quick", "High Protein"),
                category = "Main Course",
                author = "Sample Author",
                dateAdded = "2025-03-20",
                cuisineType = "Asian"
            ),
            DetailedRecipe(
                id = 1003,
                name = "Berry Smoothie Bowl",
                description = "Refreshing smoothie bowl topped with fruits and granola.",
                imageUrl = "https://via.placeholder.com/300?text=Smoothie+Bowl",
                preparationTime = 10,
                cookingTime = 0,
                servings = 1,
                difficulty = "Easy",
                ingredients = listOf(
                    IngredientItem(9, "Mixed Berries", 1f, "cup"),
                    IngredientItem(10, "Banana", 1f, ""),
                    IngredientItem(11, "Yogurt", 0.5f, "cup"),
                    IngredientItem(12, "Granola", 2f, "tbsp")
                ),
                instructions = listOf(
                    "Blend berries, banana and yogurt until smooth.",
                    "Pour into a bowl.",
                    "Top with granola and additional fruits."
                ),
                nutritionFacts = NutritionFacts(250, 8f, 40f, 5f),
                tags = listOf("Breakfast", "Healthy", "Quick"),
                category = "Breakfast",
                author = "Sample Author",
                dateAdded = "2025-03-20",
                cuisineType = "American"
            )
        )
    }

    // Method to check if we should try the API or use fallbacks only
    fun shouldUseFallbackOnly(): Boolean {
        return _apiLimitReached.value
    }

    // Method to reset the API limit status (e.g., at midnight when limits reset)
    fun resetApiLimitStatus() {
        _apiLimitReached.value = false
    }
}

@Serializable
data class SpoonacularSearchResponse(
    val results: List<SpoonacularSearchResult>,
    val offset: Int,
    val number: Int,
    val totalResults: Int
)

@Serializable
data class SpoonacularRandomResponse(
    val recipes: List<SpoonacularRandomRecipe>
)

@Serializable
data class SpoonacularIngredient(
    val id: Int? = null,
    val name: String,
    val amount: Float? = null,
    val unit: String? = null,
    val original: String? = null,
    val aisle: String? = null,
    val image: String? = null
) {
    fun toIngredientItem(): IngredientItem {
        return IngredientItem(
            id = id ?: name.hashCode(),
            name = name,
            quantity = amount ?: 0f,
            unit = unit ?: ""
        )
    }
}

@Serializable
data class SpoonacularInstructions(
    val name: String = "",
    val steps: List<SpoonacularStep> = emptyList()
)

@Serializable
data class SpoonacularStep(
    val number: Int,
    val step: String
)

@Serializable
data class SpoonacularNutrition(
    val nutrients: List<SpoonacularNutrient>
) {
    fun toNutritionFacts(): NutritionFacts {
        var calories = 0
        var protein = 0f
        var carbs = 0f
        var fat = 0f

        nutrients.forEach { nutrient ->
            when (nutrient.name.lowercase()) {
                "calories" -> calories = nutrient.amount.toInt()
                "protein" -> protein = nutrient.amount
                "carbohydrates" -> carbs = nutrient.amount
                "fat" -> fat = nutrient.amount
            }
        }

        return NutritionFacts(
            calories = calories,
            protein = protein,
            carbs = carbs,
            fat = fat
        )
    }
}

@Serializable
data class SpoonacularNutrient(
    val name: String,
    val amount: Float,
    val unit: String
)

@Serializable
data class SpoonacularSearchResult(
    val id: Int,
    val title: String,
    val image: String? = null,
    val summary: String? = null,
    val preparationMinutes: Int? = null,
    val cookingMinutes: Int? = null,
    val readyInMinutes: Int? = null,
    val servings: Int? = null,
    val vegetarian: Boolean? = null,
    val vegan: Boolean? = null,
    val glutenFree: Boolean? = null,
    val dairyFree: Boolean? = null,
    val veryHealthy: Boolean? = null,
    val analyzedInstructions: List<SpoonacularInstructions>? = emptyList(),
    val nutrition: SpoonacularNutritionInfo? = null
)

@Serializable
data class SpoonacularRandomRecipe(
    val id: Int,
    val title: String,
    val image: String? = null,
    val summary: String? = null,
    val preparationMinutes: Int? = null,
    val cookingMinutes: Int? = null,
    val readyInMinutes: Int? = null,
    val servings: Int? = null,
    val vegetarian: Boolean? = null,
    val vegan: Boolean? = null,
    val glutenFree: Boolean? = null,
    val dairyFree: Boolean? = null,
    val veryHealthy: Boolean? = null,
    val analyzedInstructions: List<SpoonacularInstructions>? = emptyList(),
    val extendedIngredients: List<SpoonacularIngredient>? = emptyList(),
    val nutrition: SpoonacularNutritionInfo? = null
)

@Serializable
data class SpoonacularRecipeResponse(
    val id: Int,
    val title: String,
    val image: String? = null,
    val summary: String? = null,
    val instructions: String? = null,
    val readyInMinutes: Int? = null,
    val preparationMinutes: Int? = null,
    val cookingMinutes: Int? = null,
    val servings: Int? = null,
    val vegetarian: Boolean? = null,
    val vegan: Boolean? = null,
    val glutenFree: Boolean? = null,
    val dairyFree: Boolean? = null,
    val veryHealthy: Boolean? = null,
    val cuisines: List<String>? = null,
    val analyzedInstructions: List<SpoonacularInstructions>? = emptyList(),
    val extendedIngredients: List<SpoonacularIngredient>? = emptyList(),
    val nutrition: SpoonacularNutritionInfo? = null
)

@Serializable
data class SpoonacularNutritionInfo(
    val nutrients: List<SpoonacularNutrient> = emptyList(),
    val ingredients: List<SpoonacularNutritionIngredient> = emptyList()
)

@Serializable
data class SpoonacularNutritionIngredient(
    val id: Int? = null,
    val name: String,
    val amount: Float? = null,
    val unit: String? = null
)

@Serializable
data class SpoonacularIngredientSearchResult(
    val id: Int,
    val title: String,
    val image: String? = null,
    val usedIngredientCount: Int,
    val missedIngredientCount: Int,
    val missedIngredients: List<SpoonacularIngredient>? = emptyList(),
    val usedIngredients: List<SpoonacularIngredient>? = emptyList(),
    val unusedIngredients: List<SpoonacularIngredient>? = emptyList(),
    val likes: Int? = 0
)

/**
 * Map SpoonacularSearchResult to DetailedRecipe
 */
private fun mapToDetailedRecipe(result: SpoonacularSearchResult): DetailedRecipe {
    // Extract ingredients
    val ingredients = result.nutrition?.ingredients?.map { ingredient ->
        IngredientItem(
            id = ingredient.id ?: ingredient.name.hashCode(),
            name = ingredient.name,
            quantity = ingredient.amount ?: 0f,
            unit = ingredient.unit ?: ""
        )
    } ?: emptyList()
    
    // Extract nutrition facts
    val nutritionFacts = NutritionFacts(
        calories = result.nutrition?.nutrients?.find { it.name == "Calories" }?.amount?.toInt() ?: 0,
        protein = result.nutrition?.nutrients?.find { it.name == "Protein" }?.amount ?: 0f,
        carbs = result.nutrition?.nutrients?.find { it.name == "Carbohydrates" }?.amount ?: 0f,
        fat = result.nutrition?.nutrients?.find { it.name == "Fat" }?.amount ?: 0f,
        fiber = result.nutrition?.nutrients?.find { it.name == "Fiber" }?.amount,
        sugar = result.nutrition?.nutrients?.find { it.name == "Sugar" }?.amount
    )
    
    // Extract instructions
    val instructions = result.analyzedInstructions?.firstOrNull()?.steps?.map { 
        it.step 
    } ?: emptyList()
    
    // Extract tags
    val tags = mutableListOf<String>()
    if (result.vegetarian == true) tags.add("Vegetarian")
    if (result.vegan == true) tags.add("Vegan")
    if (result.glutenFree == true) tags.add("Gluten-Free")
    if (result.dairyFree == true) tags.add("Dairy-Free")
    if (result.veryHealthy == true) tags.add("Healthy")
    
    // Calculate preparation and cooking times
    val prepTime = result.preparationMinutes ?: 0
    val cookTime = result.cookingMinutes ?: 0
    
    return DetailedRecipe(
        id = result.id,
        name = result.title,
        description = result.summary ?: "",
        imageUrl = result.image ?: "",
        preparationTime = prepTime,
        cookingTime = cookTime,
        servings = result.servings ?: 4,
        difficulty = if (prepTime + cookTime > 45) "Hard" else if (prepTime + cookTime > 20) "Medium" else "Easy",
        ingredients = ingredients,
        instructions = instructions,
        nutritionFacts = nutritionFacts,
        tags = tags,
        isFavorite = false // Default to not favorite
    )
}

/**
 * Map SpoonacularRandomRecipe to DetailedRecipe
 */
private fun mapRandomToDetailedRecipe(recipe: SpoonacularRandomRecipe): DetailedRecipe {
    // Extract ingredients
    val ingredients = recipe.extendedIngredients?.map { ingredient ->
        IngredientItem(
            id = ingredient.id ?: ingredient.name.hashCode(),
            name = ingredient.name,
            quantity = ingredient.amount ?: 0f,
            unit = ingredient.unit ?: ""
        )
    } ?: emptyList()
    
    // Extract nutrition facts
    val nutritionFacts = if (recipe.nutrition != null) {
        NutritionFacts(
            calories = recipe.nutrition.nutrients?.find { it.name == "Calories" }?.amount?.toInt() ?: 0,
            protein = recipe.nutrition.nutrients?.find { it.name == "Protein" }?.amount ?: 0f,
            carbs = recipe.nutrition.nutrients?.find { it.name == "Carbohydrates" }?.amount ?: 0f,
            fat = recipe.nutrition.nutrients?.find { it.name == "Fat" }?.amount ?: 0f,
            fiber = recipe.nutrition.nutrients?.find { it.name == "Fiber" }?.amount,
            sugar = recipe.nutrition.nutrients?.find { it.name == "Sugar" }?.amount
        )
    } else {
        // Default nutrition if not provided
        NutritionFacts(
            calories = recipe.servings?.times(250) ?: 500,
            protein = 15f,
            carbs = 30f,
            fat = 10f
        )
    }
    
    // Extract instructions
    val instructions = recipe.analyzedInstructions?.firstOrNull()?.steps?.map { 
        it.step 
    } ?: emptyList()
    
    // Extract tags
    val tags = mutableListOf<String>()
    if (recipe.vegetarian == true) tags.add("Vegetarian")
    if (recipe.vegan == true) tags.add("Vegan")
    if (recipe.glutenFree == true) tags.add("Gluten-Free")
    if (recipe.dairyFree == true) tags.add("Dairy-Free")
    if (recipe.veryHealthy == true) tags.add("Healthy")
    
    // Calculate preparation and cooking times
    val prepTime = recipe.preparationMinutes ?: 0
    val cookTime = recipe.cookingMinutes ?: recipe.readyInMinutes ?: 0
    
    return DetailedRecipe(
        id = recipe.id,
        name = recipe.title,
        description = recipe.summary ?: "",
        imageUrl = recipe.image ?: "",
        preparationTime = prepTime,
        cookingTime = cookTime,
        servings = recipe.servings ?: 4,
        difficulty = if (prepTime + cookTime > 45) "Hard" else if (prepTime + cookTime > 20) "Medium" else "Easy",
        ingredients = ingredients,
        instructions = instructions,
        nutritionFacts = nutritionFacts,
        tags = tags,
        isFavorite = false // Default to not favorite
    )
}

/**
 * Map SpoonacularRecipeResponse to DetailedRecipe
 */
private fun mapRecipeToDetailedRecipe(recipe: SpoonacularRecipeResponse): DetailedRecipe {
    // Extract ingredients
    val ingredients = recipe.extendedIngredients?.map { ingredient ->
        IngredientItem(
            id = ingredient.id ?: ingredient.name.hashCode(),
            name = ingredient.name,
            quantity = ingredient.amount ?: 0f,
            unit = ingredient.unit ?: ""
        )
    } ?: emptyList()
    
    // Extract nutrition facts
    val nutritionFacts = if (recipe.nutrition != null) {
        NutritionFacts(
            calories = recipe.nutrition.nutrients?.find { it.name == "Calories" }?.amount?.toInt() ?: 0,
            protein = recipe.nutrition.nutrients?.find { it.name == "Protein" }?.amount ?: 0f,
            carbs = recipe.nutrition.nutrients?.find { it.name == "Carbohydrates" }?.amount ?: 0f,
            fat = recipe.nutrition.nutrients?.find { it.name == "Fat" }?.amount ?: 0f,
            fiber = recipe.nutrition.nutrients?.find { it.name == "Fiber" }?.amount,
            sugar = recipe.nutrition.nutrients?.find { it.name == "Sugar" }?.amount
        )
    } else {
        // Default nutrition if not provided
        NutritionFacts(
            calories = 0,
            protein = 0f,
            carbs = 0f,
            fat = 0f
        )
    }
    
    // Extract instructions
    val instructions = recipe.analyzedInstructions?.firstOrNull()?.steps?.map { 
        it.step 
    } ?: recipe.instructions?.split(Regex("\\. |\\.\\s*"))?.filter { it.isNotBlank() } ?: emptyList()
    
    // Extract tags
    val tags = mutableListOf<String>()
    if (recipe.vegetarian == true) tags.add("Vegetarian")
    if (recipe.vegan == true) tags.add("Vegan")
    if (recipe.glutenFree == true) tags.add("Gluten-Free")
    if (recipe.dairyFree == true) tags.add("Dairy-Free")
    if (recipe.veryHealthy == true) tags.add("Healthy")
    
    // Calculate preparation and cooking times
    val prepTime = recipe.preparationMinutes ?: 0
    val cookTime = recipe.cookingMinutes ?: recipe.readyInMinutes ?: 0
    
    return DetailedRecipe(
        id = recipe.id,
        name = recipe.title,
        description = recipe.summary ?: "",
        imageUrl = recipe.image ?: "",
        preparationTime = prepTime,
        cookingTime = cookTime,
        servings = recipe.servings ?: 4,
        difficulty = if (prepTime + cookTime > 45) "Hard" else if (prepTime + cookTime > 20) "Medium" else "Easy",
        ingredients = ingredients,
        instructions = instructions,
        nutritionFacts = nutritionFacts,
        tags = tags,
        isFavorite = false // Default to not favorite
    )
}
