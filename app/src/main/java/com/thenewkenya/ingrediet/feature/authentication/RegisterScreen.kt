package com.thenewkenya.ingrediet.feature.authentication

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.util.Log
import android.util.Patterns
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults


import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Stable
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
import com.thenewkenya.ingrediet.data.network.UserPreferencesManager
import io.github.jan.supabase.auth.exception.AuthErrorCode
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.onEach
import androidx.compose.ui.graphics.ColorFilter
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout

enum class PasswordStrength {
    WEAK, MEDIUM, STRONG
}

// Password strength calculator
private fun calculatePasswordStrength(password: String): PasswordStrength {
    if (password.length < 8) {
        return PasswordStrength.WEAK
    }
    
    var hasUppercase = false
    var hasLowercase = false
    var hasDigit = false
    var hasSpecialChar = false
    
    for (char in password) {
        when {
            char.isUpperCase() -> hasUppercase = true
            char.isLowerCase() -> hasLowercase = true
            char.isDigit() -> hasDigit = true
            !char.isLetterOrDigit() -> hasSpecialChar = true
        }
    }
    
    val score = listOf(hasUppercase, hasLowercase, hasDigit, hasSpecialChar).count { it }
    
    return when {
        score <= 2 -> PasswordStrength.WEAK
        score == 3 -> PasswordStrength.MEDIUM
        else -> PasswordStrength.STRONG
    }
}

@Composable
fun RegisterScreen(navController: NavController) {
    var emailValue by remember { mutableStateOf("") }
    var passwordValue by remember { mutableStateOf("") }
    var confirmPasswordValue by remember { mutableStateOf("") }
    var passwordVisibility by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var errorType by remember { mutableStateOf<LoginError?>(null) }
    val context = LocalContext.current
    var authManager by remember { mutableStateOf(AuthManager(context)) }
    val coroutineScope = rememberCoroutineScope()
    var authState by remember { mutableStateOf<AuthState>(AuthState.Success) }
    var isGoogleSignInLoading by remember { mutableStateOf(false) }
    var isOnline by remember { mutableStateOf(true) }
    var passwordStrength by remember { mutableStateOf<PasswordStrength?>(null) }
    // Password match state
    var passwordsMatch by remember { mutableStateOf<Boolean?>(null) }
    // Analytics consent state
    var analyticsConsentChecked by remember { mutableStateOf(false) } // Default to unchecked
    
    // Real-time validation states
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    
    // Validation functions
    fun validateEmail(email: String): String? {
        return when {
            email.isEmpty() -> null // Don't show error for empty field initially
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Invalid email format"
            else -> null
        }
    }
    
    fun validatePassword(password: String): String? {
        return when {
            password.isEmpty() -> null // Don't show error for empty field initially
            password.length < 8 -> "Password must be at least 8 characters"
            !password.any { it.isUpperCase() } -> "Password must contain an uppercase letter"
            !password.any { it.isLowerCase() } -> "Password must contain a lowercase letter"
            !password.any { it.isDigit() } -> "Password must contain a number"
            !password.any { !it.isLetterOrDigit() } -> "Password must contain a special character"
            else -> null
        }
    }
    
    fun validateConfirmPassword(password: String, confirmPassword: String): String? {
        return when {
            confirmPassword.isEmpty() -> null // Don't show error for empty field initially
            confirmPassword != password -> "Passwords do not match"
            else -> null
        }
    }
    
    // Real-time validation
    LaunchedEffect(emailValue) {
        if (emailValue.isNotEmpty()) {
            emailError = validateEmail(emailValue)
        } else {
            emailError = null
        }
        errorMessage = null
        errorType = null
    }
    
    LaunchedEffect(passwordValue) {
        if (passwordValue.isNotEmpty()) {
            passwordError = validatePassword(passwordValue)
            passwordStrength = calculatePasswordStrength(passwordValue)
            // Re-validate confirm password when password changes
            if (confirmPasswordValue.isNotEmpty()) {
                passwordsMatch = confirmPasswordValue == passwordValue
                confirmPasswordError = validateConfirmPassword(passwordValue, confirmPasswordValue)
            }
        } else {
            passwordError = null
            passwordStrength = null
        }
        errorMessage = null
        errorType = null
    }
    
    LaunchedEffect(confirmPasswordValue) {
        if (confirmPasswordValue.isNotEmpty()) {
            passwordsMatch = confirmPasswordValue == passwordValue
            confirmPasswordError = validateConfirmPassword(passwordValue, confirmPasswordValue)
        } else {
            passwordsMatch = null
            confirmPasswordError = null
        }
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
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "App Logo",
                modifier = Modifier.size(80.dp),
                colorFilter = ColorFilter.tint(colors.onSurface.copy(alpha = 0.8f))
            )

            Text(
                text = "Create Account",
                style = typography.headlineMedium,
                color = colors.onBackground,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Sign up to get started with IngreDiet",
                style = typography.bodyMedium,
                color = colors.onBackground.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(8.dp))

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
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }

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
                    unfocusedIndicatorColor = if (passwordError != null) colors.error else colors.outline,
                    focusedIndicatorColor = if (passwordError != null) colors.error else colors.primary,
                    unfocusedLabelColor = colors.onSurfaceVariant,
                    focusedLabelColor = if (passwordError != null) colors.error else colors.primary
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = passwordError != null
            )
            
            // Password error message
            passwordError?.let { error ->
                Text(
                    text = error,
                    color = colors.error,
                    style = typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }

            // Password Strength Indicator
            if (passwordValue.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Password Strength: ",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurface.copy(alpha = 0.7f)
                    )
                    
                    val strengthColor = when (passwordStrength) {
                        PasswordStrength.WEAK -> colors.error
                        PasswordStrength.MEDIUM -> colors.tertiary
                        PasswordStrength.STRONG -> colors.primary
                        null -> colors.onSurface.copy(alpha = 0.7f)
                    }
                    
                    val strengthText = when (passwordStrength) {
                        PasswordStrength.WEAK -> "Weak"
                        PasswordStrength.MEDIUM -> "Medium"
                        PasswordStrength.STRONG -> "Strong"
                        null -> "Unknown"
                    }
                    
                    Text(
                        text = strengthText,
                        style = MaterialTheme.typography.bodySmall,
                        color = strengthColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Password requirements
                Text(
                    text = "Password should have at least 8 characters, including uppercase, lowercase, numbers and special characters.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurface.copy(alpha = 0.5f)
                )
            }

            TextField(
                value = confirmPasswordValue,
                onValueChange = { confirmPasswordValue = it },
                label = {
                    Text(
                        text = "Confirm Password",
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
                    unfocusedIndicatorColor = if (confirmPasswordError != null) colors.error else colors.outline,
                    focusedIndicatorColor = if (confirmPasswordError != null) colors.error else colors.primary,
                    unfocusedLabelColor = colors.onSurfaceVariant,
                    focusedLabelColor = if (confirmPasswordError != null) colors.error else colors.primary
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = confirmPasswordError != null
            )
            
            // Confirm password error message
            confirmPasswordError?.let { error ->
                Text(
                    text = error,
                    color = colors.error,
                    style = typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }

            // Password match indicator
            if (confirmPasswordValue.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val matchColor = when(passwordsMatch) {
                        true -> colors.primary
                        false -> colors.error
                        null -> colors.onSurface.copy(alpha = 0.7f)
                    }
                    
                    val matchText = when(passwordsMatch) {
                        true -> "Passwords match"
                        false -> "Passwords do not match"
                        null -> ""
                    }
                    
                    Text(
                        text = matchText,
                        style = MaterialTheme.typography.bodySmall,
                        color = matchColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Analytics Consent Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = analyticsConsentChecked,
                    onCheckedChange = { analyticsConsentChecked = it }
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Help improve IngreDiet",
                        style = typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = colors.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "Share anonymous usage data to help improve the app.",
                        style = typography.bodySmall,
                        color = colors.onSurfaceVariant,
                        lineHeight = typography.bodySmall.lineHeight * 1.2
                    )
                }
            }

            errorMessage?.let { message ->
                Text(
                    text = message,
                    color = colors.error,
                    style = typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    Log.d("RegisterDebug", "Signup button clicked")
                    coroutineScope.launch {
                        try {
                            withTimeout(10_000) {
                                if (!isOnline) {
                                    Log.d("RegisterDebug", "No internet connection")
                                    errorMessage = "No internet connection"
                                    errorType = LoginError.NETWORK_ERROR
                                    return@withTimeout
                                }

                                if (emailValue.isEmpty() || passwordValue.isEmpty() || confirmPasswordValue.isEmpty()) {
                                    Log.d("RegisterDebug", "Empty fields detected")
                                    errorMessage = "Please fill in all fields"
                                    errorType = LoginError.EMPTY_FIELDS
                                    return@withTimeout
                                }

                                if (passwordValue != confirmPasswordValue) {
                                    Log.d("RegisterDebug", "Passwords do not match")
                                    errorMessage = "Passwords do not match"
                                    errorType = LoginError.WRONG_PASSWORD
                                    return@withTimeout
                                }

                                Log.d("RegisterDebug", "Attempting signup")
                                authState = AuthState.Loading
                                authManager.signUpWithEmail(emailValue, passwordValue).collect { response ->
                                    when (response) {
                                        is AuthResponse.Success -> {
                                            Log.d("RegisterDebug", "Signup successful")
                                            authState = AuthState.Success
                                            
                                            // Save analytics consent preference
                                            val prefsManager = UserPreferencesManager(context)
                                            prefsManager.setAnalyticsConsent(analyticsConsentChecked)
                                            Log.d("RegisterDebug", "Analytics consent saved: $analyticsConsentChecked")
                                            
                                            Log.d("RegisterDebug", "Navigating to home")
                                            navController.navigate("home") {
                                                popUpTo("login") { inclusive = true }
                                            }
                                        }
                                        is AuthResponse.Error -> {
                                            Log.d("RegisterDebug", "Signup failed: ${response.message}")
                                            authState = AuthState.Error(response.message)
                                            errorMessage = response.message
                                            errorType = when {
                                                response.message?.contains("User already registered") == true ->
                                                    LoginError.INVALID_EMAIL
                                                response.message?.contains("Invalid email") == true ->
                                                    LoginError.INVALID_EMAIL
                                                response.message?.contains("rate") == true ->
                                                    LoginError.TOO_MANY_REQUESTS
                                                else -> LoginError.UNKNOWN_ERROR
                                            }
                                        }
                                        is AuthResponse.Loading -> {
                                            Log.d("RegisterDebug", "Signup in progress")
                                        }
                                    }
                                }
                            }
                        } catch (e: TimeoutCancellationException) {
                            Log.d("RegisterDebug", "Signup timed out")
                            errorMessage = "Signup timed out. Please try again."
                            errorType = LoginError.NETWORK_ERROR
                        } catch (e: Exception) {
                            Log.d("RegisterDebug", "Unexpected error: ${e.message}")
                            errorMessage = "An unexpected error occurred"
                            errorType = LoginError.UNKNOWN_ERROR
                        } finally {
                            authState = AuthState.Success
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (authState == AuthState.Loading) {
                    CircularProgressIndicator(
                        color = colors.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = "Sign Up",
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
                    if (!isOnline) {
                        errorMessage = "No internet connection"
                        errorType = LoginError.NETWORK_ERROR
                        return@OutlinedButton
                    }

                    coroutineScope.launch {
                        isGoogleSignInLoading = true
                        try {
                            val response = authManager.loginGoogleuser()
                            when (response) {
                                is AuthResponse.Success -> {
                                    // Save analytics consent preference for Google signup too
                                    val prefsManager = UserPreferencesManager(context)
                                    prefsManager.setAnalyticsConsent(analyticsConsentChecked)
                                    
                                    navController.navigate("home") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                                is AuthResponse.Error -> {
                                    errorMessage = response.message
                                    errorType = LoginError.UNKNOWN_ERROR
                                }
                            }
                        } catch (e: Exception) {
                            errorMessage = e.message ?: "An error occurred"
                            errorType = LoginError.UNKNOWN_ERROR
                        }
                                            isGoogleSignInLoading = false
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account? ",
                    style = typography.bodyMedium,
                    color = colors.onBackground.copy(alpha = 0.7f)
                )
                TextButton(
                    onClick = { navController.navigate("login") },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = "Sign In",
                        style = typography.titleSmall,
                        color = colors.primary
                    )
                }
            }
        }
    }
}


