package com.laptrinhdidong.DoAn3.data.remote

import com.laptrinhdidong.DoAn3.data.local.SessionManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val sessionManager: SessionManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Skip auth header for login/register and password reset endpoints
        val path = originalRequest.url.encodedPath
        val noAuthPaths = listOf(
            "/auth/login",
            "/auth/register",
            "/auth/forgot-password",
            "/auth/verify-otp",
            "/auth/reset-password",
            "/auth/resend-otp"
        )
        if (noAuthPaths.any { path.contains(it) }) {
            return chain.proceed(originalRequest)
        }

        val token = sessionManager.authToken

        return if (!token.isNullOrEmpty()) {
            val newRequest = originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
            chain.proceed(newRequest)
        } else {
            chain.proceed(originalRequest)
        }
    }
}
