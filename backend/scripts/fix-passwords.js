const mysql = require('mysql2/promise');
require('dotenv').config();

async function fixPasswords() {
    const pool = mysql.createPool({
        host: process.env.DB_HOST || 'localhost',
        user: process.env.DB_USER || 'root',
        password: process.env.DB_PASSWORD || '',
        database: process.env.DB_NAME || 'doan3_db',
        port: process.env.DB_PORT || 3306,
    });

    const correctHash = '$2a$10$uev3Pu1uftt1hD5OeSi7auSwi2yupqUp6857/CQsAwZg9Ngi4kef6';

    const emails = [
        'admin@test.com',
        'manager@test.com',
        'passenger@test.com',
        'driver1@test.com',
        'driver2@test.com',
        'driver3@test.com'
    ];

    try {
        const connection = await pool.getConnection();
        console.log('✅ Database connected!');

        for (const email of emails) {
            const [result] = await connection.query(
                'UPDATE users SET password = ? WHERE email = ?',
                [correctHash, email]
            );
            if (result.affectedRows > 0) {
                console.log(`✅ Updated password for: ${email}`);
            } else {
                console.log(`⚠️  User not found: ${email}`);
            }
        }

        // Verify
        console.log('\n🔍 Verifying admin login...');
        const [rows] = await connection.query(
            'SELECT * FROM users WHERE email = ?',
            ['admin@test.com']
        );

        if (rows.length > 0) {
            console.log('Admin user found:', {
                email: rows[0].email,
                name: rows[0].name,
                user_type: rows[0].user_type,
                password_prefix: rows[0].password.substring(0, 30) + '...'
            });
        }

        connection.release();
        console.log('\n✅ All passwords fixed!');
        process.exit(0);
    } catch (error) {
        console.error('❌ Error:', error.message);
        process.exit(1);
    }
}

fixPasswords();
