/**
 * Admin Support Routes
 * Quan ly FAQ va chat tu van cho Admin Panel
 * Su dung adminAuth (khac voi mobile app su dung auth cua khach hang)
 */
const express = require('express');
const { adminAuth, requirePermission } = require('../middleware/adminAuth');
const { pool } = require('../database/db');

const router = express.Router();

// ============================================================
// FAQ MANAGEMENT
// ============================================================

// GET /api/admin-support/faqs - Danh sach FAQ
router.get('/faqs', adminAuth, requirePermission('manage_faqs'), async (req, res, next) => {
    try {
        const { category, search, page = 1, limit = 20 } = req.query;
        const offset = (parseInt(page) - 1) * parseInt(limit);

        let sql = 'SELECT * FROM faqs WHERE 1=1';
        const params = [];

        if (category && category !== 'all') {
            sql += ' AND category = ?';
            params.push(category);
        }
        if (search) {
            sql += ' AND (question LIKE ? OR answer LIKE ?)';
            params.push(`%${search}%`, `%${search}%`);
        }

        const [countRows] = await pool.query(
            sql.replace('SELECT *', 'SELECT COUNT(*) as total'),
            params
        );

        sql += ' ORDER BY display_order ASC, created_at DESC LIMIT ? OFFSET ?';
        params.push(parseInt(limit), offset);
        const [rows] = await pool.query(sql, params);

        res.json({
            success: true,
            data: rows,
            pagination: {
                page: parseInt(page),
                limit: parseInt(limit),
                total: countRows[0].total
            }
        });
    } catch (error) {
        next(error);
    }
});

// POST /api/admin-support/faqs - Tao FAQ moi
router.post('/faqs', adminAuth, requirePermission('manage_faqs'), async (req, res, next) => {
    try {
        const { question, answer, category, display_order = 0 } = req.body;

        if (!question || !answer) {
            return res.status(400).json({ success: false, message: 'Cau hoi va cau tra loi la bat buoc' });
        }

        const [result] = await pool.query(
            `INSERT INTO faqs (question, answer, category, display_order, is_active)
             VALUES (?, ?, ?, ?, TRUE)`,
            [question, answer, category || 'general', parseInt(display_order)]
        );

        const [newFaq] = await pool.query('SELECT * FROM faqs WHERE id = ?', [result.insertId]);

        res.status(201).json({ message: 'FAQ da duoc tao', data: newFaq[0] });
    } catch (error) {
        next(error);
    }
});

// PUT /api/admin-support/faqs/:id - Cap nhat FAQ
router.put('/faqs/:id', adminAuth, requirePermission('manage_faqs'), async (req, res, next) => {
    try {
        const { question, answer, category, display_order, is_active } = req.body;
        const fields = [];
        const params = [];

        if (question !== undefined) { fields.push('question = ?'); params.push(question); }
        if (answer !== undefined) { fields.push('answer = ?'); params.push(answer); }
        if (category !== undefined) { fields.push('category = ?'); params.push(category); }
        if (display_order !== undefined) { fields.push('display_order = ?'); params.push(parseInt(display_order)); }
        if (is_active !== undefined) { fields.push('is_active = ?'); params.push(is_active); }

        if (!fields.length) {
            return res.status(400).json({ success: false, message: 'Khong co truong nao de cap nhat' });
        }

        params.push(req.params.id);
        await pool.query(`UPDATE faqs SET ${fields.join(', ')} WHERE id = ?`, params);

        const [updated] = await pool.query('SELECT * FROM faqs WHERE id = ?', [req.params.id]);
        res.json({ message: 'FAQ da duoc cap nhat', data: updated[0] });
    } catch (error) {
        next(error);
    }
});

// DELETE /api/admin-support/faqs/:id - Xoa FAQ
router.delete('/faqs/:id', adminAuth, requirePermission('manage_faqs'), async (req, res, next) => {
    try {
        await pool.query('UPDATE faqs SET is_active = FALSE WHERE id = ?', [req.params.id]);
        res.json({ message: 'FAQ da duoc xoa' });
    } catch (error) {
        next(error);
    }
});

// ============================================================
// CONSULTANT CONVERSATION MANAGEMENT
// ============================================================

// GET /api/admin-support/conversations - Tat ca cuoc tro chuyen
router.get('/conversations', adminAuth, requirePermission('view_consultant_chats'), async (req, res, next) => {
    try {
        const { status, category, page = 1, limit = 20 } = req.query;
        const offset = (parseInt(page) - 1) * parseInt(limit);

        let sql = 'SELECT cc.*, u.name as customer_name, u.email as customer_email, ' +
            'c.name as consultant_name, ' +
            '(SELECT COUNT(*) FROM consultant_messages cm WHERE cm.conversation_id = cc.id ' +
            'AND cm.is_read = FALSE AND cm.sender_type = \'customer\') as unread_count, ' +
            '(SELECT message FROM consultant_messages WHERE conversation_id = cc.id ' +
            'ORDER BY created_at DESC LIMIT 1) as last_message ' +
            'FROM consultant_conversations cc ' +
            'JOIN users u ON cc.customer_id = u.id ' +
            'LEFT JOIN users c ON cc.consultant_id = c.id WHERE 1=1';
        const params = [];

        if (status && status !== 'all') {
            sql += ' AND cc.status = ?';
            params.push(status);
        }
        if (category && category !== 'all') {
            sql += ' AND cc.category = ?';
            params.push(category);
        }

        sql += ' ORDER BY cc.priority DESC, cc.created_at DESC';

        const [countRows] = await pool.query(
            'SELECT COUNT(*) as total FROM consultant_conversations cc JOIN users u ON cc.customer_id = u.id WHERE 1=1',
            params
        );

        sql += ' LIMIT ? OFFSET ?';
        params.push(parseInt(limit), offset);
        const [rows] = await pool.query(sql, params);

        res.json({
            data: rows,
            pagination: {
                page: parseInt(page),
                limit: parseInt(limit),
                total: countRows[0].total
            }
        });
    } catch (error) {
        next(error);
    }
});

// GET /api/admin-support/conversations/:id/messages - Tin nhan cua mot cuoc tro chuyen
router.get('/conversations/:id/messages', adminAuth, requirePermission('view_consultant_chats'), async (req, res, next) => {
    try {
        const [messages] = await pool.query(
            'SELECT * FROM consultant_messages WHERE conversation_id = ? ORDER BY created_at ASC',
            [req.params.id]
        );

        await pool.query(
            `UPDATE consultant_messages SET is_read = TRUE, read_at = NOW()
             WHERE conversation_id = ? AND sender_type = 'customer' AND is_read = FALSE`,
            [req.params.id]
        );

        res.json(messages);
    } catch (error) {
        next(error);
    }
});

// POST /api/admin-support/conversations/:id/reply - Tra loi khach hang
router.post('/conversations/:id/reply', adminAuth, requirePermission('reply_consultant'), async (req, res, next) => {
    try {
        const { message } = req.body;
        if (!message?.trim()) {
            return res.status(400).json({ success: false, message: 'Tin nhan khong duoc trong' });
        }

        const [conv] = await pool.query(
            'SELECT * FROM consultant_conversations WHERE id = ?',
            [req.params.id]
        );
        if (!conv.length) {
            return res.status(404).json({ success: false, message: 'Khong tim thay cuoc tro chuyen' });
        }
        if (['resolved', 'closed'].includes(conv[0].status)) {
            return res.status(400).json({ success: false, message: 'Cuoc tro chuyen da ket thuc' });
        }

        // Gan consultant neu chua co
        if (!conv[0].consultant_id) {
            await pool.query(
                `UPDATE consultant_conversations
                 SET consultant_id = ?, status = 'active', first_response_at = NOW()
                 WHERE id = ?`,
                [req.admin.id, req.params.id]
            );
        }

        const [result] = await pool.query(
            `INSERT INTO consultant_messages
             (conversation_id, sender_id, sender_type, sender_name, message)
             VALUES (?, ?, 'consultant', ?, ?)`,
            [req.params.id, req.admin.id, req.admin.name, message.trim()]
        );

        await pool.query(
            'UPDATE consultant_conversations SET updated_at = NOW() WHERE id = ?',
            [req.params.id]
        );

        const newMessage = {
            id: result.insertId,
            conversation_id: parseInt(req.params.id),
            sender_id: req.admin.id,
            sender_type: 'consultant',
            sender_name: req.admin.name,
            message: message.trim(),
            message_type: 'text',
            is_read: false,
            created_at: new Date().toISOString()
        };

        // Gui Socket.IO
        let socketIO = null;
        try {
            const socketModule = require('../socket/index');
            socketIO = socketModule.getIO();
        } catch (e) { /* socket not init */ }

        if (socketIO) {
            socketIO.to(`user_${conv[0].customer_id}`).emit('consultant:message', newMessage);
            socketIO.to('admins').emit('consultant:message', newMessage);
        }

        res.status(201).json(newMessage);
    } catch (error) {
        next(error);
    }
});

// PUT /api/admin-support/conversations/:id/resolve - Danh dau giai quyet
router.put('/conversations/:id/resolve', adminAuth, requirePermission('reply_consultant'), async (req, res, next) => {
    try {
        await pool.query(
            `UPDATE consultant_conversations SET status = 'resolved', resolved_at = NOW() WHERE id = ?`,
            [req.params.id]
        );
        res.json({ message: 'Da danh dau giai quyet' });
    } catch (error) {
        next(error);
    }
});

// PUT /api/admin-support/conversations/:id/assign - Gan tu van vien
router.put('/conversations/:id/assign', adminAuth, requirePermission('manage_consultants'), async (req, res, next) => {
    try {
        const { consultantId } = req.body;
        await pool.query(
            `UPDATE consultant_conversations SET consultant_id = ?, status = 'active' WHERE id = ?`,
            [consultantId, req.params.id]
        );
        res.json({ message: 'Da phan cong tu van vien' });
    } catch (error) {
        next(error);
    }
});

// GET /api/admin-support/conversations/:id - Chi tiet cuoc tro chuyen
router.get('/conversations/:id', adminAuth, requirePermission('view_consultant_chats'), async (req, res, next) => {
    try {
        const [conv] = await pool.query(`
            SELECT cc.*, u.name as customer_name, u.email as customer_email, u.phone as customer_phone,
                   c.name as consultant_name
            FROM consultant_conversations cc
            JOIN users u ON cc.customer_id = u.id
            LEFT JOIN users c ON cc.consultant_id = c.id
            WHERE cc.id = ?
        `, [req.params.id]);

        if (!conv.length) {
            return res.status(404).json({ success: false, message: 'Khong tim thay cuoc tro chuyen' });
        }

        res.json(conv[0]);
    } catch (error) {
        next(error);
    }
});

module.exports = router;
