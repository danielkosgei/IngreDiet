package com.thenewkenya.ingrediet.data.network

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import com.thenewkenya.ingrediet.BuildConfig
import com.thenewkenya.ingrediet.SupabaseApplication
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.FlowType
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.auth.SessionManager
import io.github.jan.supabase.createSupabaseClient

val LocalSupabase = staticCompositionLocalOf<SupabaseClient> {
    error("No SupabaseClient provided")
}

@OptIn(SupabaseExperimental::class)
val supabase: SupabaseClient = createSupabaseClient(
    supabaseUrl = BuildConfig.SUPABASE_URL,
    supabaseKey = BuildConfig.SUPABASE_ANON_KEY
) {
    install(Auth) {
        autoSaveToStorage = true
        autoLoadFromStorage = true
    }
    install(Storage)
}