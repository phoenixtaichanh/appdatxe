const express = require('express');
const cors = require('cors');
const http = require('http');
const path = require('path');
require('dotenv').config();

const { testConnection } = require('./database/db');
const authRoutes = require('./routes/auth');
const userRoutes = require('./routes/users');
const rideRoutes = require('./routes/rides');
const driverRoutes = require('./routes/drivers');
const locationRoutes = require('./routes/locations');
const aiRoutes = require('./routes/ai');
const chatRoutes = require('./routes/chat');
const paymentRoutes = require('./routes/payments');
const adminRoutes = require('./routes/admin');
const supportRoutes = require('./routes/support');
const faqRoutes = require('./routes/faq');
const adminAuthRoutes = require('./routes/adminAuth');
const adminSupportRoutes = require('./routes/adminSupport');

const app = express();
const PORT = process.env.PORT || 3000;

// Middleware
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Health check
app.get('/api/health', (req, res) => {
    res.json({ status: 'ok', message: 'DoAn3 Backend is running!' });
});

// Root route
app.get('/', (req, res) => {
    res.json({
        name: 'DoAn3 Backend API',
        version: '1.1.0',
        status: 'running',
        message: 'Server is running!',
        endpoints: {
            health: '/api/health',
            auth: '/api/auth',
            users: '/api/users',
            rides: '/api/rides',
            driver: '/api/driver',
            location: '/api/location',
            ai: '/api/ai',
            chat: '/api/chat',
            payments: '/api/payments',
            admin: '/api/admin',
            adminAuth: '/api/admin-auth',
            adminSupport: '/api/admin-support'
        },
        websocket: 'Socket.IO connected on same port'
    });
});

// Serve admin panel static files
const adminPanelPath = path.join(__dirname, '../../admin-panel/dist');
app.use(express.static(adminPanelPath));
app.get('/admin', (req, res) => {
    res.sendFile(path.join(adminPanelPath, 'index.html'));
});

// Routes
app.use('/api/auth', authRoutes);
app.use('/api/users', userRoutes);
app.use('/api/rides', rideRoutes);
app.use('/api/driver', driverRoutes);
app.use('/api/location', locationRoutes);
app.use('/api/ai', aiRoutes);
app.use('/api/chat', chatRoutes.router);
app.use('/api/payments', paymentRoutes);
app.use('/api/admin', adminRoutes);
app.use('/api/support', supportRoutes);
app.use('/api/faq', faqRoutes);
app.use('/api/admin-auth', adminAuthRoutes);
app.use('/api/admin-support', adminSupportRoutes);

// Error handling middleware
app.use((err, req, res, next) => {
    console.error('Error:', err.message);
    res.status(err.status || 500).json({
        success: false,
        message: err.message || 'Internal server error'
    });
});

// 404 handler
app.use((req, res) => {
    res.status(404).json({ success: false, message: 'Route not found' });
});

// Create HTTP server and initialize Socket.IO
async function startServer() {
    await testConnection();

    const httpServer = http.createServer(app);

    // Initialize Socket.IO
    try {
        const { initSocket } = require('./socket');
        const io = initSocket(httpServer);
        console.log('WebSocket server initialized');
    } catch (err) {
        console.error('Socket.IO init error:', err.message);
    }

    // Initialize FCM (Firebase Cloud Messaging)
    try {
        const { initFirebase } = require('./services/notification');
        initFirebase();
    } catch (err) {
        console.error('FCM init error:', err.message);
    }

    httpServer.listen(PORT, '0.0.0.0', () => {
        console.log(`🚀 Server running on http://localhost:${PORT}`);
        console.log(`📡 API Base URL: http://localhost:${PORT}/api`);
        console.log(`🔌 WebSocket: ws://localhost:${PORT}`);
    });
}

startServer();
