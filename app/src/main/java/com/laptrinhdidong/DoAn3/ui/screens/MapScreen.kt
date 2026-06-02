package com.laptrinhdidong.DoAn3.ui.screens

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.laptrinhdidong.DoAn3.ui.components.AppTopBar
import com.laptrinhdidong.DoAn3.ui.theme.*

data class MapLocation(
    val name: String,
    val address: String,
    val lat: Double,
    val lng: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MapScreen(
    title: String = "Bản đồ",
    locations: List<MapLocation>,
    onBack: () -> Unit
) {
    val mapHtml = remember(locations) {
        if (locations.isEmpty()) {
            """
            <!DOCTYPE html>
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    body { height: 100vh; display: flex; align-items: center; justify-content: center; background: #1a1a2e; color: #aaa; font-family: sans-serif; }
                </style>
            </head>
            <body><p>Không có địa điểm để hiển thị</p></body>
            </html>
            """.trimIndent()
        } else {
            val markers = locations.mapIndexed { index, loc ->
                val icon = when {
                    index == 0 -> "color: '#00C853'"       // pickup - green
                    index == locations.lastIndex -> "color: '#FF5252'" // dropoff - red
                    else -> "color: '#667EEA'"             // stopover - purple
                }
                val label = if (index == 0) "A" else if (index == locations.lastIndex) "B" else "${index + 1}"
                """
                {
                    position: { lat: ${loc.lat}, lng: ${loc.lng} },
                    title: "${loc.name.replace("\"", "\\\"")}",
                    address: "${loc.address.replace("\"", "\\\"")}",
                    label: "$label",
                    color: $icon
                }
                """.trimIndent()
            }.joinToString(",\n                ")

            """
            <!DOCTYPE html>
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    body { height: 100vh; background: #1a1a2e; }
                    #map { width: 100%; height: calc(100vh - 50px); }
                    #info-bar {
                        height: 50px;
                        background: #1a1a2e;
                        display: flex;
                        align-items: center;
                        padding: 0 16px;
                        overflow: hidden;
                    }
                    .route-dot {
                        width: 12px; height: 12px; border-radius: 50%;
                        margin-right: 6px; flex-shrink: 0;
                    }
                    .route-label {
                        font-size: 11px; color: #888; margin-right: 16px;
                        display: flex; align-items: center; flex-shrink: 0;
                    }
                    .route-label span { color: #fff; font-weight: bold; margin-right: 4px; }
                    .route-name {
                        font-size: 11px; color: #ccc; overflow: hidden;
                        text-overflow: ellipsis; white-space: nowrap;
                    }
                    .route-connector {
                        width: 40px; height: 1px;
                        background: linear-gradient(to right, #667EEA, #764BA2);
                        margin: 0 8px; flex-shrink: 0;
                    }
                    .route-connector-last { display: none; }
                </style>
            </head>
            <body>
                <div id="map"></div>
                <div id="info-bar">
                    ${locations.mapIndexed { index, loc ->
                        val dotColor = when {
                            index == 0 -> "#00C853"
                            index == locations.lastIndex -> "#FF5252"
                            else -> "#667EEA"
                        }
                        """
                        <div class="route-label">
                            <span>${
                            if (index == 0) "A" else if (index == locations.lastIndex) "B" else "${index + 1}"
                        }</span>
                            <span class="route-name">${loc.name.replace("\"", "\\\"")}</span>
                        </div>
                        ${if (index < locations.lastIndex) "<div class=\"route-connector\"></div>" else ""}
                        """.trimIndent()
                    }.joinToString("")}
                </div>
                <script src="https://maps.googleapis.com/maps/api/js?key=AIzaSyBxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx&callback=initMap" async defer></script>
                <script>
                    function initMap() {
                        const locations = [
                            $markers
                        ];

                        const map = new google.maps.Map(document.getElementById('map'), {
                            zoom: 13,
                            center: locations[0] ? locations[0].position : { lat: 16.0544, lng: 108.2022 },
                            styles: [
                                { elementType: "geometry", stylers: [{ color: "#1d1d2e" }] },
                                { elementType: "labels.text.stroke", stylers: [{ color: "#1d1d2e" }] },
                                { elementType: "labels.text.fill", stylers: [{ color: "#746855" }] },
                                { featureType: "road", elementType: "geometry", stylers: [{ color: "#2a2a3e" }] },
                                { featureType: "road", elementType: "geometry.stroke", stylers: [{ color: "#1a1a2e" }] },
                                { featureType: "road.highway", elementType: "geometry", stylers: [{ color: "#3a3a5e" }] },
                                { featureType: "water", elementType: "geometry", stylers: [{ color: "#171723" }] },
                                { featureType: "poi", elementType: "labels", stylers: [{ visibility: "off" }] },
                                { featureType: "transit", stylers: [{ visibility: "off" }] }
                            ],
                            disableDefaultUI: true,
                            zoomControl: true
                        });

                        const labels = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';

                        const bounds = new google.maps.LatLngBounds();

                        locations.forEach(function(loc, index) {
                            var marker = new google.maps.Marker({
                                position: loc.position,
                                map: map,
                                label: {
                                    text: labels[index],
                                    color: '#fff',
                                    fontSize: '12px',
                                    fontWeight: 'bold'
                                },
                                icon: {
                                    path: google.maps.SymbolPath.CIRCLE,
                                    scale: 14,
                                    fillColor: loc.color,
                                    fillOpacity: 1,
                                    strokeColor: '#fff',
                                    strokeWeight: 2
                                }
                            });

                            var infoWindow = new google.maps.InfoWindow({
                                content: '<div style="color:#000;padding:4px;"><b>' + loc.title + '</b><br><small>' + loc.address + '</small></div>'
                            });

                            marker.addListener('click', function() {
                                infoWindow.open(map, marker);
                            });

                            bounds.extend(loc.position);
                        });

                        if (locations.length > 1) {
                            map.fitBounds(bounds, { top: 40, right: 40, bottom: 60, left: 40 });
                        }
                    }
                </script>
            </body>
            </html>
            """.trimIndent()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Custom top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .padding(top = 32.dp, bottom = 8.dp, start = 8.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Text(
                text = title,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            if (locations.size > 0) {
                Text(
                    text = "${locations.size} điểm",
                    color = PrimaryPurple,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(end = 12.dp)
                )
            }
        }

        // WebView
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            loadWithOverviewMode = true
                            useWideViewPort = true
                            builtInZoomControls = true
                            displayZoomControls = false
                            setSupportZoom(true)
                            mediaPlaybackRequiresUserGesture = false
                        }
                        settings.userAgentString = "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36"
                        webViewClient = WebViewClient()
                        webChromeClient = WebChromeClient()
                        setBackgroundColor(0xFF1D1D2E.toInt())
                        loadDataWithBaseURL(
                            "https://maps.googleapis.com",
                            mapHtml,
                            "text/html",
                            "UTF-8",
                            null
                        )
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Loading indicator
            if (locations.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = PrimaryPurple)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Đang tải bản đồ...", color = TextSecondary)
                    }
                }
            }

            // Map style info card at bottom
            if (locations.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard.copy(alpha = 0.95f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        locations.take(5).forEachIndexed { index, loc ->
                            val dotColor = when {
                                index == 0 -> AccentGreen
                                index == locations.lastIndex -> AccentRed
                                else -> PrimaryPurple
                            }
                            Surface(
                                modifier = Modifier.size(10.dp),
                                shape = RoundedCornerShape(50),
                                color = dotColor
                            ) {}
                            if (index < minOf(4, locations.size - 1)) {
                                Box(
                                    modifier = Modifier
                                        .width(20.dp)
                                        .height(1.dp)
                                        .background(
                                            color = PrimaryPurple.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(1.dp)
                                        )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${locations.size} điểm dừng",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
