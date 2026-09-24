package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "connection_history")
data class ConnectionHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val serverId: String,
    val country: String,
    val city: String,
    val ipAddress: String,
    val protocol: String,
    val flagEmoji: String,
    val startTime: Long,
    val durationSeconds: Long,
    val bytesDownloaded: Long,
    val bytesUploaded: Long
)

@Entity(tableName = "favorite_servers")
data class FavoriteServerEntity(
    @PrimaryKey
    val serverId: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "privacy_settings")
data class PrivacySettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val killSwitchEnabled: Boolean = false,
    val dnsLeakProtectionEnabled: Boolean = true,
    val selectedDnsProvider: String = "Cloudflare (1.1.1.1)",
    val customDnsIp: String = "1.1.1.1",
    val autoReconnect: Boolean = true,
    val preferredProtocol: String = "WIREGUARD",
    val bypassLocalTraffic: Boolean = true,
    val highThinkingAiEnabled: Boolean = true
)
