# 📚 Wiki - Tài liệu Đồ Án 3

> **Đề tài:** XÂY DỰNG HỆ THỐNG ĐẶT XE THÔNG MINH KẾT HỢP TRỢ LÝ DU LỊCH AI TRÊN NỀN TẢNG DI ĐỘNG

> **Nhóm thực hiện:** Lê Đăng Khoa (24IT119) & Trần Nguyễn Tuấn Anh (24IT010)
>
> **Giảng viên hướng dẫn:** TS. Nguyễn Quang Vũ
>
> **Khoa:** Khoa học Máy tính - Trường ĐH Công nghệ Thông tin & Truyền thông Việt - Hàn (VKU)

---

## 📖 Mục lục tài liệu

| # | File | Mô tả |
|---|---|---|
| 1 | [1-TongQuanDeTai.md](./1-TongQuanDeTai.md) | Tổng quan đề tài, bối cảnh, mục tiêu, công nghệ |
| 2 | [2-YeuCauHeThong.md](./2-YeuCauHeThong.md) | Yêu cầu chức năng, yêu cầu phi chức năng, Use Case |
| 3 | [3-PhanTichThietKe.md](./3-PhanTichThietKe.md) | Phân tích & thiết kế: Class Diagram, State Diagram, Activity, Sequence |
| 4 | [4-MoTaChucNang.md](./4-MoTaChucNang.md) | Mô tả chi tiết màn hình và luồng nghiệp vụ |
| 5 | [5-ThietKeCoSoDuLieu.md](./5-ThietKeCoSoDuLieu.md) | Thiết kế Database: ERD, 11 bảng, indexes, seed data |
| 6 | [6-ThietKeAPI.md](./6-ThietKeAPI.md) | Thiết kế RESTful API: Endpoints, request/response, error codes |
| 7 | [7-KeHoachThucHien.md](./7-KeHoachThucHien.md) | Kế hoạch thực hiện 10 tuần, deliverables, rủi ro |

---

## 🗂️ Cấu trúc tài liệu

```
wiki/
├── README.md                      ← Trang chủ wiki (file này)
├── 1-TongQuanDeTai.md            ← Chương 1: Tổng quan
├── 2-YeuCauHeThong.md            ← Chương 2: Yêu cầu hệ thống
├── 3-PhanTichThietKe.md          ← Chương 3: Phân tích & Thiết kế
├── 4-MoTaChucNang.md            ← Chương 4: Mô tả chức năng
├── 5-ThietKeCoSoDuLieu.md        ← Chương 5: Thiết kế CSDL
├── 6-ThietKeAPI.md               ← Chương 6: Thiết kế API
├── 7-KeHoachThucHien.md         ← Chương 7: Kế hoạch thực hiện
├── extract_docs.py                ← Script trích xuất nội dung docx
└── docs_raw.txt                  ← Nội dung thô trích xuất từ 2 file docx gốc
```

---

## 🎯 Tóm tắt đề tài

### Mục tiêu
Xây dựng hệ thống đặt xe thông minh kết hợp trợ lý du lịch AI trên nền tảng di động, cho phép người dùng đặt xe, theo dõi hành trình và nhận gợi ý du lịch thông minh.

### Công nghệ

| Thành phần | Công nghệ |
|---|---|
| **Frontend** | Kotlin + Jetpack Compose + Hilt + Retrofit |
| **Backend** | Node.js + Express.js |
| **Database** | MySQL 8.0 |
| **Maps** | Google Maps API |
| **AI** | Module trợ lý du lịch AI tích hợp |
| **Auth** | JWT + bcrypt |

### Các bảng database (11 bảng)

**Core (5 bảng):**
- `users` - Người dùng
- `drivers` - Hồ sơ tài xế
- `rides` - Chuyến đi
- `driver_locations` - Vị trí tài xế (real-time)
- `earnings` - Thu nhập tài xế

**AI (6 bảng):**
- `ai_trip_schedules` - Lịch trình AI
- `ai_waypoints` - Điểm dừng trong lịch trình
- `ai_route_alternatives` - Tuyến đường thay thế
- `ai_learning_profiles` - Hồ sơ học tập AI
- `driver_route_batches` - Lịch trình gom chuyến
- `batch_passengers` - Hành khách trong batch

### Tài khoản test

| Email | Password | Loại |
|---|---|---|
| `passenger@test.com` | `password123` | Passenger |
| `driver1@test.com` | `password123` | Driver |
| `driver2@test.com` | `password123` | Driver |

---

## 📊 Thông tin dự án

| Thông tin | Chi tiết |
|---|---|
| **Ngày nộp** | Tháng 06/2026 |
| **Thời gian thực hiện** | 10 tuần (19/03 - 27/05/2026) |
| **Số thành viên** | 2 |
| **Trường** | ĐH Công nghệ Thông tin & Truyền thông Việt - Hàn |
| **Môn học** | Đồ án Cơ sở 3 |

---

## 📝 Nguồn tài liệu

Các file tài liệu gốc nằm trong thư mục `../Research/`:

- `KHMT - Báo Cáo Đồ Án 3 - LÊ ĐĂNG KHOA - TRẦN NGUYỄN TUẤN ANH.docx` - Báo cáo đồ án đầy đủ (38MB)
- `Đề Cương Chi Tiết.docx` - Đề cương chi tiết đề tài

---

*Wiki được tạo tự động từ nội dung các file docx gốc.*
