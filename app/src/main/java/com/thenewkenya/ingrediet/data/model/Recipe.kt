package com.thenewkenya.ingrediet.data.model

import kotlinx.serialization.Serializable
import java.util.UUID
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull

@Serializable
data class DetailedRecipe(
    val id: String,
    val recipeId: String = "",
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
) {
    fun toRecipe(): Recipe {
        return Recipe(
            id = this.id,
            recipeId = this.recipeId,
            name = this.name,
            description = this.description,
            imageUrl = this.imageUrl,
            preparationTime = this.preparationTime,
            cookingTime = this.cookingTime,
            servings = this.servings,
            difficulty = this.difficulty,
            ingredients = this.ingredients,
            instructions = this.instructions,
            nutritionFacts = this.nutritionFacts,
            tags = this.tags,
            isFavorite = this.isFavorite,
            rating = this.rating,
            category = this.category,
            author = this.author,
            dateAdded = this.dateAdded,
            cuisineType = this.cuisineType,
            dietaryInfo = this.dietaryInfo
        )
    }
}

@Serializable
data class IngredientItem(
    val id: String,
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
) {
    // Secondary constructor for simplified creation
    constructor(
        calories: Int,
        protein: Int,
        carbs: Int,
        fat: Int
    ) : this(
        calories = calories,
        protein = protein.toFloat(),
        carbs = carbs.toFloat(),
        fat = fat.toFloat()
    )
    
    // Calculate progress for each nutrient, ensuring values are between 0 and 1
    fun getCaloriesProgress(): Float = (calories.toFloat() / 2000).coerceIn(0f, 1f)
    fun getProteinProgress(): Float = (protein / 50).coerceIn(0f, 1f)
    fun getCarbsProgress(): Float = (carbs / 300).coerceIn(0f, 1f)
    fun getFatProgress(): Float = (fat / 65).coerceIn(0f, 1f)
    fun getFiberProgress(): Float = (fiber?.div(25) ?: 0f).coerceIn(0f, 1f)
    fun getSugarProgress(): Float = (sugar?.div(25) ?: 0f).coerceIn(0f, 1f)
    
    // Format values for display
    fun getFormattedCalories(): String = calories.toString()
    fun getFormattedProtein(): String = "${protein.toInt()}g"
    fun getFormattedCarbs(): String = "${carbs.toInt()}g"
    fun getFormattedFat(): String = "${fat.toInt()}g"
    fun getFormattedFiber(): String? = fiber?.let { "${it.toInt()}g" }
    fun getFormattedSugar(): String? = sugar?.let { "${it.toInt()}g" }
}

@Serializable
data class Recipe(
    val id: String,
    val recipeId: String = "",
    val name: String,
    val description: String,
    val imageUrl: String,
    val preparationTime: Int, // in minutes
    val cookingTime: Int, // in minutes
    val servings: Int,
    val difficulty: String, // "Easy", "Medium", "Hard"
    val ingredients: List<IngredientItem>,
    val instructions: List<String>,
    val nutritionFacts: NutritionFacts,
    val tags: List<String>,
    val isFavorite: Boolean,
    val rating: Float,
    val category: String,
    val author: String,
    val dateAdded: String,
    val cuisineType: String,
    val dietaryInfo: List<String>
) {
    // Secondary constructor for simplified recipe creation from favorites
    constructor(
        id: String,
        name: String,
        description: String,
        imageUrl: String,
        cookingTime: Int,
        nutritionFacts: NutritionFacts,
        ingredients: List<IngredientItem> = emptyList(),
        instructions: List<String> = emptyList()
    ) : this(
        id = id,
        recipeId = id,
        name = name,
        description = description,
        imageUrl = imageUrl,
        preparationTime = 15, // default
        cookingTime = cookingTime,
        servings = 4, // default
        difficulty = "Medium", // default
        ingredients = ingredients,
        instructions = instructions,
        nutritionFacts = nutritionFacts,
        tags = emptyList(),
        isFavorite = true, // Since it's coming from favorites
        rating = 0f,
        category = "",
        author = "",
        dateAdded = "",
        cuisineType = "",
        dietaryInfo = emptyList()
    )
    
    fun toDetailedRecipe(): DetailedRecipe {
        return DetailedRecipe(
            id = this.id,
            recipeId = this.recipeId,
            name = this.name,
            description = this.description,
            imageUrl = this.imageUrl,
            preparationTime = this.preparationTime,
            cookingTime = this.cookingTime,
            servings = this.servings,
            difficulty = this.difficulty,
            ingredients = this.ingredients,
            instructions = this.instructions,
            nutritionFacts = this.nutritionFacts,
            tags = this.tags,
            isFavorite = this.isFavorite,
            rating = this.rating,
            category = this.category,
            author = this.author,
            dateAdded = this.dateAdded,
            cuisineType = this.cuisineType,
            dietaryInfo = this.dietaryInfo
        )
    }
}

@Serializable
data class RecipeDto(
    val id: String = "",
    val name: String = "",
    val description: String? = "",
    val image_url: String? = null,
    val category: String? = null,
    val ingredients: JsonElement? = null,
    val instructions: String? = "",
    val preparation_time: Int? = null,
    val cooking_time: Int? = null,
    val servings: Int? = null,
    val difficulty: String? = null,
    val tags: List<String>? = null
) {
    fun toDetailedRecipe(): DetailedRecipe {
        // Now that we fetch ingredients separately, we use an empty list here
        // The RecipeRepository will populate this with data from the recipe_ingredients table
        val parsedIngredients = emptyList<IngredientItem>()

        // Parse instructions
        val parsedInstructions = (instructions ?: "")
            .split(Regex("\\d+\\.\\s+"))
            .filter { it.isNotBlank() }

        // Create a basic nutrition facts object
        val nutritionFacts = NutritionFacts(
            calories = 0,
            protein = 0f,
            carbs = 0f,
            fat = 0f
        )

        return DetailedRecipe(
            id = id,
            name = name,
            description = description ?: "",
            imageUrl = image_url ?: "",
            preparationTime = preparation_time ?: 15,
            cookingTime = cooking_time ?: 30,
            servings = servings ?: 4,
            difficulty = difficulty ?: "Medium",
            ingredients = parsedIngredients,
            instructions = parsedInstructions,
            nutritionFacts = nutritionFacts,
            tags = tags ?: listOf(category ?: ""),
            category = category ?: ""
        )
    }
}