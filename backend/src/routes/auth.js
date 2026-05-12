const express = require('express');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const { pool } = require('../database/db');
const userRepository = require('../repositories/userRepository');

const router = express.Router();

// POST /api/auth/register
router.post('/register', async (req, res, next) => {
    try {
        const { email, password, name, phone, user_type } = req.body;

        if (!email || !password || !name) {
            return res.status(400).json({ success: false, message: 'Email, password, and name are required' });
        }

        const existing = await userRepository.findByEmail(email);
        if (existing) {
            return res.status(409).json({ success: false, message: 'Email already registered' });
        }

        const hashedPassword = await bcrypt.hash(password, 10);

        const userId = await userRepository.create({
            email, password: hashedPassword, name, phone, userType: user_type || 'passenger'
        });

        if (user_type === 'driver') {
            await pool.query('INSERT INTO drivers (user_id) VALUES (?)', [userId]);
        }

        const token = jwt.sign(
            { id: userId, email, name, user_type: user_type || 'passenger' },
            process.env.JWT_SECRET,
            { expiresIn: '30d' }
        );

        res.status(201).json({
            success: true,
            message: 'Registration successful',
            token,
            user: {
                id: userId,
                email,
                name,
                phone: phone || null,
                user_type: user_type || 'passenger',
                rating: 5.0,
                total_rides: 0
            }
        });
    } catch (error) {
        next(error);
    }
});

// POST /api/auth/login
router.post('/login', async (req, res, next) => {
    try {
        const { email, password } = req.body;

        if (!email || !password) {
            return res.status(400).json({ success: false, message: 'Email and password are required' });
        }

        const user = await userRepository.findByEmail(email);
        if (!user) {
            return res.status(401).json({ success: false, message: 'Invalid email or password' });
        }

        const isValidPassword = await bcrypt.compare(password, user.password);
        if (!isValidPassword) {
            return res.status(401).json({ success: false, message: 'Invalid email or password' });
        }

        const token = jwt.sign(
            { id: user.id, email: user.email, name: user.name, user_type: user.user_type },
            process.env.JWT_SECRET,
            { expiresIn: '30d' }
        );

        res.json({
            success: true,
            message: 'Login successful',
            token,
            user: {
                id: user.id,
                email: user.email,
                name: user.name,
                phone: user.phone,
                user_type: user.user_type,
                profile_image: user.profile_image,
                rating: user.rating,
                total_rides: user.total_rides,
                created_at: user.created_at
            }
        });
    } catch (error) {
        next(error);
    }
});

// Register FCM token
router.post('/fcm/register', auth, async (req, res, next) => {
    try {
        const { fcm_token, device_id } = req.body;
        if (!fcm_token) {
            return res.status(400).json({ success: false, message: 'FCM token required' });
        }

        await pool.query(
            `INSERT INTO user_fcm_tokens (user_id, fcm_token, device_id)
             VALUES (?, ?, ?)
             ON DUPLICATE KEY UPDATE fcm_token = VALUES(fcm_token), updated_at = NOW()`,
            [req.user.id, fcm_token, device_id || null]
        );

        res.json({ success: true, message: 'FCM token registered' });
    } catch (error) {
        next(error);
    }
});

module.exports = router;
