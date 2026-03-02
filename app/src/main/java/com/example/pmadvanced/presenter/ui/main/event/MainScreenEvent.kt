package com.example.pmadvanced.presenter.ui.main.event

import com.example.pmadvanced.data.model.MessageModel
import com.example.pmadvanced.data.model.UserModel

data class MainScreenEvent(
    val currentUser: UserModel? = null,
    val selectedUser: UserModel? = null,
    val userList : MutableList<UserModel>? = null,
    val currentChatId : String? = null,

    val messagesList : List<MessageModel>? = null,
)
