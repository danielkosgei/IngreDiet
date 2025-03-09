package com.thenewkenya.ingrediet.feature.authentication

import android.util.Log
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

    LaunchedEffect(emailValue, passwordValue) {
        errorMessage = null
        errorType = null
    }

    val colors = MaterialTheme.colorScheme // Access theme colors

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.onBackground),
        contentAlignment = Alignment.Center
    ) {
        Gradient()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .padding(top = 110.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LoginHeader()

            Spacer(modifier = Modifier.height(40.dp))

            GoogleSignInButton(
                onClick = {
                    isGoogleSignInLoading = true
                    authManager.loginGoogleuser()
                        .onEach { result ->
                            isGoogleSignInLoading = false
                            if (result is AuthResponse.Success) {
                                Log.d("auth", "Google Success")
                            } else {
                                Log.e("auth", "Google Error")
                            }
                        }
                        .launchIn(coroutineScope)
                },
                isLoading = isGoogleSignInLoading
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 30.dp)

            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(colors.onBackground.copy(alpha = 0.2f))
                )

                Text(
                    text = "or",
                    color = colors.onBackground.copy(alpha = 0.7f),
                    modifier = Modifier.padding(horizontal = 10.dp)
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(colors.onBackground.copy(alpha = 0.2f))
                )
            }

            Column(
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Email",
                    color = colors.onBackground,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                TextField(
                    value = emailValue,
                    onValueChange = { newValue ->
                        emailValue = newValue
                        errorMessage = null // Clear error on input change
                    },
                    placeholder = {
                        Text(
                            text = "john.doe@example.com",
                            color = colors.onBackground.copy(alpha = 0.7f)
                        )
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = TextFieldDefaults.colors(
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        focusedContainerColor = colors.surface,
                        unfocusedContainerColor = colors.surface
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = Color.Red,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Password",
                    color = colors.onBackground,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                TextField(
                    value = passwordValue,
                    onValueChange = { newValue ->
                        passwordValue = newValue
                    },
                    placeholder = {
                        Text(
                            text = "Enter your password",
                            color = colors.onBackground.copy(alpha = 0.7f)
                        )
                    },
                    visualTransformation = if (passwordVisibility) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        val image = if (passwordVisibility) {
                            Icons.Filled.Visibility
                        } else {
                            Icons.Filled.VisibilityOff
                        }

                        val description: String = if (passwordVisibility) "Hide password"
                        else "Show password"

                        IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                            Icon(imageVector = image, contentDescription = description)
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = TextFieldDefaults.colors(
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        focusedContainerColor = colors.surface,
                        unfocusedContainerColor = colors.surface
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(35.dp))

            Button(
                onClick = {
                    if (emailValue.isEmpty() || passwordValue.isEmpty()) {
                        errorType = LoginError.EMPTY_FIELDS
                        errorMessage = "Please fill in all fields"
                    } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailValue).matches()) {
                        errorType = LoginError.INVALID_EMAIL
                        errorMessage = "Please enter a valid email address"
                    } else {
                        authState = AuthState.Loading
                        authManager.signInWithEmail(emailValue, passwordValue)
                            .onEach { result ->
                                when (result) {
                                    is AuthResponse.Success -> {
                                        Log.d("auth", "Login success, clearing error message")
                                        authState = AuthState.Success
                                        errorMessage = null
                                        errorType = null
                                        navController.navigate("home")
                                    }
                                    is AuthResponse.Loading -> {
                                        // Handle loading state if needed
                                    }
                                    is AuthResponse.Error -> {
                                        val error = result.mesasage ?: "Unknown error"
                                        authState = AuthState.Error(AuthErrorCode.InvalidCredentials)
                                        errorType = when {
                                            error.contains("Invalid login credentials", ignoreCase = true) -> LoginError.WRONG_PASSWORD
                                            error.contains("network", ignoreCase = true) -> LoginError.NETWORK_ERROR
                                            else -> LoginError.UNKNOWN_ERROR
                                        }
                                        errorMessage = when (errorType) {
                                            LoginError.WRONG_PASSWORD -> "Incorrect email or password. Please try again."
                                            LoginError.NETWORK_ERROR -> "Network error. Please check your connection."
                                            else -> "Login failed. Please try again."
                                        }
                                    }
                                }
                            }
                            .launchIn(coroutineScope)
                    }
                },
                enabled = authState != AuthState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                if (authState == AuthState.Loading) {
                    // Show a loading indicator
                    CircularProgressIndicator(
                        color = colors.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "Sign In",
                        color = colors.onPrimary,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(25.dp))

            TextButton(
                onClick = {
                    navController.navigate("register")
                }
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Light,
                                color = colors.onBackground.copy(alpha = 0.8f)
                            )
                        ) {
                            append("Don't have an account? ")
                        }

                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Bold,
                                color = colors.onBackground
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
