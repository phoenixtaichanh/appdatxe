let initialized = false;
let admin = null;

async function initFirebase() {
    if (initialized) return;
    try {
        admin = require('firebase-admin');
        if (!admin.apps.length) {
            const serviceAccount = {
                projectId: process.env.FIREBASE_PROJECT_ID,
                clientEmail: process.env.FIREBASE_CLIENT_EMAIL,
                privateKey: process.env.FIREBASE_PRIVATE_KEY?.replace(/\\n/g, '\n')
            };
            if (serviceAccount.projectId && serviceAccount.clientEmail && serviceAccount.privateKey) {
                admin.initializeApp({
                    credential: admin.credential.cert(serviceAccount)
                });
                console.log('[FCM] Firebase Admin initialized');
            } else {
                console.warn('[FCM] Firebase credentials not configured in .env - notifications disabled');
                return;
            }
        }
        initialized = true;
    } catch (e) {
        console.error('[FCM] Init failed:', e.message);
    }
}

async function sendToUser(userId, { title, body, data = {} }) {
    if (!initialized || !admin) {
        console.log(`[FCM] Would send to user ${userId}: ${title} - ${body}`);
        return;
    }
    try {
        const { pool } = require('../database/db');
        const [tokens] = await pool.query(
            'SELECT fcm_token FROM user_fcm_tokens WHERE user_id = ?',
            [userId]
        );
        if (tokens.length === 0) {
            console.log(`[FCM] No tokens for user ${userId}`);
            return;
        }

        const message = {
            notification: { title, body },
            data: Object.fromEntries(
                Object.entries(data).map(([k, v]) => [k, String(v)])
            )
        };

        const response = await admin.messaging().sendEachForMulticast({
            ...message,
            tokens: tokens.map(t => t.fcm_token)
        });

        console.log(`[FCM] Sent: ${response.successCount}, Failed: ${response.failureCount}`);
    } catch (e) {
        console.error('[FCM] Send error:', e.message);
    }
}

async function sendToDrivers({ title, body, data = {} }) {
    if (!initialized || !admin) {
        console.log(`[FCM] Would broadcast to drivers: ${title} - ${body}`);
        return;
    }
    try {
        const { pool } = require('../database/db');
        const [tokens] = await pool.query(
            'SELECT fcm_token FROM user_fcm_tokens uft JOIN drivers d ON uft.user_id = d.user_id WHERE d.is_available = true'
        );
        if (tokens.length === 0) return;

        const message = {
            notification: { title, body },
            data: Object.fromEntries(
                Object.entries(data).map(([k, v]) => [k, String(v)])
            )
        };

        await admin.messaging().sendEachForMulticast({
            ...message,
            tokens: tokens.map(t => t.fcm_token)
        });
    } catch (e) {
        console.error('[FCM] Broadcast error:', e.message);
    }
}

module.exports = { initFirebase, sendToUser, sendToDrivers };
