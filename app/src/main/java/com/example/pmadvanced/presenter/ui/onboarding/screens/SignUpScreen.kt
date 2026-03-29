package com.example.pmadvanced.presenter.ui.onboarding.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.pmadvanced.R
import com.example.pmadvanced.data.model.UserModel
import com.example.pmadvanced.presenter.ui.onboarding.OnboardingEvents
import com.example.pmadvanced.presenter.ui.onboarding.OnboardingNavigationObject
import com.example.pmadvanced.ui.util.HeightSpacer
import com.example.pmadvanced.ui.util.LeadingIconTextField
import com.example.pmadvanced.ui.util.ThemeSolidButton
import com.example.pmadvanced.ui.util.WidthSpacer

@Composable
fun SignUpScreen(
    navController: NavHostController,
    action: (OnboardingEvents) -> Unit
) {
    var userModel by remember { mutableStateOf(UserModel()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Black)
    ) {
        Image(
            painter = painterResource(id = R.mipmap.globe_img),
            contentDescription = "Globe Image",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(fraction = 0.5f)
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .background(color = Color.Gray.copy(alpha = 0.0f)),
            verticalArrangement = Arrangement.Bottom
        ) {
            Column(
                modifier = Modifier,
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.Start
            ) {
                Image(
                    painter = painterResource(id = R.drawable.chat_logo),
                    contentDescription = "Maman-Tap",
                    modifier = Modifier
                        .width(150.dp)
                        .offset(x = -(20.dp))
                )
                HeightSpacer()
                Text(text = "Create Account", color = Color.White, fontSize = 24.sp)
                HeightSpacer()
                Text(text = "Sign up to start helping.", color = Color.White, fontSize = 12.sp)
                HeightSpacer(height = 40.dp)

                LeadingIconTextField(
                    label = "Username",
                    value = userModel.userName ?: "",
                    valueChange = { userModel = userModel.copy(userName = it) },
                    leadingIcon = R.drawable.person_icon
                )
                HeightSpacer(height = 10.dp)

                LeadingIconTextField(
                    label = "Email",
                    value = userModel.email ?: "",
                    valueChange = { userModel = userModel.copy(email = it) },
                    leadingIcon = R.drawable.person_icon
                )
                HeightSpacer(height = 10.dp)

                LeadingIconTextField(
                    label = "Password",
                    value = userModel.password ?: "",
                    valueChange = { userModel = userModel.copy(password = it) },
                    leadingIcon = R.drawable.finger_print_icon
                )

                HeightSpacer(height = 20.dp)

                Text(
                    text = "Already have an account? Log In",
                    color = Color.White,
                    fontSize = 16.sp,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navController.navigate(OnboardingNavigationObject.LOGIN_SCREEN)
                        }
                )

                HeightSpacer(height = 20.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    WidthSpacer(width = 20.dp)
                    ThemeSolidButton(
                        text = "Register",
                        modifier = Modifier.fillMaxWidth(fraction = 0.6f)
                    ) {
                        action(OnboardingEvents.SignUpClick(userModel) { status ->
                            if (status) navController.navigate(OnboardingNavigationObject.LOGIN_SCREEN)
                        })
                    }
                }
                HeightSpacer(height = 20.dp)
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SignUpScreenPreview() {
    val navController = rememberNavController()
}