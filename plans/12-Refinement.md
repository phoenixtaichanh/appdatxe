# Feature Plan #12: Polish, Performance & Refinement

---

## 1. Mô tả

Các công việc cuối cùng để hoàn thiện ứng dụng:
- Performance optimization
- Error handling improvements
- Loading states
- Empty states
- Accessibility
- Code cleanup

---

## 2. Các vấn đề cần sửa

### 2.1. DriverRepository khởi tạo trong ViewModel

```kotlin
// HIỆN TẠI (DriverHomeScreen.kt line 51)
class DriverHomeViewModel : ViewModel() {
    private val repository = DriverRepository() // ❌ Tạo trực tiếp
}

// CẦN SỬA
class DriverHomeViewModel @Inject constructor(
    private val repository: DriverRepository
) : ViewModel() // ✅ Inject qua Hilt
```

### 2.2. Missing loading states

Thêm loading state cho tất cả màn hình:
- Pull-to-refresh
- Skeleton loading
- Empty state với retry button

### 2.3. Error handling

```kotlin
// Tất cả API calls nên có error boundary
@Composable
fun ApiErrorHandler(
    result: Result<T>,
    onRetry: () -> Unit
) {
    result.onFailure { error ->
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.Error, null, tint = AccentRed, modifier = Modifier.size(64.dp))
            Spacer(Modifier.height(16.dp))
            Text("Đã xảy ra lỗi", color = TextPrimary)
            Text(error.message ?: "Unknown error", color = TextSecondary)
            Spacer(Modifier.height(16.dp))
            GradientButton(text = "Thử lại", onClick = onRetry)
        }
    }
}
```

### 2.4. Pull-to-refresh

```kotlin
@Composable
fun PullToRefreshList(
    items: List<T>,
    onRefresh: () -> Unit,
    isRefreshing: Boolean
) {
    val pullRefreshState = rememberPullToRefreshState()
    val pullRefreshModifier = Modifier.pullToRefresh(
        state = pullRefreshState,
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier.nestedScroll(pullRefreshState.nestedScrollConnection)
    )

    Box(modifier = pullRefreshModifier) {
        LazyColumn {
            items(items) { item -> /* item */ }
        }
    }
}
```

### 2.5. Offline support

```kotlin
// Kiểm tra network trước khi gọi API
object NetworkUtils {
    fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}

// Sử dụng
LaunchedEffect(Unit) {
    if (!NetworkUtils.isNetworkAvailable()) {
        snackbarHostState.showSnackbar("Không có kết nối mạng")
        return@LaunchedEffect
    }
    // Gọi API
}
```

### 2.6. Biometric authentication

```kotlin
// Thêm login bằng vân tay sau khi đăng nhập lần đầu
@Composable
fun BiometricPrompt(
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val context = LocalContext.current
    val executor = remember { ContextCompat.getMainExecutor(context) }
    val biometricPrompt = remember {
        BiometricPrompt(context, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(r: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }
            override fun onAuthenticationError(code: Int, msg: CharSequence) {
                onError(msg.toString())
            }
        })
    }
    val promptInfo = remember {
        BiometricPrompt.PromptInfo.Builder()
            .setTitle("Xác thực sinh trắc học")
            .setSubtitle("Đăng nhập bằng vân tay")
            .build()
    }

    LaunchedEffect(Unit) {
        biometricPrompt.authenticate(promptInfo)
    }
}
```

### 2.7. Accessibility improvements

```kotlin
// Thêm contentDescription cho tất cả icons và images
Icon(
    imageVector = Icons.Default.Place,
    contentDescription = "Địa điểm",
    tint = AccentRed
)

// Semantic properties cho buttons
Button(
    onClick = { /* */ },
    modifier = Modifier.semantics {
        role = Role.Button
        label = "Nút đặt xe"
    }
)

// Support TalkBack
Text(
    text = "Giá: ${price}đ",
    modifier = Modifier.semantics {
        contentDescription = "Giá tiền: ${formatPrice(price)} đồng"
    }
)
```

### 2.8. Code cleanup checklist

| File | Issue | Fix |
|---|---|---|
| `PassengerHomeScreen.kt` | Hard-coded demo locations | Replace với Places API |
| `DriverHomeScreen.kt` | repository = DriverRepository() | Inject qua @Inject |
| `AuthScreen.kt` | Gọi RetrofitClient trực tiếp | Dùng AuthRepository |
| `rides.js` | 2 GET '/' routes | Xóa duplicate |
| `MainActivity.kt` | Không check session | Thêm SplashScreen |
| Toàn bộ | TODO comments | Resolve all TODO |
| Toàn bộ | Hard-coded strings | Move sang strings.xml |

---

## 3. Testing chi tiết

### 3.1. Unit Tests

```kotlin
// test/ExampleUnitTest.kt
class ExampleUnitTest {
    @Test fun haversineDistance_calculatesCorrectly() {
        // Distance TP.HCM to Dong Nai ~30km
        val result = haversineDistance(10.7629, 106.6604, 10.95, 106.80)
        assertTrue(result in 28.0..32.0)
    }

    @Test fun calculatePrice_formulaCorrect() {
        val price = calculatePrice(distanceKm = 10.0, durationMin = 20)
        // Expected: 10000 + 50000 + 4000 = 64000
        assertEquals(64000.0, price)
    }
}
```

### 3.2. Integration Tests

```
Flow Test:
1. Register → Login → Book ride → Driver accept → Complete → Rate
2. Driver: Login → Go online → Accept ride → Navigate → Complete → Check earnings
3. AI: Create schedule → Add waypoints → Optimize → Preview route
```

---

## 4. Estimated time

**Inject repositories: 1 giờ**
**Loading/empty states: 1 giờ**
**Error handling: 1 giờ**
**Accessibility: 30 phút**
**Code cleanup: 1 giờ**
**Unit tests: 1 giờ**
**Integration tests: 1 giờ**

**Tổng: ~6.5 giờ**
