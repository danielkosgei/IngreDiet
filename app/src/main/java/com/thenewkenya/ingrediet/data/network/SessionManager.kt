package com.thenewkenya.ingrediet.data.network

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    private val key = "refresh_token"

    suspend fun saveRefreshToken(token: String) {
        withContext(Dispatchers.IO) {
            prefs.getString(key, null)
        }
    }

    suspend fun getRefreshToken(): String? {
        return withContext(Dispatchers.IO) {
            prefs.getString(key, null)
        }
    }

    suspend fun clearSession() {
        withContext(Dispatchers.IO) {
            prefs.edit().remove(key).apply()
        }
    }
}