const express = require('express');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const nodemailer = require('nodemailer');
const { pool } = require('../database/db');
const userRepository = require('../repositories/userRepository');
const auth = require('../middleware/auth');

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

// =====================================================
// PASSWORD RESET (OTP)
// =====================================================

function createTransporter() {
    if (process.env.SMTP_HOST && process.env.SMTP_USER) {
        return nodemailer.createTransport({
            host: process.env.SMTP_HOST,
            port: parseInt(process.env.SMTP_PORT || '587'),
            secure: process.env.SMTP_SECURE === 'true',
            auth: {
                user: process.env.SMTP_USER,
                pass: process.env.SMTP_PASS,
            },
        });
    }
    return null;
}

function generateOTP() {
    return Math.floor(100000 + Math.random() * 900000).toString();
}

// POST /api/auth/forgot-password
// Body: { email }
router.post('/forgot-password', async (req, res, next) => {
    try {
        const { email } = req.body;

        if (!email) {
            return res.status(400).json({ success: false, message: 'Email is required' });
        }

        const user = await userRepository.findByEmail(email.toLowerCase().trim());
        if (!user) {
            // Always return success to prevent email enumeration
            return res.json({
                success: true,
                message: 'Nếu email tồn tại trong hệ thống, mã OTP sẽ được gửi.'
            });
        }

        // Invalidate all previous unused OTPs for this email
        await pool.query(
            'UPDATE password_resets SET is_used = TRUE WHERE email = ? AND is_used = FALSE',
            [email.toLowerCase().trim()]
        );

        const otpCode = generateOTP();
        const expiresAt = new Date(Date.now() + 10 * 60 * 1000); // 10 minutes

        await pool.query(
            'INSERT INTO password_resets (email, otp_code, expires_at) VALUES (?, ?, ?)',
            [email.toLowerCase().trim(), otpCode, expiresAt]
        );

        const transporter = createTransporter();
        if (transporter) {
            await transporter.sendMail({
                from: process.env.SMTP_FROM || '"DoAn3" <noreply@doan3.vn>',
                to: email,
                subject: 'DoAn3 - Mã OTP khôi phục mật khẩu',
                html: `
                    <div style="font-family: Arial, sans-serif; max-width: 480px; margin: auto; padding: 24px; border: 1px solid #e5e7eb; border-radius: 12px;">
                        <div style="text-align: center; margin-bottom: 24px;">
                            <h1 style="color: #1e40af; font-size: 24px; margin: 0;">DoAn3</h1>
                            <p style="color: #6b7280; font-size: 14px; margin: 4px 0 0;">Khôi phục mật khẩu</p>
                        </div>
                        <div style="background: #f3f4f6; border-radius: 8px; padding: 20px; text-align: center; margin-bottom: 20px;">
                            <p style="color: #374151; font-size: 14px; margin: 0 0 8px;">Mã xác minh của bạn:</p>
                            <p style="color: #1e40af; font-size: 36px; font-weight: bold; letter-spacing: 8px; margin: 0;">${otpCode}</p>
                        </div>
                        <p style="color: #6b7280; font-size: 13px; text-align: center;">
                            Mã có hiệu lực trong <strong>10 phút</strong>.<br/>
                            Vui lòng không chia sẻ mã này với bất kỳ ai.
                        </p>
                        <hr style="border: none; border-top: 1px solid #e5e7eb; margin: 20px 0;" />
                        <p style="color: #9ca3af; font-size: 12px; text-align: center;">
                            Nếu bạn không yêu cầu khôi phục mật khẩu, hãy bỏ qua email này.
                        </p>
                    </div>
                `,
            });
        }

        res.json({
            success: true,
            message: 'Nếu email tồn tại trong hệ thống, mã OTP sẽ được gửi.',
            // Development only - include OTP in response so tester can use it without email
            ...(process.env.NODE_ENV !== 'production' && { _dev_otp: otpCode }),
        });
    } catch (error) {
        next(error);
    }
});

// POST /api/auth/verify-otp
// Body: { email, otp_code }
router.post('/verify-otp', async (req, res, next) => {
    try {
        const { email, otp_code } = req.body;

        if (!email || !otp_code) {
            return res.status(400).json({ success: false, message: 'Email and OTP code are required' });
        }

        if (!/^\d{6}$/.test(otp_code)) {
            return res.status(400).json({ success: false, message: 'OTP must be 6 digits' });
        }

        const [records] = await pool.query(
            `SELECT * FROM password_resets
             WHERE email = ? AND otp_code = ? AND is_used = FALSE AND expires_at > NOW()
             ORDER BY created_at DESC LIMIT 1`,
            [email.toLowerCase().trim(), otp_code]
        );

        if (!records.length) {
            return res.status(400).json({ success: false, message: 'Mã OTP không hợp lệ hoặc đã hết hạn' });
        }

        res.json({
            success: true,
            message: 'OTP verified successfully. You can now reset your password.',
            email: email.toLowerCase().trim(),
        });
    } catch (error) {
        next(error);
    }
});

// POST /api/auth/reset-password
// Body: { email, otp_code, new_password }
router.post('/reset-password', async (req, res, next) => {
    try {
        const { email, otp_code, new_password } = req.body;

        if (!email || !otp_code || !new_password) {
            return res.status(400).json({ success: false, message: 'Email, OTP code, and new password are required' });
        }

        if (!/^\d{6}$/.test(otp_code)) {
            return res.status(400).json({ success: false, message: 'OTP must be 6 digits' });
        }

        if (new_password.length < 6) {
            return res.status(400).json({ success: false, message: 'Password must be at least 6 characters' });
        }

        const [records] = await pool.query(
            `SELECT * FROM password_resets
             WHERE email = ? AND otp_code = ? AND is_used = FALSE AND expires_at > NOW()
             ORDER BY created_at DESC LIMIT 1`,
            [email.toLowerCase().trim(), otp_code]
        );

        if (!records.length) {
            return res.status(400).json({ success: false, message: 'Mã OTP không hợp lệ hoặc đã hết hạn' });
        }

        const hashedPassword = await bcrypt.hash(new_password, 10);
        await pool.query('UPDATE users SET password = ? WHERE email = ?', [hashedPassword, email.toLowerCase().trim()]);

        // Mark OTP as used
        await pool.query('UPDATE password_resets SET is_used = TRUE, used_at = NOW() WHERE id = ?', [records[0].id]);

        res.json({ success: true, message: 'Password reset successfully. You can now login with your new password.' });
    } catch (error) {
        next(error);
    }
});

// POST /api/auth/resend-otp
// Body: { email }
router.post('/resend-otp', async (req, res, next) => {
    try {
        const { email } = req.body;

        if (!email) {
            return res.status(400).json({ success: false, message: 'Email is required' });
        }

        const user = await userRepository.findByEmail(email.toLowerCase().trim());
        if (!user) {
            return res.json({
                success: true,
                message: 'Nếu email tồn tại trong hệ thống, mã OTP mới sẽ được gửi.'
            });
        }

        // Invalidate previous OTPs
        await pool.query(
            'UPDATE password_resets SET is_used = TRUE WHERE email = ? AND is_used = FALSE',
            [email.toLowerCase().trim()]
        );

        const otpCode = generateOTP();
        const expiresAt = new Date(Date.now() + 10 * 60 * 1000);

        await pool.query(
            'INSERT INTO password_resets (email, otp_code, expires_at) VALUES (?, ?, ?)',
            [email.toLowerCase().trim(), otpCode, expiresAt]
        );

        const transporter = createTransporter();
        if (transporter) {
            await transporter.sendMail({
                from: process.env.SMTP_FROM || '"DoAn3" <noreply@doan3.vn>',
                to: email,
                subject: 'DoAn3 - Mã OTP khôi phục mật khẩu (Mới)',
                html: `
                    <div style="font-family: Arial, sans-serif; max-width: 480px; margin: auto; padding: 24px; border: 1px solid #e5e7eb; border-radius: 12px;">
                        <div style="text-align: center; margin-bottom: 24px;">
                            <h1 style="color: #1e40af; font-size: 24px; margin: 0;">DoAn3</h1>
                            <p style="color: #6b7280; font-size: 14px; margin: 4px 0 0;">Mã OTP mới</p>
                        </div>
                        <div style="background: #f3f4f6; border-radius: 8px; padding: 20px; text-align: center; margin-bottom: 20px;">
                            <p style="color: #374151; font-size: 14px; margin: 0 0 8px;">Mã xác minh mới của bạn:</p>
                            <p style="color: #1e40af; font-size: 36px; font-weight: bold; letter-spacing: 8px; margin: 0;">${otpCode}</p>
                        </div>
                        <p style="color: #6b7280; font-size: 13px; text-align: center;">
                            Mã có hiệu lực trong <strong>10 phút</strong>.<br/>
                            Mã OTP cũ đã bị vô hiệu hóa.
                        </p>
                    </div>
                `,
            });
        }

        res.json({
            success: true,
            message: 'Nếu email tồn tại trong hệ thống, mã OTP mới sẽ được gửi.',
            ...(process.env.NODE_ENV !== 'production' && { _dev_otp: otpCode }),
        });
    } catch (error) {
        next(error);
    }
});

module.exports = router;
