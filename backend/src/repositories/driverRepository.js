const { pool } = require('../database/db');
const { haversineDistance } = require('../utils/geo');

const DRIVER_SELECT = `
    SELECT u.id, u.name, u.phone, u.email, u.rating, u.total_rides,
           d.car_model, d.car_color, d.license_plate, d.is_available,
           d.latitude, d.longitude
    FROM drivers d
    JOIN users u ON d.user_id = u.id
`;

async function findByUserId(userId) {
    const [rows] = await pool.query(`${DRIVER_SELECT} WHERE d.user_id = ?`, [userId]);
    return rows[0] || null;
}

async function updateProfile(userId, { name, phone, carModel, carColor, licensePlate }) {
    if (name || phone) {
        await pool.query(
            'UPDATE users SET name = COALESCE(?, name), phone = COALESCE(?, phone) WHERE id = ?',
            [name, phone, userId]
        );
    }

    if (carModel || carColor || licensePlate) {
        await pool.query(
            'UPDATE drivers SET car_model = COALESCE(?, car_model), car_color = COALESCE(?, car_color), license_plate = COALESCE(?, license_plate) WHERE user_id = ?',
            [carModel, carColor, licensePlate, userId]
        );
    }

    return findByUserId(userId);
}

async function updateStatus(userId, { isAvailable, latitude, longitude }) {
    await pool.query(
        'UPDATE drivers SET is_available = ?, latitude = ?, longitude = ? WHERE user_id = ?',
        [isAvailable, latitude, longitude, userId]
    );

    await pool.query(`
        INSERT INTO driver_locations (driver_id, latitude, longitude)
        VALUES ((SELECT user_id FROM drivers WHERE user_id = ?), ?, ?)
        ON DUPLICATE KEY UPDATE latitude = VALUES(latitude), longitude = VALUES(longitude), updated_at = NOW()
    `, [userId, latitude, longitude]);

    return findByUserId(userId);
}

async function setUnavailable(userId, rideId) {
    await pool.query(
        'UPDATE drivers SET is_available = FALSE, current_ride_id = ? WHERE user_id = ?',
        [rideId, userId]
    );
}

async function setAvailable(userId) {
    await pool.query(
        'UPDATE drivers SET is_available = TRUE, current_ride_id = NULL WHERE user_id = ?',
        [userId]
    );
}

async function findNearbyDrivers(lat, lng, radiusKm = 5) {
    const [rows] = await pool.query(`
        SELECT
            u.id, u.name, u.phone, u.rating, u.total_rides,
            d.car_model, d.car_color, d.license_plate, d.is_available,
            d.latitude, d.longitude,
            (6371 * acos(cos(radians(?)) * cos(radians(d.latitude))
            * cos(radians(d.longitude) - radians(?))
            + sin(radians(?)) * sin(radians(d.latitude)))) AS distance_km
        FROM drivers d
        JOIN users u ON d.user_id = u.id
        WHERE d.is_available = TRUE
        HAVING distance_km <= ?
        ORDER BY distance_km ASC
        LIMIT 10
    `, [lat, lng, lat, parseFloat(radiusKm)]);
    return rows;
}

async function getEarnings(driverId, fromDate, toDate) {
    const [rows] = await pool.query(`
        SELECT
            COALESCE(SUM(CASE WHEN DATE(e.created_at) = CURDATE() THEN e.amount ELSE 0 END), 0) as today_earnings,
            COALESCE(SUM(CASE WHEN YEARWEEK(e.created_at) = YEARWEEK(CURDATE()) THEN e.amount ELSE 0 END), 0) as week_earnings,
            COALESCE(SUM(CASE WHEN MONTH(e.created_at) = MONTH(CURDATE()) AND YEAR(e.created_at) = YEAR(CURDATE()) THEN e.amount ELSE 0 END), 0) as month_earnings,
            COUNT(*) as total_rides,
            COALESCE(SUM(e.amount), 0) as total_earnings
        FROM earnings e
        WHERE e.driver_id = ? AND e.created_at BETWEEN ? AND ?
    `, [driverId, fromDate, toDate]);
    return rows[0];
}

async function recordEarning(driverId, rideId, amount) {
    await pool.query(
        'INSERT INTO earnings (driver_id, ride_id, amount) VALUES (?, ?, ?)',
        [driverId, rideId, amount]
    );
}

module.exports = {
    findByUserId, updateProfile, updateStatus,
    setUnavailable, setAvailable, findNearbyDrivers,
    getEarnings, recordEarning
};
