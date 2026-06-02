package com.laptrinhdidong.DoAn3.ui.components

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.laptrinhdidong.DoAn3.ui.theme.PrimaryPurple
import kotlinx.coroutines.delay

private val DEFAULT_LOCATION = LatLng(10.7629, 106.6604)

fun isRunningOnEmulator(): Boolean {
    return (
        Build.FINGERPRINT.contains("generic") ||
        Build.FINGERPRINT.contains("emulator") ||
        Build.MODEL.contains("Emulator") ||
        Build.MODEL.contains("Android SDK built for x86") ||
        Build.BRAND.contains("generic") ||
        Build.DEVICE.contains("generic") ||
        Build.HARDWARE.contains("goldfish") ||
        Build.HARDWARE.contains("ranchu") ||
        Build.PRODUCT.contains("sdk") ||
        Build.PRODUCT.contains("emulator") ||
        Build.MANUFACTURER.contains("Genymotion")
    )
}

@Composable
fun TaxiMapView(
    pickupLat: Double?,
    pickupLng: Double?,
    dropoffLat: Double?,
    dropoffLng: Double?,
    driverLat: Double?,
    driverLng: Double?,
    routePoints: List<Pair<Double, Double>>?,
    onMapClick: (LatLng) -> Unit,
    modifier: Modifier = Modifier,
    zoomLevel: Float = 14f,
    showPickup: Boolean = true,
    showDropoff: Boolean = true,
    showDriver: Boolean = true,
    waypoints: List<WaypointDisplayInfo>? = null
) {
    val isEmulator = remember { isRunningOnEmulator() }

    // Emulator: use beautiful simulated map (no Google Play Services needed)
    if (isEmulator) {
        SimulatedMapView(
            pickupLat = pickupLat,
            pickupLng = pickupLng,
            dropoffLat = dropoffLat,
            dropoffLng = dropoffLng,
            driverLat = driverLat,
            driverLng = driverLng,
            routePoints = routePoints,
            onMapClick = { simLatLng ->
                onMapClick(LatLng(simLatLng.latitude, simLatLng.longitude))
            },
            modifier = modifier,
            zoomLevel = zoomLevel
        )
        return
    }

    // Real device: use Google Maps
    val pickupLatLng = if (pickupLat != null && pickupLng != null) LatLng(pickupLat, pickupLng) else null
    val dropoffLatLng = if (dropoffLat != null && dropoffLng != null) LatLng(dropoffLat, dropoffLng) else null
    val driverLatLng = if (driverLat != null && driverLng != null) LatLng(driverLat, driverLng) else null

    val initialPosition = pickupLatLng ?: dropoffLatLng ?: driverLatLng ?: DEFAULT_LOCATION

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialPosition, zoomLevel)
    }

    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(5000)
        isLoading = false
    }

    LaunchedEffect(pickupLatLng, dropoffLatLng, driverLatLng) {
        val target = driverLatLng ?: dropoffLatLng ?: pickupLatLng ?: DEFAULT_LOCATION
        try {
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(target, zoomLevel))
            isLoading = false
        } catch (e: Exception) {
            isLoading = false
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = false, mapType = MapType.NORMAL),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false,
                compassEnabled = false,
                scrollGesturesEnabled = true,
                zoomGesturesEnabled = true
            ),
            onMapClick = onMapClick
        ) {
            if (showPickup && pickupLatLng != null) {
                Marker(
                    state = MarkerState(position = pickupLatLng),
                    title = "Điểm đón",
                    snippet = "Điểm đón khách",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                )
            }

            if (showDropoff && dropoffLatLng != null) {
                Marker(
                    state = MarkerState(position = dropoffLatLng),
                    title = "Điểm đến",
                    snippet = "Điểm đến của bạn",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                )
            }

            if (showDriver && driverLatLng != null) {
                Marker(
                    state = MarkerState(position = driverLatLng),
                    title = "Tài xế",
                    snippet = "Vị trí tài xế",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                )
            }

            routePoints?.let { points ->
                if (points.size >= 2) {
                    Polyline(
                        points = points.map { LatLng(it.first, it.second) },
                        color = Color(0xFF667eea),
                        width = 12f
                    )
                }
            }

            waypoints?.forEach { wp ->
                val hue = when (wp.type.lowercase()) {
                    "pickup" -> BitmapDescriptorFactory.HUE_GREEN
                    "dropoff" -> BitmapDescriptorFactory.HUE_RED
                    else -> BitmapDescriptorFactory.HUE_VIOLET
                }
                Marker(
                    state = MarkerState(position = LatLng(wp.lat, wp.lng)),
                    title = wp.name,
                    snippet = wp.address,
                    icon = BitmapDescriptorFactory.defaultMarker(hue)
                )
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF1A1A2E), Color(0xFF16213E))
                    )
                    ),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryPurple)
            }
        }
    }
}

data class WaypointDisplayInfo(
    val lat: Double,
    val lng: Double,
    val name: String,
    val address: String,
    val type: String = "stopover"
)
