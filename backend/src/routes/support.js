const express = require('express');
const auth = require('../middleware/auth');
const { pool } = require('../database/db');

const router = express.Router();

// GET /api/faq - Lấy tất cả FAQ
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

// GET /api/faq/categories - Lấy danh sách danh mục FAQ
router.get('/categories', async (req, res, next) => {
    try {
        const [categories] = await pool.query(
            `SELECT DISTINCT category, COUNT(*) as count
             FROM faqs WHERE is_active = TRUE
             GROUP BY category ORDER BY count DESC`
        );

        const categoryLabels = {
            general: { label: 'Câu hỏi chung', icon: 'info', color: '#667eea' },
            booking: { label: 'Đặt xe & Chuyến đi', icon: 'directions_car', color: '#00C853' },
            payment: { label: 'Thanh toán', icon: 'payment', color: '#FF9800' },
            driver: { label: 'Tài xế', icon: 'person', color: '#2196F3' },
            account: { label: 'Tài khoản', icon: 'account_circle', color: '#9C27B0' },
            technical: { label: 'Kỹ thuật', icon: 'build', color: '#f44336' }
        };

        const result = categories.map(c => ({
            key: c.category,
            ...categoryLabels[c.category] || { label: c.category, icon: 'help', color: '#9E9E9E' },
            count: c.count
        }));

        res.json(result);
    } catch (error) {
        next(error);
    }
});

// GET /api/faq/:id - Lấy chi tiết một FAQ
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

// POST /api/faq/:id/helpful - Đánh dấu FAQ hữu ích
router.post('/:id/helpful', auth, async (req, res, next) => {
    try {
        const { id } = req.params;
        const { helpful } = req.body;

        if (helpful) {
            await pool.query('UPDATE faqs SET helpful_count = helpful_count + 1 WHERE id = ?', [id]);
        } else {
            await pool.query('UPDATE faqs SET unhelpful_count = unhelpful_count + 1 WHERE id = ?', [id]);
        }

        res.json({ success: true, message: 'Cảm ơn bạn đã phản hồi!' });
    } catch (error) {
        next(error);
    }
});

// ====== CONSULTANT CHAT ROUTES ======

// GET /api/support/conversations - Lấy danh sách cuộc trò chuyện
router.get('/conversations', auth, async (req, res, next) => {
    try {
        const { status, category } = req.query;
        const userId = req.user.id;
        const userType = req.user.user_type;

        // Build conversation query with all necessary JOINs
        const baseQuery = 'SELECT cc.*, u.name as customer_name, u.email as customer_email, c.name as consultant_name, ' +
            '(SELECT COUNT(*) FROM consultant_messages cm WHERE cm.conversation_id = cc.id AND cm.sender_id != ? AND cm.is_read = FALSE) as unread_count, ' +
            '(SELECT message FROM consultant_messages WHERE conversation_id = cc.id ORDER BY created_at DESC LIMIT 1) as last_message, ' +
            '(SELECT created_at FROM consultant_messages WHERE conversation_id = cc.id ORDER BY created_at DESC LIMIT 1) as last_message_at ' +
            'FROM consultant_conversations cc ' +
            'JOIN users u ON cc.customer_id = u.id ' +
            'LEFT JOIN users c ON cc.consultant_id = c.id';

        let fullQuery = baseQuery;
        const params = [];

        if (userType === 'passenger') {
            params.push(userId);
            fullQuery += ' WHERE cc.customer_id = ?';
            if (status && status !== 'all') { fullQuery += ' AND cc.status = ?'; params.push(status); }
            if (category && category !== 'all') { fullQuery += ' AND cc.category = ?'; params.push(category); }
            fullQuery += ' ORDER BY cc.priority DESC, cc.created_at DESC';
        } else if (userType === 'consultant') {
            params.push(userId);
            fullQuery += ' WHERE (cc.consultant_id = ? OR (cc.consultant_id IS NULL AND cc.status = "waiting"))';
            if (status && status !== 'all') { fullQuery += ' AND cc.status = ?'; params.push(status); }
            if (category && category !== 'all') { fullQuery += ' AND cc.category = ?'; params.push(category); }
            fullQuery += ' ORDER BY cc.priority DESC, cc.created_at DESC';
        } else if (['owner', 'admin'].includes(userType)) {
            params.push(userId);
            fullQuery += ' WHERE 1=1';
            if (status && status !== 'all') { fullQuery += ' AND cc.status = ?'; params.push(status); }
            if (category && category !== 'all') { fullQuery += ' AND cc.category = ?'; params.push(category); }
            fullQuery += ' ORDER BY cc.priority DESC, cc.created_at DESC';
        } else {
            return res.json({ success: true, data: [] });
        }

        const [rows] = await pool.query(fullQuery, params);
        res.json(rows);
    } catch (error) {
        next(error);
    }
});

// POST /api/support/conversations - Tạo cuộc trò chuyện mới (khách hàng)
// POST /api/support/conversations - Consultant nhận cuộc trò chuyện
router.post('/conversations', auth, async (req, res, next) => {
    try {
        const { subject, category, message } = req.body;
        const userId = req.user.id;
        const userType = req.user.user_type;

        // Customer: tạo cuộc trò chuyện mới
        if (userType === 'passenger') {
            if (!message || message.trim().length === 0) {
                return res.status(400).json({ success: false, message: 'Vui lòng nhập tin nhắn đầu tiên' });
            }

            const [result] = await pool.query(
                `INSERT INTO consultant_conversations
                 (customer_id, subject, category, status, first_response_at)
                 VALUES (?, ?, ?, 'waiting', NOW())`,
                [userId, subject || 'Hỗ trợ chung', category || 'general']
            );

            const conversationId = result.insertId;

            // Lưu tin nhắn đầu tiên
            await pool.query(
                `INSERT INTO consultant_messages
                 (conversation_id, sender_id, sender_type, sender_name, message)
                 VALUES (?, ?, 'customer', ?, ?)`,
                [conversationId, userId, req.user.name, message.trim()]
            );

            res.status(201).json({
                success: true,
                message: 'Da gui yeu cau ho tro. Chung toi se phan hoi som nhat co the!',
                conversation_id: conversationId
            });
        }
        // Consultant: nhận cuộc trò chuyện waiting
        else if (userType === 'consultant') {
            const [waiting] = await pool.query(
                `SELECT * FROM consultant_conversations
                 WHERE status = 'waiting' AND consultant_id IS NULL
                 ORDER BY priority DESC, created_at ASC LIMIT 1`
            );

            if (waiting.length === 0) {
                return res.status(404).json({ success: false, message: 'Không có cuộc trò chuyện nào đang chờ' });
            }

            await pool.query(
                `UPDATE consultant_conversations
                 SET consultant_id = ?, status = 'active', first_response_at = NOW()
                 WHERE id = ?`,
                [userId, waiting[0].id]
            );

            res.json({
                success: true,
                message: 'Đã nhận cuộc trò chuyện',
                data: { conversation_id: waiting[0].id }
            });
        } else {
            res.status(403).json({ success: false, message: 'Bạn không có quyền thực hiện thao tác này' });
        }
    } catch (error) {
        next(error);
    }
});

// GET /api/support/conversations/:id/messages - Lấy tin nhắn trong cuộc trò chuyện
router.get('/conversations/:id/messages', auth, async (req, res, next) => {
    try {
        const { id } = req.params;
        const userId = req.user.id;
        const userType = req.user.user_type;

        // Kiểm tra quyền truy cập
        const [conv] = await pool.query(
            'SELECT * FROM consultant_conversations WHERE id = ?',
            [id]
        );

        if (conv.length === 0) {
            return res.status(404).json({ success: false, message: 'Không tìm thấy cuộc trò chuyện' });
        }

        const conversation = conv[0];
        const hasAccess =
            conversation.customer_id === userId ||
            conversation.consultant_id === userId ||
            ['owner', 'admin'].includes(userType);

        if (!hasAccess) {
            return res.status(403).json({ success: false, message: 'Bạn không có quyền xem cuộc trò chuyện này' });
        }

        // Lấy tin nhắn
        const [messages] = await pool.query(
            `SELECT * FROM consultant_messages
             WHERE conversation_id = ?
             ORDER BY created_at ASC`,
            [id]
        );

        // Đánh dấu đã đọc (tin nhắn không phải của mình)
        await pool.query(
            `UPDATE consultant_messages
             SET is_read = TRUE, read_at = NOW()
             WHERE conversation_id = ? AND sender_id != ? AND is_read = FALSE`,
            [id, userId]
        );

        res.json(messages);
    } catch (error) {
        next(error);
    }
});

// POST /api/support/conversations/:id/messages - Gửi tin nhắn
router.post('/conversations/:id/messages', auth, async (req, res, next) => {
    try {
        const { id } = req.params;
        const { message, message_type = 'text' } = req.body;
        const userId = req.user.id;
        const userType = req.user.user_type;

        if (!message || message.trim().length === 0) {
            return res.status(400).json({ success: false, message: 'Tin nhắn không được trống' });
        }

        // Kiểm tra quyền
        const [conv] = await pool.query(
            'SELECT * FROM consultant_conversations WHERE id = ?',
            [id]
        );

        if (conv.length === 0) {
            return res.status(404).json({ success: false, message: 'Không tìm thấy cuộc trò chuyện' });
        }

        const conversation = conv[0];
        const hasAccess =
            conversation.customer_id === userId ||
            conversation.consultant_id === userId ||
            ['owner', 'admin'].includes(userType);

        if (!hasAccess) {
            return res.status(403).json({ success: false, message: 'Bạn không có quyền gửi tin nhắn' });
        }

        if (['resolved', 'closed'].includes(conversation.status)) {
            return res.status(400).json({ success: false, message: 'Cuộc trò chuyện đã kết thúc' });
        }

        const senderType = userType === 'consultant' ? 'consultant' : 'customer';

        // Lưu tin nhắn
        const [result] = await pool.query(
            `INSERT INTO consultant_messages
             (conversation_id, sender_id, sender_type, sender_name, message, message_type)
             VALUES (?, ?, ?, ?, ?, ?)`,
            [id, userId, senderType, req.user.name, message.trim(), message_type]
        );

        // Cập nhật cuộc trò chuyện
        await pool.query(
            'UPDATE consultant_conversations SET updated_at = NOW() WHERE id = ?',
            [id]
        );

        const newMessage = {
            id: result.insertId,
            conversation_id: parseInt(id),
            sender_id: userId,
            sender_type: senderType,
            sender_name: req.user.name,
            message: message.trim(),
            message_type,
            is_read: false,
            created_at: new Date().toISOString()
        };

        // Gửi real-time qua Socket.IO
        let socketIO = null;
        try {
            const socketModule = require('../socket/index');
            socketIO = socketModule.getIO();
        } catch (e) { /* Socket not initialized */ }

        if (socketIO) {
            // Gửi cho khách hàng
            if (userType === 'consultant' || userType === 'owner' || userType === 'admin') {
                socketIO.to(`user_${conversation.customer_id}`).emit('consultant:message', newMessage);
            }
            // Gửi cho consultant
            if (conversation.consultant_id) {
                socketIO.to(`user_${conversation.consultant_id}`).emit('consultant:message', newMessage);
            }
            // Gửi cho admin/owner
            socketIO.to('admins').emit('consultant:message', newMessage);
        }

        res.status(201).json(newMessage);
    } catch (error) {
        next(error);
    }
});

// PUT /api/support/conversations/:id/resolve - Đánh dấu đã giải quyết
router.put('/conversations/:id/resolve', auth, async (req, res, next) => {
    try {
        const { id } = req.params;
        const userId = req.user.id;
        const userType = req.user.user_type;

        const [conv] = await pool.query(
            'SELECT * FROM consultant_conversations WHERE id = ?',
            [id]
        );

        if (conv.length === 0) {
            return res.status(404).json({ success: false, message: 'Không tìm thấy cuộc trò chuyện' });
        }

        const conversation = conv[0];
        const hasAccess =
            conversation.consultant_id === userId ||
            ['owner', 'admin'].includes(userType);

        if (!hasAccess) {
            return res.status(403).json({ success: false, message: 'Bạn không có quyền' });
        }

        await pool.query(
            `UPDATE consultant_conversations
             SET status = 'resolved', resolved_at = NOW()
             WHERE id = ?`,
            [id]
        );

        res.json({ message: 'Cuộc trò chuyện đã được đánh dấu là đã giải quyết' });
    } catch (error) {
        next(error);
    }
});

// PUT /api/support/conversations/:id/close - Đóng cuộc trò chuyện (khách hàng)
router.put('/conversations/:id/close', auth, async (req, res, next) => {
    try {
        const { id } = req.params;
        const { rating, feedback } = req.body;
        const userId = req.user.id;

        const [conv] = await pool.query(
            'SELECT * FROM consultant_conversations WHERE id = ?',
            [id]
        );

        if (conv.length === 0) {
            return res.status(404).json({ success: false, message: 'Không tìm thấy cuộc trò chuyện' });
        }

        if (conv[0].customer_id !== userId) {
            return res.status(403).json({ success: false, message: 'Bạn không có quyền đóng cuộc trò chuyện này' });
        }

        await pool.query(
            `UPDATE consultant_conversations
             SET status = 'closed', closed_at = NOW(),
                 customer_rating = ?, customer_feedback = ?
             WHERE id = ?`,
            [rating || null, feedback || null, id]
        );

        res.json({ message: 'Cảm ơn bạn đã đánh giá! Cuộc trò chuyện đã đóng.' });
    } catch (error) {
        next(error);
    }
});

// GET /api/support/unread - Số tin nhắn chưa đọc (cho notification badge)
router.get('/unread', auth, async (req, res, next) => {
    try {
        const userId = req.user.id;
        const userType = req.user.user_type;

        let query;
        let params;

        if (userType === 'passenger') {
            query = 'SELECT COUNT(*) as count FROM consultant_messages cm ' +
                'JOIN consultant_conversations cc ON cm.conversation_id = cc.id ' +
                'WHERE cc.customer_id = ? AND cm.sender_id != ? AND cm.is_read = FALSE';
            params = [userId, userId];
        } else if (['consultant', 'owner', 'admin'].includes(userType)) {
            query = 'SELECT COUNT(*) as count FROM consultant_messages cm ' +
                'JOIN consultant_conversations cc ON cm.conversation_id = cc.id ' +
                'WHERE (cc.consultant_id = ? OR (? IN ("owner","admin"))) ' +
                'AND cm.sender_id != ? AND cm.is_read = FALSE';
            params = [userId, userType, userId];
        } else {
            return res.json({ count: 0 });
        }

        const [result] = await pool.query(query, params);
        res.json({ count: result[0].count });
    } catch (error) {
        next(error);
    }
});

module.exports = router;
