package com.example.pmadvanced.presenter.ui.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object Map: BottomNavItem("MAP_SCREEN",  Icons.Filled.Map,"Map")
    object Chats: BottomNavItem("MAIN_SCREEN", Icons.AutoMirrored.Filled.Chat,"Chats")
    object Profile: BottomNavItem("PROFILE_SCREEN", Icons.Filled.Person,"Profile")
    object Komek: BottomNavItem("Komek_SCREEN", Icons.Filled.Handshake,"Komek")

}