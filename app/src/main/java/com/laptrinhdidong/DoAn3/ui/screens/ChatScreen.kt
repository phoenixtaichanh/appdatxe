package com.laptrinhdidong.DoAn3.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
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
import com.laptrinhdidong.DoAn3.data.remote.SocketManager
import com.laptrinhdidong.DoAn3.data.repository.RideRepository
import com.laptrinhdidong.DoAn3.ui.components.AppTopBar
import com.laptrinhdidong.DoAn3.ui.components.GradientButton
import com.laptrinhdidong.DoAn3.ui.components.LoadingOverlay
import com.laptrinhdidong.DoAn3.ui.theme.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class ChatMessage(
    val id: Int,
    val message: String,
    val senderId: Int,
    val senderType: String,
    val senderName: String?,
    val messageType: String,
    val isRead: Boolean,
    val createdAt: String
)

data class ChatState(
    val isLoading: Boolean = false,
    val messages: List<ChatMessage> = emptyList(),
    val errorMessage: String? = null
)

@dagger.hilt.android.lifecycle.HiltViewModel
class ChatViewModel @javax.inject.Inject constructor(
    private val rideRepository: RideRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    private var rideId: Int = -1
    private var currentUserId: Int = -1

    fun initialize(rideId: Int, currentUserId: Int) {
        this.rideId = rideId
        this.currentUserId = currentUserId
        loadMessages()
        setupSocketListener()
    }

    private fun loadMessages() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            rideRepository.getChatMessages(rideId).onSuccess { dtoList ->
                val messages = dtoList.map { dto ->
                    ChatMessage(
                        id = dto.id,
                        message = dto.message,
                        senderId = dto.senderId,
                        senderType = dto.senderType,
                        senderName = dto.senderName,
                        messageType = dto.messageType,
                        isRead = dto.isRead,
                        createdAt = dto.createdAt
                    )
                }
                _state.value = _state.value.copy(isLoading = false, messages = messages)
            }.onFailure {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    private fun setupSocketListener() {
        viewModelScope.launch {
            SocketManager.connectionState.collect { connected ->
                if (connected) {
                    SocketManager.joinRide(rideId)
                }
            }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank() || rideId < 0) return
        viewModelScope.launch {
            rideRepository.sendChatMessage(rideId, text).onSuccess {
                loadMessages()
            }.onFailure {
                _state.value = _state.value.copy(errorMessage = "Gui tin nhan that bai")
            }
        }
    }
}

@Composable
fun ChatScreen(
    rideId: Int,
    passengerName: String,
    driverName: String,
    currentUserId: Int,
    isDriver: Boolean,
    onBack: () -> Unit,
    onCall: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(rideId) {
        viewModel.initialize(rideId, currentUserId)
    }

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextPrimary)
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(GradientPrimary)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isDriver) passengerName else driverName,
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Chuyen #$rideId",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
                IconButton(onClick = onCall) {
                    Icon(Icons.Default.Phone, "Call", tint = AccentGreen)
                }
            }

            // Messages
            if (state.isLoading) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryPurple)
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    if (state.messages.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Chua co tin nhan nao",
                                    color = TextSecondary,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                    items(state.messages) { msg ->
                        val isOwn = msg.senderId == currentUserId
                        ChatBubble(
                            message = msg.message,
                            senderName = msg.senderName,
                            timestamp = msg.createdAt,
                            isOwn = isOwn
                        )
                    }
                }
            }

            // Quick replies
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Toi da den", "Dang cho", "Cam on").forEach { reply ->
                    SuggestionChip(
                        onClick = {
                            if (inputText.isEmpty()) inputText = reply
                        },
                        label = { Text(reply, fontSize = 12.sp) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = DarkCard,
                            labelColor = TextSecondary
                        ),
                        modifier = Modifier.height(32.dp)
                    )
                }
            }

            // Input bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(DarkCard)
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Nhan tin...", color = TextSecondary, fontSize = 14.sp) },
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = DarkCard,
                        focusedContainerColor = DarkCard,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        cursorColor = PrimaryPurple,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        val uri = Uri.parse("geo:0,0?q=pickup")
                        Intent(Intent.ACTION_VIEW, uri)
                    },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(Icons.Default.LocationOn, "Location", tint = TextSecondary)
                }
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            viewModel.sendMessage(inputText.trim())
                            inputText = ""
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(PrimaryPurple)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(
    message: String,
    senderName: String?,
    timestamp: String,
    isOwn: Boolean
) {
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val displayTime = try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val parsed = sdf.parse(timestamp)
        if (parsed != null) timeFormat.format(parsed) else timestamp.takeLast(5)
    } catch (e: Exception) {
        timestamp.takeLast(8).take(5)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isOwn) Alignment.End else Alignment.Start
    ) {
        if (!isOwn && !senderName.isNullOrEmpty()) {
            Text(
                text = senderName,
                color = TextSecondary,
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
            )
        }
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isOwn) 16.dp else 4.dp,
                        bottomEnd = if (isOwn) 4.dp else 16.dp
                    )
                )
                .background(if (isOwn) PrimaryPurple else DarkCard)
                .padding(12.dp)
        ) {
            Text(
                message,
                color = if (isOwn) Color.White else TextPrimary,
                fontSize = 14.sp
            )
        }
        Text(
            displayTime,
            color = TextSecondary,
            fontSize = 10.sp,
            modifier = Modifier.padding(start = 8.dp, top = 2.dp)
        )
    }
}
