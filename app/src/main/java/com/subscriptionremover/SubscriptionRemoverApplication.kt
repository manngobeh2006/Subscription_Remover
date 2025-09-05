package com.subscriptionremover

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class SubscriptionRemoverApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    companion object {
        const val UNUSED_SUBSCRIPTION_CHANNEL_ID = "unused_subscription_channel"
        const val SCHEDULED_CANCELLATION_CHANNEL_ID = "scheduled_cancellation_channel"
        const val GENERAL_NOTIFICATIONS_CHANNEL_ID = "general_notifications_channel"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            // Unused Subscription Alerts Channel
            val unusedChannel = NotificationChannel(
                UNUSED_SUBSCRIPTION_CHANNEL_ID,
                "Unused Subscription Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for subscriptions you haven't used recently"
                enableLights(true)
                enableVibration(true)
            }

            // Scheduled Cancellation Channel
            val scheduledChannel = NotificationChannel(
                SCHEDULED_CANCELLATION_CHANNEL_ID,
                "Scheduled Cancellations",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for scheduled subscription cancellations"
                enableLights(true)
                enableVibration(true)
            }

            // General Notifications Channel
            val generalChannel = NotificationChannel(
                GENERAL_NOTIFICATIONS_CHANNEL_ID,
                "General Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General app notifications and updates"
                enableLights(false)
                enableVibration(false)
            }

            notificationManager.createNotificationChannel(unusedChannel)
            notificationManager.createNotificationChannel(scheduledChannel)
            notificationManager.createNotificationChannel(generalChannel)
        }
    }
}
