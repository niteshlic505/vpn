package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Connect : Screen("connect", "Shield", Icons.Default.PowerSettingsNew)
    object Servers : Screen("servers", "Servers", Icons.Default.Public)
    object LeakCheck : Screen("leak_check", "Audit", Icons.Default.Security)
    object SentinelAi : Screen("sentinel_ai", "AI Sentinel", Icons.Default.AutoAwesome)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}
