const express = require('express');
const auth = require('../middleware/auth');

const router = express.Router();

function requireAdmin(req, res, next) {
    const allowed = ['owner', 'revenue_manager', 'admin'];
    if (!allowed.includes(req.user.user_type)) {
        return res.status(403).json({ success: false, message: 'Admin access required' });
    }
    next();
}

// ========== DASHBOARD ==========

router.get('/dashboard', auth, requireAdmin, async (req, res, next) => {
    try {
        const { pool } = require('../database/db');

        const [userStats] = await pool.query(`
            SELECT
                COUNT(CASE WHEN user_type = 'passenger' THEN 1 END) as total_passengers,
                COUNT(CASE WHEN user_type = 'driver' THEN 1 END) as total_drivers,
                COUNT(CASE WHEN user_type = 'passenger' AND is_active = TRUE THEN 1 END) as active_passengers,
                COUNT(CASE WHEN user_type = 'driver' AND is_active = TRUE THEN 1 END) as active_drivers
            FROM users
        `);

        const [rideStats] = await pool.query(`
            SELECT
                COUNT(*) as total_rides,
                COUNT(CASE WHEN status = 'completed' THEN 1 END) as completed_rides,
                COUNT(CASE WHEN status = 'pending' THEN 1 END) as pending_rides,
                COUNT(CASE WHEN status = 'cancelled' THEN 1 END) as cancelled_rides,
                COALESCE(SUM(CASE WHEN status = 'completed' THEN price ELSE 0 END), 0) as total_revenue
            FROM rides
        `);

        const [todayStats] = await pool.query(`
            SELECT
                COUNT(*) as rides_today,
                COALESCE(SUM(CASE WHEN status = 'completed' THEN price ELSE 0 END), 0) as revenue_today
            FROM rides
            WHERE DATE(created_at) = CURDATE()
        `);

        const [recentRides] = await pool.query(`
            SELECT r.*, p.name as passenger_name, d.name as driver_name
            FROM rides r
            LEFT JOIN users p ON r.passenger_id = p.id
            LEFT JOIN users d ON r.driver_id = d.id
            ORDER BY r.created_at DESC
            LIMIT 10
        `);

        res.json({
            success: true,
            data: {
                users: {
                    total_passengers: parseInt(userStats[0]?.total_passengers || 0),
                    total_drivers: parseInt(userStats[0]?.total_drivers || 0),
                    active_passengers: parseInt(userStats[0]?.active_passengers || 0),
                    active_drivers: parseInt(userStats[0]?.active_drivers || 0)
                },
                rides: {
                    total: parseInt(rideStats[0]?.total_rides || 0),
                    completed: parseInt(rideStats[0]?.completed_rides || 0),
                    pending: parseInt(rideStats[0]?.pending_rides || 0),
                    cancelled: parseInt(rideStats[0]?.cancelled_rides || 0),
                    total_revenue: parseFloat(rideStats[0]?.total_revenue || 0)
                },
                today: {
                    rides: parseInt(todayStats[0]?.rides_today || 0),
                    revenue: parseFloat(todayStats[0]?.revenue_today || 0)
                },
                recent_rides: recentRides
            }
        });
    } catch (error) {
        next(error);
    }
});

// ========== USER MANAGEMENT ==========

router.get('/users', auth, requireAdmin, async (req, res, next) => {
    try {
        const { pool } = require('../database/db');
        const { role, search, page = 1, limit = 20 } = req.query;
        const offset = (parseInt(page) - 1) * parseInt(limit);

        let whereClause = '1=1';
        const params = [];

        if (role && role !== 'all') {
            whereClause += ' AND user_type = ?';
            params.push(role);
        }

        if (search && search.trim()) {
            whereClause += ' AND (name LIKE ? OR email LIKE ? OR phone LIKE ?)';
            const term = `%${search.trim()}%`;
            params.push(term, term, term);
        }

        const [rows] = await pool.query(`
            SELECT id, email, name, phone, user_type, rating, total_rides, is_active, created_at
            FROM users
            WHERE ${whereClause}
            ORDER BY created_at DESC
            LIMIT ? OFFSET ?
        `, [...params, parseInt(limit), offset]);

        const [countResult] = await pool.query(
            `SELECT COUNT(*) as total FROM users WHERE ${whereClause}`,
            params
        );

        res.json({
            success: true,
            data: rows,
            pagination: {
                page: parseInt(page),
                limit: parseInt(limit),
                total: countResult[0].total
            }
        });
    } catch (error) {
        next(error);
    }
});

router.put('/users/:id/status', auth, requireAdmin, async (req, res, next) => {
    try {
        const { pool } = require('../database/db');
        const { is_active, reason } = req.body;

        await pool.query(
            'UPDATE users SET is_active = ? WHERE id = ?',
            [is_active ? 1 : 0, req.params.id]
        );

        res.json({ success: true, message: 'User status updated' });
    } catch (error) {
        next(error);
    }
});

// ========== RIDE MANAGEMENT ==========

router.get('/rides', auth, requireAdmin, async (req, res, next) => {
    try {
        const { pool } = require('../database/db');
        const { status, from_date, to_date, page = 1, limit = 20 } = req.query;
        const offset = (parseInt(page) - 1) * parseInt(limit);

        let whereClause = '1=1';
        const params = [];

        if (status && status !== 'all') {
            whereClause += ' AND r.status = ?';
            params.push(status);
        }
        if (from_date) {
            whereClause += ' AND DATE(r.created_at) >= ?';
            params.push(from_date);
        }
        if (to_date) {
            whereClause += ' AND DATE(r.created_at) <= ?';
            params.push(to_date);
        }

        const [rows] = await pool.query(`
            SELECT r.*, p.name as passenger_name, d.name as driver_name
            FROM rides r
            LEFT JOIN users p ON r.passenger_id = p.id
            LEFT JOIN users d ON r.driver_id = d.id
            WHERE ${whereClause}
            ORDER BY r.created_at DESC
            LIMIT ? OFFSET ?
        `, [...params, parseInt(limit), offset]);

        const [countResult] = await pool.query(
            `SELECT COUNT(*) as total FROM rides r WHERE ${whereClause}`,
            params
        );

        res.json({
            success: true,
            data: rows,
            pagination: {
                page: parseInt(page),
                limit: parseInt(limit),
                total: countResult[0].total
            }
        });
    } catch (error) {
        next(error);
    }
});

router.put('/rides/:id/status', auth, requireAdmin, async (req, res, next) => {
    try {
        const { pool } = require('../database/db');
        const { status } = req.body;
        const validStatuses = ['pending', 'accepted', 'arrived', 'in_progress', 'completed', 'cancelled'];

        if (!validStatuses.includes(status)) {
            return res.status(400).json({ success: false, message: 'Invalid status' });
        }

        await pool.query('UPDATE rides SET status = ? WHERE id = ?', [status, req.params.id]);

        res.json({ success: true, message: 'Ride status updated' });
    } catch (error) {
        next(error);
    }
});

// ========== DRIVER MANAGEMENT ==========

router.get('/drivers', auth, requireAdmin, async (req, res, next) => {
    try {
        const { pool } = require('../database/db');
        const { search, page = 1, limit = 20 } = req.query;
        const offset = (parseInt(page) - 1) * parseInt(limit);

        let whereClause = '1=1';
        const params = [];

        if (search && search.trim()) {
            whereClause += ' AND (u.name LIKE ? OR u.email LIKE ? OR d.license_plate LIKE ?)';
            const term = `%${search.trim()}%`;
            params.push(term, term, term);
        }

        const [rows] = await pool.query(`
            SELECT u.id, u.name, u.email, u.phone, u.rating, u.total_rides,
                   d.car_model, d.car_color, d.license_plate, d.is_available,
                   d.latitude, d.longitude,
                   (SELECT COALESCE(SUM(amount), 0) FROM earnings e WHERE e.driver_id = u.id AND type = 'ride') as total_earnings
            FROM users u
            JOIN drivers d ON d.user_id = u.id
            WHERE ${whereClause}
            ORDER BY u.created_at DESC
            LIMIT ? OFFSET ?
        `, [...params, parseInt(limit), offset]);

        const [countResult] = await pool.query(
            `SELECT COUNT(*) as total FROM users u JOIN drivers d ON d.user_id = u.id WHERE ${whereClause}`,
            params
        );

        res.json({
            success: true,
            data: rows,
            pagination: {
                page: parseInt(page),
                limit: parseInt(limit),
                total: countResult[0].total
            }
        });
    } catch (error) {
        next(error);
    }
});

// ========== STATISTICS ==========

router.get('/stats/daily', auth, requireAdmin, async (req, res, next) => {
    try {
        const { pool } = require('../database/db');
        const { days = 30 } = req.query;

        const [rows] = await pool.query(`
            SELECT
                DATE(created_at) as date,
                COUNT(*) as total_rides,
                COUNT(CASE WHEN status = 'completed' THEN 1 END) as completed,
                COALESCE(SUM(CASE WHEN status = 'completed' THEN price ELSE 0 END), 0) as revenue
            FROM rides
            WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL ? DAY)
            GROUP BY DATE(created_at)
            ORDER BY date ASC
        `, [parseInt(days)]);

        res.json({
            success: true,
            data: rows.map(r => ({
                date: r.date instanceof Date ? r.date.toISOString().split('T')[0] : r.date,
                total_rides: parseInt(r.total_rides),
                completed: parseInt(r.completed),
                revenue: parseFloat(r.revenue)
            }))
        });
    } catch (error) {
        next(error);
    }
});

router.get('/stats/revenue', auth, requireAdmin, async (req, res, next) => {
    try {
        const { pool } = require('../database/db');

        const [today] = await pool.query(`
            SELECT COALESCE(SUM(price), 0) as amount FROM rides WHERE status = 'completed' AND DATE(created_at) = CURDATE()
        `);
        const [yesterday] = await pool.query(`
            SELECT COALESCE(SUM(price), 0) as amount FROM rides WHERE status = 'completed' AND DATE(created_at) = DATE_SUB(CURDATE(), INTERVAL 1 DAY)
        `);
        const [thisMonth] = await pool.query(`
            SELECT COALESCE(SUM(price), 0) as amount FROM rides WHERE status = 'completed' AND MONTH(created_at) = MONTH(CURDATE()) AND YEAR(created_at) = YEAR(CURDATE())
        `);
        const [lastMonth] = await pool.query(`
            SELECT COALESCE(SUM(price), 0) as amount FROM rides WHERE status = 'completed' AND MONTH(created_at) = MONTH(DATE_SUB(CURDATE(), INTERVAL 1 MONTH)) AND YEAR(created_at) = YEAR(DATE_SUB(CURDATE(), INTERVAL 1 MONTH))
        `);

        const todayAmount = parseFloat(today[0]?.amount || 0);
        const yesterdayAmount = parseFloat(yesterday[0]?.amount || 0);
        const change = yesterdayAmount > 0 ? ((todayAmount - yesterdayAmount) / yesterdayAmount) * 100 : 0;

        res.json({
            success: true,
            data: {
                today: todayAmount,
                yesterday: yesterdayAmount,
                this_month: parseFloat(thisMonth[0]?.amount || 0),
                last_month: parseFloat(lastMonth[0]?.amount || 0),
                change_percent: Math.round(change * 100) / 100
            }
        });
    } catch (error) {
        next(error);
    }
});

module.exports = router;
