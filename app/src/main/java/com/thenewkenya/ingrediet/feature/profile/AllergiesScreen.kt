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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.thenewkenya.ingrediet.data.network.AuthManager
import com.thenewkenya.ingrediet.data.repository.ProfileRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllergiesScreen(navController: NavController) {
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
    
    // Load user's allergies from profile
    var selectedAllergies by remember { mutableStateOf(profile?.allergies ?: emptyList()) }
    var newAllergy by remember { mutableStateOf("") }
    
    LaunchedEffect(profile) {
        profile?.let {
            selectedAllergies = it.allergies
        }
    }
    
    // Common food allergies
    val commonAllergies = listOf(
        "Milk", 
        "Eggs", 
        "Fish", 
        "Shellfish", 
        "Tree nuts",
        "Peanuts", 
        "Wheat", 
        "Soybeans", 
        "Sesame"
    )
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Allergies & Restrictions",
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
            Text(
                text = "Select your food allergies or restrictions to help us filter recipes that contain ingredients you can't eat.",
                style = typography.bodyMedium,
                color = colors.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Common Allergies Section
            Text(
                text = "Common Food Allergies",
                style = typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onBackground
            )
            
            AllergiesChipGroup(
                options = commonAllergies,
                selectedOptions = selectedAllergies,
                onSelectionChanged = { selectedAllergies = it }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Custom allergies
            Text(
                text = "Additional Allergies or Restrictions",
                style = typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onBackground
            )
            
            Text(
                text = "Add any other ingredients you need to avoid",
                style = typography.bodyMedium,
                color = colors.onSurfaceVariant
            )
            
            // Custom allergy input field
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newAllergy,
                    onValueChange = { newAllergy = it },
                    label = { Text("Add an allergy") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                IconButton(
                    onClick = {
                        if (newAllergy.isNotBlank() && !selectedAllergies.contains(newAllergy)) {
                            selectedAllergies = selectedAllergies + newAllergy
                            newAllergy = ""
                        }
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add allergy",
                        tint = colors.primary
                    )
                }
            }
            
            // Display custom allergies
            if (selectedAllergies.isNotEmpty()) {
                Text(
                    text = "Your Allergies",
                    style = typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = colors.onBackground,
                    modifier = Modifier.padding(top = 16.dp)
                )
                
                // Custom allergies chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val customAllergies = selectedAllergies.filterNot { commonAllergies.contains(it) }
                    
                    customAllergies.forEach { allergy ->
                        FilterChip(
                            selected = true,
                            onClick = { selectedAllergies = selectedAllergies - allergy },
                            label = { Text(allergy) },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove allergy",
                                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                                )
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Save button
            Button(
                onClick = {
                    profile?.let {
                        val updatedProfile = it.copy(allergies = selectedAllergies)
                        viewModel.updateProfile(updatedProfile)
                        navController.navigateUp()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Allergies")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllergiesChipGroup(
    options: List<String>,
    selectedOptions: List<String>,
    onSelectionChanged: (List<String>) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Create a flow of filter chips
        options.forEach { option ->
            val isSelected = selectedOptions.contains(option)
            
            FilterChip(
                selected = isSelected,
                onClick = {
                    if (isSelected) {
                        onSelectionChanged(selectedOptions - option)
                    } else {
                        onSelectionChanged(selectedOptions + option)
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
                } else null
            )
        }
    }
} 