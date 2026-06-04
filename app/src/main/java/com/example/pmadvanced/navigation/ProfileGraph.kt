package com.example.pmadvanced.navigation

import androidx.compose.runtime.State
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.pmadvanced.presenter.ui.main.event.MainScreenAction
import com.example.pmadvanced.presenter.ui.main.event.MainScreenEvent
import com.example.pmadvanced.presenter.ui.main.screen.ProfileScreen
import com.example.pmadvanced.presenter.ui.main.viewmodel.ProfileViewModel

fun NavGraphBuilder.profileNavGraph(
    navController: NavController,
    profileViewModel: ProfileViewModel,
    action: (MainScreenAction) -> Unit,
    mainScreenEvent: State<MainScreenEvent>
) {
    navigation(
        route = MainRoutes.PROFILE_GRAPH,
        startDestination = MainRoutes.PROFILE_SCREEN
    ) {
        composable(MainRoutes.PROFILE_SCREEN) {
            ProfileScreen(
                navController = navController,
                profileViewModel = profileViewModel,
                userId = null,
                action = action,
                mainScreenEvent = mainScreenEvent
            )
        }

        composable(
            route = MainRoutes.PROFILE_WITH_USER,
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            ProfileScreen(
                navController = navController,
                profileViewModel = profileViewModel,
                userId = backStackEntry.arguments?.getInt("userId"),
                action = action,
                mainScreenEvent = mainScreenEvent
            )
        }
    }
}
