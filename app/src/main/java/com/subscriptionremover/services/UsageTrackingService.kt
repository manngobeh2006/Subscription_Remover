package com.subscriptionremover.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.subscriptionremover.R
import com.subscriptionremover.domain.repository.SubscriptionRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class UsageTrackingService : Service() {

    @Inject
    lateinit var subscriptionRepository: SubscriptionRepository

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var trackingJob: Job? = null

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "usage_tracking_channel"
        private const val TRACKING_INTERVAL = 1000 * 60 * 15 // 15 minutes
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        startUsageTracking()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        trackingJob?.cancel()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startUsageTracking() {
        trackingJob = serviceScope.launch {
            while (isActive) {
                try {
                    trackAppUsage()
                    delay(TRACKING_INTERVAL.toLong())
                } catch (e: Exception) {
                    // Log error but continue tracking
                    delay(TRACKING_INTERVAL.toLong())
                }
            }
        }
    }

    private suspend fun trackAppUsage() {
        val usageStatsManager = getSystemService(USAGE_STATS_SERVICE) as? UsageStatsManager ?: return
        
        // Get usage stats for the last 24 hours
        val now = System.currentTimeMillis()
        val yesterday = now - (1000 * 60 * 60 * 24)
        
        val usageStatsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            yesterday,
            now
        )
        
        if (usageStatsList.isNullOrEmpty()) return
        
        // Get trackable subscriptions
        val trackableSubscriptions = subscriptionRepository.getTrackableSubscriptions()
        
        // Map package names to subscription IDs
        val packageToSubscriptionMap = trackableSubscriptions
            .filter { !it.packageName.isNullOrBlank() }
            .associateBy { it.packageName!! }
        
        // Update last used dates for matching apps
        usageStatsList.forEach { usageStats ->
            packageToSubscriptionMap[usageStats.packageName]?.let { subscription ->
                val lastUsedTime = LocalDateTime.ofEpochSecond(
                    usageStats.lastTimeUsed / 1000, 0, 
                    java.time.ZoneOffset.systemDefault().rules.getOffset(java.time.Instant.now())
                )
                
                // Only update if the app was used recently
                if (usageStats.lastTimeUsed > yesterday) {
                    subscriptionRepository.updateLastUsedDate(subscription.id, lastUsedTime)
                }
            }
        }
        
        // Check for unused subscriptions and send notifications
        checkForUnusedSubscriptions()
    }

    private suspend fun checkForUnusedSubscriptions() {
        val unusedSubscriptions = subscriptionRepository.getUnusedSubscriptions(30)
        
        unusedSubscriptions.forEach { subscription ->
            val daysSinceLastUsed = subscription.getDaysSinceLastUsed()
            
            // Send notification for significantly unused subscriptions
            if (daysSinceLastUsed > 30 && shouldSendNotification(subscription.id)) {
                sendUnusedSubscriptionNotification(subscription)
            }
        }
    }

    private fun shouldSendNotification(subscriptionId: String): Boolean {
        // Implement logic to prevent spam notifications
        // For example, only send once per week for each subscription
        val lastNotificationTime = getLastNotificationTime(subscriptionId)
        val weekAgo = System.currentTimeMillis() - (1000 * 60 * 60 * 24 * 7)
        
        return lastNotificationTime < weekAgo
    }

    private fun getLastNotificationTime(subscriptionId: String): Long {
        // Get from SharedPreferences or database
        val prefs = getSharedPreferences("usage_notifications", MODE_PRIVATE)
        return prefs.getLong("last_notification_$subscriptionId", 0)
    }

    private fun updateLastNotificationTime(subscriptionId: String) {
        val prefs = getSharedPreferences("usage_notifications", MODE_PRIVATE)
        prefs.edit().putLong("last_notification_$subscriptionId", System.currentTimeMillis()).apply()
    }

    private fun sendUnusedSubscriptionNotification(subscription: com.subscriptionremover.data.models.Subscription) {
        val daysSinceLastUsed = subscription.getDaysSinceLastUsed()
        val potentialSavings = subscription.getMonthlyEquivalentPrice()
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Unused Subscription Alert")
            .setContentText("You haven't used ${subscription.name} for $daysSinceLastUsed days. Consider canceling to save $${potentialSavings}/month.")
            .setSmallIcon(R.drawable.ic_notification_warning)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(subscription.id.hashCode(), notification)
        
        updateLastNotificationTime(subscription.id)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Usage Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Tracks app usage for subscription monitoring"
                enableLights(false)
                enableVibration(false)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification() = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("Subscription Remover")
        .setContentText("Monitoring app usage to help you save money")
        .setSmallIcon(R.drawable.ic_notification_tracking)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setOngoing(true)
        .build()
}
