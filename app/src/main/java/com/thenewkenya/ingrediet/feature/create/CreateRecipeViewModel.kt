package com.thenewkenya.ingrediet.feature.create

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thenewkenya.ingrediet.IngreDietApplication
import com.thenewkenya.ingrediet.data.model.DetailedRecipe
import com.thenewkenya.ingrediet.data.model.IngredientItem
import com.thenewkenya.ingrediet.data.model.NutritionFacts
import com.thenewkenya.ingrediet.data.model.Profile
import com.thenewkenya.ingrediet.data.network.api.IngreDietService
import com.thenewkenya.ingrediet.data.repository.ProfileRepository
import com.thenewkenya.ingrediet.data.repository.RecipeRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CreateRecipeViewModel(
    private val context: Context = IngreDietApplication.instance
) : ViewModel() {
    private val TAG = "CreateRecipeVM"
    private val edgeFunctionService = IngreDietService(context)
    private val recipeRepository = RecipeRepository(context)
    private val profileRepository = ProfileRepository()
    
    private var searchJob: Job? = null
    private var userProfile: Profile? = null
    
    private val _recipeName = MutableStateFlow("")
    val recipeName: StateFlow<String> = _recipeName.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _cookingTime = MutableStateFlow("")
    val cookingTime: StateFlow<String> = _cookingTime.asStateFlow()

    private val _calories = MutableStateFlow("")
    val calories: StateFlow<String> = _calories.asStateFlow()

    private val _ingredients = MutableStateFlow<List<String>>(emptyList())
    val ingredients: StateFlow<List<String>> = _ingredients.asStateFlow()

    private val _instructions = MutableStateFlow<List<String>>(emptyList())
    val instructions: StateFlow<List<String>> = _instructions.asStateFlow()
    
    // Enhanced state for ingredient search
    private val _currentIngredient = MutableStateFlow("")
    val currentIngredient: StateFlow<String> = _currentIngredient.asStateFlow()
    
    private val _matchingRecipes = MutableStateFlow<List<DetailedRecipe>>(emptyList())
    val matchingRecipes: StateFlow<List<DetailedRecipe>> = _matchingRecipes.asStateFlow()
    
    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()
    
    // New state for better UX
    private val _hasSearched = MutableStateFlow(false)
    val hasSearched: StateFlow<Boolean> = _hasSearched.asStateFlow()
    
    private val _searchError = MutableStateFlow<String?>(null)
    val searchError: StateFlow<String?> = _searchError.asStateFlow()
    
    private val _needsMoreIngredients = MutableStateFlow(false)
    val needsMoreIngredients: StateFlow<Boolean> = _needsMoreIngredients.asStateFlow()

    init {
        // Load user profile on initialization
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            try {
                profileRepository.getProfile().collect { result ->
                    result.fold(
                        onSuccess = { profile ->
                            userProfile = profile
                            Log.d(TAG, "User profile loaded: ${profile.dietaryPreferences.size} preferences, ${profile.allergies.size} allergies")
                        },
                        onFailure = { error ->
                            Log.w(TAG, "Could not load user profile: ${error.message}")
                        }
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading user profile", e)
            }
        }
    }

    fun updateRecipeName(name: String) {
        _recipeName.value = name
    }

    fun updateDescription(desc: String) {
        _description.value = desc
    }

    fun updateCookingTime(time: String) {
        _cookingTime.value = time
    }

    fun updateCalories(cal: String) {
        _calories.value = cal
    }

    fun addIngredient() {
        _ingredients.value = _ingredients.value + ""
    }

    fun removeIngredient(ingredient: String) {
        _ingredients.value = _ingredients.value - ingredient
        // Only search again if we still have enough ingredients
        if (shouldTriggerSearch()) {
            _needsMoreIngredients.value = false
            searchRecipesByIngredientsAuto()
        } else {
            // Clear results when we don't have enough ingredients
            _matchingRecipes.value = emptyList()
            _hasSearched.value = false
            _searchError.value = null
            _needsMoreIngredients.value = _ingredients.value.isNotEmpty()
        }
    }

    fun addInstruction() {
        _instructions.value = _instructions.value + ""
    }

    fun removeInstruction(instruction: String) {
        _instructions.value = _instructions.value - instruction
    }
    
    // Enhanced functions for ingredient search
    fun updateCurrentIngredient(ingredient: String) {
        _currentIngredient.value = ingredient
    }
    
    fun addIngredientForSearch() {
        val ingredient = _currentIngredient.value.trim()
        if (ingredient.isNotEmpty() && !_ingredients.value.contains(ingredient)) {
            _ingredients.value = _ingredients.value + ingredient
            _currentIngredient.value = ""
            
            // Only trigger search if we have enough ingredients for meaningful results
            if (shouldTriggerSearch()) {
                _needsMoreIngredients.value = false
                searchRecipesByIngredientsAuto()
            } else {
                // Clear any previous results but don't search yet
                _matchingRecipes.value = emptyList()
                _hasSearched.value = false
                _searchError.value = null
                _needsMoreIngredients.value = _ingredients.value.isNotEmpty()
            }
        }
    }
    
    // Smart threshold logic for triggering search
    private fun shouldTriggerSearch(): Boolean {
        val ingredientCount = _ingredients.value.size
        
        return when {
            // Need at least 2 ingredients for a meaningful search
            ingredientCount < 2 -> false
            // Always search with 2+ ingredients
            else -> true
        }
    }
    
    // Auto search with debounce to avoid too many requests
    private fun searchRecipesByIngredientsAuto() {
        // Cancel previous search job
        searchJob?.cancel()
        
        searchJob = viewModelScope.launch {
            // Small delay to allow user to add multiple ingredients quickly
            delay(300)
            searchRecipesByIngredients()
        }
    }
    
    fun searchRecipesByIngredients() {
        // Only search if we have enough ingredients
        if (!shouldTriggerSearch()) {
            _matchingRecipes.value = emptyList()
            _hasSearched.value = false
            return
        }
        
        viewModelScope.launch {
            try {
                _isSearching.value = true
                _searchError.value = null
                
                Log.d(TAG, "Searching for recipes with ingredients: ${_ingredients.value}")
                
                // Get matching recipes from the primary service and database
                val allRecipes = mutableListOf<DetailedRecipe>()
                
                // Try edge function first, but also search local database
                try {
                    edgeFunctionService.getRecipesByIngredients(_ingredients.value).collect { recipes ->
                        allRecipes.addAll(recipes)
                    }
                    Log.d(TAG, "Edge function returned ${allRecipes.size} recipes")
                } catch (e: Exception) {
                    Log.w(TAG, "Edge function failed: ${e.message}")
                }
                
                // Always search the local database to get real recipes from your database with reasonable limit
                try {
                    recipeRepository.searchRecipesFromDatabase("", limit = 100).collect { result ->
                        result.fold(
                            onSuccess = { dbRecipes ->
                                // Filter database recipes that contain the requested ingredients
                                val userIngredients = _ingredients.value.map { it.lowercase() }
                                
                                val filteredDbRecipes = dbRecipes.filter { recipe ->
                                    val recipeIngredients = recipe.ingredients.map { it.name.lowercase().trim() }
                                    
                                    // Check if recipe contains ANY of the user's ingredients with strict matching
                                    userIngredients.any { userIngredient ->
                                        val cleanUserIngredient = userIngredient.lowercase().trim()
                                        
                                        recipeIngredients.any { recipeIngredient ->
                                            // Exact matches or ingredient contains user input
                                            recipeIngredient == cleanUserIngredient ||
                                            recipeIngredient.contains(cleanUserIngredient) ||
                                            // Only very specific meat equivalencies
                                            (cleanUserIngredient == "beef" && (recipeIngredient.contains("beef") || recipeIngredient == "meat")) ||
                                            (cleanUserIngredient == "chicken" && recipeIngredient.contains("chicken")) ||
                                            (cleanUserIngredient == "fish" && recipeIngredient.contains("fish")) ||
                                            // Common cooking ingredients
                                            (cleanUserIngredient == "tomato" && (recipeIngredient.contains("tomato") || recipeIngredient.contains("tomatoes"))) ||
                                            (cleanUserIngredient == "onion" && (recipeIngredient.contains("onion") || recipeIngredient.contains("onions"))) ||
                                            (cleanUserIngredient == "garlic" && recipeIngredient.contains("garlic")) ||
                                            // Flour types
                                            (cleanUserIngredient == "flour" && (recipeIngredient.contains("flour") || recipeIngredient.contains("maize flour")))
                                        }
                                    }
                                }
                                
                                allRecipes.addAll(filteredDbRecipes)
                                Log.d(TAG, "Database search added ${filteredDbRecipes.size} recipes")
                            },
                            onFailure = { error ->
                                Log.e(TAG, "Database search failed: ${error.message}")
                            }
                        )
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error searching database: ${e.message}")
                }
                

                
                // Apply user preference filtering
                val filteredRecipes = filterRecipesByUserPreferences(allRecipes)
                
                // Sort by ingredient match score
                val sortedRecipes = sortRecipesByIngredientMatch(filteredRecipes)
                
                _matchingRecipes.value = sortedRecipes.take(20) // Limit to top 20 results
                _hasSearched.value = true
                _isSearching.value = false
                
                Log.d(TAG, "=== FINAL SEARCH RESULTS ===")
                Log.d(TAG, "Total recipes found: ${_matchingRecipes.value.size}")
                _matchingRecipes.value.forEachIndexed { index, recipe ->
                    Log.d(TAG, "Recipe ${index + 1}: ${recipe.name} (${recipe.cuisineType})")
                }
                Log.d(TAG, "=== END SEARCH RESULTS ===")
                
                Log.d(TAG, "Found ${_matchingRecipes.value.size} recipes matching ingredients and preferences")
                
            } catch (e: Exception) {
                Log.e(TAG, "Error searching for recipes by ingredients", e)
                _isSearching.value = false
                _hasSearched.value = true
                _searchError.value = "Failed to search recipes. Please try again."
            }
        }
    }
    
    private fun filterRecipesByUserPreferences(recipes: List<DetailedRecipe>): List<DetailedRecipe> {
        val profile = userProfile ?: return recipes
        
        return recipes.filter { recipe ->
            val recipeName = recipe.name.lowercase()
            val recipeDescription = recipe.description.lowercase()
            val recipeCategory = recipe.category.lowercase()
            val recipeTags = recipe.tags.map { it.lowercase() }
            val recipeIngredients = recipe.ingredients.map { it.name.lowercase() }
            
            // Check for allergies - exclude recipes with allergens
            val hasAllergens = profile.allergies.any { allergy ->
                val allergyLower = allergy.lowercase()
                val recipeText = "$recipeName $recipeDescription ${recipeIngredients.joinToString(" ")}"
                
                // Enhanced allergy checking with specific patterns
                when (allergyLower) {
                    "gluten" -> recipeText.contains("wheat") || recipeText.contains("flour") || 
                               recipeText.contains("bread") || recipeText.contains("pasta") ||
                               recipeText.contains("gluten")
                    "nuts" -> recipeText.contains("nuts") || recipeText.contains("almonds") || 
                             recipeText.contains("peanuts") || recipeText.contains("cashews") ||
                             recipeText.contains("walnuts") || recipeText.contains("pecans")
                    "dairy" -> recipeText.contains("milk") || recipeText.contains("cheese") || 
                              recipeText.contains("butter") || recipeText.contains("cream") ||
                              recipeText.contains("yogurt") || recipeText.contains("dairy")
                    "eggs" -> recipeText.contains("egg") || recipeText.contains("eggs")
                    "shellfish" -> recipeText.contains("shrimp") || recipeText.contains("crab") || 
                                  recipeText.contains("lobster") || recipeText.contains("shellfish")
                    "fish" -> recipeText.contains("fish") || recipeText.contains("salmon") || 
                             recipeText.contains("tuna") || recipeText.contains("cod")
                    "soy" -> recipeText.contains("soy") || recipeText.contains("tofu")
                    else -> recipeText.contains(allergyLower)
                }
            }
            
            if (hasAllergens) {
                Log.d(TAG, "Recipe ${recipe.name} excluded due to allergies")
                return@filter false
            }
            
            // If user has dietary preferences, prioritize matching recipes
            if (profile.dietaryPreferences.isNotEmpty()) {
                val hasMatchingPreference = profile.dietaryPreferences.any { preference ->
                    val prefLower = preference.lowercase()
                    
                    recipeTags.any { it.contains(prefLower) } ||
                    recipeCategory.contains(prefLower) ||
                    recipeName.contains(prefLower) ||
                    recipeDescription.contains(prefLower)
                }
                
                // For now, don't exclude non-matching recipes, just deprioritize them
                // This ensures users still see results even if preferences don't match perfectly
            }
            
            true
        }
    }
    
    private fun sortRecipesByIngredientMatch(recipes: List<DetailedRecipe>): List<DetailedRecipe> {
        val userIngredients = _ingredients.value.map { it.lowercase() }
        
        return recipes.sortedByDescending { recipe ->
            var score = 0
            val recipeIngredients = recipe.ingredients.map { it.name.lowercase() }
            
            // Score based on ingredient matches
            userIngredients.forEach { userIngredient ->
                recipeIngredients.forEach { recipeIngredient ->
                    when {
                        recipeIngredient.contains(userIngredient) -> score += 3
                        userIngredient.contains(recipeIngredient) -> score += 2
                        // Fuzzy matching for similar ingredients
                        levenshteinDistance(userIngredient, recipeIngredient) <= 2 -> score += 1
                    }
                }
            }
            
            // Bonus points for dietary preference matches
            userProfile?.dietaryPreferences?.forEach { preference ->
                val prefLower = preference.lowercase()
                if (recipe.category.lowercase().contains(prefLower) ||
                    recipe.tags.any { it.lowercase().contains(prefLower) }) {
                    score += 5
                }
            }
            
            score
        }
    }
    
    // Simple Levenshtein distance for fuzzy matching
    private fun levenshteinDistance(str1: String, str2: String): Int {
        val dp = Array(str1.length + 1) { IntArray(str2.length + 1) }
        
        for (i in 0..str1.length) dp[i][0] = i
        for (j in 0..str2.length) dp[0][j] = j
        
        for (i in 1..str1.length) {
            for (j in 1..str2.length) {
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,
                    dp[i][j - 1] + 1,
                    dp[i - 1][j - 1] + if (str1[i - 1] == str2[j - 1]) 0 else 1
                )
            }
        }
        
        return dp[str1.length][str2.length]
    }
    
    fun clearSearch() {
        _matchingRecipes.value = emptyList()
        _hasSearched.value = false
        _searchError.value = null
        _isSearching.value = false
        _needsMoreIngredients.value = false
        searchJob?.cancel()
    }
    
    fun useRecipeAsTemplate(recipe: DetailedRecipe) {
        _recipeName.value = recipe.name
        _description.value = recipe.description
        _cookingTime.value = recipe.cookingTime.toString()
        _calories.value = recipe.nutritionFacts.calories.toString()
        _ingredients.value = recipe.ingredients.map { "${it.quantity} ${it.unit} ${it.name}" }
        _instructions.value = recipe.instructions
    }

    fun saveRecipe() {
        viewModelScope.launch {
            // TODO: Implement recipe saving logic
            // This should:
            // 1. Validate all fields
            // 2. Create a Recipe object
            // 3. Save to the repository
            // 4. Navigate back to the previous screen
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        searchJob?.cancel()
    }
} 