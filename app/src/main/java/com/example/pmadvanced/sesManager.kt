package com.example.pmadvanced

import android.content.Context

class SessionManager(context: Context) {

    private val prefs =
        context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    fun saveLogin(email: String) {
        prefs.edit().putString("user_email", email).apply()
    }

    fun isLoggedIn(): Boolean {
        return prefs.contains("user_email")
    }

    fun logout() {
        prefs.edit().clear().apply()
    }

    fun getEmail(): String? {
        return prefs.getString("user_email", null)
    }
}
