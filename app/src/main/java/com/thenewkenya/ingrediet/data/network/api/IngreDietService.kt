package com.thenewkenya.ingrediet.data.network.api

import android.content.Context
import android.util.Log
import com.thenewkenya.ingrediet.data.model.DetailedRecipe
import com.thenewkenya.ingrediet.data.model.IngredientItem
import com.thenewkenya.ingrediet.data.model.KenyanRecipe
import com.thenewkenya.ingrediet.data.model.KenyanIngredient
import com.thenewkenya.ingrediet.data.model.NutritionFacts
import com.thenewkenya.ingrediet.data.model.Recipe
import com.thenewkenya.ingrediet.data.network.supabase
import io.github.jan.supabase.functions.functions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.Json
import java.util.Collections.emptyList
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CancellationException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit
import com.thenewkenya.ingrediet.BuildConfig
import kotlinx.coroutines.flow.catch
import kotlin.math.absoluteValue
import kotlinx.serialization.json.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Service class for interacting with IngreDiet API endpoints
 */
class IngreDietService(private val context: Context) {
    private val TAG = "IngreDietService"
    private val json = Json { ignoreUnknownKeys = true }
    
    // Configure HTTP client for direct API calls
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    // Extension function to replace the missing invoke function
    private suspend fun io.github.jan.supabase.functions.Functions.callFunction(
        functionName: String,
        params: Map<String, Any?>
    ): String {
        // Since we're having issues with the API, use our direct HTTP implementation instead
        return callEdgeFunctionDirectly(params, functionName)
    }

    /**
     * Direct HTTP implementation for calling IngreDiet API endpoints
     */
    private suspend fun callEdgeFunctionDirectly(params: Map<String, Any?>, functionName: String = "getRecipes"): String {
        return withContext(Dispatchers.IO) {
            try {
                val url = "https://rfbbktdetqslycdcmgxg.supabase.co/functions/v1/$functionName"
                val jsonMediaType = "application/json; charset=utf-8".toMediaType()
                
                // Convert map to a simple JSON string manually
                val jsonBuilder = StringBuilder("{")
                params.entries.forEachIndexed { index, entry ->
                    val key = entry.key
                    val value = entry.value
                    
                    jsonBuilder.append("\"$key\":")
                    
                    when (value) {
                        is String -> jsonBuilder.append("\"$value\"")
                        is Number -> jsonBuilder.append(value)
                        is Boolean -> jsonBuilder.append(value)
                        is List<*> -> {
                            jsonBuilder.append("[")
                            value.forEachIndexed { i, item ->
                                if (item is String) {
                                    jsonBuilder.append("\"$item\"")
                                } else {
                                    jsonBuilder.append(item)
                                }
                                if (i < value.size - 1) jsonBuilder.append(",")
                            }
                            jsonBuilder.append("]")
                        }
                        null -> jsonBuilder.append("null")
                        else -> jsonBuilder.append("\"$value\"")
                    }
                    
                    if (index < params.size - 1) jsonBuilder.append(",")
                }
                jsonBuilder.append("}")
                
                val jsonString = jsonBuilder.toString()
                Log.d(TAG, "Request JSON for $functionName: $jsonString")
                val jsonBody = jsonString.toRequestBody(jsonMediaType)
                
                // Use the hardcoded Bearer token that works in the curl command
                val accessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InJmYmJrdGRldHFzbHljZGNtZ3hnIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDE2MzcwMDIsImV4cCI6MjA1NzIxMzAwMn0.RoojHme5WPcFY1QKXUeBWSuJRgwHiClzbQf3k5KmEag"
                
                Log.d(TAG, "Making request to $url with auth token")
                
                val request = Request.Builder()
                    .url(url)
                    .post(jsonBody)
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer $accessToken")
                    .build()
                
                try {
                    val response = httpClient.newCall(request).execute()
                    
                    // Log detailed error information for non-successful responses
                    if (!response.isSuccessful) {
                        val errorBody = response.body?.string() ?: "No error body"
                        Log.e(TAG, "Error response from API $functionName: ${response.code} - $errorBody")
                        
                        // For all non-successful responses, fall back to sample data
                        Log.w(TAG, "Falling back to empty data for $functionName due to server error")
                        return@withContext "[]" // Return empty array to be parsed as empty list
                    }
                    
                    val responseBody = response.body?.string() ?: throw IOException("Empty response body from $functionName")
                    
                    // Check if response is really valid JSON
                    if (responseBody.isBlank() || responseBody == "[]" || responseBody == "{}") {
                        Log.w(TAG, "Received blank or empty response from $functionName, using sample data")
                        return@withContext "[]"
                    }
                    
                    // Validate that response is valid JSON
                    try {
                        json.parseToJsonElement(responseBody)
                        Log.d(TAG, "Received valid JSON response from $functionName")
                    } catch (e: Exception) {
                        Log.e(TAG, "Invalid JSON response from $functionName: ${e.message}", e)
                        Log.w(TAG, "Falling back to sample data for $functionName due to invalid JSON")
                        return@withContext "[]"
                    }
                    
                    Log.d(TAG, "Received successful response from $functionName with body length: ${responseBody.length}")
                    return@withContext responseBody
                } catch (e: java.net.SocketTimeoutException) {
                    Log.e(TAG, "Timeout connecting to $functionName: ${e.message}", e)
                    Log.w(TAG, "Falling back to empty data for $functionName due to timeout")
                    return@withContext "[]" // Return empty array to be parsed as empty list
                } catch (e: java.io.IOException) {
                    Log.e(TAG, "I/O error for $functionName: ${e.message}", e)
                    Log.w(TAG, "Falling back to empty data for $functionName due to I/O error")
                    return@withContext "[]" // Return empty array to be parsed as empty list
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error calling $functionName API: ${e.message}", e)
                Log.w(TAG, "Falling back to empty data for $functionName due to exception")
                return@withContext "[]" // Return empty array to be parsed as empty list
            }
        }
    }

    /**
     * Search for recipes
     * @param query Search query
     * @param limit Maximum number of recipes to return
     * @return Flow of DetailedRecipe list
     */
    suspend fun searchRecipes(query: String, limit: Int = 10): Flow<List<DetailedRecipe>> = flow {
        Log.d(TAG, "Searching recipes with query: $query, limit: $limit")
        
        try {
            val response = supabase.functions.callFunction(
                "getRecipes",
                mapOf(
                    "query" to query,
                    "limit" to limit
                )
            )
            
            if (response.isBlank() || response == "[]") {
                Log.w(TAG, "Empty response from getRecipes for query: $query, using sample data")
                emit(getSampleRecipesByQuery(query))
                return@flow
            }
            
            // Validate that response is valid JSON
            val recipes = try {
                json.decodeFromString<List<RecipeDto>>(response)
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing API response for query: $query, using sample data", e)
                emit(getSampleRecipesByQuery(query))
                return@flow
            }
            
            if (recipes.isEmpty()) {
                Log.w(TAG, "No recipes found for query: $query, using sample data")
                emit(getSampleRecipesByQuery(query))
                return@flow
            }
            
            val detailedRecipes = recipes.take(limit).map { it.toDetailedRecipe() }
            Log.d(TAG, "Found ${detailedRecipes.size} recipes for query: $query")
            
            emit(detailedRecipes)
        } catch (e: Exception) {
            // Check if this is just a Flow abort exception (normal cancellation)
            if (e.message?.contains("Flow was aborted") == true || 
                e::class.java.name.contains("AbortFlowException")) {
                Log.d(TAG, "Recipe search flow completed normally")
                return@flow  // Just return, don't emit or rethrow
            }
            
            // Handle other exceptions based on their type
            if (e is CancellationException) {
                Log.d(TAG, "Recipe search cancelled")
                throw e  // Re-throw cancellation to properly cancel the flow
            } else {
                // Log and emit sample recipes for other errors
                Log.e(TAG, "Error searching recipes: ${e.message}", e)
                Log.w(TAG, "Falling back to sample data for query: $query")
                emit(getSampleRecipesByQuery(query))
            }
        }
    }

    /**
     * Get recipe details by ID
     * @param recipeId Recipe ID
     * @return Flow of DetailedRecipe
     */
    suspend fun getRecipeById(recipeId: String): Flow<DetailedRecipe?> = flow {
        Log.d(TAG, "Getting recipe details for ID: $recipeId")
        
        try {
            val response = supabase.functions.callFunction(
                "getRecipes",
                mapOf("id" to recipeId)
            )
            
            if (response.isBlank() || response == "[]") {
                Log.w(TAG, "Empty response for recipe ID: $recipeId, using sample data")
                // Generate a numeric ID from the string ID for sample recipe lookup
                val numericId = try {
                    // Try to use the last part of the UUID if it contains hyphens
                    val parts = recipeId.split("-")
                    if (parts.size > 1) {
                        // Take last part and convert to int, or fallback to hashCode
                        parts.last().toIntOrNull()?.takeIf { it > 0 } ?: recipeId.hashCode().absoluteValue % 100
                    } else {
                        // Try to convert directly to int, or fallback to hashCode
                        recipeId.toIntOrNull()?.takeIf { it > 0 } ?: recipeId.hashCode().absoluteValue % 100
                    }
                } catch (e: Exception) {
                    // Use a consistent hash of the ID string as fallback
                    recipeId.hashCode().absoluteValue % 100
                }
                val sampleRecipe = getSampleRecipeById(numericId.toString())
                emit(sampleRecipe)
                return@flow
            }
            
            // Validate that response is valid JSON
            val recipes = try {
                json.decodeFromString<List<RecipeDto>>(response)
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing API response for recipe ID: $recipeId, using sample data", e)
                // Generate a numeric ID for sample lookup
                val numericId = try {
                    val parts = recipeId.split("-")
                    if (parts.size > 1) {
                        parts.last().toIntOrNull()?.takeIf { it > 0 } ?: recipeId.hashCode().absoluteValue % 100
                    } else {
                        recipeId.toIntOrNull()?.takeIf { it > 0 } ?: recipeId.hashCode().absoluteValue % 100
                    }
                } catch (e: Exception) {
                    recipeId.hashCode().absoluteValue % 100
                }
                val sampleRecipe = getSampleRecipeById(numericId.toString())
                emit(sampleRecipe)
                return@flow
            }
            
            if (recipes.isNotEmpty()) {
                val recipe = recipes.first().toDetailedRecipe()
                Log.d(TAG, "Found recipe with ID: $recipeId")
                emit(recipe)
            } else {
                Log.d(TAG, "No recipe found with ID: $recipeId, using sample data")
                // Generate a numeric ID for sample lookup
                val numericId = try {
                    val parts = recipeId.split("-")
                    if (parts.size > 1) {
                        parts.last().toIntOrNull()?.takeIf { it > 0 } ?: recipeId.hashCode().absoluteValue % 100
                    } else {
                        recipeId.toIntOrNull()?.takeIf { it > 0 } ?: recipeId.hashCode().absoluteValue % 100
                    }
                } catch (e: Exception) {
                    recipeId.hashCode().absoluteValue % 100
                }
                val sampleRecipe = getSampleRecipeById(numericId.toString())
                emit(sampleRecipe)
            }
        } catch (e: Exception) {
            // Check if this is just a Flow abort exception (normal cancellation)
            if (e.message?.contains("Flow was aborted") == true || 
                e::class.java.name.contains("AbortFlowException")) {
                Log.d(TAG, "Recipe details flow completed normally")
                return@flow  // Just return, don't emit or rethrow
            }
            
            // Only emit null if it's not a cancellation
            if (e !is CancellationException) {
                Log.e(TAG, "Error getting recipe details: ${e.message}", e)
                Log.w(TAG, "Falling back to sample data for recipe ID: $recipeId")
                // Generate a numeric ID for sample lookup
                val numericId = try {
                    val parts = recipeId.split("-")
                    if (parts.size > 1) {
                        parts.last().toIntOrNull()?.takeIf { it > 0 } ?: recipeId.hashCode().absoluteValue % 100
                    } else {
                        recipeId.toIntOrNull()?.takeIf { it > 0 } ?: recipeId.hashCode().absoluteValue % 100
                    }
                } catch (e: Exception) {
                    recipeId.hashCode().absoluteValue % 100
                }
                val sampleRecipe = getSampleRecipeById(numericId.toString())
                emit(sampleRecipe)
            } else {
                // Re-throw CancellationException to maintain proper flow collection cancellation
                Log.d(TAG, "Recipe details fetch cancelled")
                throw e
            }
        }
    }

    /**
     * Get random recipes
     * @param count Number of random recipes to fetch
     * @return Flow of DetailedRecipe list
     */
    suspend fun getRandomRecipes(count: Int = 10): Flow<List<DetailedRecipe>> = flow {
        Log.d(TAG, "Getting $count random recipes")
        
        try {
            val response = supabase.functions.callFunction(
                "getRecipes",
                mapOf(
                    "random" to true,
                    "limit" to count
                )
            )
            
            if (response.isBlank() || response == "[]") {
                Log.w(TAG, "Empty response for random recipes, using sample data")
                emit(getSampleRecipes(count))
                return@flow
            }
            
            // Validate that response is valid JSON
            val recipes = try {
                json.decodeFromString<List<RecipeDto>>(response)
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing API response for random recipes, using sample data", e)
                emit(getSampleRecipes(count))
                return@flow
            }
            
            if (recipes.isEmpty()) {
                Log.w(TAG, "No random recipes found, using sample data")
                emit(getSampleRecipes(count))
                return@flow
            }
            
            val detailedRecipes = recipes.take(count).map { it.toDetailedRecipe() }
            Log.d(TAG, "Found ${detailedRecipes.size} random recipes")
            
            emit(detailedRecipes)
        } catch (e: Exception) {
            // Check if this is just a Flow abort exception (normal cancellation)
            if (e.message?.contains("Flow was aborted") == true || 
                e::class.java.name.contains("AbortFlowException")) {
                Log.d(TAG, "Random recipes flow completed normally (abort)")
                emit(emptyList())
                return@flow
            }
            
            // Handle other exceptions based on their type
            if (e is CancellationException) {
                Log.d(TAG, "Random recipes fetch cancelled")
                throw e  // Re-throw cancellation to properly cancel the flow
            } else {
                // Log and emit sample recipes for other errors
                Log.e(TAG, "Error getting random recipes: ${e.message}", e)
                Log.w(TAG, "Falling back to sample data for random recipes")
                emit(getSampleRecipes(count))
            }
        }
    }

    /**
     * Get recipes by ingredients
     * @param ingredients List of ingredients to search for
     * @param limit Maximum number of recipes to return
     * @return Flow of DetailedRecipe list
     */
    suspend fun getRecipesByIngredients(ingredients: List<String>, limit: Int = 10): Flow<List<DetailedRecipe>> = flow {
        Log.d(TAG, "Getting recipes by ingredients: $ingredients")
        
        try {
            val response = supabase.functions.callFunction(
                "getRecipes",
                mapOf(
                    "ingredients" to ingredients,
                    "limit" to limit
                )
            )
            
            if (response.isBlank() || response == "[]") {
                Log.w(TAG, "Empty response for recipes by ingredients: $ingredients, using sample data")
                emit(getSampleRecipesByIngredients(ingredients))
                return@flow
            }
            
            // Validate that response is valid JSON
            val recipes = try {
                json.decodeFromString<List<RecipeDto>>(response)
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing API response for ingredients: $ingredients, using sample data", e)
                emit(getSampleRecipesByIngredients(ingredients))
                return@flow
            }
            
            if (recipes.isEmpty()) {
                Log.w(TAG, "No recipes found for ingredients: $ingredients, using sample data")
                emit(getSampleRecipesByIngredients(ingredients))
                return@flow
            }
            
            val detailedRecipes = recipes.take(limit).map { it.toDetailedRecipe() }
            Log.d(TAG, "Found ${detailedRecipes.size} recipes for ingredients: $ingredients")
            
            emit(detailedRecipes)
        } catch (e: Exception) {
            // Check if this is just a Flow abort exception (normal cancellation)
            if (e.message?.contains("Flow was aborted") == true || 
                e::class.java.name.contains("AbortFlowException")) {
                Log.d(TAG, "Ingredients recipe search flow completed normally")
                return@flow  // Just return, don't emit or rethrow
            }
            
            // Handle other exceptions based on their type
            if (e is CancellationException) {
                Log.d(TAG, "Ingredients recipe search cancelled")
                throw e  // Re-throw cancellation to properly cancel the flow
            } else {
                // Log and emit sample recipes for other errors
                Log.e(TAG, "Error getting recipes by ingredients: ${e.message}", e)
                Log.w(TAG, "Falling back to sample data for ingredients: $ingredients")
                emit(getSampleRecipesByIngredients(ingredients))
            }
        }
    }

    /**
     * Direct HTTP implementation for searching recipes
     */
    suspend fun searchRecipesDirectHttp(query: String, limit: Int = 10): Flow<List<DetailedRecipe>> = flow {
        Log.d(TAG, "Searching recipes via direct HTTP with query: $query, limit: $limit")
        
        try {
            val params = mapOf(
                "query" to query,
                "limit" to limit
            )
            
            val response = callEdgeFunctionDirectly(params, "getRecipes")
            
            if (response.isBlank() || response == "[]") {
                Log.w(TAG, "Empty response from direct HTTP call for query: $query, using sample data")
                emit(getSampleRecipesByQuery(query))
                return@flow
            }
            
            // Safely parse the JSON response
            val recipes = try {
                json.decodeFromString<List<RecipeDto>>(response)
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing API response for direct HTTP query: $query, using sample data", e)
                emit(getSampleRecipesByQuery(query))
                return@flow
            }
            
            if (recipes.isEmpty()) {
                Log.w(TAG, "No recipes found for direct HTTP query: $query, using sample data")
                emit(getSampleRecipesByQuery(query))
                return@flow
            }
            
            val detailedRecipes = recipes.take(limit).map { it.toDetailedRecipe() }
            Log.d(TAG, "Found ${detailedRecipes.size} recipes for query: $query via direct HTTP")
            
            emit(detailedRecipes)
        } catch (e: Exception) {
            // Check if this is just a Flow abort exception (normal cancellation)
            if (e.message?.contains("Flow was aborted") == true || 
                e::class.java.name.contains("AbortFlowException")) {
                Log.d(TAG, "Direct HTTP recipe search flow completed normally")
                return@flow  // Just return, don't emit or rethrow
            }
            
            // Handle other exceptions based on their type
            if (e is CancellationException) {
                Log.d(TAG, "Direct HTTP recipe search cancelled")
                throw e  // Re-throw cancellation to properly cancel the flow
            } else {
                // Log and emit sample recipes for other errors
                Log.e(TAG, "Error searching recipes via direct HTTP: ${e.message}", e)
                Log.w(TAG, "Falling back to sample data for direct HTTP query: $query")
                emit(getSampleRecipesByQuery(query))
            }
        }
    }

    /**
     * Get all recipes with optional limit
     * @param limit The maximum number of recipes to return
     * @return Flow of DetailedRecipe lists
     */
    fun getRecipes(limit: Int = 20): Flow<List<DetailedRecipe>> = flow {
        Log.d(TAG, "Fetching recipes with limit: $limit")
        try {
            val params = mapOf(
                "limit" to limit
            )
            
            val response = supabase.functions.callFunction(
                functionName = "getRecipes",
                params = params
            )
            
            Log.d(TAG, "General recipes response: $response")
            
            if (response.isBlank() || response == "[]") {
                Log.w(TAG, "Empty response from getRecipes, using local sample data")
                emit(getSampleRecipes(limit))
                return@flow
            }
            
            val recipes = json.decodeFromString<List<RecipeDto>>(response)
            if (recipes.isEmpty()) {
                Log.w(TAG, "No recipes returned from API, using local sample data")
                emit(getSampleRecipes(limit))
            } else {
                emit(recipes.map { it.toDetailedRecipe() })
            }
        } catch (e: CancellationException) {
            Log.d(TAG, "Recipes fetch cancelled")
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching recipes: ${e.message}", e)
            Log.w(TAG, "Falling back to local sample data for recipes")
            emit(getSampleRecipes(limit))
        }
    }

    /**
     * DTO to map API response to our model
     */
    @Serializable
    private data class RecipeDto(
        @Serializable(with = FlexibleIDSerializer::class)
        val id: String,
        val name: String,
        val description: String? = "",
        @SerialName("image_url") val imageUrl: String? = "",
        @SerialName("preparation_time") val preparationTime: Int? = 15,
        @SerialName("cooking_time") val cookingTime: Int? = 15,
        val servings: Int? = 4,
        val difficulty: String? = "Medium",
        val ingredients: List<IngredientDto>? = emptyList(),
        val instructions: List<String>? = emptyList(),
        val nutrition: NutritionDto? = null,
        val tags: List<String>? = emptyList(),
        val category: String? = "",
        val author: String? = "",
        @SerialName("date_added") val dateAdded: String? = "",
        @SerialName("cuisine_type") val cuisineType: String? = "",
        @SerialName("dietary_info") val dietaryInfo: List<String>? = emptyList()
    ) {
        fun toDetailedRecipe(): DetailedRecipe {
            // No need to convert to numeric ID anymore, just use the string ID directly
            return DetailedRecipe(
                id = this.id,
                name = this.name,
                description = this.description ?: "",
                imageUrl = this.imageUrl ?: "",
                preparationTime = this.preparationTime ?: 15,
                cookingTime = this.cookingTime ?: 15,
                servings = this.servings ?: 4,
                difficulty = this.difficulty ?: "Medium",
                ingredients = this.ingredients?.map { it.toIngredientItem() } ?: emptyList(),
                instructions = this.instructions ?: emptyList(),
                nutritionFacts = this.nutrition?.toNutritionFacts() ?: NutritionFacts(
                    calories = 0,
                    protein = 0f,
                    carbs = 0f,
                    fat = 0f
                ),
                tags = this.tags ?: emptyList(),
                category = this.category ?: "",
                author = this.author ?: "",
                dateAdded = this.dateAdded ?: "",
                cuisineType = this.cuisineType ?: "",
                dietaryInfo = this.dietaryInfo ?: emptyList()
            )
        }
    }

    @Serializable
    private data class IngredientDto(
        @Serializable(with = FlexibleIDSerializer::class)
        val id: String,
        val name: String,
        val quantity: Float? = 1f,
        val unit: String? = "",
        val calories: Int? = null,
        @SerialName("image_url") val imageUrl: String? = null,
        val alternatives: List<String>? = emptyList()
    ) {
        fun toIngredientItem(): IngredientItem {
            return IngredientItem(
                id = this.id,
                name = this.name,
                quantity = this.quantity ?: 1f,
                unit = this.unit ?: "",
                calories = this.calories,
                imageUrl = this.imageUrl,
                alternatives = this.alternatives ?: emptyList()
            )
        }
    }

    @Serializable
    private data class NutritionDto(
        val calories: Int? = 0,
        val protein: Float? = 0f,
        val carbs: Float? = 0f,
        val fat: Float? = 0f,
        val fiber: Float? = null,
        val sugar: Float? = null,
        val sodium: Float? = null,
        val cholesterol: Float? = null,
        val vitamins: Map<String, Float>? = emptyMap(),
        val minerals: Map<String, Float>? = emptyMap(),
        @SerialName("daily_value_percentage") val dailyValuePercentage: Map<String, Int>? = emptyMap()
    ) {
        fun toNutritionFacts(): NutritionFacts {
            return NutritionFacts(
                calories = this.calories ?: 0,
                protein = this.protein ?: 0f,
                carbs = this.carbs ?: 0f,
                fat = this.fat ?: 0f,
                fiber = this.fiber,
                sugar = this.sugar,
                sodium = this.sodium,
                cholesterol = this.cholesterol,
                vitamins = this.vitamins ?: emptyMap(),
                minerals = this.minerals ?: emptyMap(),
                dailyValuePercentage = this.dailyValuePercentage ?: emptyMap()
            )
        }
    }

    /**
     * Get Kenyan recipes with pagination
     * @param limit The maximum number of recipes to return
     * @return Flow of KenyanRecipe lists
     */
    fun getKenyanRecipes(limit: Int = 10): Flow<List<KenyanRecipe>> = flow {
        Log.d(TAG, "Fetching Kenyan recipes with limit: $limit")
        try {
            val params = mapOf(
                "limit" to limit
            )
            
            val response = supabase.functions.callFunction(
                functionName = "getKenyanRecipes",
                params = params
            )
            
            Log.d(TAG, "Kenyan recipes response: $response")
            
            if (response.isBlank() || response == "[]") {
                Log.w(TAG, "Empty response from getKenyanRecipes, using local sample data")
                emit(getSampleKenyanRecipes())
                return@flow
            }
            
            val recipes = json.decodeFromString<List<KenyanRecipe>>(response)
            if (recipes.isEmpty()) {
                Log.w(TAG, "No Kenyan recipes returned from API, using local sample data")
                emit(getSampleKenyanRecipes())
            } else {
                emit(recipes)
            }
        } catch (e: CancellationException) {
            Log.d(TAG, "Kenyan recipes fetch cancelled")
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching Kenyan recipes: ${e.message}", e)
            Log.w(TAG, "Falling back to local sample data for Kenyan recipes")
            emit(getSampleKenyanRecipes())
        }
    }
    
    /**
     * Get a specific Kenyan recipe by ID
     * @param recipeId The recipe ID
     * @return The KenyanRecipe or null if not found
     */
    suspend fun getKenyanRecipeById(recipeId: String): KenyanRecipe? {
        return try {
            Log.d(TAG, "Getting Kenyan recipe by ID: $recipeId")
            
            // For direct edge function call
            val params = mapOf(
                "id" to recipeId
            )
            
            val response = supabase.functions.callFunction(
                functionName = "getKenyanRecipes",
                params = params
            )
            
            // Convert the JSON response to a KenyanRecipe object
            if (response.isNotEmpty()) {
                try {
                    // Try to parse as a list first
                    val recipes = json.decodeFromString<List<KenyanRecipe>>(response)
                    recipes.firstOrNull()
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing Kenyan recipe response as list: ${e.message}", e)
                    // Try parsing as a single object
                    try {
                        json.decodeFromString<KenyanRecipe>(response)
                    } catch (e2: Exception) {
                        Log.e(TAG, "Error parsing Kenyan recipe response as single object: ${e2.message}", e2)
                        null
                    }
                }
            } else {
                Log.w(TAG, "Empty response from getKenyanRecipes for ID: $recipeId")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting Kenyan recipe by ID: $recipeId", e)
            null
        }
    }
    
    /**
     * Get Kenyan recipes by region
     * @param region The region to filter by
     * @param limit The maximum number of recipes to return
     * @return Flow of KenyanRecipe lists
     */
    fun getKenyanRecipesByRegion(region: String, limit: Int = 10): Flow<List<KenyanRecipe>> = flow {
        Log.d(TAG, "Fetching Kenyan recipes for region: $region with limit: $limit")
        try {
            val params = mapOf(
                "region" to region,
                "limit" to limit
            )
            
            val response = supabase.functions.callFunction(
                functionName = "getKenyanRecipes",
                params = params
            )
            
            Log.d(TAG, "Kenyan recipes by region response: $response")
            
            if (response.isBlank() || response == "[]") {
                Log.w(TAG, "Empty response for Kenyan recipes by region: $region, using sample data")
                emit(getSampleKenyanRecipesByRegion(region))
                return@flow
            }
            
            val recipes = json.decodeFromString<List<KenyanRecipe>>(response)
            if (recipes.isEmpty()) {
                Log.w(TAG, "No Kenyan recipes found for region: $region, using sample data")
                emit(getSampleKenyanRecipesByRegion(region))
            } else {
                emit(recipes)
            }
        } catch (e: CancellationException) {
            Log.d(TAG, "Kenyan recipes by region fetch cancelled")
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching Kenyan recipes by region: ${e.message}", e)
            emit(getSampleKenyanRecipesByRegion(region))
        }
    }
    
    /**
     * Search for Kenyan recipes
     * @param query The search query
     * @param limit The maximum number of recipes to return
     * @return Flow of KenyanRecipe lists
     */
    fun searchKenyanRecipes(query: String, limit: Int = 10): Flow<List<KenyanRecipe>> = flow {
        Log.d(TAG, "Searching Kenyan recipes with query: $query and limit: $limit")
        try {
            val params = mapOf(
                "query" to query,
                "limit" to limit
            )
            
            val response = supabase.functions.callFunction(
                functionName = "getKenyanRecipes",
                params = params
            )
            
            Log.d(TAG, "Kenyan recipes search response: $response")
            
            if (response.isBlank() || response == "[]") {
                Log.w(TAG, "Empty response for Kenyan recipes search: $query, using sample data")
                emit(searchSampleKenyanRecipes(query))
                return@flow
            }
            
            val recipes = json.decodeFromString<List<KenyanRecipe>>(response)
            if (recipes.isEmpty()) {
                Log.w(TAG, "No Kenyan recipes found for query: $query, using sample data")
                emit(searchSampleKenyanRecipes(query))
            } else {
                emit(recipes)
            }
        } catch (e: CancellationException) {
            Log.d(TAG, "Kenyan recipes search cancelled")
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error searching Kenyan recipes: ${e.message}", e)
            emit(searchSampleKenyanRecipes(query))
        }
    }
    
    /**
     * Get a list of sample Kenyan recipes for when the API is unavailable
     */
    private fun getSampleKenyanRecipes(): List<KenyanRecipe> {
        return listOf(
            KenyanRecipe(
                id = "1001",
                name = "Ugali",
                description = "A staple food in Kenya made from maize flour and water, similar to polenta but firmer.",
                imageUrl = "https://example.com/ugali.jpg",
                preparationTime = 5,
                cookingTime = 15,
                servings = 4,
                difficulty = "Easy",
                region = "National",
                calories = 150,
                ingredients = listOf(
                    KenyanIngredient(id = "1", name = "Water", quantity = 2f, unit = "cups", orderIndex = 1),
                    KenyanIngredient(id = "2", name = "Maize Flour", quantity = 2f, unit = "cups", orderIndex = 2)
                ),
                instructions = listOf(
                    "Bring water to a boil in a pot.",
                    "Gradually add maize flour while stirring continuously to avoid lumps.",
                    "Continue stirring until the mixture becomes thick and pulls away from the sides of the pot.",
                    "Cover and let it cook for a few more minutes.",
                    "Serve hot with stew or vegetables."
                ),
                tags = listOf("Staple", "Vegetarian", "Basic")
            ),
            KenyanRecipe(
                id = "1002",
                name = "Nyama Choma",
                description = "Grilled meat, typically goat or beef, seasoned with salt and sometimes served with a spicy sauce.",
                imageUrl = "https://example.com/nyama_choma.jpg",
                preparationTime = 30,
                cookingTime = 60,
                servings = 6,
                difficulty = "Medium",
                region = "National",
                calories = 450,
                ingredients = listOf(
                    KenyanIngredient(id = "1", name = "Beef or Goat Meat", quantity = 1f, unit = "kg", orderIndex = 1),
                    KenyanIngredient(id = "2", name = "Salt", quantity = 2f, unit = "tbsp", orderIndex = 2),
                    KenyanIngredient(id = "3", name = "Black Pepper", quantity = 1f, unit = "tbsp", orderIndex = 3)
                ),
                instructions = listOf(
                    "Cut the meat into large chunks.",
                    "Season with salt and pepper.",
                    "Grill over open flame or hot charcoal until cooked through.",
                    "Serve hot with kachumbari (tomato and onion salad)."
                ),
                tags = listOf("Meat", "Grilled", "Party")
            ),
            KenyanRecipe(
                id = "1003",
                name = "Sukuma Wiki",
                description = "A vegetable dish made with kale or collard greens, often served with ugali.",
                imageUrl = "https://example.com/sukuma_wiki.jpg",
                preparationTime = 10,
                cookingTime = 15,
                servings = 4,
                difficulty = "Easy",
                region = "National",
                calories = 120,
                ingredients = listOf(
                    KenyanIngredient(id = "1", name = "Kale", quantity = 500f, unit = "g", orderIndex = 1),
                    KenyanIngredient(id = "2", name = "Onion", quantity = 1f, unit = "medium", orderIndex = 2),
                    KenyanIngredient(id = "3", name = "Tomato", quantity = 2f, unit = "medium", orderIndex = 3),
                    KenyanIngredient(id = "4", name = "Oil", quantity = 2f, unit = "tbsp", orderIndex = 4),
                    KenyanIngredient(id = "5", name = "Salt", quantity = 1f, unit = "tsp", orderIndex = 5)
                ),
                instructions = listOf(
                    "Chop the kale, onion, and tomatoes.",
                    "Heat oil in a pan and fry the onions until translucent.",
                    "Add tomatoes and cook until soft.",
                    "Add kale and salt, stir well.",
                    "Cover and simmer for about 10 minutes, stirring occasionally.",
                    "Serve hot with ugali."
                ),
                tags = listOf("Vegetable", "Vegetarian", "Healthy")
            ),
            KenyanRecipe(
                id = "1004",
                name = "Nyama Choma",
                description = "Grilled meat, usually goat or beef, seasoned with salt and sometimes spices.",
                imageUrl = "https://example.com/nyamachoma.jpg",
                preparationTime = 30,
                cookingTime = 60,
                servings = 6,
                difficulty = "Medium",
                region = "National",
                calories = 300,
                ingredients = listOf(
                    KenyanIngredient(
                        id = "4",
                        name = "Goat meat or beef",
                        quantity = 1.0f,
                        unit = "kg",
                        orderIndex = 1
                    ),
                    KenyanIngredient(
                        id = "5",
                        name = "Salt",
                        quantity = 2.0f,
                        unit = "tsp",
                        orderIndex = 2
                    ),
                    KenyanIngredient(
                        id = "6",
                        name = "Black pepper",
                        quantity = 1.0f,
                        unit = "tsp",
                        orderIndex = 3
                    )
                ),
                instructions = listOf(
                    "Cut meat into pieces",
                    "Season with salt and pepper",
                    "Grill over open fire or charcoal until cooked through",
                    "Serve hot with kachumbari"
                ),
                tags = listOf("Kenyan", "National", "Meat", "Grilled")
            ),
            KenyanRecipe(
                id = "1005",
                name = "Sukuma Wiki",
                description = "A simple dish made with collard greens, onions, and tomatoes.",
                imageUrl = "https://example.com/sukumawiki.jpg",
                preparationTime = 10,
                cookingTime = 15,
                servings = 4,
                difficulty = "Easy",
                region = "National",
                calories = 80,
                ingredients = listOf(
                    KenyanIngredient(
                        id = "7",
                        name = "Collard greens (kale)",
                        quantity = 500.0f,
                        unit = "g",
                        orderIndex = 1
                    ),
                    KenyanIngredient(
                        id = "8",
                        name = "Onions",
                        quantity = 1.0f,
                        unit = "medium",
                        orderIndex = 2
                    ),
                    KenyanIngredient(
                        id = "9",
                        name = "Tomatoes",
                        quantity = 2.0f,
                        unit = "medium",
                        orderIndex = 3
                    )
                ),
                instructions = listOf(
                    "Chop collard greens into small pieces",
                    "Dice onions and tomatoes",
                    "Sauté onions in oil until translucent",
                    "Add tomatoes and cook until soft",
                    "Add collard greens and salt",
                    "Cook until greens are tender but still bright green"
                ),
                tags = listOf("Kenyan", "National", "Vegetable", "Healthy")
            )
        )
    }
    
    /**
     * Get a sample Kenyan recipe by ID
     * @param id Recipe ID
     * @return Sample KenyanRecipe or null if ID is not found
     */
    private fun getSampleKenyanRecipeById(id: String): KenyanRecipe? {
        // Try to parse the ID as an integer for legacy handling
        val idInt = id.toIntOrNull()
        
        // If we can't parse it as an Int, try to find it as a string in the sample data
        if (idInt == null) {
            return getSampleKenyanRecipes().find { it.id == id }
        }
        
        // Legacy numeric ID handling
        return when (idInt) {
            301 -> getSampleKenyanRecipes()[0]
            302 -> getSampleKenyanRecipes()[1]
            303 -> getSampleKenyanRecipes()[2]
            304 -> getSampleKenyanRecipes()[3]
            305 -> getSampleKenyanRecipes()[4]
            else -> getSampleKenyanRecipes().firstOrNull()
        }
    }
    
    /**
     * Get sample Kenyan recipes by region
     */
    private fun getSampleKenyanRecipesByRegion(region: String): List<KenyanRecipe> {
        return getSampleKenyanRecipes().filter { 
            it.region.equals(region, ignoreCase = true) || 
            (region.equals("All Regions", ignoreCase = true))
        }
    }
    
    /**
     * Search for sample Kenyan recipes by query
     */
    private fun searchSampleKenyanRecipes(query: String): List<KenyanRecipe> {
        val lowerQuery = query.lowercase()
        return getSampleKenyanRecipes().filter { recipe ->
            recipe.name.lowercase().contains(lowerQuery) ||
            recipe.description.lowercase().contains(lowerQuery) ||
            recipe.tags.any { it.lowercase().contains(lowerQuery) } ||
            recipe.ingredients.any { it.name.lowercase().contains(lowerQuery) }
        }
    }

    /**
     * Get sample recipes for fallback when the API is unavailable
     */
    private fun getSampleRecipes(count: Int = 10): List<DetailedRecipe> {
        val sampleRecipes = listOf(
            DetailedRecipe(
                id = "101",
                name = "Spaghetti Carbonara",
                description = "Classic Italian pasta dish with eggs, cheese, pancetta and black pepper.",
                imageUrl = "https://example.com/spaghetti_carbonara.jpg",
                preparationTime = 15,
                cookingTime = 15,
                servings = 4,
                difficulty = "Medium",
                ingredients = listOf(
                    IngredientItem(
                        id = "1001",
                        name = "Spaghetti",
                        quantity = 400f,
                        unit = "g"
                    ),
                    IngredientItem(
                        id = "1002",
                        name = "Eggs",
                        quantity = 4f,
                        unit = "large"
                    ),
                    IngredientItem(
                        id = "1003",
                        name = "Pancetta",
                        quantity = 150f,
                        unit = "g"
                    ),
                    IngredientItem(
                        id = "1004",
                        name = "Parmesan cheese",
                        quantity = 50f,
                        unit = "g"
                    ),
                    IngredientItem(
                        id = "1005",
                        name = "Black pepper",
                        quantity = 1f,
                        unit = "tsp"
                    )
                ),
                instructions = listOf(
                    "Cook pasta according to package instructions.",
                    "Fry pancetta until crisp.",
                    "Beat eggs with cheese and pepper.",
                    "Drain pasta and immediately add to eggs, stirring quickly.",
                    "Add pancetta and serve immediately."
                ),
                nutritionFacts = NutritionFacts(
                    calories = 450,
                    protein = 20f,
                    carbs = 50f,
                    fat = 18f
                ),
                tags = listOf("Italian", "Pasta", "Quick"),
                category = "Italian",
                cuisineType = "Italian"
            ),
            DetailedRecipe(
                id = "102",
                name = "Chicken Tikka Masala",
                description = "Popular Indian curry dish with marinated chicken in a spiced curry sauce.",
                imageUrl = "https://example.com/chicken_tikka_masala.jpg",
                preparationTime = 30,
                cookingTime = 45,
                servings = 6,
                difficulty = "Medium",
                ingredients = listOf(
                    IngredientItem(
                        id = "1006",
                        name = "Chicken breast",
                        quantity = 750f,
                        unit = "g"
                    ),
                    IngredientItem(
                        id = "1007",
                        name = "Yogurt",
                        quantity = 200f,
                        unit = "g"
                    ),
                    IngredientItem(
                        id = "1008",
                        name = "Garam masala",
                        quantity = 2f,
                        unit = "tbsp"
                    ),
                    IngredientItem(
                        id = "1009",
                        name = "Tomato sauce",
                        quantity = 400f,
                        unit = "g"
                    ),
                    IngredientItem(
                        id = "1010",
                        name = "Heavy cream",
                        quantity = 100f,
                        unit = "ml"
                    )
                ),
                instructions = listOf(
                    "Marinate chicken in yogurt and spices for at least 1 hour.",
                    "Grill or bake chicken until cooked through.",
                    "Prepare sauce with tomatoes, cream and spices.",
                    "Add cooked chicken to sauce and simmer for 10 minutes.",
                    "Serve with rice or naan bread."
                ),
                nutritionFacts = NutritionFacts(
                    calories = 320,
                    protein = 28f,
                    carbs = 15f,
                    fat = 18f
                ),
                tags = listOf("Indian", "Curry", "Chicken"),
                category = "Indian",
                cuisineType = "Indian"
            ),
            DetailedRecipe(
                id = "103",
                name = "Avocado Toast",
                description = "Simple and nutritious breakfast with mashed avocado on toasted bread.",
                imageUrl = "https://example.com/avocado_toast.jpg",
                preparationTime = 10,
                cookingTime = 5,
                servings = 2,
                difficulty = "Easy",
                ingredients = listOf(
                    IngredientItem(
                        id = "1011",
                        name = "Bread",
                        quantity = 2f,
                        unit = "slices"
                    ),
                    IngredientItem(
                        id = "1012",
                        name = "Avocado",
                        quantity = 1f,
                        unit = "large"
                    ),
                    IngredientItem(
                        id = "1013",
                        name = "Lemon juice",
                        quantity = 1f,
                        unit = "tsp"
                    ),
                    IngredientItem(
                        id = "1014",
                        name = "Salt",
                        quantity = 0.5f,
                        unit = "tsp"
                    ),
                    IngredientItem(
                        id = "1015",
                        name = "Red pepper flakes",
                        quantity = 0.5f,
                        unit = "tsp"
                    )
                ),
                instructions = listOf(
                    "Toast bread until golden and crisp.",
                    "Mash avocado with lemon juice and salt.",
                    "Spread avocado on toast.",
                    "Sprinkle with red pepper flakes.",
                    "Optional: top with a poached egg."
                ),
                nutritionFacts = NutritionFacts(
                    calories = 250,
                    protein = 6f,
                    carbs = 25f,
                    fat = 15f
                ),
                tags = listOf("Breakfast", "Vegetarian", "Quick"),
                category = "Breakfast",
                cuisineType = "American"
            ),
            DetailedRecipe(
                id = "104",
                name = "Mandazi (East African Donuts)",
                description = "Popular East African sweet, triangular-shaped fried bread that's similar to a donut. Often enjoyed with tea.",
                imageUrl = "https://example.com/mandazi.jpg",
                preparationTime = 30,
                cookingTime = 20,
                servings = 8,
                difficulty = "Medium",
                ingredients = listOf(
                    IngredientItem(
                        id = "1016",
                        name = "All-purpose flour",
                        quantity = 3f,
                        unit = "cups"
                    ),
                    IngredientItem(
                        id = "1017",
                        name = "Sugar",
                        quantity = 0.5f,
                        unit = "cup"
                    ),
                    IngredientItem(
                        id = "1018",
                        name = "Coconut milk",
                        quantity = 1f,
                        unit = "cup"
                    ),
                    IngredientItem(
                        id = "1019",
                        name = "Egg",
                        quantity = 1f,
                        unit = ""
                    ),
                    IngredientItem(
                        id = "1020",
                        name = "Baking powder",
                        quantity = 2f,
                        unit = "tsp"
                    ),
                    IngredientItem(
                        id = "1021",
                        name = "Cardamom powder",
                        quantity = 0.5f,
                        unit = "tsp"
                    ),
                    IngredientItem(
                        id = "1022",
                        name = "Vegetable oil",
                        quantity = 2f,
                        unit = "cups"
                    )
                ),
                instructions = listOf(
                    "Mix flour, sugar, baking powder, and cardamom in a bowl.",
                    "Beat egg and add coconut milk, then add to dry ingredients.",
                    "Knead into a soft dough and let rest for 15 minutes.",
                    "Roll out dough and cut into triangles.",
                    "Deep fry in oil until golden brown on both sides.",
                    "Drain on paper towels and serve warm with tea."
                ),
                nutritionFacts = NutritionFacts(
                    calories = 220,
                    protein = 4f,
                    carbs = 30f,
                    fat = 10f
                ),
                tags = listOf("African", "Breakfast", "Snack"),
                category = "African",
                cuisineType = "East African"
            )
        )
        
        return sampleRecipes.take(count)
    }
    
    /**
     * Get a sample recipe by ID
     */
    private fun getSampleRecipeById(id: String): DetailedRecipe? {
        return getSampleRecipes(20).find { it.id == id }
    }
    
    /**
     * Get sample recipes by query
     */
    private fun getSampleRecipesByQuery(query: String): List<DetailedRecipe> {
        val lowerQuery = query.lowercase()
        
        // Special handling for empty results - always return something
        val filteredRecipes = getSampleRecipes(20).filter { recipe ->
            recipe.name.lowercase().contains(lowerQuery) ||
            recipe.description.lowercase().contains(lowerQuery) ||
            recipe.tags.any { it.lowercase().contains(lowerQuery) } ||
            recipe.ingredients.any { it.name.lowercase().contains(lowerQuery) }
        }
        
        return if (filteredRecipes.isEmpty()) {
            // Return at least 3 random recipes if nothing matched the query
            getSampleRecipes(3)
        } else {
            filteredRecipes
        }
    }
    
    /**
     * Get sample recipes by ingredients
     */
    private fun getSampleRecipesByIngredients(ingredients: List<String>): List<DetailedRecipe> {
        val lowerIngredients = ingredients.map { it.lowercase() }
        
        // Special handling for empty results - always return something
        val filteredRecipes = getSampleRecipes(20).filter { recipe ->
            recipe.ingredients.any { ingredient ->
                lowerIngredients.any { loweredIngredient ->
                    ingredient.name.lowercase().contains(loweredIngredient)
                }
            }
        }
        
        return if (filteredRecipes.isEmpty()) {
            // Return at least 3 random recipes if nothing matched the ingredients
            getSampleRecipes(3)
        } else {
            filteredRecipes
        }
    }
}

/**
 * Custom serializer for IDs that could be either strings or integers
 */
object FlexibleIDSerializer : KSerializer<String> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("FlexibleID", PrimitiveKind.STRING)
    
    override fun serialize(encoder: Encoder, value: String) {
        encoder.encodeString(value)
    }
    
    override fun deserialize(decoder: Decoder): String {
        return when (decoder) {
            is JsonDecoder -> {
                val element = decoder.decodeJsonElement()
                when {
                    element is JsonPrimitive && element.isString -> element.content
                    element is JsonPrimitive -> element.toString()
                    else -> ""
                }
            }
            else -> decoder.decodeString()
        }
    }
} 