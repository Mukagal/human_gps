package com.example.pmadvanced.presenter.ui.main.screen

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.pmadvanced.R
import com.example.pmadvanced.data.model.MessageModel
import com.example.pmadvanced.presenter.ui.main.MainActivityNavigationNames
import com.example.pmadvanced.presenter.ui.main.event.MainScreenAction
import com.example.pmadvanced.presenter.ui.main.event.MainScreenEvent
import com.example.pmadvanced.presenter.ui.main.formatDayHeader
import com.example.pmadvanced.presenter.ui.main.formatMessageTime
import com.example.pmadvanced.presenter.ui.main.viewmodel.MainActivityViewModel
import com.example.pmadvanced.presenter.ui.main.viewmodel.ProfileViewModel
import com.example.pmadvanced.ui.theme.White
import com.example.pmadvanced.ui.util.WidthSpacer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavHostController,
    mainScreenEvent: State<MainScreenEvent>,
    action: (MainScreenAction) -> Unit,
    profileViewModel: ProfileViewModel,
    mainActivityViewModel: MainActivityViewModel
) {
    val messageText = remember { mutableStateOf("") }

    var selectedMessage by remember { mutableStateOf<MessageModel?>(null) }
    var showOptionsSheet by remember { mutableStateOf(false) }

    var showEditDialog by remember { mutableStateOf(false) }
    var editText by remember { mutableStateOf("") }

    DisposableEffect(Unit) {
        onDispose { mainActivityViewModel.stopMessagePolling() }
    }

    BackHandler {
        mainActivityViewModel.stopMessagePolling()
        navController.popBackStack()
    }

    if (showEditDialog && selectedMessage != null) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            containerColor = Color.DarkGray,
            title = { Text("Edit Message", color = White) },
            text = {
                OutlinedTextField(
                    value = editText,
                    onValueChange = { editText = it },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = White,
                        focusedBorderColor = White,
                        focusedTextColor = White,
                        unfocusedTextColor = White
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    selectedMessage?.messageId?.let { id ->
                        action(MainScreenAction.EditMessage(id, editText))
                    }
                    showEditDialog = false
                    showOptionsSheet = false
                }) { Text("Save", color = White) }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }

    if (showOptionsSheet && selectedMessage != null) {
        ModalBottomSheet(
            onDismissRequest = { showOptionsSheet = false },
            containerColor = Color(0xFF1C1C1E)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            editText = selectedMessage?.text ?: ""
                            showEditDialog = true
                        }
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.send_icon),
                        contentDescription = "Edit",
                        tint = White,
                        modifier = Modifier.size(20.dp)
                    )
                    WidthSpacer(width = 16.dp)
                    Text("Edit", color = White, fontSize = 16.sp)
                }

                HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedMessage?.messageId?.let { id ->
                                action(MainScreenAction.DeleteMessage(id))
                            }
                            showOptionsSheet = false
                        }
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.person_icon),
                        contentDescription = "Delete",
                        tint = Color.Red,
                        modifier = Modifier.size(20.dp)
                    )
                    WidthSpacer(width = 16.dp)
                    Text("Delete", color = Color.Red, fontSize = 16.sp)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Black),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.background(color = Color.Black)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 15.dp, horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "",
                        tint = White,
                        modifier = Modifier.clickable {
                            mainActivityViewModel.stopMessagePolling()
                            navController.popBackStack()
                        }
                    )
                    WidthSpacer()
                    Box(
                        modifier = Modifier
                            .size(45.dp)
                            .clip(CircleShape)
                            .clickable {
                                mainScreenEvent.value.selectedUser?.userId?.let { uid ->
                                    navController.navigate("${MainActivityNavigationNames.PROFILE_SCREEN}/$uid")
                                }
                            }
                    ) {
                        val photo = mainScreenEvent.value.selectedUser?.profileImage
                        if (!photo.isNullOrBlank()) {
                            AsyncImage(
                                model = photo,
                                contentDescription = "",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.DarkGray),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.person_icon),
                                    contentDescription = "",
                                    tint = White
                                )
                            }
                        }
                    }
                    WidthSpacer(width = 15.dp)
                    Text(
                        text = mainScreenEvent.value.selectedUser?.userName ?: "",
                        color = White,
                        fontSize = 20.sp
                    )
                }
                Icon(
                    painter = painterResource(id = R.drawable.dot_menu_icon),
                    contentDescription = "",
                    tint = White
                )
            }
            HorizontalDivider(thickness = 1.dp, color = Color.Gray)
        }

        LazyColumn(
            reverseLayout = true,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val messages = mainScreenEvent.value.messagesList?.reversed() ?: emptyList()
            val grouped = messages.groupBy { it.timeStamp?.substringBefore("T") }

            grouped.forEach { (day, dayMessages) ->
                dayMessages.forEach { msg ->
                    if (msg.senderId == mainScreenEvent.value.currentUser?.userId) {
                        item {
                            SendChatItem(
                                item = msg,
                                onLongClick = {
                                    selectedMessage = msg
                                    showOptionsSheet = true
                                }
                            )
                        }
                    } else {
                        item { ReceiveChatItem(msg) }
                    }
                }
                item {
                    Text(
                        text = formatDayHeader(day ?: ""),
                        color = Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(thickness = 1.dp, color = Color.Gray)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 15.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageText.value,
                    onValueChange = { messageText.value = it },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = White,
                        focusedBorderColor = White,
                        focusedTextColor = White
                    ),
                    modifier = Modifier
                        .height(80.dp)
                        .weight(1f),
                    shape = RoundedCornerShape(25.dp),
                    label = { Text(text = "Type message", color = Color.Gray) }
                )
                WidthSpacer()
                Button(
                    onClick = {
                        action(MainScreenAction.SendMessage(messageText.value) { success ->
                            if (success) messageText.value = ""
                        })
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = White,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier.size(50.dp),
                    contentPadding = PaddingValues(10.dp),
                    shape = RoundedCornerShape(15.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.send_icon),
                        contentDescription = "",
                        tint = Color.Black,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun SendChatItem(item: MessageModel, onLongClick: () -> Unit) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 15.dp, vertical = 15.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(fraction = 0.8f)
                .background(color = White, shape = RoundedCornerShape(10.dp))
                .combinedClickable(
                    onClick = {},
                    onLongClick = onLongClick
                )
                .padding(vertical = 10.dp, horizontal = 10.dp)
                .align(Alignment.CenterEnd)
        ) {
            Text(text = item.text ?: "", color = Color.Black, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())
            Text(
                text = formatMessageTime(item.timeStamp ?: ""),
                color = Color.DarkGray,
                fontSize = 9.sp,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ReceiveChatItem(item: MessageModel) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 15.dp, vertical = 15.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(fraction = 0.8f)
                .background(color = White, shape = RoundedCornerShape(10.dp))
                .padding(vertical = 10.dp, horizontal = 10.dp)
        ) {
            Text(text = item.text ?: "", color = Color.Black, textAlign = TextAlign.Start, modifier = Modifier.fillMaxWidth())
            Text(
                text = formatMessageTime(item.timeStamp ?: ""),
                color = Color.DarkGray,
                fontSize = 9.sp,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}