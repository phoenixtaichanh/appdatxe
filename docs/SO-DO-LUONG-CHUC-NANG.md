# SƠ ĐỒ LUỒNG CHỨC NĂNG - DOAN3

---

## 1. ĐĂNG NHẬP

Người dùng nhập email và password -> AuthScreen gửi tới AuthViewModel -> AuthViewModel kiểm tra format (email, password >= 6 ký tự) -> nếu lỗi thì hiển thị lỗi trên form, nếu hợp lệ thì gọi AuthRepository.login()

AuthRepository.login() gửi request tới ApiService -> ApiService gọi POST /api/auth/login qua Retrofit -> RetrofitClient gửi HTTP request tới Backend -> Backend (Node.js) nhận request tại AuthController

AuthController kiểm tra email và password -> so sánh với password đã hash trong Database MySQL (bảng users) -> nếu không khớp thì trả về lỗi 401, nếu khớp thì tạo JWT token và trả về thông tin user

Backend trả về AuthResponse (token + user info) -> Retrofit nhận response qua ApiService -> AuthRepository nhận kết quả -> AuthRepository gọi SessionManager.saveSession() để lưu token vào SharedPreferences

SessionManager lưu authToken, userId, userName, userType vào SharedPreferences -> AuthRepository gọi SocketManager.connect(token) để kết nối Socket.IO -> AuthViewModel cập nhật AuthState.isSuccess = true -> AppNavigation chuyển hướng tới PassengerHomeScreen (nếu userType = passenger) hoặc DriverHomeScreen (nếu userType = driver)

---

## 2. ĐĂNG KÝ

Người dùng chọn tab Register -> chọn loại tài khoản (Passenger/Driver) -> nhập name, email, phone, password, confirmPassword -> nhấn Create Account -> AuthScreen gửi tới AuthViewModel

AuthViewModel kiểm tra form (name không rỗng, email đúng format, phone >= 10 số, password >= 6 ký tự, password = confirmPassword) -> nếu lỗi thì hiển thị lỗi trên từng trường, nếu hợp lệ thì gọi AuthRepository.register()

AuthRepository.register() gửi request tới ApiService -> ApiService gọi POST /api/auth/register qua Retrofit -> Backend nhận request tại AuthController

AuthController kiểm tra email đã tồn tại trong Database MySQL (bảng users) -> nếu email đã tồn tại thì trả về lỗi 409, nếu chưa thì INSERT bản ghi mới vào bảng users (name, email, password đã bcrypt hash, phone, user_type) -> tạo JWT token và trả về

Backend trả về AuthResponse -> AuthRepository nhận kết quả -> gọi SessionManager.saveSession() lưu vào SharedPreferences -> gọi SocketManager.connect(token) kết nối Socket.IO -> AuthViewModel cập nhật isSuccess = true -> AppNavigation chuyển hướng tới màn hình chính

---

## 3. ĐĂNG XUẤT

Người dùng nhấn nút Đăng xuất trên ProfileScreen -> ProfileViewModel.logout() được gọi -> SocketManager.disconnect() ngắt kết nối Socket.IO -> SessionManager.clearSession() xóa toàn bộ dữ liệu trong SharedPreferences (token, userId, userName, userType) -> AppNavigation chuyển hướng tới AuthScreen

---

## 4. QUÊN MẬT KHẨU (GỬI OTP)

Người dùng nhấn link Forgot Password trên AuthScreen -> chuyển tới ForgotPasswordScreen -> nhập email đã đăng ký -> nhấn Send OTP -> ForgotPasswordViewModel gửi tới PasswordResetRepository.sendOtp(email)

PasswordResetRepository gửi request tới ApiService -> ApiService gọi POST /api/auth/forgot-password -> Backend nhận request tại AuthController -> Backend INSERT bản ghi vào bảng password_resets (email, otp_code 6 chữ số, expires_at = now + 10 phút)

Backend gửi email chứa mã OTP qua SMTP (Nodemailer) -> Backend trả về response (message + devOtp nếu ở dev mode) -> ForgotPasswordViewModel cập nhật ForgotPasswordState.emailSent = true -> chuyển tới OtpVerificationScreen với email và devOtp

---

## 5. XÁC MINH OTP

Người dùng nhập 6 chữ số OTP (hoặc dev OTP tự động điền) -> nhấn Verify -> OtpVerificationViewModel gửi tới PasswordResetRepository.verifyOtp(email, otp)

PasswordResetRepository gửi request tới ApiService -> ApiService gọi POST /api/auth/verify-otp -> Backend kiểm tra trong bảng password_resets (email khớp, otp khớp, chưa hết hạn, chưa được sử dụng) -> nếu không hợp lệ thì trả về lỗi, nếu hợp lệ thì đánh dấu OTP đã sử dụng

Backend trả về success -> OtpVerificationViewModel cập nhật OtpVerificationState.isVerified = true -> chuyển tới ResetPasswordScreen với email và otp

Người dùng nhấn Resend OTP (sau 60s) -> OtpVerificationViewModel gọi PasswordResetRepository.resendOtp(email) -> Backend UPDATE otp_code mới trong bảng password_resets và gửi email lại

---

## 6. ĐẶT LẠI MẬT KHẨU

Người dùng nhập New Password và Confirm Password -> nhấn Reset Password -> ResetPasswordViewModel kiểm tra password >= 6 ký tự và passwords khớp -> gọi PasswordResetRepository.resetPassword(email, otp, newPassword)

PasswordResetRepository gửi request tới ApiService -> ApiService gọi POST /api/auth/reset-password -> Backend cập nhật password trong bảng users (hash bcrypt mới) và đánh dấu OTP đã sử dụng trong bảng password_resets

Backend trả về success -> ResetPasswordViewModel cập nhật ResetPasswordState.isSuccess = true -> chuyển tới AuthScreen để đăng nhập lại

---

## 7. ĐẶT XE (HÀNH KHÁCH)

Người dùng nhấn nút Đặt xe trên PassengerHomeScreen -> BookingBottomSheet mở ra -> nhập điểm đón và điểm đến (hoặc chọn từ danh sách gợi ý) -> chọn loại phương tiện (Xe máy / Ô tô 4 chỗ / Ô tô 7 chỗ) -> PassengerHomeViewModel.calculateEstimate() tính giá phía client

Tính khoảng cách Haversine từ pickup tới destination (R = 6371km) -> tính durationMin = (distanceKm / 30) * 60 -> áp dụng công thức: totalPrice = baseFare + (distanceKm * pricePerKm) + (durationMin * pricePerMin) với bảng giá theo loại xe -> hiển thị chi tiết giá trên UI

Người dùng nhấn Tìm tài xế -> PassengerHomeViewModel.startDriverSearch() được gọi -> vòng lặp 5 giây gọi RideRepository.getNearbyDrivers(pickupLat, pickupLng) -> gửi GET /api/users/drivers/nearby tới Backend -> Backend query bảng drivers và driver_locations trong MySQL, lọc driver trong bán kính 5km và is_available = true -> trả về danh sách DriverDto

Danh sách tài xế hiển thị trên UI (tối đa 3) -> người dùng chọn một tài xế -> nhấn Đặt xe ngay -> PassengerHomeViewModel.requestRide() được gọi -> RideRepository.requestRide() gửi POST /api/rides/request tới Backend

Backend INSERT bản ghi vào bảng rides (passenger_id, pickup/dest lat/lng, vehicle_type, status = 'pending') -> Backend tính price (từ Google Distance Matrix API hoặc ước tính) -> trả về RideDto

RideDto được lưu vào state.currentRide -> state.isRideActive = true -> SocketManager.joinRide(rideId) gửi event join:ride tới server -> SocketManager.requestDriverLocation(rideId) gửi event request:driver_location tới server -> Bottom sheet hiển thị trạng thái chờ tài xế

---

## 8. TÀI XẾ NHẬN CHUYẾN

Tài xế bật nút Online trên DriverHomeScreen -> DriverHomeViewModel.toggleOnlineStatus() được gọi -> DriverRepository.updateDriverStatus(isAvailable=true, lat, lng) gửi PUT /api/driver/status tới Backend -> Backend UPDATE bảng drivers (is_available = true, latitude, longitude) -> trả về success

Backend gửi thông báo FCM tới hành khách có chuyến trong bán kính -> DriverHomeViewModel.loadAvailableRides() gọi GET /api/driver/ride/available -> Backend query bảng rides có status = 'pending' trong bán kính -> trả về danh sách RideDto

Danh sách chuyến mới hiển thị trên tab Chuyến mới -> tài xế nhấn Nhận trên một AvailableRideCard -> DriverHomeViewModel.acceptRide(rideId) gọi POST /api/driver/ride/:id/accept -> Backend UPDATE bảng rides (driver_id, status = 'accepted') -> trả về RideDto đã cập nhật

Backend gửi thông báo FCM tới passenger có ride đó -> currentRide được cập nhật trong state -> chuyển sang tab Đang chạy

---

## 9. CẬP NHẬT TRẠNG THÁI CHUYẾN (TÀI XẾ)

Tài xế nhấn nút hành động trên CurrentRideCard (Đã đến điểm đón / Bắt đầu chuyến / Hoàn thành chuyến / Hủy chuyến) -> DriverHomeViewModel.updateRideStatus(rideId, newStatus) được gọi

SocketManager.emitRideStatus(rideId, status) gửi event ride:status qua Socket.IO tới Backend -> Backend emit event ride:status:changed tới passenger qua Socket.IO

DriverRepository.updateRideStatus(rideId, status) gửi PUT /api/driver/ride/:id/status tới Backend -> Backend UPDATE bảng rides (status = newStatus)

Nếu status = 'completed': Backend INSERT bản ghi vào bảng earnings (amount, type = 'ride') -> Backend trả về null cho currentRide
Nếu status = 'arrived' hoặc 'in_progress': Backend trả về RideDto đã cập nhật cho currentRide

DriverHomeViewModel cập nhật state -> UI hiển thị trạng thái mới

---

## 10. REALTIME - VỊ TRÍ TÀI XẾ (SOCKET.IO)

DriverHomeScreen (khi có currentRide) liên tục gửi SocketManager.emitLocationUpdate(lat, lng, rideId) qua Socket.IO -> Backend nhận event location:update -> Backend INSERT bản ghi vào bảng driver_locations (driver_id, lat, lng, timestamp)

Backend emit event driver:location tới passenger qua Socket.IO -> SocketManager.driverLocationFlow (SharedFlow) trên Android nhận được { lat, lng, rideId, timestamp }

PassengerHomeViewModel lắng nghe driverLocationFlow trong init block -> nếu update.rideId == currentRide.id thì cập nhật state.driverLat và state.driverLng -> TaxiMapView hiển thị marker tài xế di chuyển realtime trên bản đồ

---

## 11. XEM CHI TIẾT CHUYẾN ĐI

Người dùng nhấn vào chuyến đi trên HistoryScreen hoặc PassengerHomeScreen -> chuyển tới RideDetailScreen(rideId, isDriverView) -> RideDetailViewModel.setRideId(rideId) được gọi -> RideDetailViewModel.loadRide() gọi RideRepository.getRide(rideId)

RideRepository gửi GET /api/rides/:id tới Backend -> Backend query bảng rides trong MySQL -> trả về RideDto đầy đủ

RideDetailViewModel cập nhật state.ride -> UI hiển thị thông tin: mã chuyến, trạng thái, lộ trình (điểm đón/đến), khoảng cách, thời gian, giá cước

Nếu là passenger và ride đang pending: hiển thị form chọn phương thức thanh toán
Nếu là driver: hiển thị thông tin hành khách (passengerName)
Nếu ride đã completed và chưa rating: hiển thị form đánh giá sao

---

## 12. THANH TOÁN (PASSENGER)

Người dùng chọn phương thức thanh toán (Tiền mặt / MoMo / VNPay) -> RideDetailViewModel.selectPaymentMethod(method) cập nhật state -> nhấn Xác nhận thanh toán -> RideDetailViewModel.createPayment() được gọi

RideDetailViewModel gọi PaymentRepository.createPayment(rideId, paymentMethod) -> gửi POST /api/payments/create tới Backend -> Backend INSERT bản ghi vào bảng payments

Nếu method = 'cash': Backend UPDATE bảng rides (payment_status = 'paid') -> trả về response với paymentUrl = null -> RideDetailViewModel cập nhật paymentCreated = true -> UI hiển thị "Thanh toán tiền mặt" và checkmark

Nếu method = 'vnpay': Backend ký HMAC-SHA256 với VNPay credentials (TMN_CODE, HASH_SECRET) -> tạo payment URL VNPay -> trả về response với paymentUrl VNPay -> ứng dụng mở browser để thanh toán VNPay

Nếu method = 'momo': Backend ký HMAC-SHA256 với MoMo credentials (PARTNER_CODE, ACCESS_KEY, SECRET_KEY) -> tạo payment URL MoMo -> trả về response với paymentUrl MoMo -> ứng dụng mở browser để thanh toán MoMo

Khi thanh toán thành công: VNPay/MoMo redirect về Backend (POST /api/payments/:id/confirm hoặc return URL) -> Backend UPDATE bảng payments (status = 'confirmed') và bảng rides (payment_status = 'paid')

---

## 13. ĐÁNH GIÁ CHUYẾN ĐI

Người dùng nhấn sao để chọn rating (1-5 sao) -> chọn tags đánh giá (tùy chọn) -> nhập bình luận (tùy chọn) -> nhấn Gửi đánh giá -> RideDetailViewModel.rateRide(rating, comment) được gọi

RideRepository.rateRide(rideId, rating, comment) gửi POST /api/rides/:id/rate tới Backend -> Backend UPDATE bảng rides (driver_rating = rating, rating_comment = comment) -> Backend tính lại rating trung bình và UPDATE bảng users cho driver đó

Backend trả về success -> RideDetailViewModel cập nhật ratingSubmitted = true -> UI hiển thị "Bạn đã đánh giá X sao"

---

## 14. LỊCH SỬ CHUYẾN ĐI

Người dùng nhấn icon Lịch sử trên PassengerHomeScreen hoặc DriverHomeScreen -> chuyển tới HistoryScreen(isDriver) -> HistoryViewModel.loadHistory() được gọi

Nếu là passenger: HistoryViewModel gọi RideRepository.getRideHistory() -> GET /api/rides -> Backend query bảng rides WHERE passenger_id = currentUserId -> trả về danh sách RideDto

Nếu là driver: HistoryViewModel gọi DriverRepository.getDriverHistory() -> GET /api/driver/history -> Backend query bảng rides WHERE driver_id = currentDriverId -> trả về danh sách RideDto

Danh sách hiển thị trên UI -> người dùng có thể tìm kiếm (lọc phía client theo địa chỉ, tên tài xế/hành khách) hoặc lọc theo tab (Tất cả / Hoàn thành / Đã hủy) -> nhấn vào chuyến để xem chi tiết (RideDetailScreen)

---

## 15. XEM VÀ CHỈNH SỬA HỒ SƠ

Người dùng nhấn icon Profile trên màn hình chính -> chuyển tới ProfileScreen -> ProfileViewModel.loadProfile() được gọi

ProfileViewModel lấy userType từ SessionManager -> nếu là driver: gọi DriverRepository.getDriverProfile() -> GET /api/driver/profile -> Backend JOIN bảng users và drivers trong MySQL -> trả về DriverDto (name, phone, carModel, carColor, licensePlate, rating)

Nếu là passenger: gọi RideRepository.getRideHistory() -> đếm tổng số chuyến để hiển thị totalRides

Người dùng nhấn Chỉnh sửa -> nhập thông tin mới (name, phone, carModel, carColor, licensePlate cho driver) -> nhấn Lưu -> ProfileViewModel.saveProfile() gọi DriverRepository.updateDriverProfile() -> PUT /api/driver/profile -> Backend UPDATE bảng users (name, phone) và bảng drivers (car_model, car_color, license_plate)

Backend trả về success -> ProfileViewModel loadProfile() lại để cập nhật UI

---

## 16. TẠO LỊCH TRÌNH AI

Người dùng nhấn icon Lịch trình AI trên PassengerHomeScreen -> chuyển tới AIScheduleScreen -> nhấn + Tạo lịch trình mới -> nhập tên, chọn loại tối ưu (Nhanh/Rẻ/Cân bằng), chọn số điểm dừng (2-6) -> nhấn Tạo lịch trình

AIScheduleViewModel.createSchedule() gọi AIRepository.createSchedule() -> gửi POST /api/ai/schedule/create tới Backend với danh sách waypoints

Backend INSERT vào bảng ai_trip_schedules (user_id, schedule_name, scheduled_date, optimization_type) -> Backend INSERT nhiều bản ghi vào bảng ai_waypoints (schedule_id, stop_order, lat/lng, address, stop_type)

AI xử lý và INSERT nhiều phương án vào bảng ai_route_alternatives (total_distance, total_duration, total_price, is_recommended) -> Backend trả về AIScheduleDto đầy đủ

AIScheduleViewModel cập nhật state.currentSchedule và state.alternatives -> UI hiển thị chi tiết lịch trình và các phương án tối ưu

---

## 17. TỐI ƯU LỊCH TRÌNH AI

Người dùng nhấn nút Nhanh / Rẻ / Cân bằng trên ScheduleDetailCard -> AIScheduleViewModel.optimizeSchedule(scheduleId, optimization) gọi AIRepository.optimizeSchedule() -> gửi POST /api/ai/schedule/:id/optimize tới Backend với body optimization_type

Backend AI tính toán lại các phương án tuyến đường -> UPDATE bảng ai_route_alternatives với dữ liệu mới -> trả về danh sách RouteAlternativeDto

UI cập nhật alternatives mới -> hiển thị phương án nào là "Đề xuất"

---

## 18. CHAT AI ASSISTANT

Người dùng nhấn icon AI Chat trên PassengerHomeScreen -> chuyển tới AIChatScreen -> AI hiển thị tin nhắn chào mừng

Người dùng nhấn suggestion chip (ví dụ: "Goi xe di Bach Khoa") hoặc nhập tin nhắn -> nhấn nút gửi -> AIChatScreen thêm ChatMessage(userMessage, isUser=true) vào danh sách -> hiển thị typing indicator

Sau 1.2 giây delay (giả lập AI xử lý) -> getAIResponse(userMessage) xử lý theo keywords:
- "bach khoa" / "truong" -> phản hồi đặt xe Bách Khoa
- "7 cho" -> phản hồi đặt xe 7 chỗ
- "ha long" -> phản hồi tạo lịch trình Ha Long
- "re" / "gia re" -> gợi ý xe máy
- "4 cho" -> gợi ý xe 4 chỗ
- fallback -> phản hồi chung

AI thêm ChatMessage(response, isUser=false) vào danh sách -> UI hiển thị phản hồi

---

## 19. XEM THU NHẬP (TÀI XẾ)

Người dùng nhấn icon Earnings trên DriverHomeScreen -> chuyển tới EarningsScreen -> EarningsViewModel.loadEarnings() được gọi

EarningsViewModel tính today = LocalDate.now(), weekStart = today - 6 days -> gọi DriverRepository.getEarnings(weekStart, today) -> gửi GET /api/driver/earnings?start_date=...&end_date=... tới Backend

Backend query bảng earnings trong MySQL WHERE driver_id = ? AND date BETWEEN start AND end -> JOIN bảng rides để lấy thông tin -> tính tổng todayEarnings, weekEarnings, monthEarnings, totalEarnings, totalRides

Backend trả về EarningsDto -> EarningsViewModel tính weeklyData cho biểu đồ cột -> UI hiển thị thu nhập hôm nay, biểu đồ 7 ngày, chi tiết

---

## 20. CHUYẾN GHÉP (BATCH)

Tài xế nhấn tab Batch trên DriverHomeScreen -> DriverHomeViewModel.loadBatches() gọi DriverRepository.getAvailableBatches() -> gửi GET /api/ai/batch/available tới Backend

Backend query bảng driver_route_batches WHERE driver_id = ? AND status = 'proposed' -> JOIN bảng batch_passengers để lấy danh sách hành khách -> trả về danh sách BatchDto

Danh sách batch hiển thị trên UI -> tài xế nhấn Chấp nhận Batch trên BatchCard -> DriverHomeViewModel.acceptBatch(batchId) gọi DriverRepository.acceptBatch() -> gửi POST /api/ai/batch/:id/accept tới Backend

Backend UPDATE bảng driver_route_batches (status = 'accepted', accepted_at = NOW()) -> trả về success

Backend gửi thông báo FCM tới từng passenger trong batch -> DriverHomeViewModel xóa batch khỏi danh sách -> UI cập nhật

---

## 21. SPLASH - KIỂM TRA ĐĂNG NHẬP

Người dùng mở ứng dụng -> MainActivity khởi tạo -> chuyển tới SplashScreen -> SplashViewModel kiểm tra SessionManager

SessionManager đọc SharedPreferences (authToken, isLoggedIn, userType) -> nếu isLoggedIn = true và token hợp lệ: MainViewModel xác định userType -> AppNavigation chuyển tới PassengerHomeScreen (nếu passenger) hoặc DriverHomeScreen (nếu driver)

Nếu isLoggedIn = false hoặc token không hợp lệ: AppNavigation chuyển tới AuthScreen

---

## TỔNG HỢP CÁC BẢNG DATABASE SỬ DỤNG

users: lưu tài khoản (login, register, profile, hồ sơ)
drivers: lưu hồ sơ tài xế (nhận chuyến, profile tài xế, cập nhật online)
rides: lưu thông tin chuyến đi (đặt xe, nhận chuyến, trạng thái, thanh toán, đánh giá)
driver_locations: lưu lịch sử GPS (realtime vị trí tài xế)
earnings: lưu thu nhập tài xế (thu nhập)
ai_trip_schedules: lưu lịch trình AI (tạo lịch trình)
ai_waypoints: lưu các điểm dừng trong lịch trình AI
ai_route_alternatives: lưu phương án tuyến đường AI (tối ưu lịch trình)
ai_learning_profiles: lưu hồ sơ học tập AI (preferences người dùng)
driver_route_batches: lưu thông tin chuyến ghép (batch)
batch_passengers: lưu hành khách trong chuyến ghép
payments: lưu thông tin thanh toán (VNPay, MoMo, cash)
password_resets: lưu mã OTP quên mật khẩu (OTP 6 số)
