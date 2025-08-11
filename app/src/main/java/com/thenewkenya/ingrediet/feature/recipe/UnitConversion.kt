package com.thenewkenya.ingrediet.feature.recipe

import kotlin.math.abs

object UnitConversion {
    private val pieceWeights = mapOf(
        // Common approximations; adjust as needed
        "onion" to 110f,
        "tomato" to 120f,
        "garlic" to 5f, // clove ~5g
        "egg" to 50f,
        "banana" to 120f,
        "carrot" to 60f,
        "potato" to 170f
    )

    fun toGrams(quantity: Float, unit: String?, ingredientName: String): Float {
        if (quantity <= 0f) return 0f
        val u = (unit ?: "").trim().lowercase()
        val name = ingredientName.lowercase()

        return when (u) {
            "g" -> quantity
            "kg" -> quantity * 1000f
            "mg" -> quantity / 1000f
            "lb" -> quantity * 453.592f
            "oz" -> quantity * 28.3495f
            "ml" -> quantity // assume water-like density
            "l" -> quantity * 1000f
            "tsp" -> quantity * 5f
            "tbsp" -> quantity * 15f
            "cup" -> quantity * 240f
            "slice" -> pieceWeightsFor(name, quantity, 25f)
            "clove" -> pieceWeightsFor(name, quantity, 5f)
            "piece", "pc", "pcs", "unit" -> pieceWeightsFor(name, quantity, 50f)
            else -> {
                // Handle composite like "250g" or "500 ml"
                val m = Regex("^(-?\\d+(?:\\.\\d+)?)\\s*([a-zA-Z]+)").find(u)
                if (m != null) {
                    val q = m.groupValues[1].toFloatOrNull() ?: return 0f
                    val subUnit = m.groupValues[2]
                    // quantity refers to count of packs; multiply
                    return quantity * toGrams(q, subUnit, ingredientName)
                }
                // Fallback: assume piece
                pieceWeightsFor(name, quantity, 50f)
            }
        }
    }

    private fun pieceWeightsFor(name: String, quantity: Float, defaultPerPiece: Float): Float {
        val key = pieceWeights.keys.firstOrNull { name.contains(it) }
        val per = key?.let { pieceWeights[it] } ?: defaultPerPiece
        return quantity * per
    }
} 