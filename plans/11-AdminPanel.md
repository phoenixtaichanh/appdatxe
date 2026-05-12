# Feature Plan #11: Admin Panel (Web)

---

## 1. Mô tả

Web admin panel để quản lý hệ thống (tài xế, khách hàng, chuyến đi, thống kê).

---

## 2. Trạng thái hiện tại

### Backend ❌ Chưa implement
- Không có admin routes
- Không có admin authentication

### Android ❌ Không áp dụng

---

## 3. Implementation Overview

### 3.1. Admin Authentication

```javascript
// backend/src/middleware/adminAuth.js
const jwt = require('jsonwebtoken');

function adminAuth(req, res, next) {
    const token = req.headers.authorization?.split(' ')[1];
    if (!token) return res.status(401).json({ success: false, message: 'No token' });
    try {
        const decoded = jwt.verify(token, process.env.JWT_SECRET);
        if (decoded.user_type !== 'admin') {
            return res.status(403).json({ success: false, message: 'Admin only' });
        }
        req.user = decoded;
        next();
    } catch (e) {
        res.status(401).json({ success: false, message: 'Invalid token' });
    }
}

module.exports = adminAuth;
```

### 3.2. Admin Routes

```javascript
// backend/src/routes/admin.js
const router = express.Router();
const adminAuth = require('../middleware/adminAuth');

// GET /api/admin/stats - Dashboard stats
router.get('/stats', adminAuth, async (req, res) => {
    const [[ridesStats]] = await pool.query(`
        SELECT
            COUNT(*) as total_rides,
            SUM(CASE WHEN status = 'completed' THEN 1 ELSE 0 END) as completed,
            SUM(CASE WHEN status = 'cancelled' THEN 1 ELSE 0 END) as cancelled,
            SUM(CASE WHEN DATE(created_at) = CURDATE() THEN 1 ELSE 0 END) as today_rides
        FROM rides
    `);
    const [[driversStats]] = await pool.query(`
        SELECT
            COUNT(*) as total_drivers,
            SUM(CASE WHEN is_available = TRUE THEN 1 ELSE 0 END) as online
        FROM drivers
    `);
    const [[revenueStats]] = await pool.query(`
        SELECT
            SUM(CASE WHEN DATE(created_at) = CURDATE() THEN amount ELSE 0 END) as today_revenue,
            SUM(CASE WHEN MONTH(created_at) = MONTH(CURDATE()) THEN amount ELSE 0 END) as month_revenue
        FROM earnings
    `);
    res.json({ success: true, data: { ridesStats, driversStats, revenueStats } });
});

// GET /api/admin/users - User management
router.get('/users', adminAuth, async (req, res) => {
    const [users] = await pool.query(`
        SELECT u.*, d.is_available, d.car_model, d.license_plate
        FROM users u
        LEFT JOIN drivers d ON u.id = d.user_id
        ORDER BY u.created_at DESC
        LIMIT 50
    `);
    res.json({ success: true, data: users });
});

// PUT /api/admin/users/:id - Update user status
router.put('/users/:id', adminAuth, async (req, res) => {
    const { is_banned } = req.body;
    await pool.query('UPDATE users SET is_banned = ? WHERE id = ?', [is_banned, req.params.id]);
    res.json({ success: true, message: 'User updated' });
});

// GET /api/admin/rides - Ride management
router.get('/rides', adminAuth, async (req, res) => {
    const [rides] = await pool.query(`
        SELECT r.*, p.name as passenger_name, d.name as driver_name
        FROM rides r
        LEFT JOIN users p ON r.passenger_id = p.id
        LEFT JOIN users d ON r.driver_id = d.id
        ORDER BY r.created_at DESC
        LIMIT 100
    `);
    res.json({ success: true, data: rides });
});

// GET /api/admin/drivers - Driver management
router.get('/drivers', adminAuth, async (req, res) => {
    const [drivers] = await pool.query(`
        SELECT u.*, d.*
        FROM drivers d
        JOIN users u ON d.user_id = u.id
        ORDER BY u.rating DESC
    `);
    res.json({ success: true, data: drivers });
});

// POST /api/admin/promotions - Create promotion
router.post('/promotions', adminAuth, async (req, res) => {
    const { title, code, discount_percent, min_rides } = req.body;
    await pool.query(
        'INSERT INTO promotions (title, code, discount_percent, min_rides) VALUES (?, ?, ?, ?)',
        [title, code, discount_percent, min_rides]
    );
    res.json({ success: true, message: 'Promotion created' });
});
```

### 3.3. Frontend (React/Vue) Structure

```
admin/
├── src/
│   ├── pages/
│   │   ├── Dashboard.vue         # Stats overview
│   │   ├── Users.vue             # User management
│   │   ├── Drivers.vue           # Driver management
│   │   ├── Rides.vue             # Ride management
│   │   ├── Earnings.vue          # Revenue & reports
│   │   └── Promotions.vue        # Discount codes
│   ├── components/
│   │   ├── StatCard.vue
│   │   ├── DataTable.vue
│   │   └── Sidebar.vue
│   └── App.vue
```

---

## 4. Screens

| Screen | Mô tả |
|---|---|
| Dashboard | Cards: Tổng rides, Tài xế online, Doanh thu hôm nay/tháng |
| Users | Table: Avatar, Tên, Email, Loại, Trạng thái, Actions (ban/unban) |
| Drivers | Table: Tên, Xe, Biển số, Rating, Online status, Tổng chuyến |
| Rides | Table: ID, Khách, Tài xế, Tuyến đường, Giá, Trạng thái |
| Earnings | Biểu đồ doanh thu theo ngày/tháng |
| Promotions | CRUD mã giảm giá |

---

## 5. Estimated time

**Admin auth middleware: 30 phút**
**Admin API routes: 2 giờ**
**Admin frontend (React): 8 giờ**
**Testing: 2 giờ**

**Tổng: ~12 giờ** (nếu có thời gian)
