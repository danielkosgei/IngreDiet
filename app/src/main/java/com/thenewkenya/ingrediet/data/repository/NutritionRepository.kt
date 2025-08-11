package com.thenewkenya.ingrediet.data.repository

import android.content.Context
import android.util.Log
import com.thenewkenya.ingrediet.data.model.IngredientNutrition
import com.thenewkenya.ingrediet.data.model.NutritionFacts
import com.thenewkenya.ingrediet.data.network.CacheManager
import com.thenewkenya.ingrediet.data.network.api.OpenFoodFactsService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

class NutritionRepository(context: Context) {
    private val cache = CacheManager(context)
    private val off = OpenFoodFactsService()

    private fun normalizeName(name: String): String = name
        .lowercase(Locale.getDefault())
        .replace(Regex("\\(.*?\\)"), "") // remove parentheses content
        .replace(Regex("[^a-z0-9\\s-]"), "")
        .replace(Regex("\\s+"), " ")
        .trim()

    private val synonyms: Map<String, List<String>> = mapOf(
        "spring onion" to listOf("green onion", "scallion"),
        "scallion" to listOf("green onion", "spring onion"),
        "bell pepper" to listOf("capsicum"),
        "courgette" to listOf("zucchini"),
        "aubergine" to listOf("eggplant"),
        "cilantro" to listOf("coriander"),
        "garbanzo" to listOf("chickpea"),
        "brown sugar" to listOf("sugar"),
        "powdered sugar" to listOf("icing sugar", "confectioners sugar"),
        "bicarbonate of soda" to listOf("baking soda"),
        "maize flour" to listOf("corn flour", "cornmeal")
    )

    private fun candidateQueries(norm: String): List<String> {
        val base = norm
        val singular = if (base.endsWith("s")) base.removeSuffix("s") else base
        val list = mutableSetOf(base, singular)
        synonyms[base]?.let { list.addAll(it) }
        synonyms[singular]?.let { list.addAll(it) }
        return list.toList()
    }

    // Approximate per 100g nutrition for common ingredients as a fallback
    private val fallbackPer100g: Map<String, NutritionFacts> = mapOf(
        // calories, protein g, carbs g, fat g
        "onion" to NutritionFacts(40, 1f, 9f, 0.1f),
        "tomato" to NutritionFacts(18, 0.9f, 3.9f, 0.2f),
        "garlic" to NutritionFacts(149, 6.4f, 33f, 0.5f),
        "potato" to NutritionFacts(77, 2f, 17f, 0.1f),
        "carrot" to NutritionFacts(41, 0.9f, 10f, 0.2f),
        "banana" to NutritionFacts(89, 1.1f, 23f, 0.3f),
        "rice" to NutritionFacts(130, 2.7f, 28f, 0.3f),
        "white rice" to NutritionFacts(130, 2.7f, 28f, 0.3f),
        "brown rice" to NutritionFacts(123, 2.7f, 25.6f, 1f),
        "flour" to NutritionFacts(364, 10f, 76f, 1f),
        "sugar" to NutritionFacts(387, 0f, 100f, 0f),
        "olive oil" to NutritionFacts(884, 0f, 0f, 100f),
        "chicken" to NutritionFacts(239, 27f, 0f, 14f),
        "beef" to NutritionFacts(250, 26f, 0f, 15f),
        "milk" to NutritionFacts(42, 3.4f, 5f, 1f),
        "egg" to NutritionFacts(155, 13f, 1.1f, 11f),
        "tomatoes" to NutritionFacts(18, 0.9f, 3.9f, 0.2f),
        "onions" to NutritionFacts(40, 1f, 9f, 0.1f)
    )

    private fun fallbackFor(norm: String): NutritionFacts? {
        // Try exact key, or any fallback key contained in the name
        fallbackPer100g[norm]?.let { return it }
        return fallbackPer100g.entries.firstOrNull { (k, _) -> norm.contains(k) }?.value
    }

    suspend fun getNutritionByName(name: String): IngredientNutrition? = withContext(Dispatchers.IO) {
        val norm = normalizeName(name)
        try {
            // Check nutrition cache first
            cache.getCachedIngredientNutrition(norm)?.let { return@withContext it }

            // Lookup via OpenFoodFacts by multiple candidate queries
            val queries = candidateQueries(norm)
            for (q in queries) {
                val offResult = off.getNutritionFactsByName(q)
                if (offResult != null) {
                    val nutrition = IngredientNutrition(
                        nameNormalized = norm,
                        per100g = offResult.nutritionFacts,
                        imageUrl = offResult.imageUrl
                    )
                    cache.cacheIngredientNutrition(norm, nutrition)
                    return@withContext nutrition
                }
            }
            // Fallback even if no OFF results
            fallbackFor(norm)?.let { fb ->
                val nutrition = IngredientNutrition(
                    nameNormalized = norm,
                    per100g = fb,
                    imageUrl = null
                )
                cache.cacheIngredientNutrition(norm, nutrition)
                return@withContext nutrition
            }
            null
        } catch (e: Exception) {
            Log.e("NutritionRepository", "Error fetching nutrition for $name: ${e.message}", e)
            null
        }
    }
} 