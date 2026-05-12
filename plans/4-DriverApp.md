# Feature Plan #4: Driver App Enhancements

---

## 1. Mô tả

Các cải tiến cho ứng dụng tài xế:
- Navigation tích hợp (Google Maps Navigation)
- Earnings chi tiết với biểu đồ
- Review ratings history
- Driver stats (total trips, avg rating, acceptance rate)

---

## 2. Trạng thái hiện tại

### Backend ✅ Hoàn thành
- `GET /api/driver/earnings` - Tổng thu nhập
- `GET /api/driver/profile` - Profile tài xế
- `PUT /api/driver/profile` - Cập nhật thông tin xe

### Android ✅ Hoàn thành
- Online/offline toggle
- Nhận/từ chối chuyến
- Cập nhật trạng thái ride
- Earnings screen (tab đơn giản)

### Vấn đề cần sửa
1. **Không có navigation** - Tài xế phải mở Google Maps riêng
2. **Earnings UI đơn giản** - Cần thêm biểu đồ cột/đường
3. **Không hiển thị tổng stats** - Total trips, avg rating, acceptance rate
4. **Không show route** - Tài xế không thấy đường đi

---

## 3. Implementation

### 3.1. Navigation Integration (Android)

```kotlin
// Trong DriverHomeScreen - khi có current ride
@Composable
fun NavigateButton(
    destination: String,
    currentStatus: String
) {
    if (currentStatus == "accepted" || currentStatus == "arrived") {
        val intent = remember {
            Intent(Intent.ACTION_VIEW,
                Uri.parse("google.navigation:q=$destination"))
                .setPackage("com.google.android.apps.maps")
        }
        if (intent.resolveActivity(packageManager) != null) {
            IconButton(onClick = { startActivity(intent) }) {
                Icon(Icons.Default.Navigation, "Navigate")
            }
        }
    }
}
```

### 3.2. Earnings Chart

```kotlin
@Composable
fun EarningsChart(earningsData: List<EarningsByDay>) {
    val maxEarning = earningsData.maxOfOrNull { it.amount } ?: 1.0
    Row(
        modifier = Modifier.fillMaxWidth().height(120.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        earningsData.forEach { data ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                val heightFraction = (data.amount / maxEarning).coerceIn(0.0, 1.0)
                Box(
                    modifier = Modifier
                        .width(32.dp)
                        .height((100 * heightFraction).dp)
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(Brush.verticalGradient(GradientPrimary))
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = data.day.takeLast(2),
                    fontSize = 10.sp,
                    color = TextSecondary
                )
            }
        }
    }
}
```

### 3.3. Driver Stats Card

```kotlin
@Composable
fun DriverStatsCard(stats: DriverStats) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatItem(value = "${stats.totalTrips}", label = "Tổng chuyến")
        StatItem(value = "${String.format("%.1f", stats.avgRating)}", label = "Đánh giá")
        StatItem(value = "${stats.acceptanceRate}%", label = "Tỷ lệ nhận")
        StatItem(value = "${String.format("%.0f", stats.totalEarnings)}đ", label = "Tổng thu nhập")
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, color = PrimaryPurple)
        Text(label, fontSize = 11.sp, color = TextSecondary)
    }
}
```

---

## 4. Files

| File | Action |
|---|---|
| `EarningsScreen.kt` | Sửa - thêm biểu đồ cột |
| `DriverHomeScreen.kt` | Sửa - thêm navigation button, stats |
| `DriverStatsCard.kt` | Tạo mới |

---

## 5. Estimated time

**Navigation: 1 giờ**
**Charts + Stats: 1.5 giờ**
**Testing: 30 phút**

**Tổng: ~3 giờ**
