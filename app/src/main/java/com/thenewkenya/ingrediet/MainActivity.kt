package com.thenewkenya.ingrediet

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.thenewkenya.ingrediet.ui.theme.IngreDietTheme
import com.thenewkenya.ingrediet.ui.theme.black
import com.thenewkenya.ingrediet.ui.theme.darkGray
import com.thenewkenya.ingrediet.ui.theme.darkTeal
import com.thenewkenya.ingrediet.ui.theme.teal
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.AuthErrorCode
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.createSupabaseClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.security.MessageDigest
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IngreDietTheme {
                LoginScreen()
            }
        }
    }
}

@Composable
fun RegisterScreen() {

    var emailValue by remember {
        mutableStateOf("")
    }

    var passwordValue by remember {
        mutableStateOf("")
    }

    var passwordVisibility by remember {
        mutableStateOf(false)
    }

    val context = LocalContext.current
    val authManager = remember {
        AuthManager(context)
    }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(black),
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
                        .background(Color.White.copy(alpha = 0.2f))
                )

                Text(
                    text = "Or",
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(horizontal = 10.dp)
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(Color.White.copy(alpha = 0.2f))
                )
            }

            Column(
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Email",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                TextField(
                    value = emailValue,
                    onValueChange = { newValue ->
                        emailValue = newValue
                    },
                    placeholder = {
                        Text(
                            text = "john.doe@example.com",
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = TextFieldDefaults.colors(
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        focusedContainerColor = darkGray,
                        unfocusedContainerColor = darkGray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Password",
                    color = Color.White,
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
                            color = Color.White.copy(alpha = 0.7f)
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
                            Icon(imageVector = image, contentDescription = description, tint = Color.White)
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = TextFieldDefaults.colors(
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        focusedContainerColor = darkGray,
                        unfocusedContainerColor = darkGray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(35.dp))

            var authState by remember {
                mutableStateOf<AuthState>(AuthState.Success)
            }

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
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (authState == AuthState.Loading) {
                    // Show a loading indicator
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "Sign Up",
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                }

            }

            Spacer(modifier = Modifier.height(25.dp))

            TextButton(
                onClick = {}
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Light,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        ) {
                            append("Already have an account? ")
                        }

                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        ) {
                            append("Sign In")
                        }
                    }
                )
            }

        }
    }
}

@Composable
fun LoginScreen() {
    var emailValue by remember {
        mutableStateOf("")
    }

    var passwordValue by remember {
        mutableStateOf("")
    }

    var passwordVisibility by remember {
        mutableStateOf(false)
    }

    val context = LocalContext.current
    val authManager = remember {
        AuthManager(context)
    }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(black),
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
                        .background(Color.White.copy(alpha = 0.2f))
                )

                Text(
                    text = "or",
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(horizontal = 10.dp)
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(Color.White.copy(alpha = 0.2f))
                )
            }

            Column(
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Email",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                TextField(
                    value = emailValue,
                    onValueChange = { newValue ->
                        emailValue = newValue
                    },
                    placeholder = {
                        Text(
                            text = "john.doe@example.com",
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = TextFieldDefaults.colors(
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        focusedContainerColor = darkGray,
                        unfocusedContainerColor = darkGray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Password",
                    color = Color.White,
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
                            color = Color.White.copy(alpha = 0.7f)
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
                        focusedContainerColor = darkGray,
                        unfocusedContainerColor = darkGray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(35.dp))

            var authState by remember {
                mutableStateOf<AuthState>(AuthState.Success)
            }

            Button(
                onClick = {
                    authState = AuthState.Loading
                    authManager.signInWithEmail(emailValue, passwordValue)
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
                enabled = authState != AuthState.Loading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (authState == AuthState.Loading) {
                    // Show a loading indicator
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "Sign In",
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(25.dp))

            TextButton(
                onClick = {}
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Light,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        ) {
                            append("Don't have an account? ")
                        }

                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
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
private fun GoogleSignInButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_google),
            contentDescription = "Google Logo",
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = "Continue with Google",
            color = Color.White,
            modifier = Modifier.padding(vertical = 4.dp)
        )
    }
}

@Composable
private fun RegisterHeader() {
    Text(
        text = "Register",
        style = MaterialTheme.typography.titleLarge,
        color = Color.White,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = "Create an account to get started",
        style = MaterialTheme.typography.bodyMedium,
        color = Color.White
    )
}

@Composable
private fun LoginHeader() {
    Text(
        text = "Sign In",
        style = MaterialTheme.typography.titleLarge,
        color = Color.White,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = "Welcome back! Sign in to continue",
        style = MaterialTheme.typography.bodyMedium,
        color = Color.White
    )
}

@Preview
@Composable
private fun RegisterPreview(){
    IngreDietTheme {
        RegisterScreen()
    }
}

@Composable
fun Gradient() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        teal,
                        darkTeal,
                        black
                    )
                )
        )
    )
}

sealed class AuthState {
    data object Loading : AuthState()
    data object Success : AuthState()
    data class Error(val error: AuthErrorCode) : AuthState()
}

sealed interface AuthResponse {
    data object Success: AuthResponse
    data class Error(val mesasage: String?) : AuthResponse
}

class AuthManager(
    private val context: Context
) {
    private val supabase = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_ANON_KEY
    ) {
        install(Auth)
    }

    fun signUpWithEmail(emailValue: String, passwordValue: String): Flow<AuthResponse> = flow {
        try {
            supabase.auth.signUpWith(Email) {
                email = emailValue
                password = passwordValue
            }

            emit(AuthResponse.Success)

        } catch (e: Exception) {
            emit(AuthResponse.Error(e.localizedMessage))

        }
    }

    fun signInWithEmail(emailValue: String, passwordValue: String): Flow<AuthResponse> = flow {
        try {
            supabase.auth.signInWith(Email) {
                email = emailValue
                password = passwordValue
            }
            emit(AuthResponse.Success)
        } catch (e: Exception) {
            emit(AuthResponse.Error(e.localizedMessage))
        }
    }

    fun createNonce(): String {
        val rawNonce = UUID.randomUUID().toString()
        val bytes = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)

        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    fun loginGoogleuser(): Flow<AuthResponse> = flow {
        val hashedNonce = createNonce()

        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId("363319580036-mr44i8gdn0jpauv05kdn53uaihv35g83.apps.googleusercontent.com")
            .setNonce(hashedNonce)
            .setAutoSelectEnabled(false)
            .setFilterByAuthorizedAccounts(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val credentialManager = CredentialManager.create(context)

        try {
            val result = credentialManager.getCredential(
                context = context,
                request = request
            )

            val googleIdTokenCredential = GoogleIdTokenCredential
                .createFrom(result.credential.data)

            val googleIdToken = googleIdTokenCredential.idToken

            supabase.auth.signInWith(IDToken) {
                idToken = googleIdToken
                provider = Google
            }

            emit(AuthResponse.Success)

        } catch (e: Exception) {
            emit(AuthResponse.Error(e.localizedMessage))

        }
    }
}