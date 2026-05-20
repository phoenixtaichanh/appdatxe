package com.laptrinhdidong.DoAn3.data.remote.dto

import com.google.gson.annotations.SerializedName

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
