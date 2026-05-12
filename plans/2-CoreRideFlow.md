# Feature Plan #2: Core Ride Flow

---

## 1. Mô tả

Luồng chuyến đi cốt lõi:
1. Khách hàng chọn điểm đón, điểm đến
2. Hệ thống ước tính giá (Haversine)
3. Tìm tài xế gần nhất
4. Gửi yêu cầu đặt xe
5. Tài xế nhận chuyến
6. Cập nhật trạng thái: pending → accepted → arrived → in_progress → completed

---

## 2. Trạng thái hiện tại

### Backend ✅ Hoàn thành
- `POST /api/rides/request` - Tạo ride với Haversine calculation
- `GET /api/rides` - Lấy lịch sử ride
- `GET /api/rides/:id` - Chi tiết ride
- `PUT /api/rides/:id/status` - Cập nhật trạng thái
- `POST /api/rides/:id/rate` - Đánh giá
- `GET /api/location/nearby-drivers` - Tìm tài xế gần

### Android ✅ Hoàn thành (UI placeholder)
- `PassengerHomeScreen` - Booking bottom sheet
- Haversine calculation ở phía Android (đồng bộ với backend)
- Demo locations được hard-coded

### Vấn đề cần sửa
1. **Lỗi duplicate route** - `rides.js` có 2 endpoint `GET /` giống nhau
2. **Không filter nearby drivers** - API trả tất cả thay vì trong bán kính
3. **Vehicle type chưa implement** - Chỉ có 1 loại xe, thiếu xe máy / 4 chỗ / 7 chỗ
4. **Pricing theo loại xe** - Giá cố định, không tính theo loại xe
5. **Polling cho nearby drivers** - Gọi API 1 lần, không tự refresh
6. **Không show driver info sau khi nhận** - Passenger không thấy tài xế đã nhận

---

## 3. Code cần sửa / thêm

### 3.1. Fix duplicate route trong rides.js

**File:** `backend/src/routes/rides.js`

```javascript
// HIỆN TẠI: Có 2 GET '/' route (line 67 và line 178)
// → Route thứ 2 (history) sẽ never được called

// CẦN SỬA: Xóa route thứ 2 (line 178-210)
// Giữ nguyên route đầu tiên làm history
// Thêm query param filter: GET /api/rides?status=pending hoặc /api/rides/history
```

**Giải pháp tốt nhất:**
```javascript
// Route 1: GET /api/rides - Lấy lịch sử (các ride đã hoàn thành / cancelled)
// Route 2: GET /api/rides/active - Lấy ride đang active (pending, accepted, in_progress)
router.get('/active', auth, async (req, res, next) => {
    // SELECT * FROM rides WHERE (passenger_id = ? OR driver_id = ?)
    // AND status IN ('pending', 'accepted', 'arrived', 'in_progress')
});
```

### 3.2. Fix nearby drivers - thêm bán kính

**File:** `backend/src/routes/locations.js`

```javascript
// HIỆN TẠI: Trả tất cả driver có is_available = true
// CẦN SỬA: Thêm Haversine filter trong query
router.get('/nearby-drivers', auth, async (req, res, next) => {
    const { lat, lng, radius = 5 } = req.query;
    // SELECT * FROM drivers d
    // JOIN users u ON d.user_id = u.id
    // WHERE d.is_available = true
    // AND (Haversine formula) <= radius
});
```

### 3.3. Thêm vehicle type và dynamic pricing

**Database:** Thêm trường `vehicle_type` vào bảng rides

```sql
ALTER TABLE rides ADD COLUMN vehicle_type ENUM('motorbike', 'car_4', 'car_7') DEFAULT 'car_4';
```

**Backend pricing:**
```javascript
const VEHICLE_PRICING = {
    motorbike: { base: 10000, perKm: 3000, perMin: 100 },   // 10k + 3k/km + 100/p
    car_4:      { base: 10000, perKm: 5000, perMin: 200 },  // 10k + 5k/km + 200/p
    car_7:      { base: 15000, perKm: 7000, perMin: 300 }   // 15k + 7k/km + 300/p
};

function calculatePrice(distanceKm, durationMin, vehicleType = 'car_4') {
    const pricing = VEHICLE_PRICING[vehicleType];
    return pricing.base + (distanceKm * pricing.perKm) + (durationMin * pricing.perMin);
}
```

### 3.4. Android - Thêm vehicle type selection

**File:** `BookingBottomSheet.kt`

```
Thêm row chọn loại xe trước khi tìm tài xế:

┌────────────────────────────────────────┐
│  🚗 Xe máy    │ 🚙 4 chỗ    │ 🚐 7 chỗ  │
│    42.000đ    │   64.000đ   │  91.000đ  │
└────────────────────────────────────────┘

Mỗi loại xe hiển thị:
- Icon tương ứng
- Giá ước tính cho tuyến đường hiện tại
- Active state với border màu PrimaryPurple
```

### 3.5. Android - Polling cho nearby drivers

```kotlin
// Trong PassengerHomeViewModel
fun startDriverSearch() {
    viewModelScope.launch {
        while (isSearching) {
            searchNearbyDrivers()
            delay(5000) // Refresh mỗi 5 giây
        }
    }
}

fun stopDriverSearch() {
    isSearching = false
}
```

### 3.6. Android - Hiển thị driver sau khi nhận

```kotlin
// Sau khi ride được accepted bởi driver
// PassengerHomeScreen cần poll ride status
// Khi status = "accepted" → hiển thị driver info card
LaunchedEffect(currentRide) {
    if (currentRide?.status == "accepted") {
        // Show driver info với animation
        showDriverAcceptedCard = true
    }
}
```

---

## 4. Implementation Steps

### Step 1: Fix duplicate route (15 phút)
```
1. Đọc rides.js, xác định 2 GET '/' routes
2. Xóa route thứ 2 (history endpoint)
3. Thêm route mới GET /active cho ride đang hoạt động
4. Test: GET /api/rides và GET /api/rides/active
```

### Step 2: Fix nearby drivers filter (30 phút)
```
1. Sửa locations.js, thêm Haversine trong SQL query
2. Thêm param radius vào request
3. Test: gọi API với lat/lng/radius khác nhau
```

### Step 3: Thêm vehicle type pricing (1 giờ)
```
1. Backend: Thêm trường vehicle_type vào rides table
2. Backend: Sửa pricing function theo vehicle type
3. Android: Thêm UI selector cho vehicle type
4. Android: Truyền vehicle_type khi tạo ride
5. Test: So sánh giá 3 loại xe cùng tuyến
```

### Step 4: Polling và driver info (45 phút)
```
1. Android: Thêm polling cho nearby drivers
2. Android: Thêm driver info card khi ride accepted
3. Android: Auto-refresh ride status mỗi 5 giây
4. Test: Đặt xe → thấy driver được assign
```

---

## 5. Testing Checklist

| Test Case | Kỳ vọng |
|---|---|
| TC-RIDE-01: Đặt xe thành công | Ride tạo với status="pending", price đúng |
| TC-RIDE-02: Tìm tài xế trong bán kính 5km | Chỉ thấy tài xế trong phạm vi |
| TC-RIDE-03: Tìm tài xế trong bán kính 10km | Thấy nhiều tài xế hơn |
| TC-RIDE-04: Chọn xe máy | Giá = 10k + 3k/km + 100/phút |
| TC-RIDE-05: Chọn ô tô 4 chỗ | Giá = 10k + 5k/km + 200/phút |
| TC-RIDE-06: Chọn ô tô 7 chỗ | Giá = 15k + 7k/km + 300/phút |
| TC-RIDE-07: Tài xế nhận chuyến | Passenger thấy thông tin tài xế |
| TC-RIDE-08: Driver offline không thấy trong tìm kiếm | is_available=false không xuất hiện |
| TC-RIDE-09: Polling nearby drivers | Danh sách cập nhật mỗi 5 giây |
| TC-RIDE-10: Giá Haversine Android = Backend | So sánh price trả về phải giống nhau |

---

## 6. Files affected

### Backend - Sửa
| File | Thay đổi |
|---|---|
| `rides.js` | Xóa duplicate route, thêm /active endpoint |
| `locations.js` | Thêm Haversine filter vào nearby-drivers |
| `rides.js` | Thêm vehicle_type và dynamic pricing |
| `schema.sql` | Thêm trường vehicle_type |

### Android - Sửa
| File | Thay đổi |
|---|---|
| `PassengerHomeViewModel.kt` | Thêm polling, vehicle type, driver info |
| `PassengerHomeScreen.kt` | Thêm vehicle selector, driver card |
| `BookingBottomSheet.kt` | Thêm loại xe selection |

### Android - Tạo mới
| File | Mục đích |
|---|---|
| `DriverAcceptedCard.kt` | Card hiển thị khi driver đã nhận |

---

## 7. Estimated time

- **Fix duplicate route**: 15 phút
- **Fix nearby drivers filter**: 30 phút
- **Vehicle type + pricing**: 60 phút
- **Polling + driver info**: 45 phút
- **Testing**: 30 phút

**Tổng: ~3 giờ**
