package com.thenewkenya.ingrediet.data.network

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.AuthErrorCode
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.util.UUID

class AuthManager(private val context: Context) {
    private val sessionManager = SessionManager(context)

    suspend fun saveCurrentSession() {
        try {
            val currentSession = supabase.auth.currentSessionOrNull()
            if (currentSession != null) {
                val refreshToken = currentSession.refreshToken ?: ""
                val accessToken = currentSession.accessToken ?: ""

                if (refreshToken.isNotEmpty()) {
                    sessionManager.saveTokens(refreshToken, accessToken)
                    Log.d("AuthManager", "Session saved with refresh token: ${refreshToken.take(5)}...")
                }
            } else {
                Log.d("AuthManager", "No current session to save")
            }
        } catch (e: Exception) {
            Log.e("AuthManager", "Failed to save session", e)
        }
    }

    fun signUpWithEmail(emailValue: String, passwordValue: String): Flow<AuthResponse> = flow {
        try {
            emit(AuthResponse.Loading)

            supabase.auth.signUpWith(Email) {
                email = emailValue
                password = passwordValue
            }

            // Manually save session after sing up
            saveCurrentSession()

            emit(AuthResponse.Success)
        } catch (e: Exception) {
            Log.e("AuthManager", "Sign up error", e)
            val errorMessage = when {
                e.message?.contains("User already registered") == true -> "Email already registered"
                e.message?.contains("network") == true -> "Network error. Please check your connection"
                e.message?.contains("password") == true -> "Password is too weak"
                e.message?.contains("email") == true -> "Invalid email format"
                else -> e.localizedMessage ?: "An unknown error occurred"
            }
            emit(AuthResponse.Error(errorMessage))
        }
    }

    fun signInWithEmail(emailValue: String, passwordValue: String): Flow<AuthResponse> = flow {
        try {
            emit(AuthResponse.Loading)

            supabase.auth.signInWith(Email) {
                email = emailValue
                password = passwordValue
            }

            // Manually save session after login
            saveCurrentSession()

            emit(AuthResponse.Success)
        } catch (e: Exception) {
            Log.e("AuthManager", "Login failed", e)
            val errorMessage = when {
                e.message?.contains("Invalid login credentials") == true -> "Invalid email or password"
                e.message?.contains("network") == true -> "Network error. Please check your connection"
                else -> e.localizedMessage ?: "An unknown error occurred"
            }
            emit(AuthResponse.Error(errorMessage))
        }
    }

    suspend fun signOut() {
        try {
            withContext(Dispatchers.IO) {
                supabase.auth.signOut()
                sessionManager.clearSession()
            }
        } catch (e: Exception) {
            Log.e("AuthManager", "Error signing out", e)
        }
    }

    suspend fun restoreSession(): Boolean {
        return try {
            // Check if we have a session first
            if (!sessionManager.hasValidSession()) {
                Log.d("AuthManager", "No valid session found to restore")
                return false
            }

            val refreshToken = sessionManager.getRefreshToken() ?: ""
            if (refreshToken.isEmpty()) {
                Log.d("AuthManager", "Refresh token is empty, cannot restore session")
                return false
            }

            Log.d("AuthManager", "Attempting to restore session with token: ${refreshToken.take(5)}...")

            try {
                // Use a try-catch specifically for the refresh operation
                val result = supabase.auth.refreshSession(refreshToken)
                if (result != null) {
                    Log.d("AuthManager", "Session restored successfully")
                    // Save the newly refreshed session
                    saveCurrentSession()
                    return true
                } else {
                    Log.d("AuthManager", "Session refresh returned null")
                    return false
                }
            } catch (e: Exception) {
                Log.e("AuthManager", "Session refresh failed", e)
                // Only clear session if it's invalid
                if (e.message?.contains("invalid", ignoreCase = true) == true) {
                    sessionManager.clearSession()
                }
                return false
            }
        } catch (e: Exception) {
            Log.e("AuthManager", "Error in restoreSession", e)
            return false
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

            saveCurrentSession()

            emit(AuthResponse.Success)
        } catch (e: Exception) {
            emit(AuthResponse.Error(e.localizedMessage))

        }
    }
}

sealed class AuthState {
    data object Loading : AuthState()
    data object Success : AuthState()
    data class Error(val message: String?) : AuthState()
}

sealed interface AuthResponse {
    data object Loading: AuthResponse
    data object Success: AuthResponse
    data class Error(val message: String?) : AuthResponse
}
