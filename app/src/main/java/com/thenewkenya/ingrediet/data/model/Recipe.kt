package com.thenewkenya.ingrediet.data.model

import kotlinx.serialization.Serializable

@Serializable
data class DetailedRecipe(
    val id: Int,
    val name: String,
    val description: String,
    val imageUrl: String = "",
    val preparationTime: Int, // in minutes
    val cookingTime: Int, // in minutes
    val totalTime: Int = preparationTime + cookingTime, // in minutes
    val servings: Int,
    val difficulty: String, // "Easy", "Medium", "Hard"
    val ingredients: List<IngredientItem>,
    val instructions: List<String>,
    val nutritionFacts: NutritionFacts,
    val tags: List<String>,
    val isFavorite: Boolean = false,
    val rating: Float = 0f,
    val category: String = "",
    val author: String = "",
    val dateAdded: String = "",
    val cuisineType: String = "",
    val dietaryInfo: List<String> = emptyList() // e.g., "Vegetarian", "Gluten-Free", etc.
)

@Serializable
data class IngredientItem(
    val id: Int,
    val name: String,
    val quantity: Float,
    val unit: String,
    val calories: Int? = null,
    val imageUrl: String? = null,
    val alternatives: List<String> = emptyList()
)

@Serializable
data class NutritionFacts(
    val calories: Int,
    val protein: Float, // grams
    val carbs: Float, // grams
    val fat: Float, // grams
    val fiber: Float? = null, // grams
    val sugar: Float? = null, // grams
    val sodium: Float? = null, // mg
    val cholesterol: Float? = null, // mg
    val vitamins: Map<String, Float> = emptyMap(),
    val minerals: Map<String, Float> = emptyMap(),
    val dailyValuePercentage: Map<String, Int> = emptyMap()
)