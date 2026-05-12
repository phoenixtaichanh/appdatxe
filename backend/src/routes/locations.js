const express = require('express');
const auth = require('../middleware/auth');
const locationRepository = require('../repositories/locationRepository');
const driverRepository = require('../repositories/driverRepository');

const router = express.Router();

// POST /api/location/update
router.post('/update', auth, async (req, res, next) => {
    try {
        const { latitude, longitude } = req.body;

        if (latitude == null || longitude == null) {
            return res.status(400).json({ success: false, message: 'Latitude and longitude required' });
        }

        await locationRepository.upsertLocation(req.user.id, latitude, longitude);

        res.json({ success: true, message: 'Location updated' });
    } catch (error) {
        next(error);
    }
});

// GET /api/location/driver/:id
router.get('/driver/:id', auth, async (req, res, next) => {
    try {
        const location = await locationRepository.findLocationByDriver(parseInt(req.params.id));
        if (!location) {
            return res.status(404).json({ success: false, message: 'Driver location not found' });
        }
        res.json({ success: true, data: location });
    } catch (error) {
        next(error);
    }
});

// GET /api/location/nearby-drivers
router.get('/nearby-drivers', auth, async (req, res, next) => {
    try {
        const { lat, lng, radius } = req.query;

        if (!lat || !lng) {
            return res.status(400).json({ success: false, message: 'lat and lng query params required' });
        }

        const drivers = await driverRepository.findNearbyDrivers(
            parseFloat(lat),
            parseFloat(lng),
            parseFloat(radius) || 5
        );

        res.json(drivers);
    } catch (error) {
        next(error);
    }
});

module.exports = router;
