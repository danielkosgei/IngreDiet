package com.thenewkenya.ingrediet.feature.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.thenewkenya.ingrediet.data.network.AuthManager
import com.thenewkenya.ingrediet.data.repository.ProfileRepository
import kotlin.math.min
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionGoalsScreen(
    navController: NavController,
    isOnboarding: Boolean = false // Parameter to determine if we're in onboarding mode
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    
    // Initialize ViewModel
    val viewModel = remember { 
        ProfileViewModel(
            ProfileRepository(),
            AuthManager(context)
        ) 
    }
    val profile by viewModel.profile.collectAsState()
    
    // Load user's nutrition goals from profile
    var calorieTarget by remember { mutableStateOf((profile?.calorieTarget ?: 2000).toString()) }
    var weightGoal by remember { mutableStateOf(profile?.weightGoal ?: "Maintain weight") }
    
    // Values for macro sliders (protein/carbs/fat ratio)
    var proteinRatio by remember { mutableFloatStateOf(0.3f) }
    var carbsRatio by remember { mutableFloatStateOf(0.5f) }
    var fatRatio by remember { mutableFloatStateOf(0.2f) }
    
    // For step-based UI
    var currentStep by remember { mutableIntStateOf(0) }
    val totalSteps = 3
    
    // Weight goal options with icons
    val weightGoalOptions = listOf(
        Pair("Lose weight", Icons.Default.ArrowDownward),
        Pair("Maintain weight", Icons.Default.Scale),
        Pair("Gain weight", Icons.Default.ArrowUpward)
    )
    
    LaunchedEffect(profile) {
        profile?.let {
            calorieTarget = (it.calorieTarget ?: 2000).toString()
            weightGoal = it.weightGoal ?: "Maintain weight"
        }
    }
    
    val accentGreen = Color(0xFF4CAF50) // Define a green color similar to the image
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (isOnboarding) "" else "Nutrition Goals",
                        style = typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { 
                        if (isOnboarding && currentStep > 0) {
                            currentStep--
                        } else {
                            navController.navigateUp()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (isOnboarding) {
                        // Page indicator for onboarding
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 16.dp)
                        ) {
                            for (i in 0 until totalSteps) {
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp)
                                        .size(8.dp)
                                        .background(
                                            if (i <= currentStep) accentGreen 
                                            else colors.surfaceVariant,
                                            CircleShape
                                        )
                                )
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (!isOnboarding) {
                // Only show FAB in viewing/editing mode
                FloatingActionButton(
                    onClick = {
                        // Save changes
                        profile?.let {
                            val updatedProfile = it.copy(
                                calorieTarget = calorieTarget.toIntOrNull() ?: 2000,
                                weightGoal = weightGoal
                            )
                            viewModel.updateProfile(updatedProfile)
                            navController.navigateUp()
                        }
                    },
                    shape = CircleShape,
                    containerColor = accentGreen
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Save",
                        tint = Color.White
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(colors.background.copy(alpha = 0.95f))
        ) {
            if (isOnboarding) {
                // Onboarding mode - step by step UI
                OnboardingContent(
                    currentStep = currentStep,
                    calorieTarget = calorieTarget,
                    weightGoal = weightGoal,
                    weightGoalOptions = weightGoalOptions,
                    proteinRatio = proteinRatio,
                    carbsRatio = carbsRatio,
                    fatRatio = fatRatio,
                    onCalorieChange = { calorieTarget = it },
                    onWeightGoalChange = { weightGoal = it },
                    onProteinChange = {
                        proteinRatio = it
                        val remaining = 1f - it
                        val ratio = carbsRatio / (carbsRatio + fatRatio)
                        carbsRatio = remaining * ratio
                        fatRatio = remaining * (1 - ratio)
                    },
                    onCarbsChange = {
                        carbsRatio = it
                        val remaining = 1f - it
                        val ratio = proteinRatio / (proteinRatio + fatRatio)
                        proteinRatio = remaining * ratio
                        fatRatio = remaining * (1 - ratio)
                    },
                    onFatChange = {
                        fatRatio = it
                        val remaining = 1f - it
                        val ratio = proteinRatio / (proteinRatio + carbsRatio)
                        proteinRatio = remaining * ratio
                        carbsRatio = remaining * (1 - ratio)
                    },
                    onNextStep = {
                        if (currentStep < totalSteps - 1) {
                            currentStep++
                        } else {
                            // Save and navigate back
                            profile?.let {
                                val updatedProfile = it.copy(
                                    calorieTarget = calorieTarget.toIntOrNull() ?: 2000,
                                    weightGoal = weightGoal
                                )
                                viewModel.updateProfile(updatedProfile)
                                navController.navigateUp()
                            }
                        }
                    }
                )
            } else {
                // View/Edit mode - consolidated UI
                ConsolidatedView(
                    calorieTarget = calorieTarget,
                    weightGoal = weightGoal,
                    weightGoalOptions = weightGoalOptions,
                    proteinRatio = proteinRatio,
                    carbsRatio = carbsRatio,
                    fatRatio = fatRatio,
                    onCalorieChange = { calorieTarget = it },
                    onWeightGoalChange = { weightGoal = it },
                    onProteinChange = {
                        proteinRatio = it
                        val remaining = 1f - it
                        val ratio = carbsRatio / (carbsRatio + fatRatio)
                        carbsRatio = remaining * ratio
                        fatRatio = remaining * (1 - ratio)
                    },
                    onCarbsChange = {
                        carbsRatio = it
                        val remaining = 1f - it
                        val ratio = proteinRatio / (proteinRatio + fatRatio)
                        proteinRatio = remaining * ratio
                        fatRatio = remaining * (1 - ratio)
                    },
                    onFatChange = {
                        fatRatio = it
                        val remaining = 1f - it
                        val ratio = proteinRatio / (proteinRatio + carbsRatio)
                        proteinRatio = remaining * ratio
                        carbsRatio = remaining * (1 - ratio)
                    }
                )
            }
        }
    }
}

@Composable
fun OnboardingContent(
    currentStep: Int,
    calorieTarget: String,
    weightGoal: String,
    weightGoalOptions: List<Pair<String, ImageVector>>,
    proteinRatio: Float,
    carbsRatio: Float,
    fatRatio: Float,
    onCalorieChange: (String) -> Unit,
    onWeightGoalChange: (String) -> Unit,
    onProteinChange: (Float) -> Unit,
    onCarbsChange: (Float) -> Unit,
    onFatChange: (Float) -> Unit,
    onNextStep: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val scrollState = rememberScrollState()
    val accentGreen = Color(0xFF4CAF50)
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        // Step 1: Calorie Target
        AnimatedVisibility(
            visible = currentStep == 0,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            NutritionGoalStep(
                title = "What is your daily calorie target?",
                subtitle = "We'll use this to calculate your nutrition goals"
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(32.dp),
                    modifier = Modifier.padding(top = 32.dp)
                ) {
                    // Calorie display with green meter
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Background circle
                        CircularProgress(
                            percentage = min((calorieTarget.toIntOrNull() ?: 2000).toFloat() / 3000f, 1f),
                            color = accentGreen,
                            backgroundColor = accentGreen.copy(alpha = 0.1f)
                        )
                        
                        // Text in center
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = calorieTarget,
                                style = typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = accentGreen
                            )
                            Text(
                                text = "kcal",
                                style = typography.bodyMedium,
                                color = colors.onSurfaceVariant
                            )
                        }
                    }
                    
                    // Calorie input
                    OutlinedTextField(
                        value = calorieTarget,
                        onValueChange = { input ->
                            if (input.isEmpty() || input.all { it.isDigit() }) {
                                onCalorieChange(input)
                            }
                        },
                        label = { Text("Calories per day") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .padding(vertical = 8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentGreen,
                            focusedLabelColor = accentGreen,
                            cursorColor = accentGreen
                        )
                    )
                    
                    Text(
                        text = "Recommended: 1500-2500 calories for most adults",
                        style = typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }
            }
        }
        
        // Step 2: Weight Goal
        AnimatedVisibility(
            visible = currentStep == 1,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            NutritionGoalStep(
                title = "What is your weight goal?",
                subtitle = "This helps us tailor your nutrition recommendations"
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(top = 32.dp, bottom = 16.dp)
                ) {
                    // Weight goal options in cards
                    weightGoalOptions.forEach { (option, icon) ->
                        val isSelected = option == weightGoal
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onWeightGoalChange(option) }
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) accentGreen.copy(alpha = 0.1f) else colors.surface
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = if (isSelected) 2.dp else 0.dp
                            ),
                            border = if (isSelected) {
                                androidx.compose.foundation.BorderStroke(
                                    width = 1.dp,
                                    color = accentGreen
                                )
                            } else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) accentGreen else colors.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                                
                                Spacer(modifier = Modifier.width(16.dp))
                                
                                Text(
                                    text = option,
                                    style = typography.bodyLarge,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) accentGreen else colors.onSurface
                                )
                                
                                Spacer(modifier = Modifier.weight(1f))
                                
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Selected",
                                        tint = accentGreen
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Step 3: Macronutrient Distribution
        AnimatedVisibility(
            visible = currentStep == 2,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            NutritionGoalStep(
                title = "Set your macronutrient balance",
                subtitle = "Adjust the sliders to set your preferred ratio"
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier.padding(vertical = 32.dp)
                ) {
                    // Macronutrient distribution
                    SimplifiedMacronutrientChart(
                        proteinPercentage = proteinRatio,
                        carbsPercentage = carbsRatio,
                        fatPercentage = fatRatio
                    )
                    
                    // Macronutrient sliders
                    Column(
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .padding(top = 16.dp)
                    ) {
                        // Protein slider
                        MacroSlider(
                            label = "Protein",
                            value = proteinRatio,
                            color = accentGreen,
                            onValueChange = onProteinChange
                        )
                        
                        // Carbs slider
                        MacroSlider(
                            label = "Carbs",
                            value = carbsRatio,
                            color = accentGreen,
                            onValueChange = onCarbsChange
                        )
                        
                        // Fat slider
                        MacroSlider(
                            label = "Fat",
                            value = fatRatio,
                            color = accentGreen,
                            onValueChange = onFatChange
                        )
                    }
                    
                    // Calculate grams based on calories
                    if (calorieTarget.toIntOrNull() != null) {
                        val calories = calorieTarget.toInt()
                        val proteinGrams = (calories * proteinRatio / 4).roundToInt()
                        val carbsGrams = (calories * carbsRatio / 4).roundToInt()
                        val fatGrams = (calories * fatRatio / 9).roundToInt()
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            MacroGrams("Protein", proteinGrams)
                            MacroGrams("Carbs", carbsGrams)
                            MacroGrams("Fat", fatGrams)
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Navigation button
        Button(
            onClick = onNextStep,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = accentGreen
            )
        ) {
            Text(
                text = if (currentStep < 2) "Continue" else "Save",
                style = typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
            )
            
            if (currentStep < 2) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun ConsolidatedView(
    calorieTarget: String,
    weightGoal: String,
    weightGoalOptions: List<Pair<String, ImageVector>>,
    proteinRatio: Float,
    carbsRatio: Float,
    fatRatio: Float,
    onCalorieChange: (String) -> Unit,
    onWeightGoalChange: (String) -> Unit,
    onProteinChange: (Float) -> Unit,
    onCarbsChange: (Float) -> Unit,
    onFatChange: (Float) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val scrollState = rememberScrollState()
    val accentGreen = Color(0xFF4CAF50)
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Summary Card at the top
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = accentGreen.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Daily calorie target
                Text(
                    text = "Daily Calorie Target",
                    style = typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = accentGreen
                )
                
                Text(
                    text = "$calorieTarget kcal",
                    style = typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                    color = accentGreen
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Weight goal
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val icon = weightGoalOptions.first { it.first == weightGoal }.second
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = weightGoal,
                        style = typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onSurface
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Macronutrient distribution
                if (calorieTarget.toIntOrNull() != null) {
                    val calories = calorieTarget.toInt()
                    val proteinGrams = (calories * proteinRatio / 4).roundToInt()
                    val carbsGrams = (calories * carbsRatio / 4).roundToInt()
                    val fatGrams = (calories * fatRatio / 9).roundToInt()
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        MacroSummary("Protein", proteinRatio, proteinGrams)
                        MacroSummary("Carbs", carbsRatio, carbsGrams)
                        MacroSummary("Fat", fatRatio, fatGrams)
                    }
                }
            }
        }
        
        // Daily Calorie Target Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = colors.surface
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Daily Calorie Target",
                    style = typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                OutlinedTextField(
                    value = calorieTarget,
                    onValueChange = { input ->
                        if (input.isEmpty() || input.all { it.isDigit() }) {
                            onCalorieChange(input)
                        }
                    },
                    label = { Text("Calories per day") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentGreen,
                        focusedLabelColor = accentGreen,
                        cursorColor = accentGreen
                    ),
                    suffix = { Text("kcal") }
                )
                
                Text(
                    text = "Recommended: 1500-2500 calories for most adults",
                    style = typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
        
        // Weight Goal Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = colors.surface
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Weight Goal",
                    style = typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Simplified weight goal selector
                weightGoalOptions.forEach { (option, icon) ->
                    val isSelected = option == weightGoal
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onWeightGoalChange(option) }
                            .background(if (isSelected) accentGreen.copy(alpha = 0.1f) else Color.Transparent)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { onWeightGoalChange(option) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = accentGreen
                            )
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected) accentGreen else colors.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = option,
                                style = typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) accentGreen else colors.onSurface
                            )
                        }
                    }
                }
            }
        }
        
        // Macronutrient Distribution Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = colors.surface
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp)
            ) {
                Text(
                    text = "Macronutrient Distribution",
                    style = typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Pie chart in a smaller size
                    Box(
                        modifier = Modifier
                            .weight(0.4f)
                            .padding(end = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        SimplifiedMacronutrientChart(
                            proteinPercentage = proteinRatio,
                            carbsPercentage = carbsRatio,
                            fatPercentage = fatRatio
                        )
                    }
                    
                    // Sliders in a more compact layout
                    Column(
                        modifier = Modifier
                            .weight(0.6f)
                            .padding(start = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        MacroSlider(
                            label = "Protein",
                            value = proteinRatio,
                            color = accentGreen,
                            onValueChange = onProteinChange
                        )
                        
                        MacroSlider(
                            label = "Carbs",
                            value = carbsRatio,
                            color = accentGreen,
                            onValueChange = onCarbsChange
                        )
                        
                        MacroSlider(
                            label = "Fat",
                            value = fatRatio,
                            color = accentGreen,
                            onValueChange = onFatChange
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(80.dp)) // Space for FAB
    }
}

@Composable
fun MacroSummary(
    name: String,
    percentage: Float,
    grams: Int
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val accentGreen = Color(0xFF4CAF50)
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$grams g",
            style = typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = accentGreen
        )
        
        Text(
            text = name,
            style = typography.bodySmall,
            color = colors.onSurfaceVariant
        )
        
        Text(
            text = "${(percentage * 100).roundToInt()}%",
            style = typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = accentGreen
        )
    }
}

@Composable
fun NutritionGoalStep(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    val typography = MaterialTheme.typography
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center
        )
        
        Text(
            text = subtitle,
            style = typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.alpha(0.8f)
        )
        
        content()
    }
}

@Composable
fun CircularProgress(
    percentage: Float,
    color: Color,
    backgroundColor: Color,
    strokeWidth: Float = 16f
) {
    Canvas(modifier = Modifier.size(160.dp)) {
        // Background circle
        drawArc(
            color = backgroundColor,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            size = Size(size.width, size.height)
        )
        
        // Progress arc
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = 360f * percentage,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            size = Size(size.width, size.height)
        )
    }
}

@Composable
fun SimplifiedMacronutrientChart(
    proteinPercentage: Float,
    carbsPercentage: Float,
    fatPercentage: Float
) {
    val colors = MaterialTheme.colorScheme
    val accentGreen = Color(0xFF4CAF50)
    val typography = MaterialTheme.typography
    
    Box(
        modifier = Modifier
            .size(180.dp)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Draw the pie chart
        Canvas(modifier = Modifier.size(160.dp)) {
            val proteinSweep = proteinPercentage * 360f
            val carbsSweep = carbsPercentage * 360f
            val fatSweep = fatPercentage * 360f
            
            // Draw slices with the same green color but different alpha
            drawArc(
                color = accentGreen.copy(alpha = 0.8f),
                startAngle = 0f,
                sweepAngle = proteinSweep,
                useCenter = true,
                size = Size(size.width, size.height)
            )
            
            drawArc(
                color = accentGreen.copy(alpha = 0.5f),
                startAngle = proteinSweep,
                sweepAngle = carbsSweep,
                useCenter = true,
                size = Size(size.width, size.height)
            )
            
            drawArc(
                color = accentGreen.copy(alpha = 0.3f),
                startAngle = proteinSweep + carbsSweep,
                sweepAngle = fatSweep,
                useCenter = true,
                size = Size(size.width, size.height)
            )
        }
        
        // Center circle
        Surface(
            modifier = Modifier
                .size(70.dp)
                .shadow(2.dp, CircleShape),
            shape = CircleShape,
            color = colors.surface
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${((proteinPercentage + carbsPercentage + fatPercentage) * 100).roundToInt()}%",
                    style = typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = accentGreen
                )
            }
        }
    }
    
    // Legend
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        MacroLegendItem("Protein", "${(proteinPercentage * 100).roundToInt()}%", accentGreen.copy(alpha = 0.8f))
        Spacer(modifier = Modifier.width(16.dp))
        MacroLegendItem("Carbs", "${(carbsPercentage * 100).roundToInt()}%", accentGreen.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.width(16.dp))
        MacroLegendItem("Fat", "${(fatPercentage * 100).roundToInt()}%", accentGreen.copy(alpha = 0.3f))
    }
}

@Composable
fun MacroLegendItem(
    label: String,
    value: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, CircleShape)
        )
        
        Spacer(modifier = Modifier.width(4.dp))
        
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun MacroSlider(
    label: String,
    value: Float,
    color: Color,
    onValueChange: (Float) -> Unit
) {
    val typography = MaterialTheme.typography
    val colors = MaterialTheme.colorScheme
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = colors.onSurface
            )
            
            Text(
                text = "${(value * 100).roundToInt()}%",
                style = typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = color
            )
        }
        
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0.1f..0.6f,
            steps = 0,
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color,
                inactiveTrackColor = color.copy(alpha = 0.2f)
            ),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun MacroGrams(
    name: String,
    grams: Int
) {
    val typography = MaterialTheme.typography
    val colors = MaterialTheme.colorScheme
    val accentGreen = Color(0xFF4CAF50)
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$grams g",
            style = typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = accentGreen
        )
        
        Text(
            text = name,
            style = typography.bodySmall,
            color = colors.onSurfaceVariant
        )
    }
} 