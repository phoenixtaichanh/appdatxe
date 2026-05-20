package com.laptrinhdidong.DoAn3.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laptrinhdidong.DoAn3.data.repository.PasswordResetRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private val GradientColors = listOf(Color(0xFF667eea), Color(0xFF764ba2), Color(0xFFf093fb))

data class OtpVerificationState(
    val isLoading: Boolean = false,
    val isVerified: Boolean = false,
    val errorMessage: String? = null,
    val resendCountdown: Int = 0,
    val resendLoading: Boolean = false,
)

@dagger.hilt.android.lifecycle.HiltViewModel
class OtpVerificationViewModel @javax.inject.Inject constructor(
    private val passwordResetRepository: PasswordResetRepository
) : ViewModel() {

    private val _state = MutableStateFlow(OtpVerificationState())
    val state: StateFlow<OtpVerificationState> = _state.asStateFlow()

    private var countdownJob: Job? = null

    fun startCountdown() {
        countdownJob?.cancel()
        _state.value = _state.value.copy(resendCountdown = 60)
        countdownJob = viewModelScope.launch {
            while (_state.value.resendCountdown > 0) {
                delay(1000)
                _state.value = _state.value.copy(resendCountdown = _state.value.resendCountdown - 1)
            }
        }
    }

    fun verifyOtp(email: String, otp: String) {
        if (otp.length != 6) {
            _state.value = _state.value.copy(errorMessage = "Please enter the 6-digit OTP code")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            val result = passwordResetRepository.verifyOtp(email, otp)
            result.onSuccess {
                _state.value = _state.value.copy(isLoading = false, isVerified = true)
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Invalid OTP"
                )
            }
        }
    }

    fun resendOtp(email: String) {
        if (_state.value.resendCountdown > 0) return

        viewModelScope.launch {
            _state.value = _state.value.copy(resendLoading = true, errorMessage = null)
            val result = passwordResetRepository.resendOtp(email)
            result.onSuccess {
                _state.value = _state.value.copy(resendLoading = false, errorMessage = it.message)
                startCountdown()
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    resendLoading = false,
                    errorMessage = e.message ?: "Failed to resend OTP"
                )
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }
}

@Composable
fun OtpVerificationScreen(
    email: String,
    devOtp: String?,
    onBack: () -> Unit,
    onVerified: (email: String, otp: String) -> Unit,
    viewModel: OtpVerificationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var otpCode by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val focusRequester = remember { FocusRequester() }

    // If devOtp exists (development mode), auto-fill and show hint
    LaunchedEffect(devOtp) {
        if (!devOtp.isNullOrEmpty()) {
            otpCode = devOtp
        }
        viewModel.startCountdown()
    }

    LaunchedEffect(state.isVerified) {
        if (state.isVerified) {
            onVerified(email, otpCode)
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

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.CenterHorizontally)
                    .background(Brush.linearGradient(GradientColors), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Verify OTP",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Enter the 6-digit code sent to\n$email",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            if (!devOtp.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "[DEV] OTP auto-filled: $devOtp",
                    fontSize = 12.sp,
                    color = Color(0xFF00ff00),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // OTP Input fields
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(6) { index ->
                    val char = otpCode.getOrNull(index)?.toString() ?: ""
                    OtpDigitBox(
                        char = char,
                        isFocused = otpCode.length == index,
                        onDigitEntered = { digit ->
                            if (otpCode.length <= index) {
                                val newOtp = otpCode + digit
                                otpCode = newOtp
                                if (newOtp.length == 6) {
                                    viewModel.verifyOtp(email, newOtp)
                                }
                            }
                        },
                        onBackspace = {
                            if (otpCode.isNotEmpty()) {
                                otpCode = otpCode.dropLast(1)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            LaunchedEffect(otpCode.length) {
                if (otpCode.length == 6) {
                    // verifyOtp is called automatically above
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            GradientButton(
                text = if (state.isLoading) "Verifying..." else "Verify",
                onClick = { viewModel.verifyOtp(email, otpCode) },
                enabled = otpCode.length == 6 && !state.isLoading,
                isLoading = state.isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (state.resendCountdown > 0)
                        "Resend OTP in ${state.resendCountdown}s"
                    else
                        "Didn't receive the code?",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )

                if (state.resendCountdown == 0) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (state.resendLoading) "Sending..." else "Resend OTP",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF667eea),
                        modifier = Modifier
                            .clickable(enabled = !state.resendLoading) {
                                viewModel.resendOtp(email)
                            }
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        )
    }
}

@Composable
private fun OtpDigitBox(
    char: String,
    isFocused: Boolean,
    onDigitEntered: (String) -> Unit,
    onBackspace: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentChar by remember { mutableStateOf(char) }

    LaunchedEffect(char) {
        currentChar = char
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(
                Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) Color(0xFF667eea) else Color.White.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            value = currentChar,
            onValueChange = { value ->
                if (value.length <= 1 && value.all { it.isDigit() }) {
                    if (value.isNotEmpty()) {
                        onDigitEntered(value)
                    }
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .focusRequester(FocusRequester()),
            textStyle = LocalTextStyle.current.copy(
                textAlign = TextAlign.Center,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            singleLine = true,
            cursorBrush = SolidColor(Color(0xFF667eea)),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (currentChar.isEmpty()) {
                        innerTextField()
                    }
                }
            }
        )
    }
}
