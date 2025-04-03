package com.thenewkenya.ingrediet.data.network

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.AuthErrorCode
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.util.UUID
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

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
                e.message?.contains("Invalid email") == true -> "Invalid email format"
                e.message?.contains("password") == true || passwordValue.length < 6 -> 
                    "Password is too weak. It should be at least 6 characters long."
                e.message?.contains("rate") == true -> "Too many attempts. Please try again later."
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
                e.message?.contains("Invalid email") == true -> "Invalid email format"
                e.message?.contains("rate") == true -> "Too many attempts. Please try again later."
                e.message?.contains("User is disabled") == true -> "This account has been disabled. Please contact support."
                e.message?.contains("email is not confirmed") == true -> "Please confirm your email address before signing in."
                else -> e.localizedMessage ?: "An unknown error occurred"
            }
            emit(AuthResponse.Error(errorMessage))
        }
    }

    suspend fun signOut() {
        withContext(Dispatchers.IO) {
            try {
                supabase.auth.signOut()
                sessionManager.clearSession()
                Log.d("AuthManager", "User signed out and session cleared")
            } catch (e: Exception) {
                Log.e("AuthManager", "Error signing out", e)
                throw e
            }
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
            
            // Extract profile information
            val displayName = googleIdTokenCredential.displayName
            val profilePictureUrl = googleIdTokenCredential.profilePictureUri?.toString()
            
            Log.d("GoogleSignIn", "Got user info - Name: $displayName, Picture: $profilePictureUrl")

            // Sign in with Supabase using the ID token
            supabase.auth.signInWith(IDToken) {
                idToken = googleIdToken
                provider = Google
            }
            
            // Wait briefly for the session to be created
            kotlinx.coroutines.delay(500)
            
            // Save session data
            saveCurrentSession()
            
            // Store profile picture URL in user metadata
            try {
                val currentUser = supabase.auth.currentUserOrNull()
                if (currentUser != null && (profilePictureUrl != null || displayName != null)) {
                    // Update user metadata with avatar URL and display name
                    supabase.auth.updateUser {
                        data = kotlinx.serialization.json.buildJsonObject {
                            if (profilePictureUrl != null) {
                                put("avatar_url", profilePictureUrl)
                            }
                            if (displayName != null) {
                                put("display_name", displayName)
                                
                                // Split display name into first and last name
                                val nameParts = displayName.split(" ", limit = 2)
                                val firstName = nameParts[0]
                                val lastName = if (nameParts.size > 1) nameParts[1] else ""
                                
                                // Add first and last name to metadata
                                put("first_name", firstName)
                                put("last_name", lastName)
                            }
                        }
                    }
                    Log.d("GoogleSignIn", "Updated user metadata with profile information")
                }
            } catch (e: Exception) {
                // Non-fatal error, just log it
                Log.e("GoogleSignIn", "Failed to update user metadata", e)
            }

            emit(AuthResponse.Success)
        } catch (e: Exception) {
            emit(AuthResponse.Error(e.localizedMessage))
        }
    }

    // Add reset password functionality
    fun resetPassword(email: String): Flow<AuthResponse> = flow {
        try {
            emit(AuthResponse.Loading)
            
            // Send password reset email
            supabase.auth.resetPasswordForEmail(email)
            
            emit(AuthResponse.Success)
        } catch (e: Exception) {
            Log.e("AuthManager", "Password reset error", e)
            val errorMessage = when {
                e.message?.contains("network") == true -> "Network error. Please check your connection"
                e.message?.contains("email") == true -> "Invalid email format"
                e.message?.contains("not found") == true -> "Email not found"
                else -> e.localizedMessage ?: "An unknown error occurred"
            }
            emit(AuthResponse.Error(errorMessage))
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
