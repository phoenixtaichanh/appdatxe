package com.laptrinhdidong.DoAn3.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.LatLng
import com.laptrinhdidong.DoAn3.ui.components.TaxiMapView
import com.laptrinhdidong.DoAn3.ui.theme.*
import kotlinx.coroutines.delay

// =============================================================================
//  DEMO DATA
// =============================================================================
private val DEMO_DRIVER = DemoDriverInfo(
    name = "Nguyen Hoang Thien",
    carModel = "Honda Airblade 2025",
    carColor = "Den",
    licensePlate = "43P1-123.456",
    rating = 5.0
)

// Toa do K74/Tran Cao Van (Da Nang) -> Truong DH Viet Han (Da Nang)
private val DEMO_PICKUP_LAT = 16.0696
private val DEMO_PICKUP_LNG = 108.1518
private val DEMO_DEST_LAT = 16.0719
private val DEMO_DEST_LNG = 108.1680

private data class DemoDriverInfo(
    val name: String,
    val carModel: String,
    val carColor: String,
    val licensePlate: String,
    val rating: Double
)

// =============================================================================
//  DEMO STATE MACHINE
// =============================================================================
private enum class DemoPhase {
    LOADING,
    BOOKING,
    DRIVER_ACCEPTED,
    DRIVER_ARRIVED,
    TRIP_IN_PROGRESS,
    COMPLETED,
    RATING,
    FINISHED
}

private data class DemoScreenState(
    val phase: DemoPhase = DemoPhase.LOADING,
    val driverLat: Double = DEMO_PICKUP_LAT,
    val driverLng: Double = DEMO_PICKUP_LNG,
    val elapsedSeconds: Int = 0,
    val totalTripSeconds: Int = 10,
    val ratingSubmitted: Boolean = false,
    val showRating: Boolean = false
)

// =============================================================================
//  INTERPOLATION HELPERS
// =============================================================================
private fun lerp(start: Double, end: Double, fraction: Float): Double {
    return start + (end - start) * fraction
}

// =============================================================================
//  SCREEN
// =============================================================================
@Composable
fun DemoScreen(
    onBack: () -> Unit,
    onFinished: () -> Unit
) {
    var state by remember { mutableStateOf(DemoScreenState()) }

    // -------------------------------------------------------------------------
    //  STATE MACHINE - drives the entire demo automatically
    // -------------------------------------------------------------------------
    LaunchedEffect(state.phase) {
        when (state.phase) {
            DemoPhase.LOADING -> {
                delay(1500)
                state = state.copy(phase = DemoPhase.BOOKING)
            }
            DemoPhase.BOOKING -> {
                delay(2000)
                state = state.copy(phase = DemoPhase.DRIVER_ACCEPTED)
            }
            DemoPhase.DRIVER_ACCEPTED -> {
                delay(3000)
                state = state.copy(phase = DemoPhase.DRIVER_ARRIVED)
            }
            DemoPhase.DRIVER_ARRIVED -> {
                delay(2000)
                state = state.copy(phase = DemoPhase.TRIP_IN_PROGRESS)
            }
            DemoPhase.TRIP_IN_PROGRESS -> {
                repeat(10) { sec ->
                    delay(1000)
                    val fraction = (sec + 1) / 10f
                    state = state.copy(
                        elapsedSeconds = sec + 1,
                        driverLat = lerp(DEMO_PICKUP_LAT, DEMO_DEST_LAT, fraction),
                        driverLng = lerp(DEMO_PICKUP_LNG, DEMO_DEST_LNG, fraction)
                    )
                }
                state = state.copy(phase = DemoPhase.COMPLETED)
            }
            DemoPhase.COMPLETED -> {
                delay(1500)
                state = state.copy(phase = DemoPhase.RATING, showRating = true)
            }
            DemoPhase.RATING -> {
                delay(1500)
                state = state.copy(ratingSubmitted = true)
                delay(2000)
                state = state.copy(phase = DemoPhase.FINISHED)
            }
            DemoPhase.FINISHED -> {
                delay(500)
                onFinished()
            }
        }
    }

    // -------------------------------------------------------------------------
    //  UI
    // -------------------------------------------------------------------------
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ---------- TOP BAR ----------
            DemoTopBar(
                phase = state.phase,
                onBack = onBack
            )

            // ---------- MAP AREA (REAL GOOGLE MAPS) ----------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // Real Google Maps - TaxiMapView component
                TaxiMapView(
                    pickupLat = if (state.phase == DemoPhase.BOOKING || state.phase == DemoPhase.LOADING) DEMO_PICKUP_LAT else null,
                    pickupLng = if (state.phase == DemoPhase.BOOKING || state.phase == DemoPhase.LOADING) DEMO_PICKUP_LNG else null,
                    dropoffLat = if (state.phase != DemoPhase.LOADING) DEMO_DEST_LAT else null,
                    dropoffLng = if (state.phase != DemoPhase.LOADING) DEMO_DEST_LNG else null,
                    driverLat = if (state.phase != DemoPhase.LOADING && state.phase != DemoPhase.BOOKING) state.driverLat else null,
                    driverLng = if (state.phase != DemoPhase.LOADING && state.phase != DemoPhase.BOOKING) state.driverLng else null,
                    routePoints = if (state.phase == DemoPhase.TRIP_IN_PROGRESS || state.phase == DemoPhase.COMPLETED || state.phase == DemoPhase.RATING) {
                        listOf(
                            Pair(DEMO_PICKUP_LAT, DEMO_PICKUP_LNG),
                            Pair(state.driverLat, state.driverLng),
                            Pair(DEMO_DEST_LAT, DEMO_DEST_LNG)
                        )
                    } else null,
                    onMapClick = { /* Demo is auto-driven, ignore clicks */ },
                    modifier = Modifier.fillMaxSize(),
                    zoomLevel = 16f
                )

                // Pickup & Dest labels overlay
                if (state.phase == DemoPhase.BOOKING || state.phase == DemoPhase.LOADING) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp)
                    ) {
                        DemoLocationLabel(
                            icon = Icons.Default.MyLocation,
                            color = AccentGreen,
                            title = "Diem don",
                            address = "K74/Tran Cao Van",
                            top = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        DemoLocationLabel(
                            icon = Icons.Default.LocationOn,
                            color = AccentRed,
                            title = "Diem den",
                            address = "Truong DH Viet Han",
                            top = false
                        )
                    }
                }

                // Progress bar for trip
                if (state.phase == DemoPhase.TRIP_IN_PROGRESS) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 16.dp)
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = DarkCard.copy(alpha = 0.95f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        "Dang di chuyen...",
                                        color = TextPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "Con ${state.totalTripSeconds - state.elapsedSeconds}s",
                                        color = AccentGreen,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(AccentGreen.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.TwoWheeler,
                                        contentDescription = null,
                                        tint = AccentGreen,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            LinearProgressIndicator(
                                progress = { state.elapsedSeconds / state.totalTripSeconds.toFloat() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = AccentGreen,
                                trackColor = DarkSurface
                            )
                        }
                    }
                }
            }

            // ---------- BOTTOM INFO CARD ----------
            AnimatedVisibility(
                visible = state.phase != DemoPhase.LOADING,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                DemoBottomCard(
                    state = state,
                    driver = DEMO_DRIVER,
                    onRate = { state = state.copy(ratingSubmitted = true) }
                )
            }
        }
    }
}

// =============================================================================
//  TOP BAR
// =============================================================================
@Composable
private fun DemoTopBar(
    phase: DemoPhase,
    onBack: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = DarkBackground
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp)
                .statusBarsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Quay lai",
                    tint = TextPrimary
                )
            }
            Text(
                text = "Demo",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            // Phase indicator
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        when (phase) {
                            DemoPhase.LOADING -> AccentYellow.copy(alpha = 0.2f)
                            DemoPhase.BOOKING -> AccentBlue.copy(alpha = 0.2f)
                            DemoPhase.DRIVER_ACCEPTED,
                            DemoPhase.DRIVER_ARRIVED -> AccentGreen.copy(alpha = 0.2f)
                            DemoPhase.TRIP_IN_PROGRESS -> PrimaryPurple.copy(alpha = 0.2f)
                            DemoPhase.COMPLETED -> AccentGreen.copy(alpha = 0.2f)
                            DemoPhase.RATING -> AccentYellow.copy(alpha = 0.2f)
                            DemoPhase.FINISHED -> AccentGreen.copy(alpha = 0.2f)
                        }
                    )
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = when (phase) {
                        DemoPhase.LOADING -> "Khoi tao..."
                        DemoPhase.BOOKING -> "Dat xe"
                        DemoPhase.DRIVER_ACCEPTED -> "Tai xe nhan cuoc"
                        DemoPhase.DRIVER_ARRIVED -> "Tai xe den noi"
                        DemoPhase.TRIP_IN_PROGRESS -> "Dang di chuyen"
                        DemoPhase.COMPLETED -> "Hoan thanh"
                        DemoPhase.RATING -> "Danh gia"
                        DemoPhase.FINISHED -> "Xong"
                    },
                    color = when (phase) {
                        DemoPhase.LOADING -> AccentYellow
                        DemoPhase.BOOKING -> AccentBlue
                        DemoPhase.DRIVER_ACCEPTED,
                        DemoPhase.DRIVER_ARRIVED -> AccentGreen
                        DemoPhase.TRIP_IN_PROGRESS -> PrimaryPurple
                        DemoPhase.COMPLETED -> AccentGreen
                        DemoPhase.RATING -> AccentYellow
                        DemoPhase.FINISHED -> AccentGreen
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// =============================================================================
//  LOCATION LABEL CHIP
// =============================================================================
@Composable
private fun DemoLocationLabel(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    title: String,
    address: String,
    top: Boolean
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard.copy(alpha = 0.95f)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(title, color = TextSecondary, fontSize = 10.sp)
                Text(
                    address,
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// =============================================================================
//  BOTTOM INFO CARD
// =============================================================================
@Composable
private fun DemoBottomCard(
    state: DemoScreenState,
    driver: DemoDriverInfo,
    onRate: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // ---- Phase-specific content ----

            // PHASE: BOOKING
            AnimatedVisibility(visible = state.phase == DemoPhase.BOOKING) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(40.dp),
                        color = AccentYellow,
                        strokeWidth = 3.dp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Dang tim tai xe gan ban...",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "K74/Tran Cao Van -> Truong DH Viet Han",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("~2.5 km", color = TextPrimary, fontWeight = FontWeight.Bold)
                            Text("Khoang cach", color = TextSecondary, fontSize = 11.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("~10s", color = TextPrimary, fontWeight = FontWeight.Bold)
                            Text("Thoi gian", color = TextSecondary, fontSize = 11.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "15.000d",
                                color = PrimaryPurple,
                                fontWeight = FontWeight.Bold
                            )
                            Text("Gia cuoc", color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                }
            }

            // PHASE: DRIVER ACCEPTED
            AnimatedVisibility(visible = state.phase == DemoPhase.DRIVER_ACCEPTED) {
                DriverInfoContent(
                    driver = driver,
                    statusText = "Da nhan cuoc",
                    statusColor = AccentGreen,
                    extraContent = {
                        Text(
                            "Tai xe dang di chuyen den diem don",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                )
            }

            // PHASE: DRIVER ARRIVED
            AnimatedVisibility(visible = state.phase == DemoPhase.DRIVER_ARRIVED) {
                DriverInfoContent(
                    driver = driver,
                    statusText = "Tai xe da den",
                    statusColor = AccentYellow,
                    extraContent = {
                        Text(
                            "Hay ra gap tai xe tai K74/Tran Cao Van",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                )
            }

            // PHASE: TRIP IN PROGRESS
            AnimatedVisibility(visible = state.phase == DemoPhase.TRIP_IN_PROGRESS) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.TwoWheeler,
                                contentDescription = null,
                                tint = PrimaryPurple,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Dang di chuyen",
                                    color = TextPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "-> Truong DH Viet Han",
                                    color = TextSecondary,
                                    fontSize = 13.sp
                                )
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "${state.elapsedSeconds}/${state.totalTripSeconds}s",
                                color = AccentGreen,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text("da di", color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                }
            }

            // PHASE: COMPLETED
            AnimatedVisibility(visible = state.phase == DemoPhase.COMPLETED) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val scale by animateFloatAsState(
                        targetValue = 1f,
                        animationSpec = spring(stiffness = Spring.StiffnessLow),
                        label = "checkmark"
                    )
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .scale(scale)
                            .clip(CircleShape)
                            .background(AccentGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Da den noi!",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Truong DH Viet Han",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("2.5 km", color = TextPrimary, fontWeight = FontWeight.Bold)
                            Text("Quang duong", color = TextSecondary, fontSize = 11.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("10s", color = TextPrimary, fontWeight = FontWeight.Bold)
                            Text("Thoi gian", color = TextSecondary, fontSize = 11.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("15.000d", color = PrimaryPurple, fontWeight = FontWeight.Bold)
                            Text("Tien mat", color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                }
            }

            // PHASE: RATING
            AnimatedVisibility(visible = state.phase == DemoPhase.RATING) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Cam on ban da dong hanh!",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Driver mini card
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(GradientPrimary)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    driver.name.first().toString(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    driver.name,
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "${driver.carModel} - ${driver.carColor}",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Danh gia tai xe",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Auto 5-star with animation
                    Row {
                        repeat(5) { index ->
                            val animatedAlpha by animateFloatAsState(
                                targetValue = 1f,
                                animationSpec = tween(delayMillis = index * 200),
                                label = "star$index"
                            )
                            Icon(
                                Icons.Filled.Star,
                                contentDescription = null,
                                tint = AccentYellow.copy(alpha = animatedAlpha),
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Payment method
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurface)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Money,
                                contentDescription = null,
                                tint = AccentGreen,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                "Tien mat",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Text(
                            "15.000d",
                            color = PrimaryPurple,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (state.ratingSubmitted) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(AccentGreen.copy(alpha = 0.2f))
                                .padding(12.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = AccentGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Da gui danh gia 5 sao",
                                color = AccentGreen,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        Text(
                            "Dang gui danh gia...",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // PHASE: FINISHED
            AnimatedVisibility(visible = state.phase == DemoPhase.FINISHED) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = AccentGreen,
                        strokeWidth = 3.dp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Demo hoan tat! Quay ve trang chu...",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

// =============================================================================
//  DRIVER INFO CONTENT (shared by DRIVER_ACCEPTED & DRIVER_ARRIVED)
// =============================================================================
@Composable
private fun DriverInfoContent(
    driver: DemoDriverInfo,
    statusText: String,
    statusColor: Color,
    extraContent: @Composable ColumnScope.() -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            statusText,
            color = statusColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Driver card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(GradientPrimary)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        driver.name.first().toString(),
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        driver.name,
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        repeat(5) {
                            Icon(
                                Icons.Filled.Star,
                                contentDescription = null,
                                tint = AccentYellow,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            String.format("%.1f", driver.rating),
                            color = AccentYellow,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        "${driver.carModel} - ${driver.carColor}",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(PrimaryPurple.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            driver.licensePlate,
                            color = PrimaryPurple,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Icon(
                        Icons.Default.TwoWheeler,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        extraContent()
    }
}
