package com.subscriptionremover.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subscriptionremover.data.models.User
import com.subscriptionremover.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    val currentUser = authRepository.currentUser
    val isAuthenticated = authRepository.isAuthenticated

    sealed class AuthState {
        object Idle : AuthState()
        object Loading : AuthState()
        data class Success(val user: User) : AuthState()
        data class Error(val message: String) : AuthState()
        object PasswordResetSent : AuthState()
        object SignedOut : AuthState()
    }

    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            val result = authRepository.signInWithEmail(email, password)
            _authState.value = result.fold(
                onSuccess = { user -> AuthState.Success(user) },
                onFailure = { exception -> AuthState.Error(exception.message ?: "Sign in failed") }
            )
        }
    }

    fun signUpWithEmail(email: String, password: String, fullName: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            val result = authRepository.signUpWithEmail(email, password, fullName)
            _authState.value = result.fold(
                onSuccess = { user -> AuthState.Success(user) },
                onFailure = { exception -> AuthState.Error(exception.message ?: "Sign up failed") }
            )
        }
    }

    fun signInWithGoogle() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            val result = authRepository.signInWithGoogle()
            _authState.value = result.fold(
                onSuccess = { user -> AuthState.Success(user) },
                onFailure = { exception -> AuthState.Error(exception.message ?: "Google sign in failed") }
            )
        }
    }

    fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            val result = authRepository.sendPasswordResetEmail(email)
            _authState.value = result.fold(
                onSuccess = { AuthState.PasswordResetSent },
                onFailure = { exception -> AuthState.Error(exception.message ?: "Failed to send reset email") }
            )
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _authState.value = AuthState.SignedOut
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            val result = authRepository.deleteAccount()
            _authState.value = result.fold(
                onSuccess = { AuthState.SignedOut },
                onFailure = { exception -> AuthState.Error(exception.message ?: "Failed to delete account") }
            )
        }
    }

    fun updateUserProfile(user: User) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            val result = authRepository.updateUserProfile(user)
            _authState.value = result.fold(
                onSuccess = { AuthState.Success(user) },
                onFailure = { exception -> AuthState.Error(exception.message ?: "Failed to update profile") }
            )
        }
    }

    fun refreshUserData() {
        viewModelScope.launch {
            val result = authRepository.refreshUserData()
            result.fold(
                onSuccess = { user ->
                    user?.let { _authState.value = AuthState.Success(it) }
                },
                onFailure = { exception ->
                    _authState.value = AuthState.Error(exception.message ?: "Failed to refresh user data")
                }
            )
        }
    }

    fun clearAuthState() {
        _authState.value = AuthState.Idle
    }
}
