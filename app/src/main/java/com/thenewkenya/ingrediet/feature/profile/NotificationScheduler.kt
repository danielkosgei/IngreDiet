package com.thenewkenya.ingrediet.feature.profile

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.util.Calendar

object NotificationScheduler {
    private const val TAG = "NotificationScheduler"
    
    private fun canScheduleExactAlarms(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }
    
    private fun scheduleExact(context: Context, triggerAtMillis: Long, intent: Intent) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = PendingIntent.getBroadcast(
            context,
            (System.currentTimeMillis() % Int.MAX_VALUE).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        try {
            if (canScheduleExactAlarms(context)) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
                Log.d(TAG, "Scheduled exact alarm for ${triggerAtMillis}")
            } else {
                // Fallback to inexact alarm
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
                Log.d(TAG, "Scheduled inexact alarm for ${triggerAtMillis} (exact alarms not available)")
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "Failed to schedule exact alarm, falling back to inexact alarm", e)
            try {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
            } catch (fallbackException: Exception) {
                Log.e(TAG, "Failed to schedule any alarm", fallbackException)
            }
        }
    }
    
    /**
     * Test function to immediately trigger a notification for testing
     */
    fun testNotification(context: Context, type: String = "hydration") {
        Log.d(TAG, "Testing notification: $type")
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("type", type)
            if (type == "meal") {
                putExtra("meal", "Test Meal")
            }
        }
        // Send immediately
        NotificationReceiver().onReceive(context, intent)
    }
    
    /**
     * Schedule all default notifications automatically
     */
    fun scheduleAllDefaults(context: Context) {
        Log.d(TAG, "Scheduling all default notifications")
        
        // Schedule meal reminders
        scheduleMealReminder(context, 8, 0, "Breakfast")
        scheduleMealReminder(context, 13, 0, "Lunch") 
        scheduleMealReminder(context, 19, 0, "Dinner")
        
        // Schedule hydration reminders (every 3 hours during day)
        listOf(9, 12, 15, 18).forEach { hour ->
            scheduleHydrationReminder(context, hour, 0)
        }
        
        // Schedule weekly shopping reminder (Saturday 5 PM)
        scheduleShoppingReminder(context, Calendar.SATURDAY, 17, 0)
        
        // Schedule daily goals reminder (8:30 PM)
        scheduleDailyGoalReminder(context, 20, 30)
        
        // Schedule weekly recipe suggestion (Monday 9 AM)
        scheduleWeeklyRecipeSuggestion(context, Calendar.MONDAY, 9, 0)
    }

    fun scheduleMealReminder(context: Context, hour: Int, minute: Int, mealName: String) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("type", "meal")
            putExtra("meal", mealName)
        }
        scheduleExact(context, cal.timeInMillis, intent)
    }

    fun scheduleShoppingReminder(context: Context, dayOfWeek: Int, hour: Int, minute: Int) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            // Calendar dayOfWeek: 1=Sunday ... 7=Saturday
            while (get(Calendar.DAY_OF_WEEK) != dayOfWeek || timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("type", "shopping")
        }
        scheduleExact(context, cal.timeInMillis, intent)
    }

    fun scheduleWeeklyRecipeSuggestion(context: Context, dayOfWeek: Int, hour: Int, minute: Int) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            while (get(Calendar.DAY_OF_WEEK) != dayOfWeek || timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("type", "recipes")
        }
        scheduleExact(context, cal.timeInMillis, intent)
    }

    fun scheduleDailyGoalReminder(context: Context, hour: Int, minute: Int) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("type", "goals")
        }
        scheduleExact(context, cal.timeInMillis, intent)
    }

    fun scheduleHydrationReminder(context: Context, hour: Int, minute: Int) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("type", "hydration")
        }
        scheduleExact(context, cal.timeInMillis, intent)
    }
} 