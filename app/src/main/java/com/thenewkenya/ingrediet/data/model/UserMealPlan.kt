package com.thenewkenya.ingrediet.data.model

import java.time.DayOfWeek
import kotlinx.serialization.Serializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import com.thenewkenya.ingrediet.feature.mealplanner.MealPlanItem
import com.thenewkenya.ingrediet.feature.mealplanner.MealTime

/**
 * Custom serializer for IDs that could be either strings or integers
 */
object FlexibleIDSerializer : KSerializer<String> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("FlexibleID", PrimitiveKind.STRING)
    
    override fun serialize(encoder: Encoder, value: String) {
        encoder.encodeString(value)
    }
    
    override fun deserialize(decoder: Decoder): String {
        return when (decoder) {
            is JsonDecoder -> {
                val element = decoder.decodeJsonElement()
                when {
                    element is JsonPrimitive && element.isString -> element.content
                    element is JsonPrimitive -> element.toString()
                    else -> ""
                }
            }
            else -> decoder.decodeString()
        }
    }
}

/**
 * Represents a user's meal plan item stored in the database
 */
@Serializable
data class UserMealPlan(
    val id: String = "",
    val userId: String = "",
    val dayOfWeek: String = "", // Store as string representation of DayOfWeek enum
    val mealType: String = "", // Breakfast, Lunch, Dinner, etc.
    val recipeId: String? = null,
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
                recipeId = item.recipeId?.toString(),
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
    @Serializable(with = FlexibleIDSerializer::class)
    val recipe_id: String? = null,
    val meal_name: String,
    val meal_description: String? = null,
    val calories: Int,
    val time: String,
    val image_url: String? = null
) 