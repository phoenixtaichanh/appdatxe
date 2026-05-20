const express = require('express');
const auth = require('../middleware/auth');
const rideRepository = require('../repositories/rideRepository');
const driverRepository = require('../repositories/driverRepository');
const { pool } = require('../database/db');

const router = express.Router();

// POST /api/rides/request
router.post('/request', auth, async (req, res, next) => {
    try {
        const { pickup_lat, pickup_lng, pickup_address, dest_lat, dest_lng, dest_address, vehicle_type } = req.body;

        if (!pickup_lat || !pickup_lng || !dest_lat || !dest_lng) {
            return res.status(400).json({ success: false, message: 'Pickup and destination coordinates required' });
        }

        const validVehicleTypes = ['motorbike', 'car_4_seats', 'car_7_seats'];
        const vehicleType = validVehicleTypes.includes(vehicle_type) ? vehicle_type : 'motorbike';

        const ride = await rideRepository.create({
            passengerId: req.user.id,
            pickupLat: pickup_lat, pickupLng: pickup_lng, pickupAddress: pickup_address || '',
            destLat: dest_lat, destLng: dest_lng, destAddress: dest_address || '',
            vehicleType: vehicleType
        });

        res.status(201).json({ success: true, message: 'Ride requested', data: ride });
    } catch (error) {
        next(error);
    }
});

// GET /api/rides (history)
router.get('/', auth, async (req, res, next) => {
    try {
        const rides = await rideRepository.findHistoryByUser(req.user.id, req.user.user_type);
        res.json({ success: true, data: rides });
    } catch (error) {
        next(error);
    }
});

// GET /api/rides/active
router.get('/active', auth, async (req, res, next) => {
    try {
        const ride = await rideRepository.findActiveByUser(req.user.id, req.user.user_type);
        res.json({ success: true, data: ride || null });
    } catch (error) {
        next(error);
    }
});

// GET /api/rides/:id
router.get('/:id', auth, async (req, res, next) => {
    try {
        const ride = await rideRepository.findById(req.params.id);
        if (!ride) {
            return res.status(404).json({ success: false, message: 'Ride not found' });
        }
        res.json({ success: true, data: ride });
    } catch (error) {
        next(error);
    }
});

// PUT /api/rides/:id/status
router.put('/:id/status', auth, async (req, res, next) => {
    try {
        const { id } = req.params;
        const { status } = req.body;

        const validStatuses = ['pending', 'accepted', 'arrived', 'in_progress', 'completed', 'cancelled'];
        if (!validStatuses.includes(status)) {
            return res.status(400).json({ success: false, message: 'Invalid status' });
        }

        const ride = await rideRepository.updateStatus(id, status);

        if (status === 'accepted' && req.user.user_type === 'driver') {
            await rideRepository.assignDriver(id, req.user.id);
            await driverRepository.setUnavailable(req.user.id, id);
        }

        if ((status === 'completed' || status === 'cancelled') && req.user.user_type === 'driver') {
            await driverRepository.setAvailable(req.user.id);
        }

        res.json({ success: true, message: 'Status updated', data: ride });
    } catch (error) {
        next(error);
    }
});

// POST /api/rides/:id/rate
router.post('/:id/rate', auth, async (req, res, next) => {
    try {
        const { id } = req.params;
        const { rating, comment, tags } = req.body;

        if (rating < 1 || rating > 5) {
            return res.status(400).json({ success: false, message: 'Rating must be 1-5' });
        }

        const ride = await rideRepository.rateWithTags(id, {
            rating, comment, raterType: req.user.user_type, tags: tags || []
        });

        res.json({ success: true, message: 'Rating submitted', data: ride });
    } catch (error) {
        next(error);
    }
});

// ========== RIDE CANCELLATION ==========

const CANCELLATION_RULES = {
    passenger_free_window_minutes: 5,
    passenger_fee_percent: 10,
    driver_fee_percent: 20,
    cancellation_reasons: [
        'driver_not_responding',
        'driver_wrong_location',
        'change_of_plans',
        'vehicle_unacceptable',
        'driver_requested',
        'passenger_requested',
        'traffic_condition',
        'other'
    ]
};

function calculateCancellationFee(ride, cancelledBy) {
    const price = parseFloat(ride.price) || 0;
    if (price <= 0) return 0;

    const createdAt = new Date(ride.created_at);
    const now = new Date();
    const minutesElapsed = (now - createdAt) / (1000 * 60);

    if (cancelledBy === 'passenger') {
        if (minutesElapsed <= CANCELLATION_RULES.passenger_free_window_minutes) {
            return 0;
        }
        return Math.round(price * (CANCELLATION_RULES.passenger_fee_percent / 100));
    }

    if (cancelledBy === 'driver') {
        if (minutesElapsed <= CANCELLATION_RULES.passenger_free_window_minutes) {
            return 0;
        }
        return Math.round(price * (CANCELLATION_RULES.driver_fee_percent / 100));
    }

    return 0;
}

router.post('/:id/cancel', auth, async (req, res, next) => {
    try {
        const { id } = req.params;
        const { reason } = req.body;

        const validReasons = CANCELLATION_RULES.cancellation_reasons;
        if (reason && !validReasons.includes(reason)) {
            return res.status(400).json({
                success: false,
                message: `Invalid reason. Valid reasons: ${validReasons.join(', ')}`
            });
        }

        const ride = await rideRepository.findById(id);
        if (!ride) {
            return res.status(404).json({ success: false, message: 'Ride not found' });
        }

        if (!['pending', 'accepted'].includes(ride.status)) {
            return res.status(400).json({
                success: false,
                message: 'Cannot cancel ride that is already in progress or completed'
            });
        }

        const userId = req.user.id;
        const isPassenger = ride.passenger_id === userId;
        const isDriver = ride.driver_id === userId;

        if (!isPassenger && !isDriver) {
            return res.status(403).json({ success: false, message: 'Not authorized to cancel this ride' });
        }

        const cancelledBy = isPassenger ? 'passenger' : 'driver';
        const cancellationFee = calculateCancellationFee(ride, cancelledBy);

        await rideRepository.updateStatus(id, 'cancelled');

        if (cancelledBy === 'driver' && ride.driver_id) {
            await driverRepository.setAvailable(ride.driver_id);
        }

        if (cancelledBy === 'passenger' && cancellationFee > 0) {
            await pool.query(
                `INSERT INTO earnings (driver_id, ride_id, amount, type, note)
                 VALUES (?, ?, ?, 'penalty', ?)`,
                [ride.driver_id, id, -cancellationFee, `Huy chuyen: ${reason || 'Khong ghi nhan'}`]
            );
        }

        const notificationService = (() => {
            try { return require('../services/notification'); } catch (e) { return null; }
        })();

        if (notificationService) {
            const targetId = cancelledBy === 'passenger' ? ride.driver_id : ride.passenger_id;
            if (targetId) {
                const messages = {
                    passenger: {
                        title: 'Chuyen da bi huy!',
                        body: `Ban da huy chuyen di #${id}. ${cancellationFee > 0 ? `Phi huy: ${cancellationFee.toLocaleString()} VND` : 'Khong co phi huy.'}`,
                        data: { rideId: String(id), type: 'ride_cancelled' }
                    },
                    driver: {
                        title: 'Chuyen da bi huy!',
                        body: `Khach hang da huy chuyen di #${id}. ${cancellationFee > 0 ? `Phi phat: ${cancellationFee.toLocaleString()} VND` : ''}`,
                        data: { rideId: String(id), type: 'ride_cancelled' }
                    }
                };
                notificationService.sendToUser(targetId, messages[cancelledBy])
                    .catch(e => console.error('[Cancel] FCM error:', e.message));
            }
        }

        res.json({
            success: true,
            message: 'Ride cancelled',
            data: {
                ride_id: id,
                cancelled_by: cancelledBy,
                cancellation_fee: cancellationFee,
                reason: reason || null
            }
        });
    } catch (error) {
        next(error);
    }
});

// ========== RIDE SEARCH / FILTER ==========

router.get('/search', auth, async (req, res, next) => {
    try {
        const { q, status, from_date, to_date, page = 1, limit = 20 } = req.query;
        const offset = (parseInt(page) - 1) * parseInt(limit);
        const userField = req.user.user_type === 'driver' ? 'r.driver_id' : 'r.passenger_id';

        let whereClause = `${userField} = ?`;
        const params = [req.user.id];

        if (status && status !== 'all') {
            whereClause += ` AND r.status = ?`;
            params.push(status);
        }

        if (from_date) {
            whereClause += ` AND DATE(r.created_at) >= ?`;
            params.push(from_date);
        }

        if (to_date) {
            whereClause += ` AND DATE(r.created_at) <= ?`;
            params.push(to_date);
        }

        let searchClause = '';
        if (q && q.trim().length > 0) {
            searchClause = ` AND (r.pickup_address LIKE ? OR r.dest_address LIKE ?)`;
            const searchTerm = `%${q.trim()}%`;
            params.push(searchTerm, searchTerm);
        }

        const [rows] = await pool.query(`
            SELECT r.*,
                   p.name as passenger_name,
                   d.name as driver_name
            FROM rides r
            LEFT JOIN users p ON r.passenger_id = p.id
            LEFT JOIN users d ON r.driver_id = d.id
            WHERE ${whereClause}${searchClause}
            ORDER BY r.created_at DESC
            LIMIT ? OFFSET ?
        `, [...params, parseInt(limit), offset]);

        const [countResult] = await pool.query(`
            SELECT COUNT(*) as total FROM rides r WHERE ${whereClause}${searchClause}`,
            params
        );

        res.json({
            success: true,
            data: rows,
            pagination: {
                page: parseInt(page),
                limit: parseInt(limit),
                total: countResult[0].total,
                total_pages: Math.ceil(countResult[0].total / parseInt(limit))
            }
        });
    } catch (error) {
        next(error);
    }
});

module.exports = router;
