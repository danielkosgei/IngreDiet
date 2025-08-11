package com.thenewkenya.ingrediet.feature.profile

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        NotificationUtils.ensureChannels(context)
        val type = intent.getStringExtra("type") ?: return
        val action = intent.getStringExtra("action")
        when (type) {
            "meal" -> {
                val mealName = intent.getStringExtra("meal") ?: "Meal"
                val hour = intent.getIntExtra("hour", -1)
                val minute = intent.getIntExtra("minute", -1)
                if (action == "snooze") {
                    val now = System.currentTimeMillis() + 15 * 60 * 1000L
                    val i = Intent(context, NotificationReceiver::class.java).apply {
                        putExtra("type", "meal")
                        putExtra("meal", mealName)
                        putExtra("hour", hour)
                        putExtra("minute", minute)
                    }
                    NotificationScheduler.run {
                        val am = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
                        val pi = android.app.PendingIntent.getBroadcast(
                            context,
                            (System.currentTimeMillis() % Int.MAX_VALUE).toInt(),
                            i,
                            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                        )
                        am.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, now, pi)
                    }
                } else if (action == "skip") {
                    // Do nothing today; allow regular daily schedule to handle tomorrow
                } else {
                    if (hour >= 0 && minute >= 0) {
                        NotificationUtils.showMealNotification(context, mealName, hour, minute)
                        com.thenewkenya.ingrediet.feature.notifications.NotificationStore.append(
                            context,
                            com.thenewkenya.ingrediet.feature.notifications.NotificationEntry(
                                id = System.currentTimeMillis(),
                                type = "meal",
                                title = "$mealName reminder",
                                message = "It's time for $mealName.",
                                timestamp = System.currentTimeMillis()
                            )
                        )
                    } else {
                        NotificationUtils.showNotification(
                            context,
                            NotificationUtils.CHANNEL_MEALS,
                            "${mealName} reminder",
                            "It's time for $mealName.",
                            (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
                        )
                        com.thenewkenya.ingrediet.feature.notifications.NotificationStore.append(
                            context,
                            com.thenewkenya.ingrediet.feature.notifications.NotificationEntry(
                                id = System.currentTimeMillis(),
                                type = "meal",
                                title = "$mealName reminder",
                                message = "It's time for $mealName.",
                                timestamp = System.currentTimeMillis()
                            )
                        )
                    }
                }
            }
            "shopping" -> {
                NotificationUtils.showNotification(
                    context,
                    NotificationUtils.CHANNEL_SHOPPING,
                    "Shopping reminder",
                    "Don't forget your shopping list.",
                    (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
                )
                com.thenewkenya.ingrediet.feature.notifications.NotificationStore.append(
                    context,
                    com.thenewkenya.ingrediet.feature.notifications.NotificationEntry(
                        id = System.currentTimeMillis(),
                        type = "shopping",
                        title = "Shopping reminder",
                        message = "Don't forget your shopping list.",
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
            "recipes" -> {
                NotificationUtils.showNotification(
                    context,
                    NotificationUtils.CHANNEL_RECIPES,
                    "Weekly recipes",
                    "New recipe ideas tailored to your preferences.",
                    (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
                )
                com.thenewkenya.ingrediet.feature.notifications.NotificationStore.append(
                    context,
                    com.thenewkenya.ingrediet.feature.notifications.NotificationEntry(
                        id = System.currentTimeMillis(),
                        type = "recipes",
                        title = "Weekly recipes",
                        message = "New recipe ideas tailored to your preferences.",
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
            "goals" -> {
                NotificationUtils.showNotification(
                    context,
                    NotificationUtils.CHANNEL_GOALS,
                    "Nutrition goal",
                    "You're close to your calorie/macro target today.",
                    (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
                )
                com.thenewkenya.ingrediet.feature.notifications.NotificationStore.append(
                    context,
                    com.thenewkenya.ingrediet.feature.notifications.NotificationEntry(
                        id = System.currentTimeMillis(),
                        type = "goals",
                        title = "Nutrition goal",
                        message = "You're close to your calorie/macro target today.",
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
            "hydration" -> {
                NotificationUtils.showHydrationNotification(context)
                com.thenewkenya.ingrediet.feature.notifications.NotificationStore.append(
                    context,
                    com.thenewkenya.ingrediet.feature.notifications.NotificationEntry(
                        id = System.currentTimeMillis(),
                        type = "hydration",
                        title = "Hydration break",
                        message = "Time to drink some water.",
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        }
    }
} 