package com.thenewkenya.ingrediet.data.network

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

/**
 * Utility class to handle database errors consistently across repositories
 */
object DatabaseErrorHandler {
    
    /**
     * Execute a database operation with standardized error handling
     * @param tag The tag for logging
     * @param operation String description of the operation being performed
     * @param defaultValue Value to return if the operation fails with a non-critical error
     * @param databaseCall The actual database operation to execute
     * @return Flow with Result containing either the operation result or an error
     */
    suspend fun <T> executeDatabaseOperation(
        tag: String,
        operation: String,
        defaultValue: T? = null,
        databaseCall: suspend () -> T
    ): Flow<Result<T>> = flow {
        try {
            val result = databaseCall()
            emit(Result.success(result))
        } catch (e: Exception) {
            handleDatabaseException(tag, operation, e, defaultValue, this)
        }
    }
    
    /**
     * Handle exceptions from database operations
     */
    private suspend fun <T> handleDatabaseException(
        tag: String,
        operation: String,
        e: Exception,
        defaultValue: T?,
        collector: FlowCollector<Result<T>>
    ) {
        when {
            // Handle cancellation exceptions
            e is kotlinx.coroutines.CancellationException -> {
                Log.d(tag, "Operation canceled: $operation")
                throw e // Re-throw cancellation exceptions
            }
            
            // Handle "relation does not exist" errors
            e.message?.contains("relation") == true && e.message?.contains("does not exist") == true -> {
                Log.e(tag, "Database table doesn't exist for operation: $operation", e)
                if (defaultValue != null) {
                    Log.d(tag, "Using default value for operation: $operation")
                    collector.emit(Result.success(defaultValue))
                } else {
                    collector.emit(Result.failure(
                        Exception("Database setup incomplete. Please set up the required tables.")
                    ))
                }
            }
            
            // Handle authentication errors
            e.message?.contains("unauthorized") == true || e.message?.contains("JWT") == true -> {
                Log.e(tag, "Authentication error for operation: $operation", e)
                collector.emit(Result.failure(
                    Exception("Authentication required. Please sign in to continue.")
                ))
            }
            
            // Handle network connectivity errors
            e.message?.contains("connect") == true || e is java.net.UnknownHostException -> {
                Log.e(tag, "Network error for operation: $operation", e)
                collector.emit(Result.failure(
                    Exception("Network error. Please check your internet connection.")
                ))
            }
            
            // Handle all other errors
            else -> {
                Log.e(tag, "Error during database operation: $operation", e)
                collector.emit(Result.failure(e))
            }
        }
    }
    
    /**
     * Extension function to handle database errors in a flow
     * @param tag The tag for logging
     * @param operation String description of the operation being performed
     * @param defaultValue Value to return if the operation fails with a non-critical error
     */
    fun <T> Flow<T>.handleDatabaseErrors(
        tag: String,
        operation: String,
        defaultValue: T? = null
    ): Flow<T> = this.catch { e ->
        if (e is kotlinx.coroutines.CancellationException) {
            Log.d(tag, "Operation canceled: $operation")
            throw e
        }
        
        Log.e(tag, "Error during database operation: $operation", e)
        if (defaultValue != null) {
            emit(defaultValue)
        } else {
            throw e
        }
    }
} 