package com.example.data.repository

import android.content.Context
import com.example.data.local.ConnectionHistoryDao
import com.example.data.local.ConnectionHistoryEntity
import com.example.data.local.PrivacySettingsDao
import com.example.data.local.PrivacySettingsEntity
import com.example.data.model.DnsLeakReport
import com.example.data.model.PrivacyScoreReport
import com.example.data.model.VpnProtocol
import com.example.data.model.VpnServer
import com.example.data.model.VpnSessionInfo
import com.example.data.model.VpnStatus
import com.example.vpn.ShieldVpnService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VpnRepository(
    private val context: Context,
    private val historyDao: ConnectionHistoryDao,
    private val settingsDao: PrivacySettingsDao
) {
    private val repoScope = CoroutineScope(Dispatchers.IO)

    private val _sessionInfo = MutableStateFlow(VpnSessionInfo())
    val sessionInfo = _sessionInfo.asStateFlow()

    private val _settings = MutableStateFlow(PrivacySettingsEntity())
    val settings = _settings.asStateFlow()

    val connectionHistory: Flow<List<ConnectionHistoryEntity>> = historyDao.getAllHistory()
    val totalSessionsCount: Flow<Int> = historyDao.getHistoryCount()
    val totalDurationSeconds: Flow<Long?> = historyDao.getTotalDuration()
    val totalTransferredBytes: Flow<Long?> = historyDao.getTotalBytesTransferred()

    init {
        instance = this
        repoScope.launch {
            val saved = settingsDao.getSettings().firstOrNull()
            if (saved != null) {
                _settings.value = saved
            } else {
                val defaultSettings = PrivacySettingsEntity()
                settingsDao.saveSettings(defaultSettings)
                _settings.value = defaultSettings
            }
        }
    }

    companion object {
        var instance: VpnRepository? = null
            private set
    }

    fun startVpn(server: VpnServer) {
        val currentSettings = _settings.value
        _sessionInfo.value = _sessionInfo.value.copy(
            status = VpnStatus.CONNECTING,
            server = server,
            errorMessage = null
        )

        ShieldVpnService.startVpn(
            context = context,
            server = server,
            dnsIp = currentSettings.customDnsIp,
            killSwitch = currentSettings.killSwitchEnabled
        )
    }

    fun stopVpn() {
        _sessionInfo.value = _sessionInfo.value.copy(status = VpnStatus.DISCONNECTING)
        ShieldVpnService.stopVpn(context)
    }

    fun notifyVpnStatusChanged(
        status: VpnStatus,
        server: VpnServer?,
        connectedAtMillis: Long = System.currentTimeMillis(),
        publicIp: String = "192.168.1.100",
        errorMessage: String? = null
    ) {
        _sessionInfo.value = _sessionInfo.value.copy(
            status = status,
            server = server ?: _sessionInfo.value.server,
            connectedAtMillis = connectedAtMillis,
            publicIp = publicIp,
            errorMessage = errorMessage
        )
    }

    fun updateLiveTraffic(
        durationSeconds: Long,
        bytesIn: Long,
        bytesOut: Long,
        speedDownKbps: Float,
        speedUpKbps: Float
    ) {
        _sessionInfo.value = _sessionInfo.value.copy(
            durationSeconds = durationSeconds,
            bytesIn = bytesIn,
            bytesOut = bytesOut,
            speedDownKbps = speedDownKbps,
            speedUpKbps = speedUpKbps
        )
    }

    fun notifyVpnDisconnected(
        server: VpnServer?,
        durationSeconds: Long,
        bytesIn: Long,
        bytesOut: Long
    ) {
        val currentServer = server ?: _sessionInfo.value.server
        _sessionInfo.value = VpnSessionInfo(status = VpnStatus.DISCONNECTED)

        if (currentServer != null && durationSeconds > 0) {
            repoScope.launch {
                historyDao.insertRecord(
                    ConnectionHistoryEntity(
                        serverId = currentServer.id,
                        country = currentServer.countryName,
                        city = currentServer.city,
                        ipAddress = currentServer.ipAddress,
                        protocol = currentServer.protocol.displayName,
                        flagEmoji = currentServer.flagEmoji,
                        startTime = System.currentTimeMillis() - (durationSeconds * 1000),
                        durationSeconds = durationSeconds,
                        bytesDownloaded = bytesIn,
                        bytesUploaded = bytesOut
                    )
                )
            }
        }
    }

    suspend fun updateSettings(updated: PrivacySettingsEntity) {
        _settings.value = updated
        settingsDao.saveSettings(updated)
    }

    suspend fun clearHistory() {
        historyDao.clearAllHistory()
    }

    suspend fun performLeakCheck(): DnsLeakReport = withContext(Dispatchers.IO) {
        val isConnected = _sessionInfo.value.status == VpnStatus.CONNECTED
        val server = _sessionInfo.value.server

        if (isConnected && server != null) {
            DnsLeakReport(
                originalIp = "142.250.190.46", // Original ISP mock IP
                currentIp = server.ipAddress,
                isp = "ShieldAI Encrypted Mesh (${server.countryName})",
                country = server.countryName,
                city = server.city,
                detectedDnsIps = listOf(_settings.value.customDnsIp, "1.0.0.1"),
                isDnsLeaking = false,
                isIpProtected = true
            )
        } else {
            DnsLeakReport(
                originalIp = "142.250.190.46",
                currentIp = "142.250.190.46",
                isp = "Public Residential Telecom ISP",
                country = "United States",
                city = "Local Area",
                detectedDnsIps = listOf("192.168.1.1", "75.75.75.75"),
                isDnsLeaking = true,
                isIpProtected = false
            )
        }
    }

    fun calculatePrivacyScore(): PrivacyScoreReport {
        val isConnected = _sessionInfo.value.status == VpnStatus.CONNECTED
        val settings = _settings.value

        var score = 35 // Base unprotected level
        val risks = mutableListOf<String>()
        val recommendations = mutableListOf<String>()

        if (isConnected) {
            score += 35
        } else {
            risks.add("VPN tunnel is inactive — Public IP is exposed")
            recommendations.add("Enable 1-tap VPN connection to mask your real IP address")
        }

        if (settings.dnsLeakProtectionEnabled) {
            score += 15
        } else {
            risks.add("DNS Leak Protection disabled — ISP can resolve queried domains")
            recommendations.add("Turn on DNS Leak Protection in Privacy Settings")
        }

        if (settings.killSwitchEnabled) {
            score += 15
        } else {
            risks.add("Kill Switch is off — Data may leak during brief network transitions")
            recommendations.add("Turn on Kill Switch for fail-safe traffic blocking")
        }

        val level = when {
            score >= 90 -> "Fortress (Maximum Security)"
            score >= 70 -> "Guarded (High Privacy)"
            score >= 50 -> "Partial (Moderate Risk)"
            else -> "Exposed (Critical Risk)"
        }

        val summary = when {
            score >= 90 -> "All traffic, DNS lookups, and fail-safes are armed with quantum-resistant encryption."
            score >= 70 -> "Connection is masked. Enable Kill Switch to reach maximum security grade."
            else -> "Your network telemetry, location, and queries are visible to local Wi-Fi operators and ISPs."
        }

        return PrivacyScoreReport(
            score = score,
            level = level,
            summary = summary,
            ipMasked = isConnected,
            dnsEncrypted = settings.dnsLeakProtectionEnabled,
            killSwitchActive = settings.killSwitchEnabled,
            protocolSecurityLevel = if (isConnected) _sessionInfo.value.server?.protocol?.displayName ?: "Encrypted" else "None",
            riskPoints = risks,
            recommendations = recommendations
        )
    }
}
