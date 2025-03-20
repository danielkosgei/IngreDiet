package com.thenewkenya.ingrediet.feature.profile

import android.util.Log
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
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

    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    LaunchedEffect(Unit) {
        viewModel.fetchProfile()
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is ProfileUiState.Success -> {
                profile = (uiState as ProfileUiState.Success).profile
                editableProfile = profile
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Top bar with back button and edit/save buttons
            TopAppBar(
                title = {
                    Text(
                        text = "Profile",
                        style = typography.titleLarge,
                        color = colors.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = "Back",
                            tint = colors.onBackground
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
                            contentDescription = if (isEditMode) "Save" else "Edit",
                            tint = colors.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )

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
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                                color = colors.error
                            )
                            Button(
                                onClick = { viewModel.fetchProfile() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.primary
                                )
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }

                is ProfileUiState.Success -> {
                    val currentProfile = (uiState as ProfileUiState.Success).profile
                    ProfileContent(
                        profile = currentProfile,
                        isEditMode = isEditMode,
                        onProfileValueChange = { updatedProfile ->
                            if (isEditMode) {
                                editableProfile = updatedProfile
                            }
                        },
                        onSignOut = {
                            viewModel.signOut()
                            navController.navigate("login") {
                                popUpTo("home") { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileContent(
    profile: Profile,
    isEditMode: Boolean,
    onProfileValueChange: (Profile) -> Unit,
    onSignOut: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    
    // Get the current user to access metadata
    val currentUser = supabase.auth.currentUserOrNull()
    
    // Try to extract profile picture URL from user metadata
    var profileImageUrl by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(currentUser) {
        try {
            // Try different approaches to extract the avatar URL
            val metadata = currentUser?.userMetadata
            val jsonMetadata = metadata?.toString()
            
            // Log the raw metadata for debugging
            Log.d("ProfileScreen", "Raw metadata: $jsonMetadata")
            
            // Try to extract using regex
            if (jsonMetadata != null) {
                val regex = "\"avatar_url\"\\s*:\\s*\"([^\"]+)\"".toRegex()
                val matchResult = regex.find(jsonMetadata)
                profileImageUrl = matchResult?.groupValues?.getOrNull(1)
                
                Log.d("ProfileScreen", "Extracted profile image URL: $profileImageUrl")
            }
        } catch (e: Exception) {
            Log.e("ProfileScreen", "Failed to get profile image URL", e)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Profile Picture - Now with AsyncImage if URL is available
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(colors.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (profileImageUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(profileImageUrl)
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
        }

        // Profile Fields
        ProfileTextField(
            label = "First Name",
            value = profile.firstName,
            onValueChange = { onProfileValueChange(profile.copy(firstName = it)) },
            isEditable = isEditMode
        )

        ProfileTextField(
            label = "Last Name",
            value = profile.lastName,
            onValueChange = { onProfileValueChange(profile.copy(lastName = it)) },
            isEditable = isEditMode
        )

        ProfileTextField(
            label = "Email",
            value = profile.email,
            onValueChange = { onProfileValueChange(profile.copy(email = it)) },
            isEditable = isEditMode
        )

        Spacer(modifier = Modifier.weight(1f))

        // Sign Out Button
        Button(
            onClick = onSignOut,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.error
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "Sign Out"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sign Out",
                    style = typography.bodyLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun ProfileTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isEditable: Boolean,
    keyboardType: androidx.compose.ui.text.input.KeyboardType = androidx.compose.ui.text.input.KeyboardType.Text
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Column {
        Text(
            text = label,
            color = colors.onBackground.copy(alpha = 0.7f),
            style = typography.bodySmall
        )

        Spacer(modifier = Modifier.height(4.dp))

        if (isEditable) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = keyboardType
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = colors.surfaceVariant,
                    focusedContainerColor = colors.surfaceVariant,
                    unfocusedBorderColor = colors.onSurfaceVariant,
                    focusedBorderColor = colors.primary
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                textStyle = typography.bodyLarge.copy(color = colors.onBackground)
            )
        } else {
            Text(
                text = value.ifEmpty { "Not set" },
                color = if (value.isEmpty()) colors.onBackground.copy(alpha = 0.3f) else colors.onBackground,
                style = typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surfaceVariant.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    .padding(16.dp)
            )
        }
    }
}
