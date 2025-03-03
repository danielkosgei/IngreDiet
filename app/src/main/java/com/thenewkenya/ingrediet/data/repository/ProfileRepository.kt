package com.thenewkenya.ingrediet.data.repository

import android.util.Log
import com.thenewkenya.ingrediet.data.model.Profile
import com.thenewkenya.ingrediet.data.network.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable

class ProfileRepository {
    suspend fun getProfile(): Flow<Result<Profile>> = flow {
        try {
            val userId = supabase.auth.currentUserOrNull()?.id ?: run {
                emit(Result.failure(Exception("User not authenticated")))
                return@flow
            }

            // Query profiles table for current user's profile
            val response = supabase.from("profiles")
                .select {
                    filter { eq("id", userId) }
                }
                .decodeSingle<ProfileDto>()

            // Convert DTO to domain model
            val profile = Profile(
                id = response.id,
                email = response.email ?: "",
                firstName = response.first_name ?: "",
                lastName = response.last_name ?: "",
                dietaryPreferences = response.dietary_preferences ?: emptyList(),
                allergies = response.allergies ?: emptyList(),
                weightGoal = response.weight_goal ?: "",
                calorieTarget = response.calorie_target ?: 0,
                profileImageUrl = response.profile_image_url ?: ""
            )

            emit(Result.success(profile))
        } catch (e: Exception) {
            Log.e("ProfileRepository", "Error fetching profile", e)
            emit(Result.failure(e))
        }
    }

    suspend fun updateProfile(profile: Profile): Flow<Result<Boolean>> = flow {
        try {
            val userId = supabase.auth.currentUserOrNull()?.id ?: run {
                emit(Result.failure(Exception("User not authenticated")))
                return@flow
            }

            // Update the profile in Supabase
            supabase.from("profiles")
                .update(
                    {
                        // Specify all fields to update
                        set("first_name", profile.firstName)
                        set("last_name", profile.lastName)
                        set("dietary_preferences", profile.dietaryPreferences)
                        set("allergies", profile.allergies)
                        set("weight_goal", profile.weightGoal)
                        set("calorie_target", profile.calorieTarget)
                        set("profile_image_url", profile.profileImageUrl)
                    }
                ) {
                    filter { eq("id", userId) }
                }

            emit(Result.success(true))
        } catch (e: Exception) {
            Log.e("ProfileRepository", "Error updating profile", e)
            emit(Result.failure(e))
        }
    }

    // DTO class for Supabase mapping - properly annotated for serialization
    @Serializable
    private data class ProfileDto(
        val id: String,
        val email: String? = null,
        val first_name: String? = null,
        val last_name: String? = null,
        val dietary_preferences: List<String>? = null,
        val allergies: List<String>? = null,
        val weight_goal: String? = null,
        val calorie_target: Int? = null,
        val profile_image_url: String? = null
    )
}