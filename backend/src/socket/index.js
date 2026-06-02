const { Server } = require('socket.io');
const jwt = require('jsonwebtoken');
const { pool } = require('../database/db');

let ioInstance = null;

function initSocket(httpServer) {
    ioInstance = new Server(httpServer, {
        cors: {
            origin: '*',
            methods: ['GET', 'POST']
        }
    });

    ioInstance.use((socket, next) => {
        const token = socket.handshake.auth?.token;
        if (!token) {
            return next(new Error('Authentication required'));
        }
        try {
            socket.user = jwt.verify(token, process.env.JWT_SECRET);
            next();
        } catch (e) {
            next(new Error('Invalid token'));
        }
    });

    ioInstance.on('connection', (socket) => {
        const { id, user_type } = socket.user;
        console.log(`[Socket] User connected: ${id} (${user_type})`);

        socket.join(`user_${id}`);

        // Consultants and admins join the admins room for receiving messages
        if (['consultant', 'owner', 'admin'].includes(user_type)) {
            socket.join('admins');
        }

        if (user_type === 'driver') {
            socket.join('drivers');

            socket.on('location:update', async ({ lat, lng, rideId }) => {
                try {
                    await pool.query(
                        `INSERT INTO driver_locations (driver_id, latitude, longitude)
                         VALUES (?, ?, ?)
                         ON DUPLICATE KEY UPDATE latitude = VALUES(latitude), longitude = VALUES(longitude), updated_at = NOW()`,
                        [id, lat, lng]
                    );

                    await pool.query(
                        'UPDATE drivers SET latitude = ?, longitude = ? WHERE user_id = ?',
                        [lat, lng, id]
                    );

                    if (rideId) {
                        const [rides] = await pool.query(
                            'SELECT passenger_id FROM rides WHERE id = ? AND driver_id = ?',
                            [rideId, id]
                        );
                        if (rides.length > 0) {
                            const passengerId = rides[0].passenger_id;
                            ioInstance.to(`user_${passengerId}`).emit('driver:location', {
                                lat,
                                lng,
                                rideId,
                                timestamp: Date.now()
                            });
                        }
                    }
                } catch (err) {
                    console.error('[Socket] location:update error:', err.message);
                }
            });

            socket.on('ride:status', async ({ rideId, status }) => {
                try {
                    let query = 'UPDATE rides SET status = ?';
                    const params = [status];

                    if (status === 'in_progress') {
                        query += ', started_at = NOW()';
                    } else if (status === 'completed') {
                        query += ', completed_at = NOW()';
                    }

                    query += ' WHERE id = ? AND driver_id = ?';
                    params.push(rideId, id);

                    await pool.query(query, params);

                    const [rides] = await pool.query(
                        'SELECT passenger_id FROM rides WHERE id = ?',
                        [rideId]
                    );

                    if (rides.length > 0) {
                        ioInstance.to(`user_${rides[0].passenger_id}`).emit('ride:status:changed', {
                            rideId,
                            status,
                            timestamp: Date.now()
                        });
                    }

                    ioInstance.to(`user_${id}`).emit('ride:status:changed', {
                        rideId,
                        status,
                        timestamp: Date.now()
                    });

                    console.log(`[Socket] Ride ${rideId} status -> ${status}`);
                } catch (err) {
                    console.error('[Socket] ride:status error:', err.message);
                }
            });
        }

        if (user_type === 'passenger') {
            socket.on('join:ride', async (rideId) => {
                socket.join(`ride_${rideId}`);
                console.log(`[Socket] Passenger ${id} joined ride ${rideId}`);
            });

            socket.on('leave:ride', (rideId) => {
                socket.leave(`ride_${rideId}`);
                console.log(`[Socket] Passenger ${id} left ride ${rideId}`);
            });

            socket.on('request:driver:location', async ({ rideId }) => {
                try {
                    const [rides] = await pool.query(
                        `SELECT r.driver_id, d.latitude, d.longitude
                         FROM rides r
                         JOIN drivers d ON r.driver_id = d.user_id
                         WHERE r.id = ? AND r.passenger_id = ?`,
                        [rideId, id]
                    );
                    if (rides.length > 0) {
                        const { driver_id, latitude, longitude } = rides[0];
                        ioInstance.to(`user_${id}`).emit('driver:location', {
                            lat: latitude,
                            lng: longitude,
                            rideId,
                            timestamp: Date.now()
                        });
                    }
                } catch (err) {
                    console.error('[Socket] request:driver:location error:', err.message);
                }
            });
        }

        socket.on('disconnect', () => {
            console.log(`[Socket] User disconnected: ${id}`);
        });

        // ====== CONSULTANT CHAT REAL-TIME ======
        // Join conversation room
        socket.on('consultant:join', (conversationId) => {
            socket.join(`consultant_convo_${conversationId}`);
            console.log(`[Socket] User ${id} joined consultant conversation ${conversationId}`);
        });

        // Leave conversation room
        socket.on('consultant:leave', (conversationId) => {
            socket.leave(`consultant_convo_${conversationId}`);
            console.log(`[Socket] User ${id} left consultant conversation ${conversationId}`);
        });

        // Real-time typing indicator
        socket.on('consultant:typing', ({ conversationId, isTyping }) => {
            socket.to(`consultant_convo_${conversationId}`).emit('consultant:typing', {
                conversationId,
                userId: id,
                userType: user_type,
                isTyping
            });
        });
    });

    return ioInstance;
}

function getIO() {
    return ioInstance;
}

module.exports = { initSocket, getIO };
