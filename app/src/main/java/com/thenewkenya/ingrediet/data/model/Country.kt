package com.thenewkenya.ingrediet.data.model

import kotlinx.serialization.Serializable


@Serializable
data class Country(
    val id: Int,
    val name: String
)
