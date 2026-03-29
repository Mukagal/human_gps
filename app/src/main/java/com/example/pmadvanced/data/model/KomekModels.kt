package com.example.pmadvanced.data.model

data class HelpRequest(
    val id: Int,
    val requesterId: Int,
    val title: String,
    val description: String,
    val category: String,
    val status: String,
    val expiresAt: String?,
    val applications: List<HelpApplication> = emptyList()
)

data class HelpApplication(
    val id: Int,
    val applicantId: Int,
    val message: String?,
    val status: String
)

data class NearbyRequest(
    val request: HelpRequest,
    val distanceKm: Double,
    val requesterLatitude: Double,
    val requesterLongitude: Double,
    val requesterUsername: String
)

data class KomekUiState(
    val openRequests: List<HelpRequest> = emptyList(),
    val myRequests: List<HelpRequest> = emptyList(),
    val myApplications: List<HelpApplication> = emptyList(),
    val nearbyRequests: List<NearbyRequest> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

