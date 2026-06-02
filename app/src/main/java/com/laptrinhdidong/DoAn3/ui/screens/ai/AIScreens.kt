package com.laptrinhdidong.DoAn3.ui.screens.ai

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laptrinhdidong.DoAn3.data.remote.dto.*
import com.laptrinhdidong.DoAn3.data.repository.AIRepository
import com.laptrinhdidong.DoAn3.ui.components.*
import com.laptrinhdidong.DoAn3.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

private fun formatCurrency(amount: Double?): String {
    if (amount == null) return "—"
    val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    return formatter.format(amount) + "đ"
}

private fun formatDuration(minutes: Int?): String {
    if (minutes == null) return "—"
    return if (minutes >= 60) {
        val h = minutes / 60
        val m = minutes % 60
        if (m > 0) "${h}h ${m}ph" else "${h}h"
    } else {
        "${minutes}ph"
    }
}

private fun formatDistance(km: Double?): String {
    if (km == null) return "—"
    return String.format("%.1f km", km)
}

data class AIScheduleState(
    val isLoading: Boolean = false,
    val schedules: List<AIScheduleDto> = emptyList(),
    val currentSchedule: AIScheduleDto? = null,
    val alternatives: List<RouteAlternativeDto> = emptyList(),
    val waypoints: List<AIWaypointDto> = emptyList(),
    val selectedOptimization: String = "balanced",
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val recommendations: AIRecommendationDto? = null,
    val isLoadingRecommendations: Boolean = false
)

@dagger.hilt.android.lifecycle.HiltViewModel
class AIScheduleViewModel @javax.inject.Inject constructor(
    private val repository: AIRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AIScheduleState())
    val state: StateFlow<AIScheduleState> = _state.asStateFlow()

    init { loadHistory() }

    fun loadHistory() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            repository.getAIHistory().onSuccess { schedules ->
                _state.value = _state.value.copy(isLoading = false, schedules = schedules)
            }.onFailure {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    fun createSchedule(name: String, date: String, optimization: String, waypoints: List<WaypointDto>) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            repository.createSchedule(name, date, optimization, waypoints).onSuccess { schedule ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    currentSchedule = schedule,
                    alternatives = schedule.alternatives ?: emptyList(),
                    waypoints = schedule.waypoints ?: emptyList(),
                    successMessage = "Tạo lịch trình thành công!"
                )
                loadHistory()
            }.onFailure {
                _state.value = _state.value.copy(isLoading = false, errorMessage = "Tạo lịch trình thất bại")
            }
        }
    }

    fun selectSchedule(scheduleId: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            repository.getSchedule(scheduleId).onSuccess { schedule ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    currentSchedule = schedule,
                    alternatives = schedule.alternatives ?: emptyList(),
                    waypoints = schedule.waypoints ?: emptyList()
                )
            }.onFailure {
                _state.value = _state.value.copy(isLoading = false, errorMessage = "Không thể tải lịch trình")
            }
        }
    }

    fun optimizeSchedule(scheduleId: Int, optimization: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, selectedOptimization = optimization)
            repository.optimizeSchedule(scheduleId, optimization).onSuccess { alternatives ->
                _state.value = _state.value.copy(isLoading = false, alternatives = alternatives)
            }.onFailure {
                _state.value = _state.value.copy(isLoading = false, errorMessage = "Tối ưu thất bại")
            }
        }
    }

    fun loadRecommendations() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoadingRecommendations = true)
            repository.getAIRecommendations().onSuccess { rec ->
                _state.value = _state.value.copy(
                    isLoadingRecommendations = false,
                    recommendations = rec
                )
            }.onFailure {
                _state.value = _state.value.copy(isLoadingRecommendations = false)
            }
        }
    }

    fun clearMessages() {
        _state.value = _state.value.copy(errorMessage = null, successMessage = null)
    }
}

@Composable
fun AIScheduleScreen(
    onBack: () -> Unit,
    onOpenMap: (String, List<Pair<String, Triple<Double, Double, String>>>) -> Unit = { _, _ -> },
    viewModel: AIScheduleViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showCreateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.errorMessage, state.successMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppTopBar(title = "Lịch trình AI", onBackClick = onBack)

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryPurple)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    GradientButton(
                        text = "+ Tạo lịch trình mới",
                        onClick = { showCreateDialog = true },
                        gradient = listOf(PrimaryPink, PrimaryPurple)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    if (state.currentSchedule != null) {
                        ScheduleDetailCard(
                            schedule = state.currentSchedule!!,
                            alternatives = state.alternatives,
                            waypoints = state.waypoints,
                            onOptimize = { type ->
                                viewModel.optimizeSchedule(state.currentSchedule!!.id, type)
                            },
                            onOpenMap = onOpenMap
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    Text(
                        text = "Lịch sử lịch trình",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (state.schedules.isEmpty()) {
                        EmptyState(
                            icon = Icons.Default.Schedule,
                            title = "Chưa có lịch trình",
                            subtitle = "Tạo lịch trình đầu tiên với AI"
                        )
                    } else {
                        state.schedules.forEach { schedule ->
                            ScheduleHistoryCard(
                                schedule = schedule,
                                onClick = { viewModel.selectSchedule(schedule.id) }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }

        if (showCreateDialog) {
            CreateScheduleDialog(
                onDismiss = { showCreateDialog = false },
                onCreate = { name, date, optimization, waypoints ->
                    viewModel.createSchedule(name, date, optimization, waypoints)
                    showCreateDialog = false
                },
                onOpenMap = onOpenMap
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
        )
    }
}

@Composable
private fun ScheduleDetailCard(
    schedule: AIScheduleDto,
    alternatives: List<RouteAlternativeDto>,
    waypoints: List<AIWaypointDto>,
    onOptimize: (String) -> Unit,
    onOpenMap: (String, List<Pair<String, Triple<Double, Double, String>>>) -> Unit
) {
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = schedule.scheduleName,
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Ngày: ${schedule.scheduledDate ?: "Chưa xác định"}",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
                StatusBadge(status = schedule.status)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = formatDistance(schedule.totalDistance),
                        color = PrimaryPurple,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("Khoảng cách", color = TextSecondary, fontSize = 12.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = formatDuration(schedule.totalEstimatedTime),
                        color = AccentBlue,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("Thời gian", color = TextSecondary, fontSize = 12.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = formatCurrency(schedule.totalEstimatedPrice),
                        color = AccentGreen,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("Chi phí ước tính", color = TextSecondary, fontSize = 12.sp)
                }
            }

            if (waypoints.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "📍 Lộ trình chi tiết (${waypoints.size} điểm dừng)",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))

                waypoints.forEachIndexed { index, wp ->
                    val isFirst = index == 0
                    val isLast = index == waypoints.lastIndex
                    val prevWp = if (index > 0) waypoints[index - 1] else null

                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = when (wp.stopType?.lowercase()) {
                                "pickup" -> AccentGreen.copy(alpha = 0.1f)
                                "dropoff" -> AccentRed.copy(alpha = 0.1f)
                                else -> DarkSurface
                            }
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when (wp.stopType?.lowercase()) {
                                                    "pickup" -> AccentGreen
                                                    "dropoff" -> AccentRed
                                                    else -> PrimaryPurple
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "${index + 1}",
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = wp.stopName ?: wp.address?.take(40) ?: "Điểm ${index + 1}",
                                            color = TextPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (!wp.address.isNullOrBlank() && wp.address != wp.stopName) {
                                            Text(
                                                text = wp.address,
                                                color = TextSecondary,
                                                fontSize = 11.sp,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        val allLocs: List<Pair<String, Triple<Double, Double, String>>> = waypoints.map { w: AIWaypointDto ->
                                            (w.stopName ?: w.address ?: "Điểm") to Triple(w.latitude, w.longitude, w.address ?: "")
                                        }
                                        onOpenMap(schedule.scheduleName, allLocs)
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Map,
                                        contentDescription = "Xem trên bản đồ",
                                        tint = AccentBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            if (!isFirst && prevWp != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    if (wp.distanceFromPrev != null && wp.distanceFromPrev > 0) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.Route,
                                                null,
                                                tint = TextSecondary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                formatDistance(wp.distanceFromPrev),
                                                color = TextSecondary,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                    if (wp.durationMin != null && wp.durationMin > 0) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.Schedule,
                                                null,
                                                tint = TextSecondary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                "~${formatDuration(wp.durationMin)} di chuyển",
                                                color = TextSecondary,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }

                            if (wp.estimatedPriceSegment != null && wp.estimatedPriceSegment > 0) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Phí đoạn: ${formatCurrency(wp.estimatedPriceSegment)}",
                                    color = AccentGreen,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            if (isLast) {
                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Tổng chi phí", color = TextSecondary, fontSize = 13.sp)
                                    Text(
                                        formatCurrency(schedule.totalEstimatedPrice),
                                        color = AccentGreen,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Tổng thời gian", color = TextSecondary, fontSize = 13.sp)
                                    Text(
                                        formatDuration(schedule.totalEstimatedTime),
                                        color = AccentBlue,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (alternatives.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "Phương án tối ưu",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                alternatives.forEach { alt ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (alt.isRecommended == true)
                                PrimaryPurple.copy(alpha = 0.15f)
                            else DarkSurface
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        alt.routeName,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                    if (alt.isRecommended == true) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = AccentGreen.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                "Đề xuất",
                                                color = AccentGreen,
                                                fontSize = 10.sp,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Route,
                                            null,
                                            tint = TextSecondary,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            "${alt.totalDistance}km",
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Schedule,
                                            null,
                                            tint = TextSecondary,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            formatDuration(alt.totalDuration),
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.AttachMoney,
                                            null,
                                            tint = TextSecondary,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            formatCurrency(alt.totalPrice),
                                            color = AccentGreen,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                                if (!alt.routeDescription.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        alt.routeDescription,
                                        color = TextSecondary,
                                        fontSize = 11.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("time" to "Nhanh", "cost" to "Rẻ", "balanced" to "Cân bằng").forEach { (type, label) ->
                        OutlinedButton(
                            onClick = { onOptimize(type) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (schedule.optimizationType == type) PrimaryPurple else TextSecondary
                            )
                        ) {
                            Text(label, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleHistoryCard(schedule: AIScheduleDto, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    schedule.scheduleName,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "${formatDistance(schedule.totalDistance)} • ${formatCurrency(schedule.totalEstimatedPrice)} • ${formatDuration(schedule.totalEstimatedTime)}",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
                if (!schedule.scheduledDate.isNullOrBlank()) {
                    Text(
                        "📅 ${schedule.scheduledDate}",
                        color = TextHint,
                        fontSize = 11.sp
                    )
                }
            }
            StatusBadge(status = schedule.status)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateScheduleDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String, String, List<WaypointDto>) -> Unit,
    onOpenMap: (String, List<Pair<String, Triple<Double, Double, String>>>) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var scheduleName by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(java.time.LocalDate.now().toString()) }
    var selectedOptimization by remember { mutableStateOf("balanced") }
    var waypointCount by remember { mutableIntStateOf(3) }
    var selectedVehicleType by remember { mutableStateOf("motorbike") }

    val vehicleTypes = listOf(
        Triple("motorbike", "Xe máy", Icons.Default.TwoWheeler),
        Triple("car_4_seats", "Ô tô 4 chỗ", Icons.Default.DirectionsCar),
        Triple("car_7_seats", "Ô tô 7 chỗ", Icons.Default.AirportShuttle)
    )

    val popularDestinations = remember {
        listOf(
            Triple("Sân bay Đà Nẵng", Pair(16.0544, 108.2022), "Sân bay quốc tế Đà Nẵng, Quảng Nam"),
            Triple("Trường ĐH Bách Khoa", Pair(10.7629, 106.6604), "Trường ĐH Bách Khoa TP.HCM"),
            Triple("Bãi Biển Mỹ Khê", Pair(16.0678, 108.2100), "Bãi biển Mỹ Khê, Đà Nẵng"),
            Triple("Phố cổ Hội An", Pair(15.9802, 108.2677), "Phố cổ Hội An, Quảng Nam"),
            Triple("Landmark 81", Pair(10.7952, 106.7218), "Landmark 81, Bình Thạnh, TP.HCM"),
            Triple("Bến Thành Market", Pair(10.7729, 106.6980), "Chợ Bến Thành, Q.1, TP.HCM"),
            Triple("Ngũ Hành Sơn", Pair(16.0013, 108.2670), "Ngũ Hành Sơn, Đà Nẵng"),
            Triple("Bán đảo Sơn Trà", Pair(16.0959, 108.2575), "Nghinh Phong, Sơn Trà, Đà Nẵng")
        )
    }

    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSurface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "Tạo lịch trình AI",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = scheduleName,
                onValueChange = { scheduleName = it },
                label = { Text("Tên lịch trình", color = TextSecondary) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = DarkCard,
                    unfocusedContainerColor = DarkCard,
                    focusedBorderColor = PrimaryPurple,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Loại phương tiện:", color = TextSecondary, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                vehicleTypes.forEach { (type, label, icon) ->
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedVehicleType = type },
                        shape = RoundedCornerShape(8.dp),
                        color = if (selectedVehicleType == type) PrimaryPurple else DarkCard
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                icon,
                                null,
                                tint = if (selectedVehicleType == type) Color.White else TextSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                label,
                                color = if (selectedVehicleType == type) Color.White else TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Tối ưu theo:", color = TextSecondary, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("time" to "Nhanh nhất", "cost" to "Rẻ nhất", "balanced" to "Cân bằng").forEach { (type, label) ->
                    Surface(
                        modifier = Modifier.clickable { selectedOptimization = type },
                        shape = RoundedCornerShape(8.dp),
                        color = if (selectedOptimization == type) PrimaryPurple else DarkCard
                    ) {
                        Text(
                            label,
                            color = if (selectedOptimization == type) Color.White else TextSecondary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Địa điểm phổ biến (bấm để mở Google Maps)",
                color = TextSecondary,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            popularDestinations.take(6).forEach { (name, coords, address) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .clickable {
                            val locs: List<Pair<String, Triple<Double, Double, String>>> = listOf(name to Triple(coords.first, coords.second, address))
                            onOpenMap(name, locs)
                        },
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Place,
                            null,
                            tint = PrimaryPurple,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                name,
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                address,
                                color = TextSecondary,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Icon(
                            Icons.Default.Map,
                            null,
                            tint = AccentBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Số điểm dừng trong lịch trình: $waypointCount",
                color = TextSecondary,
                fontSize = 13.sp
            )
            Slider(
                value = waypointCount.toFloat(),
                onValueChange = { waypointCount = it.toInt() },
                valueRange = 2f..8f,
                steps = 5,
                colors = SliderDefaults.colors(thumbColor = PrimaryPurple, activeTrackColor = PrimaryPurple)
            )

            Spacer(modifier = Modifier.height(20.dp))

            GradientButton(
                text = "Tạo lịch trình",
                onClick = {
                    if (scheduleName.isNotEmpty()) {
                        val waypoints = (0 until waypointCount).map { index ->
                            val loc = popularDestinations[index % popularDestinations.size]
                            WaypointDto(
                                lat = loc.second.first,
                                lng = loc.second.second,
                                address = "${loc.first}, ${loc.third}",
                                stopName = loc.first,
                                stopType = when {
                                    index == 0 -> "pickup"
                                    index == waypointCount - 1 -> "dropoff"
                                    else -> "stopover"
                                },
                                priority = if (index == 0 || index == waypointCount - 1) 1 else 0,
                                isOptional = false
                            )
                        }
                        onCreate(scheduleName, selectedDate, selectedOptimization, waypoints)
                    }
                },
                enabled = scheduleName.isNotEmpty()
            )
        }
    }
}

@Composable
fun AIRecommendationsScreen(
    onBack: () -> Unit,
    viewModel: AIScheduleViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadRecommendations()
    }

    Box(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppTopBar(title = "Đề xuất AI", onBackClick = onBack)

            if (state.isLoadingRecommendations) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = PrimaryPurple)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("AI đang phân tích...", color = TextSecondary)
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    state.recommendations?.let { rec ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkCard)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(20.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Độ tin cậy AI", color = TextSecondary, fontSize = 13.sp)
                                    Text(
                                        "${((rec.aiConfidence ?: 0.0) * 100).toInt()}%",
                                        color = AccentGreen,
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .background(AccentGreen.copy(alpha = 0.2f), RoundedCornerShape(50)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.AutoAwesome,
                                        null,
                                        tint = AccentGreen,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (!rec.preferredTime.isNullOrBlank()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = DarkCard)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Schedule,
                                        null,
                                        tint = AccentBlue,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            "Giờ thường đi",
                                            color = TextSecondary,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            rec.preferredTime,
                                            color = TextPrimary,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        if (rec.frequentRoutes?.isNotEmpty() == true) {
                            Text(
                                "Tuyến đường thường dùng",
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            rec.frequentRoutes.forEach { route ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = DarkCard)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.Route,
                                                null,
                                                tint = PrimaryPurple
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    route.pickup ?: "",
                                                    color = TextPrimary,
                                                    fontSize = 14.sp
                                                )
                                                Text(
                                                    "→ ${route.dest ?: ""}",
                                                    color = TextSecondary,
                                                    fontSize = 13.sp
                                                )
                                            }
                                            Text(
                                                "${route.count}x",
                                                color = PrimaryPurple,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            )
                                        }
                                        if (route.price != null) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                "Giá trung bình: ${formatCurrency(route.price)}",
                                                color = TextSecondary,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (rec.estimatedSavings != null && rec.estimatedSavings > 0) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = AccentGreen.copy(alpha = 0.15f))
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Savings,
                                        null,
                                        tint = AccentGreen,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(
                                            "Tiết kiệm ước tính",
                                            color = TextPrimary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            "${formatCurrency(rec.estimatedSavings.toDouble())} / chuyến",
                                            color = AccentGreen,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        if (rec.bestTimes?.isNotEmpty() == true) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = DarkCard)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        "Khung giờ tốt nhất",
                                        color = TextPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    rec.bestTimes.forEach { time ->
                                        Row(
                                            modifier = Modifier.padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.AccessTime,
                                                null,
                                                tint = AccentBlue,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(time, color = TextPrimary, fontSize = 14.sp)
                                        }
                                    }
                                }
                            }
                        }
                    } ?: run {
                        EmptyState(
                            icon = Icons.Default.AutoAwesome,
                            title = "Chưa có đề xuất",
                            subtitle = "Đặt vài chuyến để AI học thói quen của bạn"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AIProfileScreen(
    onBack: () -> Unit,
    onOpenMap: (String, List<Pair<String, Triple<Double, Double, String>>>) -> Unit = { _, _ -> }
) {
    var preference by remember { mutableFloatStateOf(0.5f) }

    Box(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppTopBar(title = "Hồ sơ AI", onBackClick = onBack)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "Tùy chỉnh AI",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        Text("Ưu tiên chi phí", color = TextSecondary, fontSize = 13.sp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Nhanh", color = AccentGreen, fontSize = 12.sp)
                            Text("Tiết kiệm", color = AccentBlue, fontSize = 12.sp)
                        }
                        Slider(
                            value = preference,
                            onValueChange = { preference = it },
                            colors = SliderDefaults.colors(thumbColor = PrimaryPurple, activeTrackColor = PrimaryPurple)
                        )
                        Text(
                            text = if (preference < 0.4) "Bạn ưu tiên tiết kiệm chi phí"
                            else if (preference > 0.6) "Bạn ưu tiên tốc độ"
                            else "Bạn muốn cân bằng",
                            color = TextPrimary,
                            fontSize = 14.sp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        GradientButton(text = "Lưu cài đặt", onClick = {})
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "Vị trí thường đến",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        listOf(
                            Triple("Trường ĐH Bách Khoa", 10.7629, 106.6604),
                            Triple("Vinmart Điện Biên Phủ", 10.7769, 106.7000),
                            Triple("Landmark 81", 10.7952, 106.7218)
                        ).forEach { (name, lat, lng) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .clickable {
                                        val locs: List<Pair<String, Triple<Double, Double, String>>> = listOf(name to Triple(lat, lng, name))
                                        onOpenMap(name, locs)
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Place, null, tint = PrimaryPurple)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(name, color = TextPrimary, modifier = Modifier.weight(1f))
                                Icon(
                                    Icons.Default.Map,
                                    null,
                                    tint = AccentBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
