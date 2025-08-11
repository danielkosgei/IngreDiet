package com.thenewkenya.ingrediet.feature.recipe

import com.thenewkenya.ingrediet.data.model.NutritionFacts

object NutritionMath {
    fun totalForWeight(per100g: NutritionFacts, grams: Float): NutritionFacts {
        val factor = (grams / 100f).coerceAtLeast(0f)
        return NutritionFacts(
            calories = (per100g.calories * factor).toInt(),
            protein = per100g.protein * factor,
            carbs = per100g.carbs * factor,
            fat = per100g.fat * factor,
            fiber = per100g.fiber?.let { it * factor },
            sugar = per100g.sugar?.let { it * factor },
            sodium = per100g.sodium?.let { it * factor },
            cholesterol = per100g.cholesterol,
            vitamins = per100g.vitamins,
            minerals = per100g.minerals,
            dailyValuePercentage = per100g.dailyValuePercentage
        )
    }
} 