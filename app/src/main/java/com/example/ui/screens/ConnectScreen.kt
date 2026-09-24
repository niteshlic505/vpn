package com.example.ui.screens

import android.net.VpnService
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PrivacySettingsEntity
import com.example.data.model.VpnServer
import com.example.data.model.VpnSessionInfo
import com.example.data.model.VpnStatus
import com.example.ui.components.AnimatedPowerButton
import com.example.ui.components.GlassCard
import com.example.ui.components.PingBadge
import com.example.ui.components.ProtocolTag
import com.example.ui.components.StatusGlowBadge
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.ElectricGreen
import com.example.ui.theme.GlowingAmber
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun ConnectScreen(
    sessionInfo: VpnSessionInfo,
    selectedServer: VpnServer,
    settings: PrivacySettingsEntity,
    recommendation: com.example.data.model.AiServerRecommendation? = null,
    onConnectRequested: (VpnServer) -> Unit,
    onDisconnectRequested: () -> Unit,
    onSelectServerClicked: () -> Unit,
    onNavigateToAiSentinel: () -> Unit,
    onRecommendationClicked: () -> Unit = {},
    onSettingsChanged: (PrivacySettingsEntity) -> Unit,
    onUpgradeClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Activity launcher for Android VpnService permission preparation
    val vpnPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            onConnectRequested(selectedServer)
        }
    }

    val isConnected = sessionInfo.status == VpnStatus.CONNECTED

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepNavy)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top App Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CyberCyan.copy(alpha = 0.15f))
                        .border(1.dp, CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "ShieldAI VPN",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Zero-Logs Privacy Tunnel",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            // Pro VIP Pill Button
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF8B5CF6), Color(0xFF6366F1))
                        )
                    )
                    .clickable { onUpgradeClicked() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("vip_upgrade_badge"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.WorkspacePremium,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "PRO VIP",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Status Glow Indicator
        StatusGlowBadge(status = sessionInfo.status)

        Spacer(modifier = Modifier.height(22.dp))

        // Large Central Power Shield Button
        AnimatedPowerButton(
            status = sessionInfo.status,
            onClick = {
                if (isConnected) {
                    onDisconnectRequested()
                } else {
                    val prepareIntent = VpnService.prepare(context)
                    if (prepareIntent != null) {
                        vpnPermissionLauncher.launch(prepareIntent)
                    } else {
                        onConnectRequested(selectedServer)
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(22.dp))

        // IP Masking Card
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = DarkSurfaceVariant.copy(alpha = 0.5f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isConnected) Icons.Default.Lock else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (isConnected) ElectricGreen else GlowingAmber,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isConnected) "Virtual Shield IP" else "Visible Public IP",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = if (isConnected) sessionInfo.publicIp else "142.250.190.46 (Exposed)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isConnected) CyberCyan else CrimsonAlert
                        )
                    }
                }

                if (isConnected) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(ElectricGreen.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "ENCRYPTED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ElectricGreen
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Selected Server Selector Card
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelectServerClicked() }
                .testTag("select_server_card")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = selectedServer.flagEmoji,
                        fontSize = 32.sp
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${selectedServer.city}, ${selectedServer.countryName}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            PingBadge(pingMs = selectedServer.pingMs)
                            Spacer(modifier = Modifier.width(8.dp))
                            ProtocolTag(protocol = selectedServer.protocol)
                        }
                    }
                }

                Icon(
                    imageVector = Icons.Default.ArrowForwardIos,
                    contentDescription = "Change Server",
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // AI Server Recommendation Prompt (when not connected)
        if (!isConnected && recommendation != null) {
            val recServer = recommendation.recommendedServer
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        androidx.compose.ui.graphics.Brush.horizontalGradient(
                            listOf(Color(0xFF0D223E), Color(0xFF091629))
                        )
                    )
                    .border(1.dp, CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .clickable { onRecommendationClicked() }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .testTag("ai_connect_recommendation_card"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "AI Recommends: ${recServer.city}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${recServer.pingMs}ms",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricGreen
                            )
                        }
                        Text(
                            text = "${recommendation.score}% Match • ${recommendation.summaryTagline}",
                            fontSize = 10.sp,
                            color = TextMuted,
                            maxLines = 1
                        )
                    }
                }

                Text(
                    text = "Why? →",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = CyberCyan
                )
            }
        }

        // Live Telemetry Stats (visible when connected)
        AnimatedVisibility(
            visible = isConnected,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column {
                Spacer(modifier = Modifier.height(14.dp))
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = CyberCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                val minutes = sessionInfo.durationSeconds / 60
                                val seconds = sessionInfo.durationSeconds % 60
                                Text(
                                    text = String.format("%02d:%02d", minutes, seconds),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }

                            Text(
                                text = "Tunnel Protocol: ${selectedServer.protocol.displayName}",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Download Stat
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CloudDownload,
                                    contentDescription = null,
                                    tint = ElectricGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "Download",
                                        fontSize = 10.sp,
                                        color = TextMuted
                                    )
                                    val mbDown = sessionInfo.bytesIn / (1024 * 1024f)
                                    Text(
                                        text = "%.2f MB".format(mbDown),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }
                            }

                            // Upload Stat
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CloudUpload,
                                    contentDescription = null,
                                    tint = CyberCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "Upload",
                                        fontSize = 10.sp,
                                        color = TextMuted
                                    )
                                    val mbUp = sessionInfo.bytesOut / (1024 * 1024f)
                                    Text(
                                        text = "%.2f MB".format(mbUp),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }
                            }

                            // Live Rate
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Current Speed",
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )
                                Text(
                                    text = "%.1f Mbps".format(sessionInfo.speedDownKbps / 1000f),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricGreen
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // AI Privacy Sentinel Insight Banner
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToAiSentinel() }
                .testTag("ai_sentinel_banner"),
            borderColor = CyberCyan.copy(alpha = 0.35f),
            backgroundColor = Color(0xFF0F1E38)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(CyberCyan.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "ShieldAI Sentinel Active",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan
                        )
                        Text(
                            text = "Powered by Google Gemini 3.1 Pro Thinking Mode",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ArrowForwardIos,
                    contentDescription = null,
                    tint = CyberCyan,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Quick Security Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Kill Switch Toggle Card
            GlassCard(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        onSettingsChanged(settings.copy(killSwitchEnabled = !settings.killSwitchEnabled))
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Kill Switch",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (settings.killSwitchEnabled) "Blocking leaks" else "Off",
                            fontSize = 10.sp,
                            color = if (settings.killSwitchEnabled) ElectricGreen else TextMuted
                        )
                    }

                    Switch(
                        checked = settings.killSwitchEnabled,
                        onCheckedChange = { isChecked ->
                            onSettingsChanged(settings.copy(killSwitchEnabled = isChecked))
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = ElectricGreen
                        ),
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            // DNS Protection Toggle Card
            GlassCard(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        onSettingsChanged(settings.copy(dnsLeakProtectionEnabled = !settings.dnsLeakProtectionEnabled))
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "DNS Shield",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (settings.dnsLeakProtectionEnabled) "Encrypted" else "ISP Default",
                            fontSize = 10.sp,
                            color = if (settings.dnsLeakProtectionEnabled) CyberCyan else TextMuted
                        )
                    }

                    Switch(
                        checked = settings.dnsLeakProtectionEnabled,
                        onCheckedChange = { isChecked ->
                            onSettingsChanged(settings.copy(dnsLeakProtectionEnabled = isChecked))
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = CyberCyan
                        ),
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
