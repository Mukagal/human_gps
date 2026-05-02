package com.example.pmadvanced.presenter.ui.main

import android.content.Context
import com.example.pmadvanced.BASE_URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

fun formatMessageTime(timestamp: String): String {
    return timestamp.substringAfter("T").substring(0, 5)
}

fun formatDayHeader(timestamp: String): String {
    val date = timestamp.substringBefore("T")
    return date
}

suspend fun refreshAccessToken(context: Context): String? {
    val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    val refreshToken = prefs.getString("refresh_token", null) ?: return null

    return withContext(Dispatchers.IO) {
        try {
            val url = URL("$BASE_URL/refresh")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Authorization", "Bearer $refreshToken")

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val json = JSONObject(connection.inputStream.bufferedReader().readText())
                val newToken = json.getString("access_token")
                prefs.edit().putString("access_token", newToken).apply()
                newToken
            } else null
        } catch (e: Exception) { null }
    }
}