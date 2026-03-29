package com.example.pmadvanced.presenter.ui.main.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.pmadvanced.R
import com.example.pmadvanced.data.model.PostModel
import com.example.pmadvanced.presenter.ui.main.viewmodel.ProfileViewModel
import com.example.pmadvanced.ui.theme.Gray
import com.example.pmadvanced.ui.theme.White
import com.example.pmadvanced.ui.util.HeightSpacer
import com.example.pmadvanced.ui.util.WidthSpacer
import coil.compose.AsyncImage
import com.example.pmadvanced.presenter.ui.main.event.MainScreenAction
import com.example.pmadvanced.presenter.ui.main.event.MainScreenEvent
import com.example.pmadvanced.presenter.ui.main.viewmodel.MainActivityViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel,
    userId: Int? = null,
    action: (MainScreenAction) -> Unit,
    mainScreenEvent: State<MainScreenEvent>,
    ) {
    val context = LocalContext.current
    val isOwnProfile = userId == null || userId == profileViewModel.currentUserId
    val targetUserId = userId ?: profileViewModel.currentUserId

    val profile by profileViewModel.profile.collectAsState()
    val posts by profileViewModel.posts.collectAsState()
    val comments by profileViewModel.comments.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var showPostDialog by remember { mutableStateOf(false) }
    var newUsername by remember { mutableStateOf("") }
    var newPostContent by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedPostForComments by remember { mutableStateOf<PostModel?>(null) }
    var selectedPostForShare by remember { mutableStateOf<PostModel?>(null) }
    var commentText by remember { mutableStateOf("") }
    var shareConversationId by remember { mutableStateOf("") }

    LaunchedEffect(targetUserId) {
        profileViewModel.loadProfile(targetUserId)
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { profileViewModel.uploadProfileImage(context, it) } }
    val postImagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    selectedPostForComments?.let { post ->
        ModalBottomSheet(
            onDismissRequest = { selectedPostForComments = null },
            containerColor = Color(0xFF1A1A1A)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Comments", color = White, fontSize = 18.sp)
                HeightSpacer(height = 8.dp)

                LazyColumn(modifier = Modifier.weight(1f, fill = false).heightIn(max = 300.dp)) {
                    items(comments) { comment ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.DarkGray),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = comment.authorName?.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                                    color = White,
                                    fontSize = 10.sp
                                )
                            }
                            WidthSpacer(width = 8.dp)
                            Column {
                                Text(
                                    text = comment.authorName ?: "User ${comment.authorId}",
                                    color = Color.Gray,
                                    fontSize = 11.sp
                                )
                                Text(text = comment.content, color = White, fontSize = 14.sp)
                                comment.createdAt?.let {
                                    Text(text = it.take(10), color = Color.Gray, fontSize = 10.sp)
                                }
                            }
                        }
                        HorizontalDivider(color = Color.DarkGray, thickness = 0.5.dp)
                    }
                }

                HeightSpacer(height = 8.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Add a comment…", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = White,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = White,
                            unfocusedTextColor = White
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    WidthSpacer(width = 8.dp)
                    Button(
                        onClick = {
                            if (commentText.isNotBlank()) {
                                profileViewModel.addComment(post.id, commentText)
                                commentText = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = White, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) { Text("Post") }
                }
                HeightSpacer(height = 16.dp)
            }
        }
    }

    selectedPostForShare?.let { post ->

        AlertDialog(
            onDismissRequest = { selectedPostForShare = null },
            title = { Text("Share to conversation") },
            text = {
                LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                    items(mainScreenEvent.value.conversationList?: emptyList()) { conv ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    profileViewModel.sharePost(post.id, conv.conversationId)
                                    selectedPostForShare = null
                                }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = conv.otherUser?.userName ?: "User ${conv.conversationId}",
                                color = White,
                                fontSize = 14.sp
                            )
                        }
                        HorizontalDivider(color = Color.DarkGray, thickness = 0.5.dp)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { selectedPostForShare = null }) { Text("Cancel") }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 15.dp, horizontal = 20.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "",
                    tint = White,
                    modifier = Modifier.clickable { navController.popBackStack() }
                )
                WidthSpacer()
                Text(
                    text = if (isOwnProfile) "My Profile" else profile?.userName ?: "Profile",
                    color = White,
                    fontSize = 20.sp
                )
            }

            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .then(if (isOwnProfile) Modifier.clickable { imagePickerLauncher.launch("image/*") } else Modifier)
                ) {
                    if (!profile?.profileImage.isNullOrBlank()) {
                        AsyncImage(
                            model = profile!!.profileImage,
                            contentDescription = "Profile photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize().background(Color.DarkGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.person_icon),
                                contentDescription = "",
                                tint = White,
                                modifier = Modifier.size(50.dp)
                            )
                        }
                    }
                }

                WidthSpacer(width = 16.dp)

                Column {
                    Text(text = profile?.userName ?: "Loading…", fontSize = 20.sp, color = White)
                    HeightSpacer(height = 4.dp)
                    Text(text = profile?.email ?: "", fontSize = 13.sp, color = Color.Gray)
                    if (isOwnProfile) {
                        HeightSpacer(height = 6.dp)
                        Button(
                            onClick = {
                                newUsername = profile?.userName ?: ""
                                showEditDialog = true
                            },
                            modifier = Modifier.height(30.dp),
                            shape = RoundedCornerShape(15.dp),
                            colors = ButtonDefaults.buttonColors(contentColor = White, containerColor = Gray),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) { Text("Edit", fontSize = 12.sp) }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Posts", color = White, fontSize = 18.sp)
                if (isOwnProfile) {
                    Button(
                        onClick = { showPostDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = White, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                    ) { Text("+ New Post", fontSize = 12.sp) }
                }
            }
        }

        items(posts) { post ->
            PostCard(
                post = post,
                onLike = { profileViewModel.toggleLike(post) },
                onComment = {
                    selectedPostForComments = post
                    profileViewModel.loadComments(post.id)
                },
                onShare = { selectedPostForShare = post }
            )
        }

        item { HeightSpacer(height = 24.dp) }
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Username") },
            text = {
                OutlinedTextField(
                    value = newUsername,
                    onValueChange = { newUsername = it },
                    label = { Text("Username") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    profileViewModel.updateUsername(newUsername)
                    showEditDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showPostDialog) {
        AlertDialog(
            onDismissRequest = { showPostDialog = false },
            title = { Text("New Post") },
            text = {
                Column {

                    OutlinedTextField(
                        value = newPostContent,
                        onValueChange = { newPostContent = it },
                        label = { Text("What's on your mind?") },
                        minLines = 3
                    )

                    Spacer(Modifier.height(10.dp))

                    Button(
                        onClick = { postImagePicker.launch("image/*") }
                    ) {
                        Text("Add Photo")
                    }

                    selectedImageUri?.let {
                        Spacer(Modifier.height(8.dp))

                        AsyncImage(
                            model = it,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newPostContent.isNotBlank()) {
                        profileViewModel.createPostWithImage(
                            newPostContent,
                            selectedImageUri,
                            context
                        )
                        newPostContent = ""
                        showPostDialog = false
                        selectedImageUri = null
                    }
                }) { Text("Post") }
            },
            dismissButton = {
                TextButton(onClick = { showPostDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun PostCard(
    post: PostModel,
    onLike: () -> Unit,
    onComment: () -> Unit,
    onShare: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = post.content, color = White, fontSize = 14.sp)

            post.imagePath?.let {
                HeightSpacer(height = 8.dp)
                AsyncImage(
                    model = it,
                    contentDescription = "Post image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            }

            post.createdAt?.let {
                HeightSpacer(height = 6.dp)
                Text(text = it.take(10), color = Color.Gray, fontSize = 10.sp)
            }

            HeightSpacer(height = 10.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onLike() }
                ) {
                    Icon(
                        imageVector = if (post.likedByMe) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (post.likedByMe) Color.Red else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                    WidthSpacer(width = 4.dp)
                    Text(text = "${post.likeCount}", color = Color.Gray, fontSize = 12.sp)
                }

                WidthSpacer(width = 20.dp)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onComment() }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Comment",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                    WidthSpacer(width = 4.dp)
                    Text(text = "${post.commentCount}", color = Color.Gray, fontSize = 12.sp)
                }

                WidthSpacer(width = 20.dp)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onShare() }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = "Share",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                    WidthSpacer(width = 4.dp)
                    Text(text = "${post.shareCount}", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }
    }
}