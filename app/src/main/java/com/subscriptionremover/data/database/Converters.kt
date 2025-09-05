package com.subscriptionremover.data.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.subscriptionremover.data.models.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Converters {
    
    private val gson = Gson()
    
    // LocalDateTime converters
    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime?): String? {
        return dateTime?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }
    
    @TypeConverter
    fun toLocalDateTime(dateTimeString: String?): LocalDateTime? {
        return dateTimeString?.let { LocalDateTime.parse(it, DateTimeFormatter.ISO_LOCAL_DATE_TIME) }
    }
    
    // LocalDate converters
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? {
        return date?.format(DateTimeFormatter.ISO_LOCAL_DATE)
    }
    
    @TypeConverter
    fun toLocalDate(dateString: String?): LocalDate? {
        return dateString?.let { LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE) }
    }
    
    // BigDecimal converters
    @TypeConverter
    fun fromBigDecimal(value: BigDecimal?): String? {
        return value?.toString()
    }
    
    @TypeConverter
    fun toBigDecimal(value: String?): BigDecimal? {
        return value?.let { BigDecimal(it) }
    }
    
    // SubscriptionCategory converters
    @TypeConverter
    fun fromSubscriptionCategory(category: SubscriptionCategory?): String? {
        return category?.name
    }
    
    @TypeConverter
    fun toSubscriptionCategory(categoryName: String?): SubscriptionCategory? {
        return categoryName?.let { SubscriptionCategory.valueOf(it) }
    }
    
    // BillingCycle converters
    @TypeConverter
    fun fromBillingCycle(billingCycle: BillingCycle?): String? {
        return billingCycle?.name
    }
    
    @TypeConverter
    fun toBillingCycle(billingCycleName: String?): BillingCycle? {
        return billingCycleName?.let { BillingCycle.valueOf(it) }
    }
    
    // ReminderFrequency converters
    @TypeConverter
    fun fromReminderFrequency(frequency: ReminderFrequency?): String? {
        return frequency?.name
    }
    
    @TypeConverter
    fun toReminderFrequency(frequencyName: String?): ReminderFrequency? {
        return frequencyName?.let { ReminderFrequency.valueOf(it) }
    }
    
    // NotificationSettings converters
    @TypeConverter
    fun fromNotificationSettings(settings: NotificationSettings?): String? {
        return settings?.let { gson.toJson(it) }
    }
    
    @TypeConverter
    fun toNotificationSettings(settingsJson: String?): NotificationSettings? {
        return settingsJson?.let {
            val type = object : TypeToken<NotificationSettings>() {}.type
            gson.fromJson(it, type)
        }
    }
    
    // UserPreferences converters
    @TypeConverter
    fun fromUserPreferences(preferences: UserPreferences?): String? {
        return preferences?.let { gson.toJson(it) }
    }
    
    @TypeConverter
    fun toUserPreferences(preferencesJson: String?): UserPreferences? {
        return preferencesJson?.let {
            val type = object : TypeToken<UserPreferences>() {}.type
            gson.fromJson(it, type)
        }
    }
    
    // UsageFrequency converters
    @TypeConverter
    fun fromUsageFrequency(frequency: UsageFrequency?): String? {
        return frequency?.name
    }
    
    @TypeConverter
    fun toUsageFrequency(frequencyName: String?): UsageFrequency? {
        return frequencyName?.let { UsageFrequency.valueOf(it) }
    }
    
    // RecommendationType converters
    @TypeConverter
    fun fromRecommendationType(type: RecommendationType?): String? {
        return type?.name
    }
    
    @TypeConverter
    fun toRecommendationType(typeName: String?): RecommendationType? {
        return typeName?.let { RecommendationType.valueOf(it) }
    }
    
    // String list converters
    @TypeConverter
    fun fromStringList(list: List<String>?): String? {
        return list?.let { gson.toJson(it) }
    }
    
    @TypeConverter
    fun toStringList(listJson: String?): List<String>? {
        return listJson?.let {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(it, type)
        }
    }
}
