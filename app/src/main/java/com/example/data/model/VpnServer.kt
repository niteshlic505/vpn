package com.example.data.model

enum class VpnProtocol(val displayName: String, val isFullTunnel: Boolean, val description: String) {
    WIREGUARD("WireGuard®", true, "Fastest, state-of-the-art cryptography & lowest battery impact"),
    OPENVPN("OpenVPN UDP", true, "Industry gold standard, resilient against deep-packet inspection"),
    SOCKS5("SOCKS5 Proxy", false, "High-speed application routing without full system TUN encryption"),
    HTTP_PROXY("HTTP/HTTPS Proxy", false, "Standard proxy protocol for browser & web privacy")
}

data class VpnServer(
    val id: String,
    val countryName: String,
    val countryCode: String,
    val city: String,
    val ipAddress: String,
    var pingMs: Int,
    var loadPercent: Int,
    val speedMbps: Int,
    val protocol: VpnProtocol = VpnProtocol.WIREGUARD,
    val isPremium: Boolean = false,
    val reliabilityScore: Int = 99,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val flagEmoji: String = "🌐",
    val isOnline: Boolean = true
)

enum class VpnStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    DISCONNECTING,
    RECONNECTING,
    ERROR
}

data class VpnSessionInfo(
    val server: VpnServer? = null,
    val status: VpnStatus = VpnStatus.DISCONNECTED,
    val connectedAtMillis: Long = 0L,
    val durationSeconds: Long = 0L,
    val bytesIn: Long = 0L,
    val bytesOut: Long = 0L,
    val speedDownKbps: Float = 0f,
    val speedUpKbps: Float = 0f,
    val virtualIp: String = "10.8.0.2",
    val publicIp: String = "192.168.1.100",
    val errorMessage: String? = null
)

data class PrivacyScoreReport(
    val score: Int,
    val level: String,
    val summary: String,
    val ipMasked: Boolean,
    val dnsEncrypted: Boolean,
    val killSwitchActive: Boolean,
    val protocolSecurityLevel: String,
    val riskPoints: List<String>,
    val recommendations: List<String>
)

data class DnsLeakReport(
    val originalIp: String,
    val currentIp: String,
    val isp: String,
    val country: String,
    val city: String,
    val detectedDnsIps: List<String>,
    val isDnsLeaking: Boolean,
    val isIpProtected: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class AiChatMessage(
    val id: String,
    val sender: ChatSender,
    val message: String,
    val thinkingProcess: String? = null,
    val suggestedAction: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

enum class ChatSender {
    USER,
    ASSISTANT,
    SYSTEM
}
