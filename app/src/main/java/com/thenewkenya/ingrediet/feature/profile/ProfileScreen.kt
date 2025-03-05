package com.thenewkenya.ingrediet.feature.profile

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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.thenewkenya.ingrediet.data.model.Profile
import com.thenewkenya.ingrediet.data.network.AuthManager
import com.thenewkenya.ingrediet.data.repository.ProfileRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }
    val profileRepository = remember { ProfileRepository() }

    // Create ViewModel
    val viewModel = remember { ProfileViewModel(profileRepository, authManager) }

    val profileState by viewModel.uiState.collectAsState()
    val profile by viewModel.profile.collectAsState()

    var isEditMode by remember { mutableStateOf(false) }

    // Local editable state for the profile
    var editableProfile by remember(profile) {
        mutableStateOf(profile?.copy() ?: Profile())
    }

    // Only fetch profile once when the screen is first composed
    LaunchedEffect(Unit) {
        viewModel.fetchProfile()
    }

    // Reset editable profile when profile changes
    LaunchedEffect(profile) {
        editableProfile = profile?.copy() ?: Profile()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    if (isEditMode) {
                        TextButton(
                            onClick = {
                                isEditMode = false
                                editableProfile = profile?.copy() ?: Profile()
                            }
                        ) {
                            Text("Cancel", color = MaterialTheme.colorScheme.primary)
                        }

                        Button(
                            onClick = {
                                isEditMode = false
                                profile?.let {
                                    viewModel.updateProfile(editableProfile)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Save")
                        }
                    } else {
                        Button(
                            onClick = { isEditMode = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Edit Profile")
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isEditMode) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "You're in edit mode. Make your changes and tap Save.",
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            when (profileState) {
                is ProfileUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }

                is ProfileUiState.Error -> {
                    val errorMessage = (profileState as ProfileUiState.Error).message
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Error: $errorMessage",
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.fetchProfile() },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }

                is ProfileUiState.Success -> {
                    profile?.let {
                        ProfileContent(
                            profile = if (isEditMode) editableProfile else it,
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
                    } ?: run {
                        Text(
                            text = "No profile data available",
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
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
    val scrollState = rememberScrollState()
    val backgroundColor = MaterialTheme.colorScheme.background
    val cardColor = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onBackground
    val accentColor = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .verticalScroll(scrollState)
    ) {
        // Profile picture
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(cardColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile Picture",
                    modifier = Modifier.size(60.dp),
                    tint = textColor.copy(alpha = 0.7f)
                )
            }

            // Add photo option when in edit mode
            if (isEditMode) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = (-16).dp, y = (-8).dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                        .clickable { /* Handle photo change */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Change Photo",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Profile information
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = cardColor),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Personal Information",
                    style = MaterialTheme.typography.titleMedium,
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                ProfileTextField(
                    label = "First Name",
                    value = profile.firstName,
                    onValueChange = { onProfileValueChange(profile.copy(firstName = it)) },
                    isEditable = isEditMode
                )

                Spacer(modifier = Modifier.height(16.dp))

                ProfileTextField(
                    label = "Last Name",
                    value = profile.lastName,
                    onValueChange = { onProfileValueChange(profile.copy(lastName = it)) },
                    isEditable = isEditMode
                )

                Spacer(modifier = Modifier.height(16.dp))

                ProfileTextField(
                    label = "Email",
                    value = profile.email,
                    onValueChange = { },
                    isEditable = false
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Dietary preferences
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = cardColor),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Diet & Nutrition",
                    style = MaterialTheme.typography.titleMedium,
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                ProfileTextField(
                    label = "Weight Goal",
                    value = profile.weightGoal,
                    onValueChange = { onProfileValueChange(profile.copy(weightGoal = it)) },
                    isEditable = isEditMode
                )

                Spacer(modifier = Modifier.height(16.dp))

                ProfileTextField(
                    label = "Daily Calorie Target",
                    value = profile.calorieTarget.toString(),
                    onValueChange = {
                        val value = it.toIntOrNull() ?: 0
                        onProfileValueChange(profile.copy(calorieTarget = value))
                    },
                    isEditable = isEditMode,
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                )

                if (isEditMode) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Dietary Preferences (coming soon)",
                        color = textColor.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Sign Out Button
        Button(
            onClick = onSignOut,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
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
                    style = MaterialTheme.typography.bodyLarge
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
    val textColor = MaterialTheme.colorScheme.onBackground
    val borderColor = MaterialTheme.colorScheme.primary
    val placeholderColor = textColor.copy(alpha = 0.3f)
    val fieldBackground = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)

    Column {
        Text(
            text = label,
            color = placeholderColor,
            style = MaterialTheme.typography.bodySmall
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
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedBorderColor = placeholderColor,
                    focusedBorderColor = borderColor
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                textStyle = androidx.compose.ui.text.TextStyle(color = textColor)
            )
        } else {
            Text(
                text = value.ifEmpty { "Not set" },
                color = if (value.isEmpty()) placeholderColor else textColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(fieldBackground, shape = RoundedCornerShape(8.dp))
                    .padding(16.dp)
            )
        }
    }
}
