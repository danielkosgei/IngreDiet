package com.thenewkenya.ingrediet.data.network

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.thenewkenya.ingrediet.data.network.AuthResponse
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

class AuthManager(
    private val context: Context
) {
    private val sessionManager = SessionManager(context)

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
            val refreshToken = sessionManager.getRefreshToken()
            if (refreshToken != null) {
                val session = supabase.auth.refreshSession(refreshToken)
                session != null
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("AuthManager", "Error restoring session", e)
            false
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

sealed class AuthState {
    data object Loading : AuthState()
    data object Success : AuthState()
    data class Error(val error: AuthErrorCode) : AuthState()
}

sealed interface AuthResponse {
    data object Success: AuthResponse
    data class Error(val mesasage: String?) : AuthResponse
}
