package com.example.pmadvanced.presenter.ui.main

fun formatMessageTime(timestamp: String): String {
    return timestamp.substringAfter("T").substring(0, 5)
}

fun formatDayHeader(timestamp: String): String {
    val date = timestamp.substringBefore("T")
    return date
}