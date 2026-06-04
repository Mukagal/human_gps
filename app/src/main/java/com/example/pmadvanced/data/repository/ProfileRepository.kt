package com.example.pmadvanced.data.repository

import com.example.pmadvanced.data.local.AuthTokenStore
import com.example.pmadvanced.data.model.CommentModel
import com.example.pmadvanced.data.model.CommentRequest
import com.example.pmadvanced.data.model.PostModel
import com.example.pmadvanced.data.model.SharePostRequest
import com.example.pmadvanced.data.model.UserModel
import com.example.pmadvanced.data.model.UserUpdateRequest
import com.example.pmadvanced.data.model.toCommentModel
import com.example.pmadvanced.data.model.toPostModel
import com.example.pmadvanced.data.model.toUserModel
import com.example.pmadvanced.data.remote.MamanTapApi
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class ProfileRepository(
    api: MamanTapApi,
    private val tokenStore: AuthTokenStore
) : BaseRepository(api, tokenStore) {
    private val api = api

    val currentUserId: Int
        get() = tokenStore.currentUserId

    suspend fun getProfile(userId: Int): UserModel? {
        return authenticatedCall { api.getUser(userId) }?.toUserModel()
            ?: authenticatedCall { api.getUserProfile(userId) }?.toUserModel()
    }

    suspend fun getUserRating(userId: Int): Pair<Double?, Int> {
        val rating = authenticatedCall { api.getUserRating(userId) }
        return rating?.averageRating to (rating?.totalRatings ?: 0)
    }

    suspend fun updateUsername(newUsername: String): Boolean {
        return authenticatedCall {
            api.updateMe(UserUpdateRequest(username = newUsername))
        } != null
    }

    suspend fun updateProfessions(professions: List<String>): Boolean {
        return authenticatedCall {
            api.updateMe(UserUpdateRequest(professions = professions))
        } != null
    }

    suspend fun uploadProfileImage(bytes: ByteArray): Boolean {
        val requestBody = bytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
        val file = MultipartBody.Part.createFormData("file", "profile.jpg", requestBody)
        return authenticatedCall { api.uploadProfileImage(file) } != null
    }

    suspend fun getPosts(userId: Int): List<PostModel> {
        return authenticatedCall { api.getUserPosts(userId) }
            ?.map { it.toPostModel() }
            .orEmpty()
    }

    suspend fun createPost(content: String, imageBytes: ByteArray?): Boolean {
        val contentBody = content.toRequestBody("text/plain".toMediaType())
        val imagePart = imageBytes?.let {
            MultipartBody.Part.createFormData(
                "file",
                "post.jpg",
                it.toRequestBody("image/jpeg".toMediaTypeOrNull())
            )
        }

        return authenticatedCall { api.createPost(contentBody, imagePart) } != null
    }

    suspend fun toggleLike(post: PostModel): Boolean {
        return if (post.likedByMe) {
            isSuccessfulCall { api.unlikePost(post.id) }
        } else {
            isSuccessfulCall { api.likePost(post.id) }
        }
    }

    suspend fun getComments(postId: Int): List<CommentModel> {
        return authenticatedCall { api.getPostComments(postId) }
            ?.map { it.toCommentModel() }
            .orEmpty()
    }

    suspend fun addComment(postId: Int, content: String): Boolean {
        return authenticatedCall {
            api.addComment(postId, CommentRequest(content))
        } != null
    }

    suspend fun sharePost(postId: Int, conversationId: Int): Boolean {
        return isSuccessfulCall {
            api.sharePost(postId, SharePostRequest(conversationId))
        }
    }

    suspend fun getPost(postId: Int): PostModel? {
        val post = authenticatedCall { api.getPost(postId) } ?: return null
        val authorName = getProfile(post.authorId)?.userName
        return post.toPostModel(authorName)
    }

    fun logout() {
        tokenStore.clear()
    }
}
