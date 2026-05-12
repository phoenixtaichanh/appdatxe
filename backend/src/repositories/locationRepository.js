const { pool } = require('../database/db');

async function upsertLocation(driverId, latitude, longitude) {
    await pool.query(`
        INSERT INTO driver_locations (driver_id, latitude, longitude)
        VALUES (?, ?, ?)
        ON DUPLICATE KEY UPDATE latitude = VALUES(latitude), longitude = VALUES(longitude), updated_at = NOW()
    `, [driverId, latitude, longitude]);

    await pool.query(
        'UPDATE drivers SET latitude = ?, longitude = ? WHERE user_id = ?',
        [latitude, longitude, driverId]
    );
}

async function findLocationByDriver(driverId) {
    const [rows] = await pool.query(`
        SELECT driver_id, latitude, longitude, updated_at
        FROM driver_locations
        WHERE driver_id = ?
        ORDER BY updated_at DESC
        LIMIT 1
    `, [driverId]);
    return rows[0] || null;
}

module.exports = { upsertLocation, findLocationByDriver };
