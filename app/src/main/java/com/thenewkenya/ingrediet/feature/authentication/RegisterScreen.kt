package com.thenewkenya.ingrediet.feature.authentication

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
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
import com.thenewkenya.ingrediet.Gradient
import com.thenewkenya.ingrediet.R
import com.thenewkenya.ingrediet.data.network.AuthManager
import com.thenewkenya.ingrediet.data.network.AuthResponse
import com.thenewkenya.ingrediet.data.network.AuthState
import io.github.jan.supabase.auth.exception.AuthErrorCode
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.onEach

@Composable
fun RegisterScreen(navController: NavController) {
    var emailValue by remember { mutableStateOf("") }
    var passwordValue by remember { mutableStateOf("") }
    var confirmPasswordValue by remember { mutableStateOf("") }
    var passwordVisibility by remember { mutableStateOf(false) }
    var confirmPasswordVisibility by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var errorType by remember { mutableStateOf<LoginError?>(null) }
    val context = LocalContext.current
    var authManager by remember { mutableStateOf(AuthManager(context)) }
    val coroutineScope = rememberCoroutineScope()
    var authState by remember { mutableStateOf<AuthState>(AuthState.Success) }
    var isGoogleSignInLoading by remember { mutableStateOf(false) }
    var isOnline by remember { mutableStateOf(true) }

    LaunchedEffect(emailValue, passwordValue, confirmPasswordValue) {
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
        Gradient()

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
                modifier = Modifier.size(80.dp)
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

            Spacer(modifier = Modifier.height(24.dp))

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
                    unfocusedContainerColor = colors.surfaceVariant,
                    focusedContainerColor = colors.surfaceVariant,
                    unfocusedLabelColor = colors.onSurfaceVariant,
                    focusedLabelColor = colors.primary
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

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
                    unfocusedContainerColor = colors.surfaceVariant,
                    focusedContainerColor = colors.surfaceVariant,
                    unfocusedLabelColor = colors.onSurfaceVariant,
                    focusedLabelColor = colors.primary
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            TextField(
                value = confirmPasswordValue,
                onValueChange = { confirmPasswordValue = it },
                label = {
                    Text(
                        text = "Confirm Password",
                        style = typography.bodyMedium
                    )
                },
                visualTransformation = if (confirmPasswordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisibility = !confirmPasswordVisibility }) {
                        Icon(
                            imageVector = if (confirmPasswordVisibility) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (confirmPasswordVisibility) "Hide password" else "Show password"
                        )
                    }
                },
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = colors.surfaceVariant,
                    focusedContainerColor = colors.surfaceVariant,
                    unfocusedLabelColor = colors.onSurfaceVariant,
                    focusedLabelColor = colors.primary
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            errorMessage?.let { message ->
                Text(
                    text = message,
                    color = colors.error,
                    style = typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (!isOnline) {
                        errorMessage = "No internet connection"
                        errorType = LoginError.NETWORK_ERROR
                        return@Button
                    }

                    if (emailValue.isEmpty() || passwordValue.isEmpty() || confirmPasswordValue.isEmpty()) {
                        errorMessage = "Please fill in all fields"
                        errorType = LoginError.EMPTY_FIELDS
                        return@Button
                    }

                    if (passwordValue != confirmPasswordValue) {
                        errorMessage = "Passwords do not match"
                        errorType = LoginError.WRONG_PASSWORD
                        return@Button
                    }

                    coroutineScope.launch {
                        authState = AuthState.Loading
                        try {
                            val response = authManager.signUpWithEmail(emailValue, passwordValue)
                            when (response) {
                                is AuthResponse.Success -> {
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
                        authState = AuthState.Success
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (authState == AuthState.Loading) {
                    CircularProgressIndicator(
                        color = colors.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "Sign Up",
                        style = typography.bodyLarge,
                        color = colors.onPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            GoogleSignInButton(
                onClick = {
                    if (!isOnline) {
                        errorMessage = "No internet connection"
                        errorType = LoginError.NETWORK_ERROR
                        return@GoogleSignInButton
                    }

                    coroutineScope.launch {
                        isGoogleSignInLoading = true
                        try {
                            val response = authManager.loginGoogleuser()
                            when (response) {
                                is AuthResponse.Success -> {
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
                isLoading = isGoogleSignInLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Already have an account? ",
                    style = typography.bodyMedium,
                    color = colors.onBackground.copy(alpha = 0.7f)
                )
                TextButton(
                    onClick = { navController.navigate("login") }
                ) {
                    Text(
                        text = "Sign In",
                        style = typography.bodyMedium,
                        color = colors.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun RegisterHeader() {
    val colors = MaterialTheme.colorScheme // Access dynamic theme colors

    Text(
        text = "Register",
        style = MaterialTheme.typography.titleLarge,
        color = colors.onBackground, // Adapts to light/dark mode
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = "Create an account to get started",
        style = MaterialTheme.typography.bodyMedium,
        color = colors.onBackground.copy(alpha = 0.8f) // Softer contrast for subtitle
    )
}
