package com.example.pmadvanced.presenter.ui.map

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import android.Manifest
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.*
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pmadvanced.presenter.ui.komek.KomekViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen() {
    val mapViewModel: MapViewModel = viewModel()
    val komekViewModel: KomekViewModel = viewModel()
    val komekUiState by komekViewModel.uiState.collectAsState()


    val context = LocalContext.current
    val myLocation by mapViewModel.myLocation.collectAsState()
    val nearbyUsers by mapViewModel.nearbyUsers.collectAsState()
    val isLoading by mapViewModel.isLoading.collectAsState()

    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    LaunchedEffect(Unit) {
        if (!locationPermission.status.isGranted) {
            locationPermission.launchPermissionRequest()
        }
    }

    LaunchedEffect(locationPermission.status) {
        if (locationPermission.status.isGranted) {
            try {
                val fusedClient = LocationServices.getFusedLocationProviderClient(context)
                fusedClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        mapViewModel.onLocationObtained(it.latitude, it.longitude)
                    }
                }
            } catch (e: SecurityException) {
                Log.e("MapScreen", "Location permission denied: ${e.message}")
            }
        }
    }

    val defaultLatLng = LatLng(43.2054548,76.667694)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(myLocation ?: defaultLatLng, 12f)
    }

    LaunchedEffect(myLocation) {
        myLocation?.let {
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(it, 14f))
            komekViewModel.loadNearbyRequests(it.latitude, it.longitude)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            !locationPermission.status.isGranted -> {
                Column(
                    modifier = Modifier.fillMaxSize().background(Color.Black),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Location permission required", color = Color.White, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { locationPermission.launchPermissionRequest() }) {
                        Text("Grant Permission")
                    }
                }
            }
            else -> {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(isMyLocationEnabled = true),
                    uiSettings = MapUiSettings(myLocationButtonEnabled = true)
                ) {
                    nearbyUsers.forEach { user ->
                        val pos = LatLng(user.latitude, user.longitude)
                        Marker(
                            state = MarkerState(position = pos),
                            title = user.username,
                            snippet = "${user.distanceKm} km away",
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                        )
                    }
                    komekUiState.nearbyRequests.forEach { nearby ->
                        val pos = LatLng(nearby.requesterLatitude, nearby.requesterLongitude)
                        Marker(
                            state = MarkerState(position = pos),
                            title = nearby.request.title,
                            snippet = "${nearby.requesterUsername} · ${nearby.distanceKm}km · ${nearby.request.category}",
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                        )
                    }
                }

                if (nearbyUsers.isNotEmpty()) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 16.dp),
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.7f)
                    ) {
                        Text(
                            text = "${nearbyUsers.size} users nearby",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }

                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.White
                    )
                }
            }
        }
    }
}