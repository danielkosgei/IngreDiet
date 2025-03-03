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
    val servings: Int,
    val difficulty: String, // "Easy", "Medium", "Hard"
    val ingredients: List<IngredientItem>,
    val instructions: List<String>,
    val nutritionFacts: NutritionFacts,
    val tags: List<String>,
    val isFavorite: Boolean = false
)

@Serializable
data class IngredientItem(
    val id: Int,
    val name: String,
    val quantity: Float,
    val unit: String
)

@Serializable
data class NutritionFacts(
    val calories: Int,
    val protein: Float, // grams
    val carbs: Float, // grams
    val fat: Float, // grams
    val fiber: Float? = null, // grams
    val sugar: Float? = null // grams
)