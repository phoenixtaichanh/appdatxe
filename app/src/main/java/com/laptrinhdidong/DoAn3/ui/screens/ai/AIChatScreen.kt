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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.laptrinhdidong.DoAn3.ui.theme.*
import com.laptrinhdidong.DoAn3.data.repository.AIRepository
import com.laptrinhdidong.DoAn3.data.remote.dto.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val quickReplies: List<String> = emptyList()
)

data class TripSuggestion(
    val title: String,
    val icon: String,
    val waypoints: List<WaypointDto>,
    val description: String,
    val estimatedPrice: String,
    val vehicleType: String
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
        Triple("Khám phá Đà Nẵng", Icons.Default.Explore, "du lich da nang"),
        Triple("Di chuyển tiết kiệm", Icons.Default.Savings, "xe may re nhat"),
        Triple("Xe gia đình", Icons.Default.FamilyRestroom, "dat xe 7 cho"),
        Triple("Lịch trình Hội An", Icons.Default.Map, "lich trinh hoi an 1 ngay"),
        Triple("Từ sân bay về", Icons.Default.Flight, "dat xe san bay")
    )

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
        Column(modifier = Modifier.fillMaxSize()) {
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
                            "Tư vấn lịch trình thông minh",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.Close, "Close", tint = TextSecondary)
                }
            }

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
                        WelcomeMessage(suggestions = suggestions.map { Triple(it.first, it.second, it.third) }) { action ->
                            if (!isTyping) {
                                messages = messages + ChatMessage(text = action, isUser = true)
                                scope.launch {
                                    isTyping = true
                                    kotlinx.coroutines.delay(800)
                                    val response = processUserMessage(action)
                                    messages = messages + response
                                    isTyping = false
                                }
                            }
                        }
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

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    "Gợi ý nhanh:",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    suggestions.take(3).forEach { (_, _, action) ->
                        val label = suggestions.find { it.third == action }?.first ?: action
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    if (!isTyping) {
                                        messages = messages + ChatMessage(text = label, isUser = true)
                                        scope.launch {
                                            isTyping = true
                                            kotlinx.coroutines.delay(800)
                                            val response = processUserMessage(action)
                                            messages = messages + response
                                            isTyping = false
                                        }
                                    }
                                },
                            shape = RoundedCornerShape(8.dp),
                            color = DarkCard
                        ) {
                            Text(
                                label,
                                color = TextPrimary,
                                fontSize = 11.sp,
                                maxLines = 1,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

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
                        Text("Hỏi AI về lịch trình, giá cả...", color = TextSecondary, fontSize = 14.sp)
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
                                kotlinx.coroutines.delay(800)
                                val response = processUserMessage(userMsg)
                                messages = messages + response
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
private fun WelcomeMessage(
    suggestions: List<Triple<String, androidx.compose.ui.graphics.vector.ImageVector, String>>,
    onSuggestionClick: (String) -> Unit
) {
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
            "Xin chào! Tôi là AI Assistant",
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Tôi có thể giúp bạn lên kế hoạch chuyến đi, so sánh giá cước, tìm tuyến đường tốt nhất...",
            color = TextSecondary,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Khám phá ngay:",
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(12.dp))

        suggestions.forEach { (label, _, action) ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onSuggestionClick(action) },
                shape = RoundedCornerShape(12.dp),
                color = DarkCard
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(PrimaryPurple.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Navigation,
                            null,
                            tint = PrimaryPurple,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(label, color = TextPrimary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        Icons.Default.ChevronRight,
                        null,
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
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
                .widthIn(max = 300.dp)
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

        if (message.quickReplies.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            message.quickReplies.forEach { reply ->
                Surface(
                    modifier = Modifier.padding(vertical = 2.dp).clickable { },
                    shape = RoundedCornerShape(8.dp),
                    color = DarkCard
                ) {
                    Text(
                        reply,
                        color = PrimaryPurple,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
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

private suspend fun processUserMessage(userMessage: String): ChatMessage {
    val msg = userMessage.lowercase()

    return when {
        msg.contains("san bay") || msg.contains("sân bay") || msg.contains("aéroport") -> {
            val response = buildString {
                appendLine("Máy bay của bạn sắp hạ cánh? Tôi sẽ giúp bạn đặt xe ngay!")
                appendLine()
                appendLine("📍 Các điểm đến phổ biến từ sân bay Đà Nẵng:")
                appendLine("• Bãi Biển Mỹ Khê - ~15 ph, ~25.000đ")
                appendLine("• Trung tâm TP Đà Nẵng - ~10 ph, ~20.000đ")
                appendLine("• Phố cổ Hội An - ~35 ph, ~65.000đ")
                appendLine("• Ngũ Hành Sơn - ~20 ph, ~35.000đ")
                appendLine()
                appendLine("Bạn muốn đến đâu? Cho tôi biết địa chỉ cụ thể nhé!")
            }
            ChatMessage(text = response, isUser = false)
        }

        msg.contains("ha long") || msg.contains("hạ long") || msg.contains("halong") -> {
            val response = buildString {
                appendLine("🏔️ Hà Long - Vịnh UNESCO tuyệt đẹp!")
                appendLine()
                appendLine("Tôi gợi ý lịch trình 2 ngày cho bạn:")
                appendLine()
                appendLine("📅 Ngày 1: Hà Nội → Hà Long")
                appendLine("• Khởi hành: 07:00 sáng")
                appendLine("• Thời gian: ~2.5h (110km)")
                appendLine("• Chi phí ước tính: 180.000đ (ô tô)")
                appendLine("• Tham quan: Vịnh Hạ Long, động Thiên Cung")
                appendLine()
                appendLine("📅 Ngày 2: Hà Long → Hà Nội")
                appendLine("• Tham quan thêm: Đảo Tuần Châu, làng chài...")
                appendLine("• Quay về: 16:00, về Hà Nội lúc ~18:30")
                appendLine()
                appendLine("💰 Tổng chi phí ước tính: 850.000đ/người")
                appendLine("(Đã bao gồm ăn trưa, vé tham quan)")
            }
            ChatMessage(text = response, isUser = false)
        }

        msg.contains("hoi an") || msg.contains("hội an") || msg.contains("pho co") || msg.contains("phố cổ") -> {
            val response = buildString {
                appendLine("🏯 Hội An - Phố cổ yên bình!")
                appendLine()
                appendLine("Tôi gợi ý lịch trình 1 ngày từ Đà Nẵng:")
                appendLine()
                appendLine("🕐 08:00 - Khởi hành từ Đà Nẵng")
                appendLine("🕐 08:35 - Đến phố cổ Hội An (30 phút)")
                appendLine()
                appendLine("📍 Các điểm tham quan:")
                appendLine("• Chùa Cầu (Cầu Nhật Bản)")
                appendLine("• Hội quán Phước Kiểu")
                appendLine("• Nhà cổ Tấn Ký")
                appendLine("• Chợ Hội An - thử đặc sản")
                appendLine()
                appendLine("🕐 16:00 - Quay về Đà Nẵng")
                appendLine()
                appendLine("💰 Chi phí ước tính:")
                appendLine("• Xe máy: ~60.000đ (cơ sở)")
                appendLine("• Ô tô 4 chỗ: ~95.000đ (thoải mái)")
            }
            ChatMessage(text = response, isUser = false)
        }

        msg.contains("bach khoa") || msg.contains("bách khoa") || msg.contains("truong dai hoc") -> {
            val response = buildString {
                appendLine("🎓 Đặt xe đến Trường ĐH Bách Khoa!")
                appendLine()
                appendLine("📍 Điểm đến: Trường Đại học Bách Khoa Đà Nẵng")
                appendLine("   54 Nguyễn Lương Bằng, P.Hòa Khánh, Q.Liên Chiểu")
                appendLine()
                appendLine("💰 So sánh giá cước:")
                appendLine("• 🛵 Xe máy: từ 18.000đ (5-8 ph)")
                appendLine("• 🚗 Ô tô 4 chỗ: từ 28.000đ (5-8 ph)")
                appendLine("• 🚐 Ô tô 7 chỗ: từ 38.000đ (5-8 ph)")
                appendLine()
                appendLine("⏱️ Tài xế sẽ đến trong khoảng 3-5 phút")
                appendLine("📍 Bạn đang ở đâu? Tôi sẽ gọi xe ngay!")
            }
            ChatMessage(text = response, isUser = false)
        }

        (msg.contains("7 cho") || msg.contains("7chỗ") || msg.contains("7cho")) && !msg.contains("gia re") -> {
            val response = buildString {
                appendLine("🚐 Đặt xe Ô tô 7 chỗ!")
                appendLine()
                appendLine("Phù hợp cho gia đình đông người hoặc nhóm bạn đi du lịch.")
                appendLine()
                appendLine("💰 Bảng giá:")
                appendLine("• Cước cơ bản: 15.000đ")
                appendLine("• Phí /km: 7.000đ/km")
                appendLine("• Phí /phút: 300đ/phút")
                appendLine()
                appendLine("📍 Ví dụ: Hà Nội → Hạ Long (~110km, ~2.5h)")
                appendLine("   → Chi phí ước tính: ~850.000đ")
                appendLine()
                appendLine("Bạn muốn đi từ đâu đến đâu? Tôi sẽ báo giá chính xác!")
            }
            ChatMessage(text = response, isUser = false)
        }

        (msg.contains("4 cho") || msg.contains("4chỗ") || msg.contains("4cho")) && !msg.contains("gia re") -> {
            val response = buildString {
                appendLine("🚗 Đặt xe Ô tô 4 chỗ!")
                appendLine()
                appendLine("Phổ biến nhất - phù hợp cho 1-4 người, thoải mái và tiện nghi.")
                appendLine()
                appendLine("💰 Bảng giá:")
                appendLine("• Cước cơ bản: 12.000đ")
                appendLine("• Phí /km: 5.000đ/km")
                appendLine("• Phí /phút: 200đ/phút")
                appendLine()
                appendLine("📍 Ví dụ: Sân bay → Trung tâm Đà Nẵng (~5km, ~10 ph)")
                appendLine("   → Chi phí ước tính: ~42.000đ")
                appendLine()
                appendLine("Cho tôi biết điểm đón và điểm đến nhé!")
            }
            ChatMessage(text = response, isUser = false)
        }

        msg.contains("xe may") || msg.contains("xe máy") || msg.contains("may") ||
        (msg.contains("re") && (msg.contains("nhat") || msg.contains("chi phi"))) ||
        msg.contains("tiet kiem") || msg.contains("tiết kiệm") ||
        msg.contains("gia re") -> {
            val response = buildString {
                appendLine("🛵 Xe máy - Lựa chọn tiết kiệm nhất!")
                appendLine()
                appendLine("💰 Bảng giá:")
                appendLine("• Cước cơ bản: 10.000đ")
                appendLine("• Phí /km: 3.000đ/km")
                appendLine("• Phí /phút: 100đ/phút")
                appendLine()
                appendLine("📍 Ví dụ các tuyến phổ biến:")
                appendLine("• Sân bay → Bãi Biển Mỹ Khê: ~25.000đ")
                appendLine("• Sân bay → Phố cổ Hội An: ~65.000đ")
                appendLine("• Vinmart → Landmark 81: ~18.000đ")
                appendLine()
                appendLine("⚡ Tài xế đến trong 3-5 phút!")
                appendLine("Bạn cần đi tuyến nào?")
            }
            ChatMessage(text = response, isUser = false)
        }

        msg.contains("du lich") || msg.contains("du lịch") || msg.contains("tour") ||
        msg.contains("tham quan") || msg.contains("di choi") -> {
            val response = buildString {
                appendLine("🗺️ Gợi ý du lịch Đà Nẵng!")
                appendLine()
                appendLine("📍 TOP điểm đến được yêu thích:")
                appendLine()
                appendLine("1️⃣ Bãi Biển Mỹ Khê")
                appendLine("   Biển đẹp nhất Đà Nẵng, miễn phí!")
                appendLine("   ~15 ph từ trung tâm")
                appendLine()
                appendLine("2️⃣ Ngũ Hành Sơn")
                appendLine("   Cảnh quan núi & biển tuyệt đẹp")
                appendLine("   ~20 ph từ trung tâm")
                appendLine()
                appendLine("3️⃣ Phố cổ Hội An")
                appendLine("   Di sản UNESCO, ăn uống ngon")
                appendLine("   ~35 ph từ trung tâm")
                appendLine()
                appendLine("4️⃣ Bán đảo Sơn Trà (Nghinh Phong)")
                appendLine("   View đẹp, ngắm toàn cảnh thành phố")
                appendLine("   ~25 ph từ trung tâm")
                appendLine()
                appendLine("5️⃣ Sun Wheel (Cánh đồng bất tận)")
                appendLine("   Góc nhìn 360° toàn thành phố")
                appendLine("   ~15 ph từ trung tâm")
                appendLine()
                appendLine("Bạn muốn tôi lên lịch trình cụ thể không?")
            }
            ChatMessage(text = response, isUser = false)
        }

        msg.contains("an") && (msg.contains("uong") || msg.contains("ăn") || msg.contains("nha hang") || msg.contains("quán")) -> {
            val response = buildString {
                appendLine("🍜 Gợi ý ăn uống ở Đà Nẵng!")
                appendLine()
                appendLine("📍 Những địa điểm nổi tiếng:")
                appendLine()
                appendLine("1️⃣ Bánh xèo Bà Dưỡng")
                appendLine("   467 Trần Cao Vân, P.Thanh Khê")
                appendLine("   ~20 ph từ trung tâm, ~35.000đ/người")
                appendLine()
                appendLine("2️⃣ Mỹ Vân Hội An")
                appendLine("   Cơm gà Hội An đích thực")
                appendLine("   ~30 ph, ~40.000đ/người")
                appendLine()
                appendLine("3️⃣ Bún chả Hà Nội Dì Hường")
                appendLine("   Đúng vị Hà Nội giữa lòng Đà Nẵng")
                appendLine("   ~10 ph, ~30.000đ/người")
                appendLine()
                appendLine("4️⃣ Hải sản Tú")
                appendLine("   Ngõ 64 Trần Phú - Hải sản tươi sống")
                appendLine("   ~15 ph, ~150.000đ/người")
                appendLine()
                appendLine("Bạn muốn đến đâu? Tôi đặt xe ngay!")
            }
            ChatMessage(text = response, isUser = false)
        }

        msg.contains("so sanh") || msg.contains("so sánh") || msg.contains("bang gia") ||
        msg.contains("bảng giá") || msg.contains("price") || msg.contains("giá") -> {
            val response = buildString {
                appendLine("📊 Bảng so sánh giá cước DoAn3")
                appendLine()
                appendLine("┌─────────────┬───────────┬─────────┬─────────┐")
                appendLine("│  Loại xe   │  Cước cơ  │  /km    │  /phút  │")
                appendLine("├─────────────┼───────────┼─────────┼─────────┤")
                appendLine("│  🛵 Xe máy │  10.000đ  │ 3.000đ  │  100đ   │")
                appendLine("│  🚗 4 chỗ  │  12.000đ  │ 5.000đ  │  200đ   │")
                appendLine("│  🚐 7 chỗ  │  15.000đ  │ 7.000đ  │  300đ   │")
                appendLine("└─────────────┴───────────┴─────────┴─────────┘")
                appendLine()
                appendLine("📍 Ví dụ: 10km, 20 phút")
                appendLine("• Xe máy: 10.000 + 30.000 + 2.000 = ~42.000đ")
                appendLine("• 4 chỗ:  12.000 + 50.000 + 4.000 = ~66.000đ")
                appendLine("• 7 chỗ:  15.000 + 70.000 + 6.000 = ~91.000đ")
                appendLine()
                appendLine("💡 Xe máy tiết kiệm ~37% so với ô tô 4 chỗ!")
            }
            ChatMessage(text = response, isUser = false)
        }

        msg.contains("thoi gian") || msg.contains("thời gian") || msg.contains("bao lau") ||
        msg.contains("bao lâu") || msg.contains("may phut") || msg.contains("mấy phút") -> {
            val response = buildString {
                appendLine("⏱️ Ước tính thời gian di chuyển")
                appendLine()
                appendLine("📍 Từ Sân bay Đà Nẵng đến:")
                appendLine("• Trung tâm thành phố: ~10-15 phút")
                appendLine("• Bãi Biển Mỹ Khê: ~15-20 phút")
                appendLine("• Ngũ Hành Sơn: ~20-25 phút")
                appendLine("• Phố cổ Hội An: ~35-45 phút")
                appendLine("• Bán đảo Sơn Trà: ~25-30 phút")
                appendLine()
                appendLine("📍 Từ Trung tâm đến:")
                appendLine("• Sân bay: ~10-15 phút")
                appendLine("• Vinmart Điện Biên Phủ: ~5-10 phút")
                appendLine("• Landmark 81: ~10-15 phút")
                appendLine("• Bến Thành Market: ~15-20 phút")
                appendLine()
                appendLine("⚠️ Lưu ý: Giờ cao điểm (7-9h, 17-19h) thời gian có thể tăng 30-50%")
            }
            ChatMessage(text = response, isUser = false)
        }

        msg.contains("toi") || msg.contains("tôi") || msg.contains("minh") ||
        msg.contains("mình") && (msg.contains("muon") || msg.contains("muốn") || msg.contains("can") || msg.contains("cần")) -> {
            val response = buildString {
                appendLine("👍 Được rồi! Tôi sẵn sàng giúp bạn!")
                appendLine()
                appendLine("Để đặt xe nhanh nhất, bạn cho tôi biết:")
                appendLine()
                appendLine("1️⃣ Địa chỉ đón (hoặc bấm vào bản đồ)")
                appendLine("2️⃣ Địa chỉ đến")
                appendLine("3️⃣ Chọn loại xe (xe máy / 4 chỗ / 7 chỗ)")
                appendLine()
                appendLine("💡 Hoặc bạn có thể:")
                appendLine("• Nói 'đặt xe đi [địa điểm]'")
                appendLine("• Hỏi 'xe máy giá bao nhiêu?'")
                appendLine("• Yêu cầu 'lên lịch trình du lịch Hội An'")
            }
            ChatMessage(text = response, isUser = false)
        }

        msg.contains("cam on") || msg.contains("cảm ơn") || msg.contains("thanks") ||
        msg.contains("thank") -> {
            ChatMessage(
                text = "Không có chi! 😊 Nếu bạn cần đặt xe hay lên kế hoạch chuyến đi, cứ nhắn cho tôi nhé. Chúc bạn một ngày tốt lành!",
                isUser = false
            )
        }

        msg.contains("chào") || msg.contains("hello") || msg.contains("hi") ||
        msg.contains("xin chao") || msg.contains("xinh chào") -> {
            val response = buildString {
                appendLine("Xin chào! 👋 Tôi là AI Assistant của DoAn3!")
                appendLine()
                appendLine("Tôi có thể giúp bạn:")
                appendLine("• 🚗 Đặt xe các loại (xe máy, 4 chỗ, 7 chỗ)")
                appendLine("• 📍 Tư vấn lịch trình du lịch")
                appendLine("• 💰 So sánh giá cước chi tiết")
                appendLine("• ⏱️ Ước tính thời gian & khoảng cách")
                appendLine("• 🗺️ Gợi ý địa điểm ăn uống, tham quan")
                appendLine()
                appendLine("Bạn cần tôi hỗ trợ gì hôm nay?")
            }
            ChatMessage(text = response, isUser = false)
        }

        else -> {
            val response = buildString {
                appendLine("Hmm, tôi chưa hiểu rõ ý bạn lắm 😅")
                appendLine()
                appendLine("Bạn có thể thử:")
                appendLine("• 'Đặt xe đi sân bay' - đặt xe ngay")
                appendLine("• 'Lịch trình du lịch Hội An' - gợi ý lịch trình")
                appendLine("• 'Xe máy giá bao nhiêu?' - xem bảng giá")
                appendLine("• 'Từ Đà Nẵng đến Hội An bao lâu?' - ước tính thời gian")
                appendLine("• 'Gợi ý chỗ ăn ngon' - địa điểm ăn uống")
                appendLine()
                appendLine("Hoặc cứ mô tả chuyến đi của bạn, tôi sẽ giúp!")
            }
            ChatMessage(text = response, isUser = false)
        }
    }
}
