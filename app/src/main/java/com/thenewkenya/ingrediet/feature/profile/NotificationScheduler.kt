package com.thenewkenya.ingrediet.feature.profile

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

object NotificationScheduler {
    private fun scheduleExact(context: Context, triggerAtMillis: Long, intent: Intent) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = PendingIntent.getBroadcast(
            context,
            (System.currentTimeMillis() % Int.MAX_VALUE).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
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