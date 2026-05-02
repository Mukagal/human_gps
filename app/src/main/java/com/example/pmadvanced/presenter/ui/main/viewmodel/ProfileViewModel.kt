package com.example.pmadvanced.presenter.ui.main.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pmadvanced.BASE_URL
import com.example.pmadvanced.data.model.CommentModel
import com.example.pmadvanced.data.model.PostModel
import com.example.pmadvanced.data.model.UserModel
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
import kotlinx.serialization.json.Json

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("auth", Context.MODE_PRIVATE)
    var token = prefs.getString("access_token", "") ?: ""
    private val refreshToken = prefs.getString("refresh_token", "") ?: ""
    val currentUserId = prefs.getInt("user_id", 0)

    private val _snackBarState = MutableStateFlow(SnackBarState())
    val snackBarState: StateFlow<SnackBarState> = _snackBarState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _profile = MutableStateFlow<UserModel?>(null)
    val profile: StateFlow<UserModel?> = _profile.asStateFlow()

    private val _posts = MutableStateFlow<List<PostModel>>(emptyList())
    val posts: StateFlow<List<PostModel>> = _posts.asStateFlow()

    private val _comments = MutableStateFlow<List<CommentModel>>(emptyList())
    val comments: StateFlow<List<CommentModel>> = _comments.asStateFlow()

    private val _averageRating = MutableStateFlow<Double?>(null)
    val averageRating: StateFlow<Double?> = _averageRating.asStateFlow()

    private val _totalRatings = MutableStateFlow(0)
    val totalRatings: StateFlow<Int> = _totalRatings.asStateFlow()

    private suspend fun refreshAndRetry(): Boolean {
        if (refreshToken.isEmpty()) return false
        return try {
            val url = URL("$BASE_URL/refresh")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Authorization", "Bearer $refreshToken")

            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val json = JSONObject(conn.inputStream.bufferedReader().readText())
                token = json.getString("access_token")
                prefs.edit().putString("access_token", token).apply()
                true
            } else false
        } catch (e: Exception) { false }
    }

    fun loadProfile(userId: Int = currentUserId) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = URL("$BASE_URL/users/$userId")
                val conn = url.openConnection() as HttpURLConnection
                conn.setRequestProperty("Authorization", "Bearer $token")
                val responseCode = conn.responseCode
                if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    if (!refreshAndRetry()) {
                        _snackBarState.value = SnackBarState(show = true, isError = true, message = "Session expired. Please log in again.")
                        return@launch
                    }
                    loadProfile(userId)  // retry
                    return@launch
                }
                val responseText = if (responseCode == HttpURLConnection.HTTP_OK)
                    conn.inputStream.bufferedReader().readText()
                else conn.errorStream?.bufferedReader()?.readText() ?: ""

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val json = JSONObject(responseText)
                    _profile.value = UserModel(
                        userId = json.getInt("id"),
                        userName = json.optString("username"),
                        email = json.optString("email"),
                        profileImage = json.optString("profile_image_path")
                            .takeIf { it.isNotBlank() && it != "null" },
                        professions = Json.decodeFromString(json.optString("professions"))
                    )
                    loadUserRating(userId)
                    loadPosts(userId)
                }
            } catch (e: Exception) {
                Log.e("ProfileVM", "loadProfile error", e)
            }
        }
    }

    fun logout() {
        prefs.edit().clear().apply()
    }

    private fun loadUserRating(userId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = URL("$BASE_URL/users/$userId/ratings")
                val conn = url.openConnection() as HttpURLConnection
                conn.setRequestProperty("Authorization", "Bearer $token")
                val responseCode = conn.responseCode
                if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    if (!refreshAndRetry()) return@launch
                    loadUserRating(userId)
                    return@launch
                }
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val responseText = conn.inputStream.bufferedReader().readText()
                    val json = JSONObject(responseText)
                    _averageRating.value = json.optDouble("average_rating", 0.0)
                    _totalRatings.value = json.optInt("total_ratings", 0)
                } else {
                    _averageRating.value = null
                    _totalRatings.value = 0
                }
            } catch (e: Exception) {
                Log.e("ProfileVM", "loadUserRating error", e)
                _averageRating.value = null
                _totalRatings.value = 0
            }
        }
    }


    fun updateUsername(newUsername: String) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = URL("$BASE_URL/users/me")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "PATCH"
                conn.setRequestProperty("Authorization", "Bearer $token")
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true
                OutputStreamWriter(conn.outputStream).use {
                    it.write(JSONObject().apply { put("username", newUsername) }.toString())
                }
                val responseCode = conn.responseCode
                if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    if (!refreshAndRetry()) {
                        viewModelScope.launch(Dispatchers.Main) {
                            _isLoading.value = false
                            _snackBarState.value = SnackBarState(show = true, isError = true, message = "Session expired.")
                        }
                        return@launch
                    }
                    updateUsername(newUsername)
                    return@launch
                }
                conn.inputStream.bufferedReader().readText()

                viewModelScope.launch(Dispatchers.Main) {
                    _isLoading.value = false
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        loadProfile(currentUserId)
                        _snackBarState.value = SnackBarState(show = true, isError = false, message = "Username updated!")
                    } else {
                        _snackBarState.value = SnackBarState(show = true, isError = true, message = "Update failed")
                    }
                }
            } catch (e: Exception) {
                viewModelScope.launch(Dispatchers.Main) {
                    _isLoading.value = false
                    _snackBarState.value = SnackBarState(show = true, isError = true, message = e.message ?: "Error")
                }
            }
        }
    }

    fun updateProfessions(professions: List<String>) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = URL("$BASE_URL/users/me")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "PATCH"
                conn.setRequestProperty("Authorization", "Bearer $token")
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true
                val body = JSONObject().apply {
                    put("professions", org.json.JSONArray(professions))
                }
                OutputStreamWriter(conn.outputStream).use { it.write(body.toString()) }
                val responseCode = conn.responseCode
                if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    if (!refreshAndRetry()) {
                        viewModelScope.launch(Dispatchers.Main) {
                            _isLoading.value = false
                            _snackBarState.value = SnackBarState(show = true, isError = true, message = "Session expired.")
                        }
                        return@launch
                    }
                    updateProfessions(professions)
                    return@launch
                }
                conn.inputStream.bufferedReader().readText()

                viewModelScope.launch(Dispatchers.Main) {
                    _isLoading.value = false
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        loadProfile(currentUserId)
                        _snackBarState.value = SnackBarState(show = true, isError = false, message = "Professions updated!")
                    } else {
                        _snackBarState.value = SnackBarState(show = true, isError = true, message = "Update failed")
                    }
                }
            } catch (e: Exception) {
                viewModelScope.launch(Dispatchers.Main) {
                    _isLoading.value = false
                    _snackBarState.value = SnackBarState(show = true, isError = true, message = e.message ?: "Error")
                }
            }
        }
    }


    fun uploadProfileImage(context: Context, imageUri: Uri) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val bytes = context.contentResolver.openInputStream(imageUri)?.readBytes() ?: return@launch
                val boundary = "Boundary-${System.currentTimeMillis()}"
                val url = URL("$BASE_URL/users/me/profile-image")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Authorization", "Bearer $token")
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
                conn.doOutput = true
                conn.outputStream.use { os ->
                    os.write("--$boundary\r\n".toByteArray())
                    os.write("Content-Disposition: form-data; name=\"file\"; filename=\"profile.jpg\"\r\n".toByteArray())
                    os.write("Content-Type: image/jpeg\r\n\r\n".toByteArray())
                    os.write(bytes)
                    os.write("\r\n--$boundary--\r\n".toByteArray())
                }
                val responseCode = conn.responseCode
                if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    if (!refreshAndRetry()) {
                        viewModelScope.launch(Dispatchers.Main) {
                            _isLoading.value = false
                            _snackBarState.value = SnackBarState(show = true, isError = true, message = "Session expired.")
                        }
                        return@launch
                    }
                    uploadProfileImage(context, imageUri)
                    return@launch
                }
                conn.inputStream.bufferedReader().readText()

                viewModelScope.launch(Dispatchers.Main) {
                    _isLoading.value = false
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        loadProfile(currentUserId)
                        _snackBarState.value = SnackBarState(show = true, isError = false, message = "Photo updated!")
                    } else {
                        _snackBarState.value = SnackBarState(show = true, isError = true, message = "Upload failed")
                    }
                }
            } catch (e: Exception) {
                viewModelScope.launch(Dispatchers.Main) {
                    _isLoading.value = false
                    _snackBarState.value = SnackBarState(show = true, isError = true, message = e.message ?: "Error")
                }
            }
        }
    }


    fun loadPosts(userId: Int = currentUserId) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = URL("$BASE_URL/users/$userId/posts")
                val conn = url.openConnection() as HttpURLConnection
                conn.setRequestProperty("Authorization", "Bearer $token")
                val responseCode = conn.responseCode
                if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    if (!refreshAndRetry()) return@launch
                    loadPosts(userId)
                    return@launch
                }
                val responseText = if (responseCode == HttpURLConnection.HTTP_OK)
                    conn.inputStream.bufferedReader().readText() else "[]"

                val arr = JSONArray(responseText)
                val list = mutableListOf<PostModel>()
                for (i in 0 until arr.length()) {
                    val item = arr.getJSONObject(i)
                    list.add(PostModel(
                        id = item.getInt("id"),
                        authorId = item.getInt("author_id"),
                        content = item.optString("content"),
                        imagePath = item.optString("image_path").takeIf { it.isNotBlank() && it != "null" },
                        createdAt = item.optString("created_at"),
                        likeCount = item.optInt("like_count", 0),
                        commentCount = item.optInt("comment_count", 0),
                        shareCount = item.optInt("share_count", 0)
                    ))
                }
                viewModelScope.launch(Dispatchers.Main) { _posts.value = list }
            } catch (e: Exception) {
                Log.e("ProfileVM", "loadPosts error", e)
            }
        }
    }


    fun createPostWithImage(content: String, imageUri: Uri?, context: Context) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val boundary = "Boundary-${System.currentTimeMillis()}"
                val url = URL("$BASE_URL/posts")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Authorization", "Bearer $token")
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
                conn.doOutput = true

                conn.outputStream.use { os ->
                    os.write("--$boundary\r\n".toByteArray())
                    os.write("Content-Disposition: form-data; name=\"content\"\r\n\r\n".toByteArray())
                    os.write("$content\r\n".toByteArray())

                    if (imageUri != null) {
                        val bytes = context.contentResolver.openInputStream(imageUri)?.readBytes()
                        if (bytes != null) {
                            os.write("--$boundary\r\n".toByteArray())
                            os.write("Content-Disposition: form-data; name=\"file\"; filename=\"post.jpg\"\r\n".toByteArray())
                            os.write("Content-Type: image/jpeg\r\n\r\n".toByteArray())
                            os.write(bytes)
                            os.write("\r\n".toByteArray())
                        }
                    }

                    os.write("--$boundary--\r\n".toByteArray())
                }

                val responseCode = conn.responseCode
                if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    if (!refreshAndRetry()) return@launch
                    createPostWithImage(content, imageUri, context)
                    return@launch
                }
                conn.inputStream.bufferedReader().readText()

                viewModelScope.launch(Dispatchers.Main) {
                    _isLoading.value = false
                    if (responseCode == HttpURLConnection.HTTP_CREATED) {
                        loadPosts(currentUserId)
                        _snackBarState.value = SnackBarState(show = true, isError = false, message = "Post created!")
                    } else {
                        _snackBarState.value = SnackBarState(show = true, isError = true, message = "Failed to create post")
                    }
                }
            } catch (e: Exception) {
                Log.e("ProfileVM", "createPostWithImage error", e)
                viewModelScope.launch(Dispatchers.Main) {
                    _isLoading.value = false
                    _snackBarState.value = SnackBarState(show = true, isError = true, message = e.message ?: "Error")
                }
            }
        }
    }

    fun toggleLike(post: PostModel) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val method = if (post.likedByMe) "DELETE" else "POST"
                val url = URL("$BASE_URL/posts/${post.id}/like")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = method
                conn.setRequestProperty("Authorization", "Bearer $token")
                conn.responseCode
                if (conn.responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    if (refreshAndRetry()) toggleLike(post)
                    return@launch
                }
                conn.inputStream.bufferedReader().readText()

                viewModelScope.launch(Dispatchers.Main) {
                    _posts.value = _posts.value.map {
                        if (it.id == post.id) it.copy(
                            likedByMe = !post.likedByMe,
                            likeCount = if (post.likedByMe) it.likeCount - 1 else it.likeCount + 1
                        ) else it
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
                val url = URL("$BASE_URL/posts/$postId/comments")
                val conn = url.openConnection() as HttpURLConnection
                conn.setRequestProperty("Authorization", "Bearer $token")
                val responseCode = conn.responseCode
                if (conn.responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    if (refreshAndRetry()) loadComments(postId)
                    return@launch
                }
                val responseText = if (responseCode == HttpURLConnection.HTTP_OK)
                    conn.inputStream.bufferedReader().readText() else "[]"

                val arr = JSONArray(responseText)
                val list = mutableListOf<CommentModel>()
                for (i in 0 until arr.length()) {
                    val item = arr.getJSONObject(i)
                    list.add(CommentModel(
                        id = item.getInt("id"),
                        postId = item.getInt("post_id"),
                        authorId = item.getInt("author_id"),
                        content = item.optString("content"),
                        createdAt = item.optString("created_at"),
                        authorName = item.optString("author_username", "Unknown"),
                        ))
                }
                viewModelScope.launch(Dispatchers.Main) { _comments.value = list }
            } catch (e: Exception) {
                Log.e("ProfileVM", "loadComments error", e)
            }
        }
    }


    fun addComment(postId: Int, content: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = URL("$BASE_URL/posts/$postId/comments")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Authorization", "Bearer $token")
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true
                OutputStreamWriter(conn.outputStream).use {
                    it.write(JSONObject().apply { put("content", content) }.toString())
                }
                val responseCode = conn.responseCode
                if (conn.responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    if (refreshAndRetry()) addComment(postId, content)
                    return@launch
                }
                conn.inputStream.bufferedReader().readText()

                if (responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_OK) {
                    loadComments(postId)
                    viewModelScope.launch(Dispatchers.Main) {
                        _posts.value = _posts.value.map {
                            if (it.id == postId) it.copy(commentCount = it.commentCount + 1) else it
                        }
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
                val url = URL("$BASE_URL/posts/$postId/share")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Authorization", "Bearer $token")
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true
                OutputStreamWriter(conn.outputStream).use {
                    it.write(JSONObject().apply { put("conversation_id", conversationId) }.toString())
                }
                val responseCode = conn.responseCode
                if (conn.responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    if (refreshAndRetry()) sharePost(postId, conversationId)
                    return@launch
                }
                conn.inputStream.bufferedReader().readText()

                viewModelScope.launch(Dispatchers.Main) {
                    if (responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_OK) {
                        _snackBarState.value = SnackBarState(show = true, isError = false, message = "Post shared!")
                        _posts.value = _posts.value.map {
                            if (it.id == postId) it.copy(shareCount = it.shareCount + 1) else it
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("ProfileVM", "sharePost error", e)
            }
        }
    }
}