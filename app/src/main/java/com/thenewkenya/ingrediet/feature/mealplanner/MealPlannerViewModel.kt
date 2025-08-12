package com.thenewkenya.ingrediet.feature.mealplanner

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.thenewkenya.ingrediet.data.model.DetailedRecipe
import com.thenewkenya.ingrediet.data.repository.RecipeRepository
import com.thenewkenya.ingrediet.data.repository.MealPlanRepository
import com.thenewkenya.ingrediet.data.repository.ProfileRepository
import com.thenewkenya.ingrediet.data.network.supabase
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale
import java.io.File
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.withTimeout
import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.CancellationException
import com.thenewkenya.ingrediet.data.mealplan.MealPlanGenerator
import java.time.LocalDateTime
import java.util.UUID

// Add the MealTime enum
enum class MealTime {
    Breakfast, Lunch, Dinner, Snacks
}

data class MealPlanItem(
    val id: String,
    val name: String,
    val calories: Int,
    val day: DayOfWeek,
    val time: MealTime,
    val description: String? = null,
    val recipeId: String? = null,
    val imageUrl: String? = null
)

/**
 * Data class for nutrition summary
 */
data class NutritionSummary(
    val calories: Int,
    val protein: Int, // grams
    val carbs: Int,   // grams
    val fat: Int      // grams
)

// Add a custom exception class
class MealPlanTimeoutException(message: String) : Exception(message)

class MealPlannerViewModel(context: Context) : ViewModel() {
    private val repository = RecipeRepository(context)
    private val mealPlanRepository = MealPlanRepository()
    private val profileRepository = ProfileRepository()
    private val appContext = context.applicationContext
    
    private val _currentWeek = MutableStateFlow(getCurrentWeekString())
    val currentWeek: StateFlow<String> = _currentWeek.asStateFlow()

    private val _mealPlans = MutableStateFlow<Map<DayOfWeek, List<MealPlanItem>>>(emptyMap())
    val mealPlans: StateFlow<Map<DayOfWeek, List<MealPlanItem>>> = _mealPlans.asStateFlow()
    
    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()
    
    private val _dailyNutrition = MutableStateFlow<Map<DayOfWeek, NutritionSummary>>(emptyMap())
    val dailyNutrition: StateFlow<Map<DayOfWeek, NutritionSummary>> = _dailyNutrition.asStateFlow()
    
    // Loading and error states
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Add progress indicators for meal plan generation
    private val _generationProgress = MutableStateFlow(0f)
    val generationProgress: StateFlow<Float> = _generationProgress.asStateFlow()

    private val _generationStage = MutableStateFlow<String?>(null)
    val generationStage: StateFlow<String?> = _generationStage.asStateFlow()

    // Flag to indicate if the user is authenticated
    private val _isUserAuthenticated = MutableStateFlow(false)
    val isUserAuthenticated: StateFlow<Boolean> = _isUserAuthenticated.asStateFlow()

    init {
        checkAuthentication()
        loadExistingMealPlansOnly()
        // Load local cache if present
        try {
            val cached = LocalMealPlanStore.load(appContext)
            if (cached.isNotEmpty()) {
                _mealPlans.value = cached
            }
        } catch (_: Exception) {}
    }
    
    private fun checkAuthentication() {
        viewModelScope.launch {
            _isUserAuthenticated.value = supabase.auth.currentUserOrNull() != null
        }
    }

    private fun getCurrentWeekString(): String {
        val now = LocalDate.now()
        val weekFields = WeekFields.of(Locale.getDefault())
        val weekOfYear = now.get(weekFields.weekOfWeekBasedYear())
        return "Week $weekOfYear"
    }

    fun updateWeek(week: String) {
        _currentWeek.value = week
        loadExistingMealPlansOnly()
    }

    fun loadMealPlans() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                // Check if the user is authenticated
                val currentUser = supabase.auth.currentUserOrNull()
                _isUserAuthenticated.value = currentUser != null
                
                if (currentUser != null) {
                    try {
                        // Check if the user has saved meal plans
                        mealPlanRepository.hasMealPlans()
                            .catch { e -> 
                                Log.e("MealPlannerViewModel", "Error checking for meal plans", e)
                                // Continue with plan generation on error
                                generateMealPlanWithRealRecipes()
                            }
                            .collect { result ->
                                val hasMealPlans = result.getOrNull() ?: false
                                
                                if (hasMealPlans) {
                                    Log.d("MealPlannerViewModel", "User has saved meal plans")
                                    // Load the meal plans with proper error handling
                                    mealPlanRepository.getUserMealPlans()
                                        .catch { e ->
                                            if (e is CancellationException || e.message?.contains("composition") == true) {
                                                Log.d("MealPlannerViewModel", "Operation cancelled while loading meal plans")
                                            } else {
                                                Log.e("MealPlannerViewModel", "Error loading meal plans", e)
                                            }
                                            // Continue with plan generation if there's an error
                                            generateMealPlanWithRealRecipes()
                                        }
                                        .collect { userMealPlansResult ->
                                            userMealPlansResult.onSuccess { mealPlanData ->
                                                _mealPlans.value = mealPlanData
                                                
                                                // Calculate nutrition summaries
                                                val nutritionSummaries = calculateNutritionSummaries(mealPlanData)
                                                _dailyNutrition.value = nutritionSummaries
                                                
                                                _isLoading.value = false
                                                // Don't continue with the rest of the coroutine
                                                return@collect
                                            }.onFailure {
                                                // Continue with plan generation on failure
                                                generateMealPlanWithRealRecipes()
                                            }
                                        }
                                } else {
                                    // No user meal plans found, generate new plans
                                    generateMealPlanWithRealRecipes()
                                }
                            }
                    } catch (e: Exception) {
                        if (e is CancellationException || e.message?.contains("composition") == true) {
                            Log.d("MealPlannerViewModel", "Operation cancelled while loading meal plans")
                            return@launch
                        }
                        Log.e("MealPlannerViewModel", "Error in meal plan flow handling", e)
                        // Continue with generating a new meal plan
                        generateMealPlanWithRealRecipes()
                    }
                } else {
                    // Not authenticated, generate a new meal plan
                    generateMealPlanWithRealRecipes()
                }
            } catch (e: Exception) {
                Log.e("MealPlannerViewModel", "Error in loadMealPlans", e)
                _error.value = e.message
                _isLoading.value = false
                
                // Use fallback meal plans if everything else fails
                useFallbackMealPlans()
            }
        }
    }
    
    /**
     * Convert MealTime enum to string and back
     */
    private fun getMealTimeFromString(timeString: String): MealTime {
        return when (timeString) {
            "Breakfast" -> MealTime.Breakfast
            "Lunch" -> MealTime.Lunch 
            "Dinner" -> MealTime.Dinner
            else -> MealTime.Snacks
        }
    }

    /**
     * Generate a meal plan with real recipes
     * Uses MealPlanGenerator directly to avoid flow issues
     */
    private fun generateMealPlanWithRealRecipes() {
        viewModelScope.launch {
            try {
                _isGenerating.value = true
                _error.value = null
                _generationProgress.value = 0.1f
                _generationStage.value = "Initializing meal generation..."
                
                // Set default parameters
                var calorieTarget = 2000
                var dietType = "Balanced"
                var allergies = emptyList<String>()

                // Try to get user preferences if authenticated
                if (_isUserAuthenticated.value) {
                    try {
                        _generationStage.value = "Retrieving your preferences..."
                        
                        // Get user profile data
                        profileRepository.getProfile().collect { result ->
                            result.onSuccess { profile ->
                                // Update parameters based on profile
                                calorieTarget = profile.calorieTarget
                                dietType = profile.dietaryPreferences.firstOrNull()?.lowercase() ?: "Balanced"
                                allergies = profile.allergies
                                
                                Log.d("MealPlannerViewModel", "Using profile data: calories=$calorieTarget, diet=$dietType, allergies=$allergies")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("MealPlannerViewModel", "Error getting user profile: ${e.message}")
                        // Continue with defaults
                    }
                } else {
                    Log.d("MealPlannerViewModel", "User not authenticated, using default parameters")
                }

                // Fire up a quick job to pre-cache some recipes for better variety
                // This runs in parallel with the main meal plan generation
                var cacheJobCompleted = false
                
                // Use a shorter timeout for pre-caching
                val cacheJob = viewModelScope.launch {
                    try {
                        withTimeout(8000) { // 8 second timeout for pre-caching
                            _generationStage.value = "Finding delicious recipes for you..."
                            
                            // Launch parallel requests for different meal types
                            coroutineScope {
                                launch { repository.searchRecipes("breakfast $dietType", 10).catch { e ->
                                    Log.e("MealPlannerViewModel", "Error pre-caching breakfast recipes: ${e.message}")
                                }.collect {}}
                                
                                launch { repository.searchRecipes("lunch $dietType", 10).catch { e ->
                                    Log.e("MealPlannerViewModel", "Error pre-caching lunch recipes: ${e.message}")
                                }.collect {}}
                                
                                launch { repository.searchRecipes("dinner $dietType", 10).catch { e ->
                                    Log.e("MealPlannerViewModel", "Error pre-caching dinner recipes: ${e.message}")
                                }.collect {}}
                            }
                            
                            cacheJobCompleted = true
                            Log.d("MealPlannerViewModel", "Recipe pre-caching completed successfully")
                        }
                    } catch (e: Exception) {
                        Log.d("MealPlannerViewModel", "Pre-caching job timed out or failed: ${e.message}")
                        // This is expected - we don't need to wait for all pre-caching to complete
                        cacheJobCompleted = true
                    }
                }
                
                // Give the cache job a head start but don't wait indefinitely
                _generationStage.value = "Building your personalized meal plan..."
                
                // Wait up to 2 seconds for pre-caching to complete, but proceed anyway
                withTimeoutOrNull(2000) {
                    while (!cacheJobCompleted) {
                        delay(100)
                    }
                }
                
                if (!cacheJobCompleted) {
                    Log.d("MealPlannerViewModel", "Proceeding with meal plan generation while pre-caching continues in background")
                }
                
                // Now generate the meal plan directly using MealPlanGenerator
                val mealPlanResult = withTimeoutOrNull(30000) { // 30 seconds max for the main operation
                    try {
                        val days = DayOfWeek.values().toList()
                        MealPlanGenerator.generateMealPlan(
                            calorieTarget = calorieTarget,
                            days = days.size,
                            dietaryPreferences = listOf(dietType) + allergies
                        )
                    } catch (e: Exception) {
                        Log.e("MealPlannerViewModel", "Exception during meal plan generation: ${e.message}", e)
                        null
                    }
                } ?: Result.failure(MealPlanTimeoutException("Meal plan generation timed out"))
                
                mealPlanResult.fold(
                    onSuccess = { mealPlanMap -> 
                        Log.d("MealPlannerViewModel", "Successfully generated meal plan")
                        
                        // Convert to our UI state model
                        val mealItems = mutableListOf<MealPlanItem>()
                        
                        mealPlanMap.forEach { (dayStr, meals) ->
                            val day = try {
                                // Parse day string like "Day 1" to a DayOfWeek
                                val dayNum = dayStr.substringAfter("Day ").toIntOrNull() ?: 1
                                // Map day numbers to DayOfWeek values (1 = Monday, etc.)
                                DayOfWeek.of((dayNum - 1) % 7 + 1)
                            } catch (e: Exception) {
                                // Default to Monday if parsing fails
                                DayOfWeek.MONDAY
                            }
                            
                            meals.forEachIndexed { index, meal ->
                                // Assign different meal times based on the index
                                val mealTime = when (index % 4) {
                                    0 -> MealTime.Breakfast
                                    1 -> MealTime.Lunch
                                    2 -> MealTime.Dinner
                                    else -> MealTime.Snacks
                                }
                                
                                mealItems.add(
                                    MealPlanItem(
                                        id = meal.id,
                                        name = when (mealTime) {
                                            MealTime.Breakfast -> "Breakfast"
                                            MealTime.Lunch -> "Lunch"
                                            MealTime.Dinner -> "Dinner" 
                                            MealTime.Snacks -> "Snacks"
                                        },
                                        day = day,
                                        time = mealTime,
                                        calories = meal.nutritionFacts.calories,
                                        recipeId = meal.id,
                                        imageUrl = meal.imageUrl
                                    )
                                )
                            }
                        }
                        
                        // Calculate nutrition summary
                        val summaries = calculateNutritionSummaries(mealItems)
                        
                        _generationStage.value = "Finalizing your meal plan..."
                        _generationProgress.value = 0.8f
                        
                        _mealPlans.value = mealItems.groupBy { it.day }
                        _dailyNutrition.value = summaries
                        
                        // Save the meal plan if user is authenticated
                        if (_isUserAuthenticated.value) {
                            try {
                                mealPlanRepository.saveUserMealPlans(mealItems.groupBy { it.day }).collect { saveResult ->
                                    saveResult.onSuccess {
                                        Log.d("MealPlannerViewModel", "Successfully saved auto-generated meal plans")
                                    }
                                    saveResult.onFailure { error ->
                                        Log.e("MealPlannerViewModel", "Failed to save auto-generated meal plans", error)
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("MealPlannerViewModel", "Error saving auto-generated meal plans", e)
                            }
                        }
                    },
                    onFailure = { error ->
                        Log.w("MealPlannerViewModel", "Failed to generate meal plan, using fallback")
                        Log.e("MealPlannerViewModel", "Error: ${error.message}")
                        
                        // Use a hardcoded fallback plan
                        createFallbackMealPlan(calorieTarget = 2000, dietType = "Balanced")
                    }
                )
            } catch (e: Exception) {
                Log.e("MealPlannerViewModel", "Error generating meal plan: ${e.message}", e)
                _error.value = e.message ?: "An unexpected error occurred"
                
                // Use offline fallback
                try {
                    val fallbackPlan = createOfflineMealPlan(2000, "balanced")
                    val fallbackNutrition = calculateNutritionSummaries(fallbackPlan)
                    
                    _mealPlans.value = fallbackPlan
                    _dailyNutrition.value = fallbackNutrition
                } catch (fallbackError: Exception) {
                    Log.e("MealPlannerViewModel", "Failed to create fallback plan", fallbackError)
                }
            } finally {
                _isGenerating.value = false
                _generationStage.value = null
                _generationProgress.value = 0f
            }
        }
    }
    
    private fun createDiverseMealPlan(sourceData: Map<DayOfWeek, List<MealPlanItem>>): Map<DayOfWeek, List<MealPlanItem>> {
        try {
            Log.d("MealPlannerViewModel", "Creating diverse meal plan from ${sourceData.size} days of source data")
            
            // Collect all meals from all days into one pool
            val allMeals = sourceData.values.flatten()
            
            if (allMeals.isEmpty()) {
                Log.w("MealPlannerViewModel", "No meals available to create diverse plan")
                return emptyMap()
            }
            
            // Track used recipe IDs to avoid duplicates
            val usedRecipeIds = mutableSetOf<String?>()
            
            // Group meals by their type (breakfast, lunch, dinner, etc.)
            val mealsByTime = allMeals.groupBy { it.time }
            
            // Create a map to store our optimized plan
            val result = mutableMapOf<DayOfWeek, List<MealPlanItem>>()
            
            // Determine needed days (either all days in source or all days of week)
            val daysToFill = if (sourceData.isNotEmpty()) sourceData.keys else DayOfWeek.values().toList()
            
            // Create a diverse plan for each day
            for (day in daysToFill) {
                val dayMeals = mutableListOf<MealPlanItem>()
                
                // For each meal time, try to find a unique recipe
                for (mealTime in MealTime.values()) {
                    val mealsForTime = mealsByTime[mealTime] ?: emptyList()
                    
                    // Find meals that haven't been used yet (unique recipes)
                    val unusedMeals = mealsForTime.filter { meal -> 
                        meal.recipeId == null || !usedRecipeIds.contains(meal.recipeId)
                    }
                    
                    // Choose a meal, preferring unused ones
                    val selectedMeal = if (unusedMeals.isNotEmpty()) {
                        unusedMeals.random()
                    } else if (mealsForTime.isNotEmpty()) {
                        // If all recipes are used, just pick a random one
                        mealsForTime.random()
                    } else {
                        // If no meals available for this time, try to get one from another time
                        val anyUnusedMeal = allMeals.filter { meal -> 
                            meal.recipeId == null || !usedRecipeIds.contains(meal.recipeId)
                        }.randomOrNull()
                        
                        anyUnusedMeal?.copy(time = mealTime) ?: continue
                    }
                    
                    // Track this recipe as used
                    if (selectedMeal.recipeId != null) {
                        usedRecipeIds.add(selectedMeal.recipeId)
                    }
                    
                    // Add to our day's meals with this day set
                    dayMeals.add(selectedMeal.copy(day = day))
                }
                
                // Only add days that have at least one meal
                if (dayMeals.isNotEmpty()) {
                    result[day] = dayMeals
                }
            }
            
            Log.d("MealPlannerViewModel", "Successfully created diverse meal plan with ${result.size} days and ${usedRecipeIds.size} unique recipes")
            return result
            
        } catch (e: Exception) {
            Log.e("MealPlannerViewModel", "Error creating diverse meal plan", e)
            return sourceData // Return original data on error
        }
    }
    
    // Helper function to get default meal images if none provided
    private fun getDefaultMealImage(mealType: String): String {
        return when(mealType) {
            "Breakfast" -> "https://images.unsplash.com/photo-1533089860892-a9c9f5a37eb5?q=80&w=2370&auto=format&fit=crop"
            "Lunch" -> "https://images.unsplash.com/photo-1546793665-c74683f339c1?q=80&w=2374&auto=format&fit=crop"
            "Dinner" -> "https://images.unsplash.com/photo-1547592180-85f173990554?q=80&w=2370&auto=format&fit=crop"
            "Snacks" -> "https://images.unsplash.com/photo-1612105675765-07416cdb0b89?q=80&w=1974&auto=format&fit=crop"
            else -> "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?q=80&w=2360&auto=format&fit=crop"
        }
    }

    // Fix method name clash and references
    private fun calculateNutritionSummaries(mealItems: List<MealPlanItem>): Map<DayOfWeek, NutritionSummary> {
        // Group by day
        val mealsByDay = mealItems.groupBy { it.day }
        
        // Calculate totals for each day
        return mealsByDay.mapValues { (_, meals) ->
            NutritionSummary(
                calories = meals.sumOf { it.calories },
                // Estimate macros based on calories - in a real app you'd get this from the recipes
                protein = (meals.sumOf { it.calories } * 0.2f / 4).toInt(), // 20% protein
                carbs = (meals.sumOf { it.calories } * 0.5f / 4).toInt(),   // 50% carbs
                fat = (meals.sumOf { it.calories } * 0.3f / 9).toInt()      // 30% fat
            )
        }
    }
    
    private fun useFallbackMealPlans() {
        // Create a fallback plan with the offline meal generator
        val enhancedPlan = createOfflineMealPlan(2000, "balanced")
        val nutritionSummaries = calculateNutritionSummaries(enhancedPlan)
        
        _mealPlans.value = enhancedPlan
        _dailyNutrition.value = nutritionSummaries
    }

    fun getMealsForDay(day: DayOfWeek): List<MealPlanItem> {
        return _mealPlans.value[day] ?: emptyList()
    }

    fun addMeal(meal: MealPlanItem) {
        val currentMeals = _mealPlans.value.toMutableMap()
        val dayMeals = currentMeals[meal.day]?.toMutableList() ?: mutableListOf()
        dayMeals.add(meal)
        currentMeals[meal.day] = dayMeals
        _mealPlans.value = currentMeals
        updateNutritionSummary(meal.day)
        try { LocalMealPlanStore.save(appContext, _mealPlans.value) } catch (_: Exception) {}
        viewModelScope.launch {
            try {
                mealPlanRepository.saveUserMealPlans(_mealPlans.value).collect { }
            } catch (_: Exception) {}
        }
    }

    fun removeMeal(mealId: String) {
        val currentMeals = _mealPlans.value.toMutableMap()
        var affectedDay: DayOfWeek? = null
        
        currentMeals.forEach { (day, meals) ->
            val filteredMeals = meals.filter { it.id != mealId }
            if (filteredMeals.size != meals.size) {
                affectedDay = day
            }
            currentMeals[day] = filteredMeals
        }
        
        _mealPlans.value = currentMeals
        affectedDay?.let { updateNutritionSummary(it) }
        try { LocalMealPlanStore.save(appContext, _mealPlans.value) } catch (_: Exception) {}
        viewModelScope.launch {
            try {
                mealPlanRepository.saveUserMealPlans(_mealPlans.value).collect { }
            } catch (_: Exception) {}
        }
    }

    fun updateMeal(updated: MealPlanItem) {
        val currentMeals = _mealPlans.value.toMutableMap()
        val dayMeals = currentMeals[updated.day]?.toMutableList() ?: mutableListOf()
        val index = dayMeals.indexOfFirst { it.id == updated.id }
        if (index >= 0) {
            dayMeals[index] = updated
            currentMeals[updated.day] = dayMeals
            _mealPlans.value = currentMeals
            updateNutritionSummary(updated.day)
            try { LocalMealPlanStore.save(appContext, _mealPlans.value) } catch (_: Exception) {}
            viewModelScope.launch {
                try { mealPlanRepository.saveUserMealPlans(_mealPlans.value).collect { } } catch (_: Exception) {}
            }
        }
    }
    
    private fun updateNutritionSummary(day: DayOfWeek) {
        val meals = _mealPlans.value[day] ?: emptyList()
        // Launch async aggregation so UI stays responsive; keep heuristic until we compute
        val heuristicCalories = meals.sumOf { it.calories }
        val heuristic = NutritionSummary(
            calories = heuristicCalories,
            protein = (heuristicCalories * 0.3).toInt(),
            carbs = (heuristicCalories * 0.4).toInt(),
            fat = (heuristicCalories * 0.3).toInt()
        )
        _dailyNutrition.value = _dailyNutrition.value.toMutableMap().apply { put(day, heuristic) }

        viewModelScope.launch {
            try {
                var totalCalories = 0
                var totalProtein = 0f
                var totalCarbs = 0f
                var totalFat = 0f

                val nutritionRepo = com.thenewkenya.ingrediet.data.repository.NutritionRepository(appContext)
                val recipeRepo = RecipeRepository(appContext)

                fun cookingFactors(recipeName: String, description: String?): Triple<Float, Float, Float> {
                    val text = (recipeName + " " + (description ?: "")).lowercase()
                    return when {
                        // frying increases fat via oil absorption; minor protein loss
                        text.contains("fried") || text.contains("fry") -> Triple(1.0f, 0.95f, 1.15f)
                        // roasting reduces water, concentrates macros slightly
                        text.contains("roast") || text.contains("baked") -> Triple(1.0f, 1.05f, 1.05f)
                        // boiling may leach some carbs; protein/fat largely unchanged
                        text.contains("boil") || text.contains("simmer") -> Triple(0.95f, 1.0f, 1.0f)
                        else -> Triple(1.0f, 1.0f, 1.0f)
                    }
                }

                // For each meal, fetch its recipe and aggregate
                for (meal in meals) {
                    val recipeId = meal.recipeId ?: continue
                    val recipe = recipeRepo.getRecipeDetails(recipeId).first().getOrNull() ?: continue
                    val (carbFactor, proteinFactor, fatFactor) = cookingFactors(recipe.name, recipe.description)
                    for (ing in recipe.ingredients) {
                        val nut = nutritionRepo.getNutritionByName(ing.name) ?: continue
                        val grams = com.thenewkenya.ingrediet.feature.recipe.UnitConversion.toGrams(ing.quantity, ing.unit, ing.name)
                        val totals = com.thenewkenya.ingrediet.feature.recipe.NutritionMath.totalForWeight(nut.per100g, grams)
                        // Apply cooking method factors
                        totalCalories += totals.calories
                        totalProtein += totals.protein * proteinFactor
                        totalCarbs += totals.carbs * carbFactor
                        totalFat += totals.fat * fatFactor
                    }
                }

                val computed = NutritionSummary(
            calories = totalCalories,
                    protein = totalProtein.toInt(),
                    carbs = totalCarbs.toInt(),
                    fat = totalFat.toInt()
                )
                _dailyNutrition.value = _dailyNutrition.value.toMutableMap().apply { put(day, computed) }
            } catch (e: Exception) {
                // Keep heuristic on failure
                android.util.Log.e("MealPlannerViewModel", "Failed to compute nutrition: ${e.message}", e)
            }
        }
    }
    
    // New method that creates a meal plan using entirely offline data
    private fun createOfflineMealPlan(calorieTarget: Int, dietType: String): Map<DayOfWeek, List<MealPlanItem>> {
        val daysOfWeek = DayOfWeek.values()
        val mealPlan = mutableMapOf<DayOfWeek, List<MealPlanItem>>()
        
        val breakfastCalories = (calorieTarget * 0.25).toInt()
        val lunchCalories = (calorieTarget * 0.35).toInt()
        val dinnerCalories = (calorieTarget * 0.30).toInt()
        val snackCalories = (calorieTarget * 0.10).toInt()
        
        // Create expanded collections of predefined meals that match the diet type
        val breakfastOptions = getPresetMeals("Breakfast", dietType)
        val lunchOptions = getPresetMeals("Lunch", dietType)
        val dinnerOptions = getPresetMeals("Dinner", dietType)
        val snackOptions = getPresetMeals("Snacks", dietType)
        
        // Track used meals to ensure maximum variety
        val usedBreakfasts = mutableSetOf<String>()
        val usedLunches = mutableSetOf<String>()
        val usedDinners = mutableSetOf<String>()
        val usedSnacks = mutableSetOf<String>()
        
        // Generate a meal plan for each day with proper meal distribution
        // and ensuring maximum variety across days
        daysOfWeek.forEach { day ->
            // For each meal type, select an unused meal if possible
            val availableBreakfasts = breakfastOptions.filter { it !in usedBreakfasts }
            val breakfast = if (availableBreakfasts.isNotEmpty()) {
                availableBreakfasts.random().also { usedBreakfasts.add(it) }
            } else {
                // If all options have been used, reset and pick a random one
                usedBreakfasts.clear()
                breakfastOptions.random().also { usedBreakfasts.add(it) }
            }
            
            val availableLunches = lunchOptions.filter { it !in usedLunches }
            val lunch = if (availableLunches.isNotEmpty()) {
                availableLunches.random().also { usedLunches.add(it) }
            } else {
                usedLunches.clear()
                lunchOptions.random().also { usedLunches.add(it) }
            }
            
            val availableDinners = dinnerOptions.filter { it !in usedDinners }
            val dinner = if (availableDinners.isNotEmpty()) {
                availableDinners.random().also { usedDinners.add(it) }
            } else {
                usedDinners.clear()
                dinnerOptions.random().also { usedDinners.add(it) }
            }
            
            val availableSnacks = snackOptions.filter { it !in usedSnacks }
            val snack = if (availableSnacks.isNotEmpty()) {
                availableSnacks.random().also { usedSnacks.add(it) }
            } else {
                usedSnacks.clear()
                snackOptions.random().also { usedSnacks.add(it) }
            }
            
            // Create meal items with unique IDs based on timestamp
            val breakfastItem = MealPlanItem(
                id = "${day}_BREAKFAST_${System.currentTimeMillis() + day.ordinal}",
                name = "Breakfast",
                calories = breakfastCalories,
                day = day,
                time = MealTime.Breakfast,
                description = breakfast,
                recipeId = null,
                imageUrl = "https://source.unsplash.com/random/300x200?breakfast,${breakfast.hashCode()}"
            )
            
            val lunchItem = MealPlanItem(
                id = "${day}_LUNCH_${System.currentTimeMillis() + day.ordinal}",
                name = "Lunch",
                calories = lunchCalories,
                day = day,
                time = MealTime.Lunch,
                description = lunch,
                recipeId = null,
                imageUrl = "https://source.unsplash.com/random/300x200?lunch,${lunch.hashCode()}"
            )
            
            val dinnerItem = MealPlanItem(
                id = "${day}_DINNER_${System.currentTimeMillis() + day.ordinal}",
                name = "Dinner",
                calories = dinnerCalories,
                day = day,
                time = MealTime.Dinner,
                description = dinner,
                recipeId = null,
                imageUrl = "https://source.unsplash.com/random/300x200?dinner,${dinner.hashCode()}"
            )
            
            val snackItem = MealPlanItem(
                id = "${day}_SNACKS_${System.currentTimeMillis() + day.ordinal}",
                name = "Snacks",
                calories = snackCalories,
                day = day,
                time = MealTime.Snacks,
                description = snack,
                recipeId = null,
                imageUrl = "https://source.unsplash.com/random/300x200?snack,${snack.hashCode()}"
            )
            
            mealPlan[day] = listOf(breakfastItem, lunchItem, dinnerItem, snackItem)
        }
        
        return mealPlan
    }
    
    // Helper method to get preset meals based on meal type and diet type
    private fun getPresetMeals(mealType: String, dietType: String): List<String> {
        // Base options for each meal type - expanded with more variety
        val baseOptions = when (mealType) {
            "Breakfast" -> listOf(
                "Oatmeal with berries and nuts",
                "Avocado toast with eggs",
                "Greek yogurt with honey and granola",
                "Protein smoothie bowl",
                "Breakfast burrito with eggs and vegetables",
                "Whole grain cereal with milk and fruit",
                "Pancakes with fresh fruit",
                "Overnight oats with chia seeds",
                "Breakfast sandwich with egg and cheese",
                "Spinach and mushroom omelette",
                "French toast with maple syrup",
                "Breakfast hash with sweet potatoes",
                "Acai bowl with granola and banana",
                "Breakfast quesadilla with eggs and cheese",
                "Smoked salmon on whole grain toast"
            )
            "Lunch" -> listOf(
                "Grilled chicken salad",
                "Turkey and avocado wrap",
                "Vegetable soup with whole grain bread",
                "Quinoa bowl with roasted vegetables",
                "Mediterranean pasta salad",
                "Tuna salad sandwich",
                "Rice bowl with beans and vegetables",
                "Falafel wrap with hummus",
                "Caprese sandwich on sourdough",
                "Poke bowl with brown rice",
                "Greek salad with grilled chicken",
                "Tomato basil soup with grilled cheese",
                "Chicken Caesar wrap",
                "Bento box with rice, protein and vegetables",
                "Black bean and corn burrito bowl"
            )
            "Dinner" -> listOf(
                "Grilled salmon with steamed vegetables",
                "Chicken stir-fry with rice",
                "Beef stew with vegetables",
                "Vegetable lasagna",
                "Baked chicken with roasted potatoes",
                "Fish tacos with slaw",
                "Pasta with marinara sauce and vegetables",
                "Shrimp and vegetable skewers",
                "Roasted pork tenderloin with vegetables",
                "Vegetable curry with brown rice",
                "Beef and broccoli with quinoa",
                "Stuffed bell peppers with ground turkey",
                "Baked cod with lemon and herbs",
                "Spaghetti Bolognese with side salad",
                "Eggplant parmesan with whole grain pasta"
            )
            else -> listOf(
                "Apple with peanut butter",
                "Greek yogurt with berries",
                "Protein bar",
                "Handful of mixed nuts",
                "Carrot sticks with hummus",
                "String cheese with crackers",
                "Fruit smoothie",
                "Dark chocolate with almonds",
                "Rice cakes with avocado",
                "Cottage cheese with pineapple",
                "Trail mix with dried fruit",
                "Banana with almond butter",
                "Edamame with sea salt",
                "Roasted chickpeas",
                "Vegetable chips with guacamole"
            )
        }
        
        // Diet-specific options to add variety
        val dietSpecificOptions = when (dietType.lowercase()) {
            "low-carb" -> when (mealType) {
                "Breakfast" -> listOf(
                    "Egg and vegetable omelette",
                    "Chia seed pudding with berries",
                    "Cottage cheese with sliced tomatoes",
                    "Bacon and avocado plate",
                    "Low-carb breakfast bowl with eggs and greens",
                    "Smoked salmon and cream cheese roll-ups",
                    "Keto pancakes with butter",
                    "Egg muffins with spinach and feta",
                    "Avocado and egg breakfast salad",
                    "Low-carb yogurt parfait with nuts"
                )
                "Lunch" -> listOf(
                    "Lettuce wrap with turkey and cheese",
                    "Zucchini noodles with pesto and chicken",
                    "Cauliflower rice bowl with shrimp",
                    "Avocado egg salad",
                    "Spinach salad with steak strips",
                    "Cucumber subs with tuna",
                    "Cobb salad with ranch dressing",
                    "Stuffed avocados with chicken salad",
                    "Cabbage wrap with ground beef",
                    "Low-carb vegetable soup with protein"
                )
                "Dinner" -> listOf(
                    "Grilled salmon with asparagus",
                    "Chicken and vegetable stir-fry (no rice)",
                    "Stuffed bell peppers with ground turkey",
                    "Zucchini lasagna",
                    "Cauliflower crust pizza with vegetables",
                    "Baked cod with Brussels sprouts",
                    "Grilled steak with buttered vegetables",
                    "Pork chops with cabbage slaw",
                    "Turkey burgers with portobello buns",
                    "Spaghetti squash with meat sauce"
                )
                else -> listOf(
                    "Cheese cubes with olives",
                    "Celery sticks with cream cheese",
                    "Hard-boiled eggs",
                    "Beef jerky",
                    "Avocado with salt and pepper",
                    "Pepperoni chips",
                    "Cucumber with smoked salmon",
                    "Pork rinds with dip",
                    "Macadamia nuts",
                    "Kale chips with sea salt"
                )
            }
            "high-protein" -> when (mealType) {
                "Breakfast" -> listOf(
                    "Protein pancakes with whey protein",
                    "Egg white omelette with spinach and turkey",
                    "Greek yogurt parfait with extra protein",
                    "Protein smoothie with almond milk",
                    "Scrambled eggs with turkey bacon",
                    "Cottage cheese with fresh berries",
                    "Protein-packed breakfast sandwich",
                    "Tofu scramble with nutritional yeast",
                    "Salmon and egg breakfast bowl",
                    "High-protein overnight oats"
                )
                "Lunch" -> listOf(
                    "Grilled chicken breast with quinoa",
                    "Protein-packed tuna salad",
                    "Lentil soup with grilled chicken",
                    "Tempeh and vegetable stir-fry",
                    "Cottage cheese with fruit and nuts",
                    "Turkey and chickpea wrap",
                    "Salmon poke bowl with edamame",
                    "High-protein pasta with lean beef",
                    "Shrimp and bean salad",
                    "Chicken and black bean burrito bowl"
                )
                "Dinner" -> listOf(
                    "Baked cod with steamed broccoli",
                    "Turkey meatballs with zucchini noodles",
                    "Grilled steak with asparagus",
                    "Salmon with roasted Brussels sprouts",
                    "Chicken and black bean burrito bowl",
                    "Tofu and vegetable stir-fry",
                    "Lamb chops with protein-rich sides",
                    "Bison burger with sweet potato fries",
                    "Protein-packed vegetable curry",
                    "Grilled chicken kabobs with vegetables"
                )
                else -> listOf(
                    "Protein shake",
                    "Turkey roll-ups",
                    "Edamame",
                    "Greek yogurt with protein powder",
                    "Tuna on cucumber slices",
                    "Beef jerky with nuts",
                    "Protein bar",
                    "Boiled eggs with hot sauce",
                    "Protein pudding",
                    "Cottage cheese with flaxseeds"
                )
            }
            "vegetarian" -> when (mealType) {
                "Breakfast" -> listOf(
                    "Vegetable frittata",
                    "Peanut butter banana toast",
                    "Breakfast quinoa bowl",
                    "Spinach and feta omelette",
                    "Avocado smoothie bowl",
                    "Vegetarian breakfast burrito",
                    "Banana pancakes with nut butter",
                    "Breakfast power bowl with beans",
                    "Cheese and vegetable breakfast muffins",
                    "Yogurt parfait with fruits and nuts"
                )
                "Lunch" -> listOf(
                    "Caprese sandwich",
                    "Lentil and vegetable soup",
                    "Falafel wrap with tahini",
                    "Spinach and strawberry salad",
                    "Vegetable sushi rolls",
                    "Stuffed sweet potato with black beans",
                    "Veggie burger with avocado",
                    "Greek salad with feta cheese",
                    "Mushroom and spinach quesadilla",
                    "Mediterranean vegetable wrap"
                )
                "Dinner" -> listOf(
                    "Eggplant parmesan",
                    "Black bean and sweet potato tacos",
                    "Mushroom risotto",
                    "Vegetable curry with rice",
                    "Stuffed bell peppers with quinoa",
                    "Vegetarian chili with cornbread",
                    "Pasta primavera with cheese",
                    "Spinach and ricotta cannelloni",
                    "Vegetable lasagna with tofu ricotta",
                    "Cauliflower and chickpea curry"
                )
                else -> listOf(
                    "Hummus with bell pepper slices",
                    "Trail mix with dried fruit",
                    "Roasted chickpeas",
                    "Caprese skewers",
                    "Yogurt parfait with granola",
                    "Cheese and crackers",
                    "Apple with nut butter",
                    "Vegetable chips with salsa",
                    "Greek yogurt with honey",
                    "Stuffed dates with cream cheese"
                )
            }
            "vegan" -> when (mealType) {
                "Breakfast" -> listOf(
                    "Overnight oats with almond milk",
                    "Tofu scramble with vegetables",
                    "Avocado toast with nutritional yeast",
                    "Chia seed pudding with coconut milk",
                    "Green smoothie bowl",
                    "Vegan protein pancakes",
                    "Quinoa breakfast bowl with fruits",
                    "Chickpea flour omelette",
                    "Whole grain cereal with plant milk",
                    "Vegan breakfast burrito with beans"
                )
                "Lunch" -> listOf(
                    "Quinoa salad with roasted vegetables",
                    "Lentil and vegetable soup",
                    "Hummus and vegetable wrap",
                    "Buddha bowl with tahini dressing",
                    "Vegan burrito with beans and rice",
                    "Falafel pita with tahini sauce",
                    "Vegan sushi rolls with avocado",
                    "Chickpea salad sandwich",
                    "Sweet potato and black bean bowl",
                    "Tempeh BLT sandwich"
                )
                "Dinner" -> listOf(
                    "Chickpea curry with coconut milk",
                    "Stuffed bell peppers with lentils",
                    "Vegetable stir-fry with tofu",
                    "Cauliflower steaks with chimichurri",
                    "Pasta with vegan pesto and vegetables",
                    "Vegan lentil shepherd's pie",
                    "Black bean and corn enchiladas",
                    "Vegetable paella with saffron",
                    "Mushroom and walnut bolognese",
                    "Sweet potato and chickpea curry"
                )
                else -> listOf(
                    "Apple slices with almond butter",
                    "Roasted chickpeas",
                    "Energy bites with dates and nuts",
                    "Vegetable sticks with hummus",
                    "Fruit salad with mint",
                    "Avocado chocolate pudding",
                    "Trail mix with dried fruit and seeds",
                    "Rice cakes with nut butter",
                    "Coconut yogurt with berries",
                    "Seaweed snacks"
                )
            }
            else -> emptyList() // Use base options for balanced diet
        }
        
        // Combine base options with diet-specific options and return a shuffled list
        return (baseOptions + dietSpecificOptions).shuffled()
    }
    
    // Background function to refresh recipe cache without blocking UI
    private suspend fun refreshRecipeCacheInBackground(limit: Int = 40) {
        try {
            Log.d("MealPlannerViewModel", "Starting background recipe cache refresh for future variety")
            
            // Use a coroutine scope to run multiple requests in parallel
            kotlinx.coroutines.coroutineScope {
                // Launch different recipe queries in parallel with shorter timeouts
                val mealTypes = listOf("breakfast", "lunch", "dinner", "snack", "dessert")
                val cuisines = listOf("african", "american", "asian", "european", "mediterranean")
                
                // Randomize meal types and cuisines to get different recipes each time
                val selectedMealTypes = mealTypes.shuffled().take(3)
                val selectedCuisines = cuisines.shuffled().take(2)
                
                // Launch parallel recipe searches
                val jobs = mutableListOf<kotlinx.coroutines.Job>()
                
                // Search by meal type
                selectedMealTypes.forEach { mealType ->
                    jobs.add(launch {
                        withTimeoutOrNull(5000) {
                            repository.searchRecipes(mealType, limit = limit / 5).first()
                        }
                    })
                }
                
                // Search by cuisine
                selectedCuisines.forEach { cuisine ->
                    jobs.add(launch {
                        withTimeoutOrNull(5000) {
                            repository.searchRecipes(cuisine, limit = limit / 5).first()
                        }
                    })
                }
                
                // Get random recipes
                jobs.add(launch {
                    withTimeoutOrNull(8000) {
                        repository.getRandomRecipes(limit / 2).first()
                    }
                })
                
                // Wait for all jobs to complete or timeout
                withTimeoutOrNull(10000) {
                    jobs.forEach { it.join() }
                }
                
                // Cancel any remaining jobs to prevent wasting resources
                jobs.forEach { if (it.isActive) it.cancel() }
            }
            
            Log.d("MealPlannerViewModel", "Background recipe cache refresh completed")
        } catch (e: Exception) {
            Log.w("MealPlannerViewModel", "Background recipe cache refresh interrupted: ${e.message}")
        }
    }

    // Save and export meal plan functionality
    fun generateMealPlanSummary(): String {
        val mealPlanData = _mealPlans.value
        val nutritionData = _dailyNutrition.value
        
        if (mealPlanData.isEmpty()) {
            return "No meal plan available."
        }
        
        val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d")
        val today = LocalDate.now()
        val dateByDay = DayOfWeek.values().associateWith { day ->
            when (day) {
                DayOfWeek.MONDAY -> today.with(DayOfWeek.MONDAY)
                DayOfWeek.TUESDAY -> today.with(DayOfWeek.TUESDAY)
                DayOfWeek.WEDNESDAY -> today.with(DayOfWeek.WEDNESDAY)
                DayOfWeek.THURSDAY -> today.with(DayOfWeek.THURSDAY)
                DayOfWeek.FRIDAY -> today.with(DayOfWeek.FRIDAY)
                DayOfWeek.SATURDAY -> today.with(DayOfWeek.SATURDAY)
                DayOfWeek.SUNDAY -> today.with(DayOfWeek.SUNDAY)
            }
        }
        
        val summary = StringBuilder()
        summary.append("WEEKLY MEAL PLAN\n")
        summary.append("===================================\n\n")
        
        // Sort the days
        val sortedDays = DayOfWeek.values().toList()
        
        for (day in sortedDays) {
            val formattedDate = dateByDay[day]?.format(dateFormatter) ?: day.toString()
            summary.append("$formattedDate\n")
            summary.append("-----------------------------------\n")
            
            val mealsForDay = mealPlanData[day] ?: emptyList()
            if (mealsForDay.isEmpty()) {
                summary.append("No meals planned for this day.\n")
            } else {
                // Group and sort meals by time
                val mealsByType = mealsForDay.groupBy { it.name }
                val mealTypes = listOf("Breakfast", "Lunch", "Dinner", "Snacks")
                
                for (mealType in mealTypes) {
                    mealsByType[mealType]?.firstOrNull()?.let { meal ->
                        summary.append("$mealType (${meal.time}): ${meal.description ?: meal.name} - ${meal.calories}kcal\n")
                    }
                }
            }
            
            // Add daily nutrition summary if available
            nutritionData[day]?.let { nutrition ->
                summary.append("\nNutrition: ${nutrition.calories}kcal | ")
                summary.append("Protein: ${nutrition.protein}g | ")
                summary.append("Carbs: ${nutrition.carbs}g | ")
                summary.append("Fat: ${nutrition.fat}g\n")
            }
            
            summary.append("\n")
        }
        
        // Add a footer
        summary.append("===================================\n")
        summary.append("Generated by IngreDiet | ${LocalDate.now().format(DateTimeFormatter.ISO_DATE)}")
        
        return summary.toString()
    }
    
    fun saveMealPlanToFile(text: String): Result<String> {
        return try {
            val fileName = "MealPlan_${LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)}.txt"
            val downloadsDir = appContext.getExternalFilesDir(null)
            val file = File(downloadsDir?.path ?: "", fileName)
            
            file.outputStream().use { outputStream ->
                outputStream.write(text.toByteArray())
            }
            
            Result.success(file.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun shareMealPlan(): Pair<String, String> {
        val text = generateMealPlanSummary()
        val fileName = "MealPlan_${LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)}.txt"
        
        return Pair(text, fileName)
    }


    /**
     * Generate a meal plan with specific parameters
     * Uses MealPlanGenerator directly to avoid flow issues
     */
    fun generateMealPlan(calorieTarget: Int, dietType: String, allergies: List<String> = emptyList()) {
        viewModelScope.launch {
            try {
                _isGenerating.value = true
                _error.value = null
                _generationProgress.value = 0.5f
                _generationStage.value = "Generating your meal plan..."
                
                android.util.Log.d("MealPlannerViewModel", "Starting meal plan generation with calories=$calorieTarget, diet=$dietType, allergies=$allergies")
                
                // Generate the plan
                val plan = buildNutritionAwareMealPlan(calorieTarget, dietType, allergies)
                android.util.Log.d("MealPlannerViewModel", "Plan generation completed. Plan size: ${plan.size}")
                android.util.Log.d("MealPlannerViewModel", "Plan contents: ${plan.mapValues { it.value.size }}")
                
                // Check if we got a valid plan
                val hasContent = plan.values.any { it.isNotEmpty() }
                android.util.Log.d("MealPlannerViewModel", "Plan has content: $hasContent")
                
                if (hasContent) {
                    android.util.Log.d("MealPlannerViewModel", "Successfully generated plan with ${plan.values.sumOf { it.size }} meals")
                    android.util.Log.d("MealPlannerViewModel", "Setting mealPlans state...")
                    _mealPlans.value = plan
                    android.util.Log.d("MealPlannerViewModel", "MealPlans state updated. Current value size: ${_mealPlans.value.size}")
                    
                    // Save locally and to server
                    try { 
                        LocalMealPlanStore.save(appContext, plan)
                        android.util.Log.d("MealPlannerViewModel", "Saved plan locally")
                    } catch (e: Exception) {
                        android.util.Log.w("MealPlannerViewModel", "Failed to save locally: ${e.message}")
                    }
                    
                    viewModelScope.launch {
                        try { 
                            mealPlanRepository.saveUserMealPlans(plan).collect { }
                            android.util.Log.d("MealPlannerViewModel", "Saved plan to server")
                        } catch (e: Exception) {
                            android.util.Log.w("MealPlannerViewModel", "Failed to save to server: ${e.message}")
                        }
                    }
                    
                    // Update nutrition for all days
                    android.util.Log.d("MealPlannerViewModel", "Updating nutrition summaries...")
                    DayOfWeek.values().forEach { updateNutritionSummary(it) }
                    android.util.Log.d("MealPlannerViewModel", "Nutrition summaries updated")
                } else {
                    android.util.Log.w("MealPlannerViewModel", "Generated plan was empty, using fallback")
                    android.util.Log.d("MealPlannerViewModel", "Creating offline fallback plan...")
                    val fallback = createOfflineMealPlan(calorieTarget, dietType)
                    android.util.Log.d("MealPlannerViewModel", "Fallback plan created with ${fallback.values.sumOf { it.size }} meals")
                    _mealPlans.value = fallback
                    
                    try { LocalMealPlanStore.save(appContext, fallback) } catch (_: Exception) {}
                    viewModelScope.launch {
                        try { mealPlanRepository.saveUserMealPlans(fallback).collect { } } catch (_: Exception) {}
                    }
                    DayOfWeek.values().forEach { updateNutritionSummary(it) }
                }
                
                _generationProgress.value = 1f
                _generationStage.value = "Complete!"
                android.util.Log.d("MealPlannerViewModel", "Meal plan generation finished successfully")
            } catch (e: Exception) {
                android.util.Log.e("MealPlannerViewModel", "Meal plan generation failed: ${e.message}", e)
                _error.value = "Failed to generate meal plan: ${e.message}"
            } finally {
                _isGenerating.value = false
                kotlinx.coroutines.delay(1000) // Show "Complete!" for a moment
                _generationStage.value = null
                _generationProgress.value = 0f
            }
        }
    }

    /**
     * Helper functions to categorize recipes by meal type
     */
    private fun isBreakfastRecipe(recipe: com.thenewkenya.ingrediet.data.model.DetailedRecipe): Boolean {
        val name = recipe.name.lowercase()
        val category = recipe.category.lowercase()
        val tags = recipe.tags.map { it.lowercase() }
        val allTags = tags + recipe.dietaryInfo.map { it.lowercase() }
        
        // Breakfast keywords in name
        val breakfastKeywords = listOf(
            "breakfast", "pancake", "waffle", "cereal", "oatmeal", "porridge", "toast",
            "egg", "omelet", "omelette", "scrambled", "fried egg", "muffin", "croissant",
            "bagel", "smoothie", "yogurt", "granola", "coffee", "tea", "juice"
        )
        
        return category.contains("breakfast") ||
                tags.any { it.contains("breakfast") } ||
                breakfastKeywords.any { keyword -> name.contains(keyword) } ||
                allTags.any { tag -> breakfastKeywords.any { keyword -> tag.contains(keyword) } }
    }
    
    private fun isLunchRecipe(recipe: com.thenewkenya.ingrediet.data.model.DetailedRecipe): Boolean {
        val name = recipe.name.lowercase()
        val category = recipe.category.lowercase()
        val tags = recipe.tags.map { it.lowercase() }
        val allTags = tags + recipe.dietaryInfo.map { it.lowercase() }
        
        // Lunch keywords
        val lunchKeywords = listOf(
            "lunch", "sandwich", "salad", "wrap", "burger", "soup", "pasta", "pizza",
            "rice", "noodle", "stir fry", "bowl", "quinoa", "grain", "light meal"
        )
        
        return category.contains("lunch") ||
                tags.any { it.contains("lunch") } ||
                lunchKeywords.any { keyword -> name.contains(keyword) } ||
                allTags.any { tag -> lunchKeywords.any { keyword -> tag.contains(keyword) } }
    }
    
    private fun isDinnerRecipe(recipe: com.thenewkenya.ingrediet.data.model.DetailedRecipe): Boolean {
        val name = recipe.name.lowercase()
        val category = recipe.category.lowercase()
        val tags = recipe.tags.map { it.lowercase() }
        val allTags = tags + recipe.dietaryInfo.map { it.lowercase() }
        
        // Dinner keywords
        val dinnerKeywords = listOf(
            "dinner", "main course", "roast", "steak", "chicken", "beef", "pork", "fish",
            "seafood", "curry", "stew", "casserole", "lasagna", "grilled", "baked",
            "braised", "seared", "hearty", "main dish"
        )
        
        return category.contains("dinner") || category.contains("main") ||
                tags.any { it.contains("dinner") || it.contains("main") } ||
                dinnerKeywords.any { keyword -> name.contains(keyword) } ||
                allTags.any { tag -> dinnerKeywords.any { keyword -> tag.contains(keyword) } }
    }
    
    private fun isSnackRecipe(recipe: com.thenewkenya.ingrediet.data.model.DetailedRecipe): Boolean {
        val name = recipe.name.lowercase()
        val category = recipe.category.lowercase()
        val tags = recipe.tags.map { it.lowercase() }
        val allTags = tags + recipe.dietaryInfo.map { it.lowercase() }
        
        // Snack keywords
        val snackKeywords = listOf(
            "snack", "appetizer", "finger food", "dip", "chips", "nuts", "trail mix",
            "energy bar", "protein bar", "smoothie bowl", "fruit", "vegetable sticks",
            "crackers", "popcorn", "small bite"
        )
        
        return category.contains("snack") || category.contains("appetizer") ||
                tags.any { it.contains("snack") || it.contains("appetizer") } ||
                snackKeywords.any { keyword -> name.contains(keyword) } ||
                allTags.any { tag -> snackKeywords.any { keyword -> tag.contains(keyword) } }
    }
    
    /**
     * Get next recipe for a specific meal type with fallback
     */
    private fun getNextRecipeForMealType(
        mealTypeRecipes: List<com.thenewkenya.ingrediet.data.model.DetailedRecipe>,
        index: Int,
        fallbackRecipes: List<com.thenewkenya.ingrediet.data.model.DetailedRecipe>,
        mealType: String
    ): com.thenewkenya.ingrediet.data.model.DetailedRecipe? {
        return if (mealTypeRecipes.isNotEmpty()) {
            val recipe = mealTypeRecipes[index % mealTypeRecipes.size]
            android.util.Log.d("MealPlannerViewModel", "Selected $mealType recipe: ${recipe.name} (category: ${recipe.category})")
            recipe
        } else if (fallbackRecipes.isNotEmpty()) {
            val fallbackRecipe = fallbackRecipes.random()
            android.util.Log.d("MealPlannerViewModel", "Using fallback recipe for $mealType: ${fallbackRecipe.name}")
            fallbackRecipe
        } else {
            android.util.Log.w("MealPlannerViewModel", "No recipes available for $mealType")
            null
        }
    }

    private suspend fun buildNutritionAwareMealPlan(
        calorieTarget: Int,
        dietType: String,
        allergies: List<String>
    ): Map<DayOfWeek, List<MealPlanItem>> {
        android.util.Log.d("MealPlannerViewModel", "Starting simple meal plan generation...")
        android.util.Log.d("MealPlannerViewModel", "Parameters: calorieTarget=$calorieTarget, dietType='$dietType', allergies=$allergies")
        
        // Step 1: Get recipes - try random first, it's most reliable
        val allRecipes = mutableListOf<com.thenewkenya.ingrediet.data.model.DetailedRecipe>()
        
        try {
            android.util.Log.d("MealPlannerViewModel", "Fetching random recipes...")
            val randomResult = repository.getRandomRecipes(50).first()
            android.util.Log.d("MealPlannerViewModel", "Random result type: ${randomResult.javaClass.simpleName}")
            randomResult.onSuccess { recipes ->
                allRecipes.addAll(recipes)
                android.util.Log.d("MealPlannerViewModel", "Got ${recipes.size} random recipes")
                recipes.take(5).forEach { recipe ->
                    android.util.Log.d("MealPlannerViewModel", "Sample recipe: ${recipe.name} (id=${recipe.id})")
                }
            }
            randomResult.onFailure { error ->
                android.util.Log.e("MealPlannerViewModel", "Random recipes failed: ${error.message}", error)
            }
        } catch (e: Exception) {
            android.util.Log.w("MealPlannerViewModel", "Random recipes failed: ${e.message}")
            android.util.Log.w("MealPlannerViewModel", "Exception details:", e)
        }
        
        // If we have no recipes, return empty plan (will trigger fallback)
        if (allRecipes.isEmpty()) {
            android.util.Log.w("MealPlannerViewModel", "No recipes available, returning empty plan")
            return emptyMap()
        }
        
        android.util.Log.d("MealPlannerViewModel", "Total recipes fetched: ${allRecipes.size}")
        
        // Step 2: Simple filtering - only exclude obvious mismatches
        android.util.Log.d("MealPlannerViewModel", "Starting filtering with dietType='$dietType', allergies=$allergies")
        val usableRecipes = allRecipes.filter { recipe ->
            // Basic diet filtering - only strict exclusions
            val dietOk = when (dietType.lowercase()) {
                "vegan" -> {
                    val nonVegan = listOf("chicken", "beef", "pork", "fish", "egg", "milk", "cheese", "butter")
                    val isVeganOk = !recipe.name.lowercase().let { name -> nonVegan.any { name.contains(it) } }
                    if (!isVeganOk) {
                        android.util.Log.d("MealPlannerViewModel", "Filtered out non-vegan recipe: ${recipe.name}")
                    }
                    isVeganOk
                }
                "vegetarian" -> {
                    val nonVeg = listOf("chicken", "beef", "pork", "fish", "meat")
                    val isVegOk = !recipe.name.lowercase().let { name -> nonVeg.any { name.contains(it) } }
                    if (!isVegOk) {
                        android.util.Log.d("MealPlannerViewModel", "Filtered out non-vegetarian recipe: ${recipe.name}")
                    }
                    isVegOk
                }
                else -> {
                    android.util.Log.d("MealPlannerViewModel", "Diet '$dietType' - allowing all recipes")
                    true // Allow everything for other diets
                }
            }
            
            // Basic allergy filtering - only common allergens
            val allergyOk = if (allergies.isEmpty()) {
                android.util.Log.d("MealPlannerViewModel", "No allergies specified - allowing all recipes")
                true
            } else {
                android.util.Log.d("MealPlannerViewModel", "Checking allergies for recipe: ${recipe.name}")
                val recipeName = recipe.name.lowercase()
                val hasAllergy = allergies.any { allergy ->
                    when (allergy.lowercase()) {
                        "nuts", "peanuts" -> recipeName.contains("nut") || recipeName.contains("peanut")
                        "dairy" -> recipeName.contains("milk") || recipeName.contains("cheese")
                        "gluten" -> recipeName.contains("wheat") || recipeName.contains("bread")
                        else -> recipeName.contains(allergy.lowercase())
                    }
                }
                if (hasAllergy) {
                    android.util.Log.d("MealPlannerViewModel", "Filtered out recipe due to allergy: ${recipe.name}")
                }
                !hasAllergy
            }
            
            val keep = dietOk && allergyOk
            if (!keep) {
                android.util.Log.d("MealPlannerViewModel", "Recipe ${recipe.name} filtered out: dietOk=$dietOk, allergyOk=$allergyOk")
            }
            dietOk && allergyOk
        }
        
        val finalRecipes = if (usableRecipes.isNotEmpty()) usableRecipes else allRecipes
        android.util.Log.d("MealPlannerViewModel", "Using ${finalRecipes.size} recipes after filtering")
        android.util.Log.d("MealPlannerViewModel", "Filtered recipes count: usable=${usableRecipes.size}, final=${finalRecipes.size}")
        
        // Step 3: Build the plan - simple assignment
        android.util.Log.d("MealPlannerViewModel", "Starting plan building...")
        val plan = mutableMapOf<DayOfWeek, List<MealPlanItem>>()
        val shuffledRecipes = finalRecipes.shuffled() // Fallback recipes
        android.util.Log.d("MealPlannerViewModel", "Shuffled ${shuffledRecipes.size} total recipes for fallback")
        
        // Calculate target calories per meal
        val breakfastCals = (calorieTarget * 0.25).toInt()
        val lunchCals = (calorieTarget * 0.35).toInt()
        val dinnerCals = (calorieTarget * 0.30).toInt()
        val snackCals = (calorieTarget * 0.10).toInt()
        android.util.Log.d("MealPlannerViewModel", "Calorie targets: B=$breakfastCals, L=$lunchCals, D=$dinnerCals, S=$snackCals")
        
        // Categorize recipes by meal type
        val breakfastRecipes = finalRecipes.filter { isBreakfastRecipe(it) }
        val lunchRecipes = finalRecipes.filter { isLunchRecipe(it) }
        val dinnerRecipes = finalRecipes.filter { isDinnerRecipe(it) }
        val snackRecipes = finalRecipes.filter { isSnackRecipe(it) }
        
        android.util.Log.d("MealPlannerViewModel", "Categorized recipes: Breakfast=${breakfastRecipes.size}, Lunch=${lunchRecipes.size}, Dinner=${dinnerRecipes.size}, Snacks=${snackRecipes.size}")
        
        // Shuffle each category for variety
        val shuffledBreakfast = breakfastRecipes.shuffled()
        val shuffledLunch = lunchRecipes.shuffled()
        val shuffledDinner = dinnerRecipes.shuffled()
        val shuffledSnacks = snackRecipes.shuffled()
        
        // Keep track of indices for each meal type
        var breakfastIndex = 0
        var lunchIndex = 0
        var dinnerIndex = 0
        var snackIndex = 0
        
        DayOfWeek.values().forEach { day ->
            android.util.Log.d("MealPlannerViewModel", "Building meals for $day")
            val dayMeals = mutableListOf<MealPlanItem>()
            
            // Breakfast - use breakfast-specific recipes
            getNextRecipeForMealType(shuffledBreakfast, breakfastIndex, shuffledRecipes, "breakfast")?.let { recipe ->
                android.util.Log.d("MealPlannerViewModel", "Adding breakfast for $day: ${recipe.name}")
                dayMeals.add(
                    MealPlanItem(
                        id = "${recipe.id}-B-$day",
                        name = recipe.name,
                        calories = breakfastCals, // Use target calories for consistency
                        day = day,
                        time = MealTime.Breakfast,
                        description = recipe.description,
                        recipeId = recipe.id,
                        imageUrl = recipe.imageUrl
                    )
                )
                breakfastIndex++
            } ?: android.util.Log.w("MealPlannerViewModel", "No recipe available for breakfast on $day")
            
            // Lunch - use lunch-specific recipes
            getNextRecipeForMealType(shuffledLunch, lunchIndex, shuffledRecipes, "lunch")?.let { recipe ->
                android.util.Log.d("MealPlannerViewModel", "Adding lunch for $day: ${recipe.name}")
                dayMeals.add(
                    MealPlanItem(
                        id = "${recipe.id}-L-$day",
                        name = recipe.name,
                        calories = lunchCals,
                        day = day,
                        time = MealTime.Lunch,
                        description = recipe.description,
                        recipeId = recipe.id,
                        imageUrl = recipe.imageUrl
                    )
                )
                lunchIndex++
            } ?: android.util.Log.w("MealPlannerViewModel", "No recipe available for lunch on $day")
            
            // Dinner - use dinner-specific recipes
            getNextRecipeForMealType(shuffledDinner, dinnerIndex, shuffledRecipes, "dinner")?.let { recipe ->
                android.util.Log.d("MealPlannerViewModel", "Adding dinner for $day: ${recipe.name}")
                dayMeals.add(
                    MealPlanItem(
                        id = "${recipe.id}-D-$day",
                        name = recipe.name,
                        calories = dinnerCals,
                        day = day,
                        time = MealTime.Dinner,
                        description = recipe.description,
                        recipeId = recipe.id,
                        imageUrl = recipe.imageUrl
                    )
                )
                dinnerIndex++
            } ?: android.util.Log.w("MealPlannerViewModel", "No recipe available for dinner on $day")
            
            // Snack (optional) - use snack-specific recipes
            if (shuffledSnacks.isNotEmpty() || finalRecipes.size > 21) { // Add snacks if we have snack recipes or enough variety
                android.util.Log.d("MealPlannerViewModel", "Adding snack for $day")
                getNextRecipeForMealType(shuffledSnacks, snackIndex, shuffledRecipes, "snack")?.let { recipe ->
                    android.util.Log.d("MealPlannerViewModel", "Adding snack for $day: ${recipe.name}")
                    dayMeals.add(
                        MealPlanItem(
                            id = "${recipe.id}-S-$day",
                            name = recipe.name,
                            calories = snackCals,
                            day = day,
                            time = MealTime.Snacks,
                            description = recipe.description,
                            recipeId = recipe.id,
                            imageUrl = recipe.imageUrl
                        )
                    )
                    snackIndex++
                }
            } else {
                android.util.Log.d("MealPlannerViewModel", "Skipping snack for $day (no snack recipes available)")
            }
            
            android.util.Log.d("MealPlannerViewModel", "Day $day completed with ${dayMeals.size} meals")
            plan[day] = dayMeals
        }
        
        val totalMeals = plan.values.sumOf { it.size }
        android.util.Log.d("MealPlannerViewModel", "Generated plan with ${totalMeals} total meals across ${plan.size} days")
        
        // Log plan summary
        plan.forEach { (day, meals) ->
            android.util.Log.d("MealPlannerViewModel", "$day: ${meals.size} meals - ${meals.joinToString { "${it.time.name}:${it.name}" }}")
        }
        
        return plan
    }

    /**
     * Create a fallback meal plan to use when API calls fail
     */
    private fun createFallbackMealPlan(calorieTarget: Int, dietType: String) {
        // Create offline fallback meal plan
        _generationStage.value = "Creating offline meal plan for you..."
        val fallbackPlan = createOfflineMealPlan(calorieTarget, dietType)
        
        // Calculate nutrition summaries
        _generationStage.value = "Calculating nutrition values..."
        val nutritionSummaries = calculateNutritionSummaries(fallbackPlan)
        
        // Update UI state
        _mealPlans.value = fallbackPlan
        _dailyNutrition.value = nutritionSummaries
        
        _generationProgress.value = 1f
        _generationStage.value = "Done!"
        _isLoading.value = false
    }

    /**
     * Calculate nutrition summaries for groups of meals
     */
    private fun calculateNutritionSummaries(mealPlan: Map<DayOfWeek, List<MealPlanItem>>): Map<DayOfWeek, NutritionSummary> {
        return mealPlan.mapValues { (_, meals) ->
            NutritionSummary(
                calories = meals.sumOf { it.calories },
                protein = (meals.sumOf { it.calories } * 0.2f / 4).toInt(), // 20% protein
                carbs = (meals.sumOf { it.calories } * 0.5f / 4).toInt(),   // 50% carbs
                fat = (meals.sumOf { it.calories } * 0.3f / 9).toInt()      // 30% fat
            )
        }
    }

    /**
     * Loads only existing meal plans without auto-generating new ones.
     * Used by the home screen to avoid showing meal plans that weren’t explicitly generated.
     */
    fun loadExistingMealPlansOnly() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                // Check if the user is authenticated
                val currentUser = supabase.auth.currentUserOrNull()
                _isUserAuthenticated.value = currentUser != null
                
                if (currentUser != null) {
                    try {
                        // Check if the user has saved meal plans
                        mealPlanRepository.hasMealPlans()
                            .catch { e -> 
                                Log.e("MealPlannerViewModel", "Error checking for meal plans", e)
                                _isLoading.value = false
                            }
                            .collect { result ->
                                val hasMealPlans = result.getOrNull() ?: false
                                
                                if (hasMealPlans) {
                                    Log.d("MealPlannerViewModel", "User has saved meal plans")
                                    // Load the meal plans with proper error handling
                                    mealPlanRepository.getUserMealPlans()
                                        .catch { e ->
                                            Log.e("MealPlannerViewModel", "Error loading meal plans", e)
                                            _isLoading.value = false
                                        }
                                        .collect { userMealPlansResult ->
                                            userMealPlansResult.onSuccess { mealPlanData ->
                                                _mealPlans.value = mealPlanData
                                                
                                                // Calculate nutrition summaries
                                                val nutritionSummaries = calculateNutritionSummaries(mealPlanData)
                                                _dailyNutrition.value = nutritionSummaries
                                            }
                                            _isLoading.value = false
                                        }
                                } else {
                                    // No meal plans - just set empty state instead of generating
                                    _mealPlans.value = emptyMap()
                                    _dailyNutrition.value = emptyMap()
                                    _isLoading.value = false
                                }
                            }
                    } catch (e: Exception) {
                        Log.e("MealPlannerViewModel", "Error in meal plan flow handling", e)
                        _mealPlans.value = emptyMap()
                        _dailyNutrition.value = emptyMap()
                        _isLoading.value = false
                    }
                } else {
                    // Not authenticated, set empty state
                    _mealPlans.value = emptyMap()
                    _dailyNutrition.value = emptyMap()
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                Log.e("MealPlannerViewModel", "Error in loadExistingMealPlansOnly", e)
                _error.value = e.message
                _mealPlans.value = emptyMap()
                _dailyNutrition.value = emptyMap()
                _isLoading.value = false
            }
        }
    }
}

class MealPlannerViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MealPlannerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MealPlannerViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 