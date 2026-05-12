package com.laptrinhdidong.DoAn3.data.remote.dto

import com.google.gson.annotations.SerializedName

// ========== AUTH ==========

data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String,
    val phone: String,
    @SerializedName("user_type")
    val userType: String = "passenger" // "passenger" | "driver"
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val success: Boolean,
    val message: String?,
    val token: String?,
    val user: UserDto?
)

// ========== USER ==========

data class UserDto(
    val id: Int,
    val name: String?,
    val email: String?,
    val phone: String?,
    @SerializedName("user_type")
    val userType: String?,
    @SerializedName("profile_image")
    val profileImage: String?,
    val rating: Double?,
    @SerializedName("total_rides")
    val totalRides: Int?,
    @SerializedName("created_at")
    val createdAt: String?
)

data class UpdateUserRequest(
    val name: String?,
    val phone: String?,
    @SerializedName("profile_image")
    val profileImage: String?
)

// ========== DRIVER ==========

data class DriverDto(
    val id: Int,
    val name: String?,
    val phone: String?,
    val rating: Double,
    @SerializedName("total_rides")
    val totalRides: Int,
    val latitude: Double?,
    val longitude: Double?,
    @SerializedName("car_model")
    val carModel: String?,
    @SerializedName("car_color")
    val carColor: String?,
    @SerializedName("license_plate")
    val licensePlate: String?,
    @SerializedName("profile_image")
    val profileImage: String?,
    @SerializedName("is_available")
    val isAvailable: Boolean,
    @SerializedName("distance_km")
    val distanceKm: Double?,
    @SerializedName("vehicle_type")
    val vehicleType: String?
)

data class UpdateDriverRequest(
    val name: String?,
    val phone: String?,
    @SerializedName("car_model")
    val carModel: String?,
    @SerializedName("car_color")
    val carColor: String?,
    @SerializedName("license_plate")
    val licensePlate: String?
)

data class UpdateDriverStatusRequest(
    @SerializedName("is_available")
    val isAvailable: Boolean,
    val latitude: Double?,
    val longitude: Double?
)

// ========== RIDE ==========

data class RideDto(
    val id: Int,
    @SerializedName("passenger_id")
    val passengerId: Int,
    @SerializedName("driver_id")
    val driverId: Int?,
    @SerializedName("passenger_name")
    val passengerName: String?,
    @SerializedName("driver_name")
    val driverName: String?,
    @SerializedName("pickup_lat")
    val pickupLat: Double,
    @SerializedName("pickup_lng")
    val pickupLng: Double,
    @SerializedName("pickup_address")
    val pickupAddress: String,
    @SerializedName("dest_lat")
    val destLat: Double,
    @SerializedName("dest_lng")
    val destLng: Double,
    @SerializedName("dest_address")
    val destAddress: String,
    @SerializedName("distance_km")
    val distanceKm: Double,
    @SerializedName("duration_min")
    val durationMin: Int,
    val price: Double,
    @SerializedName("vehicle_type")
    val vehicleType: String?,
    val status: String, // "pending" | "accepted" | "arrived" | "in_progress" | "completed" | "cancelled"
    @SerializedName("driver_rating")
    val driverRating: Double?,
    @SerializedName("passenger_rating")
    val passengerRating: Double?,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("started_at")
    val startedAt: String?,
    @SerializedName("completed_at")
    val completedAt: String?
)

data class RideRequestDto(
    @SerializedName("pickup_lat")
    val pickupLat: Double,
    @SerializedName("pickup_lng")
    val pickupLng: Double,
    @SerializedName("pickup_address")
    val pickupAddress: String,
    @SerializedName("dest_lat")
    val destLat: Double,
    @SerializedName("dest_lng")
    val destLng: Double,
    @SerializedName("dest_address")
    val destAddress: String,
    @SerializedName("vehicle_type")
    val vehicleType: String = "motorbike"
)

enum class VehicleType(val key: String, val label: String, val icon: String) {
    MOTORBIKE("motorbike", "Xe may", "2__"),
    CAR_4_SEATS("car_4_seats", "O to 4 cho", "4__"),
    CAR_7_SEATS("car_7_seats", "O to 7 cho", "7__")
}

data class UpdateRideStatusRequest(
    val status: String,
    @SerializedName("driver_lat")
    val driverLat: Double?,
    @SerializedName("driver_lng")
    val driverLng: Double?
)

data class RateRideRequest(
    val rating: Int, // 1-5
    val comment: String?
)

// ========== LOCATION ==========

data class UpdateLocationRequest(
    val latitude: Double,
    val longitude: Double
)

data class DriverLocationDto(
    @SerializedName("driver_id")
    val driverId: Int,
    val latitude: Double,
    val longitude: Double,
    @SerializedName("updated_at")
    val updatedAt: String
)

// ========== EARNINGS ==========

data class EarningsDto(
    @SerializedName("today_earnings")
    val todayEarnings: Double,
    @SerializedName("week_earnings")
    val weekEarnings: Double,
    @SerializedName("month_earnings")
    val monthEarnings: Double,
    @SerializedName("total_rides")
    val totalRides: Int,
    @SerializedName("total_earnings")
    val totalEarnings: Double
)

// ========== COMMON ==========

data class ApiError(
    val success: Boolean = false,
    val message: String,
    val error: String?
)

data class ApiResponse<T>(
    val success: Boolean,
    val message: String?,
    val data: T?
) {
    val isSuccess: Boolean get() = success && data != null
    fun getOrThrow(): T = data ?: throw Exception(message ?: "Unknown error")
}

data class ChatMessageDto(
    val id: Int,
    @SerializedName("ride_id")
    val rideId: Int,
    @SerializedName("sender_id")
    val senderId: Int,
    @SerializedName("sender_type")
    val senderType: String,
    @SerializedName("sender_name")
    val senderName: String?,
    val message: String,
    @SerializedName("message_type")
    val messageType: String,
    @SerializedName("is_read")
    val isRead: Boolean,
    @SerializedName("created_at")
    val createdAt: String
)
