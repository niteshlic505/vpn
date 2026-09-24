package com.example.data.model

data class UserLocation(
    val city: String,
    val country: String,
    val countryCode: String,
    val latitude: Double,
    val longitude: Double,
    val ip: String,
    val isp: String
) {
    companion object {
        val DEFAULT = UserLocation(
            city = "New York",
            country = "United States",
            countryCode = "US",
            latitude = 40.7128,
            longitude = -74.0060,
            ip = "142.250.190.46",
            isp = "Residential High-Speed Fiber"
        )

        val PRESET_LOCATIONS = listOf(
            DEFAULT,
            UserLocation("Los Angeles", "United States", "US", 34.0522, -118.2437, "198.51.100.12", "Pacific Broadband"),
            UserLocation("London", "United Kingdom", "GB", 51.5074, -0.1278, "185.120.44.1", "British Telecom Fiber"),
            UserLocation("Frankfurt", "Germany", "DE", 50.1109, 8.6821, "194.109.6.1", "Deutsche Telekom"),
            UserLocation("Tokyo", "Japan", "JP", 35.6762, 139.6503, "133.242.18.1", "NTT Communications"),
            UserLocation("Sydney", "Australia", "AU", -33.8688, 151.2093, "139.130.4.1", "Telstra Ultra")
        )
    }
}

data class SpeedTestMetric(
    val pingMs: Int,
    val jitterMs: Int,
    val downloadSpeedMbps: Float,
    val uploadSpeedMbps: Float,
    val packetLossPercent: Float = 0.0f,
    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        val DEFAULT = SpeedTestMetric(
            pingMs = 24,
            jitterMs = 2,
            downloadSpeedMbps = 245.8f,
            uploadSpeedMbps = 52.4f,
            packetLossPercent = 0.0f
        )
    }
}

enum class RecommendationGoal(
    val id: String,
    val displayName: String,
    val description: String,
    val iconName: String
) {
    BALANCED("balanced", "Best Overall", "Optimal mix of low latency, server headroom & security", "bolt"),
    GAMING("gaming", "Low-Ping Gaming", "Prioritizes minimal latency (ms), zero jitter & packet stability", "sports_esports"),
    STREAMING("streaming", "4K Streaming", "Maximum bandwidth throughput for UHD/4K buffer-free playback", "play_circle"),
    PRIVACY("privacy", "Maximum Stealth", "Jurisdiction-safe WireGuard nodes with anti-DPI tunneling", "shield")
}

data class RecommendationFactor(
    val title: String,
    val value: String,
    val explanation: String,
    val isFavorable: Boolean = true
)

data class AiServerRecommendation(
    val recommendedServer: VpnServer,
    val score: Int, // 0 - 100
    val calculatedDistanceKm: Int,
    val summaryTagline: String,
    val reasoning: String,
    val keyFactors: List<RecommendationFactor>,
    val runnerUpServers: List<Pair<VpnServer, String>> = emptyList(),
    val goal: RecommendationGoal = RecommendationGoal.BALANCED,
    val testedMetrics: SpeedTestMetric = SpeedTestMetric.DEFAULT,
    val userLocation: UserLocation = UserLocation.DEFAULT,
    val aiModelUsed: String = "Google Gemini 3.5 Flash",
    val thinkingProcess: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
