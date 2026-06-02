package com.laptrinhdidong.DoAn3.data.remote.dto

import com.google.gson.annotations.SerializedName

// ========== AUTH ==========

data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String,
    val phone: String,
    @SerializedName("user_type")
    val userType: String = "passenger", // "passenger" | "driver"
    @SerializedName("vehicle_type")
    val vehicleType: String? = null, // "motorbike" | "car_4_seats" | "car_7_seats"
    @SerializedName("car_model")
    val carModel: String? = null,
    @SerializedName("car_color")
    val carColor: String? = null,
    @SerializedName("car_plate")
    val carPlate: String? = null
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

data class VehicleTypeInfo(
    val carModel: String?,
    val carColor: String?,
    val licensePlate: String?
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
    @SerializedName("vehicle_type")
    val vehicleType: VehicleTypeInfo?,
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
    val completedAt: String?,
    @SerializedName("is_rated")
    val isRated: Boolean = false
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

data class EarningsDto(
    @SerializedName("summary")
    val summary: EarningsSummary,
    @SerializedName("daily")
    val daily: List<EarningsDailyEntry>?,
    @SerializedName("comparison")
    val comparison: EarningsComparison,
    @SerializedName("stats")
    val stats: EarningsStats
) {
    val todayEarnings: Double get() = summary.todayEarnings
    val weekEarnings: Double get() = summary.weekEarnings
    val monthEarnings: Double get() = summary.monthEarnings
    val totalRides: Int get() = summary.totalRides
    val totalEarnings: Double get() = summary.totalEarnings
}

data class EarningsSummary(
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

data class EarningsDailyEntry(
    val date: String,
    val amount: Double,
    @SerializedName("ride_count")
    val rideCount: Int
)

data class EarningsComparison(
    @SerializedName("this_week")
    val thisWeek: Double,
    @SerializedName("last_week")
    val lastWeek: Double
)

data class EarningsStats(
    @SerializedName("total_rides")
    val totalRides: Int,
    @SerializedName("avg_rating")
    val avgRating: String?,
    val completed: Int,
    val cancelled: Int
)

// ========== PAYMENT ==========

data class PaymentMethodDto(
    val code: String,
    val label: String,
    val icon: String,
    val enabled: Boolean
)

data class CreatePaymentRequest(
    @SerializedName("ride_id")
    val rideId: Int,
    @SerializedName("payment_method")
    val paymentMethod: String
)

data class CreatePaymentResponse(
    val id: Int,
    @SerializedName("payment_id")
    val paymentId: Int,
    val amount: Double,
    @SerializedName("payment_method")
    val paymentMethod: String,
    val status: String,
    @SerializedName("qr_url")
    val qrUrl: String?,
    @SerializedName("payment_url")
    val paymentUrl: String?,
    @SerializedName("is_sandbox")
    val isSandbox: Boolean?,
    @SerializedName("payment_info")
    val paymentInfo: Map<String, Any>?
)

data class PaymentDetailDto(
    val id: Int,
    @SerializedName("ride_id")
    val rideId: Int?,
    val amount: Double,
    val type: String,
    val category: String?,
    @SerializedName("payment_method")
    val paymentMethod: String,
    val status: String,
    val description: String?,
    @SerializedName("created_at")
    val createdAt: String?
)

data class ConfirmPaymentRequest(
    val status: String
)

data class ConfirmPaymentResponse(
    val message: String?
)

data class PaymentHistoryItemDto(
    val id: Int,
    @SerializedName("ride_id")
    val rideId: Int?,
    val amount: Double,
    val type: String,
    val category: String?,
    @SerializedName("payment_method")
    val paymentMethod: String,
    val status: String,
    val description: String?,
    @SerializedName("pickup_address")
    val pickupAddress: String?,
    @SerializedName("dest_address")
    val destAddress: String?,
    @SerializedName("ride_status")
    val rideStatus: String?,
    @SerializedName("created_at")
    val createdAt: String?
)

data class PaymentPaginationDto(
    val page: Int,
    val limit: Int,
    val total: Int,
    @SerializedName("total_pages")
    val totalPages: Int
)

data class PaymentHistoryResponse(
    val data: List<PaymentHistoryItemDto>,
    val pagination: PaymentPaginationDto
)

// ========== PASSWORD RESET ==========

data class ForgotPasswordRequest(
    val email: String
)

data class ForgotPasswordResponse(
    val success: Boolean,
    val message: String?,
    @SerializedName("_dev_otp")
    val devOtp: String?
)

data class VerifyOtpRequest(
    val email: String,
    @SerializedName("otp_code")
    val otpCode: String
)

data class VerifyOtpResponse(
    val success: Boolean,
    val message: String?,
    val email: String?
)

data class ResetPasswordRequest(
    val email: String,
    @SerializedName("otp_code")
    val otpCode: String,
    @SerializedName("new_password")
    val newPassword: String
)

data class ResetPasswordResponse(
    val success: Boolean,
    val message: String?
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

// ========== FAQ ==========

data class FaqDto(
    val id: Int,
    val category: String,
    val question: String,
    val answer: String,
    @SerializedName("display_order")
    val displayOrder: Int,
    @SerializedName("view_count")
    val viewCount: Int,
    @SerializedName("helpful_count")
    val helpfulCount: Int,
    @SerializedName("unhelpful_count")
    val unhelpfulCount: Int,
    @SerializedName("created_at")
    val createdAt: String?
)

data class FaqCategoryDto(
    val key: String,
    val label: String,
    val icon: String,
    val color: String,
    val count: Int
)

// ========== CONSULTANT CHAT ==========

data class ConsultantConversationDto(
    val id: Int,
    @SerializedName("customer_id")
    val customerId: Int,
    @SerializedName("consultant_id")
    val consultantId: Int?,
    @SerializedName("subject")
    val subject: String?,
    val category: String,
    val status: String,
    val priority: String,
    @SerializedName("customer_name")
    val customerName: String?,
    @SerializedName("customer_email")
    val customerEmail: String?,
    @SerializedName("consultant_name")
    val consultantName: String?,
    @SerializedName("unread_count")
    val unreadCount: Int,
    @SerializedName("last_message")
    val lastMessage: String?,
    @SerializedName("last_message_at")
    val lastMessageAt: String?,
    @SerializedName("created_at")
    val createdAt: String?
)

data class ConsultantMessageDto(
    val id: Int,
    @SerializedName("conversation_id")
    val conversationId: Int,
    @SerializedName("sender_id")
    val senderId: Int,
    @SerializedName("sender_type")
    val senderType: String,
    @SerializedName("sender_name")
    val senderName: String?,
    val message: String,
    @SerializedName("message_type")
    val messageType: String,
    @SerializedName("attachment_url")
    val attachmentUrl: String?,
    @SerializedName("is_read")
    val isRead: Boolean,
    @SerializedName("created_at")
    val createdAt: String
)
