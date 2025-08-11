package com.thenewkenya.ingrediet.feature.notifications

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.builtins.ListSerializer

@Serializable
data class NotificationEntry(
    val id: Long,
    val type: String, // meal, shopping, recipes, goals
    val title: String,
    val message: String,
    val timestamp: Long,
    val isRead: Boolean = false
)

object NotificationStore {
    private const val PREFS = "ingrediet_notifications"
    private const val KEY = "entries"
    private val json = Json { ignoreUnknownKeys = true }

    fun getAll(context: Context): List<NotificationEntry> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val str = prefs.getString(KEY, null) ?: return emptyList()
        return try {
            json.decodeFromString(ListSerializer(NotificationEntry.serializer()), str)
        } catch (_: Exception) { emptyList() }
    }

    private fun saveAll(context: Context, list: List<NotificationEntry>) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val str = json.encodeToString(ListSerializer(NotificationEntry.serializer()), list)
        prefs.edit().putString(KEY, str).apply()
    }

    fun append(context: Context, entry: NotificationEntry) {
        val list = getAll(context).toMutableList()
        list.add(0, entry)
        saveAll(context, list)
    }

    fun markRead(context: Context, id: Long, read: Boolean = true) {
        val list = getAll(context).map { if (it.id == id) it.copy(isRead = read) else it }
        saveAll(context, list)
    }

    fun markAllRead(context: Context) {
        val list = getAll(context).map { it.copy(isRead = true) }
        saveAll(context, list)
    }

    fun clear(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY).apply()
    }
} 