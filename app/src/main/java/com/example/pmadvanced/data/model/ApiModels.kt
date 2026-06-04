package com.example.pmadvanced.data.model

import com.google.gson.annotations.SerializedName

data class TokenRefreshResponse(
    @SerializedName("access_token") val accessToken: String
)

data class UserResponse(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("username") val username: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("profile_image_path") val profileImagePath: String? = null,
    @SerializedName("professions") val professions: List<String>? = null
)

data class UserLocationResponse(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("latitude") val latitude: Double?,
    @SerializedName("longitude") val longitude: Double?
)

data class NearbyUserResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("username") val username: String? = null,
    @SerializedName("profile_image_path") val profileImagePath: String? = null,
    @SerializedName("distance_km") val distanceKm: Double? = null
)

data class ConversationResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("user1_id") val user1Id: Int,
    @SerializedName("user2_id") val user2Id: Int,
    @SerializedName("created_at") val createdAt: String? = null
)

data class MessageResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("conversation_id") val conversationId: Int? = null,
    @SerializedName("sender_id") val senderId: Int,
    @SerializedName("receiver_id") val receiverId: Int? = null,
    @SerializedName("content") val content: String,
    @SerializedName("sent_at") val sentAt: String? = null,
    @SerializedName("received_at") val receivedAt: String? = null
)

data class MessageRequest(
    @SerializedName("content") val content: String
)

data class UserUpdateRequest(
    @SerializedName("username") val username: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("password") val password: String? = null,
    @SerializedName("professions") val professions: List<String>? = null
)

data class PostResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("author_id") val authorId: Int,
    @SerializedName("content") val content: String? = null,
    @SerializedName("image_path") val imagePath: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("like_count") val likeCount: Int = 0,
    @SerializedName("comment_count") val commentCount: Int = 0,
    @SerializedName("share_count") val shareCount: Int = 0
)

data class CommentResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("post_id") val postId: Int,
    @SerializedName("author_id") val authorId: Int,
    @SerializedName("content") val content: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("author_username") val authorUsername: String? = null
)

data class CommentRequest(
    @SerializedName("content") val content: String
)

data class RatingSummaryResponse(
    @SerializedName("average_rating") val averageRating: Double? = null,
    @SerializedName("total_ratings") val totalRatings: Int = 0
)

data class SharePostRequest(
    @SerializedName("conversation_id") val conversationId: Int
)

fun UserResponse.toUserModel() = UserModel(
    userId = id,
    userName = username,
    email = email,
    profileImage = profileImagePath?.takeIf { it.isNotBlank() && it != "null" },
    professions = professions
)

fun NearbyUserResponse.toUserModel() = UserModel(
    userId = id,
    userName = username,
    profileImage = profileImagePath?.takeIf { it.isNotBlank() && it != "null" }
)

fun MessageResponse.toMessageModel() = MessageModel(
    messageId = id,
    senderId = senderId,
    text = content,
    timeStamp = sentAt.orEmpty()
)

fun PostResponse.toPostModel(authorName: String? = null) = PostModel(
    id = id,
    authorId = authorId,
    authorName = authorName,
    content = content.orEmpty(),
    imagePath = imagePath?.takeIf { it.isNotBlank() && it != "null" },
    createdAt = createdAt,
    likeCount = likeCount,
    commentCount = commentCount,
    shareCount = shareCount
)

fun CommentResponse.toCommentModel() = CommentModel(
    id = id,
    postId = postId,
    authorId = authorId,
    authorName = authorUsername,
    content = content.orEmpty(),
    createdAt = createdAt
)
