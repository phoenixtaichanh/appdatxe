const { pool } = require('../database/db');

async function findByEmail(email) {
    const [rows] = await pool.query('SELECT * FROM users WHERE email = ?', [email]);
    return rows[0] || null;
}

async function findById(id) {
    const [rows] = await pool.query(
        'SELECT id, email, name, phone, user_type, profile_image, rating, total_rides, created_at FROM users WHERE id = ?',
        [id]
    );
    return rows[0] || null;
}

async function create({ email, password, name, phone, userType }) {
    const [result] = await pool.query(
        'INSERT INTO users (email, password, name, phone, user_type) VALUES (?, ?, ?, ?, ?)',
        [email, password, name, phone || null, userType || 'passenger']
    );
    return result.insertId;
}

async function updateProfile(id, { name, phone, profileImage }) {
    await pool.query(
        'UPDATE users SET name = COALESCE(?, name), phone = COALESCE(?, phone), profile_image = COALESCE(?, profile_image) WHERE id = ?',
        [name, phone, profileImage, id]
    );
    return findById(id);
}

async function updateRating(driverId) {
    const [rows] = await pool.query(`
        UPDATE users u
        SET u.rating = (
            SELECT AVG(r2.driver_rating)
            FROM rides r2
            WHERE r2.driver_id = u.id AND r2.driver_rating IS NOT NULL
        )
        WHERE u.id = ?
    `, [driverId]);
    return rows;
}

module.exports = { findByEmail, findById, create, updateProfile, updateRating };
