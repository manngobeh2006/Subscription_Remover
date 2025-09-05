package com.subscriptionremover.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.subscriptionremover.data.database.SubscriptionDao
import com.subscriptionremover.data.models.*
import com.subscriptionremover.domain.repository.SubscriptionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.math.BigDecimal
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionRepositoryImpl @Inject constructor(
    private val subscriptionDao: SubscriptionDao,
    private val firestore: FirebaseFirestore
) : SubscriptionRepository {

    override fun getAllActiveSubscriptions(): Flow<List<Subscription>> {
        return subscriptionDao.getAllActiveSubscriptions()
    }

    override fun getAllSubscriptions(): Flow<List<Subscription>> {
        return subscriptionDao.getAllSubscriptions()
    }

    override fun getSubscriptionById(id: String): Flow<Subscription?> {
        return subscriptionDao.getSubscriptionByIdFlow(id)
    }

    override suspend fun insertSubscription(subscription: Subscription): Result<Unit> {
        return try {
            subscriptionDao.insertSubscription(subscription)
            
            // Sync to cloud
            syncSubscriptionToCloud(subscription)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateSubscription(subscription: Subscription): Result<Unit> {
        return try {
            val updatedSubscription = subscription.copy(updatedAt = LocalDateTime.now())
            subscriptionDao.updateSubscription(updatedSubscription)
            
            // Sync to cloud
            syncSubscriptionToCloud(updatedSubscription)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteSubscription(subscription: Subscription): Result<Unit> {
        return try {
            subscriptionDao.deleteSubscription(subscription)
            
            // Remove from cloud
            firestore.collection("subscriptions").document(subscription.id).delete().await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getSubscriptionsByCategory(category: SubscriptionCategory): Flow<List<Subscription>> {
        return subscriptionDao.getSubscriptionsByCategory(category)
    }

    override fun getCategoryDistribution(): Flow<Map<SubscriptionCategory, Int>> {
        return subscriptionDao.getCategoryDistribution()
    }

    override fun getTotalMonthlySpending(): Flow<BigDecimal> {
        return subscriptionDao.getTotalMonthlySpending().map { it ?: BigDecimal.ZERO }
    }

    override fun getSpendingByCategory(): Flow<Map<SubscriptionCategory, BigDecimal>> {
        return subscriptionDao.getSpendingByCategory()
    }

    override suspend fun getAverageSubscriptionPrice(): BigDecimal {
        return subscriptionDao.getAverageSubscriptionPrice() ?: BigDecimal.ZERO
    }

    override fun getActiveSubscriptionCount(): Flow<Int> {
        return subscriptionDao.getActiveSubscriptionCount()
    }

    override suspend fun getUnusedSubscriptions(thresholdDays: Int): List<Subscription> {
        val threshold = LocalDateTime.now().minusDays(thresholdDays.toLong())
        return subscriptionDao.getUnusedSubscriptions(threshold)
    }

    override suspend fun getUpcomingBills(days: Int): List<Subscription> {
        return subscriptionDao.getSubscriptionsDueForBilling(days)
    }

    override fun getRecentSubscriptions(): Flow<List<Subscription>> {
        return subscriptionDao.getAllActiveSubscriptions().map { subscriptions ->
            subscriptions.sortedByDescending { it.createdAt }.take(10)
        }
    }

    override suspend fun cancelSubscriptions(subscriptionIds: List<String>): Result<Int> {
        return try {
            subscriptionDao.deactivateSubscriptions(subscriptionIds, LocalDateTime.now())
            Result.success(subscriptionIds.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun scheduleSubscriptionCancellations(
        subscriptionIds: List<String>,
        cancellationDate: LocalDateTime
    ): Result<Int> {
        return try {
            subscriptionDao.scheduleSubscriptionCancellations(
                subscriptionIds, 
                cancellationDate, 
                LocalDateTime.now()
            )
            Result.success(subscriptionIds.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cancelScheduledCancellations(subscriptionIds: List<String>): Result<Int> {
        return try {
            subscriptionDao.cancelScheduledCancellations(subscriptionIds, LocalDateTime.now())
            Result.success(subscriptionIds.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateLastUsedDate(subscriptionId: String, lastUsed: LocalDateTime): Result<Unit> {
        return try {
            val subscription = subscriptionDao.getSubscriptionById(subscriptionId)
            subscription?.let {
                val updated = it.copy(
                    lastUsedDate = lastUsed,
                    updatedAt = LocalDateTime.now()
                )
                subscriptionDao.updateSubscription(updated)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateLastUsedByPackageName(packageName: String, lastUsed: LocalDateTime): Result<Unit> {
        return try {
            subscriptionDao.updateLastUsedByPackage(packageName, "%$packageName%", lastUsed)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTrackableSubscriptions(): List<Subscription> {
        return subscriptionDao.getTrackableSubscriptions()
    }

    override fun searchSubscriptions(query: String): Flow<List<Subscription>> {
        return subscriptionDao.searchSubscriptions(query)
    }

    override fun getSubscriptionsByPriceRange(minPrice: BigDecimal, maxPrice: BigDecimal): Flow<List<Subscription>> {
        return subscriptionDao.getSubscriptionsByPriceRange(minPrice, maxPrice)
    }

    override suspend fun generateRecommendations(): List<SubscriptionRecommendation> {
        val recommendations = mutableListOf<SubscriptionRecommendation>()
        
        // Find unused subscriptions
        val unusedSubscriptions = getUnusedSubscriptions(30)
        unusedSubscriptions.forEach { subscription ->
            recommendations.add(
                SubscriptionRecommendation(
                    subscriptionId = subscription.id,
                    recommendationType = RecommendationType.CANCEL_UNUSED,
                    reason = "You haven't used ${subscription.name} for ${subscription.getDaysSinceLastUsed()} days",
                    potentialSavings = subscription.getMonthlyEquivalentPrice(),
                    confidence = if (subscription.getDaysSinceLastUsed() > 60) 0.9f else 0.7f
                )
            )
        }
        
        // Find duplicate subscriptions (same category, similar names)
        val activeSubscriptions = subscriptionDao.getAllActiveSubscriptions()
        // Implementation for duplicate detection would go here
        
        return recommendations
    }

    override suspend fun getSubscriptionUsageStats(subscriptionId: String): SubscriptionUsage? {
        val subscription = subscriptionDao.getSubscriptionById(subscriptionId) ?: return null
        
        return SubscriptionUsage(
            subscriptionId = subscriptionId,
            lastOpenDate = subscription.lastUsedDate,
            daysSinceLastUse = subscription.getDaysSinceLastUsed(),
            usageFrequency = determineUsageFrequency(subscription)
        )
    }

    override suspend fun syncWithCloud(): Result<Unit> {
        return try {
            val allSubscriptions = subscriptionDao.getAllSubscriptions()
            // Sync implementation would go here
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun exportSubscriptions(): Result<String> {
        return try {
            // Implementation for data export
            Result.success("Export functionality not yet implemented")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun importSubscriptions(data: String): Result<Int> {
        return try {
            // Implementation for data import
            Result.success(0)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun syncSubscriptionToCloud(subscription: Subscription) {
        try {
            val subscriptionMap = mapOf(
                "id" to subscription.id,
                "name" to subscription.name,
                "description" to subscription.description,
                "category" to subscription.category.name,
                "monthlyPrice" to subscription.monthlyPrice.toString(),
                "billingCycle" to subscription.billingCycle.name,
                "nextBillingDate" to subscription.nextBillingDate.toString(),
                "websiteUrl" to subscription.websiteUrl,
                "cancellationUrl" to subscription.cancellationUrl,
                "logoUrl" to subscription.logoUrl,
                "isActive" to subscription.isActive,
                "lastUsedDate" to subscription.lastUsedDate?.toString(),
                "createdAt" to subscription.createdAt.toString(),
                "updatedAt" to subscription.updatedAt.toString(),
                "scheduledCancellationDate" to subscription.scheduledCancellationDate?.toString(),
                "usageTrackingEnabled" to subscription.usageTrackingEnabled,
                "reminderFrequency" to subscription.reminderFrequency.name,
                "totalSpent" to subscription.totalSpent.toString(),
                "platformIdentifier" to subscription.platformIdentifier,
                "packageName" to subscription.packageName,
                "notes" to subscription.notes
            )
            
            firestore.collection("subscriptions").document(subscription.id).set(subscriptionMap).await()
        } catch (e: Exception) {
            // Log error but don't fail the operation
        }
    }

    private fun determineUsageFrequency(subscription: Subscription): UsageFrequency {
        val daysSinceLastUsed = subscription.getDaysSinceLastUsed()
        
        return when {
            daysSinceLastUsed == -1 -> UsageFrequency.UNKNOWN
            daysSinceLastUsed <= 1 -> UsageFrequency.DAILY
            daysSinceLastUsed <= 7 -> UsageFrequency.WEEKLY
            daysSinceLastUsed <= 30 -> UsageFrequency.MONTHLY
            daysSinceLastUsed <= 90 -> UsageFrequency.RARELY
            else -> UsageFrequency.NEVER
        }
    }
}
