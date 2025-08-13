package com.thenewkenya.ingrediet.feature.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun WelcomeStep(
    onGetStarted: () -> Unit,
    colors: androidx.compose.material3.ColorScheme,
    typography: androidx.compose.material3.Typography
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Restaurant,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = colors.primary
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Welcome to IngreDiet!",
            style = typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Let's personalize your nutrition journey by getting to know you better. This helps us provide tailored recipe recommendations and health insights.",
            style = typography.bodyLarge,
            color = colors.onSurface.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            lineHeight = typography.bodyLarge.lineHeight
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onGetStarted,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primary
            )
        ) {
            Text(
                text = "Get Started",
                style = typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Don't worry - you can skip any step or update this information later",
            style = typography.bodySmall,
            color = colors.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun PersonalInfoStep(
    firstName: String,
    lastName: String,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    colors: androidx.compose.material3.ColorScheme,
    typography: androidx.compose.material3.Typography
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Tell us about yourself",
                style = typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )
        }
        
        item {
            Text(
                text = "This helps us personalize your experience",
                style = typography.bodyMedium,
                color = colors.onSurface.copy(alpha = 0.7f)
            )
        }
        
        item { Spacer(modifier = Modifier.height(8.dp)) }
        
        item {
            OutlinedTextField(
                value = firstName,
                onValueChange = onFirstNameChange,
                label = { Text("First Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primary,
                    focusedLabelColor = colors.primary
                )
            )
        }
        
        item {
            OutlinedTextField(
                value = lastName,
                onValueChange = onLastNameChange,
                label = { Text("Last Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primary,
                    focusedLabelColor = colors.primary
                )
            )
        }
    }
}

@Composable
fun PhysicalDataStep(
    age: String,
    height: String,
    weight: String,
    sex: String,
    onAgeChange: (String) -> Unit,
    onHeightChange: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    onSexChange: (String) -> Unit,
    colors: androidx.compose.material3.ColorScheme,
    typography: androidx.compose.material3.Typography
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Physical Information",
                style = typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )
        }
        
        item {
            Text(
                text = "This helps us calculate your BMI and provide personalized nutrition recommendations",
                style = typography.bodyMedium,
                color = colors.onSurface.copy(alpha = 0.7f)
            )
        }
        
        item { Spacer(modifier = Modifier.height(8.dp)) }
        
        // Sex selection
        item {
            Text(
                text = "Biological Sex",
                style = typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = colors.onSurface
            )
        }
        
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                listOf("Male", "Female", "Other").forEach { option ->
                    SelectableCard(
                        text = option,
                        isSelected = sex.equals(option, ignoreCase = true),
                        onClick = { onSexChange(option.lowercase()) },
                        modifier = Modifier.weight(1f),
                        colors = colors
                    )
                }
            }
        }
        
        item { Spacer(modifier = Modifier.height(8.dp)) }
        
        // Age
        item {
            OutlinedTextField(
                value = age,
                onValueChange = { if (it.all { char -> char.isDigit() } && it.length <= 3) onAgeChange(it) },
                label = { Text("Age") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                suffix = { Text("years") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primary,
                    focusedLabelColor = colors.primary
                )
            )
        }
        
        // Height
        item {
            OutlinedTextField(
                value = height,
                onValueChange = { if (it.matches(Regex("^\\d*\\.?\\d*$")) && it.length <= 6) onHeightChange(it) },
                label = { Text("Height") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                suffix = { Text("cm") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primary,
                    focusedLabelColor = colors.primary
                )
            )
        }
        
        // Weight
        item {
            OutlinedTextField(
                value = weight,
                onValueChange = { if (it.matches(Regex("^\\d*\\.?\\d*$")) && it.length <= 6) onWeightChange(it) },
                label = { Text("Weight") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                suffix = { Text("kg") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primary,
                    focusedLabelColor = colors.primary
                )
            )
        }
        
        // BMI Preview
        if (height.isNotBlank() && weight.isNotBlank()) {
            item {
                val heightValue = height.toFloatOrNull()
                val weightValue = weight.toFloatOrNull()
                
                if (heightValue != null && weightValue != null && heightValue > 0) {
                    val bmi = weightValue / ((heightValue / 100) * (heightValue / 100))
                    val bmiCategory = when {
                        bmi < 18.5 -> "Underweight"
                        bmi < 25.0 -> "Normal weight"
                        bmi < 30.0 -> "Overweight"
                        else -> "Obese"
                    }
                    
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = colors.primaryContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "BMI Preview",
                                style = typography.titleSmall,
                                fontWeight = FontWeight.Medium,
                                color = colors.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "%.1f - %s".format(bmi, bmiCategory),
                                style = typography.bodyMedium,
                                color = colors.onSurface.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActivityLevelStep(
    selectedLevel: String,
    onLevelSelected: (String) -> Unit,
    colors: androidx.compose.material3.ColorScheme,
    typography: androidx.compose.material3.Typography
) {
    val activityLevels = listOf(
        Triple("sedentary", "Sedentary", "Little to no exercise"),
        Triple("light", "Light", "Light exercise 1-3 days/week"),
        Triple("moderate", "Moderate", "Moderate exercise 3-5 days/week"),
        Triple("active", "Active", "Heavy exercise 6-7 days/week"),
        Triple("very_active", "Very Active", "Very heavy exercise, physical job")
    )
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Activity Level",
                style = typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )
        }
        
        item {
            Text(
                text = "This helps us calculate your daily caloric needs",
                style = typography.bodyMedium,
                color = colors.onSurface.copy(alpha = 0.7f)
            )
        }
        
        item { Spacer(modifier = Modifier.height(8.dp)) }
        
        items(activityLevels) { (value, title, description) ->
            ActivityLevelCard(
                title = title,
                description = description,
                isSelected = selectedLevel == value,
                onClick = { onLevelSelected(value) },
                colors = colors,
                typography = typography
            )
        }
    }
}

@Composable
fun DietaryPreferencesStep(
    selectedPreferences: List<String>,
    selectedAllergies: List<String>,
    onPreferencesChange: (List<String>) -> Unit,
    onAllergiesChange: (List<String>) -> Unit,
    colors: androidx.compose.material3.ColorScheme,
    typography: androidx.compose.material3.Typography
) {
    val dietaryOptions = listOf(
        "Vegetarian", "Vegan", "Pescatarian", "Keto", "Paleo", 
        "Mediterranean", "Dairy-Free", "Gluten-Free", "Low-Carb", "Halal"
    )
    
    val allergyOptions = listOf(
        "Nuts", "Dairy", "Eggs", "Fish", "Shellfish", 
        "Wheat", "Soy", "Sesame", "Pork", "Beef"
    )
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Dietary Preferences",
                style = typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )
        }
        
        item {
            Text(
                text = "Help us recommend recipes that fit your lifestyle",
                style = typography.bodyMedium,
                color = colors.onSurface.copy(alpha = 0.7f)
            )
        }
        
        item { Spacer(modifier = Modifier.height(8.dp)) }
        
        item {
            Text(
                text = "Dietary Preferences",
                style = typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = colors.onSurface
            )
        }
        
        item {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(240.dp)
            ) {
                items(dietaryOptions) { option ->
                    SelectableCard(
                        text = option,
                        isSelected = selectedPreferences.contains(option),
                        onClick = {
                            if (selectedPreferences.contains(option)) {
                                onPreferencesChange(selectedPreferences - option)
                            } else {
                                onPreferencesChange(selectedPreferences + option)
                            }
                        },
                        colors = colors
                    )
                }
            }
        }
        
        item { Spacer(modifier = Modifier.height(16.dp)) }
        
        item {
            Text(
                text = "Allergies & Restrictions",
                style = typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = colors.onSurface
            )
        }
        
        item {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(240.dp)
            ) {
                items(allergyOptions) { option ->
                    SelectableCard(
                        text = option,
                        isSelected = selectedAllergies.contains(option),
                        onClick = {
                            if (selectedAllergies.contains(option)) {
                                onAllergiesChange(selectedAllergies - option)
                            } else {
                                onAllergiesChange(selectedAllergies + option)
                            }
                        },
                        colors = colors
                    )
                }
            }
        }
    }
}

@Composable
fun HealthInfoStep(
    selectedConditions: List<String>,
    onConditionsChange: (List<String>) -> Unit,
    colors: androidx.compose.material3.ColorScheme,
    typography: androidx.compose.material3.Typography
) {
    val healthConditions = listOf(
        "None", "Diabetes", "Hypertension", "Heart Disease", 
        "High Cholesterol", "PCOS", "Thyroid Issues", "Other"
    )
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Health Information",
                style = typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )
        }
        
        item {
            Text(
                text = "This helps us provide safer and more appropriate recipe recommendations",
                style = typography.bodyMedium,
                color = colors.onSurface.copy(alpha = 0.7f)
            )
        }
        
        item { Spacer(modifier = Modifier.height(8.dp)) }
        
        item {
            Text(
                text = "Do you have any of these health conditions?",
                style = typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = colors.onSurface
            )
        }
        
        item {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(200.dp)
            ) {
                items(healthConditions) { condition ->
                    SelectableCard(
                        text = condition,
                        isSelected = selectedConditions.contains(condition),
                        onClick = {
                            if (condition == "None") {
                                onConditionsChange(listOf("None"))
                            } else {
                                val newConditions = if (selectedConditions.contains(condition)) {
                                    selectedConditions - condition
                                } else {
                                    (selectedConditions - "None") + condition
                                }
                                onConditionsChange(newConditions)
                            }
                        },
                        colors = colors
                    )
                }
            }
        }
    }
}

@Composable
fun GoalsStep(
    selectedGoals: List<String>,
    selectedWeightGoal: String,
    onGoalsChange: (List<String>) -> Unit,
    onWeightGoalChange: (String) -> Unit,
    colors: androidx.compose.material3.ColorScheme,
    typography: androidx.compose.material3.Typography
) {
    val healthGoals = listOf(
        "Weight Loss", "Weight Gain", "Muscle Building", "Heart Health",
        "Better Digestion", "More Energy", "Better Sleep", "General Wellness"
    )
    
    val weightGoals = listOf(
        Triple("maintain", "Maintain", "Keep current weight"),
        Triple("lose", "Lose Weight", "Gradual weight loss"),
        Triple("gain", "Gain Weight", "Healthy weight gain")
    )
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Your Health Goals",
                style = typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )
        }
        
        item {
            Text(
                text = "Let us know what you're working towards",
                style = typography.bodyMedium,
                color = colors.onSurface.copy(alpha = 0.7f)
            )
        }
        
        item { Spacer(modifier = Modifier.height(8.dp)) }
        
        item {
            Text(
                text = "Weight Goal",
                style = typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = colors.onSurface
            )
        }
        
        items(weightGoals) { (value, title, description) ->
            ActivityLevelCard(
                title = title,
                description = description,
                isSelected = selectedWeightGoal == value,
                onClick = { onWeightGoalChange(value) },
                colors = colors,
                typography = typography
            )
        }
        
        item { Spacer(modifier = Modifier.height(16.dp)) }
        
        item {
            Text(
                text = "Health Goals (Select all that apply)",
                style = typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = colors.onSurface
            )
        }
        
        item {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(200.dp)
            ) {
                items(healthGoals) { goal ->
                    SelectableCard(
                        text = goal,
                        isSelected = selectedGoals.contains(goal),
                        onClick = {
                            if (selectedGoals.contains(goal)) {
                                onGoalsChange(selectedGoals - goal)
                            } else {
                                onGoalsChange(selectedGoals + goal)
                            }
                        },
                        colors = colors
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectableCard(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colors: androidx.compose.material3.ColorScheme
) {
    Card(
        modifier = modifier
            .clickable { onClick() }
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) colors.primaryContainer else colors.surfaceVariant
        ),
        border = if (isSelected) {
            BorderStroke(2.dp, colors.primary)
        } else null,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                color = if (isSelected) colors.onPrimaryContainer else colors.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ActivityLevelCard(
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    colors: androidx.compose.material3.ColorScheme,
    typography: androidx.compose.material3.Typography
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) colors.primaryContainer else colors.surfaceVariant
        ),
        border = if (isSelected) {
            BorderStroke(2.dp, colors.primary)
        } else null,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) colors.onPrimaryContainer else colors.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = typography.bodySmall,
                color = if (isSelected) colors.onPrimaryContainer.copy(alpha = 0.8f) else colors.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
} 