package com.example.pmadvanced.data.repository

import com.example.pmadvanced.data.local.AuthTokenStore
import com.example.pmadvanced.data.model.ConversationModel
import com.example.pmadvanced.data.model.MessageModel
import com.example.pmadvanced.data.model.MessageRequest
import com.example.pmadvanced.data.model.UserModel
import com.example.pmadvanced.data.model.toMessageModel
import com.example.pmadvanced.data.model.toUserModel
import com.example.pmadvanced.data.remote.MamanTapApi

class ChatRepository(
    api: MamanTapApi,
    private val tokenStore: AuthTokenStore
) : BaseRepository(api, tokenStore) {
    private val api = api
    private val userCache = mutableMapOf<Int, UserModel>()

    val currentUserId: Int
        get() = tokenStore.currentUserId

    suspend fun getConversations(): MutableList<ConversationModel>? {
        val conversations = authenticatedCall { api.getConversations(currentUserId) } ?: return null

        return conversations.map { item ->
            val otherUserId = if (item.user1Id == currentUserId) item.user2Id else item.user1Id
            val messages = getMessages(item.id)
            val lastMessage = messages.lastOrNull()

            ConversationModel(
                conversationId = item.id,
                otherUser = getUserProfile(otherUserId),
                lastMessage = lastMessage?.text,
                lastMessageTime = lastMessage?.timeStamp
            )
        }
            .sortedByDescending { it.lastMessageTime.orEmpty() }
            .toMutableList()
    }

    suspend fun getMessages(conversationId: Int): List<MessageModel> {
        return authenticatedCall { api.getMessages(conversationId) }
            ?.map { it.toMessageModel() }
            ?.sortedBy { it.messageId }
            .orEmpty()
    }

    suspend fun searchUsers(query: String): MutableList<UserModel> {
        return authenticatedCall { api.getUsers() }
            ?.filter { it.id != currentUserId }
            ?.filter { query.isBlank() || it.username.orEmpty().contains(query, ignoreCase = true) }
            ?.map { it.toUserModel() }
            ?.toMutableList()
            ?: mutableListOf()
    }

    suspend fun createConversation(user: UserModel): Int? {
        val userId = user.userId ?: return null
        return authenticatedCall { api.createConversation(userId) }?.id
    }

    suspend fun sendMessage(conversationId: Int, message: String): MessageModel? {
        return authenticatedCall {
            api.sendMessage(conversationId, MessageRequest(message))
        }?.toMessageModel()
    }

    suspend fun deleteMessage(messageId: Int): Boolean {
        return isSuccessfulCall { api.deleteMessage(messageId) }
    }

    suspend fun editMessage(messageId: Int, newContent: String): Boolean {
        return authenticatedCall {
            api.editMessage(messageId, MessageRequest(newContent))
        } != null
    }

    private suspend fun getUserProfile(userId: Int): UserModel? {
        userCache[userId]?.let { return it }
        return authenticatedCall { api.getUserProfile(userId) }
            ?.toUserModel()
            ?.also { userCache[userId] = it }
    }
}
