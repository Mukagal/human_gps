package com.example.pmadvanced.data.model

import kotlinx.serialization.Serializable

@Serializable
data class RatingRequest(
    val targetUserId: Int,
    val requestId: Int,
    val rating: Int,
    val comment: String? = null
)

@Serializable
data class UserRating(
    val id: Int,
    val raterId: Int,
    val targetUserId: Int,
    val rating: Int,
    val comment: String?,
    val createdAt: String
)

data class RatingUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)