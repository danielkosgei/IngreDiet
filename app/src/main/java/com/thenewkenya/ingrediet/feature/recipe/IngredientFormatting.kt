package com.thenewkenya.ingrediet.feature.recipe

import kotlin.math.abs

/**
 * Formats ingredient quantity and unit for display.
 * - Removes quotes from unit
 * - Treats numeric-only units as empty
 * - Normalizes common unit names
 * - Handles singular/plural for select units
 */
fun formatQuantityUnit(quantity: Float, rawUnit: String?): String {
    val unit = normalizeUnit(rawUnit)
    val qtyText = formatQuantityValue(quantity)

    if (unit.isEmpty()) return qtyText

    // If unit begins with a number (e.g., "250g", "500 ml"), keep it as-is and
    // do not prepend the quantity when quantity == 1 to avoid "1 250g"
    val numericLeading = unit.matches(Regex("^[-+]?\\d+(\\.\\d+)?\\s*[a-zA-Z]+.*$"))
    if (numericLeading) {
        return if (abs(quantity - 1f) < 0.0001f) unit else "${formatQuantityValue(quantity)} × $unit"
    }

    val finalUnit = pluralizeUnit(unit, quantity)
    return "$qtyText $finalUnit"
}

/**
 * Formats a complete ingredient phrase like "250g of onions".
 * - If unit starts with a number and quantity == 1 -> "250g of X"
 * - If unit starts with a number and quantity != 1 -> "2 × 250g of X"
 * - Otherwise -> "<qty unit> of X"
 */
fun formatIngredientPhrase(quantity: Float, rawUnit: String?, name: String): String {
    val unit = normalizeUnit(rawUnit)
    val qtyText = formatQuantityValue(quantity)

    if (unit.isEmpty()) return "$qtyText ${if (abs(quantity - 1f) < 0.0001f) "piece" else "pieces"} of $name"

    val numericLeading = unit.matches(Regex("^[-+]?\\d+(\\.\\d+)?\\s*[a-zA-Z]+.*$"))
    return if (numericLeading) {
        if (abs(quantity - 1f) < 0.0001f) "$unit of $name" else "${formatQuantityValue(quantity)} × $unit of $name"
    } else {
        val finalUnit = pluralizeUnit(unit, quantity)
        "$qtyText $finalUnit of $name"
    }
}

private fun formatQuantityValue(quantity: Float): String {
    val intPart = quantity.toInt()
    return if (quantity == intPart.toFloat()) intPart.toString() else {
        // Keep up to 2 decimals, trim trailing zeros
        val text = String.format("%.2f", quantity)
        text.trimEnd('0').trimEnd('.')
    }
}

private fun normalizeUnit(raw: String?): String {
    if (raw == null) return ""
    // Remove quotes and trim
    var u = raw.replace("\"", "").replace("'", "").trim()
    if (u.isEmpty()) return ""

    // If unit is numeric, treat as empty (bad data like unit="1")
    if (u.matches(Regex("^-?\\d+(\\.\\d+)?$"))) return ""

    u = u.lowercase()

    // Canonicalize common units
    val map = mapOf(
        "teaspoon" to "tsp",
        "teaspoons" to "tsp",
        "tsp" to "tsp",
        "tablespoon" to "tbsp",
        "tablespoons" to "tbsp",
        "tbsp" to "tbsp",
        "cup" to "cup",
        "cups" to "cup",
        "gram" to "g",
        "grams" to "g",
        "g" to "g",
        "kilogram" to "kg",
        "kilograms" to "kg",
        "kg" to "kg",
        "milliliter" to "ml",
        "milliliters" to "ml",
        "ml" to "ml",
        "liter" to "l",
        "liters" to "l",
        "l" to "l",
        "ounce" to "oz",
        "ounces" to "oz",
        "oz" to "oz",
        "pound" to "lb",
        "pounds" to "lb",
        "lb" to "lb",
        "slice" to "slice",
        "slices" to "slice",
        "clove" to "clove",
        "cloves" to "clove",
        "piece" to "piece",
        "pieces" to "piece",
        "pcs" to "piece",
        "pc" to "piece"
    )

    return map[u] ?: u
}

private fun pluralizeUnit(unit: String, quantity: Float): String {
    val isOne = abs(quantity - 1f) < 0.0001f

    // Units that don't pluralize
    val invariant = setOf("g", "kg", "ml", "l", "tsp", "tbsp", "oz", "lb")
    if (unit in invariant) return unit

    return when (unit) {
        "cup" -> if (isOne) "cup" else "cups"
        "slice" -> if (isOne) "slice" else "slices"
        "clove" -> if (isOne) "clove" else "cloves"
        "piece" -> if (isOne) "piece" else "pieces"
        else -> unit
    }
} 