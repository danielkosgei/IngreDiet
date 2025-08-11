package com.thenewkenya.ingrediet.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.rememberTooltipState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.thenewkenya.ingrediet.data.network.AuthManager
import com.thenewkenya.ingrediet.data.repository.ProfileRepository
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material3.TextButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DietaryPreferencesScreen(navController: NavController) {
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
    
    // Load user's preferences from profile
    var selectedPreferences by remember { mutableStateOf(profile?.dietaryPreferences ?: emptyList<String>()) }
    var customPref by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var filterQuery by remember { mutableStateOf("") }
    
    LaunchedEffect(profile) {
        profile?.let {
            selectedPreferences = it.dietaryPreferences
            // will update customPreferences after lists are initialized
        }
    }
    
    // Common dietary preferences
    val commonDiets = listOf(
        "Vegetarian", 
        "Vegan", 
        "Pescatarian", 
        "Gluten-Free", 
        "Dairy-Free",
        "Keto", 
        "Paleo", 
        "Low-Carb", 
        "Mediterranean", 
        "Whole30"
    )
    
    // Religious preferences
    val religiousDiets = listOf(
        "Halal",
        "Kosher"
    )

    // Health conditions
    val healthDiets = listOf(
        "Diabetic-Friendly",
        "Low-Sodium",
        "Low-Fat",
        "FODMAP"
    )
    val allKnown = remember { (commonDiets + religiousDiets + healthDiets).toSet() }
    var customPreferences by remember { mutableStateOf(selectedPreferences.filter { it !in allKnown }) }
    LaunchedEffect(selectedPreferences) {
        customPreferences = selectedPreferences.filter { it !in allKnown }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Dietary Preferences",
                        style = typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.navigateUp() },
                        modifier = Modifier
                            .padding(8.dp)
                            .size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background,
                    titleContentColor = colors.onBackground
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Quick filter
            OutlinedTextField(
                value = filterQuery,
                onValueChange = { filterQuery = it },
                label = { Text("Search preferences") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Preset combinations
            PresetRow(
                onApply = { preset ->
                    val merged = (selectedPreferences.toSet() + preset).toList()
                    selectedPreferences = merged
                }
            )

            Text(
                text = "Select your dietary preferences to help us suggest recipes that match your eating habits.",
                style = typography.bodyMedium,
                color = colors.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            SectionCard(title = "Common Diets", icon = Icons.Outlined.Restaurant) {
                DietChipGroup(
                    options = commonDiets.filter { filterQuery.isBlank() || it.contains(filterQuery, ignoreCase = true) },
                    selectedOptions = selectedPreferences,
                    onSelectionChanged = { selectedPreferences = it },
                    definitions = dietDefinitions
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            SectionCard(title = "Religious & Cultural", icon = Icons.Outlined.FavoriteBorder) {
                DietChipGroup(
                    options = religiousDiets.filter { filterQuery.isBlank() || it.contains(filterQuery, ignoreCase = true) },
                    selectedOptions = selectedPreferences,
                    onSelectionChanged = { selectedPreferences = it },
                    definitions = dietDefinitions
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            SectionCard(title = "Health Conditions", icon = Icons.Outlined.HealthAndSafety) {
                DietChipGroup(
                    options = healthDiets.filter { filterQuery.isBlank() || it.contains(filterQuery, ignoreCase = true) },
                    selectedOptions = selectedPreferences,
                    onSelectionChanged = { selectedPreferences = it },
                    definitions = dietDefinitions
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Custom preferences
            Text(
                text = "Have specific preferences not listed above?",
                style = typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = colors.onBackground
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = customPref,
                    onValueChange = { customPref = it },
                    label = { Text("Add preference") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = {
                        val v = customPref.trim()
                        if (v.isNotEmpty() && !selectedPreferences.contains(v)) {
                            selectedPreferences = selectedPreferences + v
                            customPreferences = customPreferences + v
                            customPref = ""
                        }
                    },
                    enabled = customPref.trim().isNotEmpty()
                ) { Text("Add") }
            }

            if (customPreferences.isNotEmpty()) {
                SectionCard(title = "Custom Preferences", icon = Icons.Outlined.FavoriteBorder) {
                    DietChipGroup(
                        options = customPreferences.filter { filterQuery.isBlank() || it.contains(filterQuery, ignoreCase = true) },
                        selectedOptions = selectedPreferences,
                        onSelectionChanged = { selectedPreferences = it },
                        deletableOptions = customPreferences.toSet(),
                        onDelete = { pref ->
                            selectedPreferences = selectedPreferences - pref
                            customPreferences = customPreferences - pref
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Save button
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TextButton(
                    onClick = {
                        selectedPreferences = profile?.dietaryPreferences ?: emptyList()
                        customPref = ""
                        customPreferences = selectedPreferences.filter { it !in allKnown }
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("Reset") }
                Button(
                    onClick = {
                        profile?.let {
                            isSaving = true
                            val updatedProfile = it.copy(dietaryPreferences = selectedPreferences)
                            viewModel.updateProfile(updatedProfile)
                            isSaving = false
                            navController.navigateUp()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = profile != null && (selectedPreferences != (profile?.dietaryPreferences ?: emptyList<String>()))
                ) {
                    Text(if (isSaving) "Saving..." else "Save Preferences")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DietChipGroup(
    options: List<String>,
    selectedOptions: List<String>,
    onSelectionChanged: (List<String>) -> Unit,
    definitions: Map<String, String> = emptyMap(),
    deletableOptions: Set<String> = emptySet(),
    onDelete: ((String) -> Unit)? = null
) {
    val colors = MaterialTheme.colorScheme
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            val isSelected = selectedOptions.contains(option)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                val tooltipState = rememberTooltipState()
                TooltipBox(
                    positionProvider = androidx.compose.material3.TooltipDefaults.rememberPlainTooltipPositionProvider(),
                    tooltip = {
                        val def = definitions[option] ?: option
                        PlainTooltip { Text(def) }
                    },
                    state = tooltipState
                ) {
                    FilterChip(
                        modifier = Modifier.pointerInput(option) {
                            detectTapGestures(
                                onLongPress = {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    scope.launch { tooltipState.show() }
                                }
                            )
                        },
                        selected = isSelected,
                        onClick = {
                            if (isSelected) {
                                onSelectionChanged(selectedOptions - option)
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress) // remove
                            } else {
                                onSelectionChanged(selectedOptions + option)
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove) // add (lighter)
                            }
                        },
                        label = { Text(option) },
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                                )
                            }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colors.primaryContainer,
                            selectedLabelColor = colors.onPrimaryContainer
                        )
                    )
                }
                if (deletableOptions.contains(option) && onDelete != null) {
                    IconButton(onClick = {
                        onDelete(option)
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    }) {
                        Icon(Icons.Filled.Close, contentDescription = "Remove", tint = colors.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    Card(
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(icon, contentDescription = null, tint = colors.primary)
                Text(title, style = typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
            content()
        }
    }
}

// Minimal diet definitions for tooltips
private val dietDefinitions: Map<String, String> = mapOf(
    "Vegetarian" to "No meat, may include dairy/eggs.",
    "Vegan" to "No animal products.",
    "Pescatarian" to "Includes fish, no other meat.",
    "Gluten-Free" to "Avoids gluten (wheat, barley, rye).",
    "Dairy-Free" to "Avoids milk and dairy products.",
    "Keto" to "Very low-carb, high-fat.",
    "Paleo" to "Whole foods; excludes grains/legumes.",
    "Low-Carb" to "Reduced carbohydrate intake.",
    "Mediterranean" to "Plant-forward with olive oil, fish.",
    "Whole30" to "30-day elimination diet.",
    "Halal" to "Permissible under Islamic law.",
    "Kosher" to "Meets Jewish dietary laws.",
    "Diabetic-Friendly" to "Focus on balanced carbs and sugars.",
    "Low-Sodium" to "Limited salt intake.",
    "Low-Fat" to "Reduced fat intake.",
    "FODMAP" to "Limits fermentable carbs to reduce IBS symptoms."
) 

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PresetRow(
    onApply: (List<String>) -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val presets: List<Pair<String, List<String>>> = listOf(
        "Balanced" to listOf("Mediterranean"),
        "High-Protein" to listOf("Low-Carb", "High-Protein"),
        "Plant-Based" to listOf("Vegan"),
        "Low-Carb Starter" to listOf("Low-Carb", "Keto"),
        "Halal + Low-Sodium" to listOf("Halal", "Low-Sodium")
    )
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        presets.forEach { (label, prefs) ->
            AssistChip(
                onClick = {
                    onApply(prefs)
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                },
                label = { Text(label) }
            )
        }
    }
} 