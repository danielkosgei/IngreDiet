package com.thenewkenya.ingrediet.feature.mealplanner

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.time.DayOfWeek

@Serializable
private data class MealPlanItemDto(
    val id: String,
    val name: String,
    val calories: Int,
    val day: String,
    val time: String,
    val description: String? = null,
    val recipeId: String? = null,
    val imageUrl: String? = null
)

object LocalMealPlanStore {
    private const val PREFS = "ingrediet_mealplans"
    private const val KEY = "plans"
    private val json = Json { ignoreUnknownKeys = true }

    private fun toDto(item: MealPlanItem): MealPlanItemDto = MealPlanItemDto(
        id = item.id,
        name = item.name,
        calories = item.calories,
        day = item.day.name,
        time = item.time.name,
        description = item.description,
        recipeId = item.recipeId,
        imageUrl = item.imageUrl
    )

    private fun fromDto(dto: MealPlanItemDto): MealPlanItem = MealPlanItem(
        id = dto.id,
        name = dto.name,
        calories = dto.calories,
        day = runCatching { DayOfWeek.valueOf(dto.day) }.getOrDefault(DayOfWeek.MONDAY),
        time = runCatching { MealTime.valueOf(dto.time) }.getOrDefault(MealTime.Breakfast),
        description = dto.description,
        recipeId = dto.recipeId,
        imageUrl = dto.imageUrl
    )

    fun save(context: Context, data: Map<DayOfWeek, List<MealPlanItem>>) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val mapped: Map<String, List<MealPlanItemDto>> = data.mapKeys { it.key.name }
            .mapValues { entry -> entry.value.map { toDto(it) } }
        val str = json.encodeToString(mapped)
        prefs.edit().putString(KEY, str).apply()
    }

    fun load(context: Context): Map<DayOfWeek, List<MealPlanItem>> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val str = prefs.getString(KEY, null) ?: return emptyMap()
        return try {
            val decoded: Map<String, List<MealPlanItemDto>> = json.decodeFromString(str)
            decoded.mapKeys { runCatching { DayOfWeek.valueOf(it.key) }.getOrDefault(DayOfWeek.MONDAY) }
                .mapValues { e -> e.value.map { fromDto(it) } }
        } catch (_: Exception) {
            emptyMap()
        }
    }
} 