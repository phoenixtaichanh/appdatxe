# Feature Plan #6: Payment & Rating System

---

## 1. Mô tả

Hệ thống thanh toán và đánh giá sau chuyến đi.

---

## 2. Trạng thái hiện tại

### Backend ✅ Hoàn thành
- `POST /api/rides/:id/rate` - Đánh giá sao (1-5)
- Rating được lưu vào rides table
- Driver rating được tính trung bình

### Android ✅ Cơ bản
- Rating bar trong RideDetailScreen
- Submit rating sau khi ride completed

### Vấn đề cần cải thiện
1. **Không có thanh toán thực** - Chỉ tính tiền, không giao dịch
2. **Không có payment method selection** - Chỉ có COD
3. **Rating UI đơn giản** - Cần thêm feedback text
4. **Không có receipt** - Không xuất hóa đơn

---

## 3. Implementation

### 3.1. Payment Method Selection

```kotlin
enum class PaymentMethod(val id: String, val displayName: String, val icon: String) {
    CASH("cash", "Tiền mặt", "wallet"),
    WALLET("wallet", "Ví điện tử", "account_balance_wallet"),
    CREDIT_CARD("card", "Thẻ ngân hàng", "credit_card")
}

@Composable
fun PaymentMethodSelector(
    selected: PaymentMethod,
    onSelect: (PaymentMethod) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PaymentMethod.entries.forEach { method ->
            PaymentMethodCard(
                method = method,
                isSelected = method == selected,
                onClick = { onSelect(method) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PaymentMethodCard(
    method: PaymentMethod,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) PrimaryPurple.copy(alpha = 0.2f) else DarkCard
        ),
        border = if (isSelected) BorderStroke(2.dp, PrimaryPurple) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = when (method.id) {
                    "cash" -> Icons.Default.Money
                    "wallet" -> Icons.Default.AccountBalanceWallet
                    else -> Icons.Default.CreditCard
                },
                contentDescription = null,
                tint = if (isSelected) PrimaryPurple else TextSecondary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = method.displayName,
                color = if (isSelected) PrimaryPurple else TextPrimary,
                fontSize = 11.sp
            )
        }
    }
}
```

### 3.2. Rating Screen với Feedback

```kotlin
@Composable
fun RatingScreen(
    rideId: Int,
    isRatingDriver: Boolean,
    onSubmit: () -> Unit
) {
    var rating by remember { mutableFloatStateOf(5f) }
    var selectedTags by remember { mutableStateOf(setOf<String>()) }
    var comment by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Driver/Passenger avatar
        Box(modifier = Modifier.size(80.dp).clip(CircleShape)
            .background(Brush.linearGradient(GradientPrimary)),
            contentAlignment = Alignment.Center) {
            Text(name.first().toString(), color = Color.White, fontSize = 32.sp)
        }
        Spacer(Modifier.height(16.dp))
        Text(name, color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("của bạn", color = TextSecondary, fontSize = 14.sp)

        Spacer(Modifier.height(32.dp))

        // Star rating
        Row {
            (1..5).forEach { star ->
                IconButton(onClick = { rating = star.toFloat() }) {
                    Icon(
                        imageVector = if (rating >= star) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = null,
                        tint = if (rating >= star) AccentYellow else TextSecondary,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Quick tags
        val tags = if (isRatingDriver) listOf(
            "Lái xe an toàn", "Thân thiện", "Xe sạch sẽ",
            "Đúng giờ", "Hỗ trợ tốt", "Đường tốt"
        ) else listOf(
            "Đúng giờ", "Thân thiện", "Hợp tác tốt"
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tags.forEach { tag ->
                TagChip(
                    text = tag,
                    isSelected = tag in selectedTags,
                    onClick = {
                        selectedTags = if (tag in selectedTags) {
                            selectedTags - tag
                        } else {
                            selectedTags + tag
                        }
                    }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Comment
        OutlinedTextField(
            value = comment,
            onValueChange = { comment = it },
            label = { Text("Nhận xét thêm (tùy chọn)") },
            modifier = Modifier.fillMaxWidth().height(100.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryPurple,
                cursorColor = PrimaryPurple
            )
        )

        Spacer(Modifier.weight(1f))

        GradientButton(
            text = "Gửi đánh giá",
            onClick = { onSubmit() },
            enabled = rating > 0
        )
    }
}

@Composable
private fun TagChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(text) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = PrimaryPurple,
            selectedLabelColor = Color.White
        )
    )
}
```

### 3.3. Ride Receipt

```kotlin
@Composable
fun RideReceipt(ride: RideDto) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Biên nhận", fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("#${ride.id}", color = TextSecondary)
            }
            HorizontalDivider(Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.1f))

            // Route
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Place, null, tint = AccentGreen, modifier = Modifier.size(16.dp))
                Text(ride.pickupAddress, color = TextPrimary, modifier = Modifier.weight(1f).padding(start = 8.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                Icon(Icons.Default.LocationOn, null, tint = AccentRed, modifier = Modifier.size(16.dp))
                Text(ride.destAddress, color = TextPrimary, modifier = Modifier.weight(1f).padding(start = 8.dp))
            }

            HorizontalDivider(Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.1f))

            // Price breakdown
            PriceRow("Cước phí", "${formatPrice(ride.price)}đ")
            PriceRow("Phương thức", ride.paymentMethod ?: "Tiền mặt")
            HorizontalDivider(Modifier.padding(vertical = 8.dp), color = Color.White.copy(alpha = 0.1f))
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Tổng cộng", fontWeight = FontWeight.Bold, color = PrimaryPurple)
                Text("${formatPrice(ride.price)}đ", fontWeight = FontWeight.Bold, color = PrimaryPurple)
            }

            Spacer(Modifier.height(12.dp))

            // Driver info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(32.dp).clip(CircleShape)
                    .background(PrimaryPurple), contentAlignment = Alignment.Center) {
                    Text(ride.driverName?.first().toString() ?: "D", color = Color.White)
                }
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(ride.driverName ?: "Tài xế", color = TextPrimary)
                    Row { repeat(5) { Icon(Icons.Filled.Star, null, AccentYellow, 12.dp) } }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Timestamp
            Text(
                "Ngày: ${formatDate(ride.completedAt ?: ride.createdAt)}",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}
```

---

## 4. Testing Checklist

| Test Case | Kỳ vọng |
|---|---|
| TC-PAY-01: Thanh toán tiền mặt | Ride hoàn thành, không gọi payment API |
| TC-PAY-02: Rating 5 sao + tags | Rating hiển thị đúng |
| TC-PAY-03: Rating 1 sao + comment | Lưu comment vào DB |
| TC-PAY-04: Xem biên nhận | Receipt hiển thị đầy đủ thông tin |

---

## 5. Estimated time

**Payment method: 1 giờ**
**Rating enhancement: 1.5 giờ**
**Receipt: 1 giờ**
**Testing: 30 phút**

**Tổng: ~4 giờ**
