# Yêu cầu hệ thống

---

## Mục lục

1. [Giới thiệu](#1-giới-thiệu)
2. [Yêu cầu chức năng](#2-yêu-cầu-chức-năng)
3. [Yêu cầu phi chức năng](#3-yêu-cầu-phi-chức-năng)
4. [Các tác nhân (Actors)](#4-các-tác-nhân-actors)
5. [Sơ đồ Use Case](#5-sơ-đồ-use-case)

---

## 1. Giới thiệu

Chương này trình bày các **yêu cầu chức năng** và **yêu cầu phi chức năng** của hệ thống đặt xe thông minh kết hợp trợ lý du lịch AI. Các yêu cầu được phân tích dựa trên đặc điểm hoạt động của mô hình đặt xe trực tuyến kết hợp trợ lý du lịch AI, đảm bảo ứng dụng đáp ứng đúng mục tiêu sử dụng của từng nhóm đối tượng.

---

## 2. Yêu cầu chức năng

### 2.1. Nhóm khách hàng

#### 2.1.1. Chức năng không cần đăng nhập

| Mã | Tên chức năng | Mô tả |
|---|---|---|
| KH-001 | Xem giới thiệu | Xem màn hình giới thiệu ứng dụng |
| KH-002 | Xem thông tin dịch vụ | Xem thông tin tổng quan về dịch vụ |
| KH-003 | Truy cập đăng ký/đăng nhập | Truy cập chức năng đăng ký hoặc đăng nhập |

#### 2.1.2. Chức năng đăng ký / đăng nhập

| Mã | Tên chức năng | Mô tả |
|---|---|---|
| KH-010 | Đăng ký tài khoản | Tạo tài khoản mới với thông tin cá nhân |
| KH-011 | Đăng nhập | Đăng nhập bằng email hoặc số điện thoại |
| KH-012 | Quên mật khẩu | Khôi phục mật khẩu qua email/SMS |
| KH-013 | Cập nhật thông tin | Chỉnh sửa thông tin cá nhân |
| KH-014 | Đăng xuất | Đăng xuất khỏi ứng dụng |

#### 2.1.3. Chức năng đặt xe

| Mã | Tên chức năng | Mô tả |
|---|---|---|
| KH-020 | Chọn điểm đón | Nhập hoặc chọn địa điểm đón khách |
| KH-021 | Chọn điểm đến | Nhập hoặc chọn địa điểm đến |
| KH-022 | Chọn loại xe | Chọn xe máy, ô tô 4 chỗ hoặc ô tô 7 chỗ |
| KH-023 | Ước tính giá | Hiển thị khoảng cách và chi phí dự kiến |
| KH-024 | Gửi yêu cầu đặt xe | Gửi yêu cầu đặt xe đến hệ thống |
| KH-025 | Theo dõi tìm tài xế | Xem trạng thái đang tìm kiếm tài xế |
| KH-026 | Xem thông tin tài xế | Xem thông tin tài xế và phương tiện |
| KH-027 | Hủy chuyến | Hủy chuyến đi trong trường hợp cần thiết |
| KH-028 | Theo dõi hành trình | Theo dõi vị trí tài xế trên bản đồ |
| KH-029 | Hoàn tất chuyến đi | Xác nhận hoàn tất chuyến đi |

#### 2.1.4. Chức năng thanh toán

| Mã | Tên chức năng | Mô tả |
|---|---|---|
| KH-030 | Chọn phương thức thanh toán | Tiền mặt hoặc chuyển khoản |
| KH-031 | Thanh toán | Thực hiện thanh toán sau chuyến đi |
| KH-032 | Xem trạng thái thanh toán | Kiểm tra trạng thái thanh toán |
| KH-033 | Xem hóa đơn | Xem chi tiết hóa đơn chuyến đi |
| KH-034 | Xem lịch sử thanh toán | Xem lịch sử các giao dịch thanh toán |

#### 2.1.5. Chức năng đánh giá

| Mã | Tên chức năng | Mô tả |
|---|---|---|
| KH-040 | Đánh giá tài xế | Đánh giá sao và nhận xét sau chuyến đi |
| KH-041 | Gửi phản hồi | Gửi phản hồi hoặc khiếu nại về dịch vụ |
| KH-042 | Theo dõi hỗ trợ | Theo dõi trạng thái xử lý yêu cầu hỗ trợ |

#### 2.1.6. Chức năng trợ lý du lịch AI

| Mã | Tên chức năng | Mô tả |
|---|---|---|
| KH-050 | Hỏi đáp thông tin du lịch | Tương tác với AI để hỏi về du lịch |
| KH-051 | Gợi ý địa điểm tham quan | Nhận gợi ý các địa điểm du lịch |
| KH-052 | Gợi ý lịch trình | Nhận gợi ý lịch trình theo thời gian và ngân sách |
| KH-053 | Gợi ý dịch vụ | Gợi ý nhà hàng, khách sạn gần vị trí |
| KH-054 | Tư vấn điểm đến | Hỗ trợ lựa chọn điểm đến phù hợp |

#### 2.1.7. Chức năng quản lý tài khoản

| Mã | Tên chức năng | Mô tả |
|---|---|---|
| KH-060 | Xem hồ sơ | Xem thông tin tài khoản cá nhân |
| KH-061 | Chỉnh sửa hồ sơ | Cập nhật tên, số điện thoại, email |
| KH-062 | Đổi mật khẩu | Thay đổi mật khẩu đăng nhập |
| KH-063 | Xem lịch sử chuyến đi | Xem danh sách các chuyến đã đặt |

---

### 2.2. Nhóm tài xế

| Mã | Tên chức năng | Mô tả |
|---|---|---|
| TX-001 | Đăng nhập | Đăng nhập vào ứng dụng tài xế |
| TX-002 | Cập nhật trạng thái online/offline | Bật/tắt trạng thái sẵn sàng nhận chuyến |
| TX-003 | Nhận yêu cầu chuyến đi | Nhận thông báo khi có khách đặt xe |
| TX-004 | Chấp nhận chuyến đi | Đồng ý nhận chuyến đi |
| TX-005 | Từ chối chuyến đi | Từ chối nhận chuyến đi |
| TX-006 | Xem điểm đón/điểm đến | Xem thông tin địa điểm trên bản đồ |
| TX-007 | Cập nhật vị trí | Gửi vị trí GPS lên hệ thống |
| TX-008 | Bắt đầu chuyến đi | Xác nhận bắt đầu đón khách |
| TX-009 | Hoàn thành chuyến đi | Xác nhận kết thúc chuyến đi |
| TX-010 | Xem lịch sử chuyến đi | Xem danh sách các chuyến đã thực hiện |
| TX-011 | Xem thu nhập | Xem thu nhập theo ngày, tuần, tháng |
| TX-012 | Nhận đánh giá | Xem đánh giá từ khách hàng |
| TX-013 | Cập nhật thông tin xe | Cập nhật biển số, màu xe, model |

---

### 2.3. Nhóm chủ doanh nghiệp

| Mã | Tên chức năng | Mô tả |
|---|---|---|
| CDB-001 | Dashboard tổng quan | Xem tổng quan số chuyến, doanh thu |
| CDB-002 | Quản lý tài xế | Thêm, sửa, khóa tài khoản tài xế |
| CDB-003 | Quản lý khách hàng | Quản lý tài khoản khách hàng |
| CDB-004 | Xem báo cáo thống kê | Xem biểu đồ doanh thu theo thời gian |
| CDB-005 | Theo dõi hoạt động | Theo dõi tình trạng tài xế, chuyến đang chạy |

---

### 2.4. Nhóm nhân viên tư vấn

| Mã | Tên chức năng | Mô tả |
|---|---|---|
| NVTC-001 | Tiếp nhận yêu cầu hỗ trợ | Nhận yêu cầu hỗ trợ từ khách hàng |
| NVTC-002 | Phản hồi khách hàng | Trả lời câu hỏi, giải đáp thắc mắc |
| NVTC-003 | Hỗ trợ xử lý sự cố | Xử lý các sự cố trong chuyến đi |
| NVTC-004 | Theo dõi yêu cầu | Theo dõi trạng thái các yêu cầu hỗ trợ |

---

### 2.5. Nhóm nhân viên quản lý nhân sự

| Mã | Tên chức năng | Mô tả |
|---|---|---|
| QLNS-001 | Xem danh sách người dùng | Xem toàn bộ tài khoản trong hệ thống |
| NVQL-002 | Thêm tài khoản | Tạo tài khoản mới cho nhân viên |
| NVQL-003 | Sửa tài khoản | Cập nhật thông tin tài khoản |
| NVQL-004 | Khóa/mở tài khoản | Vô hiệu hóa hoặc kích hoạt tài khoản |
| NVQL-005 | Gửi báo cáo | Gửi báo cáo lên chủ doanh nghiệp |

---

### 2.6. Nhóm nhân viên quản lý doanh thu

| Mã | Tên chức năng | Mô tả |
|---|---|---|
| QLDT-001 | Xem danh sách giao dịch | Xem toàn bộ giao dịch thanh toán |
| QLDT-002 | Theo dõi doanh thu | Theo dõi doanh thu từ các chuyến đi |
| QLDT-003 | Kiểm tra thanh toán | Kiểm tra trạng thái các khoản thanh toán |
| QLDT-004 | Thống kê doanh thu | Thống kê theo ngày, tuần, tháng, năm |
| QLDT-005 | Lập báo cáo | Tạo báo cáo tài chính |
| QLDT-006 | Gửi báo cáo | Gửi báo cáo cho chủ doanh nghiệp |

---

## 3. Yêu cầu phi chức năng

### 3.1. Tính bảo mật

| Yêu cầu | Mô tả |
|---|---|
| Mã hóa mật khẩu | Mật khẩu người dùng được mã hóa bằng bcrypt trước khi lưu |
| Phân quyền rõ ràng | Phân quyền giữa khách hàng, tài xế, nhân viên, chủ doanh nghiệp |
| Bảo vệ API | API quản trị được bảo vệ khỏi truy cập trái phép |
| Bảo mật dữ liệu cá nhân | Thông tin cá nhân và dữ liệu thanh toán được bảo vệ |
| Xác thực JWT | Sử dụng JWT token để xác thực người dùng |
| Kiểm soát phiên | Kiểm soát đăng nhập và phiên làm việc |

### 3.2. Tính khả dụng

| Yêu cầu | Mô tả |
|---|---|
| Giao diện trực quan | Giao diện dễ sử dụng, thân thiện với người dùng |
| Hoạt động ổn định | Ứng dụng hoạt động ổn định trên nhiều thiết bị Android |
| Thao tác đơn giản | Quy trình đặt xe, thanh toán, theo dõi hành trình đơn giản |
| Hỗ trợ nhanh chóng | Hỗ trợ người dùng khi gặp sự cố |

### 3.3. Tính hiệu năng

| Yêu cầu | Mô tả |
|---|---|
| Phản hồi nhanh | Hệ thống phản hồi nhanh khi đặt xe |
| Cập nhật kịp thời | Cập nhật vị trí và trạng thái chuyến đi kịp thời |
| Tìm tài xế nhanh | Tìm kiếm tài xế phù hợp trong thời gian ngắn |
| Xử lý đồng thời | Xử lý nhiều người dùng truy cập cùng lúc |
| Tối ưu truy vấn | Tối ưu truy vấn dữ liệu lịch sử và doanh thu |

### 3.4. Tính toàn vẹn dữ liệu

| Yêu cầu | Mô tả |
|---|---|
| Lưu đầy đủ | Dữ liệu chuyến đi phải được lưu đầy đủ |
| Trạng thái chính xác | Trạng thái thanh toán phải chính xác |
| Hóa đơn khớp | Hóa đơn phải khớp với chuyến đi và giao dịch |
| Lưu vết | Các thao tác cập nhật có lưu vết |
| Dữ liệu thực | Báo cáo phản ánh đúng hoạt động thực tế |

### 3.5. Tính mở rộng

| Yêu cầu | Mô tả |
|---|---|
| Thêm loại phương tiện | Hệ thống có thể mở rộng thêm nhiều loại phương tiện |
| Tích hợp thanh toán | Có thể tích hợp thêm nhiều cổng thanh toán |
| Web quản trị | Có thể phát triển thêm phiên bản web quản trị |
| Mở rộng AI | Có thể mở rộng trợ lý AI hỗ trợ nhiều địa phương |

---

## 4. Các tác nhân (Actors)

Dựa trên yêu cầu nghiệp vụ, hệ thống có **6 tác nhân chính**:

| STT | Tác nhân | Ký hiệu | Mô tả |
|---|---|---|---|
| 1 | Khách hàng | `Actor_Passenger` | Người sử dụng đặt xe và dịch vụ du lịch |
| 2 | Tài xế | `Actor_Driver` | Người tiếp nhận và thực hiện chuyến đi |
| 3 | Chủ doanh nghiệp | `Actor_Owner` | Người quản lý tổng thể hệ thống |
| 4 | Nhân viên tư vấn | `Actor_Consultant` | Người hỗ trợ khách hàng |
| 5 | Nhân viên QLNS | `Actor_HRManager` | Người quản lý tài khoản người dùng |
| 6 | Nhân viên QLDT | `Actor_RevenueManager` | Người quản lý doanh thu |

---

## 5. Sơ đồ Use Case

### 5.1. Tổng quan Use Case

```
┌─────────────┐
│  Khách hàng │
│  (Passenger) │
└──────┬──────┘
       │
       ├── UC1:  Đăng ký tài khoản
       ├── UC2:  Đăng nhập
       ├── UC3:  Đặt xe
       ├── UC4:  Theo dõi hành trình
       ├── UC5:  Thanh toán
       ├── UC6:  Đánh giá tài xế
       ├── UC7:  Sử dụng trợ lý AI
       ├── UC8:  Quản lý thông tin cá nhân
       ├── UC9:  Xem lịch sử chuyến đi
       └── UC10: Gửi yêu cầu hỗ trợ

┌─────────────┐
│   Tài xế    │
│  (Driver)   │
└──────┬──────┘
       │
       ├── UC11: Đăng nhập
       ├── UC12: Nhận chuyến xe
       ├── UC13: Cập nhật trạng thái chuyến đi
       ├── UC14: Xem doanh thu cá nhân
       └── UC15: Quản lý thông tin cá nhân

┌─────────────┐
│ Chủ DN      │
│ (Owner)     │
└──────┬──────┘
       │
       ├── UC16: Dashboard tổng quan
       ├── UC17: Quản lý tài xế
       ├── UC18: Quản lý khách hàng
       └── UC19: Xem báo cáo thống kê

┌─────────────┐
│ NV Tư vấn   │
│(Consultant) │
└──────┬──────┘
       │
       ├── UC20: Tiếp nhận yêu cầu hỗ trợ
       ├── UC21: Phản hồi khách hàng
       └── UC22: Hỗ trợ xử lý sự cố

┌─────────────┐
│ NV QLNS     │
│(HR Manager) │
└──────┬──────┘
       │
       ├── UC23: Quản lý tài khoản người dùng
       ├── UC24: Khóa/mở tài khoản
       └── UC25: Gửi báo cáo

┌─────────────┐
│ NV QLDT     │
│(Revenue Mgr)│
└──────┬──────┘
       │
       ├── UC26: Theo dõi giao dịch
       ├── UC27: Thống kê doanh thu
       └── UC28: Lập báo cáo tài chính
```

### 5.2. Use Case chi tiết: Đặt xe

| Thuộc tính | Chi tiết |
|---|---|
| **ID** | UC-003 |
| **Tên** | Đặt xe |
| **Actor** | Khách hàng |
| **Mô tả** | Khách hàng chọn điểm đón, điểm đến và loại xe để gửi yêu cầu đặt xe |
| **Pre-condition** | Khách hàng đã đăng nhập |
| **Post-condition** | Yêu cầu đặt xe được gửi đến hệ thống và chờ tài xế xác nhận |

**Luồng chính:**
1. Khách hàng chọn điểm đón trên bản đồ
2. Khách hàng chọn điểm đến
3. Hệ thống hiển thị khoảng cách và chi phí dự kiến
4. Khách hàng chọn loại xe (xe máy / 4 chỗ / 7 chỗ)
5. Khách hàng nhấn nút "Đặt xe"
6. Hệ thống gửi yêu cầu đến các tài xế gần đó
7. Hệ thống hiển thị trạng thái đang tìm tài xế
8. Tài xế chấp nhận chuyến đi
9. Hệ thống thông báo cho khách hàng

**Luồng phụ:**
- *(Khách hủy)* Khách hàng có thể hủy trước khi tài xế chấp nhận
- *(Không tìm được tài xế)* Hệ thống thông báo không có tài xế khả dụng

### 5.3. Use Case chi tiết: Trợ lý du lịch AI

| Thuộc tính | Chi tiết |
|---|---|
| **ID** | UC-007 |
| **Tên** | Sử dụng trợ lý du lịch AI |
| **Actor** | Khách hàng |
| **Mô tả** | Khách hàng tương tác với AI để nhận gợi ý về địa điểm, lịch trình du lịch |
| **Pre-condition** | Khách hàng đã đăng nhập |
| **Post-condition** | Khách hàng nhận được gợi ý phù hợp từ AI |

**Luồng chính:**
1. Khách hàng mở màn hình trợ lý AI
2. Khách hàng nhập câu hỏi hoặc chọn chủ đề gợi ý
3. AI phân tích yêu cầu và xử lý
4. AI trả về gợi ý (địa điểm / lịch trình / dịch vụ)
5. Khách hàng có thể tiếp tục hỏi hoặc đặt xe đến địa điểm được gợi ý

### 5.4. Use Case chi tiết: Nhận chuyến xe (Tài xế)

| Thuộc tính | Chi tiết |
|---|---|
| **ID** | UC-012 |
| **Tên** | Nhận chuyến xe |
| **Actor** | Tài xế |
| **Mô tả** | Tài xế nhận thông báo và quyết định nhận hoặc từ chối chuyến đi |
| **Pre-condition** | Tài xế đang online và chưa có chuyến đang thực hiện |
| **Post-condition** | Tài xế chấp nhận hoặc từ chối chuyến đi |

**Luồng chính:**
1. Hệ thống thông báo có yêu cầu đặt xe mới
2. Tài xế xem thông tin chi tiết (điểm đón, điểm đến, khoảng cách, giá)
3. Tài xế chọn "Nhận chuyến" hoặc "Từ chối"
4. Nếu nhận: hệ thống gán chuyến và thông báo khách hàng
5. Nếu từ chối: hệ thống tìm tài xế khác

---

## Liên kết

- [ Quay lại: Tổng quan đề tài](./1-TongQuanDeTai.md)
- [ Tiếp theo: Phân tích & Thiết kế](./3-PhanTichThietKe.md)
