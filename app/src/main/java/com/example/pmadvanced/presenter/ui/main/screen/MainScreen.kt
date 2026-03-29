package com.example.pmadvanced.presenter.ui.main.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.pmadvanced.R
import com.example.pmadvanced.data.model.UserModel
import com.example.pmadvanced.presenter.ui.main.MainActivityNavigationNames
import com.example.pmadvanced.presenter.ui.main.event.MainScreenAction
import com.example.pmadvanced.presenter.ui.main.event.MainScreenEvent
import com.example.pmadvanced.presenter.ui.main.viewmodel.ProfileViewModel
import com.example.pmadvanced.ui.theme.Black
import com.example.pmadvanced.ui.theme.White
import com.example.pmadvanced.ui.util.HeightSpacer
import com.example.pmadvanced.ui.util.WidthSpacer
import coil.compose.AsyncImage

@Composable
fun MainScreen(
    navController: NavHostController,
    mainScreenEvent: State<MainScreenEvent>,
    action: (MainScreenAction) -> Unit,
    onRefresh: () -> Unit,
    profileViewModel: ProfileViewModel
) {

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
    ) { constraints

        Column (
            modifier = Modifier
                .fillMaxSize()
        ) {
            Row (
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                HeightSpacer()
                Text(
                    text = "Maman-Tap",
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = White
                )

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable { navController.navigate(MainActivityNavigationNames.PROFILE_SCREEN) }
                ) {
                    val myPhoto = profileViewModel.profile.collectAsState().value?.profileImage
                    if (!myPhoto.isNullOrBlank()) {
                        AsyncImage(model = myPhoto, contentDescription = "", contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize())
                    } else {
                        Image(painter = painterResource(id = R.drawable.person_icon), contentDescription = "",
                            modifier = Modifier.fillMaxSize().background(White, CircleShape).padding(5.dp))
                    }
                }
            }


            Text(
                text = "Chats",
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier
                    .padding(horizontal = 20.dp)
            )
            HeightSpacer()
            mainScreenEvent.value.conversationList?.let { list ->
                LazyColumn {
                    items(list.size){ index ->
                    val conv = list[index]
                    ChatItem(
                        userItem = conv.otherUser ?: UserModel(),
                        lastMessage = conv.lastMessage ?: "No message yet",
                        navController = navController,
                        otherUserId = conv.otherUser?.userId)
                    {
                        action(MainScreenAction.SelectConversation( conv.conversationId, conv.otherUser ?: UserModel()))
                        navController.navigate(MainActivityNavigationNames.CHAT_SCREEN)
                    }
                }
                }
            }

        }


        FloatingActionButton(
            onClick = {
                navController.navigate(MainActivityNavigationNames.SEARCH_SCREEN)
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(15.dp),
            containerColor = White,
            contentColor = Color.Black
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = ""
            )
        }

    }
}


@Composable
fun ChatItem(
    userItem: UserModel,
    lastMessage: String = "No message yet",
    navController: NavController,
    otherUserId: Int? = null,
    onClick: () -> Unit
) {
    Column (
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            }
        ,
        verticalArrangement = Arrangement.Bottom
    ){

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ){
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier
                   .fillMaxWidth()
                   .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {

                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .clickable {
                            otherUserId?.let {
                                navController.navigate("${MainActivityNavigationNames.PROFILE_SCREEN}/$it")
                            }
                        }
                ) {
                    val photo = userItem.profileImage
                    if (!photo.isNullOrBlank()) {
                        AsyncImage(model = photo, contentDescription = "", contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize())
                    } else {
                        Box(modifier = Modifier.fillMaxSize().background(Color.DarkGray),
                            contentAlignment = Alignment.Center) {
                            Icon(painter = painterResource(R.drawable.person_icon), contentDescription = "", tint = White)
                        }
                    }
                }

                WidthSpacer(width = 20.dp)

                Column(
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Top,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = userItem.userName ?: "",
                        fontSize = 20.sp,
                        color = White
                    )
                    HeightSpacer(height = 10.dp)
                    Text(
                        text = lastMessage,
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }
            }
            Column (
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.End,
                modifier = Modifier
                    .height(80.dp)
                    .padding(top = 10.dp)
            ) {
                Text(
                    text = "4:29 pm",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }


        HorizontalDivider(
            thickness = 1.dp,
            color = Color.Gray
        )
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainScreenPreview() {
}