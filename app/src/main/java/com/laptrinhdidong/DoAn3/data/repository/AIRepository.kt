package com.laptrinhdidong.DoAn3.data.repository

import com.laptrinhdidong.DoAn3.data.remote.ApiService
import com.laptrinhdidong.DoAn3.data.remote.dto.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AIRepository(private val apiService: ApiService) {

    suspend fun getAIHistory(): Result<List<AIScheduleDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAIHistory()
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.success(emptyList())
            } else {
                Result.failure(Exception("Failed to get AI history: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createSchedule(
        scheduleName: String,
        scheduledDate: String,
        optimizationType: String,
        waypoints: List<WaypointDto>
    ): Result<AIScheduleDto> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.createAISchedule(
                CreateScheduleRequest(scheduleName, scheduledDate, optimizationType, waypoints)
            )
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.failure(Exception("Empty response"))
            } else {
                Result.failure(Exception("Failed to create AI schedule: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSchedule(scheduleId: Int): Result<AIScheduleDto> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAISchedule(scheduleId)
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.failure(Exception("Empty response"))
            } else {
                Result.failure(Exception("Failed to get AI schedule: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun optimizeSchedule(scheduleId: Int, optimization: String): Result<List<RouteAlternativeDto>> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.optimizeSchedule(scheduleId, mapOf("optimization_type" to optimization))
                if (response.isSuccessful) {
                    response.body()?.let { Result.success(it) }
                        ?: Result.success(emptyList())
                } else {
                    Result.failure(Exception("Failed to optimize schedule: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getAIRecommendations(): Result<AIRecommendationDto> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAIRecommendations()
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.failure(Exception("Empty response"))
            } else {
                Result.failure(Exception("Failed to get recommendations: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun previewRoute(waypoints: List<WaypointDto>): Result<RoutePreviewDto> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.previewRoute(RoutePreviewRequest(waypoints))
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.failure(Exception("Empty response"))
            } else {
                Result.failure(Exception("Failed to preview route: ${response.code()}"))
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
}
