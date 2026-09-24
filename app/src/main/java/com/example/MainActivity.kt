package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DnsLeakReport
import com.example.data.model.VpnServer
import com.example.ui.components.AiRecommendationDialog
import com.example.ui.components.VipUpgradeDialog
import com.example.ui.navigation.Screen
import com.example.ui.screens.AiAssistantScreen
import com.example.ui.screens.ConnectScreen
import com.example.ui.screens.LeakCheckScreen
import com.example.ui.screens.ServerListScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.ElectricGreen
import com.example.ui.theme.GlowingAmber
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainAppRoot()
            }
        }
    }
}

@Composable
fun MainAppRoot() {
    val app = ShieldApplication.instance
    val vpnRepository = app.vpnRepository
    val serverRepository = app.serverRepository
    val aiRepository = app.aiRepository
    val recommendationRepository = app.recommendationRepository

    val sessionInfo by vpnRepository.sessionInfo.collectAsState()
    val settings by vpnRepository.settings.collectAsState()
    val servers by serverRepository.servers.collectAsState()
    val favoriteIds by serverRepository.favoriteIds.collectAsState(initial = emptyList())
    val historyList by vpnRepository.connectionHistory.collectAsState(initial = emptyList())
    val totalSessions by vpnRepository.totalSessionsCount.collectAsState(initial = 0)
    val totalDuration by vpnRepository.totalDurationSeconds.collectAsState(initial = 0L)
    val totalBytes by vpnRepository.totalTransferredBytes.collectAsState(initial = 0L)
    val aiMessages by aiRepository.messages.collectAsState()
    val isAiGenerating by aiRepository.isGenerating.collectAsState()

    val recommendation by recommendationRepository.recommendation.collectAsState()
    val userLocation by recommendationRepository.userLocation.collectAsState()
    val speedTest by recommendationRepository.speedTest.collectAsState()
    val selectedGoal by recommendationRepository.selectedGoal.collectAsState()
    val isAnalyzingRecommendation by recommendationRepository.isAnalyzing.collectAsState()
    val recommendationProgress by recommendationRepository.analysisProgress.collectAsState()
    val recommendationStatus by recommendationRepository.statusMessage.collectAsState()

    var currentScreen by remember { mutableStateOf<Screen>(Screen.Connect) }
    var showAiRecommendationDialog by remember { mutableStateOf(false) }
    var selectedServer by remember(servers) {
        mutableStateOf(servers.firstOrNull() ?: VpnServer(
            id = "us_nyc",
            countryName = "United States",
            countryCode = "US",
            city = "New York",
            ipAddress = "198.51.100.24",
            pingMs = 28,
            loadPercent = 35,
            speedMbps = 900,
            protocol = com.example.data.model.VpnProtocol.WIREGUARD,
            isPremium = false,
            reliabilityScore = 99,
            latitude = 40.7128,
            longitude = -74.0060,
            flagEmoji = "🇺🇸"
        ))
    }

    var leakReport by remember {
        mutableStateOf(
            DnsLeakReport(
                originalIp = "142.250.190.46",
                currentIp = "142.250.190.46",
                isp = "Residential Telecom Carrier",
                country = "United States",
                city = "Local Area",
                detectedDnsIps = listOf("192.168.1.1", "75.75.75.75"),
                isDnsLeaking = true,
                isIpProtected = false
            )
        )
    }

    var showVipDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val privacyScoreReport = remember(sessionInfo, settings) {
        vpnRepository.calculatePrivacyScore()
    }

    if (showVipDialog) {
        VipUpgradeDialog(
            onDismiss = { showVipDialog = false },
            onSubscribed = {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("🎉 ShieldAI PRO VIP Active! 10Gbps Gateways Unlocked.")
                }
            }
        )
    }

    if (showAiRecommendationDialog) {
        AiRecommendationDialog(
            recommendation = recommendation,
            userLocation = userLocation,
            speedTest = speedTest,
            selectedGoal = selectedGoal,
            isAnalyzing = isAnalyzingRecommendation,
            analysisProgress = recommendationProgress,
            statusMessage = recommendationStatus,
            onGoalSelected = { goal -> recommendationRepository.setGoal(goal) },
            onLocationChanged = { loc -> recommendationRepository.setUserLocation(loc) },
            onRunBenchmark = {
                coroutineScope.launch {
                    recommendationRepository.runRecommendationAnalysis(runBenchmark = true)
                }
            },
            onConnectServer = { server ->
                selectedServer = server
                vpnRepository.startVpn(server)
                currentScreen = Screen.Connect
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Connected to recommended gateway: ${server.city} (${server.pingMs}ms)")
                }
            },
            onDismiss = { showAiRecommendationDialog = false }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DeepNavy,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(width = 1.dp, color = DarkBorder)
            ) {
                NavigationBar(
                    containerColor = DarkSurface,
                    contentColor = TextPrimary,
                    tonalElevation = 0.dp,
                    modifier = Modifier.testTag("main_bottom_nav")
                ) {
                    val screens = listOf(
                        Screen.Connect,
                        Screen.Servers,
                        Screen.LeakCheck,
                        Screen.SentinelAi,
                        Screen.Settings
                    )

                    screens.forEach { screen ->
                        val isSelected = currentScreen == screen
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.title,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            selected = isSelected,
                            onClick = { currentScreen = screen },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = CyberCyan,
                                selectedTextColor = CyberCyan,
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted,
                                indicatorColor = CyberCyan.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier.testTag("nav_item_${screen.route}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                is Screen.Connect -> {
                    ConnectScreen(
                        sessionInfo = sessionInfo,
                        selectedServer = selectedServer,
                        settings = settings,
                        recommendation = recommendation,
                        onConnectRequested = { server ->
                            vpnRepository.startVpn(server)
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Establishing quantum tunnel to ${server.city}...")
                            }
                        },
                        onDisconnectRequested = {
                            vpnRepository.stopVpn()
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("VPN tunnel disconnected.")
                            }
                        },
                        onSelectServerClicked = { currentScreen = Screen.Servers },
                        onNavigateToAiSentinel = { currentScreen = Screen.SentinelAi },
                        onRecommendationClicked = { showAiRecommendationDialog = true },
                        onSettingsChanged = { updated ->
                            coroutineScope.launch { vpnRepository.updateSettings(updated) }
                        },
                        onUpgradeClicked = { showVipDialog = true }
                    )
                }

                is Screen.Servers -> {
                    ServerListScreen(
                        servers = servers,
                        favoriteIds = favoriteIds,
                        selectedServer = selectedServer,
                        recommendation = recommendation,
                        userLocation = userLocation,
                        speedTest = speedTest,
                        selectedGoal = selectedGoal,
                        isAnalyzing = isAnalyzingRecommendation,
                        analysisProgress = recommendationProgress,
                        statusMessage = recommendationStatus,
                        onServerSelected = { server -> selectedServer = server },
                        onConnectClicked = { server ->
                            selectedServer = server
                            vpnRepository.startVpn(server)
                            currentScreen = Screen.Connect
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Routing all device traffic through ${server.city}")
                            }
                        },
                        onToggleFavorite = { id, isFav ->
                            coroutineScope.launch {
                                serverRepository.toggleFavorite(id, isFav)
                            }
                        },
                        onRefreshPings = {
                            coroutineScope.launch {
                                serverRepository.refreshServerPings()
                                snackbarHostState.showSnackbar("Server latencies refreshed")
                            }
                        },
                        onGoalSelected = { goal ->
                            recommendationRepository.setGoal(goal)
                        },
                        onLocationChanged = { loc ->
                            recommendationRepository.setUserLocation(loc)
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Location set to ${loc.city}, ${loc.countryCode}. AI re-evaluating gateways.")
                            }
                        },
                        onRunBenchmark = {
                            coroutineScope.launch {
                                recommendationRepository.runRecommendationAnalysis(runBenchmark = true)
                            }
                        },
                        onUpgradeClicked = { showVipDialog = true }
                    )
                }

                is Screen.LeakCheck -> {
                    LeakCheckScreen(
                        privacyScoreReport = privacyScoreReport,
                        leakReport = leakReport,
                        onRunLeakTest = {
                            leakReport = vpnRepository.performLeakCheck()
                            snackbarHostState.showSnackbar("Comprehensive leak audit complete!")
                        },
                        onConnectVpnClicked = {
                            vpnRepository.startVpn(selectedServer)
                            currentScreen = Screen.Connect
                        },
                        onFixSettingsClicked = {
                            coroutineScope.launch {
                                vpnRepository.updateSettings(
                                    settings.copy(
                                        killSwitchEnabled = true,
                                        dnsLeakProtectionEnabled = true
                                    )
                                )
                                snackbarHostState.showSnackbar("Armed Kill Switch & Encrypted DNS Shield!")
                            }
                        }
                    )
                }

                is Screen.SentinelAi -> {
                    AiAssistantScreen(
                        messages = aiMessages,
                        isGenerating = isAiGenerating,
                        onSendMessage = { text, highThinking ->
                            coroutineScope.launch {
                                aiRepository.sendMessage(text, enableHighThinking = highThinking)
                            }
                        },
                        onRecommendServerClicked = { showAiRecommendationDialog = true }
                    )
                }

                is Screen.Settings -> {
                    SettingsScreen(
                        settings = settings,
                        historyList = historyList,
                        totalSessions = totalSessions,
                        totalDuration = totalDuration ?: 0L,
                        totalBytes = totalBytes ?: 0L,
                        onSettingsChanged = { updated ->
                            coroutineScope.launch {
                                vpnRepository.updateSettings(updated)
                                snackbarHostState.showSnackbar("Settings updated")
                            }
                        },
                        onClearHistory = {
                            coroutineScope.launch {
                                vpnRepository.clearHistory()
                                snackbarHostState.showSnackbar("Connection history cleared")
                            }
                        },
                        onUpgradeClicked = { showVipDialog = true }
                    )
                }
            }
        }
    }
}
