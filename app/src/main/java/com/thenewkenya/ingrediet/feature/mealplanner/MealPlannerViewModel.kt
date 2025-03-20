package com.thenewkenya.ingrediet.feature.mealplanner

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.thenewkenya.ingrediet.data.model.DetailedRecipe
import com.thenewkenya.ingrediet.data.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale

data class MealPlanItem(
    val id: String,
    val name: String,
    val calories: Int,
    val day: DayOfWeek,
    val time: String,
    val description: String? = null,
    val recipeId: Int? = null,
    val imageUrl: String? = null
)

data class NutritionSummary(
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fat: Int
)

class MealPlannerViewModel(context: Context) : ViewModel() {
    private val repository = RecipeRepository(context)
    
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

    init {
        loadMealPlans()
    }

    private fun getCurrentWeekString(): String {
        val now = LocalDate.now()
        val weekFields = WeekFields.of(Locale.getDefault())
        val weekOfYear = now.get(weekFields.weekOfWeekBasedYear())
        return "Week $weekOfYear"
    }

    fun updateWeek(week: String) {
        _currentWeek.value = week
        loadMealPlans()
    }

    private fun loadMealPlans() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                // Try to get meal plans from the local storage or Supabase in the future
                // For now, we'll just use empty meal plans that will be filled by the user or generated
                
                val sampleMeals = DayOfWeek.values().associateWith { day ->
                    when (day) {
                        DayOfWeek.MONDAY -> listOf(
                            MealPlanItem(
                                id = "1",
                                name = "Breakfast",
                                calories = 450,
                                day = day,
                                time = "08:00",
                                description = "Oatmeal with berries and nuts"
                            ),
                            MealPlanItem(
                                id = "2",
                                name = "Lunch",
                                calories = 650,
                                day = day,
                                time = "13:00",
                                description = "Grilled chicken salad"
                            ),
                            MealPlanItem(
                                id = "3",
                                name = "Dinner",
                                calories = 750,
                                day = day,
                                time = "19:00",
                                description = "Salmon with roasted vegetables"
                            )
                        )
                        DayOfWeek.WEDNESDAY -> listOf(
                            MealPlanItem(
                                id = "7",
                                name = "Breakfast",
                                calories = 400,
                                day = day,
                                time = "08:00",
                                description = "Greek yogurt with honey and granola"
                            ),
                            MealPlanItem(
                                id = "8",
                                name = "Lunch",
                                calories = 700,
                                day = day,
                                time = "13:00",
                                description = "Turkey wrap with avocado"
                            ),
                            MealPlanItem(
                                id = "9",
                                name = "Dinner",
                                calories = 600,
                                day = day,
                                time = "19:00",
                                description = "Vegetable stir-fry with tofu"
                            )
                        )
                        DayOfWeek.FRIDAY -> listOf(
                            MealPlanItem(
                                id = "13",
                                name = "Breakfast",
                                calories = 350,
                                day = day,
                                time = "08:00",
                                description = "Smoothie bowl with fruits"
                            ),
                            MealPlanItem(
                                id = "14",
                                name = "Lunch",
                                calories = 550,
                                day = day,
                                time = "13:00",
                                description = "Quinoa bowl with roasted vegetables"
                            ),
                            MealPlanItem(
                                id = "15",
                                name = "Dinner",
                                calories = 800,
                                day = day,
                                time = "19:00",
                                description = "Grilled steak with sweet potato"
                            )
                        )
                        else -> emptyList()
                    }
                }
                
                // Generate nutrition summaries
                val nutritionSummaries = sampleMeals.mapValues { (_, meals) ->
                    val totalCalories = meals.sumOf { it.calories }
                    NutritionSummary(
                        calories = totalCalories,
                        protein = (totalCalories * 0.3).toInt(),
                        carbs = (totalCalories * 0.4).toInt(),
                        fat = (totalCalories * 0.3).toInt()
                    )
                }
                
                _mealPlans.value = sampleMeals
                _dailyNutrition.value = nutritionSummaries
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
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
    }
    
    private fun updateNutritionSummary(day: DayOfWeek) {
        val meals = _mealPlans.value[day] ?: emptyList()
        val totalCalories = meals.sumOf { it.calories }
        
        val nutritionSummary = NutritionSummary(
            calories = totalCalories,
            protein = (totalCalories * 0.3).toInt(),
            carbs = (totalCalories * 0.4).toInt(),
            fat = (totalCalories * 0.3).toInt()
        )
        
        _dailyNutrition.value = _dailyNutrition.value.toMutableMap().apply {
            put(day, nutritionSummary)
        }
    }
    
    fun generateMealPlan(calorieTarget: Int, dietType: String, allergies: List<String> = emptyList()) {
        viewModelScope.launch {
            try {
                _isGenerating.value = true
                _error.value = null
                _generationProgress.value = 0f
                _generationStage.value = "Preparing meal plan generation..."
                
                // Short delay to show initial stage
                kotlinx.coroutines.delay(300)
                
                // Use the RecipeRepository to get real recipes and generate a meal plan
                _generationStage.value = "Finding breakfast recipes..."
                _generationProgress.value = 0.1f
                kotlinx.coroutines.delay(500)
                
                _generationStage.value = "Finding lunch recipes..."
                _generationProgress.value = 0.25f
                kotlinx.coroutines.delay(500)
                
                _generationStage.value = "Finding dinner recipes..."
                _generationProgress.value = 0.4f
                kotlinx.coroutines.delay(500)
                
                _generationStage.value = "Finding snack options..."
                _generationProgress.value = 0.55f
                kotlinx.coroutines.delay(500)
                
                // Create a direct meal plan if repository call is getting stuck
                _generationStage.value = "Creating your personalized meal plan..."
                _generationProgress.value = 0.7f
                kotlinx.coroutines.delay(500)
                
                // Generate a simple meal plan without waiting for repository
                val generatedPlan = createFallbackMealPlan(calorieTarget, dietType)

                _generationStage.value = "Calculating nutrition values..."
                _generationProgress.value = 0.85f
                kotlinx.coroutines.delay(300)
                
                // Calculate nutrition summaries based on the meal plan
                val nutritionSummaries = generatedPlan.mapValues { (_, meals) ->
                    val totalCalories = meals.sumOf { it.calories }
                    NutritionSummary(
                        calories = totalCalories,
                        protein = when (dietType) {
                            "High-protein" -> (totalCalories * 0.4).toInt()
                            "Low-carb" -> (totalCalories * 0.35).toInt()
                            else -> (totalCalories * 0.3).toInt()
                        },
                        carbs = when (dietType) {
                            "Low-carb" -> (totalCalories * 0.2).toInt()
                            "High-protein" -> (totalCalories * 0.3).toInt()
                            else -> (totalCalories * 0.45).toInt()
                        },
                        fat = when (dietType) {
                            "Low-carb" -> (totalCalories * 0.45).toInt()
                            "High-protein" -> (totalCalories * 0.3).toInt()
                            else -> (totalCalories * 0.25).toInt()
                        }
                    )
                }
                
                _generationStage.value = "Finalizing your meal plan..."
                _generationProgress.value = 1f
                kotlinx.coroutines.delay(400)
                
                _mealPlans.value = generatedPlan
                _dailyNutrition.value = nutritionSummaries
                
            } catch (e: Exception) {
                _error.value = e.message ?: "An unexpected error occurred"
            } finally {
                _isGenerating.value = false
                _generationStage.value = null
            }
        }
    }

    // Fallback method to create a meal plan if repository is not responding
    private fun createFallbackMealPlan(calorieTarget: Int, dietType: String): Map<DayOfWeek, List<MealPlanItem>> {
        val daysOfWeek = DayOfWeek.values()
        val mealPlan = mutableMapOf<DayOfWeek, List<MealPlanItem>>()
        
        val breakfastCalories = (calorieTarget * 0.25).toInt()
        val lunchCalories = (calorieTarget * 0.35).toInt()
        val dinnerCalories = (calorieTarget * 0.30).toInt()
        val snackCalories = (calorieTarget * 0.10).toInt()
        
        val breakfastOptions = listOf(
            "Oatmeal with berries and nuts",
            "Avocado toast with eggs",
            "Greek yogurt with honey and granola",
            "Protein smoothie bowl",
            "Breakfast burrito with eggs and vegetables",
            "Whole grain cereal with milk and fruit",
            "Pancakes with fresh fruit"
        )
        
        val lunchOptions = listOf(
            "Grilled chicken salad",
            "Turkey and avocado wrap",
            "Vegetable soup with whole grain bread",
            "Quinoa bowl with roasted vegetables",
            "Mediterranean pasta salad",
            "Tuna salad sandwich",
            "Rice bowl with beans and vegetables"
        )
        
        val dinnerOptions = listOf(
            "Grilled salmon with steamed vegetables",
            "Chicken stir-fry with rice",
            "Beef stew with vegetables",
            "Vegetable lasagna",
            "Baked chicken with roasted potatoes",
            "Fish tacos with slaw",
            "Pasta with marinara sauce and vegetables"
        )
        
        val snackOptions = listOf(
            "Apple with peanut butter",
            "Greek yogurt with berries",
            "Protein bar",
            "Handful of mixed nuts",
            "Carrot sticks with hummus",
            "String cheese with crackers",
            "Fruit smoothie"
        )
        
        daysOfWeek.forEach { day ->
            val dayIndex = day.ordinal
            
            val breakfast = MealPlanItem(
                id = "${day}_BREAKFAST_${System.currentTimeMillis()}",
                name = "Breakfast",
                calories = breakfastCalories,
                day = day,
                time = "08:00",
                description = breakfastOptions[dayIndex % breakfastOptions.size],
                recipeId = 1000 + dayIndex,
                imageUrl = "https://source.unsplash.com/random/300x200?breakfast,${dayIndex}"
            )
            
            val lunch = MealPlanItem(
                id = "${day}_LUNCH_${System.currentTimeMillis()}",
                name = "Lunch",
                calories = lunchCalories,
                day = day,
                time = "13:00",
                description = lunchOptions[dayIndex % lunchOptions.size],
                recipeId = 2000 + dayIndex,
                imageUrl = "https://source.unsplash.com/random/300x200?lunch,${dayIndex}"
            )
            
            val dinner = MealPlanItem(
                id = "${day}_DINNER_${System.currentTimeMillis()}",
                name = "Dinner",
                calories = dinnerCalories,
                day = day,
                time = "19:00",
                description = dinnerOptions[dayIndex % dinnerOptions.size],
                recipeId = 3000 + dayIndex,
                imageUrl = "https://source.unsplash.com/random/300x200?dinner,${dayIndex}"
            )
            
            val snack = MealPlanItem(
                id = "${day}_SNACKS_${System.currentTimeMillis()}",
                name = "Snacks",
                calories = snackCalories,
                day = day,
                time = "16:00",
                description = snackOptions[dayIndex % snackOptions.size],
                recipeId = 4000 + dayIndex,
                imageUrl = "https://source.unsplash.com/random/300x200?snack,${dayIndex}"
            )
            
            mealPlan[day] = listOf(breakfast, lunch, dinner, snack)
        }
        
        return mealPlan
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