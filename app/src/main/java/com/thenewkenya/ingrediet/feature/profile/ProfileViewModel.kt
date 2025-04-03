package com.thenewkenya.ingrediet.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thenewkenya.ingrediet.data.model.Profile
import com.thenewkenya.ingrediet.data.network.AuthManager
import com.thenewkenya.ingrediet.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val profileRepository: ProfileRepository,
    private val authManager: AuthManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _profile = MutableStateFlow<Profile?>(null)
    val profile: StateFlow<Profile?> = _profile.asStateFlow()

    init {
        fetchProfile()
    }

    fun fetchProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            profileRepository.getProfile().collect { result ->
                result.fold(
                    onSuccess = { profile ->
                        _profile.value = profile
                        _uiState.value = ProfileUiState.Success(profile)
                    },
                    onFailure = { error ->
                        _uiState.value = ProfileUiState.Error(error.message ?: "Unknown error")
                    }
                )
            }
        }
    }

    // Add this method to update the profile without making a network call
    fun setProfile(profile: Profile) {
        _profile.value = profile
    }

    fun updateProfile(updatedProfile: Profile) {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            profileRepository.updateProfile(updatedProfile).collect { result ->
                result.fold(
                    onSuccess = {
                        _profile.value = updatedProfile
                        _uiState.value = ProfileUiState.Success(updatedProfile)
                    },
                    onFailure = { error ->
                        _uiState.value = ProfileUiState.Error(error.message ?: "Failed to update profile")
                    }
                )
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authManager.signOut()
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            try {
                // First sign out the user which will delete the session
                authManager.signOut()
                // Set error state with "Account deleted" message to trigger navigation
                _uiState.value = ProfileUiState.Error("Account deleted")
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error(e.message ?: "Failed to delete account")
            }
        }
    }
}

sealed class ProfileUiState {
    data object Loading : ProfileUiState()
    data class Success(val profile: Profile) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}