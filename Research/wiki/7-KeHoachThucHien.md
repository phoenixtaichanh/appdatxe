# Kế hoạch thực hiện

---

## Mục lục

1. [Tổng quan](#1-tổng-quan)
2. [Timeline](#2-timeline)
3. [Chi tiết từng giai đoạn](#3-chi-tiết-từng-giai-đoạn)
4. [Deliverables](#4-deliverables)
5. [Công nghệ và công cụ](#5-công-nghệ-và-công-cụ)
6. [Rủi ro và kế hoạch dự phòng](#6-rủi-ro-và-kế-hoạch-dự-phòng)

---

## 1. Tổng quan

Đề tài được thực hiện trong **10 tuần** (từ 19/03/2026 đến 27/05/2026), với sự tham gia của **2 thành viên**:

| Thành viên | Vai trò | Nhiệm vụ chính |
|---|---|---|
| **Lê Đăng Khoa** (24IT119) | Tech Lead | Backend, API, Database, AI Module |
| **Trần Nguyễn Tuấn Anh** (24IT010) | Frontend Lead | Android UI, Navigation, State Management |

**Giảng viên hướng dẫn:** TS. Nguyễn Quang Vũ

---

## 2. Timeline

```
Tuần 1     Tuần 2     Tuần 3     Tuần 4     Tuần 5     Tuần 6     Tuần 7     Tuần 8     Tuần 9     Tuần 10
  19-25       26-01      02-08      09-15      16-22      23-29      30-06      07-13      14-20      21-27
 Mar       Mar        Apr        Apr        Apr        Apr        May        May        May        May
```

| Tuần | Giai đoạn | Công việc chính |
|---|---|---|
| **1** | Khảo sát & Phân tích | Xác định yêu cầu, Use Case, Kiến trúc |
| **2** | Thiết kế DB | ERD, Schema, Bảng, Indexes |
| **3** | Thiết kế UI/UX | Wireframe, Figma, Design System |
| **4** | Setup Project | Android project, Backend setup, DB init |
| **5** | Auth & User | Register, Login, Profile, JWT |
| **6** | Rides Core | Đặt xe, Tìm tài xế, Theo dõi |
| **7** | Payments & History | Thanh toán, Hóa đơn, Lịch sử |
| **8** | AI Module | Schedule, Optimization, Batching |
| **9** | Testing | Kiểm thử, Fix bugs, Optimization |
| **10** | Deployment | Triển khai, Hoàn thiện báo cáo |

---

## 3. Chi tiết từng giai đoạn

### Tuần 1: Khảo sát & Phân tích (19-25/03/2026)

#### Mục tiêu
- Hoàn thành tài liệu yêu cầu (SRS)
- Xác định phạm vi dự án
- Phác thảo kiến trúc tổng quan

#### Công việc cụ thể

**Lê Đăng Khoa:**
- Khảo sát yêu cầu nghiệp vụ
- Xây dựng Use Case Diagram
- Thiết kế kiến trúc hệ thống (3-tier)
- Viết tài liệu phân tích

**Trần Nguyễn Tuấn Anh:**
- Nghiên cứu UI/UX của các app đặt xe (Grab, Be, Uber)
- Phân tích đối tượng người dùng (Personas)
- Xây dựng Information Architecture
- Phác thảo wireframe sơ bộ

#### Deliverables
- [x] Báo cáo phân tích yêu cầu
- [x] Use Case Diagram
- [x] Sơ đồ kiến trúc hệ thống
- [x] Danh sách tính năng (Feature List)

---

### Tuần 2: Thiết kế Cơ sở dữ liệu (26/03-01/04/2026)

#### Mục tiêu
- Hoàn thành ERD chi tiết
- Tạo Schema MySQL
- Xác định indexes và constraints

#### Công việc cụ thể

**Lê Đăng Khoa:**
- Thiết kế ERD (11 bảng)
- Viết SQL Schema (users, drivers, rides, payments, earnings, ai_*)
- Tạo indexes cho hiệu năng
- Viết seed data cho test
- Cài đặt MySQL Server

**Trần Nguyễn Tuấn Anh:**
- Thiết kế data models trong Android
- Tạo class diagram (Entity, DTO)
- Lên kế hoạch API endpoints

#### Deliverables
- [x] ERD chi tiết
- [x] SQL Schema hoàn chỉnh
- [x] Database indexes plan
- [x] API endpoints specification

---

### Tuần 3: Thiết kế UI/UX (02-08/04/2026)

#### Mục tiêu
- Hoàn thành wireframe cho tất cả màn hình
- Xây dựng Design System
- Tạo prototype trên Figma

#### Công việc cụ thể

**Trần Nguyễn Tuấn Anh:**
- Thiết kế wireframe cho 25+ màn hình
- Xây dựng Design System (colors, typography, spacing)
- Tạo prototype tương tác trên Figma
- Thiết kế icon set, illustrations

**Lê Đăng Khoa:**
- Review wireframe từ góc độ kỹ thuật
- Đánh giá tính khả thi của thiết kế
- Feedback về UX flow

#### Deliverables
- [x] Wireframe tất cả màn hình (Figma)
- [x] Design System documentation
- [x] Component library
- [x] User flow diagrams

---

### Tuần 4: Setup Project (09-15/04/2026)

#### Mục tiêu
- Khởi tạo Android project với cấu trúc MVVM
- Khởi tạo Backend Node.js/Express
- Kết nối Database
- Setup CI/CD (nếu có thời gian)

#### Công việc cụ thể

**Lê Đăng Khoa:**
```
Backend Setup:
├── Khởi tạo Node.js project
├── Cài đặt Express, MySQL2, JWT, bcrypt
├── Tạo cấu trúc thư mục (routes, controllers, models)
├── Kết nối MySQL
├── Test connection
└── Push lên Git
```

**Trần Nguyễn Tuấn Anh:**
```
Android Setup:
├── Khởi tạo Android Studio project
├── Cài đặt Kotlin, Compose, Hilt, Retrofit
├── Tạo cấu trúc package (data, domain, ui)
├── Setup Navigation
├── Setup Hilt DI
├── Tạo base components (Button, TextField, Card)
└── Push lên Git
```

#### Deliverables
- [x] Android project với Gradle sync thành công
- [x] Backend chạy được trên localhost:3000
- [x] Database connected
- [x] Git repository initialized

---

### Tuần 5: Authentication & User Management (16-22/04/2026)

#### Mục tiêu
- Hoàn thành đăng ký / đăng nhập
- JWT authentication
- Quản lý profile

#### Công việc cụ thể

**Lê Đăng Khoa:**
```
Backend Auth:
├── POST /api/auth/register
├── POST /api/auth/login
├── JWT middleware
├── bcrypt password hashing
├── GET /api/users/me
├── PUT /api/users/me
└── Error handling
```

**Trần Nguyễn Tuấn Anh:**
```
Android Auth UI:
├── SplashScreen
├── LoginScreen (email, password)
├── RegisterScreen (name, email, phone, password)
├── ForgotPasswordScreen
├── SessionManager (SharedPreferences)
├── AuthRepository (Hilt injection)
├── ViewModels
└── Navigation flow
```

#### Deliverables
- [x] Backend: Auth APIs hoạt động
- [x] Android: Login/Register screens hoàn chỉnh
- [x] JWT token lưu trữ và refresh
- [x] Profile screen với edit

---

### Tuần 6: Rides Core (23-29/04/2026)

#### Mục tiêu
- Hoàn thành chức năng đặt xe
- Tìm và hiển thị tài xế gần đó
- Theo dõi hành trình (real-time)

#### Công việc cụ thể

**Lê Đăng Khoa:**
```
Backend Rides:
├── POST /api/rides (tạo ride)
├── GET /api/rides (lịch sử)
├── GET /api/rides/:id (chi tiết)
├── PATCH /api/rides/:id/status
├── GET /api/location/nearby-drivers (Haversine)
├── POST /api/driver/accept/:rideId
├── POST /api/driver/reject/:rideId
├── PUT /api/driver/status (online/offline)
└── PUT /api/driver/location (GPS update)
```

**Trần Nguyễn Tuấn Anh:**
```
Android Rides UI:
├── PassengerHomeScreen
│   ├── Map placeholder
│   ├── BookingBottomSheet
│   ├── Driver selection
│   └── Price estimation
├── ActiveRideScreen
│   ├── Real-time tracking
│   ├── Status updates
│   └── ETA display
├── DriverHomeScreen
│   ├── Online toggle
│   ├── Ride request cards
│   └── Accept/Reject buttons
└── RideDetailScreen
```

#### Deliverables
- [x] Backend: Rides APIs đầy đủ
- [x] Android: Đặt xe flow hoàn chỉnh
- [x] Android: Driver nhận chuyến
- [x] Real-time status updates

---

### Tuần 7: Payments & History (30/04-06/05/2026)

#### Mục tiêu
- Chức năng thanh toán
- Hóa đơn
- Lịch sử chuyến đi
- Đánh giá tài xế

#### Công việc cụ thể

**Lê Đăng Khoa:**
```
Backend:
├── POST /api/rides/:id/rate
├── GET /api/driver/earnings
├── Earnings aggregation
├── Ride history filtering
└── Rating calculation
```

**Trần Nguyễn Tuấn Anh:**
```
Android UI:
├── PaymentScreen
│   ├── Invoice display
│   ├── Cash payment
│   └── Transfer payment
├── InvoiceScreen
├── HistoryScreen
│   ├── Filter (all/completed/cancelled)
│   └── Ride cards
├── RatingScreen
│   ├── Star rating
│   └── Comment
├── EarningsScreen (Driver)
│   ├── Today/Week/Month tabs
│   └── Charts
└── DriverHistoryScreen
```

#### Deliverables
- [x] Thanh toán tiền mặt
- [x] Hóa đơn chi tiết
- [x] Lịch sử chuyến đi
- [x] Đánh giá sao + comment
- [x] Thu nhập tài xế

---

### Tuần 8: AI Module (07-13/05/2026)

#### Mục tiêu
- Trợ lý du lịch AI (chat interface)
- Tạo lịch trình AI (Schedule)
- Route optimization (nhanh/rẻ/cân bằng)
- Driver batching (gom chuyến)

#### Công việc cụ thể

**Lê Đăng Khoa:**
```
Backend AI:
├── POST /api/ai/schedules
├── GET /api/ai/schedules
├── PUT /api/ai/schedules/:id/optimize
├── GET /api/ai/route-alternatives
├── GET /api/ai/profile
├── PUT /api/ai/profile
├── GET /api/ai/recommendations
├── GET /api/ai/schedule-preview
├── GET /api/driver/batches/available
├── POST /api/driver/batches/:id/accept
└── POST /api/driver/batches/:id/reject
```

**Trần Nguyễn Tuấn Anh:**
```
Android AI UI:
├── AIScreen
│   ├── Chat interface
│   ├── Quick actions
│   └── Recommendations list
├── AIScheduleScreen
│   ├── Create schedule
│   ├── Waypoints editor
│   └── Route alternatives
├── AIProfileScreen
│   ├── Preferences slider
│   └── Time preferences
├── AIRecommendationsScreen
│   ├── Personalized tips
│   └── Savings estimate
├── BatchOfferScreen (Driver)
│   ├── Batch details
│   └── Accept/Reject
└── AIHistoryScreen
```

#### Deliverables
- [x] Backend: AI APIs đầy đủ
- [x] Android: AI Schedule creation
- [x] Android: Route optimization (3 modes)
- [x] Android: Driver batch offers
- [x] Android: Personalized recommendations

---

### Tuần 9: Testing & Bug Fixes (14-20/05/2026)

#### Mục tiêu
- Kiểm thử chức năng (functional testing)
- Fix bugs phát hiện được
- Tối ưu hiệu năng

#### Công việc cụ thể

| Loại test | Chi tiết | Ai làm |
|---|---|---|
| Unit Test | Backend: Auth, Rides, AI logic | Lê Đăng Khoa |
| UI Test | Android: Login, Booking, Rating | Trần Nguyễn Tuấn Anh |
| API Test | POSTMAN collection | Lê Đăng Khoa |
| Integration Test | Android ↔ Backend | Cả 2 |
| Performance Test | Load testing, stress test | Lê Đăng Khoa |
| UX Review | Usability testing | Trần Nguyễn Tuấn Anh |

#### Test Cases quan trọng

| ID | Test Case | Kết quả mong đợi |
|---|---|---|
| TC001 | Đăng ký tài khoản mới | Tạo thành công, nhận JWT |
| TC002 | Đăng nhập sai mật khẩu | Hiển thị lỗi "Sai mật khẩu" |
| TC003 | Đặt xe thành công | Ride tạo với status "pending" |
| TC004 | Tài xế nhận chuyến | Status chuyển sang "accepted" |
| TC005 | Tài xế cập nhật GPS | Vị trí cập nhật trong DB |
| TC006 | Tạo AI schedule | Tạo với route alternatives |
| TC007 | Tối ưu schedule (cost) | Trả về route rẻ nhất |
| TC008 | Driver accept batch | Batch chuyển sang "accepted" |
| TC009 | Đánh giá chuyến đi | Rating lưu và cập nhật average |
| TC010 | Xem thu nhập | Hiển thị đúng số liệu |

#### Deliverables
- [x] Test case documentation
- [x] Bug report & fix log
- [x] Performance optimization
- [x] Security check

---

### Tuần 10: Deployment & Hoàn thiện (21-27/05/2026)

#### Mục tiêu
- Triển khai ứng dụng
- Hoàn thiện báo cáo
- Chuẩn bị bảo vệ

#### Công việc cụ thể

**Lê Đăng Khoa:**
- Triển khai Backend (localhost / hosting)
- Chạy thử trên Android emulator/real device
- Kiểm tra tất cả APIs
- Hoàn thiện báo cáo Chương 4, 5
- Chuẩn bị slide bảo vệ

**Trần Nguyễn Tuấn Anh:**
- Build APK release
- Kiểm tra trên real device
- Chụp ảnh giao diện (screenshots)
- Hoàn thiện tài liệu
- Practice presentation

#### Deliverables
- [x] Backend chạy ổn định
- [x] APK build thành công
- [x] Báo cáo đồ án hoàn chỉnh
- [x] Slide bảo vệ
- [x] Demo video

---

## 4. Deliverables

### 4.1. Tài liệu

| # | Tài liệu | Trạng thái |
|---|---|---|
| 1 | Đề cương chi tiết | ✅ Hoàn thành |
| 2 | Báo cáo phân tích yêu cầu (SRS) | ✅ Hoàn thành |
| 3 | Thiết kế ERD | ✅ Hoàn thành |
| 4 | Thiết kế API | ✅ Hoàn thành |
| 5 | Wireframe UI/UX | ✅ Hoàn thành |
| 6 | Test case documentation | ⏳ Tuần 9 |
| 7 | Báo cáo đồ án (5 chương) | ⏳ Tuần 10 |
| 8 | Slide bảo vệ | ⏳ Tuần 10 |

### 4.2. Sản phẩm phần mềm

| # | Sản phẩm | Trạng thái |
|---|---|---|
| 1 | Backend Node.js API | ✅ Hoàn thành |
| 2 | Android App (APK) | ✅ Build thành công |
| 3 | Database Schema | ✅ Hoàn thành |
| 4 | Source code (Git) | ✅ Committed |
| 5 | Demo video | ⏳ Tuần 10 |

---

## 5. Công nghệ và công cụ

### 5.1. Development Tools

| Công cụ | Mục đích | Phiên bản |
|---|---|---|
| **Android Studio** | Phát triển Android app | Latest (2024+) |
| **VS Code** | Phát triển Backend | Latest |
| **MySQL Workbench** | Quản lý Database | 8.0+ |
| **Postman** | API Testing | Latest |
| **Figma** | UI/UX Design | Free tier |
| **Git** | Version Control | 2.40+ |
| **GitHub** | Remote repo | - |
| **Node.js** | Runtime | 18+ |
| **npm** | Package manager | 9+ |

### 5.2. Dependencies chính

**Android (`app/build.gradle.kts`):**
```kotlin
// Core
implementation("androidx.core:core-ktx:1.13.0")
implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.0")
implementation("androidx.activity:activity-compose:1.9.0")

// Compose
implementation(platform("androidx.compose:compose-bom:2024.06.00"))
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.material3:material3")

// Navigation
implementation("androidx.navigation:navigation-compose:2.7.7")

// Networking
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.okhttp3:okhttp:4.12.0")

// DI
implementation("com.google.dagger:hilt-android:2.51")
ksp("com.google.dagger:hilt-android-compiler:2.51")

// Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
```

**Backend (`package.json`):**
```json
{
    "dependencies": {
        "express": "^4.21.2",
        "mysql2": "^3.12.0",
        "jsonwebtoken": "^9.0.2",
        "bcryptjs": "^2.4.3",
        "cors": "^2.8.5",
        "dotenv": "^16.4.7"
    }
}
```

---

## 6. Rủi ro và kế hoạch dự phòng

### 6.1. Rủi ro

| # | Rủi ro | Mức độ | Xác suất | Kế hoạch dự phòng |
|---|---|---|---|---|
| 1 | Backend API chậm hoặc lỗi | Cao | Trung bình | Sử dụng mock data, tối ưu truy vấn |
| 2 | Android build failed | Trung bình | Thấp | Kiểm tra Gradle sync thường xuyên |
| 3 | Công thức Haversine không chính xác | Trung bình | Thấp | Sử dụng Google Distance Matrix API |
| 4 | AI algorithm chưa tối ưu | Trung bình | Trung bình | Sử dụng heuristic đơn giản, test nhiều case |
| 5 | GPS tracking không ổn định | Cao | Trung bình | Sử dụng FusedLocationProviderClient |
| 6 | Hết thời gian | Cao | Thấp | Ưu tiên core features, defer less important |

### 6.2. Phân chia công việc khi có khó khăn

Nếu một thành viên gặp khó khăn:
- **Lê Đăng Khoa** (Backend): Hỗ trợ Android trong trường hợp cần thiết
- **Trần Nguyễn Tuấn Anh** (Frontend): Hỗ trợ Backend trong trường hợp cần thiết

---

## Liên kết

- [ Quay lại: Thiết kế API](./6-ThietKeAPI.md)
- [ Quay lại: Tổng quan đề tài](./1-TongQuanDeTai.md)
