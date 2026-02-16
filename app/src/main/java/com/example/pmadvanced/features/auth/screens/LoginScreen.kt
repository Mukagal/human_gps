package com.example.pmadvanced.features.auth.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.pmadvanced.R
import com.example.pmadvanced.SessionManager
import com.example.pmadvanced.features.auth.viewModels.AuthViewModel
import com.example.pmadvanced.navigation.Routes


@Composable
fun LoginScreen(navController: NavController, session: SessionManager) {
    val context = LocalContext.current
    var email = remember { mutableStateOf("") }
    var password = remember { mutableStateOf("") }
    val viewModel: AuthViewModel = viewModel ()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        TextField(
            value = email.value,
            onValueChange = { email.value = it},
            label = { Text("Email") }
        )

        Spacer(Modifier.height(12.dp))

        TextField(
            value = password.value,
            onValueChange = { password.value = it },
            label = { Text("Password") }
        )

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                viewModel.registerUser(
                    email = email.value,
                    password = password.value,
                    onSuccess = {
                        Toast.makeText(context, context.getString(R.string.regsucc), Toast.LENGTH_SHORT).show()
                        session.saveLogin(email.value)
                        navController.navigate(Routes.HOME)
                    },
                    onError = { error ->
                        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        ) {
            Text(stringResource(R.string.login))
        }
        Row {
            Text("Already have an account? ")
            Button(onClick = {navController.navigate("register")}) { Text("Register") }
        }

    }
}