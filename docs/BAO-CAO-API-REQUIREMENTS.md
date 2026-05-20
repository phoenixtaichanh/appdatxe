# Bao Cao API Requirements

**Du an:** Xay dung he thong dat xe thong minh ket hop tro ly du lich AI
**Ngay:** 2026-05-13
**Trang thai:** Hoan thanh

---

## Muc luc

1. [Tong quan he thong](#1-tong-quan-he-thong)
2. [Google API Requirements](#2-google-api-requirements)
3. [Firebase API Requirements](#3-firebase-api-requirements)
4. [Backend REST API Requirements](#4-backend-rest-api-requirements)
5. [Admin Panel API Requirements](#5-admin-panel-api-requirements)
6. [WebSocket / Realtime API Requirements](#6-websocket--realtime-api-requirements)
7. [Third-party API Requirements khac](#7-third-party-api-requirements-khac)
8. [Danh sach Environment Variables](#8-danh-sach-environment-variables)
9. [Payment Integration Details](#9-payment-integration-details)
10. [Security Requirements](#10-security-requirements)
11. [Checklist trien khai API](#11-checklist-trien-khai-api)

---

## 1. Tong quan he thong

Du an bao gom 3 thanh phan chinh:

| Thanh phan | Cong nghe | Vai tro |
|------------|-----------|---------|
| **Android App** | Kotlin + Jetpack Compose | Giao dien nguoi dung, goi API |
| **Backend API** | Node.js + Express + MySQL | Xu ly logic, co so du lieu, realtime |
| **Admin Panel** | React + Vite + Tailwind | Quan ly he thong (web) |

**Kien truc ung dung:** Clean Architecture + MVVM

```
Android App ──HTTP/REST──> Backend API ──MySQL──> Database
Android App ──WebSocket──> Socket.IO Server
Android App ──Maps SDK──> Google Maps
Android App <──FCM──       Firebase Cloud Messaging
Backend     <──FCM SDK──   Firebase Admin SDK
Admin Panel ──HTTP/REST──> Backend API
```

---

## 2. Google API Requirements

### 2.1. Google Maps SDK (Android)

Google Maps la API bat buoc, duoc su dung cho toan bo tinh nang ban do va dinh vi.

#### Thong tin cau hinh hien tai

| Truong | Gia tri |
|--------|---------|
| API Key | `AIzaSyB2uPnpGi9NDtk5dPIhnmMY-ZL8xoZoADo` |
| Noi cau hinh | `local.properties` dong `MAPS_API_KEY` |
| Console | [Google Cloud Console](https://console.cloud.google.com/google/maps-apis) |

#### API Endpoints bat buoc bat tren Google Cloud

| API | Muc dich su dung trong du an |
|-----|------------------------------|
| **Maps SDK for Android** | Hien thi ban do, vi tri tai xe, diem don/tra |
| **Directions API** | Tinh toan lo trinh, thoi gian, khoang cach |
| **Distance Matrix API** | Tinh gia cuoc dua tren quang duong |
| **Geocoding API** | Chuyen doi toa do <-> dia chi |
| **Places API** | Tim kiem dia diem, autocomplete dia chi |

#### Cach bat API tren Google Cloud Console

1. Truy cap [Google Cloud Console](https://console.cloud.google.com/)
2. Chon project tuong ung
3. Vao **APIs & Services > Library**
4. Tim va bat lan luot cac API tren
5. Vao **APIs & Services > Credentials**
6. Tao **API Key** moi hoac gioi han cho cac API da bat

#### Rang buoc API Key (khuyen nghi)

```
HTTP referrer restrictions:
  - localhost/* (dev only)

API restrictions:
  ✓ Maps SDK for Android
  ✓ Directions API
  ✓ Distance Matrix API
  ✓ Geocoding API
  ✓ Places API
```

#### Tinh nang su dung Maps trong code

| Tinh nang | File | Mo ta |
|-----------|------|-------|
| Hien thi ban do | `ui/components/MapComponents.kt` | TaxiMapView composable |
| Marker tai xe | `ui/components/MapComponents.kt` | Hien thi vi tri realtime |
| Polyline lo trinh | `ui/components/MapComponents.kt` | Ve duong di |
| Camera di chuyen | `ui/components/MapComponents.kt` | Theo doi tai xe |
| Dieu huong Intent | `ui/screens/*` | Mo Google Maps chi danh duong |

#### Chi phi uoc tinh (thang)

| API | Mien phi/thang | Vuot qua |
|-----|---------------|----------|
| Maps SDK for Android | Khong gioi han khi da bat | Khong |
| Directions API | 40,000 requests | $5/1,000 |
| Distance Matrix API | 1,000 elements | $5/1,000 |
| Geocoding API | 40,000 requests | $5/1,000 |
| Places API | 150,000 requests | variable |

---

## 3. Firebase API Requirements

### 3.1. Firebase Cloud Messaging (FCM)

FCM duoc su dung cho **push notifications** tu backend gui den Android app.

#### Thong tin cau hinh

| Truong | Noi cau hinh |
|--------|-------------|
| `FIREBASE_PROJECT_ID` | `backend/.env` |
| `FIREBASE_CLIENT_EMAIL` | `backend/.env` |
| `FIREBASE_PRIVATE_KEY` | `backend/.env` |
| `google-services.json` | `app/google-services.json` (Android side) |

#### Cach lay Firebase credentials

1. Truy cap [Firebase Console](https://console.firebase.google.com/)
2. Tao project moi hoac chon project hien co
3. Vao **Project Settings > Service accounts**
4. Click **Generate new private key**
5. Luu file JSON va dien vao `backend/.env`

#### Cach lay google-services.json (Android)

1. Firebase Console > **Project Settings**
2. Tab **General** > **Your apps** > Them Android app
3. Nhap package name: `com.laptrinhdidong.DoAn3`
4. Download `google-services.json`
5. Dat vao `app/google-services.json`

#### Tinh nang su dung FCM

| Su kien | Huong | Mo ta |
|---------|-------|-------|
| Tai xe nhan chuyen | Backend -> Passenger | Thong bao co tai xe nhan chuyen |
| Tai xe den noi | Backend -> Passenger | Thong bao tai xe da den |
| Chuyen di hoan thanh | Backend -> Passenger | Thong bao ket thuc chuyen |
| Yeu cau dat xe | Backend -> Driver | Thong bao co khach dat xe gan do |
| Tin nhan chat moi | Backend -> User | Thong bao tin nhan moi |

#### File su dung FCM

| Vai tro | File |
|--------|------|
| Android FCM Service | `app/src/main/java/com/laptrinhdidong/DoAn3/service/DoAn3FCMService.kt` |
| Backend FCM Service | `backend/src/services/notification.js` |
| FCM Token Registration | `ApiService.kt` -> `POST /api/auth/fcm/register` |

### 3.2. Firebase Realtime Database (tuy chon mo rong)

Hien tai du an dung Socket.IO cho realtime, nhung co the mo rong them Firebase Realtime Database cho:
- Luu tru vi tri tai xe offline
- Backup trang thai chuyen di

---

## 4. Backend REST API Requirements

### 4.1. Base URL

| Moi truong | URL |
|-----------|-----|
| Development | `http://localhost:3000/api` |
| Production | `https://api.doan3.vn/api` |

### 4.2. Authentication

Tat ca API (tru `/auth/*` va `/auth/forgot-password`, `/auth/verify-otp`, `/auth/reset-password`) yeu cau JWT token trong header:

```
Authorization: Bearer <jwt_token>
```

JWT Payload:
```json
{
    "userId": 1,
    "email": "user@test.com",
    "userType": "passenger | driver",
    "iat": 1704067200,
    "exp": 1704153600
}
```

### 4.3. Danh sach API Endpoints day du

#### 4.3.1. Auth APIs

| Method | Endpoint | Mo ta | Auth |
|--------|----------|-------|------|
| `POST` | `/api/auth/register` | Dang ky tai khoan | Khong |
| `POST` | `/api/auth/login` | Dang nhap | Khong |
| `POST` | `/api/auth/fcm/register` | Dang ky FCM token | Co |
| `POST` | `/api/auth/forgot-password` | Gui ma OTP qua email (6 chu so, 10 phut) | Khong |
| `POST` | `/api/auth/verify-otp` | Xac minh ma OTP | Khong |
| `POST` | `/api/auth/reset-password` | Dat lai mat khau | Khong |
| `POST` | `/api/auth/resend-otp` | Gui lai ma OTP | Khong |

**Forgot Password Flow:**
```
1. POST /api/auth/forgot-password { email }
   -> Gui email voi ma 6 chu so, luu vao bang password_resets
2. POST /api/auth/verify-otp { email, otp }
   -> Kiem tra OTP con han, tra ve success
3. POST /api/auth/reset-password { email, otp, newPassword }
   -> Dat mat khau moi, danh dau OTP da su dung
```

#### 4.3.2. User APIs

| Method | Endpoint | Mo ta | Auth |
|--------|----------|-------|------|
| `GET` | `/api/users/me` | Lay thong tin user hien tai | Co |
| `GET` | `/api/users/:id` | Lay thong tin user theo ID | Co |
| `GET` | `/api/users/drivers/nearby` | Tim tai xe gan do (query: lat, lng, radius) | Co |

#### 4.3.3. Ride APIs

| Method | Endpoint | Mo ta | Auth |
|--------|----------|-------|------|
| `POST` | `/api/rides/request` | Tao yeu cau dat xe | Passenger |
| `GET` | `/api/rides` | Lay lich su chuyen di (query: status, search) | Co |
| `GET` | `/api/rides/search` | Tim kiem chuyen di | Co |
| `GET` | `/api/rides/active` | Lay chuyen di dang hoat dong | Co |
| `GET` | `/api/rides/:id` | Lay chi tiet chuyen di | Co |
| `PUT` | `/api/rides/:id/status` | Cap nhat trang thai | Passenger/Driver |
| `PUT` | `/api/rides/:id/cancel` | Huy chuyen di (voi ly do) | Co |
| `POST` | `/api/rides/:id/rate` | Danh gia chuyen di (rating, tags, comment) | Co |

**Cancellation Rules:**
- Trong 5 phut dau: mien phi
- Sau 5 phut: khach tra 10%, tai xe tra 20%
- Ly do huy: driver_not_responding, change_of_plans, emergency, driver_too_far, wrong_address, passenger_cancelled, duplicate_booking, other

#### 4.3.4. Driver APIs

| Method | Endpoint | Mo ta | Auth |
|--------|----------|-------|------|
| `GET` | `/api/driver/profile` | Lay ho so tai xe | Driver |
| `PUT` | `/api/driver/profile` | Cap nhat ho so (xe) | Driver |
| `PUT` | `/api/driver/status` | Cap nhat online/offline | Driver |
| `GET` | `/api/driver/ride/available` | Lay chuyen kha dung | Driver |
| `POST` | `/api/driver/ride/:id/accept` | Nhan chuyen | Driver |
| `POST` | `/api/driver/ride/:id/reject` | Tu choi chuyen | Driver |
| `GET` | `/api/driver/earnings` | Lay thu nhap (today/week/month/total + daily breakdown + week comparison) | Driver |
| `GET` | `/api/driver/history` | Lich su chuyen di | Driver |

#### 4.3.5. Location APIs

| Method | Endpoint | Mo ta | Auth |
|--------|----------|-------|------|
| `PUT` | `/api/location/update` | Cap nhat vi tri tai xe | Driver |
| `GET` | `/api/location/driver/:id` | Lay vi tri tai xe | Co |
| `GET` | `/api/location/nearby-drivers` | Tim tai xe gan (query: lat, lng, radius) | Co |

#### 4.3.6. AI Schedule APIs

| Method | Endpoint | Mo ta | Auth |
|--------|----------|-------|------|
| `POST` | `/api/ai/schedule/create` | Tao lich trinh AI | Co |
| `GET` | `/api/ai/schedule/:id` | Lay chi tiet lich trinh | Co |
| `PUT` | `/api/ai/schedule/:id` | Cap nhat lich trinh | Co |
| `POST` | `/api/ai/schedule/:id/optimize` | Toi uu lich trinh (time/cost/balanced) | Co |
| `GET` | `/api/ai/schedule-preview` | Xem truoc lich trinh | Co |
| `GET` | `/api/ai/history` | Lich su lich trinh AI | Co |

#### 4.3.7. AI Profile APIs

| Method | Endpoint | Mo ta | Auth |
|--------|----------|-------|------|
| `GET` | `/api/ai/profile` | Lay ho so hoc tap AI | Co |
| `PUT` | `/api/ai/profile` | Cap nhat ho so AI | Co |
| `GET` | `/api/ai/recommendations` | Lay goi y ca nhan hoa | Co |

#### 4.3.8. AI Batch APIs

| Method | Endpoint | Mo ta | Auth |
|--------|----------|-------|------|
| `GET` | `/api/ai/batch/available` | Lay batch kha dung | Driver |
| `POST` | `/api/ai/batch/:id/accept` | Chap nhan batch | Driver |
| `POST` | `/api/ai/batch/:id/reject` | Tu choi batch | Driver |

#### 4.3.9. Chat APIs

| Method | Endpoint | Mo ta | Auth |
|--------|----------|-------|------|
| `GET` | `/api/chat/:rideId/messages` | Lay tin nhan | Co |
| `POST` | `/api/chat/:rideId/send` | Gui tin nhan | Co |

#### 4.3.10. Payment APIs

| Method | Endpoint | Mo ta | Auth |
|--------|----------|-------|------|
| `GET` | `/api/payments/methods` | Danh sach phuong thuc thanh toan | Co |
| `POST` | `/api/payments/create` | Tao payment cho ride | Co |
| `GET` | `/api/payments/:id` | Chi tiet payment | Co |
| `POST` | `/api/payments/:id/confirm` | Xac nhan thanh toan (callback) | Khong |
| `POST` | `/api/payments/vnpay/return` | VNPay return URL | Khong |
| `POST` | `/api/payments/momo/return` | MoMo return URL | Khong |
| `GET` | `/api/payments/history` | Lich su thanh toan | Co |
| `GET` | `/api/payments/admin/all` | Tat ca payments (admin) | Admin |

**Payment Methods:**
| Method ID | Display Name | Status |
|----------|-------------|--------|
| `cash` | Tien mat | Active |
| `wallet` | Vi dien tu | Active |
| `vnpay` | VNPay | Active (HMAC signed) |
| `momo` | MoMo | Active (HMAC signed) |

### 4.4. Response Structure

#### Thanh cong
```json
{
    "success": true,
    "data": { ... },
    "message": "Operation successful"
}
```

#### Loi
```json
{
    "success": false,
    "error": {
        "code": "AUTH001",
        "message": "Invalid credentials"
    }
}
```

### 4.5. Error Codes

| Ma | HTTP | Mo ta |
|----|------|-------|
| `AUTH001` | 401 | Email hoac mat khau khong dung |
| `AUTH002` | 401 | Token khong hop le hoac da het han |
| `AUTH003` | 403 | Khong co quyen truy cap |
| `AUTH004` | 409 | Email da ton tai |
| `AUTH005` | 400 | OTP khong dung hoac da het han |
| `AUTH006` | 400 | OTP da duoc su dung |
| `RIDE001` | 404 | Chuyen di khong tim thay |
| `RIDE002` | 400 | Trang thai khong hop le |
| `RIDE003` | 400 | Khong the huy chuyen dang thuc hien |
| `RIDE004` | 409 | Tai xe dang ban chuyen khac |
| `RIDE005` | 404 | Khong tim thay tai xe gan do |
| `PAY001` | 400 | Payment method khong hop le |
| `PAY002` | 400 | Payment signature khong hop le |
| `AI001` | 400 | Lich trinh can it nhat 2 diem dung |
| `AI002` | 400 | Loai toi uu khong hop le |
| `AI003` | 404 | Lich trinh AI khong tim thay |
| `GEN001` | 500 | Loi server noi bo |
| `GEN002` | 503 | Dich vu tam thoi khong kha dung |
| `GEN003` | 400 | Du lieu dau vao khong hop le |

---

## 5. Admin Panel API Requirements

### 5.1. Admin Authentication

Admin Panel su dung cung JWT token voi Backend. JWT payload chua them truong `role`:

```json
{
    "userId": 1,
    "email": "admin@test.com",
    "userType": "admin",
    "role": "owner | revenue_manager",
    "iat": 1704067200,
    "exp": 1704153600
}
```

Middleware `adminAuth` kiem tra:
1. Token ton tai va hop le
2. `user.user_type === 'admin'`

### 5.2. Admin Endpoints

| Method | Endpoint | Mo ta | Role |
|--------|----------|-------|------|
| `GET` | `/api/admin/dashboard` | Tong quan he thong | owner, revenue_manager |
| `GET` | `/api/admin/users` | Danh sach nguoi dung (query: search, role) | owner |
| `PUT` | `/api/admin/users/:id/status` | Khoa / Mo tai khoan (body: is_banned) | owner |
| `GET` | `/api/admin/rides` | Danh sach chuyen di (query: status, search) | owner, revenue_manager |
| `PUT` | `/api/admin/rides/:id/status` | Sua trang thai ride | owner |
| `GET` | `/api/admin/drivers` | Danh sach tai xe (query: search, online) | owner |
| `GET` | `/api/admin/stats/daily` | Thong ke theo ngay (query: start_date, end_date) | owner, revenue_manager |
| `GET` | `/api/admin/stats/revenue` | Thong ke doanh thu (query: start_date, end_date) | owner, revenue_manager |

### 5.3. Admin Dashboard Response

```json
{
  "success": true,
  "data": {
    "stats": {
      "totalUsers": 50,
      "totalDrivers": 15,
      "totalRides": 200,
      "todayRides": 12,
      "totalRevenue": 15000000,
      "todayRevenue": 500000
    },
    "recentRides": [
      {
        "id": 1,
        "passengerName": "Nguyen Van A",
        "driverName": "Tran Van B",
        "pickupAddress": "123 Le Lai",
        "dropoffAddress": "456 Nguyen Hue",
        "price": 65000,
        "status": "completed",
        "createdAt": "2026-05-13T10:00:00Z"
      }
    ]
  }
}
```

### 5.4. Admin Statistics Response

```json
{
  "success": true,
  "data": {
    "dailyStats": [
      { "date": "2026-05-01", "rides": 10, "revenue": 500000 },
      { "date": "2026-05-02", "rides": 15, "revenue": 750000 }
    ],
    "summary": {
      "totalRides": 200,
      "totalRevenue": 15000000,
      "avgPerDay": 500000,
      "comparisonLastPeriod": 15.5
    }
  }
}
```

---

## 6. WebSocket / Realtime API Requirements

Du an su dung **Socket.IO** cho giao tiep realtime.

### 6.1. Ket noi

| Truong | Gia tri |
|--------|---------|
| URL | Socket URL trong `AppConfig.kt` (cung BASE_URL) |
| Path | `/socket.io/` |
| Auth | JWT token trong `socket.handshake.auth.token` |
| Transport | WebSocket (fallback: polling) |
| CORS | `origin: *` |

### 6.2. Socket Events

#### 6.2.1. Driver -> Server

| Event | Payload | Mo ta |
|-------|---------|-------|
| `location:update` | `{ lat: number, lng: number, rideId?: number }` | Gui vi tri GPS realtime |
| `ride:status` | `{ rideId: number, status: string }` | Thay doi trang thai chuyen |

#### 6.2.2. Server -> Passenger

| Event | Payload | Mo ta |
|-------|---------|-------|
| `driver:location` | `{ lat: number, lng: number, rideId: number, timestamp: number }` | Vi tri tai xe realtime |
| `ride:status:changed` | `{ rideId: number, status: string, timestamp: number }` | Trang thai chuyen thay doi |

#### 6.2.3. Passenger -> Server

| Event | Payload | Mo ta |
|-------|---------|-------|
| `join:ride` | `rideId: number` | Tham gia phong chuyen di |
| `leave:ride` | `rideId: number` | Roi phong chuyen di |

#### 6.2.4. Chat

| Event | Direction | Mo ta |
|-------|-----------|-------|
| `chat:message` | Server -> Recipient | Tin nhan moi |

### 6.3. Socket Rooms

| Room | Thanh vien | Mo ta |
|------|-----------|-------|
| `user_{userId}` | User cu the | Nhan thong bao ca nhan |
| `ride_{rideId}` | Passenger + Driver | Phong chuyen di |
| `drivers` | Tat ca tai xe | Broadcast den tai xe |

### 6.4. File lien quan

| File | Mo ta |
|------|-------|
| `app/data/remote/SocketManager.kt` | Socket.IO client (Android) |
| `backend/src/socket/index.js` | Socket.IO server (Node.js) |

---

## 7. Third-party API Requirements khac

### 7.1. MySQL Database

| Truong | Gia tri |
|--------|---------|
| Phien ban | MySQL 8.0 |
| Host | `localhost` |
| Port | `3306` |
| Database | `doan3_db` |
| User | `root` |
| Password | `1234` |

Database schema gom **17 bang**: users, drivers, rides, driver_locations, transactions, ai_trip_schedules, ai_waypoints, ai_route_alternatives, ai_learning_profiles, driver_route_batches, batch_passengers, chat_messages, ride_rating_tags, cancellation_log, ride_images, user_fcm_tokens, password_resets.

### 7.2. JWT Secret

```
JWT_SECRET=DoAn3_KhoaTuanAnh_2026_JWT_Secret_Key_xK9mP2vL5nQ8jR4wT7yB3cF6hJ1mN4pL9sZ2aB5dE8fG1
```

### 7.3. VNPay Integration

| Truong | Gia tri |
|--------|---------|
| TMN Code | `VNPAY_TMN_CODE` (tu .env) |
| Hash Secret | `VNPAY_HASH_SECRET` (tu .env) |
| Return URL | `http://localhost:3000/api/payments/vnpay/return` |
| Algorithm | HMAC-SHA256 |
| Sandbox | Tu dong fallback khi chua cau hinh |

**VNPay Request Fields:**
```json
{
  "vnp_Version": "2.1.0",
  "vnp_Command": "pay",
  "vnp_TmnCode": "<TMN_CODE>",
  "vnp_Amount": 6600000,
  "vnp_CurrCode": "VND",
  "vnp_TxnRef": "RIDE_123",
  "vnp_OrderInfo": "Thanh toan chuyen di #123",
  "vnp_ReturnUrl": "http://localhost:3000/api/payments/vnpay/return",
  "vnp_CreateDate": "20260513103000",
  "vnp_IpAddr": "127.0.0.1",
  "vnp_Locale": "vn",
  "vnp_OrderType": "other"
}
```

### 7.4. MoMo Integration

| Truong | Gia tri |
|--------|---------|
| Partner Code | `MOMO_PARTNER_CODE` (tu .env) |
| Access Key | `MOMO_ACCESS_KEY` (tu .env) |
| Secret Key | `MOMO_SECRET_KEY` (tu .env) |
| Return URL | `http://localhost:3000/api/payments/momo/return` |
| Algorithm | HMAC-SHA256 |
| Sandbox | Tu dong fallback khi chua cau hinh |

### 7.5. Email SMTP (Nodemailer)

| Truong | Gia tri |
|--------|---------|
| Host | `SMTP_HOST` (tu .env, mac dinh smtp.gmail.com) |
| Port | `SMTP_PORT` (tu .env, mac dinh 587) |
| User | `SMTP_USER` (tu .env) |
| Pass | `SMTP_PASS` (tu .env, app password) |
| Su dung cho | Gui ma OTP khi quen mat khau |

### 7.6. Cac thu vien ben thu ba (Backend)

| Thu vien | Phien ban | Muc dich |
|----------|-----------|---------|
| express | 4.21.x | Web framework |
| socket.io | 4.8.x | Realtime communication |
| mysql2 | 3.12.x | MySQL driver voi Promise |
| jsonwebtoken | 9.0.x | JWT authentication |
| bcryptjs | 2.4.x | Password hashing |
| cors | 2.8.x | Cross-origin resource sharing |
| dotenv | 16.4.x | Environment variables |
| firebase-admin | latest | FCM server-side |
| nodemailer | 6.9.x | Email sending |
| crypto | built-in | HMAC-SHA256 signing |

### 7.7. Cac thu vien ben thu ba (Android)

| Thu vien | Phien ban | Muc dich |
|----------|-----------|---------|
| Retrofit | 2.9.0 | REST API client |
| OkHttp | 4.x | HTTP client |
| Socket.IO Client | 2.1.0 | Realtime client |
| Hilt | 2.51 | Dependency injection |
| Google Maps | maps + maps-compose | Ban do |
| Firebase Messaging | BOM managed | Push notifications |
| Kotlin Coroutines | 1.8.0 | Async programming |
| Compose BOM | 2024.02.00 | Jetpack Compose |

---

## 8. Danh sach Environment Variables

### 8.1. Backend (backend/.env)

```env
# Database
DB_HOST=localhost
DB_USER=root
DB_PASSWORD=1234
DB_NAME=doan3_db
DB_PORT=3306

# JWT
JWT_SECRET=DoAn3_KhoaTuanAnh_2026_JWT_Secret_Key_xK9mP2vL5nQ8jR4wT7yB3cF6hJ1mN4pL9sZ2aB5dE8fG1

# Server
PORT=3000

# Firebase (bat buoc cho push notifications)
FIREBASE_PROJECT_ID=your-project-id
FIREBASE_CLIENT_EMAIL=firebase-adminsdk-xxxxx@your-project.iam.gserviceaccount.com
FIREBASE_PRIVATE_KEY="-----BEGIN PRIVATE KEY-----\n...\n-----END PRIVATE KEY-----\n"

# Email SMTP (cho OTP)
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=your-email@gmail.com
SMTP_PASS=your-app-password

# Payment - VNPay
VNPAY_TMN_CODE=YOUR_TMN_CODE
VNPAY_HASH_SECRET=YOUR_HASH_SECRET
VNPAY_RETURN_URL=http://localhost:3000/api/payments/vnpay/return

# Payment - MoMo
MOMO_PARTNER_CODE=YOUR_PARTNER_CODE
MOMO_ACCESS_KEY=YOUR_ACCESS_KEY
MOMO_SECRET_KEY=YOUR_SECRET_KEY
MOMO_RETURN_URL=http://localhost:3000/api/payments/momo/return
```

### 8.2. Android (local.properties)

```properties
sdk.dir=C\:/Users/LOQ/AppData/Local/Android/sdk
MAPS_API_KEY=AIzaSyB2uPnpGi9NDtk5dPIhnmMY-ZL8xoZoADo
```

### 8.3. Android (app/google-services.json)

Download tu Firebase Console, dat tai `app/google-services.json`.

---

## 9. Payment Integration Details

### 9.1. Payment Flow

```
1. Passenger chon phuong thuc thanh toan (cash/vnpay/momo)
2. Neu cash -> Cap nhat ride.payment_status = 'paid'
3. Neu VNPay/MoMo:
   a. Backend tao payment record
   b. Backend sign HMAC-SHA256
   c. Backend tra ve payment URL
   d. Android mo browser voi URL
   e. User thanh toan tren trinh duyet
   f. Payment gateway redirect ve return URL
   g. Backend xac nhan, cap nhat payment_status
4. Ride completion -> Tao transaction record
```

### 9.2. Dynamic Pricing

| Loai xe | Gia cuoc (VND) | Phi/km | Phi/phut |
|---------|----------------|--------|----------|
| motorbike | 10.000 | 3.000 | 100 |
| car_4_seats | 12.000 | 5.000 | 200 |
| car_7_seats | 15.000 | 7.000 | 300 |

**Cong thuc:**
```
Gia = Gia cuoc + (Khoang cach km x Phi/km) + (Thoi gian phut x Phi/phut)
```

### 9.3. Cancellation Fees

| Thoi gian | Phi khach tra | Phi tai xe tra |
|-----------|---------------|----------------|
| Trong 5 phut dau | 0% | 0% |
| Sau 5 phut | 10% cua gia | 20% cua gia |

---

## 10. Security Requirements

### 10.1. API Security

| Yeu cau | Mo ta |
|---------|-------|
| HTTPS | Bat buoc tren production |
| JWT Token | Expiry 30 ngay, luu trong SharedPreferences |
| Authorization Headers | Bearer token cho tat ca protected routes |
| Role-based Access | Passenger, Driver, Admin phan biet qua middleware |
| Input Validation | Server-side validation tren tat ca inputs |
| Rate Limiting | (Tuy chon) Gioi han request/s |

### 10.2. API Key Security

| API Key | Rang buoc |
|---------|----------|
| Google Maps API Key | HTTP referrer + API restrictions |
| Firebase Private Key | Khong commit vao git, chi trong .env |
| VNPay Hash Secret | Khong commit vao git, chi trong .env |
| MoMo Secret Key | Khong commit vao git, chi trong .env |

### 10.3. CORS Configuration

```
Backend CORS:
  origin: * (development)
  origin: https://doan3.vn (production - khuyen nghi whitelist)
```

---

## 11. Checklist trien khai API

### Bat buoc

- [x] Bat **Google Maps SDK for Android** tren Cloud Console
- [x] Bat **Directions API** tren Cloud Console
- [x] Bat **Distance Matrix API** tren Cloud Console
- [x] Bat **Geocoding API** tren Cloud Console
- [x] Bat **Places API** tren Cloud Console
- [x] Cau hinh **API Key restrictions** (HTTP referrer)
- [x] Tao **Firebase project**, generate **Service Account key**
- [x] Dien Firebase credentials vao `backend/.env`
- [x] Download `google-services.json`, dat vao `app/`
- [x] Cau hinh **MySQL 8.0**, chay schema
- [x] Tao `.env` cho backend voi `JWT_SECRET`
- [x] Cau hinh **VNPay** HMAC credentials (hoac dung sandbox)
- [x] Cau hinh **MoMo** HMAC credentials (hoac dung sandbox)
- [x] Cau hinh **SMTP** cho email OTP

### Khuyen nghi

- [ ] Cau hinh rate limiting tren Express
- [ ] Them API key rotation mechanism
- [ ] Cau hinh CORS whitelist thay vi `*`
- [ ] Them request logging va monitoring
- [ ] Setup Firebase Analytics cho usage tracking
- [ ] Backup database thuong xuyen

---

## Tai lieu tham khao

- [Google Maps Platform Documentation](https://developers.google.com/maps/documentation)
- [Firebase Cloud Messaging Documentation](https://firebase.google.com/docs/cloud-messaging)
- [Socket.IO Documentation](https://socket.io/docs/v4/)
- [Retrofit Documentation](https://square.github.io/retrofit/)
- [Express.js API Reference](https://expressjs.com/en/api.html)
- [VNPay Integration Guide](https://sandbox.vnpayment.vn/apis/docs/thanh-toan-pay/pay.html)
- [MoMo Payment Integration](https://developers.momo.vn/)
- [Nodemailer Documentation](https://nodemailer.com/usage/)

---

*Tai lieu cap nhat: 2026-05-13*
