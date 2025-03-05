package com.thenewkenya.ingrediet.feature.authentication

import android.util.Log
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.thenewkenya.ingrediet.Gradient
import com.thenewkenya.ingrediet.data.network.AuthManager
import com.thenewkenya.ingrediet.data.network.AuthResponse
import com.thenewkenya.ingrediet.data.network.AuthState
import io.github.jan.supabase.auth.exception.AuthErrorCode
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach


@Composable
fun RegisterScreen(navController: NavController) {
    var emailValue by remember { mutableStateOf("") }
    var passwordValue by remember { mutableStateOf("") }
    var passwordVisibility by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }
    val coroutineScope = rememberCoroutineScope()

    val colors = MaterialTheme.colorScheme // Access current theme colors

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background), // Use dynamic background color
        contentAlignment = Alignment.Center
    ) {
        Gradient() // Uses theme-aware colors in gradient

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .padding(top = 110.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RegisterHeader()

            Spacer(modifier = Modifier.height(40.dp))

            GoogleSignInButton(
                onClick = {
                    authManager.loginGoogleuser()
                        .onEach { result ->
                            if (result is AuthResponse.Success) {
                                Log.d("auth", "Google Success")
                            } else {
                                Log.e("auth", "Google Error")
                            }
                        }
                        .launchIn(coroutineScope)
                }
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
                    text = "Or",
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

            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = "Email",
                    color = colors.onBackground, // Adapts to theme
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                TextField(
                    value = emailValue,
                    onValueChange = { newValue -> emailValue = newValue },
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
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = "Password",
                    color = colors.onBackground,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                TextField(
                    value = passwordValue,
                    onValueChange = { newValue -> passwordValue = newValue },
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

                        val description: String = if (passwordVisibility) "Hide password" else "Show password"

                        IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                            Icon(
                                imageVector = image,
                                contentDescription = description,
                                tint = colors.onBackground // Use theme-aware icon tint
                            )
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

            var authState by remember { mutableStateOf<AuthState>(AuthState.Success) }

            Button(
                onClick = {
                    authState = AuthState.Loading
                    authManager.signUpWithEmail(emailValue, passwordValue)
                        .onEach { result ->
                            if (result is AuthResponse.Success) {
                                authState = AuthState.Success
                                Log.d("auth", "Email Success")
                            } else {
                                authState = AuthState.Error(AuthErrorCode.InvalidCredentials)
                                Log.e("auth", "Email Error")
                            }
                        }
                        .launchIn(coroutineScope)
                },
                enabled = authState != AuthState.Loading, // Disable button while loading
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary // Use dynamic theme color
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (authState == AuthState.Loading) {
                    CircularProgressIndicator(
                        color = colors.onPrimary, // Adapted to theme
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "Sign Up",
                        color = colors.onPrimary, // Ensure contrast
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(25.dp))

            TextButton(
                onClick = { navController.navigate("login") }
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Light,
                                color = colors.onBackground.copy(alpha = 0.8f)
                            )
                        ) { append("Already have an account? ") }

                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Bold,
                                color = colors.onBackground
                            )
                        ) { append("Sign In") }
                    }
                )
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
