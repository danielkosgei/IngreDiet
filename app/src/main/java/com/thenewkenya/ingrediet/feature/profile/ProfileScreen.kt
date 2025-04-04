package com.thenewkenya.ingrediet.feature.profile

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.outlined.Help
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocalDining
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Report
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.thenewkenya.ingrediet.data.model.Profile
import com.thenewkenya.ingrediet.data.network.AuthManager
import com.thenewkenya.ingrediet.data.network.supabase
import com.thenewkenya.ingrediet.data.repository.ProfileRepository
import io.github.jan.supabase.auth.auth
import androidx.compose.foundation.BorderStroke
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController, isEditMode: Boolean = false) {
    val context = LocalContext.current
    val viewModel = remember { 
        ProfileViewModel(
            ProfileRepository(),
            AuthManager(context)
        ) 
    }
    val uiState by viewModel.uiState.collectAsState()
    var showSignOutConfirmation by remember { mutableStateOf(false) }
    var showDeleteAccountConfirmation by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    LaunchedEffect(Unit) {
        viewModel.fetchProfile()
    }

    LaunchedEffect(uiState) {
        if (uiState is ProfileUiState.Error && (uiState as ProfileUiState.Error).message == "Account deleted") {
            Toast.makeText(context, "Account successfully deleted", Toast.LENGTH_SHORT).show()
            navController.navigate("login") {
                popUpTo("home") { inclusive = true }
            }
        }
    }

    // Sign Out Confirmation Dialog
    if (showSignOutConfirmation) {
        var isSigningOut by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { 
                if (!isSigningOut) showSignOutConfirmation = false 
            },
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    tint = colors.error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = { 
                Text(
                    text = if (isSigningOut) "Signing Out..." else "Sign Out",
                    style = typography.headlineSmall,
                    textAlign = TextAlign.Center
                ) 
            },
            text = { 
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isSigningOut) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(36.dp),
                                color = colors.primary,
                                strokeWidth = 3.dp
                            )
                            Text(
                                text = "Logging you out...",
                                style = typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        Text(
                            text = "You will be logged out of your account and returned to the login screen.",
                            style = typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isSigningOut = true
                        // Add a slight delay to show loading state
                        coroutineScope.launch {
                            try {
                                viewModel.signOut()
                                delay(500) // Small delay for UI feedback
                                showSignOutConfirmation = false
                                navController.navigate("login") {
                                    popUpTo("home") { inclusive = true }
                                }
                            } catch (e: Exception) {
                                Log.e("ProfileScreen", "Error signing out", e)
                                isSigningOut = false
                                Toast.makeText(context, "Failed to sign out. Please try again.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.error,
                        contentColor = colors.onError
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp),
                    enabled = !isSigningOut,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Sign Out")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showSignOutConfirmation = false },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = colors.onSurface
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp),
                    enabled = !isSigningOut,
                    border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancel")
                }
            },
            containerColor = colors.surface,
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 8.dp,
            properties = DialogProperties(
                dismissOnBackPress = !isSigningOut,
                dismissOnClickOutside = !isSigningOut
            )
        )
    }

    // Delete Account Confirmation Dialog
    if (showDeleteAccountConfirmation) {
        AlertDialog(
            onDismissRequest = { 
                if (!isDeleting) showDeleteAccountConfirmation = false 
            },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = null,
                    tint = colors.error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = { 
                Text(
                    text = if (isDeleting) "Deleting Account" else "Delete Account",
                    style = typography.headlineSmall,
                    textAlign = TextAlign.Center
                ) 
            },
            text = { 
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isDeleting) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(36.dp),
                                color = colors.primary,
                                strokeWidth = 3.dp
                            )
                            Text(
                                text = "Deleting your account...",
                                style = typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        Text(
                            text = "Are you sure you want to delete your account? This action cannot be undone and all your data will be permanently lost.",
                            style = typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isDeleting = true
                        coroutineScope.launch {
                            try {
                                viewModel.deleteAccount()
                            } catch (e: Exception) {
                                Log.e("ProfileScreen", "Error deleting account", e)
                                isDeleting = false
                                Toast.makeText(context, "Failed to delete account. Please try again.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.error,
                        contentColor = colors.onError
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp),
                    enabled = !isDeleting,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Delete Account")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteAccountConfirmation = false },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = colors.onSurface
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp),
                    enabled = !isDeleting,
                    border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancel")
                }
            },
            containerColor = colors.surface,
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 8.dp,
            properties = DialogProperties(
                dismissOnBackPress = !isDeleting,
                dismissOnClickOutside = !isDeleting
            )
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditMode) "Edit Profile" else "My Profile",
                        style = typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.navigateUp() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = "Back",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                actions = {
                    if (isEditMode) {
                        // Save button for edit mode
                        IconButton(onClick = { 
                            // Handle save and navigate back
                            navController.navigateUp()
                        }) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Save"
                            )
                        }
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
                    
                    // Track editable values
                    var editedFirstName by remember { mutableStateOf(currentProfile.firstName) }
                    var editedLastName by remember { mutableStateOf(currentProfile.lastName) }
                    var editedEmail by remember { mutableStateOf(currentProfile.email) }
                    
                    // For displaying Google user's name
                    var displayName by remember { mutableStateOf<String?>(null) }
                    
                    LaunchedEffect(currentUser) {
                        try {
                            val metadata = currentUser?.userMetadata
                            val jsonMetadata = metadata?.toString()
                            
                            if (jsonMetadata != null) {
                                // Extract avatar URL
                                val avatarRegex = "\"avatar_url\"\\s*:\\s*\"([^\"]+)\"".toRegex()
                                val avatarMatch = avatarRegex.find(jsonMetadata)
                                profileImageUrl = avatarMatch?.groupValues?.getOrNull(1)
                                
                                // Extract display name
                                val nameRegex = "\"display_name\"\\s*:\\s*\"([^\"]+)\"".toRegex()
                                val nameMatch = nameRegex.find(jsonMetadata)
                                displayName = nameMatch?.groupValues?.getOrNull(1)
                                
                                // Extract first name
                                val firstNameRegex = "\"first_name\"\\s*:\\s*\"([^\"]+)\"".toRegex()
                                val firstNameMatch = firstNameRegex.find(jsonMetadata)
                                val metadataFirstName = firstNameMatch?.groupValues?.getOrNull(1)
                                
                                // Extract last name
                                val lastNameRegex = "\"last_name\"\\s*:\\s*\"([^\"]+)\"".toRegex()
                                val lastNameMatch = lastNameRegex.find(jsonMetadata)
                                val metadataLastName = lastNameMatch?.groupValues?.getOrNull(1)
                                
                                // Update editable values if profile values are empty but metadata exists
                                if (editedFirstName.isEmpty() && metadataFirstName != null) {
                                    editedFirstName = metadataFirstName
                                }
                                
                                if (editedLastName.isEmpty() && metadataLastName != null) {
                                    editedLastName = metadataLastName
                                }
                                
                                Log.d("ProfileScreen", "Metadata: $jsonMetadata")
                                Log.d("ProfileScreen", "Display name from metadata: $displayName")
                            }
                        } catch (e: Exception) {
                            Log.e("ProfileScreen", "Failed to get profile data from metadata", e)
                        }
                    }

                    if (isEditMode) {
                        // Edit Mode UI
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(colors.background)
                                .padding(horizontal = 16.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            // Profile Header (similar but with edit capabilities)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 24.dp, bottom = 20.dp)
                            ) {
                                // Profile Image with edit overlay
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(CircleShape)
                                        .background(colors.surfaceVariant)
                                        .clickable { /* Handle image change */ },
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
                                            modifier = Modifier.size(50.dp)
                                        )
                                    }
                                    
                                    // Camera icon overlay for changing image
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.3f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Camera,
                                            contentDescription = "Change profile picture",
                                            tint = Color.White,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                // Editable fields
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Text(
                                            text = "Profile Information",
                                            style = typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        
                                        // Name field
                                        OutlinedTextField(
                                            value = editedFirstName,
                                            onValueChange = { editedFirstName = it },
                                            label = { Text("First Name") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true
                                        )
                                        
                                        OutlinedTextField(
                                            value = editedLastName,
                                            onValueChange = { editedLastName = it },
                                            label = { Text("Last Name") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true
                                        )
                                        
                                        OutlinedTextField(
                                            value = editedEmail,
                                            onValueChange = { editedEmail = it },
                                            label = { Text("Email address") },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Outlined.Email,
                                                    contentDescription = null
                                                )
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            enabled = false // Email usually not editable after account creation
                                        )
                                        
                                        OutlinedTextField(
                                            value = "••••••••",
                                            onValueChange = { },
                                            label = { Text("Password") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            enabled = false,
                                            trailingIcon = {
                                                IconButton(onClick = { navController.navigate("profile/account") }) {
                                                    Icon(
                                                        imageVector = Icons.Outlined.Settings,
                                                        contentDescription = "Change password"
                                                    )
                                                }
                                            }
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                Button(
                                    onClick = {
                                        // Update profile with edited values
                                        val updatedProfile = currentProfile.copy(
                                            firstName = editedFirstName,
                                            lastName = editedLastName,
                                            email = editedEmail
                                        )
                                        viewModel.updateProfile(updatedProfile)
                                        navController.navigateUp()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Save Changes")
                                }
                            }
                        }
                    } else {
                        // Regular Profile View
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(colors.background)
                                .padding(horizontal = 20.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            // Profile Header - Updated layout with image left, text right
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 32.dp, bottom = 32.dp)
                            ) {
                                // Profile Image - Larger size
                                Box(
                                    modifier = Modifier
                                        .size(130.dp)
                                        .clip(CircleShape)
                                        .background(colors.surfaceVariant),
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
                                            modifier = Modifier.size(65.dp)
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.width(20.dp))
                                
                                // User name and details - Now on the right
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 4.dp)
                                ) {
                                    Text(
                                        text = if (currentProfile.firstName.isNotEmpty() || currentProfile.lastName.isNotEmpty()) 
                                            "${currentProfile.firstName} ${currentProfile.lastName}".trim() 
                                        else if (displayName != null) 
                                            displayName!!
                                        else 
                                            "User",
                                        style = typography.titleLarge.copy(
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = colors.onBackground,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Text(
                                        text = currentProfile.email,
                                        style = typography.bodyMedium,
                                        color = colors.onSurfaceVariant
                                    )
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    Button(
                                        onClick = { navController.navigate("profile/edit") },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = colors.primary.copy(alpha = 0.1f),
                                            contentColor = colors.primary
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Edit Profile")
                                    }
                                }
                            }

                            // Main options - organized in two cards with more spacing
                            // Card 1: Personalization
                            Text(
                                text = "Personalization",
                                style = typography.titleMedium,
                                color = colors.onSurfaceVariant,
                                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                            )
                            
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = colors.surface),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    ProfileListItem(
                                        icon = Icons.Outlined.Favorite,
                                        title = "Favourites",
                                        tint = colors.primary,
                                        onClick = { navController.navigate("favorites") }
                                    )
                                    
                                    ProfileListItem(
                                        icon = Icons.Outlined.LocalDining,
                                        title = "Diet & Nutrition",
                                        tint = colors.primary,
                                        onClick = { navController.navigate("profile/diet-preferences") }
                                    )
                                    
                                    ProfileListItem(
                                        icon = Icons.Outlined.Tune,
                                        title = "Appearance",
                                        tint = colors.primary,
                                        onClick = { navController.navigate("profile/appearance") },
                                        showDivider = false
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(28.dp))
                            
                            // Card 2: Account Settings
                            Text(
                                text = "Account Settings",
                                style = typography.titleMedium,
                                color = colors.onSurfaceVariant,
                                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                            )
                            
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = colors.surface),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {                                    
                                    ProfileListItem(
                                        icon = Icons.Outlined.Notifications,
                                        title = "Notifications",
                                        tint = colors.primary,
                                        onClick = { navController.navigate("profile/notifications") }
                                    )
                                    
                                    ProfileListItem(
                                        icon = Icons.Outlined.Security,
                                        title = "Privacy & Security",
                                        tint = colors.primary,
                                        onClick = { navController.navigate("profile/privacy") },
                                        showDivider = false
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(32.dp))
                            
                            // Standalone Log Out Button
                            OutlinedButton(
                                onClick = { showSignOutConfirmation = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp)
                                    .padding(bottom = 4.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = colors.error
                                ),
                                border = BorderStroke(1.dp, colors.error.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Logout,
                                        contentDescription = null,
                                        tint = colors.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Log out",
                                        style = typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileListItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    tint: Color,
    onClick: () -> Unit,
    showDivider: Boolean = true
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(20.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
        
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 64.dp, end = 16.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
        }
    }
}

// Remove unused components that don't match the new design
// ... existing code ...
