package com.thenewkenya.ingrediet.data.network.api

import android.util.Log
import com.thenewkenya.ingrediet.data.model.DetailedRecipe
import com.thenewkenya.ingrediet.data.model.IngredientItem
import com.thenewkenya.ingrediet.data.model.NutritionFacts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL

/**
 * Service class for interacting with Spoonacular API
 */
class SpoonacularService {
    private val json = Json { ignoreUnknownKeys = true }
    private val baseUrl = "https://api.spoonacular.com"
    private val apiKey = "8633f6e288b441c48e43a51c3c968d78"

    /**
     * Search for recipes by query
     */
    suspend fun searchRecipes(query: String): List<DetailedRecipe> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/recipes/complexSearch?apiKey=$apiKey&query=$query&addRecipeInformation=true")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val searchResponse = json.decodeFromString<SpoonacularSearchResponse>(response)
            
            return@withContext searchResponse.results.map { it.toDetailedRecipe() }
        } catch (e: Exception) {
            Log.e("SpoonacularService", "Error searching recipes: ${e.message}", e)
            return@withContext emptyList()
        }
    }

    /**
     * Get random recipes
     */
    suspend fun getRandomRecipes(number: Int = 10): List<DetailedRecipe> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/recipes/random?apiKey=$apiKey&number=$number")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val randomResponse = json.decodeFromString<SpoonacularRandomResponse>(response)
            
            return@withContext randomResponse.recipes.map { it.toDetailedRecipe() }
        } catch (e: Exception) {
            Log.e("SpoonacularService", "Error getting random recipes: ${e.message}", e)
            return@withContext emptyList()
        }
    }

    /**
     * Get recipe by ID
     */
    suspend fun getRecipeById(id: Int): DetailedRecipe? = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/recipes/$id/information?apiKey=$apiKey")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val recipe = json.decodeFromString<SpoonacularRecipe>(response)
            
            return@withContext recipe.toDetailedRecipe()
        } catch (e: Exception) {
            Log.e("SpoonacularService", "Error getting recipe details: ${e.message}", e)
            return@withContext null
        }
    }
}

@Serializable
data class SpoonacularSearchResponse(
    val results: List<SpoonacularRecipe>,
    val offset: Int,
    val number: Int,
    val totalResults: Int
)

@Serializable
data class SpoonacularRandomResponse(
    val recipes: List<SpoonacularRecipe>
)

@Serializable
data class SpoonacularRecipe(
    val id: Int,
    val title: String,
    val image: String,
    @SerialName("readyInMinutes")
    val totalTime: Int = 0,
    val servings: Int = 4,
    @SerialName("extendedIngredients")
    val ingredients: List<SpoonacularIngredient> = emptyList(),
    val analyzedInstructions: List<SpoonacularInstructions> = emptyList(),
    val cuisines: List<String> = emptyList(),
    val dishTypes: List<String> = emptyList(),
    val diets: List<String> = emptyList(),
    val summary: String = "",
    val nutrition: SpoonacularNutrition? = null
) {
    fun toDetailedRecipe(): DetailedRecipe {
        val allTags = mutableListOf<String>()
        allTags.addAll(cuisines)
        allTags.addAll(dishTypes)
        allTags.addAll(diets)

        val instructions = analyzedInstructions
            .flatMap { it.steps }
            .map { it.step }

        return DetailedRecipe(
            id = id,
            name = title,
            description = summary,
            imageUrl = image,
            preparationTime = totalTime / 2, // Estimate prep time as half of total time
            cookingTime = totalTime / 2,     // Estimate cooking time as half of total time
            servings = servings,
            difficulty = when {
                totalTime <= 30 -> "Easy"
                totalTime <= 60 -> "Medium"
                else -> "Hard"
            },
            ingredients = ingredients.map { it.toIngredientItem() },
            instructions = instructions,
            nutritionFacts = nutrition?.toNutritionFacts() ?: NutritionFacts(0, 0f, 0f, 0f),
            tags = allTags,
            category = dishTypes.firstOrNull() ?: "",
            author = "",
            dateAdded = "",
            cuisineType = cuisines.firstOrNull() ?: ""
        )
    }
}

@Serializable
data class SpoonacularIngredient(
    val id: Int,
    val name: String,
    val amount: Double,
    val unit: String,
    val original: String
) {
    fun toIngredientItem(): IngredientItem {
        return IngredientItem(
            id = id,
            name = name,
            quantity = amount.toFloat(),
            unit = unit,
            imageUrl = "https://spoonacular.com/cdn/ingredients_100x100/$name.jpg"
        )
    }
}

@Serializable
data class SpoonacularInstructions(
    val name: String = "",
    val steps: List<SpoonacularStep>
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
