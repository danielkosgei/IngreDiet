package com.thenewkenya.ingrediet.data.network

import androidx.compose.runtime.staticCompositionLocalOf
import com.thenewkenya.ingrediet.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.functions.Functions

val LocalSupabase = staticCompositionLocalOf<SupabaseClient> {
    error("No SupabaseClient provided")
}

val supabase: SupabaseClient = createSupabaseClient(
    supabaseUrl = BuildConfig.SUPABASE_URL,
    supabaseKey = BuildConfig.SUPABASE_ANON_KEY
) {
    install(Auth) {
        autoSaveToStorage = true
        autoLoadFromStorage = true
    }
    install(Storage)
    install(Postgrest)
    install(Functions) {
        // Add any custom configuration if needed
    }
}