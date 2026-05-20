const mysql = require('mysql2/promise');
require('dotenv').config();

async function checkAndFixEarnings() {
    const pool = mysql.createPool({
        host: process.env.DB_HOST || 'localhost',
        user: process.env.DB_USER || 'root',
        password: process.env.DB_PASSWORD || '',
        database: process.env.DB_NAME || 'doan3_db',
        port: process.env.DB_PORT || 3306,
    });

    try {
        const connection = await pool.getConnection();

        // Check earnings table structure
        const [cols] = await connection.query('DESCRIBE earnings');
        console.log('📋 Current earnings table columns:');
        cols.forEach(c => console.log(`  - ${c.Field}: ${c.Type}`));

        // Check if 'type' column exists
        const hasType = cols.find(c => c.Field === 'type');
        if (!hasType) {
            console.log('\n⚠️  Column "type" is missing! Adding...');
            await connection.query('ALTER TABLE earnings ADD COLUMN type ENUM("ride", "bonus", "penalty", "withdrawal") DEFAULT "ride" AFTER ride_id');
            console.log('✅ Added "type" column to earnings table');
        } else {
            console.log('\n✅ Column "type" exists');
        }

        // Also check for other missing columns
        const hasNote = cols.find(c => c.Field === 'note');
        if (!hasNote) {
            console.log('⚠️  Column "note" is missing! Adding...');
            await connection.query('ALTER TABLE earnings ADD COLUMN note VARCHAR(255) DEFAULT NULL AFTER type');
            console.log('✅ Added "note" column to earnings table');
        }

        connection.release();
        console.log('\n🎉 Earnings table fixed!');
    } catch (error) {
        console.error('❌ Error:', error.message);
        process.exit(1);
    }
}

checkAndFixEarnings();
