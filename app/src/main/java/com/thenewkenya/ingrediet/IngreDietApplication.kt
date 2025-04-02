package com.thenewkenya.ingrediet

import android.app.Application
import android.se.omapi.Session
import android.util.Log
import com.thenewkenya.ingrediet.data.network.SessionManager
import com.thenewkenya.ingrediet.data.network.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class IngreDietApplication: Application() {
    lateinit var sessionManager: SessionManager
        private set

    // Application-scoped CoroutineScope that will be cancelled when the app is destroyed
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        instance = this
        sessionManager = SessionManager(applicationContext)

        // Monitor auth session status
        appScope.launch {
            try {
                supabase.auth.sessionStatus.collect { status ->
                    Log.d("IngreDietApplication", "Session status changed: $status")
                    when (status) {
                        is SessionStatus.Authenticated -> {
                            val refreshToken = status.session.refreshToken ?: ""
                            val accessToken = status.session.accessToken ?: ""

                            if (refreshToken.isNotEmpty()) {
                                // Use the updated method signature with both tokens
                                sessionManager.saveTokens(refreshToken, accessToken)
                                Log.d("IngreDietApplication", "Session tokens saved from status update")
                            }
                        }
                        is SessionStatus.NotAuthenticated -> {
                            // Clear the session when logged out
                            sessionManager.clearSession()
                            Log.d("IngreDietApplication", "Session cleared due to logout")
                        }
                        else -> {
                            // Handle loading state if needed
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("IngreDietApplication", "Error collecting session status", e)
            }
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        // Cancel all coroutines when the application is terminated
        appScope.cancel()
    }

    companion object {
        lateinit var instance: IngreDietApplication
            private set
    }
} 