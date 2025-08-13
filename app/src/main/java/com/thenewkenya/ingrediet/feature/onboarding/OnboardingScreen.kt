package com.thenewkenya.ingrediet.feature.onboarding

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.thenewkenya.ingrediet.data.model.Profile
import com.thenewkenya.ingrediet.data.repository.ProfileRepository
import com.thenewkenya.ingrediet.data.network.supabase
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    navController: NavController
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val context = LocalContext.current
    val profileRepository = remember { ProfileRepository() }
    
    var currentStep by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(false) }
    
    // Profile data states
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var sex by remember { mutableStateOf("") }
    var activityLevel by remember { mutableStateOf("") }
    var dietaryPreferences by remember { mutableStateOf(emptyList<String>()) }
    var allergies by remember { mutableStateOf(emptyList<String>()) }
    var healthGoals by remember { mutableStateOf(emptyList<String>()) }
    var healthConditions by remember { mutableStateOf(emptyList<String>()) }
    var weightGoal by remember { mutableStateOf("") }
    
    val totalSteps = 7
    val canProceed = when (currentStep) {
        0 -> true // Welcome screen
        1 -> firstName.isNotBlank() && lastName.isNotBlank()
        2 -> true // Physical data is optional
        3 -> true // Activity level is optional
        4 -> true // Dietary preferences are optional
        5 -> true // Health info is optional
        6 -> true // Goals are optional
        else -> false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Welcome to IngreDiet",
                            style = typography.titleMedium
                        )
                        if (currentStep > 0) {
                            Text(
                                text = "Step ${currentStep} of ${totalSteps - 1}",
                                style = typography.bodySmall,
                                color = colors.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    if (currentStep > 0) {
                        IconButton(onClick = { currentStep-- }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            // Skip to home
                            navController.navigate("home") {
                                popUpTo("onboarding") { inclusive = true }
                            }
                        }
                    ) {
                        Text("Skip")
                    }
                }
            )
        },
        bottomBar = {
            if (currentStep > 0) {
                OnboardingBottomBar(
                    currentStep = currentStep,
                    totalSteps = totalSteps - 1,
                    canProceed = canProceed,
                    isLoading = isLoading,
                    onNext = {
                        if (currentStep < totalSteps - 1) {
                            currentStep++
                        } else {
                            // Complete onboarding
                            isLoading = true
                            // Save profile data and navigate to home
                            saveProfileAndComplete(
                                profileRepository = profileRepository,
                                firstName = firstName,
                                lastName = lastName,
                                age = age,
                                height = height,
                                weight = weight,
                                sex = sex,
                                activityLevel = activityLevel,
                                dietaryPreferences = dietaryPreferences,
                                allergies = allergies,
                                healthGoals = healthGoals,
                                healthConditions = healthConditions,
                                weightGoal = weightGoal,
                                onComplete = {
                                    isLoading = false
                                    navController.navigate("home") {
                                        popUpTo("onboarding") { inclusive = true }
                                    }
                                }
                            )
                        }
                    },
                    colors = colors
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Progress indicator
            if (currentStep > 0) {
                LinearProgressIndicator(
                    progress = { currentStep.toFloat() / (totalSteps - 1).toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    color = colors.primary,
                    trackColor = colors.primary.copy(alpha = 0.2f)
                )
            }
            
            // Content
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    slideInHorizontally { it } + fadeIn() togetherWith
                            slideOutHorizontally { -it } + fadeOut()
                },
                label = "onboarding_content"
            ) { step ->
                when (step) {
                    0 -> WelcomeStep(
                        onGetStarted = { currentStep = 1 },
                        colors = colors,
                        typography = typography
                    )
                    1 -> PersonalInfoStep(
                        firstName = firstName,
                        lastName = lastName,
                        onFirstNameChange = { firstName = it },
                        onLastNameChange = { lastName = it },
                        colors = colors,
                        typography = typography
                    )
                    2 -> PhysicalDataStep(
                        age = age,
                        height = height,
                        weight = weight,
                        sex = sex,
                        onAgeChange = { age = it },
                        onHeightChange = { height = it },
                        onWeightChange = { weight = it },
                        onSexChange = { sex = it },
                        colors = colors,
                        typography = typography
                    )
                    3 -> ActivityLevelStep(
                        selectedLevel = activityLevel,
                        onLevelSelected = { activityLevel = it },
                        colors = colors,
                        typography = typography
                    )
                    4 -> DietaryPreferencesStep(
                        selectedPreferences = dietaryPreferences,
                        selectedAllergies = allergies,
                        onPreferencesChange = { dietaryPreferences = it },
                        onAllergiesChange = { allergies = it },
                        colors = colors,
                        typography = typography
                    )
                    5 -> HealthInfoStep(
                        selectedConditions = healthConditions,
                        onConditionsChange = { healthConditions = it },
                        colors = colors,
                        typography = typography
                    )
                    6 -> GoalsStep(
                        selectedGoals = healthGoals,
                        selectedWeightGoal = weightGoal,
                        onGoalsChange = { healthGoals = it },
                        onWeightGoalChange = { weightGoal = it },
                        colors = colors,
                        typography = typography
                    )
                }
            }
        }
    }
}

@Composable
private fun OnboardingBottomBar(
    currentStep: Int,
    totalSteps: Int,
    canProceed: Boolean,
    isLoading: Boolean,
    onNext: () -> Unit,
    colors: androidx.compose.material3.ColorScheme
) {
    Surface(
        color = colors.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = onNext,
                enabled = canProceed && !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = colors.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    if (currentStep < totalSteps) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Next",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Next")
                    } else {
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = "Complete",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Complete")
                    }
                }
            }
        }
    }
}

private fun saveProfileAndComplete(
    profileRepository: ProfileRepository,
    firstName: String,
    lastName: String,
    age: String,
    height: String,
    weight: String,
    sex: String,
    activityLevel: String,
    dietaryPreferences: List<String>,
    allergies: List<String>,
    healthGoals: List<String>,
    healthConditions: List<String>,
    weightGoal: String,
    onComplete: () -> Unit
) {
    // Save to repository using proper flow
    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
        try {
            // First get the existing profile to preserve id and email
            profileRepository.getProfile().collect { getResult ->
                getResult.fold(
                    onSuccess = { existingProfile ->
                        // Update the existing profile with onboarding data
                        val updatedProfile = existingProfile.copy(
                            firstName = firstName,
                            lastName = lastName,
                            age = age.toIntOrNull(),
                            height = height.toFloatOrNull(),
                            weight = weight.toFloatOrNull(),
                            sex = sex,
                            activityLevel = activityLevel,
                            dietaryPreferences = dietaryPreferences,
                            allergies = allergies,
                            healthGoals = healthGoals,
                            healthConditions = healthConditions,
                            weightGoal = weightGoal,
                            isOnboardingCompleted = true
                        )
                        
                        android.util.Log.d("OnboardingScreen", "Updating profile: $updatedProfile")
                        
                        // Now update with the complete profile
                        profileRepository.updateProfile(updatedProfile).collect { updateResult ->
                            updateResult.fold(
                                onSuccess = {
                                    android.util.Log.d("OnboardingScreen", "Profile saved successfully")
                                    onComplete()
                                },
                                onFailure = { error ->
                                    android.util.Log.e("OnboardingScreen", "Error updating profile: ${error.message}")
                                    // Complete anyway to avoid blocking user
                                    onComplete()
                                }
                            )
                        }
                    },
                    onFailure = { error ->
                        android.util.Log.e("OnboardingScreen", "Error getting existing profile: ${error.message}")
                        // If we can't get the profile, try to create one with available user data
                        val currentUser = supabase.auth.currentUserOrNull()
                        val userEmail = currentUser?.email ?: ""
                        
                        // Create profile with available data
                        profileRepository.createProfile(userEmail).collect { createResult ->
                            createResult.fold(
                                onSuccess = {
                                    android.util.Log.d("OnboardingScreen", "Profile created, now updating...")
                                    // Retry the save after creating profile
                                    saveProfileAndComplete(
                                        profileRepository, firstName, lastName, age, height, weight,
                                        sex, activityLevel, dietaryPreferences, allergies, 
                                        healthGoals, healthConditions, weightGoal, onComplete
                                    )
                                },
                                onFailure = { createError ->
                                    android.util.Log.e("OnboardingScreen", "Error creating profile: ${createError.message}")
                                    onComplete()
                                }
                            )
                        }
                    }
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("OnboardingScreen", "Unexpected error: ${e.message}")
            onComplete()
        }
    }
} 