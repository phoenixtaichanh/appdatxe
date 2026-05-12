package com.laptrinhdidong.DoAn3.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.laptrinhdidong.DoAn3.data.repository.AuthRepository

// ============ THEME COLORS ============
private val GradientColors = listOf(
    Color(0xFF667eea),
    Color(0xFF764ba2),
    Color(0xFFf093fb)
)

// ============ AUTH STATE ============
data class AuthState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val userType: String = "passenger"
)

data class LoginFormState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null
)

data class RegisterFormState(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val nameError: String? = null,
    val emailError: String? = null,
    val phoneError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null
)

// ============ AUTH VIEWMODEL ============
@dagger.hilt.android.lifecycle.HiltViewModel
class AuthViewModel @javax.inject.Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _loginForm = MutableStateFlow(LoginFormState())
    val loginForm: StateFlow<LoginFormState> = _loginForm.asStateFlow()

    private val _registerForm = MutableStateFlow(RegisterFormState())
    val registerForm: StateFlow<RegisterFormState> = _registerForm.asStateFlow()

    fun updateLoginEmail(email: String) {
        _loginForm.value = _loginForm.value.copy(email = email, emailError = null)
    }

    fun updateLoginPassword(password: String) {
        _loginForm.value = _loginForm.value.copy(password = password, passwordError = null)
    }

    fun updateRegisterName(name: String) {
        _registerForm.value = _registerForm.value.copy(name = name, nameError = null)
    }

    fun updateRegisterEmail(email: String) {
        _registerForm.value = _registerForm.value.copy(email = email, emailError = null)
    }

    fun updateRegisterPhone(phone: String) {
        _registerForm.value = _registerForm.value.copy(phone = phone, phoneError = null)
    }

    fun updateRegisterPassword(password: String) {
        _registerForm.value = _registerForm.value.copy(password = password, passwordError = null)
    }

    fun updateRegisterConfirmPassword(confirmPassword: String) {
        _registerForm.value = _registerForm.value.copy(confirmPassword = confirmPassword, confirmPasswordError = null)
    }

    fun updateUserType(userType: String) {
        _authState.value = _authState.value.copy(userType = userType)
    }

    fun login() {
        val form = _loginForm.value
        var hasError = false
        var newForm = form

        if (form.email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(form.email).matches()) {
            newForm = newForm.copy(emailError = "Please enter a valid email")
            hasError = true
        }
        if (form.password.length < 6) {
            newForm = newForm.copy(passwordError = "Password must be at least 6 characters")
            hasError = true
        }

        if (hasError) {
            _loginForm.value = newForm
            return
        }

        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, errorMessage = null)
            val result = authRepository.login(form.email, form.password)
            result.onSuccess { response ->
                _authState.value = _authState.value.copy(isLoading = false, isSuccess = true)
            }.onFailure { e ->
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Login failed"
                )
            }
        }
    }

    fun register() {
        val form = _registerForm.value
        val auth = _authState.value
        var hasError = false
        var newForm = form

        if (form.name.isBlank()) { newForm = newForm.copy(nameError = "Name is required"); hasError = true }
        if (form.email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(form.email).matches()) {
            newForm = newForm.copy(emailError = "Please enter a valid email"); hasError = true
        }
        if (form.phone.isBlank() || form.phone.length < 10) {
            newForm = newForm.copy(phoneError = "Please enter a valid phone number"); hasError = true
        }
        if (form.password.length < 6) { newForm = newForm.copy(passwordError = "Password must be at least 6 characters"); hasError = true }
        if (form.password != form.confirmPassword) { newForm = newForm.copy(confirmPasswordError = "Passwords do not match"); hasError = true }

        if (hasError) { _registerForm.value = newForm; return }

        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, errorMessage = null)
            val result = authRepository.register(form.name, form.email, form.password, form.phone, auth.userType)
            result.onSuccess { response ->
                _authState.value = _authState.value.copy(isLoading = false, isSuccess = true)
            }.onFailure { e ->
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Registration failed"
                )
            }
        }
    }

    fun clearError() { _authState.value = _authState.value.copy(errorMessage = null) }
    fun resetState() { _authState.value = AuthState(); _loginForm.value = LoginFormState(); _registerForm.value = RegisterFormState() }
}

// ============ MAIN AUTH SCREEN ============
@Composable
fun AuthScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onAuthSuccess: (String, String) -> Unit = { _, _ -> }
) {
    val authState by viewModel.authState.collectAsState()
    val loginForm by viewModel.loginForm.collectAsState()
    val registerForm by viewModel.registerForm.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(authState.isSuccess) {
        if (authState.isSuccess) {
            onAuthSuccess(authState.userType, "")
        }
    }

    LaunchedEffect(authState.errorMessage) {
        authState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(60.dp))
                LogoSection()
                Spacer(modifier = Modifier.height(40.dp))
                AnimatedTabSwitcher(selectedTab = selectedTab, onTabSelected = { selectedTab = it })
                Spacer(modifier = Modifier.height(32.dp))

                AnimatedVisibility(visible = selectedTab == 1, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                    Column {
                        UserTypeSelector(selectedType = authState.userType, onTypeSelected = { viewModel.updateUserType(it) })
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                AnimatedContent(targetState = selectedTab, transitionSpec = {
                    fadeIn(animationSpec = tween(300)) + slideInHorizontally(
                        initialOffsetX = { if (targetState == 0) it else -it },
                        animationSpec = tween(300)
                    ) togetherWith fadeOut(animationSpec = tween(300)) + slideOutHorizontally(
                        targetOffsetX = { if (targetState == 0) -it else it },
                        animationSpec = tween(300)
                    )
                }, label = "tabContent") { tab ->
                    if (tab == 0) {
                        LoginForm(email = loginForm.email, onEmailChange = viewModel::updateLoginEmail, emailError = loginForm.emailError,
                            password = loginForm.password, onPasswordChange = viewModel::updateLoginPassword, passwordError = loginForm.passwordError,
                            onLoginClick = viewModel::login, isLoading = authState.isLoading)
                    } else {
                        RegisterForm(form = registerForm, userType = authState.userType,
                            onNameChange = viewModel::updateRegisterName, onEmailChange = viewModel::updateRegisterEmail,
                            onPhoneChange = viewModel::updateRegisterPhone, onPasswordChange = viewModel::updateRegisterPassword,
                            onConfirmPasswordChange = viewModel::updateRegisterConfirmPassword, onRegisterClick = viewModel::register,
                            isLoading = authState.isLoading)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                AnimatedDivider()
                Spacer(modifier = Modifier.height(24.dp))
                SocialLoginSection()
                Spacer(modifier = Modifier.height(32.dp))
                TermsText()
            }
        }

        AnimatedVisibility(visible = authState.isLoading, enter = fadeIn(), exit = fadeOut()) {
            LoadingOverlay(message = if (selectedTab == 0) "Logging in..." else "Creating account...")
        }

        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp))
    }
}

// ============ ANIMATED BACKGROUND ============
@Composable
fun AnimatedBackground(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "background")
    val animatedOffset1 by infiniteTransition.animateFloat(initialValue = 0f, targetValue = 1000f,
        animationSpec = infiniteRepeatable(animation = tween(20000, easing = LinearEasing), repeatMode = RepeatMode.Restart), label = "offset1")
    val animatedOffset2 by infiniteTransition.animateFloat(initialValue = 0f, targetValue = 800f,
        animationSpec = infiniteRepeatable(animation = tween(25000, easing = LinearEasing), repeatMode = RepeatMode.Restart), label = "offset2")

    Box(modifier = modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF1a1a2e), Color(0xFF16213e), Color(0xFF0f3460))))) {
        Box(modifier = Modifier.size(300.dp).offset(x = (-100 + (animatedOffset1 % 500)).dp, y = (100 + (animatedOffset1 % 300)).dp)
            .clip(RoundedCornerShape(50)).background(Brush.radialGradient(listOf(Color(0x40667eea), Color.Transparent))))
        Box(modifier = Modifier.size(250.dp).offset(x = (200 + (animatedOffset2 % 400)).dp, y = (400 + (animatedOffset2 % 200)).dp)
            .clip(RoundedCornerShape(50)).background(Brush.radialGradient(listOf(Color(0x40f093fb), Color.Transparent))))
        content()
    }
}

// ============ LOGO SECTION ============
@Composable
private fun LogoSection() {
    val infiniteTransition = rememberInfiniteTransition(label = "logo")
    val scale by infiniteTransition.animateFloat(initialValue = 1f, targetValue = 1.1f,
        animationSpec = infiniteRepeatable(animation = tween(2000, easing = EaseInOutSine), repeatMode = RepeatMode.Reverse), label = "logoScale")

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(100.dp).scale(scale).clip(CircleShape)
            .background(Brush.linearGradient(listOf(Color(0xFF667eea), Color(0xFF764ba2)))),
            contentAlignment = Alignment.Center) {
            Icon(imageVector = Icons.Default.DirectionsCar, contentDescription = "App Logo", modifier = Modifier.size(50.dp), tint = Color.White)
        }
        Spacer(modifier = Modifier.height(16.dp))
        AnimatedTitle(text = "DoAn3")
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Uber Clone - Di chuyển dễ dàng", fontSize = 14.sp, color = Color.White.copy(alpha = 0.7f))
    }
}

// ============ ANIMATED TITLE ============
@Composable
fun AnimatedTitle(text: String, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "title")
    val scale by infiniteTransition.animateFloat(initialValue = 1f, targetValue = 1.02f,
        animationSpec = infiniteRepeatable(animation = tween(2000, easing = EaseInOutSine), repeatMode = RepeatMode.Reverse), label = "scale")
    Text(text = text, fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = modifier.scale(scale))
}

// ============ ANIMATED TAB SWITCHER ============
@Composable
fun AnimatedTabSwitcher(selectedTab: Int, onTabSelected: (Int) -> Unit, modifier: Modifier = Modifier) {
    val tabTitles = listOf("Login", "Register")
    Row(modifier = modifier.fillMaxWidth().background(Color.White.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp)).padding(4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly) {
        tabTitles.forEachIndexed { index, title ->
            val isSelected = selectedTab == index
            val scale by animateFloatAsState(targetValue = if (isSelected) 1f else 0.9f,
                animationSpec = spring(stiffness = Spring.StiffnessMedium), label = "tabScale")

            Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp))
                .background(if (isSelected) Brush.horizontalGradient(GradientColors) else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent)))
                .clickable { onTabSelected(index) }.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                Text(text = title, fontSize = 16.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = Color.White, modifier = Modifier.scale(scale))
            }
        }
    }
}

// ============ USER TYPE SELECTOR ============
@Composable
private fun UserTypeSelector(selectedType: String, onTypeSelected: (String) -> Unit) {
    Column {
        Text(text = "I want to:", fontSize = 14.sp, color = Color.White.copy(alpha = 0.7f), modifier = Modifier.padding(bottom = 12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            UserTypeCard(title = "Passenger", description = "Đặt xe đi lại", icon = Icons.Default.Person,
                isSelected = selectedType == "passenger", onClick = { onTypeSelected("passenger") }, modifier = Modifier.weight(1f))
            UserTypeCard(title = "Driver", description = "Nhận chuyến xe", icon = Icons.Default.LocalTaxi,
                isSelected = selectedType == "driver", onClick = { onTypeSelected("driver") }, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun UserTypeCard(title: String, description: String, icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val borderColor by animateColorAsState(targetValue = if (isSelected) Color(0xFF667eea) else Color.Transparent,
        animationSpec = tween(300), label = "border")
    val scale by animateFloatAsState(targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium), label = "scale")

    Card(modifier = modifier.scale(scale).clickable { onClick() }, shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFF667eea).copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f)),
        border = androidx.compose.foundation.BorderStroke(width = if (isSelected) 2.dp else 0.dp, color = borderColor)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = icon, contentDescription = null,
                tint = if (isSelected) Color(0xFF667eea) else Color.White.copy(alpha = 0.7f), modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(text = description, fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
        }
    }
}

// ============ ANIMATED TEXT FIELD ============
@Composable
fun AnimatedTextField(value: String, onValueChange: (String) -> Unit, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier, isPassword: Boolean = false, keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next, isError: Boolean = false, errorMessage: String = "", onDone: () -> Unit = {}) {

    var isFocused by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    val borderColor by animateColorAsState(targetValue = when {
        isError -> Color(0xFFff6b6b)
        isFocused -> Color(0xFF667eea)
        else -> Color.White.copy(alpha = 0.3f)
    }, animationSpec = tween(300), label = "borderColor")

    val labelColor by animateColorAsState(targetValue = when {
        isError -> Color(0xFFff6b6b)
        isFocused -> Color(0xFF667eea)
        else -> Color.White.copy(alpha = 0.6f)
    }, animationSpec = tween(300), label = "labelColor")

    val iconTint by animateColorAsState(targetValue = if (isFocused) Color(0xFF667eea) else Color.White.copy(alpha = 0.7f),
        animationSpec = tween(300), label = "iconTint")

    Column(modifier = modifier) {
        OutlinedTextField(value = value, onValueChange = onValueChange,
            label = { Text(text = label, color = labelColor) },
            leadingIcon = { Icon(imageVector = icon, contentDescription = null, tint = iconTint) },
            trailingIcon = { if (isPassword) IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = if (passwordVisible) "Hide password" else "Show password", tint = iconTint) } },
            visualTransformation = if (isPassword && !passwordVisible) androidx.compose.ui.text.input.PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                focusedContainerColor = Color.White.copy(alpha = 0.1f), unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                cursorColor = Color(0xFF667eea), focusedBorderColor = borderColor, unfocusedBorderColor = borderColor),
            shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth(), singleLine = true)

        AnimatedVisibility(visible = isError && errorMessage.isNotEmpty(), enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
            Text(text = errorMessage, color = Color(0xFFff6b6b), fontSize = 12.sp, modifier = Modifier.padding(start = 16.dp, top = 4.dp))
        }
    }
}

// ============ GRADIENT BUTTON ============
@Composable
fun GradientButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, isLoading: Boolean = false) {
    val infiniteTransition = rememberInfiniteTransition(label = "button")
    val shimmerOffset by infiniteTransition.animateFloat(initialValue = -200f, targetValue = 500f,
        animationSpec = infiniteRepeatable(animation = tween(2000, easing = LinearEasing), repeatMode = RepeatMode.Restart), label = "shimmer")
    val buttonScale by animateFloatAsState(targetValue = if (isLoading) 0.98f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh), label = "buttonScale")

    Button(onClick = onClick, enabled = enabled && !isLoading,
        modifier = modifier.height(56.dp).fillMaxWidth().scale(buttonScale),
        shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, disabledContainerColor = Color.Transparent),
        contentPadding = PaddingValues()) {
        Box(modifier = Modifier.fillMaxSize().background(Brush.horizontalGradient(
            colors = if (enabled) GradientColors else listOf(Color.Gray, Color.DarkGray)), shape = RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
            if (enabled && !isLoading) {
                Box(modifier = Modifier.fillMaxSize().background(Brush.horizontalGradient(
                    listOf(Color.Transparent, Color.White.copy(alpha = 0.3f), Color.Transparent))).offset(x = shimmerOffset.dp))
            }
            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
            else Text(text = text, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

// ============ PASSWORD STRENGTH INDICATOR ============
@Composable
fun PasswordStrengthIndicator(password: String, modifier: Modifier = Modifier) {
    val strength = remember(password) { calculatePasswordStrength(password) }
    val animatedStrength by animateFloatAsState(targetValue = strength.percentage, animationSpec = tween(300), label = "strength")

    Column(modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(4) { index ->
                val isActive = animatedStrength > (index * 25f)
                val segmentColor by animateColorAsState(targetValue = when {
                    !isActive -> Color.White.copy(alpha = 0.2f)
                    strength.level <= 1 -> Color(0xFFff6b6b)
                    strength.level == 2 -> Color(0xFFffa500)
                    strength.level == 3 -> Color(0xFF90EE90)
                    else -> Color(0xFF00ff00)
                }, animationSpec = tween(300), label = "segmentColor")

                Box(modifier = Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(2.dp))
                    .background(if (isActive) segmentColor else Color.White.copy(alpha = 0.2f)))
            }
        }
        AnimatedVisibility(visible = password.isNotEmpty(), enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
            Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Strength: ${strength.label}", fontSize = 11.sp, color = when (strength.level) {
                    0 -> Color.White.copy(alpha = 0.5f); 1 -> Color(0xFFff6b6b); 2 -> Color(0xFFffa500); 3 -> Color(0xFF90EE90); else -> Color(0xFF00ff00)
                })
                Text(text = "${password.length} characters", fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f))
            }
        }
    }
}

private data class PasswordStrength(val level: Int, val percentage: Float, val label: String)

private fun calculatePasswordStrength(password: String): PasswordStrength {
    if (password.isEmpty()) return PasswordStrength(0, 0f, "")
    var score = 0
    if (password.length >= 8) score++; if (password.length >= 12) score++; if (password.any { it.isUpperCase() }) score++
    if (password.any { it.isLowerCase() }) score++; if (password.any { it.isDigit() }) score++; if (password.any { !it.isLetterOrDigit() }) score++
    return when { score <= 2 -> PasswordStrength(1, 25f, "Weak"); score <= 3 -> PasswordStrength(2, 50f, "Fair"); score <= 4 -> PasswordStrength(3, 75f, "Good"); else -> PasswordStrength(4, 100f, "Strong") }
}

// ============ SOCIAL LOGIN BUTTON ============
@Composable
fun SocialLoginButton(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, onClick: () -> Unit,
    modifier: Modifier = Modifier, backgroundColor: Color = Color.White.copy(alpha = 0.1f)) {
    val scale by animateFloatAsState(targetValue = 1f, animationSpec = spring(stiffness = Spring.StiffnessHigh), label = "socialScale")
    OutlinedButton(onClick = onClick, modifier = modifier.height(50.dp).scale(scale),
        shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.outlinedButtonColors(containerColor = backgroundColor, contentColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

// ============ LOGIN FORM ============
@Composable
private fun LoginForm(email: String, onEmailChange: (String) -> Unit, emailError: String?, password: String,
    onPasswordChange: (String) -> Unit, passwordError: String?, onLoginClick: () -> Unit, isLoading: Boolean) {
    Column {
        AnimatedTextField(value = email, onValueChange = onEmailChange, label = "Email", icon = Icons.Outlined.Email,
            keyboardType = KeyboardType.Email, isError = emailError != null, errorMessage = emailError ?: "")
        Spacer(modifier = Modifier.height(16.dp))
        AnimatedTextField(value = password, onValueChange = onPasswordChange, label = "Password", icon = Icons.Outlined.Lock,
            isPassword = true, imeAction = ImeAction.Done, isError = passwordError != null, errorMessage = passwordError ?: "", onDone = onLoginClick)
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Text(text = "Forgot Password?", fontSize = 13.sp, color = Color(0xFF667eea), modifier = Modifier.clickable { })
        }
        Spacer(modifier = Modifier.height(24.dp))
        GradientButton(text = "Login", onClick = onLoginClick, enabled = email.isNotBlank() && password.isNotBlank(), isLoading = isLoading)
    }
}

// ============ REGISTER FORM ============
@Composable
private fun RegisterForm(form: RegisterFormState, userType: String, onNameChange: (String) -> Unit, onEmailChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit, onPasswordChange: (String) -> Unit, onConfirmPasswordChange: (String) -> Unit,
    onRegisterClick: () -> Unit, isLoading: Boolean) {
    Column {
        AnimatedTextField(value = form.name, onValueChange = onNameChange, label = "Full Name", icon = Icons.Outlined.Person,
            isError = form.nameError != null, errorMessage = form.nameError ?: "")
        Spacer(modifier = Modifier.height(16.dp))
        AnimatedTextField(value = form.email, onValueChange = onEmailChange, label = "Email", icon = Icons.Outlined.Email,
            keyboardType = KeyboardType.Email, isError = form.emailError != null, errorMessage = form.emailError ?: "")
        Spacer(modifier = Modifier.height(16.dp))
        AnimatedTextField(value = form.phone, onValueChange = onPhoneChange, label = "Phone Number", icon = Icons.Outlined.Phone,
            keyboardType = KeyboardType.Phone, isError = form.phoneError != null, errorMessage = form.phoneError ?: "")
        Spacer(modifier = Modifier.height(16.dp))
        AnimatedTextField(value = form.password, onValueChange = onPasswordChange, label = "Password", icon = Icons.Outlined.Lock,
            isPassword = true, isError = form.passwordError != null, errorMessage = form.passwordError ?: "")
        if (form.password.isNotEmpty()) { Spacer(modifier = Modifier.height(8.dp)); PasswordStrengthIndicator(password = form.password) }
        Spacer(modifier = Modifier.height(16.dp))
        AnimatedTextField(value = form.confirmPassword, onValueChange = onConfirmPasswordChange, label = "Confirm Password", icon = Icons.Outlined.Lock,
            isPassword = true, imeAction = ImeAction.Done, isError = form.confirmPasswordError != null, errorMessage = form.confirmPasswordError ?: "", onDone = onRegisterClick)
        Spacer(modifier = Modifier.height(24.dp))
        GradientButton(text = "Create Account", onClick = onRegisterClick,
            enabled = form.name.isNotBlank() && form.email.isNotBlank() && form.phone.isNotBlank() && form.password.isNotBlank() && form.confirmPassword.isNotBlank(), isLoading = isLoading)
    }
}

// ============ DIVIDER ============
@Composable
private fun AnimatedDivider() {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.weight(1f).height(1.dp).background(Brush.horizontalGradient(listOf(Color.Transparent, Color.White.copy(alpha = 0.3f)))))
        Text(text = "  or  ", fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f))
        Box(modifier = Modifier.weight(1f).height(1.dp).background(Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.3f), Color.Transparent))))
    }
}

// ============ SOCIAL LOGIN ============
@Composable
private fun SocialLoginSection() {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        SocialLoginButton(icon = Icons.Default.Email, text = "Google", onClick = { }, modifier = Modifier.weight(1f))
        SocialLoginButton(icon = Icons.Default.Phone, text = "Phone", onClick = { }, modifier = Modifier.weight(1f))
    }
}

// ============ TERMS ============
@Composable
private fun TermsText() {
    Row(horizontalArrangement = Arrangement.Center) {
        Text(text = "By continuing, you agree to our ", fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f))
        Text(text = "Terms", fontSize = 12.sp, color = Color(0xFF667eea), modifier = Modifier.clickable { })
        Text(text = " and ", fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f))
        Text(text = "Privacy", fontSize = 12.sp, color = Color(0xFF667eea), modifier = Modifier.clickable { })
    }
}

// ============ LOADING OVERLAY ============
@Composable
private fun LoadingOverlay(message: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val rotation by infiniteTransition.animateFloat(initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(1500, easing = LinearEasing), repeatMode = RepeatMode.Restart), label = "rotation")

    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.8f)), contentAlignment = Alignment.Center) {
        Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF1a1a2e))) {
            Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(modifier = Modifier.size(64.dp).graphicsLayer { rotationZ = rotation },
                    color = Color(0xFF667eea), strokeWidth = 4.dp, trackColor = Color.White.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(20.dp))
                Text(text = message, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Please wait...", fontSize = 13.sp, color = Color.White.copy(alpha = 0.6f))
            }
        }
    }
}
