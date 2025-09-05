package com.subscriptionremover.domain.repository

import com.subscriptionremover.data.models.*
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

interface SubscriptionRepository {
    
    // Basic CRUD operations
    fun getAllActiveSubscriptions(): Flow<List<Subscription>>
    fun getAllSubscriptions(): Flow<List<Subscription>>
    fun getSubscriptionById(id: String): Flow<Subscription?>
    suspend fun insertSubscription(subscription: Subscription): Result<Unit>
    suspend fun updateSubscription(subscription: Subscription): Result<Unit>
    suspend fun deleteSubscription(subscription: Subscription): Result<Unit>
    
    // Category-based queries
    fun getSubscriptionsByCategory(category: SubscriptionCategory): Flow<List<Subscription>>
    fun getCategoryDistribution(): Flow<Map<SubscriptionCategory, Int>>
    
    // Financial queries
    fun getTotalMonthlySpending(): Flow<BigDecimal>
    fun getSpendingByCategory(): Flow<Map<SubscriptionCategory, BigDecimal>>
    suspend fun getAverageSubscriptionPrice(): BigDecimal
    
    // Analytics and insights
    fun getActiveSubscriptionCount(): Flow<Int>
    suspend fun getUnusedSubscriptions(thresholdDays: Int = 30): List<Subscription>
    suspend fun getUpcomingBills(days: Int = 7): List<Subscription>
    fun getRecentSubscriptions(): Flow<List<Subscription>>
    
    // Batch operations
    suspend fun cancelSubscriptions(subscriptionIds: List<String>): Result<Int>
    suspend fun scheduleSubscriptionCancellations(
        subscriptionIds: List<String>, 
        cancellationDate: LocalDateTime
    ): Result<Int>
    suspend fun cancelScheduledCancellations(subscriptionIds: List<String>): Result<Int>
    
    // Usage tracking
    suspend fun updateLastUsedDate(subscriptionId: String, lastUsed: LocalDateTime): Result<Unit>
    suspend fun updateLastUsedByPackageName(packageName: String, lastUsed: LocalDateTime): Result<Unit>
    suspend fun getTrackableSubscriptions(): List<Subscription>
    
    // Search and filtering
    fun searchSubscriptions(query: String): Flow<List<Subscription>>
    fun getSubscriptionsByPriceRange(minPrice: BigDecimal, maxPrice: BigDecimal): Flow<List<Subscription>>
    
    // Recommendations
    suspend fun generateRecommendations(): List<SubscriptionRecommendation>
    suspend fun getSubscriptionUsageStats(subscriptionId: String): SubscriptionUsage?
    
    // Data management
    suspend fun syncWithCloud(): Result<Unit>
    suspend fun exportSubscriptions(): Result<String>
    suspend fun importSubscriptions(data: String): Result<Int>
}
