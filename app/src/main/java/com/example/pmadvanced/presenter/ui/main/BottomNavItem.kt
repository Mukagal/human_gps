package com.example.pmadvanced.presenter.ui.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.pmadvanced.navigation.MainRoutes

sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object Map: BottomNavItem(MainRoutes.MAP_SCREEN, Icons.Filled.Map, "Map")
    object Chats: BottomNavItem(MainRoutes.MAIN_SCREEN, Icons.AutoMirrored.Filled.Chat, "Chats")
    object Profile: BottomNavItem(MainRoutes.PROFILE_SCREEN, Icons.Filled.Person, "Profile")
    object Komek: BottomNavItem(MainRoutes.KOMEK_SCREEN, Icons.Filled.Handshake, "Komek")
}
