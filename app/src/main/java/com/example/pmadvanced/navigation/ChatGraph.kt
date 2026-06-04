package com.example.pmadvanced.navigation

import androidx.compose.runtime.State
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.pmadvanced.presenter.ui.main.event.MainScreenAction
import com.example.pmadvanced.presenter.ui.main.event.MainScreenEvent
import com.example.pmadvanced.presenter.ui.main.screen.ChatScreen
import com.example.pmadvanced.presenter.ui.main.screen.MainScreen
import com.example.pmadvanced.presenter.ui.main.screen.SearchScreen
import com.example.pmadvanced.presenter.ui.main.viewmodel.MainActivityViewModel
import com.example.pmadvanced.presenter.ui.main.viewmodel.ProfileViewModel

fun NavGraphBuilder.chatNavGraph(
    navController: NavHostController,
    mainScreenEvent: State<MainScreenEvent>,
    action: (MainScreenAction) -> Unit,
    mainActivityViewModel: MainActivityViewModel,
    profileViewModel: ProfileViewModel
) {
    navigation(
        route = MainRoutes.CHAT_GRAPH,
        startDestination = MainRoutes.MAIN_SCREEN
    ) {
        composable(MainRoutes.MAIN_SCREEN) {
            MainScreen(
                navController = navController,
                mainScreenEvent = mainScreenEvent,
                action = action,
                onRefresh = { action(MainScreenAction.LoadConversations) },
                profileViewModel = profileViewModel
            )
        }

        composable(MainRoutes.CHAT_SCREEN) {
            ChatScreen(
                navController = navController,
                mainScreenEvent = mainScreenEvent,
                action = action,
                profileViewModel = profileViewModel,
                mainActivityViewModel = mainActivityViewModel
            )
        }

        composable(MainRoutes.SEARCH_SCREEN) {
            SearchScreen(
                mainScreenEvent = mainScreenEvent,
                action = action,
                navController = navController
            )
        }
    }
}
