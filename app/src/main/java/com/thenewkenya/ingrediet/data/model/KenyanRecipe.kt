package com.thenewkenya.ingrediet.data.model

import kotlinx.serialization.Serializable

/**
 * Model class for Kenyan recipes
 */
@Serializable
data class KenyanRecipe(
    val id: String,
    val name: String,
    val description: String,
    val imageUrl: String = "",
    val preparationTime: Int, // in minutes
    val cookingTime: Int, // in minutes
    val totalTime: Int = preparationTime + cookingTime, // in minutes
    val servings: Int = 4,
    val difficulty: String = "Medium", // "Easy", "Medium", "Hard"
    val region: String = "Traditional", // "Central", "Coastal", "Nyanza", etc.
    val calories: Int = 0,
    val ingredients: List<KenyanIngredient> = emptyList(),
    val instructions: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    /**
     * Convert to a DetailedRecipe for compatibility with existing UIs
     */
    fun toDetailedRecipe(): DetailedRecipe {
        return DetailedRecipe(
            id = this.id,
            name = this.name,
            description = this.description,
            imageUrl = this.imageUrl,
            preparationTime = this.preparationTime,
            cookingTime = this.cookingTime,
            servings = this.servings,
            difficulty = this.difficulty,
            ingredients = this.ingredients.map { it.toIngredientItem() },
            instructions = this.instructions,
            nutritionFacts = NutritionFacts(
                calories = this.calories,
                protein = 0f, // Not available in Kenyan recipes schema
                carbs = 0f,   // Not available in Kenyan recipes schema
                fat = 0f      // Not available in Kenyan recipes schema
            ),
            tags = this.tags,
            category = "Kenyan",
            cuisineType = this.region,
            dietaryInfo = emptyList() // Not available in Kenyan recipes schema
        )
    }
}

/**
 * Model class for Kenyan recipe ingredients
 */
@Serializable
data class KenyanIngredient(
    val id: String,
    val name: String,
    val quantity: Float,
    val unit: String,
    val orderIndex: Int
) {
    /**
     * Convert to an IngredientItem for compatibility with existing UIs
     */
    fun toIngredientItem(): IngredientItem {
        return IngredientItem(
            id = this.id,
            name = this.name,
            quantity = this.quantity,
            unit = this.unit,
            calories = null, // Not available in Kenyan recipes schema
            imageUrl = null, // Not available in Kenyan recipes schema
            alternatives = emptyList() // Not available in Kenyan recipes schema
        )
    }
} 