const R = 6371; // Earth's radius in km

function haversineDistance(lat1, lng1, lat2, lng2) {
    const dLat = (lat2 - lat1) * Math.PI / 180;
    const dLng = (lng2 - lng1) * Math.PI / 180;
    const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
        Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
        Math.sin(dLng / 2) * Math.sin(dLng / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c;
}

function estimateRideDetails(pickupLat, pickupLng, destLat, destLng) {
    const distanceKm = haversineDistance(pickupLat, pickupLng, destLat, destLng);
    const durationMin = Math.round((distanceKm / 30) * 60);
    return { distanceKm, durationMin };
}

function getTrafficCondition(hour = new Date().getHours()) {
    if (hour >= 7 && hour <= 9) return 'heavy';
    if (hour >= 17 && hour <= 19) return 'heavy';
    if (hour >= 22 || hour <= 5) return 'light';
    return 'normal';
}

module.exports = { haversineDistance, estimateRideDetails, getTrafficCondition };
