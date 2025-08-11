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