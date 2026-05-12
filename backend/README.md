# DoAn3 Backend - Uber Clone API

Backend server cho ứng dụng Uber Clone, sử dụng **Node.js + Express + MySQL**.

## Yêu cầu

- **Node.js** >= 18.x
- **MySQL** >= 8.0

## Cách chạy

### 1. Cài đặt MySQL

**Cách 1: MySQL Local**
- Cài MySQL từ https://dev.mysql.com/downloads/mysql/
- Chạy MySQL service

**Cách 2: MySQL Cloud (Khuyến nghị - Không cần cài đặt)**

Đăng ký tài khoản miễn phí tại một trong các dịch vụ sau:

| Dịch vụ | Link | Free Tier |
|---------|------|-----------|
| **PlanetScale** | https://planetscale.com | 1 database, 1 branch |
| **Railway** | https://railway.app | $5 credit/tháng |
| **FreeSQL** | https://freesql.cloud | 10MB storage |

### 2. Setup Database

Sau khi có MySQL, chạy file `schema.sql`:

```bash
# Đăng nhập MySQL
mysql -u root -p

# Chạy schema
source src/database/schema.sql
```

Hoặc import file `schema.sql` qua phpMyAdmin / MySQL Workbench.

### 3. Cài đặt Backend

```bash
# Clone/Copy thư mục backend
cd backend

# Copy file môi trường
cp .env.example .env

# Chỉnh sửa .env với thông tin database của bạn
# Xem phần "Cấu hình .env" bên dưới

# Cài đặt dependencies
npm install

# Chạy server
npm start
```

Server sẽ chạy tại: `http://localhost:3000`

### 4. Cấu hình .env

```env
# MySQL Local
DB_HOST=localhost
DB_USER=root
DB_PASSWORD=your_password
DB_NAME=doan3_db
DB_PORT=3306

# MySQL Cloud (PlanetScale/Railway)
DB_HOST=your-cloud-host.aws.com
DB_USER=your-username
DB_PASSWORD=your-password
DB_NAME=doan3_db

# JWT Secret (thay đổi giá trị này!)
JWT_SECRET=your_secret_key_here

# Port
PORT=3000
```

## API Endpoints

### Authentication

| Method | Endpoint | Mô tả |
|--------|----------|-------|
| POST | `/api/auth/register` | Đăng ký tài khoản |
| POST | `/api/auth/login` | Đăng nhập |

### Users

| Method | Endpoint | Mô tả |
|--------|----------|-------|
| GET | `/api/users/:id` | Lấy thông tin user |
| PUT | `/api/users/:id` | Cập nhật user |
| GET | `/api/users/drivers/nearby` | Tìm tài xế gần |

### Rides

| Method | Endpoint | Mô tả |
|--------|----------|-------|
| POST | `/api/rides/request` | Đặt xe |
| GET | `/api/rides/:id` | Lấy thông tin chuyến đi |
| PUT | `/api/rides/:id/status` | Cập nhật trạng thái |
| GET | `/api/rides` | Lịch sử chuyến đi |
| POST | `/api/rides/:id/rate` | Đánh giá chuyến đi |

### Driver

| Method | Endpoint | Mô tả |
|--------|----------|-------|
| GET | `/api/driver/profile` | Lấy profile tài xế |
| PUT | `/api/driver/profile` | Cập nhật profile |
| PUT | `/api/driver/status` | Cập nhật trạng thái online/offline |
| GET | `/api/driver/ride/available` | Lấy danh sách chuyến đang chờ |
| POST | `/api/driver/ride/:id/accept` | Nhận chuyến đi |
| POST | `/api/driver/ride/:id/reject` | Từ chối chuyến đi |
| GET | `/api/driver/earnings` | Xem thu nhập |

### Location

| Method | Endpoint | Mô tả |
|--------|----------|-------|
| POST | `/api/location/update` | Cập nhật vị trí tài xế |
| GET | `/api/location/driver/:id` | Lấy vị trí tài xế |

## Test Accounts (sau khi chạy schema.sql)

| Loại | Email | Password |
|------|-------|----------|
| Passenger | passenger@test.com | password123 |
| Driver | driver1@test.com | password123 |
| Driver | driver2@test.com | password123 |

## Test API

```bash
# Health check
curl http://localhost:3000/api/health

# Register
curl -X POST http://localhost:3000/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"123456","name":"Test User","phone":"0909123456","user_type":"passenger"}'

# Login
curl -X POST http://localhost:3000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"passenger@test.com","password":"password123"}'
```

## Tích hợp với Android App

Trong `RetrofitClient.kt` và `AppModule.kt` của Android app, thay đổi `BASE_URL`:

- **Android Emulator**: `http://10.0.2.2:3000/api/`
- **Android Device cùng mạng**: `http://YOUR_PC_IP:3000/api/`

Để tìm IP của máy tính:
- Windows: `ipconfig` → IPv4 Address
- Mac/Linux: `ifconfig` → inet

## Các bước tiếp theo

Sau khi backend chạy thành công, tôi sẽ giúp bạn:
1. Tạo UI màn hình Login/Register
2. Tạo UI màn hình đặt xe (Passenger)
3. Tạo UI màn hình nhận chuyến (Driver)
4. Tích hợp bản đồ (Google Maps)
