package com.thenewkenya.ingrediet.feature.profile

import android.util.Log
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
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocalDining
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Report
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
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
                if (!isEditMode) {
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
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = "Back",
                            modifier = Modifier.size(20.dp)
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
                        },
                        modifier = Modifier
                            .padding(8.dp)
                            .size(40.dp)
                    ) {
                        Icon(
                            imageVector = if (isEditMode) Icons.Default.Check else Icons.Default.Edit,
                            contentDescription = if (isEditMode) "Save" else "Edit",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.surface,
                    scrolledContainerColor = colors.surface
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
                    ) {
                        // Profile header with elegant styling
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 32.dp, bottom = 24.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Animated profile image with shadow and border
                                Surface(
                                    shape = CircleShape,
                                    shadowElevation = 4.dp,
                                    modifier = Modifier
                                        .size(120.dp)
                                        .clip(CircleShape)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(colors.surfaceVariant)
                                            .border(2.dp, colors.primary.copy(alpha = 0.2f), CircleShape)
                                            .clip(CircleShape),
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
                                                tint = colors.primary.copy(alpha = 0.6f),
                                                modifier = Modifier.size(60.dp)
                                            )
                                        }
                                        
                                        // Edit icon overlay with ripple effect
                                        if (isEditMode) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(colors.surface.copy(alpha = 0.7f))
                                                    .clickable(
                                                        interactionSource = remember { MutableInteractionSource() },
                                                        indication = androidx.compose.material.ripple.rememberRipple(
                                                            bounded = true,
                                                            color = colors.primary
                                                        )
                                                    ) { 
                                                        // Image picker would go here
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Camera,
                                                    contentDescription = "Change profile picture",
                                                    tint = colors.primary,
                                                    modifier = Modifier.size(36.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(20.dp))
                                
                                // User name display with refined typography
                                if (!isEditMode) {
                                    Text(
                                        text = "${currentProfile.firstName} ${currentProfile.lastName}",
                                        style = typography.headlineMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = (-0.5).sp
                                        ),
                                        color = colors.onBackground,
                                        textAlign = TextAlign.Center
                                    )
                                    
                                    Spacer(modifier = Modifier.height(4.dp))
                                    
                                    Text(
                                        text = currentProfile.email,
                                        style = typography.bodyMedium,
                                        color = colors.onBackground.copy(alpha = 0.7f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                        
                        // Elegant divider with fade effect
                        HorizontalDivider(
                            thickness = 1.dp, 
                            color = colors.outlineVariant.copy(alpha = 0.4f),
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        if (isEditMode) {
                            // Animated entry for edit mode fields
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(tween(300)) + slideInVertically(
                                    initialOffsetY = { it / 2 },
                                    animationSpec = spring(stiffness = 300f)
                                )
                            ) {
                                // Edit Mode: Show editable fields
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Text(
                                        text = "Personal Information",
                                        style = typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.onBackground
                                    )
                                    
                                    ProfileTextField(
                                        label = "First Name",
                                        value = editableProfile?.firstName ?: "",
                                        onValueChange = { editableProfile = editableProfile?.copy(firstName = it) },
                                        isEditable = true,
                                        leadingIcon = null
                                    )
                                    
                                    ProfileTextField(
                                        label = "Last Name",
                                        value = editableProfile?.lastName ?: "",
                                        onValueChange = { editableProfile = editableProfile?.copy(lastName = it) },
                                        isEditable = true,
                                        leadingIcon = null
                                    )
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Text(
                                        text = "Dietary Preferences",
                                        style = typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.onBackground
                                    )
                                    
                                    ProfileTextField(
                                        label = "Dietary Preferences",
                                        value = editableProfile?.dietaryPreferences?.joinToString(", ") ?: "",
                                        onValueChange = { 
                                            val preferences = it.split(",").map { pref -> pref.trim() }.filter { pref -> pref.isNotEmpty() }
                                            editableProfile = editableProfile?.copy(dietaryPreferences = preferences)
                                        },
                                        isEditable = true,
                                        leadingIcon = Icons.Outlined.LocalDining,
                                        helperText = "Separate multiple preferences with commas"
                                    )
                                    
                                    ProfileTextField(
                                        label = "Allergies",
                                        value = editableProfile?.allergies?.joinToString(", ") ?: "",
                                        onValueChange = { 
                                            val allergies = it.split(",").map { allergy -> allergy.trim() }.filter { allergy -> allergy.isNotEmpty() }
                                            editableProfile = editableProfile?.copy(allergies = allergies)
                                        },
                                        isEditable = true,
                                        leadingIcon = null,
                                        helperText = "Separate multiple allergies with commas"
                                    )
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Text(
                                        text = "Health Goals",
                                        style = typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.onBackground
                                    )
                                    
                                    ProfileTextField(
                                        label = "Weight Goal",
                                        value = editableProfile?.weightGoal ?: "",
                                        onValueChange = { editableProfile = editableProfile?.copy(weightGoal = it) },
                                        isEditable = true,
                                        leadingIcon = null
                                    )
                                    
                                    ProfileTextField(
                                        label = "Daily Calorie Target",
                                        value = (editableProfile?.calorieTarget ?: 0).toString(),
                                        onValueChange = { 
                                            val calories = it.toIntOrNull() ?: 0
                                            editableProfile = editableProfile?.copy(calorieTarget = calories)
                                        },
                                        isEditable = true,
                                        leadingIcon = null,
                                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                    )
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        TextButton(
                                            onClick = { isEditMode = false }
                                        ) {
                                            Text("Cancel")
                                        }
                                        
                                        Spacer(modifier = Modifier.width(8.dp))
                                        
                                        Button(
                                            onClick = {
                                                editableProfile?.let { viewModel.updateProfile(it) }
                                                isEditMode = false
                                            }
                                        ) {
                                            Text("Save")
                                        }
                                    }
                                }
                            }
                        } else {
                            // Enhanced navigation sections with smooth animations
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            ) {
                                // Edit Profile Section with improved visuals
                                ProfileNavigationItem(
                                    title = "Edit Profile",
                                    icon = Icons.Default.Edit,
                                    iconTint = colors.primary,
                                    onClick = { isEditMode = true },
                                    content = {
                                        // Only show preview if there's actual content
                                        if (currentProfile.dietaryPreferences.isNotEmpty() || 
                                            currentProfile.allergies.isNotEmpty() ||
                                            currentProfile.weightGoal.isNotEmpty() ||
                                            currentProfile.calorieTarget > 0) {
                                            
                                            Column(modifier = Modifier.padding(start = 64.dp, end = 16.dp, bottom = 16.dp, top = 4.dp)) {
                                                // Preview of dietary preferences with enhanced styling
                                                if (currentProfile.dietaryPreferences.isNotEmpty()) {
                                                    EnhancedInfoItem(
                                                        label = "Dietary Preferences", 
                                                        value = currentProfile.dietaryPreferences.joinToString(", "),
                                                        icon = Icons.Outlined.LocalDining,
                                                        iconColor = colors.primary.copy(alpha = 0.6f)
                                                    )
                                                }
                                                
                                                // Preview of allergies
                                                if (currentProfile.allergies.isNotEmpty()) {
                                                    EnhancedInfoItem(
                                                        label = "Allergies", 
                                                        value = currentProfile.allergies.joinToString(", "),
                                                        icon = null
                                                    )
                                                }
                                                
                                                // Preview of health goals with better organization
                                                if (currentProfile.weightGoal.isNotEmpty() || currentProfile.calorieTarget > 0) {
                                                    if (currentProfile.weightGoal.isNotEmpty()) {
                                                        EnhancedInfoItem(
                                                            label = "Weight Goal", 
                                                            value = currentProfile.weightGoal,
                                                            icon = Icons.Outlined.EmojiEvents,
                                                            iconColor = colors.tertiary.copy(alpha = 0.6f)
                                                        )
                                                    }
                                                    
                                                    if (currentProfile.calorieTarget > 0) {
                                                        EnhancedInfoItem(
                                                            label = "Daily Calories", 
                                                            value = "${currentProfile.calorieTarget} kcal",
                                                            icon = null
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                )
                                
                                // Enhanced settings section
                                ProfileNavigationItem(
                                    title = "Settings",
                                    icon = Icons.Outlined.Settings,
                                    subtitle = "App preferences, notifications & privacy",
                                    iconTint = colors.secondary,
                                    onClick = { navController.navigate("profile/settings") }
                                )
                                
                                // Enhanced support section
                                ProfileNavigationItem(
                                    title = "Support",
                                    icon = Icons.AutoMirrored.Outlined.Help,
                                    subtitle = "Help center & feedback",
                                    iconTint = colors.tertiary,
                                    onClick = { navController.navigate("profile/support") }
                                )
                                
                                // Enhanced account actions section
                                ProfileNavigationItem(
                                    title = "Account",
                                    icon = Icons.Outlined.ManageAccounts,
                                    subtitle = "Manage your account & privacy",
                                    iconTint = colors.primary,
                                    onClick = { navController.navigate("profile/account") },
                                    content = {
                                        Column(modifier = Modifier.padding(start = 64.dp, end = 16.dp, bottom = 16.dp, top = 4.dp)) {
                                            // Enhanced sign out option
                                            Surface(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .clickable(
                                                        interactionSource = remember { MutableInteractionSource() },
                                                        indication = androidx.compose.material.ripple.rememberRipple(
                                                            bounded = true,
                                                            color = colors.error
                                                        )
                                                    ) { showSignOutConfirmation = true },
                                                color = colors.errorContainer.copy(alpha = 0.1f)
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(12.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.AutoMirrored.Filled.Logout,
                                                        contentDescription = null,
                                                        tint = colors.error,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                    
                                                    Text(
                                                        text = "Sign Out",
                                                        style = typography.labelLarge,
                                                        color = colors.error,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                            }
                                        }
                                    }
                                )
                                
                                Spacer(modifier = Modifier.height(40.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileNavigationItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    onClick: () -> Unit,
    subtitle: String? = null,
    content: @Composable (() -> Unit)? = null
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val interactionSource = remember { MutableInteractionSource() }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Surface with subtle hover effect and rounded corners
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable(
                    interactionSource = interactionSource,
                    indication = androidx.compose.material.ripple.rememberRipple(bounded = true)
                ) { onClick() },
            color = colors.surfaceVariant.copy(alpha = 0.1f),
            shadowElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon with colorful background
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    color = iconTint.copy(alpha = 0.1f)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(20.dp))
                
                // Title and optional subtitle
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onBackground
                    )
                    
                    if (subtitle != null) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = subtitle,
                            style = typography.bodySmall,
                            color = colors.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                // Arrow indicator
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = colors.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        // Animated content expansion
        AnimatedVisibility(
            visible = content != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut()
        ) {
            Column {
                content?.invoke()
            }
        }
        
        // Subtle divider
        if (content == null) {
            HorizontalDivider(
                color = colors.outlineVariant.copy(alpha = 0.2f),
                thickness = 0.5.dp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
fun EnhancedInfoItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    iconColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        Column {
            Text(
                text = label,
                style = typography.labelSmall,
                color = colors.onSurfaceVariant,
                letterSpacing = 0.4.sp
            )
            
            Spacer(modifier = Modifier.height(2.dp))
            
            Text(
                text = value,
                style = typography.bodyMedium,
                color = colors.onSurface,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// Keep the ProfileTextField component for edit mode
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
        },
        shape = RoundedCornerShape(12.dp)
    )
}
