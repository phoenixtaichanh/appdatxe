# DoAn3 - Smart Ride Booking System with AI Travel Assistant

> **Phiên bản:** 1.0.1 | **Ngày cập nhật:** 2026-05-10
> **Trạng thái:** Backend 100% | Android 100% | BUILD SUCCESSFUL, 0 warnings
> **Tác giả:** Lê Đăng Khoa, Trần Nguyễn Tuấn Anh
> **Trường:** Đại học Bách Khoa TP.HCM (HUTECH)
> **App ID:** `com.laptrinhdidong.DoAn3`

---

## Mục lục

1. [Tổng quan dự án](#1-tổng-quan-dự-án)
2. [Công nghệ sử dụng](#2-công-nghệ-sử-dụng)
3. [Cấu trúc dự án](#3-cấu-trúc-dự-án)
4. [Tính năng](#4-tính-năng)
5. [Database Schema](#5-database-schema)
6. [API Endpoints](#6-api-endpoints)
7. [Cấu hình](#7-cấu-hình)
8. [Tài khoản test](#8-tài-khoản-test)
9. [Hướng dẫn cài đặt](#9-hướng-dẫn-cài-đặt)

---

## 1. Tổng quan dự án

**DoAn3** là một ứng dụng di động **đặt xe trực tuyến** (ride-hailing, giống Uber) kết hợp tính năng **trợ lý du lịch AI**. Người dùng có thể đặt xe máy, xe 4 chỗ hoặc xe 7 chỗ, đồng thời sử dụng các tính năng AI để lập kế hoạch hành trình, tối ưu tuyến đường và nhận đề xuất cá nhân hóa.

### 1.1 Các loại người dùng

| Loại | Mô tả |
|------|-------|
| **Passenger** (Hành khách) | Đặt xe, theo dõi chuyến, đánh giá tài xế |
| **Driver** (Tài xế) | Nhận yêu cầu, cập nhật trạng thái, xem thu nhập |
| **Owner** (Chủ xe) | Quản lý phương tiện |
| **Admin** (Quản trị) | Quản lý hệ thống |

---

## 2. Công nghệ sử dụng

### 2.1 Frontend (Android)

| Thành phần | Công nghệ |
|------------|-----------|
| Ngôn ngữ | Kotlin |
| UI Framework | Jetpack Compose + Material Design 3 |
| Architecture | MVVM + Clean Architecture |
| Dependency Injection | Hilt |
| Networking | Retrofit + OkHttp + Gson |
| Async | Kotlin Coroutines + Flow |
| Navigation | Jetpack Navigation Compose |
| Local Storage | SharedPreferences (SessionManager) |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 36 |

### 2.2 Backend

| Thành phần | Công nghệ |
|------------|-----------|
| Runtime | Node.js |
| Framework | Express.js |
| Database | MySQL 8.0 |
| Authentication | JWT (JSON Web Tokens) |
| Password Hashing | bcryptjs |
| Dev Server | nodemon |

---

## 3. Cấu trúc dự án

### 3.1 Backend (`/backend`)

```
backend/
├── src/
│   ├── index.js              # Express app entry point (port 3000)
│   ├── database/
│   │   ├── db.js             # MySQL connection pool
│   │   ├── schema.sql        # 11 tables (core + AI)
│   │   ├── seed.sql          # Test data
│   │   └── fix_password.js   # Password hash fix script
│   ├── routes/
│   │   ├── auth.js           # Login/Register
│   │   ├── users.js          # User management
│   │   ├── rides.js          # Ride operations
│   │   ├── drivers.js        # Driver features
│   │   ├── locations.js      # Location services
│   │   └── ai.js             # AI schedule & batch
│   ├── middleware/
│   │   └── auth.js           # JWT verification
│   └── repositories/         # Data access layer
├── package.json
└── .env
```

### 3.2 Android App (`/app`)

```
app/src/main/java/com/laptrinhdidong/DoAn3/
├── MainActivity.kt
├── DoAn3Application.kt       # Hilt Application
├── AppConfig.kt              # BASE_URL configuration
├── data/
│   ├── local/
│   │   └── SessionManager.kt # SharedPreferences
│   ├── remote/
│   │   ├── RetrofitClient.kt  # HTTP client setup
│   │   ├── ApiService.kt     # API endpoints interface
│   │   └── dto/              # Request/Response DTOs
│   └── repository/           # Repository pattern
├── di/
│   └── AppModule.kt          # Hilt modules
└── ui/
    ├── theme/               # Material3 theming
    ├── components/          # Reusable UI components
    ├── navigation/
    │   └── AppNavigation.kt
    └── screens/
        ├── auth/            # Login/Register
        ├── splash/          # Splash with auto-login
        ├── passenger/       # Passenger features
        ├── driver/          # Driver features
        └── ai/              # AI features
```

---

## 4. Tính năng

### 4.1 Xác thực người dùng

- Đăng nhập / Đăng ký với phân loại hành khách / tài xế
- JWT token-based authentication (thời hạn 30 ngày)
- Validation form real-time
- Chỉ báo độ mạnh mật khẩu
- Animated gradient UI
- Auto-login từ Splash screen

### 4.2 Tính năng hành khách

| Tính năng | Mô tả |
|-----------|-------|
| **Đặt xe** | Chọn điểm đón, điểm đến, loại xe (xe máy, 4 chỗ, 7 chỗ) |
| **Theo dõi chuyến** | Trạng thái: pending → accepted → arrived → in_progress → completed |
| **Đánh giá** | Rate tài xế 1-5 sao kèm bình luận |
| **Lịch sử chuyến đi** | Xem danh sách chuyến đã hoàn thành, có bộ lọc |
| **Quản lý hồ sơ** | Cập nhật thông tin cá nhân |

### 4.3 Tính năng tài xế

| Tính năng | Mô tả |
|-----------|-------|
| **Online/Offline** | Bật/tắt trạng thái nhận chuyến |
| **Nhận chuyến** | Xem danh sách yêu cầu, chấp nhận hoặc từ chối |
| **Cập nhật trạng thái** | Đánh dấu đã đến, bắt đầu, hoàn thành chuyến |
| **Thu nhập** | Xem thu nhập theo ngày, tuần, tháng, tổng |
| **Lịch sử chuyến đi** | Xem danh sách chuyến đã hoàn thành |
| **Quản lý hồ sơ** | Cập nhật thông tin xe |

### 4.4 Tính năng AI

| Tính năng | Mô tả |
|-----------|-------|
| **AI Trip Scheduler** | Tạo lịch trình nhiều điểm dừng, tối ưu theo thời gian / chi phí / cân bằng |
| **AI Route Alternatives** | Đề xuất các tuyến đường thay thế với các kịch bản giao thông |
| **AI Learning Profile** | Ghi nhận thời gian di chuyển ưa thích, slider chi phí/thời gian, địa điểm thường đến |
| **Ride Batching (tài xế)** | AI đề xuất nhóm hành khách tối ưu hóa, chấm điểm hiệu quả |
| **Personalized Recommendations** | Tuyến đường thường dùng, thời gian tốt nhất, tiết kiệm chi phí |

---

## 5. Database Schema

### 5.1 Bảng cốt lõi

#### `users` - Tài khoản người dùng

| Column | Type | Mô tả |
|--------|------|-------|
| `id` | INT PRIMARY KEY | ID người dùng |
| `email` | VARCHAR(255) UNIQUE | Email đăng nhập |
| `password_hash` | VARCHAR(255) | Mật khẩu đã mã hóa |
| `full_name` | VARCHAR(255) | Họ tên đầy đủ |
| `phone` | VARCHAR(20) | Số điện thoại |
| `user_type` | ENUM | passenger, driver, owner, admin |
| `total_rides` | INT DEFAULT 0 | Tổng số chuyến đã đi |
| `created_at` | TIMESTAMP | Ngày tạo |

#### `drivers` - Hồ sơ tài xế

| Column | Type | Mô tả |
|--------|------|-------|
| `id` | INT PRIMARY KEY | ID tài xế |
| `user_id` | INT FK | Liên kết users |
| `license_plate` | VARCHAR(20) | Biển số xe |
| `vehicle_type` | ENUM | motorbike, 4seat, 7seat |
| `vehicle_model` | VARCHAR(100) | Mẫu xe |
| `is_online` | BOOLEAN | Trạng thái online |
| `latitude` | DECIMAL(10,8) | Vĩ độ hiện tại |
| `longitude` | DECIMAL(11,8) | Kinh độ hiện tại |

#### `rides` - Bản ghi chuyến đi

| Column | Type | Mô tả |
|--------|------|-------|
| `id` | INT PRIMARY KEY | ID chuyến đi |
| `passenger_id` | INT FK | ID hành khách |
| `driver_id` | INT FK | ID tài xế |
| `pickup_lat` | DECIMAL(10,8) | Vĩ độ điểm đón |
| `pickup_lng` | DECIMAL(11,8) | Kinh độ điểm đón |
| `pickup_address` | VARCHAR(255) | Địa chỉ điểm đón |
| `dropoff_lat` | DECIMAL(10,8) | Vĩ độ điểm trả |
| `dropoff_lng` | DECIMAL(11,8) | Kinh độ điểm trả |
| `dropoff_address` | VARCHAR(255) | Địa chỉ điểm trả |
| `vehicle_type` | ENUM | Loại xe |
| `status` | ENUM | pending, accepted, arrived, in_progress, completed, cancelled |
| `fare` | DECIMAL(10,2) | Giá tiền |
| `rating` | INT | Đánh giá (1-5) |
| `comment` | TEXT | Bình luận |
| `created_at` | TIMESTAMP | Thời gian tạo |

#### `driver_locations` - Vị trí real-time của tài xế

| Column | Type | Mô tả |
|--------|------|-------|
| `driver_id` | INT PK FK | ID tài xế |
| `latitude` | DECIMAL(10,8) | Vĩ độ |
| `longitude` | DECIMAL(11,8) | Kinh độ |
| `updated_at` | TIMESTAMP | Thời gian cập nhật |

#### `earnings` - Bản ghi thu nhập tài xế

| Column | Type | Mô tả |
|--------|------|-------|
| `id` | INT PRIMARY KEY | ID |
| `driver_id` | INT FK | ID tài xế |
| `ride_id` | INT FK | ID chuyến đi |
| `amount` | DECIMAL(10,2) | Số tiền |
| `date` | DATE | Ngày |

### 5.2 Bảng AI

#### `ai_trip_schedules` - Kế hoạch chuyến đi

| Column | Type | Mô tả |
|--------|------|-------|
| `id` | INT PRIMARY KEY | ID lịch trình |
| `user_id` | INT FK | ID người dùng |
| `schedule_name` | VARCHAR(255) | Tên lịch trình |
| `optimization_type` | ENUM | time, cost, balanced |
| `created_at` | TIMESTAMP | Ngày tạo |

#### `ai_waypoints` - Các điểm dừng

| Column | Type | Mô tả |
|--------|------|-------|
| `id` | INT PRIMARY KEY | ID |
| `schedule_id` | INT FK | ID lịch trình |
| `location_name` | VARCHAR(255) | Tên địa điểm |
| `latitude` | DECIMAL(10,8) | Vĩ độ |
| `longitude` | DECIMAL(11,8) | Kinh độ |
| `order` | INT | Thứ tự |

#### `ai_route_alternatives` - Tuyến đường thay thế

| Column | Type | Mô tả |
|--------|------|-------|
| `id` | INT PRIMARY KEY | ID |
| `schedule_id` | INT FK | ID lịch trình |
| `route_name` | VARCHAR(100) | Tên tuyến (e.g., Fastest, Cheapest) |
| `traffic_scenario` | ENUM | normal, rush_hour, off_peak |
| `estimated_time` | INT | Thời gian ước tính (phút) |
| `estimated_cost` | DECIMAL(10,2) | Chi phí ước tính |

#### `ai_learning_profiles` - Hồ sơ học sở thích

| Column | Type | Mô tả |
|--------|------|-------|
| `id` | INT PRIMARY KEY | ID |
| `user_id` | INT FK UNIQUE | ID người dùng |
| `preferred_time_start` | TIME | Giờ bắt đầu ưa thích |
| `preferred_time_end` | TIME | Giờ kết thúc ưa thích |
| `cost_vs_time_preference` | DECIMAL(3,2) | Slider chi phí/thời gian (0.00-1.00) |
| `frequent_locations` | JSON | Array địa điểm thường đến |

#### `driver_route_batches` - Nhóm chuyến đi gộp

| Column | Type | Mô tả |
|--------|------|-------|
| `id` | INT PRIMARY KEY | ID |
| `driver_id` | INT FK | ID tài xế |
| `efficiency_score` | DECIMAL(3,2) | Điểm hiệu quả (0.00-1.00) |
| `status` | ENUM | pending, accepted, rejected |
| `created_at` | TIMESTAMP | Ngày tạo |

#### `batch_passengers` - Hành khách trong batch

| Column | Type | Mô tả |
|--------|------|-------|
| `id` | INT PRIMARY KEY | ID |
| `batch_id` | INT FK | ID batch |
| `passenger_id` | INT FK | ID hành khách |
| `ride_id` | INT FK | ID chuyến đi |

### 5.3 Trigger

```sql
-- Tự động tăng total_rides cho user sau khi hoàn thành chuyến đi
CREATE TRIGGER after_ride_completed
AFTER UPDATE ON rides
FOR EACH ROW
BEGIN
  IF NEW.status = 'completed' AND OLD.status != 'completed' THEN
    UPDATE users SET total_rides = total_rides + 1 WHERE id = NEW.passenger_id;
  END IF;
END;
```

---

## 6. API Endpoints

### 6.1 Authentication

| Method | Endpoint | Mô tả | Body |
|--------|----------|-------|------|
| POST | `/api/auth/register` | Đăng ký | `{ email, password, full_name, phone, user_type }` |
| POST | `/api/auth/login` | Đăng nhập | `{ email, password }` |

**Response (Login):**
```json
{
  "success": true,
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "email": "user@example.com",
    "full_name": "Nguyen Van A",
    "user_type": "passenger"
  }
}
```

### 6.2 Rides

| Method | Endpoint | Mô tả | Body / Params |
|--------|----------|-------|---------------|
| POST | `/api/rides/request` | Yêu cầu đặt xe | `{ pickup_lat, pickup_lng, dropoff_lat, dropoff_lng, vehicle_type }` |
| GET | `/api/rides` | Lấy lịch sử chuyến đi | Query: `?status=completed` |
| PUT | `/api/rides/:id/status` | Cập nhật trạng thái | `{ status: "accepted" }` |
| POST | `/api/rides/:id/rate` | Đánh giá chuyến đi | `{ rating, comment }` |

**Trạng thái chuyến đi:**
```
pending → accepted → arrived → in_progress → completed
                ↘ cancelled ↙
```

### 6.3 Driver

| Method | Endpoint | Mô tả | Body / Params |
|--------|----------|-------|---------------|
| GET | `/api/driver/profile` | Lấy hồ sơ tài xế | - |
| PUT | `/api/driver/status` | Bật/tắt online | `{ is_online: true }` |
| GET | `/api/driver/ride/available` | Lấy yêu cầu chờ | - |
| POST | `/api/driver/ride/:id/accept` | Nhận chuyến đi | - |
| GET | `/api/driver/earnings` | Lấy thu nhập | Query: `?period=today\|week\|month\|total` |

### 6.4 AI

| Method | Endpoint | Mô tả | Body |
|--------|----------|-------|------|
| POST | `/api/ai/schedule/create` | Tạo lịch trình AI | `{ schedule_name, waypoints[], optimization_type }` |
| POST | `/api/ai/schedule/:id/optimize` | Tối ưu lại tuyến | `{ optimization_type }` |
| GET | `/api/ai/recommendations` | Lấy đề xuất cá nhân | - |
| GET | `/api/ai/batch/available` | Lấy nhóm chuyến đi | - |
| POST | `/api/ai/batch/:id/accept` | Nhận batch | - |

**Cấu trúc tạo schedule:**
```json
{
  "schedule_name": "Chuyến đi cuối tuần",
  "waypoints": [
    { "location_name": "Nhà", "latitude": 10.8231, "longitude": 106.6297 },
    { "location_name": "Quán cafe", "latitude": 10.8300, "longitude": 106.6400 },
    { "location_name": "Trung tâm thương mại", "latitude": 10.8350, "longitude": 106.6500 }
  ],
  "optimization_type": "balanced"
}
```

### 6.5 Location

| Method | Endpoint | Mô tả | Body |
|--------|----------|-------|------|
| GET | `/api/users/drivers/nearby` | Tìm tài xế gần | Query: `?lat=10.8231&lng=106.6297&radius=5` |
| PUT | `/api/location/update` | Cập nhật vị trí | `{ latitude, longitude }` |

---

## 7. Cấu hình

### 7.1 Backend `.env`

```env
DB_HOST=localhost
DB_USER=root
DB_PASSWORD=1234
DB_NAME=doan3_db
DB_PORT=3306
JWT_SECRET=DoAn3_KhoaTuanAnh_2026_JWT_Secret_Key_xK9mP2vL5nQ8jR4wT7yB3cF6hJ1mN4pL9sZ2aB5dE8fG1
PORT=3000
```

### 7.2 Android `AppConfig.kt`

```kotlin
// Emulator (mặc định)
const val BASE_URL = "http://10.0.2.2:3000/api/"

// Thiết bị thật (cùng mạng LAN)
const val BASE_URL = "http://192.168.x.x:3000/api/"
```

> **Lưu ý:** Thay `192.168.x.x` bằng địa chỉ IP thực của máy chạy backend trong cùng mạng LAN. Kiểm tra IP bằng lệnh `ipconfig` (Windows) hoặc `ifconfig` (Linux/Mac).

### 7.3 Database Setup

```bash
# Kết nối MySQL và chạy schema
mysql -u root -p < backend/src/database/schema.sql

# Chạy dữ liệu test
mysql -u root -p doan3_db < backend/src/database/seed.sql
```

---

## 8. Tài khoản test

| Loại | Email | Mật khẩu |
|------|-------|-----------|
| Hành khách | passenger@test.com | password123 |
| Tài xế 1 | driver1@test.com | password123 |
| Tài xế 2 | driver2@test.com | password123 |
| Tài xế 3 | driver3@test.com | password123 |

---

## 9. Hướng dẫn cài đặt

### 9.1 Backend

```bash
# 1. Di chuyển vào thư mục backend
cd backend

# 2. Cài đặt dependencies
npm install

# 3. Tạo database và chạy schema
mysql -u root -p < src/database/schema.sql
mysql -u root -p < src/database/seed.sql

# 4. Chạy server
npm start
# Hoặc dev mode:
npm run dev
```

Server sẽ chạy tại `http://localhost:3000`

### 9.2 Android App

```bash
# 1. Di chuyển vào thư mục app
cd app

# 2. Chạy build debug APK
./gradlew assembleDebug

# APK sẽ nằm tại:
# app/build/outputs/apk/debug/app-debug.apk
```

### 9.3 Cài đặt APK trên thiết bị thật

1. Đảm bảo backend đang chạy và thiết bị cùng mạng LAN
2. Cập nhật `AppConfig.kt` với IP thực của máy chạy backend
3. Build lại APK và cài đặt trên thiết bị
4. Bật debug USB hoặc cài trực tiếp file APK

---


---

## 9.5 Các lỗi đã được sửa (2026-05-10)

### Android

| File | Lỗi | Fix |
|------|------|-----|
| `di/AppModule.kt` | `baseUrl` hardcoded | Thay bang `AppConfig.BASE_URL` |
| `ui/theme/Theme.kt` | Deprecated `window.statusBarColor` | Xoa dong set + import `toArgb` |
| `ui/components/CommonComponents.kt` | `Icons.Default.ArrowBack` deprecated | `Icons.AutoMirrored.Filled.ArrowBack` |
| `ui/screens/ProfileScreen.kt` | `Icons.Default.Help/Logout` deprecated | `Icons.AutoMirrored.Filled.*` |
| `ui/screens/driver/DriverScreens.kt` | `Icons.Default.TrendingUp` deprecated | `Icons.AutoMirrored.Filled.TrendingUp` |
| `ui/screens/passenger/PassengerHomeScreen.kt` | `Icons.Default.Chat/ArrowForward` deprecated | `Icons.AutoMirrored.Filled.*` |

### Backend

| File | Lỗi | Fix |
|------|------|-----|
| `socket/index.js` | `getIO()` tra ve `undefined` (runtime crash) | Them `ioInstance` global variable |
| `routes/locations.js` | SyntaxError - dau ngoac thua `)` | Xoa dau `)` thua |

### Ket qua build

| Kiem tra | Ket qua |
|----------|---------|
| Android Clean Build | BUILD SUCCESSFUL (43 tasks, ~1 phut) |
| Android APK | `app-debug.apk` - 19.55 MB |
| Android Warnings | 0 |
| Backend JS Syntax (20 files) | Tat ca OK |
| APK Path | `app/build/outputs/apk/debug/app-debug.apk` |


---

## 9.5 Các lỗi đã được sửa (2026-05-10)

### Android

| File | Lỗi | Fix |
|------|------|-----|
| `di/AppModule.kt` | `baseUrl` hardcoded | Thay bang `AppConfig.BASE_URL` |
| `ui/theme/Theme.kt` | Deprecated `window.statusBarColor` | Xoa dong set + import `toArgb` |
| `ui/components/CommonComponents.kt` | `Icons.Default.ArrowBack` deprecated | `Icons.AutoMirrored.Filled.ArrowBack` |
| `ui/screens/ProfileScreen.kt` | `Icons.Default.Help/Logout` deprecated | `Icons.AutoMirrored.Filled.*` |
| `ui/screens/driver/DriverScreens.kt` | `Icons.Default.TrendingUp` deprecated | `Icons.AutoMirrored.Filled.TrendingUp` |
| `ui/screens/passenger/PassengerHomeScreen.kt` | `Icons.Default.Chat/ArrowForward` deprecated | `Icons.AutoMirrored.Filled.*` |

### Backend

| File | Lỗi | Fix |
|------|------|-----|
| `socket/index.js` | `getIO()` tra ve `undefined` (runtime crash) | Them `ioInstance` global variable |
| `routes/locations.js` | SyntaxError - dau ngoac thua `)` | Xoa dau `)` thua |

### Ket qua build

| Kiem tra | Ket qua |
|----------|---------|
| Android Clean Build | BUILD SUCCESSFUL (43 tasks, ~1 phut) |
| Android APK | `app-debug.apk` - 19.55 MB |
| Android Warnings | 0 |
| Backend JS Syntax (20 files) | Tat ca OK |
| APK Path | `app/build/outputs/apk/debug/app-debug.apk` |

## 10. Các file quan trọng

| File | Mục đích |
|------|----------|
| `AppConfig.kt` | Cấu hình BASE_URL backend |
| `ApiService.kt` | Interface định nghĩa tất cả API endpoints |
| `SessionManager.kt` | Lưu trữ token trong SharedPreferences |
| `AuthScreen.kt` | Giao diện đăng nhập/đăng ký với animation |
| `PassengerHomeScreen.kt` | Giao diện chính của hành khách |
| `DriverHomeScreen.kt` | Giao diện chính của tài xế |
| `AIScreens.kt` | Giao diện các tính năng AI |
| `schema.sql` | Định nghĩa 11 bảng database |
| `seed.sql` | Dữ liệu test ban đầu |

---

*Tài liệu này được tạo tự động từ codebase DoAn3 - 2026*
