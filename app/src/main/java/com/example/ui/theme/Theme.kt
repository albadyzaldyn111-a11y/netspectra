package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SaasLightColorScheme = lightColorScheme(
    primary = SaasPrimary,
    secondary = SaasSecondary,
    background = SaasBackground,
    surface = SaasSurface,
    surfaceVariant = Color(0xFFF1F5F9), // Slate 100
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF0F172A), // Slate 900
    onSurface = Color(0xFF0F172A),
    onSurfaceVariant = Color(0xFF475569), // Slate 600
    error = SaasDanger,
    primaryContainer = Color(0xFFDBEAFE), // Blue 100
    secondaryContainer = Color(0xFFEFF6FF) // Blue 50
)

private val SaasDarkColorScheme = darkColorScheme(
    primary = SaasPrimary,
    secondary = SaasSecondary,
    background = SaasDarkBackground,
    surface = Color(0xFF1E293B), // Slate 800
    surfaceVariant = Color(0xFF334155), // Slate 700
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFFF8FAFC), // Slate 50
    onSurface = Color(0xFFF8FAFC),
    onSurfaceVariant = Color(0xFF94A3B8), // Slate 400
    error = SaasDanger,
    primaryContainer = Color(0xFF1E3A8A), // Blue 900
    secondaryContainer = Color(0xFF172554) // Blue 950
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // We will favor a light SaaS theme unless explicitly in darkTheme, or force Light Theme for Stripe-style look
    val colorScheme = if (darkTheme) SaasDarkColorScheme else SaasLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
