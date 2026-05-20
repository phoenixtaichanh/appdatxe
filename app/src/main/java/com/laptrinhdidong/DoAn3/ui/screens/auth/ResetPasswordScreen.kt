package com.laptrinhdidong.DoAn3.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
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

data class ResetPasswordState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
)

@dagger.hilt.android.lifecycle.HiltViewModel
class ResetPasswordViewModel @javax.inject.Inject constructor(
    private val passwordResetRepository: PasswordResetRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ResetPasswordState())
    val state: StateFlow<ResetPasswordState> = _state.asStateFlow()

    fun resetPassword(email: String, otp: String, newPassword: String) {
        if (newPassword.length < 6) {
            _state.value = _state.value.copy(errorMessage = "Password must be at least 6 characters")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            val result = passwordResetRepository.resetPassword(email, otp, newPassword)
            result.onSuccess {
                _state.value = _state.value.copy(isLoading = false, isSuccess = true)
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to reset password"
                )
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }
}

@Composable
fun ResetPasswordScreen(
    email: String,
    otp: String,
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: ResetPasswordViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onSuccess()
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
                text = "New Password",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Create a new secure password for your account.",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Password field
            PasswordField(
                value = password,
                onValueChange = { password = it; localError = null },
                label = "New Password",
                isVisible = passwordVisible,
                onToggleVisibility = { passwordVisible = !passwordVisible },
                error = if (password.isNotEmpty() && password.length < 6) "At least 6 characters" else null
            )

            if (password.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                PasswordStrengthIndicator(password = password)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm Password field
            PasswordField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it; localError = null },
                label = "Confirm Password",
                isVisible = confirmVisible,
                onToggleVisibility = { confirmVisible = !confirmVisible },
                error = if (confirmPassword.isNotEmpty() && confirmPassword != password)
                    "Passwords do not match" else null
            )

            if (confirmPassword.isNotEmpty() && confirmPassword == password) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color(0xFF00C853),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Passwords match",
                        fontSize = 12.sp,
                        color = Color(0xFF00C853)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            GradientButton(
                text = if (state.isLoading) "Resetting..." else "Reset Password",
                onClick = {
                    if (password != confirmPassword) {
                        localError = "Passwords do not match"
                        return@GradientButton
                    }
                    viewModel.resetPassword(email, otp, password)
                },
                enabled = password.isNotBlank() && confirmPassword.isNotBlank(),
                isLoading = state.isLoading
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

@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isVisible: Boolean,
    onToggleVisibility: () -> Unit,
    error: String? = null
) {
    var isFocused by remember { mutableStateOf(false) }

    val borderColor by animateColorAsState(
        targetValue = when {
            error != null -> Color(0xFFff6b6b)
            isFocused -> Color(0xFF667eea)
            else -> Color.White.copy(alpha = 0.3f)
        },
        animationSpec = androidx.compose.animation.core.tween(300),
        label = "borderColor"
    )

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = if (isFocused) Color(0xFF667eea) else Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 12.dp),
                    textStyle = LocalTextStyle.current.copy(
                        color = Color.White,
                        fontSize = 16.sp
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(Color(0xFF667eea)),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = if (isVisible)
                            androidx.compose.ui.text.input.KeyboardType.Text
                        else
                            androidx.compose.ui.text.input.KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    visualTransformation = if (!isVisible)
                        androidx.compose.ui.text.input.PasswordVisualTransformation()
                    else
                        androidx.compose.ui.text.input.VisualTransformation.None,
                    decorationBox = { innerTextField ->
                        Box {
                            if (value.isEmpty()) {
                                Text(
                                    text = label,
                                    fontSize = 16.sp,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            }
                            innerTextField()
                        }
                    }
                )
                IconButton(onClick = onToggleVisibility) {
                    Icon(
                        imageVector = if (isVisible)
                            Icons.Filled.VisibilityOff
                        else
                            Icons.Filled.Visibility,
                        contentDescription = if (isVisible) "Hide" else "Show",
                        tint = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = error != null,
            enter = fadeIn() + androidx.compose.animation.expandVertically(),
            exit = fadeOut() + androidx.compose.animation.shrinkVertically()
        ) {
            Text(
                text = error ?: "",
                color = Color(0xFFff6b6b),
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}
