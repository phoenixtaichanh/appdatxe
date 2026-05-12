const BASE_FARE_MOTORBIKE = 10000;
const BASE_FARE_4SEATS = 12000;
const BASE_FARE_7SEATS = 15000;

const PRICE_PER_KM_MOTORBIKE = 3000;
const PRICE_PER_KM_4SEATS = 5000;
const PRICE_PER_KM_7SEATS = 7000;

const PRICE_PER_MIN_MOTORBIKE = 100;
const PRICE_PER_MIN_4SEATS = 200;
const PRICE_PER_MIN_7SEATS = 300;

const VEHICLE_PRICING = {
    motorbike: {
        base: BASE_FARE_MOTORBIKE,
        perKm: PRICE_PER_KM_MOTORBIKE,
        perMin: PRICE_PER_MIN_MOTORBIKE,
        label: 'Xe may',
        icon: 'motorbike'
    },
    car_4_seats: {
        base: BASE_FARE_4SEATS,
        perKm: PRICE_PER_KM_4SEATS,
        perMin: PRICE_PER_MIN_4SEATS,
        label: 'O to 4 cho',
        icon: 'car_4'
    },
    car_7_seats: {
        base: BASE_FARE_7SEATS,
        perKm: PRICE_PER_KM_7SEATS,
        perMin: PRICE_PER_MIN_7SEATS,
        label: 'O to 7 cho',
        icon: 'car_7'
    }
};

function calculatePrice(distanceKm, durationMin, vehicleType) {
    vehicleType = vehicleType || 'motorbike';
    const pricing = VEHICLE_PRICING[vehicleType] || VEHICLE_PRICING.motorbike;
    return Math.round(pricing.base + (distanceKm * pricing.perKm) + (durationMin * pricing.perMin));
}

function getPricingBreakdown(distanceKm, durationMin, vehicleType) {
    vehicleType = vehicleType || 'motorbike';
    const pricing = VEHICLE_PRICING[vehicleType] || VEHICLE_PRICING.motorbike;
    const base = pricing.base;
    const distanceFare = Math.round(distanceKm * pricing.perKm);
    const timeFare = Math.round(durationMin * pricing.perMin);
    const total = base + distanceFare + timeFare;
    return {
        base: base,
        distanceFare: distanceFare,
        timeFare: timeFare,
        total: total,
        vehicleType: vehicleType,
        label: pricing.label,
        icon: pricing.icon
    };
}

function optimizeNearestNeighbor(rides) {
    if (rides.length <= 2) return rides;
    const { haversineDistance } = require('./geo');
    const optimized = [rides[0]];
    const remaining = rides.slice(1);

    while (remaining.length > 0) {
        const last = optimized[optimized.length - 1];
        let nearestIndex = 0;
        let nearestDist = Infinity;

        for (let i = 0; i < remaining.length; i++) {
            const dist = haversineDistance(
                last.destLat || last.pickup_lat,
                last.destLng || last.pickup_lng,
                remaining[i].pickupLat || remaining[i].pickup_lat,
                remaining[i].pickupLng || remaining[i].pickup_lng
            );
            if (dist < nearestDist) {
                nearestDist = dist;
                nearestIndex = i;
            }
        }

        optimized.push(remaining[nearestIndex]);
        remaining.splice(nearestIndex, 1);
    }

    return optimized;
}

module.exports = {
    VEHICLE_PRICING,
    BASE_FARE_MOTORBIKE, BASE_FARE_4SEATS, BASE_FARE_7SEATS,
    PRICE_PER_KM_MOTORBIKE, PRICE_PER_KM_4SEATS, PRICE_PER_KM_7SEATS,
    PRICE_PER_MIN_MOTORBIKE, PRICE_PER_MIN_4SEATS, PRICE_PER_MIN_7SEATS,
    calculatePrice,
    getPricingBreakdown,
    optimizeNearestNeighbor
};
