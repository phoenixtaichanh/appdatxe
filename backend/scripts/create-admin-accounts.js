const mysql = require('mysql2/promise');
require('dotenv').config();

async function migrateAdminRoles() {
    const pool = mysql.createPool({
        host: process.env.DB_HOST || 'localhost',
        user: process.env.DB_USER || 'root',
        password: process.env.DB_PASSWORD || '',
        database: process.env.DB_NAME || 'doan3_db',
        port: process.env.DB_PORT || 3306,
    });

    const correctHash = '$2a$10$uev3Pu1uftt1hD5OeSi7auSwi2yupqUp6857/CQsAwZg9Ngi4kef6';

    try {
        const connection = await pool.getConnection();

        // Step 1: Check current ENUM definition
        const [cols] = await connection.query("SHOW COLUMNS FROM users LIKE 'user_type'");
        console.log('Current user_type:', cols[0]?.Type);

        // Step 2: Alter ENUM to add missing roles
        const alterSql = `
            ALTER TABLE users
            MODIFY COLUMN user_type
            ENUM('passenger', 'driver', 'owner', 'consultant', 'hr_manager', 'revenue_manager')
            DEFAULT 'passenger'
        `;
        await connection.query(alterSql);
        console.log('✅ user_type ENUM extended with admin roles');

        // Step 3: Create admin accounts
        const adminUsers = [
            { email: 'admin@test.com', name: 'Quan Tri Vien', phone: '0909000001', user_type: 'owner' },
            { email: 'manager@test.com', name: 'Nhan Vien Quan Ly', phone: '0909000002', user_type: 'revenue_manager' }
        ];

        for (const user of adminUsers) {
            const [existing] = await connection.query(
                'SELECT * FROM users WHERE email = ?',
                [user.email]
            );

            if (existing.length > 0) {
                await connection.query(
                    'UPDATE users SET password = ?, name = ?, phone = ?, user_type = ? WHERE email = ?',
                    [correctHash, user.name, user.phone, user.user_type, user.email]
                );
                console.log(`✅ Updated: ${user.email} -> ${user.user_type}`);
            } else {
                const sql = `INSERT INTO users (email, password, name, phone, user_type, rating, total_rides) VALUES ('${user.email}', '${correctHash}', '${user.name}', '${user.phone}', '${user.user_type}', 5.0, 0)`;
                await connection.query(sql);
                console.log(`✅ Created: ${user.email} -> ${user.user_type}`);
            }
        }

        // Step 4: Verify
        console.log('\n🔍 Verifying all admin accounts...');
        const [rows] = await connection.query(
            "SELECT id, email, name, user_type, rating FROM users WHERE email IN ('admin@test.com', 'manager@test.com', 'passenger@test.com', 'driver1@test.com')"
        );
        for (const row of rows) {
            console.log(`  ✅ ID:${row.id} | ${row.email} | ${row.name} | ${row.user_type}`);
        }

        connection.release();
        console.log('\n🎉 Migration complete! Admin login should work now.');
        process.exit(0);
    } catch (error) {
        console.error('❌ Error:', error.message);
        process.exit(1);
    }
}

migrateAdminRoles();
