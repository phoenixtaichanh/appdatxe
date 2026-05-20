package com.laptrinhdidong.DoAn3.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laptrinhdidong.DoAn3.data.repository.PasswordResetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private val GradientColors = listOf(Color(0xFF667eea), Color(0xFF764ba2), Color(0xFFf093fb))

data class ForgotPasswordState(
    val isLoading: Boolean = false,
    val emailSent: Boolean = false,
    val errorMessage: String? = null,
    val devOtp: String? = null,
)

@dagger.hilt.android.lifecycle.HiltViewModel
class ForgotPasswordViewModel @javax.inject.Inject constructor(
    private val passwordResetRepository: PasswordResetRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ForgotPasswordState())
    val state: StateFlow<ForgotPasswordState> = _state.asStateFlow()

    fun sendOtp(email: String) {
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _state.value = _state.value.copy(errorMessage = "Please enter a valid email address")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            val result = passwordResetRepository.sendOtp(email)
            result.onSuccess { response ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    emailSent = true,
                    devOtp = response.devOtp,
                    errorMessage = response.message
                )
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to send OTP"
                )
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }
}

@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit,
    onEmailSent: (email: String, devOtp: String?) -> Unit,
    viewModel: ForgotPasswordViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var email by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.emailSent) {
        if (state.emailSent) {
            onEmailSent(email, state.devOtp)
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFF1a1a2e), Color(0xFF16213e), Color(0xFF0f3460)))
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.CenterHorizontally)
                    .background(
                        Brush.linearGradient(GradientColors),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Forgot Password?",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Enter your email address and we'll send you an OTP code to reset your password.",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            AnimatedTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email Address",
                icon = Icons.Default.Email,
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Email,
                imeAction = ImeAction.Done,
                onDone = { viewModel.sendOtp(email) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            GradientButton(
                text = if (state.isLoading) "Sending..." else "Send OTP",
                onClick = { viewModel.sendOtp(email) },
                enabled = email.isNotBlank(),
                isLoading = state.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "The OTP code will be valid for 10 minutes.",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        )
    }
}
