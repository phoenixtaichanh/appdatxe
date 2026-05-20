package com.laptrinhdidong.DoAn3.data.remote.dto

import com.google.gson.annotations.SerializedName

// ========== PAYMENT ==========

data class PaymentMethodDto(
    val code: String,
    val label: String,
    val icon: String,
    val enabled: Boolean
)

data class PaymentDetailDto(
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
    val isSandbox: Boolean?
)

data class CreatePaymentRequest(
    @SerializedName("ride_id")
    val rideId: Int,
    @SerializedName("payment_method")
    val paymentMethod: String
)

data class CreatePaymentResponse(
    val success: Boolean,
    val message: String?,
    val data: PaymentDetailDto?
)

data class ConfirmPaymentRequest(
    val status: String
)

data class ConfirmPaymentResponse(
    val success: Boolean,
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
    val success: Boolean,
    val data: List<PaymentHistoryItemDto>,
    val pagination: PaymentPaginationDto?
)
