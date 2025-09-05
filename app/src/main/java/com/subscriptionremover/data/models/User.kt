package com.subscriptionremover.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val uid: String,
    val email: String,
    val displayName: String?,
    val photoUrl: String? = null,
    val isPremium: Boolean = false,
    val trialStartDate: LocalDateTime? = null,
    val trialEndDate: LocalDateTime? = null,
    val subscriptionStartDate: LocalDateTime? = null,
    val subscriptionEndDate: LocalDateTime? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val lastLoginDate: LocalDateTime = LocalDateTime.now(),
    val notificationSettings: NotificationSettings = NotificationSettings(),
    val preferences: UserPreferences = UserPreferences()
)

data class NotificationSettings(
    val enableUnusedSubscriptionAlerts: Boolean = true,
    val enableBillingReminders: Boolean = true,
    val enableScheduledCancellationAlerts: Boolean = true,
    val unusedThresholdDays: Int = 30,
    val billingReminderDaysBefore: Int = 3,
    val quietHoursStart: String = "22:00",
    val quietHoursEnd: String = "08:00",
    val enableQuietHours: Boolean = true
)

data class UserPreferences(
    val defaultCurrency: String = "USD",
    val darkMode: Boolean = false,
    val autoBackup: Boolean = true,
    val biometricAuth: Boolean = false,
    val analyticsSharing: Boolean = false,
    val marketingEmails: Boolean = false
)

// Trial and subscription status helpers
fun User.isInFreeTrial(): Boolean {
    val now = LocalDateTime.now()
    return trialStartDate != null && trialEndDate != null && 
           now.isAfter(trialStartDate) && now.isBefore(trialEndDate)
}

fun User.isTrialExpired(): Boolean {
    val now = LocalDateTime.now()
    return trialEndDate != null && now.isAfter(trialEndDate)
}

fun User.getTrialDaysRemaining(): Int {
    return if (isInFreeTrial()) {
        java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), trialEndDate).toInt()
    } else 0
}

fun User.hasActiveSubscription(): Boolean {
    val now = LocalDateTime.now()
    return isPremium && subscriptionEndDate != null && now.isBefore(subscriptionEndDate!!)
}

// Data class for user statistics
data class UserStatistics(
    val totalSubscriptions: Int = 0,
    val activeSubscriptions: Int = 0,
    val monthlySpending: Double = 0.0,
    val yearlySpending: Double = 0.0,
    val potentialSavings: Double = 0.0,
    val unusedSubscriptions: Int = 0,
    val mostExpensiveCategory: String? = null,
    val averageSubscriptionPrice: Double = 0.0
)
