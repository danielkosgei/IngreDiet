package com.thenewkenya.ingrediet

import android.app.Application
import android.content.Context
import com.thenewkenya.ingrediet.data.network.SessionManager

class SupabaseApplication: Application() {
    /*
    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
    }

    companion object {
        lateinit var appContext: Context
            private set
    } */
    lateinit var  sessionManager: SessionManager
        private set

    override fun onCreate() {
        super.onCreate()
        sessionManager = SessionManager(applicationContext)
    }
}