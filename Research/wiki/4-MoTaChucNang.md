# Mô tả chức năng chi tiết

---

## Mục lục

1. [Giới thiệu](#1-giới-thiệu)
2. [Màn hình ứng dụng (App Screens)](#2-màn-hình-ứng-dụng-app-screens)
3. [Chức năng theo nhóm người dùng](#3-chức-năng-theo-nhóm-người-dùng)
4. [Luồng nghiệp vụ chính](#4-luồng-nghiệp-vụ-chính)
5. [Công thức tính giá](#5-công-thức-tính-giá)

---

## 1. Giới thiệu

Chương này mô tả chi tiết các **màn hình giao diện** và **chức năng** của hệ thống theo từng nhóm người dùng, bao gồm khách hàng, tài xế và nhóm quản trị.

---

## 2. Màn hình ứng dụng (App Screens)

### 2.1. Tổng quan màn hình

```
Ứng dụng Khách hàng
├── SplashScreen              (Màn hình khởi động)
├── WelcomeScreen            (Màn hình chào, giới thiệu app)
├── LoginScreen              (Đăng nhập)
├── RegisterScreen           (Đăng ký)
├── ForgotPasswordScreen     (Quên mật khẩu)
├── PassengerHomeScreen      (Trang chủ - đặt xe)
│   ├── MapView              (Bản đồ hiển thị)
│   ├── BookingSheet         (Form đặt xe)
│   └── DriverListSheet      (Danh sách tài xế đề xuất)
├── RideDetailScreen         (Chi tiết chuyến đi)
├── ActiveRideScreen         (Chuyến đang diễn ra)
├── PaymentScreen            (Thanh toán)
├── InvoiceScreen           (Hóa đơn)
├── HistoryScreen            (Lịch sử chuyến đi)
├── RatingScreen             (Đánh giá tài xế)
├── AIScreen                 (Trợ lý du lịch AI)
│   ├── ChatView             (Giao diện chat với AI)
│   ├── RecommendationsView  (Gợi ý địa điểm)
│   └── ScheduleCreatorView  (Tạo lịch trình)
├── AIRecommendationsScreen  (Gợi ý cá nhân hóa)
├── AIProfileScreen          (Cài đặt AI cá nhân)
├── ProfileScreen            (Hồ sơ cá nhân)
├── EditProfileScreen        (Chỉnh sửa hồ sơ)
├── ChangePasswordScreen     (Đổi mật khẩu)
├── SupportScreen            (Hỗ trợ / Liên hệ)
└── NotificationScreen       (Thông báo)

Ứng dụng Tài xế
├── SplashScreen
├── LoginScreen
├── DriverHomeScreen        (Trang chủ tài xế)
│   ├── OnlineToggle        (Bật/tắt trạng thái online)
│   ├── RideRequestsList    (Danh sách yêu cầu đặt xe)
│   └── ActiveRideCard      (Chuyến đang thực hiện)
├── RideDetailScreen        (Chi tiết chuyến đi)
├── EarningsScreen          (Thu nhập)
│   ├── TodayTab
│   ├── WeekTab
│   ├── MonthTab
│   └── TotalTab
├── BatchOfferScreen        (Đề xuất gom chuyến AI)
├── DriverHistoryScreen     (Lịch sử chuyến đi)
├── ProfileScreen           (Hồ sơ tài xế)
└── DriverRatingScreen      (Xem đánh giá)
```

---

## 3. Chức năng theo nhóm người dùng

### 3.1. Nhóm Khách hàng

#### 3.1.1. Đăng nhập / Đăng ký

**Màn hình:** `LoginScreen`, `RegisterScreen`

| Trường | Kiểu | Bắt buộc | Validation |
|---|---|---|---|
| Họ tên | Text | Đăng ký | Tối thiểu 3 ký tự |
| Email | Email | Cả 2 | Định dạng email hợp lệ |
| Số điện thoại | Phone | Cả 2 | 10-11 số, bắt đầu bằng 0 |
| Mật khẩu | Password | Cả 2 | Tối thiểu 6 ký tự |
| Xác nhận mật khẩu | Password | Đăng ký | Phải khớp với mật khẩu |

**Validation:**
- Email phải là duy nhất trong hệ thống
- Số điện thoại phải là duy nhất trong hệ thống
- Mật khẩu được mã hóa bằng bcrypt trước khi lưu

**Trạng thái:**
- Đăng ký thành công → Chuyển đến `PassengerHomeScreen`
- Đăng nhập thành công → Tạo JWT token, lưu vào SharedPreferences → Chuyển đến màn hình tương ứng
- Đăng nhập thất bại → Hiển thị thông báo lỗi

#### 3.1.2. Đặt xe

**Màn hình:** `PassengerHomeScreen`

**Quy trình đặt xe:**

```
1. Chọn loại xe
   ├── Xe máy (mặc định)
   ├── Ô tô 4 chỗ
   └── Ô tô 7 chỗ

2. Nhập điểm đón
   ├── Tự động lấy vị trí hiện tại (GPS)
   ├── Hoặc nhập tay địa chỉ
   └── Hoặc chọn trên bản đồ

3. Nhập điểm đến
   ├── Nhập địa chỉ
   └── Hoặc chọn trên bản đồ

4. Hệ thống tính toán
   ├── Khoảng cách (km) - Haversine Formula
   ├── Thời gian ước tính (phút)
   └── Giá dự kiến (đ)

5. Hiển thị tài xế gợi ý
   ├── Danh sách 3-5 tài xế gần nhất
   ├── Thông tin: tên, xe, biển số, rating
   └── Chọn tài xế hoặc để hệ thống tự gán

6. Xác nhận đặt xe
   └── Nhấn nút "Đặt xe ngay"
```

**Công thức tính giá:**

```javascript
Giá = BASE_FARE + (distance_km × PRICE_PER_KM) + (duration_min × PRICE_PER_MIN)

// Theo đề cương:
BASE_FARE       = 10.000 đ
PRICE_PER_KM    =  5.000 đ/km
PRICE_PER_MIN   =    200 đ/phút

// Ví dụ: 10km, 20 phút
Giá = 10.000 + (10 × 5.000) + (20 × 200)
    = 10.000 + 50.000 + 4.000
    = 64.000 đ
```

#### 3.1.3. Theo dõi hành trình

**Màn hình:** `ActiveRideScreen`

| Trạng thái | Giao diện |
|---|---|
| `PENDING` | Animation tìm tài xế + danh sách tài xế đang nhận |
| `ACCEPTED` | Thông tin tài xế + nút gọi + bản đồ tracking |
| `ARRIVED` | Thông báo "Tài xế đã đến" + animation |
| `IN_PROGRESS` | Bản đồ hiển thị tuyến đường + ETA countdown |
| `COMPLETED` | Kết quả chuyến đi + chuyển đến màn hình thanh toán |

**Cập nhật vị trí:**
- Tài xế gửi GPS mỗi 5 giây
- Khách hàng cập nhật tracking mỗi 5 giây
- Dùng Google Maps Directions API để vẽ tuyến đường

#### 3.1.4. Thanh toán

**Màn hình:** `PaymentScreen`

| Phương thức | Mô tả |
|---|---|
| Tiền mặt | Thanh toán trực tiếp cho tài xế |
| Chuyển khoản | QR code / Banking |

**Luồng thanh toán:**
```
1. Chuyến đi hoàn thành (COMPLETED)
2. Hiển thị chi tiết hóa đơn
   ├── Quãng đường: X km
   ├── Thời gian: Y phút
   ├── Giá cước: Z đ
   └── Tổng cộng: T đ
3. Chọn phương thức thanh toán
4. Xác nhận thanh toán
5. Lưu vào bảng payments
6. Chuyển đến màn hình đánh giá
```

#### 3.1.5. Đánh giá

**Màn hình:** `RatingScreen`

| Thành phần | Mô tả |
|---|---|
| Star Rating | 1-5 sao |
| Comment | Nhận xét (tối đa 500 ký tự) |
| Tags gợi ý | Nhanh chóng, thân thiện, xe sạch, lái xe cẩn thận |

**Ràng buộc:**
- Rating là bắt buộc (1-5 sao)
- Comment là tùy chọn
- Sau khi đánh giá → Cập nhật `rating` của tài xế trong bảng `users`

#### 3.1.6. Trợ lý du lịch AI

**Màn hình:** `AIScreen`

| Tính năng | Mô tả |
|---|---|
| **Chat AI** | Giao diện hội thoại với AI, hỏi đáp thông tin du lịch |
| **Gợi ý địa điểm** | AI gợi ý địa điểm tham quan, nhà hàng, khách sạn |
| **Lịch trình AI** | Tạo lịch trình du lịch tối ưu với nhiều điểm đến |
| **Route Optimization** | Tối ưu tuyến đường (nhanh nhất / rẻ nhất / cân bằng) |
| **Cá nhân hóa** | AI học từ sở thích và lịch sử của người dùng |

**AI Chat Examples:**
```
Khách: "Gợi ý quán cafe đẹp ở Đà Nẵng"
AI: "Dựa trên đánh giá, tôi gợi ý:
     1. Quan Cafe - 4.8 sao - 2km từ vị trí bạn
     2. The S Coffee - 4.6 sao - 3.5km
     Bạn muốn đặt xe đến đâu?"

Khách: "Lên lịch trình du lịch Đà Nẵng 2 ngày"
AI: "Đây là lịch trình gợi ý:
     Ngày 1: Bãi Biển Mỹ Khê → Cầu Rồng → Phố cổ Hội An
     Ngày 2: Bà Nà Hills → Ngũ Hành Sơn → Ưu đãi ăn uống
     Tổng chi phí ước tính: 850.000đ
     Bạn muốn tôi tối ưu lịch trình này không?"
```

---

### 3.2. Nhóm Tài xế

#### 3.2.1. Quản lý trạng thái

**Màn hình:** `DriverHomeScreen`

| Trạng thái | Ý nghĩa | Tác động |
|---|---|---|
| `OFFLINE` | Không hoạt động | Không nhận chuyến |
| `ONLINE` | Sẵn sàng | Nhận thông báo chuyến mới |
| `BUSY` | Đang thực hiện chuyến | Không nhận chuyến khác |

**Toggle Online/Offline:**
- Nút gạt lớn trên màn hình chính
- Khi online → Gửi vị trí GPS liên tục
- Khi offline → Dừng gửi GPS

#### 3.2.2. Nhận yêu cầu chuyến đi

**Luồng:**
```
1. Hệ thống thông báo có yêu cầu mới
2. Hiển thị popup/card với thông tin:
   ├── Điểm đón (địa chỉ)
   ├── Điểm đến (địa chỉ)
   ├── Khoảng cách
   ├── Giá cước ước tính
   └── Thời gian chờ khách
3. Tài xế chọn:
   ├── "Nhận chuyến" → ACCEPTED
   └── "Bỏ qua" → Tiếp tục nhận chuyến khác
```

#### 3.2.3. Thực hiện chuyến đi

**Cập nhật trạng thái (tài xế):**

| Hành động | Trạng thái | Mô tả |
|---|---|---|
| Bắt đầu di chuyển | `ACCEPTED` → `ARRIVED` | Đã đến điểm đón |
| Đón khách | `ARRIVED` → `IN_PROGRESS` | Bắt đầu chuyến đi |
| Hoàn thành | `IN_PROGRESS` → `COMPLETED` | Đã đến điểm đến |

**Tính năng:**
- Điều hướng đến điểm đón (Google Maps Navigation)
- Gọi điện cho khách
- Nhắn tin cho khách
- Xem thông tin khách (tên, số điện thoại)

#### 3.2.4. Xem thu nhập

**Màn hình:** `EarningsScreen`

| Tab | Dữ liệu |
|---|---|
| Hôm nay | Tổng thu nhập hôm nay + danh sách chuyến |
| Tuần này | Tổng tuần + so sánh với tuần trước |
| Tháng này | Tổng tháng + biểu đồ xu hướng |
| Tổng cộng | Tổng tất cả thu nhập |

**Biểu đồ:**
- Biểu đồ cột: Thu nhập theo ngày
- Biểu đồ đường: Xu hướng thu nhập

#### 3.2.5. Đề xuất gom chuyến AI

**Màn hình:** `BatchOfferScreen`

| Thông tin | Mô tả |
|---|---|
| Tên batch | AI tự đặt hoặc mặc định |
| Số khách | Số lượng khách trong batch |
| Tổng doanh thu | Tổng cộng tất cả chuyến |
| Tổng khoảng cách | Quãng đường tối ưu |
| Efficiency Score | Điểm hiệu quả (0-100) |
| Thời gian bắt đầu/ kết thúc ước tính |

**Quyết định:**
- **Chấp nhận** → Batch chuyển sang `ACCEPTED`, gán các chuyến vào
- **Từ chối** → Batch chuyển sang `REJECTED`, chuyến giữ nguyên

---

### 3.3. Nhóm Quản trị (Admin)

#### 3.3.1. Dashboard

| Chỉ số | Mô tả |
|---|---|
| Tổng số chuyến hôm nay | Số chuyến hoàn thành trong ngày |
| Doanh thu hôm nay | Tổng doanh thu ngày |
| Số tài xế online | Tài xế đang hoạt động |
| Số khách đặt xe | Khách hàng đặt xe hôm nay |
| Biểu đồ tuần | Chuyến đi 7 ngày gần nhất |
| Biểu đồ tháng | Doanh thu 30 ngày gần nhất |

#### 3.3.2. Quản lý người dùng

| Chức năng | Mô tả |
|---|---|
| Danh sách tài khoản | Phân trang, tìm kiếm, lọc theo loại |
| Thêm tài khoản | Tạo tài khoản mới |
| Sửa tài khoản | Cập nhật thông tin |
| Khóa/ Mở tài khoản | Vô hiệu hóa hoặc kích hoạt |
| Xem chi tiết | Xem lịch sử hoạt động |

#### 3.3.3. Quản lý doanh thu

| Chức năng | Mô tả |
|---|---|
| Danh sách giao dịch | Tất cả các khoản thanh toán |
| Thống kê theo thời gian | Ngày / Tuần / Tháng / Năm |
| Xuất báo cáo | Export Excel / PDF |
| Biểu đồ trực quan | Cột, đường, tròn |

---

## 4. Luồng nghiệp vụ chính

### 4.1. Luồng đặt xe hoàn chỉnh

```
┌──────────────────────────────────────────────────────────────┐
│                     LUỒNG ĐẶT XE                              │
├──────────────────────────────────────────────────────────────┤
│                                                               │
│  KHÁCH HÀNG              HỆ THỐNG              TÀI XẾ       │
│                                                               │
│       │                          │                      │       │
│       │  1. Mở app              │                      │       │
│       │─────────────────────────▶│                      │       │
│       │                          │                      │       │
│       │  2. Chọn điểm đón       │                      │       │
│       │─────────────────────────▶│                      │       │
│       │                          │                      │       │
│       │  3. Chọn điểm đến       │                      │       │
│       │─────────────────────────▶│                      │       │
│       │                          │                      │       │
│       │                          │ 4. Tính giá          │       │
│       │                          │──────────────────────│       │
│       │                          │                      │       │
│       │  5. Hiển thị giá        │                      │       │
│       │◀─────────────────────────│                      │       │
│       │                          │                      │       │
│       │  6. Nhấn "Đặt xe"       │                      │       │
│       │─────────────────────────▶│                      │       │
│       │                          │                      │       │
│       │                          │ 7. Tạo ride (PENDING)│       │
│       │                          │──────────────────────│       │
│       │                          │                      │       │
│       │                          │ 8. Thông báo tài xế  │       │
│       │                          │──────────────────────────────▶│
│       │                          │                      │       │
│       │                          │                      │ 9. Nhận thông báo
│       │                          │                      │       │
│       │                          │                      │ 10. Xem chi tiết
│       │                          │                      │       │
│       │                          │                      │ 11. Nhận/ Từ chối
│       │                          │◀──────────────────────────────│
│       │                          │                      │       │
│       │  12. Thông báo tài xế   │                      │       │
│       │◀─────────────────────────│                      │       │
│       │                          │                      │       │
│       │  13. Tracking realtime   │                      │       │
│       │◀─────────────────────────│◀──────────────────────────────│
│       │                          │                      │       │
│       │  14. Tài xế đến đón     │                      │       │
│       │◀─────────────────────────│◀──────────────────────────────│
│       │                          │                      │       │
│       │  15. Đang di chuyển      │                      │       │
│       │◀─────────────────────────│◀──────────────────────────────│
│       │                          │                      │       │
│       │  16. Hoàn thành          │                      │       │
│       │◀─────────────────────────│◀──────────────────────────────│
│       │                          │                      │       │
│       │  17. Thanh toán          │                      │       │
│       │─────────────────────────▶│                      │       │
│       │                          │                      │       │
│       │  18. Đánh giá            │                      │       │
│       │─────────────────────────▶│──────────────────────│       │
│       │                          │                      │       │
└──────────────────────────────────────────────────────────────┘
```

### 4.2. Luồng gom chuyến AI

```
1. Hệ thống phân tích các chuyến đang chờ trong khu vực 5km
       │
       ▼
2. AI tìm các chuyến có tuyến đường gần nhau
       │
       ▼
3. Tính toán hiệu quả:
   - Tổng doanh thu = Σ(giá từng chuyến)
   - Tổng khoảng cách = quãng đường tối ưu
   - Detour km = khoảng cách thêm do gom chuyến
   - Efficiency = (Tổng doanh thu) / (Tổng km)
       │
       ▼
4. Nếu Efficiency Score >= 70 → Tạo Batch đề xuất
       │
       ▼
5. Gửi Batch đến tài xế có:
   - Đánh giá cao (>= 4.5 sao)
   - Kinh nghiệm nhiều (>= 50 chuyến)
   - Vị trí gần điểm bắt đầu batch
       │
       ▼
6. Tài xế xem và quyết định
       │
   ┌───┴───┐
   │       │
 ACKed   REJECT
   │       │
   ▼       ▼
Gán batch  Hủy batch,
           chuyến riêng
```

---

## 5. Công thức tính giá

### 5.1. Công thức chuẩn

```
Giá_tổng = BASE_FARE + (distance_km × PRICE_PER_KM) + (duration_min × PRICE_PER_MIN)
```

### 5.2. Bảng giá theo loại xe

| Loại xe | Base Fare | /km | /phút | Ví dụ 10km/20phút |
|---|---|---|---|---|
| Xe máy | 10.000đ | 3.000đ | 100đ | 10.000 + 30.000 + 2.000 = **42.000đ** |
| Ô tô 4 chỗ | 10.000đ | 5.000đ | 200đ | 10.000 + 50.000 + 4.000 = **64.000đ** |
| Ô tô 7 chỗ | 15.000đ | 7.000đ | 300đ | 15.000 + 70.000 + 6.000 = **91.000đ** |

### 5.3. Phụ phí

| Loại phụ phí | Giá trị | Điều kiện |
|---|---|---|
| Phí cao điểm | +20% giá | 7:00-9:00 và 17:00-19:00 các ngày làm việc |
| Phí đêm | +10% giá | 22:00-06:00 |
| Phí đường cao tốc | Thực tế | Có sử dụng đường cao tốc |
| Phí chờ | 2.000đ/5phút | Khách yêu cầu dừng đợi |

### 5.4. Tính khoảng cách (Haversine Formula)

```javascript
function haversineDistance(lat1, lon1, lat2, lon2) {
    const R = 6371; // Bán kính trái đất (km)
    const dLat = toRad(lat2 - lat1);
    const dLon = toRad(lon2 - lon1);
    const a = Math.sin(dLat/2) * Math.sin(dLat/2) +
              Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) *
              Math.sin(dLon/2) * Math.sin(dLon/2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    return R * c;
}
```

---

## Liên kết

- [ Quay lại: Phân tích & Thiết kế](./3-PhanTichThietKe.md)
- [ Tiếp theo: Thiết kế Cơ sở dữ liệu](./5-ThietKeCoSoDuLieu.md)
