package com.example.pmadvanced.presenter.ui.main.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pmadvanced.data.local.AuthTokenStore
import com.example.pmadvanced.data.model.CommentModel
import com.example.pmadvanced.data.model.PostModel
import com.example.pmadvanced.data.model.UserModel
import com.example.pmadvanced.data.remote.RetrofitClient
import com.example.pmadvanced.data.repository.ProfileRepository
import com.example.pmadvanced.ui.util.SnackBarState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenStore = AuthTokenStore(application)
    private val repository = ProfileRepository(
        api = RetrofitClient.create(application),
        tokenStore = tokenStore
    )

    val currentUserId = repository.currentUserId

    private val _snackBarState = MutableStateFlow(SnackBarState())
    val snackBarState: StateFlow<SnackBarState> = _snackBarState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _profile = MutableStateFlow<UserModel?>(null)
    val profile: StateFlow<UserModel?> = _profile.asStateFlow()

    private val _ownProfile = MutableStateFlow<UserModel?>(null)
    val ownProfile: StateFlow<UserModel?> = _ownProfile.asStateFlow()

    private val _posts = MutableStateFlow<List<PostModel>>(emptyList())
    val posts: StateFlow<List<PostModel>> = _posts.asStateFlow()

    private val _comments = MutableStateFlow<List<CommentModel>>(emptyList())
    val comments: StateFlow<List<CommentModel>> = _comments.asStateFlow()

    private val _averageRating = MutableStateFlow<Double?>(null)
    val averageRating: StateFlow<Double?> = _averageRating.asStateFlow()

    private val _totalRatings = MutableStateFlow(0)
    val totalRatings: StateFlow<Int> = _totalRatings.asStateFlow()

    init {
        loadOwnProfile()
    }

    fun loadOwnProfile() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _ownProfile.value = repository.getProfile(currentUserId)
            } catch (e: Exception) {
                Log.e("ProfileVM", "loadOwnProfile error", e)
            }
        }
    }

    fun loadProfile(userId: Int = currentUserId) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val profile = repository.getProfile(userId)
                if (profile != null) {
                    _profile.value = profile
                    if (userId == currentUserId) {
                        _ownProfile.value = profile
                    }
                    loadUserRating(userId)
                    loadPosts(userId)
                } else {
                    showSessionExpired()
                }
            } catch (e: Exception) {
                Log.e("ProfileVM", "loadProfile error", e)
            }
        }
    }

    fun logout() {
        repository.logout()
    }

    private fun loadUserRating(userId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val (average, total) = repository.getUserRating(userId)
                _averageRating.value = average
                _totalRatings.value = total
            } catch (e: Exception) {
                Log.e("ProfileVM", "loadUserRating error", e)
                _averageRating.value = null
                _totalRatings.value = 0
            }
        }
    }

    fun updateUsername(newUsername: String) {
        runProfileMutation(
            successMessage = "Username updated!",
            failureMessage = "Update failed",
            mutation = { repository.updateUsername(newUsername) }
        )
    }

    fun updateProfessions(professions: List<String>) {
        runProfileMutation(
            successMessage = "Professions updated!",
            failureMessage = "Update failed",
            mutation = { repository.updateProfessions(professions) }
        )
    }

    fun uploadProfileImage(context: Context, imageUri: Uri) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val bytes = context.contentResolver.openInputStream(imageUri)?.use { it.readBytes() }
                val success = bytes != null && repository.uploadProfileImage(bytes)
                _isLoading.value = false
                if (success) {
                    loadOwnProfile()
                    loadProfile(currentUserId)
                    showSuccess("Photo updated!")
                } else {
                    showError("Upload failed")
                }
            } catch (e: Exception) {
                Log.e("ProfileVM", "uploadProfileImage error", e)
                _isLoading.value = false
                showError(e.message ?: "Error")
            }
        }
    }

    fun loadPosts(userId: Int = currentUserId) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _posts.value = repository.getPosts(userId)
            } catch (e: Exception) {
                Log.e("ProfileVM", "loadPosts error", e)
            }
        }
    }

    fun createPostWithImage(content: String, imageUri: Uri?, context: Context) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val imageBytes = imageUri?.let {
                    context.contentResolver.openInputStream(it)?.use { stream -> stream.readBytes() }
                }
                val success = repository.createPost(content, imageBytes)
                _isLoading.value = false
                if (success) {
                    loadPosts(currentUserId)
                    showSuccess("Post created!")
                } else {
                    showError("Failed to create post")
                }
            } catch (e: Exception) {
                Log.e("ProfileVM", "createPostWithImage error", e)
                _isLoading.value = false
                showError(e.message ?: "Error")
            }
        }
    }

    fun toggleLike(post: PostModel) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (repository.toggleLike(post)) {
                    _posts.value = _posts.value.map {
                        if (it.id == post.id) {
                            it.copy(
                                likedByMe = !post.likedByMe,
                                likeCount = if (post.likedByMe) it.likeCount - 1 else it.likeCount + 1
                            )
                        } else {
                            it
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("ProfileVM", "toggleLike error", e)
            }
        }
    }

    fun loadComments(postId: Int) {
        _comments.value = emptyList()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _comments.value = repository.getComments(postId)
            } catch (e: Exception) {
                Log.e("ProfileVM", "loadComments error", e)
            }
        }
    }

    fun addComment(postId: Int, content: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (repository.addComment(postId, content)) {
                    loadComments(postId)
                    _posts.value = _posts.value.map {
                        if (it.id == postId) it.copy(commentCount = it.commentCount + 1) else it
                    }
                }
            } catch (e: Exception) {
                Log.e("ProfileVM", "addComment error", e)
            }
        }
    }

    fun sharePost(postId: Int, conversationId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (repository.sharePost(postId, conversationId)) {
                    showSuccess("Post shared!")
                    _posts.value = _posts.value.map {
                        if (it.id == postId) it.copy(shareCount = it.shareCount + 1) else it
                    }
                }
            } catch (e: Exception) {
                Log.e("ProfileVM", "sharePost error", e)
            }
        }
    }

    fun fetchPostById(postId: Int, onResult: (PostModel?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val post = repository.getPost(postId)
                withContext(Dispatchers.Main) { onResult(post) }
            } catch (e: Exception) {
                Log.e("ProfileVM", "fetchPostById error", e)
                withContext(Dispatchers.Main) { onResult(null) }
            }
        }
    }

    private fun runProfileMutation(
        successMessage: String,
        failureMessage: String,
        mutation: suspend () -> Boolean
    ) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val success = mutation()
                _isLoading.value = false
                if (success) {
                    loadOwnProfile()
                    loadProfile(currentUserId)
                    showSuccess(successMessage)
                } else {
                    showError(failureMessage)
                }
            } catch (e: Exception) {
                _isLoading.value = false
                showError(e.message ?: "Error")
            }
        }
    }

    private fun showSessionExpired() {
        showError("Session expired. Please log in again.")
    }

    private fun showSuccess(message: String) {
        _snackBarState.value = SnackBarState(show = true, isError = false, message = message)
    }

    private fun showError(message: String) {
        _snackBarState.value = SnackBarState(show = true, isError = true, message = message)
    }
}
