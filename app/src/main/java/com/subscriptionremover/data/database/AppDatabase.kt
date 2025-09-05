package com.subscriptionremover.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.subscriptionremover.data.models.Subscription
import com.subscriptionremover.data.models.User

@Database(
    entities = [
        Subscription::class,
        User::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun userDao(): UserDao
    
    companion object {
        private const val DATABASE_NAME = "subscription_remover_db"
        
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration() // For development - remove in production
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
