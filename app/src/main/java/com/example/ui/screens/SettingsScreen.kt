package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ConnectionHistoryEntity
import com.example.data.local.PrivacySettingsEntity
import com.example.ui.components.GlassCard
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(
    settings: PrivacySettingsEntity,
    historyList: List<ConnectionHistoryEntity>,
    totalSessions: Int,
    totalDuration: Long,
    totalBytes: Long,
    onSettingsChanged: (PrivacySettingsEntity) -> Unit,
    onClearHistory: () -> Unit,
    onUpgradeClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isDnsDropdownExpanded by remember { mutableStateOf(false) }
    var isProtocolDropdownExpanded by remember { mutableStateOf(false) }

    val dnsOptions = listOf(
        Pair("Cloudflare (1.1.1.1)", "1.1.1.1"),
        Pair("Quad9 (9.9.9.9)", "9.9.9.9"),
        Pair("AdGuard DNS (94.140.14.14)", "94.140.14.14"),
        Pair("Google DNS (8.8.8.8)", "8.8.8.8")
    )

    val protocolOptions = listOf(
        "WIREGUARD",
        "OPENVPN",
        "SOCKS5"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepNavy)
            .verticalScroll(rememberScrollState())
            .padding(18.dp)
    ) {
        // Header
        Text(
            text = "Privacy & Core Shield Settings",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary
        )
        Text(
            text = "Configure fail-safes, DNS encryption, and AI reasoning",
            fontSize = 12.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // VIP Pro Subscription Card
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onUpgradeClicked() }
                .testTag("vip_settings_banner"),
            borderColor = GlowingAmber.copy(alpha = 0.5f),
            backgroundColor = Color(0xFF1E1B38)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(GlowingAmber.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            tint = GlowingAmber,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "ShieldAI VIP Access",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlowingAmber
                        )
                        Text(
                            text = "10Gbps dedicated nodes, double-hop & priority AI",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                Button(
                    onClick = onUpgradeClicked,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GlowingAmber,
                        contentColor = Color(0xFF060B18)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Upgrade", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section: Network Security Controls
        Text(
            text = "NETWORK DEFENSE",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = CyberCyan,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Kill Switch Setting
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (settings.killSwitchEnabled) ElectricGreen else TextMuted,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Kill Switch (Fail-Safe)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Blocks non-VPN traffic to prevent leaks during reconnects",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Switch(
                        checked = settings.killSwitchEnabled,
                        onCheckedChange = { onSettingsChanged(settings.copy(killSwitchEnabled = it)) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = ElectricGreen
                        ),
                        modifier = Modifier.testTag("kill_switch_toggle")
                    )
                }

                Divider(color = DarkBorder, modifier = Modifier.padding(vertical = 12.dp))

                // DNS Leak Protection Setting
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Dns,
                            contentDescription = null,
                            tint = if (settings.dnsLeakProtectionEnabled) CyberCyan else TextMuted,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "DNS Leak Protection",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Forces encrypted DNS through dedicated resolver",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Switch(
                        checked = settings.dnsLeakProtectionEnabled,
                        onCheckedChange = { onSettingsChanged(settings.copy(dnsLeakProtectionEnabled = it)) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = CyberCyan
                        ),
                        modifier = Modifier.testTag("dns_protection_toggle")
                    )
                }

                if (settings.dnsLeakProtectionEnabled) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkSurfaceVariant)
                            .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
                            .clickable { isDnsDropdownExpanded = true }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("DNS Provider", fontSize = 10.sp, color = TextMuted)
                                Text(settings.selectedDnsProvider, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CyberCyan)
                            }
                            Text("Change ▼", fontSize = 11.sp, color = TextSecondary)
                        }

                        DropdownMenu(
                            expanded = isDnsDropdownExpanded,
                            onDismissRequest = { isDnsDropdownExpanded = false }
                        ) {
                            dnsOptions.forEach { (name, ip) ->
                                DropdownMenuItem(
                                    text = { Text("$name ($ip)") },
                                    onClick = {
                                        onSettingsChanged(
                                            settings.copy(
                                                selectedDnsProvider = name,
                                                customDnsIp = ip
                                            )
                                        )
                                        isDnsDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Divider(color = DarkBorder, modifier = Modifier.padding(vertical = 12.dp))

                // Auto-Reconnect Setting
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Auto-Reconnect",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Instantly restore tunnel if Wi-Fi or LTE drops",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Switch(
                        checked = settings.autoReconnect,
                        onCheckedChange = { onSettingsChanged(settings.copy(autoReconnect = it)) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = CyberCyan
                        )
                    )
                }

                Divider(color = DarkBorder, modifier = Modifier.padding(vertical = 12.dp))

                // Default Protocol Selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Default Protocol",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "WireGuard® recommended for top speed and low power",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Box {
                        OutlinedButton(
                            onClick = { isProtocolDropdownExpanded = true },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(settings.preferredProtocol, fontSize = 11.sp)
                        }

                        DropdownMenu(
                            expanded = isProtocolDropdownExpanded,
                            onDismissRequest = { isProtocolDropdownExpanded = false }
                        ) {
                            protocolOptions.forEach { proto ->
                                DropdownMenuItem(
                                    text = { Text(proto) },
                                    onClick = {
                                        onSettingsChanged(settings.copy(preferredProtocol = proto))
                                        isProtocolDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Section: Connection History from Room Database
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "CONNECTION HISTORY (LOCAL ROOM DB)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CyberCyan,
                letterSpacing = 1.sp
            )

            if (historyList.isNotEmpty()) {
                IconButton(
                    onClick = onClearHistory,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clear History",
                        tint = CrimsonAlert,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // History Summary Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GlassCard(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Sessions", fontSize = 10.sp, color = TextMuted)
                    Text("$totalSessions", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            }
            GlassCard(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total Time", fontSize = 10.sp, color = TextMuted)
                    val hrs = totalDuration / 3600
                    val mins = (totalDuration % 3600) / 60
                    Text("${hrs}h ${mins}m", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ElectricGreen)
                }
            }
            GlassCard(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Transferred", fontSize = 10.sp, color = TextMuted)
                    val mb = totalBytes / (1024 * 1024f)
                    Text("%.1f MB".format(mb), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = CyberCyan)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Recent session history items
        if (historyList.isEmpty()) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No past sessions yet. Connect to record telemetry.", color = TextMuted, fontSize = 12.sp)
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                historyList.take(5).forEach { session ->
                    val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                    val dateStr = dateFormat.format(Date(session.startTime))
                    val durationMin = session.durationSeconds / 60
                    val durationSec = session.durationSeconds % 60
                    val mb = (session.bytesDownloaded + session.bytesUploaded) / (1024 * 1024f)

                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(session.flagEmoji, fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("${session.city}, ${session.country}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text("$dateStr • ${session.protocol}", fontSize = 11.sp, color = TextSecondary)
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(String.format("%02d:%02d", durationMin, durationSec), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ElectricGreen)
                                Text("%.2f MB".format(mb), fontSize = 11.sp, color = TextMuted)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Section: Zero-Logs & Security Charter
        Text(
            text = "ZERO-LOGS PRIVACY CHARTER",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = CyberCyan,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "🔒 Zero Traffic Logs Guaranteed",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "ShieldAI does not store browsing history, destination URLs, DNS lookups, or payload data. Only minimum session metrics (connection duration & byte counts) are recorded locally on your device in Room DB.",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}
