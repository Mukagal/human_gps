package com.example.pmadvanced.features.auth.viewModels

import androidx.lifecycle.ViewModel
import com.example.pmadvanced.features.auth.repositores.AuthRepository

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    fun registerUser(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        repository.register(email, password) { success, error ->

            if (success)
                onSuccess()
            else
                onError(error ?: "Unknown error")
        }
    }
    fun loginUser(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ){
        repository.loginUser(email,password){success, error ->
            if (success)
                onSuccess()
            else
                onError(error ?: "Error")
        }
    }
}