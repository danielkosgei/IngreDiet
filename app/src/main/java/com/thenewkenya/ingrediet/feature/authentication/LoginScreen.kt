package com.thenewkenya.ingrediet.feature.authentication

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import com.thenewkenya.ingrediet.data.network.supabase
import io.github.jan.supabase.auth.exception.AuthErrorCode
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

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

    val colors = MaterialTheme.colorScheme // Access theme colors

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LoginHeader()

            Spacer(modifier = Modifier.height(32.dp))

            // Email TextField
            TextField(
                value = emailValue,
                onValueChange = { emailValue = it },
                label = {
                    Text(
                        text = "Email",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurface.copy(alpha = 0.7f)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = colors.surface,
                    unfocusedContainerColor = colors.surface,
                    focusedTextColor = colors.onSurface,
                    unfocusedTextColor = colors.onSurface,
                    focusedLabelColor = colors.primary,
                    unfocusedLabelColor = colors.onSurface.copy(alpha = 0.7f),
                    cursorColor = colors.primary,
                    focusedIndicatorColor = colors.primary,
                    unfocusedIndicatorColor = colors.onSurface.copy(alpha = 0.2f)
                ),
                shape = RoundedCornerShape(10.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password TextField
            TextField(
                value = passwordValue,
                onValueChange = { passwordValue = it },
                label = {
                    Text(
                        text = "Password",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurface.copy(alpha = 0.7f)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                        Icon(
                            imageVector = if (passwordVisibility) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (passwordVisibility) "Hide password" else "Show password",
                            tint = colors.onSurface.copy(alpha = 0.7f)
                        )
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = colors.surface,
                    unfocusedContainerColor = colors.surface,
                    focusedTextColor = colors.onSurface,
                    unfocusedTextColor = colors.onSurface,
                    focusedLabelColor = colors.primary,
                    unfocusedLabelColor = colors.onSurface.copy(alpha = 0.7f),
                    cursorColor = colors.primary,
                    focusedIndicatorColor = colors.primary,
                    unfocusedIndicatorColor = colors.onSurface.copy(alpha = 0.2f)
                ),
                shape = RoundedCornerShape(10.dp),
                singleLine = true
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage!!,
                    color = colors.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sign In Button
            Button(
                onClick = {
                    coroutineScope.launch {
                        if (!isOnline) {
                            errorMessage = "No internet connection"
                            errorType = LoginError.NETWORK_ERROR
                            return@launch
                        }

                        if (emailValue.isEmpty() || passwordValue.isEmpty()) {
                            errorMessage = "Please fill in all fields"
                            errorType = LoginError.EMPTY_FIELDS
                            return@launch
                        }

                        authState = AuthState.Loading
                        val response = authManager.signInWithEmail(emailValue, passwordValue)
                        when (response) {
                            is AuthResponse.Success -> {
                                authState = AuthState.Success
                                navController.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                            is AuthResponse.Error -> {
                                authState = AuthState.Error(response.message)
                                errorMessage = response.message
                                errorType = when {
                                    response.message?.contains("Invalid login credentials") == true -> LoginError.WRONG_PASSWORD
                                    response.message?.contains("Invalid email") == true -> LoginError.INVALID_EMAIL
                                    response.message?.contains("Too many requests") == true -> LoginError.TOO_MANY_REQUESTS
                                    else -> LoginError.UNKNOWN_ERROR
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary,
                    disabledContainerColor = colors.primary.copy(alpha = 0.5f),
                    disabledContentColor = colors.onPrimary.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(10.dp),
                enabled = authState != AuthState.Loading
            ) {
                if (authState == AuthState.Loading) {
                    CircularProgressIndicator(
                        color = colors.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "Sign In",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                        color = colors.onPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Google Sign In Button
            GoogleSignInButton(
                onClick = {
                    isGoogleSignInLoading = true
                    coroutineScope.launch {
                        val response = authManager.loginGoogleuser()
                        isGoogleSignInLoading = false
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
                    }
                },
                isLoading = isGoogleSignInLoading
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Sign Up Link
            TextButton(
                onClick = { navController.navigate("register") }
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Normal,
                                color = colors.onBackground.copy(alpha = 0.7f)
                            )
                        ) {
                            append("Don't have an account? ")
                        }
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Bold,
                                color = colors.primary
                            )
                        ) {
                            append("Sign Up")
                        }
                    }
                )
            }
        }
    }

}

@Composable
private fun LoginHeader() {
    val textColor = MaterialTheme.colorScheme.onBackground

    Text(
        text = "Sign In",
        style = MaterialTheme.typography.titleLarge,
        color = textColor,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = "Welcome back! Sign in to continue",
        style = MaterialTheme.typography.bodyMedium,
        color = textColor.copy(alpha = 0.8f)
    )
}

@Composable
fun GoogleSignInButton(
    onClick: () -> Unit,
    isLoading: Boolean = false
) {
    val buttonColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onPrimary

    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth(),
        enabled = !isLoading
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = textColor,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_google),
                    contentDescription = "Google Logo",
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "Continue with Google",
                    color = textColor,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}
