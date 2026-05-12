package com.laptrinhdidong.DoAn3.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

private val DEFAULT_LOCATION = LatLng(10.7629, 106.6604)

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
    zoomLevel: Float = 14f
) {
    val pickupLatLng = if (pickupLat != null && pickupLng != null) LatLng(pickupLat, pickupLng) else null
    val dropoffLatLng = if (dropoffLat != null && dropoffLng != null) LatLng(dropoffLat, dropoffLng) else null
    val driverLatLng = if (driverLat != null && driverLng != null) LatLng(driverLat, driverLng) else null

    val initialPosition = pickupLatLng ?: dropoffLatLng ?: driverLatLng ?: DEFAULT_LOCATION

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialPosition, zoomLevel)
    }

    LaunchedEffect(pickupLatLng, dropoffLatLng, driverLatLng) {
        val target = driverLatLng ?: dropoffLatLng ?: pickupLatLng ?: DEFAULT_LOCATION
        cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(target, zoomLevel))
    }

    val mapProperties = remember {
        MapProperties(
            isMyLocationEnabled = false,
            mapType = MapType.NORMAL
        )
    }

    val uiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = true,
            myLocationButtonEnabled = false,
            compassEnabled = true
        )
    }

    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = mapProperties,
        uiSettings = uiSettings,
        onMapClick = onMapClick
    ) {
        pickupLatLng?.let {
            Marker(
                state = MarkerState(position = it),
                title = "Diem don",
                snippet = "Diem don khach",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
            )
        }

        dropoffLatLng?.let {
            Marker(
                state = MarkerState(position = it),
                title = "Diem den",
                snippet = "Diem den cua ban",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
            )
        }

        driverLatLng?.let {
            Marker(
                state = MarkerState(position = it),
                title = "Tai xe",
                snippet = "Vi tri tai xe",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
            )
        }

        routePoints?.let { points ->
            if (points.size >= 2) {
                val polylinePoints = points.map { LatLng(it.first, it.second) }
                Polyline(
                    points = polylinePoints,
                    color = Color(0xFF667eea),
                    width = 12f
                )
            }
        }
    }
}
