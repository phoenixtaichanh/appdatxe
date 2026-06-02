package com.laptrinhdidong.DoAn3.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.laptrinhdidong.DoAn3.ui.theme.AccentGreen
import com.laptrinhdidong.DoAn3.ui.theme.AccentRed
import com.laptrinhdidong.DoAn3.ui.theme.AccentYellow
import com.laptrinhdidong.DoAn3.ui.theme.PrimaryPurple

private val MAP_DARK_1 = Color(0xFF0D1117)
private val MAP_DARK_2 = Color(0xFF161B22)
private val MAP_DARK_3 = Color(0xFF21262D)
private val MAP_ROAD = Color(0xFF30363D)
private val MAP_ROAD_MAIN = Color(0xFF484F58)
private val MAP_WATER = Color(0xFF0D1B2A)
private val MAP_GREEN = Color(0xFF1B4332)
private val MAP_STREET_TEXT = Color(0xFF8B949E)

@Composable
fun SimulatedMapView(
    pickupLat: Double?,
    pickupLng: Double?,
    dropoffLat: Double?,
    dropoffLng: Double?,
    driverLat: Double?,
    driverLng: Double?,
    routePoints: List<Pair<Double, Double>>?,
    onMapClick: (SimulatedLatLng) -> Unit,
    modifier: Modifier = Modifier,
    zoomLevel: Float = 14f
) {
    val allLats = listOfNotNull(pickupLat, dropoffLat, driverLat)
    val allLngs = listOfNotNull(pickupLng, dropoffLng, driverLng)

    val minLat = if (allLats.isNotEmpty()) allLats.min() else 10.76
    val maxLat = if (allLats.isNotEmpty()) allLats.max() else 10.78
    val minLng = if (allLngs.isNotEmpty()) allLngs.min() else 106.65
    val maxLng = if (allLngs.isNotEmpty()) allLngs.max() else 106.72

    val padding = 0.08f

    fun latLngToXY(lat: Double, lng: Double): Offset {
        val xF = ((lng - minLng) / (maxLng - minLng) * (1f - 2 * padding) + padding).toFloat()
        val yF = ((maxLat - lat) / (maxLat - minLat) * (1f - 2 * padding) + padding).toFloat()
        return Offset(xF, yF)
    }

    val pickupXY = if (pickupLat != null && pickupLng != null) latLngToXY(pickupLat, pickupLng) else null
    val dropoffXY = if (dropoffLat != null && dropoffLng != null) latLngToXY(dropoffLat, dropoffLng) else null
    val driverXY = if (driverLat != null && driverLng != null) latLngToXY(driverLat, driverLng) else null

    val infiniteTransition = rememberInfiniteTransition(label = "map")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse"
    )
    val driverPulse by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ), label = "driverPulse"
    )
    val roadShimmer by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "roadShimmer"
    )

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(MAP_DARK_1, MAP_DARK_2, MAP_DARK_3)
                    )
                )
        ) {
            val w = size.width
            val h = size.height

            // ---- WATER & GREEN AREAS ----
            drawCircle(
                color = MAP_WATER.copy(alpha = 0.6f),
                radius = w * 0.15f,
                center = Offset(w * 0.8f, h * 0.7f)
            )
            drawCircle(
                color = MAP_GREEN.copy(alpha = 0.4f),
                radius = w * 0.08f,
                center = Offset(w * 0.2f, h * 0.3f)
            )
            drawCircle(
                color = MAP_GREEN.copy(alpha = 0.3f),
                radius = w * 0.06f,
                center = Offset(w * 0.7f, h * 0.2f)
            )

            // ---- ROAD GRID ----
            val verticalRoads = listOf(0.08f, 0.2f, 0.35f, 0.5f, 0.65f, 0.8f, 0.92f)
            val horizontalRoads = listOf(0.08f, 0.22f, 0.38f, 0.5f, 0.62f, 0.78f, 0.92f)

            verticalRoads.forEachIndexed { idx, xf ->
                val isMain = (idx == 2 || idx == 4)
                drawLine(
                    color = if (isMain) MAP_ROAD_MAIN else MAP_ROAD,
                    start = Offset(w * xf, 0f),
                    end = Offset(w * xf, h),
                    strokeWidth = if (isMain) 3f else 1.5f
                )
            }
            horizontalRoads.forEachIndexed { idx, yf ->
                val isMain = (idx == 3 || idx == 5)
                drawLine(
                    color = if (isMain) MAP_ROAD_MAIN else MAP_ROAD,
                    start = Offset(0f, h * yf),
                    end = Offset(w, h * yf),
                    strokeWidth = if (isMain) 3f else 1.5f
                )
            }

            // ---- TRAFFIC DOTS ----
            val trafficDots = listOf(
                Pair(w * 0.35f, h * 0.5f),
                Pair(w * 0.65f, h * 0.5f),
                Pair(w * 0.5f, h * 0.38f),
                Pair(w * 0.5f, h * 0.62f),
                Pair(w * 0.35f, h * 0.78f),
                Pair(w * 0.65f, h * 0.22f)
            )
            trafficDots.forEachIndexed { idx, dot ->
                val phase = (roadShimmer + idx * 0.3f) % 1f
                val alpha = (kotlin.math.sin(phase * Math.PI * 2) * 0.3 + 0.3).toFloat()
                drawCircle(
                    color = MAP_STREET_TEXT.copy(alpha = alpha.coerceIn(0f, 0.6f)),
                    radius = 2.5f,
                    center = Offset(
                        dot.first + (phase - 0.5f) * w * 0.08f,
                        dot.second + (phase - 0.5f) * h * 0.04f
                    )
                )
            }

            // ---- ROUTE BEZIER CURVE ----
            if (pickupXY != null && dropoffXY != null) {
                val cp1 = Offset(
                    pickupXY.x * 0.7f + dropoffXY.x * 0.3f,
                    (pickupXY.y * 0.3f + dropoffXY.y * 0.7f)
                )
                val cp2 = Offset(
                    pickupXY.x * 0.3f + dropoffXY.x * 0.7f,
                    (pickupXY.y * 0.7f + dropoffXY.y * 0.3f)
                )
                // Glow layer
                drawBezierCurve(pickupXY, cp1, cp2, dropoffXY, PrimaryPurple.copy(alpha = 0.2f), 18f)
                // Main dashed route
                drawBezierCurve(
                    pickupXY, cp1, cp2, dropoffXY,
                    PrimaryPurple.copy(alpha = 0.8f), 4f,
                    PathEffect.dashPathEffect(floatArrayOf(20f, 10f), phase = roadShimmer * 30f)
                )
            }

            // ---- PICKUP MARKER (A) ----
            if (pickupXY != null) {
                val px = pickupXY.x * w
                val py = pickupXY.y * h
                val pulseVal = pulse

                // Outer pulse ring
                drawCircle(
                    color = AccentGreen.copy(alpha = 0.12f + pulseVal * 0.08f),
                    radius = 40f + pulseVal * 10f,
                    center = Offset(px, py)
                )
                // Ring
                drawCircle(
                    color = AccentGreen.copy(alpha = 0.35f),
                    radius = 22f,
                    center = Offset(px, py)
                )
                // Main circle
                drawCircle(color = AccentGreen, radius = 14f, center = Offset(px, py))
                // White center
                drawCircle(color = Color.White, radius = 7f, center = Offset(px, py))
                // "A" label
                drawContext.canvas.nativeCanvas.drawText(
                    "A", px - 5f, py + 6f,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = 18f
                        isFakeBoldText = true
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                )
            }

            // ---- DROPOFF MARKER (B) ----
            if (dropoffXY != null) {
                val dx = dropoffXY.x * w
                val dy = dropoffXY.y * h
                val pulseVal = pulse

                drawCircle(
                    color = AccentRed.copy(alpha = 0.12f + pulseVal * 0.08f),
                    radius = 40f + pulseVal * 10f,
                    center = Offset(dx, dy)
                )
                drawCircle(
                    color = AccentRed.copy(alpha = 0.35f),
                    radius = 22f,
                    center = Offset(dx, dy)
                )
                drawCircle(color = AccentRed, radius = 14f, center = Offset(dx, dy))
                drawCircle(color = Color.White, radius = 7f, center = Offset(dx, dy))
                drawContext.canvas.nativeCanvas.drawText(
                    "B", dx - 5f, dy + 6f,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = 18f
                        isFakeBoldText = true
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                )
            }

            // ---- DRIVER MARKER ----
            if (driverXY != null) {
                val dvrx = driverXY.x * w
                val dvry = driverXY.y * h
                val dPulse = driverPulse

                drawCircle(
                    color = PrimaryPurple.copy(alpha = (0.2f - dPulse * 0.15f).coerceAtLeast(0.05f)),
                    radius = 36f + dPulse * 18f,
                    center = Offset(dvrx, dvry)
                )
                drawCircle(
                    color = Color.Black.copy(alpha = 0.3f),
                    radius = 18f,
                    center = Offset(dvrx + 2f, dvry + 3f)
                )
                drawCircle(color = PrimaryPurple, radius = 16f, center = Offset(dvrx, dvry))
                drawCircle(color = Color.White, radius = 8f, center = Offset(dvrx, dvry))
                drawContext.canvas.nativeCanvas.drawText(
                    "\uD83D\uDE97", dvrx - 10f, dvry + 6f,
                    android.graphics.Paint().apply { textSize = 22f }
                )
            }

            // ---- MINI BUILDINGS ----
            val buildings = listOf(
                Triple(w * 0.12f, h * 0.12f, 14f),
                Triple(w * 0.85f, h * 0.15f, 18f),
                Triple(w * 0.88f, h * 0.55f, 12f),
                Triple(w * 0.15f, h * 0.85f, 16f),
                Triple(w * 0.25f, h * 0.55f, 10f),
                Triple(w * 0.75f, h * 0.88f, 20f)
            )
            buildings.forEach { (bx, by, br) ->
                drawCircle(
                    color = MAP_DARK_3.copy(alpha = 0.6f),
                    radius = br,
                    center = Offset(bx, by)
                )
                val winAlpha = (0.3f + pulse * 0.3f).coerceIn(0f, 0.6f)
                listOf(
                    Offset(bx - br * 0.3f, by - br * 0.3f),
                    Offset(bx + br * 0.3f, by - br * 0.3f),
                    Offset(bx, by)
                ).forEach {woff ->
                    drawCircle(
                        color = AccentYellow.copy(alpha = winAlpha),
                        radius = br * 0.15f,
                        center = Offset(bx + woff.x, by + woff.y)
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawBezierCurve(
    p0: Offset,
    cp1: Offset,
    cp2: Offset,
    p3: Offset,
    color: Color,
    strokeWidth: Float,
    pathEffect: PathEffect? = null
) {
    val steps = 50
    var prevPt = p0
    for (i in 1..steps) {
        val t = i.toFloat() / steps
        val mt = 1f - t
        val mt2 = mt * mt
        val mt3 = mt2 * mt
        val t2 = t * t
        val t3 = t2 * t

        val x = mt3 * p0.x + 3 * mt2 * t * cp1.x + 3 * mt * t2 * cp2.x + t3 * p3.x
        val y = mt3 * p0.y + 3 * mt2 * t * cp1.y + 3 * mt * t2 * cp2.y + t3 * p3.y

        drawLine(
            color = color,
            start = Offset(prevPt.x * size.width, prevPt.y * size.height),
            end = Offset(x * size.width, y * size.height),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
            pathEffect = pathEffect
        )
        prevPt = Offset(x, y)
    }
}

data class SimulatedLatLng(val latitude: Double, val longitude: Double)
