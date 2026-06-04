package com.example.pmadvanced.data.local

import android.content.Context
import android.content.SharedPreferences

class AuthTokenStore(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    val accessToken: String
        get() = prefs.getString(KEY_ACCESS_TOKEN, "").orEmpty()

    val refreshToken: String
        get() = prefs.getString(KEY_REFRESH_TOKEN, "").orEmpty()

    val currentUserId: Int
        get() = prefs.getInt(KEY_USER_ID, 0)

    fun saveAccessToken(token: String) {
        prefs.edit().putString(KEY_ACCESS_TOKEN, token).apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREF_NAME = "auth"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_ID = "user_id"
    }
}
