package com.laptrinhdidong.DoAn3.data.remote

import com.laptrinhdidong.DoAn3.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface DriverApiService {

    @GET("driver/profile")
    suspend fun getDriverProfile(): Response<DriverDto>

    @PUT("driver/profile")
    suspend fun updateDriverProfile(@Body request: UpdateDriverRequest): Response<DriverDto>

    @PUT("driver/status")
    suspend fun updateDriverStatus(@Body request: UpdateDriverStatusRequest): Response<DriverDto>

    @GET("driver/ride/available")
    suspend fun getAvailableRides(): Response<List<RideDto>>

    @POST("driver/ride/{id}/accept")
    suspend fun acceptRide(@Path("id") rideId: Int): Response<RideDto>

    @POST("driver/ride/{id}/reject")
    suspend fun rejectRide(@Path("id") rideId: Int): Response<Unit>

    @PUT("driver/ride/{id}/status")
    suspend fun updateRideStatus(
        @Path("id") rideId: Int,
        @Body request: UpdateRideStatusRequest
    ): Response<RideDto>

    @GET("driver/earnings")
    suspend fun getEarnings(
        @Query("from") fromDate: String,
        @Query("to") toDate: String
    ): Response<EarningsDto>

    @GET("driver/history")
    suspend fun getRideHistory(): Response<List<RideDto>>

    // ========== AI BATCH ==========
    @GET("ai/batch/available")
    suspend fun getAvailableBatches(): Response<List<BatchDto>>

    @POST("ai/batch/{id}/accept")
    suspend fun acceptBatch(@Path("id") batchId: Int): Response<BatchDto>

    @POST("ai/batch/{id}/reject")
    suspend fun rejectBatch(@Path("id") batchId: Int): Response<Unit>
}
