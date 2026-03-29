package com.example.pmadvanced.data.model


data class UserModel(
    val userId : Int? = 0,
    val userName: String? = null,
    val profileImage : String? = null,
    val email: String? = null,
    val password: String? = null
)