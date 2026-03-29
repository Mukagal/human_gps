package com.example.pmadvanced.presenter.ui.onboarding

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pmadvanced.data.model.UserModel
import com.example.pmadvanced.ui.util.SnackBarState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import com.example.pmadvanced.BASE_URL

class OnboardingViewModel : ViewModel() {

    private val _snackBarState = MutableStateFlow(SnackBarState())
    val snackBarState: StateFlow<SnackBarState> = _snackBarState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    var accessToken: String = ""
    var currentUserId: Int = 0

    var userModel = UserModel()

    fun action(event: OnboardingEvents) {
        _isLoading.value = true
        when (event) {
            is OnboardingEvents.LoginClick -> loginClick(event.userModel, event.status)
            is OnboardingEvents.SignUpClick -> signUpClick(event.userModel, event.status)
        }
    }

    private fun loginClick(userModel: UserModel, status: (Boolean) -> Unit) {
        this.userModel = userModel
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = URL("$BASE_URL/login")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                val body = JSONObject().apply {
                    put("email", userModel.email ?: "")
                    put("password", userModel.password ?: "")
                }.toString()

                OutputStreamWriter(connection.outputStream).use { it.write(body) }

                val responseCode = connection.responseCode
                val responseText = if (responseCode == HttpURLConnection.HTTP_OK) {
                    connection.inputStream.bufferedReader().readText()
                } else {
                    connection.errorStream?.bufferedReader()?.readText() ?: "Login failed"
                }

                viewModelScope.launch(Dispatchers.Main) {
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        val json = JSONObject(responseText)
                        accessToken = json.getString("access_token")
                        currentUserId = json.getJSONObject("user").getInt("id")
                        _isLoading.value = false
                        status(true)
                    } else {
                        val message = runCatching { JSONObject(responseText).getString("detail") }.getOrDefault("Login failed")
                        _isLoading.value = false
                        _snackBarState.value = _snackBarState.value.copy(show = true, isError = true, message = message)
                        status(false)
                    }
                }
            } catch (e: Exception) {
                Log.e("OnboardingViewModel", "loginClick error", e)
                viewModelScope.launch(Dispatchers.Main) {
                    _isLoading.value = false
                    _snackBarState.value = _snackBarState.value.copy(show = true, isError = true, message = e.message ?: "Network error")
                    status(false)
                }
            }
        }
    }

    private fun signUpClick(userModel: UserModel, status: (Boolean) -> Unit) {
        this.userModel = userModel
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = URL("$BASE_URL/signup")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                val body = JSONObject().apply {
                    put("username", userModel.userName ?: "")
                    put("email", userModel.email ?: "")
                    put("password", userModel.password ?: "")
                }.toString()

                OutputStreamWriter(connection.outputStream).use { it.write(body) }

                val responseCode = connection.responseCode
                val responseText = connection.inputStream.bufferedReader().readText()

                viewModelScope.launch(Dispatchers.Main) {
                    if (responseCode == HttpURLConnection.HTTP_CREATED) {
                        _isLoading.value = false
                        status(true)
                    } else {
                        val message = runCatching { JSONObject(responseText).getString("detail") }.getOrDefault("Sign up failed")
                        _isLoading.value = false
                        _snackBarState.value = _snackBarState.value.copy(show = true, isError = true, message = message)
                        status(false)
                    }
                }
            } catch (e: Exception) {
                viewModelScope.launch(Dispatchers.Main) {
                    _isLoading.value = false
                    _snackBarState.value = _snackBarState.value.copy(show = true, isError = true, message = e.message ?: "Network error")
                    status(false)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
    }
}