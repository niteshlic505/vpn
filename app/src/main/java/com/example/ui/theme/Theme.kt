package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CyberCyan,
    onPrimary = DeepNavy,
    primaryContainer = Color(0xFF003747),
    onPrimaryContainer = Color(0xFF97F0FF),
    secondary = ElectricGreen,
    onSecondary = Color(0xFF003923),
    secondaryContainer = Color(0xFF005234),
    onSecondaryContainer = Color(0xFF6CF8B8),
    tertiary = CyberViolet,
    onTertiary = Color(0xFF2E004E),
    tertiaryContainer = Color(0xFF491475),
    onTertiaryContainer = Color(0xFFEADBFF),
    background = DeepNavy,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = DarkBorder,
    outlineVariant = Color(0xFF1E293B),
    error = CrimsonAlert,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF00687A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF97F0FF),
    onPrimaryContainer = Color(0xFF001F26),
    secondary = Color(0xFF006C46),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF8CF5BF),
    onSecondaryContainer = Color(0xFF002113),
    tertiary = Color(0xFF6B459B),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFEADBFF),
    onTertiaryContainer = Color(0xFF25005A),
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder,
    error = CrimsonAlert,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to sleek cyber dark mode for VPN
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
