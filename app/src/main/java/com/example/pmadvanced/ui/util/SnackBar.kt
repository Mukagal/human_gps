package com.example.pmadvanced.ui.util

data class SnackBarState(
    val show : Boolean = false,
    val message : String = "",
    val isError : Boolean = false
)