const { pool } = require('../database/db');
const { haversineDistance, getTrafficCondition } = require('../utils/geo');
const { calculatePrice, optimizeNearestNeighbor } = require('../utils/price');

// ========== SCHEDULE ==========

async function createSchedule(userId, { scheduleName, scheduledDate, optimizationType, waypoints }) {
    let totalDistance = 0;
    let totalDuration = 0;
    let totalPrice = 0;

    for (let i = 1; i < waypoints.length; i++) {
        const dist = haversineDistance(
            waypoints[i - 1].lat, waypoints[i - 1].lng,
            waypoints[i].lat, waypoints[i].lng
        );
        totalDistance += dist;
        totalDuration += Math.round((dist / 30) * 60);
    }
    totalPrice = calculatePrice(totalDistance, totalDuration);

    const [result] = await pool.query(
        `INSERT INTO ai_trip_schedules
         (user_id, schedule_name, scheduled_date, total_estimated_time,
          total_estimated_price, total_distance, optimization_type, traffic_condition, ai_confidence_score)
         VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)`,
        [userId, scheduleName, scheduledDate, totalDuration, Math.round(totalPrice),
         Math.round(totalDistance * 100) / 100, optimizationType || 'balanced',
         getTrafficCondition(), 0.85 + Math.random() * 0.1]
    );

    const scheduleId = result.insertId;

    for (let i = 0; i < waypoints.length; i++) {
        const wp = waypoints[i];
        let segmentDistance = 0, segmentDuration = 0, segmentPrice = 0;
        if (i > 0) {
            segmentDistance = haversineDistance(
                waypoints[i - 1].lat, waypoints[i - 1].lng, wp.lat, wp.lng
            );
            segmentDuration = Math.round((segmentDistance / 30) * 60);
            segmentPrice = calculatePrice(segmentDistance, segmentDuration);
        }

        await pool.query(
            `INSERT INTO ai_waypoints
             (schedule_id, stop_order, stop_type, latitude, longitude, address,
              stop_name, distance_from_prev, duration_min, estimated_price_segment, is_optional, priority)
             VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`,
            [scheduleId, i + 1, wp.stop_type || 'stopover', wp.lat, wp.lng,
             wp.address || '', wp.stop_name || null,
             Math.round(segmentDistance * 100) / 100, segmentDuration,
             Math.round(segmentPrice), wp.is_optional || false, wp.priority || 0]
        );
    }

    await generateRouteAlternatives(scheduleId, optimizationType || 'balanced');
    return getScheduleById(scheduleId);
}

async function getScheduleById(id, userId = null) {
    const condition = userId ? 'AND user_id = ?' : '';
    const params = userId ? [id, userId] : [id];

    const [schedule] = await pool.query(
        `SELECT * FROM ai_trip_schedules WHERE id = ? ${condition}`, params
    );
    if (!schedule[0]) return null;

    const [waypoints] = await pool.query(
        'SELECT * FROM ai_waypoints WHERE schedule_id = ? ORDER BY stop_order', [id]
    );
    const [alternatives] = await pool.query(
        'SELECT * FROM ai_route_alternatives WHERE schedule_id = ? ORDER BY is_recommended DESC, total_price ASC', [id]
    );

    return { ...schedule[0], waypoints, alternatives };
}

async function updateSchedule(id, { scheduleName, status }) {
    await pool.query(
        'UPDATE ai_trip_schedules SET schedule_name = COALESCE(?, schedule_name), status = COALESCE(?, status) WHERE id = ?',
        [scheduleName, status, id]
    );
    return getScheduleById(id);
}

async function getAlternatives(scheduleId) {
    const [rows] = await pool.query(`
        SELECT ara.*,
               (SELECT COUNT(*) FROM ai_waypoints aw WHERE aw.schedule_id = ara.schedule_id) as waypoint_count
        FROM ai_route_alternatives ara
        WHERE ara.schedule_id = ?
        ORDER BY ara.is_recommended DESC, ara.total_price ASC
    `, [scheduleId]);
    return rows;
}

async function reoptimize(id, optimizationType) {
    await pool.query('DELETE FROM ai_route_alternatives WHERE schedule_id = ?', [id]);
    const schedule = await getScheduleById(id);
    if (schedule) {
        await generateRouteAlternatives(id, optimizationType || schedule.optimization_type);
        await pool.query('UPDATE ai_trip_schedules SET optimization_type = ? WHERE id = ?',
            [optimizationType || 'balanced', id]);
    }
    return getAlternatives(id);
}

async function getHistory(userId) {
    const [schedules] = await pool.query(`
        SELECT * FROM ai_trip_schedules WHERE user_id = ? ORDER BY created_at DESC LIMIT 20`,
        [userId]
    );
    for (const s of schedules) {
        const [waypoints] = await pool.query(
            'SELECT * FROM ai_waypoints WHERE schedule_id = ? ORDER BY stop_order', [s.id]
        );
        s.waypoints = waypoints;
    }
    return schedules;
}

async function generateRouteAlternatives(scheduleId, optimizationType) {
    const [waypoints] = await pool.query(
        'SELECT * FROM ai_waypoints WHERE schedule_id = ? ORDER BY stop_order', [scheduleId]
    );

    let totalDistance = 0, totalDuration = 0;
    for (let i = 1; i < waypoints.length; i++) {
        const dist = haversineDistance(
            waypoints[i - 1].latitude, waypoints[i - 1].longitude,
            waypoints[i].latitude, waypoints[i].longitude
        );
        totalDistance += dist;
        totalDuration += Math.round((dist / 30) * 60);
    }
    const totalPrice = calculatePrice(totalDistance, totalDuration);

    const alternatives = [
        {
            name: 'Nhanh nhat',
            duration: Math.round(totalDuration * 0.85),
            price: Math.round(totalPrice * 1.1),
            desc: 'Tuyen duong nhanh nhat, co the di qua duong cao toc hoac duong tat',
            recommended: optimizationType === 'time',
            traffic: 'morning_peak',
            impact: 0.1
        },
        {
            name: 'Re nhat',
            duration: Math.round(totalDuration * 1.15),
            price: Math.round(totalPrice * 0.95),
            desc: 'Tuyen duong tiet kiem chi phi nhat, chon duong tran cao toc',
            recommended: optimizationType === 'cost',
            traffic: 'typical',
            impact: 0.0
        },
        {
            name: 'Can bang',
            duration: totalDuration,
            price: Math.round(totalPrice),
            desc: 'Tuyen duong can bang giua thoi gian va chi phi',
            recommended: optimizationType === 'balanced',
            traffic: 'typical',
            impact: 0.0
        }
    ];

    for (const alt of alternatives) {
        await pool.query(
            `INSERT INTO ai_route_alternatives
             (schedule_id, route_name, total_distance, total_duration, total_price,
              route_description, is_recommended, traffic_scenario, weather_impact)
             VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)`,
            [scheduleId, alt.name, Math.round(totalDistance * 100) / 100,
             alt.duration, alt.price, alt.desc, alt.recommended, alt.traffic, alt.impact]
        );
    }
}

// ========== AI PROFILE ==========

async function getProfile(userId) {
    const [rows] = await pool.query(
        'SELECT * FROM ai_learning_profiles WHERE user_id = ?', [userId]
    );
    if (rows[0]) return rows[0];

    const [result] = await pool.query(
        'INSERT INTO ai_learning_profiles (user_id) VALUES (?)', [userId]
    );
    const [newProfile] = await pool.query(
        'SELECT * FROM ai_learning_profiles WHERE id = ?', [result.insertId]
    );
    return newProfile[0];
}

async function updateProfile(userId, fields) {
    const existing = await getProfile(userId);
    if (!existing) {
        await pool.query(
            `INSERT INTO ai_learning_profiles
             (user_id, preferred_time_start, preferred_time_end, preference_cost_vs_time,
              frequent_locations, avoid_locations)
             VALUES (?, ?, ?, ?, ?, ?)`,
            [userId, fields.preferredTimeStart, fields.preferredTimeEnd,
             fields.preferenceCostVsTime || 0.5,
             fields.frequentLocations ? JSON.stringify(fields.frequentLocations) : null,
             fields.avoidLocations ? JSON.stringify(fields.avoidLocations) : null]
        );
    } else {
        await pool.query(
            `UPDATE ai_learning_profiles SET
             preferred_time_start = COALESCE(?, preferred_time_start),
             preferred_time_end = COALESCE(?, preferred_time_end),
             preference_cost_vs_time = COALESCE(?, preference_cost_vs_time),
             frequent_locations = COALESCE(?, frequent_locations),
             avoid_locations = COALESCE(?, avoid_locations),
             model_version = 'v1.1'
             WHERE user_id = ?`,
            [fields.preferredTimeStart, fields.preferredTimeEnd,
             fields.preferenceCostVsTime,
             fields.frequentLocations ? JSON.stringify(fields.frequentLocations) : null,
             fields.avoidLocations ? JSON.stringify(fields.avoidLocations) : null,
             userId]
        );
    }
    return getProfile(userId);
}

// ========== RECOMMENDATIONS ==========

async function getRecommendations(userId) {
    const [profiles] = await pool.query(
        'SELECT * FROM ai_learning_profiles WHERE user_id = ?', [userId]
    );
    const profile = profiles[0] || null;

    const [recentRides] = await pool.query(`
        SELECT * FROM rides WHERE passenger_id = ? ORDER BY created_at DESC LIMIT 10`,
        [userId]
    );

    const recommendations = { frequent_routes: [], best_times: [], estimated_savings: 0 };

    const routeMap = {};
    for (const ride of recentRides) {
        const key = `${ride.pickup_lat.toFixed(4)}_${ride.pickup_lng.toFixed(4)}-${ride.dest_lat.toFixed(4)}_${ride.dest_lng.toFixed(4)}`;
        if (!routeMap[key]) {
            routeMap[key] = { count: 0, pickup: ride.pickup_address, dest: ride.dest_address, price: ride.price };
        }
        routeMap[key].count++;
    }

    recommendations.frequent_routes = Object.values(routeMap)
        .filter(r => r.count >= 2)
        .sort((a, b) => b.count - a.count)
        .slice(0, 3);

    if (recentRides.length > 0) {
        const avgPrice = recentRides.reduce((sum, r) => sum + r.price, 0) / recentRides.length;
        recommendations.estimated_savings = Math.round(avgPrice * 0.15);
    }

    if (profile?.preferred_time_start) {
        recommendations.preferred_time = profile.preferred_time_start;
    }

    recommendations.ai_confidence = profile ? 0.85 : 0.6;
    return recommendations;
}

// ========== ROUTE PREVIEW ==========

async function previewRoute(waypoints) {
    let totalDistance = 0, totalDuration = 0;
    const segments = [];

    for (let i = 1; i < waypoints.length; i++) {
        const dist = haversineDistance(
            waypoints[i - 1].lat, waypoints[i - 1].lng,
            waypoints[i].lat, waypoints[i].lng
        );
        const dur = Math.round((dist / 30) * 60);
        const price = calculatePrice(dist, dur);

        totalDistance += dist;
        totalDuration += dur;

        segments.push({
            from: waypoints[i - 1].address || `Point ${i}`,
            to: waypoints[i].address || `Point ${i + 1}`,
            distance: Math.round(dist * 100) / 100,
            duration: dur,
            price: Math.round(price)
        });
    }

    const totalPrice = calculatePrice(totalDistance, totalDuration);

    return {
        total_distance: Math.round(totalDistance * 100) / 100,
        total_duration: totalDuration,
        total_price: Math.round(totalPrice),
        segments,
        recommendations: [
            { type: 'time', value: totalDuration, label: 'phut' },
            { type: 'cost', value: Math.round(totalPrice), label: 'VND' }
        ]
    };
}

// ========== RIDE OPTIMIZATION ==========

async function optimizeRides(passengerCount, rides) {
    if (!rides || rides.length < 2) return null;

    const optimized = optimizeNearestNeighbor(rides);

    let totalOriginalPrice = rides.reduce((sum, r) => sum + (r.price || 0), 0);
    let totalOptimizedPrice = optimized.reduce((sum, r) => sum + (r.price || 0), 0);

    const discount = passengerCount >= 4 ? 0.20 : passengerCount >= 3 ? 0.15 : 0.10;

    return {
        optimized_order: optimized,
        total_original_price: Math.round(totalOriginalPrice),
        total_optimized_price: Math.round(totalOptimizedPrice * (1 - discount)),
        discount_percent: discount * 100,
        estimated_savings: Math.round(totalOriginalPrice - totalOptimizedPrice * (1 - discount)),
        efficiency_score: Math.round((1 - optimized.length / rides.length) * 100) / 100
    };
}

// ========== BATCH ==========

async function getAvailableBatches(driverId) {
    const [batches] = await pool.query(`
        SELECT drb.*,
               (SELECT COUNT(*) FROM batch_passengers bp WHERE bp.batch_id = drb.id) as passenger_count
        FROM driver_route_batches drb
        WHERE drb.driver_id = ? AND drb.status = 'proposed'
        ORDER BY drb.efficiency_score DESC
    `, [driverId]);

    for (const batch of batches) {
        const [passengers] = await pool.query(`
            SELECT bp.*, u.name as passenger_name
            FROM batch_passengers bp
            JOIN users u ON bp.passenger_id = u.id
            WHERE bp.batch_id = ?
        `, [batch.id]);
        batch.passengers = passengers;
    }

    return batches;
}

async function acceptBatch(id, driverId) {
    await pool.query(
        `UPDATE driver_route_batches SET status = 'accepted', accepted_at = NOW() WHERE id = ? AND driver_id = ?`,
        [id, driverId]
    );
    await pool.query(`UPDATE batch_passengers SET status = 'pending' WHERE batch_id = ?`, [id]);
    const [rows] = await pool.query('SELECT * FROM driver_route_batches WHERE id = ?', [id]);
    return rows[0];
}

async function rejectBatch(id, driverId) {
    await pool.query(
        `UPDATE driver_route_batches SET status = 'cancelled' WHERE id = ? AND driver_id = ?`,
        [id, driverId]
    );
}

module.exports = {
    createSchedule, getScheduleById, updateSchedule, getAlternatives,
    reoptimize, getHistory, getProfile, updateProfile,
    getRecommendations, previewRoute, optimizeRides,
    getAvailableBatches, acceptBatch, rejectBatch
};
