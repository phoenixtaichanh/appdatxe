package com.laptrinhdidong.DoAn3.di

import android.content.Context
import com.laptrinhdidong.DoAn3.AppConfig
import com.laptrinhdidong.DoAn3.data.local.SessionManager
import com.laptrinhdidong.DoAn3.data.remote.AuthInterceptor
import com.laptrinhdidong.DoAn3.data.remote.ApiService
import com.laptrinhdidong.DoAn3.data.repository.AIRepository
import com.laptrinhdidong.DoAn3.data.repository.AuthRepository
import com.laptrinhdidong.DoAn3.data.repository.DriverRepository
import com.laptrinhdidong.DoAn3.data.repository.PasswordResetRepository
import com.laptrinhdidong.DoAn3.data.repository.PaymentRepository
import com.laptrinhdidong.DoAn3.data.repository.RideRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSessionManager(@ApplicationContext context: Context): SessionManager {
        return SessionManager(context)
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(sessionManager: SessionManager): AuthInterceptor {
        return AuthInterceptor(sessionManager)
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .build()
                chain.proceed(request)
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(AppConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(sessionManager: SessionManager, apiService: ApiService): AuthRepository {
        return AuthRepository(sessionManager, apiService)
    }

    @Provides
    @Singleton
    fun provideRideRepository(apiService: ApiService): RideRepository {
        return RideRepository(apiService)
    }

    @Provides
    @Singleton
    fun provideDriverRepository(apiService: ApiService): DriverRepository {
        return DriverRepository(apiService)
    }

    @Provides
    @Singleton
    fun provideAIRepository(apiService: ApiService): AIRepository {
        return AIRepository(apiService)
    }

    @Provides
    @Singleton
    fun providePasswordResetRepository(apiService: ApiService): PasswordResetRepository {
        return PasswordResetRepository(apiService)
    }

    @Provides
    @Singleton
    fun providePaymentRepository(apiService: ApiService): PaymentRepository {
        return PaymentRepository(apiService)
    }
}
