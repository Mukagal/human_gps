package com.example.pmadvanced.presenter.ui.map

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pmadvanced.BASE_URL
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.io.OutputStreamWriter
import com.example.pmadvanced.data.model.KomekUiState
import com.example.pmadvanced.data.model.NearbyRequest

data class NearbyUser(
    val id: Int,
    val username: String,
    val profileImagePath: String?,
    val distanceKm: Double,
    val latitude: Double,
    val longitude: Double
)

class MapViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("auth", Context.MODE_PRIVATE)
    private val token = prefs.getString("access_token", "") ?: ""

    private val _myLocation = MutableStateFlow<LatLng?>(null)
    val myLocation: StateFlow<LatLng?> = _myLocation

    private val _nearbyUsers = MutableStateFlow<List<NearbyUser>>(emptyList())
    val nearbyUsers: StateFlow<List<NearbyUser>> = _nearbyUsers

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    private val _nearbyRequests = MutableStateFlow<List<NearbyRequest>>(emptyList())
    val nearbyRequests: StateFlow<List<NearbyRequest>> = _nearbyRequests

    fun onLocationObtained(lat: Double, lng: Double) {
        _myLocation.value = LatLng(lat, lng)
        updateLocationOnServer(lat, lng)
        loadNearbyUsers(lat, lng)
    }

    private fun updateLocationOnServer(lat: Double, lng: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = URL("$BASE_URL/users/me/location")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "PATCH"
                conn.setRequestProperty("Authorization", "Bearer $token")
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true
                val body = JSONObject().apply {
                    put("latitude", lat)
                    put("longitude", lng)
                }.toString()
                OutputStreamWriter(conn.outputStream).use { it.write(body) }
                conn.responseCode 
            } catch (e: Exception) {
                Log.e("MapVM", "updateLocation error", e)
            }
        }
    }

    private fun loadNearbyUsers(lat: Double, lng: Double, radiusKm: Double = 50.0) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = URL("$BASE_URL/users/nearby?latitude=$lat&longitude=$lng&radius_km=$radiusKm")
                val conn = url.openConnection() as HttpURLConnection
                conn.setRequestProperty("Authorization", "Bearer $token")

                if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                    val arr = JSONArray(conn.inputStream.bufferedReader().readText())
                    val users = mutableListOf<NearbyUser>()
                    for (i in 0 until arr.length()) {
                        val item = arr.getJSONObject(i)
                        val userId = item.getInt("id")
                        val profile = fetchUserProfile(userId)
                        if (profile != null) {
                            users.add(NearbyUser(
                                id = userId,
                                username = item.getString("username"),
                                profileImagePath = item.optString("profile_image_path").takeIf { it.isNotBlank() },
                                distanceKm = item.getDouble("distance_km"),
                                latitude = profile.first,
                                longitude = profile.second
                            ))
                        }
                    }
                    _nearbyUsers.value = users
                }
            } catch (e: Exception) {
                Log.e("MapVM", "loadNearbyUsers error", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun fetchUserProfile(userId: Int): Pair<Double, Double>? {
        return try {
            val url = URL("$BASE_URL/users/$userId/location")
            val conn = url.openConnection() as HttpURLConnection
            conn.setRequestProperty("Authorization", "Bearer $token")
            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val json = JSONObject(conn.inputStream.bufferedReader().readText())
                Pair(json.getDouble("latitude"), json.getDouble("longitude"))
            } else null
        } catch (e: Exception) { null }
    }
}