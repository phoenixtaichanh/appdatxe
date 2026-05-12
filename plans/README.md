# Feature Plan - DoAn3 Taxi App

> **De tai:** Xay dung he thong dat xe thong minh ket hop tro ly du lich AI

> **Trang thai du an:** Backend 100% | Android 100% | BUILD SUCCESSFUL (2026-05-10)

---

## Muc luc

| # | Feature Plan | Mo ta |
|---|---|---|
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
|---|---|---|
| Auth (Register/Login) | ✅ | ✅ |
| Dat xe (tao ride) | ✅ | ✅ |
| Tim tai xe gan | ✅ | ✅ |
| Tai xe nhan chuyen | ✅ | ✅ |
| Cap nhat trang thai ride | ✅ | ✅ |
| Danh gia sao | ✅ | ✅ |
| Xem lich su chuyen di | ✅ | ✅ |
| Thu nhap tai xe | ✅ | ✅ |
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
| Payment method selection | N/A | ✅ |
| Enhanced rating with tags | N/A | ✅ |
| History search | N/A | ✅ |

### Can hoan thien them

| Feature | Trang thai |Uu tien |
|---|---|---|
| Thanh toan thuc (VNPay/Momo) | Chua implement | Thap |
| Admin Panel | Chua implement | Thap |
| OTP / Quen mat khau | Chua implement | Trung binh |
| Ride cancellation policy | Chua implement | Trung binh |

---

## Da fix trong session nay

### Backend (2026-05-10)
- Dynamic pricing voi 3 loai xe (xe may, 4 cho, 7 cho)
- Vehicle type trong rides table
- WebSocket server (Socket.IO) cho real-time tracking
- FCM notification service
- Chat API (chat messages, send/receive)
- Notification khi ride status thay doi
- **Fix socket/index.js:** `getIO()` tra ve undefined (runtime crash) -> them `ioInstance` global variable
- **Fix locations.js:** SyntaxError dau ngoac thua `)` -> xoa dau thua

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
- **Fix AppModule.kt:** baseUrl hardcoded -> AppConfig.BASE_URL
- **Fix Theme.kt:** Deprecated statusBarColor -> xoa dong set
- **Fix 6 deprecated Icons.Default -> Icons.AutoMirrored.Filled**

### Build Result (2026-05-10)
- Android Clean Build: **BUILD SUCCESSFUL** (43 tasks, ~1 phut)
- APK: `app-debug.apk` - **19.55 MB**
- Android Warnings: **0**
- Android Lint: **0 errors**
- Backend JS Syntax (20 files): **Tat ca OK**

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

### De test day du
1. Backend: node src/index.js (port 3000)
2. MySQL: Chay script SQL trong backend/database/schema.sql
3. Firebase: Tao project Firebase, lay credentials cho backend FCM
4. Android: Cai dat google-services.json (neu co Firebase) hoac build ma khong can FCM
5. Google Maps: Lay API key va them vao local.properties

---

## Database Tables

| Table | Mo ta |
|---|---|
| users | Nguoi dung (khach hang, tai xe) |
| drivers | Thong tin tai xe |
| rides | Chuyen di |
| transactions | Giao dich thanh toan |
| support_conversations | Cuoc tro chuyen ho tro |
| support_messages | Tin nhan ho tro |
| addresses | Dia chi da luu |
| ai_schedules | Lich trinh AI |
| ai_route_alternatives | Phuong an duong di |
| chat_messages | Tin nhan chat |
| driver_locations | Vi tri tai xe |
| user_fcm_tokens | FCM tokens |

---

## API Endpoints

| Method | Endpoint | Mo ta |
|---|---|---|
| POST | /api/auth/register | Dang ky |
| POST | /api/auth/login | Dang nhap |
| POST | /api/auth/fcm/register | Dang ky FCM token |
| GET | /api/users/me | Thong tin nguoi dung |
| GET | /api/users/drivers/nearby | Tim tai xe gan |
| POST | /api/rides/request | Dat xe |
| GET | /api/rides/:id | Chi tiet chuyen di |
| PUT | /api/rides/:id/status | Cap nhat trang thai |
| POST | /api/rides/:id/rate | Danh gia |
| GET | /api/driver/profile | Profile tai xe |
| PUT | /api/driver/status | Trang thai online/offline |
| POST | /api/driver/ride/:id/accept | Nhan chuyen |
| GET | /api/driver/earnings | Thu nhap |
| POST | /api/ai/schedule/create | Tao lich trinh AI |
| POST | /api/ai/schedule/:id/optimize | Toi uu lich trinh |
| GET | /api/ai/recommendations | Goi y AI |
| GET | /api/ai/batch/available | Batch offers |
| GET | /api/chat/:rideId/messages | Tin nhan chat |
| POST | /api/chat/:rideId/send | Gui tin nhan |

---

## WebSocket Events

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
