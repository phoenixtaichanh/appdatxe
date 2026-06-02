const express = require('express');
const auth = require('../middleware/auth');
const { pool } = require('../database/db');

const router = express.Router();

let notificationService = null;
let socketIO = null;
try {
    notificationService = require('../services/notification');
    const socketModule = require('../socket');
    socketIO = socketModule.getIO();
} catch (e) { /* FCM or socket not configured */ }

// GET /api/chat/:rideId/messages
router.get('/:rideId/messages', auth, async (req, res, next) => {
    try {
        const { rideId } = req.params;

        const [messages] = await pool.query(`
            SELECT cm.*, u.name as sender_name
            FROM chat_messages cm
            JOIN users u ON cm.sender_id = u.id
            WHERE cm.ride_id = ?
            ORDER BY cm.created_at ASC
        `, [rideId]);

        await pool.query(`
            UPDATE chat_messages SET is_read = TRUE
            WHERE ride_id = ? AND sender_id != ?
        `, [rideId, req.user.id]);

        res.json(messages);
    } catch (error) {
        next(error);
    }
});

// POST /api/chat/:rideId/send
router.post('/:rideId/send', auth, async (req, res, next) => {
    try {
        const { rideId } = req.params;
        const { message, message_type = 'text' } = req.body;

        if (!message || message.trim().length === 0) {
            return res.status(400).json({ success: false, message: 'Message is required' });
        }

        const [ride] = await pool.query(
            'SELECT * FROM rides WHERE id = ? AND (passenger_id = ? OR driver_id = ?)',
            [rideId, req.user.id, req.user.id]
        );
        if (ride.length === 0) {
            return res.status(404).json({ success: false, message: 'Ride not found' });
        }

        const senderType = req.user.user_type;
        const [result] = await pool.query(
            'INSERT INTO chat_messages (ride_id, sender_id, sender_type, message, message_type) VALUES (?, ?, ?, ?, ?)',
            [rideId, req.user.id, senderType, message.trim(), message_type]
        );

        const [userInfo] = await pool.query('SELECT name FROM users WHERE id = ?', [req.user.id]);
        const senderName = userInfo.length > 0 ? userInfo[0].name : req.user.user_type;

        const receiverId = senderType === 'driver' ? ride[0].passenger_id : ride[0].driver_id;

        if (notificationService && receiverId) {
            const title = senderType === 'driver' ? 'Tai xe nhan tin' : 'Khach nhan tin';
            notificationService.sendToUser(receiverId, {
                title,
                body: message.substring(0, 80),
                data: { rideId: String(rideId), type: 'chat_message' }
            }).catch(e => console.error('[Chat] FCM error:', e.message));
        }

        if (socketIO) {
            socketIO.to(`user_${receiverId}`).emit('chat:message', {
                id: result.insertId,
                rideId: parseInt(rideId),
                message: message.trim(),
                senderId: req.user.id,
                senderType,
                senderName,
                messageType: message_type,
                createdAt: new Date().toISOString()
            });
        }

        res.status(201).json({ id: result.insertId });
    } catch (error) {
        next(error);
    }
});

module.exports = { router };
