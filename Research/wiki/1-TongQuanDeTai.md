# 📋 Tổng Quan Đề Tài

> **Đề tài:** XÂY DỰNG HỆ THỐNG ĐẶT XE THÔNG MINH KẾT HỢP TRỢ LÝ DU LỊCH AI TRÊN NỀN TẢNG DI ĐỘNG
>
> **Nhóm thực hiện:**
> - **Lê Đăng Khoa** – 24IT119 – khoald.24it@vku.udn.vn
> - **Trần Nguyễn Tuấn Anh** – 24IT010 – anhtnt.24it@vku.udn.vn
>
> **Giảng viên hướng dẫn:** TS. Nguyễn Quang Vũ
>
> **Khoa:** Khoa học Máy tính – Trường ĐH Công nghệ Thông tin & Truyền thông Việt – Hàn (VKU)
>
> **Thời gian:** Tháng 03 – 06/2026

---

## Mục lục

1. [Tổng quan](#1-tổng-quan)
2. [Bối cảnh](#2-bối-cảnh)
3. [Mục tiêu đề tài](#3-mục-tiêu-đề-tài)
4. [Phạm vi đề tài](#4-phạm-vi-đề-tài)
5. [Công nghệ sử dụng](#5-công-nghệ-sử-dụng)
6. [Đối tượng sử dụng](#6-đối-tượng-sử-dụng)

---

## 1. Tổng quan

Đề tài **"Xây dựng hệ thống đặt xe thông minh kết hợp trợ lý du lịch AI trên nền tảng di động"** là một đồ án cơ sở 3 thuộc Khoa Khoa học Máy tính, trường Đại học Công nghệ Thông tin & Truyền thông Việt – Hàn. Đề tài tập trung phát triển một ứng dụng di động hỗ trợ người dùng đặt xe trực tuyến, theo dõi hành trình và đồng thời cung cấp các gợi ý du lịch thông minh thông qua trợ lý du lịch AI.

Điểm nổi bật của đề tài là sự kết hợp giữa **dịch vụ vận tải** và **trợ lý du lịch thông minh** trong cùng một nền tảng, giúp người dùng không chỉ đặt xe mà còn được hỗ trợ tìm kiếm địa điểm, lên kế hoạch lịch trình và khám phá các dịch vụ du lịch phù hợp.

---

## 2. Bối cảnh

### 2.1. Thực tiễn

Trong bối cảnh chuyển đổi số diễn ra mạnh mẽ tại Việt Nam, các ứng dụng di động thông minh ngày càng đóng vai trò quan trọng trong đời sống, đặc biệt trong lĩnh vực **vận tải** và **du lịch**. Sự phổ biến của điện thoại thông minh cùng Internet tốc độ cao đã thúc đẩy xu hướng sử dụng các ứng dụng đặt xe trực tuyến như Grab, Be, GoJek.

Bên cạnh đó, ngành du lịch cũng đang từng bước chuyển đổi số nhằm nâng cao trải nghiệm khách hàng. Nhu cầu tìm kiếm thông tin địa điểm du lịch, gợi ý lịch trình, lựa chọn phương tiện di chuyển ngày càng tăng cao.

### 2.2. Vấn đề tồn tại

Phần lớn các ứng dụng hiện nay chỉ tập trung vào một chức năng riêng lẻ như đặt xe **hoặc** hỗ trợ du lịch mà chưa tích hợp đồng bộ. Nhiều hệ thống còn tồn tại các hạn chế:

| Vấn đề | Mô tả |
|---|---|
| Khả năng cá nhân hóa thấp | Hệ thống chưa hiểu rõ sở thích, thói quen di chuyển của từng người dùng |
| Thiếu hỗ trợ tư vấn thông minh | Người dùng phải tự tìm kiếm thông tin thay vì được gợi ý phù hợp |
| Chưa tận dụng AI | Chưa áp dụng trí tuệ nhân tạo để nâng cao trải nghiệm |
| Tách biệt đặt xe và du lịch | Người dùng phải sử dụng nhiều ứng dụng riêng biệt |

### 2.3. Giải pháp đề xuất

Xuất phát từ thực tế trên, đề tài đề xuất xây dựng một hệ thống tích hợp **đặt xe** và **trợ lý du lịch AI**, giúp người dùng chỉ cần một ứng dụng duy nhất để:

- Đặt xe nhanh chóng (xe máy, ô tô 4 chỗ, ô tô 7 chỗ)
- Được gợi ý địa điểm du lịch phù hợp
- Lên lịch trình tối ưu với sự hỗ trợ của AI
- Theo dõi hành trình trên bản đồ theo thời gian thực

---

## 3. Mục tiêu đề tài

### 3.1. Mục tiêu chính

Xây dựng một hệ thống hoàn chỉnh bao gồm **ứng dụng di động** dành cho khách hàng và tài xế, cùng với **hệ thống quản trị dữ liệu** ở phía máy chủ.

### 3.2. Mục tiêu cụ thể

| # | Mục tiêu | Mô tả |
|---|---|---|
| 1 | Đặt xe thông minh | Cho phép người dùng đặt xe nhanh chóng qua giao diện thân thiện |
| 2 | Trợ lý du lịch AI | Gợi ý địa điểm tham quan, nhà hàng, khách sạn, lịch trình phù hợp |
| 3 | Định vị GPS & Bản đồ | Hỗ trợ xác định vị trí, theo dõi hành trình trên bản đồ số |
| 4 | Quản lý dữ liệu | Lưu trữ và quản lý thông tin người dùng, lịch sử chuyến đi |
| 5 | Thanh toán | Hỗ trợ nhiều phương thức thanh toán (tiền mặt, chuyển khoản) |
| 6 | Đánh giá | Người dùng có thể đánh giá tài xế và chuyến đi |

### 3.3. Kết quả dự kiến

- Ứng dụng di động đặt xe thông minh với các chức năng cơ bản (đăng ký, đăng nhập, đặt xe, theo dõi hành trình)
- Tích hợp trợ lý du lịch AI gợi ý địa điểm, nhà hàng, khách sạn
- Công nghệ bản đồ và định vị GPS tích hợp
- Cơ sở dữ liệu quản lý người dùng, chuyến đi và dịch vụ
- Nền tảng có thể tiếp tục phát triển và mở rộng trong tương lai

---

## 4. Phạm vi đề tài

### 4.1. Phạm vi chức năng

**Chức năng cốt lõi:**
- Đăng ký / Đăng nhập / Quên mật khẩu
- Đặt xe (xe máy, ô tô 4 chỗ, ô tô 7 chỗ)
- Quản lý chuyến đi (theo dõi, hủy, hoàn thành)
- Theo dõi hành trình trên bản đồ (GPS thời gian thực)
- Thanh toán (tiền mặt / chuyển khoản)
- Đánh giá và phản hồi
- Trợ lý du lịch AI
- Quản lý thông tin cá nhân

**Phạm vi quản trị:**
- Quản lý tài khoản người dùng
- Quản lý tài xế
- Quản lý doanh thu
- Thống kê báo cáo (theo ngày, tuần, tháng)
- Dashboard tổng quan hệ thống

### 4.2. Phạm vi công nghệ

- **Frontend:** Android (Kotlin + Jetpack Compose)
- **Backend:** Node.js + Express
- **Database:** MySQL 8.0
- **Giao tiếp:** RESTful API
- **Maps:** Google Maps API
- **AI:** Module trợ lý du lịch AI tích hợp

### 4.3. Phạm vi ngoài phạm vi

- Tích hợp thanh toán trực tuyến (VNPay, MoMo) *(hướng phát triển tương lai)*
- Phiên bản iOS
- Hệ thống web quản trị đầy đủ
- Tích hợp thông báo push notification

---

## 5. Công nghệ sử dụng

### 5.1. Android (Frontend)

| Công nghệ | Mô tả |
|---|---|
| **Kotlin 1.9+** | Ngôn ngữ chính thức được Google hỗ trợ cho Android, cú pháp hiện đại, ngắn gọn |
| **Jetpack Compose** | Thư viện UI toolkit giúp xây dựng giao diện declarative, hiện đại |
| **Android Studio** | IDE chính thức cho phát triển Android |
| **Hilt** | Dependency Injection framework |
| **Retrofit + OkHttp** | Networking library cho REST API |
| **Coroutines + Flow** | Xử lý bất đồng bộ và reactive programming |
| **Google Maps SDK** | Hiển thị bản đồ và định vị |
| **Material 3** | Thiết kế UI theo tiêu chuẩn Material Design |

### 5.2. Node.js (Backend)

| Công nghệ | Mô tả |
|---|---|
| **Node.js 18+** | JavaScript runtime mạnh mẽ, hiệu năng cao |
| **Express.js** | Web framework nhẹ, linh hoạt |
| **MySQL 8.0** | Hệ quản trị cơ sở dữ liệu quan hệ |
| **JWT** | JSON Web Token cho xác thực người dùng |
| **bcryptjs** | Mã hóa mật khẩu |
| **CORS** | Cross-Origin Resource Sharing |

### 5.3. Kiến trúc hệ thống

```
┌─────────────────────────┐
│   Ứng dụng Android       │
│   (Kotlin + Compose)     │
│   - Khách hàng App        │
│   - Tài xế App            │
└───────────┬───────────────┘
            │ REST API (JSON)
            ▼
┌─────────────────────────┐
│   Backend Node.js        │
│   (Express.js)            │
│   - API Routes            │
│   - Business Logic        │
│   - AI Module             │
└───────────┬───────────────┘
            │ SQL Queries
            ▼
┌─────────────────────────┐
│   MySQL Database         │
│   (doan3_db)             │
│   - users, drivers        │
│   - rides, earnings       │
│   - AI tables             │
└─────────────────────────┘
```

---

## 6. Đối tượng sử dụng

### 6.1. Tổng quan

Hệ thống phục vụ **6 nhóm đối tượng** chính, mỗi nhóm có các quyền hạn và chức năng riêng biệt.

### 6.2. Chi tiết từng nhóm

| Đối tượng | Vai trò | Mô tả |
|---|---|---|
| **Khách hàng** | Passenger | Người sử dụng chính, đặt xe và sử dụng dịch vụ du lịch |
| **Tài xế** | Driver | Người tiếp nhận và thực hiện chuyến đi |
| **Chủ doanh nghiệp** | Owner | Quản lý tổng thể hệ thống, doanh thu |
| **Nhân viên tư vấn** | Consultant | Hỗ trợ khách hàng, giải đáp thắc mắc |
| **Nhân viên quản lý nhân sự** | HR Manager | Quản lý tài khoản người dùng, tài xế |
| **Nhân viên quản lý doanh thu** | Revenue Manager | Theo dõi, tổng hợp doanh thu và báo cáo |

### 6.3. Quyền hạn từng nhóm

#### 👤 Khách hàng
- Đăng ký, đăng nhập tài khoản
- Đặt xe (xe máy, ô tô 4 chỗ, ô tô 7 chỗ)
- Theo dõi hành trình trên bản đồ
- Thanh toán chuyến đi
- Đánh giá tài xế
- Sử dụng trợ lý du lịch AI
- Xem lịch sử chuyến đi
- Gửi yêu cầu hỗ trợ

#### 🚗 Tài xế
- Đăng nhập, nhận chuyến xe
- Xác nhận / từ chối chuyến đi
- Cập nhật trạng thái chuyến đi
- Xem doanh thu cá nhân
- Nhận đánh giá từ khách hàng

#### 🏢 Chủ doanh nghiệp
- Dashboard tổng quan
- Quản lý tài xế và khách hàng
- Theo dõi doanh thu
- Xem báo cáo thống kê

#### 📞 Nhân viên tư vấn
- Tiếp nhận yêu cầu hỗ trợ
- Tư vấn thông tin dịch vụ
- Hỗ trợ xử lý sự cố

#### 👥 Nhân viên quản lý nhân sự
- Quản lý tài khoản người dùng
- Thêm, sửa, khóa tài khoản
- Gửi báo cáo lên chủ doanh nghiệp

#### 💰 Nhân viên quản lý doanh thu
- Theo dõi giao dịch
- Thống kê doanh thu (ngày, tuần, tháng, năm)
- Lập báo cáo tài chính

---

## Liên kết

- [2. Yêu cầu hệ thống](./2-YeuCauHeThong.md)
- [3. Phân tích thiết kế](./3-PhanTichThietKe.md)
- [4. Mô tả chức năng](./4-MoTaChucNang.md)
- [5. Thiết kế Cơ sở dữ liệu](./5-ThietKeCoSoDuLieu.md)
- [6. Thiết kế API](./6-ThietKeAPI.md)
- [7. Kế hoạch thực hiện](./7-KeHoachThucHien.md)
