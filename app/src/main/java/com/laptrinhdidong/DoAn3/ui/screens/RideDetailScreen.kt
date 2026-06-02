package com.laptrinhdidong.DoAn3.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import java.text.NumberFormat
import java.util.Locale
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laptrinhdidong.DoAn3.data.remote.dto.DriverDto
import com.laptrinhdidong.DoAn3.data.remote.dto.RideDto
import com.laptrinhdidong.DoAn3.data.repository.DriverRepository
import com.laptrinhdidong.DoAn3.data.repository.PaymentRepository
import com.laptrinhdidong.DoAn3.data.repository.RideRepository
import com.laptrinhdidong.DoAn3.ui.components.*
import com.laptrinhdidong.DoAn3.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RideDetailState(
    val isLoading: Boolean = false,
    val ride: RideDto? = null,
    val driver: DriverDto? = null,
    val errorMessage: String? = null,
    val ratingSubmitted: Boolean = false,
    val selectedPaymentMethod: String = "cash",
    val paymentCreating: Boolean = false,
    val paymentCreated: Boolean = false,
    val paymentUrl: String? = null,
    val paymentError: String? = null
)

@dagger.hilt.android.lifecycle.HiltViewModel
class RideDetailViewModel @javax.inject.Inject constructor(
    private val rideRepository: RideRepository,
    private val driverRepository: DriverRepository,
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    private var rideId: Int = -1

    private val _state = MutableStateFlow(RideDetailState())
    val state: StateFlow<RideDetailState> = _state.asStateFlow()

    fun setRideId(id: Int) {
        if (rideId != id) {
            rideId = id
            loadRide()
        }
    }

    fun loadRide() {
        if (rideId < 0) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            rideRepository.getRide(rideId).onSuccess { ride ->
                _state.value = _state.value.copy(ride = ride, isLoading = false)
                ride.driverId?.let { loadDriver(it) }
            }.onFailure {
                _state.value = _state.value.copy(isLoading = false, errorMessage = "Không thể tải chuyến")
            }
        }
    }

    private fun loadDriver(driverId: Int) {
        viewModelScope.launch {
            driverRepository.getDriverProfile().onSuccess { driver ->
                _state.value = _state.value.copy(driver = driver)
            }
        }
    }

    fun updateStatus(status: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            rideRepository.updateRideStatus(rideId, status).onSuccess { ride ->
                _state.value = _state.value.copy(ride = ride, isLoading = false)
            }.onFailure {
                _state.value = _state.value.copy(isLoading = false, errorMessage = "Cập nhật thất bại")
            }
        }
    }

    fun rateRide(rating: Int, comment: String?) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            rideRepository.rateRide(rideId, rating, comment).onSuccess {
                _state.value = _state.value.copy(isLoading = false, ratingSubmitted = true)
            }.onFailure {
                _state.value = _state.value.copy(isLoading = false, errorMessage = "Gửi đánh giá thất bại")
            }
        }
    }

    fun selectPaymentMethod(method: String) {
        _state.value = _state.value.copy(selectedPaymentMethod = method)
    }

    fun createPayment() {
        viewModelScope.launch {
            _state.value = _state.value.copy(paymentCreating = true, paymentError = null)
            val result = paymentRepository.createPayment(rideId, _state.value.selectedPaymentMethod)
            result.onSuccess { paymentData ->
                _state.value = _state.value.copy(
                    paymentCreating = false,
                    paymentCreated = true,
                    paymentUrl = paymentData.paymentUrl
                )
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    paymentCreating = false,
                    paymentError = e.message ?: "Tạo thanh toán thất bại"
                )
            }
        }
    }

    fun clearPaymentError() {
        _state.value = _state.value.copy(paymentError = null)
    }

    fun clearPaymentCreated() {
        _state.value = _state.value.copy(paymentCreated = false, paymentUrl = null)
    }
}

@Composable
fun RideDetailScreen(
    rideId: Int,
    isDriverView: Boolean,
    onBack: () -> Unit,
    viewModel: RideDetailViewModel = hiltViewModel()
) {
    LaunchedEffect(rideId) { viewModel.setRideId(rideId) }
    val state by viewModel.state.collectAsState()
    var showRatingDialog by remember { mutableStateOf(false) }
    var selectedRating by remember { mutableIntStateOf(5) }
    var ratingComment by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppTopBar(title = "Chi tiết chuyến đi", onBackClick = onBack)

            if (state.ride == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryPurple)
                }
            } else {
                val ride = state.ride!!

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Status card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkCard)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Mã chuyến", color = TextSecondary, fontSize = 12.sp)
                                    Text("#${ride.id}", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                }
                                StatusBadge(status = ride.status)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Route card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkCard)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("Lộ trình", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(32.dp).clip(CircleShape).background(AccentGreen.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Circle, null, tint = AccentGreen, modifier = Modifier.size(12.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Điểm đón", color = TextSecondary, fontSize = 11.sp)
                                    Text(ride.pickupAddress.ifEmpty { "Đang cập nhật..." }, color = TextPrimary, fontSize = 14.sp)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .padding(start = 15.dp)
                                    .width(2.dp)
                                    .height(30.dp)
                                    .background(Color.White.copy(alpha = 0.2f))
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(32.dp).clip(CircleShape).background(AccentRed.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.LocationOn, null, tint = AccentRed, modifier = Modifier.size(16.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Điểm đến", color = TextSecondary, fontSize = 11.sp)
                                    Text(ride.destAddress.ifEmpty { "Đang cập nhật..." }, color = TextPrimary, fontSize = 14.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Price card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkCard)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Route, null, tint = PrimaryPurple)
                                Text("${ride.distanceKm} km", color = TextPrimary, fontWeight = FontWeight.Bold)
                                Text("Khoảng cách", color = TextSecondary, fontSize = 12.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Schedule, null, tint = AccentBlue)
                                Text("~${ride.durationMin} ph", color = TextPrimary, fontWeight = FontWeight.Bold)
                                Text("Thời gian", color = TextSecondary, fontSize = 12.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.AttachMoney, null, tint = AccentGreen)
                                Text(
                                    "${String.format("%.0f", ride.price).replace(",", ".")}đ",
                                    color = PrimaryPurple,
                                    fontWeight = FontWeight.Bold
                                )
                                Text("Giá cước", color = TextSecondary, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Payment method (only for passengers with pending rides)
                    if (!isDriverView && ride.status == "pending") {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkCard)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text("Phuong thuc thanh toan", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    val paymentMethods = listOf(
                                        Triple("cash", "Tien mat", Icons.Default.Money),
                                        Triple("momo", "MoMo", Icons.Default.PhoneAndroid),
                                        Triple("vnpay", "VNPay", Icons.Default.CreditCard),
                                    )
                                    paymentMethods.forEach { (code, label, icon) ->
                                        val isSelected = state.selectedPaymentMethod == code
                                        PaymentMethodChip(
                                            icon = icon,
                                            label = label,
                                            isSelected = isSelected,
                                            onClick = { viewModel.selectPaymentMethod(code) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }

                                if (!state.paymentCreated) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    val priceText = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
                                        .format(ride.price)
                                    GradientButton(
                                        text = if (state.paymentCreating) "Dang xu li..." else "Xac nhan thanh toan $priceText",
                                        onClick = { viewModel.createPayment() },
                                        enabled = !state.paymentCreating,
                                        isLoading = state.paymentCreating
                                    )
                                } else {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            null,
                                            tint = AccentGreen,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            if (state.selectedPaymentMethod == "cash")
                                                "Thanh toan tien mat"
                                            else
                                                "Da tao thanh toan",
                                            color = AccentGreen,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Driver/Passenger info
                    if (isDriverView && ride.passengerName != null) {
                        PersonInfoCard(
                            name = ride.passengerName,
                            icon = Icons.Default.Person,
                            title = "Hanh khach"
                        )
                    } else if (!isDriverView && state.driver != null) {
                        PersonInfoCard(
                            name = state.driver!!.name ?: "Tai xe",
                            icon = Icons.Default.DirectionsCar,
                            title = "Tai xe",
                            subtitle = "${state.driver!!.carModel ?: ""} ${state.driver!!.carColor ?: ""}",
                            plate = state.driver!!.licensePlate,
                            rating = state.driver!!.rating
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Rating
                    if (ride.status == "completed" && !state.ratingSubmitted) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkCard)
                        ) {
                            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Danh gia", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(16.dp))
                                Row {
                                    (1..5).forEach { star ->
                                        IconButton(onClick = { selectedRating = star }) {
                                            Icon(
                                                if (star <= selectedRating) Icons.Filled.Star else Icons.Outlined.Star,
                                                null,
                                                tint = AccentYellow,
                                                modifier = Modifier.size(40.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                val ratingTags = if (isDriverView) listOf(
                                    "An toan", "Than thien", "Ho tro tot"
                                ) else listOf(
                                    "Lai xe an toan", "Xe sach", "Dung gio", "Than thien"
                                )
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(ratingTags) { tag ->
                                        FilterChip(
                                            selected = ratingComment.contains(tag),
                                            onClick = {
                                                ratingComment = if (ratingComment.contains(tag)) {
                                                    ratingComment.replace(tag, "").trim()
                                                } else {
                                                    if (ratingComment.isNotEmpty()) "$ratingComment, $tag" else tag
                                                }
                                            },
                                            label = { Text(tag, fontSize = 12.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = PrimaryPurple,
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                GradientButton(
                                    text = "Gui danh gia",
                                    onClick = {
                                        viewModel.rateRide(selectedRating, ratingComment.takeIf { it.isNotEmpty() })
                                    }
                                )
                            }
                        }
                    }

                    if (ride.status == "completed" && ride.driverRating != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkCard)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.Star, null, tint = AccentYellow)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Bạn đã đánh giá ${ride.driverRating} sao", color = TextSecondary)
                            }
                        }
                    }
                }
            }
        }

        // Loading
        if (state.isLoading) {
            LoadingOverlay()
        }

        // Payment error snackbar
        val snackbarHostState = remember { SnackbarHostState() }
        LaunchedEffect(state.paymentError) {
            state.paymentError?.let {
                snackbarHostState.showSnackbar(it)
                viewModel.clearPaymentError()
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }

    // Rating dialog
    if (showRatingDialog) {
        AlertDialog(
            onDismissRequest = { showRatingDialog = false },
            containerColor = DarkCard,
            title = { Text("Đánh giá tài xế", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Bạn cảm thấy chuyến đi như thế nào?", color = TextSecondary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row {
                        (1..5).forEach { star ->
                            IconButton(onClick = { selectedRating = star }) {
                                Icon(
                                    if (star <= selectedRating) Icons.Filled.Star else Icons.Outlined.Star,
                                    null,
                                    tint = AccentYellow,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = ratingComment,
                        onValueChange = { ratingComment = it },
                        label = { Text("Bình luận (tùy chọn)", color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = PrimaryPurple,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.rateRide(selectedRating, ratingComment.takeIf { it.isNotEmpty() })
                    showRatingDialog = false
                }) {
                    Text("Gửi", color = PrimaryPurple)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRatingDialog = false }) {
                    Text("Hủy", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun PersonInfoCard(
    name: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String? = null,
    plate: String? = null,
    rating: Double? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(56.dp).clip(CircleShape).background(Brush.linearGradient(GradientPrimary)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = TextSecondary, fontSize = 12.sp)
                Text(name, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                if (subtitle != null) Text(subtitle, color = TextSecondary, fontSize = 13.sp)
                if (plate != null) Text(plate, color = PrimaryPurple, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
            if (rating != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    RatingBar(rating = rating, size = 16)
                    Text(String.format("%.1f", rating), color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun PaymentMethodChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) PrimaryPurple.copy(alpha = 0.2f) else DarkCard,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, PrimaryPurple) else null
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                null,
                tint = if (isSelected) PrimaryPurple else TextSecondary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                label,
                color = if (isSelected) PrimaryPurple else TextPrimary,
                fontSize = 11.sp
            )
        }
    }
}
