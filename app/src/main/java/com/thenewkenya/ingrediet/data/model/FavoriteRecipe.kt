package com.thenewkenya.ingrediet.data.model

/**
 * Model class representing a favorite recipe.
 */
data class FavoriteRecipe(
    val id: String,
    val name: String,
    val description: String,
    val imageUrl: String,
    val cookingTime: Int,
    val nutritionFacts: NutritionFacts
) {
    /**
     * Nutrition facts for the recipe.
     */
    data class NutritionFacts(
        val calories: Int,
        val protein: Int,
        val carbs: Int,
        val fat: Int
    )
}

/**
 * Extension function to convert FavoriteRecipe to Recipe model
 * for use in the UI layer.
 */
fun FavoriteRecipe.toRecipe(): Recipe {
    return Recipe(
        id = this.id,
        name = this.name,
        description = this.description,
        imageUrl = this.imageUrl,
        cookingTime = this.cookingTime,
        nutritionFacts = com.thenewkenya.ingrediet.data.model.NutritionFacts(
            calories = this.nutritionFacts.calories,
            protein = this.nutritionFacts.protein,
            carbs = this.nutritionFacts.carbs,
            fat = this.nutritionFacts.fat
        )
    )
} 