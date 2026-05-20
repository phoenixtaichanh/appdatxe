const express = require('express');
const crypto = require('crypto-js');
const auth = require('../middleware/auth');

const router = express.Router();

// Payment methods enum
const PAYMENT_METHODS = ['cash', 'wallet', 'vnpay', 'momo'];
const PAYMENT_STATUSES = ['pending', 'completed', 'failed', 'refunded'];

// ========== VNPAY HELPER ==========

function buildVNPayUrl(amount, txnRef, orderInfo, returnUrl) {
    const vnp_TmnCode = process.env.VNPAY_TMN_CODE || '';
    const vnp_HashSecret = process.env.VNPAY_HASH_SECRET || '';
    const vnp_Url = process.env.VNPAY_URL || 'https://sandbox.vnpayment.vn/tryitnow/Home/Index';

    // If no real credentials, fall back to sandbox
    if (!vnp_TmnCode || !vnp_HashSecret || vnp_TmnCode === 'YOUR_TMN_CODE') {
        return {
            url: `${vnp_Url}?vnp_Amount=${Math.round(amount * 100)}&vnp_TxnRef=${txnRef}&vnp_OrderInfo=${encodeURIComponent(orderInfo)}`,
            isSandbox: true
        };
    }

    const vnp_Version = '2.1.0';
    const vnp_Command = 'pay';
    const vnp_Locale = 'vn';
    const vnp_CurrCode = 'VND';
    const vnp_IpAddr = '127.0.0.1';
    const vnp_CreateDate = new Date().toISOString().replace(/[-:T]/g, '').slice(0, 14);

    const params = {
        vnp_Version,
        vnp_Command,
        vnp_TmnCode,
        vnp_Locale,
        vnp_CurrCode,
        vnp_IpAddr,
        vnp_CreateDate,
        vnp_Amount: Math.round(amount * 100).toString(),
        vnp_CurrCode,
        vnp_TxnRef: txnRef.toString(),
        vnp_OrderInfo: orderInfo,
        vnp_ReturnUrl: returnUrl,
    };

    const sortedKeys = Object.keys(params).sort();
    const queryParts = sortedKeys.map(key => `${key}=${params[key]}`);
    const queryString = queryParts.join('&');
    const vnp_SecureHash = crypto.HmacSHA256(queryString, vnp_HashSecret).toString();

    return {
        url: `${vnp_Url}?${queryString}&vnp_SecureHash=${vnp_SecureHash}`,
        isSandbox: false,
        params: { ...params, vnp_SecureHash }
    };
}

// ========== MOMO HELPER ==========

function buildMoMoUrl(amount, orderId, orderInfo, returnUrl) {
    const partnerCode = process.env.MOMO_PARTNER_CODE || '';
    const accessKey = process.env.MOMO_ACCESS_KEY || '';
    const secretKey = process.env.MOMO_SECRET_KEY || '';
    const requestUrl = process.env.MOMO_URL || 'https://test-payment.momo.vn/v2/gateway/api/create';

    // If no real credentials, fall back to sandbox
    if (!partnerCode || !accessKey || !secretKey || partnerCode === 'YOUR_PARTNER_CODE') {
        return {
            url: `${requestUrl}?partnerCode=&accessKey=&amount=${amount}&orderId=${orderId}&orderInfo=${encodeURIComponent(orderInfo)}&requestId=${Date.now()}&signature=sandbox`,
            isSandbox: true
        };
    }

    const requestId = Date.now().toString();
    const requestType = 'captureWallet';
    const extraData = '';

    const rawSignature = `accessKey=${accessKey}&amount=${amount}&extraData=${extraData}&orderId=${orderId}&orderInfo=${orderInfo}&partnerCode=${partnerCode}&requestId=${requestId}&requestType=${requestType}`;

    const signature = crypto.HmacSHA256(rawSignature, secretKey).toString();

    const body = {
        partnerCode,
        accessKey,
        requestId,
        amount: amount.toString(),
        orderId,
        orderInfo,
        requestType,
        signature,
        extraData,
        returnUrl
    };

    // For MoMo, we return the URL as a string (Android SDK can open this)
    const paramsString = Object.entries(body)
        .map(([k, v]) => `${k}=${encodeURIComponent(v)}`)
        .join('&');

    return {
        url: `${requestUrl}?${paramsString}`,
        isSandbox: false,
        body
    };
}

// ========== ROUTES ==========

// GET /api/payments/methods - Get available payment methods
router.get('/methods', auth, async (req, res) => {
    res.json({
        success: true,
        data: PAYMENT_METHODS.map(m => ({
            code: m,
            label: m === 'cash' ? 'Tien mat' : m === 'wallet' ? 'Wallet' : m === 'vnpay' ? 'VNPay' : 'MoMo',
            icon: m,
            enabled: true
        }))
    });
});

// POST /api/payments/create - Create payment for a ride
router.post('/create', auth, async (req, res, next) => {
    try {
        const { ride_id, payment_method } = req.body;
        const { pool } = require('../database/db');

        if (!ride_id) {
            return res.status(400).json({ success: false, message: 'ride_id is required' });
        }

        const [ride] = await pool.query('SELECT * FROM rides WHERE id = ?', [ride_id]);
        if (ride.length === 0) {
            return res.status(404).json({ success: false, message: 'Ride not found' });
        }

        if (ride[0].passenger_id !== req.user.id) {
            return res.status(403).json({ success: false, message: 'Not authorized' });
        }

        const method = PAYMENT_METHODS.includes(payment_method) ? payment_method : 'cash';
        const amount = parseFloat(ride[0].price) || 0;

        // Check if payment already exists
        const [existing] = await pool.query(
            'SELECT * FROM transactions WHERE ride_id = ? AND status = ?',
            [ride_id, 'pending']
        );

        if (existing.length > 0) {
            return res.json({
                success: true,
                message: 'Payment already exists',
                data: {
                    id: existing[0].id,
                    payment_id: existing[0].id,
                    amount: parseFloat(existing[0].amount),
                    payment_method: existing[0].payment_method,
                    status: existing[0].status,
                    qr_url: null,
                    payment_url: null
                }
            });
        }

        const [result] = await pool.query(
            `INSERT INTO transactions (user_id, ride_id, type, category, amount, payment_method, status, description)
             VALUES (?, ?, 'income', 'ride_fare', ?, ?, 'pending', ?)`,
            [req.user.id, ride_id, amount, method, `Thanh toan chuyen di #${ride_id}`]
        );

        let qrUrl = null;
        let paymentUrl = null;
        let paymentInfo = null;

        if (method === 'vnpay') {
            const baseReturnUrl = `${process.env.APP_URL || 'http://localhost:3000'}/api/payments/${result.insertId}/vnpay-return`;
            const vnpayResult = buildVNPayUrl(amount, result.insertId, `Thanh toan chuyen di #${ride_id}`, baseReturnUrl);
            paymentUrl = vnpayResult.url;
            paymentInfo = vnpayResult;
            if (vnpayResult.isSandbox) {
                await pool.query('UPDATE transactions SET description = ? WHERE id = ?',
                    [`[SANDBOX] VNPay checkout for ride #${ride_id}`, result.insertId]);
            } else {
                await pool.query('UPDATE transactions SET description = ? WHERE id = ?',
                    [`VNPay checkout for ride #${ride_id}`, result.insertId]);
            }
        } else if (method === 'momo') {
            const baseReturnUrl = `${process.env.APP_URL || 'http://localhost:3000'}/api/payments/${result.insertId}/momo-return`;
            const momoResult = buildMoMoUrl(amount, result.insertId, `Thanh toan chuyen di #${ride_id}`, baseReturnUrl);
            paymentUrl = momoResult.url;
            paymentInfo = momoResult;
            if (momoResult.isSandbox) {
                await pool.query('UPDATE transactions SET description = ? WHERE id = ?',
                    [`[SANDBOX] MoMo checkout for ride #${ride_id}`, result.insertId]);
            } else {
                await pool.query('UPDATE transactions SET description = ? WHERE id = ?',
                    [`MoMo checkout for ride #${ride_id}`, result.insertId]);
            }
        } else if (method === 'cash') {
            await pool.query('UPDATE transactions SET status = ? WHERE id = ?',
                ['completed', result.insertId]);
        }

        res.status(201).json({
            success: true,
            message: method === 'cash' ? 'Cash payment recorded' : 'Payment created',
            data: {
                id: result.insertId,
                payment_id: result.insertId,
                amount,
                payment_method: method,
                status: method === 'cash' ? 'completed' : 'pending',
                qr_url: qrUrl,
                payment_url: paymentUrl,
                is_sandbox: paymentInfo?.isSandbox ?? true,
                payment_info: paymentInfo || null
            }
        });
    } catch (error) {
        next(error);
    }
});

// GET /api/payments/:id - Get payment details
router.get('/:id', auth, async (req, res, next) => {
    try {
        const { pool } = require('../database/db');
        const [rows] = await pool.query(
            'SELECT * FROM transactions WHERE id = ? AND user_id = ?',
            [req.params.id, req.user.id]
        );

        if (rows.length === 0) {
            return res.status(404).json({ success: false, message: 'Payment not found' });
        }

        res.json({
            success: true,
            data: {
                id: rows[0].id,
                ride_id: rows[0].ride_id,
                amount: parseFloat(rows[0].amount),
                type: rows[0].type,
                category: rows[0].category,
                payment_method: rows[0].payment_method,
                status: rows[0].status,
                description: rows[0].description,
                created_at: rows[0].created_at
            }
        });
    } catch (error) {
        next(error);
    }
});

// POST /api/payments/:id/confirm - Confirm/callback payment (for VNPay/MoMo)
router.post('/:id/confirm', auth, async (req, res, next) => {
    try {
        const { pool } = require('../database/db');
        const { status } = req.body;

        const validStatuses = ['completed', 'failed'];
        if (!validStatuses.includes(status)) {
            return res.status(400).json({ success: false, message: 'Invalid status' });
        }

        const [rows] = await pool.query(
            'SELECT * FROM transactions WHERE id = ? AND user_id = ?',
            [req.params.id, req.user.id]
        );

        if (rows.length === 0) {
            return res.status(404).json({ success: false, message: 'Payment not found' });
        }

        await pool.query(
            'UPDATE transactions SET status = ? WHERE id = ?',
            [status, req.params.id]
        );

        res.json({ success: true, message: `Payment marked as ${status}` });
    } catch (error) {
        next(error);
    }
});

// GET /api/payments/history - Get payment history
router.get('/', auth, async (req, res, next) => {
    try {
        const { pool } = require('../database/db');
        const { type, page = 1, limit = 20 } = req.query;
        const offset = (parseInt(page) - 1) * parseInt(limit);

        let whereClause = 'user_id = ?';
        const params = [req.user.id];

        if (type && type !== 'all') {
            whereClause += ' AND type = ?';
            params.push(type);
        }

        const [rows] = await pool.query(`
            SELECT t.*, r.pickup_address, r.dest_address, r.status as ride_status
            FROM transactions t
            LEFT JOIN rides r ON t.ride_id = r.id
            WHERE ${whereClause}
            ORDER BY t.created_at DESC
            LIMIT ? OFFSET ?
        `, [...params, parseInt(limit), offset]);

        const [countResult] = await pool.query(
            `SELECT COUNT(*) as total FROM transactions WHERE ${whereClause}`,
            params
        );

        res.json({
            success: true,
            data: rows.map(r => ({
                id: r.id,
                ride_id: r.ride_id,
                amount: parseFloat(r.amount),
                type: r.type,
                category: r.category,
                payment_method: r.payment_method,
                status: r.status,
                description: r.description,
                pickup_address: r.pickup_address,
                dest_address: r.dest_address,
                ride_status: r.ride_status,
                created_at: r.created_at
            })),
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

// ========== ADMIN PAYMENT ROUTES ==========

router.get('/admin/all', auth, async (req, res, next) => {
    try {
        if (req.user.user_type !== 'owner' && req.user.user_type !== 'revenue_manager') {
            return res.status(403).json({ success: false, message: 'Admin access only' });
        }

        const { pool } = require('../database/db');
        const { from_date, to_date, status, page = 1, limit = 50 } = req.query;
        const offset = (parseInt(page) - 1) * parseInt(limit);

        let whereClause = '1=1';
        const params = [];

        if (from_date) {
            whereClause += ' AND DATE(t.created_at) >= ?';
            params.push(from_date);
        }
        if (to_date) {
            whereClause += ' AND DATE(t.created_at) <= ?';
            params.push(to_date);
        }
        if (status && status !== 'all') {
            whereClause += ' AND t.status = ?';
            params.push(status);
        }

        const [rows] = await pool.query(`
            SELECT t.*, u.name as user_name, u.email as user_email,
                   r.pickup_address, r.dest_address
            FROM transactions t
            JOIN users u ON t.user_id = u.id
            LEFT JOIN rides r ON t.ride_id = r.id
            WHERE ${whereClause}
            ORDER BY t.created_at DESC
            LIMIT ? OFFSET ?
        `, [...params, parseInt(limit), offset]);

        const [summary] = await pool.query(`
            SELECT
                COALESCE(SUM(CASE WHEN status = 'completed' THEN amount ELSE 0 END), 0) as total_revenue,
                COALESCE(SUM(CASE WHEN status = 'pending' THEN amount ELSE 0 END), 0) as pending,
                COUNT(CASE WHEN status = 'completed' THEN 1 END) as completed_count,
                COUNT(CASE WHEN status = 'failed' THEN 1 END) as failed_count
            FROM transactions
            WHERE ${whereClause}
        `, params);

        const [countResult] = await pool.query(
            `SELECT COUNT(*) as total FROM transactions WHERE ${whereClause}`,
            params
        );

        res.json({
            success: true,
            data: rows,
            summary: {
                total_revenue: parseFloat(summary[0]?.total_revenue || 0),
                pending: parseFloat(summary[0]?.pending || 0),
                completed_count: parseInt(summary[0]?.completed_count || 0),
                failed_count: parseInt(summary[0]?.failed_count || 0)
            },
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
