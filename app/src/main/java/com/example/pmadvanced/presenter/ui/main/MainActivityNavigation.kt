package com.example.pmadvanced.presenter.ui.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pmadvanced.presenter.ui.main.MainActivityNavigationNames.CHAT_SCREEN
import com.example.pmadvanced.presenter.ui.main.MainActivityNavigationNames.MAIN_SCREEN
import com.example.pmadvanced.presenter.ui.main.MainActivityNavigationNames.PROFILE_SCREEN
import com.example.pmadvanced.presenter.ui.main.MainActivityNavigationNames.SEARCH_SCREEN
import com.example.pmadvanced.presenter.ui.main.screen.ChatScreen
import com.example.pmadvanced.presenter.ui.main.screen.MainScreen
import com.example.pmadvanced.presenter.ui.main.screen.ProfileScreen
import com.example.pmadvanced.presenter.ui.main.screen.SearchScreen
import com.example.pmadvanced.presenter.ui.main.viewmodel.MainActivityViewModel

@Composable
fun MainActivityNavigation(mainActivityViewModel: MainActivityViewModel) {

    val navController = rememberNavController()

    NavHost(navController = navController,
        startDestination = MAIN_SCREEN)
    {

        composable(MAIN_SCREEN){
            MainScreen(
                navController,
                mainActivityViewModel.mainScreenEvent.collectAsState(),
                mainActivityViewModel::action
            )
        }

        composable(CHAT_SCREEN){
            ChatScreen(
                navController,
                mainActivityViewModel.mainScreenEvent.collectAsState(),
                mainActivityViewModel::action
            )
        }
        composable(PROFILE_SCREEN){
            ProfileScreen()
        }
        composable(SEARCH_SCREEN){
            SearchScreen()
        }

    }

}

object MainActivityNavigationNames {
    const val MAIN_SCREEN = "MAIN_SCREEN"
    const val CHAT_SCREEN = "CHAT_SCREEN"
    const val PROFILE_SCREEN = "PROFILE_SCREEN"
    const val SEARCH_SCREEN = "SEARCH_SCREEN"
}