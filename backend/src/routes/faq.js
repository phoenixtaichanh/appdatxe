const express = require('express');
const { pool } = require('../database/db');

const router = express.Router();

// GET /api/faq - Lay tat ca FAQ
router.get('/', async (req, res, next) => {
    try {
        const { category, search } = req.query;

        let query = 'SELECT * FROM faqs WHERE is_active = TRUE';
        const params = [];

        if (category && category !== 'all') {
            query += ' AND category = ?';
            params.push(category);
        }

        if (search) {
            query += ' AND (question LIKE ? OR answer LIKE ?)';
            params.push(`%${search}%`, `%${search}%`);
        }

        query += ' ORDER BY display_order ASC, created_at DESC';

        const [faqs] = await pool.query(query, params);

        res.json(faqs);
    } catch (error) {
        next(error);
    }
});

// GET /api/faq/categories - Lay danh sach danh muc FAQ
router.get('/categories', async (req, res, next) => {
    try {
        const [categories] = await pool.query(
            `SELECT DISTINCT category, COUNT(*) as count
             FROM faqs WHERE is_active = TRUE
             GROUP BY category ORDER BY count DESC`
        );

        const categoryLabels = {
            general: { label: 'Cau hoi chung', icon: 'info', color: '#667eea' },
            booking: { label: 'Dat xe & Chuyen di', icon: 'directions_car', color: '#00C853' },
            payment: { label: 'Thanh toan', icon: 'payment', color: '#FF9800' },
            driver: { label: 'Tai xe', icon: 'person', color: '#2196F3' },
            account: { label: 'Tai khoan', icon: 'account_circle', color: '#9C27B0' },
            technical: { label: 'Ky thuat', icon: 'build', color: '#f44336' }
        };

        const result = categories.map(c => ({
            key: c.category,
            ...(categoryLabels[c.category] || { label: c.category, icon: 'help', color: '#9E9E9E' }),
            count: c.count
        }));

        res.json(result);
    } catch (error) {
        next(error);
    }
});

// GET /api/faq/:id - Lay chi tiet mot FAQ
router.get('/:id', async (req, res, next) => {
    try {
        const { id } = req.params;

        const [faq] = await pool.query(
            'SELECT * FROM faqs WHERE id = ? AND is_active = TRUE',
            [id]
        );

        if (faq.length === 0) {
            return res.status(404).json({ success: false, message: 'FAQ not found' });
        }

        await pool.query('UPDATE faqs SET view_count = view_count + 1 WHERE id = ?', [id]);

        res.json(faq[0]);
    } catch (error) {
        next(error);
    }
});

module.exports = router;
