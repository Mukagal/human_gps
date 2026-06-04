package com.example.pmadvanced.data.remote

import com.example.pmadvanced.data.model.CommentRequest
import com.example.pmadvanced.data.model.CommentResponse
import com.example.pmadvanced.data.model.ConversationResponse
import com.example.pmadvanced.data.model.MessageRequest
import com.example.pmadvanced.data.model.MessageResponse
import com.example.pmadvanced.data.model.NearbyUserResponse
import com.example.pmadvanced.data.model.PostResponse
import com.example.pmadvanced.data.model.RatingSummaryResponse
import com.example.pmadvanced.data.model.SharePostRequest
import com.example.pmadvanced.data.model.TokenRefreshResponse
import com.example.pmadvanced.data.model.UserLocationResponse
import com.example.pmadvanced.data.model.UserResponse
import com.example.pmadvanced.data.model.UserUpdateRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface MamanTapApi {
    @POST("refresh")
    suspend fun refreshToken(
        @Header("Authorization") refreshToken: String
    ): Response<TokenRefreshResponse>

    @GET("users/{userId}")
    suspend fun getUser(
        @Path("userId") userId: Int
    ): Response<UserResponse>

    @GET("users/{userId}/profile")
    suspend fun getUserProfile(
        @Path("userId") userId: Int
    ): Response<UserResponse>

    @GET("users")
    suspend fun getUsers(
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 100
    ): Response<List<UserResponse>>

    @GET("users/{userId}/location")
    suspend fun getUserLocation(
        @Path("userId") userId: Int
    ): Response<UserLocationResponse>

    @GET("users/nearby")
    suspend fun getNearbyUsers(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("radius_km") radiusKm: Double
    ): Response<List<NearbyUserResponse>>

    @GET("users/{userId}/conversations")
    suspend fun getConversations(
        @Path("userId") userId: Int
    ): Response<List<ConversationResponse>>

    @POST("conversations")
    suspend fun createConversation(
        @Query("user_b") userB: Int
    ): Response<ConversationResponse>

    @GET("conversations/{conversationId}/messages")
    suspend fun getMessages(
        @Path("conversationId") conversationId: Int,
        @Query("limit") limit: Int = 20
    ): Response<List<MessageResponse>>

    @POST("conversations/{conversationId}/messages")
    suspend fun sendMessage(
        @Path("conversationId") conversationId: Int,
        @Body request: MessageRequest
    ): Response<MessageResponse>

    @PATCH("messages/{messageId}")
    suspend fun editMessage(
        @Path("messageId") messageId: Int,
        @Body request: MessageRequest
    ): Response<MessageResponse>

    @DELETE("messages/{messageId}")
    suspend fun deleteMessage(
        @Path("messageId") messageId: Int
    ): Response<Unit>

    @GET("users/{userId}/ratings")
    suspend fun getUserRating(
        @Path("userId") userId: Int
    ): Response<RatingSummaryResponse>

    @PATCH("users/me")
    suspend fun updateMe(
        @Body request: UserUpdateRequest
    ): Response<UserResponse>

    @Multipart
    @POST("users/me/profile-image")
    suspend fun uploadProfileImage(
        @Part file: MultipartBody.Part
    ): Response<UserResponse>

    @GET("users/{userId}/posts")
    suspend fun getUserPosts(
        @Path("userId") userId: Int,
        @Query("sort_by") sortBy: String = "latest",
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 20
    ): Response<List<PostResponse>>

    @GET("posts/{postId}")
    suspend fun getPost(
        @Path("postId") postId: Int
    ): Response<PostResponse>

    @Multipart
    @POST("posts")
    suspend fun createPost(
        @Part("content") content: RequestBody,
        @Part file: MultipartBody.Part?
    ): Response<PostResponse>

    @POST("posts/{postId}/like")
    suspend fun likePost(
        @Path("postId") postId: Int
    ): Response<Unit>

    @DELETE("posts/{postId}/like")
    suspend fun unlikePost(
        @Path("postId") postId: Int
    ): Response<Unit>

    @GET("posts/{postId}/comments")
    suspend fun getPostComments(
        @Path("postId") postId: Int
    ): Response<List<CommentResponse>>

    @POST("posts/{postId}/comments")
    suspend fun addComment(
        @Path("postId") postId: Int,
        @Body request: CommentRequest
    ): Response<CommentResponse>

    @POST("posts/{postId}/share")
    suspend fun sharePost(
        @Path("postId") postId: Int,
        @Body request: SharePostRequest
    ): Response<Unit>
}
