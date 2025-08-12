package com.thenewkenya.ingrediet.feature.authentication

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.thenewkenya.ingrediet.R
import com.thenewkenya.ingrediet.data.network.AuthManager
import com.thenewkenya.ingrediet.data.network.AuthResponse
import com.thenewkenya.ingrediet.data.network.AuthState
import kotlinx.coroutines.launch
import android.util.Patterns

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(navController: NavController) {
    var emailValue by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }
    val coroutineScope = rememberCoroutineScope()
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    
    // Email validation
    fun validateEmail(email: String): String? {
        return when {
            email.isEmpty() -> "Email is required"
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Invalid email format"
            else -> null
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Reset Password",
                        style = typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = "Back",
                            modifier = Modifier.size(20.dp)
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
                .background(colors.background)
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "App Logo",
                    modifier = Modifier.size(80.dp),
                    colorFilter = ColorFilter.tint(colors.onSurface.copy(alpha = 0.8f))
                )

                Text(
                    text = "Forgot Your Password?",
                    style = typography.headlineMedium,
                    color = colors.onBackground,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Enter your email address and we'll send you a link to reset your password.",
                    style = typography.bodyMedium,
                    color = colors.onBackground.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = emailValue,
                    onValueChange = { 
                        emailValue = it
                        emailError = null
                        errorMessage = null
                        successMessage = null
                    },
                    label = {
                        Text(
                            text = "Email Address",
                            style = typography.bodyMedium
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = colors.background,
                        focusedContainerColor = colors.background,
                        unfocusedIndicatorColor = if (emailError != null) colors.error else colors.outline,
                        focusedIndicatorColor = if (emailError != null) colors.error else colors.primary,
                        unfocusedLabelColor = colors.onSurfaceVariant,
                        focusedLabelColor = if (emailError != null) colors.error else colors.primary
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = emailError != null
                )
                
                // Email error message
                emailError?.let { error ->
                    Text(
                        text = error,
                        color = colors.error,
                        style = typography.bodySmall,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // General error message
                errorMessage?.let { message ->
                    Text(
                        text = message,
                        color = colors.error,
                        style = typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                // Success message
                successMessage?.let { message ->
                    Text(
                        text = message,
                        color = colors.primary,
                        style = typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        emailError = validateEmail(emailValue)
                        if (emailError == null) {
                            coroutineScope.launch {
                                isLoading = true
                                errorMessage = null
                                successMessage = null
                                
                                authManager.resetPassword(emailValue).collect { response ->
                                    when (response) {
                                        is AuthResponse.Loading -> {
                                            // Keep loading state
                                        }
                                        is AuthResponse.Success -> {
                                            isLoading = false
                                            successMessage = "Password reset email sent! Please check your inbox and follow the instructions to reset your password."
                                        }
                                        is AuthResponse.Error -> {
                                            isLoading = false
                                            errorMessage = response.message ?: "An error occurred while sending the reset email"
                                        }
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = colors.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            text = "Send Reset Email",
                            style = typography.titleMedium,
                            color = colors.onPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Remember your password?",
                    style = typography.bodyMedium,
                    color = colors.onBackground.copy(alpha = 0.7f)
                )
                
                Button(
                    onClick = { navController.navigateUp() },
                    colors = ButtonDefaults.textButtonColors(),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        text = "Back to Sign In",
                        style = typography.titleSmall,
                        color = colors.primary
                    )
                }
            }
        }
    }
} 