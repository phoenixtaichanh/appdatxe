# DoAn3 - Smart Ride Booking System with AI Travel Assistant

> **Phien ban:** 1.3.0 | **Ngay cap nhat:** 2026-05-13
> **Trang thai:** Backend 100% | Android 100% | Admin Panel 100% | BUILD SUCCESSFUL
> **Tac gia:** Le Dang Khoa, Tran Nguyen Tuan Anh
> **Truong:** Dai hoc Bach Khoa TP.HCM (HUTECH)
> **App ID:** `com.laptrinhdidong.DoAn3`

---

## Muc luc

1. [Tong quan du an](#1-tong-quan-du-an)
2. [Cong nghe su dung](#2-cong-nghe-su-dung)
3. [Cau truc du an](#3-cau-truc-du-an)
4. [Tinh nang](#4-tinh-nang)
5. [Database Schema](#5-database-schema)
6. [API Endpoints](#6-api-endpoints)
7. [WebSocket Events](#7-websocket-events)
8. [Cau hinh](#8-cau-hinh)
9. [Tai khoan test](#9-tai-khoan-test)
10. [Huong dan cai dat](#10-huong-dan-cai-dat)
11. [Cac loi da duoc sua](#11-cac-loi-da-duoc-sua)
12. [Lich su phat trien](#12-lich-su-phat-trien)

---

## 1. Tong quan du an

**DoAn3** la mot ung dung di dong **dat xe truc tuyen** (ride-hailing, giong Uber) ket hop tinh nang **tro ly du lich AI**. Nguoi dung co the dat xe may, xe 4 cho hoac xe 7 cho, dong thoi su dung cac tinh nang AI de lap ke hoach hanh trinh, toi uu tuyen duong va nhan deu xuat ca nhan hoa.

### 1.1 Cac loai nguoi dung

| Loai | Mo ta | Quyen |
|------|-------|-------|
| **Passenger** | Khach hang dat xe | Dat xe, theo doi, danh gia |
| **Driver** | Tai xe nhan chuyen | Nhan chuyen, cap nhat trang thai, xem thu nhap |
| **Owner** | Chu xe | Quan ly phuong tien |
| **Consultant** | Tu van khach hang | Ho tro khach hang |
| **Admin** | Quan tri he thong | Quan ly toan bo he thong |
| **HR Manager** | Quan ly nhan su | Quan ly tai xe |
| **Revenue Manager** | Quan ly doanh thu | Xem bao cao doanh thu |

### 1.2 Cac thanh phan chinh

| Thanh phan | Mo ta |
|------------|-------|
| **Android App** | Ung dung Kotlin + Jetpack Compose cho nguoi dung cuoi |
| **Backend API** | Node.js + Express xu ly logic, MySQL |
| **Admin Panel** | React + Vite + Tailwind CSS |
| **Socket.IO** | Giao tiep real-time giua app va server |
| **FCM** | Push notifications tu server den app |

---

## 2. Cong nghe su dung

### 2.1 Frontend (Android)

| Thanh phan | Cong nghe | Phien ban |
|------------|-----------|-----------|
| Ngon ngu | Kotlin | 1.9+ |
| UI Framework | Jetpack Compose + Material Design 3 | BOM 2024.02.00 |
| Architecture | MVVM + Clean Architecture | - |
| Dependency Injection | Hilt | 2.51 |
| Networking | Retrofit + OkHttp + Gson | 2.9.0 / 4.x |
| Async | Kotlin Coroutines + Flow | 1.8.0 |
| Navigation | Jetpack Navigation Compose | - |
| Real-time | Socket.IO Client | 2.1.0 |
| Maps | Google Maps SDK + maps-compose | 18.2.0 / 4.3.0 |
| Push Notifications | Firebase Messaging | BOM managed |
| Local Storage | SharedPreferences (SessionManager) | - |
| Min SDK | 24 (Android 7.0) | - |
| Target SDK | 36 | - |

### 2.2 Backend

| Thanh phan | Cong nghe | Phien ban |
|------------|-----------|-----------|
| Runtime | Node.js | 18.x+ |
| Framework | Express.js | 4.21.x |
| Database | MySQL | 8.0 |
| Authentication | JWT (JSON Web Tokens) | 9.0.2 |
| Password Hashing | bcryptjs | 2.4.3 |
| Realtime | Socket.IO | 4.8.3 |
| Push Notifications | Firebase Admin SDK | - |
| Dev Server | nodemon | - |
| Email | Nodemailer | SMTP |

### 2.3 Admin Panel

| Thanh phan | Cong nghe |
|------------|-----------|
| Framework | React 18 + Vite |
| Styling | Tailwind CSS |
| Icons | Lucide React |
| Charts | Recharts |
| HTTP Client | Fetch API |
| State | React Context |

---

## 3. Cau truc du an

### 3.1 Backend (`/backend`)

```
backend/
├── src/
│   ├── index.js                  # Entry point (port 3000)
│   ├── database/
│   │   ├── db.js                # MySQL connection pool
│   │   ├── schema.sql           # 16 bang (core + AI + payment + admin)
│   │   └── seed.sql             # Du lieu test + admin accounts
│   ├── routes/
│   │   ├── auth.js              # Auth + OTP + Forgot Password
│   │   ├── users.js             # User management
│   │   ├── rides.js             # Ride operations + cancel + rate + search
│   │   ├── drivers.js           # Driver features + earnings breakdown
│   │   ├── locations.js         # Location + nearby drivers
│   │   ├── ai.js               # AI schedule + batch + recommendations
│   │   ├── chat.js             # P2P chat
│   │   ├── payments.js          # Payment CRUD + VNPay + MoMo HMAC
│   │   └── admin.js            # Admin dashboard + user/ride/driver management
│   ├── repositories/
│   │   ├── userRepository.js
│   │   ├── driverRepository.js
│   │   ├── rideRepository.js
│   │   ├── aiRepository.js
│   │   └── locationRepository.js
│   ├── services/
│   │   └── notification.js     # FCM push notifications
│   ├── socket/
│   │   └── index.js            # Socket.IO real-time (location, status, chat)
│   ├── middleware/
│   │   └── auth.js             # JWT verification + adminAuth
│   └── utils/
│       ├── geo.js              # Haversine distance calculation
│       └── price.js            # Dynamic pricing (3 vehicle types)
├── package.json
└── .env
```

### 3.2 Android App (`/app`)

```
app/src/main/java/com/laptrinhdidong/DoAn3/
├── MainActivity.kt
├── DoAn3Application.kt          # Hilt Application
├── AppConfig.kt                 # BASE_URL configuration
├── data/
│   ├── local/
│   │   └── SessionManager.kt    # SharedPreferences + JWT token
│   ├── remote/
│   │   ├── RetrofitClient.kt   # HTTP client + OkHttp interceptor
│   │   ├── SocketManager.kt    # Socket.IO client singleton
│   │   ├── ApiService.kt      # All API endpoints interface
│   │   └── dto/
│   │       ├── AuthDto.kt
│   │       ├── RideDto.kt
│   │       ├── DriverDto.kt
│   │       ├── AIDto.kt
│   │       └── PaymentDto.kt
│   └── repository/
│       ├── AuthRepository.kt
│       ├── RideRepository.kt
│       ├── DriverRepository.kt
│       ├── AIRepository.kt
│       └── PaymentRepository.kt
├── di/
│   └── AppModule.kt            # Hilt DI modules
├── ui/
│   ├── theme/
│   │   ├── Color.kt          # Dark theme colors
│   │   ├── Theme.kt          # Material3 dark theme
│   │   └── Type.kt           # Typography
│   ├── components/
│   │   ├── CommonComponents.kt # GradientButton, RatingBar, DriverCard, RideCard, etc.
│   │   └── MapComponents.kt   # TaxiMapView (Google Maps composable)
│   ├── navigation/
│   │   └── AppNavigation.kt   # Navigation graph (SplashScreen, Auth, Home, AI, etc.)
│   └── screens/
│       ├── auth/
│       │   ├── AuthScreen.kt  # Login/Register with animation
│       │   ├── ForgotPasswordScreen.kt
│       │   ├── OtpVerificationScreen.kt
│       │   └── ResetPasswordScreen.kt
│       ├── splash/
│       │   └── SplashScreen.kt # Auto-login check
│       ├── passenger/
│       │   ├── PassengerHomeScreen.kt # Booking + Map + Driver tracking
│       │   └── HistoryScreen.kt    # Ride history + search + filter
│       ├── driver/
│       │   ├── DriverHomeScreen.kt  # Online/offline + accept rides
│       │   ├── EarningsScreen.kt    # Earnings + chart + stats
│       │   └── DriverScreens.kt     # Combined driver screens
│       ├── ai/
│       │   └── AIScreens.kt         # Schedule, Recommendations, Chat
│       └── shared/
│           ├── ProfileScreen.kt      # User + Driver profile
│           └── RideDetailScreen.kt   # Ride detail + payment + rating
└── service/
    └── DoAn3FCMService.kt     # Firebase Cloud Messaging
```

### 3.3 Admin Panel (`/admin-panel`)

```
admin-panel/
├── src/
│   ├── pages/
│   │   ├── Dashboard.jsx      # Stat cards + recent rides
│   │   ├── Users.jsx          # User search + ban/unban
│   │   ├── Drivers.jsx        # Driver list + earnings
│   │   ├── Rides.jsx          # Ride management + status edit
│   │   └── Statistics.jsx      # Charts + revenue + date range
│   ├── components/
│   │   ├── Sidebar.jsx        # Navigation sidebar
│   │   └── StatCard.jsx       # Dashboard stat card
│   ├── context/
│   │   └── AuthContext.jsx   # JWT auth context
│   ├── api/
│   │   └── adminApi.js        # API calls
│   ├── App.jsx
│   └── main.jsx
├── package.json
├── vite.config.js
└── tailwind.config.js
```

---

## 4. Tinh nang

### 4.1 Xac thuc nguoi dung

| Tinh nang | Mo ta | Trang thai |
|-----------|-------|------------|
| Dang nhap / Dang ky | Phan loai passenger / driver | ✅ Hoan thanh |
| JWT token | 30 ngay expiry | ✅ Hoan thanh |
| Form validation | Real-time + chi bao do manh mat khau | ✅ Hoan thanh |
| Auto-login | Splash screen kiem tra session | ✅ Hoan thanh |
| Quen mat khau | OTP 6 chu so qua email (10 phut) | ✅ Hoan thanh |
| FCM token registration | Luu FCM token cho push notification | ✅ Hoan thanh |

### 4.2 Tinh nang khach hang

| Tinh nang | Mo ta | Trang thai |
|-----------|-------|------------|
| **Dat xe** | Chon diem don / diem den / loai xe | ✅ Hoan thanh |
| **3 loai xe** | Xe may, o to 4 cho, o to 7 cho | ✅ Hoan thanh |
| **Dynamic pricing** | Gia theo loai xe + quang duong + thoi gian | ✅ Hoan thanh |
| **Tim tai xe gan** | Haversine filter, polling 5s | ✅ Hoan thanh |
| **Theo doi realtime** | WebSocket location update | ✅ Hoan thanh |
| **Trang thai ride** | pending -> accepted -> arrived -> in_progress -> completed | ✅ Hoan thanh |
| **Google Maps** | Map SDK + markers + polyline + navigation | ✅ Hoan thanh |
| **Chat voi tai xe** | P2P chat real-time | ✅ Hoan thanh |
| **Thanh toan** | Tien mat / MoMo / VNPay (HMAC signed) | ✅ Hoan thanh |
| **Danh gia** | 1-5 sao + tags + binh luan | ✅ Hoan thanh |
| **Lich su chuyen di** | Tim kiem + loc theo trang thai / ngay | ✅ Hoan thanh |
| **Huy chuyen** | Chinh sach huy (5 phut dau mien phi) | ✅ Hoan thanh |

### 4.3 Tinh nang tai xe

| Tinh nang | Mo ta | Trang thai |
|-----------|-------|------------|
| **Online / Offline** | Toggle trang thai | ✅ Hoan thanh |
| **Nhan chuyen** | Xem yeu cau, accept / reject | ✅ Hoan thanh |
| **Cap nhat trang thai** | arrived / in_progress / completed | ✅ Hoan thanh |
| **Thu nhap** | Hom nay / Tuan / Thang / Tong | ✅ Hoan thanh |
| **Thu nhap chi tiet** | Bieu do 30 ngay + so sanh tuan truoc | ✅ Hoan thanh |
| **Driver stats** | Tong chuyen, rating TB, ti le nhan | ✅ Hoan thanh |
| **Lich su chuyen di** | Xem danh sach chuyen da hoan thanh | ✅ Hoan thanh |
| **Ho so xe** | Cap nhat thong tin xe | ✅ Hoan thanh |

### 4.4 Tinh nang AI

| Tinh nang | Mo ta | Trang thai |
|-----------|-------|------------|
| **AI Trip Scheduler** | Tao lich trinh nhieu diem dung | ✅ Hoan thanh |
| **AI Optimization** | Nhanh nhat / Re nhat / Can bang | ✅ Hoan thanh |
| **AI Route Alternatives** | Nhieu phuong an tuyen duong | ✅ Hoan thanh |
| **AI Learning Profile** | Thoi gian / chi phi / dia diem uu thich | ✅ Hoan thanh |
| **AI Recommendations** | Goi y ca nhan hoa | ✅ Hoan thanh |
| **AI Batch Offers** | Gom nhieu khach cho 1 tai xe | ✅ Hoan thanh |
| **Tro ly AI Chat** | Chat voi tro ly AI | ✅ Hoan thanh |

### 4.5 Tinh nang Admin

| Tinh nang | Mo ta | Trang thai |
|-----------|-------|------------|
| **Dashboard** | Tong quan + stat cards + recent rides | ✅ Hoan thanh |
| **User Management** | Tim kiem + loc theo role + ban/unban | ✅ Hoan thanh |
| **Driver Management** | Danh sach + earnings + online status | ✅ Hoan thanh |
| **Ride Management** | Danh sach + loc + sua trang thai | ✅ Hoan thanh |
| **Statistics** | Bieu do cot / duong + doanh thu + date range | ✅ Hoan thanh |

---

## 5. Database Schema

### 5.1 Bang nguoi dung (users)

| Cot | Kieu | Mo ta |
|-----|------|-------|
| id | INT PK | ID nguoi dung |
| email | VARCHAR(255) UNIQUE | Email dang nhap |
| password_hash | VARCHAR(255) | Mat khau da bam (bcrypt) |
| name | VARCHAR(255) | Ho ten day du |
| phone | VARCHAR(20) | So dien thoai |
| user_type | ENUM | passenger, driver, owner, consultant, hr_manager, revenue_manager, admin |
| rating | DECIMAL(3,2) DEFAULT 5.00 | Diem danh gia trung binh |
| total_rides | INT DEFAULT 0 | Tong so chuyen da di |
| is_banned | BOOLEAN DEFAULT FALSE | Bi khoa tai khoan |
| created_at | TIMESTAMP | Ngay tao |

### 5.2 Bang tai xe (drivers)

| Cot | Kieu | Mo ta |
|-----|------|-------|
| id | INT PK | ID tai xe |
| user_id | INT FK UNIQUE | Lien ket users (1:1) |
| car_model | VARCHAR(100) | Hieu xe |
| car_color | VARCHAR(50) | Mau xe |
| license_plate | VARCHAR(20) | Bien so |
| is_available | BOOLEAN DEFAULT FALSE | Trang thai online |
| latitude | DECIMAL(10,8) | Vi do hien tai |
| longitude | DECIMAL(11,8) | Kinh do hien tai |
| vehicle_type | ENUM | motorbike, car_4_seats, car_7_seats |
| created_at | TIMESTAMP | Ngay tao |

### 5.3 Bang chuyen di (rides)

| Cot | Kieu | Mo ta |
|-----|------|-------|
| id | INT PK | ID chuyen di |
| passenger_id | INT FK | ID khach hang |
| driver_id | INT FK NULL | ID tai xe (chua co -> null) |
| pickup_lat | DECIMAL(10,8) | Vi do diem don |
| pickup_lng | DECIMAL(11,8) | Kinh do diem don |
| dest_lat | DECIMAL(10,8) | Vi do diem tra |
| dest_lng | DECIMAL(11,8) | Kinh do diem tra |
| pickup_address | VARCHAR(500) | Dia chi diem don |
| dest_address | VARCHAR(500) | Dia chi diem tra |
| vehicle_type | ENUM | Loai xe da chon |
| distance_km | DECIMAL(8,2) | Quang duong (km) |
| duration_min | INT | Thoi gian uoc tinh (phut) |
| price | DECIMAL(10,0) | Gia tien (VND) |
| status | ENUM | pending, accepted, arrived, in_progress, completed, cancelled |
| driver_rating | TINYINT NULL | Danh gia cua khach (1-5) |
| passenger_rating | TINYINT NULL | Danh gia cua tai xe |
| rating_comment | VARCHAR(500) | Binh luan danh gia |
| started_at | TIMESTAMP NULL | Thoi gian bat dau |
| completed_at | TIMESTAMP NULL | Thoi gian hoan thanh |
| cancellation_reason | VARCHAR(100) NULL | Ly do huy |
| cancellation_fee | DECIMAL(10,0) NULL | Phi huy |
| payment_method | ENUM NULL | cash, wallet, vnpay, momo |
| payment_status | ENUM DEFAULT 'pending' | pending, paid, failed |
| created_at | TIMESTAMP | Ngay tao |

### 5.4 Bang vi tri tai xe (driver_locations)

| Cot | Kieu | Mo ta |
|-----|------|-------|
| id | INT PK | ID |
| driver_id | INT FK | ID tai xe |
| latitude | DECIMAL(10,8) | Vi do |
| longitude | DECIMAL(11,8) | Kinh do |
| accuracy | DECIMAL(8,2) NULL | Do chinh xac GPS |
| speed | DECIMAL(6,2) NULL | Van toc (m/s) |
| heading | INT NULL | Huong (0-360) |
| updated_at | TIMESTAMP | Lan cap nhat cuoi |

### 5.5 Bang thu nhap (transactions / earnings)

| Cot | Kieu | Mo ta |
|-----|------|-------|
| id | INT PK | ID |
| driver_id | INT FK | ID tai xe |
| ride_id | INT FK NULL | ID chuyen di |
| amount | DECIMAL(12,0) | So tien (VND) |
| type | ENUM | ride, bonus, penalty, withdrawal, cancellation |
| note | VARCHAR(255) NULL | Ghi chu |
| created_at | TIMESTAMP | Ngay tao |

### 5.6 Bang lich trinh AI (ai_trip_schedules)

| Cot | Kieu | Mo ta |
|-----|------|-------|
| id | INT PK | ID lich trinh |
| user_id | INT FK | ID nguoi dung |
| schedule_name | VARCHAR(255) | Ten lich trinh |
| scheduled_date | DATE | Ngay du kien |
| total_estimated_time | INT NULL | Tong thoi gian uoc tinh (phut) |
| total_estimated_price | DECIMAL(12,0) NULL | Tong gia uoc tinh (VND) |
| total_distance | DECIMAL(8,2) NULL | Tong quang duong (km) |
| optimization_type | ENUM | time, cost, balanced |
| ai_confidence_score | DECIMAL(3,2) NULL | Diem tu tin AI (0-1) |
| traffic_condition | VARCHAR(50) NULL | Tinh trang giao thong |
| status | ENUM | planned, in_progress, completed, cancelled |
| created_at | TIMESTAMP | Ngay tao |

### 5.7 Bang diem dung (ai_waypoints)

| Cot | Kieu | Mo ta |
|-----|------|-------|
| id | INT PK | ID |
| schedule_id | INT FK | ID lich trinh |
| stop_order | INT | Thu tu diem dung |
| stop_type | ENUM | pickup, dropoff, stopover |
| latitude | DECIMAL(10,8) | Vi do |
| longitude | DECIMAL(11,8) | Kinh do |
| address | VARCHAR(500) | Dia chi |
| stop_name | VARCHAR(255) NULL | Ten diem dung |
| estimated_arrival | TIME NULL | Thoi gian den du kien |
| duration_min | INT NULL | Thoi gian dung (phut) |
| distance_from_prev | DECIMAL(8,2) NULL | Quang duong tu diem truoc (km) |
| is_optional | BOOLEAN DEFAULT FALSE | Co the bo qua |
| priority | INT DEFAULT 0 | Do uu tien |
| estimated_price_segment | DECIMAL(12,0) NULL | Gia cho doan nay |

### 5.8 Bang tuyen thay the (ai_route_alternatives)

| Cot | Kieu | Mo ta |
|-----|------|-------|
| id | INT PK | ID |
| schedule_id | INT FK | ID lich trinh |
| route_name | VARCHAR(100) | Ten tuyen (Fastest, Cheapest, Balanced) |
| total_distance | DECIMAL(8,2) | Tong quang duong (km) |
| total_duration | INT | Tong thoi gian (phut) |
| total_price | DECIMAL(12,0) | Tong gia (VND) |
| route_description | TEXT NULL | Mo ta tuyen duong |
| is_recommended | BOOLEAN DEFAULT FALSE | Duoc AI goi y |
| traffic_scenario | VARCHAR(50) NULL | Kich ban giao thong |

### 5.9 Bang ho so hoc tap AI (ai_learning_profiles)

| Cot | Kieu | Mo ta |
|-----|------|-------|
| id | INT PK | ID |
| user_id | INT FK UNIQUE | ID nguoi dung |
| preferred_time_start | TIME NULL | Gio bat dau uu thich |
| preferred_time_end | TIME NULL | Gio ket thuc uu thich |
| average_trip_duration | DECIMAL(6,2) NULL | Thoi gian TB mot chuyen (phut) |
| average_trip_cost | DECIMAL(12,0) NULL | Chi phi TB mot chuyen (VND) |
| total_distance_travelled | DECIMAL(10,2) NULL | Tong quang duong da di (km) |
| frequent_locations | TEXT NULL | Array JSON dia diem thuong den |
| avoid_locations | TEXT NULL | Array JSON dia diem tranh |
| preference_cost_vs_time | DECIMAL(3,2) NULL | 0=chi phi, 1=thoi gian |

### 5.10 Bang chuyen gom (driver_route_batches)

| Cot | Kieu | Mo ta |
|-----|------|-------|
| id | INT PK | ID |
| driver_id | INT FK | ID tai xe |
| batch_name | VARCHAR(255) NULL | Ten batch |
| status | ENUM | proposed, accepted, rejected, completed, cancelled |
| total_revenue | DECIMAL(12,0) NULL | Tong doanh thu (VND) |
| total_distance | DECIMAL(8,2) NULL | Tong quang duong (km) |
| passenger_count | INT DEFAULT 0 | So khach |
| efficiency_score | DECIMAL(4,2) NULL | Diem hieu qua (0-100%) |
| ai_confidence | DECIMAL(3,2) NULL | Do tu tin AI (0-1) |
| accepted_at | TIMESTAMP NULL | Thoi gian chap nhan |
| completed_at | TIMESTAMP NULL | Thoi gian hoan thanh |
| created_at | TIMESTAMP | Ngay tao |

### 5.11 Bang khach hang trong batch (batch_passengers)

| Cot | Kieu | Mo ta |
|-----|------|-------|
| id | INT PK | ID |
| batch_id | INT FK | ID batch |
| passenger_id | INT FK | ID khach hang |
| original_ride_id | INT FK | ID chuyen di goc |
| pickup_order | INT NULL | Thu tu don |
| dropoff_order | INT NULL | Thu tu tra |
| pickup_lat | DECIMAL(10,8) | Vi do don |
| pickup_lng | DECIMAL(11,8) | Kinh do don |
| dropoff_lat | DECIMAL(10,8) | Vi do tra |
| dropoff_lng | DECIMAL(11,8) | Kinh do tra |
| estimated_pickup_time | TIME NULL | Thoi gian don du kien |
| detour_km | DECIMAL(6,2) NULL | Km vuot dinh muc |
| price_adjustment | DECIMAL(12,0) NULL | Dieu chinh gia |
| status | ENUM | pending, picked_up, dropped_off, cancelled |

### 5.12 Bang tin nhan chat (chat_messages)

| Cot | Kieu | Mo ta |
|-----|------|-------|
| id | INT PK | ID |
| ride_id | INT FK | ID chuyen di |
| sender_id | INT FK | ID nguoi gui |
| sender_type | ENUM | passenger, driver |
| message | TEXT | Noi dung tin nhan |
| message_type | ENUM DEFAULT 'text' | text, location, image |
| is_read | BOOLEAN DEFAULT FALSE | Da doc |
| created_at | TIMESTAMP | Thoi gian gui |

### 5.13 Bang tags danh gia (ride_rating_tags)

| Cot | Kieu | Mo ta |
|-----|------|-------|
| id | INT PK | ID |
| ride_id | INT FK | ID chuyen di |
| tag_name | VARCHAR(50) | Ten tag |
| created_at | TIMESTAMP | Thoi gian tao |

### 5.14 Bang log huy chuyen (cancellation_log)

| Cot | Kieu | Mo ta |
|-----|------|-------|
| id | INT PK | ID |
| ride_id | INT FK | ID chuyen di |
| cancelled_by | ENUM | passenger, driver |
| reason | VARCHAR(100) | Ly do huy |
| passenger_fee | DECIMAL(10,0) | Phi huy khach tra |
| driver_fee | DECIMAL(10,0) | Phi huy tai xe tra |
| created_at | TIMESTAMP | Thoi gian huy |

### 5.15 Bang hinh anh chuyen di (ride_images)

| Cot | Kieu | Mo ta |
|-----|------|-------|
| id | INT PK | ID |
| ride_id | INT FK | ID chuyen di |
| image_url | VARCHAR(500) | URL hinh anh |
| image_type | VARCHAR(20) | Loai (pickup_photo, dropoff_photo) |
| created_at | TIMESTAMP | Thoi gian tai |

### 5.16 Bang FCM tokens (user_fcm_tokens)

| Cot | Kieu | Mo ta |
|-----|------|-------|
| id | INT PK | ID |
| user_id | INT FK | ID nguoi dung |
| fcm_token | TEXT | FCM token |
| device_id | VARCHAR(255) | ID thiet bi |
| created_at | TIMESTAMP | Ngay tao |

### 5.17 Bang dat lai mat khau (password_resets)

| Cot | Kieu | Mo ta |
|-----|------|-------|
| id | INT PK | ID |
| email | VARCHAR(255) | Email nguoi dung |
| token | VARCHAR(255) | OTP token |
| expires_at | TIMESTAMP | Thoi han (10 phut) |
| used | BOOLEAN DEFAULT FALSE | Da su dung |
| created_at | TIMESTAMP | Ngay tao |

---

## 6. API Endpoints

### 6.1 Authentication

| Method | Endpoint | Mo ta |
|--------|----------|-------|
| POST | `/api/auth/register` | Dang ky tai khoan |
| POST | `/api/auth/login` | Dang nhap |
| POST | `/api/auth/fcm/register` | Dang ky FCM token |
| POST | `/api/auth/forgot-password` | Gui ma OTP qua email |
| POST | `/api/auth/verify-otp` | Xac minh ma OTP |
| POST | `/api/auth/reset-password` | Dat lai mat khau |
| POST | `/api/auth/resend-otp` | Gui lai ma OTP |

### 6.2 Users

| Method | Endpoint | Mo ta |
|--------|----------|-------|
| GET | `/api/users/me` | Thong tin nguoi dung hien tai |
| GET | `/api/users/:id` | Thong tin nguoi dung theo ID |
| GET | `/api/users/drivers/nearby` | Tim tai xe gan (query: lat, lng, radius) |

### 6.3 Rides

| Method | Endpoint | Mo ta |
|--------|----------|-------|
| POST | `/api/rides/request` | Dat xe |
| GET | `/api/rides` | Lich su chuyen di (query: status, search) |
| GET | `/api/rides/search` | Tim kiem chuyen di |
| GET | `/api/rides/active` | Chuyen di dang hoat dong |
| GET | `/api/rides/:id` | Chi tiet chuyen di |
| PUT | `/api/rides/:id/status` | Cap nhat trang thai |
| PUT | `/api/rides/:id/cancel` | Huy chuyen di |
| POST | `/api/rides/:id/rate` | Danh gia (rating, tags, comment) |

### 6.4 Driver

| Method | Endpoint | Mo ta |
|--------|----------|-------|
| GET | `/api/driver/profile` | Ho so tai xe |
| PUT | `/api/driver/profile` | Cap nhat ho so |
| PUT | `/api/driver/status` | Online / Offline |
| GET | `/api/driver/ride/available` | Chuyen di kha dung |
| POST | `/api/driver/ride/:id/accept` | Nhan chuyen |
| POST | `/api/driver/ride/:id/reject` | Tu choi chuyen |
| GET | `/api/driver/earnings` | Thu nhap chi tiet (summary + daily + week comparison) |
| GET | `/api/driver/history` | Lich su chuyen di |

### 6.5 Location

| Method | Endpoint | Mo ta |
|--------|----------|-------|
| PUT | `/api/location/update` | Cap nhat vi tri tai xe |
| GET | `/api/location/driver/:id` | Lay vi tri tai xe |
| GET | `/api/location/nearby-drivers` | Danh sach tai xe gan (query: lat, lng, radius) |

### 6.6 AI

| Method | Endpoint | Mo ta |
|--------|----------|-------|
| POST | `/api/ai/schedule/create` | Tao lich trinh AI |
| GET | `/api/ai/schedule/:id` | Chi tiet lich trinh |
| POST | `/api/ai/schedule/:id/optimize` | Toi uu lich trinh |
| GET | `/api/ai/schedule-preview` | Xem truoc lich trinh |
| GET | `/api/ai/history` | Lich su lich trinh AI |
| GET | `/api/ai/profile` | Ho so hoc tap AI |
| PUT | `/api/ai/profile` | Cap nhat ho so AI |
| GET | `/api/ai/recommendations` | Goi y ca nhan hoa |
| GET | `/api/ai/batch/available` | Batch offers kha dung |
| POST | `/api/ai/batch/:id/accept` | Chap nhan batch |
| POST | `/api/ai/batch/:id/reject` | Tu choi batch |

### 6.7 Chat

| Method | Endpoint | Mo ta |
|--------|----------|-------|
| GET | `/api/chat/:rideId/messages` | Lay tin nhan |
| POST | `/api/chat/:rideId/send` | Gui tin nhan |

### 6.8 Payment

| Method | Endpoint | Mo ta |
|--------|----------|-------|
| GET | `/api/payments/methods` | Danh sach phuong thuc thanh toan |
| POST | `/api/payments/create` | Tao payment cho ride |
| GET | `/api/payments/:id` | Chi tiet payment |
| POST | `/api/payments/:id/confirm` | Xac nhan thanh toan |
| GET | `/api/payments/history` | Lich su thanh toan |
| GET | `/api/payments/admin/all` | Tat ca payments (admin) |

### 6.9 Admin

| Method | Endpoint | Mo ta |
|--------|----------|-------|
| GET | `/api/admin/dashboard` | Tong quan he thong |
| GET | `/api/admin/users` | Danh sach nguoi dung |
| PUT | `/api/admin/users/:id/status` | Khoa / Mo tai khoan |
| GET | `/api/admin/rides` | Danh sach chuyen di |
| PUT | `/api/admin/rides/:id/status` | Sua trang thai ride |
| GET | `/api/admin/drivers` | Danh sach tai xe |
| GET | `/api/admin/stats/daily` | Thong ke theo ngay |
| GET | `/api/admin/stats/revenue` | Thong ke doanh thu |

---

## 7. WebSocket Events

### Driver -> Server

| Event | Payload | Mo ta |
|-------|---------|-------|
| `location:update` | `{ lat, lng, rideId? }` | Gui vi tri GPS realtime |
| `ride:status` | `{ rideId, status }` | Thay doi trang thai |

### Server -> Passenger

| Event | Payload | Mo ta |
|-------|---------|-------|
| `driver:location` | `{ lat, lng, rideId, timestamp }` | Vi tri tai xe realtime |
| `ride:status:changed` | `{ rideId, status, timestamp }` | Trang thai thay doi |

### Chat

| Event | Direction | Mo ta |
|-------|----------|-------|
| `chat:message` | Server -> Recipient | Tin nhan moi |

---

## 8. Cau hinh

### 8.1 Backend (.env)

```env
DB_HOST=localhost
DB_USER=root
DB_PASSWORD=1234
DB_NAME=doan3_db
DB_PORT=3306
JWT_SECRET=DoAn3_KhoaTuanAnh_2026_JWT_Secret_Key_xK9mP2vL5nQ8jR4wT7yB3cF6hJ1mN4pL9sZ2aB5dE8fG1
PORT=3000

# Firebase (cho push notifications)
FIREBASE_PROJECT_ID=your-project-id
FIREBASE_CLIENT_EMAIL=firebase-adminsdk-xxxxx@your-project.iam.gserviceaccount.com
FIREBASE_PRIVATE_KEY="-----BEGIN PRIVATE KEY-----\n...\n-----END PRIVATE KEY-----\n"

# Email SMTP (cho OTP)
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=your-email@gmail.com
SMTP_PASS=your-app-password

# Payment (VNPay)
VNPAY_TMN_CODE=YOUR_TMN_CODE
VNPAY_HASH_SECRET=YOUR_HASH_SECRET
VNPAY_RETURN_URL=http://localhost:3000/api/payments/vnpay/return

# Payment (MoMo)
MOMO_PARTNER_CODE=YOUR_PARTNER_CODE
MOMO_ACCESS_KEY=YOUR_ACCESS_KEY
MOMO_SECRET_KEY=YOUR_SECRET_KEY
MOMO_RETURN_URL=http://localhost:3000/api/payments/momo/return
```

### 8.2 Android (AppConfig.kt)

```kotlin
// Emulator (mac dinh)
const val BASE_URL = "http://10.0.2.2:3000/api/"

// May that (cung LAN)
const val BASE_URL = "http://192.168.x.x:3000/api/"
```

### 8.3 Android (local.properties)

```properties
sdk.dir=C\:/Users/LOQ/AppData/Local/Android/sdk
MAPS_API_KEY=AIzaSyB2uPnpGi9NDtk5dPIhnmMY-ZL8xoZoADo
```

---

## 9. Tai khoan test

### Khach hang

| Email | Mat khau | Mo ta |
|-------|----------|-------|
| passenger@test.com | password123 | Khach hang mau |

### Tai xe

| Email | Mat khau | Trang thai |
|-------|----------|------------|
| driver1@test.com | password123 | San sang nhan chuyen |
| driver2@test.com | password123 | San sang nhan chuyen |
| driver3@test.com | password123 | San sang nhan chuyen |

### Admin

| Email | Mat khau | Quyen |
|-------|----------|-------|
| admin@test.com | password123 | Owner - toan quyen |
| manager@test.com | password123 | Revenue Manager |

---

## 10. Huong dan cai dat

### 10.1 Backend

```bash
cd backend
npm install
node src/index.js
```

### 10.2 Database

```bash
mysql -u root -p
CREATE DATABASE doan3_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
EXIT

mysql -u root -p doan3_db < backend/src/database/schema.sql
mysql -u root -p doan3_db < backend/src/database/seed.sql
```

### 10.3 Android

```bash
cd app
.\gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

### 10.4 Admin Panel

```bash
cd admin-panel
npm install
npm run dev
# Chay tai: http://localhost:5173
```

---

## 11. Cac loi da duoc sua

### Android

| File | Loi | Fix |
|------|-----|-----|
| `di/AppModule.kt` | baseUrl hardcoded | AppConfig.BASE_URL |
| `ui/theme/Theme.kt` | Deprecated statusBarColor | Xoa dong set |
| `CommonComponents.kt` | Icons.Default ArrowBack | Icons.AutoMirrored.Filled.ArrowBack |
| `ProfileScreen.kt` | Icons.Default Help/Logout | Icons.AutoMirrored.Filled.* |
| `DriverScreens.kt` | Icons.Default TrendingUp | Icons.AutoMirrored.Filled.TrendingUp |
| `PassengerHomeScreen.kt` | Icons.Default Chat/ArrowForward | Icons.AutoMirrored.Filled.* |
| `ProfileScreen.kt` | NullPointerException driver.name | Null safety + fallback |
| `AuthViewModel` | Tham so sai thu tu register | Fix thu tu name/email/password/phone |
| `DriverDto` | name/phone nullable | Chuyen thanh String? |
| `UserDto` | name/email/phone/userType nullable | Chuyen thanh String? |
| `SessionManager` | Chua save userType | Luu userType |
| `ProfileScreen` | sessionManager trong Composable | Chuyen thanh state |

### Backend

| File | Loi | Fix |
|------|-----|-----|
| `socket/index.js` | getIO() undefined (runtime crash) | Them ioInstance global |
| `routes/locations.js` | SyntaxError dau ngoac thua ) | Xoa dau ) thua |
| `rides.js` | 2 GET / routes giong nhau | Xoa duplicate, them /active |
| `rides.js` | Cancellation policy | Them phi huy + log |
| `rides.js` | Enhanced rating with tags | Them ride_rating_tags |

### Build Results

| Kiem tra | Ket qua |
|----------|--------|
| Android Clean Build | BUILD SUCCESSFUL (43 tasks) |
| Android APK | app-debug.apk - 19.55 MB |
| Android Warnings | 0 |
| Android Lint Errors | 0 |
| Backend JS Syntax (22 files) | Tat ca OK |
| Admin Panel Build | BUILD SUCCESSFUL |

---

## 12. Lich su phat trien

### 2026-05-13 - Phien 3 (Admin + Payments + OTP)

**Admin Panel (Moi):**
- React + Vite + Tailwind CSS
- Dashboard, Users, Drivers, Rides, Statistics
- JWT auth voi admin role check

**Payment:**
- VNPay HMAC-SHA256 signing
- MoMo HMAC-SHA256 signing
- Sandbox auto-fallback khi chua co API keys
- Payment history CRUD

**OTP / Quen mat khau:**
- 4 endpoints: forgot-password, verify-otp, reset-password, resend-otp
- Nodemailer SMTP integration
- 6 chu so OTP, 10 phut het han

**Database moi:**
- password_resets, transactions, ride_rating_tags, cancellation_log

### 2026-05-10 - Phien 2 (Maps + Realtime + Chat)

**Google Maps SDK:**
- TaxiMapView voi markers + polyline
- Google Maps navigation intent
- Maps SDK + maps-compose dependencies

**WebSocket Realtime:**
- Socket.IO client + server
- Driver location tracking 3s interval
- Ride status change notifications

**P2P Chat:**
- Chat screen giua khach va tai xe
- Socket.IO real-time tin nhan
- Chat API endpoints

**AI Enhancements:**
- AI Chat Assistant screen
- Batch detail screen
- Enhanced earnings chart

### 2026-05-09 - Phien 1 (Core Features)

**Backend:**
- Auth, Users, Rides, Drivers, Locations, AI routes
- 17 bang database (core + AI)
- JWT authentication, bcrypt password
- Seed data voi tai khoan test

**Android:**
- Jetpack Compose UI
- Hilt DI
- Retrofit + OkHttp
- All core screens (Auth, Passenger, Driver, AI)
- MVVM architecture

---

*Tai lieu nay duoc cap nhat tu dong: 2026-05-13*
