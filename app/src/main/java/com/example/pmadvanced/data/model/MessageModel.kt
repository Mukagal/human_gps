package com.example.pmadvanced.data.model

import android.widget.EditText

data class MessageModel(
    val messageId : Int? = null,
    val senderId : Int? = null,
    val text: String? = null,
    val timeStamp: String? = null
)
