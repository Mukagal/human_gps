package com.example.pmadvanced.presenter.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pmadvanced.presenter.ui.onboarding.screens.LoginScreen
import com.example.pmadvanced.presenter.ui.onboarding.screens.OnboardingScreen
import com.example.pmadvanced.presenter.ui.onboarding.screens.SignUpScreen
import kotlinx.coroutines.launch

object OnboardingNavigationObject {
    const val ONBOARDING_SCREEN = "ONBOARDING_SCREEN"
    const val LOGIN_SCREEN = "LOGIN_SCREEN"
    const val SIGNUP_SCREEN = "SIGNUP_SCREEN"
}

@Composable
fun OnboardingNavigation(
    onboardingViewModel: OnboardingViewModel,
    onLoginSuccess: (token: String, userId: Int) -> Unit
) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }
    val snackBarState by onboardingViewModel.snackBarState.collectAsState()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) }
    ) { paddingValues ->
        val it = paddingValues

        NavHost(
            navController = navController,
            startDestination = OnboardingNavigationObject.ONBOARDING_SCREEN
        ) {
            composable(OnboardingNavigationObject.ONBOARDING_SCREEN) {
                OnboardingScreen(navController)
            }
            composable(OnboardingNavigationObject.LOGIN_SCREEN) {
                LoginScreen(navController) { event ->
                    if (event is OnboardingEvents.LoginClick) {
                        onboardingViewModel.action(OnboardingEvents.LoginClick(event.userModel) { status ->
                            if (status) {
                                onLoginSuccess(
                                    onboardingViewModel.accessToken,
                                    onboardingViewModel.currentUserId
                                )
                            }
                            event.status(status)
                        })
                    } else {
                        onboardingViewModel.action(event)
                    }
                }
            }
            composable(OnboardingNavigationObject.SIGNUP_SCREEN) {
                SignUpScreen(navController, onboardingViewModel::action)
            }
        }

        LaunchedEffect(key1 = snackBarState.show) {
            scope.launch {
                if (snackBarState.show) {
                    snackBarHostState.showSnackbar(
                        message = snackBarState.message,
                        actionLabel = if (snackBarState.isError) "Error" else "Success",
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }

        if (onboardingViewModel.isLoading.collectAsState().value) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(200.dp),
                    strokeCap = StrokeCap.Round,
                    strokeWidth = 8.dp,
                    trackColor = Color.White
                )
            }
        }
    }
}