package com.thenewkenya.ingrediet.data.network

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    private val refreshTokenKey = "refresh_token"
    private val accessTokenKey = "access_token"
    private val userIdKey = "user_id"

    suspend fun saveTokens(refreshToken: String, accessToken: String) {
        withContext(Dispatchers.IO) {
            prefs.edit()
                .putString(refreshTokenKey, refreshToken)
                .putString(accessTokenKey, accessToken)
                .apply()
            Log.d("SessionManager", "Tokens saved successfully")
        }
    }

    suspend fun getRefreshToken(): String? {
        return withContext(Dispatchers.IO) {
            val token = prefs.getString(refreshTokenKey, null)
            Log.d("SessionManager", "Retrieved refresh token: ${token?.take(5) ?: "null"}")
            token
        }
    }

    suspend fun getAccessToken(): String? {
        return withContext(Dispatchers.IO) {
            val token = prefs.getString(accessTokenKey, null)
            Log.d("SessionManager", "Retrieved access token: ${token?.take(5) ?: "null"}")
            token
        }
    }

    suspend fun clearSession() {
        withContext(Dispatchers.IO) {
            Log.d("SessionManager", "Clearing session")
            prefs.edit()
                .remove(refreshTokenKey)
                .remove(accessTokenKey)
                .remove(userIdKey)
                .apply()
            Log.d("SessionManager", "Session cleared")
        }
    }

    suspend fun hasValidSession(): Boolean {
        return withContext(Dispatchers.IO) {
            val refreshToken = prefs.getString(refreshTokenKey, null)
            val hasToken = !refreshToken.isNullOrEmpty()
            Log.d("SessionManager", "Checking for valid session: $hasToken")
            hasToken
        }
    }

    suspend fun saveUserId(userId: String) {
        withContext(Dispatchers.IO) {
            prefs.edit()
                .putString(userIdKey, userId)
                .apply()
            Log.d("SessionManager", "User ID saved successfully")
        }
    }

    suspend fun getCurrentUserId(): String? {
        return withContext(Dispatchers.IO) {
            val userId = prefs.getString(userIdKey, null)
            Log.d("SessionManager", "Retrieved user ID: $userId")
            userId
        }
    }
}