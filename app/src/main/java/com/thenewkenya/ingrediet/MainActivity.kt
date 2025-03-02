package com.thenewkenya.ingrediet

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.thenewkenya.ingrediet.data.network.AuthManager
import com.thenewkenya.ingrediet.data.network.LocalSupabase
import com.thenewkenya.ingrediet.data.network.SessionManager
import com.thenewkenya.ingrediet.data.network.supabase
import com.thenewkenya.ingrediet.feature.authentication.LoginScreen
import com.thenewkenya.ingrediet.feature.authentication.RegisterScreen
import com.thenewkenya.ingrediet.feature.navigation.HomeScreen
import com.thenewkenya.ingrediet.feature.navigation.LoadingScreen
import com.thenewkenya.ingrediet.ui.theme.IngreDietTheme
import com.thenewkenya.ingrediet.ui.theme.black
import com.thenewkenya.ingrediet.ui.theme.darkTeal
import com.thenewkenya.ingrediet.ui.theme.teal
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sessionManager = SessionManager(applicationContext)
        setContent {
            IngreDietTheme {
                // Surface container using the background color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CompositionLocalProvider(
                        LocalSupabase provides supabase,
                        LocalSessionManager provides sessionManager
                    ) {
                        AppNavigation()
                    }
                }
            }
        }
    }

}

val LocalSessionManager = staticCompositionLocalOf<SessionManager> {
    error("No SessionManager provided")
}
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()
    var startDestination by remember { mutableStateOf("splash") }
    val context = LocalContext.current
    val sessionManager = LocalSessionManager.current
    val authManager = remember { AuthManager(context) }
    val supabase = LocalSupabase.current

    // Check for session directly from SharedPreferences
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            // Check if we have a stored session before trying to restore
            val hasSession = sessionManager.hasValidSession()
            Log.d("AppNavigation", "Has stored session: $hasSession")

            if (hasSession) {
                // Only try to restore if we have something to restore
                val sessionRestored = authManager.restoreSession()
                Log.d("AppNavigation", "Session restored: $sessionRestored")

                if (sessionRestored) {
                    startDestination = "home"
                }
            }

            // Now monitor the actual session status
            supabase.auth.sessionStatus.collect { status ->
                Log.d("AppNavigation", "Session status update: $status")
                startDestination = when (status) {
                    is SessionStatus.Authenticated -> {
                        Log.d("AppNavigation", "User authenticated: ${status.session.user?.email}")
                        // Save session when authenticated
                        authManager.saveCurrentSession()
                        "home"
                    }
                    is SessionStatus.NotAuthenticated -> {
                        Log.d("AppNavigation", "User not authenticated")
                        "login"
                    }
                    else -> {
                        Log.d("AppNavigation", "Auth status loading")
                        if (sessionManager.hasValidSession()) "home" else "login"
                    }
                }
            }
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") { LoginScreen(navController) }
        composable("register") { RegisterScreen(navController) }
        composable("home") { HomeScreen(navController) }
        composable("loading") { LoadingScreen() }
        composable("splash") { SplashScreen(navController) }
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

@Composable
fun SplashScreen(navController: NavHostController) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background

    ) {
        // Add splash screen eg. logo
    }
}
