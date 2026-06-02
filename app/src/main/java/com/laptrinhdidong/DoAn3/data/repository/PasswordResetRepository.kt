package com.laptrinhdidong.DoAn3.data.repository

import com.laptrinhdidong.DoAn3.data.remote.ApiService
import com.laptrinhdidong.DoAn3.data.remote.dto.ForgotPasswordRequest
import com.laptrinhdidong.DoAn3.data.remote.dto.ForgotPasswordResponse
import com.laptrinhdidong.DoAn3.data.remote.dto.ResetPasswordRequest
import com.laptrinhdidong.DoAn3.data.remote.dto.ResetPasswordResponse
import com.laptrinhdidong.DoAn3.data.remote.dto.VerifyOtpRequest
import com.laptrinhdidong.DoAn3.data.remote.dto.VerifyOtpResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PasswordResetRepository(
    private val apiService: ApiService
) {

    suspend fun sendOtp(email: String): Result<ForgotPasswordResponse> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.forgotPassword(ForgotPasswordRequest(email))
                if (response.isSuccessful) {
                    response.body()?.let { Result.success(it) }
                        ?: Result.failure(Exception("Empty response"))
                } else {
                    Result.failure(Exception("Server error: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun verifyOtp(email: String, otpCode: String): Result<VerifyOtpResponse> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.verifyOtp(VerifyOtpRequest(email, otpCode))
                if (response.isSuccessful) {
                    response.body()?.let { Result.success(it) }
                        ?: Result.failure(Exception("Empty response"))
                } else {
                    Result.failure(Exception("Server error: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun resetPassword(email: String, otpCode: String, newPassword: String): Result<ResetPasswordResponse> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.resetPassword(ResetPasswordRequest(email, otpCode, newPassword))
                if (response.isSuccessful) {
                    response.body()?.let { Result.success(it) }
                        ?: Result.failure(Exception("Empty response"))
                } else {
                    Result.failure(Exception("Server error: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun resendOtp(email: String): Result<ForgotPasswordResponse> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.resendOtp(ForgotPasswordRequest(email))
                if (response.isSuccessful) {
                    response.body()?.let { Result.success(it) }
                        ?: Result.failure(Exception("Empty response"))
                } else {
                    Result.failure(Exception("Server error: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
