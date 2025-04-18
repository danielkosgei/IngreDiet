package com.thenewkenya.ingrediet.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserFavoriteDto(
    val id: Int? = null,
    @SerialName("user_id")
    val userId: String,
    @SerialName("recipe_id")
    val recipeId: String,
    @SerialName("created_at")
    val createdAt: String? = null
) 