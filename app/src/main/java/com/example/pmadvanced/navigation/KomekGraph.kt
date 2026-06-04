package com.example.pmadvanced.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.pmadvanced.presenter.ui.komek.KomekScreen

fun NavGraphBuilder.komekNavGraph(
    navController: NavHostController,
    currentUserId: Int
) {
    navigation(
        route = MainRoutes.KOMEK_GRAPH,
        startDestination = MainRoutes.KOMEK_SCREEN
    ) {
        composable(MainRoutes.KOMEK_SCREEN) {
            KomekScreen(currentUserId = currentUserId, navController = navController)
        }
    }
}
