package com.subscriptionremover.data.database

import androidx.room.*
import com.subscriptionremover.data.models.User
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface UserDao {
    
    @Query("SELECT * FROM users WHERE uid = :uid")
    suspend fun getUserById(uid: String): User?
    
    @Query("SELECT * FROM users WHERE uid = :uid")
    fun getUserByIdFlow(uid: String): Flow<User?>
    
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)
    
    @Update
    suspend fun updateUser(user: User)
    
    @Delete
    suspend fun deleteUser(user: User)
    
    @Query("DELETE FROM users WHERE uid = :uid")
    suspend fun deleteUserById(uid: String)
    
    @Query("UPDATE users SET lastLoginDate = :loginDate WHERE uid = :uid")
    suspend fun updateLastLoginDate(uid: String, loginDate: LocalDateTime)
    
    @Query("UPDATE users SET isPremium = :isPremium WHERE uid = :uid")
    suspend fun updatePremiumStatus(uid: String, isPremium: Boolean)
    
    @Query("""
        UPDATE users 
        SET trialStartDate = :startDate, trialEndDate = :endDate 
        WHERE uid = :uid
    """)
    suspend fun updateTrialPeriod(uid: String, startDate: LocalDateTime, endDate: LocalDateTime)
    
    @Query("""
        UPDATE users 
        SET subscriptionStartDate = :startDate, subscriptionEndDate = :endDate, isPremium = 1 
        WHERE uid = :uid
    """)
    suspend fun updateSubscriptionPeriod(uid: String, startDate: LocalDateTime, endDate: LocalDateTime)
    
    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int
    
    @Query("SELECT COUNT(*) FROM users WHERE isPremium = 1")
    suspend fun getPremiumUserCount(): Int
    
    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsers(): Flow<List<User>>
}
