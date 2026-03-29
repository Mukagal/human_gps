package com.example.pmadvanced.presenter.ui.main.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pmadvanced.BASE_URL
import com.example.pmadvanced.data.model.ConversationModel
import com.example.pmadvanced.data.model.MessageModel
import com.example.pmadvanced.data.model.UserModel
import com.example.pmadvanced.presenter.ui.main.event.MainScreenAction
import com.example.pmadvanced.presenter.ui.main.event.MainScreenEvent
import com.example.pmadvanced.ui.util.SnackBarState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("auth", Context.MODE_PRIVATE)
    private val token = prefs.getString("access_token", "") ?: ""
    private val currentUserId = prefs.getInt("user_id", 0)

    private val _snackBarState = MutableStateFlow(SnackBarState())
    val snackBarState: StateFlow<SnackBarState> = _snackBarState.asStateFlow()

    private val _mainScreenEvent = MutableStateFlow(
        MainScreenEvent(
            currentUser = UserModel(userId = currentUserId)
        )
    )
    val mainScreenEvent: StateFlow<MainScreenEvent> = _mainScreenEvent.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadConversations()
    }

    fun action(event: MainScreenAction) {
        when (event) {
            is MainScreenAction.SelectConversation -> loadMessages(event.conversationId, event.otherUser)
            is MainScreenAction.SelectUser -> createConversation(event.userModel)
            is MainScreenAction.SearchUsers -> searchUsers(event.query)
            is MainScreenAction.SendMessage -> sendMessage(event.message, event.callBack)
            is MainScreenAction.LoadConversations -> loadConversations()
        }
    }


    private fun loadConversations() {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = URL("$BASE_URL/users/$currentUserId/conversations")
                val conn = url.openConnection() as HttpURLConnection
                conn.setRequestProperty("Authorization", "Bearer $token")

                if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                    val response = conn.inputStream.bufferedReader().readText()
                    val jsonArray = JSONArray(response)
                    val conversations = mutableListOf<ConversationModel>()
                    Log.d("MainVM", "currentUserId from prefs = $currentUserId")

                    for (i in 0 until jsonArray.length()) {
                        val item = jsonArray.getJSONObject(i)
                        val convId = item.getInt("id")
                        val user1Id = item.getInt("user1_id")
                        val user2Id = item.getInt("user2_id")
                        val otherUserId = if (user1Id == currentUserId) user2Id else user1Id

                        Log.d("MainVM", "conv=$convId user1=$user1Id user2=$user2Id currentUser=$currentUserId → fetching otherUser=$otherUserId")

                        val otherUser = fetchUser(otherUserId)

                        val lastMsg = fetchLastMessage(convId)

                        conversations.add(
                            ConversationModel(
                                conversationId = convId,
                                otherUser = otherUser,
                                lastMessage = lastMsg?.first,
                                lastMessageTime = lastMsg?.second
                            )
                        )
                    }

                    _mainScreenEvent.value = _mainScreenEvent.value.copy(
                        conversationList = conversations
                    )
                }
            } catch (e: Exception) {
                Log.e("MainVM", "loadConversations error", e)
                _snackBarState.value = _snackBarState.value.copy(show = true, isError = true, message = e.message ?: "")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun fetchUser(userId: Int): UserModel? {
        return try {
            val url = URL("$BASE_URL/users/$userId/profile")
            val conn = url.openConnection() as HttpURLConnection
            conn.setRequestProperty("Authorization", "Bearer $token")
            val responseCode = conn.responseCode
            val responseText = conn.inputStream.bufferedReader().readText()

            Log.d("MainVM", "fetchUser($userId) code=$responseCode body=$responseText")
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val json = JSONObject(responseText)
                UserModel(
                    userId = json.getInt("id"),
                    userName = json.optString("username", ""),
                    email = json.optString("email", "")
                )
            } else null
        } catch (e: Exception) {
            Log.e("MainVM", "fetchUser error", e)
            null
        }
    }

    private fun fetchLastMessage(conversationId: Int): Pair<String, String>? {
        return try {
            val url = URL("$BASE_URL/conversations/$conversationId/messages?limit=1")
            val conn = url.openConnection() as HttpURLConnection
            conn.setRequestProperty("Authorization", "Bearer $token")
            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val arr = JSONArray(conn.inputStream.bufferedReader().readText())
                if (arr.length() > 0) {
                    val msg = arr.getJSONObject(0)
                    Pair(msg.getString("content"), msg.optString("sent_at", ""))
                } else null
            } else null
        } catch (e: Exception) { null }
    }


    private fun searchUsers(query: String) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = URL("$BASE_URL/users")
                val conn = url.openConnection() as HttpURLConnection
                conn.setRequestProperty("Authorization", "Bearer $token")

                if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                    val jsonArray = JSONArray(conn.inputStream.bufferedReader().readText())
                    val list = mutableListOf<UserModel>()
                    for (i in 0 until jsonArray.length()) {
                        val item = jsonArray.getJSONObject(i)
                        val id = item.getInt("id")
                        if (id == currentUserId) continue
                        val username = item.optString("username", "")
                        if (query.isBlank() || username.contains(query, ignoreCase = true)) {
                            list.add(UserModel(userId = id, userName = username, email = item.optString("email")))
                        }
                    }
                    _mainScreenEvent.value = _mainScreenEvent.value.copy(userList = list)
                }
            } catch (e: Exception) {
                Log.e("MainVM", "searchUsers error", e)
            } finally {
                _isLoading.value = false
            }
        }
    }


    private fun createConversation(userModel: UserModel) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = URL("$BASE_URL/conversations?user_b=${userModel.userId}")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Authorization", "Bearer $token")
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true
                OutputStreamWriter(conn.outputStream).use { it.write("") }

                if (conn.responseCode == HttpURLConnection.HTTP_OK || conn.responseCode == HttpURLConnection.HTTP_CREATED) {
                    val json = JSONObject(conn.inputStream.bufferedReader().readText())
                    val convId = json.getInt("id")
                    _mainScreenEvent.value = _mainScreenEvent.value.copy(
                        selectedUser = userModel,
                        currentConversationId = convId,
                        messagesList = emptyList()
                    )
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                Log.e("MainVM", "createOrOpenConversation error", e)
                _isLoading.value = false
            }
        }
    }


    private fun loadMessages(conversationId: Int, otherUser: UserModel) {
        _isLoading.value = true
        _mainScreenEvent.value = _mainScreenEvent.value.copy(
            currentConversationId = conversationId,
            selectedUser = otherUser,
            messagesList = emptyList()
        )
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = URL("$BASE_URL/conversations/$conversationId/messages")
                val conn = url.openConnection() as HttpURLConnection
                conn.setRequestProperty("Authorization", "Bearer $token")

                if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                    val arr = JSONArray(conn.inputStream.bufferedReader().readText())
                    val messages = mutableListOf<MessageModel>()
                    for (i in 0 until arr.length()) {
                        val item = arr.getJSONObject(i)
                        messages.add(
                            MessageModel(
                                messageId = item.getInt("id"),
                                text = item.getString("content"),
                                senderId = item.getInt("sender_id"),
                                timeStamp = item.optString("sent_at", "")
                            )
                        )
                    }
                    _mainScreenEvent.value = _mainScreenEvent.value.copy(messagesList = messages)
                }
            } catch (e: Exception) {
                Log.e("MainVM", "loadMessages error", e)
            } finally {
                _isLoading.value = false
            }
        }
    }


    private fun sendMessage(message: String, callBack: (Boolean) -> Unit) {
        val convId = _mainScreenEvent.value.currentConversationId ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = URL("$BASE_URL/conversations/$convId/messages")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Authorization", "Bearer $token")
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true

                val body = JSONObject().apply { put("content", message) }.toString()
                OutputStreamWriter(conn.outputStream).use { it.write(body) }

                if (conn.responseCode == HttpURLConnection.HTTP_OK || conn.responseCode == HttpURLConnection.HTTP_CREATED) {
                    val json = JSONObject(conn.inputStream.bufferedReader().readText())
                    val newMessage = MessageModel(
                        messageId = json.getInt("id"),
                        text = json.getString("content"),
                        senderId = json.getInt("sender_id"),
                        timeStamp = json.optString("sent_at", "")
                    )
                    val updated = (_mainScreenEvent.value.messagesList ?: emptyList()) + newMessage
                    _mainScreenEvent.value = _mainScreenEvent.value.copy(messagesList = updated)
                    callBack(true)
                } else {
                    callBack(false)
                }
            } catch (e: Exception) {
                Log.e("MainVM", "sendMessage error", e)
                callBack(false)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
    }
}