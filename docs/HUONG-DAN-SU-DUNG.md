# Hướng Dẫn Sử Dụng Ứng Dụng Đặt Xe Thông Minh DoAn3

> **Dự án:** Ứng dụng đặt xe thông minh kết hợp trợ lý du lịch AI
> **Nền tảng:** Android Native (Kotlin + Jetpack Compose)
> **Ngày cập nhật:** 2026-05-21

---

## Mục lục

1. [Luồng khởi động (Splash)](#1-luồng-khởi-động-splash)
2. [Xác thực - Đăng nhập / Đăng ký](#2-xác-thực---đăng-nhập--đăng-ký)
3. [Quên mật khẩu - OTP - Đặt lại mật khẩu](#3-quên-mật-khẩu---otp---đặt-lại-mật-khẩu)
4. [Đặt xe - Hành khách](#4-đặt-xe---hành-khách)
5. [Nhận chuyến - Tài xế](#5-nhận-chuyến---tài-xế)
6. [Chi tiết chuyến đi & Thanh toán & Đánh giá](#6-chi-tiết-chuyến-đi--thanh-toán--đánh-giá)
7. [Lịch sử chuyến đi](#7-lịch-sử-chuyến-đi)
8. [Hồ sơ người dùng](#8-hồ-sơ-người-dùng)
9. [Tính năng AI - Lịch trình](#9-tính-năng-ai---lịch-trình)
10. [Tính năng AI - Chat Assistant](#10-tính-năng-ai---chat-assistant)
11. [Tính năng AI - Đề xuất & Hồ sơ AI](#11-tính-năng-ai---đề-xuất--hồ-sơ-ai)
12. [Thu nhập tài xế](#12-thu-nhập-tài-xế)
13. [Chuyến ghép (Batch)](#13-chuyến-ghép-batch)
14. [Sơ đồ tổng quan kiến trúc](#14-sơ-đồ-tổng-quan-kiến-trúc)
15. [Cấu trúc Database](#15-cấu-trúc-database)

---

## 1. Luồng khởi động (Splash)

### Mô tả
Khi người dùng mở ứng dụng, màn hình Splash hiển thị logo có animation. Trong lúc đó, ứng dụng kiểm tra trạng thái đăng nhập.

### Cách dùng
- Mở ứng dụng → tự động chuyển đến Splash Screen → kiểm tra session → chuyển tiếp

### Luồng file

```
MainActivity.kt
  │
  ├─► SplashScreen.kt (composables)
  │      │
  │      └─► SplashViewModel (kiểm tra SessionManager)
  │              │
  │              └─► SessionManager.kt (SharedPreferences)
  │                     │
  │                     ├─► Có session hợp lệ ──► MainViewModel ──► AppNavigation
  │                     │         (isLoggedIn == true)        ──► PassengerHomeScreen
  │                     │                                      hoặc DriverHomeScreen
  │                     │
  │                     └─► Không có session ──► AuthScreen
```

### Kết quả ra
| Trường hợp | Kết quả |
|------------|---------|
| Đã đăng nhập + Passenger | Chuyển đến `PassengerHomeScreen` |
| Đã đăng nhập + Driver | Chuyển đến `DriverHomeScreen` |
| Chưa đăng nhập | Chuyển đến `AuthScreen` |

### Chi tiết các file tham gia

| File | Vai trò | Mô tả |
|------|---------|--------|
| `MainActivity.kt` | Entry point | Khởi tạo Compose, gọi `SplashDestination` |
| `SplashScreen.kt` | UI | Hiển thị logo animated, loading indicator |
| `AppNavigation.kt` | Navigation | Định nghĩa NavHost, `MainViewModel`, điều hướng theo userType |
| `SessionManager.kt` | Local Storage | Đọc SharedPreferences: `isLoggedIn`, `userType`, `authToken` |

---

## 2. Xác thực - Đăng nhập / Đăng ký

### Mô tả
Màn hình kết hợp Login và Register trong một giao diện với tab chuyển đổi. Người dùng có thể chọn đăng nhập với vai trò **Passenger** (Hành khách) hoặc **Driver** (Tài xế).

### Cách dùng

#### Đăng nhập (Tab Login - mặc định)
1. Nhập **Email** → nhấn Tab hoặc nhấn vào trường Password
2. Nhập **Password** (tối thiểu 6 ký tự)
3. Nhấn nút **Login** màu gradient
4. Nếu đăng nhập thành công → chuyển đến màn hình chính

#### Đăng ký (Tab Register)
1. Nhấn tab **Register**
2. Chọn loại tài khoản: **Passenger** (Khách hàng) hoặc **Driver** (Tài xế)
   - Nếu chọn **Driver**: hiển thị thêm phần chọn loại xe, mẫu xe và biển số
3. Nhập **Full Name** (bắt buộc)
4. Nhập **Email** (format hợp lệ)
5. Nhập **Phone Number** (tối thiểu 10 số)
6. Nhập **Password** (tối thiểu 6 ký tự) → hiển thị chỉ báo độ mạnh mật khẩu
7. Nhập **Confirm Password** (phải khớp với password)
8. Nếu là **Tài xế**, chọn thêm:
   - **Loại phương tiện**: Xe máy / Ô tô 4 chỗ / Ô tô 7 chỗ
   - **Mẫu xe** (VD: Toyota Camry) - bắt buộc
   - **Biển số xe** (VD: 43A-123.45) - bắt buộc
9. Nhấn **Create Account**

#### Quên mật khẩu
- Nhấn link **"Forgot Password?"** bên dưới form Login → chuyển đến `ForgotPasswordScreen`

### Luồng file - Đăng nhập

```
AuthScreen.kt (UI)
  │
  ├─► AuthViewModel.login()
  │      │
  │      ├─► Validate form (email format, password length)
  │      │      │
  │      │      └─► Form có lỗi? ──► Cập nhật LoginFormState.emailError/passwordError
  │      │                                        ──► Hiển thị lỗi trên UI
  │      │
  │      └─► Form hợp lệ ──► AuthRepository.login(email, password)
  │                             │
  │                             ├─► AuthInterceptor.kt (thêm Authorization header)
  │                             │
  │                             └─► ApiService.kt → Retrofit
  │                                    │
  │                                    ├─► POST /api/auth/login
  │                                    │
  │                                    └─► Database: users (MySQL)
  │                                           Kiểm tra: email + password (bcrypt hash)
  │                                           Trả về: JWT token + user info
  │                             │
  │                             ├─► Thành công ──► AuthRepository.login()
  │                             │      │
  │                             │      ├─► SessionManager.saveSession()
  │                             │      │     (SharedPreferences: token, userId, userName, userType)
  │                             │      │
  │                             │      ├─► SocketManager.connect(token)
  │                             │      │     (Socket.IO: kết nối realtime)
  │                             │      │
  │                             │      └─► AuthState.isSuccess = true
  │                             │             ──► onAuthSuccess(userType) → AppNavigation
  │                             │
  │                             └─► Thất bại ──► AuthState.errorMessage
  │                                    ──► Snackbar hiển thị lỗi
```

### Luồng file - Đăng ký

```
AuthScreen.kt (UI)
  │
  ├─► AuthViewModel.register()
  │      │
  │      ├─► Validate form (name, email, phone, password, confirmPassword)
  │      │
  │      └─► Form hợp lệ ──► AuthRepository.register()
  │                             │
  │                             └─► ApiService.kt → Retrofit
  │                                    │
  │                                    ├─► POST /api/auth/register
  │                                    │
  │                                    └─► Database: users (MySQL)
  │                                           INSERT: email, password (bcrypt), name, phone, user_type
  │                                           Kiểm tra: email UNIQUE constraint
  │                                           Trả về: JWT token + user info
  │                             │
  │                             ├─► Thành công ──► AuthRepository.register()
  │                             │      │
  │                             │      ├─► SessionManager.saveSession()
  │                             │      ├─► SocketManager.connect(token)
  │                             │      └─► AuthState.isSuccess = true
  │                             │
  │                             └─► Thất bại (email tồn tại) ──► AuthState.errorMessage
```

### Kết quả ra

| Hành động | Kết quả | Database |
|-----------|---------|----------|
| Login thành công | Lưu session, kết nối Socket.IO, chuyển màn hình | Kiểm tra `users.email` + `users.password` |
| Login thất bại | Snackbar báo lỗi | Không thay đổi |
| Register thành công | Tạo tài khoản, lưu session, kết nối Socket.IO | INSERT vào `users` |
| Register thất bại (email tồn tại) | Snackbar báo lỗi | Không thay đổi |

### Chi tiết các file tham gia

| File | Vai trò | Mô tả |
|------|---------|--------|
| `AuthScreen.kt` | UI + ViewModel | Giao diện Login/Register, quản lý form state, animation |
| `AuthViewModel` | Logic | Validate, gọi `authRepository.login()` / `register()` |
| `AuthRepository.kt` | Repository | Gọi API, xử lý response, tương tác SessionManager |
| `SessionManager.kt` | Local Storage | Lưu/đọc SharedPreferences (token, userId, userType, userName) |
| `AuthInterceptor.kt` | OkHttp Interceptor | Tự động thêm `Authorization: Bearer <token>` vào request |
| `ApiService.kt` | API Interface | Định nghĩa `POST /auth/login`, `POST /auth/register` |
| `SocketManager.kt` | Realtime | Kết nối Socket.IO sau khi login thành công |
| `AppModule.kt` | DI | Cung cấp tất cả dependency (Hilt) |

---

## 3. Quên mật khẩu - OTP - Đặt lại mật khẩu

### Mô tả
Quy trình 4 bước để khôi phục mật khẩu qua email và mã OTP 6 chữ số.

### Cách dùng

#### Bước 1: Nhập Email (ForgotPasswordScreen)
1. Nhập **email đã đăng ký**
2. Nhấn **Send OTP**
3. Đợi email chứa mã 6 chữ số (hoặc dev OTP hiển thị trên màn hình)
4. Chuyển đến `OtpVerificationScreen`

#### Bước 2: Xác minh OTP (OtpVerificationScreen)
1. Nhập **6 chữ số OTP** (tự động chuyển ô khi nhập đủ)
2. Nhấn **Verify** hoặc đợi auto-verify khi nhập đủ 6 số
3. Nếu đúng → chuyển đến `ResetPasswordScreen`
4. Nếu sai → hiển thị lỗi
5. Nhấn **Resend OTP** (sau 60 giây chờ)

#### Bước 3: Đặt lại mật khẩu (ResetPasswordScreen)
1. Nhập **New Password** (tối thiểu 6 ký tự) → hiển thị chỉ báo độ mạnh
2. Nhập **Confirm Password** → hiển thị "Passwords match" khi khớp
3. Nhấn **Reset Password**
4. Thành công → chuyển về `AuthScreen` để đăng nhập

### Luồng file - Quên mật khẩu

```
ForgotPasswordScreen.kt (UI)
  │
  ├─► ForgotPasswordViewModel.sendOtp(email)
  │      │
  │      ├─► Validate email format
  │      │
  │      └─► PasswordResetRepository.sendOtp(email)
  │             │
  │             └─► ApiService.kt → POST /api/auth/forgot-password
  │                    │
  │                    └─► Database: password_resets (MySQL)
  │                           INSERT: email, otp_code (6 chữ số), expires_at (10 phút)
  │                           Gửi email OTP qua SMTP (Nodemailer)
  │                           Trả về: message + devOtp (trong dev mode)
  │             │
  │             ├─► Thành công ──► ForgotPasswordState.emailSent = true
  │             │      ──► onEmailSent(email, devOtp) → OtpVerificationScreen
  │             │
  │             └─► Thất bại ──► Snackbar lỗi
```

### Luồng file - Xác minh OTP

```
OtpVerificationScreen.kt (UI)
  │
  ├─► OtpVerificationViewModel.verifyOtp(email, otp)
  │      │
  │      ├─► Validate (6 chữ số)
  │      │
  │      └─► PasswordResetRepository.verifyOtp(email, otp)
  │             │
  │             └─► ApiService.kt → POST /api/auth/verify-otp
  │                    │
  │                    └─► Database: password_resets (MySQL)
  │                           Kiểm tra: email + otp_code + chưa hết hạn + chưa sử dụng
  │                           Trả về: success/error
  │             │
  │             ├─► Thành công ──► OtpVerificationState.isVerified = true
  │             │      ──► onVerified(email, otp) → ResetPasswordScreen
  │             │
  │             └─► Thất bại ──► Snackbar "Invalid OTP"
  │
  └─► OtpVerificationViewModel.resendOtp(email)
         │
         └─► PasswordResetRepository.resendOtp(email)
                │
                └─► ApiService.kt → POST /api/auth/resend-otp
                       │
                       └─► Database: password_resets (MySQL)
                              Cập nhật: otp_code mới, expires_at mới
                              Gửi email lại
```

### Luồng file - Đặt lại mật khẩu

```
ResetPasswordScreen.kt (UI)
  │
  ├─► ResetPasswordViewModel.resetPassword(email, otp, newPassword)
  │      │
  │      ├─► Validate (password >= 6 ký tự, passwords match)
  │      │
  │      └─► PasswordResetRepository.resetPassword(email, otp, newPassword)
  │             │
  │             └─► ApiService.kt → POST /api/auth/reset-password
  │                    │
  │                    └─► Database:
  │                           password_resets: đánh dấu OTP đã sử dụng
  │                           users: cập nhật password (bcrypt hash mới)
  │             │
  │             ├─► Thành công ──► ResetPasswordState.isSuccess = true
  │             │      ──► onSuccess() → AuthScreen
  │             │
  │             └─► Thất bại ──► Snackbar lỗi
```

### Kết quả ra

| Bước | Kết quả | Database |
|------|---------|----------|
| Send OTP | Email chứa mã OTP (hoặc dev OTP hiển thị màn hình) | INSERT `password_resets` |
| Verify OTP | Xác minh thành công → bước tiếp theo | Kiểm tra `password_resets` |
| Resend OTP | Gửi lại mã mới, đếm ngược 60s | UPDATE `password_resets.otp_code` |
| Reset Password | Cập nhật mật khẩu mới → về Auth | UPDATE `users.password` |

### Chi tiết các file tham gia

| File | Vai trò | Mô tả |
|------|---------|--------|
| `ForgotPasswordScreen.kt` | UI | Form nhập email, nút Send OTP |
| `ForgotPasswordViewModel` | Logic | Validate email, gọi `sendOtp()` |
| `OtpVerificationScreen.kt` | UI | 6 ô nhập OTP, countdown timer, resend |
| `OtpVerificationViewModel` | Logic | `verifyOtp()`, `resendOtp()`, countdown timer |
| `ResetPasswordScreen.kt` | UI | Form nhập password mới |
| `ResetPasswordViewModel` | Logic | `resetPassword()` |
| `PasswordResetRepository.kt` | Repository | Gọi 4 API endpoints cho quy trình reset |
| `PasswordResetDto.kt` | DTO | Các request/response model |
| `ApiService.kt` | API Interface | 4 endpoints: forgot-password, verify-otp, reset-password, resend-otp |

---

## 4. Đặt xe - Hành khách

### Mô tả
Màn hình chính của hành khách với bản đồ Google Maps, cho phép chọn điểm đón/đến, chọn loại xe, tìm tài xế gần đó và đặt xe.

### Cách dùng

#### Đặt xe nhanh
1. Nhấn **"Đặt xe"** trên bản đồ hoặc card ở dưới
2. **Bottom Sheet** mở ra → nhập **Điểm đón**
3. Nhập **Điểm đến**
4. *(Tùy chọn)* Chọn địa điểm phổ biến từ danh sách gợi ý
5. Chọn **loại phương tiện**: Xe máy / Ô tô 4 chỗ / Ô tô 7 chỗ
6. Nhấn **Tìm tài xế** → ứng dụng liên tục tìm tài xế trong bán kính 5km
7. Danh sách **tài xế gần đó** hiển thị (tối đa 3)
8. Chọn một tài xế → hiển thị **chi tiết giá** (cước cơ bản, phí km, phí thời gian)
9. Nhấn **Đặt xe ngay** → tạo chuyến đi

#### Các nút trên màn hình
- **🔔 Icon AI Chat**: Mở `AIChatScreen` - trợ lý AI
- **📅 Icon Lịch trình**: Mở `AIScheduleScreen` - lịch trình AI
- **📜 Icon Lịch sử**: Mở `HistoryScreen`
- **👤 Icon Profile**: Mở `ProfileScreen`
- **🚪 Icon Logout**: Đăng xuất

### Luồng file - Tìm tài xế gần đó

```
PassengerHomeScreen.kt (UI - BookingBottomSheet)
  │
  ├─► PassengerHomeViewModel.startDriverSearch()
  │      │
  │      ├─► Cập nhật isSearchingDrivers = true
  │      │
  │      ├─► Vòng lặp (5 giây/lần) ──► RideRepository.getNearbyDrivers(lat, lng)
  │      │      │
  │      │      └─► ApiService.kt → GET /api/users/drivers/nearby
  │      │             │
  │      │             └─► Database: drivers + driver_locations (MySQL)
  │      │                    Tính khoảng cách từ pickup đến driver.location
  │      │                    Lọc: bán kính 5km, driver.is_available = true
  │      │                    Trả về: danh sách DriverDto
  │      │      │
  │      │      ├─► Cập nhật state.nearbyDrivers
  │      │      └─► Hiển thị danh sách tài xế trên UI
  │      │
  │      └─► Khi người dùng nhấn "Dừng tìm":
  │             PassengerHomeViewModel.stopDriverSearch()
  │             ──► Hủy vòng lặp, isSearchingDrivers = false
  │             ──► SocketManager.leaveRide(rideId) (nếu có ride)
```

### Luồng file - Tính giá (Client-side)

```
PassengerHomeViewModel.calculateEstimate()
  │
  ├─► Tính khoảng cách Haversine (pickup ↔ destination)
  │      R = 6371km, d = R × 2 × atan2(√sin²(Δlat/2) + ... , √(1-sin²(...)))
  │      ──► distanceKm
  │      ──► durationMin = (distanceKm / 30) × 60
  │
  ├─► Áp dụng bảng giá theo loại xe:
  │      ┌─────────────┬──────────┬───────────┬──────────┐
  │      │ Loại xe     │ Cước cơ bản│ Phí/km   │ Phí/phút │
  │      ├─────────────┼──────────┼───────────┼──────────┤
  │      │ Xe máy      │ 10,000đ  │ 3,000đ    │ 100đ     │
  │      │ Ô tô 4 chỗ  │ 12,000đ  │ 5,000đ    │ 200đ     │
  │      │ Ô tô 7 chỗ  │ 15,000đ  │ 7,000đ    │ 300đ     │
  │      └─────────────┴──────────┴───────────┴──────────┘
  │
  ├─► totalPrice = baseFare + (distanceKm × pricePerKm) + (durationMin × pricePerMin)
  │
  └─► Cập nhật state.pricing (PricingInfo)
         ──► Hiển thị trên UI: tổng giá, km, phút, chi tiết giá
```

### Luồng file - Đặt xe

```
PassengerHomeScreen.kt (nhấn nút "Đặt xe ngay")
  │
  ├─► PassengerHomeViewModel.requestRide()
  │      │
  │      ├─► Cập nhật isLoading = true, isSearchingDrivers = false
  │      │
  │      └─► RideRepository.requestRide()
  │             │
  │             └─► ApiService.kt → POST /api/rides/request
  │                    Body: { pickupLat, pickupLng, destLat, destLng, vehicleType }
  │                    │
  │                    └─► Database: rides (MySQL)
  │                           INSERT: passenger_id, pickup/dest lat/lng, vehicle_type, status='pending'
  │                           Tính price từ Google Distance Matrix API (hoặc ước tính)
  │                           Trả về: RideDto
  │             │
  │             ├─► Thành công ──► RideDto
  │             │      │
  │             │      ├─► state.currentRide = ride
  │             │      ├─► state.isRideActive = true
  │             │      ├─► SocketManager.joinRide(rideId)
  │             │      │     (Socket event: join:ride → server)
  │             │      ├─► SocketManager.requestDriverLocation(rideId)
  │             │      │     (Socket event: request:driver_location → server)
  │             │      └─► Bottom Sheet hiển thị "Đang tìm tài xế..."
  │             │
  │             └─► Thất bại ──► Snackbar lỗi
```

### Realtime - Cập nhật vị trí tài xế (Socket.IO)

```
Backend (Socket.IO Server)
  │
  ├─► Nhận event: location:update từ tài xế
  │      { lat, lng, rideId }
  │
  ├─► Cập nhật driver_locations trong Database
  │      (MySQL: INSERT driver_locations)
  │
  └─► Emit event: driver:location → passenger
       { lat, lng, rideId, timestamp }
         │
         └─► SocketManager.kt (Android)
                │
                └─► driverLocationFlow (SharedFlow)
                      │
                      └─► PassengerHomeViewModel.init { SocketManager.driverLocationFlow.collect {...} }
                             │
                             ├─► Nếu update.rideId == currentRide.id
                             │      Cập nhật state.driverLat, state.driverLng
                             │      TaxiMapView hiển thị marker tài xế di chuyển realtime
                             │
                             └─► Nếu rideId không khớp → bỏ qua
```

### Realtime - Cập nhật trạng thái chuyến đi

```
Backend
  │
  ├─► Tài xế nhấn nút cập nhật trạng thái
  │      ──► API PUT /api/driver/ride/:id/status
  │      ──► Database: rides.status = newStatus
  │      ──► Emit: ride:status:changed → passenger
  │           { rideId, status, timestamp }
  │
  └─► Passenger (Android)
       │
       └─► SocketManager.rideStatusFlow (SharedFlow)
             │
             └─► PassengerHomeViewModel
                    │
                    ├─► Cập nhật state.currentRide.status
                    ├─► Cập nhật state.isRideActive
                    └─► Nếu completed/cancelled: xóa driverLat/Lng
```

### Kết quả ra

| Hành động | Kết quả | Database |
|-----------|---------|----------|
| Nhập pickup/dest | Tính giá ước tính hiển thị | Không thay đổi |
| Tìm tài xế | Danh sách tài xế trong bán kính 5km | Query `drivers` + `driver_locations` |
| Đặt xe | Tạo ride → chờ tài xế nhận | INSERT `rides` (status=pending) |
| Tài xế nhận | Trạng thái chuyển sang "accepted" | UPDATE `rides` |
| Realtime | Marker tài xế di chuyển trên bản đồ | Cập nhật `driver_locations` liên tục |

### Chi tiết các file tham gia

| File | Vai trò | Mô tả |
|------|---------|--------|
| `PassengerHomeScreen.kt` | UI + Bottom Sheet | Bản đồ, booking form, danh sách tài xế |
| `PassengerHomeViewModel` | Logic | Tìm tài xế, tính giá, đặt xe, lắng nghe Socket.IO |
| `RideRepository.kt` | Repository | `requestRide()`, `getNearbyDrivers()` |
| `SocketManager.kt` | Realtime | `joinRide()`, `leaveRide()`, `requestDriverLocation()`, `driverLocationFlow`, `rideStatusFlow` |
| `ApiService.kt` | API Interface | `POST /rides/request`, `GET /users/drivers/nearby` |
| `MapComponents.kt` (TaxiMapView) | UI Map | Hiển thị bản đồ, marker pickup (xanh), dropoff (đỏ), driver (xanh dương) |
| `DataModels.kt` | DTO | `RideDto`, `DriverDto`, `VehicleType` enum |
| `SessionManager.kt` | Auth | Lấy `userId` từ SharedPreferences |

---

## 5. Nhận chuyến - Tài xế

### Mô tả
Màn hình dashboard dành cho tài xế với 3 tab: **Chuyến mới** (available rides), **Đang chạy** (current ride), **Batch** (chuyến ghép).

### Cách dùng

#### Bật/Tắt trạng thái Online
1. Nhấn **nút tròn** trên card "Bạn đang online/offline"
2. Khi bật online:
   - `DriverHomeViewModel.toggleOnlineStatus()` → gọi `updateDriverStatus(true)`
   - Tài xế nhận thông báo từ backend về chuyến mới
   - Backend cập nhật `drivers.is_available = true`

#### Tab "Chuyến mới" (Chỉ khi online)
1. Danh sách chuyến đang chờ hiển thị
2. Mỗi chuyến gồm: điểm đón, điểm đến, giá, khoảng cách, loại xe
3. Nhấn **Nhận** → chuyển sang tab "Đang chạy" với trạng thái "accepted"
4. Nhấn **Từ chối** → xóa khỏi danh sách

#### Tab "Đang chạy"
1. Hiển thị chuyến đang thực hiện với 4 nút hành động theo trạng thái:

| Trạng thái hiện tại | Nút hành động | Trạng thái mới |
|---------------------|---------------|----------------|
| `accepted` | "Đã đến điểm đón" | `arrived` |
| `arrived` | "Bắt đầu chuyến" | `in_progress` |
| `in_progress` | "Hoàn thành chuyến" | `completed` |
| *(bất kỳ)* | "Hủy chuyến" | `cancelled` |

2. Nút **"Đến điểm đón"** / **"Đến điểm đến"** → mở Google Maps Navigation
3. Nút **"Xem chi tiết"** → chuyển đến `RideDetailScreen`

#### Realtime - Gửi vị trí GPS

```
DriverHomeScreen.kt (khi có currentRide)
  │
  └─► SocketManager.emitLocationUpdate(lat, lng, rideId)
         │
         └─► Socket event: location:update
                { lat, lng, rideId }
                    │
                    └─► Backend
                           │
                           ├─► INSERT driver_locations (MySQL)
                           └─► Emit driver:location → passenger
                                (qua SocketManager.driverLocationFlow)
```

### Luồng file - Cập nhật trạng thái online

```
DriverHomeScreen.kt (nhấn toggle)
  │
  ├─► DriverHomeViewModel.toggleOnlineStatus()
  │      │
  │      ├─► repository.updateDriverStatus(isAvailable=true/false, lat, lng)
  │      │      │
  │      │      └─► ApiService.kt → PUT /api/driver/status
  │      │             Body: { is_available, latitude, longitude }
  │      │             │
  │      │             └─► Database: drivers (MySQL)
  │      │                    UPDATE is_available, latitude, longitude
  │      │
  │      ├─► isOnline = true → loadAvailableRides() + loadEarnings()
  │      │      │
  │      │      └─► DriverRepository.getAvailableRides()
  │      │             │
  │      │             └─► ApiService.kt → GET /api/driver/ride/available
  │      │                    │
  │      │                    └─► Database: rides (MySQL)
  │      │                           Lọc: status='pending', trong bán kính
  │      │                           Trả về: danh sách RideDto
  │      │
  │      └─► isOnline = false → dừng nhận chuyến
```

### Luồng file - Nhận/Từ chối chuyến

```
DriverHomeScreen.kt (nhấn "Nhận" trên AvailableRideCard)
  │
  ├─► DriverHomeViewModel.acceptRide(rideId)
  │      │
  │      └─► DriverRepository.acceptRide(rideId)
  │             │
  │             └─► ApiService.kt → POST /api/driver/ride/:id/accept
  │                    │
  │                    └─► Database: rides (MySQL)
  │                           UPDATE: driver_id = currentDriverId, status = 'accepted'
  │                           Trả về: RideDto
  │             │
  │             ├─► state.currentRide = ride
  │             ├─► state.availableRides -= ride
  │             └─► Backend gửi thông báo FCM cho passenger
  │
  └─► DriverHomeViewModel.rejectRide(rideId)
         │
         └─► DriverRepository.rejectRide(rideId)
                │
                └─► ApiService.kt → POST /api/driver/ride/:id/reject
                       │
                       └─► Backend: xóa khỏi danh sách tài xế này
```

### Luồng file - Cập nhật trạng thái chuyến

```
DriverHomeScreen.kt (nhấn nút hành động trạng thái)
  │
  ├─► DriverHomeViewModel.updateRideStatus(rideId, status)
  │      │
  │      ├─► SocketManager.emitRideStatus(rideId, status)
  │      │      (Socket event: ride:status → server → passenger)
  │      │
  │      └─► DriverRepository.updateRideStatus(rideId, status)
  │             │
  │             └─► ApiService.kt → PUT /api/driver/ride/:id/status
  │                    Body: { status }
  │                    │
  │                    └─► Database: rides (MySQL)
  │                           UPDATE: status
  │                           Nếu completed: cập nhật completed_at, tạo earnings
  │                           Nếu arrived/in_progress: gửi thông báo FCM
  │             │
  │             ├─► completed → state.currentRide = null, loadEarnings()
  │             └─► accepted/arrived/in_progress → state.currentRide = ride
```

### Kết quả ra

| Hành động | Kết quả | Database |
|-----------|---------|----------|
| Bật Online | Nhận danh sách chuyến mới | UPDATE `drivers.is_available=true` |
| Tắt Online | Dừng nhận chuyến | UPDATE `drivers.is_available=false` |
| Nhận chuyến | Chuyến chuyển sang "accepted" | UPDATE `rides.driver_id`, `rides.status='accepted'` |
| Từ chối | Xóa khỏi danh sách chờ | Không thay đổi DB |
| Đã đến | Trạng thái = "arrived" | UPDATE `rides.status='arrived'` |
| Bắt đầu | Trạng thái = "in_progress" | UPDATE `rides.status='in_progress'` |
| Hoàn thành | Trạng thái = "completed" | UPDATE `rides.status='completed'`, INSERT `earnings` |
| Hủy chuyến | Trạng thái = "cancelled" | UPDATE `rides.status='cancelled'` |
| Realtime GPS | Passenger thấy tài xế di chuyển | INSERT `driver_locations` |

### Chi tiết các file tham gia

| File | Vai trò | Mô tả |
|------|---------|--------|
| `DriverHomeScreen.kt` | UI + ViewModel | Dashboard tài xế, 3 tabs, toggle online, nút hành động |
| `DriverHomeViewModel` | Logic | `toggleOnlineStatus`, `acceptRide`, `rejectRide`, `updateRideStatus` |
| `DriverRepository.kt` | Repository | Gọi tất cả driver API |
| `DriverApiService.kt` | API Interface | Các endpoint riêng cho driver |
| `SocketManager.kt` | Realtime | `emitLocationUpdate()`, `emitRideStatus()` |
| `ApiService.kt` | API Interface | Driver endpoints: profile, status, accept, reject, earnings |
| `DriverScreens.kt` (AvailableRideCard, CurrentRideCard) | UI Components | Card hiển thị chuyến có sẵn / đang chạy |

---

## 6. Chi tiết chuyến đi & Thanh toán & Đánh giá

### Mô tả
Màn hình chi tiết chuyến đi hiển thị lộ trình, giá cước, thông tin tài xế/hành khách, phương thức thanh toán (chỉ passenger khi ride còn pending), và đánh giá sao (sau khi completed).

### Cách dùng

#### Xem chi tiết
- Mở từ `PassengerHomeScreen` (nhấn vào ride đang active) hoặc `HistoryScreen` (nhấn vào ride cũ)

#### Thanh toán (Passenger - chỉ khi status = "pending")
1. Chọn **phương thức thanh toán**:
   - **Tiền mặt** (cash)
   - **MoMo** (momo)
   - **VNPay** (vnpay)
2. Nhấn **"Xác nhận thanh toán"**
3. Nếu **Tiền mặt**: → `paymentCreated = true`, hiển thị "Thanh toán tiền mặt"
4. Nếu **MoMo/VNPay**: → tạo payment, mở URL thanh toán trong browser

#### Đánh giá (sau khi status = "completed")
1. Mục "Đánh giá" hiển thị với 5 sao
2. Nhấn vào sao để chọn rating (1-5)
3. *(Tùy chọn)* Chọn tags đánh giá:
   - Passenger: "Lái xe an toan", "Xe sach", "Dung gio", "Than thien"
   - Driver: "An toan", "Than thien", "Ho tro tot"
4. *(Tùy chọn)* Nhập bình luận
5. Nhấn **"Gửi đánh giá"**

### Luồng file - Thanh toán

```
RideDetailScreen.kt (Passenger - status=pending)
  │
  ├─► RideDetailViewModel.selectPaymentMethod(method)
  │      ──► state.selectedPaymentMethod = method
  │
  └─► RideDetailViewModel.createPayment()
         │
         ├─► PaymentRepository.createPayment(rideId, paymentMethod)
         │      │
         │      └─► ApiService.kt → POST /api/payments/create
         │             Body: { ride_id, payment_method }
         │             │
         │             └─► Backend tạo payment record
         │                    │
         │                    ├─► Nếu cash:
         │                    │      Cập nhật rides.payment_status = 'paid'
         │                    │      Trả về: { paymentUrl = null }
         │                    │
         │                    ├─► Nếu MoMo:
         │                    │      Ký HMAC-SHA256 với MoMo credentials
         │                    │      Trả về: { paymentUrl: "https://..." }
         │                    │
         │                    └─► Nếu VNPay:
         │                           Ký HMAC-SHA256 với VNPay credentials
         │                           Trả về: { paymentUrl: "https://..." }
         │
         ├─► paymentUrl != null → mở browser để thanh toán MoMo/VNPay
         │
         └─► Backend callback:
                ├─► POST /api/payments/:id/confirm (MoMo/VNPay callback)
                │      Cập nhật payment.status = 'confirmed'
                │      Cập nhật rides.payment_status = 'paid'
                │
                └─► Database: payments, rides (MySQL)
```

### Luồng file - Đánh giá

```
RideDetailScreen.kt (status=completed, chưa rating)
  │
  ├─► RideDetailViewModel.rateRide(rating, comment)
  │      │
  │      └─► RideRepository.rateRide(rideId, rating, comment)
  │             │
  │             └─► ApiService.kt → POST /api/rides/:id/rate
  │                    Body: { rating, comment }
  │                    │
  │                    └─► Database: rides (MySQL)
  │                           UPDATE: driver_rating, rating_comment
  │                           Cập nhật avg rating của driver trong users table
  │             │
  │             ├─► Thành công ──► state.ratingSubmitted = true
  │             │      ──► Hiển thị "Bạn đã đánh giá X sao"
  │             │
  │             └─► Thất bại ──► Snackbar lỗi
```

### Kết quả ra

| Hành động | Kết quả | Database |
|-----------|---------|----------|
| Chọn thanh toán tiền mặt | Xác nhận thanh toán thành công | INSERT `payments`, UPDATE `rides.payment_status` |
| Chọn MoMo/VNPay | Mở URL thanh toán | INSERT `payments` (status=pending) |
| Callback thanh toán | Thanh toán xác nhận | UPDATE `payments.status='confirmed'` |
| Gửi đánh giá | Đánh giá được lưu | UPDATE `rides.driver_rating`, UPDATE `users.rating` |

### Chi tiết các file tham gia

| File | Vai trò | Mô tả |
|------|---------|--------|
| `RideDetailScreen.kt` | UI + ViewModel | Chi tiết ride, payment form, rating UI |
| `RideDetailViewModel` | Logic | `loadRide`, `createPayment`, `rateRide`, `updateStatus` |
| `PaymentRepository.kt` | Repository | `createPayment()`, `getPayment()`, `confirmPayment()` |
| `PaymentDto.kt` | DTO | Payment request/response models |
| `ApiService.kt` | API Interface | 5 endpoints: create, get, confirm, vnpay/return, momo/return |
| `RideRepository.kt` | Repository | `getRide()`, `rateRide()` |

---

## 7. Lịch sử chuyến đi

### Mô tả
Màn hình lịch sử hiển thị tất cả chuyến đi của người dùng (passenger hoặc driver) với chức năng tìm kiếm và lọc theo trạng thái.

### Cách dùng
1. Nhấn **icon Lịch sử** trên màn hình chính
2. Nhấn vào **thanh tìm kiếm** → nhập địa điểm, tên tài xế/hành khách
3. Nhấn các **tab lọc**: Tất cả / Hoàn thành / Đã hủy
4. Nhấn vào **bất kỳ chuyến nào** → chuyển đến `RideDetailScreen`

### Luồng file

```
HistoryScreen.kt (UI)
  │
  ├─► HistoryViewModel.loadHistory()
  │      │
  │      ├─► Passenger: RideRepository.getRideHistory()
  │      │      │
  │      │      └─► ApiService.kt → GET /api/rides
  │      │             │
  │      │             └─► Database: rides (MySQL)
  │      │                    WHERE passenger_id = currentUserId
  │      │                    Trả về: danh sách RideDto
  │      │
  │      └─► Driver: DriverRepository.getDriverHistory()
  │             │
  │             └─► ApiService.kt → GET /api/driver/history
  │                    │
  │                    └─► Database: rides (MySQL)
  │                           WHERE driver_id = currentDriverId
  │
  ├─► Lọc phía client:
  │      │
  │      ├─► Filter: selectedFilter (all/completed/cancelled)
  │      └─► Search: searchQuery (pickup/dest address, driverName, passengerName)
  │
  └─► Nhấn vào RideCard → onRideClick(rideId) → RideDetailScreen
```

### Kết quả ra

| Hành động | Kết quả | Database |
|-----------|---------|----------|
| Mở màn hình | Tải danh sách chuyến đi | Query `rides` |
| Tìm kiếm | Lọc danh sách phía client (không call API) | Không thay đổi |
| Lọc tab | Lọc theo status phía client | Không thay đổi |
| Nhấn chuyến | Mở chi tiết | Không thay đổi |

---

## 8. Hồ sơ người dùng

### Mô tả
Màn hình hồ sơ hiển thị thông tin cá nhân và thông tin xe (đối với tài xế). Hỗ trợ chỉnh sửa.

### Cách dùng

#### Xem hồ sơ
- Hiển thị: Avatar, tên, email, số điện thoại, số sao đánh giá, số chuyến đi
- Driver thêm: Mẫu xe, màu xe, biển số

#### Chỉnh sửa (Passenger)
1. Nhấn **"Chỉnh sửa"**
2. Sửa **Họ tên**, **Số điện thoại**
3. Nhấn **"Lưu"**

#### Chỉnh sửa (Driver)
1. Nhấn **"Chỉnh sửa"**
2. Sửa **Họ tên**, **Số điện thoại**
3. Sửa **Thông tin xe**: Mẫu xe, Màu xe, Biển số
4. Nhấn **"Lưu"**

#### Đăng xuất
- Nhấn **"Đăng xuất"** trong mục Cài đặt

### Luồng file - Tải hồ sơ

```
ProfileScreen.kt (UI)
  │
  ├─► ProfileViewModel.loadProfile()
  │      │
  │      ├─► sessionManager.userType
  │      │
  │      ├─► Driver: DriverRepository.getDriverProfile()
  │      │      │
  │      │      └─► ApiService.kt → GET /api/driver/profile
  │      │             │
  │      │             └─► Database: users + drivers (MySQL, JOIN)
  │      │                    Trả về: DriverDto (name, phone, carModel, carColor, licensePlate, rating)
  │      │
  │      └─► Passenger: RideRepository.getRideHistory()
  │             │
  │             └─► Đếm tổng số chuyến → totalRides
```

### Luồng file - Cập nhật hồ sơ

```
ProfileScreen.kt (nhấn "Lưu")
  │
  └─► ProfileViewModel.saveProfile()
         │
         └─► DriverRepository.updateDriverProfile()
                Body: { name, phone, carModel, carColor, licensePlate }
                │
                └─► ApiService.kt → PUT /api/driver/profile
                       │
                       └─► Database: users + drivers (MySQL)
                              UPDATE users SET name, phone WHERE id = userId
                              UPDATE drivers SET car_model, car_color, license_plate WHERE user_id = userId
```

### Luồng file - Đăng xuất

```
ProfileScreen.kt (nhấn "Đăng xuất")
  │
  └─► ProfileViewModel.logout(onLogout)
         │
         ├─► SocketManager.disconnect()
         │      (Socket event: disconnect)
         │
         ├─► SessionManager.clearSession()
         │      (Xóa SharedPreferences: token, userId, userName, userType)
         │
         └─► onLogout() → AppNavigation → AuthScreen
```

### Chi tiết các file tham gia

| File | Vai trò | Mô tả |
|------|---------|--------|
| `ProfileScreen.kt` | UI + ViewModel | Hiển thị và chỉnh sửa hồ sơ |
| `ProfileViewModel` | Logic | `loadProfile`, `saveProfile`, `logout` |
| `DriverRepository.kt` | Repository | `getDriverProfile`, `updateDriverProfile` |
| `ApiService.kt` | API Interface | `GET /driver/profile`, `PUT /driver/profile` |

---

## 9. Tính năng AI - Lịch trình

### Mô tả
Cho phép hành khách tạo lịch trình với nhiều điểm dừng (waypoints) và AI sẽ đề xuất các phương án tối ưu theo thời gian, chi phí, hoặc cân bằng.

### Cách dùng

#### Tạo lịch trình mới
1. Nhấn **"Lịch trình"** trên PassengerHomeScreen
2. Nhấn **"+ Tạo lịch trình mới"**
3. Nhập **Tên lịch trình**
4. Chọn **tối ưu theo**: Nhanh nhất / Rẻ nhất / Cân bằng
5. Chọn **số điểm dừng** (2-6) bằng slider
6. Nhấn **"Tạo lịch trình"**
7. Xem **chi tiết lịch trình**: tổng km, thời gian, chi phí, các điểm dừng
8. Xem **phương án tối ưu** (alternatives) do AI đề xuất
9. Nhấn các nút **"Nhanh" / "Rẻ" / "Cân bằng"** để xem alternatives khác nhau

#### Xem lịch sử lịch trình
- Cuộn xuống dưới để xem danh sách các lịch trình đã tạo trước đó
- Nhấn vào lịch trình → xem chi tiết

### Luồng file - Tạo lịch trình

```
AIScheduleScreen.kt (nhấn "Tạo lịch trình")
  │
  └─► AIScheduleViewModel.createSchedule(name, date, optimization, waypoints)
         │
         ├─► AIRepository.createSchedule()
         │      │
         │      └─► ApiService.kt → POST /api/ai/schedule/create
         │             Body: { schedule_name, scheduled_date, optimization_type, waypoints[] }
         │             │
         │             └─► Database: ai_trip_schedules (MySQL)
         │                    INSERT: user_id, schedule_name, scheduled_date, optimization_type
         │                    INSERT nhiều: ai_waypoints (pickup/dropoff/stopover)
         │                    AI xử lý → tạo ai_route_alternatives
         │                    Trả về: AIScheduleDto (với alternatives, waypoints)
         │
         ├─► state.currentSchedule = schedule
         ├─► state.alternatives = schedule.alternatives
         ├─► state.waypoints = schedule.waypoints
         └─► loadHistory() → cập nhật danh sách
```

### Luồng file - Tối ưu lịch trình

```
AIScheduleScreen.kt (nhấn nút "Nhanh" / "Rẻ" / "Cân bằng")
  │
  └─► AIScheduleViewModel.optimizeSchedule(scheduleId, optimization)
         │
         └─► AIRepository.optimizeSchedule()
                │
                └─► ApiService.kt → POST /api/ai/schedule/:id/optimize
                       Body: { optimization_type: "time" | "cost" | "balanced" }
                       │
                       └─► Database: ai_trip_schedules (MySQL)
                              AI tính toán lại alternatives
                              Trả về: List<RouteAlternativeDto>
```

### Database tables tham gia

| Bảng | Mô tả |
|------|--------|
| `ai_trip_schedules` | Lưu thông tin lịch trình (name, date, optimization_type, status, confidence_score) |
| `ai_waypoints` | Lưu các điểm dừng (schedule_id, stop_order, lat/lng, address, type) |
| `ai_route_alternatives` | Lưu các phương án tuyến đường (distance, duration, price, is_recommended) |
| `ai_learning_profiles` | Hồ sơ học tập AI của user (preferred_time, frequent_locations) |

### Chi tiết các file tham gia

| File | Vai trò | Mô tả |
|------|---------|--------|
| `AIScreens.kt` | UI + ViewModel | AIScheduleScreen, ScheduleDetailCard, CreateScheduleDialog |
| `AIScheduleViewModel` | Logic | `createSchedule`, `selectSchedule`, `optimizeSchedule`, `loadHistory` |
| `AIRepository.kt` | Repository | Gọi các AI endpoints |
| `ApiService.kt` | API Interface | `POST /ai/schedule/create`, `GET /ai/schedule/:id`, `POST /ai/schedule/:id/optimize`, `GET /ai/history` |
| `AIModels.kt` | DTO | `CreateScheduleRequest`, `AIScheduleDto`, `WaypointDto`, `RouteAlternativeDto` |

---

## 10. Tính năng AI - Chat Assistant

### Mô tả
Giao diện chat với AI Assistant để đặt xe nhanh hoặc tạo lịch trình bằng ngôn ngữ tự nhiên.

### Cách dùng
1. Nhấn **icon 💬 AI Chat** trên PassengerHomeScreen
2. AI hiển thị **tin nhắn chào mừng**
3. Nhấn **suggestion chip** (gợi ý nhanh) hoặc nhập tin nhắn
4. AI phản hồi với các gợi ý:
   - "Goi xe di Bach Khoa" → đặt xe đến Bách Khoa
   - "Dat xe 7 cho sang Quan 1" → đặt xe 7 chỗ
   - "Lich trinh Ha Long 2 ngay" → tạo lịch trình
   - "Dich vu gia re nhat" → gợi ý xe máy
   - "Toi muon xe 4 cho" → gợi ý xe 4 chỗ

### Luồng file

```
AIChatScreen.kt (UI)
  │
  ├─► Nhấn suggestion chip hoặc nhấn nút gửi
  │      │
  │      ├─► messages += ChatMessage(text, isUser=true)
  │      │
  │      ├─► isTyping = true (hiển thị typing indicator)
  │      │
  │      ├─► kotlinx.coroutines.delay(1200ms) (giả lập AI xử lý)
  │      │
  │      └─► response = getAIResponse(userMessage)
  │             (hàm trong file - rule-based, không gọi API backend)
  │             │
  │             └─► Trả về text phản hồi dựa trên keywords:
  │                    ├── "bach khoa" / "truong" → đặt xe Bách Khoa
  │                    ├── "7 cho" / "7cho" → đặt xe 7 chỗ
  │                    ├── "ha long" / "halong" → lịch trình Ha Long
  │                    ├── "re" / "gia re" → gợi ý xe máy
  │                    ├── "4 cho" / "4cho" → gợi ý xe 4 chỗ
  │                    └── fallback → phản hồi chung
  │
  └─► messages += ChatMessage(response, isUser=false)
         isTyping = false
```

> **Lưu ý:** Hiện tại AI Chat sử dụng **rule-based logic** (getAIResponse) trong code, **chưa gọi API backend**. Backend API có thể mở rộng để tích hợp AI thực sự (LLM).

### Kết quả ra

| Input | AI phản hồi |
|-------|-------------|
| "Goi xe di Bach Khoa" | "Da dat xe cho ban di BACH KHOA! Tai xe se den trong 5 phut. Gia uu tien: 25,000d." |
| "Dat xe 7 cho sang Quan 1" | "OK! Dat xe O to 7 cho. Loai xe nay phu hop cho gia dinh hoac nhom. Gia: tu 35,000d." |
| "Lich trinh Ha Long 2 ngay" | Tạo lịch trình Ha Long 2 ngày với chi phí ước tính |
| "Dich vu gia re nhat" | Gợi ý xe máy với bảng giá |

### Chi tiết các file tham gia

| File | Vai trò | Mô tả |
|------|---------|--------|
| `AIChatScreen.kt` | UI | Giao diện chat, tin nhắn, suggestion chips, typing indicator |
| `getAIResponse()` | Logic | Hàm rule-based xử lý tin nhắn (trong cùng file) |

---

## 11. Tính năng AI - Đề xuất & Hồ sơ AI

### Mô tả
Hai màn hình: **Đề xuất AI** (hiển thị tuyến đường thường dùng, tiết kiệm) và **Hồ sơ AI** (tùy chỉnh preferences cho AI).

### Cách dùng

#### Màn hình Đề xuất AI (AIRecommendationsScreen)
1. Mở → hiển thị loading "AI đang phân tích..."
2. Sau 1.5s → hiển thị:
   - **Độ tin cậy AI** (phần trăm)
   - **Tuyến đường thường dùng** (pickup → dropoff, số lần đi, giá TB)
   - **Tiết kiệm ước tính** (VND/chuyến)

#### Màn hình Hồ sơ AI (AIProfileScreen)
1. **Slider Ưu tiên chi phí**: Nhanh ↔ Tiết kiệm
2. Hiển thị text mô tả lựa chọn
3. Nhấn **"Lưu cài đặt"**
4. **Vị trí thường đến**: Danh sách các địa điểm quen thuộc

### Luồng file - Đề xuất AI

```
AIRecommendationsScreen.kt (UI)
  │
  └─► LaunchedEffect(Unit)
         │
         ├─► kotlinx.coroutines.delay(1500ms)
         │
         └─► Tạo AIRecommendationDto (mock data - hiện tại chưa gọi API)
                │
                ├─► frequentRoutes: [Bách Khoa→Vinmart, Bến Thành→Landmark 81]
                ├─► bestTimes: ["7:00-9:00", "17:00-19:00"]
                ├─► estimatedSavings: 8500 VND
                ├─► aiConfidence: 0.92
                └─► Cập nhật state
```

### Luồng file - Hồ sơ AI

```
AIProfileScreen.kt (UI - slider)
  │
  └─► AIRepository.updateAIProfile() (khi nhấn "Lưu cài đặt")
         │
         └─► ApiService.kt → PUT /api/ai/profile
                Body: { preference_cost_vs_time: 0.0-1.0 }
                │
                └─► Database: ai_learning_profiles (MySQL)
                       UPDATE: preference_cost_vs_time, preferred_time_start/end
```

---

## 12. Thu nhập tài xế

### Mô tả
Màn hình thống kê thu nhập của tài xế với biểu đồ cột 7 ngày và chi tiết theo ngày/tuần/tháng.

### Cách dùng
1. Nhấn **icon 💰 (Earnings)** trên DriverHomeScreen
2. Xem **tổng quan**: Thu nhập hôm nay (số lớn), số chuyến, tuần này, tháng này
3. Xem **biểu đồ cột 7 ngày** (thu nhập mỗi ngày)
4. Xem **chi tiết hôm nay**: số chuyến, thu nhập, tuần, tháng

### Luồng file

```
EarningsScreen.kt (UI)
  │
  └─► EarningsViewModel.loadEarnings()
         │
         ├─► today = LocalDate.now()
         ├─► weekStart = today - 6 days
         │
         └─► DriverRepository.getEarnings(weekStart, today)
                │
                └─► ApiService.kt → GET /api/driver/earnings
                       Query params: ?start_date=...&end_date=...
                       │
                       └─► Database: earnings (MySQL)
                              JOIN rides ON earnings.ride_id = rides.id
                              WHERE driver_id = ? AND date range
                              Tính tổng: todayEarnings, weekEarnings, monthEarnings, totalEarnings, totalRides
                              Trả về: EarningsDto
```

### Kết quả ra (EarningsDto)

```json
{
  "todayEarnings": 150000,
  "weekEarnings": 850000,
  "monthEarnings": 3200000,
  "totalEarnings": 15000000,
  "totalRides": 245
}
```

### Chi tiết các file tham gia

| File | Vai trò | Mô tả |
|------|---------|--------|
| `DriverScreens.kt` (EarningsScreen) | UI | Biểu đồ cột, stats card, chi tiết |
| `EarningsViewModel` | Logic | `loadEarnings()` |
| `DriverRepository.kt` | Repository | `getEarnings()` |
| `ApiService.kt` | API Interface | `GET /driver/earnings` |
| `DataModels.kt` | DTO | `EarningsDto` |

---

## 13. Chuyến ghép (Batch)

### Mô tả
Tính năng ghép nhiều hành khách có tuyến đường tương tự vào một chuyến để tăng hiệu quả cho tài xế.

### Cách dùng

#### Tab "Batch" trên DriverHomeScreen
1. Nhấn tab **"Batch"** trên DriverHomeScreen
2. Danh sách các **batch đề xuất** hiển thị
3. Mỗi batch gồm: tên batch, số hành khách, doanh thu, quãng đường, % hiệu quả
4. Nhấn **"Chấp nhận Batch"** → tài xế nhận ghép chuyến
5. Chi tiết: danh sách hành khách trong batch, điểm đón/trả, độ lệch km

#### BatchOfferScreen (mở rộng)
- Màn hình riêng hiển thị chi tiết batch với thông tin từng hành khách

### Luồng file

```
DriverHomeScreen.kt (Tab "Batch")
  │
  ├─► DriverHomeViewModel.loadBatches()
  │      │
  │      └─► DriverRepository.getAvailableBatches()
  │             │
  │             └─► ApiService.kt → GET /api/ai/batch/available
  │                    │
  │                    └─► Database: driver_route_batches (MySQL)
  │                           JOIN batch_passengers ON batch.id = batch_passengers.batch_id
  │                           WHERE driver_id = ? AND status = 'proposed'
  │                           Trả về: List<BatchDto>
  │
  └─► DriverHomeViewModel.acceptBatch(batchId)
         │
         └─► DriverRepository.acceptBatch(batchId)
                │
                └─► ApiService.kt → POST /api/ai/batch/:id/accept
                       │
                       └─► Database: driver_route_batches (MySQL)
                              UPDATE: status = 'accepted', accepted_at = NOW()
```

### Database tables tham gia

| Bảng | Mô tả |
|------|--------|
| `driver_route_batches` | Thông tin batch (driver_id, status, total_revenue, total_distance, passenger_count, efficiency_score) |
| `batch_passengers` | Hành khách trong batch (batch_id, passenger_id, pickup/dropoff lat/lng, detour_km, price_adjustment) |

---

## 14. Sơ đồ tổng quan kiến trúc

```
╔══════════════════════════════════════════════════════════════════╗
║                     ANDROID APP (DoAn3)                        ║
╠══════════════════════════════════════════════════════════════════╣
║                                                                  ║
║  ┌──────────────┐    ┌──────────────────────┐    ┌──────────┐ ║
║  │  UI Layer    │    │  Navigation Layer     │    │  Theme    │ ║
║  │  (Compose)   │───▶│  AppNavigation.kt     │    │  Color.kt │ ║
║  │              │    │  NavHost, Screen()    │    │  Type.kt  │ ║
║  │  Screens:    │    │  MainViewModel        │    │  Theme.kt │ ║
║  │  - Auth      │    └──────────────────────┘    └──────────┘ ║
║  │  - Passenger │                                                ║
║  │  - Driver    │    ┌──────────────────────┐    ┌────────────┐ ║
║  │  - RideDetail│───▶│  ViewModel Layer     │───▶│  DI        │ ║
║  │  - History   │    │                     │    │  AppModule │ ║
║  │  - Profile   │    │  AuthVM             │    │  (Hilt)    │ ║
║  │  - AI        │    │  PassengerHomeVM     │    └────────────┘ ║
║  │  - Earnings  │    │  DriverHomeVM        │                    ║
║  └──────────────┘    │  RideDetailVM        │    ┌────────────┐ ║
║                      │  HistoryVM            │    │  Socket.IO │ ║
║  ┌──────────────┐    │  ProfileVM          │───▶│  Real-time │ ║
║  │  Components  │    │  AIScheduleVM        │    │  Manager   │ ║
║  │              │    │  EarningsVM         │    └────────────┘ ║
║  │  TaxiMapView │    │  ...                │                    ║
║  │  GradientBtn │    └──────────────────────┘    ┌────────────┐ ║
║  │  RideCard   │             │                  │  FCM       │ ║
║  │  StatusBadge│             ▼                  │  Service   │ ║
║  └──────────────┘    ┌──────────────────────┐    └────────────┘ ║
║                      │  Repository Layer     │                    ║
║                      │                      │                    ║
║                      │  AuthRepository      │    ┌────────────┐ ║
║                      │  RideRepository      │───▶│  Local     │ ║
║                      │  DriverRepository    │    │  SessionMgr│ ║
║                      │  AIRepository        │    │(SharedPref)│ ║
║                      │  PaymentRepository   │    └────────────┘ ║
║                      │  PasswordResetRepo   │                    ║
║                      └──────────────────────┘                    ║
║                                │                                 ║
║                                ▼                                 ║
║                      ┌──────────────────────┐                    ║
║                      │   Data Layer         │                    ║
║                      │                      │                    ║
║                      │  ApiService.kt       │                    ║
║                      │  (Retrofit Interface)│                    ║
║                      │  AuthInterceptor.kt  │                    ║
║                      │  RetrofitClient.kt   │                    ║
║                      │  DTOs (DataModels,   │                    ║
║                      │    PaymentDto, AIMdl) │                    ║
║                      └──────────────────────┘                    ║
║                                │                                 ║
║               HTTP/REST ◀──────┼────────────────────────▶ HTTP/REST ║
║               Socket.IO ◀──────┘                     ◀────── Socket.IO  ║
║                                │                                 ║
╚══════════════════════════════════════════════════════════════════╝
                                    │
                                    ▼
╔══════════════════════════════════════════════════════════════════╗
║                     BACKEND (Node.js + Express)                  ║
║                                                                  ║
║  ┌────────────┐  ┌────────────┐  ┌────────────┐  ┌───────────┐ ║
║  │ Auth API   │  │ Ride API   │  │Driver API  │  │Payment API│ ║
║  │ /auth/*    │  │ /rides/*   │  │ /driver/*  │  │/payments/*│ ║
║  └────────────┘  └────────────┘  └────────────┘  └───────────┘ ║
║  ┌────────────┐  ┌────────────┐  ┌────────────┐  ┌───────────┐ ║
║  │ Location   │  │ AI API     │  │Chat API    │  │ Admin API │ ║
║  │ /location/*│  │ /ai/*      │  │ /chat/*    │  │ /admin/*  │ ║
║  └────────────┘  └────────────┘  └────────────┘  └───────────┘ ║
║                            │                                    ║
║                            ▼                                    ║
║  ┌──────────────────────────────────────────────────────────┐  ║
║  │              Socket.IO Server                            │  ║
║  │  Events: location:update, ride:status, driver:location,  │  ║
║  │          ride:status:changed, chat:message, join:ride      │  ║
║  └──────────────────────────────────────────────────────────┘  ║
║                            │                                    ║
║                            ▼                                    ║
║  ┌──────────────────────────────────────────────────────────┐  ║
║  │              MySQL Database (doan3_db)                   │  ║
║  │                                                          │  ║
║  │  users | drivers | rides | driver_locations | earnings   │  ║
║  │  ai_trip_schedules | ai_waypoints | ai_route_alternatives│  ║
║  │  ai_learning_profiles | driver_route_batches | batch_pass│  ║
║  │  payments | transactions | chat_messages | password_resets│  ║
║  │  user_fcm_tokens | ride_rating_tags | cancellation_log   │  ║
║  └──────────────────────────────────────────────────────────┘  ║
║                            │                                    ║
║          ┌─────────────────┼─────────────────┐                ║
║          ▼                 ▼                 ▼                ║
║  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐         ║
║  │ Firebase FCM │  │   VNPay      │  │    MoMo     │         ║
║  │ Notifications│  │   Payment    │  │   Payment   │         ║
║  └──────────────┘  └──────────────┘  └──────────────┘         ║
║                                                                  ║
║  ┌──────────────────────────────────────────────────────────┐  ║
║  │              Google Maps Platform                         │  ║
║  │  Directions API | Distance Matrix API | Geocoding API    │  ║
║  └──────────────────────────────────────────────────────────┘  ║
║                                                                  ║
╚══════════════════════════════════════════════════════════════════╝
```

---

## 15. Cấu trúc Database

### Tổng quan
**17 bảng** trong MySQL database `doan3_db` với các mối quan hệ:

```
users ──1:1── drivers ──1:*── rides ──0:1── earnings
         │
         └─1:*── driver_locations
         │
         └─1:*── driver_route_batches ──1:*── batch_passengers ──1:*── users
         │
         └─1:*── ai_trip_schedules ──1:*── ai_waypoints
         │              │
         │              └─1:*── ai_route_alternatives
         │
         └─0:1── ai_learning_profiles

users ──1:*── rides (as passenger) ──1:*── batch_passengers

rides ──1:*── chat_messages
rides ──1:1── payments ──1:*── transactions

users ──1:*── user_fcm_tokens
users ──1:*── password_resets
rides ──1:*── cancellation_log
rides ──1:*── ride_rating_tags
```

### Chi tiết các bảng

#### `users` - Tài khoản người dùng
```
PK: id (INT, AUTO_INCREMENT)
UQ: email (VARCHAR 255)
    password (VARCHAR 255, bcrypt hash)
    name, phone, user_type (ENUM: passenger/driver/owner/consultant/hr_manager/revenue_manager)
    rating (DECIMAL 3,2, DEFAULT 5.00)
    total_rides (INT, DEFAULT 0)
    created_at
```

#### `drivers` - Hồ sơ tài xế (1:1 với users)
```
PK: id
FK: user_id → users.id (UNIQUE)
    car_model, car_color, license_plate
    is_available (BOOLEAN, DEFAULT FALSE)
    latitude, longitude (vị trí GPS hiện tại)
    created_at
```

#### `rides` - Chuyến đi
```
PK: id
FK: passenger_id → users.id
FK: driver_id → drivers.id (NULL khi chưa có tài xế)
    pickup_lat/lng, dest_lat/lng (tọa độ)
    pickup_address, dest_address (địa chỉ text)
    vehicle_type (ENUM: motorbike/car_4_seats/car_7_seats)
    distance_km, duration_min, price (VND)
    status (ENUM: pending/accepted/arrived/in_progress/completed/cancelled)
    driver_rating, passenger_rating (1-5, NULL khi chưa đánh giá)
    rating_comment
    started_at, completed_at, created_at
```

#### `driver_locations` - Lịch sử GPS tài xế
```
PK: id
FK: driver_id → drivers.id
    latitude, longitude
    accuracy, speed, heading
    updated_at
```

#### `earnings` - Thu nhập tài xế
```
PK: id
FK: driver_id → drivers.id
FK: ride_id → rides.id (NULL)
    amount (VND)
    type (ENUM: ride/bonus/penalty/withdrawal)
    note, created_at
```

#### `ai_trip_schedules` - Lịch trình AI
```
PK: id
FK: user_id → users.id
    schedule_name, scheduled_date
    total_estimated_time, total_estimated_price, total_distance
    optimization_type (ENUM: time/cost/balanced)
    ai_confidence_score (0.00-1.00)
    traffic_condition, status (ENUM: planned/in_progress/completed/cancelled)
    created_at
```

#### `ai_waypoints` - Điểm dừng trong lịch trình
```
PK: id
FK: schedule_id → ai_trip_schedules.id
    stop_order, stop_type (ENUM: pickup/dropoff/stopover)
    latitude, longitude, address, stop_name
    estimated_arrival, duration_min, distance_from_prev
    is_optional (BOOLEAN), priority
    estimated_price_segment
```

#### `ai_route_alternatives` - Phương án tuyến đường thay thế
```
PK: id
FK: schedule_id → ai_trip_schedules.id
    route_name, total_distance, total_duration, total_price
    route_description
    is_recommended (BOOLEAN)
    traffic_scenario
```

#### `ai_learning_profiles` - Hồ sơ học tập AI
```
PK: id
FK: user_id → users.id (UNIQUE)
    preferred_time_start/end
    average_trip_duration, average_trip_cost
    total_distance_travelled
    frequent_locations (JSON), avoid_locations (JSON)
    preference_cost_vs_time (0=cost, 1=time)
    model_version
```

#### `driver_route_batches` - Chuyến ghép
```
PK: id
FK: driver_id → drivers.id
    batch_name, status (ENUM: proposed/accepted/rejected/completed/cancelled)
    total_revenue, total_distance
    passenger_count, efficiency_score, ai_confidence
    accepted_at, completed_at
```

#### `batch_passengers` - Hành khách trong chuyến ghép
```
PK: id
FK: batch_id → driver_route_batches.id
FK: passenger_id → users.id
FK: original_ride_id → rides.id
    pickup_order, dropoff_order
    pickup_lat/lng, dropoff_lat/lng
    estimated_pickup_time, detour_km, price_adjustment
    status (ENUM: pending/picked_up/dropped_off/cancelled)
```

---

## Tổng kết

Ứng dụng **DoAn3** là một hệ thống đặt xe hoàn chỉnh với các đặc điểm:

- **Kiến trúc:** Clean Architecture + MVVM + Hilt DI
- **UI:** Jetpack Compose + Material 3 (Dark theme)
- **Networking:** Retrofit + OkHttp + Socket.IO
- **Maps:** Google Maps SDK
- **Realtime:** Socket.IO cho vị trí tài xế và trạng thái chuyến
- **Push Notifications:** Firebase Cloud Messaging
- **Thanh toán:** Tích hợp VNPay, MoMo, Tiền mặt
- **AI Features:** Lịch trình thông minh, chat assistant, đề xuất cá nhân hóa
- **Database:** 17 bảng MySQL với quan hệ phức tạp

---

## Phụ lục: Cấu hình mạng LAN & Tài khoản

### Kết nối ứng dụng qua mạng LAN (khuyến nghị)

Để test ứng dụng trên **2 điện thoại thật** cùng lúc, cả hai điện thoại và máy tính phải kết nối **cùng một mạng WiFi**.

**Bước 1: Tìm IP máy tính**
1. Mở CMD (Command Prompt) trên Windows
2. Gõ lệnh: `ipconfig`
3. Tìm dòng **IPv4 Address** (thường có dạng `192.168.x.x`)

**Bước 2: Cập nhật AppConfig.kt**
Mở file `AppConfig.kt` và đảm bảo dòng này đúng IP:
```kotlin
const val BASE_URL = "http://192.168.x.x:3000/api/"
```
Thay `192.168.x.x` bằng IP thực của máy tính.

**Bước 3: Build lại APK**
```bash
.\gradlew.bat assembleDebug
```
Cài đặt APK mới trên cả 2 điện thoại.

**Bước 4: Khởi động Backend**
```bash
cd backend
node src/index.js
```
Backend chạy tại `http://localhost:3000`.

### Admin Panel

Admin Panel là giao diện quản lý dành cho nhân viên tư vấn và quản trị viên.

**Truy cập:** Mở trình duyện truy cập `http://localhost:3000/admin`

**Tài khoản đăng nhập Admin Panel:**

| Email | Mật khẩu | Vai trò |
|-------|----------|---------|
| owner@doan3.vn | Admin@123 | Chủ sở hữu |
| admin@doan3.vn | Admin@123 | Quản trị viên |
| consultant@doan3.vn | Admin@123 | Nhân viên tư vấn |
| tu_van1@doan3.vn | Admin@123 | Nhân viên tư vấn |
| tai_chinh@doan3.vn | Admin@123 | Nhân viên tài chính |

**Tài khoản test ứng dụng (app):**

| Email | Mật khẩu | Vai trò |
|-------|----------|---------|
| passenger@test.com | password123 | Khách hàng |
| driver1@test.com | password123 | Tài xế |
| driver2@test.com | password123 | Tài xế |

### Tính năng Chat Hỗ trợ Realtime

Nhân viên tư vấn có thể trả lời khách hàng ngay trên Admin Panel:

1. Đăng nhập bằng tài khoản `consultant@doan3.vn` / `Admin@123`
2. Vào mục **Hỗ trợ** trên sidebar
3. Danh sách cuộc trò chuyện hiển thị realtime
4. Chọn cuộc trò chuyện để xem và trả lời
5. Nhấn **Đánh dấu xong** để đóng hội thoại

Khách hàng trên app liên hệ tư vấn qua: **Hồ sơ → Hỗ trợ & FAQ → Trò chuyện với tư vấn viên**

### FAQ (Câu hỏi thường gặp)

Ứng dụng có sẵn **28 câu hỏi thường gặp** trong các danh mục:

| Danh mục | Số câu hỏi |
|----------|-----------|
| Câu hỏi chung (General) | 4 |
| Đặt xe & Chuyến đi (Booking) | 6 |
| Thanh toán (Payment) | 5 |
| Tài khoản (Account) | 4 |
| Tài xế (Driver) | 4 |
| Kỹ thuật (Technical) | 5 |

---

*Tài liệu được cập nhật: 2026-05-21*
