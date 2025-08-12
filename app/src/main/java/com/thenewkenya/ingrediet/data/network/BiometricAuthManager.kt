package com.thenewkenya.ingrediet.data.network

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

class BiometricAuthManager(private val context: Context) {
    
    private val _authResults = Channel<BiometricAuthResult>()
    val authResults: Flow<BiometricAuthResult> = _authResults.receiveAsFlow()
    
    /**
     * Check if biometric authentication is available on the device
     */
    fun isBiometricAvailable(): BiometricAvailability {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricAvailability.AVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricAvailability.NO_HARDWARE
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricAvailability.HARDWARE_UNAVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricAvailability.NONE_ENROLLED
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> BiometricAvailability.SECURITY_UPDATE_REQUIRED
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> BiometricAvailability.UNSUPPORTED
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> BiometricAvailability.UNKNOWN
            else -> BiometricAvailability.UNKNOWN
        }
    }
    
    /**
     * Show biometric prompt for authentication
     */
    fun authenticate(
        activity: FragmentActivity,
        title: String = "Fingerprint Authentication",
        subtitle: String = "Use your fingerprint to authenticate",
        negativeButtonText: String = "Cancel"
    ) {
        val executor = ContextCompat.getMainExecutor(context)
        val biometricPrompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                _authResults.trySend(BiometricAuthResult.Error(errString.toString()))
            }
            
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                _authResults.trySend(BiometricAuthResult.Success)
            }
            
            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                _authResults.trySend(BiometricAuthResult.Failed)
            }
        })
        
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(negativeButtonText)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()
        
        biometricPrompt.authenticate(promptInfo)
    }
    
    /**
     * Show biometric prompt for authentication with Context (attempts to cast to FragmentActivity)
     */
    fun authenticateWithContext(
        context: Context,
        title: String = "Fingerprint Authentication",
        subtitle: String = "Use your fingerprint to authenticate",
        negativeButtonText: String = "Cancel"
    ): Boolean {
        val activity = context as? FragmentActivity
        return if (activity != null) {
            authenticate(activity, title, subtitle, negativeButtonText)
            true
        } else {
            _authResults.trySend(BiometricAuthResult.Error("Unable to show biometric prompt: Activity context required"))
            false
        }
    }
    
    /**
     * Get user-friendly message for biometric availability status
     */
    fun getAvailabilityMessage(availability: BiometricAvailability): String {
        return when (availability) {
            BiometricAvailability.AVAILABLE -> "Fingerprint authentication is available"
            BiometricAvailability.NO_HARDWARE -> "This device doesn't have fingerprint hardware"
            BiometricAvailability.HARDWARE_UNAVAILABLE -> "Fingerprint hardware is currently unavailable"
            BiometricAvailability.NONE_ENROLLED -> "No fingerprint enrolled. Please set up fingerprint unlock in device settings"
            BiometricAvailability.SECURITY_UPDATE_REQUIRED -> "Security update required for fingerprint authentication"
            BiometricAvailability.UNSUPPORTED -> "Fingerprint authentication is not supported"
            BiometricAvailability.UNKNOWN -> "Fingerprint authentication status unknown"
        }
    }
}

enum class BiometricAvailability {
    AVAILABLE,
    NO_HARDWARE,
    HARDWARE_UNAVAILABLE,
    NONE_ENROLLED,
    SECURITY_UPDATE_REQUIRED,
    UNSUPPORTED,
    UNKNOWN
}

sealed class BiometricAuthResult {
    object Success : BiometricAuthResult()
    object Failed : BiometricAuthResult()
    data class Error(val message: String) : BiometricAuthResult()
} 