const { pool } = require('../database/db');
const { haversineDistance, estimateRideDetails } = require('../utils/geo');
const { calculatePrice } = require('../utils/price');

const RIDE_SELECT = `
    SELECT r.*,
           p.name as passenger_name,
           d.name as driver_name
    FROM rides r
    LEFT JOIN users p ON r.passenger_id = p.id
    LEFT JOIN users d ON r.driver_id = d.id
`;

async function create({ passengerId, pickupLat, pickupLng, pickupAddress, destLat, destLng, destAddress, vehicleType }) {
    const { distanceKm, durationMin } = estimateRideDetails(pickupLat, pickupLng, destLat, destLng);
    const price = calculatePrice(distanceKm, durationMin, vehicleType);

    const [result] = await pool.query(
        `INSERT INTO rides (passenger_id, pickup_lat, pickup_lng, pickup_address,
         dest_lat, dest_lng, dest_address, distance_km, duration_min, price, vehicle_type, status)
         VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'pending')`,
        [passengerId, pickupLat, pickupLng, pickupAddress || '',
         destLat, destLng, destAddress || '', distanceKm, durationMin, price, vehicleType || 'motorbike']
    );

    return findById(result.insertId);
}

async function findById(id) {
    const [rows] = await pool.query(`${RIDE_SELECT} WHERE r.id = ?`, [id]);
    return rows[0] || null;
}

async function findHistoryByUser(userId, userType) {
    const field = userType === 'driver' ? 'r.driver_id' : 'r.passenger_id';
    const [rows] = await pool.query(
        `${RIDE_SELECT} WHERE ${field} = ? ORDER BY r.created_at DESC`,
        [userId]
    );
    return rows;
}

async function findActiveByUser(userId, userType) {
    const field = userType === 'driver' ? 'r.driver_id' : 'r.passenger_id';
    const [rows] = await pool.query(
        `${RIDE_SELECT}
         WHERE ${field} = ? AND r.status IN ('pending', 'accepted', 'arrived', 'in_progress')
         ORDER BY r.created_at DESC LIMIT 1`,
        [userId]
    );
    return rows[0] || null;
}

async function updateStatus(id, status) {
    let query = 'UPDATE rides SET status = ?';
    const params = [status];

    if (status === 'in_progress') {
        query += ', started_at = NOW()';
    } else if (status === 'completed') {
        query += ', completed_at = NOW()';
    }

    query += ' WHERE id = ?';
    params.push(id);

    await pool.query(query, params);
    return findById(id);
}

async function assignDriver(rideId, driverId) {
    await pool.query('UPDATE rides SET driver_id = ? WHERE id = ?', [driverId, rideId]);
}

async function rate(id, { rating, comment, raterType }) {
    const field = raterType === 'passenger' ? 'driver_rating' : 'passenger_rating';
    const commentField = raterType === 'passenger' ? 'rating_comment' : null;

    let query = `UPDATE rides SET ${field} = ?`;
    const params = [rating];

    if (commentField) {
        query += `, ${commentField} = ?`;
        params.push(comment || null);
    }

    query += ' WHERE id = ?';
    params.push(id);

    await pool.query(query, params);

    if (raterType === 'passenger') {
        const [ride] = await pool.query('SELECT driver_id FROM rides WHERE id = ?', [id]);
        if (ride.length > 0) {
            const [avgRows] = await pool.query(
                `SELECT AVG(driver_rating) as avg_rating FROM rides WHERE driver_id = ? AND driver_rating IS NOT NULL`,
                [ride[0].driver_id]
            );
            if (avgRows[0]?.avg_rating) {
                await pool.query('UPDATE users SET rating = ? WHERE id = ?', [avgRows[0].avg_rating, ride[0].driver_id]);
            }
        }
    }

    return findById(id);
}

module.exports = { create, findById, findHistoryByUser, findActiveByUser, updateStatus, assignDriver, rate };
