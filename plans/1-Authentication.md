# Feature Plan #1: Authentication System

---

## 1. Mô tả

Hệ thống xác thực người dùng bao gồm:
- **Register**: Đăng ký tài khoản passenger hoặc driver
- **Login**: Đăng nhập bằng email + password
- **Session Management**: JWT token lưu trong SharedPreferences
- **Auto-login**: Kiểm tra session hợp lệ khi mở app

---

## 2. Trạng thái hiện tại

### Backend ✅ Hoàn thành
- `POST /api/auth/register` - Tạo tài khoản + tạo driver profile nếu là driver
- `POST /api/auth/login` - Xác thực + trả JWT token
- Password hash bằng bcrypt (cost 10)
- JWT expires in 30 days

### Android ✅ Hoàn thành (cần cải thiện)
- `AuthScreen.kt` - UI đăng nhập/đăng ký với animations
- `SessionManager.kt` - Lưu trữ token và user info
- `AuthViewModel` - Xử lý logic auth

### Vấn đề cần sửa
1. **AuthViewModel gọi RetrofitClient trực tiếp** - Nên dùng `AuthRepository`
2. **Không có Splash Screen auto-login** - App luôn bắt đầu ở AuthScreen
3. **Không có Forgot Password** - UI có placeholder nhưng không hoạt động
4. **Register không gọi SessionManager** - Token không được lưu sau đăng ký

---

## 3. Code cần sửa / thêm

### 3.1. Fix AuthViewModel → dùng AuthRepository

**File:** `app/src/main/java/.../ui/screens/auth/AuthScreen.kt`

```kotlin
// HIỆN TẠI (gọi trực tiếp RetrofitClient)
val response = RetrofitClient.apiService.login(LoginRequest(form.email, form.password))

// CẦN SỬA (dùng repository - inject)
val result = authRepository.login(form.email, form.password)
result.onSuccess { /* lưu session ở đây */ }
```

### 3.2. Tạo SplashScreen

**File mới:** `app/src/main/java/.../ui/screens/splash/SplashScreen.kt`

```
Màn hình splash hiển thị logo 2 giây
→ Kiểm tra SessionManager.isLoggedIn()
  ├── true  → Điều hướng đến PassengerHome / DriverHome
  └── false → Điều hướng đến AuthScreen
```

### 3.3. Fix Register → lưu token

**Trong AuthViewModel.register()**
```kotlin
// HIỆN TẠI: chỉ set isSuccess = true
if (response.body()?.success == true) {
    _authState.value = _authState.value.copy(isLoading = false, isSuccess = true)
}

// CẦN SỬA: parse token và user rồi lưu vào SessionManager
if (response.body()?.success == true) {
    val body = response.body()!!
    body.token?.let { sessionManager.authToken = it }
    body.user?.let {
        sessionManager.saveUser(it.id, it.name, it.email, it.userType)
    }
    _authState.value = _authState.value.copy(isLoading = false, isSuccess = true)
}
```

### 3.4. Thêm Forgot Password flow

**Backend:** Thêm endpoint mới
```javascript
// backend/src/routes/auth.js
// POST /api/auth/forgot-password
// Body: { email }
// Response: { success: true, message: "OTP sent to email" }

// POST /api/auth/reset-password
// Body: { email, otp, newPassword }
```

**Android:** Implement UI + logic
- Nút "Quên mật khẩu?" → mở BottomSheet
- Nhập email → gửi OTP
- Nhập OTP + mật khẩu mới → gọi reset API

---

## 4. Implementation Steps

### Step 1: Tái cấu trúc AuthViewModel (30 phút)
```
1. Inject AuthRepository vào AuthViewModel qua constructor
2. Thay RetrofitClient.apiService.login/register bằng repository methods
3. Xử lý Result<AuthResponse> trả về từ repository
4. Lưu token vào SessionManager sau login/register thành công
```

### Step 2: Tạo SplashScreen (45 phút)
```
1. Tạo SplashScreen composable với logo + animation
2. Trong MainActivity, thay AuthScreen bằng SplashScreen làm startDestination
3. Sau khi check session → navigate đến đúng màn hình
4. Xử lý case: token hết hạn → clear session → go to Auth
```

### Step 3: Fix Register flow (15 phút)
```
1. Parse response.token từ register API
2. Gọi sessionManager.authToken = token
3. Gọi sessionManager.saveUser(...) với user info
4. Test: đăng ký → đăng nhập → navigate đúng role
```

### Step 4: Implement Forgot Password (1 giờ)
```
1. Backend: Thêm /api/auth/forgot-password endpoint
2. Backend: Thêm /api/auth/reset-password endpoint
3. Android: Tạo ForgotPasswordBottomSheet composable
4. Android: AuthViewModel thêm method requestPasswordReset()
5. Android: Test flow đầy đủ
```

---

## 5. Testing Checklist

| Test Case | Kỳ vọng |
|---|---|
| TC-AUTH-01: Đăng nhập đúng credentials | Redirect đến Home tương ứng |
| TC-AUTH-02: Đăng nhập sai password | Hiển thị lỗi "Invalid email or password" |
| TC-AUTH-03: Đăng nhập tài khoản không tồn tại | Hiển thị lỗi "Invalid email or password" |
| TC-AUTH-04: Đăng ký passenger mới | Tạo user → lưu token → redirect PassengerHome |
| TC-AUTH-05: Đăng ký driver mới | Tạo user + driver profile → redirect DriverHome |
| TC-AUTH-06: Email đã tồn tại | Hiển thị lỗi "Email already registered" |
| TC-AUTH-07: Splash → auto-login khi đã có token | Chuyển thẳng đến Home |
| TC-AUTH-08: Splash → Auth khi không có token | Chuyển đến AuthScreen |
| TC-AUTH-09: Splash → Auth khi token hết hạn | Clear session → AuthScreen |
| TC-AUTH-10: Quên mật khẩu | Nhận OTP (mock) → đặt lại thành công |

---

## 6. Files affected

### Sửa
| File | Thay đổi |
|---|---|
| `AuthScreen.kt` | Inject AuthRepository, fix login/register flow |
| `AppNavigation.kt` | Thêm SplashScreen route, change startDestination |
| `MainActivity.kt` | Sử dụng startDestination từ SessionManager |

### Tạo mới
| File | Mục đích |
|---|---|
| `SplashScreen.kt` | Màn hình splash với auto-login |
| `ForgotPasswordBottomSheet.kt` | UI quên mật khẩu |

### Backend
| File | Thay đổi |
|---|---|
| `auth.js` | Thêm forgot-password, reset-password endpoints |

---

## 7. Dependencies

```kotlin
// build.gradle.kts (app)
// Không cần thêm dependency mới - AuthRepository đã tồn tại
```

---

## 8. Estimated time

- **Tái cấu trúc AuthViewModel**: 30 phút
- **Tạo SplashScreen**: 45 phút
- **Fix Register flow**: 15 phút
- **Implement Forgot Password**: 60 phút
- **Testing**: 30 phút

**Tổng: ~3 giờ**
