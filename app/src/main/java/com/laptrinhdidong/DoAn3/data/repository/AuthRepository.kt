package com.laptrinhdidong.DoAn3.data.repository

import com.laptrinhdidong.DoAn3.data.local.SessionManager
import com.laptrinhdidong.DoAn3.data.remote.ApiService
import com.laptrinhdidong.DoAn3.data.remote.SocketManager
import com.laptrinhdidong.DoAn3.data.remote.dto.AuthResponse
import com.laptrinhdidong.DoAn3.data.remote.dto.LoginRequest
import com.laptrinhdidong.DoAn3.data.remote.dto.RegisterRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(
    private val sessionManager: SessionManager,
    private val apiService: ApiService
) {

    suspend fun login(email: String, password: String): Result<AuthResponse> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.login(LoginRequest(email, password))
                if (response.isSuccessful) {
                    val body = response.body()!!
                    if (body.success && body.token != null) {
                        body.token.let {
                            sessionManager.authToken = it
                            SocketManager.connect(it)
                        }
                        body.user?.let {
                            sessionManager.saveUser(
                                it.id,
                                it.name ?: "",
                                it.email ?: "",
                                it.userType ?: "passenger"
                            )
                        }
                        Result.success(body)
                    } else {
                        Result.failure(Exception(body.message ?: "Login failed"))
                    }
                } else {
                    Result.failure(Exception("Login failed: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun register(
        name: String,
        email: String,
        password: String,
        phone: String,
        userType: String,
        vehicleType: String? = null,
        carModel: String? = null,
        carColor: String? = null,
        carPlate: String? = null
    ): Result<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.register(
                RegisterRequest(email, password, name, phone, userType, vehicleType, carModel, carColor, carPlate)
            )
            if (response.isSuccessful) {
                val body = response.body()!!
                if (body.success && body.token != null) {
                    body.token.let {
                        sessionManager.authToken = it
                        SocketManager.connect(it)
                    }
                    body.user?.let {
                        sessionManager.saveUser(
                            it.id,
                            it.name ?: name,
                            it.email ?: email,
                            it.userType ?: userType
                        )
                    }
                    Result.success(body)
                } else {
                    Result.failure(Exception(body.message ?: "Registration failed"))
                }
            } else {
                Result.failure(Exception("Registration failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        SocketManager.disconnect()
        sessionManager.clearSession()
    }

    fun isLoggedIn() = sessionManager.isLoggedIn
    fun isDriver() = sessionManager.isDriver
    fun isPassenger() = sessionManager.isPassenger
}
