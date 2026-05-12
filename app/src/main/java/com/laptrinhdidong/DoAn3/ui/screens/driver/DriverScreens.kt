package com.laptrinhdidong.DoAn3.ui.screens.driver

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
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
import com.laptrinhdidong.DoAn3.data.remote.dto.BatchDto
import com.laptrinhdidong.DoAn3.data.remote.dto.EarningsDto
import com.laptrinhdidong.DoAn3.data.repository.DriverRepository
import com.laptrinhdidong.DoAn3.ui.components.*
import com.laptrinhdidong.DoAn3.ui.theme.*
import kotlinx.coroutines.launch

data class EarningsState(
    val isLoading: Boolean = false,
    val earnings: EarningsDto? = null,
    val weeklyData: List<Pair<String, Double>> = emptyList(),
    val errorMessage: String? = null
)

@dagger.hilt.android.lifecycle.HiltViewModel
class EarningsViewModel @javax.inject.Inject constructor(
    private val repository: DriverRepository
) : ViewModel() {

    private val _state = androidx.compose.runtime.mutableStateOf<EarningsState>(EarningsState())
    val state: androidx.compose.runtime.State<EarningsState> = _state

    init { loadEarnings() }

    fun loadEarnings() {
        viewModelScope.launch {
            _state.value = EarningsState(isLoading = true)
            val today = java.time.LocalDate.now()
            val weekStart = today.minusDays(6)

            repository.getEarnings(weekStart.toString(), today.toString()).onSuccess { earnings ->
                val weeklyTotal = earnings.weekEarnings
                val avgPerDay = if (weeklyTotal > 0) weeklyTotal / 7.0 else 0.0
                val weeklyData = (0..6).map { offset ->
                    val day = today.minusDays((6 - offset).toLong())
                    Pair(day.toString().takeLast(5), avgPerDay)
                }
                _state.value = EarningsState(
                    isLoading = false,
                    earnings = earnings,
                    weeklyData = weeklyData
                )
            }.onFailure {
                val today = java.time.LocalDate.now()
                _state.value = EarningsState(
                    isLoading = false,
                    weeklyData = (0..6).map { offset ->
                        val day = today.minusDays((6 - offset).toLong())
                        Pair(day.toString().takeLast(5), 0.0)
                    }
                )
            }
        }
    }
}

@Composable
fun EarningsScreen(
    onBack: () -> Unit,
    viewModel: EarningsViewModel = hiltViewModel()
) {
    val state = viewModel.state.value

    Box(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppTopBar(title = "Thu nhập", onBackClick = onBack)

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
                    // Summary card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkCard)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(
                                        listOf(PrimaryPurple, PrimaryPurpleDark)
                                    )
                                )
                                .padding(24.dp)
                        ) {
                            Text("Thu nhập hôm nay", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${String.format("%.0f", state.earnings?.todayEarnings ?: 0.0).replace(",", ".")}đ",
                                color = Color.White,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${state.earnings?.totalRides ?: 0}",
                                        color = Color.White,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text("Chuyến", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${String.format("%.0f", state.earnings?.weekEarnings ?: 0.0).replace(",", ".")}đ",
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text("Tuần này", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${String.format("%.0f", state.earnings?.monthEarnings ?: 0.0).replace(",", ".")}đ",
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text("Tháng này", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Stats
                    Text("Thống kê", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            icon = Icons.Default.DirectionsCar,
                            label = "Tổng chuyến",
                            value = "${state.earnings?.totalRides ?: 0}",
                            color = AccentBlue,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            icon = Icons.AutoMirrored.Filled.TrendingUp,
                            label = "Tổng thu",
                            value = "${String.format("%.0f", state.earnings?.totalEarnings ?: 0.0).replace(",", ".")}đ",
                            color = AccentGreen,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Weekly chart
                    Text("Thu nhập 7 ngày", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkCard)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            EarningsBarChart(
                                data = state.weeklyData,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Today's breakdown
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkCard)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("Chi tiết hôm nay", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(16.dp))

                            InfoRow(Icons.Default.Today, "Số chuyến", "${state.earnings?.totalRides ?: 0}")
                            InfoRow(Icons.Default.AttachMoney, "Thu nhập hôm nay", "${String.format("%.0f", state.earnings?.todayEarnings ?: 0.0).replace(",", ".")}đ", valueColor = AccentGreen)
                            InfoRow(Icons.Default.Schedule, "Tuần này", "${String.format("%.0f", state.earnings?.weekEarnings ?: 0.0).replace(",", ".")}đ", valueColor = AccentBlue)
                            InfoRow(Icons.Default.CalendarMonth, "Tháng này", "${String.format("%.0f", state.earnings?.monthEarnings ?: 0.0).replace(",", ".")}đ", valueColor = PrimaryPurple)
                        }
                    }
                }
            }
        }

        if (state.isLoading) {
            LoadingOverlay()
        }
    }
}

@Composable
private fun StatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(value, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(label, color = TextSecondary, fontSize = 12.sp)
        }
    }
}

@Composable
private fun EarningsBarChart(
    data: List<Pair<String, Double>>,
    modifier: Modifier = Modifier
) {
    val maxValue = data.maxOfOrNull { it.second }?.coerceAtLeast(1.0) ?: 1.0

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { (day, amount) ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (amount > 0) "${String.format("%.0f", amount / 1000)}k" else "0",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    modifier = Modifier.height(16.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                val barHeight = ((amount / maxValue) * 100).coerceIn(4.0, 100.0)
                Box(
                    modifier = Modifier
                        .width(28.dp)
                        .height(barHeight.dp)
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(PrimaryPurple, PrimaryPurpleDark)
                            )
                        )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = day.takeLast(2),
                    color = TextSecondary,
                    fontSize = 10.sp
                )
            }
        }
    }
}

// ========== BATCH OFFER SCREEN ==========
@Composable
fun BatchOfferScreen(
    onBack: () -> Unit,
    onBatchClick: (Int) -> Unit
) {
    var batches by remember { mutableStateOf<List<BatchDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(1000)
        batches = emptyList()
        isLoading = false
    }

    Box(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppTopBar(title = "Đề xuất ghép chuyến", onBackClick = onBack)

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryPurple)
                }
            } else if (batches.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.Group,
                    title = "Không có batch",
                    subtitle = "Không có đề xuất ghép chuyến nào cho bạn",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    batches.forEach { batch ->
                        BatchDetailCard(batch = batch, onClick = { onBatchClick(batch.id) })
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun BatchDetailCard(batch: BatchDto, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
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
                    text = batch.batchName ?: "Batch #${batch.id}",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AccentBlue.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "${batch.passengerCount ?: 0} khách",
                        color = AccentBlue,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${String.format("%.0f", batch.totalRevenue ?: 0.0).replace(",", ".")}đ",
                        color = AccentGreen,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("Doanh thu", color = TextSecondary, fontSize = 12.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${batch.totalDistance ?: 0.0}km",
                        color = PrimaryPurple,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("Quãng đường", color = TextSecondary, fontSize = 12.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${((batch.efficiencyScore ?: 0.0) * 100).toInt()}%",
                        color = AccentYellow,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("Hiệu quả", color = TextSecondary, fontSize = 12.sp)
                }
            }

            if (batch.passengers != null && batch.passengers!!.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(12.dp))

                Text("Hành khách trong batch:", color = TextSecondary, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))

                batch.passengers!!.forEach { passenger ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Person,
                            null,
                            tint = PrimaryPurple,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(passenger.passengerName ?: "Khách #${passenger.passengerId}", color = TextPrimary, fontSize = 14.sp)
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            "Đoạn: ${passenger.detourKm ?: 0.0}km",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            GradientButton(
                text = "Chấp nhận Batch",
                onClick = onClick,
                gradient = listOf(AccentBlue, AccentBlue.copy(alpha = 0.7f))
            )
        }
    }
}
