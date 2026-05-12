# Thiết kế API

---

## Mục lục

1. [Tổng quan](#1-tổng-quan)
2. [Cấu trúc Response](#2-cấu-trúc-response)
3. [Authentication](#3-authentication)
4. [API Auth](#4-api-auth)
5. [API Users](#5-api-users)
6. [API Rides](#6-api-rides)
7. [API Drivers](#7-api-drivers)
8. [API Locations](#8-api-locations)
9. [API AI](#9-api-ai)
10. [API Driver (Batch)](#10-api-driver-batch)
11. [Mã lỗi](#11-mã-lỗi)

---

## 1. Tổng quan

API được xây dựng theo chuẩn **RESTful**, sử dụng giao thức **HTTP/HTTPS** và định dạng dữ liệu **JSON**.

### 1.1. Base URL

```
Development: http://localhost:3000/api
Production:  https://api.doan3.vn/api
```

### 1.2. HTTP Methods

| Method | Mục đích | Ví dụ |
|---|---|---|
| `GET` | Lấy dữ liệu | `GET /users/1` |
| `POST` | Tạo mới | `POST /rides` |
| `PUT` | Cập nhật toàn bộ | `PUT /users/1` |
| `PATCH` | Cập nhật một phần | `PATCH /rides/1/status` |
| `DELETE` | Xóa | `DELETE /ai/schedules/1` |

### 1.3. HTTP Headers

```
Content-Type: application/json
Authorization: Bearer <jwt_token>   (cho các API cần xác thực)
Accept: application/json
```

---

## 2. Cấu trúc Response

### 2.1. Response thành công

```json
{
    "success": true,
    "data": { ... },
    "message": "Operation successful"
}
```

### 2.2. Response lỗi

```json
{
    "success": false,
    "error": {
        "code": "AUTH001",
        "message": "Invalid credentials"
    }
}
```

### 2.3. Phân trang

```json
{
    "success": true,
    "data": [ ... ],
    "pagination": {
        "page": 1,
        "limit": 20,
        "total": 100,
        "totalPages": 5
    }
}
```

---

## 3. Authentication

### 3.1. JWT Token

**Header:** `Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`

**Payload:**
```json
{
    "userId": 1,
    "email": "user@test.com",
    "userType": "passenger",
    "iat": 1704067200,
    "exp": 1704153600
}
```

### 3.2. Middleware bảo mật

| Middleware | Mô tả |
|---|---|
| `authenticate` | Kiểm tra JWT token hợp lệ |
| `isPassenger` | Chỉ cho phép passenger |
| `isDriver` | Chỉ cho phép driver |
| `isAdmin` | Chỉ cho phép admin/owner |
| `isStaff` | Cho phép nhân viên |

---

## 4. API Auth

### 4.1. POST `/api/auth/register`

**Mô tả:** Đăng ký tài khoản mới

**Body:**
```json
{
    "email": "user@example.com",
    "password": "password123",
    "name": "Nguyen Van A",
    "phone": "0909123456",
    "userType": "passenger"
}
```

**Response (201):**
```json
{
    "success": true,
    "data": {
        "user": {
            "id": 1,
            "email": "user@example.com",
            "name": "Nguyen Van A",
            "userType": "passenger"
        },
        "token": "eyJhbGciOiJIUzI1NiIs..."
    }
}
```

### 4.2. POST `/api/auth/login`

**Mô tả:** Đăng nhập

**Body:**
```json
{
    "email": "user@example.com",
    "password": "password123"
}
```

**Response (200):**
```json
{
    "success": true,
    "data": {
        "user": {
            "id": 1,
            "email": "user@example.com",
            "name": "Nguyen Van A",
            "userType": "passenger",
            "rating": 5.0
        },
        "token": "eyJhbGciOiJIUzI1NiIs..."
    }
}
```

---

## 5. API Users

### 5.1. GET `/api/users/me`

**Mô tả:** Lấy thông tin người dùng hiện tại

**Headers:** `Authorization: Bearer <token>`

**Response (200):**
```json
{
    "success": true,
    "data": {
        "id": 1,
        "email": "user@example.com",
        "name": "Nguyen Van A",
        "phone": "0909123456",
        "userType": "passenger",
        "rating": 4.8,
        "totalRides": 15,
        "profileImage": "https://..."
    }
}
```

### 5.2. PUT `/api/users/me`

**Mô tả:** Cập nhật thông tin cá nhân

**Body:**
```json
{
    "name": "Nguyen Van B",
    "phone": "0912345678",
    "profileImage": "https://..."
}
```

---

## 6. API Rides

### 6.1. POST `/api/rides`

**Mô tả:** Tạo yêu cầu đặt xe mới

**Headers:** `Authorization: Bearer <token>` (Passenger)

**Body:**
```json
{
    "pickupLat": 10.7629,
    "pickupLng": 106.6604,
    "pickupAddress": "123 Nguyen Hue, Q1, HCMC",
    "destLat": 10.7800,
    "destLng": 106.7000,
    "destAddress": "456 Le Lai, Q1, HCMC",
    "vehicleType": "car_4_seats"
}
```

**Response (201):**
```json
{
    "success": true,
    "data": {
        "id": 1,
        "passengerId": 1,
        "pickupLat": 10.7629,
        "pickupLng": 106.6604,
        "destLat": 10.7800,
        "destLng": 106.7000,
        "distanceKm": 5.2,
        "durationMin": 15,
        "price": 36000,
        "status": "pending"
    }
}
```

### 6.2. GET `/api/rides`

**Mô tả:** Lấy lịch sử chuyến đi

**Headers:** `Authorization: Bearer <token>`

**Query params:**
- `?status=completed` - Lọc theo trạng thái
- `?page=1&limit=20` - Phân trang

**Response (200):**
```json
{
    "success": true,
    "data": [
        {
            "id": 1,
            "pickupAddress": "123 Nguyen Hue, Q1",
            "destAddress": "456 Le Lai, Q1",
            "distanceKm": 5.2,
            "price": 36000,
            "status": "completed",
            "driver": {
                "id": 2,
                "name": "Driver One",
                "phone": "0909234567",
                "carModel": "Toyota Camry",
                "rating": 4.8
            },
            "createdAt": "2026-01-15T10:30:00Z"
        }
    ],
    "pagination": {
        "page": 1,
        "limit": 20,
        "total": 50
    }
}
```

### 6.3. GET `/api/rides/:id`

**Mô tả:** Lấy chi tiết chuyến đi

**Response (200):**
```json
{
    "success": true,
    "data": {
        "id": 1,
        "passenger": { "id": 1, "name": "Nguyen Van A", "phone": "0909123456" },
        "driver": { "id": 2, "name": "Driver One", "phone": "0909234567" },
        "pickupLat": 10.7629,
        "pickupLng": 106.6604,
        "pickupAddress": "123 Nguyen Hue, Q1",
        "destLat": 10.7800,
        "destLng": 106.7000,
        "destAddress": "456 Le Lai, Q1",
        "distanceKm": 5.2,
        "durationMin": 15,
        "price": 36000,
        "status": "in_progress",
        "driverRating": 5,
        "createdAt": "2026-01-15T10:30:00Z",
        "startedAt": "2026-01-15T10:35:00Z"
    }
}
```

### 6.4. PATCH `/api/rides/:id/status`

**Mô tả:** Cập nhật trạng thái chuyến đi

**Body:**
```json
{
    "status": "in_progress"
}
```

**Giá trị status hợp lệ:** `accepted`, `arrived`, `in_progress`, `completed`, `cancelled`

### 6.5. POST `/api/rides/:id/rate`

**Mô tả:** Đánh giá chuyến đi

**Body:**
```json
{
    "rating": 5,
    "comment": "Tài xế rất thân thiện, xe sạch sẽ"
}
```

---

## 7. API Drivers

### 7.1. POST `/api/driver/accept/:rideId`

**Mô tả:** Tài xế nhận chuyến đi

**Headers:** `Authorization: Bearer <token>` (Driver)

**Response (200):**
```json
{
    "success": true,
    "data": {
        "rideId": 1,
        "status": "accepted",
        "message": "Bạn đã nhận chuyến thành công"
    }
}
```

### 7.2. POST `/api/driver/reject/:rideId`

**Mô tả:** Tài xế từ chối chuyến đi

### 7.3. PUT `/api/driver/status`

**Mô tả:** Cập nhật trạng thái online/offline

**Body:**
```json
{
    "isAvailable": true,
    "latitude": 10.7629,
    "longitude": 106.6604
}
```

### 7.4. PUT `/api/driver/location`

**Mô tả:** Cập nhật vị trí GPS của tài xế

**Body:**
```json
{
    "latitude": 10.7630,
    "longitude": 106.6610
}
```

### 7.5. GET `/api/driver/earnings`

**Mô tả:** Lấy thu nhập của tài xế

**Query params:**
- `?period=today|week|month|total`
- `?startDate=2026-01-01&endDate=2026-01-31`

**Response (200):**
```json
{
    "success": true,
    "data": {
        "total": 1500000,
        "rides": 25,
        "averagePerRide": 60000,
        "earnings": [
            { "date": "2026-01-15", "amount": 150000 },
            { "date": "2026-01-16", "amount": 200000 }
        ]
    }
}
```

---

## 8. API Locations

### 8.1. GET `/api/location/nearby-drivers`

**Mô tả:** Tìm tài xế gần điểm đón

**Query params:**
- `?lat=10.7629&lng=106.6604&radius=5`

**Response (200):**
```json
{
    "success": true,
    "data": [
        {
            "id": 2,
            "name": "Driver One",
            "carModel": "Toyota Camry",
            "carColor": "Đen",
            "licensePlate": "43A-123.45",
            "rating": 4.8,
            "distance": 1.2,
            "latitude": 10.7635,
            "longitude": 106.6610
        }
    ]
}
```

### 8.2. GET `/api/location/ride/:rideId/driver`

**Mô tả:** Lấy vị trí hiện tại của tài xế đang thực hiện chuyến

**Response (200):**
```json
{
    "success": true,
    "data": {
        "driverId": 2,
        "latitude": 10.7640,
        "longitude": 106.6620,
        "updatedAt": "2026-01-15T10:45:00Z"
    }
}
```

---

## 9. API AI

### 9.1. POST `/api/ai/schedules`

**Mô tả:** Tạo lịch trình AI mới

**Headers:** `Authorization: Bearer <token>`

**Body:**
```json
{
    "scheduleName": "Du lịch Đà Nẵng 2 ngày",
    "scheduledDate": "2026-02-15",
    "waypoints": [
        {
            "stopOrder": 1,
            "stopType": "pickup",
            "latitude": 16.0544,
            "longitude": 108.2022,
            "address": "Sân bay Đà Nẵng",
            "stopName": "Điểm khởi hành"
        },
        {
            "stopOrder": 2,
            "stopType": "stopover",
            "latitude": 15.9802,
            "longitude": 108.2677,
            "address": "Bãi Biển Mỹ Khê",
            "stopName": "Bãi Biển Mỹ Khê"
        },
        {
            "stopOrder": 3,
            "stopType": "dropoff",
            "latitude": 15.8877,
            "longitude": 108.3314,
            "address": "Phố cổ Hội An",
            "stopName": "Hội An"
        }
    ]
}
```

**Response (201):**
```json
{
    "success": true,
    "data": {
        "id": 1,
        "scheduleName": "Du lịch Đà Nẵng 2 ngày",
        "scheduledDate": "2026-02-15",
        "totalEstimatedTime": 240,
        "totalEstimatedPrice": 850000,
        "totalDistance": 45.5,
        "optimizationType": "balanced",
        "aiConfidenceScore": 0.92,
        "status": "planned",
        "waypoints": [...],
        "routeAlternatives": [
            {
                "id": 1,
                "routeName": "Tuyến nhanh nhất",
                "totalDistance": 42.0,
                "totalDuration": 210,
                "totalPrice": 780000,
                "isRecommended": true
            },
            {
                "id": 2,
                "routeName": "Tuyến rẻ nhất",
                "totalDistance": 45.5,
                "totalDuration": 240,
                "totalPrice": 720000,
                "isRecommended": false
            }
        ]
    }
}
```

### 9.2. GET `/api/ai/schedules`

**Mô tả:** Lấy danh sách lịch trình AI

**Query params:**
- `?status=planned|completed|cancelled`

### 9.3. PUT `/api/ai/schedules/:id/optimize`

**Mô tả:** Tối ưu lại lịch trình

**Body:**
```json
{
    "optimizationType": "cost"
}
```

**optimizationType:** `time` | `cost` | `balanced`

**Response (200):**
```json
{
    "success": true,
    "data": {
        "scheduleId": 1,
        "optimizationType": "cost",
        "totalEstimatedTime": 260,
        "totalEstimatedPrice": 720000,
        "totalDistance": 48.0,
        "aiConfidenceScore": 0.95,
        "routeAlternatives": [...]
    }
}
```

### 9.4. GET `/api/ai/route-alternatives/:scheduleId`

**Mô tả:** Lấy các tuyến đường thay thế

### 9.5. GET `/api/ai/profile`

**Mô tả:** Lấy hồ sơ học tập AI

**Response (200):**
```json
{
    "success": true,
    "data": {
        "userId": 1,
        "preferredTimeStart": "08:00:00",
        "preferredTimeEnd": "20:00:00",
        "averageTripDuration": 25.5,
        "averageTripCost": 65000,
        "totalDistanceTravelled": 450.5,
        "frequentLocations": [
            { "lat": 16.0544, "lng": 108.2022, "name": "Sân bay", "count": 12 }
        ],
        "preferenceCostVsTime": 0.7,
        "modelVersion": "v1.0"
    }
}
```

### 9.6. PUT `/api/ai/profile`

**Mô tả:** Cập nhật hồ sơ AI

**Body:**
```json
{
    "preferenceCostVsTime": 0.8,
    "preferredTimeStart": "09:00:00",
    "preferredTimeEnd": "21:00:00",
    "avoidLocations": [
        { "lat": 16.0678, "lng": 108.2100, "name": "Khu vực A" }
    ]
}
```

### 9.7. GET `/api/ai/recommendations`

**Mô tả:** Lấy gợi ý cá nhân hóa

**Response (200):**
```json
{
    "success": true,
    "data": {
        "recommendations": [
            {
                "type": "location",
                "title": "Có thể bạn muốn đến",
                "items": [
                    {
                        "name": "Bãi Biển Mỹ Khê",
                        "address": "Phước Mỹ, Sơn Trà, Đà Nẵng",
                        "estimatedPrice": 35000,
                        "similarUsers": 85
                    }
                ]
            },
            {
                "type": "frequent_route",
                "title": "Tuyến đường thường đi",
                "items": [
                    {
                        "from": "Nhà",
                        "to": "Công ty",
                        "averagePrice": 42000,
                        "count": 45
                    }
                ]
            }
        ],
        "potentialSavings": {
            "weekly": 250000,
            "monthly": 1000000,
            "tips": "Đặt xe vào giờ thấp điểm để tiết kiệm 20%"
        }
    }
}
```

### 9.8. GET `/api/ai/schedule-preview`

**Mô tả:** Xem trước lịch trình

**Body:**
```json
{
    "waypoints": [
        { "lat": 16.0544, "lng": 108.2022, "address": "Điểm A" },
        { "lat": 15.9802, "lng": 108.2677, "address": "Điểm B" }
    ],
    "optimizationType": "balanced"
}
```

---

## 10. API Driver (Batch)

### 10.1. GET `/api/driver/batches/available`

**Mô tả:** Lấy danh sách batch khả dụng cho tài xế

**Headers:** `Authorization: Bearer <token>` (Driver)

**Response (200):**
```json
{
    "success": true,
    "data": [
        {
            "id": 1,
            "batchName": "Batch sáng sớm - Khu vực 1",
            "status": "proposed",
            "passengerCount": 3,
            "totalRevenue": 185000,
            "totalDistance": 22.5,
            "efficiencyScore": 82,
            "aiConfidence": 0.88,
            "estimatedStartTime": "07:00:00",
            "estimatedEndTime": "08:30:00",
            "passengers": [
                {
                    "id": 1,
                    "name": "Khách A",
                    "pickupAddress": "123A Đường X",
                    "destAddress": "456 Đường Y",
                    "price": 55000
                }
            ]
        }
    ]
}
```

### 10.2. POST `/api/driver/batches/:batchId/accept`

**Mô tả:** Tài xế chấp nhận batch

### 10.3. POST `/api/driver/batches/:batchId/reject`

**Mô tả:** Tài xế từ chối batch

### 10.4. GET `/api/driver/batches`

**Mô tả:** Lấy lịch sử batch của tài xế

---

## 11. Mã lỗi

### 11.1. Authentication Errors (AUTH)

| Mã | HTTP | Mô tả |
|---|---|---|
| `AUTH001` | 401 | Email hoặc mật khẩu không đúng |
| `AUTH002` | 401 | Token không hợp lệ hoặc đã hết hạn |
| `AUTH003` | 403 | Không có quyền truy cập |
| `AUTH004` | 409 | Email đã tồn tại |
| `AUTH005` | 400 | Email không hợp lệ |

### 11.2. Ride Errors (RIDE)

| Mã | HTTP | Mô tả |
|---|---|---|
| `RIDE001` | 404 | Chuyến đi không tìm thấy |
| `RIDE002` | 400 | Trạng thái không hợp lệ |
| `RIDE003` | 400 | Không thể hủy chuyến đang thực hiện |
| `RIDE004` | 409 | Tài xế đang bận chuyến khác |
| `RIDE005` | 404 | Không tìm thấy tài xế gần đó |

### 11.3. AI Errors (AI)

| Mã | HTTP | Mô tả |
|---|---|---|
| `AI001` | 400 | Lịch trình cần ít nhất 2 điểm dừng |
| `AI002` | 400 | Loại tối ưu không hợp lệ |
| `AI003` | 404 | Lịch trình AI không tìm thấy |

### 11.4. General Errors (GEN)

| Mã | HTTP | Mô tả |
|---|---|---|
| `GEN001` | 500 | Lỗi server nội bộ |
| `GEN002` | 503 | Dịch vụ tạm thời không khả dụng |
| `GEN003` | 400 | Dữ liệu đầu vào không hợp lệ |

---

## Liên kết

- [ Quay lại: Thiết kế Cơ sở dữ liệu](./5-ThietKeCoSoDuLieu.md)
- [ Tiếp theo: Kế hoạch thực hiện](./7-KeHoachThucHien.md)
