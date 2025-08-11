package com.thenewkenya.ingrediet.data.model

import kotlinx.serialization.Serializable

@Serializable
data class IngredientNutrition(
    val nameNormalized: String,
    val per100g: NutritionFacts,
    val imageUrl: String? = null
) 