package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VpnServer
import com.example.ui.theme.CyberBlue
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricGreen
import com.example.ui.theme.GlowingAmber
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.hypot

@Composable
fun InteractiveWorldMap(
    servers: List<VpnServer>,
    selectedServer: VpnServer?,
    onServerSelected: (VpnServer) -> Unit,
    onConnectClicked: (VpnServer) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mapPulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 6f,
        targetValue = 24f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarPulse"
    )

    var activePopupServer by remember(selectedServer) { mutableStateOf(selectedServer ?: servers.firstOrNull()) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(DarkSurfaceVariant.copy(alpha = 0.6f))
            .border(1.dp, DarkBorder, RoundedCornerShape(20.dp))
            .testTag("interactive_world_map")
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(servers) {
                    detectTapGestures { tapOffset ->
                        val w = size.width
                        val h = size.height

                        // Find closest server node within 32dp touch radius
                        var closest: VpnServer? = null
                        var minDistance = Float.MAX_VALUE

                        servers.forEach { s ->
                            val sx = ((s.longitude + 180f) / 360f * w).toFloat()
                            val sy = ((90f - s.latitude) / 180f * h).toFloat()
                            val dist = hypot(tapOffset.x - sx, tapOffset.y - sy)
                            if (dist < 80f && dist < minDistance) {
                                minDistance = dist
                                closest = s
                            }
                        }

                        closest?.let {
                            activePopupServer = it
                            onServerSelected(it)
                        }
                    }
                }
        ) {
            val w = size.width
            val h = size.height

            // 1. Draw subtle latitude/longitude cyber grid
            for (i in 1..5) {
                val y = h * (i / 6f)
                drawLine(
                    color = DarkBorder.copy(alpha = 0.35f),
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 1f
                )
            }
            for (j in 1..7) {
                val x = w * (j / 8f)
                drawLine(
                    color = DarkBorder.copy(alpha = 0.35f),
                    start = Offset(x, 0f),
                    end = Offset(x, h),
                    strokeWidth = 1f
                )
            }

            // 2. Simplified continent silhouette shapes
            val landColor = Color(0xFF1E293B).copy(alpha = 0.65f)

            // North America
            drawCircle(landColor, radius = w * 0.16f, center = Offset(w * 0.22f, h * 0.34f))
            // South America
            drawCircle(landColor, radius = w * 0.11f, center = Offset(w * 0.32f, h * 0.70f))
            // Europe
            drawCircle(landColor, radius = w * 0.10f, center = Offset(w * 0.52f, h * 0.32f))
            // Africa
            drawCircle(landColor, radius = w * 0.13f, center = Offset(w * 0.54f, h * 0.58f))
            // Asia
            drawCircle(landColor, radius = w * 0.22f, center = Offset(w * 0.72f, h * 0.36f))
            // Australia
            drawCircle(landColor, radius = w * 0.09f, center = Offset(w * 0.85f, h * 0.76f))

            // 3. Draw connection arcs between key global gateways
            val gatewayCoords = listOf(
                Pair(w * 0.25f, h * 0.35f), // NYC
                Pair(w * 0.51f, h * 0.32f), // London / Frankfurt
                Pair(w * 0.78f, h * 0.38f), // Tokyo
                Pair(w * 0.72f, h * 0.52f)  // Singapore
            )
            for (i in 0 until gatewayCoords.size - 1) {
                val p1 = gatewayCoords[i]
                val p2 = gatewayCoords[i + 1]
                val arcPath = Path().apply {
                    moveTo(p1.first, p1.second)
                    quadraticTo(
                        (p1.first + p2.first) / 2f,
                        (p1.second + p2.second) / 2f - 20f,
                        p2.first,
                        p2.second
                    )
                }
                drawPath(
                    path = arcPath,
                    color = CyberCyan.copy(alpha = 0.25f),
                    style = Stroke(width = 1.5f)
                )
            }

            // 4. Draw server node pins
            servers.forEach { server ->
                val sx = ((server.longitude + 180f) / 360f * w).toFloat()
                val sy = ((90f - server.latitude) / 180f * h).toFloat()
                val isSelected = activePopupServer?.id == server.id

                val pinColor = if (isSelected) ElectricGreen else CyberCyan

                if (isSelected) {
                    // Pulsing radar ring
                    drawCircle(
                        color = ElectricGreen.copy(alpha = 0.3f),
                        radius = pulseRadius,
                        center = Offset(sx, sy)
                    )
                    drawCircle(
                        color = ElectricGreen.copy(alpha = 0.6f),
                        radius = 8f,
                        center = Offset(sx, sy)
                    )
                }

                // Core pin dot
                drawCircle(
                    color = pinColor,
                    radius = if (isSelected) 5f else 3.5f,
                    center = Offset(sx, sy)
                )
                drawCircle(
                    color = Color.White,
                    radius = if (isSelected) 2.5f else 1.5f,
                    center = Offset(sx, sy)
                )
            }
        }

        // Active Server Floating Detail Card at the bottom of the map
        activePopupServer?.let { server ->
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(10.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkSurface.copy(alpha = 0.95f))
                    .border(1.dp, CyberCyan.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = server.flagEmoji, fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "${server.city}, ${server.countryCode}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                PingBadge(pingMs = server.pingMs)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Load: ${server.loadPercent}%",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { onConnectClicked(server) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberCyan,
                            contentColor = Color(0xFF060B18)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("map_connect_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Connect",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
