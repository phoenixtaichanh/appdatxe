# Huong Dan Su Dung - DoAn3 Taxi App

> **Phien ban:** 1.2.0

> **De tai:** Xay dung he thong dat xe thong minh ket hop tro ly du lich AI

> **Cong nghe:** Kotlin (Android) + Node.js (Backend) + MySQL + AI

> **Cap nhat moi nhat:**
> - Them 3 loai xe (xe may, o to 4 cho, o to 7 cho)
> - Dynamic pricing theo loai xe
> - Polling tai xe gan (5 giay)
> - Google Maps SDK integration
> - TaxiMapView voi markers va polyline
> - WebSocket real-time tracking (Socket.IO)
> - Push Notification (FCM)
> - P2P Chat giua khach va tai xe
> - Navigation button (Google Maps navigation)
> - Earnings chart va driver stats
> - AI Chat Assistant screen
> - Payment method selector va enhanced rating
> - [2026-05-10] Fix AppModule baseUrl hardcoded -> AppConfig.BASE_URL
> - [2026-05-10] Fix deprecated statusBarColor trong Theme.kt
> - [2026-05-10] Fix 6 deprecated Icons.Default -> Icons.AutoMirrored.Filled
> - [2026-05-10] Fix socket/index.js getIO() runtime crash (ioInstance reference)
> - [2026-05-10] Fix locations.js syntax error (extra closing paren)
> - [2026-05-10] Build thanh cong - 0 warnings, 0 errors

---

## Muc luc

1. [Cai dat](#1-cai-dat)
2. [Khoi chay Backend](#2-khoi-chay-backend)
3. [Khoi chay Android App](#3-khoi-chay-android-app)
4. [Tai khoan Test](#4-tai-khoan-test)
5. [Bang gia dich vu](#5-bang-gia-dich-vu)
6. [Huong dan su dung - Khach hang](#6-huong-dan-su-dung---khach-hang)
7. [Huong dan su dung - Tai xe](#7-huong-dan-su-dung---tai-xe)
8. [Huong dan su dung - AI](#8-huong-dan-su-dung---ai)
9. [Cau truc Project](#9-cau-truc-project)
10. [API Endpoints](#10-api-endpoints)
11. [WebSocket Events](#11-websocket-events)
12. [Xu ly loi thuong gap](#12-xu-ly-loi-thuong-gap)

---

## 1. Cai dat

### Yeu cau he thong

| Phan mem | Phien ban toi thieu |
|---|---|
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
5. Import seed data: `backend/src/database/seed.sql`

### Cau hinh Environment

**Backend - File `.env`:**

```bash
DB_HOST=localhost
DB_USER=root
DB_PASSWORD=your_password
DB_NAME=doan3_db
DB_PORT=3306
JWT_SECRET=doan3_super_secret_key_change_this_2024
PORT=3000
```

**Android - File `local.properties`:**

```bash
sdk.dir=C\:/Users/YOUR_USER/AppData/Local/Android/sdk
MAPS_API_KEY=YOUR_GOOGLE_MAPS_API_KEY_HERE
```

> **QUAN TRONG:** De su dung Google Maps, ban can:
> 1. Tao project tai Google Cloud Console
> 2. Bat Maps SDK for Android
> 3. Tao API Key
> 4. Copy API key vao `local.properties`
> 5. Build lai app

---

## 2. Khoi chay Backend

```powershell
cd backend
npm run dev
```

**Ket qua mong doi:**

```
> doan3-backend@1.0.0 dev
> nodemon src/index.js
✅ Database connected successfully!
🚀 Server running on http://localhost:3000
📡 API Base URL: http://localhost:3000/api
🔌 WebSocket: ws://localhost:3000
```

---

## 3. Khoi chay Android App

1. Mo Android Studio -> Open project `D:\laptrinhdidong\DoAn3`
2. Sync Gradle: Tools -> Sync Project with Gradle Files
3. Chay App: Shift + F10

---

## 4. Tai khoan Test

| Loai | Email | Mat khau |
|---|---|---|
| Passenger | passenger@test.com | password123 |
| Driver 1 | driver1@test.com | password123 |
| Driver 2 | driver2@test.com | password123 |
| Driver 3 | driver3@test.com | password123 |

### Dang ky tai khoan moi

1. Mo app -> man hinh Dang nhap
2. Chuyen sang tab Dang ky
3. Dien thong tin:
   - Ho ten: Nguyen Van Test
   - Email: test@example.com
   - So dien thoai: 0912345678
   - Mat khau: password123 (toi thieu 6 ky tu)
4. Chon loai tai khoan:
   - Passenger - De dat xe
   - Driver - De nhan chuyen
5. Nhan Tao tai khoan

---

## 5. Bang gia dich vu

| Loai phuong tien | Cuoc co ban | Gia/km | Gia/phut |
|---|---|---|---|
| Xe may | 10.000d | 3.000d | 100d |
| O to 4 cho | 12.000d | 5.000d | 200d |
| O to 7 cho | 15.000d | 7.000d | 300d |

> **Cong thuc tinh gia:** Cuoc co ban + (Khoang cach x Gia/km) + (Thoi gian x Gia/phut)

---

## 6. Huong dan su dung - Khach hang

### 6.1. Dang nhap / Dang ky

1. Mo app -> Hien thi Splash Screen (logo voi animation)
2. Chuyen sang man hinh Auth neu chua dang nhap
3. Dang nhap bang email/password
4. Dang ky neu chua co tai khoan

### 6.2. Dat xe

1. Tu man hinh chinh, nhan nut Dat xe
2. Nhap diem don - nhan vao ban do hoac nhap dia chi
3. Nhap diem den
4. Chon loai xe:
   - Xe may - Gia thap nhat (10k cuoc co ban)
   - O to 4 cho - Pho bien nhat (12k cuoc co ban)
   - O to 7 cho - Cho nhom (15k cuoc co ban)
5. Xem uoc tinh gia (chi tiet: cuoc co ban, phi quang duong, phi thoi gian)
6. Nhan Tim tai xe:
   - He thong tim tai xe trong ban kinh 5km
   - Hien thi so luong tai xe gan day (polling 5 giay)
7. Nhan Dat xe ngay - cho tai xe nhan chuyen

### 6.3. Theo doi chuyen di (Real-time)

1. Man hinh chinh hien thi:
   - Thong tin tai xe (ten, so dien thoai, xe)
   - **Vi tri tai xe tren ban do real-time** (WebSocket)
   - Thoi gian uoc tinh den
2. Cac trang thai chuyen di:
   - pending - Cho tai xe nhan
   - accepted - Tai xe da nhan chuyen
   - arrived - Tai xe da den diem don
   - in_progress - Dang di chuyen
   - completed - Hoan thanh
   - cancelled - Da huy

### 6.4. Danh gia sau chuyen

1. Sau khi chuyen hoan thanh, man hinh danh gia hien thi
2. Chon so sao (1-5 sao)
3. Chon nhanh tags (Lai xe an toan, Than thien, Xe sach se,...)
4. Nhap nhan xet (tuy chon)
5. Nhan Gui danh gia

### 6.5. Chat voi tai xe

1. Khi co chuyen dang di, nhan nut Chat
2. Gui tin nhan den tai xe
3. Xem lich su tin nhan
4. Tai xe nhan duoc tin nhan real-time

### 6.6. Xem lich su chuyen di

1. Nhan icon Lich su
2. **Tim kiem** theo dia diem
3. Loc theo: Tat ca, Hoan thanh, Da huy
4. Nhan vao chuyen de xem chi tiet

---

## 7. Huong dan su dung - Tai xe

### 7.1. Dang nhap

1. Dang nhap voi tai khoan Driver: driver1@test.com / password123
2. Dien thong tin xe: loai xe, mau xe, bien so

### 7.2. Bat/Tat truc tuyen

1. Nhan nut toggle Online/Offline
2. Online (xanh la) - Nhan duoc thong bao chuyen moi
3. Offline (xam) - Khong nhan chuyen

### 7.3. Nhan chuyen

1. Khi co chuyen moi, thong bao hien thi
2. Tab Chuyen moi: Hien thi danh sach chuyen dang cho
   - Xem diem don, diem den
   - Xem gia, khoang cach, thoi gian
   - **Xem loai phuong tien** khach hang chon
3. Nhan Nhan - Chuyen duoc assign cho ban
4. Nhan Tu choi - Bo qua chuyen nay

### 7.4. Thuc hien chuyen

1. Tab Dang chay: Hien thi chuyen dang thuc hien
2. Cap nhat trang thai:
   - Da den diem don - Khi den noi don khach
   - Bat dau chuyen - Khi khach len xe
   - Hoan thanh chuyen - Khi den diem den
3. **Dieu huong:**
   - Nhan nut Navigate de mo **Google Maps**
   - Tu dong dieu huong den diem den

### 7.5. Xem thu nhap

1. Nhan icon Vi
2. Xem thong ke:
   - Thu nhap hom nay / tuan / thang
   - Tong so chuyen
   - **Bieu do cot** thu nhap theo ngay
   - **Driver stats**: tong chuyen, danh gia, ty le nhan

### 7.6. AI Batch (Ghep chuyen)

1. Tab Batch: Xem cac de xuat ghep chuyen
2. Xem chi tiet: so luong khach, doanh thu, diem hieu qua
3. Nhan Chap nhan Batch - nhan tat ca chuyen trong batch

---

## 8. Huong dan su dung - AI

### 8.1. AI Chat Assistant

1. Tu man hinh chinh, nhan icon Chat (neu co)
2. Chat voi AI Assistant:
   - Dat xe: "Goi xe di Bach Khoa"
   - Dat xe 7 cho: "Dat xe 7 cho sang Quan 1"
   - Lich trinh: "Lich trinh Ha Long 2 ngay"
   - Tu van gia: "Gia re nhat"
3. AI se goi y nhanh va tra loi cau hoi

### 8.2. Tao lich trinh AI

1. Nhan icon AI (hinh sao/phep thuat)
2. Nhan Tao lich trinh moi
3. Nhap thong tin: ten lich trinh, ngay di, loai toi uu
4. Them cac diem dung (waypoints)
5. Nhan Tao lich trinh
6. Xem ket qua: tong khoang cach, thoi gian, gia tien

### 8.3. Cai dat AI

1. Nhan Cai dat AI trong man hinh AI
2. Thi lap so thich: gio ua thich, uu tien gia/thoi gian
3. AI hoc tu lich su de dua ra goi y chinh xac hon

---

## 9. Cau truc Project

### 9.1. Backend (Node.js)

```
backend/
├── src/
│   ├── index.js              # Entry point, Express + Socket.IO
│   ├── database/
│   │   ├── db.js            # MySQL connection pool
│   │   ├── schema.sql        # Database schema
│   │   └── seed.sql          # Test data
│   ├── routes/
│   │   ├── auth.js          # Auth + FCM token
│   │   ├── users.js         # User management
│   │   ├── drivers.js       # Driver management
│   │   ├── rides.js         # Ride management
│   │   ├── locations.js     # Location services
│   │   ├── ai.js            # AI features
│   │   └── chat.js          # P2P Chat API
│   ├── middleware/
│   │   └── auth.js          # JWT authentication
│   ├── repositories/
│   │   ├── rideRepository.js
│   │   └── driverRepository.js
│   ├── services/
│   │   └── notification.js   # FCM notifications
│   ├── socket/
│   │   └── index.js         # Socket.IO server
│   └── utils/
│       ├── geo.js           # Haversine distance
│       └── price.js         # Dynamic pricing
├── package.json
└── .env
```

### 9.2. Android (Kotlin)

```
app/src/main/java/com/laptrinhdidong/DoAn3/
├── MainActivity.kt
├── DoAn3Application.kt       # Hilt Application
├── AppConfig.kt              # API configuration
├── data/
│   ├── local/
│   │   └── SessionManager.kt
│   ├── remote/
│   │   ├── RetrofitClient.kt
│   │   ├── ApiService.kt
│   │   ├── SocketManager.kt  # Socket.IO client
│   │   └── dto/
│   └── repository/
├── service/
│   └── DoAn3FCMService.kt   # FCM Service
├── di/
│   └── AppModule.kt         # Hilt DI
├── ui/
│   ├── theme/
│   ├── components/
│   │   ├── CommonComponents.kt
│   │   └── MapComponents.kt  # TaxiMapView
│   ├── navigation/
│   │   └── AppNavigation.kt
│   └── screens/
│       ├── auth/
│       ├── splash/
│       ├── passenger/
│       ├── driver/
│       └── ai/
└── util/
```

---

## 10. API Endpoints

### Authentication

| Method | Endpoint | Mo ta |
|---|---|---|
| POST | /api/auth/register | Dang ky |
| POST | /api/auth/login | Dang nhap |
| POST | /api/auth/fcm/register | Dang ky FCM token |

### Rides

| Method | Endpoint | Mo ta |
|---|---|---|
| POST | /api/rides/request | Tao yeu cau dat xe (voi vehicle_type) |
| GET | /api/rides | Lay lich su chuyen di |
| GET | /api/rides/:id | Chi tiet chuyen di |
| PUT | /api/rides/:id/status | Cap nhat trang thai |
| POST | /api/rides/:id/rate | Danh gia chuyen di |

### Driver

| Method | Endpoint | Mo ta |
|---|---|---|
| GET | /api/driver/profile | Lay profile tai xe |
| PUT | /api/driver/status | Cap nhat online/offline |
| GET | /api/driver/ride/available | Lay chuyen kha dung |
| POST | /api/driver/ride/:id/accept | Nhan chuyen |
| POST | /api/driver/ride/:id/reject | Tu choi chuyen |
| PUT | /api/driver/ride/:id/status | Cap nhat trang thai |
| GET | /api/driver/earnings | Lay thu nhap |

### AI

| Method | Endpoint | Mo ta |
|---|---|---|
| POST | /api/ai/schedule/create | Tao lich trinh AI |
| POST | /api/ai/schedule/:id/optimize | Toi uu lich trinh |
| GET | /api/ai/recommendations | Goi y ca nhan hoa |
| GET | /api/ai/batch/available | Lay batch kha dung |
| POST | /api/ai/batch/:id/accept | Chap nhan batch |

### Chat

| Method | Endpoint | Mo ta |
|---|---|---|
| GET | /api/chat/:rideId/messages | Lay tin nhan chat |
| POST | /api/chat/:rideId/send | Gui tin nhan |

---

## 11. WebSocket Events

| Event | Huong | Mo ta |
|---|---|---|
| location:update | Driver -> Server | Cap nhat vi tri tai xe |
| driver:location | Server -> Passenger | Vi tri tai xe real-time |
| ride:status | Driver -> Server | Cap nhat trang thai ride |
| ride:status:changed | Server -> Passenger | Thong bao trang thai |
| join:ride | Passenger -> Server | Tham gia phong ride |
| leave:ride | Passenger -> Server | Roi phong ride |
| chat:message | Server -> Recipient | Tin nhan chat moi |

---

## 12. Xu ly loi thuong gap

### Loi Backend

| Loi | Nguyen nhan | Cach khac phuc |
|---|---|---|
| ECONNREFUSED | MySQL khong chay | Khoi dong MySQL service |
| ER_ACCESS_DENIED_ERROR | Sai password MySQL | Kiem tra .env |
| jwt malformed | Token khong hop le | Dang nhap lai |
| Port 3000 already in use | Port bi chiem | Kill process hoac doi port |
| `location:update` khong broadcast | Loi reference io trong socket/index.js | Da fix - thay bang `ioInstance` |
| SyntaxError locations.js | Dau ngoac thua trong route | Da fix - xoa dau ) thua |

### Loi Android

| Loi | Nguyen nhan | Cach khac phuc |
|---|---|---|
| Gradle sync failed | Version conflict | File -> Invalidate Caches -> Restart |
| compileSdk mismatch | SDK version | Cap nhat compileSdk = 36 |
| Network request failed | Server khong chay | Khoi dong backend truoc |
| 10.0.2.2:3000 khong hoat dong | Su dung thiet bi that | Doi sang IP may tinh |
| Ban do khong hien thi | Thieu Maps API Key | Them MAPS_API_KEY vao local.properties |

---

**Ban quyen:** Do an 3 - Truong DH Bach Khoa TP.HCM
**Nhom:** Le Dang Khoa, Tran Nguyen Tuan Anh
