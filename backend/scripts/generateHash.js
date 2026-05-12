// Script để tạo hash bcrypt cho seed data
// Chạy: node scripts/generateHash.js

const bcrypt = require('bcryptjs');

async function generateHash() {
    const password = 'password123';
    const hash = await bcrypt.hash(password, 10);
    console.log('Password:', password);
    console.log('Hash:', hash);
    console.log('\nDùng hash này trong seed.sql: UPDATE users SET password = "' + hash + '";');
}

generateHash();
