package com.laptrinhdidong.DoAn3.ui.screens.driver

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laptrinhdidong.DoAn3.data.remote.SocketManager
import com.laptrinhdidong.DoAn3.data.remote.dto.BatchDto
import com.laptrinhdidong.DoAn3.data.remote.dto.DriverDto
import com.laptrinhdidong.DoAn3.data.remote.dto.EarningsDto
import com.laptrinhdidong.DoAn3.data.remote.dto.RideDto
import com.laptrinhdidong.DoAn3.data.remote.NewRideNotification
import com.laptrinhdidong.DoAn3.data.repository.DriverRepository
import com.laptrinhdidong.DoAn3.ui.components.*
import com.laptrinhdidong.DoAn3.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ========== STATE ==========
data class DriverHomeState(
    val isLoading: Boolean = false,
    val isOnline: Boolean = false,
    val driver: DriverDto? = null,
    val availableRides: List<RideDto> = emptyList(),
    val currentRide: RideDto? = null,
    val earnings: EarningsDto? = null,
    val batches: List<BatchDto> = emptyList(),
    val errorMessage: String? = null
)

// ========== VIEWMODEL ==========
@dagger.hilt.android.lifecycle.HiltViewModel
class DriverHomeViewModel @javax.inject.Inject constructor(
    private val repository: DriverRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DriverHomeState())
    val state: StateFlow<DriverHomeState> = _state.asStateFlow()

    init {
        loadDriverProfile()
    }

    private fun loadDriverProfile() {
        viewModelScope.launch {
            repository.getDriverProfile().onSuccess { driver ->
                _state.value = _state.value.copy(
                    driver = driver,
                    isOnline = driver.isAvailable
                )
            }
        }
    }

    fun toggleOnlineStatus() {
        viewModelScope.launch {
            val newStatus = !_state.value.isOnline
            _state.value = _state.value.copy(isLoading = true)

            repository.updateDriverStatus(
                isAvailable = newStatus,
                latitude = 10.7629,
                longitude = 106.6604
            ).onSuccess {
                _state.value = _state.value.copy(
                    isLoading = false,
                    isOnline = newStatus
                )
                if (newStatus) {
                    loadAvailableRides()
                    loadEarnings()
                    // Start listening for new ride notifications
                    startNewRideListener()
                }
            }.onFailure {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Không thể cập nhật trạng thái"
                )
            }
        }
    }

    private fun startNewRideListener() {
        viewModelScope.launch {
            SocketManager.newRideFlow.collect { notification ->
                // Convert notification to RideDto for display
                val ride = RideDto(
                    id = notification.rideId,
                    pickupLat = notification.pickupLat,
                    pickupLng = notification.pickupLng,
                    pickupAddress = notification.pickupAddress,
                    destLat = 0.0,
                    destLng = 0.0,
                    destAddress = notification.destAddress,
                    status = "pending",
                    passengerId = 0,
                    driverId = null,
                    passengerName = notification.passengerName,
                    driverName = null,
                    vehicleType = notification.vehicleType,
                    price = notification.price,
                    distanceKm = notification.distanceKm,
                    durationMin = notification.durationMin,
                    createdAt = java.time.Instant.ofEpochMilli(notification.timestamp).toString(),
                    startedAt = null,
                    completedAt = null,
                    driverRating = null,
                    passengerRating = null
                )
                // Add to available rides if not already present
                if (_state.value.availableRides.none { it.id == ride.id }) {
                    _state.value = _state.value.copy(
                        availableRides = listOf(ride) + _state.value.availableRides
                    )
                }
            }
        }
    }

    fun loadAvailableRides() {
        viewModelScope.launch {
            repository.getAvailableRides().onSuccess { rides ->
                _state.value = _state.value.copy(availableRides = rides)
            }
        }
    }

    fun loadEarnings() {
        viewModelScope.launch {
            val today = java.time.LocalDate.now().toString()
            repository.getEarnings(today, today).onSuccess { earnings ->
                _state.value = _state.value.copy(earnings = earnings)
            }
        }
    }

    fun acceptRide(rideId: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            repository.acceptRide(rideId).onSuccess { ride ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    currentRide = ride,
                    availableRides = _state.value.availableRides.filter { it.id != rideId }
                )
            }.onFailure {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Không thể nhận chuyến"
                )
            }
        }
    }

    fun rejectRide(rideId: Int) {
        viewModelScope.launch {
            repository.rejectRide(rideId).onSuccess {
                _state.value = _state.value.copy(
                    availableRides = _state.value.availableRides.filter { it.id != rideId }
                )
            }
        }
    }

    fun updateRideStatus(rideId: Int, status: String) {
        SocketManager.emitRideStatus(rideId, status)
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            repository.updateRideStatus(rideId, status).onSuccess { ride ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    currentRide = if (status == "completed" || status == "cancelled") null else ride
                )
                if (status == "completed") {
                    loadEarnings()
                }
            }.onFailure {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    fun loadBatches() {
        viewModelScope.launch {
            repository.getAvailableBatches().onSuccess { batches ->
                _state.value = _state.value.copy(batches = batches)
            }
        }
    }

    fun acceptBatch(batchId: Int) {
        viewModelScope.launch {
            repository.acceptBatch(batchId).onSuccess {
                _state.value = _state.value.copy(
                    batches = _state.value.batches.filter { it.id != batchId }
                )
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }
}

// ========== SCREEN ==========
@Composable
fun DriverHomeScreen(
    onNavigateToRideDetail: (Int) -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToEarnings: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToBatch: () -> Unit,
    onNavigateToAISchedule: () -> Unit,
    onNavigateToSupport: () -> Unit = {},
    onLogout: () -> Unit,
    viewModel: DriverHomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(state.isOnline) {
        if (state.isOnline) {
            viewModel.loadAvailableRides()
            viewModel.loadEarnings()
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
        ) {
            // Top bar - enhanced with action bar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard.copy(alpha = 0.95f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Profile row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(GradientPrimary)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = state.driver?.name?.firstOrNull()?.uppercase() ?: "D",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Xin chào, ${state.driver?.name ?: "Tài xế"}",
                                    color = TextPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (state.isOnline) AccentGreen else StatusOffline)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (state.isOnline) "Đang trực tuyến" else "Ngoại tuyến",
                                        color = if (state.isOnline) AccentGreen else StatusOffline,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(
                                onClick = onNavigateToEarnings,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(AccentYellow.copy(alpha = 0.15f))
                            ) {
                                Icon(Icons.Default.AccountBalanceWallet, "Earnings", tint = AccentYellow, modifier = Modifier.size(20.dp))
                            }
                            IconButton(
                                onClick = onNavigateToProfile,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryPurple.copy(alpha = 0.15f))
                            ) {
                                Icon(Icons.Default.Person, "Profile", tint = PrimaryPurple, modifier = Modifier.size(20.dp))
                            }
                            IconButton(
                                onClick = onLogout,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(AccentRed.copy(alpha = 0.15f))
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Logout, "Logout", tint = AccentRed, modifier = Modifier.size(20.dp))
                            }
                        }
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.06f), thickness = 1.dp)

                    // Action bar row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ActionBarButtonDriver(
                            icon = Icons.Default.DirectionsCar,
                            label = "Chuyến mới",
                            iconColor = AccentGreen,
                            bgColor = AccentGreen.copy(alpha = 0.15f),
                            onClick = { selectedTab = 0 }
                        )
                        ActionBarButtonDriver(
                            icon = Icons.Default.Route,
                            label = "Đang chạy",
                            iconColor = AccentBlue,
                            bgColor = AccentBlue.copy(alpha = 0.15f),
                            onClick = { selectedTab = 1 }
                        )
                        ActionBarButtonDriver(
                            icon = Icons.Default.Group,
                            label = "Batch",
                            iconColor = PrimaryPurple,
                            bgColor = PrimaryPurple.copy(alpha = 0.15f),
                            onClick = { selectedTab = 2 }
                        )
                        ActionBarButtonDriver(
                            icon = Icons.Default.Wallet,
                            label = "Thu nhập",
                            iconColor = AccentYellow,
                            bgColor = AccentYellow.copy(alpha = 0.15f),
                            onClick = onNavigateToEarnings
                        )
                        ActionBarButtonDriver(
                            icon = Icons.Default.Support,
                            label = "Hỗ trợ",
                            iconColor = AccentGreen,
                            bgColor = AccentGreen.copy(alpha = 0.15f),
                            onClick = onNavigateToSupport
                        )
                    }
                }
            }

            // Online toggle card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (state.isOnline) "Bạn đang online" else "Bạn đang offline",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (state.isOnline) "Nhận chuyến ngay!" else "Bật để nhận chuyến",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }

                    // Animated toggle switch
                    val scale by animateFloatAsState(
                        targetValue = if (state.isOnline) 1.1f else 1f,
                        animationSpec = spring(stiffness = Spring.StiffnessMedium),
                        label = "toggleScale"
                    )

                    Box(
                        modifier = Modifier
                            .scale(scale)
                            .clip(CircleShape)
                            .background(
                                if (state.isOnline) AccentGreen else Color.White.copy(alpha = 0.2f)
                            )
                            .clickable { viewModel.toggleOnlineStatus() }
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            if (state.isOnline) {
                                Icon(
                                    Icons.Default.Check,
                                    null,
                                    tint = AccentGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Content based on tab
            when (selectedTab) {
                0 -> {
                    // Available rides
                    if (!state.isOnline) {
                        EmptyState(
                            icon = Icons.Default.WifiOff,
                            title = "Bạn đang offline",
                            subtitle = "Bật chế độ online để nhận chuyến"
                        )
                    } else if (state.availableRides.isEmpty()) {
                        EmptyState(
                            icon = Icons.Default.DirectionsCar,
                            title = "Không có chuyến mới",
                            subtitle = "Đang tìm chuyến cho bạn..."
                        )
                    } else {
                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            Text(
                                text = "${state.availableRides.size} chuyến mới",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            state.availableRides.forEach { ride ->
                                AvailableRideCard(
                                    pickupAddress = ride.pickupAddress,
                                    destAddress = ride.destAddress,
                                    price = ride.price,
                                    distance = ride.distanceKm,
                                    duration = ride.durationMin,
                                    vehicleType = ride.vehicleType,
                                    onAccept = { viewModel.acceptRide(ride.id) },
                                    onReject = { viewModel.rejectRide(ride.id) }
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                }
                1 -> {
                    // Current ride
                    val ride = state.currentRide
                    if (ride == null) {
                        EmptyState(
                            icon = Icons.Default.LocalTaxi,
                            title = "Khong co chuyen",
                            subtitle = "Nhan chuyen de bat dau"
                        )
                    } else {
                        val context = androidx.compose.ui.platform.LocalContext.current

                        if (state.driver != null) {
                            DriverStatsCard(
                                totalTrips = state.earnings?.totalRides ?: 0,
                                avgRating = state.driver?.rating ?: 0.0,
                                acceptanceRate = 85,
                                totalEarnings = state.earnings?.totalEarnings ?: 0.0
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        CurrentRideCard(
                            ride = ride,
                            onStatusUpdate = { status ->
                                viewModel.updateRideStatus(ride.id, status)
                            },
                            onViewDetail = { onNavigateToRideDetail(ride.id) },
                            onNavigateToPickup = {
                                val uri = Uri.parse("google.navigation:q=${Uri.encode(ride.pickupAddress.ifEmpty { "Diem don" })}")
                                val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                                    setPackage("com.google.android.apps.maps")
                                }
                                if (intent.resolveActivity(context.packageManager) != null) {
                                    context.startActivity(intent)
                                } else {
                                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${Uri.encode(ride.pickupAddress.ifEmpty { "Diem don" })}"))
                                    context.startActivity(browserIntent)
                                }
                            },
                            onNavigateToDropoff = {
                                val uri = Uri.parse("google.navigation:q=${Uri.encode(ride.destAddress.ifEmpty { "Diem den" })}")
                                val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                                    setPackage("com.google.android.apps.maps")
                                }
                                if (intent.resolveActivity(context.packageManager) != null) {
                                    context.startActivity(intent)
                                } else {
                                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${Uri.encode(ride.destAddress.ifEmpty { "Diem den" })}"))
                                    context.startActivity(browserIntent)
                                }
                            }
                        )
                    }
                }
                2 -> {
                    // Batch offers
                    LaunchedEffect(Unit) { viewModel.loadBatches() }
                    if (state.batches.isEmpty()) {
                        EmptyState(
                            icon = Icons.Default.Group,
                            title = "Không có batch",
                            subtitle = "Không có đề xuất ghép chuyến nào"
                        )
                    } else {
                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            state.batches.forEach { batch ->
                                BatchCard(
                                    batch = batch,
                                    onAccept = { viewModel.acceptBatch(batch.id) }
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                }
            }
        }

        // Loading overlay
        if (state.isLoading) {
            LoadingOverlay()
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}

@Composable
private fun AvailableRideCard(
    pickupAddress: String,
    destAddress: String,
    price: Double,
    distance: Double,
    duration: Int,
    vehicleType: String?,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    val vehicleLabel = when (vehicleType) {
        "motorbike" -> "Xe may"
        "car_4_seats" -> "O to 4 cho"
        "car_7_seats" -> "O to 7 cho"
        else -> "Xe may"
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Circle, null, tint = AccentGreen, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(pickupAddress.ifEmpty { "Diem don" }, color = TextPrimary, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, null, tint = AccentRed, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(destAddress.ifEmpty { "Diem den" }, color = TextPrimary, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = PrimaryPurple.copy(alpha = 0.15f)
            ) {
                Text(
                    text = vehicleLabel,
                    color = PrimaryPurple,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${String.format("%.0f", price).replace(",", ".")}đ",
                        color = PrimaryPurple,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${distance}km | ~${duration}ph",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onReject,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentRed),
                        border = BorderStroke(1.dp, AccentRed.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("Tu choi", fontSize = 13.sp)
                    }
                    GradientButton(
                        text = "Nhan",
                        onClick = onAccept,
                        modifier = Modifier.width(100.dp),
                        gradient = GradientSuccess
                    )
                }
            }
        }
    }
}

@Composable
private fun CurrentRideCard(
    ride: RideDto,
    onStatusUpdate: (String) -> Unit,
    onViewDetail: () -> Unit,
    onNavigateToPickup: () -> Unit,
    onNavigateToDropoff: () -> Unit
) {
    val vehicleLabel = when (ride.vehicleType) {
        "motorbike" -> "Xe may"
        "car_4_seats" -> "O to 4 cho"
        "car_7_seats" -> "O to 7 cho"
        else -> "Xe may"
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Chuyen dang chay", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                StatusBadge(status = ride.status)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = PrimaryPurple.copy(alpha = 0.15f)
            ) {
                Text(
                    text = vehicleLabel,
                    color = PrimaryPurple,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            InfoRow(
                icon = Icons.Default.Person,
                label = "Khách",
                value = ride.passengerName ?: "N/A"
            )
            InfoRow(
                icon = Icons.Default.Place,
                label = "Đón",
                value = ride.pickupAddress.ifEmpty { "N/A" }
            )
            InfoRow(
                icon = Icons.Default.LocationOn,
                label = "Đến",
                value = ride.destAddress.ifEmpty { "N/A" }
            )
            InfoRow(
                icon = Icons.Default.AttachMoney,
                label = "Giá",
                value = "${String.format("%.0f", ride.price).replace(",", ".")}đ",
                valueColor = PrimaryPurple
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Navigation buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                    OutlinedButton(
                        onClick = onNavigateToPickup,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentGreen)
                    ) {
                        Icon(Icons.Default.NearMe, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Den diem don", fontSize = 12.sp)
                    }
                    OutlinedButton(
                        onClick = onNavigateToDropoff,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentRed)
                    ) {
                        Icon(Icons.Default.Navigation, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Den diem den", fontSize = 12.sp)
                    }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons based on status
            when (ride.status) {
                "accepted" -> {
                    GradientButton(
                        text = "Đã đến điểm đón",
                        onClick = { onStatusUpdate("arrived") },
                        gradient = GradientPrimary
                    )
                }
                "arrived" -> {
                    GradientButton(
                        text = "Bắt đầu chuyến",
                        onClick = { onStatusUpdate("in_progress") },
                        gradient = GradientSuccess
                    )
                }
                "in_progress" -> {
                    GradientButton(
                        text = "Hoàn thành chuyến",
                        onClick = { onStatusUpdate("completed") },
                        gradient = GradientSuccess
                    )
                }
                else -> {
                    OutlinedButton(
                        onClick = onViewDetail,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Xem chi tiết")
                    }
                }
            }

            if (ride.status != "completed" && ride.status != "cancelled") {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { onStatusUpdate("cancelled") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Hủy chuyến")
                }
            }
        }
    }
}

@Composable
private fun DriverStatsCard(
    totalTrips: Int,
    avgRating: Double,
    acceptanceRate: Int,
    totalEarnings: Double
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$totalTrips",
                    color = PrimaryPurple,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text("Tong chuyen", color = TextSecondary, fontSize = 11.sp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = String.format("%.1f", avgRating),
                    color = AccentYellow,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text("Danh gia", color = TextSecondary, fontSize = 11.sp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$acceptanceRate%",
                    color = AccentGreen,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text("Ty le nhan", color = TextSecondary, fontSize = 11.sp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${String.format("%.0f", totalEarnings).replace(",", ".")}d",
                    color = AccentBlue,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text("Tong thu", color = TextSecondary, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun BatchCard(
    batch: BatchDto,
    onAccept: () -> Unit
) {
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
                        text = batch.batchName ?: "Batch #${batch.id}",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${batch.passengerCount ?: 0} khách",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${String.format("%.0f", batch.totalRevenue ?: 0.0).replace(",", ".")}đ",
                        color = PrimaryPurple,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Hiệu quả: ${String.format("%.0f", (batch.efficiencyScore ?: 0.0) * 100)}%",
                        color = AccentGreen,
                        fontSize = 12.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            GradientButton(
                text = "Chấp nhận Batch",
                onClick = onAccept,
                gradient = listOf(AccentBlue, AccentBlue.copy(alpha = 0.7f))
            )
        }
    }
}

@Composable
private fun ActionBarButtonDriver(
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
