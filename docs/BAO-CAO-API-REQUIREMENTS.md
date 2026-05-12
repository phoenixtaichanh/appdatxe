# Báo Cáo API Requirements

**Dự án:** Xây dựng hệ thống đặt xe thông minh kết hợp trợ lý du lịch AI  
**Ngày:** 2026-05-10  
**Trạng thái:** Hoàn thành

---

## Mục lục

1. [Tổng quan hệ thống](#1-tổng-quan-hệ-thống)
2. [Google API Requirements](#2-google-api-requirements)
3. [Firebase API Requirements](#3-firebase-api-requirements)
4. [Backend REST API Requirements](#4-backend-rest-api-requirements)
5. [WebSocket / Realtime API Requirements](#5-websocket--realtime-api-requirements)
6. [Third-party API Requirements khác](#6-third-party-api-requirements-khác)
7. [Danh sách Environment Variables](#7-danh-sách-environment-variables)
8. [Security Requirements](#8-security-requirements)

---

## 1. Tổng quan hệ thống

Dự án bao gồm 2 thành phần chính:

| Thành phần | Công nghệ | Vai trò |
|---|---|---|
| **Android App** | Kotlin + Jetpack Compose | Giao diện người dùng, gọi API |
| **Backend** | Node.js + Express | Xử lý logic, cơ sở dữ liệu, realtime |

**Kiến trúc ứng dụng:** Clean Architecture + MVVM

```
Android App ──HTTP/REST──> Backend API
Android App ──WebSocket──> Socket.IO Server
Android App ──Maps SDK──> Google Maps
Android App <──FCM──      Firebase Cloud Messaging
Backend     <──FCM SDK──  Firebase Admin SDK
```

---

## 2. Google API Requirements

### 2.1. Google Maps SDK (Android)

Google Maps là API bắt buộc, được sử dụng cho toàn bộ tính năng bản đồ và định vị.

#### Thông tin cấu hình hiện tại

| Trường | Giá trị |
|---|---|
| API Key | `AIzaSyB2uPnpGi9NDtk5dPIhnmMY-ZL8xoZoADo` |
| Nơi cấu hình | `local.properties` dòng `MAPS_API_KEY` |
| Console | [Google Cloud Console](https://console.cloud.google.com/google/maps-apis) |

#### API Endpoints bắt buộc bật trên Google Cloud

| API | Mục đích sử dụng trong dự án |
|---|---|
| **Maps SDK for Android** | Hiển thị bản đồ, vị trí tài xế, điểm đón/trả |
| **Maps SDK for Android - Gaming** | (tùy chọn, cho game hóa) |
| **Directions API** | Tính toán lộ trình, thời gian, khoảng cách |
| **Distance Matrix API** | Tính giá cước dựa trên quãng đường |
| **Geocoding API** | Chuyển đổi tọa độ ↔ địa chỉ |
| **Places API** | Tìm kiếm địa điểm, autocomplete địa chỉ |

#### Cách bật API trên Google Cloud Console

1. Truy cập [Google Cloud Console](https://console.cloud.google.com/)
2. Chọn project tương ứng
3. Vào **APIs & Services > Library**
4. Tìm và bật lần lượt các API trên
5. Vào **APIs & Services > Credentials**
6. Tạo **API Key** mới hoặc giới hạn cho các API đã bật

#### Ràng buộc API Key (khuyến nghị)

```
HTTP referrer restrictions:
  - *.doan3.vn/*
  - localhost/* (dev only)

API restrictions:
  ✓ Maps SDK for Android
  ✓ Directions API
  ✓ Distance Matrix API
  ✓ Geocoding API
  ✓ Places API
```

#### Tính năng sử dụng Maps trong code

| Tính năng | File | Mô tả |
|---|---|---|
| Hiển thị bản đồ | `ui/components/MapComponents.kt` | TaxiMapView composable |
| Marker tài xế | `ui/components/MapComponents.kt` | Hiển thị vị trí realtime |
| Polyline lộ trình | `ui/components/MapComponents.kt` | Vẽ đường đi |
| Camera di chuyển | `ui/components/MapComponents.kt` | Theo dõi tài xế |
| Điều hướng Intent | `ui/screens/*` | Mở Google Maps chỉ đường |

#### Chi phí ước tính (tháng)

| API | Miễn phí/tháng | Vượt quá |
|---|---|---|
| Maps SDK for Android | Không giới hạn khi đã bật | Không |
| Directions API | 40,000 requests | $5/1,000 |
| Distance Matrix API | 1,000 elements | $5/1,000 |
| Geocoding API | 40,000 requests | $5/1,000 |
| Places API | 150,000 requests | variable |

---

## 3. Firebase API Requirements

### 3.1. Firebase Cloud Messaging (FCM)

FCM được sử dụng cho **push notifications** từ backend gửi đến Android app.

#### Thông tin cấu hình

| Trường | Nơi cấu hình |
|---|---|
| `FIREBASE_PROJECT_ID` | `backend/.env` |
| `FIREBASE_CLIENT_EMAIL` | `backend/.env` |
| `FIREBASE_PRIVATE_KEY` | `backend/.env` |
| `google-services.json` | `app/google-services.json` (Android side) |

#### Cách lấy Firebase credentials

1. Truy cập [Firebase Console](https://console.firebase.google.com/)
2. Tạo project mới hoặc chọn project hiện có
3. Vào **Project Settings > Service accounts**
4. Click **Generate new private key**
5. Lưu file JSON và điền vào `backend/.env`:

```
FIREBASE_PROJECT_ID=your-project-id
FIREBASE_CLIENT_EMAIL=firebase-adminsdk-xxxxx@your-project.iam.gserviceaccount.com
FIREBASE_PRIVATE_KEY="-----BEGIN PRIVATE KEY-----\n...\n-----END PRIVATE KEY-----\n"
```

#### Cách lấy google-services.json (Android)

1. Firebase Console > **Project Settings**
2. Tab **General** > **Your apps** > Thêm Android app
3. Nhập package name: `com.laptrinhdidong.DoAn3`
4. Download `google-services.json`
5. Đặt vào `app/google-services.json`

#### Tính năng sử dụng FCM

| Sự kiện | Hướng | Mô tả |
|---|---|---|
| Tài xế nhận chuyến | Backend → Passenger | Thông báo có tài xế nhận chuyến |
| Tài xế đến nơi | Backend → Passenger | Thông báo tài xế đã đến |
| Chuyến đi hoàn thành | Backend → Passenger | Thông báo kết thúc chuyến |
| Yêu cầu đặt xe | Backend → Driver | Thông báo có khách đặt xe gần đó |
| Tin nhắn chat mới | Backend → User | Thông báo tin nhắn mới |

#### File sử dụng FCM

| Vai trò | File |
|---|---|
| Android FCM Service | `app/src/main/java/com/laptrinhdidong/DoAn3/service/DoAn3FCMService.kt` |
| Backend FCM Service | `backend/src/services/notification.js` |
| FCM Token Registration | `ApiService.kt` → `POST /auth/fcm/register` |

### 3.2. Firebase Realtime Database (tùy chọn mở rộng)

Hiện tại dự án dùng Socket.IO cho realtime, nhưng có thể mở rộng thêm Firebase Realtime Database cho:
- Lưu trữ vị trí tài xế offline
- Backup trạng thái chuyến đi

### 3.3. Firebase Analytics (tùy chọn)

Có thể thêm `firebase-analytics` dependency để theo dõi hành vi người dùng.

---

## 4. Backend REST API Requirements

### 4.1. Base URL

| Môi trường | URL |
|---|---|
| Development | `http://localhost:3000/api` |
| Production | `https://api.doan3.vn/api` |

### 4.2. Authentication

Tất cả API (trừ `/auth/*`) yêu cầu JWT token trong header:

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

### 4.3. Danh sách API Endpoints

#### 4.3.1. Auth APIs

| Method | Endpoint | Mô tả | Auth |
|---|---|---|---|
| `POST` | `/api/auth/register` | Đăng ký tài khoản | Không |
| `POST` | `/api/auth/login` | Đăng nhập | Không |
| `POST` | `/api/auth/fcm/register` | Đăng ký FCM token | Có |

#### 4.3.2. User APIs

| Method | Endpoint | Mô tả | Auth |
|---|---|---|---|
| `GET` | `/api/users/me` | Lấy thông tin user hiện tại | Có |
| `GET` | `/api/users/{id}` | Lấy thông tin user theo ID | Có |
| `PUT` | `/api/users/{id}` | Cập nhật thông tin user | Có |
| `GET` | `/api/users/drivers/nearby` | Tìm tài xế gần đó | Có |

#### 4.3.3. Ride APIs

| Method | Endpoint | Mô tả | Auth |
|---|---|---|---|
| `POST` | `/api/rides/request` | Tạo yêu cầu đặt xe | Passenger |
| `GET` | `/api/rides` | Lấy lịch sử chuyến đi | Có |
| `GET` | `/api/rides/{id}` | Lấy chi tiết chuyến đi | Có |
| `PUT` | `/api/rides/{id}/status` | Cập nhật trạng thái | Passenger |
| `POST` | `/api/rides/{id}/rate` | Đánh giá chuyến đi | Passenger |

#### 4.3.4. Driver APIs

| Method | Endpoint | Mô tả | Auth |
|---|---|---|---|
| `GET` | `/api/driver/profile` | Lấy hồ sơ tài xế | Driver |
| `PUT` | `/api/driver/profile` | Cập nhật hồ sơ | Driver |
| `PUT` | `/api/driver/status` | Cập nhật online/offline | Driver |
| `GET` | `/api/driver/ride/available` | Lấy chuyến khả dụng | Driver |
| `POST` | `/api/driver/ride/{id}/accept` | Nhận chuyến | Driver |
| `POST` | `/api/driver/ride/{id}/reject` | Từ chối chuyến | Driver |
| `PUT` | `/api/driver/ride/{id}/status` | Cập nhật trạng thái chuyến | Driver |
| `GET` | `/api/driver/earnings` | Lấy thu nhập | Driver |
| `GET` | `/api/driver/history` | Lịch sử chuyến đi | Driver |
| `GET` | `/api/driver/batches/available` | Lấy batch khả dụng | Driver |
| `POST` | `/api/driver/batches/{id}/accept` | Chấp nhận batch | Driver |
| `POST` | `/api/driver/batches/{id}/reject` | Từ chối batch | Driver |

#### 4.3.5. Location APIs

| Method | Endpoint | Mô tả | Auth |
|---|---|---|---|
| `POST` | `/api/location/update` | Cập nhật vị trí tài xế | Driver |
| `GET` | `/api/location/driver/{id}` | Lấy vị trí tài xế | Có |

#### 4.3.6. AI Schedule APIs

| Method | Endpoint | Mô tả | Auth |
|---|---|---|---|
| `POST` | `/api/ai/schedule/create` | Tạo lịch trình AI | Có |
| `GET` | `/api/ai/schedule/{id}` | Lấy chi tiết lịch trình | Có |
| `PUT` | `/api/ai/schedule/{id}` | Cập nhật lịch trình | Có |
| `GET` | `/api/ai/schedule/{id}/alternatives` | Lấy tuyến đường thay thế | Có |
| `POST` | `/api/ai/schedule/{id}/optimize` | Tối ưu lịch trình | Có |
| `GET` | `/api/ai/schedule-preview` | Xem trước lịch trình | Có |
| `GET` | `/api/ai/history` | Lịch sử lịch trình AI | Có |

#### 4.3.7. AI Profile APIs

| Method | Endpoint | Mô tả | Auth |
|---|---|---|---|
| `GET` | `/api/ai/profile` | Lấy hồ sơ học tập AI | Có |
| `PUT` | `/api/ai/profile` | Cập nhật hồ sơ AI | Có |
| `GET` | `/api/ai/recommendations` | Lấy gợi ý cá nhân hóa | Có |

#### 4.3.8. Chat APIs

| Method | Endpoint | Mô tả | Auth |
|---|---|---|---|
| `GET` | `/api/chat/{rideId}/messages` | Lấy tin nhắn | Có |
| `POST` | `/api/chat/{rideId}/send` | Gửi tin nhắn | Có |

### 4.4. Response Structure

#### Thành công
```json
{
    "success": true,
    "data": { ... },
    "message": "Operation successful"
}
```

#### Lỗi
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

| Mã | HTTP | Mô tả |
|---|---|---|
| `AUTH001` | 401 | Email hoặc mật khẩu không đúng |
| `AUTH002` | 401 | Token không hợp lệ hoặc đã hết hạn |
| `AUTH003` | 403 | Không có quyền truy cập |
| `AUTH004` | 409 | Email đã tồn tại |
| `RIDE001` | 404 | Chuyến đi không tìm thấy |
| `RIDE002` | 400 | Trạng thái không hợp lệ |
| `RIDE003` | 400 | Không thể hủy chuyến đang thực hiện |
| `RIDE004` | 409 | Tài xế đang bận chuyến khác |
| `RIDE005` | 404 | Không tìm thấy tài xế gần đó |
| `AI001` | 400 | Lịch trình cần ít nhất 2 điểm dừng |
| `AI002` | 400 | Loại tối ưu không hợp lệ |
| `AI003` | 404 | Lịch trình AI không tìm thấy |
| `GEN001` | 500 | Lỗi server nội bộ |
| `GEN002` | 503 | Dịch vụ tạm thời không khả dụng |
| `GEN003` | 400 | Dữ liệu đầu vào không hợp lệ |

---

## 5. WebSocket / Realtime API Requirements

Dự án sử dụng **Socket.IO** cho giao tiếp realtime.

### 5.1. Kết nối

| Trường | Giá trị |
|---|---|
| URL | Socket URL trong `AppConfig.kt` (cùng BASE_URL) |
| Path | `/socket.io/` |
| Auth | JWT token trong `socket.handshake.auth.token` |
| Transport | WebSocket (fallback: polling) |
| CORS | `origin: *` |

### 5.2. Socket Events

#### 5.2.1. Driver → Server

| Event | Payload | Mô tả |
|---|---|---|
| `location:update` | `{ lat: number, lng: number, rideId?: number }` | Gửi vị trí GPS realtime |
| `ride:status` | `{ rideId: number, status: string }` | Thay đổi trạng thái chuyến |

#### 5.2.2. Server → Passenger

| Event | Payload | Mô tả |
|---|---|---|
| `driver:location` | `{ lat: number, lng: number, rideId: number, timestamp: number }` | Vị trí tài xế realtime |
| `ride:status:changed` | `{ rideId: number, status: string, timestamp: number }` | Trạng thái chuyến thay đổi |

#### 5.2.3. Passenger → Server

| Event | Payload | Mô tả |
|---|---|---|
| `join:ride` | `rideId: number` | Tham gia phòng chuyến đi |
| `leave:ride` | `rideId: number` | Rời phòng chuyến đi |
| `request:driver:location` | `{ rideId: number }` | Yêu cầu vị trí tài xế |

### 5.3. Socket Rooms

| Room | Thành viên | Mô tả |
|---|---|---|
| `user_{userId}` | User cụ thể | Nhận thông báo cá nhân |
| `ride_{rideId}` | Passenger + Driver | Phòng chuyến đi |
| `drivers` | Tất cả tài xế | Broadcast đến tài xế |

### 5.4. File liên quan

| File | Mô tả |
|---|---|
| `app/data/remote/SocketManager.kt` | Socket.IO client (Android) |
| `backend/src/socket/index.js` | Socket.IO server (Node.js) |

---

## 6. Third-party API Requirements khác

### 6.1. MySQL Database

| Trường | Giá trị |
|---|---|
| Phiên bản | MySQL 8.0 |
| Host | `localhost` |
| Port | `3306` |
| Database | `doan3_db` |
| User | `root` |
| Password | `1234` |

Database schema gồm **11 bảng**: `users`, `drivers`, `rides`, `driver_locations`, `vehicles`, `ratings`, `ai_schedules`, `ai_profiles`, `ai_recommendations`, `chat_messages`, `user_fcm_tokens`.

### 6.2. JWT Secret

```
JWT_SECRET=DoAn3_KhoaTuanAnh_2026_JWT_Secret_Key_xK9mP2vL5nQ8jR4wT7yB3cF6hJ1mN4pL9sZ2aB5dE8fG1
```

### 6.3. Các thư viện bên thứ ba (Backend)

| Thư viện | Phiên bản | Mục đích |
|---|---|---|
| `express` | 4.21.2 | Web framework |
| `socket.io` | 4.8.3 | Realtime communication |
| `mysql2` | 3.12.0 | MySQL driver với Promise |
| `jsonwebtoken` | 9.0.2 | JWT authentication |
| `bcryptjs` | 2.4.3 | Password hashing |
| `cors` | 2.8.5 | Cross-origin resource sharing |
| `dotenv` | 16.4.7 | Environment variables |
| `firebase-admin` | (trong package.json) | FCM server-side |
| `express-validator` | (nếu có) | Input validation |

### 6.4. Các thư viện bên thứ ba (Android)

| Thư viện | Phiên bản | Mục đích |
|---|---|---|
| Retrofit | 2.9.0 | REST API client |
| OkHttp | 4.x | HTTP client |
| Socket.IO Client | 2.1.0 | Realtime client |
| Hilt | 2.51 | Dependency injection |
| Google Maps | maps + maps-compose | Bản đồ |
| Firebase Messaging | BOM managed | Push notifications |
| Kotlin Coroutines | 1.8.0 | Async programming |
| Compose BOM | 2024.02.00 | Jetpack Compose |

---

## 7. Danh sách Environment Variables

### 7.1. Backend (backend/.env)

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

# Firebase (bắt buộc cho push notifications)
FIREBASE_PROJECT_ID=your-project-id
FIREBASE_CLIENT_EMAIL=firebase-adminsdk-xxxxx@your-project.iam.gserviceaccount.com
FIREBASE_PRIVATE_KEY="-----BEGIN PRIVATE KEY-----\n...\n-----END PRIVATE KEY-----\n"
```

### 7.2. Android (local.properties)

```properties
sdk.dir=C\:/Users/LOQ/AppData/Local/Android/sdk
MAPS_API_KEY=AIzaSyB2uPnpGi9NDtk5dPIhnmMY-ZL8xoZoADo
```

### 7.3. Android (app/google-services.json)

Download từ Firebase Console, đặt tại `app/google-services.json`.

---

## 8. Security Requirements

### 8.1. API Security

| Yêu cầu | Mô tả |
|---|---|
| HTTPS | Bắt buộc trên production |
| JWT Token | Expiry 24h, lưu trong SharedPreferences |
| Authorization Headers | Bearer token cho tất cả protected routes |
| Role-based Access | Passenger, Driver, Admin phân biệt qua middleware |
| Input Validation | Server-side validation trên tất cả inputs |
| Rate Limiting | (Tùy chọn) Giới hạn request/s |

### 8.2. API Key Security

| API Key | Ràng buộc |
|---|---|
| Google Maps API Key | HTTP referrer + API restrictions |
| Firebase Private Key | Không commit vào git, chỉ trong .env |

### 8.3. CORS Configuration

```
Backend CORS:
  origin: * (development)
  origin: https://doan3.vn (production - khuyến nghị whitelist)
```

---

## 9. Checklist triển khai API

### Bắt buộc

- [ ] Bật **Google Maps SDK for Android** trên Cloud Console
- [ ] Bật **Directions API** trên Cloud Console
- [ ] Bật **Distance Matrix API** trên Cloud Console
- [ ] Bật **Geocoding API** trên Cloud Console
- [ ] Bật **Places API** trên Cloud Console
- [ ] Cấu hình **API Key restrictions** (HTTP referrer)
- [ ] Tạo **Firebase project**, generate **Service Account key**
- [ ] Điền Firebase credentials vào `backend/.env`
- [ ] Download `google-services.json`, đặt vào `app/`
- [ ] Cấu hình **MySQL 8.0**, chạy schema
- [ ] Tạo `.env` cho backend với `JWT_SECRET`
- [ ] Bật **HTTPS** trên production server

### Khuyến nghị

- [ ] Cấu hình rate limiting trên Express
- [ ] Thêm API key rotation mechanism
- [ ] Cấu hình CORS whitelist thay vì `*`
- [ ] Thêm request logging và monitoring
- [ ] Setup Firebase Analytics cho usage tracking
- [ ] Backup database thường xuyên

---

## 10. Tài liệu tham khảo

- [Google Maps Platform Documentation](https://developers.google.com/maps/documentation)
- [Firebase Cloud Messaging Documentation](https://firebase.google.com/docs/cloud-messaging)
- [Socket.IO Documentation](https://socket.io/docs/v4/)
- [Retrofit Documentation](https://square.github.io/retrofit/)
- [Express.js API Reference](https://expressjs.com/en/api.html)
