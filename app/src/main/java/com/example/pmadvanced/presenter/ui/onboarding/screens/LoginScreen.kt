package com.example.pmadvanced.presenter.ui.onboarding.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.core.text.isDigitsOnly
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.pmadvanced.R
import com.example.pmadvanced.data.model.UserModel
import com.example.pmadvanced.presenter.ui.onboarding.OnboardingEvents
import com.example.pmadvanced.presenter.ui.onboarding.OnboardingNavigationObject
import com.example.pmadvanced.ui.util.HeightSpacer
import com.example.pmadvanced.ui.util.ThemeSolidButton
import androidx.compose.foundation.clickable
import android.content.Intent
import androidx.compose.foundation.layout.Box
import com.example.pmadvanced.presenter.ui.main.MainActivity
import com.example.pmadvanced.ui.util.LeadingIconTextField

@Composable
fun LoginScreen(navController: NavHostController, action: (OnboardingEvents) -> Unit) {

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
                .padding(20.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Column(
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.Start
            ) {
                Image(
                    painter = painterResource(id = R.drawable.chat_u_logo),
                    contentDescription = "Maman-Tap",
                    modifier = Modifier
                        .width(150.dp)
                        .offset(x = -(20.dp))
                )
                HeightSpacer()
                Text(text = "Ready to Help?", color = Color.White, fontSize = 24.sp)
                HeightSpacer(height = 60.dp)

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
                    text = "Create an account!",
                    color = Color.White,
                    fontSize = 16.sp,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navController.navigate(OnboardingNavigationObject.SIGNUP_SCREEN)
                        }
                )

                HeightSpacer(height = 20.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    ThemeSolidButton(
                        text = "Login", 
                        modifier = Modifier.fillMaxWidth(fraction = 0.6f)
                    ) {
                        action(OnboardingEvents.LoginClick(userModel) { status ->
                            if (status) {
                                navController.context.startActivity(
                                    Intent(navController.context, MainActivity::class.java)
                                )
                            }
                        })
                    }
                }
                HeightSpacer(height = 20.dp)
            }
        }
    }
}