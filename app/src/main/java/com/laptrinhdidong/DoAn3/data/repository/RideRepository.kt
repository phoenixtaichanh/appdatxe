package com.laptrinhdidong.DoAn3.data.repository

import com.laptrinhdidong.DoAn3.data.remote.ApiService
import com.laptrinhdidong.DoAn3.data.remote.dto.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RideRepository(private val apiService: ApiService) {

    suspend fun requestRide(
        pickupLat: Double,
        pickupLng: Double,
        pickupAddress: String,
        destLat: Double,
        destLng: Double,
        destAddress: String,
        vehicleType: String = "motorbike"
    ): Result<RideDto> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.requestRide(
                RideRequestDto(pickupLat, pickupLng, pickupAddress, destLat, destLng, destAddress, vehicleType)
            )
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.failure(Exception("Empty response"))
            } else {
                Result.failure(Exception("Failed to request ride: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRide(rideId: Int): Result<RideDto> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getRide(rideId)
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.failure(Exception("Empty response"))
            } else {
                Result.failure(Exception("Failed to get ride: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateRideStatus(rideId: Int, status: String): Result<RideDto> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.updateRideStatus(
                    rideId,
                    UpdateRideStatusRequest(status, null, null)
                )
                if (response.isSuccessful) {
                    response.body()?.let { Result.success(it) }
                        ?: Result.failure(Exception("Empty response"))
                } else {
                    Result.failure(Exception("Failed to update ride status: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getNearbyDrivers(lat: Double, lng: Double): Result<List<DriverDto>> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getNearbyDrivers(lat, lng)
                if (response.isSuccessful) {
                    response.body()?.let { Result.success(it) }
                        ?: Result.success(emptyList())
                } else {
                    Result.failure(Exception("Failed to get nearby drivers: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun rateRide(rideId: Int, rating: Int, comment: String?): Result<RideDto> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.rateRide(rideId, RateRideRequest(rating, comment))
                if (response.isSuccessful) {
                    response.body()?.let { Result.success(it) }
                        ?: Result.failure(Exception("Empty response"))
                } else {
                    Result.failure(Exception("Failed to rate ride: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getRideHistory(): Result<List<RideDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getRideHistory()
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.success(emptyList())
            } else {
                Result.failure(Exception("Failed to get ride history: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getChatMessages(rideId: Int): Result<List<ChatMessageDto>> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getChatMessages(rideId)
                if (response.isSuccessful) {
                    response.body()?.let { Result.success(it) }
                        ?: Result.success(emptyList())
                } else {
                    Result.failure(Exception("Failed to get messages: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun sendChatMessage(rideId: Int, message: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.sendChatMessage(rideId, mapOf("message" to message))
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Failed to send message: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
