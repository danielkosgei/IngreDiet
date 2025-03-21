package com.thenewkenya.ingrediet.data.model

import java.time.DayOfWeek
import kotlinx.serialization.Serializable
import com.thenewkenya.ingrediet.feature.mealplanner.MealPlanItem
import com.thenewkenya.ingrediet.feature.mealplanner.MealTime

/**
 * Represents a user's meal plan item stored in the database
 */
@Serializable
data class UserMealPlan(
    val id: String = "",
    val userId: String = "",
    val dayOfWeek: String = "", // Store as string representation of DayOfWeek enum
    val mealType: String = "", // Breakfast, Lunch, Dinner, etc.
    val recipeId: Int? = null,
    val mealName: String = "",
    val mealDescription: String? = null,
    val calories: Int = 0,
    val time: String = "", // Time of day for the meal (e.g., "08:00")
    val imageUrl: String? = null
) {
    fun toDayOfWeek(): DayOfWeek = DayOfWeek.valueOf(dayOfWeek)
    
    companion object {
        fun fromMealPlanItem(userId: String, item: MealPlanItem): UserMealPlan {
            return UserMealPlan(
                userId = userId,
                dayOfWeek = item.day.name,
                mealType = item.name,
                recipeId = item.recipeId,
                mealName = item.name,
                mealDescription = item.description,
                calories = item.calories,
                time = item.time.toString(),
                imageUrl = item.imageUrl
            )
        }
    }
}

/**
 * DTO for Supabase database operations
 */
@Serializable
data class UserMealPlanDto(
    val id: String? = null,
    val user_id: String,
    val day_of_week: String,
    val meal_type: String,
    val recipe_id: Int? = null,
    val meal_name: String,
    val meal_description: String? = null,
    val calories: Int,
    val time: String,
    val image_url: String? = null
) 