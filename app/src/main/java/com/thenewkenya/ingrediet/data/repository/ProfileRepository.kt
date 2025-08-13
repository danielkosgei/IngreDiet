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
                Log.e("ProfileRepository", "No authenticated user found")
                emit(Result.failure(Exception("User not authenticated")))
                return@flow
            }

            Log.d("ProfileRepository", "Fetching profile for user: $userId")

            // Query profiles table for current user's profile
            val response = supabase.from("profiles")
                .select {
                    filter { eq("id", userId) }
                }
                .decodeSingleOrNull<ProfileDto>()

            if (response == null) {
                Log.w("ProfileRepository", "No profile found for user: $userId, creating one...")
                // Profile doesn't exist, create one
                val auth = supabase.auth.currentUserOrNull()
                val userEmail = auth?.email ?: ""
                
                createProfile(userEmail).collect { createResult ->
                    createResult.fold(
                        onSuccess = {
                            // Retry fetching after creation
                            getProfile().collect { retryResult ->
                                emit(retryResult)
                            }
                        },
                        onFailure = { createError ->
                            Log.e("ProfileRepository", "Failed to create profile: ${createError.message}")
                            emit(Result.failure(createError))
                        }
                    )
                }
                return@flow
            }

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
                profileImageUrl = response.profile_image_url ?: "",
                
                // Health fields
                age = response.age,
                height = response.height,
                weight = response.weight,
                sex = response.sex ?: "",
                activityLevel = response.activity_level ?: "",
                healthGoals = response.health_goals ?: emptyList(),
                healthConditions = response.health_conditions ?: emptyList(),
                isOnboardingCompleted = response.is_onboarding_completed ?: false
            )

            Log.d("ProfileRepository", "Profile loaded successfully: firstName=${profile.firstName}, lastName=${profile.lastName}, onboarding=${profile.isOnboardingCompleted}")

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
                        
                        // Health fields
                        set("age", profile.age)
                        set("height", profile.height)
                        set("weight", profile.weight)
                        set("sex", profile.sex)
                        set("activity_level", profile.activityLevel)
                        set("health_goals", profile.healthGoals)
                        set("health_conditions", profile.healthConditions)
                        set("is_onboarding_completed", profile.isOnboardingCompleted)
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

    suspend fun createProfile(email: String): Flow<Result<Boolean>> = flow {
        try {
            val userId = supabase.auth.currentUserOrNull()?.id ?: run {
                emit(Result.failure(Exception("User not authenticated")))
                return@flow
            }

            // Check if profile already exists
            val existingProfile = try {
                supabase.from("profiles")
                    .select {
                        filter { eq("id", userId) }
                    }
                    .decodeSingleOrNull<ProfileDto>()
            } catch (e: Exception) {
                null
            }

            if (existingProfile == null) {
                // Create new profile with default values
                val profileDto = ProfileDto(
                    id = userId,
                    email = email,
                    first_name = "",
                    last_name = "",
                    dietary_preferences = emptyList(),
                    allergies = emptyList(),
                    weight_goal = "",
                    calorie_target = 0,
                    profile_image_url = "",
                    age = null,
                    height = null,
                    weight = null,
                    sex = "",
                    activity_level = "",
                    health_goals = emptyList(),
                    health_conditions = emptyList(),
                    is_onboarding_completed = false
                )

                supabase.from("profiles")
                    .insert(profileDto)

                Log.d("ProfileRepository", "Profile created for user: $userId")
            }

            emit(Result.success(true))
        } catch (e: Exception) {
            Log.e("ProfileRepository", "Error creating profile", e)
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
        val profile_image_url: String? = null,
        
        // Health fields
        val age: Int? = null,
        val height: Float? = null,
        val weight: Float? = null,
        val sex: String? = null,
        val activity_level: String? = null,
        val health_goals: List<String>? = null,
        val health_conditions: List<String>? = null,
        val is_onboarding_completed: Boolean? = null
    )
}