package com.thenewkenya.ingrediet.data.network

import androidx.compose.runtime.staticCompositionLocalOf
import com.thenewkenya.ingrediet.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient

val LocalSupabase = staticCompositionLocalOf<SupabaseClient> {
    error("No SupabaseClient provided")
}

val supabase: SupabaseClient = createSupabaseClient(
    supabaseUrl = BuildConfig.SUPABASE_URL,
    supabaseKey = BuildConfig.SUPABASE_ANON_KEY
) {
    install(Auth)
}