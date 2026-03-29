package com.example.pmadvanced.presenter.ui.main.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pmadvanced.presenter.ui.main.MainActivityNavigationNames
import com.example.pmadvanced.presenter.ui.main.event.MainScreenAction
import com.example.pmadvanced.presenter.ui.main.event.MainScreenEvent
import com.example.pmadvanced.ui.theme.White
import com.example.pmadvanced.ui.util.HeightSpacer
import com.example.pmadvanced.ui.util.WidthSpacer
import androidx.compose.foundation.clickable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun SearchScreen(
    mainScreenEvent: State<MainScreenEvent>,
    action: (MainScreenAction) -> Unit,
    navController: NavController
) {
    LaunchedEffect(Unit) {
        action(MainScreenAction.SearchUsers(""))
    }

    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Black),
        verticalArrangement = Arrangement.Top  
    ) {
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
            Text(text = "Search", color = White, fontSize = 20.sp)
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                action(MainScreenAction.SearchUsers(it)) 
            },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = White,
                focusedBorderColor = White,
                focusedTextColor = White,
                unfocusedTextColor = White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(25.dp),
            label = { Text(text = "Search User", color = Color.Gray) }
        )

        HeightSpacer(height = 10.dp)

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(mainScreenEvent.value.userList?.size ?: 0) { index ->
                val user = mainScreenEvent.value.userList!![index]
                ChatItem(
                    userItem = user,
                    navController = navController,
                    otherUserId = user.userId
                ) {
                    action(MainScreenAction.SelectUser(user))
                    navController.navigate(MainActivityNavigationNames.CHAT_SCREEN)
                }
            }
        }
    }
}