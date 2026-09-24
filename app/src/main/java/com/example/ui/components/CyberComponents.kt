package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VpnProtocol
import com.example.data.model.VpnStatus
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricGreen
import com.example.ui.theme.GlowingAmber
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(20.dp),
    borderColor: Color = DarkBorder,
    backgroundColor: Color = DarkSurface.copy(alpha = 0.85f),
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(shape)
            .border(1.dp, borderColor, shape),
        color = backgroundColor,
        shape = shape
    ) {
        content()
    }
}

@Composable
fun AnimatedPowerButton(
    status: VpnStatus,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 190.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val rotateAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotate"
    )

    val (buttonColor, glowColor, statusText) = when (status) {
        VpnStatus.CONNECTED -> Triple(ElectricGreen, ElectricGreen.copy(alpha = 0.35f), "PROTECTED")
        VpnStatus.CONNECTING, VpnStatus.RECONNECTING -> Triple(GlowingAmber, GlowingAmber.copy(alpha = 0.35f), "CONNECTING")
        VpnStatus.DISCONNECTING -> Triple(GlowingAmber, GlowingAmber.copy(alpha = 0.2f), "DISCONNECTING")
        VpnStatus.DISCONNECTED -> Triple(CyberCyan, CyberCyan.copy(alpha = 0.25f), "DISCONNECTED")
        VpnStatus.ERROR -> Triple(CrimsonAlert, CrimsonAlert.copy(alpha = 0.35f), "ERROR")
    }

    Box(
        modifier = modifier
            .size(size + 30.dp)
            .testTag("vpn_power_button_container"),
        contentAlignment = Alignment.Center
    ) {
        // Outer Radar Waves when Connected or Connecting
        if (status == VpnStatus.CONNECTED || status == VpnStatus.CONNECTING) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .scale(pulseScale)
            ) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(glowColor, Color.Transparent),
                        center = center,
                        radius = this.size.width / 2
                    )
                )
            }
        }

        // Circular Rotating Dash Border
        Canvas(
            modifier = Modifier
                .size(size)
                .rotate(if (status == VpnStatus.CONNECTING) rotateAngle else 0f)
        ) {
            drawCircle(
                color = buttonColor.copy(alpha = 0.3f),
                radius = this.size.width / 2,
                style = Stroke(width = 3.dp.toPx())
            )
            drawCircle(
                color = buttonColor,
                radius = this.size.width / 2,
                style = Stroke(
                    width = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )
        }

        // Inner Circle Core Button
        Box(
            modifier = Modifier
                .size(size - 30.dp)
                .shadow(elevation = 16.dp, shape = CircleShape, spotColor = buttonColor)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            DarkSurfaceVariant,
                            DarkSurface
                        )
                    )
                )
                .border(2.dp, buttonColor.copy(alpha = 0.8f), CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                )
                .testTag("vpn_connect_button"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (status == VpnStatus.CONNECTING) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(42.dp),
                        color = buttonColor,
                        strokeWidth = 3.dp
                    )
                } else {
                    Icon(
                        imageVector = if (status == VpnStatus.CONNECTED) Icons.Default.Shield else Icons.Default.PowerSettingsNew,
                        contentDescription = "VPN Power Status",
                        modifier = Modifier.size(48.dp),
                        tint = buttonColor
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = statusText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = buttonColor
                )
            }
        }
    }
}

@Composable
fun PingBadge(pingMs: Int, modifier: Modifier = Modifier) {
    val (color, text) = when {
        pingMs < 45 -> Pair(ElectricGreen, "${pingMs}ms")
        pingMs < 90 -> Pair(GlowingAmber, "${pingMs}ms")
        else -> Pair(CrimsonAlert, "${pingMs}ms")
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.15f))
            .border(0.5.dp, color.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

@Composable
fun ProtocolTag(protocol: VpnProtocol, modifier: Modifier = Modifier) {
    val isTunnel = protocol.isFullTunnel
    val tagColor = if (isTunnel) CyberCyan else GlowingAmber
    val label = if (isTunnel) "VPN TUNNEL" else "PROXY"

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(tagColor.copy(alpha = 0.12f))
            .border(0.5.dp, tagColor.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label • ${protocol.displayName}",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = tagColor
        )
    }
}

@Composable
fun StatusGlowBadge(status: VpnStatus, modifier: Modifier = Modifier) {
    val (color, label) = when (status) {
        VpnStatus.CONNECTED -> Pair(ElectricGreen, "PROTECTED")
        VpnStatus.CONNECTING -> Pair(GlowingAmber, "ESTABLISHING TUNNEL")
        VpnStatus.DISCONNECTING -> Pair(GlowingAmber, "CLOSING TUNNEL")
        VpnStatus.DISCONNECTED -> Pair(TextMuted, "UNPROTECTED")
        VpnStatus.RECONNECTING -> Pair(GlowingAmber, "RECONNECTING")
        VpnStatus.ERROR -> Pair(CrimsonAlert, "CONNECTION FAILED")
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp,
            color = color
        )
    }
}
