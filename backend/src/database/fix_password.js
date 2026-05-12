const mysql = require('mysql2/promise');
const bcrypt = require('bcryptjs');
require('dotenv').config({ path: __dirname + '/../../.env' });

async function updatePasswords() {
    const pool = mysql.createPool({
        host: process.env.DB_HOST || 'localhost',
        user: process.env.DB_USER || 'root',
        password: process.env.DB_PASSWORD || '',
        database: process.env.DB_NAME || 'doan3_db',
        port: process.env.DB_PORT || 3306
    });

    const correctHash = await bcrypt.hash('password123', 10);
    console.log('New hash:', correctHash);

    const [result] = await pool.query(
        `UPDATE users SET password = ? WHERE email IN ('passenger@test.com','driver1@test.com','driver2@test.com','driver3@test.com')`,
        [correctHash]
    );

    console.log('Updated rows:', result.affectedRows);

    // Verify
    const [rows] = await pool.query("SELECT email FROM users WHERE email LIKE '%@test.com'");
    for (const user of rows) {
        const [check] = await pool.query("SELECT password FROM users WHERE email = ?", [user.email]);
        const match = await bcrypt.compare('password123', check[0].password);
        console.log(`${user.email}: password123 = ${match ? 'OK' : 'FAIL'}`);
    }

    await pool.end();
    console.log('Done!');
}

updatePasswords().catch(console.error);
