package com.laptrinhdidong.DoAn3.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laptrinhdidong.DoAn3.data.local.SessionManager
import com.laptrinhdidong.DoAn3.data.remote.dto.RideDto
import com.laptrinhdidong.DoAn3.data.repository.DriverRepository
import com.laptrinhdidong.DoAn3.data.repository.RideRepository
import com.laptrinhdidong.DoAn3.ui.components.*
import com.laptrinhdidong.DoAn3.ui.theme.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HistoryState(
    val isLoading: Boolean = false,
    val rides: List<RideDto> = emptyList(),
    val selectedFilter: String = "all",
    val searchQuery: String = "",
    val errorMessage: String? = null
)

@dagger.hilt.android.lifecycle.HiltViewModel
class HistoryViewModel @javax.inject.Inject constructor(
    private val rideRepository: RideRepository,
    private val driverRepository: DriverRepository,
    private val sessionManager: com.laptrinhdidong.DoAn3.data.local.SessionManager
) : ViewModel() {
    private val isDriver: Boolean get() = sessionManager.isDriver

    private val _state = MutableStateFlow(HistoryState())
    val state: StateFlow<HistoryState> = _state.asStateFlow()

    init { loadHistory() }

    fun loadHistory() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val result = if (isDriver) driverRepository.getDriverHistory() else rideRepository.getRideHistory()
            result.onSuccess { rides ->
                _state.value = _state.value.copy(isLoading = false, rides = rides)
            }.onFailure {
                _state.value = _state.value.copy(isLoading = false, errorMessage = "Không thể tải lịch sử")
            }
        }
    }

    fun setFilter(filter: String) {
        _state.value = _state.value.copy(selectedFilter = filter)
    }

    fun setSearchQuery(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
    }
}

@Composable
fun HistoryScreen(
    isDriver: Boolean,
    onBack: () -> Unit,
    onRideClick: (Int) -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppTopBar(
                title = if (isDriver) "Lich su chuyen" else "Lich su dat xe",
                onBackClick = onBack
            )

            // Search bar
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = {
                    Text("Tim kiem dia diem...", color = TextSecondary, fontSize = 14.sp)
                },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = TextSecondary, modifier = Modifier.size(20.dp)) },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryPurple,
                    unfocusedBorderColor = DarkCard,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = PrimaryPurple,
                    focusedContainerColor = DarkCard,
                    unfocusedContainerColor = DarkCard
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
            )

            // Filter tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkCard)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("all" to "Tất cả", "completed" to "Hoàn thành", "cancelled" to "Đã hủy").forEach { (filter, label) ->
                    val isSelected = state.selectedFilter == filter
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) PrimaryPurple else Color.Transparent)
                            .clickable { viewModel.setFilter(filter) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color.White else TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }

            val filteredRides = state.rides.filter { ride ->
                val matchFilter = state.selectedFilter == "all" || ride.status == state.selectedFilter
                val query = state.searchQuery.lowercase()
                val matchQuery = query.isEmpty() ||
                    ride.pickupAddress.lowercase().contains(query) ||
                    ride.destAddress.lowercase().contains(query) ||
                    (ride.driverName?.lowercase()?.contains(query) == true) ||
                    (ride.passengerName?.lowercase()?.contains(query) == true)
                matchFilter && matchQuery
            }

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryPurple)
                }
            } else if (filteredRides.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.History,
                    title = "Chưa có chuyến đi",
                    subtitle = "Các chuyến đi của bạn sẽ hiển thị ở đây",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    filteredRides.forEach { ride ->
                        RideCard(
                            pickupAddress = ride.pickupAddress,
                            destAddress = ride.destAddress,
                            price = ride.price,
                            status = ride.status,
                            createdAt = ride.createdAt,
                            driverName = ride.driverName,
                            passengerName = ride.passengerName,
                            isDriverView = isDriver,
                            onClick = { onRideClick(ride.id) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
        )
    }
}
