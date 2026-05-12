package com.laptrinhdidong.DoAn3.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
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
import com.laptrinhdidong.DoAn3.data.local.SessionManager
import com.laptrinhdidong.DoAn3.data.remote.dto.DriverDto
import com.laptrinhdidong.DoAn3.data.remote.dto.UserDto
import com.laptrinhdidong.DoAn3.data.repository.AuthRepository
import com.laptrinhdidong.DoAn3.data.repository.DriverRepository
import com.laptrinhdidong.DoAn3.data.repository.RideRepository
import com.laptrinhdidong.DoAn3.data.remote.SocketManager
import com.laptrinhdidong.DoAn3.ui.components.*
import com.laptrinhdidong.DoAn3.ui.theme.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileState(
    val isLoading: Boolean = false,
    val user: UserDto? = null,
    val driver: DriverDto? = null,
    val userName: String = "",
    val userEmail: String = "",
    val userType: String = "",
    val totalRides: Int = 0,
    val isEditing: Boolean = false,
    val editName: String = "",
    val editPhone: String = "",
    val editCarModel: String = "",
    val editCarColor: String = "",
    val editLicensePlate: String = "",
    val errorMessage: String? = null
)

@dagger.hilt.android.lifecycle.HiltViewModel
class ProfileViewModel @javax.inject.Inject constructor(
    private val sessionManager: SessionManager,
    private val authRepository: AuthRepository,
    private val rideRepository: RideRepository,
    private val driverRepository: DriverRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val userId = sessionManager.userId
            val userType = sessionManager.userType

            _state.value = _state.value.copy(
                userName = sessionManager.userName ?: "",
                userEmail = sessionManager.userEmail ?: "",
                userType = userType ?: ""
            )

            if (userType == "driver") {
                driverRepository.getDriverProfile().onSuccess { driver ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        driver = driver,
                        editName = driver.name ?: "",
                        editPhone = driver.phone ?: "",
                        editCarModel = driver.carModel ?: "",
                        editCarColor = driver.carColor ?: "",
                        editLicensePlate = driver.licensePlate ?: ""
                    )
                }.onFailure {
                    _state.value = _state.value.copy(isLoading = false)
                }
            } else {
                rideRepository.getRideHistory().onSuccess { rides ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        totalRides = rides.size
                    )
                }.onFailure {
                    _state.value = _state.value.copy(isLoading = false)
                }
            }

            rideRepository.getRideHistory().onSuccess { rides ->
                _state.value = _state.value.copy(totalRides = rides.size)
            }
        }
    }

    fun startEditing() {
        val driver = _state.value.driver
        _state.value = _state.value.copy(
            isEditing = true,
            editName = driver?.name ?: "",
            editPhone = driver?.phone ?: "",
            editCarModel = driver?.carModel ?: "",
            editCarColor = driver?.carColor ?: "",
            editLicensePlate = driver?.licensePlate ?: ""
        )
    }

    fun cancelEditing() {
        _state.value = _state.value.copy(isEditing = false)
    }

    fun updateEditField(field: String, value: String) {
        _state.value = when (field) {
            "name" -> _state.value.copy(editName = value)
            "phone" -> _state.value.copy(editPhone = value)
            "carModel" -> _state.value.copy(editCarModel = value)
            "carColor" -> _state.value.copy(editCarColor = value)
            "licensePlate" -> _state.value.copy(editLicensePlate = value)
            else -> _state.value
        }
    }

    fun saveProfile() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val state = _state.value

            driverRepository.updateDriverProfile(
                name = state.editName.ifEmpty { sessionManager.userName ?: "" },
                phone = state.editPhone.takeIf { it.isNotEmpty() },
                carModel = state.editCarModel.takeIf { it.isNotEmpty() },
                carColor = state.editCarColor.takeIf { it.isNotEmpty() },
                licensePlate = state.editLicensePlate.takeIf { it.isNotEmpty() }
            ).onSuccess {
                _state.value = _state.value.copy(isLoading = false, isEditing = false)
                loadProfile()
            }.onFailure {
                _state.value = _state.value.copy(isLoading = false, errorMessage = "Lưu thất bại")
            }
        }
    }

    fun logout(onLogout: () -> Unit) {
        SocketManager.disconnect()
        sessionManager.clearSession()
        onLogout()
    }
}

@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
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
            AppTopBar(title = "Hồ sơ", onBackClick = onBack)

            if (state.isLoading && state.driver == null) {
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
                    // Profile header
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkCard)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(GradientPrimary)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (state.driver?.name ?: state.userName).firstOrNull()?.uppercase()?.toString() ?: "U",
                                    color = Color.White,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = state.driver?.name ?: state.userName.ifEmpty { "Nguoi dung" },
                                color = TextPrimary,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (state.userType == "driver") "Tài xế" else "Hành khách",
                                color = PrimaryPurple,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = String.format("%.1f", state.driver?.rating ?: 5.0),
                                        color = TextPrimary,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        repeat(5) {
                                            Icon(
                                                Icons.Filled.Star, null,
                                                tint = AccentYellow,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                    Text("Đánh giá", color = TextSecondary, fontSize = 12.sp)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${state.totalRides}",
                                        color = TextPrimary,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text("Chuyến đi", color = TextSecondary, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Edit form or info
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
                                Text("Thông tin cá nhân", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                                if (!state.isEditing) {
                                    TextButton(onClick = { viewModel.startEditing() }) {
                                        Text("Chỉnh sửa", color = PrimaryPurple)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            if (state.isEditing) {
                                AppTextField(
                                    value = state.editName,
                                    onValueChange = { viewModel.updateEditField("name", it) },
                                    label = "Họ tên",
                                    icon = Icons.Default.Person
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                AppTextField(
                                    value = state.editPhone,
                                    onValueChange = { viewModel.updateEditField("phone", it) },
                                    label = "Số điện thoại",
                                    icon = Icons.Default.Phone
                                )

                                if (state.userType == "driver") {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("Thông tin xe", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    AppTextField(
                                        value = state.editCarModel,
                                        onValueChange = { viewModel.updateEditField("carModel", it) },
                                        label = "Mẫu xe",
                                        icon = Icons.Default.DirectionsCar
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    AppTextField(
                                        value = state.editCarColor,
                                        onValueChange = { viewModel.updateEditField("carColor", it) },
                                        label = "Màu xe",
                                        icon = Icons.Default.Palette
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    AppTextField(
                                        value = state.editLicensePlate,
                                        onValueChange = { viewModel.updateEditField("licensePlate", it) },
                                        label = "Biển số",
                                        icon = Icons.Default.Badge
                                    )
                                }

                                Spacer(modifier = Modifier.height(20.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    OutlinedButton(
                                        onClick = { viewModel.cancelEditing() },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Hủy")
                                    }
                                    GradientButton(
                                        text = "Lưu",
                                        onClick = { viewModel.saveProfile() },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            } else {
                                val displayName = state.driver?.name ?: state.userName.ifEmpty { "Nguoi dung" }
                                InfoRow(Icons.Default.Person, "Ho ten", displayName)
                                InfoRow(Icons.Default.Email, "Email", state.userEmail.ifEmpty { "Chua cap nhat" })
                                InfoRow(Icons.Default.Phone, "Dien thoai", state.driver?.phone ?: "Chua cap nhat")

                                if (state.userType == "driver") {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("Thông tin xe", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    InfoRow(Icons.Default.DirectionsCar, "Mẫu xe", state.driver?.carModel ?: "Chưa cập nhật")
                                    InfoRow(Icons.Default.Palette, "Màu xe", state.driver?.carColor ?: "Chưa cập nhật")
                                    InfoRow(Icons.Default.Badge, "Biển số", state.driver?.licensePlate ?: "Chưa cập nhật")
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Settings
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkCard)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            SettingsItem(Icons.Default.Notifications, "Thông báo", onClick = {})
                            SettingsItem(Icons.Default.Security, "Bảo mật", onClick = {})
                            SettingsItem(Icons.AutoMirrored.Filled.Help, "Hỗ trợ", onClick = {})
                            SettingsItem(Icons.Default.Info, "Về ứng dụng", onClick = {})
                            SettingsItem(Icons.AutoMirrored.Filled.Logout, "Đăng xuất", onClick = { viewModel.logout(onLogout) }, tint = AccentRed)
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }

        if (state.isLoading && state.driver != null) {
            LoadingOverlay()
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
        )
    }
}

@Composable
private fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit,
    tint: Color = PrimaryPurple
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text, color = TextPrimary, fontSize = 15.sp, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, null, tint = TextSecondary)
    }
}
