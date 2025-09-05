package com.subscriptionremover.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(tableName = "subscriptions")
data class Subscription(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val description: String? = null,
    val category: SubscriptionCategory,
    val monthlyPrice: BigDecimal,
    val billingCycle: BillingCycle,
    val nextBillingDate: LocalDate,
    val websiteUrl: String? = null,
    val cancellationUrl: String? = null,
    val logoUrl: String? = null,
    val isActive: Boolean = true,
    val lastUsedDate: LocalDateTime? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val scheduledCancellationDate: LocalDateTime? = null,
    val usageTrackingEnabled: Boolean = true,
    val reminderFrequency: ReminderFrequency = ReminderFrequency.WEEKLY,
    val totalSpent: BigDecimal = BigDecimal.ZERO,
    val platformIdentifier: String? = null, // For automatic detection
    val packageName: String? = null, // Android package name for usage tracking
    val notes: String? = null
)

enum class SubscriptionCategory(val displayName: String, val colorHex: String) {
    ENTERTAINMENT("Entertainment", "#8B5CF6"),
    SOCIAL_MEDIA("Social Media", "#EC4899"),
    PRODUCTIVITY("Productivity", "#3B82F6"),
    FITNESS("Fitness & Health", "#10B981"),
    NEWS("News & Magazines", "#F59E0B"),
    GAMING("Gaming", "#EF4444"),
    SHOPPING("Shopping", "#8B5CF6"),
    MUSIC("Music", "#06B6D4"),
    VIDEO_STREAMING("Video Streaming", "#8B5CF6"),
    CLOUD_STORAGE("Cloud Storage", "#6B7280"),
    DATING("Dating", "#EC4899"),
    FOOD_DELIVERY("Food Delivery", "#F59E0B"),
    MISCELLANEOUS("Miscellaneous", "#6B7280")
}

enum class BillingCycle(val displayName: String, val monthsMultiplier: Int) {
    WEEKLY("Weekly", 0), // Special case
    MONTHLY("Monthly", 1),
    QUARTERLY("Quarterly", 3),
    BIANNUALLY("Bi-annually", 6),
    ANNUALLY("Annually", 12)
}

enum class ReminderFrequency(val displayName: String, val daysInterval: Int) {
    DAILY("Daily", 1),
    WEEKLY("Weekly", 7),
    BIWEEKLY("Bi-weekly", 14),
    MONTHLY("Monthly", 30),
    NEVER("Never", -1)
}

// Data class for subscription usage statistics
data class SubscriptionUsage(
    val subscriptionId: String,
    val lastOpenDate: LocalDateTime?,
    val totalOpenCount: Int = 0,
    val averageSessionDuration: Long = 0, // in minutes
    val daysSinceLastUse: Int = -1,
    val usageFrequency: UsageFrequency = UsageFrequency.UNKNOWN
)

enum class UsageFrequency(val displayName: String) {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    RARELY("Rarely"),
    NEVER("Never"),
    UNKNOWN("Unknown")
}

// Data class for subscription recommendations
data class SubscriptionRecommendation(
    val subscriptionId: String,
    val recommendationType: RecommendationType,
    val reason: String,
    val potentialSavings: BigDecimal,
    val confidence: Float, // 0.0 to 1.0
    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class RecommendationType {
    CANCEL_UNUSED,
    CANCEL_DUPLICATE,
    SWITCH_PLAN,
    PAUSE_TEMPORARILY,
    KEEP_ACTIVE
}

// Extension functions for easy calculations
fun Subscription.getMonthlyEquivalentPrice(): BigDecimal {
    return when (billingCycle) {
        BillingCycle.WEEKLY -> monthlyPrice.multiply(BigDecimal(4.33)) // Average weeks per month
        BillingCycle.MONTHLY -> monthlyPrice
        BillingCycle.QUARTERLY -> monthlyPrice.divide(BigDecimal(3))
        BillingCycle.BIANNUALLY -> monthlyPrice.divide(BigDecimal(6))
        BillingCycle.ANNUALLY -> monthlyPrice.divide(BigDecimal(12))
    }
}

fun Subscription.getDaysSinceLastUsed(): Int {
    return lastUsedDate?.let { lastUsed ->
        java.time.temporal.ChronoUnit.DAYS.between(lastUsed.toLocalDate(), LocalDate.now()).toInt()
    } ?: -1
}

fun Subscription.isUnused(thresholdDays: Int = 30): Boolean {
    val daysSinceLastUsed = getDaysSinceLastUsed()
    return daysSinceLastUsed > thresholdDays || (lastUsedDate == null && createdAt.isBefore(LocalDateTime.now().minusDays(thresholdDays.toLong())))
}
