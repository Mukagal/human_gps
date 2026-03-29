package com.example.pmadvanced.presenter.ui.onboarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.pmadvanced.presenter.ui.main.MainActivity

class OnboardingActivity : ComponentActivity() {

    val onboardingViewModel: OnboardingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("auth", Context.MODE_PRIVATE)
        val savedToken = prefs.getString("access_token", null)

        if (!savedToken.isNullOrEmpty()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContent {
            OnboardingNavigation(onboardingViewModel) { token, userId ->
                prefs.edit()
                    .putString("access_token", token)
                    .putInt("user_id", userId)
                    .apply()
            }
        }
    }
}