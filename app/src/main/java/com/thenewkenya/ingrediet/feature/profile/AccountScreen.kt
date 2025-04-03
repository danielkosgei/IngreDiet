package com.thenewkenya.ingrediet.feature.profile

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.thenewkenya.ingrediet.data.network.AuthManager
import com.thenewkenya.ingrediet.data.repository.ProfileRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(navController: NavController) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    
    // Initialize ViewModel
    val viewModel = remember { 
        ProfileViewModel(
            ProfileRepository(),
            AuthManager(context)
        ) 
    }
    val profile by viewModel.profile.collectAsState()
    
    // Dialog state
    var showSignOutConfirmation by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Account",
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
            // Profile Information Section
            Text(
                text = "Profile Information",
                style = typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onBackground,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            AccountItem(
                title = "Personal Information",
                subtitle = "${profile?.firstName ?: ""} ${profile?.lastName ?: ""}",
                icon = Icons.Outlined.Person,
                iconTint = colors.primary,
                onClick = { /* Navigate to personal info edit */ }
            )
            
            AccountItem(
                title = "Email Address",
                subtitle = profile?.email ?: "",
                icon = Icons.Filled.Email,
                iconTint = colors.tertiary,
                onClick = { /* Navigate to email edit */ }
            )
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = colors.outlineVariant.copy(alpha = 0.3f)
            )
            
            // Security Section
            Text(
                text = "Security",
                style = typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onBackground,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            AccountItem(
                title = "Change Password",
                subtitle = "Update your password for enhanced security",
                icon = Icons.Outlined.Lock,
                iconTint = colors.error,
                onClick = { /* Navigate to password change */ }
            )
            
            AccountItem(
                title = "Privacy Settings",
                subtitle = "Manage your privacy preferences",
                icon = Icons.Outlined.PrivacyTip,
                iconTint = colors.secondary,
                onClick = { /* Navigate to privacy settings */ }
            )
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = colors.outlineVariant.copy(alpha = 0.3f)
            )
            
            // Account Actions Section
            Text(
                text = "Account Actions",
                style = typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onBackground,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            // Sign Out Button
            AccountActionItem(
                title = "Sign Out",
                icon = Icons.AutoMirrored.Filled.Logout,
                iconTint = colors.primary,
                onClick = { showSignOutConfirmation = true }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
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
}

@Composable
fun AccountItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon with colorful background
        Surface(
            modifier = Modifier.size(40.dp),
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
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Title and subtitle
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = colors.onBackground
            )
            
            Text(
                text = subtitle,
                style = typography.bodySmall,
                color = colors.onSurfaceVariant
            )
        }
        
        // Edit icon
        IconButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Outlined.ManageAccounts,
                contentDescription = "Edit",
                modifier = Modifier.size(24.dp),
                tint = colors.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun AccountActionItem(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon with colorful background
        Surface(
            modifier = Modifier.size(40.dp),
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
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Title only
        Text(
            text = title,
            style = typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = colors.onBackground,
            modifier = Modifier.weight(1f)
        )
    }
} 