const express = require('express');
const auth = require('../middleware/auth');
const rideRepository = require('../repositories/rideRepository');
const driverRepository = require('../repositories/driverRepository');

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
        const { rating, comment } = req.body;

        if (rating < 1 || rating > 5) {
            return res.status(400).json({ success: false, message: 'Rating must be 1-5' });
        }

        const ride = await rideRepository.rate(id, {
            rating, comment, raterType: req.user.user_type
        });

        res.json({ success: true, message: 'Rating submitted', data: ride });
    } catch (error) {
        next(error);
    }
});

module.exports = router;
