package com.thenewkenya.ingrediet.data.network

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.upload
import java.io.InputStream
import java.util.UUID

class ImageUploadManager {

    /**
     * Upload a profile image to Supabase Storage
     * @param context Android context for accessing content resolver
     * @param imageUri Uri of the selected image
     * @param userId User ID for creating unique file names
     * @return Flow with Result containing the public URL of uploaded image
     */
    suspend fun uploadProfileImage(
        context: Context,
        imageUri: Uri,
        userId: String
    ): Flow<Result<String>> = flow {
        try {
            emit(Result.success("Loading...")) // Emit loading state
            
            Log.d("ImageUploadManager", "Starting upload for user: $userId")
            
            // Generate unique filename - simpler structure for RLS compatibility
            val fileExtension = getFileExtension(context, imageUri)
            val fileName = "${userId}/profile.${fileExtension}"
            
            // Get input stream from URI
            val inputStream: InputStream = context.contentResolver.openInputStream(imageUri)
                ?: throw Exception("Unable to open image file")
            
            val imageBytes = inputStream.readBytes()
            inputStream.close()
            
            Log.d("ImageUploadManager", "Image size: ${imageBytes.size} bytes")
            
            // Upload to Supabase Storage (upsert will replace existing file)
            supabase.storage["profile-images"].upload(fileName, imageBytes) {
                upsert = true // Allow overwriting existing profile image
            }
            
            // Get public URL
            val publicUrl = supabase.storage["profile-images"].publicUrl(fileName)
            
            Log.d("ImageUploadManager", "Upload successful: $publicUrl")
            emit(Result.success(publicUrl))
            
        } catch (e: Exception) {
            Log.e("ImageUploadManager", "Upload failed", e)
            emit(Result.failure(e))
        }
    }
    
    /**
     * Delete a profile image from Supabase Storage
     * @param imageUrl The public URL of the image to delete
     * @param userId User ID for constructing the file path
     */
    suspend fun deleteProfileImage(imageUrl: String, userId: String): Flow<Result<Boolean>> = flow {
        try {
            // Use the userId/profile.ext structure for deletion
            val fileExtension = imageUrl.substringAfterLast(".")
            val fileName = "${userId}/profile.${fileExtension}"
            
            supabase.storage["profile-images"].delete(fileName)
            Log.d("ImageUploadManager", "Deleted image: $fileName")
            
            emit(Result.success(true))
        } catch (e: Exception) {
            Log.e("ImageUploadManager", "Delete failed", e)
            emit(Result.failure(e))
        }
    }
    
    /**
     * Get file extension from URI
     */
    private fun getFileExtension(context: Context, uri: Uri): String {
        val contentResolver = context.contentResolver
        val type = contentResolver.getType(uri)
        return when (type) {
            "image/jpeg" -> "jpg"
            "image/png" -> "png"
            "image/webp" -> "webp"
            else -> "jpg" // default
        }
    }
} 