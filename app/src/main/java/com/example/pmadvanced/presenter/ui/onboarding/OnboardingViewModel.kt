package com.example.pmadvanced.presenter.ui.onboarding

import android.app.Activity
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pmadvanced.data.model.UserModel
import com.example.pmadvanced.ui.util.SnackBarState
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class OnboardingViewModel : ViewModel(){

    val auth = FirebaseAuth.getInstance()
    val databaseRef = FirebaseDatabase.getInstance("https://pmadvanced-default-rtdb.asia-southeast1.firebasedatabase.app/")

    lateinit var activity: Activity

    var storageVerificationId: String = ""
    private var isFromLogin = false

    private val _snackBarState = MutableStateFlow(SnackBarState())
    val snackBarState: StateFlow<SnackBarState> = _snackBarState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun provideActivity(activity: Activity) {
        this.activity = activity

    }


    var userModel = UserModel()

    fun action(even : OnboardingEvents){
        _isLoading.value = true
        when (even) {
            is OnboardingEvents.LoginClick -> loginClick(even.userModel, even.status)
            is OnboardingEvents.SignUpClick -> signUpClick(even.userModel, even.status)
        }
    }

    private fun loginClick(userModel: UserModel, status: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.Main) {
            auth.signInWithEmailAndPassword(
                userModel.email ?: "",
                userModel.password ?: ""
            ).addOnCompleteListener { task ->
                _isLoading.value = false
                if (task.isSuccessful) {
                    status(true)
                } else {
                    _snackBarState.value = _snackBarState.value.copy(
                        show = true, isError = true,
                        message = task.exception?.message ?: "Login failed"
                    )
                    status(false)
                }
            }
        }
    }
    private fun signUpClick(userModel: UserModel, status: (Boolean) -> Unit) {
        this.userModel = userModel
        viewModelScope.launch(Dispatchers.Main) {
            auth.createUserWithEmailAndPassword(
                userModel.email ?: "",
                userModel.password ?: ""
            ).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    uid?.let {
                        val userToSave = userModel.copy(userId = it, password = null) // never save password
                        databaseRef.getReference("users").child(it).setValue(userToSave)
                            .addOnSuccessListener {
                                _isLoading.value = false
                                status(true)
                            }
                            .addOnFailureListener { e ->
                                _isLoading.value = false
                                _snackBarState.value = _snackBarState.value.copy(
                                    show = true, isError = true, message = e.message ?: ""
                                )
                                status(false)
                            }
                    }
                } else {
                    _isLoading.value = false
                    _snackBarState.value = _snackBarState.value.copy(
                        show = true, isError = true,
                        message = task.exception?.message ?: "Sign up failed"
                    )
                    status(false)
                }
            }
        }
    }



    override fun onCleared() {
        super.onCleared()
    }

}