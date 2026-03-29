package com.example.pmadvanced.presenter.ui.main.event

import com.example.pmadvanced.data.model.MessageModel
import com.example.pmadvanced.data.model.UserModel
import com.example.pmadvanced.data.model.ConversationModel

data class MainScreenEvent(

    val currentUser: UserModel? = null,
    val selectedUser: UserModel? = null,

    val conversationList: MutableList<ConversationModel>? = null,

    val userList: MutableList<UserModel>? = null,

    val currentConversationId: Int? = null,
    val messagesList: List<MessageModel>? = null,
)
