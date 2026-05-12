package com.laptrinhdidong.DoAn3.ui.theme

import androidx.compose.ui.graphics.Color

// ========== PRIMARY COLORS ==========
val PrimaryPurple = Color(0xFF667eea)
val PrimaryPurpleDark = Color(0xFF764ba2)
val PrimaryPink = Color(0xFFf093fb)

// ========== DARK THEME ==========
val DarkBackground = Color(0xFF1a1a2e)
val DarkSurface = Color(0xFF16213e)
val DarkSurfaceVariant = Color(0xFF0f3460)
val DarkCard = Color(0xFF1f2b47)

// ========== ACCENT COLORS ==========
val AccentGreen = Color(0xFF00C853)
val AccentGreenLight = Color(0xFF69F0AE)
val AccentOrange = Color(0xFFFF9800)
val AccentRed = Color(0xFFFF5252)
val AccentBlue = Color(0xFF2196F3)
val AccentYellow = Color(0xFFFFEB3B)

// ========== TEXT COLORS ==========
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xB3FFFFFF)
val TextHint = Color(0x80FFFFFF)
val TextOnPrimary = Color(0xFFFFFFFF)

// ========== GRADIENTS ==========
val GradientPrimary = listOf(PrimaryPurple, PrimaryPurpleDark)
val GradientSecondary = listOf(PrimaryPurple, PrimaryPink)
val GradientSuccess = listOf(AccentGreen, AccentGreenLight)
val GradientDark = listOf(DarkBackground, DarkSurface)

// ========== STATUS COLORS ==========
val StatusOnline = AccentGreen
val StatusOffline = Color(0xFF9E9E9E)
val StatusBusy = AccentOrange
val StatusError = AccentRed

// ========== RIDE STATUS ==========
val StatusPending = Color(0xFFFFB300)
val StatusAccepted = Color(0xFF42A5F5)
val StatusArrived = Color(0xFF7E57C2)
val StatusInProgress = AccentGreen
val StatusCompleted = AccentGreen
val StatusCancelled = AccentRed
