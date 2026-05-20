const mysql = require('mysql2/promise');
require('dotenv').config();

async function fixDatabase() {
    const pool = mysql.createPool({
        host: process.env.DB_HOST || 'localhost',
        user: process.env.DB_USER || 'root',
        password: process.env.DB_PASSWORD || '',
        database: process.env.DB_NAME || 'doan3_db',
        port: process.env.DB_PORT || 3306,
    });

    try {
        const connection = await pool.getConnection();
        console.log('✅ Database connected!');

        // Check current columns
        const [cols] = await connection.query('DESCRIBE users');
        console.log('\n📋 Current users table columns:');
        cols.forEach(c => console.log(`  - ${c.Field}: ${c.Type}`));

        // Add missing columns
        const missingColumns = [
            { name: 'is_active', sql: 'ALTER TABLE users ADD COLUMN is_active BOOLEAN DEFAULT TRUE AFTER total_rides' }
        ];

        for (const col of missingColumns) {
            const exists = cols.find(c => c.Field === col.name);
            if (!exists) {
                try {
                    await connection.query(col.sql);
                    console.log(`\n✅ Added column: ${col.name}`);
                } catch (e) {
                    if (e.code === 'ER_DUP_FIELDNAME') {
                        console.log(`\n⚠️  Column already exists: ${col.name}`);
                    } else {
                        console.log(`\n❌ Error adding ${col.name}: ${e.message}`);
                    }
                }
            } else {
                console.log(`\n✅ Column exists: ${col.name}`);
            }
        }

        // Also check drivers table
        const [driverCols] = await connection.query('DESCRIBE drivers');
        console.log('\n📋 Current drivers table columns:');
        driverCols.forEach(c => console.log(`  - ${c.Field}: ${c.Type}`));

        connection.release();
        console.log('\n🎉 Database schema fixed!');
    } catch (error) {
        console.error('❌ Error:', error.message);
        process.exit(1);
    }
}

fixDatabase();
