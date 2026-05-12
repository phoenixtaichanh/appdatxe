# Feature Plan #9: Google Maps Integration

---

## 1. Mô tả

Thay thế map placeholder bằng Google Maps SDK thực sự:
- Hiển thị bản đồ
- Marker cho pickup/dropoff
- Driver marker di chuyển real-time
- Route polyline

---

## 2. Trạng thái hiện tại

### Backend ❌ Chưa cần (chủ yếu là Android)
- API directions đã có logic Haversine
- Cần thêm endpoint Google Directions API

### Android ❌ Chưa implement
- Map placeholder với gradient + grid lines
- Driver markers là icons hard-coded

---

## 3. Implementation

### 3.1. Android - Google Maps SDK

**Thêm dependencies:**
```kotlin
// build.gradle.kts (app)
implementation("com.google.maps.android:maps-compose:4.3.0")
implementation("com.google.android.gms:play-services-maps:18.2.0")
implementation("com.google.android.gms:play-services-location:21.1.0")
```

**Thêm API key vào `local.properties`:**
```
MAPS_API_KEY=YOUR_GOOGLE_MAPS_API_KEY
```

**AndroidManifest.xml:**
```xml
<application>
    <meta-data
        android:name="com.google.android.geo.API_KEY"
        android:value="${MAPS_API_KEY}" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
</application>
```

### 3.2. Map Composable

```kotlin
@Composable
fun TaxiMapView(
    pickupLatLng: LatLng?,
    dropoffLatLng: LatLng?,
    driverLatLng: LatLng?,
    routePolyline: List<LatLng>?,
    onMapClick: (LatLng) -> Unit
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            pickupLatLng ?: LatLng(10.7629, 106.6604),
            14f
        )
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            isMyLocationEnabled = true,
            mapType = MapType.NORMAL
        ),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = true,
            myLocationButtonEnabled = true,
            compassEnabled = true
        ),
        onMapClick = onMapClick
    ) {
        // Pickup marker
        pickupLatLng?.let {
            Marker(
                state = MarkerState(position = it),
                title = "Điểm đón",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
            )
        }

        // Dropoff marker
        dropoffLatLng?.let {
            Marker(
                state = MarkerState(position = it),
                title = "Điểm đến",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
            )
        }

        // Driver marker
        driverLatLng?.let {
            Marker(
                state = MarkerState(position = it),
                title = "Tài xế",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
            )
        }

        // Route polyline
        routePolyline?.let { route ->
            Polyline(
                points = route,
                color = Color(0xFF667eea),
                width = 12f
            )
        }
    }
}
```

### 3.3. Integrate vào PassengerHomeScreen

```kotlin
@Composable
fun PassengerHomeScreen(...) {
    var driverLatLng by remember { mutableStateOf<LatLng?>(null) }

    // Update driver position from Socket
    LaunchedEffect(Unit) {
        SocketManager.onDriverLocation { lat, lng ->
            driverLatLng = LatLng(lat, lng)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Thay gradient background bằng map
        TaxiMapView(
            pickupLatLng = if (state.pickupLat != 0.0) LatLng(state.pickupLat, state.pickupLng) else null,
            dropoffLatLng = if (state.destLat != 0.0) LatLng(state.destLat, state.destLng) else null,
            driverLatLng = driverLatLng,
            routePolyline = routePoints,
            onMapClick = { latLng ->
                // Nếu chưa có pickup → set pickup
                // Nếu đã có pickup → set dropoff
            }
        )

        // Giữ nguyên UI overlay (search bar, bottom sheet trigger)
    }
}
```

---

## 4. Estimated time

**Setup Maps SDK: 30 phút**
**Map composable: 1 giờ**
**Integration: 1.5 giờ**
**Testing: 1 giờ**

**Tổng: ~4 giờ**
