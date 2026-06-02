// Firebase Cloud Messaging (FCM) - Push Notifications
// Chi can cau hinh 3 bien FIREBASE_* trong .env la hoat dong

const { pool } = require('../database/db');

let initialized = false;
let admin = null;
let isAvailable = false;

async function initFirebase() {
    if (initialized) return;

    const projectId = process.env.FIREBASE_PROJECT_ID;
    const clientEmail = process.env.FIREBASE_CLIENT_EMAIL;
    const privateKey = process.env.FIREBASE_PRIVATE_KEY?.replace(/\\n/g, '\n');

    if (!projectId || !clientEmail || !privateKey ||
        projectId === 'your-firebase-project-id' ||
        privateKey.includes('your-firebase')) {
        console.log('[FCM] Credentials not configured - push notifications disabled (hoat dong sau khi cau hinh .env)');
        initialized = true;
        return;
    }

    try {
        admin = require('firebase-admin');
        if (!admin.apps.length) {
            admin.initializeApp({
                credential: admin.credential.cert({ projectId, clientEmail, privateKey })
            });
        }
        isAvailable = true;
        console.log('[FCM] Firebase Admin initialized - Push notifications ENABLED');
    } catch (e) {
        console.warn('[FCM] Init failed:', e.message);
    }

    initialized = true;
}

async function sendToUser(userId, { title, body, data = {} }) {
    if (!isAvailable || !admin) {
        console.log(`[FCM] To user ${userId}: ${title} - ${body}`);
        return;
    }
    try {
        const [tokens] = await pool.query(
            'SELECT fcm_token FROM user_fcm_tokens WHERE user_id = ? AND is_active = TRUE',
            [userId]
        );
        if (!tokens.length) {
            console.log(`[FCM] No active tokens for user ${userId}`);
            return;
        }

        const message = {
            notification: { title, body },
            data: Object.fromEntries(Object.entries(data).map(([k, v]) => [k, String(v)]))
        };

        const response = await admin.messaging().sendEachForMulticast({
            ...message,
            tokens: tokens.map(t => t.fcm_token)
        });

        console.log(`[FCM] Sent to user ${userId}: ${response.successCount} ok, ${response.failureCount} failed`);
    } catch (e) {
        console.error('[FCM] Send error:', e.message);
    }
}

async function sendToDrivers({ title, body, data = {} }) {
    if (!isAvailable || !admin) {
        console.log(`[FCM] Broadcast to drivers: ${title} - ${body}`);
        return;
    }
    try {
        const [tokens] = await pool.query(`
            SELECT uft.fcm_token FROM user_fcm_tokens uft
            JOIN drivers d ON uft.user_id = d.user_id
            WHERE d.is_available = TRUE AND uft.is_active = TRUE
        `);
        if (!tokens.length) return;

        const message = {
            notification: { title, body },
            data: Object.fromEntries(Object.entries(data).map(([k, v]) => [k, String(v)]))
        };

        const response = await admin.messaging().sendEachForMulticast({
            ...message,
            tokens: tokens.map(t => t.fcm_token)
        });

        console.log(`[FCM] Broadcast to drivers: ${response.successCount} ok, ${response.failureCount} failed`);
    } catch (e) {
        console.error('[FCM] Broadcast error:', e.message);
    }
}

module.exports = { initFirebase, sendToUser, sendToDrivers };
