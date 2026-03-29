package com.example.pmadvanced.presenter.ui.main.event

import com.example.pmadvanced.data.model.UserModel

sealed interface MainScreenAction {
    data class SelectConversation(val conversationId: Int, val otherUser: UserModel) : MainScreenAction
    data class SelectUser(val userModel: UserModel) : MainScreenAction
    data class SearchUsers(val query: String) : MainScreenAction
    data class SendMessage(val message: String, val callBack: (status: Boolean) -> Unit) : MainScreenAction
    object LoadConversations : MainScreenAction
}