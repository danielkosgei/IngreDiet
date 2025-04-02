package com.thenewkenya.ingrediet.feature.profile

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Cake
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.LocalDining
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.thenewkenya.ingrediet.data.model.Profile
import com.thenewkenya.ingrediet.data.network.AuthManager
import com.thenewkenya.ingrediet.data.network.supabase
import com.thenewkenya.ingrediet.data.repository.ProfileRepository
import io.github.jan.supabase.auth.auth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel = remember { 
        ProfileViewModel(
            ProfileRepository(),
            AuthManager(context)
        ) 
    }
    val uiState by viewModel.uiState.collectAsState()
    var isEditMode by remember { mutableStateOf(false) }
    var editableProfile by remember { mutableStateOf<Profile?>(null) }
    var profile by remember { mutableStateOf<Profile?>(null) }
    var showSignOutConfirmation by remember { mutableStateOf(false) }

    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.fetchProfile()
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is ProfileUiState.Success -> {
                profile = (uiState as ProfileUiState.Success).profile
                if (!isEditMode) { // Only reset editable if not in edit mode
                    editableProfile = profile
                }
            }
            else -> {}
        }
    }

    // Sign Out Confirmation Dialog
    if (showSignOutConfirmation) {
        AlertDialog(
            onDismissRequest = { showSignOutConfirmation = false },
            title = { Text("Sign Out") },
            text = { Text("Are you sure you want to sign out?") },
            confirmButton = {
                Button(
                    onClick = {
                        showSignOutConfirmation = false
                        viewModel.signOut()
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.error
                    )
                ) {
                    Text("Sign Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My Profile",
                        style = typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (isEditMode) {
                                editableProfile?.let { viewModel.updateProfile(it) }
                            }
                            isEditMode = !isEditMode
                        }
                    ) {
                        Icon(
                            imageVector = if (isEditMode) Icons.Default.Check else Icons.Default.Edit,
                            contentDescription = if (isEditMode) "Save" else "Edit"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.surface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(colors.background)
        ) {
            when (uiState) {
                is ProfileUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = colors.primary
                        )
                    }
                }

                is ProfileUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = colors.errorContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = null,
                                    tint = colors.error,
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = (uiState as ProfileUiState.Error).message,
                                    style = typography.bodyLarge,
                                    color = colors.error,
                                    textAlign = TextAlign.Center
                                )
                                Button(
                                    onClick = { viewModel.fetchProfile() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colors.error
                                    )
                                ) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                }

                is ProfileUiState.Success -> {
                    val currentProfile = (uiState as ProfileUiState.Success).profile
                    val currentUser = supabase.auth.currentUserOrNull()
                    var profileImageUrl by remember { mutableStateOf<String?>(null) }
                    
                    LaunchedEffect(currentUser) {
                        try {
                            val metadata = currentUser?.userMetadata
                            val jsonMetadata = metadata?.toString()
                            
                            if (jsonMetadata != null) {
                                val regex = "\"avatar_url\"\\s*:\\s*\"([^\"]+)\"".toRegex()
                                val matchResult = regex.find(jsonMetadata)
                                profileImageUrl = matchResult?.groupValues?.getOrNull(1)
                            }
                        } catch (e: Exception) {
                            Log.e("ProfileScreen", "Failed to get profile image URL", e)
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(horizontal = 16.dp)
                    ) {
                        // Profile header with image
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Profile Image
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .background(colors.surfaceVariant)
                                    .border(3.dp, colors.primary.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (profileImageUrl != null) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(profileImageUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Profile picture",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = colors.onSurfaceVariant,
                                        modifier = Modifier.size(60.dp)
                                    )
                                }
                                
                                // Edit icon overlay
                                if (isEditMode) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(colors.surface.copy(alpha = 0.5f))
                                            .clickable { 
                                                // Image picker would go here
                                                // For now just a placeholder
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Camera,
                                            contentDescription = "Change profile picture",
                                            tint = colors.onSurface,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }
                            }
                            
                            // User name display below image
                            if (!isEditMode) {
                                Text(
                                    text = "${currentProfile.firstName} ${currentProfile.lastName}",
                                    style = typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 140.dp)
                                )
                            }
                        }
                        
                        // Personal Information Section
                        ProfileSection(
                            title = "Personal Information",
                            icon = Icons.Outlined.Person
                        ) {
                            ProfileTextField(
                                label = "First Name",
                                value = if (isEditMode) editableProfile?.firstName ?: "" else currentProfile.firstName,
                                onValueChange = { editableProfile = editableProfile?.copy(firstName = it) },
                                isEditable = isEditMode,
                                leadingIcon = null
                            )
                            
                            ProfileTextField(
                                label = "Last Name",
                                value = if (isEditMode) editableProfile?.lastName ?: "" else currentProfile.lastName,
                                onValueChange = { editableProfile = editableProfile?.copy(lastName = it) },
                                isEditable = isEditMode,
                                leadingIcon = null
                            )
                            
                            ProfileTextField(
                                label = "Email",
                                value = currentProfile.email,
                                onValueChange = { /* Email is typically not editable */ },
                                isEditable = false,
                                leadingIcon = Icons.Outlined.Email
                            )
                        }
                        
                        // Dietary Preferences Section
                        ProfileSection(
                            title = "Dietary Preferences",
                            icon = Icons.Outlined.Restaurant
                        ) {
                            val preferencesValue = currentProfile.dietaryPreferences.joinToString(", ")
                            ProfileTextField(
                                label = "Dietary Preferences",
                                value = if (isEditMode) 
                                    editableProfile?.dietaryPreferences?.joinToString(", ") ?: "" 
                                else 
                                    preferencesValue,
                                onValueChange = { 
                                    val preferences = it.split(",").map { pref -> pref.trim() }.filter { pref -> pref.isNotEmpty() }
                                    editableProfile = editableProfile?.copy(dietaryPreferences = preferences)
                                },
                                isEditable = isEditMode,
                                leadingIcon = Icons.Outlined.LocalDining,
                                helperText = if (isEditMode) "Separate multiple preferences with commas" else null
                            )
                            
                            val allergiesValue = currentProfile.allergies.joinToString(", ")
                            ProfileTextField(
                                label = "Allergies",
                                value = if (isEditMode) 
                                    editableProfile?.allergies?.joinToString(", ") ?: "" 
                                else 
                                    allergiesValue,
                                onValueChange = { 
                                    val allergies = it.split(",").map { allergy -> allergy.trim() }.filter { allergy -> allergy.isNotEmpty() }
                                    editableProfile = editableProfile?.copy(allergies = allergies)
                                },
                                isEditable = isEditMode,
                                leadingIcon = null,
                                helperText = if (isEditMode) "Separate multiple allergies with commas" else null
                            )
                        }
                        
                        // Goals Section
                        ProfileSection(
                            title = "Health Goals",
                            icon = Icons.Outlined.EmojiEvents
                        ) {
                            ProfileTextField(
                                label = "Weight Goal",
                                value = if (isEditMode) editableProfile?.weightGoal ?: "" else currentProfile.weightGoal,
                                onValueChange = { editableProfile = editableProfile?.copy(weightGoal = it) },
                                isEditable = isEditMode,
                                leadingIcon = null
                            )
                            
                            ProfileTextField(
                                label = "Daily Calorie Target",
                                value = if (isEditMode) 
                                    (editableProfile?.calorieTarget ?: 0).toString() 
                                else 
                                    if (currentProfile.calorieTarget > 0) 
                                        currentProfile.calorieTarget.toString() 
                                    else 
                                        "",
                                onValueChange = { 
                                    val calories = it.toIntOrNull() ?: 0
                                    editableProfile = editableProfile?.copy(calorieTarget = calories)
                                },
                                isEditable = isEditMode,
                                leadingIcon = null,
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        Button(
                            onClick = { showSignOutConfirmation = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.surfaceVariant,
                                contentColor = colors.error
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = "Sign Out"
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Sign Out",
                                    style = typography.titleMedium
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    content: @Composable () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    
    Card(
        colors = CardDefaults.cardColors(
            containerColor = colors.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                
                Text(
                    text = title,
                    style = typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.primary
                )
            }
            
            androidx.compose.material3.HorizontalDivider(
                color = colors.outlineVariant,
                thickness = 1.dp
            )
            
            // Section content
            content()
        }
    }
}

@Composable
fun ProfileTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isEditable: Boolean,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    helperText: String? = null,
    keyboardType: androidx.compose.ui.text.input.KeyboardType = androidx.compose.ui.text.input.KeyboardType.Text
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Column {
        if (isEditable) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { 
                    Text(
                        text = label,
                        style = typography.bodyMedium
                    ) 
                },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = keyboardType
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedBorderColor = colors.outline,
                    focusedBorderColor = colors.primary
                ),
                modifier = Modifier.fillMaxWidth(),
                textStyle = typography.bodyLarge,
                leadingIcon = leadingIcon?.let { icon ->
                    { 
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = colors.onSurfaceVariant
                        )
                    }
                },
                supportingText = helperText?.let { text ->
                    {
                        Text(
                            text = text,
                            style = typography.bodySmall,
                            color = colors.onSurfaceVariant
                        )
                    }
                }
            )
        } else {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = label,
                    style = typography.bodyMedium,
                    color = colors.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(16.dp)
                ) {
                    if (leadingIcon != null) {
                        Icon(
                            imageVector = leadingIcon,
                            contentDescription = null,
                            tint = colors.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    
                    Text(
                        text = if (value.isNotEmpty()) value else "Not set",
                        style = typography.bodyLarge,
                        color = if (value.isNotEmpty()) colors.onSurface else colors.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}
