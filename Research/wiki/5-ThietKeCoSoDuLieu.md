# Thiết kế Cơ sở Dữ liệu

---

## Mục lục

1. [Tổng quan](#1-tổng-quan)
2. [ERD](#2-erd)
3. [Mô tả các bảng](#3-mô-tả-các-bảng)
4. [Indexes cho hiệu năng](#4-indexes-cho-hiệu-năng)
5. [Ràng buộc (Constraints)](#5-ràng-buộc-constraints)
6. [Seed Data (Dữ liệu thử nghiệm)](#6-seed-data-dữ-liệu-thử-nghiệm)

---

## 1. Tổng quan

Cơ sở dữ liệu sử dụng **MySQL 8.0** với **11 bảng** (5 bảng core + 6 bảng AI). Thiết kế theo mô hình quan hệ chuẩn hóa **3NF** (Third Normal Form).

### 1.1. Tổ chức bảng

| Nhóm | Bảng | Mô tả |
|---|---|---|
| **Core - Users** | `users` | Thông tin người dùng (hành khách & tài xế) |
| **Core - Drivers** | `drivers` | Thông tin tài xế (xe, trạng thái online) |
| **Core - Rides** | `rides` | Thông tin chuyến đi |
| **Core - Tracking** | `driver_locations` | Vị trí real-time của tài xế |
| **Core - Revenue** | `earnings` | Thu nhập của tài xế |
| **AI - Schedules** | `ai_trip_schedules` | Lịch trình AI |
| **AI - Waypoints** | `ai_waypoints` | Điểm dừng trong lịch trình AI |
| **AI - Routes** | `ai_route_alternatives` | Các tuyến đường thay thế |
| **AI - Profiles** | `ai_learning_profiles` | Hồ sơ học tập AI theo người dùng |
| **AI - Batching** | `driver_route_batches` | Lịch trình gom chuyến cho tài xế |
| **AI - Batch Passengers** | `batch_passengers` | Hành khách trong lịch trình gom |

---

## 2. ERD

```
┌──────────────┐       ┌──────────────┐
│    users     │       │   drivers    │
├──────────────┤       ├──────────────┤
│ id (PK)     │──┐    │ id (PK)     │
│ email        │  │    │ user_id (FK)│◀─┘
│ password     │  └───▶│ car_model    │
│ name         │       │ car_color    │
│ phone        │       │ license_plate│
│ user_type    │       │ is_available │
│ rating       │       │ latitude     │
│ total_rides  │       │ longitude    │
└──────┬───────┘       └──────────────┘
       │                       │
       │ 1:N                   │ 1:1
       │                       │
       ▼                       ▼
┌──────────────┐       ┌──────────────────┐
│    rides      │       │ driver_locations  │
├──────────────┤       ├──────────────────┤
│ id (PK)     │       │ id (PK)          │
│ passenger_id │───────▶│ driver_id (FK)   │───┐
│ driver_id    │◀──────│ latitude          │   │
│ pickup_lat   │       │ longitude         │   │
│ pickup_lng   │       │ updated_at        │   │
│ dest_lat     │       └──────────────────┘   │
│ dest_lng     │                              │
│ distance_km  │       ┌──────────────┐       │
│ duration_min │       │  earnings    │       │
│ price        │       ├──────────────┤       │
│ status       │       │ id (PK)     │       │
│ driver_rating│       │ driver_id (FK)│◀─────┘
│ passenger_rating│     │ ride_id (FK) │
│ created_at   │       │ amount        │
└──────┬───────┘       │ created_at    │
       │               └──────────────┘
       │ 1:N
       ▼
┌──────────────────────────┐
│   ai_trip_schedules      │
├──────────────────────────┤
│ id (PK)                 │
│ user_id (FK)───────────▶│
│ schedule_name           │
│ scheduled_date           │
│ total_estimated_time    │
│ total_estimated_price   │
│ optimization_type       │
│ status                  │
│ ai_confidence_score     │
│ traffic_condition       │
└────────────┬────────────┘
             │ 1:N
             ▼
┌──────────────────────────┐
│     ai_waypoints        │
├──────────────────────────┤
│ id (PK)                 │
│ schedule_id (FK)        │
│ stop_order              │
│ stop_type               │
│ latitude                │
│ longitude               │
│ address                 │
│ stop_name               │
│ estimated_arrival       │
│ duration_min            │
│ is_optional             │
│ priority                │
└──────────────────────────┘

┌──────────────────────────┐
│  ai_route_alternatives  │
├──────────────────────────┤
│ id (PK)                 │
│ schedule_id (FK)        │
│ route_name              │
│ total_distance          │
│ total_duration          │
│ total_price             │
│ route_description       │
│ is_recommended          │
│ traffic_scenario        │
│ weather_impact          │
└──────────────────────────┘

┌──────────────────────────┐
│  ai_learning_profiles    │
├──────────────────────────┤
│ id (PK)                 │
│ user_id (FK)───────────▶│ UNIQUE
│ preferred_time_start    │
│ preferred_time_end      │
│ average_trip_duration   │
│ average_trip_cost       │
│ total_distance_travelled│
│ peak_hours_pattern (JSON)│
│ frequent_locations (JSON)│
│ preference_cost_vs_time  │
│ last_trip_hour_distrib  │
│ model_version           │
└──────────────────────────┘

┌──────────────────────────┐
│  driver_route_batches    │
├──────────────────────────┤
│ id (PK)                 │
│ driver_id (FK)─────────▶│
│ batch_name              │
│ status                  │
│ estimated_start_time    │
│ estimated_end_time      │
│ total_revenue           │
│ total_distance          │
│ passenger_count         │
│ efficiency_score        │
│ ai_confidence           │
│ accepted_at             │
│ completed_at            │
└────────────┬────────────┘
             │ 1:N
             ▼
┌──────────────────────────┐
│    batch_passengers      │
├──────────────────────────┤
│ id (PK)                 │
│ batch_id (FK)          │
│ passenger_id (FK)──────▶│
│ original_ride_id (FK)──▶│
│ pickup_order            │
│ dropoff_order          │
│ pickup_lat              │
│ pickup_lng              │
│ dropoff_lat             │
│ dropoff_lng             │
│ estimated_pickup_time   │
│ detour_km               │
│ price_adjustment        │
│ status                  │
└──────────────────────────┘
```

---

## 3. Mô tả các bảng

### 3.1. Bảng `users`

| Trường | Kiểu | Ràng buộc | Mô tả |
|---|---|---|---|
| `id` | INT | PRIMARY KEY, AUTO_INCREMENT | ID người dùng |
| `email` | VARCHAR(255) | UNIQUE, NOT NULL | Email đăng nhập |
| `password` | VARCHAR(255) | NOT NULL | Mật khẩu (bcrypt hash) |
| `name` | VARCHAR(255) | NOT NULL | Họ tên |
| `phone` | VARCHAR(20) | | Số điện thoại |
| `user_type` | ENUM | NOT NULL, DEFAULT 'passenger' | Loại: 'passenger', 'driver' |
| `profile_image` | VARCHAR(500) | | URL ảnh đại diện |
| `rating` | DOUBLE | DEFAULT 5.0 | Điểm đánh giá trung bình |
| `total_rides` | INT | DEFAULT 0 | Tổng số chuyến đã đi |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Thời gian tạo |
| `updated_at` | TIMESTAMP | ON UPDATE CURRENT_TIMESTAMP | Thời gian cập nhật |

### 3.2. Bảng `drivers`

| Trường | Kiểu | Ràng buộc | Mô tả |
|---|---|---|---|
| `id` | INT | PRIMARY KEY, AUTO_INCREMENT | ID hồ sơ tài xế |
| `user_id` | INT | FOREIGN KEY → users(id), NOT NULL | ID user tương ứng |
| `car_model` | VARCHAR(255) | | Model xe (VD: Toyota Camry) |
| `car_color` | VARCHAR(100) | | Màu xe (VD: Đen, Trắng) |
| `license_plate` | VARCHAR(20) | | Biển số xe |
| `is_available` | BOOLEAN | DEFAULT TRUE | Trạng thái online |
| `latitude` | DOUBLE | | Vĩ độ hiện tại |
| `longitude` | DOUBLE | | Kinh độ hiện tại |
| `current_ride_id` | INT | | ID chuyến đang thực hiện |

### 3.3. Bảng `rides`

| Trường | Kiểu | Ràng buộc | Mô tả |
|---|---|---|---|
| `id` | INT | PRIMARY KEY, AUTO_INCREMENT | ID chuyến đi |
| `passenger_id` | INT | FOREIGN KEY → users(id), NOT NULL | ID khách hàng |
| `driver_id` | INT | FOREIGN KEY → users(id) | ID tài xế |
| `pickup_lat` | DOUBLE | NOT NULL | Vĩ độ điểm đón |
| `pickup_lng` | DOUBLE | NOT NULL | Kinh độ điểm đón |
| `pickup_address` | VARCHAR(500) | NOT NULL | Địa chỉ điểm đón |
| `dest_lat` | DOUBLE | NOT NULL | Vĩ độ điểm đến |
| `dest_lng` | DOUBLE | NOT NULL | Kinh độ điểm đến |
| `dest_address` | VARCHAR(500) | NOT NULL | Địa chỉ điểm đến |
| `distance_km` | DOUBLE | NOT NULL | Khoảng cách (km) |
| `duration_min` | INT | NOT NULL | Thời gian ước tính (phút) |
| `price` | DOUBLE | NOT NULL | Giá chuyến đi (VNĐ) |
| `status` | ENUM | DEFAULT 'pending' | Trạng thái chuyến |
| `driver_rating` | INT | | Đánh giá của khách (1-5) |
| `passenger_rating` | INT | | Đánh giá của tài xế |
| `rating_comment` | VARCHAR(500) | | Nhận xét đánh giá |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Thời gian tạo |
| `started_at` | TIMESTAMP | NULL | Thời gian bắt đầu |
| `completed_at` | TIMESTAMP | NULL | Thời gian hoàn thành |

**ENUM `status`:**
- `'pending'` - Chờ tài xế nhận
- `'accepted'` - Tài xế đã nhận
- `'arrived'` - Tài xế đến điểm đón
- `'in_progress'` - Đang di chuyển
- `'completed'` - Hoàn thành
- `'cancelled'` - Bị hủy

### 3.4. Bảng `driver_locations`

| Trường | Kiểu | Ràng buộc | Mô tả |
|---|---|---|---|
| `id` | INT | PRIMARY KEY, AUTO_INCREMENT | ID bản ghi |
| `driver_id` | INT | FOREIGN KEY → users(id), NOT NULL | ID tài xế |
| `latitude` | DOUBLE | NOT NULL | Vĩ độ |
| `longitude` | DOUBLE | NOT NULL | Kinh độ |
| `updated_at` | TIMESTAMP | ON UPDATE CURRENT_TIMESTAMP | Thời gian cập nhật |

### 3.5. Bảng `earnings`

| Trường | Kiểu | Ràng buộc | Mô tả |
|---|---|---|---|
| `id` | INT | PRIMARY KEY, AUTO_INCREMENT | ID bản ghi |
| `driver_id` | INT | FOREIGN KEY → users(id), NOT NULL | ID tài xế |
| `ride_id` | INT | FOREIGN KEY → rides(id), NOT NULL | ID chuyến đi |
| `amount` | DOUBLE | NOT NULL | Số tiền thu nhập |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Thời gian ghi nhận |

### 3.6. Bảng `ai_trip_schedules`

| Trường | Kiểu | Ràng buộc | Mô tả |
|---|---|---|---|
| `id` | INT | PRIMARY KEY, AUTO_INCREMENT | ID lịch trình |
| `user_id` | INT | FOREIGN KEY → users(id), NOT NULL | ID người dùng |
| `schedule_name` | VARCHAR(255) | NOT NULL | Tên lịch trình |
| `scheduled_date` | DATE | NOT NULL | Ngày dự kiến |
| `total_estimated_time` | INT | | Tổng thời gian ước tính (phút) |
| `total_estimated_price` | DOUBLE | | Tổng giá ước tính |
| `total_distance` | DOUBLE | | Tổng khoảng cách (km) |
| `optimization_type` | ENUM | DEFAULT 'balanced' | Loại tối ưu |
| `status` | ENUM | DEFAULT 'planned' | Trạng thái |
| `ai_confidence_score` | DOUBLE | DEFAULT 0.0 | Độ tin cậy AI |
| `traffic_condition` | ENUM | DEFAULT 'normal' | Điều kiện giao thông |

**ENUM `optimization_type`:** `'time'`, `'cost'`, `'balanced'`

### 3.7. Bảng `ai_waypoints`

| Trường | Kiểu | Ràng buộc | Mô tả |
|---|---|---|---|
| `id` | INT | PRIMARY KEY, AUTO_INCREMENT | ID waypoint |
| `schedule_id` | INT | FOREIGN KEY → ai_trip_schedules(id), NOT NULL | ID lịch trình |
| `stop_order` | INT | NOT NULL | Thứ tự dừng |
| `stop_type` | ENUM | NOT NULL | Loại: pickup, dropoff, stopover |
| `latitude` | DOUBLE | NOT NULL | Vĩ độ |
| `longitude` | DOUBLE | NOT NULL | Kinh độ |
| `address` | VARCHAR(500) | NOT NULL | Địa chỉ |
| `stop_name` | VARCHAR(255) | | Tên địa điểm |
| `estimated_arrival` | TIME | | Giờ đến ước tính |
| `estimated_departure` | TIME | | Giờ rời đi ước tính |
| `duration_min` | INT | DEFAULT 0 | Thời gian dừng (phút) |
| `distance_from_prev` | DOUBLE | | Khoảng cách từ điểm trước |
| `estimated_price_segment` | DOUBLE | | Giá chặng này |
| `is_optional` | BOOLEAN | DEFAULT FALSE | Có bỏ qua được không |
| `priority` | INT | DEFAULT 0 | Độ ưu tiên (cao hơn = quan trọng hơn) |

### 3.8. Bảng `ai_route_alternatives`

| Trường | Kiểu | Ràng buộc | Mô tả |
|---|---|---|---|
| `id` | INT | PRIMARY KEY, AUTO_INCREMENT | ID tuyến |
| `schedule_id` | INT | FOREIGN KEY → ai_trip_schedules(id), NOT NULL | ID lịch trình |
| `route_name` | VARCHAR(255) | NOT NULL | Tên tuyến |
| `total_distance` | DOUBLE | NOT NULL | Tổng khoảng cách |
| `total_duration` | INT | NOT NULL | Tổng thời gian (phút) |
| `total_price` | DOUBLE | NOT NULL | Tổng giá |
| `route_description` | TEXT | | Mô tả tuyến đường |
| `is_recommended` | BOOLEAN | DEFAULT FALSE | Có phải tuyến gợi ý không |
| `traffic_scenario` | ENUM | DEFAULT 'typical' | Kịch bản giao thông |
| `weather_impact` | DOUBLE | DEFAULT 0.0 | Tác động của thời tiết |

### 3.9. Bảng `ai_learning_profiles`

| Trường | Kiểu | Ràng buộc | Mô tả |
|---|---|---|---|
| `id` | INT | PRIMARY KEY, AUTO_INCREMENT | ID hồ sơ |
| `user_id` | INT | FOREIGN KEY → users(id), UNIQUE, NOT NULL | ID người dùng |
| `preferred_time_start` | TIME | | Thời gian bắt đầu ưa thích |
| `preferred_time_end` | TIME | | Thời gian kết thúc ưa thích |
| `average_trip_duration` | DOUBLE | DEFAULT 0 | Thời gian trung bình chuyến đi |
| `average_trip_cost` | DOUBLE | DEFAULT 0 | Chi phí trung bình chuyến đi |
| `total_distance_travelled` | DOUBLE | DEFAULT 0 | Tổng khoảng cách đã đi |
| `peak_hours_pattern` | JSON | | Pattern giờ cao điểm |
| `frequent_locations` | JSON | | Các địa điểm thường đến |
| `avoid_locations` | JSON | | Các địa điểm nên tránh |
| `preference_cost_vs_time` | DOUBLE | DEFAULT 0.5 | Ưu tiên chi phí vs thời gian (0-1) |
| `last_trip_hour_distribution` | JSON | | Phân bố giờ đi gần đây |
| `model_version` | VARCHAR(50) | DEFAULT 'v1.0' | Phiên bản mô hình AI |
| `updated_at` | TIMESTAMP | ON UPDATE CURRENT_TIMESTAMP | Cập nhật lần cuối |

### 3.10. Bảng `driver_route_batches`

| Trường | Kiểu | Ràng buộc | Mô tả |
|---|---|---|---|
| `id` | INT | PRIMARY KEY, AUTO_INCREMENT | ID batch |
| `driver_id` | INT | FOREIGN KEY → users(id), NOT NULL | ID tài xế |
| `batch_name` | VARCHAR(255) | | Tên batch |
| `status` | ENUM | DEFAULT 'proposed' | Trạng thái |
| `estimated_start_time` | TIME | | Giờ bắt đầu ước tính |
| `estimated_end_time` | TIME | | Giờ kết thúc ước tính |
| `total_revenue` | DOUBLE | DEFAULT 0 | Tổng doanh thu batch |
| `total_distance` | DOUBLE | DEFAULT 0 | Tổng khoảng cách |
| `passenger_count` | INT | DEFAULT 0 | Số khách |
| `efficiency_score` | DOUBLE | DEFAULT 0 | Điểm hiệu quả (0-100) |
| `ai_confidence` | DOUBLE | DEFAULT 0 | Độ tin cậy AI |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Thời gian tạo |
| `accepted_at` | TIMESTAMP | NULL | Thời gian chấp nhận |
| `completed_at` | TIMESTAMP | NULL | Thời gian hoàn thành |

### 3.11. Bảng `batch_passengers`

| Trường | Kiểu | Ràng buộc | Mô tả |
|---|---|---|---|
| `id` | INT | PRIMARY KEY, AUTO_INCREMENT | ID bản ghi |
| `batch_id` | INT | FOREIGN KEY → driver_route_batches(id), NOT NULL | ID batch |
| `passenger_id` | INT | FOREIGN KEY → users(id), NOT NULL | ID khách |
| `original_ride_id` | INT | FOREIGN KEY → rides(id), NOT NULL | ID chuyến gốc |
| `pickup_order` | INT | NOT NULL | Thứ tự đón |
| `dropoff_order` | INT | NOT NULL | Thứ tự trả |
| `pickup_lat` | DOUBLE | NOT NULL | Vĩ độ điểm đón |
| `pickup_lng` | DOUBLE | NOT NULL | Kinh độ điểm đón |
| `dropoff_lat` | DOUBLE | NOT NULL | Vĩ độ điểm trả |
| `dropoff_lng` | DOUBLE | NOT NULL | Kinh độ điểm trả |
| `estimated_pickup_time` | TIME | NOT NULL | Giờ đón ước tính |
| `detour_km` | DOUBLE | DEFAULT 0 | Khoảng cách lệch thêm |
| `price_adjustment` | DOUBLE | DEFAULT 0 | Điều chỉnh giá |
| `status` | ENUM | DEFAULT 'pending' | Trạng thái |

---

## 4. Indexes cho hiệu năng

```sql
-- Tăng tốc tìm kiếm chuyến đi
CREATE INDEX idx_rides_passenger ON rides(passenger_id);
CREATE INDEX idx_rides_driver ON rides(driver_id);
CREATE INDEX idx_rides_status ON rides(status);

-- Tăng tốc tìm tài xế online
CREATE INDEX idx_drivers_available ON drivers(is_available);

-- Tăng tốc cập nhật vị trí tài xế
CREATE INDEX idx_driver_locations_driver ON driver_locations(driver_id);

-- Tăng tốc tìm lịch trình AI theo ngày
CREATE INDEX idx_ai_schedules_user_date ON ai_trip_schedules(user_id, scheduled_date);

-- Tăng tốc tìm batch theo trạng thái
CREATE INDEX idx_batches_driver_status ON driver_route_batches(driver_id, status);
```

---

## 5. Ràng buộc (Constraints)

### 5.1. Foreign Key Constraints

| Bảng | Trường | Tham chiếu | ON DELETE | ON UPDATE |
|---|---|---|---|---|
| `drivers` | `user_id` | `users(id)` | CASCADE | CASCADE |
| `rides` | `passenger_id` | `users(id)` | CASCADE | CASCADE |
| `rides` | `driver_id` | `users(id)` | SET NULL | CASCADE |
| `driver_locations` | `driver_id` | `users(id)` | CASCADE | CASCADE |
| `earnings` | `driver_id` | `users(id)` | CASCADE | CASCADE |
| `earnings` | `ride_id` | `rides(id)` | CASCADE | CASCADE |
| `ai_trip_schedules` | `user_id` | `users(id)` | CASCADE | CASCADE |
| `ai_waypoints` | `schedule_id` | `ai_trip_schedules(id)` | CASCADE | CASCADE |
| `ai_route_alternatives` | `schedule_id` | `ai_trip_schedules(id)` | CASCADE | CASCADE |
| `ai_learning_profiles` | `user_id` | `users(id)` | CASCADE | CASCADE |
| `driver_route_batches` | `driver_id` | `users(id)` | CASCADE | CASCADE |
| `batch_passengers` | `batch_id` | `driver_route_batches(id)` | CASCADE | CASCADE |
| `batch_passengers` | `passenger_id` | `users(id)` | CASCADE | CASCADE |
| `batch_passengers` | `original_ride_id` | `rides(id)` | CASCADE | CASCADE |

### 5.2. Business Rules

| Quy tắc | Mô tả |
|---|---|
| `BR001` | Một tài xế chỉ có thể nhận 1 chuyến tại một thời điểm |
| `BR002` | Chuyến đi chỉ có thể bị hủy khi status = 'pending' hoặc 'accepted' |
| `BR003` | Đánh giá chỉ được gửi khi status = 'completed' |
| `BR004` | AI schedule chỉ được tối ưu khi có ít nhất 2 waypoints |
| `BR005` | Batch chỉ được accept khi status = 'proposed' |
| `BR006` | Thời gian estimated_arrival phải > thời gian estimated_departure của điểm trước |
| `BR007` | passenger_count trong batch = số lượng batch_passengers |

---

## 6. Seed Data (Dữ liệu thử nghiệm)

### 6.1. Tài khoản test

| Email | Password | Họ tên | Loại | Ghi chú |
|---|---|---|---|---|
| `passenger@test.com` | `password123` | Test Passenger | Passenger | Tài khoản test hành khách |
| `driver1@test.com` | `password123` | Driver One | Driver | Tài xế test 1 |
| `driver2@test.com` | `password123` | Driver Two | Driver | Tài xế test 2 |

### 6.2. Dữ liệu tài xế

| Tên | Xe | Màu | Biển số | Vị trí |
|---|---|---|---|---|
| Driver One | Toyota Camry | Đen | 43A-123.45 | 10.7629, 106.6604 |
| Driver Two | Honda Civic | Trắng | 51B-678.90 | 10.7800, 106.7000 |

---

## Liên kết

- [ Quay lại: Mô tả chức năng](./4-MoTaChucNang.md)
- [ Tiếp theo: Thiết kế API](./6-ThietKeAPI.md)
