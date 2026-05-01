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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
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
        MainScreenEvent(currentUser = UserModel(userId = currentUserId))
    )
    val mainScreenEvent: StateFlow<MainScreenEvent> = _mainScreenEvent.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var conversationPollingJob: Job? = null
    private var messagePollingJob: Job? = null

    private val userCache = mutableMapOf<Int, UserModel>()


    companion object {
        private const val CONVERSATION_POLL_INTERVAL_MS = 10_000L // 10 seconds
        private const val MESSAGE_POLL_INTERVAL_MS = 3_000L       // 3 seconds
    }

    init {
        loadConversations()
        startConversationPolling()
    }

    fun action(event: MainScreenAction) {
        when (event) {
            is MainScreenAction.SelectConversation -> {
                loadMessages(event.conversationId, event.otherUser)
                startMessagePolling(event.conversationId)
            }
            is MainScreenAction.SelectUser -> createConversation(event.userModel)
            is MainScreenAction.SearchUsers -> searchUsers(event.query)
            is MainScreenAction.SendMessage -> sendMessage(event.message, event.callBack)
            is MainScreenAction.LoadConversations -> loadConversations()
        }
    }


    private fun startConversationPolling() {
        conversationPollingJob?.cancel()
        conversationPollingJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                delay(CONVERSATION_POLL_INTERVAL_MS)
                loadConversationsSilently()
            }
        }
    }

    private fun startMessagePolling(conversationId: Int) {
        messagePollingJob?.cancel()
        messagePollingJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                delay(MESSAGE_POLL_INTERVAL_MS)
                refreshMessages(conversationId)
            }
        }
    }

    fun stopMessagePolling() {
        messagePollingJob?.cancel()
        messagePollingJob = null
    }



    private fun loadConversations() {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val conversations = fetchConversations()
                if (conversations != null) {
                    _mainScreenEvent.value = _mainScreenEvent.value.copy(
                        conversationList = conversations
                    )
                }
            } catch (e: Exception) {
                Log.e("MainVM", "loadConversations error", e)
                _snackBarState.value = _snackBarState.value.copy(
                    show = true, isError = true, message = e.message ?: ""
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun loadConversationsSilently() {
        try {
            val conversations = fetchConversations()
            if (conversations != null) {
                _mainScreenEvent.value = _mainScreenEvent.value.copy(
                    conversationList = conversations
                )
            }
        } catch (e: Exception) {
            Log.e("MainVM", "loadConversationsSilently error", e)
        }
    }

    private fun fetchConversations(): MutableList<ConversationModel>? {
        val url = URL("$BASE_URL/users/$currentUserId/conversations")
        val conn = url.openConnection() as HttpURLConnection
        conn.setRequestProperty("Authorization", "Bearer $token")

        if (conn.responseCode != HttpURLConnection.HTTP_OK) return null

        val response = conn.inputStream.bufferedReader().readText()
        val jsonArray = JSONArray(response)
        val conversations = mutableListOf<ConversationModel>()

        for (i in 0 until jsonArray.length()) {
            val item = jsonArray.getJSONObject(i)
            val convId = item.getInt("id")
            val user1Id = item.getInt("user1_id")
            val user2Id = item.getInt("user2_id")
            val otherUserId = if (user1Id == currentUserId) user2Id else user1Id

            val otherUser = fetchUser(otherUserId)
            // FIX: fetch ALL messages and take the last one, because ?limit=1 returns
            // the oldest message (ascending default order) on most backends.
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

        // FIX: sort by lastMessageTime descending so newest conversation is at the top.
        // Timestamps are ISO-8601 strings so lexicographic sort works correctly.
        conversations.sortByDescending { it.lastMessageTime ?: "" }

        return conversations
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
                val messages = fetchMessages(conversationId)
                _mainScreenEvent.value = _mainScreenEvent.value.copy(messagesList = messages)
            } catch (e: Exception) {
                Log.e("MainVM", "loadMessages error", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun refreshMessages(conversationId: Int) {
        try {
            val messages = fetchMessages(conversationId)
            val current = _mainScreenEvent.value.messagesList ?: emptyList()
            if (messages.size >= current.size && messages != current) {
                _mainScreenEvent.value = _mainScreenEvent.value.copy(messagesList = messages)
            }
        } catch (e: Exception) {
            Log.e("MainVM", "refreshMessages error", e)
        }
    }

    private fun fetchMessages(conversationId: Int): List<MessageModel> {
        val url = URL("$BASE_URL/conversations/$conversationId/messages")
        val conn = url.openConnection() as HttpURLConnection
        conn.setRequestProperty("Authorization", "Bearer $token")

        if (conn.responseCode != HttpURLConnection.HTTP_OK) return emptyList()

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
        // Ensure ascending order by messageId in case the API is inconsistent
        messages.sortBy { it.messageId }
        return messages
    }

    private fun fetchLastMessage(conversationId: Int): Pair<String, String>? {
        return try {
            val url = URL("$BASE_URL/conversations/$conversationId/messages")
            val conn = url.openConnection() as HttpURLConnection
            conn.setRequestProperty("Authorization", "Bearer $token")
            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val arr = JSONArray(conn.inputStream.bufferedReader().readText())
                if (arr.length() > 0) {
                    // Take the LAST element — API returns ascending order
                    val msg = arr.getJSONObject(0)
                    Pair(msg.getString("content"), msg.optString("sent_at", ""))
                } else null
            } else null
        } catch (e: Exception) {
            Log.e("MainVM", "fetchLastMessage error", e)
            null
        }
    }


    private fun fetchUser(userId: Int): UserModel? {
        userCache[userId]?.let { return it }

        return try {
            val url = URL("$BASE_URL/users/$userId/profile")
            val conn = url.openConnection() as HttpURLConnection
            conn.setRequestProperty("Authorization", "Bearer $token")

            val responseCode = conn.responseCode

            if (responseCode == HttpURLConnection.HTTP_OK) {
                val responseText = conn.inputStream.bufferedReader().readText()
                val json = JSONObject(responseText)
                val user = UserModel(
                    userId = json.getInt("id"),
                    userName = json.optString("username", ""),
                    email = json.optString("email", "")
                )
                userCache[userId] = user
                user
            } else {
                conn.errorStream?.close()
                Log.w("MainVM", "fetchUser($userId) returned $responseCode")
                null
            }
        } catch (e: Exception) {
            Log.e("MainVM", "fetchUser($userId) error", e)
            null
        }
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

    // ─── Conversations / send ────────────────────────────────────────────────────

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
                    startMessagePolling(convId)
                }
            } catch (e: Exception) {
                Log.e("MainVM", "createOrOpenConversation error", e)
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
                    // Append at the END (ascending order) — reverseLayout=true shows it at the bottom
                    val updated = (_mainScreenEvent.value.messagesList ?: emptyList()) + listOf(newMessage)
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
        conversationPollingJob?.cancel()
        messagePollingJob?.cancel()
    }
}