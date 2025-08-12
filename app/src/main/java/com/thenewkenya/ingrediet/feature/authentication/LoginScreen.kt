package com.thenewkenya.ingrediet.feature.authentication

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults


import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.thenewkenya.ingrediet.R
import com.thenewkenya.ingrediet.data.network.AuthManager
import com.thenewkenya.ingrediet.data.network.AuthResponse
import com.thenewkenya.ingrediet.data.network.AuthState
import com.thenewkenya.ingrediet.data.network.supabase
import io.github.jan.supabase.auth.exception.AuthErrorCode
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout

import androidx.compose.ui.graphics.ColorFilter

enum class LoginError {
    EMPTY_FIELDS,
    INVALID_EMAIL,
    WRONG_PASSWORD,
    NETWORK_ERROR,
    TOO_MANY_REQUESTS,
    UNKNOWN_ERROR
}

@Composable
fun LoginScreen(navController: NavController) {
    var emailValue by remember { mutableStateOf("") }
    var passwordValue by remember { mutableStateOf("") }
    var passwordVisibility by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var errorType by remember { mutableStateOf<LoginError?>(null) }
    val context = LocalContext.current
    var authManager by remember { mutableStateOf(AuthManager(context)) }
    val coroutineScope = rememberCoroutineScope()
    var authState by remember { mutableStateOf<AuthState>(AuthState.Success) }
    var isGoogleSignInLoading by remember { mutableStateOf(false) }
    var isOnline by remember { mutableStateOf(true) }

    LaunchedEffect(emailValue, passwordValue) {
        errorMessage = null
        errorType = null
    }

    LaunchedEffect(Unit) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        isOnline = activeNetwork?.isConnectedOrConnecting == true

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                isOnline = true
            }
            override fun onLost(network: Network) {
                isOnline = false
            }
        }
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }

    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Logo
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "App Logo",
                modifier = Modifier.size(80.dp),
                colorFilter = ColorFilter.tint(colors.onSurface.copy(alpha = 0.8f))
            )
            
            Text(
                text = "Sign In",
                style = typography.headlineMedium,
                color = colors.onBackground,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Welcome back to IngreDiet",
                style = typography.bodyMedium,
                color = colors.onBackground.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Email TextField
            TextField(
                value = emailValue,
                onValueChange = { emailValue = it },
                label = {
                    Text(
                        text = "Email",
                        style = typography.bodyMedium
                    )
                },
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = colors.background,
                    focusedContainerColor = colors.background,
                    unfocusedIndicatorColor = colors.outline,
                    focusedIndicatorColor = colors.primary,
                    unfocusedLabelColor = colors.onSurfaceVariant,
                    focusedLabelColor = colors.primary
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Password TextField
            TextField(
                value = passwordValue,
                onValueChange = { passwordValue = it },
                label = {
                    Text(
                        text = "Password",
                        style = typography.bodyMedium
                    )
                },
                visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                        Icon(
                            imageVector = if (passwordVisibility) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (passwordVisibility) "Hide password" else "Show password"
                        )
                    }
                },
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = colors.background,
                    focusedContainerColor = colors.background,
                    unfocusedIndicatorColor = colors.outline,
                    focusedIndicatorColor = colors.primary,
                    unfocusedLabelColor = colors.onSurfaceVariant,
                    focusedLabelColor = colors.primary
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = colors.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            // Forgot Password Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = {
                        if (!isOnline) {
                            errorMessage = "No internet connection"
                            errorType = LoginError.NETWORK_ERROR
                            return@TextButton
                        }
                        
                        if (emailValue.isEmpty()) {
                            errorMessage = "Please enter your email address"
                            errorType = LoginError.EMPTY_FIELDS
                            return@TextButton
                        }
                        
                        coroutineScope.launch {
                            authState = AuthState.Loading
                            authManager.resetPassword(emailValue).collect { response ->
                                when (response) {
                                    is AuthResponse.Success -> {
                                        Toast.makeText(
                                            context,
                                            "Password reset email sent. Please check your inbox.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        authState = AuthState.Success
                                    }
                                    is AuthResponse.Error -> {
                                        authState = AuthState.Error(response.message)
                                        errorMessage = response.message
                                        errorType = LoginError.UNKNOWN_ERROR
                                    }
                                    else -> {}
                                }
                            }
                        }
                    },
                    modifier = Modifier.padding(vertical = 0.dp)
                ) {
                    Text(
                        text = "Forgot Password?",
                        style = typography.bodyMedium,
                        color = colors.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sign In Button
            Button(
                onClick = {
                    Log.d("LoginDebug", "Login button clicked")
                    coroutineScope.launch {
                        try {
                            withTimeout(10_000) {
                                if (!isOnline) {
                                    Log.d("LoginDebug", "No internet connection")
                                    errorMessage = "No internet connection"
                                    errorType = LoginError.NETWORK_ERROR
                                    return@withTimeout
                                }

                                if (emailValue.isEmpty() || passwordValue.isEmpty()) {
                                    Log.d("LoginDebug", "Empty fields detected")
                                    errorMessage = "Please fill in all fields"
                                    errorType = LoginError.EMPTY_FIELDS
                                    return@withTimeout
                                }

                                Log.d("LoginDebug", "Attempting authentication")
                                authState = AuthState.Loading
                                val response = authManager.signInWithEmail(emailValue, passwordValue)
                                response.collect { authResponse ->
                                    when (authResponse) {
                                        is AuthResponse.Success -> {
                                            Log.d("LoginDebug", "Authentication successful")
                                            authState = AuthState.Success
                                            Log.d("LoginDebug", "Navigating to home")
                                            navController.navigate("home") {
                                                popUpTo("login") { inclusive = true }
                                            }
                                        }
                                        is AuthResponse.Error -> {
                                            Log.d("LoginDebug", "Authentication failed: ${authResponse.message}")
                                            authState = AuthState.Error(authResponse.message)
                                            errorMessage = authResponse.message
                                            errorType = when {
                                                authResponse.message?.contains("Invalid login credentials") == true -> 
                                                    LoginError.WRONG_PASSWORD
                                                authResponse.message?.contains("Invalid email") == true -> 
                                                    LoginError.INVALID_EMAIL
                                                authResponse.message?.contains("Too many requests") == true -> 
                                                    LoginError.TOO_MANY_REQUESTS
                                                else -> LoginError.UNKNOWN_ERROR
                                            }
                                        }
                                        else -> {}
                                    }
                                }
                            }
                        } catch (e: TimeoutCancellationException) {
                            Log.d("LoginDebug", "Authentication timed out")
                            errorMessage = "Login timed out. Please try again."
                            errorType = LoginError.NETWORK_ERROR
                        } catch (e: Exception) {
                            Log.d("LoginDebug", "Unexpected error: ${e.message}")
                            errorMessage = "An unexpected error occurred"
                            errorType = LoginError.UNKNOWN_ERROR
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
                enabled = authState != AuthState.Loading
            ) {
                if (authState == AuthState.Loading) {
                    CircularProgressIndicator(
                        color = colors.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = "Sign In",
                        style = typography.titleMedium,
                        color = colors.onPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // OR Divider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(colors.onBackground.copy(alpha = 0.1f))
                )
                
                Text(
                    text = "  OR  ",
                    style = typography.bodyMedium,
                    color = colors.onBackground.copy(alpha = 0.5f)
                )
                
                Spacer(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(colors.onBackground.copy(alpha = 0.1f))
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Social Sign In Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Google Sign In Button
                OutlinedButton(
                onClick = {
                    isGoogleSignInLoading = true
                    coroutineScope.launch {
                        authManager.loginGoogleuser().collect { response ->
                            when (response) {
                                is AuthResponse.Loading -> {
                                    // Keep showing loading state
                                }
                                is AuthResponse.Success -> {
                                    isGoogleSignInLoading = false
                                    navController.navigate("home") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                                is AuthResponse.Error -> {
                                    isGoogleSignInLoading = false
                                    errorMessage = response.message ?: "Failed to sign in with Google"
                                    errorType = LoginError.UNKNOWN_ERROR
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                enabled = !isGoogleSignInLoading,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = colors.surface,
                    contentColor = colors.onSurface
                ),
                border = BorderStroke(1.dp, colors.outline)
            ) {
                if (isGoogleSignInLoading) {
                    CircularProgressIndicator(
                        color = colors.primary,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_google),
                            contentDescription = "Google Logo",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Google",
                            style = typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            // Apple Sign In Button
            OutlinedButton(
                onClick = {
                    // Coming soon functionality
                    android.widget.Toast.makeText(context, "Coming soon", android.widget.Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = colors.surface,
                    contentColor = colors.onSurface
                ),
                border = BorderStroke(1.dp, colors.outline)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Apple logo
                    AppleLogo(
                        modifier = Modifier.size(20.dp),
                        color = colors.onSurface
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Apple",
                        style = typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sign Up Link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Don't have an account? ",
                    style = typography.bodyMedium,
                    color = colors.onBackground.copy(alpha = 0.7f)
                )
                TextButton(
                    onClick = { navController.navigate("register") },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = "Sign Up",
                        style = typography.titleSmall,
                        color = colors.primary
                    )
                }
            }
        }
    }
}


