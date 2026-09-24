package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AiServerRecommendation
import com.example.data.model.RecommendationGoal
import com.example.data.model.SpeedTestMetric
import com.example.data.model.UserLocation
import com.example.data.model.VpnProtocol
import com.example.data.model.VpnServer
import com.example.ui.components.AiRecommendationBanner
import com.example.ui.components.AiRecommendationDialog
import com.example.ui.components.GlassCard
import com.example.ui.components.InteractiveWorldMap
import com.example.ui.components.PingBadge
import com.example.ui.components.ProtocolTag
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

enum class ServerFilterTab {
    ALL,
    AI_RECOMMENDED,
    FAVORITES,
    FASTEST,
    FREE,
    VIP,
    WIREGUARD
}

enum class ViewMode {
    LIST,
    MAP
}

@Composable
fun ServerListScreen(
    servers: List<VpnServer>,
    favoriteIds: List<String>,
    selectedServer: VpnServer,
    recommendation: AiServerRecommendation?,
    userLocation: UserLocation,
    speedTest: SpeedTestMetric,
    selectedGoal: RecommendationGoal,
    isAnalyzing: Boolean,
    analysisProgress: Float,
    statusMessage: String,
    onServerSelected: (VpnServer) -> Unit,
    onConnectClicked: (VpnServer) -> Unit,
    onToggleFavorite: (String, Boolean) -> Unit,
    onRefreshPings: () -> Unit,
    onGoalSelected: (RecommendationGoal) -> Unit,
    onLocationChanged: (UserLocation) -> Unit,
    onRunBenchmark: () -> Unit,
    onUpgradeClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(ServerFilterTab.ALL) }
    var viewMode by remember { mutableStateOf(ViewMode.LIST) }
    var isRefreshing by remember { mutableStateOf(false) }
    var showRecommendationDialog by remember { mutableStateOf(false) }

    if (showRecommendationDialog) {
        AiRecommendationDialog(
            recommendation = recommendation,
            userLocation = userLocation,
            speedTest = speedTest,
            selectedGoal = selectedGoal,
            isAnalyzing = isAnalyzing,
            analysisProgress = analysisProgress,
            statusMessage = statusMessage,
            onGoalSelected = onGoalSelected,
            onLocationChanged = onLocationChanged,
            onRunBenchmark = onRunBenchmark,
            onConnectServer = { server ->
                onServerSelected(server)
                onConnectClicked(server)
            },
            onDismiss = { showRecommendationDialog = false }
        )
    }

    val fastestServer = remember(servers) {
        servers.filter { it.isOnline }.minByOrNull { it.pingMs } ?: servers.first()
    }

    // Filter servers
    val filteredServers = remember(servers, searchQuery, selectedFilter, favoriteIds, recommendation) {
        servers.filter { server ->
            val matchesSearch = searchQuery.isBlank() ||
                    server.city.contains(searchQuery, ignoreCase = true) ||
                    server.countryName.contains(searchQuery, ignoreCase = true) ||
                    server.countryCode.contains(searchQuery, ignoreCase = true) ||
                    server.protocol.displayName.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (selectedFilter) {
                ServerFilterTab.ALL -> true
                ServerFilterTab.AI_RECOMMENDED -> recommendation?.recommendedServer?.id == server.id || recommendation?.runnerUpServers?.any { it.first.id == server.id } == true
                ServerFilterTab.FAVORITES -> favoriteIds.contains(server.id)
                ServerFilterTab.FASTEST -> server.pingMs < 45
                ServerFilterTab.FREE -> !server.isPremium
                ServerFilterTab.VIP -> server.isPremium
                ServerFilterTab.WIREGUARD -> server.protocol == VpnProtocol.WIREGUARD
            }

            matchesSearch && matchesFilter
        }.sortedWith(
            when (selectedFilter) {
                ServerFilterTab.FASTEST -> compareBy { it.pingMs }
                ServerFilterTab.FAVORITES -> compareByDescending { favoriteIds.contains(it.id) }
                else -> compareBy { it.countryName }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepNavy)
            .padding(top = 12.dp)
    ) {
        // Screen Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Global Gateways",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
                Text(
                    text = "${servers.size} Public & Licensed Privacy Nodes",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Refresh Button
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            isRefreshing = true
                            onRefreshPings()
                            isRefreshing = false
                        }
                    },
                    modifier = Modifier.size(38.dp)
                ) {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = CyberCyan,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Ping Latencies",
                            tint = CyberCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // View Mode Switcher
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurfaceVariant)
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (viewMode == ViewMode.LIST) CyberCyan else Color.Transparent)
                            .clickable { viewMode = ViewMode.LIST }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ViewList,
                            contentDescription = "List View",
                            tint = if (viewMode == ViewMode.LIST) Color(0xFF060B18) else TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (viewMode == ViewMode.MAP) CyberCyan else Color.Transparent)
                            .clickable { viewMode = ViewMode.MAP }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = "Map View",
                            tint = if (viewMode == ViewMode.MAP) Color(0xFF060B18) else TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .testTag("server_search_field"),
            placeholder = { Text("Search countries, cities, protocols...", color = TextMuted, fontSize = 13.sp) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyberCyan,
                unfocusedBorderColor = DarkBorder,
                focusedContainerColor = DarkSurface,
                unfocusedContainerColor = DarkSurface,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filter Chips Row
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filters = listOf(
                Pair(ServerFilterTab.ALL, "All (${servers.size})"),
                Pair(ServerFilterTab.AI_RECOMMENDED, "✨ AI Top Match"),
                Pair(ServerFilterTab.FAVORITES, "Favorites (${favoriteIds.size})"),
                Pair(ServerFilterTab.FASTEST, "⚡ Fastest (<45ms)"),
                Pair(ServerFilterTab.FREE, "Free"),
                Pair(ServerFilterTab.VIP, "VIP Pro 10Gbps"),
                Pair(ServerFilterTab.WIREGUARD, "WireGuard®")
            )

            items(filters) { (tab, label) ->
                val isSelected = selectedFilter == tab
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) CyberCyan.copy(alpha = 0.2f) else DarkSurface)
                        .border(
                            1.dp,
                            if (isSelected) CyberCyan else DarkBorder,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { selectedFilter = tab }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) CyberCyan else TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (viewMode == ViewMode.MAP) {
            // Interactive Map View
            Column(modifier = Modifier.padding(horizontal = 18.dp)) {
                Text(
                    text = "Interactive Mesh Radar",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Tap any node pin to inspect and route traffic",
                    fontSize = 11.sp,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(8.dp))
                InteractiveWorldMap(
                    servers = servers,
                    selectedServer = selectedServer,
                    onServerSelected = onServerSelected,
                    onConnectClicked = onConnectClicked
                )
            }
        }

        // AI Smart Recommendation Banner
        if (recommendation != null) {
            AiRecommendationBanner(
                recommendation = recommendation,
                onViewDetails = { showRecommendationDialog = true },
                onConnect = { server ->
                    onServerSelected(server)
                    onConnectClicked(server)
                },
                onRunBenchmark = onRunBenchmark,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp)
            )
        }

        // Auto-Select Fastest Banner
        Box(
            modifier = Modifier
                .padding(horizontal = 18.dp, vertical = 8.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(
                    androidx.compose.ui.graphics.Brush.horizontalGradient(
                        listOf(Color(0xFF0F2B48), Color(0xFF0D1D33))
                    )
                )
                .border(1.dp, CyberCyan.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                .clickable {
                    onServerSelected(fastestServer)
                    onConnectClicked(fastestServer)
                }
                .padding(14.dp)
                .testTag("auto_select_fastest_card")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(ElectricGreen.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = ElectricGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Auto-Select Fastest Node",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${fastestServer.flagEmoji} ${fastestServer.city}, ${fastestServer.countryName} (${fastestServer.pingMs}ms)",
                            fontSize = 11.sp,
                            color = ElectricGreen
                        )
                    }
                }

                Button(
                    onClick = {
                        onServerSelected(fastestServer)
                        onConnectClicked(fastestServer)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricGreen,
                        contentColor = Color(0xFF060B18)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("Connect", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Servers List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(filteredServers, key = { it.id }) { server ->
                val isSelected = selectedServer.id == server.id
                val isFavorite = favoriteIds.contains(server.id)

                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onServerSelected(server) }
                        .testTag("server_item_${server.id}"),
                    borderColor = if (isSelected) CyberCyan else DarkBorder,
                    backgroundColor = if (isSelected) Color(0xFF0F1E38) else DarkSurface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left Flag & Details
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = server.flagEmoji,
                                fontSize = 28.sp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${server.city}, ${server.countryName}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    if (server.isPremium) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Default.WorkspacePremium,
                                            contentDescription = "VIP",
                                            tint = GlowingAmber,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    PingBadge(pingMs = server.pingMs)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    ProtocolTag(protocol = server.protocol)
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.width(150.dp)
                                ) {
                                    LinearProgressIndicator(
                                        progress = { server.loadPercent / 100f },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(4.dp)
                                            .clip(RoundedCornerShape(2.dp)),
                                        color = if (server.loadPercent < 70) CyberCyan else GlowingAmber,
                                        trackColor = DarkBorder
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${server.loadPercent}% load",
                                        fontSize = 10.sp,
                                        color = TextMuted
                                    )
                                }
                            }
                        }

                        // Right Actions: Favorite & Connect
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { onToggleFavorite(server.id, isFavorite) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "Favorite",
                                    tint = if (isFavorite) GlowingAmber else TextMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            if (server.isPremium) {
                                Button(
                                    onClick = { onUpgradeClicked() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = GlowingAmber.copy(alpha = 0.2f),
                                        contentColor = GlowingAmber
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("VIP", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Button(
                                    onClick = {
                                        onServerSelected(server)
                                        onConnectClicked(server)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) ElectricGreen else CyberCyan,
                                        contentColor = Color(0xFF060B18)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text(
                                        text = if (isSelected) "Active" else "Connect",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
