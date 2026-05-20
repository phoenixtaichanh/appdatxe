package com.laptrinhdidong.DoAn3.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.laptrinhdidong.DoAn3.data.local.SessionManager
import com.laptrinhdidong.DoAn3.ui.screens.HistoryScreen
import com.laptrinhdidong.DoAn3.ui.screens.ProfileScreen
import com.laptrinhdidong.DoAn3.ui.screens.RideDetailScreen
import com.laptrinhdidong.DoAn3.ui.screens.auth.AuthScreen
import com.laptrinhdidong.DoAn3.ui.screens.auth.ForgotPasswordScreen
import com.laptrinhdidong.DoAn3.ui.screens.auth.OtpVerificationScreen
import com.laptrinhdidong.DoAn3.ui.screens.auth.ResetPasswordScreen
import com.laptrinhdidong.DoAn3.ui.screens.splash.SplashScreen
import com.laptrinhdidong.DoAn3.ui.screens.splash.SplashDestination
import com.laptrinhdidong.DoAn3.ui.screens.splash.SplashViewModel
import com.laptrinhdidong.DoAn3.ui.screens.ai.AIChatScreen
import com.laptrinhdidong.DoAn3.ui.screens.ai.AIProfileScreen
import com.laptrinhdidong.DoAn3.ui.screens.ai.AIRecommendationsScreen
import com.laptrinhdidong.DoAn3.ui.screens.ai.AIScheduleScreen
import com.laptrinhdidong.DoAn3.ui.screens.driver.BatchOfferScreen
import com.laptrinhdidong.DoAn3.ui.screens.driver.DriverHomeScreen
import com.laptrinhdidong.DoAn3.ui.screens.driver.EarningsScreen
import com.laptrinhdidong.DoAn3.ui.screens.passenger.PassengerHomeScreen
import com.laptrinhdidong.DoAn3.ui.theme.DarkBackground
import com.laptrinhdidong.DoAn3.ui.theme.DarkSurface
import com.laptrinhdidong.DoAn3.ui.theme.TextPrimary
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Auth : Screen("auth")
    object PassengerHome : Screen("passenger_home")
    object DriverHome : Screen("driver_home")
    object RideDetail : Screen("ride_detail/{rideId}/{isDriver}") {
        fun createRoute(rideId: Int, isDriver: Boolean) = "ride_detail/$rideId/$isDriver"
    }
    object Profile : Screen("profile")
    object History : Screen("history/{isDriver}") {
        fun createRoute(isDriver: Boolean) = "history/$isDriver"
    }
    object AISchedule : Screen("ai_schedule")
    object AIRoutePreview : Screen("ai_route_preview/{scheduleId}") {
        fun createRoute(scheduleId: Int) = "ai_route_preview/$scheduleId"
    }
    object AIAlternatives : Screen("ai_alternatives/{scheduleId}") {
        fun createRoute(scheduleId: Int) = "ai_alternatives/$scheduleId"
    }
    object BatchOffer : Screen("batch_offer")
    object AIProfile : Screen("ai_profile")
    object AIRecommendations : Screen("ai_recommendations")
    object Earnings : Screen("earnings")
    object AIChat : Screen("ai_chat")
    object ForgotPassword : Screen("forgot_password")
    object OtpVerification : Screen("otp_verification/{email}/{devOtp}") {
        fun createRoute(email: String, devOtp: String?) = "otp_verification/$email/${devOtp ?: "none"}"
    }
    object ResetPassword : Screen("reset_password/{email}/{otp}") {
        fun createRoute(email: String, otp: String) = "reset_password/$email/$otp"
    }
}

@HiltViewModel
class MainViewModel @Inject constructor(
    val sessionManager: SessionManager
) : ViewModel()

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier,
    sessionManager: SessionManager? = null,
    startDestination: String = Screen.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = {
            fadeIn(animationSpec = tween(300)) + slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(300)
            )
        },
        exitTransition = {
            fadeOut(animationSpec = tween(300)) + slideOutHorizontally(
                targetOffsetX = { -it },
                animationSpec = tween(300)
            )
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(300)) + slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = tween(300)
            )
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(300)) + slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(300)
            )
        }
    ) {
        composable(Screen.Splash.route) {
            val viewModel: SplashViewModel = hiltViewModel()

            SplashScreen(
                onNavigateToAuth = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToHome = { userType ->
                    val destination = when (userType) {
                        "driver" -> Screen.DriverHome.route
                        else -> Screen.PassengerHome.route
                    }
                    navController.navigate(destination) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                viewModel = viewModel
            )
        }

        composable(Screen.Auth.route) {
            AuthScreen(
                onAuthSuccess = { userType, _ ->
                    val destination = when (userType) {
                        "driver" -> Screen.DriverHome.route
                        else -> Screen.PassengerHome.route
                    }
                    navController.navigate(destination) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Screen.ForgotPassword.route)
                }
            )
        }

        composable(Screen.PassengerHome.route) {
            PassengerHomeScreen(
                onNavigateToRideDetail = { rideId ->
                    navController.navigate(Screen.RideDetail.createRoute(rideId, false))
                },
                onNavigateToHistory = {
                    navController.navigate(Screen.History.createRoute(false))
                },
                onNavigateToAISchedule = {
                    navController.navigate(Screen.AISchedule.route)
                },
                onNavigateToAIChat = {
                    navController.navigate(Screen.AIChat.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onLogout = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.DriverHome.route) {
            DriverHomeScreen(
                onNavigateToRideDetail = { rideId ->
                    navController.navigate(Screen.RideDetail.createRoute(rideId, true))
                },
                onNavigateToHistory = {
                    navController.navigate(Screen.History.createRoute(true))
                },
                onNavigateToEarnings = {
                    navController.navigate(Screen.Earnings.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onNavigateToBatch = {
                    navController.navigate(Screen.BatchOffer.route)
                },
                onNavigateToAISchedule = {
                    navController.navigate(Screen.AISchedule.route)
                },
                onLogout = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.RideDetail.route,
            arguments = listOf(
                navArgument("rideId") { type = NavType.IntType },
                navArgument("isDriver") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val rideId = backStackEntry.arguments?.getInt("rideId") ?: 0
            val isDriver = backStackEntry.arguments?.getBoolean("isDriver") ?: false
            RideDetailScreen(
                rideId = rideId,
                isDriverView = isDriver,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.History.route,
            arguments = listOf(navArgument("isDriver") { type = NavType.BoolType })
        ) { backStackEntry ->
            val isDriver = backStackEntry.arguments?.getBoolean("isDriver") ?: false
            HistoryScreen(
                isDriver = isDriver,
                onBack = { navController.popBackStack() },
                onRideClick = { rideId ->
                    navController.navigate(Screen.RideDetail.createRoute(rideId, isDriver))
                }
            )
        }

        composable(Screen.AISchedule.route) {
            AIScheduleScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AIRoutePreview.route) {
            Box(
                modifier = Modifier.fillMaxSize().background(
                    Brush.verticalGradient(listOf(DarkBackground, DarkSurface))
                ),
                contentAlignment = Alignment.Center
            ) {
                Text("AI Route Preview", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
        }

        composable(Screen.AIAlternatives.route) {
            Box(
                modifier = Modifier.fillMaxSize().background(
                    Brush.verticalGradient(listOf(DarkBackground, DarkSurface))
                ),
                contentAlignment = Alignment.Center
            ) {
                Text("AI Alternatives", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
        }

        composable(Screen.BatchOffer.route) {
            BatchOfferScreen(
                onBack = { navController.popBackStack() },
                onBatchClick = { batchId ->
                    // Navigate to batch detail if needed
                }
            )
        }

        composable(Screen.AIProfile.route) {
            AIProfileScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AIRecommendations.route) {
            AIRecommendationsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AIChat.route) {
            AIChatScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Earnings.route) {
            EarningsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onBack = { navController.popBackStack() },
                onEmailSent = { email, devOtp ->
                    navController.navigate(Screen.OtpVerification.createRoute(email, devOtp))
                }
            )
        }

        composable(
            route = Screen.OtpVerification.route,
            arguments = listOf(
                navArgument("email") { type = NavType.StringType },
                navArgument("devOtp") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val email = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("email") ?: "", "UTF-8"
            )
            val devOtp = backStackEntry.arguments?.getString("devOtp")?.takeIf { it != "none" }
            OtpVerificationScreen(
                email = email,
                devOtp = devOtp,
                onBack = { navController.popBackStack() },
                onVerified = { verifiedEmail, otp ->
                    navController.navigate(Screen.ResetPassword.createRoute(verifiedEmail, otp)) {
                        popUpTo(Screen.ForgotPassword.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.ResetPassword.route,
            arguments = listOf(
                navArgument("email") { type = NavType.StringType },
                navArgument("otp") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val email = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("email") ?: "", "UTF-8"
            )
            val otp = backStackEntry.arguments?.getString("otp") ?: ""
            ResetPasswordScreen(
                email = email,
                otp = otp,
                onBack = { navController.popBackStack() },
                onSuccess = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
