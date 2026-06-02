package com.laptrinhdidong.DoAn3.data.remote.dto

import com.google.gson.annotations.SerializedName

// ========== AI SCHEDULE ==========

data class CreateScheduleRequest(
    val schedule_name: String,
    val scheduled_date: String,
    val optimization_type: String = "balanced",
    val waypoints: List<WaypointDto>
)

data class WaypointDto(
    val lat: Double,
    val lng: Double,
    val address: String,
    @SerializedName("stop_name") val stopName: String? = null,
    @SerializedName("stop_type") val stopType: String = "stopover",
    val priority: Int = 0,
    @SerializedName("is_optional") val isOptional: Boolean = false
)

data class AIScheduleDto(
    val id: Int,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("schedule_name")
    val scheduleName: String,
    @SerializedName("scheduled_date")
    val scheduledDate: String,
    @SerializedName("total_estimated_time")
    val totalEstimatedTime: Int?,
    @SerializedName("total_estimated_price")
    val totalEstimatedPrice: Double?,
    @SerializedName("total_distance")
    val totalDistance: Double?,
    @SerializedName("optimization_type")
    val optimizationType: String,
    val status: String,
    @SerializedName("ai_confidence_score")
    val aiConfidenceScore: Double?,
    @SerializedName("traffic_condition")
    val trafficCondition: String?,
    @SerializedName("created_at")
    val createdAt: String?,
    val waypoints: List<AIWaypointDto>? = null,
    val alternatives: List<RouteAlternativeDto>? = null
)

data class AIWaypointDto(
    val id: Int,
    @SerializedName("schedule_id")
    val scheduleId: Int,
    @SerializedName("stop_order")
    val stopOrder: Int,
    @SerializedName("stop_type")
    val stopType: String,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    @SerializedName("stop_name")
    val stopName: String?,
    @SerializedName("estimated_arrival")
    val estimatedArrival: String?,
    @SerializedName("estimated_departure")
    val estimatedDeparture: String?,
    @SerializedName("duration_min")
    val durationMin: Int?,
    @SerializedName("distance_from_prev")
    val distanceFromPrev: Double?,
    @SerializedName("estimated_price_segment")
    val estimatedPriceSegment: Double?,
    @SerializedName("is_optional")
    val isOptional: Boolean?,
    val priority: Int?
)

data class RouteAlternativeDto(
    val id: Int,
    @SerializedName("schedule_id")
    val scheduleId: Int,
    @SerializedName("route_name")
    val routeName: String,
    @SerializedName("total_distance")
    val totalDistance: Double,
    @SerializedName("total_duration")
    val totalDuration: Int,
    @SerializedName("total_price")
    val totalPrice: Double,
    @SerializedName("route_description")
    val routeDescription: String?,
    @SerializedName("is_recommended")
    val isRecommended: Boolean?,
    @SerializedName("traffic_scenario")
    val trafficScenario: String?,
    @SerializedName("weather_impact")
    val weatherImpact: Double?,
    @SerializedName("created_at")
    val createdAt: String?
)

// ========== AI PROFILE ==========

data class AIProfileDto(
    val id: Int,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("preferred_time_start")
    val preferredTimeStart: String?,
    @SerializedName("preferred_time_end")
    val preferredTimeEnd: String?,
    @SerializedName("average_trip_duration")
    val averageTripDuration: Double?,
    @SerializedName("average_trip_cost")
    val averageTripCost: Double?,
    @SerializedName("total_distance_travelled")
    val totalDistanceTravelled: Double?,
    @SerializedName("peak_hours_pattern")
    val peakHoursPattern: String?,
    @SerializedName("frequent_locations")
    val frequentLocations: String?,
    @SerializedName("avoid_locations")
    val avoidLocations: String?,
    @SerializedName("preference_cost_vs_time")
    val preferenceCostVsTime: Double?,
    @SerializedName("model_version")
    val modelVersion: String?,
    @SerializedName("updated_at")
    val updatedAt: String?
)

data class UpdateAIProfileRequest(
    @SerializedName("preferred_time_start")
    val preferredTimeStart: String?,
    @SerializedName("preferred_time_end")
    val preferredTimeEnd: String?,
    @SerializedName("preference_cost_vs_time")
    val preferenceCostVsTime: Double?,
    val frequent_locations: List<FrequentLocation>? = null,
    val avoid_locations: List<FrequentLocation>? = null
)

data class FrequentLocation(
    val lat: Double,
    val lng: Double,
    val name: String
)

// ========== AI RECOMMENDATIONS ==========

data class AIRecommendationDto(
    @SerializedName("frequent_routes")
    val frequentRoutes: List<FrequentRoute>?,
    @SerializedName("best_times")
    val bestTimes: List<String>?,
    @SerializedName("estimated_savings")
    val estimatedSavings: Int?,
    @SerializedName("preferred_time")
    val preferredTime: String?,
    @SerializedName("ai_confidence")
    val aiConfidence: Double?
)

data class FrequentRoute(
    val count: Int,
    val pickup: String?,
    val dest: String?,
    val price: Double?
)

// ========== BATCH ==========

data class BatchDto(
    val id: Int,
    @SerializedName("driver_id")
    val driverId: Int,
    @SerializedName("batch_name")
    val batchName: String?,
    val status: String,
    @SerializedName("estimated_start_time")
    val estimatedStartTime: String?,
    @SerializedName("estimated_end_time")
    val estimatedEndTime: String?,
    @SerializedName("total_revenue")
    val totalRevenue: Double?,
    @SerializedName("total_distance")
    val totalDistance: Double?,
    @SerializedName("passenger_count")
    val passengerCount: Int?,
    @SerializedName("efficiency_score")
    val efficiencyScore: Double?,
    @SerializedName("ai_confidence")
    val aiConfidence: Double?,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("accepted_at")
    val acceptedAt: String?,
    @SerializedName("completed_at")
    val completedAt: String?,
    val passengers: List<BatchPassengerDto>? = null
)

data class BatchPassengerDto(
    val id: Int,
    @SerializedName("batch_id")
    val batchId: Int,
    @SerializedName("passenger_id")
    val passengerId: Int,
    @SerializedName("passenger_name")
    val passengerName: String?,
    @SerializedName("original_ride_id")
    val originalRideId: Int,
    @SerializedName("pickup_order")
    val pickupOrder: Int,
    @SerializedName("dropoff_order")
    val dropoffOrder: Int,
    @SerializedName("pickup_lat")
    val pickupLat: Double,
    @SerializedName("pickup_lng")
    val pickupLng: Double,
    @SerializedName("dropoff_lat")
    val dropoffLat: Double,
    @SerializedName("dropoff_lng")
    val dropoffLng: Double,
    @SerializedName("estimated_pickup_time")
    val estimatedPickupTime: String,
    @SerializedName("detour_km")
    val detourKm: Double?,
    @SerializedName("price_adjustment")
    val priceAdjustment: Double?,
    val status: String?
)

// ========== ROUTE PREVIEW ==========

data class RoutePreviewRequest(
    val waypoints: List<WaypointDto>
)

data class RoutePreviewDto(
    @SerializedName("total_distance")
    val totalDistance: Double,
    @SerializedName("total_duration")
    val totalDuration: Int,
    @SerializedName("total_price")
    val totalPrice: Double,
    val segments: List<RouteSegmentDto>?,
    val recommendations: List<RouteRecommendationDto>?
)

data class RouteSegmentDto(
    val from: String,
    val to: String,
    val distance: Double,
    val duration: Int,
    val price: Int
)

data class RouteRecommendationDto(
    val type: String,
    val value: Any,
    val label: String
)

// ========== RIDE OPTIMIZE ==========

data class OptimizeRidesRequest(
    @SerializedName("passenger_count")
    val passengerCount: Int,
    val rides: List<RideOptimizeDto>
)

data class RideOptimizeDto(
    val id: Int,
    @SerializedName("pickup_lat")
    val pickupLat: Double,
    @SerializedName("pickup_lng")
    val pickupLng: Double,
    @SerializedName("dest_lat")
    val destLat: Double,
    @SerializedName("dest_lng")
    val destLng: Double,
    val price: Double
)

data class OptimizeRidesDto(
    @SerializedName("optimized_order")
    val optimizedOrder: List<RideOptimizeDto>,
    @SerializedName("total_original_price")
    val totalOriginalPrice: Double,
    @SerializedName("total_optimized_price")
    val totalOptimizedPrice: Double,
    @SerializedName("discount_percent")
    val discountPercent: Double,
    @SerializedName("estimated_savings")
    val estimatedSavings: Double,
    @SerializedName("efficiency_score")
    val efficiencyScore: Double
)
