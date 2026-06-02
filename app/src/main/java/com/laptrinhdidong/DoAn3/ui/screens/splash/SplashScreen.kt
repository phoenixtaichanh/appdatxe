package com.laptrinhdidong.DoAn3.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.laptrinhdidong.DoAn3.data.local.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

sealed class SplashDestination {
    data object Loading : SplashDestination()
    data class Auth(val message: String? = null) : SplashDestination()
    data class Home(val userType: String) : SplashDestination()
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {
    private val _destination = MutableStateFlow<SplashDestination>(SplashDestination.Loading)
    val destination: StateFlow<SplashDestination> = _destination

    suspend fun checkSession() {
        delay(2000)
        val isLoggedIn = sessionManager.isLoggedIn
        val userType = sessionManager.userType
        _destination.value = if (!isLoggedIn) {
            SplashDestination.Auth()
        } else {
            when (userType) {
                "driver" -> SplashDestination.Home("driver")
                "consultant" -> SplashDestination.Home("consultant")
                "owner", "admin", "revenue_manager" -> SplashDestination.Home("admin")
                else -> SplashDestination.Home("passenger")
            }
        }
    }
}

@Composable
fun SplashScreen(
    onNavigateToAuth: () -> Unit,
    onNavigateToHome: (String) -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val destination by viewModel.destination.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.checkSession()
    }

    LaunchedEffect(destination) {
        when (val dest = destination) {
            is SplashDestination.Auth -> onNavigateToAuth()
            is SplashDestination.Home -> onNavigateToHome(dest.userType)
            is SplashDestination.Loading -> {}
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "splash")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1a1a2e),
                        Color(0xFF16213e),
                        Color(0xFF0f3460)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "🚗",
                fontSize = 72.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Text(
                text = "DoAn3",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Ứng dụng đặt xe thông minh",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(48.dp))

            CircularProgressIndicator(
                modifier = Modifier.size(40.dp),
                color = Color(0xFF667eea),
                strokeWidth = 3.dp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Đang khởi động...",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = alpha)
            )
        }

        Text(
            text = "v1.0.0",
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.3f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        )
    }
}
