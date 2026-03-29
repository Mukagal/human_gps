package com.example.pmadvanced.presenter.ui.main.screen

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.pmadvanced.R
import com.example.pmadvanced.data.model.MessageModel
import com.example.pmadvanced.presenter.ui.main.MainActivityNavigationNames
import com.example.pmadvanced.presenter.ui.main.event.MainScreenAction
import com.example.pmadvanced.presenter.ui.main.event.MainScreenEvent
import com.example.pmadvanced.presenter.ui.main.viewmodel.ProfileViewModel
import com.example.pmadvanced.ui.theme.White
import com.example.pmadvanced.ui.util.HeightSpacer
import com.example.pmadvanced.ui.util.ImageCircle
import com.example.pmadvanced.ui.util.WidthSpacer
import com.example.pmadvanced.ui.util.formatTimestamp

@Composable
fun ChatScreen(
    navController: NavHostController,
    mainScreenEvent: State<MainScreenEvent>,
    action: (MainScreenAction) -> Unit,
    profileViewModel: ProfileViewModel
) {

    val backDispatcher = LocalOnBackPressedDispatcherOwner.current

    val messageText = remember {
        mutableStateOf("")
    }


    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Black),
        verticalArrangement = Arrangement.SpaceBetween
    ){
        Column (
            modifier = Modifier
                .background(color = Color.Black)
        ) {
            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 15.dp, horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row (
                    modifier = Modifier,
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "",
                        tint = White,
                        modifier = Modifier.clickable {
                            backDispatcher?.onBackPressedDispatcher?.onBackPressed()
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
                            AsyncImage(model = photo, contentDescription = "", contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize())
                        } else {
                            Box(modifier = Modifier.fillMaxSize().background(Color.DarkGray),
                                contentAlignment = Alignment.Center) {
                                Icon(painter = painterResource(R.drawable.person_icon), contentDescription = "", tint = White)
                            }
                        }
                    }
                    WidthSpacer(width = 15.dp)
                    Text(text = mainScreenEvent.value.selectedUser?.userName ?: "", color = White, fontSize = 20.sp)
                }

                Icon(
                    painter = painterResource(id = R.drawable.dot_menu_icon),
                    contentDescription = "",
                    tint = White
                )
            }

            HorizontalDivider(
                thickness = 1.dp,
                color = Color.Gray
            )
        }

        LazyColumn(
            reverseLayout = true,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            mainScreenEvent.value.messagesList?.forEach { item ->
                if (item.senderId == mainScreenEvent.value.currentUser?.userId){
                    item {
                        SendChatItem(item)
                    }
                } else {
                    item {
                        ReceiveChatItem(item)
                    }
                }
            }
        }

        Column (
            modifier = Modifier
                .fillMaxWidth()
        ){
            HorizontalDivider(
                thickness = 1.dp,
                color = Color.Gray
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 15.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageText.value,
                    onValueChange = {
                        messageText.value = it
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = White,
                        focusedBorderColor = White,
                        focusedTextColor = White
                    ),
                    modifier = Modifier
                        .height(80.dp)
                        .weight(1f)
                    ,
                    shape = RoundedCornerShape(25.dp),
                    label = {
                        Text(text = "Type message", color = Color.Gray)
                    }
                )

                WidthSpacer()

                Button(onClick = {
                    action(MainScreenAction.SendMessage(messageText.value){
                        if (it){
                            messageText.value = ""
                        }
                    })
                },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = White,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .size(50.dp),
                    contentPadding = PaddingValues(10.dp),
                    shape = RoundedCornerShape(15.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.send_icon),
                        contentDescription = "",
                        tint = Color.Black,
                        modifier = Modifier
                            .fillMaxSize()
                        )
                }

            }

        }

    }


}

@Composable
fun SendChatItem(item: MessageModel) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 15.dp, vertical = 15.dp)
    ) { constraints
        
        Column(
            modifier = Modifier
                .fillMaxWidth(fraction = 0.8f)
                .align(Alignment.CenterEnd)
        ) {
            Text(text = item.text?: "",
                color = Color.Black,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = White, shape = RoundedCornerShape(10.dp))
                    .padding(vertical = 15.dp, horizontal = 10.dp)
                )
            HeightSpacer(height = 5.dp)
            Text(text = item.timeStamp?:"",
                color = Color.Gray,
                fontSize = 8.sp,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .padding(start = 5.dp)
                )
        }
        
    }   
}

@Composable
fun ReceiveChatItem(item: MessageModel) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 15.dp, vertical = 15.dp)
    ) { constraints

        Column(
            modifier = Modifier
                .fillMaxWidth(fraction = 0.8f)
                .align(Alignment.CenterStart)
        ) {
            Text(text = item.text?: "",
                color = Color.Black,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color.Gray, shape = RoundedCornerShape(10.dp))
                    .padding(vertical = 15.dp, horizontal = 10.dp)
            )
            HeightSpacer(height = 5.dp)
            Text(text = item.timeStamp?: "",
                color = Color.Gray,
                fontSize = 8.sp,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .padding(end = 5.dp)
                    .fillMaxWidth()
            )
        }

    }
}
