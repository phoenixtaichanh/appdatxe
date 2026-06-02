package com.laptrinhdidong.DoAn3.data.remote

import com.laptrinhdidong.DoAn3.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ========== AUTH ==========
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/fcm/register")
    suspend fun registerFcmToken(@Body body: Map<String, String?>): Response<Unit>

    // ========== PASSWORD RESET ==========
    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<ForgotPasswordResponse>

    @POST("auth/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): Response<VerifyOtpResponse>

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<ResetPasswordResponse>

    @POST("auth/resend-otp")
    suspend fun resendOtp(@Body request: ForgotPasswordRequest): Response<ForgotPasswordResponse>

    // ========== PAYMENT ==========
    @GET("payments/methods")
    suspend fun getPaymentMethods(): Response<List<PaymentMethodDto>>

    @POST("payments/create")
    suspend fun createPayment(@Body request: CreatePaymentRequest): Response<CreatePaymentResponse>

    @GET("payments/{id}")
    suspend fun getPayment(@Path("id") paymentId: Int): Response<PaymentDetailDto>

    @POST("payments/{id}/confirm")
    suspend fun confirmPayment(@Path("id") paymentId: Int, @Body request: ConfirmPaymentRequest): Response<ConfirmPaymentResponse>

    @GET("payments")
    suspend fun getPaymentHistory(
        @Query("type") type: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<PaymentHistoryResponse>

    // ========== USER ==========
    @GET("users/me")
    suspend fun getCurrentUser(): Response<UserDto>

    @GET("users/{id}")
    suspend fun getUser(@Path("id") userId: Int): Response<UserDto>

    @PUT("users/{id}")
    suspend fun updateUser(@Path("id") userId: Int, @Body request: UpdateUserRequest): Response<UserDto>

    @GET("users/drivers/nearby")
    suspend fun getNearbyDrivers(
        @Query("lat") latitude: Double,
        @Query("lng") longitude: Double,
        @Query("radius") radiusKm: Double = 5.0
    ): Response<List<DriverDto>>

    // ========== RIDE ==========
    @POST("rides/request")
    suspend fun requestRide(@Body request: RideRequestDto): Response<RideDto>

    @GET("rides/{id}")
    suspend fun getRide(@Path("id") rideId: Int): Response<RideDto>

    @PUT("rides/{id}/status")
    suspend fun updateRideStatus(
        @Path("id") rideId: Int,
        @Body request: UpdateRideStatusRequest
    ): Response<RideDto>

    @GET("rides")
    suspend fun getRideHistory(): Response<List<RideDto>>

    @POST("rides/{id}/rate")
    suspend fun rateRide(
        @Path("id") rideId: Int,
        @Body request: RateRideRequest
    ): Response<RideDto>

    // ========== LOCATION ==========
    @POST("location/update")
    suspend fun updateDriverLocation(@Body request: UpdateLocationRequest): Response<Unit>

    @GET("location/driver/{id}")
    suspend fun getDriverLocation(@Path("id") driverId: Int): Response<DriverLocationDto>

    // ========== AI SCHEDULE ==========
    @POST("ai/schedule/create")
    suspend fun createAISchedule(@Body request: CreateScheduleRequest): Response<AIScheduleDto>

    @GET("ai/schedule/{id}")
    suspend fun getAISchedule(@Path("id") scheduleId: Int): Response<AIScheduleDto>

    @PUT("ai/schedule/{id}")
    suspend fun updateAISchedule(
        @Path("id") scheduleId: Int,
        @Body request: Map<String, Any>
    ): Response<AIScheduleDto>

    @GET("ai/schedule/{id}/alternatives")
    suspend fun getRouteAlternatives(@Path("id") scheduleId: Int): Response<List<RouteAlternativeDto>>

    @POST("ai/schedule/{id}/optimize")
    suspend fun optimizeSchedule(
        @Path("id") scheduleId: Int,
        @Body request: Map<String, String>
    ): Response<List<RouteAlternativeDto>>

    // ========== AI PROFILE ==========
    @GET("ai/profile")
    suspend fun getAIProfile(): Response<AIProfileDto>

    @PUT("ai/profile")
    suspend fun updateAIProfile(@Body request: UpdateAIProfileRequest): Response<AIProfileDto>

    // ========== AI RECOMMENDATIONS ==========
    @GET("ai/recommendations")
    suspend fun getAIRecommendations(): Response<AIRecommendationDto>

    @POST("ai/route/preview")
    suspend fun previewRoute(@Body request: RoutePreviewRequest): Response<RoutePreviewDto>

    // ========== AI HISTORY ==========
    @GET("ai/history")
    suspend fun getAIHistory(): Response<List<AIScheduleDto>>

    // ========== AI BATCH ==========
    @GET("ai/batch/available")
    suspend fun getAvailableBatches(): Response<List<BatchDto>>

    @POST("ai/batch/{id}/accept")
    suspend fun acceptBatch(@Path("id") batchId: Int): Response<BatchDto>

    @POST("ai/batch/{id}/reject")
    suspend fun rejectBatch(@Path("id") batchId: Int): Response<Unit>

    // ========== DRIVER ==========
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
    suspend fun updateDriverRideStatus(
        @Path("id") rideId: Int,
        @Body request: UpdateRideStatusRequest
    ): Response<RideDto>

    @GET("driver/earnings")
    suspend fun getEarnings(
        @Query("from") fromDate: String,
        @Query("to") toDate: String
    ): Response<EarningsDto>

    @GET("driver/history")
    suspend fun getDriverHistory(): Response<List<RideDto>>

    // ========== CHAT ==========
    @GET("chat/{rideId}/messages")
    suspend fun getChatMessages(@Path("rideId") rideId: Int): Response<List<ChatMessageDto>>

    @POST("chat/{rideId}/send")
    suspend fun sendChatMessage(@Path("rideId") rideId: Int, @Body body: Map<String, String>): Response<Map<String, Any>>

    // ========== SUPPORT / FAQ ==========
    @GET("faq")
    suspend fun getFAQs(@Query("category") category: String? = null): Response<List<FaqDto>>

    @GET("faq/categories")
    suspend fun getFAQCategories(): Response<List<FaqCategoryDto>>

    @GET("faq/{id}")
    suspend fun getFAQDetail(@Path("id") faqId: Int): Response<FaqDto>

    @POST("faq/{id}/helpful")
    suspend fun rateFAQ(@Path("id") faqId: Int, @Body body: Map<String, Boolean>): Response<Unit>

    // ========== CONSULTANT CHAT ==========
    @GET("support/conversations")
    suspend fun getConsultantConversations(
        @Query("status") status: String? = null,
        @Query("category") category: String? = null
    ): Response<List<ConsultantConversationDto>>

    @POST("support/conversations")
    suspend fun createConsultantConversation(@Body body: Map<String, String>): Response<Map<String, Any>>

    @GET("support/conversations/{id}/messages")
    suspend fun getConsultantMessages(@Path("id") conversationId: Int): Response<List<ConsultantMessageDto>>

    @POST("support/conversations/{id}/messages")
    suspend fun sendConsultantMessage(@Path("id") conversationId: Int, @Body body: Map<String, String>): Response<Map<String, Any>>

    @PUT("support/conversations/{id}/resolve")
    suspend fun resolveConsultantConversation(@Path("id") conversationId: Int): Response<Map<String, Any>>

    @PUT("support/conversations/{id}/close")
    suspend fun closeConsultantConversation(@Path("id") conversationId: Int, @Body body: Map<String, Any>): Response<Map<String, Any>>

    @GET("support/unread")
    suspend fun getUnreadConsultantCount(): Response<Map<String, Int>>
}
