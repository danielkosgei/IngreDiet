package com.thenewkenya.ingrediet.feature.mealplanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val time: String
)

class MealPlannerViewModel : ViewModel() {
    private val _currentWeek = MutableStateFlow(getCurrentWeekString())
    val currentWeek: StateFlow<String> = _currentWeek.asStateFlow()

    private val _mealPlans = MutableStateFlow<Map<DayOfWeek, List<MealPlanItem>>>(emptyMap())
    val mealPlans: StateFlow<Map<DayOfWeek, List<MealPlanItem>>> = _mealPlans.asStateFlow()

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
            // TODO: Load meal plans from repository
            // For now, using sample data
            val sampleMeals = DayOfWeek.values().associateWith { day ->
                listOf(
                    MealPlanItem(
                        id = "1",
                        name = "Breakfast",
                        calories = 500,
                        day = day,
                        time = "08:00"
                    ),
                    MealPlanItem(
                        id = "2",
                        name = "Lunch",
                        calories = 800,
                        day = day,
                        time = "13:00"
                    ),
                    MealPlanItem(
                        id = "3",
                        name = "Dinner",
                        calories = 600,
                        day = day,
                        time = "19:00"
                    )
                )
            }
            _mealPlans.value = sampleMeals
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
    }

    fun removeMeal(mealId: String) {
        val currentMeals = _mealPlans.value.toMutableMap()
        currentMeals.forEach { (day, meals) ->
            currentMeals[day] = meals.filter { it.id != mealId }
        }
        _mealPlans.value = currentMeals
    }
} 