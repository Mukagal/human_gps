package com.example.pmadvanced.presenter.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.pmadvanced.navigation.MainRoutes
import com.example.pmadvanced.navigation.chatNavGraph
import com.example.pmadvanced.navigation.komekNavGraph
import com.example.pmadvanced.navigation.mapNavGraph
import com.example.pmadvanced.navigation.profileNavGraph
import com.example.pmadvanced.presenter.ui.main.viewmodel.MainActivityViewModel
import com.example.pmadvanced.presenter.ui.main.viewmodel.ProfileViewModel

@Composable
fun MainActivityNavigation(mainActivityViewModel: MainActivityViewModel) {
    val navController = rememberNavController()
    val profileViewModel: ProfileViewModel = viewModel()
    val mainScreenEvent = mainActivityViewModel.mainScreenEvent.collectAsState()
    val bottomItems = listOf(
        BottomNavItem.Map,
        BottomNavItem.Chats,
        BottomNavItem.Profile,
        BottomNavItem.Komek
    )

    Scaffold(
        bottomBar = {
            val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
            if (currentRoute in bottomItems.map { it.route } || currentRoute == MainRoutes.MAP_WITH_USER) {
                NavigationBar {
                    bottomItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = { navController.navigate(item.route) { launchSingleTop = true } },
                            icon = { Icon(item.icon, item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = MainRoutes.CHAT_GRAPH,
            modifier = Modifier.padding(paddingValues)
        ) {
            chatNavGraph(
                navController = navController,
                mainScreenEvent = mainScreenEvent,
                action = mainActivityViewModel::action,
                mainActivityViewModel = mainActivityViewModel,
                profileViewModel = profileViewModel
            )
            mapNavGraph()
            profileNavGraph(
                navController = navController,
                profileViewModel = profileViewModel,
                action = mainActivityViewModel::action,
                mainScreenEvent = mainScreenEvent
            )
            komekNavGraph(
                navController = navController,
                currentUserId = mainActivityViewModel.currentUserId
            )
        }
    }
}

object MainActivityNavigationNames {
    const val MAIN_SCREEN = MainRoutes.MAIN_SCREEN
    const val CHAT_SCREEN = MainRoutes.CHAT_SCREEN
    const val PROFILE_SCREEN = MainRoutes.PROFILE_SCREEN
    const val SEARCH_SCREEN = MainRoutes.SEARCH_SCREEN
    const val MAP_SCREEN = MainRoutes.MAP_SCREEN
    const val KOMEK_SCREEN = MainRoutes.KOMEK_SCREEN
}
