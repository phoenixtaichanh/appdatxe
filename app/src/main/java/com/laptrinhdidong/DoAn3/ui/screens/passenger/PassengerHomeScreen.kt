package com.laptrinhdidong.DoAn3.ui.screens.passenger

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.laptrinhdidong.DoAn3.data.local.SessionManager
import com.laptrinhdidong.DoAn3.data.remote.SocketManager
import com.laptrinhdidong.DoAn3.data.remote.dto.DriverDto
import com.laptrinhdidong.DoAn3.data.remote.dto.RideDto
import com.laptrinhdidong.DoAn3.data.remote.dto.VehicleType
import com.laptrinhdidong.DoAn3.data.repository.RideRepository
import com.laptrinhdidong.DoAn3.ui.components.*
import com.laptrinhdidong.DoAn3.ui.theme.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PricingInfo(
    val vehicleType: VehicleType = VehicleType.MOTORBIKE,
    val baseFare: Double = 10000.0,
    val distanceFare: Double = 0.0,
    val timeFare: Double = 0.0,
    val totalPrice: Double = 0.0,
    val distanceKm: Double = 0.0,
    val durationMin: Int = 0
)

data class PassengerHomeState(
    val isLoading: Boolean = false,
    val pickupLocation: String = "",
    val destLocation: String = "",
    val pickupLat: Double = 10.7629,
    val pickupLng: Double = 106.6604,
    val destLat: Double = 10.7769,
    val destLng: Double = 106.7000,
    val nearbyDrivers: List<DriverDto> = emptyList(),
    val selectedDriver: DriverDto? = null,
    val pricing: PricingInfo = PricingInfo(),
    val currentRide: RideDto? = null,
    val isRideActive: Boolean = false,
    val isSearchingDrivers: Boolean = false,
    val errorMessage: String? = null,
    val searchRadiusKm: Double = 5.0,
    val driverLat: Double? = null,
    val driverLng: Double? = null
)

@dagger.hilt.android.lifecycle.HiltViewModel
class PassengerHomeViewModel @javax.inject.Inject constructor(
    private val repository: RideRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PassengerHomeState())
    val state: StateFlow<PassengerHomeState> = _state.asStateFlow()

    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            SocketManager.driverLocationFlow.collect { update ->
                val ride = _state.value.currentRide
                if (ride != null && update.rideId == ride.id) {
                    _state.value = _state.value.copy(
                        driverLat = update.lat,
                        driverLng = update.lng
                    )
                }
            }
        }
        viewModelScope.launch {
            SocketManager.rideStatusFlow.collect { statusUpdate ->
                val ride = _state.value.currentRide
                if (ride != null && statusUpdate.rideId == ride.id) {
                    _state.value = _state.value.copy(
                        currentRide = ride.copy(status = statusUpdate.status),
                        isRideActive = statusUpdate.status != "completed" && statusUpdate.status != "cancelled"
                    )
                    if (statusUpdate.status == "completed" || statusUpdate.status == "cancelled") {
                        _state.value = _state.value.copy(driverLat = null, driverLng = null)
                    }
                }
            }
        }
    }

    fun updatePickupLocation(address: String, lat: Double, lng: Double) {
        _state.value = _state.value.copy(
            pickupLocation = address,
            pickupLat = lat,
            pickupLng = lng
        )
        calculateEstimate()
    }

    fun updateDestLocation(address: String, lat: Double, lng: Double) {
        _state.value = _state.value.copy(
            destLocation = address,
            destLat = lat,
            destLng = lng
        )
        calculateEstimate()
    }

    fun updateVehicleType(vehicleType: VehicleType) {
        _state.value = _state.value.copy(
            pricing = _state.value.pricing.copy(vehicleType = vehicleType)
        )
        calculateEstimate()
    }

    private fun calculateEstimate() {
        val pickup = _state.value
        val R = 6371.0
        val dLat = (pickup.destLat - pickup.pickupLat) * Math.PI / 180
        val dLng = (pickup.destLng - pickup.pickupLng) * Math.PI / 180
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(pickup.pickupLat * Math.PI / 180) * Math.cos(pickup.destLat * Math.PI / 180) *
                Math.sin(dLng / 2) * Math.sin(dLng / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        val distanceKm = R * c
        val durationMin = Math.round((distanceKm / 30) * 60).toInt()

        val vt = pickup.pricing.vehicleType
        val (baseFare, pricePerKm, pricePerMin) = when (vt) {
            VehicleType.MOTORBIKE -> Triple(10000.0, 3000.0, 100.0)
            VehicleType.CAR_4_SEATS -> Triple(12000.0, 5000.0, 200.0)
            VehicleType.CAR_7_SEATS -> Triple(15000.0, 7000.0, 300.0)
        }

        val distanceFare = distanceKm * pricePerKm
        val timeFare = durationMin * pricePerMin
        val totalPrice = baseFare + distanceFare + timeFare

        _state.value = _state.value.copy(
            pricing = PricingInfo(
                vehicleType = vt,
                baseFare = baseFare,
                distanceFare = Math.round(distanceFare).toDouble(),
                timeFare = Math.round(timeFare).toDouble(),
                totalPrice = Math.round(totalPrice).toDouble(),
                distanceKm = Math.round(distanceKm * 100).toDouble() / 100,
                durationMin = durationMin
            )
        )
    }

    fun startDriverSearch() {
        if (_state.value.isSearchingDrivers) return
        _state.value = _state.value.copy(isSearchingDrivers = true, nearbyDrivers = emptyList())
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            while (_state.value.isSearchingDrivers) {
                val result = repository.getNearbyDrivers(
                    _state.value.pickupLat,
                    _state.value.pickupLng
                )
                result.onSuccess { drivers ->
                    _state.value = _state.value.copy(nearbyDrivers = drivers)
                }
                delay(5000)
            }
        }
    }

    fun stopDriverSearch() {
        searchJob?.cancel()
        _state.value = _state.value.copy(isSearchingDrivers = false)
        val ride = _state.value.currentRide
        if (ride != null) {
            SocketManager.leaveRide(ride.id)
        }
    }

    fun searchNearbyDriversOnce() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val result = repository.getNearbyDrivers(
                _state.value.pickupLat,
                _state.value.pickupLng
            )
            result.onSuccess { drivers ->
                _state.value = _state.value.copy(isLoading = false, nearbyDrivers = drivers)
            }.onFailure {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Khong tim thay tai xe gan day"
                )
            }
        }
    }

    fun selectDriver(driver: DriverDto) {
        _state.value = _state.value.copy(selectedDriver = driver)
    }

    fun requestRide() {
        val st = _state.value
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, isSearchingDrivers = false)
            searchJob?.cancel()
            val result = repository.requestRide(
                pickupLat = st.pickupLat,
                pickupLng = st.pickupLng,
                pickupAddress = st.pickupLocation,
                destLat = st.destLat,
                destLng = st.destLng,
                destAddress = st.destLocation,
                vehicleType = st.pricing.vehicleType.key
            )
            result.onSuccess { ride ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    currentRide = ride,
                    isRideActive = true
                )
                SocketManager.joinRide(ride.id)
                SocketManager.requestDriverLocation(ride.id)
            }.onFailure {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Khong the dat xe. Vui long thu lai."
                )
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }

    fun refreshRideStatus() {
        val ride = _state.value.currentRide ?: return
        viewModelScope.launch {
            val result = repository.getRide(ride.id)
            result.onSuccess { updatedRide ->
                _state.value = _state.value.copy(
                    currentRide = updatedRide,
                    isRideActive = updatedRide.status != "completed" && updatedRide.status != "cancelled"
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        searchJob?.cancel()
        _state.value.currentRide?.let { SocketManager.leaveRide(it.id) }
    }
}

@Composable
fun PassengerHomeScreen(
    onNavigateToRideDetail: (Int) -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToAISchedule: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToAIChat: () -> Unit,
    onLogout: () -> Unit,
    sessionManager: SessionManager,
    viewModel: PassengerHomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showBookingSheet by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopDriverSearch()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        TaxiMapView(
            pickupLat = if (state.pickupLocation.isNotEmpty()) state.pickupLat else null,
            pickupLng = if (state.pickupLocation.isNotEmpty()) state.pickupLng else null,
            dropoffLat = if (state.destLocation.isNotEmpty()) state.destLat else null,
            dropoffLng = if (state.destLocation.isNotEmpty()) state.destLng else null,
            driverLat = state.driverLat,
            driverLng = state.driverLng,
            routePoints = null,
            onMapClick = { latLng ->
                if (state.pickupLocation.isEmpty()) {
                    val addr = "Diem don (${String.format("%.4f", latLng.latitude)}, ${String.format("%.4f", latLng.longitude)})"
                    viewModel.updatePickupLocation(addr, latLng.latitude, latLng.longitude)
                } else if (state.destLocation.isEmpty()) {
                    val addr = "Diem den (${String.format("%.4f", latLng.latitude)}, ${String.format("%.4f", latLng.longitude)})"
                    viewModel.updateDestLocation(addr, latLng.latitude, latLng.longitude)
                }
            }
        )

        // --- Route info overlay on map (always visible when booking sheet is shown) ---
        if (showBookingSheet && (state.pickupLocation.isNotEmpty() || state.destLocation.isNotEmpty())) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 185.dp)
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard.copy(alpha = 0.95f)),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Pickup
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(12.dp).clip(CircleShape)
                                .background(AccentGreen)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Diem don", color = TextSecondary, fontSize = 10.sp)
                            Text(
                                text = state.pickupLocation.ifEmpty { "Chua chon" },
                                color = TextPrimary, fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1
                            )
                        }
                    }
                    // Connector line
                    if (state.destLocation.isNotEmpty()) {
                        Row {
                            Box(
                                modifier = Modifier
                                    .width(12.dp)
                                    .height(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(modifier = Modifier.width(2.dp).height(16.dp).background(TextSecondary.copy(alpha = 0.4f)))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(modifier = Modifier.weight(1f)) {
                                Box(modifier = Modifier.height(1.dp).fillMaxWidth().background(TextSecondary.copy(alpha = 0.2f)))
                            }
                        }
                        // Dropoff
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(12.dp).clip(CircleShape)
                                    .background(AccentRed)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Diem den", color = TextSecondary, fontSize = 10.sp)
                                Text(
                                    text = state.destLocation.ifEmpty { "Chua chon" },
                                    color = TextPrimary, fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        state.nearbyDrivers.take(3).forEachIndexed { index, driver ->
            val offsets = listOf(Pair(-80f, 100f), Pair(80f, 150f), Pair(0f, 200f))
            val (xOff, yOff) = offsets.getOrElse(index) { Pair(0f, 250f) }
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = xOff.dp, y = yOff.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = if (driver.carModel?.contains("may", ignoreCase = true) == true)
                            Icons.Default.TwoWheeler else Icons.Default.LocalTaxi,
                        contentDescription = null,
                        tint = AccentYellow,
                        modifier = Modifier.size(32.dp)
                    )
                    if (driver.distanceKm != null) {
                        Text(
                            text = String.format("%.1fkm", driver.distanceKm),
                            color = Color.White,
                            fontSize = 10.sp,
                            modifier = Modifier
                                .background(DarkCard.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        if (state.isSearchingDrivers) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 80.dp)
                    .background(DarkCard.copy(alpha = 0.9f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = AccentYellow,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Dang tim tai xe...",
                        color = TextPrimary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${state.nearbyDrivers.size} gan day",
                        color = AccentGreen,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // ====== BEAUTIFUL TOP ACTION BAR ======
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard.copy(alpha = 0.95f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Top row: greeting + profile
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(PrimaryPurple, PrimaryPink)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = sessionManager.userName?.firstOrNull()?.uppercase() ?: "U",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Xin chào!",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                            Text(
                                text = sessionManager.userName ?: "Hành khách",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    IconButton(
                        onClick = onNavigateToProfile,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(PrimaryPurple.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = PrimaryPurple,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                HorizontalDivider(
                    color = Color.White.copy(alpha = 0.06f),
                    thickness = 1.dp
                )

                // Main action bar - scrollable row of quick actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ActionBarButton(
                        icon = Icons.Default.DirectionsCar,
                        label = "Đặt xe",
                        iconColor = PrimaryPurple,
                        bgColor = PrimaryPurple.copy(alpha = 0.15f),
                        onClick = { showBookingSheet = true }
                    )
                    ActionBarButton(
                        icon = Icons.Default.Schedule,
                        label = "Lịch trình",
                        iconColor = AccentBlue,
                        bgColor = AccentBlue.copy(alpha = 0.15f),
                        onClick = onNavigateToAISchedule
                    )
                    ActionBarButton(
                        icon = Icons.Default.AutoAwesome,
                        label = "AI Chat",
                        iconColor = PrimaryPink,
                        bgColor = PrimaryPink.copy(alpha = 0.15f),
                        onClick = onNavigateToAIChat
                    )
                    ActionBarButton(
                        icon = Icons.Default.History,
                        label = "Lịch sử",
                        iconColor = AccentOrange,
                        bgColor = AccentOrange.copy(alpha = 0.15f),
                        onClick = onNavigateToHistory
                    )
                    ActionBarButton(
                        icon = Icons.AutoMirrored.Filled.Logout,
                        label = "Đăng xuất",
                        iconColor = AccentRed,
                        bgColor = AccentRed.copy(alpha = 0.15f),
                        onClick = onLogout
                    )
                }
            }
        }

        // Quick action shortcut: Dat xe ngay
        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 185.dp)
                .clickable { showBookingSheet = true },
            shape = RoundedCornerShape(50.dp),
            colors = CardDefaults.cardColors(containerColor = PrimaryPurple)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Đặt xe ngay",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Bottom: booking shortcut + destination card
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
        ) {
            // Booking shortcut card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showBookingSheet = true },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(PrimaryPurple.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = PrimaryPurple,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Bạn muốn đi đâu?",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Nhập điểm đến của bạn",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(AccentGreen.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = AccentGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        if (showBookingSheet) {
            BookingBottomSheet(
                state = state,
                onPickupChange = { address, lat, lng ->
                    viewModel.updatePickupLocation(address, lat, lng)
                },
                onDestChange = { address, lat, lng ->
                    viewModel.updateDestLocation(address, lat, lng)
                },
                onVehicleTypeChange = { viewModel.updateVehicleType(it) },
                onStartSearch = { viewModel.startDriverSearch() },
                onStopSearch = { viewModel.stopDriverSearch() },
                onSearchDriversOnce = { viewModel.searchNearbyDriversOnce() },
                onSelectDriver = { viewModel.selectDriver(it) },
                onRequestRide = { viewModel.requestRide() },
                onDismiss = {
                    showBookingSheet = false
                    if (state.isRideActive) {
                        state.currentRide?.let { onNavigateToRideDetail(it.id) }
                    }
                },
                onClose = {
                    showBookingSheet = false
                }
            )
        }

        if (state.isLoading) {
            LoadingOverlay()
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp)
        )
    }
}

@Composable
private fun QuickActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    gradient: List<Color>
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(gradient)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun ActionBarButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    iconColor: Color,
    bgColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookingBottomSheet(
    state: PassengerHomeState,
    onPickupChange: (String, Double, Double) -> Unit,
    onDestChange: (String, Double, Double) -> Unit,
    onVehicleTypeChange: (VehicleType) -> Unit,
    onStartSearch: () -> Unit,
    onStopSearch: () -> Unit,
    onSearchDriversOnce: () -> Unit,
    onSelectDriver: (DriverDto) -> Unit,
    onRequestRide: () -> Unit,
    onDismiss: () -> Unit,
    onClose: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var pickupText by remember { mutableStateOf("") }
    var destText by remember { mutableStateOf("") }

    val demoLocations = listOf(
        Triple("Truong DH Bach Khoa", 10.7629, 106.6604),
        Triple("Vinmart Dien Bien Phu", 10.7769, 106.7000),
        Triple("Ben Thanh Market", 10.7729, 106.6980),
        Triple("Landmark 81", 10.7952, 106.7218),
        Triple("Saigon Zoo", 10.7870, 106.7055)
    )

    // Pre-fill pickup with default location so marker is visible on map immediately
    LaunchedEffect(Unit) {
        val defaultPickup = demoLocations.first()
        pickupText = defaultPickup.first
        onPickupChange(defaultPickup.first, defaultPickup.second, defaultPickup.third)
    }

    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = sheetState,
        containerColor = DarkSurface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.3f))
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Dat xe",
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = pickupText,
                onValueChange = { pickupText = it },
                label = { Text("Diem don", color = TextSecondary) },
                leadingIcon = {
                    Icon(Icons.Default.MyLocation, null, tint = AccentGreen)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = DarkCard,
                    unfocusedContainerColor = DarkCard,
                    cursorColor = PrimaryPurple,
                    focusedBorderColor = PrimaryPurple,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = destText,
                onValueChange = { destText = it },
                label = { Text("Diem den", color = TextSecondary) },
                leadingIcon = {
                    Icon(Icons.Default.LocationOn, null, tint = AccentRed)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = DarkCard,
                    unfocusedContainerColor = DarkCard,
                    cursorColor = PrimaryPurple,
                    focusedBorderColor = PrimaryPurple,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Loai phuong tien",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VehicleType.entries.forEach { vt ->
                    val isSelected = state.pricing.vehicleType == vt
                    val (icon, label) = when (vt) {
                        VehicleType.MOTORBIKE -> Pair(Icons.Default.TwoWheeler, "Xe may")
                        VehicleType.CAR_4_SEATS -> Pair(Icons.Default.DirectionsCar, "O to 4 cho")
                        VehicleType.CAR_7_SEATS -> Pair(Icons.Default.AirportShuttle, "O to 7 cho")
                    }
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onVehicleTypeChange(vt) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) PrimaryPurple.copy(alpha = 0.2f) else DarkCard,
                        border = if (isSelected) BorderStroke(2.dp, PrimaryPurple) else null
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected) PrimaryPurple else TextSecondary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = label,
                                color = if (isSelected) PrimaryPurple else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Dia diem pho bien",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                demoLocations.take(3).forEach { (name, lat, lng) ->
                    Surface(
                        modifier = Modifier.clickable {
                            if (pickupText.isEmpty()) {
                                pickupText = name
                                onPickupChange(name, lat, lng)
                            } else if (destText.isEmpty()) {
                                destText = name
                                onDestChange(name, lat, lng)
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = DarkCard
                    ) {
                        Text(
                            text = name.take(14),
                            color = TextSecondary,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (!state.isSearchingDrivers && state.nearbyDrivers.isEmpty()) {
                GradientButton(
                    text = "Tim tai xe",
                    onClick = {
                        if (pickupText.isNotEmpty() && destText.isNotEmpty()) {
                            onPickupChange(pickupText, 10.7629, 106.6604)
                            onDestChange(destText, 10.7769, 106.7000)
                            onStartSearch()
                        }
                    },
                    enabled = pickupText.isNotEmpty() && destText.isNotEmpty()
                )
            } else if (state.isSearchingDrivers) {
                OutlinedButton(
                    onClick = onStopSearch,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentRed)
                ) {
                    Icon(Icons.Default.Close, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Dung tim")
                }
            }

            if (state.nearbyDrivers.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tai xe gan day (${state.nearbyDrivers.size})",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (state.isSearchingDrivers) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = AccentYellow,
                            strokeWidth = 2.dp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                state.nearbyDrivers.take(3).forEach { driver ->
                    val isSelected = state.selectedDriver?.id == driver.id
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectDriver(driver) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) PrimaryPurple.copy(alpha = 0.15f) else DarkCard,
                        border = if (isSelected) BorderStroke(1.5.dp, PrimaryPurple) else null
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryPurple.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (driver.carModel?.contains("may", ignoreCase = true) == true)
                                        Icons.Default.TwoWheeler else Icons.Default.DirectionsCar,
                                    contentDescription = null,
                                    tint = PrimaryPurple,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = driver.name ?: "Tai xe",
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${driver.carModel ?: ""} ${driver.carColor ?: ""}".trim(),
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = driver.licensePlate ?: "",
                                    color = AccentYellow,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = AccentYellow,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = String.format("%.1f", driver.rating),
                                        color = AccentYellow,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                if (driver.distanceKm != null) {
                                    Text(
                                        text = String.format("%.1fkm", driver.distanceKm),
                                        color = AccentGreen,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = state.pricing.vehicleType.label,
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "${String.format("%.0f", state.pricing.totalPrice)}đ",
                                    color = PrimaryPurple,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${state.pricing.distanceKm} km",
                                    color = TextPrimary,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "~${state.pricing.durationMin} phut",
                                    color = TextSecondary,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Cuoc co ban", color = TextSecondary, fontSize = 12.sp)
                            Text(
                                "${String.format("%.0f", state.pricing.baseFare)}đ",
                                color = TextPrimary,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Phí quãng đường", color = TextSecondary, fontSize = 12.sp)
                            Text(
                                "${String.format("%.0f", state.pricing.distanceFare)}đ",
                                color = TextPrimary,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Phí thời gian", color = TextSecondary, fontSize = 12.sp)
                            Text(
                                "${String.format("%.0f", state.pricing.timeFare)}đ",
                                color = TextPrimary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                GradientButton(
                    text = "Dat xe ngay",
                    onClick = onRequestRide,
                    enabled = state.selectedDriver != null
                )
            }
        }
    }
}
