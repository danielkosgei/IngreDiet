package com.thenewkenya.ingrediet.feature.profile

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.thenewkenya.ingrediet.R
import android.app.PendingIntent
import android.content.Intent

object NotificationUtils {
    const val CHANNEL_MEALS = "meals"
    const val CHANNEL_SHOPPING = "shopping"
    const val CHANNEL_RECIPES = "recipes"
    const val CHANNEL_GOALS = "goals"
    const val CHANNEL_HYDRATION = "hydration"

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channels = listOf(
                NotificationChannel(CHANNEL_MEALS, "Meal Reminders", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "Reminders for planned meals"
                },
                NotificationChannel(CHANNEL_SHOPPING, "Shopping Reminders", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "Shopping list reminders"
                },
                NotificationChannel(CHANNEL_RECIPES, "Recipe Suggestions", NotificationManager.IMPORTANCE_LOW).apply {
                    description = "Weekly recipe suggestions"
                },
                NotificationChannel(CHANNEL_GOALS, "Goals & Streaks", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "Reminders to hit calorie/macro goals"
                },
                NotificationChannel(CHANNEL_HYDRATION, "Hydration", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "Hydration reminders"
                }
            )
            nm.createNotificationChannels(channels)
        }
    }

    fun showNotification(context: Context, channelId: String, title: String, text: String, id: Int) {
        val notificationManager = NotificationManagerCompat.from(context)
        if (!notificationManager.areNotificationsEnabled()) {
            return
        }
        // Check permission explicitly for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) 
                != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        notificationManager.notify(id, builder.build())
    }

    fun showMealNotification(context: Context, mealName: String, hour: Int, minute: Int) {
        val id = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
        val snoozeIntent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("type", "meal")
            putExtra("meal", mealName)
            putExtra("action", "snooze")
            putExtra("hour", hour)
            putExtra("minute", minute)
            putExtra("repeat", "daily")
        }
        val skipIntent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("type", "meal")
            putExtra("meal", mealName)
            putExtra("action", "skip")
            putExtra("hour", hour)
            putExtra("minute", minute)
            putExtra("repeat", "daily")
        }
        val snoozePi = PendingIntent.getBroadcast(
            context, id + 1, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val skipPi = PendingIntent.getBroadcast(
            context, id + 2, skipIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notificationManager = NotificationManagerCompat.from(context)
        if (!notificationManager.areNotificationsEnabled()) {
            return
        }
        // Check permission explicitly for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) 
                != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }
        val builder = NotificationCompat.Builder(context, CHANNEL_MEALS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("$mealName reminder")
            .setContentText("It's time for $mealName.")
            .setAutoCancel(true)
            .addAction(0, "Snooze 15m", snoozePi)
            .addAction(0, "Skip today", skipPi)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        notificationManager.notify(id, builder.build())
    }

    fun showHydrationNotification(context: Context) {
        val id = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
        val notificationManager = NotificationManagerCompat.from(context)
        if (!notificationManager.areNotificationsEnabled()) {
            return
        }
        // Check permission explicitly for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) 
                != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }
        val builder = NotificationCompat.Builder(context, CHANNEL_HYDRATION)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Hydration break")
            .setContentText("Time to drink some water.")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        notificationManager.notify(id, builder.build())
    }
} 