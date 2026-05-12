# Feature Plan #3: Realtime Tracking & WebSocket

---

## 1. Mô tả

Theo dõi vị trí real-time của tài xế và cập nhật trạng thái chuyến đi tức thì thông qua WebSocket.

---

## 2. Trạng thái hiện tại

### Backend ❌ Chưa implement
- Chỉ có REST API update location
- Không có WebSocket server

### Android ❌ Chưa implement
- Driver không gửi location liên tục
- Passenger polling ride status mỗi 5 giây (hiện tại)

---

## 3. Implementation

### 3.1. Backend - WebSocket Server

**Thêm Socket.io vào backend:**

```bash
cd backend
npm install socket.io
```

**File mới:** `backend/src/socket/index.js`
```javascript
const { Server } = require('socket.io');
const jwt = require('jsonwebtoken');

function initSocket(httpServer) {
    const io = new Server(httpServer, {
        cors: { origin: '*' }
    });

    io.use((socket, next) => {
        const token = socket.handshake.auth.token;
        if (!token) return next(new Error('Auth required'));
        try {
            socket.user = jwt.verify(token, process.env.JWT_SECRET);
            next();
        } catch (e) { next(new Error('Invalid token')); }
    });

    io.on('connection', (socket) => {
        const { id, user_type } = socket.user;
        socket.join(`user_${id}`);
        if (user_type === 'driver') {
            socket.join('drivers');
            // Lắng nghe driver location
            socket.on('location:update', ({ lat, lng }) => {
                // UPDATE driver_locations in DB
                // BROADCAST đến passenger đang có ride với driver này
                // io.to(`ride_${currentRideId}`).emit('driver:location', { lat, lng });
            });
        }
        socket.on('ride:status', ({ rideId, status }) => {
            // Cập nhật DB → broadcast đến tất cả parties
            // io.to(`ride_${rideId}`).emit('ride:status:changed', { status });
        });
        socket.on('join:ride', (rideId) => { socket.join(`ride_${rideId}`); });
        socket.on('leave:ride', (rideId) => { socket.leave(`ride_${rideId}`); });
    });

    return io;
}

module.exports = { initSocket };
```

**Cập nhật `backend/src/index.js`:**
```javascript
const { initSocket } = require('./socket');
// ...
const httpServer = app.listen(PORT, ...);
const io = initSocket(httpServer);
```

### 3.2. Android - Socket.IO Client

**Thêm dependency:**
```kotlin
// build.gradle.kts
implementation("io.socket:socket.io-client:2.1.0") {
    exclude(group = "org.json", module = "json")
}
```

**File mới:** `data/remote/SocketManager.kt`
```kotlin
object SocketManager {
    private var socket: Socket? = null

    fun connect(token: String) {
        socket = IO.socket(BASE_URL, Options().apply {
            auth = mapOf("token" to token)
            reconnection = true
            reconnectionDelay = 1000
        })
        socket?.connect()
        socket?.on(Socket.EVENT_CONNECT) { Log.d("Socket", "Connected") }
        socket?.on("driver:location") { args ->
            val lat = args[0] as JSONObject
            _driverLocation.emit(lat.getDouble("lat"), lat.getDouble("lng"))
        }
    }

    fun emitLocation(lat: Double, lng: Double) {
        socket?.emit("location:update", mapOf("lat" to lat, "lng" to lng))
    }

    fun joinRide(rideId: Int) {
        socket?.emit("join:ride", rideId)
    }

    fun onDriverLocation(cb: (Double, Double) -> Unit) {
        socket?.on("driver:location") { args -> cb(...) }
    }

    fun disconnect() { socket?.disconnect(); socket = null }
}
```

### 3.3. Android - Driver Location Service

**File mới:** `service/LocationTrackingService.kt`
```kotlin
class LocationTrackingService : Service() {
    private val fusedClient by lazy { LocationServices.getFusedLocationProviderClient(this) }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startLocationUpdates()
        return START_STICKY
    }

    private fun startLocationUpdates() {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setMinUpdateIntervalMillis(3000).build()
        fusedClient.requestLocationUpdates(request, object : LocationCallback() {
            override fun onLocationResult(r: LocationResult) {
                r.lastLocation?.let {
                    SocketManager.emitLocation(it.latitude, it.longitude)
                }
            }
        }, Looper.getMainLooper())
    }
}
```

### 3.4. Android - Passenger realtime map

```kotlin
// Trong ActiveRideScreen hoặc PassengerHomeScreen
@Composable
fun RealtimeDriverTracking(rideId: Int) {
    LaunchedEffect(rideId) {
        SocketManager.joinRide(rideId)
    }
    LaunchedEffect(Unit) {
        SocketManager.onDriverLocation { lat, lng ->
            // Cập nhật marker trên bản đồ
            driverMarkerPosition = LatLng(lat, lng)
        }
    }
}
```

---

## 4. Events Flow

```
┌──────────┐                    ┌────────────┐                    ┌──────────┐
│  Driver  │                    │   Socket   │                    │Passenger │
│   App    │                    │   Server   │                    │   App    │
└────┬─────┘                    └─────┬──────┘                    └────┬─────┘
     │                               │                               │
     │ emit("location:update")       │                               │
     │──────────────────────────────▶│                               │
     │                               │                               │
     │                               │ io.to(`ride_${id}`)         │
     │                               │──────────────────────────────▶│
     │                               │                               │ Cập nhật
     │                               │                               │ marker
     │                               │                               │
     │◀─────────────────────────────│ emit("ride:status:changed") │──▶│
     │   Status update             │                               │
```

---

## 5. Testing Checklist

| Test Case | Kỳ vọng |
|---|---|
| TC-WS-01: Driver gửi location | Passenger nhận và cập nhật map trong 3 giây |
| TC-WS-02: Driver mất mạng | Socket tự reconnect, location tiếp tục gửi |
| TC-WS-03: Socket disconnect | App tự reconnect, không crash |
| TC-WS-04: Ride completed | Cả 2 bên nhận notification |

---

## 6. Files

| File | Action |
|---|---|
| `backend/src/socket/index.js` | Tạo mới |
| `backend/src/index.js` | Sửa - thêm socket init |
| `SocketManager.kt` | Tạo mới |
| `LocationTrackingService.kt` | Tạo mới |
| `ActiveRideScreen.kt` | Sửa - realtime tracking |

---

## 7. Estimated time

**Backend WebSocket: 1.5 giờ**
**Android Socket.IO + Service: 2 giờ**
**Testing: 1 giờ**

**Tổng: ~4.5 giờ**
