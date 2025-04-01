package com.thenewkenya.ingrediet.data.network.api

import android.util.Log
import com.thenewkenya.ingrediet.data.model.IngredientItem
import com.thenewkenya.ingrediet.data.model.NutritionFacts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Service class for interacting with Open Food Facts API
 * Open Food Facts is a free API with no request limits
 */
class OpenFoodFactsService {
    private val json = Json { ignoreUnknownKeys = true }
    private val baseUrl = "https://world.openfoodfacts.org/api/v0"

    /**
     * Search for ingredients by name
     */
    suspend fun searchIngredients(query: String): List<IngredientItem> = withContext(Dispatchers.IO) {
        try {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val url = URL("$baseUrl/search?search_terms=$encodedQuery&page_size=10")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val searchResponse = json.decodeFromString<ProductSearchResponse>(response)
            
            return@withContext searchResponse.products.mapIndexed { index, product ->
                IngredientItem(
                    id = index.toString(),
                    name = product.productName ?: query,
                    quantity = 1f,
                    unit = "unit",
                    calories = product.nutriments?.energyKcal100g?.toInt(),
                    imageUrl = product.imageUrl,
                    alternatives = emptyList()
                )
            }
        } catch (e: Exception) {
            Log.e("OpenFoodFactsService", "Error searching ingredients: ${e.message}", e)
            return@withContext emptyList()
        }
    }
    
    /**
     * Get ingredient details by barcode
     */
    suspend fun getIngredientByBarcode(barcode: String): IngredientItem? = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/product/$barcode.json")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val productResponse = json.decodeFromString<ProductResponse>(response)
            
            if (productResponse.status == 1) {
                val product = productResponse.product
                return@withContext IngredientItem(
                    id = barcode,
                    name = product.productName ?: "Unknown",
                    quantity = 1f,
                    unit = "unit",
                    calories = product.nutriments?.energyKcal100g?.toInt(),
                    imageUrl = product.imageUrl,
                    alternatives = emptyList()
                )
            } else {
                return@withContext null
            }
        } catch (e: Exception) {
            Log.e("OpenFoodFactsService", "Error getting ingredient by barcode: ${e.message}", e)
            return@withContext null
        }
    }
    
    /**
     * Get nutrition facts for an ingredient
     */
    suspend fun getNutritionFacts(barcode: String): NutritionFacts? = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/product/$barcode.json")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val productResponse = json.decodeFromString<ProductResponse>(response)
            
            if (productResponse.status == 1 && productResponse.product.nutriments != null) {
                val nutriments = productResponse.product.nutriments
                return@withContext NutritionFacts(
                    calories = nutriments.energyKcal100g?.toInt() ?: 0,
                    protein = nutriments.proteins100g ?: 0f,
                    carbs = nutriments.carbohydrates100g ?: 0f,
                    fat = nutriments.fat100g ?: 0f,
                    fiber = nutriments.fiber100g,
                    sugar = nutriments.sugars100g,
                    sodium = nutriments.sodium100g,
                    cholesterol = null,
                    vitamins = mapOf(),
                    minerals = mapOf(),
                    dailyValuePercentage = mapOf()
                )
            } else {
                return@withContext null
            }
        } catch (e: Exception) {
            Log.e("OpenFoodFactsService", "Error getting nutrition facts: ${e.message}", e)
            return@withContext null
        }
    }
}

// Data Transfer Objects for Open Food Facts API
@Serializable
data class ProductSearchResponse(
    val count: Int,
    val page: Int,
    val pageCount: Int,
    val pageSize: Int,
    val products: List<ProductDto>,
    val skip: Int
)

@Serializable
data class ProductResponse(
    val code: String,
    val product: ProductDto,
    val status: Int,
    val statusVerbose: String
)

@Serializable
data class ProductDto(
    @SerialName("_id")
    val id: String? = null,
    @SerialName("product_name")
    val productName: String? = null,
    @SerialName("generic_name")
    val genericName: String? = null,
    val brands: String? = null,
    @SerialName("image_url")
    val imageUrl: String? = null,
    val nutriments: NutrimentsDto? = null,
    val ingredients: List<IngredientDto>? = null
)

@Serializable
data class IngredientDto(
    val id: String? = null,
    val text: String? = null,
    val rank: Int? = null,
    val percent: Float? = null
)

@Serializable
data class NutrimentsDto(
    @SerialName("energy-kcal_100g")
    val energyKcal100g: Float? = null,
    @SerialName("proteins_100g")
    val proteins100g: Float? = null,
    @SerialName("carbohydrates_100g")
    val carbohydrates100g: Float? = null,
    @SerialName("fat_100g")
    val fat100g: Float? = null,
    @SerialName("fiber_100g")
    val fiber100g: Float? = null,
    @SerialName("sugars_100g")
    val sugars100g: Float? = null,
    @SerialName("sodium_100g")
    val sodium100g: Float? = null
)
