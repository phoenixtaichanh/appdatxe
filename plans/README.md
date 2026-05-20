# Feature Plan - DoAn3 Taxi App

> **De tai:** Xay dung he thong dat xe thong minh ket hop tro ly du lich AI

> **Trang thai du an:** Backend 100% | Android 100% | Admin Panel 100% | BUILD SUCCESSFUL (2026-05-13)

---

## Muc luc

| # | Feature Plan | Mo ta |
|---|-------------|-------|
| **1** | [1-Authentication.md](./1-Authentication.md) | Dang nhap / Dang ky / Quen mat khau / Session |
| **2** | [2-CoreRideFlow.md](./2-CoreRideFlow.md) | Dat xe -> Tim tai xe -> Theo doi -> Hoan thanh |
| **3** | [3-RealtimeTracking.md](./3-RealtimeTracking.md) | GPS real-time, WebSocket, Map integration |
| **4** | [4-DriverApp.md](./4-DriverApp.md) | Ung dung tai xe hoan chinh |
| **5** | [5-AIIntelligence.md](./5-AIIntelligence.md) | AI Schedule, Recommendations, Driver Batching |
| **6** | [6-PaymentRating.md](./6-PaymentRating.md) | Thanh toan, danh gia, hoa don |
| **7** | [7-ProfileHistory.md](./7-ProfileHistory.md) | Profile, lich su chuyen di, earnings |
| **8** | [8-Notifications.md](./8-Notifications.md) | Push notification, FCM |
| **9** | [9-MapIntegration.md](./9-MapIntegration.md) | Google Maps SDK, Directions API |
| **10** | [10-P2PCommunication.md](./10-P2PCommunication.md) | Chat giua khach va tai xe |
| **11** | [11-AdminPanel.md](./11-AdminPanel.md) | Web admin panel |
| **12** | [12-Refinement.md](./12-Refinement.md) | Performance, polish, error handling |

---

## Tong quan trang thai

### Da hoan thanh

| Feature | Backend | Android |
|---------|---------|---------|
| Auth (Register/Login) | ✅ | ✅ |
| Dat xe (tao ride) | ✅ | ✅ |
| Tim tai xe gan | ✅ | ✅ |
| Tai xe nhan chuyen | ✅ | ✅ |
| Cap nhat trang thai ride | ✅ | ✅ |
| Danh gia sao | ✅ | ✅ |
| Danh gia voi tags | ✅ | ✅ |
| Xem lich su chuyen di | ✅ | ✅ |
| Lich su chuyen di (tim kiem + loc) | ✅ | ✅ |
| Thu nhap tai xe | ✅ | ✅ |
| Thu nhap tai xe (chi tiet + chart breakdown) | ✅ | ✅ |
| Online/Offline toggle | ✅ | ✅ |
| AI Schedule creation | ✅ | ✅ |
| AI Route optimization | ✅ | ✅ |
| AI Profile preferences | ✅ | ✅ |
| AI Recommendations | ✅ | ✅ |
| Driver Batch offers | ✅ | ✅ |
| Ride detail screen | ✅ | ✅ |
| Profile screen | ✅ | ✅ |
| Navigation graph | ✅ | ✅ |
| Splash screen -> auto-login | N/A | ✅ |
| Multi-vehicle type selection | ✅ | ✅ |
| Dynamic pricing | ✅ | ✅ |
| Google Maps SDK | N/A | ✅ |
| Polling nearby drivers | ✅ | ✅ |
| WebSocket real-time tracking | ✅ | ✅ |
| Push Notification (FCM) | ✅ | ✅ |
| P2P Chat khach <-> tai xe | ✅ | ✅ |
| Navigation button (Google Maps) | N/A | ✅ |
| Earnings chart | N/A | ✅ |
| Driver stats card | N/A | ✅ |
| AI Chat Assistant | N/A | ✅ |
| Payment method selection | ✅ | ✅ |
| Payment processing (cash/wallet) | ✅ | ✅ |
| VNPay/MoMo sandbox links | ✅ | ✅ |
| Enhanced rating with tags | ✅ | ✅ |
| History search | ✅ | ✅ |
| Ride cancellation policy | ✅ | ✅ |
| Cancellation fee rules | ✅ | N/A |
| Admin Dashboard API | ✅ | N/A |
| Admin User Management API | ✅ | N/A |
| Admin Ride Management API | ✅ | N/A |
| Admin Stats API | ✅ | N/A |
| Admin Panel Web UI (React) | ✅ | N/A |
| OTP / Quen mat khau (Backend) | ✅ | N/A |
| OTP / Quen mat khau (Android) | N/A | ✅ |
| Payment method selection (Android) | ✅ | ✅ |
| Real VNPay integration (HMAC) | ✅ | N/A |
| Real MoMo integration (HMAC) | ✅ | N/A |
| Payment history API | ✅ | ✅ |

### Can hoan thien them

> **Tat ca cac feature chinh da hoan thanh!** Chi con improvement nho.

| Feature | Trang thai | Uu tien |
|---------|-----------|---------|
| Thanh toan thuc (VNPay/MoMo) - requires real API keys | Ready (HMAC signing, sandbox auto-fallback) | Thap |

---

## Da fix trong session nay

### Backend (2026-05-13)
- **Ride cancellation policy** (`rides.js`): POST /api/rides/:id/cancel voi rules:
  - 5 phut dau: khong co phi huy
  - Sau 5 phut: khach tra 10%, tai xe tra 20%
  - 8 ly do huy: driver_not_responding, change_of_plans, etc.
  - Notification khi huy
  - Ghi log vao cancellation_log table
- **Enhanced rating** (`rides.js`): POST /api/rides/:id/rate ho tro tags
- **Ride search API** (`rides.js`): GET /api/rides/search voi loc theo status, ngay, noi dung
- **Driver earnings breakdown** (`drivers.js`): GET /api/driver/earnings tra ve:
  - summary: today/week/month/total
  - daily breakdown cho chart (30 ngay)
  - week comparison (this vs last week)
  - ride stats: total/completed/cancelled/avg_rating
- **Payment APIs** (`payments.js`): CRUD day du:
  - GET /api/payments/methods - danh sach phuong thuc
  - POST /api/payments/create - tao payment cho ride
  - GET /api/payments/:id - chi tiet payment
  - POST /api/payments/:id/confirm - confirm/callback
  - GET /api/payments/history - lich su thanh toan
  - GET /api/payments/admin/all - tat ca payments (admin)
- **Admin APIs** (`admin.js`): Dashboard + User + Ride + Driver + Stats:
  - GET /api/admin/dashboard - tong quan he thong
  - GET /api/admin/users - danh sach nguoi dung
  - PUT /api/admin/users/:id/status - khoa/mo tai khoan
  - GET /api/admin/rides - danh sach chuyen di
  - PUT /api/admin/rides/:id/status - admin sua trang thai ride
  - GET /api/admin/drivers - danh sach tai xe
  - GET /api/admin/stats/daily - thong ke theo ngay
  - GET /api/admin/stats/revenue - thong ke doanh thu
- **Database schema** (`schema.sql`): Them 3 bang:
  - `ride_rating_tags` - tags cho danh gia
  - `cancellation_log` - log huy chuyen
  - `ride_images` - hinh anh chuyen di
- **Seed data** (`seed.sql`): Them admin/manager accounts:
  - admin@test.com (owner) / password123
  - manager@test.com (revenue_manager) / password123

### Android (2026-05-10)
- Google Maps SDK integration (TaxiMapView)
- Nearby drivers polling (5s interval)
- Socket.IO client (SocketManager)
- Real-time driver location updates tren map
- Navigation button (Google Maps navigation intent)
- Earnings bar chart
- Driver stats card
- AI Chat Assistant screen
- Enhanced rating voi tags
- Payment method selector
- History search bar
- P2P Chat screen
- FCM service (DoAn3FCMService)
- Fix NullPointerException khi driver moi dang ky
- Fix AppModule.kt: baseUrl hardcoded -> AppConfig.BASE_URL
- Fix Theme.kt: Deprecated statusBarColor -> xoa dong set
- Fix 6 deprecated Icons.Default -> Icons.AutoMirrored.Filled

### Backend (2026-05-13 - Phien tiep)
- **OTP / Forgot Password** (`auth.js`): 4 new endpoints:
  - `POST /api/auth/forgot-password` - Gui OTP 6 chu so qua email (10 phut het han)
  - `POST /api/auth/verify-otp` - Xac minh OTP
  - `POST /api/auth/reset-password` - Dat lai mat khau
  - `POST /api/auth/resend-otp` - Gui lai OTP
- **Nodemailer integration**: SMTP config trong .env, HTML email template voi ma OTP
- **Database schema** (`schema.sql`): Them bang `password_resets` va `transactions`, `user_fcm_tokens`
- **Real Payment Integration** (`payments.js`):
  - VNPay HMAC-SHA256 signing voi secure hash
  - MoMo HMAC-SHA256 signing
  - Sandbox auto-fallback khi chua co API keys
  - Payment return URLs
- **Payment credentials** (`.env`): VNPAY_TMN_CODE, VNPAY_HASH_SECRET, MOMO_PARTNER_CODE, MOMO_ACCESS_KEY, MOMO_SECRET_KEY

### Android (2026-05-13 - Phien tiep)
- **OTP screens** (3 screens):
  - `ForgotPasswordScreen.kt` - Nhap email de nhan OTP
  - `OtpVerificationScreen.kt` - Nhap 6 chu so OTP voi countdown timer va resend
  - `ResetPasswordScreen.kt` - Dat lai mat khau voi password strength indicator
- **Navigation routes**: ForgotPassword, OtpVerification, ResetPassword
- **ApiService.kt**: Them 4 API methods cho password reset
- **PasswordResetRepository.kt**: Full repository voi Result pattern
- **PaymentRepository.kt**: Tao moi voi cac method CRUD day du
- **PaymentDto.kt**: Day du DTOs cho payment flow
- **RideDetailScreen.kt**: Payment method selector hoat dong:
  - Chon phuong thuc: Tien mat / MoMo / VNPay
  - Nut xac nhan thanh toan goi API
  - Hien thi trang thai thanh toan

### Admin Panel (2026-05-13 - Moi)
- **React + Vite + Tailwind CSS** (full admin panel)
- **Pages**: Login, Dashboard, Users, Drivers, Rides, Statistics
- **Features**:
  - JWT authentication voi admin role check
  - Dashboard: Stat cards, recent rides table, revenue summary
  - Users: Search, filter by role, ban/unban accounts
  - Drivers: Search, earnings display, online/offline status
  - Rides: Filter by status, inline status editing
  - Statistics: Bar/line charts (Recharts), revenue comparison, date range selector
- **Location**: `admin-panel/` (node_modules da install, build thanh cong)

### Build Result (2026-05-13)
- Backend JS Syntax: **Tat ca OK** (22 files checked)
- Android Clean Build: **BUILD SUCCESSFUL** (43 tasks)
- APK: `app-debug.apk` - **19.55 MB**
- Android Warnings: **0**
- Android Lint: **0 errors**
- Admin Panel Build: **BUILD SUCCESSFUL** (dist/ output)

---

## Huong dan chay

### Backend
```bash
cd backend
npm install
node src/index.js
```

### Android
```bash
# Build APK
.\gradlew assembleDebug

# APK output: app/build/outputs/apk/debug/app-debug.apk
```

### Admin Panel
```bash
cd admin-panel
npm install
npm run dev
# Chay tai http://localhost:5173
```

### De test day du
1. Backend: node src/index.js (port 3000)
2. MySQL: Chay script SQL trong backend/src/database/schema.sql
3. MySQL: Chay seed data trong backend/src/database/seed.sql
4. Firebase: Tao project Firebase, lay credentials cho backend FCM
5. Android: Cai dat google-services.json (neu co Firebase) hoac build ma khong can FCM
6. Google Maps: Lay API key va them vao local.properties
7. Admin Panel: cd admin-panel && npm install && npm run dev (port 5173)

---

## Database Tables

| Table | Mo ta |
|-------|-------|
| users | Nguoi dung (khach hang, tai xe, admin) |
| drivers | Thong tin tai xe |
| rides | Chuyen di |
| transactions | Giao dich thanh toan |
| driver_locations | Vi tri tai xe real-time |
| chat_messages | Tin nhan chat |
| ai_trip_schedules | Lich trinh AI |
| ai_waypoints | Diem dung trong lich trinh AI |
| ai_route_alternatives | Phuong an tuyen duong |
| ai_learning_profiles | Ho so hoc tap AI |
| driver_route_batches | Chuyen gom (batch) |
| batch_passengers | Khach trong chuyen gom |
| user_fcm_tokens | FCM tokens |
| ride_rating_tags | Tags danh gia |
| cancellation_log | Log huy chuyen |
| ride_images | Hinh anh chuyen di |
| password_resets | OTP reset mat khau |

---

## API Endpoints

| Method | Endpoint | Mo ta |
|--------|----------|-------|
| POST | /api/auth/register | Dang ky |
| POST | /api/auth/login | Dang nhap |
| POST | /api/auth/fcm/register | Dang ky FCM token |
| POST | /api/auth/forgot-password | Gui OTP |
| POST | /api/auth/verify-otp | Xac minh OTP |
| POST | /api/auth/reset-password | Dat lai mat khau |
| POST | /api/auth/resend-otp | Gui lai OTP |
| GET | /api/users/me | Thong tin nguoi dung |
| GET | /api/users/drivers/nearby | Tim tai xe gan |
| POST | /api/rides/request | Dat xe |
| GET | /api/rides | Lich su chuyen di |
| GET | /api/rides/search | Tim kiem chuyen di |
| GET | /api/rides/active | Chuyen di dang hoat dong |
| GET | /api/rides/:id | Chi tiet chuyen di |
| PUT | /api/rides/:id/status | Cap nhat trang thai |
| PUT | /api/rides/:id/cancel | Huy chuyen di |
| POST | /api/rides/:id/rate | Danh gia |
| GET | /api/driver/profile | Profile tai xe |
| PUT | /api/driver/status | Trang thai online/offline |
| POST | /api/driver/ride/:id/accept | Nhan chuyen |
| GET | /api/driver/earnings | Thu nhap (chi tiet + chart) |
| POST | /api/ai/schedule/create | Tao lich trinh AI |
| POST | /api/ai/schedule/:id/optimize | Toi uu lich trinh |
| GET | /api/ai/recommendations | Goi y AI |
| GET | /api/ai/batch/available | Batch offers |
| GET | /api/chat/:rideId/messages | Tin nhan chat |
| POST | /api/chat/:rideId/send | Gui tin nhan |
| GET | /api/payments/methods | Phuong thuc thanh toan |
| POST | /api/payments/create | Tao payment |
| GET | /api/payments/history | Lich su thanh toan |
| GET | /api/admin/dashboard | Dashboard admin |
| GET | /api/admin/users | Quan ly nguoi dung |
| GET | /api/admin/rides | Quan ly chuyen di |
| GET | /api/admin/drivers | Quan ly tai xe |
| GET | /api/admin/stats/daily | Thong ke theo ngay |

---

## WebSocket Events

| Event | Huong | Mo ta |
|-------|-------|-------|
| location:update | Driver -> Server | Cap nhat vi tri tai xe |
| driver:location | Server -> Passenger | Vi tri tai xe real-time |
| ride:status | Driver -> Server | Cap nhat trang thai ride |
| ride:status:changed | Server -> Passenger | Thong bao trang thai |
| join:ride | Passenger -> Server | Tham gia phong ride |
| leave:ride | Passenger -> Server | Roi phong ride |
| chat:message | Server -> Recipient | Tin nhan chat moi |

---

## Quy uoc dat ten

### Dat ten nhanh
- CamelCase cho bien va ham (Kotlin/JavaScript)
- PascalCase cho class/struct (Kotlin/JS)
- snake_case cho database columns
- SCREAMING_SNAKE_CASE cho hang so (JS)

### Dat ten mang y nghia
- **Driver**: tai xe
- **Passenger**: khach hang
- **Ride**: chuyen di
- **Schedule**: lich trinh AI
- **Waypoint**: diem dung

### Trang thai ride
- pending: Cho tai xe nhan
- accepted: Tai xe da nhan
- arrived: Tai xe da den
- in_progress: Dang di
- completed: Da hoan thanh
- cancelled: Da huy

### Loai xe
- motorbike: Xe may
- car_4_seats: O to 4 cho
- car_7_seats: O to 7 cho

### Phuong thuc thanh toan
- cash: Tien mat
- wallet: Wallet
- vnpay: VNPay
- momo: MoMo
