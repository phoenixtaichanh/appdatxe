# Feature Plan #5: AI Intelligence Module

---

## 1. Mô tả

Module AI đã được implement ở backend. Cần cải thiện Android UI và thêm tính năng.

---

## 2. Trạng thái hiện tại

### Backend ✅ Hoàn thành
- `POST /api/ai/schedule/create` - Tạo schedule với waypoints
- `POST /api/ai/schedule/:id/optimize` - Tối ưu (time/cost/balanced)
- `GET /api/ai/recommendations` - Gợi ý cá nhân hóa
- `GET /api/ai/batch/available` - Batch offers
- `GET /api/ai/profile` - AI preferences

### Android ✅ Cơ bản hoàn thành
- `AIScheduleScreen` - Tạo schedule
- `AIProfileScreen` - Cài đặt preferences
- `AIRecommendationsScreen` - Gợi ý

### Vấn đề cần cải thiện
1. **AIScheduleScreen UI đơn giản** - Cần thêm waypoint editor
2. **Không preview route** - Cần hiển thị bản đồ
3. **AI Chat UI** - Chưa implement giao diện chat
4. **Batch offer detail** - Cần xem chi tiết từng chuyến trong batch

---

## 3. Implementation

### 3.1. AI Waypoint Editor

```kotlin
@Composable
fun WaypointEditor(
    waypoints: List<Waypoint>,
    onAdd: (Waypoint) -> Unit,
    onRemove: (Int) -> Unit,
    onReorder: (Int, Int) -> Unit
) {
    Column {
        waypoints.forEachIndexed { index, wp ->
            WaypointCard(
                waypoint = wp,
                index = index,
                isFirst = index == 0,
                isLast = index == waypoints.lastIndex,
                onRemove = { onRemove(index) }
            )
            if (index < waypoints.lastIndex) {
                DottedLine()
                // Hiển thị segment info
                val segment = waypoints[index + 1]
                SegmentInfo(distance = segment.distanceFromPrev, price = segment.price)
            }
        }
        AddWaypointButton(onClick = onAdd)
    }
}

@Composable
private fun WaypointCard(waypoint: Waypoint, ...) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        // Số thứ tự
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (wp.type == "pickup") AccentGreen else PrimaryPurple),
            contentAlignment = Alignment.Center
        ) {
            Text("${index + 1}", color = Color.White, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(wp.address, color = TextPrimary)
            Text(wp.stopName ?: wp.stopType, color = TextSecondary, fontSize = 12.sp)
        }
        if (!isFirst && !isLast) {
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Close, "Remove", tint = AccentRed)
            }
        }
    }
}
```

### 3.2. AI Chat Interface

```kotlin
@Composable
fun AIChatScreen(
    viewModel: AIChatViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsState()
    var inputText by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        // Messages list
        LazyColumn(
            modifier = Modifier.weight(1f),
            reverseLayout = false
        ) {
            items(messages) { msg ->
                ChatBubble(
                    text = msg.text,
                    isUser = msg.isUser,
                    timestamp = msg.timestamp
                )
            }
        }
        // Quick actions
        QuickActionsRow(
            suggestions = listOf(
                "Gợi ý quán cafe gần đây",
                "Lên lịch trình Đà Nẵng",
                "Địa điểm du lịch nổi tiếng"
            ),
            onSuggestionClick = { viewModel.sendMessage(it) }
        )
        // Input bar
        ChatInputBar(
            text = inputText,
            onTextChange = { inputText = it },
            onSend = {
                viewModel.sendMessage(inputText)
                inputText = ""
            }
        )
    }
}
```

### 3.3. Batch Detail Screen

```kotlin
@Composable
fun BatchDetailScreen(batchId: Int) {
    val batch by viewModel.batch.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // Header stats
        BatchStatsHeader(
            revenue = batch.totalRevenue,
            distance = batch.totalDistance,
            passengers = batch.passengerCount,
            efficiency = batch.efficiencyScore
        )
        // Route map
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            // Map placeholder với route
        }
        // Passenger list
        LazyColumn(modifier = Modifier.height(200.dp)) {
            items(batch.passengers) { passenger ->
                PassengerBatchCard(passenger = passenger)
            }
        }
        // Action buttons
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            OutlinedButton(onClick = { /* reject */ }, modifier = Modifier.weight(1f)) {
                Text("Từ chối")
            }
            Spacer(Modifier.width(12.dp))
            GradientButton(
                text = "Chấp nhận Batch",
                onClick = { /* accept */ },
                modifier = Modifier.weight(1f)
            )
        }
    }
}
```

---

## 4. Testing Checklist

| Test Case | Kỳ vọng |
|---|---|
| TC-AI-01: Tạo schedule với 3 waypoints | Schedule tạo thành công với alternatives |
| TC-AI-02: Tối ưu theo giá rẻ nhất | Route thay đổi, price giảm |
| TC-AI-03: AI Chat gửi câu hỏi | Nhận response từ AI |
| TC-AI-04: Xem chi tiết batch | Hiển thị tất cả passenger với pickup/dropoff |
| TC-AI-05: Thêm/sửa/xóa waypoint | Schedule cập nhật real-time |

---

## 5. Files

| File | Action |
|---|---|
| `WaypointEditor.kt` | Tạo mới |
| `AIChatScreen.kt` | Tạo mới |
| `BatchDetailScreen.kt` | Tạo mới |
| `AIScheduleScreen.kt` | Sửa - dùng WaypointEditor |
| `AIChatViewModel.kt` | Tạo mới |

---

## 6. Estimated time

**Waypoint Editor: 2 giờ**
**AI Chat UI: 2 giờ**
**Batch Detail: 1 giờ**
**Testing: 1 giờ**

**Tổng: ~6 giờ**
