package com.thenewkenya.ingrediet.feature.security

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.thenewkenya.ingrediet.R
import com.thenewkenya.ingrediet.data.network.AppLockManager
import com.thenewkenya.ingrediet.data.network.BiometricAuthManager
import com.thenewkenya.ingrediet.data.network.BiometricAuthResult
import com.thenewkenya.ingrediet.data.network.BiometricAvailability
import android.util.Log

@Composable
fun AppLockScreen(
    appLockManager: AppLockManager,
    biometricAuthManager: BiometricAuthManager,
    onUnlocked: () -> Unit = {},
    onReloginRequested: () -> Unit = {}
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val context = LocalContext.current
    
    // State for error messages
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showRetryButton by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    
    // Observe authentication results
    val authResult by appLockManager.appLockAuthResult.collectAsState()
    val biometricAvailability = remember { biometricAuthManager.isBiometricAvailable() }
    
    // Handle authentication results
    LaunchedEffect(authResult) {
        when (authResult) {
            is BiometricAuthResult.Success -> {
                Log.d("AppLockScreen", "Authentication successful, unlocking app")
                onUnlocked()
            }
            is BiometricAuthResult.Failed -> {
                Log.d("AppLockScreen", "Authentication failed")
                errorMessage = "Authentication failed. Please try again."
                showRetryButton = true
            }
            is BiometricAuthResult.Error -> {
                val errorResult = authResult as BiometricAuthResult.Error
                Log.d("AppLockScreen", "Authentication error: ${errorResult.message}")
                errorMessage = errorResult.message
                showRetryButton = true
            }
            null -> {
                // Initial state or reset
                errorMessage = null
                showRetryButton = false
            }
        }
    }
    
    // Auto-trigger biometric authentication when screen appears
    LaunchedEffect(Unit) {
        if (biometricAvailability == BiometricAvailability.AVAILABLE) {
            val activity = context as? FragmentActivity
            if (activity != null) {
                biometricAuthManager.authenticate(
                    activity = activity,
                    title = "Unlock IngreDiet",
                    subtitle = "Use your fingerprint to access the app"
                )
            }
        }
    }
    
    // Listen to biometric results and forward to app lock manager
    LaunchedEffect(Unit) {
        biometricAuthManager.authResults.collect { result ->
            appLockManager.handleAuthResult(result)
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            // App logo or lock icon
            if (biometricAvailability == BiometricAvailability.AVAILABLE) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "App Logo",
                    modifier = Modifier.size(120.dp),
                    colorFilter = ColorFilter.tint(colors.primary)
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = "Locked",
                    modifier = Modifier.size(120.dp),
                    tint = colors.primary
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "IngreDiet",
                style = typography.headlineLarge,
                color = colors.onBackground,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = if (biometricAvailability == BiometricAvailability.AVAILABLE) {
                    "App is locked for your privacy"
                } else {
                    "Fingerprint authentication is not available"
                },
                style = typography.bodyLarge,
                color = colors.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (biometricAvailability == BiometricAvailability.AVAILABLE) {
                Text(
                    text = "Use your fingerprint to unlock",
                    style = typography.bodyMedium,
                    color = colors.onBackground.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    text = biometricAuthManager.getAvailabilityMessage(biometricAvailability),
                    style = typography.bodyMedium,
                    color = colors.error,
                    textAlign = TextAlign.Center
                )
            }
            
            // Error message
            errorMessage?.let { message ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = message,
                    style = typography.bodyMedium,
                    color = colors.error,
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Retry button for authentication
            if (showRetryButton && biometricAvailability == BiometricAvailability.AVAILABLE) {
                Button(
                    onClick = {
                        errorMessage = null
                        showRetryButton = false
                        val activity = context as? FragmentActivity
                        if (activity != null) {
                            biometricAuthManager.authenticate(
                                activity = activity,
                                title = "Unlock IngreDiet",
                                subtitle = "Use your fingerprint to access the app"
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "Try Again",
                        style = typography.titleMedium,
                        color = colors.onPrimary
                    )
                }
            }
            
            // Option to get help if user has trouble with fingerprint
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(
                    onClick = {
                        showHelpDialog = true
                    }
                ) {
                    Text(
                        text = "Need help?",
                        style = typography.bodyMedium,
                        color = colors.primary
                    )
                }
            }
            
            // Help dialog with relogin option
            if (showHelpDialog) {
                AlertDialog(
                    onDismissRequest = { showHelpDialog = false },
                    title = {
                        Text(
                            text = "Can't Access Fingerprint?",
                            style = typography.headlineSmall
                        )
                    },
                    text = {
                        Text(
                            text = "If you're having trouble with fingerprint authentication, you can sign out and log back into your account. This will temporarily disable app lock so you can access your account and re-enable it later in Privacy & Security settings.",
                            style = typography.bodyMedium
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showHelpDialog = false
                                onReloginRequested()
                            }
                        ) {
                            Text("Sign Out & Re-login")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showHelpDialog = false }
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
} 