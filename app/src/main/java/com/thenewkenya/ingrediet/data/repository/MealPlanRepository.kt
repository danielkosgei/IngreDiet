package com.thenewkenya.ingrediet.data.repository

import android.util.Log
import com.thenewkenya.ingrediet.data.model.UserMealPlan
import com.thenewkenya.ingrediet.data.model.UserMealPlanDto
import com.thenewkenya.ingrediet.data.network.supabase
import com.thenewkenya.ingrediet.feature.mealplanner.MealPlanItem
import com.thenewkenya.ingrediet.feature.mealplanner.MealTime
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.DayOfWeek

class MealPlanRepository {
    
    /**
     * Get a user's meal plans for all days of the week
     */
    suspend fun getUserMealPlans(): Flow<Result<Map<DayOfWeek, List<MealPlanItem>>>> = flow {
        try {
            val userId = supabase.auth.currentUserOrNull()?.id ?: run {
                emit(Result.failure(Exception("User not authenticated")))
                return@flow
            }
            
            // Retrieve meal plans from the database
            val userMealPlans = supabase.from("user_meal_plans")
                .select {
                    filter { eq("user_id", userId) }
                }
                .decodeList<UserMealPlanDto>()
            
            // Map to our UI model
            val mealPlansByDay = userMealPlans
                .map { dto ->
                    MealPlanItem(
                        id = dto.id ?: "",
                        name = dto.meal_type,
                        calories = dto.calories,
                        day = DayOfWeek.valueOf(dto.day_of_week),
                        time = getMealTimeFromString(dto.time),
                        description = dto.meal_description,
                        recipeId = dto.recipe_id,
                        imageUrl = dto.image_url
                    )
                }
                .groupBy { it.day }
            
            // Ensure all days are represented in the map
            val completePlan = DayOfWeek.values().associateWith { day ->
                mealPlansByDay[day] ?: emptyList()
            }
            
            emit(Result.success(completePlan))
        } catch (e: Exception) {
            Log.e("MealPlanRepository", "Error fetching user meal plans", e)
            emit(Result.failure(e))
        }
    }
    
    /**
     * Save a user's meal plans to the database
     */
    suspend fun saveUserMealPlans(mealPlans: Map<DayOfWeek, List<MealPlanItem>>): Flow<Result<Boolean>> = flow {
        try {
            val userId = supabase.auth.currentUserOrNull()?.id ?: run {
                emit(Result.failure(Exception("User not authenticated")))
                return@flow
            }
            
            // First delete existing meal plans for this user
            supabase.from("user_meal_plans")
                .delete {
                    filter { eq("user_id", userId) }
                }
                
            // Create DTOs for inserting
            val mealPlanDtos = mutableListOf<UserMealPlanDto>()
            
            mealPlans.forEach { (day, meals) ->
                meals.forEach { meal ->
                    mealPlanDtos.add(
                        UserMealPlanDto(
                            user_id = userId,
                            day_of_week = day.name,
                            meal_type = meal.name,
                            recipe_id = meal.recipeId,
                            meal_name = meal.name,
                            meal_description = meal.description,
                            calories = meal.calories,
                            time = meal.time.toString(),
                            image_url = meal.imageUrl
                        )
                    )
                }
            }
            
            // Insert meal plans in batches to avoid potential size limits
            val batchSize = 20
            mealPlanDtos.chunked(batchSize).forEach { batch ->
                supabase.from("user_meal_plans")
                    .insert(batch)
            }
            
            emit(Result.success(true))
        } catch (e: Exception) {
            Log.e("MealPlanRepository", "Error saving user meal plans", e)
            emit(Result.failure(e))
        }
    }
    
    /**
     * Check if the user has any meal plans
     */
    suspend fun hasMealPlans(): Flow<Result<Boolean>> = flow {
        val result = try {
            val userId = supabase.auth.currentUserOrNull()?.id
            if (userId == null) {
                Result.failure(Exception("User not authenticated"))
            } else {
                // Use select and count the results manually
                val plans = supabase.from("user_meal_plans")
                    .select {
                        filter { eq("user_id", userId) }
                    }
                    .decodeList<UserMealPlanDto>()
                    
                Result.success(plans.isNotEmpty())
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException || 
                e.message?.contains("composition") == true || 
                e.cause is kotlinx.coroutines.CancellationException) {
                Log.d("MealPlanRepository", "Operation cancelled normally")
                // Don't emit for cancellation, just return
                return@flow
            }
            
            Log.e("MealPlanRepository", "Error checking if user has meal plans", e)
            Result.failure(e)
        }
        
        // Emit only once at the end, outside of try/catch
        emit(result)
    }
    
    /**
     * Convert string to MealTime enum
     */
    private fun getMealTimeFromString(timeString: String): MealTime {
        return when (timeString) {
            "Breakfast" -> MealTime.Breakfast
            "Lunch" -> MealTime.Lunch 
            "Dinner" -> MealTime.Dinner
            else -> MealTime.Snacks
        }
    }
} 