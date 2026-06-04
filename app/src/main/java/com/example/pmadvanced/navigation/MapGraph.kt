package com.example.pmadvanced.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.pmadvanced.presenter.ui.map.MapScreen

fun NavGraphBuilder.mapNavGraph() {
    navigation(
        route = MainRoutes.MAP_GRAPH,
        startDestination = MainRoutes.MAP_SCREEN
    ) {
        composable(MainRoutes.MAP_SCREEN) {
            MapScreen()
        }

        composable(
            route = MainRoutes.MAP_WITH_USER,
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            MapScreen(focusUserId = backStackEntry.arguments?.getInt("userId"))
        }
    }
}
