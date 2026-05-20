const axios = require('axios');
require('dotenv').config();

async function testAdminAPI() {
    try {
        // Step 1: Login as admin
        console.log('1️⃣ Logging in as admin@test.com...');
        const loginRes = await axios.post('http://localhost:3000/api/auth/login', {
            email: 'admin@test.com',
            password: 'password123'
        });

        if (!loginRes.data.success) {
            console.log('❌ Login failed:', loginRes.data.message);
            return;
        }

        const token = loginRes.data.token;
        console.log('✅ Login successful!');
        console.log('   User:', loginRes.data.user.name, `(${loginRes.data.user.user_type})`);

        // Step 2: Test admin endpoints
        console.log('\n2️⃣ Testing admin endpoints...');
        const requests = [
            { name: 'Dashboard', url: 'http://localhost:3000/api/admin/dashboard' },
            { name: 'Users', url: 'http://localhost:3000/api/admin/users?page=1&limit=20' },
            { name: 'Drivers', url: 'http://localhost:3000/api/admin/drivers?page=1&limit=20' },
            { name: 'Rides', url: 'http://localhost:3000/api/admin/rides?page=1&limit=20' },
            { name: 'Stats Daily', url: 'http://localhost:3000/api/admin/stats/daily?days=30' },
            { name: 'Stats Revenue', url: 'http://localhost:3000/api/admin/stats/revenue' },
        ];

        for (const req of requests) {
            try {
                const res = await axios.get(req.url, {
                    headers: { Authorization: `Bearer ${token}` }
                });
                console.log(`✅ ${req.name}: ${res.status}`);
            } catch (error) {
                console.log(`❌ ${req.name}: ${error.response?.status || 'No Response'}`);
                if (error.response?.data) {
                    console.log(`   Error: ${error.response.data.message || JSON.stringify(error.response.data)}`);
                } else {
                    console.log(`   Error: ${error.message}`);
                }
            }
        }

    } catch (error) {
        console.log('❌ Error:', error.response?.data?.message || error.message);
        if (error.response?.data?.stack) {
            console.log('\n📋 Stack trace:');
            console.log(error.response.data.stack);
        }
    }
}

testAdminAPI();
