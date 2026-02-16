package com.example.pmadvanced.features.auth.screens

import android.R.attr.text
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.pmadvanced.R
import com.example.pmadvanced.features.auth.viewModels.AuthViewModel
import com.example.pmadvanced.navigation.Routes

@Composable
fun Register(navController: NavController) {
    val context = LocalContext.current
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val viewModel: AuthViewModel = viewModel()

    Column {
        Text(stringResource(R.string.email))
        TextField(
            value = email.value,
            onValueChange = { email.value = it },
            label = { Text(stringResource(R.string.email)) }
        )

        Text(stringResource(R.string.password))
        TextField(
            value = password.value,
            onValueChange = { password.value = it },
            label = { Text(stringResource(R.string.password)) }
        )
        Button(
            onClick = {
                viewModel.registerUser(
                    email = email.value,
                    password = password.value,
                    onSuccess = {
                        Toast.makeText(context, context.getString(R.string.regsucc), Toast.LENGTH_SHORT).show()
                        navController.navigate(Routes.LOGIN)
                    },
                    onError = { error ->
                        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        ) {
            Text(stringResource(R.string.register))
        }
    }
}

