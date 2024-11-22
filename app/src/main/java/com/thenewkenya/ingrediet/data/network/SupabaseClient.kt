package com.thenewkenya.ingrediet.data.network

import android.content.Context
import com.thenewkenya.ingrediet.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(@ApplicationContext context: Context): SupabaseClient {
        return try {
            createSupabaseClient(
                supabaseUrl = BuildConfig.SUPABASE_URL,
                supabaseKey = BuildConfig.SUPABASE_ANON_KEY
            ) {
                install(Postgrest)
            }
        } catch (e: Exception) {
            // Handle the exception, e.g., log it or display an error message
            throw e // Re-throw the exception for now
        }
    }

}