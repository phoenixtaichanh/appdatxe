const express = require('express');
const auth = require('../middleware/auth');
const aiRepository = require('../repositories/aiRepository');

const router = express.Router();

// ========== SCHEDULE ==========

// POST /api/ai/schedule/create
router.post('/schedule/create', auth, async (req, res, next) => {
    try {
        const { schedule_name, scheduled_date, optimization_type, waypoints } = req.body;

        if (!schedule_name || !scheduled_date || !waypoints || waypoints.length < 2) {
            return res.status(400).json({
                success: false,
                message: 'schedule_name, scheduled_date, and at least 2 waypoints are required'
            });
        }

        const schedule = await aiRepository.createSchedule(req.user.id, {
            scheduleName: schedule_name,
            scheduledDate: scheduled_date,
            optimizationType: optimization_type || 'balanced',
            waypoints
        });

        res.status(201).json({ success: true, message: 'AI schedule created', data: schedule });
    } catch (error) {
        next(error);
    }
});

// GET /api/ai/schedule/:id
router.get('/schedule/:id', auth, async (req, res, next) => {
    try {
        const schedule = await aiRepository.getScheduleById(parseInt(req.params.id), req.user.id);
        if (!schedule) {
            return res.status(404).json({ success: false, message: 'Schedule not found' });
        }
        res.json({ success: true, data: schedule });
    } catch (error) {
        next(error);
    }
});

// PUT /api/ai/schedule/:id
router.put('/schedule/:id', auth, async (req, res, next) => {
    try {
        const { schedule_name, status } = req.body;
        const schedule = await aiRepository.updateSchedule(parseInt(req.params.id), { scheduleName: schedule_name, status });
        res.json({ success: true, message: 'Schedule updated', data: schedule });
    } catch (error) {
        next(error);
    }
});

// GET /api/ai/schedule/:id/alternatives
router.get('/schedule/:id/alternatives', auth, async (req, res, next) => {
    try {
        const alternatives = await aiRepository.getAlternatives(parseInt(req.params.id));
        res.json({ success: true, data: alternatives });
    } catch (error) {
        next(error);
    }
});

// POST /api/ai/schedule/:id/optimize
router.post('/schedule/:id/optimize', auth, async (req, res, next) => {
    try {
        const { optimization_type } = req.body;
        const alternatives = await aiRepository.reoptimize(
            parseInt(req.params.id),
            optimization_type || 'balanced'
        );
        res.json({ success: true, message: 'Schedule re-optimized', data: alternatives });
    } catch (error) {
        next(error);
    }
});

// ========== AI PROFILE ==========

// GET /api/ai/profile
router.get('/profile', auth, async (req, res, next) => {
    try {
        const profile = await aiRepository.getProfile(req.user.id);
        res.json({ success: true, data: profile });
    } catch (error) {
        next(error);
    }
});

// PUT /api/ai/profile
router.put('/profile', auth, async (req, res, next) => {
    try {
        const { preferred_time_start, preferred_time_end, preference_cost_vs_time,
            frequent_locations, avoid_locations } = req.body;

        const profile = await aiRepository.updateProfile(req.user.id, {
            preferredTimeStart: preferred_time_start,
            preferredTimeEnd: preferred_time_end,
            preferenceCostVsTime: preference_cost_vs_time,
            frequentLocations: frequent_locations,
            avoidLocations: avoid_locations
        });

        res.json({ success: true, message: 'AI profile updated', data: profile });
    } catch (error) {
        next(error);
    }
});

// ========== RECOMMENDATIONS ==========

// GET /api/ai/recommendations
router.get('/recommendations', auth, async (req, res, next) => {
    try {
        const recommendations = await aiRepository.getRecommendations(req.user.id);
        res.json({ success: true, data: recommendations });
    } catch (error) {
        next(error);
    }
});

// POST /api/ai/route/preview
router.post('/route/preview', auth, async (req, res, next) => {
    try {
        const { waypoints } = req.body;

        if (!waypoints || waypoints.length < 2) {
            return res.status(400).json({
                success: false,
                message: 'At least 2 waypoints required'
            });
        }

        const preview = await aiRepository.previewRoute(waypoints);
        res.json({ success: true, data: preview });
    } catch (error) {
        next(error);
    }
});

// ========== RIDE OPTIMIZATION ==========

// POST /api/ai/rides/optimize
router.post('/rides/optimize', auth, async (req, res, next) => {
    try {
        if (req.user.user_type !== 'driver') {
            return res.status(403).json({ success: false, message: 'Driver access only' });
        }

        const { passenger_count, rides } = req.body;
        const result = await aiRepository.optimizeRides(passenger_count, rides);

        if (!result) {
            return res.status(400).json({
                success: false,
                message: 'At least 2 rides required for optimization'
            });
        }

        res.json({ success: true, data: result });
    } catch (error) {
        next(error);
    }
});

// ========== BATCH ==========

// GET /api/ai/batch/available
router.get('/batch/available', auth, async (req, res, next) => {
    try {
        if (req.user.user_type !== 'driver') {
            return res.status(403).json({ success: false, message: 'Driver access only' });
        }
        const batches = await aiRepository.getAvailableBatches(req.user.id);
        res.json({ success: true, data: batches });
    } catch (error) {
        next(error);
    }
});

// POST /api/ai/batch/:id/accept
router.post('/batch/:id/accept', auth, async (req, res, next) => {
    try {
        if (req.user.user_type !== 'driver') {
            return res.status(403).json({ success: false, message: 'Driver access only' });
        }
        const batch = await aiRepository.acceptBatch(parseInt(req.params.id), req.user.id);
        res.json({ success: true, message: 'Batch accepted', data: batch });
    } catch (error) {
        next(error);
    }
});

// POST /api/ai/batch/:id/reject
router.post('/batch/:id/reject', auth, async (req, res, next) => {
    try {
        if (req.user.user_type !== 'driver') {
            return res.status(403).json({ success: false, message: 'Driver access only' });
        }
        await aiRepository.rejectBatch(parseInt(req.params.id), req.user.id);
        res.json({ success: true, message: 'Batch rejected' });
    } catch (error) {
        next(error);
    }
});

// ========== HISTORY ==========

// GET /api/ai/history
router.get('/history', auth, async (req, res, next) => {
    try {
        const schedules = await aiRepository.getHistory(req.user.id);
        res.json({ success: true, data: schedules });
    } catch (error) {
        next(error);
    }
});

module.exports = router;
