package com.example.pmadvanced.data.model

data class PostModel(
    val id: Int = 0,
    val authorId: Int = 0,
    val content: String = "",
    val imagePath: String? = null,
    val createdAt: String? = null,
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val shareCount: Int = 0,
    val likedByMe: Boolean = false
)