package com.example.pmadvanced.presenter.ui.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.pmadvanced.presenter.ui.FirebaseApp
import com.example.pmadvanced.presenter.ui.main.MainActivity
import com.google.firebase.auth.FirebaseAuth

class OnboardingActivity : ComponentActivity() {

    val onboardingViewModel: OnboardingViewModel by viewModels<OnboardingViewModel>()

    var currentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (currentUser != null) {
            this.startActivity(Intent(this, MainActivity::class.java).apply {
            })
        }
        setContent {
            OnboardingNavigation(onboardingViewModel)
        }

    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}