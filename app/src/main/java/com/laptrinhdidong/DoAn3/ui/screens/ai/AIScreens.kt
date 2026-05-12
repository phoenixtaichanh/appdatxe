package com.laptrinhdidong.DoAn3.ui.screens.ai

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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

// ========== SCHEDULE STATE ==========
data class AIScheduleState(
    val isLoading: Boolean = false,
    val schedules: List<AIScheduleDto> = emptyList(),
    val currentSchedule: AIScheduleDto? = null,
    val alternatives: List<RouteAlternativeDto> = emptyList(),
    val waypoints: List<WaypointDto> = emptyList(),
    val selectedOptimization: String = "balanced",
    val errorMessage: String? = null,
    val successMessage: String? = null
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
                    waypoints = schedule.waypoints?.map {
                        WaypointDto(it.latitude, it.longitude, it.address, it.stopName, it.stopType, it.priority ?: 0, it.isOptional ?: false)
                    } ?: emptyList(),
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
                    waypoints = schedule.waypoints?.map {
                        WaypointDto(it.latitude, it.longitude, it.address, it.stopName, it.stopType, it.priority ?: 0, it.isOptional ?: false)
                    } ?: emptyList()
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

    fun clearMessages() {
        _state.value = _state.value.copy(errorMessage = null, successMessage = null)
    }
}

@Composable
fun AIScheduleScreen(
    onBack: () -> Unit,
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
                    // Create new schedule button
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
                            onOptimize = { type ->
                                viewModel.optimizeSchedule(state.currentSchedule!!.id, type)
                            }
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // History
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
                }
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
    onOptimize: (String) -> Unit
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
                Text(
                    text = schedule.scheduleName,
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                StatusBadge(status = schedule.status)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${schedule.totalDistance?.let { String.format("%.1f", it) } ?: "—"} km",
                        color = PrimaryPurple,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("Khoảng cách", color = TextSecondary, fontSize = 12.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${schedule.totalEstimatedTime ?: "—"} ph",
                        color = AccentBlue,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("Thời gian", color = TextSecondary, fontSize = 12.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${schedule.totalEstimatedPrice?.let { String.format("%.0f", it).replace(",", ".") } ?: "—"}đ",
                        color = AccentGreen,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("Chi phí", color = TextSecondary, fontSize = 12.sp)
                }
            }

            if (schedule.waypoints != null && schedule.waypoints!!.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(12.dp))

                schedule.waypoints!!.forEachIndexed { index, wp ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${index + 1}", color = PrimaryPurple, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(wp.address, color = TextPrimary, fontSize = 14.sp)
                    }
                    if (index < schedule.waypoints!!.size - 1) {
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }

            if (alternatives.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(12.dp))

                Text("Phương án tối ưu", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
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
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(alt.routeName, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                                    if (alt.isRecommended == true) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(shape = RoundedCornerShape(4.dp), color = AccentGreen.copy(alpha = 0.2f)) {
                                            Text("Đề xuất", color = AccentGreen, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                    }
                                }
                                Text(
                                    text = "${alt.totalDistance}km • ${alt.totalDuration}ph • ${String.format("%.0f", alt.totalPrice).replace(",", ".")}đ",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
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
                Text(schedule.scheduleName, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Text(
                    text = "${schedule.totalDistance ?: 0}km • ${String.format("%.0f", schedule.totalEstimatedPrice ?: 0.0).replace(",", ".")}đ",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }
            StatusBadge(status = schedule.status)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateScheduleDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String, String, List<WaypointDto>) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var scheduleName by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(java.time.LocalDate.now().toString()) }
    var selectedOptimization by remember { mutableStateOf("balanced") }
    var waypointCount by remember { mutableIntStateOf(2) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSurface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp).padding(bottom = 32.dp)
        ) {
            Text("Tạo lịch trình AI", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
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

            Spacer(modifier = Modifier.height(12.dp))

            Text("Tối ưu theo:", color = TextSecondary, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("time" to "Nhanh nhất", "cost" to "Rẻ nhất", "balanced" to "Cân bằng").forEach { (type, label) ->
                    Surface(
                        modifier = Modifier.clickable { selectedOptimization = type },
                        shape = RoundedCornerShape(8.dp),
                        color = if (selectedOptimization == type) PrimaryPurple else DarkCard
                    ) {
                        Text(label, color = if (selectedOptimization == type) Color.White else TextSecondary, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("Số điểm dừng: $waypointCount", color = TextSecondary, fontSize = 13.sp)
            Slider(
                value = waypointCount.toFloat(),
                onValueChange = { waypointCount = it.toInt() },
                valueRange = 2f..6f,
                steps = 3,
                colors = SliderDefaults.colors(thumbColor = PrimaryPurple, activeTrackColor = PrimaryPurple)
            )

            Spacer(modifier = Modifier.height(20.dp))

            GradientButton(
                text = "Tạo lịch trình",
                onClick = {
                    if (scheduleName.isNotEmpty()) {
                        val waypoints = (0 until waypointCount).map { index ->
                            val locations = listOf(
                                Triple("Trường ĐH Bách Khoa", 10.7629, 106.6604),
                                Triple("Vinmart Điện Biên Phủ", 10.7769, 106.7000),
                                Triple("Bến Thành Market", 10.7729, 106.6980),
                                Triple("Landmark 81", 10.7952, 106.7218),
                                Triple("Saigon Zoo", 10.7870, 106.7055),
                                Triple("Starbucks Diamond Plaza", 10.7798, 106.6952)
                            )
                            val loc = locations[index % locations.size]
                            WaypointDto(loc.second, loc.third, loc.first, loc.first, "stopover", 0, false)
                        }
                        onCreate(scheduleName, selectedDate, selectedOptimization, waypoints)
                    }
                },
                enabled = scheduleName.isNotEmpty()
            )
        }
    }
}

// ========== AI RECOMMENDATIONS ==========
@Composable
fun AIRecommendationsScreen(
    onBack: () -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }
    var recommendations by remember { mutableStateOf<AIRecommendationDto?>(null) }

    LaunchedEffect(Unit) {
        // Simulated recommendations
        kotlinx.coroutines.delay(1500)
        recommendations = AIRecommendationDto(
            frequentRoutes = listOf(
                FrequentRoute(5, "Trường ĐH Bách Khoa", "Vinmart Điện Biên Phủ", 35000.0),
                FrequentRoute(3, "Bến Thành Market", "Landmark 81", 45000.0)
            ),
            bestTimes = listOf("7:00-9:00", "17:00-19:00"),
            estimatedSavings = 8500,
            preferredTime = "7:00",
            aiConfidence = 0.92
        )
        isLoading = false
    }

    Box(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppTopBar(title = "Đề xuất AI", onBackClick = onBack)

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = PrimaryPurple)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("AI đang phân tích...", color = TextSecondary)
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
                ) {
                    recommendations?.let { rec ->
                        // AI confidence
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
                                        "${(rec.aiConfidence ?: 0.0) * 100}%",
                                        color = AccentGreen,
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Box(
                                    modifier = Modifier.size(60.dp).background(
                                        AccentGreen.copy(alpha = 0.2f),
                                        RoundedCornerShape(50)
                                    ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.AutoAwesome, null, tint = AccentGreen, modifier = Modifier.size(30.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Frequent routes
                        Text("Tuyến đường thường dùng", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))

                        rec.frequentRoutes?.forEach { route ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = DarkCard)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Route, null, tint = PrimaryPurple)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(route.pickup ?: "", color = TextPrimary, fontSize = 14.sp)
                                            Text("→ ${route.dest ?: ""}", color = TextSecondary, fontSize = 13.sp)
                                        }
                                        Spacer(modifier = Modifier.weight(1f))
                                        Text("${route.count}x", color = PrimaryPurple, fontWeight = FontWeight.Bold)
                                    }
                                    if (route.price != null) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            "Giá trung bình: ${String.format("%.0f", route.price).replace(",", ".")}đ",
                                            color = TextSecondary,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Estimated savings
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
                                    Icon(Icons.Default.Savings, null, tint = AccentGreen, modifier = Modifier.size(40.dp))
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text("Tiết kiệm ước tính", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            "${String.format("%.0f", rec.estimatedSavings).replace(",", ".")}đ / chuyến",
                                            color = AccentGreen,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ========== AI PROFILE ==========
@Composable
fun AIProfileScreen(
    onBack: () -> Unit
) {
    var preference by remember { mutableFloatStateOf(0.5f) }

    Box(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppTopBar(title = "Hồ sơ AI", onBackClick = onBack)

            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Tùy chỉnh AI", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
                        Text("Vị trí thường đến", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(12.dp))
                        listOf("Trường ĐH Bách Khoa", "Vinmart", "Landmark 81").forEach { loc ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Place, null, tint = PrimaryPurple)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(loc, color = TextPrimary)
                            }
                        }
                    }
                }
            }
        }
    }
}
