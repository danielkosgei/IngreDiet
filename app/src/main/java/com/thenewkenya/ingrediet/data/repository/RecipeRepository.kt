package com.thenewkenya.ingrediet.data.repository

import android.util.Log
import com.thenewkenya.ingrediet.data.model.DetailedRecipe
import com.thenewkenya.ingrediet.data.model.IngredientItem
import com.thenewkenya.ingrediet.data.model.NutritionFacts
import com.thenewkenya.ingrediet.data.network.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.filter.PostgrestFilterBuilder
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable

class RecipeRepository {

    suspend fun validateRecipeData(recipeId: Int): Map<String, Boolean> {
        val result = mutableMapOf<String, Boolean>()

        try {
            // Log the process
            Log.d("RecipeRepository", "Validating recipe data for ID: $recipeId")

            // Check if recipe exists
            val recipeExists = try {
                val recipeList = supabase.from("recipes")
                    .select(columns = Columns.list("id")) {
                        filter { eq("id", recipeId) }
                    }
                    .decodeList<IdDto>() // Use our serializable DTO

                val exists = recipeList.isNotEmpty()
                Log.d("RecipeRepository", "Recipe $recipeId exists check: exists = $exists")
                exists
            } catch (e: Exception) {
                Log.e("RecipeRepository", "Error checking if recipe exists: ${e.message}", e)
                false
            }
            result["recipe_exists"] = recipeExists

            // Only check other data if recipe exists
            if (recipeExists) {
                // Check if recipe_ingredients entries exist
                val ingredientsExist = try {
                    val ingredientsList = supabase.from("recipe_ingredients")
                        .select(columns = Columns.list("id")) {
                            filter { eq("recipe_id", recipeId) }
                        }
                        .decodeList<IdDto>() // Use our serializable DTO

                    val exists = ingredientsList.isNotEmpty()
                    Log.d("RecipeRepository", "Recipe $recipeId ingredients check: exists = $exists")
                    exists
                } catch (e: Exception) {
                    Log.e("RecipeRepository", "Error checking ingredients: ${e.message}", e)
                    false
                }
                result["ingredients_exist"] = ingredientsExist

                // Check nutrition
                val nutritionExists = try {
                    val nutritionList = supabase.from("recipe_nutrition")
                        .select(columns = Columns.list("id")) {
                            filter { eq("recipe_id", recipeId) }
                        }
                        .decodeList<IdDto>() // Use our serializable DTO

                    val exists = nutritionList.isNotEmpty()
                    Log.d("RecipeRepository", "Recipe $recipeId nutrition check: exists = $exists")
                    exists
                } catch (e: Exception) {
                    Log.e("RecipeRepository", "Error checking nutrition: ${e.message}", e)
                    false
                }
                result["nutrition_exists"] = nutritionExists

                // Check instructions
                val instructionsExist = try {
                    val instructionsList = supabase.from("recipe_instructions")
                        .select(columns = Columns.list("id")) {
                            filter { eq("recipe_id", recipeId) }
                        }
                        .decodeList<IdDto>() // Use our serializable DTO

                    val exists = instructionsList.isNotEmpty()
                    Log.d("RecipeRepository", "Recipe $recipeId instructions check: exists = $exists")
                    exists
                } catch (e: Exception) {
                    Log.e("RecipeRepository", "Error checking instructions: ${e.message}", e)
                    false
                }
                result["instructions_exist"] = instructionsExist
            } else {
                // If recipe doesn't exist, other data doesn't exist either
                result["ingredients_exist"] = false
                result["nutrition_exists"] = false
                result["instructions_exist"] = false
            }

            Log.d("RecipeRepository", "Validation results for recipe $recipeId: $result")
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Error validating recipe data: ${e.message}", e)
            // Set all to false on error
            result["recipe_exists"] = false
            result["ingredients_exist"] = false
            result["nutrition_exists"] = false
            result["instructions_exist"] = false
        }

        return result
    }

    suspend fun getRecipeDetails(recipeId: Int): Flow<Result<DetailedRecipe>> = flow {
        try {
            Log.d("RecipeRepository", "Fetching recipe details for ID: $recipeId")

            // 1. Fetch basic recipe data
            val recipeList = supabase.from("recipes")
                .select() {
                    filter { eq("id", recipeId) }
                }
                .decodeList<RecipeDto>()

            if (recipeList.isEmpty()) {
                emit(Result.failure(Exception("Recipe not found")))
                return@flow
            }

            val recipeResponse = recipeList.first()

            // 2. Fetch ingredients
            val ingredients = supabase.from("recipe_ingredients")
                .select() {
                    filter { eq("recipe_id", recipeId) }
                }
                .decodeList<RecipeIngredientDto>()

            Log.d("RecipeRepository", "Found ${ingredients.size} ingredients for recipe $recipeId")

            // 3. Fetch ingredient details
            val ingredientDetails = mutableListOf<IngredientItem>()

            for (ingredient in ingredients) {
                try {
                    val ingredientDataList = supabase.from("ingredients")
                        .select() {
                            filter { eq("id", ingredient.ingredient_id) }
                        }
                        .decodeList<IngredientDto>()

                    if (ingredientDataList.isNotEmpty()) {
                        val ingredientData = ingredientDataList.first()
                        ingredientDetails.add(
                            IngredientItem(
                                id = ingredient.ingredient_id,
                                name = ingredientData.name,
                                quantity = ingredient.quantity,
                                unit = ingredient.unit
                            )
                        )
                    }
                } catch (e: Exception) {
                    Log.e("RecipeRepository", "Error fetching ingredient ${ingredient.ingredient_id}: ${e.message}", e)
                }
            }

            // 4. Fetch instructions
            val instructions = supabase.from("recipe_instructions")
                .select() {
                    filter { eq("recipe_id", recipeId) }
                    order("step_number", Order.ASCENDING)
                }
                .decodeList<RecipeInstructionDto>()
                .map { it.instruction }

            // 5. Fetch nutrition facts
            val nutritionList = supabase.from("recipe_nutrition")
                .select() {
                    filter { eq("recipe_id", recipeId) }
                }
                .decodeList<RecipeNutritionDto>()

            val nutritionResponse = if (nutritionList.isNotEmpty()) {
                nutritionList.first()
            } else {
                // Default nutrition data
                RecipeNutritionDto(
                    id = 0,
                    recipe_id = recipeId,
                    calories = 0,
                    protein = 0f,
                    carbs = 0f,
                    fat = 0f,
                    fiber = null,
                    sugar = null
                )
            }

            // 6. Check favorite status
            val isFavorite = try {
                val currentUser = supabase.auth.currentUserOrNull()?.id
                if (currentUser != null) {
                    // Use the proper DTO class for deserialization
                    val favoritesList = supabase.from("user_favorites")
                        .select(columns = Columns.list("id")) {
                            filter {
                                eq("user_id", currentUser)
                                eq("recipe_id", recipeId)
                            }
                        }
                        .decodeList<FavoriteDto>()

                    favoritesList.isNotEmpty()
                } else {
                    // User not logged in, can't have favorites
                    false
                }
            } catch (e: Exception) {
                Log.e("RecipeRepository", "Error checking favorite status: ${e.message}", e)
                false
            }

            // Create the final recipe object
            val detailedRecipe = DetailedRecipe(
                id = recipeResponse.id,
                name = recipeResponse.name,
                description = recipeResponse.description ?: "",
                imageUrl = recipeResponse.image_url ?: "",
                preparationTime = recipeResponse.preparation_time ?: 0,
                cookingTime = recipeResponse.cooking_time ?: 0,
                servings = recipeResponse.servings ?: 0,
                difficulty = recipeResponse.difficulty ?: "Medium",
                ingredients = ingredientDetails,
                instructions = instructions,
                nutritionFacts = NutritionFacts(
                    calories = nutritionResponse.calories ?: 0,
                    protein = nutritionResponse.protein ?: 0f,
                    carbs = nutritionResponse.carbs ?: 0f,
                    fat = nutritionResponse.fat ?: 0f,
                    fiber = nutritionResponse.fiber,
                    sugar = nutritionResponse.sugar
                ),
                tags = recipeResponse.tags ?: emptyList(),
                isFavorite = isFavorite
            )

            emit(Result.success(detailedRecipe))
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Error in getRecipeDetails: ${e.message}", e)
            emit(Result.failure(e))
        }
    }

    suspend fun toggleFavorite(recipeId: Int, isFavorite: Boolean): Flow<Result<Boolean>> = flow {
        try {
            val currentUser = supabase.auth.currentUserOrNull()?.id ?: run {
                emit(Result.failure(Exception("User not authenticated")))
                return@flow
            }

            if (isFavorite) {
                // Add to favorites - use the object-based insert
                val favoriteData = UserFavoriteDto(
                    user_id = currentUser,
                    recipe_id = recipeId
                )

                supabase.from("user_favorites")
                    .insert(favoriteData)
            } else {
                // Remove from favorites
                supabase.from("user_favorites")
                    .delete {
                        filter {
                            eq("user_id", currentUser)
                            eq("recipe_id", recipeId)
                        }
                    }
            }

            emit(Result.success(true))
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Error toggling favorite", e)
            emit(Result.failure(e))
        }
    }

    suspend fun getRecipes(
        query: String? = null,
        category: String? = null,
        limit: Int = 10
    ): Flow<Result<List<RecipeListItem>>> = flow {
        try {
            // Build and execute the query
            val recipes = supabase.from("recipes")
                .select(columns = Columns.list("id, name, image_url, preparation_time, cooking_time, difficulty, tags")) {
                    // Apply filters if provided
                    filter {
                        if (!query.isNullOrEmpty()) {
                            ilike("name", "%$query%")
                        }

                        if (!category.isNullOrEmpty() && category.lowercase() != "all recipes") {
                            contains("tags", arrayOf(category).toList())
                        }
                    }

                    limit(limit.toLong())
                    order("id", Order.DESCENDING) // false for descending
                }
                .decodeList<RecipeListItemDto>()

            Log.d("RecipeRepository", "Found ${recipes.size} recipes matching criteria")

            // Process each recipe and its nutrition separately
            val results = recipes.map { recipe ->
                val calories = try {
                    val nutritionData = supabase.from("recipe_nutrition")
                        .select(columns = Columns.list("calories")) {
                            filter { eq("recipe_id", recipe.id) }
                        }
                        .decodeList<NutritiondataDto>()
                        .firstOrNull()

                    nutritionData?.calories ?: 0
                } catch (e: Exception) {
                    Log.e("RecipeRepository", "Error fetching nutrition for recipe ${recipe.id}: ${e.message}", e)
                    0
                }

                RecipeListItem(
                    id = recipe.id,
                    name = recipe.name,
                    imageUrl = recipe.image_url ?: "",
                    time = "${(recipe.preparation_time ?: 0) + (recipe.cooking_time ?: 0)} min",
                    calories = calories,
                    category = recipe.tags?.firstOrNull() ?: ""
                )
            }

            emit(Result.success(results))
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Error fetching recipes", e)
            emit(Result.failure(e))
        }
    }



    // DTO classes for deserialization
    @Serializable
    private data class RecipeDto(
        val id: Int,
        val name: String,
        val description: String?,
        val image_url: String?,
        val preparation_time: Int?,
        val cooking_time: Int?,
        val servings: Int?,
        val difficulty: String?,
        val tags: List<String>?
    )

    data class RecipeListItem(
        val id: Int,
        val name: String,
        val imageUrl: String,
        val time: String,
        val calories: Int,
        val category: String
    )

    @Serializable
    private data class RecipeIngredientDto(
        val id: Int,
        val recipe_id: Int,
        val ingredient_id: Int,
        val quantity: Float,
        val unit: String,
        //val ingredients: IngredientDto
    )

    @Serializable
    private data class IngredientDto(
        val id: Int,
        val name: String
    )

    @Serializable
    private data class RecipeInstructionDto(
        val id: Int,
        val recipe_id: Int,
        val step_number: Int,
        val instruction: String
    )

    @Serializable
    private data class RecipeNutritionDto(
        val id: Int,
        val recipe_id: Int,
        val calories: Int?,
        val protein: Float?,
        val carbs: Float?,
        val fat: Float?,
        val fiber: Float?,
        val sugar: Float?
    )

    @Serializable
    private data class RecipeListItemDto(
        val id: Int,
        val name: String,
        val image_url: String?,
        val preparation_time: Int?,
        val cooking_time: Int?,
        val difficulty: String?,
        val tags: List<String>?
    )

    @Serializable
    private data class RecipeNutritionSimpleDto(
        val recipe_id: Int,
        val calories: Int
    )

    @Serializable
    private data class UserFavoriteDto(
        val user_id: String,
        val recipe_id: Int
    )

    @Serializable
    private data class NutritiondataDto(
        val calories: Int? = null
    )

    @Serializable
    private data class FavoriteDto(
        val id: Int
    )

    @Serializable
    private data class IdDto(
        val id: Int? = null
    )
}