const express = require('express');
const auth = require('../middleware/auth');
const driverRepository = require('../repositories/driverRepository');
const rideRepository = require('../repositories/rideRepository');
const { pool } = require('../database/db');

const router = express.Router();

let notificationService = null;
try {
    notificationService = require('../services/notification');
} catch (e) { /* FCM not configured */ }

function requireDriver(req, res, next) {
    if (req.user.user_type !== 'driver') {
        return res.status(403).json({ success: false, message: 'Driver access only' });
    }
    next();
}

// GET /api/driver/profile
router.get('/profile', auth, requireDriver, async (req, res, next) => {
    try {
        const driver = await driverRepository.findByUserId(req.user.id);
        if (!driver) {
            return res.status(404).json({ success: false, message: 'Driver profile not found' });
        }
        res.json(driver);
    } catch (error) {
        next(error);
    }
});

// PUT /api/driver/profile
router.put('/profile', auth, requireDriver, async (req, res, next) => {
    try {
        const { name, phone, car_model, car_color, license_plate } = req.body;

        const driver = await driverRepository.updateProfile(req.user.id, {
            name, phone, carModel: car_model, carColor: car_color, licensePlate: license_plate
        });

        res.json(driver);
    } catch (error) {
        next(error);
    }
});

// PUT /api/driver/status
router.put('/status', auth, requireDriver, async (req, res, next) => {
    try {
        const { is_available, latitude, longitude } = req.body;

        const driver = await driverRepository.updateStatus(req.user.id, {
            isAvailable: is_available, latitude: latitude || 0, longitude: longitude || 0
        });

        res.json(driver);
    } catch (error) {
        next(error);
    }
});

// GET /api/driver/ride/available
router.get('/ride/available', auth, requireDriver, async (req, res, next) => {
    try {
        const [rides] = await pool.query(`
            SELECT r.*, p.name as passenger_name, p.phone as passenger_phone
            FROM rides r
            JOIN users p ON r.passenger_id = p.id
            WHERE r.status = 'pending'
            ORDER BY r.created_at ASC
            LIMIT 20
        `);
        res.json(rides);
    } catch (error) {
        next(error);
    }
});

// POST /api/driver/ride/:id/accept
router.post('/ride/:id/accept', auth, requireDriver, async (req, res, next) => {
    try {
        const { id } = req.params;
        const [rides] = await pool.query('SELECT * FROM rides WHERE id = ? AND status = ?', [id, 'pending']);
        if (rides.length === 0) {
            return res.status(404).json({ success: false, message: 'Ride no longer available' });
        }

        await rideRepository.assignDriver(id, req.user.id);
        await rideRepository.updateStatus(id, 'accepted');
        await driverRepository.setUnavailable(req.user.id, id);

        const ride = await rideRepository.findById(id);

        // Notify passenger
        if (notificationService && ride.passengerId) {
            notificationService.sendToUser(ride.passengerId, {
                title: 'Tai xe da nhan chuyen!',
                body: `Tai xe ${ride.driverName || 'co'} da nhan chuyen cua ban`,
                data: { rideId: String(id), type: 'ride_accepted' }
            }).catch(e => console.error('[FCM] Notify passenger error:', e.message));
        }

        res.json(ride);
    } catch (error) {
        next(error);
    }
});

// POST /api/driver/ride/:id/reject
router.post('/ride/:id/reject', auth, requireDriver, async (req, res, next) => {
    try {
        const { id } = req.params;

        const [ride] = await pool.query(
            'SELECT * FROM rides WHERE id = ? AND status = ?', [id, 'pending']
        );

        if (ride.length === 0) {
            return res.status(404).json({ success: false, message: 'Ride not found or not available' });
        }

        // Ride rejection acknowledged - ride stays pending for other drivers
        res.json({ message: 'Ride rejected' });
    } catch (error) {
        next(error);
    }
});

// PUT /api/driver/ride/:id/status
router.put('/ride/:id/status', auth, requireDriver, async (req, res, next) => {
    try {
        const { id } = req.params;
        const { status } = req.body;

        const validStatuses = ['accepted', 'arrived', 'in_progress', 'completed', 'cancelled'];
        if (!validStatuses.includes(status)) {
            return res.status(400).json({ success: false, message: 'Invalid status' });
        }

        const ride = await rideRepository.updateStatus(id, status);

        if (status === 'completed') {
            const [rideRow] = await pool.query('SELECT price FROM rides WHERE id = ?', [id]);
            if (rideRow.length > 0) {
                await driverRepository.recordEarning(req.user.id, id, rideRow[0].price);
            }
            if (notificationService && ride.passengerId) {
                notificationService.sendToUser(ride.passengerId, {
                    title: 'Chuyen da hoan thanh!',
                    body: `Chuyen di #${id} da hoan thanh. Cam on ban da su dung dich vu!`,
                    data: { rideId: String(id), type: 'ride_completed' }
                }).catch(e => console.error('[FCM] Notify complete error:', e.message));
            }
        } else if (status === 'arrived' && notificationService && ride.passengerId) {
            notificationService.sendToUser(ride.passengerId, {
                title: 'Tai xe da den!',
                body: 'Tai xe da co mat tai diem don. Vui long ra ngoai.',
                data: { rideId: String(id), type: 'driver_arrived' }
            }).catch(e => console.error('[FCM] Notify arrived error:', e.message));
        }

        if (status === 'cancelled') {
            await driverRepository.setAvailable(req.user.id);
            if (notificationService && ride.passengerId) {
                notificationService.sendToUser(ride.passengerId, {
                    title: 'Chuyen da bi huy!',
                    body: `Tai xe da huy chuyen di #${id}. Vui long dat chuyen moi.`,
                    data: { rideId: String(id), type: 'ride_cancelled' }
                }).catch(e => console.error('[FCM] Notify cancel error:', e.message));
            }
        }

        res.json(ride);
    } catch (error) {
        next(error);
    }
});

// GET /api/driver/earnings
router.get('/earnings', auth, requireDriver, async (req, res, next) => {
    try {
        const { from, to } = req.query;
        const fromDate = from || new Date(new Date().setHours(0, 0, 0, 0)).toISOString().split('T')[0];
        const toDate = to || new Date().toISOString().split('T')[0];

        const earnings = await driverRepository.getEarnings(req.user.id, fromDate, toDate);

        // Get daily breakdown for chart (last 30 days)
        const [dailyRows] = await pool.query(`
            SELECT
                DATE(created_at) as date,
                COALESCE(SUM(amount), 0) as amount,
                COUNT(*) as ride_count
            FROM earnings
            WHERE driver_id = ?
              AND type = 'ride'
              AND created_at BETWEEN ? AND ?
            GROUP BY DATE(created_at)
            ORDER BY date ASC
        `, [req.user.id, fromDate, toDate]);

        // Get week comparison (current vs last week)
        const [weekCompare] = await pool.query(`
            SELECT
                COALESCE(SUM(CASE WHEN YEARWEEK(created_at) = YEARWEEK(CURDATE()) THEN amount ELSE 0 END), 0) as this_week,
                COALESCE(SUM(CASE WHEN YEARWEEK(created_at) = YEARWEEK(DATE_SUB(CURDATE(), INTERVAL 1 WEEK)) THEN amount ELSE 0 END), 0) as last_week
            FROM earnings
            WHERE driver_id = ? AND type = 'ride'
        `, [req.user.id]);

        // Get ride stats
        const [rideStats] = await pool.query(`
            SELECT
                COUNT(*) as total_rides,
                COALESCE(AVG(driver_rating), 0) as avg_rating,
                COALESCE(SUM(CASE WHEN status = 'completed' THEN 1 ELSE 0 END), 0) as completed,
                COALESCE(SUM(CASE WHEN status = 'cancelled' THEN 1 ELSE 0 END), 0) as cancelled
            FROM rides
            WHERE driver_id = ?
        `, [req.user.id]);

        res.json({
            summary: earnings,
            daily: dailyRows.map(r => ({
                date: r.date instanceof Date ? r.date.toISOString().split('T')[0] : r.date,
                amount: parseFloat(r.amount),
                ride_count: r.ride_count
            })),
            comparison: {
                this_week: parseFloat(weekCompare[0]?.this_week || 0),
                last_week: parseFloat(weekCompare[0]?.last_week || 0)
            },
            stats: {
                total_rides: parseInt(rideStats[0]?.total_rides || 0),
                avg_rating: parseFloat(rideStats[0]?.avg_rating || 0).toFixed(2),
                completed: parseInt(rideStats[0]?.completed || 0),
                cancelled: parseInt(rideStats[0]?.cancelled || 0)
            }
        });
    } catch (error) {
        next(error);
    }
});

// GET /api/driver/history
router.get('/history', auth, requireDriver, async (req, res, next) => {
    try {
        const rides = await rideRepository.findHistoryByUser(req.user.id, 'driver');
        res.json(rides);
    } catch (error) {
        next(error);
    }
});

module.exports = router;
