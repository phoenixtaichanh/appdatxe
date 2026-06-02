package com.laptrinhdidong.DoAn3.data.repository

import com.laptrinhdidong.DoAn3.data.remote.ApiService
import com.laptrinhdidong.DoAn3.data.remote.dto.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PaymentRepository(
    private val apiService: ApiService
) {

    suspend fun getPaymentMethods(): Result<List<PaymentMethodDto>> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getPaymentMethods()
                if (response.isSuccessful) {
                    Result.success(response.body() ?: emptyList())
                } else {
                    Result.failure(Exception("Failed to load payment methods: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun createPayment(rideId: Int, paymentMethod: String): Result<CreatePaymentResponse> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.createPayment(CreatePaymentRequest(rideId, paymentMethod))
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

    suspend fun getPayment(paymentId: Int): Result<PaymentDetailDto> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getPayment(paymentId)
                if (response.isSuccessful) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Server error: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun confirmPayment(paymentId: Int, status: String): Result<ConfirmPaymentResponse> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.confirmPayment(paymentId, ConfirmPaymentRequest(status))
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

    suspend fun getPaymentHistory(type: String? = null, page: Int = 1): Result<PaymentHistoryResponse> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getPaymentHistory(type, page)
                if (response.isSuccessful) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Server error: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
