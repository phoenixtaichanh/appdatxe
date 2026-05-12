# Feature Plan #10: Passenger-Driver Communication

---

## 1. Mô tả

Cho phép khách hàng và tài xế chat với nhau trong suốt chuyến đi.

---

## 2. Trạng thái hiện tại

### Backend ❌ Chưa implement
- Không có bảng chat/messages
- Không có API cho chat

### Android ❌ Chưa implement
- Không có chat screen

---

## 3. Implementation

### 3.1. Database Schema

```sql
CREATE TABLE chat_messages (
    id INT AUTO_INCREMENT PRIMARY KEY,
    ride_id INT NOT NULL,
    sender_id INT NOT NULL,
    sender_type ENUM('passenger', 'driver') NOT NULL,
    message TEXT NOT NULL,
    message_type ENUM('text', 'location', 'image') DEFAULT 'text',
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ride_id) REFERENCES rides(id),
    FOREIGN KEY (sender_id) REFERENCES users(id),
    INDEX idx_ride_created (ride_id, created_at)
);
```

### 3.2. Backend - Chat API

```javascript
// backend/src/routes/chat.js
const router = express.Router();

// GET /api/chat/:rideId/messages
router.get('/:rideId/messages', auth, async (req, res) => {
    const [messages] = await pool.query(`
        SELECT * FROM chat_messages
        WHERE ride_id = ?
        ORDER BY created_at ASC
    `, [req.params.rideId]);

    // Mark as read
    await pool.query(`
        UPDATE chat_messages SET is_read = TRUE
        WHERE ride_id = ? AND sender_id != ?
    `, [req.params.rideId, req.user.id]);

    res.json({ success: true, data: messages });
});

// POST /api/chat/:rideId/send
router.post('/:rideId/send', auth, async (req, res) => {
    const { message, message_type = 'text' } = req.body;
    const [ride] = await pool.query('SELECT * FROM rides WHERE id = ?', [req.params.rideId]);
    if (ride.length === 0) return res.status(404).json({ success: false });

    const senderType = req.user.user_type;
    const [result] = await pool.query(
        'INSERT INTO chat_messages (ride_id, sender_id, sender_type, message, message_type) VALUES (?, ?, ?, ?, ?)',
        [req.params.rideId, req.user.id, senderType, message, message_type]
    );

    // Gửi notification cho người nhận
    const receiverId = senderType === 'driver' ? ride[0].passenger_id : ride[0].driver_id;
    sendToUser(receiverId, {
        title: senderType === 'driver' ? 'Tài xế nhắn tin' : 'Khách nhắn tin',
        body: message.substring(0, 50),
        data: { rideId: req.params.rideId, type: 'chat_message' }
    });

    // Socket notification
    io.to(`user_${receiverId}`).emit('chat:message', {
        id: result.insertId,
        rideId: req.params.rideId,
        message,
        senderType,
        createdAt: new Date()
    });

    res.status(201).json({ success: true, data: { id: result.insertId } });
});
```

### 3.3. Android - Chat Screen

```kotlin
@Composable
fun ChatScreen(
    rideId: Int,
    passengerName: String,
    driverName: String,
    isDriver: Boolean
) {
    val messages by viewModel.messages.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto scroll to bottom
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Socket listener
    LaunchedEffect(Unit) {
        SocketManager.onChatMessage { msg ->
            if (msg.rideId == rideId) {
                viewModel.addMessage(msg)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        TopAppBar(
            title = {
                Column {
                    Text(if (isDriver) passengerName else driverName, fontWeight = FontWeight.Bold)
                    Text("Chuyến #$rideId", fontSize = 12.sp, color = TextSecondary)
                }
            },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
            actions = {
                // Call button
                IconButton(onClick = { /* initiate call */ }) {
                    Icon(Icons.Default.Phone, "Gọi điện")
                }
                // Send location button
                IconButton(onClick = { viewModel.sendLocation() }) {
                    Icon(Icons.Default.LocationOn, "Gửi vị trí")
                }
            }
        )

        // Messages
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { msg ->
                ChatBubble(
                    message = msg,
                    isOwnMessage = msg.senderId == currentUserId
                )
            }
        }

        // Input bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Nhắn tin...") },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryPurple
                )
            )
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        viewModel.sendMessage(inputText)
                        inputText = ""
                    }
                },
                modifier = Modifier.size(48.dp).background(PrimaryPurple, CircleShape)
            ) {
                Icon(Icons.Default.Send, "Gửi", tint = Color.White)
            }
        }
    }
}

@Composable
private fun ChatBubble(message: Message, isOwnMessage: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isOwnMessage) Arrangement.End else Arrangement.Start
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 16.dp, topEnd = 16.dp,
                bottomStart = if (isOwnMessage) 16.dp else 4.dp,
                bottomEnd = if (isOwnMessage) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isOwnMessage) PrimaryPurple else DarkCard
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(message.text, color = Color.White)
                Text(
                    formatTime(message.createdAt),
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
```

---

## 4. Estimated time

**Database: 15 phút**
**Backend API: 1 giờ**
**Android Chat UI: 2 giờ**
**Testing: 30 phút**

**Tổng: ~4 giờ**
