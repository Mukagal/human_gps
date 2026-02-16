package com.example.pmadvanced.features.auth.repositores

import com.google.firebase.auth.FirebaseAuth

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()

    fun register(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful)
                    onResult(true, null)
                else
                    onResult(false, task.exception?.message)
            }
    }
    fun loginUser(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful){
                    val user = auth.currentUser
                    if (user != null && user.isEmailVerified){
                        onResult(true, null)
                    }
                    else{
                        onResult(false, "Verify email first")
                    }
                }else{
                    onResult(false, task.exception?.message)
                }
            }
    }
}
