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
    val profileImageUrl: String = ""
)