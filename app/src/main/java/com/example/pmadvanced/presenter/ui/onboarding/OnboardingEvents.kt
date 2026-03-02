package com.example.pmadvanced.presenter.ui.onboarding

import com.example.pmadvanced.data.model.UserModel

sealed interface OnboardingEvents {
    data class SignUpClick(val userModel: UserModel, val status: (Boolean) -> Unit) : OnboardingEvents
    data class LoginClick(val userModel: UserModel, val status: (Boolean) -> Unit) : OnboardingEvents
}