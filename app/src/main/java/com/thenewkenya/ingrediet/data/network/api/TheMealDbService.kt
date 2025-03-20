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
 * Service class for interacting with TheMealDB API
 * TheMealDB is a free API with no request limits
 */
class TheMealDbService {
    private val json = Json { ignoreUnknownKeys = true }
    private val baseUrl = "https://www.themealdb.com/api/json/v1/1"

    /**
     * Search for meals by name
     */
    suspend fun searchMealsByName(query: String): List<DetailedRecipe> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/search.php?s=$query")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val searchResponse = json.decodeFromString<MealSearchResponse>(response)
            
            // Search endpoint returns full meal details
            return@withContext searchResponse.meals?.map { meal -> meal.toDetailedRecipe() } ?: emptyList()
        } catch (e: Exception) {
            Log.e("TheMealDbService", "Error searching meals: ${e.message}", e)
            return@withContext emptyList()
        }
    }
    
    /**
     * Get meal details by ID
     */
    suspend fun getMealById(id: String): DetailedRecipe? = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/lookup.php?i=$id")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val searchResponse = json.decodeFromString<MealSearchResponse>(response)
            
            return@withContext searchResponse.meals?.firstOrNull()?.toDetailedRecipe()
        } catch (e: Exception) {
            Log.e("TheMealDbService", "Error getting meal details: ${e.message}", e)
            return@withContext null
        }
    }
    
    /**
     * Get random meal
     */
    suspend fun getRandomMeal(): DetailedRecipe? = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/random.php")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val searchResponse = json.decodeFromString<MealSearchResponse>(response)
            
            return@withContext searchResponse.meals?.firstOrNull()?.toDetailedRecipe()
        } catch (e: Exception) {
            Log.e("TheMealDbService", "Error getting random meal: ${e.message}", e)
            return@withContext null
        }
    }
    
    /**
     * List all meal categories
     */
    suspend fun getCategories(): List<String> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/categories.php")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val categoriesResponse = json.decodeFromString<CategoryResponse>(response)
            
            return@withContext categoriesResponse.categories?.map { it.strCategory } ?: emptyList()
        } catch (e: Exception) {
            Log.e("TheMealDbService", "Error getting categories: ${e.message}", e)
            return@withContext emptyList()
        }
    }
    
    /**
     * Filter meals by category
     */
    suspend fun filterByCategory(category: String): List<DetailedRecipe> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/filter.php?c=$category")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val filterResponse = json.decodeFromString<MealFilterResponse>(response)
            
            // Filter response now contains full recipe details
            return@withContext filterResponse.meals?.map { meal -> meal.toDetailedRecipe() } ?: emptyList()
        } catch (e: Exception) {
            Log.e("TheMealDbService", "Error filtering by category: ${e.message}", e)
            return@withContext emptyList()
        }
    }
}

// Data Transfer Objects for TheMealDB API
@Serializable
data class MealSearchResponse(
    val meals: List<MealDto>?
)

@Serializable
data class MealFilterResponse(
    val meals: List<MealDto>?
)

@Serializable
data class MealSummaryDto(
    val idMeal: String,
    val strMeal: String,
    val strMealThumb: String,
    val strCategory: String? = null,
    val strArea: String? = null,
    val strTags: String? = null
) {
    fun toDetailedRecipe(): DetailedRecipe {
        // Parse and clean all tags
        val allTags = mutableSetOf<String>()
        
        // Add category if available
        strCategory?.takeIf { it.isNotBlank() }?.let { allTags.add(it) }
        
        // Add area/cuisine if available
        strArea?.takeIf { it.isNotBlank() }?.let { allTags.add(it) }
        
        // Add additional tags from strTags, excluding duplicates
        strTags?.split(",")
            ?.asSequence()
            ?.map { it.trim() }
            ?.filter { tag ->
                tag.isNotBlank() &&
                !allTags.any { existing -> existing.equals(tag, ignoreCase = true) }
            }
            ?.toList()
            ?.let { allTags.addAll(it) }
        
        return DetailedRecipe(
            id = idMeal.toIntOrNull() ?: 0,
            name = strMeal,
            description = "",
            imageUrl = strMealThumb,
            preparationTime = 15,
            cookingTime = 30,
            servings = 4,
            difficulty = "Medium",
            ingredients = emptyList(),
            instructions = emptyList(),
            nutritionFacts = NutritionFacts(
                calories = 0,
                protein = 0f,
                carbs = 0f,
                fat = 0f
            ),
            tags = allTags.toList(),
            category = strCategory ?: "",
            author = "",
            dateAdded = "",
            cuisineType = strArea ?: ""
        )
    }
}

@Serializable
data class CategoryResponse(
    val categories: List<CategoryDto>?
)

@Serializable
data class CategoryDto(
    val idCategory: String,
    val strCategory: String,
    val strCategoryThumb: String,
    val strCategoryDescription: String
)

@Serializable
data class MealDto(
    val idMeal: String,
    val strMeal: String,
    val strDrinkAlternate: String? = null,
    val strCategory: String? = null,
    val strArea: String? = null,
    val strInstructions: String? = null,
    val strMealThumb: String? = null,
    val strTags: String? = null,
    val strYoutube: String? = null,
    val strIngredient1: String? = null,
    val strIngredient2: String? = null,
    val strIngredient3: String? = null,
    val strIngredient4: String? = null,
    val strIngredient5: String? = null,
    val strIngredient6: String? = null,
    val strIngredient7: String? = null,
    val strIngredient8: String? = null,
    val strIngredient9: String? = null,
    val strIngredient10: String? = null,
    val strIngredient11: String? = null,
    val strIngredient12: String? = null,
    val strIngredient13: String? = null,
    val strIngredient14: String? = null,
    val strIngredient15: String? = null,
    val strIngredient16: String? = null,
    val strIngredient17: String? = null,
    val strIngredient18: String? = null,
    val strIngredient19: String? = null,
    val strIngredient20: String? = null,
    val strMeasure1: String? = null,
    val strMeasure2: String? = null,
    val strMeasure3: String? = null,
    val strMeasure4: String? = null,
    val strMeasure5: String? = null,
    val strMeasure6: String? = null,
    val strMeasure7: String? = null,
    val strMeasure8: String? = null,
    val strMeasure9: String? = null,
    val strMeasure10: String? = null,
    val strMeasure11: String? = null,
    val strMeasure12: String? = null,
    val strMeasure13: String? = null,
    val strMeasure14: String? = null,
    val strMeasure15: String? = null,
    val strMeasure16: String? = null,
    val strMeasure17: String? = null,
    val strMeasure18: String? = null,
    val strMeasure19: String? = null,
    val strMeasure20: String? = null,
    val strSource: String? = null,
    val strImageSource: String? = null,
    val strCreativeCommonsConfirmed: String? = null,
    val dateModified: String? = null
) {
    fun toDetailedRecipe(): DetailedRecipe {
        // Parse instructions into a list
        val instructionsList = strInstructions?.split("\r\n", "\n")
            ?.filter { it.isNotBlank() }
            ?: emptyList()
            
        // Parse and clean all tags
        val allTags = mutableSetOf<String>()
        
        // Add category if available
        strCategory?.takeIf { it.isNotBlank() }?.let { allTags.add(it) }
        
        // Add area/cuisine if available
        strArea?.takeIf { it.isNotBlank() }?.let { allTags.add(it) }
        
        // Add additional tags from strTags, excluding duplicates
        strTags?.split(",")
            ?.asSequence()
            ?.map { it.trim() }
            ?.filter { tag ->
                tag.isNotBlank() &&
                !allTags.any { existing -> existing.equals(tag, ignoreCase = true) }
            }
            ?.toList()
            ?.let { allTags.addAll(it) }
        
        val tagsList = allTags.toList()
            
        // Extract ingredients and measures
        val ingredients = mutableListOf<IngredientItem>()
        
        // Helper function to add ingredients
        fun addIngredient(index: Int, ingredientName: String?, measure: String?) {
            if (!ingredientName.isNullOrBlank() && !measure.isNullOrBlank()) {
                ingredients.add(
                    IngredientItem(
                        id = index,
                        name = ingredientName,
                        quantity = parseQuantity(measure),
                        unit = parseUnit(measure)
                    )
                )
            }
        }
        
        // Add all ingredients
        addIngredient(1, strIngredient1, strMeasure1)
        addIngredient(2, strIngredient2, strMeasure2)
        addIngredient(3, strIngredient3, strMeasure3)
        addIngredient(4, strIngredient4, strMeasure4)
        addIngredient(5, strIngredient5, strMeasure5)
        addIngredient(6, strIngredient6, strMeasure6)
        addIngredient(7, strIngredient7, strMeasure7)
        addIngredient(8, strIngredient8, strMeasure8)
        addIngredient(9, strIngredient9, strMeasure9)
        addIngredient(10, strIngredient10, strMeasure10)
        addIngredient(11, strIngredient11, strMeasure11)
        addIngredient(12, strIngredient12, strMeasure12)
        addIngredient(13, strIngredient13, strMeasure13)
        addIngredient(14, strIngredient14, strMeasure14)
        addIngredient(15, strIngredient15, strMeasure15)
        addIngredient(16, strIngredient16, strMeasure16)
        addIngredient(17, strIngredient17, strMeasure17)
        addIngredient(18, strIngredient18, strMeasure18)
        addIngredient(19, strIngredient19, strMeasure19)
        addIngredient(20, strIngredient20, strMeasure20)
        
        // Create a default nutrition facts object
        // Note: TheMealDB doesn't provide nutrition information
        val nutritionFacts = NutritionFacts(
            calories = 0,
            protein = 0f,
            carbs = 0f,
            fat = 0f
        )
        
        return DetailedRecipe(
            id = idMeal.toIntOrNull() ?: 0,
            name = strMeal,
            description = instructionsList.firstOrNull() ?: "",
            imageUrl = strMealThumb ?: "",
            preparationTime = 15, // Default values since TheMealDB doesn't provide these
            cookingTime = 30,
            servings = 4,
            difficulty = "Medium",
            ingredients = ingredients,
            instructions = instructionsList,
            nutritionFacts = nutritionFacts,
            tags = tagsList,
            category = strCategory ?: "",
            author = "",
            dateAdded = dateModified ?: "",
            cuisineType = strArea ?: ""
        )
    }
    
    // Helper function to parse quantity from measure string
    private fun parseQuantity(measure: String): Float {
        val quantityRegex = """(\d+(?:\.\d+)?)""".toRegex()
        val match = quantityRegex.find(measure)
        return match?.groupValues?.get(1)?.toFloatOrNull() ?: 1f
    }
    
    // Helper function to parse unit from measure string
    private fun parseUnit(measure: String): String {
        val unitRegex = """(?:\d+(?:\.\d+)?\s*)(\w+)""".toRegex()
        val match = unitRegex.find(measure)
        return match?.groupValues?.get(1) ?: "unit"
    }
}
