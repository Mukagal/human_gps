package com.example.pmadvanced.presenter.ui.komek

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pmadvanced.BASE_URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import com.example.pmadvanced.data.model.KomekUiState
import com.example.pmadvanced.data.model.HelpApplication
import com.example.pmadvanced.data.model.HelpRequest
import com.example.pmadvanced.data.model.NearbyRequest

class KomekViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("auth", Context.MODE_PRIVATE)
    private val token = prefs.getString("access_token", "") ?: ""

    private val _uiState = MutableStateFlow(KomekUiState())
    val uiState: StateFlow<KomekUiState> = _uiState

    init { loadOpenRequests() }

    fun loadOpenRequests() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val url = URL("$BASE_URL/requests")
                val conn = url.openConnection() as HttpURLConnection
                conn.setRequestProperty("Authorization", "Bearer $token")
                if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                    val arr = JSONArray(conn.inputStream.bufferedReader().readText())
                    _uiState.value = _uiState.value.copy(openRequests = parseRequests(arr))
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun loadMyRequests() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = URL("$BASE_URL/requests/me")
                val conn = url.openConnection() as HttpURLConnection
                conn.setRequestProperty("Authorization", "Bearer $token")
                if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                    val arr = JSONArray(conn.inputStream.bufferedReader().readText())
                    _uiState.value = _uiState.value.copy(myRequests = parseRequests(arr))
                }
            } catch (e: Exception) { Log.e("KomekVM", "loadMyRequests", e) }
        }
    }

    fun loadNearbyRequests(lat: Double, lng: Double, radiusKm: Double = 20.0) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = URL("$BASE_URL/requests/nearby?latitude=$lat&longitude=$lng&radius_km=$radiusKm")
                val conn = url.openConnection() as HttpURLConnection
                conn.setRequestProperty("Authorization", "Bearer $token")
                if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                    val arr = JSONArray(conn.inputStream.bufferedReader().readText())
                    val list = mutableListOf<NearbyRequest>()
                    for (i in 0 until arr.length()) {
                        val item = arr.getJSONObject(i)
                        val req = parseRequest(item.getJSONObject("request"))
                        list.add(NearbyRequest(
                            request = req,
                            distanceKm = item.getDouble("distance_km"),
                            requesterLatitude = item.getDouble("requester_latitude"),
                            requesterLongitude = item.getDouble("requester_longitude"),
                            requesterUsername = item.getString("requester_username")
                        ))
                    }
                    _uiState.value = _uiState.value.copy(nearbyRequests = list)
                }
            } catch (e: Exception) { Log.e("KomekVM", "loadNearbyRequests", e) }
        }
    }

    fun createRequest(title: String, description: String, category: String, expiresInDays: Int = 3) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val url = URL("$BASE_URL/requests")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Authorization", "Bearer $token")
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true
                val body = JSONObject().apply {
                    put("title", title)
                    put("description", description)
                    put("category", category)
                    put("expires_in_days", expiresInDays)
                }.toString()
                OutputStreamWriter(conn.outputStream).use { it.write(body) }
                if (conn.responseCode == HttpURLConnection.HTTP_CREATED) {
                    _uiState.value = _uiState.value.copy(successMessage = "Request created!")
                    loadOpenRequests()
                    loadMyRequests()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun applyToRequest(requestId: Int, message: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = URL("$BASE_URL/requests/$requestId/apply")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Authorization", "Bearer $token")
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true
                val body = JSONObject().apply { put("message", message ?: "") }.toString()
                OutputStreamWriter(conn.outputStream).use { it.write(body) }
                if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                    _uiState.value = _uiState.value.copy(successMessage = "Applied successfully!")
                    loadOpenRequests()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun cancelRequest(requestId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = URL("$BASE_URL/requests/$requestId/cancel")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "DELETE"
                conn.setRequestProperty("Authorization", "Bearer $token")
                conn.responseCode
                loadMyRequests()
            } catch (e: Exception) { Log.e("KomekVM", "cancelRequest", e) }
        }
    }

    fun acceptApplication(requestId: Int, applicationId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = URL("$BASE_URL/requests/$requestId/applications/$applicationId/accept")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Authorization", "Bearer $token")
                conn.responseCode
                loadMyRequests()
            } catch (e: Exception) { Log.e("KomekVM", "acceptApplication", e) }
        }
    }

    fun rejectApplication(requestId: Int, applicationId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = URL("$BASE_URL/requests/$requestId/applications/$applicationId/reject")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Authorization", "Bearer $token")
                conn.responseCode
                loadMyRequests()
            } catch (e: Exception) { Log.e("KomekVM", "rejectApplication", e) }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }

    private fun parseRequests(arr: JSONArray): List<HelpRequest> {
        val list = mutableListOf<HelpRequest>()
        for (i in 0 until arr.length()) list.add(parseRequest(arr.getJSONObject(i)))
        return list
    }

    private fun parseRequest(obj: JSONObject): HelpRequest {
        val appsArr = obj.optJSONArray("applications") ?: JSONArray()
        val apps = mutableListOf<HelpApplication>()
        for (i in 0 until appsArr.length()) {
            val a = appsArr.getJSONObject(i)
            apps.add(HelpApplication(
                id = a.getInt("id"),
                applicantId = a.getInt("applicant_id"),
                message = a.optString("message").takeIf { it.isNotBlank() },
                status = a.getString("status")
            ))
        }
        return HelpRequest(
            id = obj.getInt("id"),
            requesterId = obj.getInt("requester_id"),
            title = obj.getString("title"),
            description = obj.getString("description"),
            category = obj.getString("category"),
            status = obj.getString("status"),
            expiresAt = obj.optString("expires_at").takeIf { it.isNotBlank() },
            applications = apps
        )
    }
}