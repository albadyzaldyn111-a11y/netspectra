package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Modern Premium SaaS Color Palette
val SaasPrimary = Color(0xFF1E40AF)          // Deep Blue
val SaasSecondary = Color(0xFF3B82F6)        // Bright Blue
val SaasTertiary = Color(0xFF06B6D4)         // Cyan
val SaasSurface = Color(0xFFFFFFFF)          // White
val SaasBackground = Color(0xFFF0F9FF)       // Light Blue Background
val SaasDarkBackground = Color(0xFF0F172A)   // Dark Slate
val SaasSuccess = Color(0xFF10B981)          // Emerald Green
val SaasWarning = Color(0xFFF59E0B)          // Amber
val SaasDanger = Color(0xFFDC2626)           // Red
val SaasGradientStart = Color(0xFF1E40AF)    // Gradient Start
val SaasGradientEnd = Color(0xFF06B6D4)      // Gradient End

// Keep backward compatibility mappings to avoid build breakages
val CyberPrimary = SaasPrimary
val CyberSecondary = SaasSecondary
val CyberBackground = SaasBackground
val CyberSurface = SaasSurface
val CyberSurfaceVariant = Color(0xFFE0F2FE)  // light blue 100
val CyberTextPrimary = Color(0xFF1E293B)     // slate 800 for dark text
val CyberTextSecondary = Color(0xFF64748B)   // slate 500
val CyberAccent = SaasSecondary
val CyberSuccess = SaasSuccess
val CyberWarning = SaasWarning
val CyberError = SaasDanger
