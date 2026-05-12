const express = require('express');
const auth = require('../middleware/auth');
const userRepository = require('../repositories/userRepository');
const driverRepository = require('../repositories/driverRepository');

const router = express.Router();

// GET /api/users/me
router.get('/me', auth, async (req, res, next) => {
    try {
        const user = await userRepository.findById(req.user.id);
        if (!user) {
            return res.status(404).json({ success: false, message: 'User not found' });
        }
        res.json({ success: true, data: user });
    } catch (error) {
        next(error);
    }
});

// GET /api/users/drivers/nearby
router.get('/drivers/nearby', auth, async (req, res, next) => {
    try {
        const { lat, lng, radius = 5 } = req.query;

        if (!lat || !lng) {
            return res.status(400).json({ success: false, message: 'Latitude and longitude required' });
        }

        const drivers = await driverRepository.findNearbyDrivers(
            parseFloat(lat), parseFloat(lng), parseFloat(radius)
        );

        res.json(drivers);
    } catch (error) {
        next(error);
    }
});

// GET /api/users/:id
router.get('/:id', auth, async (req, res, next) => {
    try {
        const { id } = req.params;

        const user = await userRepository.findById(id);
        if (!user) {
            return res.status(404).json({ success: false, message: 'User not found' });
        }

        res.json({ success: true, data: user });
    } catch (error) {
        next(error);
    }
});

// PUT /api/users/:id
router.put('/:id', auth, async (req, res, next) => {
    try {
        const { id } = req.params;
        const { name, phone, profile_image } = req.body;

        if (req.user.id !== parseInt(id)) {
            return res.status(403).json({ success: false, message: 'Unauthorized' });
        }

        const user = await userRepository.updateProfile(id, { name, phone, profileImage: profile_image });

        res.json({ success: true, message: 'User updated', data: user });
    } catch (error) {
        next(error);
    }
});

module.exports = router;
