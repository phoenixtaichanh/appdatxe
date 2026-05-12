package com.laptrinhdidong.DoAn3.data.repository

import com.laptrinhdidong.DoAn3.data.remote.ApiService
import com.laptrinhdidong.DoAn3.data.remote.dto.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DriverRepository(private val apiService: ApiService) {

    suspend fun getDriverProfile(): Result<DriverDto> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getDriverProfile()
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.failure(Exception("Empty response"))
            } else {
                Result.failure(Exception("Failed to get driver profile: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateDriverProfile(
        name: String?, phone: String?, carModel: String?, carColor: String?, licensePlate: String?
    ): Result<DriverDto> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.updateDriverProfile(
                UpdateDriverRequest(name, phone, carModel, carColor, licensePlate)
            )
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.failure(Exception("Empty response"))
            } else {
                Result.failure(Exception("Failed to update driver profile: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateDriverStatus(isAvailable: Boolean, latitude: Double?, longitude: Double?): Result<DriverDto> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.updateDriverStatus(
                    UpdateDriverStatusRequest(isAvailable, latitude, longitude)
                )
                if (response.isSuccessful) {
                    response.body()?.let { Result.success(it) }
                        ?: Result.failure(Exception("Empty response"))
                } else {
                    Result.failure(Exception("Failed to update driver status: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getAvailableRides(): Result<List<RideDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAvailableRides()
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.success(emptyList())
            } else {
                Result.failure(Exception("Failed to get available rides: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun acceptRide(rideId: Int): Result<RideDto> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.acceptRide(rideId)
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.failure(Exception("Empty response"))
            } else {
                Result.failure(Exception("Failed to accept ride: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun rejectRide(rideId: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.rejectRide(rideId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to reject ride"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateRideStatus(rideId: Int, status: String): Result<RideDto> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.updateDriverRideStatus(
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

    suspend fun getEarnings(fromDate: String, toDate: String): Result<EarningsDto> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getEarnings(fromDate, toDate)
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.failure(Exception("Empty response"))
            } else {
                Result.failure(Exception("Failed to get earnings: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDriverHistory(): Result<List<RideDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getDriverHistory()
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

    suspend fun getAvailableBatches(): Result<List<BatchDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAvailableBatches()
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.success(emptyList())
            } else {
                Result.failure(Exception("Failed to get available batches: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun acceptBatch(batchId: Int): Result<BatchDto> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.acceptBatch(batchId)
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.failure(Exception("Empty response"))
            } else {
                Result.failure(Exception("Failed to accept batch: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun rejectBatch(batchId: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.rejectBatch(batchId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to reject batch"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
