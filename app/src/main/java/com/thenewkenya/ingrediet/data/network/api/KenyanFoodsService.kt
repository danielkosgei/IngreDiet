package com.thenewkenya.ingrediet.data.network.api

import android.util.Log
import com.thenewkenya.ingrediet.data.model.DetailedRecipe
import com.thenewkenya.ingrediet.data.model.IngredientItem
import com.thenewkenya.ingrediet.data.model.NutritionFacts
import com.thenewkenya.ingrediet.data.network.supabase
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.PostgrestRequestBuilder
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.postgrest.query.filter.PostgrestFilterBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Service class for Kenyan foods and recipes
 * Provides access to traditional Kenyan dishes that might not be available in other APIs
 */
class KenyanFoodsService {

    /**
     * Search for Kenyan recipes by name using fuzzy matching
     */
    suspend fun searchKenyanRecipes(query: String): List<DetailedRecipe> = withContext(Dispatchers.IO) {
        try {
            Log.d("KenyanFoodsService", "Searching for recipes with query: $query")
            // Use the search_kenyan_recipes function
            val searchResults = supabase.from("kenyan_recipes")
                .select(columns = Columns.list("id", "name", "description", "region", "tags", "image_url", "preparation_time", "cooking_time", "servings", "difficulty", "calories")) {
                    filter {
                        or {
                            ilike("name", "%$query%")
                            ilike("description", "%$query%")
                        }
                    }
                    limit(10)
                }
                .decodeList<SearchResultDto>()
            Log.d("KenyanFoodsService", "Found ${searchResults.size} search results")

            // Get complete recipe details for each search result
            return@withContext searchResults.map { searchResult ->
                getCompleteRecipe(searchResult.id) ?: searchResult.toBasicDetailedRecipe()
            }
        } catch (e: Exception) {
            Log.e("KenyanFoodsService", "Error searching Kenyan recipes: ${e.message}", e)
            // Fallback to local dataset
            return@withContext (localKenyanRecipes as List<Any>)
                .filter { recipe -> (recipe as? LocalKenyanRecipe)?.name?.contains(query, ignoreCase = true) ?: false }
                .map { recipe -> (recipe as? LocalKenyanRecipe)?.toDetailedRecipe() ?: DetailedRecipe(
                    id = 0,
                    name = "",
                    description = "",
                    preparationTime = 0,
                    cookingTime = 0,
                    servings = 0,
                    difficulty = "",
                    ingredients = emptyList(),
                    instructions = emptyList(),
                    nutritionFacts = NutritionFacts(
                        calories = 0,
                        protein = 0f,
                        carbs = 0f,
                        fat = 0f
                    ),
                    tags = emptyList()
                ) }
                .take(10)
                .toList()
        }
    }

    /**
     * Get suggestions for Kenyan foods based on query using fuzzy search
     */
    suspend fun getKenyanFoodSuggestions(query: String): List<String> = withContext(Dispatchers.IO) {
        try {
            // Search recipes and get names
            val searchResults = supabase.from("kenyan_recipes")
                .select(columns = Columns.list("id", "name", "description", "region", "tags", "image_url", "preparation_time", "cooking_time", "servings", "difficulty", "calories")) {
                    filter {
                        or {
                            ilike("name", "%$query%")
                            ilike("description", "%$query%")
                        }
                    }
                    limit(10)
                }
                .decodeList<BasicRecipeDto>()
                .map { recipe -> recipe.name }
                .distinct()

            if (searchResults.isNotEmpty()) {
                return@withContext searchResults
            }

            // Fallback to local dataset
            return@withContext (localKenyanRecipes as List<Any>)
                .filter { recipe -> (recipe as? LocalKenyanRecipe)?.name?.contains(query, ignoreCase = true) ?: false }
                .map { recipe -> (recipe as? LocalKenyanRecipe)?.name ?: "" }
                .take(5)
                .toList()
        } catch (e: Exception) {
            Log.e("KenyanFoodsService", "Error getting Kenyan food suggestions: ${e.message}", e)
            return@withContext (localKenyanRecipes as List<Any>)
                .filter { recipe -> (recipe as? LocalKenyanRecipe)?.name?.contains(query, ignoreCase = true) ?: false }
                .map { recipe -> (recipe as? LocalKenyanRecipe)?.name ?: "" }
                .take(5)
                .toList()
        }
    }
    
    /**
     * Get a complete recipe with all its details using the get_complete_kenyan_recipe function
     */
    suspend fun getCompleteRecipe(recipeId: Int): DetailedRecipe? = withContext(Dispatchers.IO) {
        try {
            Log.d("KenyanFoodsService", "Fetching complete recipe for ID: $recipeId")
            
            // Get the basic recipe info
            val recipe = try {
                supabase.from("kenyan_recipes")
                    .select(columns = Columns.list("id", "name", "description", "image_url", "preparation_time", "cooking_time", "servings", "difficulty", "region", "calories")) {
                        filter { eq("id", recipeId) }
                    }
                    .decodeAs<BasicRecipeDto>()
                    .also { Log.d("KenyanFoodsService", "Found basic recipe: ${it.name}") }
            } catch (e: Exception) {
                Log.e("KenyanFoodsService", "Error getting basic recipe: ${e.message}")
                // If we can't get the basic recipe info, return null
                return@withContext null
            }

            // Get ingredients
            val ingredients = try {
                supabase.from("kenyan_recipe_ingredients")
                    .select(columns = Columns.list("name", "quantity", "unit", "order_index")) {
                        filter { eq("recipe_id", recipeId) }
                        order("order_index", Order.ASCENDING)
                    }
                    .decodeList<IngredientDto>()
                    .also { Log.d("KenyanFoodsService", "Found ${it.size} ingredients") }
            } catch (e: Exception) {
                Log.e("KenyanFoodsService", "Error getting ingredients: ${e.message}")
                emptyList()
            }

            // Get instructions
            val instructions = try {
                supabase.from("kenyan_recipe_instructions")
                    .select(columns = Columns.list("instruction_text", "step_number")) {
                        filter { eq("recipe_id", recipeId) }
                        order("step_number", Order.ASCENDING)
                    }
                    .decodeList<InstructionDto>()
                    .also { Log.d("KenyanFoodsService", "Found ${it.size} instructions") }
            } catch (e: Exception) {
                Log.e("KenyanFoodsService", "Error getting instructions: ${e.message}")
                emptyList()
            }

            // Get tags
            val tags = try {
                supabase.from("kenyan_recipe_tags")
                    .select(columns = Columns.list("tag_name")) {
                        filter { eq("recipe_id", recipeId) }
                    }
                    .decodeList<Map<String, String>>()
                    .map { it["tag_name"] ?: "" }
                    .filter { it.isNotEmpty() }
                    .also { Log.d("KenyanFoodsService", "Found ${it.size} tags") }
            } catch (e: Exception) {
                Log.e("KenyanFoodsService", "Error getting tags: ${e.message}")
                emptyList()
            }

            // Create the complete recipe
            val completeRecipe = CompleteRecipeDto(
                recipe = recipe,
                ingredients = ingredients,
                instructions = instructions,
                tags = tags
            ).toDetailedRecipe()

            Log.d("KenyanFoodsService", "Successfully created complete recipe: ${completeRecipe.name}")
            return@withContext completeRecipe

        } catch (e: Exception) {
            Log.e("KenyanFoodsService", "Error getting complete recipe: ${e.message}", e)
            return@withContext null
        }
    }

    /**
     * Get recipes by region
     */
    suspend fun getRecipesByRegion(region: String): List<DetailedRecipe> = withContext(Dispatchers.IO) {
        try {
            val results = supabase.from("kenyan_recipes")
                .select(columns = Columns.list("id", "name", "description", "region", "tags", "image_url", "preparation_time", "cooking_time", "servings", "difficulty", "calories")) {
                    filter { eq("region", region) }
                }
                .decodeList<BasicRecipeDto>()
            return@withContext results.map { recipe -> recipe.toBasicDetailedRecipe() }
        } catch (e: Exception) {
            Log.e("KenyanFoodsService", "Error getting recipes by region: ${e.message}", e)
            return@withContext (localKenyanRecipes as List<Any>)
                .filter { recipe -> (recipe as? LocalKenyanRecipe)?.region?.equals(region, ignoreCase = true) ?: false }
                .map { recipe -> (recipe as? LocalKenyanRecipe)?.toDetailedRecipe() ?: DetailedRecipe(
                    id = 0,
                    name = "",
                    description = "",
                    preparationTime = 0,
                    cookingTime = 0,
                    servings = 0,
                    difficulty = "",
                    ingredients = emptyList(),
                    instructions = emptyList(),
                    nutritionFacts = NutritionFacts(
                        calories = 0,
                        protein = 0f,
                        carbs = 0f,
                        fat = 0f
                    ),
                    tags = emptyList()
                ) }
                .toList()
        }
    }

    /**
     * Get recipe suggestions based on available ingredients
     */
    suspend fun getRecipeSuggestionsByIngredients(ingredients: List<String>): List<DetailedRecipe> = withContext(Dispatchers.IO) {
        try {
            // First get recipe IDs that contain any of the ingredients
            val recipeIds = supabase.from("kenyan_recipe_ingredients")
                .select(columns = Columns.list("recipe_id")) {
                    filter {
                        or {
                            ingredients.forEach { ingredient ->
                                ilike("name", "%$ingredient%")
                            }
                        }
                    }
                }
                .decodeList<Map<String, Int>>()
                .map { it["recipe_id"] ?: 0 }
                .distinct()
                .filter { it != 0 }

            if (recipeIds.isEmpty()) {
                return@withContext emptyList()
            }

            // Then get the recipe details
            val results = supabase.from("kenyan_recipes")
                .select(columns = Columns.list("id", "name", "description", "region", "tags", "image_url", "preparation_time", "cooking_time", "servings", "difficulty", "calories")) {
                    filter {
                        or {
                            recipeIds.forEach { recipeId ->
                                eq("id", recipeId)
                            }
                        }
                    }
                    limit(10)
                }
                .decodeList<BasicRecipeDto>()

            return@withContext results.map { recipe -> recipe.toBasicDetailedRecipe() }
        } catch (e: Exception) {
            Log.e("KenyanFoodsService", "Error getting recipe suggestions: ${e.message}", e)
            return@withContext emptyList()
        }
    }

    @Serializable
    private data class SearchResultDto(
        val id: Int,
        val name: String,
        val description: String,
        val image_url: String = "",
        val preparation_time: Int = 0,
        val cooking_time: Int = 0,
        val servings: Int = 4,
        val difficulty: String = "Medium",
        val region: String? = null,
        val calories: Int? = null,
        val tags: List<String> = emptyList()
    ) {
        fun toBasicDetailedRecipe(): DetailedRecipe {
            val tags = buildList {
                // Add standard tags first
                add("Kenyan")
                // Add region if available, otherwise Traditional
                region?.takeIf { it.isNotBlank() }?.let { add(it) } ?: add("Traditional")
                
                // Add additional tags, excluding duplicates
                tags.asSequence()
                    .map { it.trim() }
                    .filter { tag ->
                        tag.isNotBlank() &&
                        !tag.equals("Kenyan", ignoreCase = true) &&
                        !tag.equals(region ?: "Traditional", ignoreCase = true)
                    }
                    .toList()
                    .let { addAll(it) }
            }.distinct()
            return DetailedRecipe(
                id = id,
                name = name,
                description = description,
                imageUrl = image_url,
                preparationTime = preparation_time,
                cookingTime = cooking_time,
                servings = servings,
                difficulty = difficulty,
                ingredients = emptyList(),
                instructions = emptyList(),
                nutritionFacts = NutritionFacts(
                    calories = calories ?: 0,
                    protein = 0f,
                    carbs = 0f,
                    fat = 0f
                ),
                tags = tags
            )
        }
    }

    @Serializable
    private data class CompleteRecipeDto(
        val recipe: BasicRecipeDto,
        val ingredients: List<IngredientDto>,
        val instructions: List<InstructionDto>,
        val tags: List<String>
    ) {
        fun toDetailedRecipe(): DetailedRecipe {
            return DetailedRecipe(
                id = recipe.id,
                name = recipe.name,
                description = recipe.description,
                imageUrl = recipe.image_url,
                preparationTime = recipe.preparation_time,
                cookingTime = recipe.cooking_time,
                servings = recipe.servings,
                difficulty = recipe.difficulty,
                ingredients = ingredients.map { it.toIngredientItem() },
                instructions = instructions.map { it.instruction_text },
                nutritionFacts = NutritionFacts(
                    calories = recipe.calories ?: 0,
                    protein = 0f,
                    carbs = 0f,
                    fat = 0f
                ),
                tags = buildList {
                    // Add standard tags first
                    add("Kenyan")
                    // Add region if available, otherwise Traditional
                    recipe.region?.takeIf { it.isNotBlank() }?.let { add(it) } ?: add("Traditional")
                    
                    // Add additional tags, excluding duplicates
                    tags.asSequence()
                        .map { it.trim() }
                        .filter { tag ->
                            tag.isNotBlank() &&
                            !tag.equals("Kenyan", ignoreCase = true) &&
                            !tag.equals(recipe.region ?: "Traditional", ignoreCase = true)
                        }
                        .toList()
                        .let { addAll(it) }
                }.distinct()
            )
        }
    }

    @Serializable
    private data class BasicRecipeDto(
        val id: Int,
        val name: String,
        val description: String,
        val image_url: String,
        val preparation_time: Int,
        val cooking_time: Int,
        val servings: Int = 4,
        val difficulty: String = "Medium",
        val region: String? = null,
        val calories: Int? = null,
        val tags: List<String> = emptyList()
    ) {
        fun toBasicDetailedRecipe(): DetailedRecipe {
            return DetailedRecipe(
                id = id,
                name = name,
                description = description,
                imageUrl = image_url,
                preparationTime = preparation_time,
                cookingTime = cooking_time,
                servings = servings,
                difficulty = difficulty,
                ingredients = emptyList(),
                instructions = emptyList(),
                nutritionFacts = NutritionFacts(
                    calories = calories ?: 0,
                    protein = 0f,
                    carbs = 0f,
                    fat = 0f
                ),
                tags = buildList {
                    // Add standard tags first
                    add("Kenyan")
                    // Add region if available, otherwise Traditional
                    region?.takeIf { it.isNotBlank() }?.let { add(it) } ?: add("Traditional")
                    
                    // Add additional tags, excluding duplicates
                    tags.asSequence()
                        .map { it.trim() }
                        .filter { tag ->
                            tag.isNotBlank() &&
                            !tag.equals("Kenyan", ignoreCase = true) &&
                            !tag.equals(region ?: "Traditional", ignoreCase = true)
                        }
                        .toList()
                        .let { addAll(it) }
                }.distinct()
            )
        }
    }

    @Serializable
    private data class IngredientDto(
        val name: String,
        val quantity: Float,
        val unit: String,
        val order_index: Int
    ) {
        fun toIngredientItem(): IngredientItem {
            return IngredientItem(
                id = order_index,
                name = name,
                quantity = quantity,
                unit = unit
            )
        }
    }

    @Serializable
    private data class InstructionDto(
        val instruction_text: String,
        val step_number: Int
    )
    
    @Serializable
    private data class LocalKenyanRecipe(
        val id: Int,
        val name: String,
        val description: String,
        val region: String,
        val calories: Int,
        val ingredients: List<String>,
        val instructions: List<String>
    ) {
        fun toDetailedRecipe(): DetailedRecipe {
            return DetailedRecipe(
                id = id,
                name = name,
                description = description,
                preparationTime = 30,  // Default values since not in local data
                cookingTime = 60,      // Default values since not in local data
                servings = 4,          // Default values since not in local data
                difficulty = "Medium",  // Default values since not in local data
                ingredients = ingredients.mapIndexed { index, ingredient ->
                    IngredientItem(
                        id = index,
                        name = ingredient,
                        quantity = 1f,
                        unit = "unit"
                    )
                },
                instructions = instructions,
                nutritionFacts = NutritionFacts(
                    calories = calories,
                    protein = 0f,      // Default values since not in local data
                    carbs = 0f,        // Default values since not in local data
                    fat = 0f          // Default values since not in local data
                ),
                tags = emptyList()     // Default values since not in local data
            )
        }
    }
    
    // Local dataset of Kenyan recipes to use when Supabase doesn't have data yet
    // This can be expanded with more detailed information
    private val localKenyanRecipes: List<LocalKenyanRecipe> = listOf(
        LocalKenyanRecipe(
            id = 1001,
            name = "Ugali",
            description = "A staple food in Kenya made from maize flour and water, similar to polenta but firmer.",
            region = "National",
            calories = 150,
            ingredients = listOf("Maize flour", "Water", "Salt"),
            instructions = listOf(
                "Boil water in a pot",
                "Gradually add maize flour while stirring",
                "Continue stirring until it forms a firm dough",
                "Cover and let it cook for 5 minutes",
                "Serve hot with stew or vegetables"
            )
        ),
        LocalKenyanRecipe(
            id = 1002,
            name = "Nyama Choma",
            description = "Grilled meat, usually goat or beef, seasoned with salt and sometimes spices.",
            region = "National",
            calories = 300,
            ingredients = listOf("Goat meat or beef", "Salt", "Black pepper", "Optional spices"),
            instructions = listOf(
                "Cut meat into pieces",
                "Season with salt and pepper",
                "Grill over open fire or charcoal until cooked through",
                "Serve hot with kachumbari"
            )
        ),
        LocalKenyanRecipe(
            id = 1003,
            name = "Sukuma Wiki",
            description = "A simple dish made with collard greens, onions, and tomatoes.",
            region = "National",
            calories = 80,
            ingredients = listOf("Collard greens (kale)", "Onions", "Tomatoes", "Oil", "Salt"),
            instructions = listOf(
                "Chop collard greens into small pieces",
                "Dice onions and tomatoes",
                "Sauté onions in oil until translucent",
                "Add tomatoes and cook until soft",
                "Add collard greens and salt",
                "Cook until greens are tender but still bright green"
            )
        ),
        LocalKenyanRecipe(
            id = 1004,
            name = "Githeri",
            description = "A traditional Kenyan dish made with maize and beans, sometimes with vegetables added.",
            region = "Central",
            calories = 250,
            ingredients = listOf("Maize kernels", "Beans", "Onions", "Tomatoes", "Salt", "Oil"),
            instructions = listOf(
                "Soak maize and beans overnight",
                "Boil until soft",
                "In a separate pan, sauté onions and tomatoes",
                "Add the cooked maize and beans",
                "Season with salt and simmer for 10 minutes"
            )
        ),
        LocalKenyanRecipe(
            id = 1005,
            name = "Pilau",
            description = "Spiced rice dish with meat, popular in coastal Kenya.",
            region = "Coastal",
            calories = 400,
            ingredients = listOf("Rice", "Meat (beef or chicken)", "Onions", "Pilau masala", "Garlic", "Ginger", "Oil"),
            instructions = listOf(
                "Brown meat with onions, garlic, and ginger",
                "Add pilau masala and stir",
                "Add rice and water",
                "Cook until rice is tender and water is absorbed"
            )
        ),
        LocalKenyanRecipe(
            id = 1006,
            name = "Chapati",
            description = "Flatbread common in Kenya, similar to Indian chapati.",
            region = "National",
            calories = 150,
            ingredients = listOf("Wheat flour", "Water", "Salt", "Oil"),
            instructions = listOf(
                "Mix flour with water and let ferment overnight",
                "Knead until smooth",
                "Divide into balls and roll out into circles",
                "Cook on a hot pan with oil until golden brown on both sides"
            )
        ),
        LocalKenyanRecipe(
            id = 1007,
            name = "Mandazi",
            description = "Sweet, triangular-shaped fried bread, similar to a doughnut.",
            region = "Coastal",
            calories = 200,
            ingredients = listOf("Flour", "Sugar", "Milk", "Eggs", "Baking powder", "Cardamom", "Oil for frying"),
            instructions = listOf(
                "Mix dry ingredients",
                "Add wet ingredients to form dough",
                "Roll out and cut into triangles",
                "Deep fry until golden brown"
            )
        ),
        LocalKenyanRecipe(
            id = 1008,
            name = "Mukimo",
            description = "Mashed potatoes mixed with peas, corn, and greens.",
            region = "Central",
            calories = 200,
            ingredients = listOf("Potatoes", "Green peas", "Corn", "Spinach or pumpkin leaves", "Onions", "Salt"),
            instructions = listOf(
                "Boil potatoes until soft",
                "Cook peas, corn, and greens separately",
                "Mash potatoes and mix in the vegetables",
                "Season with sautéed onions and salt"
            )
        ),
        LocalKenyanRecipe(
            id = 1009,
            name = "Irio",
            description = "Similar to Mukimo, but with mashed green peas, corn, and potatoes.",
            region = "Central",
            calories = 220,
            ingredients = listOf("Potatoes", "Green peas", "Corn", "Onions", "Salt"),
            instructions = listOf(
                "Boil potatoes, peas, and corn until soft",
                "Mash together",
                "Season with sautéed onions and salt"
            )
        ),
        LocalKenyanRecipe(
            id = 1010,
            name = "Matoke",
            description = "Plantain stew, popular in Western Kenya.",
            region = "Western",
            calories = 250,
            ingredients = listOf("Green plantains", "Onions", "Tomatoes", "Bell peppers", "Oil", "Salt"),
            instructions = listOf(
                "Peel and chop plantains",
                "Sauté onions, tomatoes, and bell peppers",
                "Add plantains and a little water",
                "Simmer until plantains are soft and sauce thickens"
            )
        ),
        LocalKenyanRecipe(
            id = 1011,
            name = "Bhajia",
            description = "Spiced potato fritters, popular street food.",
            region = "Urban",
            calories = 180,
            ingredients = listOf("Potatoes", "Gram flour", "Turmeric", "Chili powder", "Salt", "Oil for frying"),
            instructions = listOf(
                "Slice potatoes thinly",
                "Make batter with gram flour and spices",
                "Dip potato slices in batter",
                "Deep fry until golden and crispy"
            )
        ),
        LocalKenyanRecipe(
            id = 1012,
            name = "Kachumbari",
            description = "Fresh tomato and onion salad, often served with nyama choma.",
            region = "National",
            calories = 50,
            ingredients = listOf("Tomatoes", "Onions", "Cilantro", "Lemon juice", "Salt"),
            instructions = listOf(
                "Dice tomatoes and onions",
                "Chop cilantro",
                "Mix together with lemon juice and salt"
            )
        ),
        LocalKenyanRecipe(
            id = 1013,
            name = "Mutura",
            description = "Kenyan blood sausage made with meat, blood, and spices.",
            region = "Central",
            calories = 300,
            ingredients = listOf("Meat trimmings", "Animal blood", "Intestines for casing", "Spices"),
            instructions = listOf(
                "Clean intestines thoroughly",
                "Mix meat, blood, and spices",
                "Stuff mixture into intestines",
                "Grill over open fire until cooked"
            )
        ),
        LocalKenyanRecipe(
            id = 1014,
            name = "Mahindi Choma",
            description = "Roasted corn on the cob, a popular street food.",
            region = "Urban",
            calories = 120,
            ingredients = listOf("Corn on the cob", "Lime or lemon", "Salt", "Chili powder (optional)"),
            instructions = listOf(
                "Roast corn over open fire or grill",
                "Rub with lime/lemon",
                "Sprinkle with salt and chili powder if desired"
            )
        ),
        LocalKenyanRecipe(
            id = 1015,
            name = "Viazi Karai",
            description = "Deep-fried potatoes coated in a spiced gram flour batter.",
            region = "Coastal",
            calories = 200,
            ingredients = listOf("Potatoes", "Gram flour", "Garlic", "Turmeric", "Salt", "Oil for frying"),
            instructions = listOf(
                "Boil potatoes until just tender",
                "Make batter with gram flour and spices",
                "Dip potato pieces in batter",
                "Deep fry until golden and crispy"
            )
        ),
        LocalKenyanRecipe(
            id = 1016,
            name = "Uji",
            description = "Fermented porridge made from millet, sorghum, or maize flour.",
            region = "National",
            calories = 100,
            ingredients = listOf("Millet flour", "Water", "Sugar or honey", "Lemon (optional)"),
            instructions = listOf(
                "Mix flour with water and let ferment overnight",
                "Cook the mixture until it thickens",
                "Sweeten with sugar or honey",
                "Add lemon juice if desired"
            )
        ),
        LocalKenyanRecipe(
            id = 1017,
            name = "Omena",
            description = "Small dried fish, usually cooked with tomatoes and onions.",
            region = "Lake Region",
            calories = 150,
            ingredients = listOf("Dried omena fish", "Onions", "Tomatoes", "Oil", "Salt"),
            instructions = listOf(
                "Rinse omena thoroughly",
                "Sauté onions and tomatoes",
                "Add omena and cook until tender",
                "Season with salt"
            )
        ),
        LocalKenyanRecipe(
            id = 1018,
            name = "Mursik",
            description = "Fermented milk, traditional among the Kalenjin community.",
            region = "Rift Valley",
            calories = 120,
            ingredients = listOf("Milk", "Special gourd", "Special herb ash"),
            instructions = listOf(
                "Clean gourd with special herb ash",
                "Pour fresh milk into gourd",
                "Cover and let ferment for several days"
            )
        ),
        LocalKenyanRecipe(
            id = 1019,
            name = "Samosa",
            description = "Triangular pastry filled with spiced meat or vegetables.",
            region = "Urban",
            calories = 180,
            ingredients = listOf("Flour", "Oil", "Minced meat or vegetables", "Onions", "Spices"),
            instructions = listOf(
                "Make dough with flour and water",
                "Prepare filling with meat/vegetables and spices",
                "Form triangular pockets and fill",
                "Deep fry until golden brown"
            )
        ),
        LocalKenyanRecipe(
            id = 1020,
            name = "Mahamri",
            description = "Sweet, spiced triangular donuts, popular at the coast.",
            region = "Coastal",
            calories = 200,
            ingredients = listOf("Flour", "Coconut milk", "Sugar", "Cardamom", "Yeast", "Oil for frying"),
            instructions = listOf(
                "Mix flour, sugar, cardamom, and yeast",
                "Add coconut milk to form dough",
                "Let rise for 1-2 hours",
                "Roll out, cut into triangles",
                "Deep fry until golden brown"
            )
        ),
        // New recipe added
        LocalKenyanRecipe(
            id = 1021,
            name = "Nyoyo",
            description = "A traditional Luo dish made with beans and maize, similar to githeri but with specific preparation methods.",
            region = "Nyanza",
            calories = 280,
            ingredients = listOf("Beans", "Maize", "Onions", "Tomatoes", "Salt", "Oil"),
            instructions = listOf(
                "Soak beans and maize overnight",
                "Boil together until soft",
                "In a separate pan, sauté onions and tomatoes",
                "Mix the beans and maize with the sautéed vegetables",
                "Season with salt and simmer for 10 minutes"
            )
        )
    )
}
