package com.subscriptionremover.data.database

import androidx.room.*
import com.subscriptionremover.data.models.*
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal
import java.time.LocalDateTime

@Dao
interface SubscriptionDao {
    
    // Basic CRUD operations
    @Query("SELECT * FROM subscriptions WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getAllActiveSubscriptions(): Flow<List<Subscription>>
    
    @Query("SELECT * FROM subscriptions ORDER BY createdAt DESC")
    fun getAllSubscriptions(): Flow<List<Subscription>>
    
    @Query("SELECT * FROM subscriptions WHERE id = :id")
    suspend fun getSubscriptionById(id: String): Subscription?
    
    @Query("SELECT * FROM subscriptions WHERE id = :id")
    fun getSubscriptionByIdFlow(id: String): Flow<Subscription?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscription(subscription: Subscription)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscriptions(subscriptions: List<Subscription>)
    
    @Update
    suspend fun updateSubscription(subscription: Subscription)
    
    @Delete
    suspend fun deleteSubscription(subscription: Subscription)
    
    @Query("DELETE FROM subscriptions WHERE id = :id")
    suspend fun deleteSubscriptionById(id: String)
    
    // Category-based queries
    @Query("SELECT * FROM subscriptions WHERE category = :category AND isActive = 1 ORDER BY createdAt DESC")
    fun getSubscriptionsByCategory(category: SubscriptionCategory): Flow<List<Subscription>>
    
    @Query("SELECT category, COUNT(*) as count FROM subscriptions WHERE isActive = 1 GROUP BY category ORDER BY count DESC")
    fun getCategoryDistribution(): Flow<Map<SubscriptionCategory, Int>>
    
    // Usage and activity queries
    @Query("SELECT * FROM subscriptions WHERE lastUsedDate IS NULL OR lastUsedDate < :threshold ORDER BY createdAt DESC")
    suspend fun getUnusedSubscriptions(threshold: LocalDateTime): List<Subscription>
    
    @Query("SELECT * FROM subscriptions WHERE scheduledCancellationDate IS NOT NULL AND scheduledCancellationDate <= :date")
    suspend fun getScheduledCancellations(date: LocalDateTime): List<Subscription>
    
    @Query("UPDATE subscriptions SET lastUsedDate = :lastUsed WHERE packageName = :packageName OR name LIKE :namePattern")
    suspend fun updateLastUsedByPackage(packageName: String, namePattern: String, lastUsed: LocalDateTime)
    
    // Financial queries
    @Query("SELECT SUM(monthlyPrice) FROM subscriptions WHERE isActive = 1")
    fun getTotalMonthlySpending(): Flow<BigDecimal?>
    
    @Query("SELECT category, SUM(monthlyPrice) as total FROM subscriptions WHERE isActive = 1 GROUP BY category ORDER BY total DESC")
    fun getSpendingByCategory(): Flow<Map<SubscriptionCategory, BigDecimal>>
    
    @Query("SELECT AVG(monthlyPrice) FROM subscriptions WHERE isActive = 1")
    suspend fun getAverageSubscriptionPrice(): BigDecimal?
    
    // Search and filtering
    @Query("""
        SELECT * FROM subscriptions 
        WHERE (name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%')
        AND isActive = 1
        ORDER BY 
            CASE WHEN name LIKE :query || '%' THEN 1 ELSE 2 END,
            createdAt DESC
    """)
    fun searchSubscriptions(query: String): Flow<List<Subscription>>
    
    @Query("SELECT * FROM subscriptions WHERE isActive = 1 AND monthlyPrice BETWEEN :minPrice AND :maxPrice ORDER BY monthlyPrice ASC")
    fun getSubscriptionsByPriceRange(minPrice: BigDecimal, maxPrice: BigDecimal): Flow<List<Subscription>>
    
    // Billing and reminders
    @Query("SELECT * FROM subscriptions WHERE nextBillingDate BETWEEN :startDate AND :endDate AND isActive = 1 ORDER BY nextBillingDate ASC")
    fun getUpcomingBills(startDate: String, endDate: String): Flow<List<Subscription>>
    
    @Query("SELECT * FROM subscriptions WHERE nextBillingDate <= date('now', '+' || :days || ' days') AND isActive = 1")
    suspend fun getSubscriptionsDueForBilling(days: Int): List<Subscription>
    
    // Analytics and statistics
    @Query("SELECT COUNT(*) FROM subscriptions WHERE isActive = 1")
    fun getActiveSubscriptionCount(): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM subscriptions")
    fun getTotalSubscriptionCount(): Flow<Int>
    
    @Query("""
        SELECT COUNT(*) FROM subscriptions 
        WHERE isActive = 1 AND (
            lastUsedDate IS NULL OR 
            lastUsedDate < datetime('now', '-30 days')
        )
    """)
    suspend fun getUnusedSubscriptionCount(): Int
    
    // Batch operations
    @Query("UPDATE subscriptions SET isActive = 0, updatedAt = :updatedAt WHERE id IN (:ids)")
    suspend fun deactivateSubscriptions(ids: List<String>, updatedAt: LocalDateTime)
    
    @Query("UPDATE subscriptions SET scheduledCancellationDate = :cancellationDate, updatedAt = :updatedAt WHERE id IN (:ids)")
    suspend fun scheduleSubscriptionCancellations(ids: List<String>, cancellationDate: LocalDateTime, updatedAt: LocalDateTime)
    
    @Query("UPDATE subscriptions SET scheduledCancellationDate = NULL, updatedAt = :updatedAt WHERE id IN (:ids)")
    suspend fun cancelScheduledCancellations(ids: List<String>, updatedAt: LocalDateTime)
    
    // Data validation and cleanup
    @Query("DELETE FROM subscriptions WHERE isActive = 0 AND updatedAt < :threshold")
    suspend fun cleanupOldInactiveSubscriptions(threshold: LocalDateTime)
    
    @Query("UPDATE subscriptions SET totalSpent = monthlyPrice * :months WHERE id = :id")
    suspend fun updateTotalSpent(id: String, months: Int)
    
    // Custom subscription tracking
    @Query("SELECT * FROM subscriptions WHERE packageName IS NOT NULL AND isActive = 1")
    suspend fun getTrackableSubscriptions(): List<Subscription>
    
    @Query("UPDATE subscriptions SET usageTrackingEnabled = :enabled WHERE id = :id")
    suspend fun updateUsageTracking(id: String, enabled: Boolean)
}
