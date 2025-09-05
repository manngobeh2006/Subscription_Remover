package com.subscriptionremover.domain.repository

import com.subscriptionremover.data.models.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    
    val currentUser: Flow<User?>
    val isAuthenticated: Flow<Boolean>
    
    suspend fun signInWithEmail(email: String, password: String): Result<User>
    suspend fun signUpWithEmail(email: String, password: String, fullName: String): Result<User>
    suspend fun signInWithGoogle(): Result<User>
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    suspend fun signOut()
    suspend fun deleteAccount(): Result<Unit>
    
    suspend fun getCurrentUser(): User?
    suspend fun updateUserProfile(user: User): Result<Unit>
    suspend fun refreshUserData(): Result<User?>
}
