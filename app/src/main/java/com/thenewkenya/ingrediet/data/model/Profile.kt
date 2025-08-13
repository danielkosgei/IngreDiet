package com.thenewkenya.ingrediet.data.model

data class Profile(
    val id: String = "",
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val dietaryPreferences: List<String> = emptyList(),
    val allergies: List<String> = emptyList(),
    val weightGoal: String = "", // e.g "maintain", "lose", "gain"
    val calorieTarget: Int = 0,
    val profileImageUrl: String = "",
    
    // Health & Physical Data
    val age: Int? = null,
    val height: Float? = null, // in cm
    val weight: Float? = null, // in kg
    val sex: String = "", // "male", "female", "other", ""
    val activityLevel: String = "", // "sedentary", "light", "moderate", "active", "very_active"
    
    // Health Goals & Conditions
    val healthGoals: List<String> = emptyList(), // e.g., "weight_loss", "muscle_gain", "heart_health"
    val healthConditions: List<String> = emptyList(), // e.g., "diabetes", "hypertension", "none"
    
    // Onboarding Status
    val isOnboardingCompleted: Boolean = false
) {
    /**
     * Calculate BMI if height and weight are available
     * BMI = weight (kg) / (height (m))^2
     */
    val bmi: Float?
        get() = if (height != null && weight != null && height > 0) {
            weight / ((height / 100) * (height / 100))
        } else null
    
    /**
     * Get BMI category based on calculated BMI
     */
    val bmiCategory: String
        get() {
            val bmiValue = bmi
            return when {
                bmiValue == null -> "Unknown"
                bmiValue < 18.5 -> "Underweight"
                bmiValue < 25.0 -> "Normal weight"
                bmiValue < 30.0 -> "Overweight"
                else -> "Obese"
            }
        }
    
    /**
     * Calculate daily calorie needs using Mifflin-St Jeor Equation
     */
    val estimatedDailyCaloricNeeds: Int?
        get() {
            if (age == null || height == null || weight == null || sex.isEmpty()) return null
            
            // BMR calculation
            val bmr = when (sex.lowercase()) {
                "male" -> (10 * weight) + (6.25 * height) - (5 * age) + 5
                "female" -> (10 * weight) + (6.25 * height) - (5 * age) - 161
                else -> null
            } ?: return null
            
            // Activity multiplier
            val activityMultiplier = when (activityLevel) {
                "sedentary" -> 1.2f
                "light" -> 1.375f
                "moderate" -> 1.55f
                "active" -> 1.725f
                "very_active" -> 1.9f
                else -> 1.2f // default to sedentary
            }
            
            return (bmr * activityMultiplier).toInt()
        }
}