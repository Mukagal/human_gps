package com.example.pmadvanced

import android.os.Bundle
import androidx.compose.runtime.*
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pmadvanced.features.auth.screens.LoginScreen
import com.example.pmadvanced.navigation.Routes
import com.example.pmadvanced.ui.theme.PmAdvancedTheme
import com.example.pmadvanced.features.main.screens.HomeScreen
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val session = SessionManager(this)

        setContent {
            PmAdvancedTheme {
                val startDestination = if (session.isLoggedIn()) Routes.HOME else Routes.LOGIN
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = startDestination
                ) {

                    composable(Routes.LOGIN) {
                        LoginScreen(navController, session)
                    }

                    composable(Routes.HOME) {
                        HomeScreen(navController, session)
                    }
                }

            }
        }
    }
}
