# Feature Plan #8: Push Notifications (FCM)

---

## 1. Mô tả

Hệ thống push notification để thông báo real-time đến người dùng:
- Tài xế nhận được chuyến mới
- Khách hàng biết tài xế đã nhận chuyến
- Nhắc nhở lịch trình AI
- Thông báo hoàn thành chuyến

---

## 2. Trạng thái hiện tại

### Backend ❌ Chưa implement
- Không có FCM integration
- Không có notification service

### Android ❌ Chưa implement
- Chưa setup Firebase Cloud Messaging
- Chưa có FCM token registration

---

## 3. Implementation

### 3.1. Backend - FCM Setup

```bash
cd backend
npm install firebase-admin
```

**File mới:** `backend/src/services/notification.js`
```javascript
const admin = require('firebase-admin');

let initialized = false;

function initFirebase() {
    if (initialized) return;
    try {
        admin.initializeApp({
            credential: admin.credential.cert({
                projectId: process.env.FIREBASE_PROJECT_ID,
                clientEmail: process.env.FIREBASE_CLIENT_EMAIL,
                privateKey: process.env.FIREBASE_PRIVATE_KEY?.replace(/\\n/g, '\n')
            })
        });
        initialized = true;
    } catch (e) {
        console.error('Firebase init failed:', e.message);
    }
}

async function sendToUser(userId, { title, body, data = {} }) {
    // Lấy FCM token từ DB
    const [tokens] = await pool.query(
        'SELECT fcm_token FROM user_fcm_tokens WHERE user_id = ?',
        [userId]
    );
    if (tokens.length === 0) return;

    const message = {
        notification: { title, body },
        data: Object.fromEntries(
            Object.entries(data).map(([k, v]) => [k, String(v)])
        ),
        tokens: tokens.map(t => t.fcm_token)
    };

    try {
        const response = await admin.messaging().sendEachForMulticast(message);
        console.log(`Sent: ${response.successCount}, Failed: ${response.failureCount}`);
    } catch (e) {
        console.error('FCM error:', e.message);
    }
}

module.exports = { initFirebase, sendToUser };
```

**Database table:**
```sql
CREATE TABLE user_fcm_tokens (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    fcm_token TEXT NOT NULL,
    device_id VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY unique_user_device (user_id, device_id)
);
```

### 3.2. Backend - Integrate vào ride events

```javascript
// Trong backend/src/routes/rides.js
const { sendToUser } = require('../services/notification');

// Khi passenger tạo ride → notify nearby drivers
async function notifyDrivers(rideId, pickupLat, pickupLng) {
    // Lấy drivers trong bán kính 5km
    const [drivers] = await pool.query(`
        SELECT d.user_id FROM drivers d
        JOIN users u ON d.user_id = u.id
        WHERE d.is_available = true
        AND (Haversine formula) <= 5
    `);
    for (const driver of drivers) {
        sendToUser(driver.user_id, {
            title: '🚗 Chuyến mới!',
            body: 'Có khách đặt xe gần bạn',
            data: { rideId: String(rideId), type: 'new_ride' }
        });
    }
}

// Khi driver accept → notify passenger
router.post('/api/driver/ride/:id/accept', auth, async (req, res, next) => {
    // ... existing logic ...
    const [ride] = await pool.query('SELECT passenger_id FROM rides WHERE id = ?', [id]);
    sendToUser(ride[0].passenger_id, {
        title: '✅ Tài xế đã nhận chuyến!',
        body: `Tài xế ${driverName} đã nhận chuyến của bạn`,
        data: { rideId: String(id), type: 'ride_accepted' }
    });
});
```

### 3.3. Android - FCM Setup

**Thêm dependency:**
```kotlin
// build.gradle.kts
implementation(platform("com.google.firebase:firebase-bom:33.0.0"))
implementation("com.google.firebase:firebase-messaging-ktx")
```

**AndroidManifest.xml:**
```xml
<service android:name=".service.DoAn3FCMService"
    android:exported="false">
    <intent-filter>
        <action android:name="com.google.firebase.MESSAGING_EVENT" />
    </intent-filter>
</service>
```

**DoAn3FCMService.kt:**
```kotlin
class DoAn3FCMService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Gửi token lên server
        lifecycleScope.launch {
            RetrofitClient.apiService.registerFcmToken(token)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "Thông báo"
        val body = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: ""
        val type = remoteMessage.data["type"]

        val notificationManager = NotificationManagerCompat.from(this)
        val channel = NotificationChannel(
            CHANNEL_ID, "DoAn3 Notifications",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Taxi app notifications"
            enableVibration(true)
        }
        notificationManager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        try {
            notificationManager.notify(System.currentTimeMillis().toInt(), notification)
        } catch (e: SecurityException) {
            // User chưa grant notification permission
        }
    }

    companion object {
        const val CHANNEL_ID = "doan3_notifications"
    }
}
```

**Đăng ký token khi app start:**
```kotlin
// Trong DoAn3Application.kt
override fun onCreate() {
    super.onCreate()
    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
        if (task.isSuccessful) {
            val token = task.result
            // Lưu vào SharedPreferences và gửi lên server
        }
    }
}
```

---

## 4. Estimated time

**Backend FCM: 1.5 giờ**
**Android FCM: 1.5 giờ**
**Integration testing: 1 giờ**

**Tổng: ~4 giờ**
