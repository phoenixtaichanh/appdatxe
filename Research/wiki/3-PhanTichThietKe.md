# Phân tích và Thiết kế Hệ thống

---

## Mục lục

1. [Giới thiệu](#1-giới-thiệu)
2. [Biểu đồ lớp (Class Diagram)](#2-biểu-đồ-lớp-class-diagram)
3. [Biểu đồ trạng thái (State Diagram)](#3-biểu-đồ-trạng-thái-state-diagram)
4. [Biểu đồ hoạt động (Activity Diagram)](#4-biểu-đồ-hoạt-động-activity-diagram)
5. [Biểu đồ trình tự (Sequence Diagram)](#5-biểu-đồ-trình-tự-sequence-diagram)
6. [Kiến trúc hệ thống](#6-kiến-trúc-hệ-thống)
7. [Thiết kế bảo mật](#7-thiết-kế-bảo-mật)

---

## 1. Giới thiệu

Chương này trình bày quá trình **phân tích** và **thiết kế** hệ thống đặt xe thông minh kết hợp trợ lý du lịch AI. Các mô hình UML được sử dụng bao gồm:

| Loại sơ đồ | Mục đích |
|---|---|
| **Class Diagram** | Mô tả cấu trúc lớp và quan hệ giữa các đối tượng |
| **State Diagram** | Mô tả các trạng thái và chuyển đổi trạng thái |
| **Activity Diagram** | Mô tả luồng xử lý và hoạt động nghiệp vụ |
| **Sequence Diagram** | Mô tả tương tác theo thời gian giữa các đối tượng |

---

## 2. Biểu đồ lớp (Class Diagram)

### 2.1. Các lớp chính

```
┌─────────────────────────────┐
│           User              │
├─────────────────────────────┤
│ - id: int                   │
│ - email: string             │
│ - password: string (hash)    │
│ - name: string              │
│ - phone: string             │
│ - userType: enum            │
│ - rating: double            │
│ - totalRides: int           │
│ - createdAt: timestamp      │
└─────────────┬───────────────┘
              │
     ┌────────┴────────┐
     │                 │
┌────▼───────────┐ ┌───▼─────────────┐
│   Passenger    │ │     Driver      │
├───────────────┤ │├─────────────────┤
│ - userId: int │ │ - userId: int   │
│ - trips[]     │ │ - carModel      │
│ - payments[]   │ │ - carColor      │
│ - aiProfile    │ │ - licensePlate  │
└───────────────┘ │ - isAvailable    │
                  │ - latitude       │
                  │ - longitude      │
                  │ - currentRideId  │
                  │ - earnings[]     │
                  └─────────────────┘

┌─────────────────────────────┐
│           Ride              │
├─────────────────────────────┤
│ - id: int                   │
│ - passengerId: int          │
│ - driverId: int            │
│ - pickupLat/Lng: double    │
│ - pickupAddress: string    │
│ - destLat/Lng: double      │
│ - destAddress: string       │
│ - distanceKm: double       │
│ - durationMin: int          │
│ - price: double             │
│ - status: enum              │
│ - driverRating: int        │
│ - passengerRating: int     │
│ - ratingComment: string    │
│ - createdAt: timestamp      │
│ - startedAt: timestamp      │
│ - completedAt: timestamp    │
└─────────────┬───────────────┘
              │
     ┌────────┴────────┐
     │                 │
┌────▼───────────┐ ┌───▼─────────────┐
│    Payment     │ │    Rating       │
├───────────────┤ │├─────────────────┤
│ - id: int     │ │ - id: int       │
│ - rideId: int │ │ - rideId: int   │
│ - amount      │ │ - stars: int    │
│ - method      │ │ - comment       │
│ - status      │ │ - createdAt     │
│ - paidAt      │ └─────────────────┘
└───────────────┘

┌─────────────────────────────┐
│    AISchedule               │
├─────────────────────────────┤
│ - id: int                   │
│ - userId: int              │
│ - scheduleName: string     │
│ - scheduledDate: date      │
│ - totalTime: int           │
│ - totalPrice: double       │
│ - optimizationType: enum   │
│ - status: enum             │
│ - aiConfidenceScore        │
│ - waypoints[]              │
│ - routeAlternatives[]      │
└─────────────────────────────┘

┌─────────────────────────────┐
│    DriverBatch              │
├─────────────────────────────┤
│ - id: int                   │
│ - driverId: int            │
│ - batchName: string        │
│ - status: enum             │
│ - estimatedStartTime       │
│ - estimatedEndTime         │
│ - totalRevenue: double     │
│ - totalDistance: double    │
│ - passengerCount: int      │
│ - efficiencyScore: double  │
│ - passengers[]              │
└─────────────────────────────┘
```

### 2.2. Mối quan hệ giữa các lớp

| Lớp nguồn | Quan hệ | Lớp đích | Loại |
|---|---|---|---|
| `User` | 1:N | `Passenger` | Composition |
| `User` | 1:N | `Driver` | Composition |
| `User` | 1:N | `Ride` (passenger) | Association |
| `User` | 1:N | `Ride` (driver) | Association |
| `Ride` | 1:1 | `Payment` | Composition |
| `Ride` | 1:N | `Rating` | Association |
| `User` | 1:1 | `AIProfile` | Composition |
| `AIProfile` | 1:N | `AISchedule` | Association |
| `AISchedule` | 1:N | `Waypoint` | Composition |
| `AISchedule` | 1:N | `RouteAlternative` | Composition |
| `Driver` | 1:N | `DriverBatch` | Association |
| `DriverBatch` | 1:N | `BatchPassenger` | Composition |

---

## 3. Biểu đồ trạng thái (State Diagram)

### 3.1. Trạng thái chuyến đi (Ride)

```
                    ┌──────────┐
                    │ (START)  │
                    └────┬─────┘
                         │ createRide()
                         ▼
                  ┌──────────────┐
                  │   PENDING    │── cancelByUser() ──▶ ┌───────────┐
                  │  (Chờ tài xế) │                    │ CANCELLED │
                  └──────┬───────┘                    └───────────┘
                         │ acceptByDriver()
                         ▼
                  ┌──────────────┐
                  │   ACCEPTED   │── cancelByDriver() ──▶ ┌───────────┐
                  │ (Đã nhận xe) │                      │ CANCELLED │
                  └──────┬───────┘                      └───────────┘
                         │ driverArrives()
                         ▼
                  ┌──────────────┐
                  │   ARRIVED    │
                  │ (Tài xế đến)│
                  └──────┬───────┘
                         │ startRide()
                         ▼
                  ┌──────────────┐
                  │  IN_PROGRESS │
                  │  (Đang chạy) │
                  └──────┬───────┘
                         │ completeRide()
                         ▼
                  ┌──────────────┐
                  │  COMPLETED   │── rate() ──▶ ┌───────────┐
                  │ (Hoàn thành) │              │  (RATED)  │
                  └──────────────┘              └───────────┘
```

**Chi tiết trạng thái:**

| Trạng thái | Mô tả | Sự kiện chuyển đổi |
|---|---|---|
| `PENDING` | Yêu cầu đặt xe đã gửi, chờ tài xế | `acceptByDriver()` → `ACCEPTED` |
| `ACCEPTED` | Tài xế đã nhận chuyến, đang di chuyển đến | `driverArrives()` → `ARRIVED` |
| `ARRIVED` | Tài xế đã đến điểm đón | `startRide()` → `IN_PROGRESS` |
| `IN_PROGRESS` | Chuyến đi đang thực hiện | `completeRide()` → `COMPLETED` |
| `COMPLETED` | Chuyến đi đã hoàn thành | `rate()` → `RATED` |
| `CANCELLED` | Chuyến đi đã bị hủy | Bởi khách hoặc tài xế |

### 3.2. Trạng thái tài xế (Driver)

```
    ┌──────────────┐
    │   OFFLINE    │◀── goOffline()
    │  (Không hoạt │
    │    động)     │
    └──────┬───────┘
           │ goOnline()
           ▼
    ┌──────────────┐
    │   ONLINE     │─── hasActiveRide() ──▶ ┌──────────────┐
    │  (Sẵn sàng)  │                        │   BUSY       │
    └──────────────┘                         │ (Đang chạy)  │
                                           └──────┬───────┘
                                                  │ rideCompleted()
                                                  ▼
                                           ┌──────────────┐
                                           │   ONLINE     │
                                           │  (Sẵn sàng)  │
                                           └──────────────┘
```

### 3.3. Trạng thái thanh toán

```
    ┌──────────────┐
    │   PENDING    │── complete() ──▶ ┌──────────────┐
    │  (Chưa thanh │                 │   PAID       │
    │    toán)     │── fail() ──▶    │  (Đã thanh)  │
    └──────────────┘                 └──────────────┘
```

### 3.4. Trạng thái AI Schedule

```
    ┌──────────────┐
    │   PLANNED    │── start() ──▶ ┌──────────────┐
    │  (Đã lên lịch)│               │  IN_PROGRESS │
    └──────────────┘               └──────┬───────┘
                                         │ complete()
                                         ▼
                                  ┌──────────────┐
                                  │  COMPLETED   │
                                  └──────────────┘
```

---

## 4. Biểu đồ hoạt động (Activity Diagram)

### 4.1. Quy trình đặt xe

```
┌─────────────────────────────────────────────────────────┐
│                     BẮT ĐẦU                             │
└────────────────────────┬────────────────────────────────┘
                         │
                         ▼
              ┌───────────────────────┐
              │  Đăng nhập thành công │
              └───────────┬─────────────┘
                          │
                          ▼
               ┌────────────────────────┐
               │ Chọn điểm đón trên   │
               │ bản đồ                │
               └───────────┬────────────┘
                           │
                           ▼
               ┌────────────────────────┐
               │ Chọn điểm đến trên   │
               │ bản đồ                │
               └───────────┬────────────┘
                           │
                           ▼
               ┌────────────────────────┐
               │ Hệ thống tính toán:   │
               │ - Khoảng cách (km)     │
               │ - Thời gian (phút)    │
               │ - Giá dự kiến (đ)     │
               └───────────┬────────────┘
                           │
                           ▼
               ┌────────────────────────┐
               │ Chọn loại xe           │
               │ (xe máy/4 chỗ/7 chỗ)  │
               └───────────┬────────────┘
                           │
                           ▼
               ┌────────────────────────┐
               │ Nhấn nút "Đặt xe"     │
               └───────────┬────────────┘
                           │
                           ▼
               ┌────────────────────────┐
               │ Tìm tài xế gần nhất   │
               │ (bán kính 5km)        │
               └───────────┬────────────┘
                           │
                           ▼
               ┌────────────────────────┐
               │  Tài xế nhận chuyến?   │
               └───────────┬────────────┘
                    ┌─────┴─────┐
                    │           │
                 YES ▼        NO ▼
        ┌──────────────────┐  ┌──────────────────┐
        │ Thông báo khách   │  │ Tìm tài xế tiếp │
        │ Tài xế đã nhận    │  │ theo (lặp lại)  │
        └─────────┬────────┘  └─────────┬────────┘
                  │                     │
                  ▼                     │
        ┌──────────────────┐             │
        │ Tài xế đến điểm │             │
        │ đón               │             │
        └─────────┬────────┘             │
                  │                     │
                  ▼                     │
        ┌──────────────────┐             │
        │ Bắt đầu chuyến  │             │
        │ (IN_PROGRESS)    │             │
        └─────────┬────────┘             │
                  │                     │
                  ▼                     │
        ┌──────────────────┐             │
        │ Hoàn thành chuyến│             │
        │ (COMPLETED)       │             │
        └─────────┬────────┘             │
                  │                     │
                  ▼                     ▼
         ┌──────────────────────────────┐
         │         KẾT THÚC              │
         └──────────────────────────────────┘
```

### 4.2. Quy trình đăng nhập

```
┌─────────────────────────────────────┐
│            BẮT ĐẦU                  │
└────────────────┬────────────────────┘
                 │
                 ▼
      ┌──────────────────────┐
      │ Nhập email/số điện  │
      │ thoại và mật khẩu   │
      └──────────┬───────────┘
                 │
                 ▼
      ┌──────────────────────┐
      │ Tài khoản tồn tại?  │
      └──────────┬───────────┘
           ┌─────┴─────┐
          NO           YES
           │            │
           ▼            ▼
  ┌─────────────────┐  ┌────────────────────┐
  │ Hiển thị lỗi    │  │ Mật khẩu khớp?    │
  │ "Tài khoản      │  └────────┬───────────┘
  │ không tồn tại"  │       YES│        │NO
  └────────┬────────┘        │         ▼
           │                 │  ┌─────────────────┐
           │                 │  │ Hiển thị lỗi   │
           │                 │  │ "Sai mật khẩu" │
           │                 │  └────────┬────────┘
           │                 │           │
           │                 ▼           │
           │        ┌───────────────────┘
           │        │
           │        ▼
           │ ┌──────────────────────┐
           │ │ Đăng nhập thành công │
           │ │ - Tạo JWT token      │
           │ │ - Lưu session        │
           │ │ - Chuyển đến Home   │
           │ └──────────────────────┘
           │        │
           ▼        ▼
    ┌────────────────────┐
    │    KẾT THÚC        │
    └────────────────────┘
```

### 4.3. Quy trình trợ lý du lịch AI

```
┌────────────────────────────────────────┐
│              BẮT ĐẦU                   │
└─────────────────┬──────────────────────┘
                  │
                  ▼
       ┌─────────────────────┐
       │ Mở màn hình AI     │
       │ (Chat / Gợi ý)     │
       └──────────┬──────────┘
                  │
                  ▼
       ┌─────────────────────┐
       │ Nhập câu hỏi hoặc  │
       │ chọn gợi ý có sẵn  │
       └──────────┬──────────┘
                  │
                  ▼
       ┌─────────────────────┐
       │ AI phân tích yêu   │
       │ cầu người dùng     │
       └──────────┬──────────┘
                  │
                  ▼
       ┌─────────────────────┐
       │ Xác định loại gợi  │
       │ ý: Địa điểm / Lịch │
       │ trình / Dịch vụ    │
       └──────────┬──────────┘
                  │
        ┌─────────┼─────────┐
        │         │         │
        ▼         ▼         ▼
  ┌────────┐┌──────────┐┌──────────┐
  │ Địa điểm││Lịch trình││ Dịch vụ  │
  │ tham quan││du lịch   ││(khách sạn│
  │         ││          ││ nhà hàng)│
  └────┬───┘└────┬─────┘└────┬─────┘
       │          │            │
       ▼          ▼            ▼
  ┌─────────────────────────────────┐
  │ Trả về kết quả gợi ý + thao    │
  │ tác "Đặt xe đến địa điểm này" │
  └────────────────┬────────────────┘
                   │
                   ▼
           ┌────────────────┐
           │ KẾT THÚC      │
           └────────────────┘
```

### 4.4. Quy trình gom chuyến AI (Driver Batching)

```
┌─────────────────────────────────────────┐
│           BẮT ĐẦU (Hệ thống)            │
└─────────────────┬───────────────────────┘
                  │
                  ▼
      ┌──────────────────────────┐
      │ AI phân tích các chuyến  │
      │ đang chờ trong khu vực  │
      └────────────┬─────────────┘
                   │
                   ▼
      ┌──────────────────────────┐
      │ Tìm các chuyến có tuyến │
      │ đường gần nhau          │
      └────────────┬─────────────┘
                   │
                   ▼
      ┌──────────────────────────┐
      │ Tính toán hiệu quả:     │
      │ - Tổng doanh thu         │
      │ - Tổng khoảng cách       │
      │ - Độ lệch (detour)       │
      │ - Efficiency Score       │
      └────────────┬─────────────┘
                   │
                   ▼
      ┌──────────────────────────┐
      │ Đề xuất Batch cho tài xế │
      │ (PROPOSED status)        │
      └────────────┬─────────────┘
                   │
                   ▼
      ┌──────────────────────────┐
      │ Tài xế nhận/ Từ chối?   │
      └────────────┬─────────────┘
           ┌───────┴───────┐
          ACCEPT         REJECT
           │               │
           ▼               ▼
  ┌────────────────┐  ┌────────────────┐
  │ Batch: ACCEPTED│  │ Batch: REJECTED│
  │ Gán các chuyến │  │ Giữ nguyên    │
  │ vào batch      │  │ chuyến riêng  │
  └───────┬────────┘  └────────────────┘
          │
          ▼
  ┌────────────────┐
  │ Driver thực hiện│
  │ batch theo thứ tự│
  │ pickup/dropoff  │
  └───────┬────────┘
          │
          ▼
  ┌────────────────┐
  │ Batch COMPLETED │
  └────────────────┘
```

---

## 5. Biểu đồ trình tự (Sequence Diagram)

### 5.1. Sequence: Đặt xe

```
┌─────────┐   ┌──────────────┐   ┌─────────┐   ┌──────────┐   ┌────────┐
│Passenger│   │   App Android │   │ Backend │   │  MySQL   │   │ Driver │
└────┬────┘   └───────┬──────┘   └────┬────┘   └────┬─────┘   └───┬────┘
     │                │                │              │            │
     │ 1. Đặt xe      │                │              │            │
     │──────────────▶│                │              │            │
     │                │ 2. POST /rides │              │            │
     │                │───────────────▶│              │            │
     │                │                │ 3. INSERT ride│            │
     │                │                │─────────────▶│            │
     │                │                │◀─────────────│            │
     │                │ 4. return rideId              │            │
     │                │◀───────────────│              │            │
     │                │                │ 5. Tìm tài xế│            │
     │                │                │─────────────▶│            │
     │                │                │◀─────────────│            │
     │                │ 6. Thông báo   │              │            │
     │                │◀─────────────────────────────│───────────│
     │ 7. Trạng thái │                │              │            │
     │    "Đang tìm"  │                │              │            │
     │◀──────────────││              │              │            │
     │                │               │              │            │
     │                │               │◀──── 8. Driver nhận ────────│
     │                │ 9. PUT /rides │              │            │
     │                │◀───────────────│              │            │
     │                │               │ 10. UPDATE ride│           │
     │                │               │─────────────▶│            │
     │ 11. Tài xế đã│               │              │            │
     │    nhận       │               │              │            │
     │◀──────────────││              │              │            │
```

### 5.2. Sequence: AI Schedule Optimization

```
┌──────────┐   ┌──────────────┐   ┌─────────┐   ┌──────────┐
│ Passenger│   │  App Android │   │ Backend │   │   AI     │
└────┬─────┘   └───────┬──────┘   └───┬─────┘   └────┬─────┘
     │                │               │              │
     │ 1. Tạo lịch trình│            │              │
     │──────────────▶│ 2. POST /ai/schedules│      │
     │                │───────────────▶│ 3. Tạo schedule│
     │                │               │──────────────▶│
     │                │               │◀──────────────│
     │                │               │ 4. Tính route│
     │                │               │──────────────▶│
     │                │               │◀──────────────│
     │                │ 5. Route alternatives│       │
     │                │◀───────────────│              │
     │ 6. Hiển thị các│               │              │
     │    tuyến thay thế│             │              │
     │◀──────────────││              │              │
     │                │               │              │
     │ 7. Chọn tối ưu│               │              │
     │   (nhanh/rẻ)  │               │              │
     │──────────────▶│ 8. PUT /ai/schedules/optimize│
     │                │───────────────▶│ 9. Re-optimize│
     │                │               │──────────────▶│
     │                │◀───────────────│              │
     │ 10. Kết quả   │               │              │
     │    tối ưu     │               │              │
     │◀──────────────││              │              │
```

---

## 6. Kiến trúc hệ thống

### 6.1. Kiến trúc tổng thể (3-tier)

```
┌──────────────────────────────────────────────────────────────┐
│                        CLIENT LAYER                          │
│  ┌─────────────────────┐       ┌─────────────────────────┐  │
│  │  Android App        │       │   Web Admin Panel       │  │
│  │  (Passenger UI)     │       │   (Owner/Staff UI)      │  │
│  │  (Driver UI)        │       │                         │  │
│  └──────────┬──────────┘       └────────────┬────────────┘  │
│             │                                  │              │
│         HTTPS                              HTTPS            │
└─────────────┼──────────────────────────────────┼──────────────┘
              │                                  │
              │ REST API                          │ REST API
              ▼                                  ▼
┌─────────────────────────────────────────────────────────────┐
│                      SERVICE LAYER                           │
│  ┌─────────────────────────────────────────────────────┐    │
│  │                  Express.js Backend                  │    │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐         │    │
│  │  │ Auth API │  │ Ride API │  │ Driver API│         │    │
│  │  └──────────┘  └──────────┘  └──────────┘         │    │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐         │    │
│  │  │User API  │  │Location API│ │  AI API   │         │    │
│  │  └──────────┘  └──────────┘  └──────────┘         │    │
│  └─────────────────────────────────────────────────────┘    │
└────────────────────────────┬────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────┐
│                       DATA LAYER                            │
│  ┌─────────────────────────┐   ┌───────────────────────┐    │
│  │       MySQL 8.0          │   │   File Storage        │    │
│  │  - users                 │   │   - Profile images    │    │
│  │  - drivers               │   │   - Ride receipts      │    │
│  │  - rides                 │   │                       │    │
│  │  - payments              │   │                       │    │
│  │  - earnings              │   │                       │    │
│  │  - ai_* tables           │   │                       │    │
│  └─────────────────────────┘   └───────────────────────┘    │
└─────────────────────────────────────────────────────────────┘
```

### 6.2. Kiến trúc Android (MVVM + Clean Architecture)

```
┌─────────────────────────────────────────────────────────────┐
│                     PRESENTATION LAYER                       │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐          │
│  │   Screens   │  │   ViewModels │  │    State    │          │
│  │  (Compose)  │──│  (ViewModel) │──│  (UI State) │          │
│  └─────────────┘  └──────┬──────┘  └─────────────┘          │
└────────────────────────────┼────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────┐
│                       DOMAIN LAYER                           │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐        │
│  │  Use Cases  │  │  Repository │  │    Models    │        │
│  │ (Interactors)│ │ Interfaces  │  │  (Entities)  │        │
│  └─────────────┘  └──────┬──────┘  └─────────────┘        │
└────────────────────────────┼────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────┐
│                        DATA LAYER                           │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐        │
│  │  Repository │  │  Remote DTO │  │  Local Data  │        │
│  │  Impl       │──│  (Retrofit) │  │  (SharedPref)│        │
│  └─────────────┘  └─────────────┘  └─────────────┘        │
└─────────────────────────────────────────────────────────────┘
```

---

## 7. Thiết kế bảo mật

### 7.1. Các lớp bảo mật

| Lớp | Giải pháp | Mô tả |
|---|---|---|
| **Authentication** | JWT Token | Mỗi request phải có token hợp lệ |
| **Password** | bcrypt (cost factor 10) | Mật khẩu được hash trước khi lưu |
| **Input Validation** | Server-side validation | Kiểm tra dữ liệu đầu vào |
| **API Security** | Rate limiting | Giới hạn số request |
| **Data Protection** | HTTPS | Mã hóa dữ liệu truyền |
| **Role-based Access** | Middleware | Kiểm tra quyền trước khi xử lý |

### 7.2. Phân quyền API

```
┌────────────────────────────────────────┐
│           JWT Token Structure           │
├────────────────────────────────────────┤
│ {                                     │
│   "userId": 1,                        │
│   "email": "user@test.com",           │
│   "userType": "passenger",             │
│   "iat": 1704067200,                  │
│   "exp": 1704153600                   │
│ }                                     │
└────────────────────────────────────────┘

Middleware kiểm tra:
  ├─ isAuthenticated()  → Kiểm tra JWT hợp lệ
  ├─ isPassenger()      → Chỉ cho phép passenger
  ├─ isDriver()         → Chỉ cho phép driver
  ├─ isAdmin()          → Chỉ cho phép admin/owner
  └─ isStaff()          → Cho phép nhân viên
```

### 7.3. Các nhóm quyền

| Nhóm | Mã quyền | Mô tả |
|---|---|---|
| Guest | `ROLE_GUEST` | Người dùng chưa đăng nhập |
| Passenger | `ROLE_PASSENGER` | Khách hàng đã đăng nhập |
| Driver | `ROLE_DRIVER` | Tài xế đã đăng nhập |
| Consultant | `ROLE_CONSULTANT` | Nhân viên tư vấn |
| HR Manager | `ROLE_HR_MANAGER` | Nhân viên quản lý nhân sự |
| Revenue Manager | `ROLE_REVENUE_MANAGER` | Nhân viên quản lý doanh thu |
| Owner | `ROLE_OWNER` | Chủ doanh nghiệp |
| Admin | `ROLE_ADMIN` | Quản trị viên hệ thống |

---

## Liên kết

- [ Quay lại: Yêu cầu hệ thống](./2-YeuCauHeThong.md)
- [ Tiếp theo: Mô tả chức năng](./4-MoTaChucNang.md)
