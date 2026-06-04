package com.example.pmadvanced.presenter.ui.main.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pmadvanced.data.local.AuthTokenStore
import com.example.pmadvanced.data.model.UserModel
import com.example.pmadvanced.data.remote.RetrofitClient
import com.example.pmadvanced.data.repository.ChatRepository
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
import kotlinx.coroutines.withContext

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenStore = AuthTokenStore(application)
    private val repository = ChatRepository(
        api = RetrofitClient.create(application),
        tokenStore = tokenStore
    )
    val currentUserId = repository.currentUserId

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

    companion object {
        private const val CONVERSATION_POLL_INTERVAL_MS = 10_000L
        private const val MESSAGE_POLL_INTERVAL_MS = 3_000L
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
            is MainScreenAction.DeleteMessage -> deleteMessage(event.messageId)
            is MainScreenAction.EditMessage -> editMessage(event.messageId, event.newContent)
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
                val conversations = repository.getConversations()
                if (conversations != null) {
                    _mainScreenEvent.value = _mainScreenEvent.value.copy(
                        conversationList = conversations
                    )
                } else {
                    showSessionExpired()
                }
            } catch (e: Exception) {
                Log.e("MainVM", "loadConversations error", e)
                _snackBarState.value = SnackBarState(
                    show = true,
                    isError = true,
                    message = e.message.orEmpty()
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun loadConversationsSilently() {
        try {
            repository.getConversations()?.let { conversations ->
                _mainScreenEvent.value = _mainScreenEvent.value.copy(
                    conversationList = conversations
                )
            }
        } catch (e: Exception) {
            Log.e("MainVM", "loadConversationsSilently error", e)
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
                val messages = repository.getMessages(conversationId)
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
            val messages = repository.getMessages(conversationId)
            val current = _mainScreenEvent.value.messagesList.orEmpty()
            if (messages.size >= current.size && messages != current) {
                _mainScreenEvent.value = _mainScreenEvent.value.copy(messagesList = messages)
            }
        } catch (e: Exception) {
            Log.e("MainVM", "refreshMessages error", e)
        }
    }

    private fun searchUsers(query: String) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _mainScreenEvent.value = _mainScreenEvent.value.copy(
                    userList = repository.searchUsers(query)
                )
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
                val conversationId = repository.createConversation(userModel)
                if (conversationId != null) {
                    _mainScreenEvent.value = _mainScreenEvent.value.copy(
                        selectedUser = userModel,
                        currentConversationId = conversationId,
                        messagesList = emptyList()
                    )
                    startMessagePolling(conversationId)
                }
            } catch (e: Exception) {
                Log.e("MainVM", "createConversation error", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun sendMessage(message: String, callBack: (Boolean) -> Unit) {
        val conversationId = _mainScreenEvent.value.currentConversationId ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val newMessage = repository.sendMessage(conversationId, message)
                if (newMessage != null) {
                    val updated = _mainScreenEvent.value.messagesList.orEmpty() + newMessage
                    _mainScreenEvent.value = _mainScreenEvent.value.copy(messagesList = updated)
                    withContext(Dispatchers.Main) { callBack(true) }
                } else {
                    withContext(Dispatchers.Main) { callBack(false) }
                }
            } catch (e: Exception) {
                Log.e("MainVM", "sendMessage error", e)
                withContext(Dispatchers.Main) { callBack(false) }
            }
        }
    }

    private fun deleteMessage(messageId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (repository.deleteMessage(messageId)) {
                    val updated = _mainScreenEvent.value.messagesList.orEmpty()
                        .filter { it.messageId != messageId }
                    _mainScreenEvent.value = _mainScreenEvent.value.copy(messagesList = updated)
                }
            } catch (e: Exception) {
                Log.e("MainVM", "deleteMessage error", e)
            }
        }
    }

    private fun editMessage(messageId: Int, newContent: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (repository.editMessage(messageId, newContent)) {
                    val updated = _mainScreenEvent.value.messagesList.orEmpty().map {
                        if (it.messageId == messageId) it.copy(text = newContent) else it
                    }
                    _mainScreenEvent.value = _mainScreenEvent.value.copy(messagesList = updated)
                }
            } catch (e: Exception) {
                Log.e("MainVM", "editMessage error", e)
            }
        }
    }

    private fun showSessionExpired() {
        _snackBarState.value = SnackBarState(
            show = true,
            isError = true,
            message = "Session expired. Please log in again."
        )
    }

    override fun onCleared() {
        super.onCleared()
        conversationPollingJob?.cancel()
        messagePollingJob?.cancel()
    }
}
