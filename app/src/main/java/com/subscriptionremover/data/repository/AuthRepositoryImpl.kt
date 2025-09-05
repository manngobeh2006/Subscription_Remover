package com.subscriptionremover.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.subscriptionremover.data.database.UserDao
import com.subscriptionremover.data.models.User
import com.subscriptionremover.domain.repository.AuthRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userDao: UserDao
) : AuthRepository {

    override val currentUser: Flow<User?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser?.toUser())
        }
        firebaseAuth.addAuthStateListener(authStateListener)
        awaitClose { firebaseAuth.removeAuthStateListener(authStateListener) }
    }

    override val isAuthenticated: Flow<Boolean> = currentUser.map { it != null }

    override suspend fun signInWithEmail(email: String, password: String): Result<User> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = result.user?.toUser() ?: throw Exception("User data not available")
            
            // Update local database
            updateLocalUser(user.copy(lastLoginDate = LocalDateTime.now()))
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signUpWithEmail(email: String, password: String, fullName: String): Result<User> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: throw Exception("User creation failed")
            
            // Update Firebase profile
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(fullName)
                .build()
            firebaseUser.updateProfile(profileUpdates).await()
            
            val user = firebaseUser.toUser().copy(displayName = fullName)
            
            // Start free trial
            val userWithTrial = user.copy(
                trialStartDate = LocalDateTime.now(),
                trialEndDate = LocalDateTime.now().plusDays(7)
            )
            
            // Save to Firestore and local database
            saveUserToFirestore(userWithTrial)
            updateLocalUser(userWithTrial)
            
            Result.success(userWithTrial)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInWithGoogle(): Result<User> {
        return try {
            // This would need to be implemented with Google Sign-In SDK
            // For now, returning a failure
            Result.failure(Exception("Google Sign-In not implemented yet"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }

    override suspend fun deleteAccount(): Result<Unit> {
        return try {
            val currentFirebaseUser = firebaseAuth.currentUser
            if (currentFirebaseUser != null) {
                // Delete from Firestore
                firestore.collection("users").document(currentFirebaseUser.uid).delete().await()
                
                // Delete from local database
                userDao.deleteUserById(currentFirebaseUser.uid)
                
                // Delete Firebase account
                currentFirebaseUser.delete().await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCurrentUser(): User? {
        return firebaseAuth.currentUser?.toUser()?.let { firebaseUser ->
            // Try to get from local database first, then Firestore
            userDao.getUserById(firebaseUser.uid) ?: run {
                try {
                    val firestoreUser = getFirestoreUser(firebaseUser.uid)
                    firestoreUser?.let { updateLocalUser(it) }
                    firestoreUser
                } catch (e: Exception) {
                    firebaseUser
                }
            }
        }
    }

    override suspend fun updateUserProfile(user: User): Result<Unit> {
        return try {
            saveUserToFirestore(user)
            updateLocalUser(user)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun refreshUserData(): Result<User?> {
        return try {
            val currentFirebaseUser = firebaseAuth.currentUser
            if (currentFirebaseUser != null) {
                val firestoreUser = getFirestoreUser(currentFirebaseUser.uid)
                firestoreUser?.let { updateLocalUser(it) }
                Result.success(firestoreUser)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun FirebaseUser.toUser(): User {
        return User(
            uid = uid,
            email = email ?: "",
            displayName = displayName,
            photoUrl = photoUrl?.toString(),
            createdAt = LocalDateTime.now(),
            lastLoginDate = LocalDateTime.now()
        )
    }

    private suspend fun saveUserToFirestore(user: User) {
        val userMap = mapOf(
            "uid" to user.uid,
            "email" to user.email,
            "displayName" to user.displayName,
            "photoUrl" to user.photoUrl,
            "isPremium" to user.isPremium,
            "trialStartDate" to user.trialStartDate?.toString(),
            "trialEndDate" to user.trialEndDate?.toString(),
            "subscriptionStartDate" to user.subscriptionStartDate?.toString(),
            "subscriptionEndDate" to user.subscriptionEndDate?.toString(),
            "createdAt" to user.createdAt.toString(),
            "lastLoginDate" to user.lastLoginDate.toString(),
            "notificationSettings" to user.notificationSettings,
            "preferences" to user.preferences
        )
        
        firestore.collection("users").document(user.uid).set(userMap).await()
    }

    private suspend fun getFirestoreUser(uid: String): User? {
        return try {
            val document = firestore.collection("users").document(uid).get().await()
            if (document.exists()) {
                val data = document.data ?: return null
                User(
                    uid = data["uid"] as String,
                    email = data["email"] as String,
                    displayName = data["displayName"] as? String,
                    photoUrl = data["photoUrl"] as? String,
                    isPremium = data["isPremium"] as? Boolean ?: false,
                    trialStartDate = (data["trialStartDate"] as? String)?.let { LocalDateTime.parse(it) },
                    trialEndDate = (data["trialEndDate"] as? String)?.let { LocalDateTime.parse(it) },
                    subscriptionStartDate = (data["subscriptionStartDate"] as? String)?.let { LocalDateTime.parse(it) },
                    subscriptionEndDate = (data["subscriptionEndDate"] as? String)?.let { LocalDateTime.parse(it) },
                    createdAt = LocalDateTime.parse(data["createdAt"] as String),
                    lastLoginDate = LocalDateTime.parse(data["lastLoginDate"] as String)
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun updateLocalUser(user: User) {
        userDao.insertUser(user)
    }
}
