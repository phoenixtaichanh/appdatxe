# Huong Dan Su Dung - DoAn3 Taxi App

> **Phien ban:** 1.3.0

> **De tai:** Xay dung he thong dat xe thong minh ket hop tro ly du lich AI

> **Cong nghe:** Kotlin (Android) + Node.js (Backend) + MySQL + AI

> **Cap nhat moi nhat:**
> - [2026-05-13] Full Admin Panel (React + Vite + Tailwind CSS)
> - [2026-05-13] OTP / Quen mat khau (Backend: 4 endpoints Nodemailer, Android: 3 screens)
> - [2026-05-13] Real Payment Integration (VNPay + MoMo HMAC-SHA256)
> - [2026-05-13] Enhanced rating with tags + cancellation policy
> - [2026-05-13] Driver earnings breakdown (daily chart, week comparison)
> - [2026-05-13] Payment history + Payment CRUD APIs
> - [2026-05-13] BUILD SUCCESSFUL - 0 warnings, 0 errors
> - [2026-05-10] Them 3 loai xe (xe may, o to 4 cho, o to 7 cho)
> - [2026-05-10] Dynamic pricing theo loai xe
> - [2026-05-10] Polling tai xe gan (5 giay)
> - [2026-05-10] Google Maps SDK integration
> - [2026-05-10] TaxiMapView voi markers va polyline
> - [2026-05-10] WebSocket real-time tracking (Socket.IO)
> - [2026-05-10] Push Notification (FCM)
> - [2026-05-10] P2P Chat giua khach va tai xe
> - [2026-05-10] Navigation button (Google Maps navigation)
> - [2026-05-10] Earnings chart va driver stats
> - [2026-05-10] AI Chat Assistant screen
> - [2026-05-10] Payment method selector va enhanced rating
> - [2026-05-10] Fix AppModule baseUrl hardcoded -> AppConfig.BASE_URL
> - [2026-05-10] Fix deprecated statusBarColor trong Theme.kt
> - [2026-05-10] Fix 6 deprecated Icons.Default -> Icons.AutoMirrored.Filled
> - [2026-05-10] Fix socket/index.js getIO() runtime crash (ioInstance reference)
> - [2026-05-10] Fix locations.js syntax error (extra closing paren)

---

## Muc luc

1. [Cai dat](#1-cai-dat)
2. [Khoi chay Backend](#2-khoi-chay-backend)
3. [Khoi chay Android App](#3-khoi-chay-android-app)
4. [Khoi chay Admin Panel](#4-khoi-chay-admin-panel)
5. [Tai khoan Test](#5-tai-khoan-test)
6. [Bang gia dich vu](#6-bang-gia-dich-vu)
7. [Huong dan su dung - Khach hang](#7-huong-dan-su-dung---khach-hang)
8. [Huong dan su dung - Tai xe](#8-huong-dan-su-dung---tai-xe)
9. [Huong dan su dung - AI](#9-huong-dan-su-dung---ai)
10. [Huong dan su dung - Admin](#10-huong-dan-su-dung---admin)
11. [Cau truc Project](#11-cau-truc-project)
12. [API Endpoints](#12-api-endpoints)
13. [WebSocket Events](#13-websocket-events)
14. [Xu ly loi thuong gap](#14-xu-ly-loi-thuong-gap)

---

## 1. Cai dat

### Yeu cau he thong

| Phan mem | Phien ban toi thieu |
|----------|---------------------|
| Node.js | 18.x tro len |
| npm | 9.x tro len |
| MySQL | 8.0 tro len |
| MySQL Workbench | 8.0 |
| Android Studio | Hedgehog (2023.1.1) tro len |
| Java / JDK | 17 |
| Gradle | 8.x |
| Android SDK | API 36 |

### Cai dat Backend

```powershell
cd backend
npm install
```

### Cai dat Database (MySQL Workbench)

**Cach 1: Su dung MySQL Workbench**

1. Mo MySQL Workbench
2. Ket noi den MySQL Server (localhost:3306)
3. Tao database moi:
   ```sql
   CREATE DATABASE doan3_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
4. Import schema: Server -> Data Import -> chon file `backend/src/database/schema.sql`
5. Import seed data: Server -> Data Import -> chon file `backend/src/database/seed.sql`

**Cach 2: Su dung Command Line**

```bash
mysql -u root -p
CREATE DATABASE doan3_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
EXIT

mysql -u root -p doan3_db < backend/src/database/schema.sql
mysql -u root -p doan3_db < backend/src/database/seed.sql
```

### Cai dat Admin Panel

```powershell
cd admin-panel
npm install
```

---

## 2. Khoi chay Backend

```powershell
cd backend
node src/index.js
```

Backend chay tai: `http://localhost:3000`

**Kiem tra Backend hoat dong:**

```powershell
Invoke-RestMethod -Uri "http://localhost:3000/" -Method GET
```

**Output mong muon:**

```json
{
  "success": true,
  "message": "DoAn3 API is running",
  "version": "1.0.0"
}
```

---

## 3. Khoi chay Android App

### Build Debug APK

```powershell
cd app
.\gradlew assembleDebug
```

APK xuat tai: `app/build/outputs/apk/debug/app-debug.apk`

### Cai dat tren may that

1. Dam bao Backend dang chay va thiet bi cung mang LAN
2. Cap nhat IP thuc cua may chay Backend trong `AppConfig.kt`
3. Build lai APK
4. Copy APK vao dien thoai va cai dat

### Cau hinh Google Maps API Key

**File:** `local.properties`

```properties
sdk.dir=C\:/Users/LOQ/AppData/Local/Android/sdk
MAPS_API_KEY=AIzaSyB2uPnpGi9NDtk5dPIhnmMY-ZL8xoZoADo
```

**Neu can lay API Key moi:**
1. Vao [Google Cloud Console](https://console.cloud.google.com/)
2. APIs & Services > Library > Bat: Maps SDK for Android, Directions API, Distance Matrix API, Geocoding API, Places API
3. APIs & Services > Credentials > Tao API Key > Copy vao `local.properties`

---

## 4. Khoi chay Admin Panel

### Development mode

```powershell
cd admin-panel
npm run dev
```

Admin Panel chay tai: `http://localhost:5173`

### Production build

```powershell
cd admin-panel
npm run build
```

Output trong `admin-panel/dist/`

### Dang nhap Admin

| Email | Mat khau |
|-------|----------|
| admin@test.com | password123 |
| manager@test.com | password123 |

---

## 5. Tai khoan Test

### Khach hang

| Email | Mat khau | Mo ta |
|-------|----------|-------|
| passenger@test.com | password123 | Tai khoan khach hang mau |

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
| manager@test.com | password123 | Revenue Manager - chi xem doanh thu |

---

## 6. Bang gia dich vu

### Gia theo loai xe

| Loai xe | Gia cuoc (VND) | Phi / km | Phi / phut |
|---------|----------------|---------|------------|
| **Xe may** | 10.000 | 3.000 | 100 |
| **O to 4 cho** | 12.000 | 5.000 | 200 |
| **O to 7 cho** | 15.000 | 7.000 | 300 |

### Cong thuc tinh gia

```
Gia = Gia cuoc + (Khoang cach km x Phi/km) + (Thoi gian phut x Phi/phut)
```

### Vi du tinh gia (O to 4 cho, 10km, 20 phut)

```
Gia = 12.000 + (10 x 5.000) + (20 x 200) = 12.000 + 50.000 + 4.000 = 66.000 VND
```

### Chinh sach huy chuyen

| Thoi gian | Phi huy |
|-----------|---------|
| Trong 5 phut dau | Mien phi |
| Sau 5 phut | Khach tra 10%, Tai xe tra 20% |

**Ly do huy chap nhan:** driver_not_responding, change_of_plans, emergency, driver_too_far, wrong_address, passenger_cancelled, duplicate_booking, other

---

## 7. Huong dan su dung - Khach hang

### 7.1 Dang nhap / Dang ky

1. Mo app -> Man hinh Dang nhap
2. Chon "Dang ky" neu chua co tai khoan
3. Nhap thong tin:
   - Ho va ten
   - Email (dinh dang email@domain.com)
   - So dien thoai (10-11 so)
   - Mat khau (toi thieu 6 ky tu)
   - Loai tai khoan: Khach hang
4. Nhan "Dang ky" -> Tu dong dang nhap

### 7.2 Quen mat khau

1. Tu man hinh Dang nhap, nhan "Quen mat khau?"
2. Nhap email da dang ky
3. Nhan "Gui ma OTP" -> Ma 6 chu so duoc gui qua email
4. Nhap ma OTP trong 10 phut
5. Dat mat khau moi -> Xac nhan
6. Dang nhap voi mat khau moi

### 7.3 Dat xe

1. Dang nhap -> Man hinh chinh khach hang
2. Tren ban do, nhan vao diem muon di (diem don)
3. Nhap dia diem muon den (diem tra)
4. Chon loai xe:
   - Xe may (re nhat)
   - O to 4 cho (mac dinh)
   - O to 7 cho (dat nhat)
5. Kiem tra gia uoc tinh
6. Nhan "Dat xe" -> Cho tai xe nhan

### 7.4 Theo doi chuyen di

1. Sau khi dat xe, man hinh hien thi:
   - Thong tin tai xe duoc gan
   - Trang thai: "Dang tim tai xe..." -> "Tai xe da nhan" -> "Tai xe dang den" -> "Dang di chuyen" -> "Da den noi"
2. Theo doi vi tri tai xe real-time tren ban do
3. Nhan "Mo Google Maps" de xem huong dan chi duong

### 7.5 Chat voi tai xe

1. Trong luc chuyen di, nhan nut "Tin nhan" (icon chat)
2. Nhap tin nhan va gui
3. Tai xe nhan duoc tin nhan va phan hoi

### 7.6 Thanh toan

1. Sau khi den noi, tai xe xac nhan "Hoan thanh chuyen di"
2. Man hinh thanh toan hien thi:
   - Tong gia
   - Chon phuong thuc:
     - **Tien mat** (mac dinh)
     - **MoMo** (sandbox)
     - **VNPay** (sandbox)
   - Xac nhan thanh toan
3. Sau khi thanh toan, di chuyen den man hinh danh gia

### 7.7 Danh gia chuyen di

1. Chon so sao (1-5)
2. Chon cac tag danh gia nhanh:
   - Lai xe an toan
   - Than thien
   - Xe sach se
   - Dung gio
   - Ho tro tot
   - Duong tot
3. Nhap binh luan them (khong bat buoc)
4. Nhan "Gui danh gia"

### 7.8 Xem lich su chuyen di

1. Tu man hinh chinh, nhan tab "Lich su"
2. Xem danh sach chuyen di:
   - Da hoan thanh
   - Da huy
   - Theo ngay
3. Tim kiem theo dia diem
4. Nhan vao chuyen di de xem chi tiet

### 7.9 Tinh nang AI

1. Tu man hinh chinh, nhan tab "AI"
2. **Lich trinh AI:** Tao lich trinh voi nhieu diem dung, toi uu theo thoi gian/chi phi/can bang
3. **Goi y:** Xem cac goi y ca nhan hoa
4. **Tro ly AI:** Chat voi tro ly AI de duoc tu van

---

## 8. Huong dan su dung - Tai xe

### 8.1 Dang ky tai xe

1. Dang ky tai khoan nhu binh thuong
2. Chon "Tai xe" lam loai tai khoan
3. Sau khi dang ky, dien them thong tin xe:
   - Hieu xe (VD: Toyota Camry)
   - Mau xe (VD: Den)
   - Bien so (VD: 51A-123.45)
4. Luu thong tin -> Tai khoan san sang su dung

### 8.2 Bat / Tat trang thai online

1. Dang nhap -> Man hinh tai xe
2. Nhan nut toggle "Online" / "Offline"
3. Khi online, tai xe se nhan duoc cac yeu cau dat xe gan

### 8.3 Nhan chuyen di

1. Khi co khach dat xe gan, thong bao hien thi:
   - Dia diem don
   - Dia diem tra
   - Khoang cach
   - Gia uoc tinh
2. Nhan "Nhan chuyen" de chap nhan
3. Nhan "Tu choi" de huy yeu cau

### 8.4 Cap nhat trang thai chuyen di

1. Sau khi nhan chuyen, cap nhat lan luot:
   - **Da den:** Khi da gap khach
   - **Dang di:** Khi bat dau di chuyen
   - **Hoan thanh:** Khi da tra khach den noi

### 8.5 Thu nhap

1. Tu man hinh tai xe, nhan tab "Thu nhap"
2. Xem tong quan:
   - Hom nay / Tuan nay / Thang nay / Tong
3. Xem bieu do thanh 30 ngay gan nhat
4. So sanh voi tuan truoc

### 8.6 Tinh nang AI

1. **Batch Offers:** Xem cac goi y gom chuyen di nhieu khach
2. **AI Recommendations:** Xem cac goi y toi uu hoa

---

## 9. Huong dan su dung - AI

### 9.1 Tao lich trinh AI

1. Tu man hinh AI, chon "Tao lich trinh"
2. Nhap ten lich trinh (VD: "Cuoi tuan Sai Gon")
3. Them cac diem dung:
   - Nhan "+" de them diem
   - Nhap dia diem hoac chon tren ban do
   - Dat thu tu diem dung
4. Chon loai toi uu hoa:
   - **Nhanh nhat:**Uu tien thoi gian
   - **Re nhat:** Uu tien chi phi
   - **Can bang:** Tong hop ca hai
5. Nhan "Tao lich trinh" -> Xem cac phuong an tuyen duong
6. Chon phuong an phu hop -> Xem truoc tren ban do

### 9.2 Toi uu lich trinh

1. Tu man hinh chi tiet lich trinh, nhan "Toi uu"
2. Chon loai toi uu:
   - **Nhanh nhat**
   - **Re nhat**
   - **Can bang**
3. He thong se tinh toan lai va hien thi phuong an moi

### 9.3 Tro ly AI Chat

1. Tu man hinh AI, chon "Tro ly AI"
2. Nhap cau hoi hoac chon goi y nhanh:
   - "Goi y quan cafe gan day"
   - "Len lich trinh Da Nang"
   - "Dia diem du lich noi tieng"
3. AI tra loi voi goi y ca nhan hoa

---

## 10. Huong dan su dung - Admin

### 10.1 Dang nhap

1. Mo trinh duyet, truy cap `http://localhost:5173`
2. Nhap email va mat khau admin
3. Dang nhap -> Man hinh Dashboard

### 10.2 Dashboard

Xem tong quan he thong:
- Tong so nguoi dung
- Tong so tai xe
- Tong so chuyen di
- Doanh thu hom nay / thang nay

### 10.3 Quan ly nguoi dung

1. Tu menu, chon "Nguoi dung"
2. Xem danh sach nguoi dung
3. Tim kiem theo ten, email
4. Lay loc theo loai: Khach hang, Tai xe, Admin
5. Khoa / Mo tai khoan: Nhan nut Ban/Unban

### 10.4 Quan ly tai xe

1. Tu menu, chon "Tai xe"
2. Xem danh sach tai xe
3. Xem thong tin: Xe, bien so, trang thai online/offline, rating
4. Loc theo trang thai online

### 10.5 Quan ly chuyen di

1. Tu menu, chon "Chuyen di"
2. Xem tat ca chuyen di
3. Lay loc theo trang thai: Cho, Da nhan, Dang di, Hoan thanh, Da huy
4. Sua trang thai chuyen di (Admin)

### 10.6 Thong ke

1. Tu menu, chon "Thong ke"
2. Xem bieu do:
   - So chuyen di theo ngay (bieu do cot)
   - Doanh thu theo ngay (bieu do duong)
3. Chon khoang ngay de loc du lieu
4. Xem so sanh voi ky truoc

---

## 11. Cau truc Project

### Backend

```
backend/
├── src/
│   ├── index.js                 # Entry point (port 3000)
│   ├── database/
│   │   ├── db.js                # MySQL connection pool
│   │   ├── schema.sql           # 16 bang (core + AI + payment + admin)
│   │   └── seed.sql             # Du lieu test
│   ├── routes/
│   │   ├── auth.js              # Auth + OTP + Forgot Password
│   │   ├── users.js             # User management
│   │   ├── rides.js             # Ride operations + cancel + rate
│   │   ├── drivers.js           # Driver features + earnings
│   │   ├── locations.js         # Location + nearby drivers
│   │   ├── ai.js               # AI schedule + batch
│   │   ├── chat.js             # P2P chat
│   │   ├── payments.js         # Payment CRUD + VNPay + MoMo
│   │   └── admin.js            # Admin dashboard
│   ├── repositories/            # Data access layer
│   ├── services/
│   │   └── notification.js     # FCM push notifications
│   ├── socket/
│   │   └── index.js            # Socket.IO real-time
│   ├── middleware/
│   │   └── auth.js             # JWT verification
│   └── utils/
│       ├── geo.js              # Haversine distance
│       └── price.js            # Dynamic pricing
├── package.json
└── .env
```

### Android App

```
app/src/main/java/com/laptrinhdidong/DoAn3/
├── MainActivity.kt
├── DoAn3Application.kt          # Hilt Application
├── AppConfig.kt                 # BASE_URL configuration
├── data/
│   ├── local/
│   │   └── SessionManager.kt    # SharedPreferences + JWT
│   ├── remote/
│   │   ├── RetrofitClient.kt   # HTTP client + OkHttp
│   │   ├── SocketManager.kt    # Socket.IO client
│   │   ├── ApiService.kt       # All API endpoints
│   │   └── dto/                # Request/Response DTOs
│   └── repository/              # AuthRepository, RideRepository, DriverRepository, AIRepository, PaymentRepository
├── di/
│   └── AppModule.kt            # Hilt DI modules
├── ui/
│   ├── theme/                  # Material3 theming
│   ├── components/
│   │   ├── CommonComponents.kt # GradientButton, RatingBar, DriverCard, etc.
│   │   └── MapComponents.kt    # TaxiMapView (Google Maps)
│   ├── navigation/
│   │   └── AppNavigation.kt    # Navigation graph
│   └── screens/
│       ├── auth/               # AuthScreen, SplashScreen
│       ├── passenger/          # PassengerHomeScreen, HistoryScreen
│       ├── driver/             # DriverHomeScreen, EarningsScreen
│       ├── ai/                # AIScreens (Schedule, Recommendations, Chat)
│       └── shared/            # ProfileScreen, RideDetailScreen
└── service/
    └── DoAn3FCMService.kt     # Firebase Cloud Messaging
```

### Admin Panel

```
admin-panel/
├── src/
│   ├── pages/
│   │   ├── Dashboard.jsx
│   │   ├── Users.jsx
│   │   ├── Drivers.jsx
│   │   ├── Rides.jsx
│   │   └── Statistics.jsx
│   ├── components/
│   │   ├── Sidebar.jsx
│   │   └── StatCard.jsx
│   ├── context/
│   │   └── AuthContext.jsx
│   ├── api/
│   │   └── adminApi.js
│   ├── App.jsx
│   └── main.jsx
├── package.json
├── vite.config.js
└── tailwind.config.js
```

---

## 12. API Endpoints

### Authentication

| Method | Endpoint | Mo ta |
|--------|----------|-------|
| POST | `/api/auth/register` | Dang ky tai khoan |
| POST | `/api/auth/login` | Dang nhap |
| POST | `/api/auth/fcm/register` | Dang ky FCM token |
| POST | `/api/auth/forgot-password` | Gui ma OTP qua email |
| POST | `/api/auth/verify-otp` | Xac minh ma OTP |
| POST | `/api/auth/reset-password` | Dat lai mat khau |
| POST | `/api/auth/resend-otp` | Gui lai ma OTP |

### Users

| Method | Endpoint | Mo ta |
|--------|----------|-------|
| GET | `/api/users/me` | Thong tin nguoi dung hien tai |
| GET | `/api/users/drivers/nearby` | Tim tai xe gan (query: lat, lng, radius) |
| GET | `/api/users/:id` | Thong tin nguoi dung theo ID |

### Rides

| Method | Endpoint | Mo ta |
|--------|----------|-------|
| POST | `/api/rides/request` | Dat xe (pickup, dropoff, vehicle_type) |
| GET | `/api/rides` | Lich su chuyen di (query: status, search) |
| GET | `/api/rides/search` | Tim kiem chuyen di |
| GET | `/api/rides/active` | Chuyen di dang hoat dong |
| GET | `/api/rides/:id` | Chi tiet chuyen di |
| PUT | `/api/rides/:id/status` | Cap nhat trang thai |
| PUT | `/api/rides/:id/cancel` | Huy chuyen di |
| POST | `/api/rides/:id/rate` | Danh gia (rating, tags, comment) |

### Driver

| Method | Endpoint | Mo ta |
|--------|----------|-------|
| GET | `/api/driver/profile` | Ho so tai xe |
| PUT | `/api/driver/profile` | Cap nhat ho so |
| PUT | `/api/driver/status` | Online / Offline |
| GET | `/api/driver/ride/available` | Chuyen di kha dung |
| POST | `/api/driver/ride/:id/accept` | Nhan chuyen |
| POST | `/api/driver/ride/:id/reject` | Tu choi chuyen |
| GET | `/api/driver/earnings` | Thu nhap (today, week, month, total, daily breakdown) |
| GET | `/api/driver/history` | Lich su chuyen di |

### Location

| Method | Endpoint | Mo ta |
|--------|----------|-------|
| PUT | `/api/location/update` | Cap nhat vi tri tai xe |
| GET | `/api/location/driver/:id` | Lay vi tri tai xe |
| GET | `/api/location/nearby-drivers` | Danh sach tai xe gan (query: lat, lng, radius) |

### AI

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

### Chat

| Method | Endpoint | Mo ta |
|--------|----------|-------|
| GET | `/api/chat/:rideId/messages` | Lay tin nhan cua chuyen di |
| POST | `/api/chat/:rideId/send` | Gui tin nhan |

### Payment

| Method | Endpoint | Mo ta |
|--------|----------|-------|
| GET | `/api/payments/methods` | Danh sach phuong thuc thanh toan |
| POST | `/api/payments/create` | Tao payment cho ride |
| GET | `/api/payments/:id` | Chi tiet payment |
| POST | `/api/payments/:id/confirm` | Xac nhan thanh toan |
| GET | `/api/payments/history` | Lich su thanh toan |
| GET | `/api/payments/admin/all` | Tat ca payments (admin) |

### Admin

| Method | Endpoint | Mo ta |
|--------|----------|-------|
| GET | `/api/admin/dashboard` | Tong quan he thong |
| GET | `/api/admin/users` | Danh sach nguoi dung |
| PUT | `/api/admin/users/:id/status` | Khoa / Mo tai khoan |
| GET | `/api/admin/rides` | Danh sach chuyen di |
| PUT | `/api/admin/rides/:id/status` | Sua trang thai ride (admin) |
| GET | `/api/admin/drivers` | Danh sach tai xe |
| GET | `/api/admin/stats/daily` | Thong ke theo ngay |
| GET | `/api/admin/stats/revenue` | Thong ke doanh thu |

---

## 13. WebSocket Events

### Driver -> Server

| Event | Payload | Mo ta |
|-------|---------|-------|
| `location:update` | `{ lat, lng, rideId }` | Gui vi tri GPS realtime |
| `ride:status` | `{ rideId, status }` | Thay doi trang thai chuyen di |

### Server -> Passenger

| Event | Payload | Mo ta |
|-------|---------|-------|
| `driver:location` | `{ lat, lng, rideId, timestamp }` | Vi tri tai xe realtime |
| `ride:status:changed` | `{ rideId, status, timestamp }` | Trang thai chuyen di thay doi |

### Passenger -> Server

| Event | Payload | Mo ta |
|-------|---------|-------|
| `join:ride` | `rideId` | Tham gia phong chuyen di |
| `leave:ride` | `rideId` | Roi phong chuyen di |

### Chat

| Event | Direction | Mo ta |
|-------|----------|-------|
| `chat:message` | Server -> Recipient | Tin nhan chat moi |

---

## 14. Xu ly loi thuong gap

### Loi "Route not found" (Backend)

**Nguyen nhan:** Backend chua khoi dong hoac chay loi.

**Cach xu ly:**
1. Kiem tra backend dang chay: `node src/index.js`
2. Kiem tra port 3000 khong bi chiem: `netstat -ano | findstr 3000`
3. Kiem tra MySQL ket noi: `mysql -u root -p -e "SELECT 1"`
4. Restart backend: `node src/index.js`

### Loi "Khong goi duoc API" (Android)

**Nguyen nhan:** IP khong dung hoac Backend khong cho phep CORS.

**Cach xu ly:**
1. Kiem tra IP may chay Backend: `ipconfig`
2. Cap nhat `AppConfig.kt`:
   - Emulator: `http://10.0.2.2:3000/api/`
   - May that: `http://192.168.x.x:3000/api/` (IP cua may chay Backend)
3. Dam bao may that va dien thoai cung mang LAN
4. Tat firewall Windows cho port 3000

### Loi "Database connection failed"

**Nguyen nhan:** Thong tin MySQL khong dung.

**Cach xu ly:**
1. Kiem tra MySQL dang chay
2. Cap nhat `.env` trong backend:
   ```
   DB_HOST=localhost
   DB_USER=root
   DB_PASSWORD=1234
   DB_NAME=doan3_db
   DB_PORT=3306
   ```
3. Chay lai schema: `mysql -u root -p doan3_db < backend/src/database/schema.sql`

### Loi "Ban do khong hien thi" (Google Maps)

**Nguyen nhan:** API Key chua duoc cau hinh dung.

**Cach xu ly:**
1. Vao Google Cloud Console -> APIs & Services -> Credentials
2. Bat cac API: Maps SDK for Android, Directions API, Geocoding API
3. Copy API Key vao `local.properties`
4. Build lai app: `.\gradlew assembleDebug`

### Loi "App crash khi dang nhap driver moi"

**Nguyen nhan:** Driver moi dang ky chua co thong tin name.

**Cach xu ly:** Da duoc fix trong code. Dam bao su dung APK moi nhat.

### Loi "JWT Token expired"

**Nguyen nhan:** Token het han (30 ngay).

**Cach xu ly:**
1. Dang xuat: Xoa app -> Cai lai
2. Hoac: Reset mat khau de nhan token moi

### Loi "Firebase notification khong nhan"

**Nguyen nhan:** Chua cau hinh Firebase.

**Cach xu ly:**
1. Tao Firebase project
2. Download `google-services.json` vao `app/`
3. Cau hinh Firebase credentials trong `backend/.env`

---

## Bang thong tin nguoi dung

### Loai nguoi dung

| Loai | Mo ta |
|------|-------|
| passenger | Khach hang dat xe |
| driver | Tai xe nhan chuyen |
| owner | Chu xe |
| consultant | Tu van khach hang |
| hr_manager | Quan ly nhan su |
| revenue_manager | Quan ly doanh thu |

### Trang thai chuyen di

```
pending -> accepted -> arrived -> in_progress -> completed
              \            \            \           \
               \            \            \           -> cancelled
                \            \            -> cancelled
                 \            -> cancelled
                  -> cancelled (boi khach)
```

### Loai xe

| Loai | Mo ta |
|------|-------|
| motorbike | Xe may |
| car_4_seats | O to 4 cho |
| car_7_seats | O to 7 cho |

---

*Luu y: Tai lieu nay duoc cap nhat lan cuoi: 2026-05-13*
