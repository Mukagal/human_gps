package com.example.pmadvanced.data.model

data class CommentModel(
    val id: Int = 0,
    val postId: Int = 0,
    val authorId: Int = 0,
    val authorName: String? = null,
    val content: String = "",
    val createdAt: String? = null
)