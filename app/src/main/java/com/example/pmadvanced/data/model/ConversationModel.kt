package com.example.pmadvanced.data.model

data class ConversationModel(
    val conversationId: Int = 0,
    val otherUser: UserModel? = null,
    val lastMessage: String? = null,
    val lastMessageTime: String? = null

)
