package com.thenewkenya.ingrediet.data.network

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserPreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
    
    companion object {
        private const val ANALYTICS_CONSENT_KEY = "analytics_consent"
        private const val BIOMETRIC_ENABLED_KEY = "biometric_enabled"
    }
    
    suspend fun setAnalyticsConsent(enabled: Boolean) {
        withContext(Dispatchers.IO) {
            prefs.edit()
                .putBoolean(ANALYTICS_CONSENT_KEY, enabled)
                .apply()
        }
    }
    
    suspend fun getAnalyticsConsent(): Boolean {
        return withContext(Dispatchers.IO) {
            prefs.getBoolean(ANALYTICS_CONSENT_KEY, true) // Default to true
        }
    }
    
    suspend fun setBiometricEnabled(enabled: Boolean) {
        withContext(Dispatchers.IO) {
            prefs.edit()
                .putBoolean(BIOMETRIC_ENABLED_KEY, enabled)
                .apply()
        }
    }
    
    suspend fun getBiometricEnabled(): Boolean {
        return withContext(Dispatchers.IO) {
            prefs.getBoolean(BIOMETRIC_ENABLED_KEY, false) // Default to false
        }
    }
    
    suspend fun clearPreferences() {
        withContext(Dispatchers.IO) {
            prefs.edit().clear().apply()
        }
    }
} 