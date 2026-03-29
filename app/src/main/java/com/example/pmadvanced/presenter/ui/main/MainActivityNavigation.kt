package com.example.pmadvanced.presenter.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.pmadvanced.presenter.ui.komek.KomekScreen
import com.example.pmadvanced.presenter.ui.main.MainActivityNavigationNames.CHAT_SCREEN
import com.example.pmadvanced.presenter.ui.main.MainActivityNavigationNames.MAIN_SCREEN
import com.example.pmadvanced.presenter.ui.main.MainActivityNavigationNames.MAP_SCREEN
import com.example.pmadvanced.presenter.ui.main.MainActivityNavigationNames.PROFILE_SCREEN
import com.example.pmadvanced.presenter.ui.main.MainActivityNavigationNames.SEARCH_SCREEN
import com.example.pmadvanced.presenter.ui.main.MainActivityNavigationNames.KOMEK_SCREEN
import com.example.pmadvanced.presenter.ui.main.screen.ChatScreen
import com.example.pmadvanced.presenter.ui.main.screen.MainScreen
import com.example.pmadvanced.presenter.ui.main.screen.ProfileScreen
import com.example.pmadvanced.presenter.ui.main.screen.SearchScreen
import com.example.pmadvanced.presenter.ui.main.viewmodel.MainActivityViewModel
import com.example.pmadvanced.presenter.ui.main.viewmodel.ProfileViewModel
import com.example.pmadvanced.presenter.ui.map.MapScreen

@Composable
fun MainActivityNavigation(mainActivityViewModel: MainActivityViewModel) {

    val navController = rememberNavController()
    val profileViewModel: ProfileViewModel = viewModel()
    val items = listOf(
        BottomNavItem.Map,
        BottomNavItem.Chats,
        BottomNavItem.Profile,
        BottomNavItem.Komek
    )
    Scaffold(
        bottomBar = {
            val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
            val showBar = currentRoute in listOf("MAIN_SCREEN","PROFILE_SCREEN","MAP_SCREEN", "KOMEK_SCREEN")
            if (showBar) {
                NavigationBar {items.forEach { item ->
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
    ) {paddingValues ->
        NavHost(
            navController = navController,
            startDestination = MAIN_SCREEN,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(MAIN_SCREEN) {
                MainScreen(
                    navController,
                    mainActivityViewModel.mainScreenEvent.collectAsState(),
                    mainActivityViewModel::action,
                    onRefresh = {
                        mainActivityViewModel.action(
                            com.example.pmadvanced.presenter.ui.main.event.MainScreenAction.LoadConversations
                        )
                    },
                    profileViewModel = profileViewModel
                )
            }
            composable(MAP_SCREEN) {
                MapScreen()
            }

            composable(CHAT_SCREEN) {
                ChatScreen(
                    navController,
                    mainActivityViewModel.mainScreenEvent.collectAsState(),
                    mainActivityViewModel::action,
                    profileViewModel = profileViewModel
                )
            }

            composable(PROFILE_SCREEN) {
                ProfileScreen(
                    navController = navController,
                    profileViewModel = profileViewModel,
                    userId = null,
                    mainActivityViewModel::action,
                    mainActivityViewModel.mainScreenEvent.collectAsState(),
                )
            }

            composable(
                route = "$PROFILE_SCREEN/{userId}",
                arguments = listOf(navArgument("userId") { type = NavType.IntType })
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getInt("userId")
                ProfileScreen(
                    navController = navController,
                    profileViewModel = profileViewModel,
                    userId = userId,
                    mainActivityViewModel::action,
                    mainActivityViewModel.mainScreenEvent.collectAsState(),
                )
            }

            composable(SEARCH_SCREEN) {
                SearchScreen(
                    mainScreenEvent = mainActivityViewModel.mainScreenEvent.collectAsState(),
                    action = mainActivityViewModel::action,
                    navController = navController
                )
            }
            composable (KOMEK_SCREEN){backStackEntry ->
                val userId = backStackEntry.arguments?.getInt("userId")
                KomekScreen(userId)
            }
        }
    }
}

object MainActivityNavigationNames {
    const val MAIN_SCREEN = "MAIN_SCREEN"
    const val CHAT_SCREEN = "CHAT_SCREEN"
    const val PROFILE_SCREEN = "PROFILE_SCREEN"
    const val SEARCH_SCREEN = "SEARCH_SCREEN"
    const val MAP_SCREEN = "MAP_SCREEN"
    const val KOMEK_SCREEN = "KOMEK_SCREEN"
}