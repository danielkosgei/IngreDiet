package com.thenewkenya.ingrediet.data.network

import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import android.util.Log

class AppLockManager(
    private val context: Context,
    private val userPreferencesManager: UserPreferencesManager,
    private val biometricAuthManager: BiometricAuthManager
) : DefaultLifecycleObserver {
    
    private val _isAppLocked = MutableStateFlow(false)
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()
    
    private val _showAppLockScreen = MutableStateFlow(false)
    val showAppLockScreen: StateFlow<Boolean> = _showAppLockScreen.asStateFlow()
    
    private val _appLockAuthResult = MutableStateFlow<BiometricAuthResult?>(null)
    val appLockAuthResult: StateFlow<BiometricAuthResult?> = _appLockAuthResult.asStateFlow()
    
    private var lastPauseTime: Long = 0
    private val lockTimeoutMs = 0L // Immediate lock when app goes to background
    
    init {
        Log.d("AppLockManager", "AppLockManager initialized")
    }
    
    /**
     * Check if app lock is enabled in user preferences
     */
    suspend fun isAppLockEnabled(): Boolean {
        return userPreferencesManager.getBiometricEnabled()
    }
    
    /**
     * Lock the app immediately
     */
    fun lockApp() {
        Log.d("AppLockManager", "Locking app")
        _isAppLocked.value = true
        _showAppLockScreen.value = true
    }
    
    /**
     * Unlock the app after successful authentication
     */
    fun unlockApp() {
        Log.d("AppLockManager", "Unlocking app")
        _isAppLocked.value = false
        _showAppLockScreen.value = false
        _appLockAuthResult.value = null
    }
    
    /**
     * Show the app lock screen for authentication
     */
    fun showLockScreen() {
        Log.d("AppLockManager", "Showing lock screen")
        _showAppLockScreen.value = true
    }
    
    /**
     * Hide the app lock screen
     */
    fun hideLockScreen() {
        Log.d("AppLockManager", "Hiding lock screen")
        _showAppLockScreen.value = false
    }
    
    /**
     * Handle biometric authentication result for app lock
     */
    fun handleAuthResult(result: BiometricAuthResult) {
        Log.d("AppLockManager", "Handling auth result: $result")
        _appLockAuthResult.value = result
        when (result) {
            is BiometricAuthResult.Success -> {
                unlockApp()
            }
            is BiometricAuthResult.Failed,
            is BiometricAuthResult.Error -> {
                // Keep the app locked
                _isAppLocked.value = true
            }
        }
    }
    
    /**
     * Check if app should be locked on app resume
     */
    suspend fun checkAppLockOnResume() {
        if (!isAppLockEnabled()) {
            Log.d("AppLockManager", "App lock disabled, not locking")
            return
        }
        
        if (biometricAuthManager.isBiometricAvailable() != BiometricAvailability.AVAILABLE) {
            Log.d("AppLockManager", "Biometric not available, not locking")
            return
        }
        
        val timeSincePause = System.currentTimeMillis() - lastPauseTime
        Log.d("AppLockManager", "Time since pause: ${timeSincePause}ms, threshold: ${lockTimeoutMs}ms")
        
        if (lastPauseTime > 0 && timeSincePause >= lockTimeoutMs) {
            lockApp()
        }
    }
    
    /**
     * Called when app starts for the first time
     */
    suspend fun checkAppLockOnStart() {
        if (!isAppLockEnabled()) {
            Log.d("AppLockManager", "App lock disabled on start")
            return
        }
        
        if (biometricAuthManager.isBiometricAvailable() != BiometricAvailability.AVAILABLE) {
            Log.d("AppLockManager", "Biometric not available on start")
            return
        }
        
        Log.d("AppLockManager", "App lock enabled, locking on start")
        lockApp()
    }
    
    // Lifecycle callbacks
    override fun onResume(owner: LifecycleOwner) {
        Log.d("AppLockManager", "App resumed")
        // Check app lock will be handled in MainActivity
    }
    
    override fun onPause(owner: LifecycleOwner) {
        Log.d("AppLockManager", "App paused")
        lastPauseTime = System.currentTimeMillis()
    }
    
    override fun onStop(owner: LifecycleOwner) {
        Log.d("AppLockManager", "App stopped")
        // App goes to background, prepare for potential lock on resume
    }
} 