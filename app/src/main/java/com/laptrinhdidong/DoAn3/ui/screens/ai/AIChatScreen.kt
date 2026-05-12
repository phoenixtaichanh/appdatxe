package com.laptrinhdidong.DoAn3.ui.screens.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.laptrinhdidong.DoAn3.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@Composable
fun AIChatScreen(
    onBack: () -> Unit
) {
    var messages by remember { mutableStateOf(listOf<ChatMessage>()) }
    var inputText by remember { mutableStateOf("") }
    var isTyping by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val suggestions = listOf(
        "Goi xe di Bach Khoa",
        "Dat xe 7 cho sang Quan 1",
        "Lich trinh Ha Long 2 ngay",
        "Dich vu gia re nhat",
        "Toi muon xe 4 cho"
    )

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(PrimaryPurple.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            null,
                            tint = PrimaryPurple,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "AI Assistant",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "AI lich trinh thong minh",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.Close, "Close", tint = TextSecondary)
                }
            }

            // Messages
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (messages.isEmpty()) {
                    item {
                        WelcomeMessage()
                    }
                }
                items(messages) { msg ->
                    ChatBubble(message = msg)
                }
                if (isTyping) {
                    item {
                        TypingIndicator()
                    }
                }
            }

            // Suggestions
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    "De xuat nhanh:",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    suggestions.take(3).forEach { suggestion ->
                        SuggestionChip(
                            onClick = {
                                if (!isTyping) {
                                    messages = messages + ChatMessage(text = suggestion, isUser = true)
                                    scope.launch {
                                        isTyping = true
                                        kotlinx.coroutines.delay(1200)
                                        val response = getAIResponse(suggestion)
                                    messages = messages + ChatMessage(text = response, isUser = false)
                                    isTyping = false
                                    }
                                }
                            },
                            label = {
                                Text(
                                    suggestion,
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )
                            },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = DarkCard,
                                labelColor = TextPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

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
                    placeholder = {
                        Text("Nhap loi nhan...", color = TextSecondary, fontSize = 14.sp)
                    },
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = DarkCard,
                        focusedContainerColor = DarkCard,
                        unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                        focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
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
                        if (inputText.isNotBlank() && !isTyping) {
                            val userMsg = inputText.trim()
                            messages = messages + ChatMessage(text = userMsg, isUser = true)
                            inputText = ""
                            scope.launch {
                                isTyping = true
                                kotlinx.coroutines.delay(1200)
                                val response = getAIResponse(userMsg)
                                messages = messages + ChatMessage(text = response, isUser = false)
                                isTyping = false
                            }
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(PrimaryPurple)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        null,
                        tint = androidx.compose.ui.graphics.Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun WelcomeMessage() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(PrimaryPurple.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.AutoAwesome,
                null,
                tint = PrimaryPurple,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Xin chao! Toi la AI Assistant",
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Toi co the giup ban dat xe, tao lich trinh, tu van gia...",
            color = TextSecondary,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    val dateFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (message.isUser) 16.dp else 4.dp,
                        bottomEnd = if (message.isUser) 4.dp else 16.dp
                    )
                )
                .background(
                    if (message.isUser) PrimaryPurple else DarkCard
                )
                .padding(12.dp)
        ) {
            Text(
                message.text,
                color = if (message.isUser) androidx.compose.ui.graphics.Color.White else TextPrimary,
                fontSize = 14.sp
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            dateFormat.format(Date(message.timestamp)),
            color = TextSecondary,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun TypingIndicator() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(DarkCard)
                .padding(12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(3) { index ->
                    var enabled by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(index * 200L)
                        enabled = true
                    }
                    if (enabled) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(PrimaryPurple.copy(alpha = 0.7f))
                        ) {}
                    }
                }
            }
        }
    }
}

private fun getAIResponse(userMessage: String): String {
    val msg = userMessage.lowercase()
    return when {
        msg.contains("bach khoa") || msg.contains("truong") ->
            "Da dat xe cho ban di BACH KHOA! Tai xe se den trong 5 phut. Gia uu tien: 25,000d."
        msg.contains("7 cho") || msg.contains("7cho") ->
            "OK! Dat xe O to 7 cho. Loai xe nay phu hop cho gia dinh hoac nhom. Gia: tu 35,000d."
        msg.contains("ha long") || msg.contains("halong") ->
            "Toi se tao lich trinh Ha Long 2 ngay cho ban:\n- Ngay 1: Ha Noi -> Ha Long (120km)\n- Ngay 2: Ha Long -> Ha Noi\nChi phi uoc tinh: 850,000d"
        msg.contains("re nhat") || msg.contains("re") || msg.contains("gia re") ->
            "Dep nhat! Xe may la lua chon tiet kiem nhat:\n- Gia cuoc: 10,000d + 3,000d/km\n- Thich hop cho hanh trinh ngan"
        msg.contains("4 cho") || msg.contains("4cho") ->
            "Dat xe O to 4 cho! Loai xe pho bien, gia hop ly: tu 28,000d. Tai xe se lien he trong 3 phut."
        else ->
            "Cam on ban! Toi da ghi nhan yeu cau. Ban co the dat xe ngay bay gio hoac hoi them ve dich vu nhe!"
    }
}
