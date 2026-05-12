# Feature Plan #7: Profile & History

---

## 1. Mô tả

Quản lý profile người dùng và xem lịch sử chuyến đi.

---

## 2. Trạng thái hiện tại

### Backend ✅ Hoàn thành
- `GET /api/users/profile`
- `PUT /api/users/profile`
- `GET /api/rides` (history)

### Android ✅ Cơ bản
- `ProfileScreen.kt` - Cơ bản hoàn thành
- `HistoryScreen.kt` - Danh sách rides với filter

### Vấn đề cần cải thiện
1. **Profile avatar** - Chưa có chức năng upload ảnh
2. **Profile editing** - Chưa cho phép sửa phone, name
3. **History filter** - Chỉ filter cơ bản, cần thêm date range
4. **History search** - Không tìm kiếm được theo địa chỉ
5. **Favorite places** - Lưu địa điểm yêu thích

---

## 3. Implementation

### 3.1. Profile với Avatar Upload

```kotlin
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    var user by remember { mutableStateOf<User?>(null) }
    var isEditing by remember { mutableStateOf(false) }
    var showAvatarPicker by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Avatar section
        Box(modifier = Modifier.fillMaxWidth().height(200.dp),
            contentAlignment = Alignment.Center) {
            Box(modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(GradientPrimary))
                .clickable { showAvatarPicker = true },
                contentAlignment = Alignment.Center) {
                if (user?.avatarUrl != null) {
                    AsyncImage(model = user.avatarUrl, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Text(user?.name?.first().toString() ?: "U", color = Color.White, fontSize = 40.sp)
                }
            }
            // Camera icon overlay
            Box(modifier = Modifier.align(Alignment.BottomEnd).offset(x = 4.dp, y = 4.dp)
                .size(32.dp).clip(CircleShape).background(PrimaryPurple)
                .clickable { showAvatarPicker = true },
                contentAlignment = Alignment.Center) {
                Icon(Icons.Default.CameraAlt, null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }

        // Editable fields
        ProfileField(label = "Họ tên", value = user?.name ?: "", onEdit = { /* */ })
        ProfileField(label = "Email", value = user?.email ?: "", onEdit = null, enabled = false)
        ProfileField(label = "Số điện thoại", value = user?.phone ?: "", onEdit = { /* */ })
        ProfileField(label = "Ngày tham gia", value = formatDate(user?.createdAt), onEdit = null, enabled = false)

        if (user?.userType == "driver") {
            Divider(Modifier.padding(vertical = 16.dp))
            Text("Thông tin xe", fontWeight = FontWeight.Bold)
            ProfileField(label = "Loại xe", value = driver?.carModel ?: "", onEdit = { /* */ })
            ProfileField(label = "Màu xe", value = driver?.carColor ?: "", onEdit = { /* */ })
            ProfileField(label = "Biển số", value = driver?.licensePlate ?: "", onEdit = { /* */ })
        }
    }
}

@Composable
private fun ProfileField(
    label: String, value: String,
    onEdit: (() -> Unit)?,
    enabled: Boolean = true
) {
    var editing by remember { mutableStateOf(false) }
    var text by remember { mutableStateOf(value) }

    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = TextSecondary, fontSize = 12.sp)
            if (editing) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryPurple,
                        cursorColor = PrimaryPurple
                    )
                )
            } else {
                Text(value, color = TextPrimary, fontSize = 16.sp)
            }
        }
        if (onEdit != null) {
            IconButton(onClick = {
                if (editing) {
                    onEdit() // Call API
                }
                editing = !editing
            }) {
                Icon(if (editing) Icons.Default.Check else Icons.Default.Edit,
                    null, tint = PrimaryPurple)
            }
        }
    }
}
```

### 3.2. History với Advanced Filter

```kotlin
@Composable
fun HistoryScreen(isDriver: Boolean) {
    var selectedFilter by remember { mutableStateOf("all") }
    var dateRange by remember { mutableStateOf<DateRange?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Tìm kiếm địa điểm...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(12.dp)
        )

        // Filter chips
        Row(modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = selectedFilter == "all", onClick = { selectedFilter = "all" }, label = { Text("Tất cả") })
            FilterChip(selected = selectedFilter == "completed", onClick = { selectedFilter = "completed" }, label = { Text("Hoàn thành") })
            FilterChip(selected = selectedFilter == "cancelled", onClick = { selectedFilter = "cancelled" }, label = { Text("Đã hủy") })
            FilterChip(selected = selectedFilter == "date", onClick = { /* show date picker */ }, label = { Text("Ngày") })
        }

        Spacer(Modifier.height(8.dp))

        // Rides list
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(filteredRides) { ride ->
                HistoryRideCard(
                    ride = ride,
                    onClick = { onRideClick(ride.id) }
                )
            }
        }
    }
}

private fun filterRides(rides: List<Ride>, filter: String, query: String, dateRange: DateRange?): List<Ride> {
    return rides.filter { ride ->
        val matchFilter = when (filter) {
            "all" -> true
            "completed" -> ride.status == "completed"
            "cancelled" -> ride.status == "cancelled"
            else -> true
        }
        val matchQuery = query.isEmpty() ||
                ride.pickupAddress.contains(query, ignoreCase = true) ||
                ride.destAddress.contains(query, ignoreCase = true)
        val matchDate = dateRange == null ||
                (ride.createdAt.isAfter(dateRange.start) && ride.createdAt.isBefore(dateRange.end))
        matchFilter && matchQuery && matchDate
    }
}
```

---

## 4. Estimated time

**Avatar upload: 1.5 giờ**
**Profile editing: 1 giờ**
**History enhancement: 1.5 giờ**
**Testing: 30 phút**

**Tổng: ~4.5 giờ**
