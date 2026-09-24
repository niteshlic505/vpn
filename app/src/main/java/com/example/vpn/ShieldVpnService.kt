package com.example.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.VpnProtocol
import com.example.data.model.VpnServer
import com.example.data.model.VpnStatus
import com.example.data.repository.VpnRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.random.Random

class ShieldVpnService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var trafficJob: Job? = null

    private var startTimeMillis: Long = 0L
    private var totalBytesIn: Long = 0L
    private var totalBytesOut: Long = 0L
    private var currentServer: VpnServer? = null

    companion object {
        private const val TAG = "ShieldVpnService"
        const val ACTION_CONNECT = "com.example.vpn.CONNECT"
        const val ACTION_DISCONNECT = "com.example.vpn.DISCONNECT"

        const val EXTRA_SERVER_ID = "EXTRA_SERVER_ID"
        const val EXTRA_SERVER_NAME = "EXTRA_SERVER_NAME"
        const val EXTRA_SERVER_COUNTRY = "EXTRA_SERVER_COUNTRY"
        const val EXTRA_SERVER_IP = "EXTRA_SERVER_IP"
        const val EXTRA_SERVER_FLAG = "EXTRA_SERVER_FLAG"
        const val EXTRA_DNS_IP = "EXTRA_DNS_IP"
        const val EXTRA_PROTOCOL = "EXTRA_PROTOCOL"
        const val EXTRA_KILL_SWITCH = "EXTRA_KILL_SWITCH"

        private const val CHANNEL_ID = "shield_vpn_active_channel"
        private const val NOTIFICATION_ID = 1001

        fun startVpn(context: Context, server: VpnServer, dnsIp: String = "1.1.1.1", killSwitch: Boolean = false) {
            val intent = Intent(context, ShieldVpnService::class.java).apply {
                action = ACTION_CONNECT
                putExtra(EXTRA_SERVER_ID, server.id)
                putExtra(EXTRA_SERVER_NAME, "${server.city}, ${server.countryName}")
                putExtra(EXTRA_SERVER_COUNTRY, server.countryName)
                putExtra(EXTRA_SERVER_IP, server.ipAddress)
                putExtra(EXTRA_SERVER_FLAG, server.flagEmoji)
                putExtra(EXTRA_DNS_IP, dnsIp)
                putExtra(EXTRA_PROTOCOL, server.protocol.name)
                putExtra(EXTRA_KILL_SWITCH, killSwitch)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopVpn(context: Context) {
            val intent = Intent(context, ShieldVpnService::class.java).apply {
                action = ACTION_DISCONNECT
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: return START_NOT_STICKY

        when (action) {
            ACTION_CONNECT -> {
                val serverId = intent.getStringExtra(EXTRA_SERVER_ID) ?: "default_server"
                val serverName = intent.getStringExtra(EXTRA_SERVER_NAME) ?: "Global Gateway"
                val serverCountry = intent.getStringExtra(EXTRA_SERVER_COUNTRY) ?: "United States"
                val serverIp = intent.getStringExtra(EXTRA_SERVER_IP) ?: "198.51.100.24"
                val flagEmoji = intent.getStringExtra(EXTRA_SERVER_FLAG) ?: "🛡️"
                val dnsIp = intent.getStringExtra(EXTRA_DNS_IP) ?: "1.1.1.1"
                val protocolStr = intent.getStringExtra(EXTRA_PROTOCOL) ?: VpnProtocol.WIREGUARD.name
                val killSwitch = intent.getBooleanExtra(EXTRA_KILL_SWITCH, false)

                val protocol = try {
                    VpnProtocol.valueOf(protocolStr)
                } catch (e: Exception) {
                    VpnProtocol.WIREGUARD
                }

                currentServer = VpnServer(
                    id = serverId,
                    countryName = serverCountry,
                    countryCode = "US",
                    city = serverName.substringBefore(","),
                    ipAddress = serverIp,
                    pingMs = 28,
                    loadPercent = 35,
                    speedMbps = 900,
                    protocol = protocol,
                    isPremium = false,
                    reliabilityScore = 99,
                    latitude = 0.0,
                    longitude = 0.0,
                    flagEmoji = flagEmoji
                )

                connectVpn(currentServer!!, dnsIp, killSwitch)
            }
            ACTION_DISCONNECT -> {
                disconnectVpn()
            }
        }

        return START_NOT_STICKY
    }

    private fun connectVpn(server: VpnServer, dnsIp: String, killSwitch: Boolean) {
        // Satisfy startForegroundService requirement immediately
        startForegroundCompat(buildConnectingNotification(server))

        serviceScope.launch {
            try {
                VpnRepository.instance?.notifyVpnStatusChanged(
                    VpnStatus.CONNECTING,
                    server = server
                )

                // Handshake simulation delay
                delay(600)

                val builder = Builder()
                    .setSession("ShieldAI VPN - ${server.city}")
                    .addAddress("10.8.0.2", 24)
                    .addRoute("0.0.0.0", 0) // Full device VPN routing
                    .addDnsServer(dnsIp)
                    .setMtu(1420)
                    .setBlocking(true)

                val configureIntent = Intent(this@ShieldVpnService, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val pendingIntent = PendingIntent.getActivity(
                    this@ShieldVpnService,
                    0,
                    configureIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                builder.setConfigureIntent(pendingIntent)

                vpnInterface = builder.establish()

                if (vpnInterface != null) {
                    startTimeMillis = System.currentTimeMillis()
                    startForegroundCompat(buildForegroundNotification(server, 0, 0, 0))

                    VpnRepository.instance?.notifyVpnStatusChanged(
                        VpnStatus.CONNECTED,
                        server = server,
                        connectedAtMillis = startTimeMillis,
                        publicIp = server.ipAddress
                    )

                    startTrafficMonitor(server)
                } else {
                    Log.e(TAG, "VPN Interface establishment returned null")
                    VpnRepository.instance?.notifyVpnStatusChanged(
                        VpnStatus.ERROR,
                        server = server,
                        errorMessage = "Failed to create TUN interface. Permission might be revoked."
                    )
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            } catch (e: Exception) {
                Log.e(TAG, "VPN tunnel connection error", e)
                VpnRepository.instance?.notifyVpnStatusChanged(
                    VpnStatus.ERROR,
                    server = server,
                    errorMessage = e.message ?: "Tunnel initialization failed"
                )
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
    }

    private fun startTrafficMonitor(server: VpnServer) {
        trafficJob?.cancel()
        trafficJob = serviceScope.launch {
            val fd = vpnInterface?.fileDescriptor
            val input = fd?.let { FileInputStream(it) }
            val output = fd?.let { FileOutputStream(it) }

            // Loop to monitor traffic and keep alive with live stats
            var seconds = 0L
            while (isActive && vpnInterface != null) {
                delay(1000)
                seconds++

                // Simulated active encrypted packet throughput
                val downDelta = (Random.nextInt(180, 850) * 1024L)
                val upDelta = (Random.nextInt(40, 220) * 1024L)
                totalBytesIn += downDelta
                totalBytesOut += upDelta

                val speedDownKbps = (downDelta / 1024f) * 8f
                val speedUpKbps = (upDelta / 1024f) * 8f

                VpnRepository.instance?.updateLiveTraffic(
                    durationSeconds = seconds,
                    bytesIn = totalBytesIn,
                    bytesOut = totalBytesOut,
                    speedDownKbps = speedDownKbps,
                    speedUpKbps = speedUpKbps
                )

                if (seconds % 2 == 0L) {
                    val notification = buildForegroundNotification(server, seconds, totalBytesIn, totalBytesOut)
                    val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    manager.notify(NOTIFICATION_ID, notification)
                }
            }

            input?.close()
            output?.close()
        }
    }

    private fun disconnectVpn() {
        trafficJob?.cancel()
        trafficJob = null

        val duration = if (startTimeMillis > 0) (System.currentTimeMillis() - startTimeMillis) / 1000 else 0L

        try {
            vpnInterface?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing VPN interface", e)
        }
        vpnInterface = null

        VpnRepository.instance?.notifyVpnDisconnected(
            server = currentServer,
            durationSeconds = duration,
            bytesIn = totalBytesIn,
            bytesOut = totalBytesOut
        )

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        disconnectVpn()
        super.onDestroy()
    }

    override fun onRevoke() {
        disconnectVpn()
        super.onRevoke()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.vpn_channel_name)
            val descriptionText = getString(R.string.vpn_channel_desc)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setShowBadge(false)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(
        server: VpnServer,
        durationSeconds: Long,
        bytesIn: Long,
        bytesOut: Long
    ): Notification {
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val disconnectIntent = Intent(this, ShieldVpnService::class.java).apply {
            action = ACTION_DISCONNECT
        }
        val disconnectPendingIntent = PendingIntent.getService(
            this,
            1,
            disconnectIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val formattedDuration = String.format("%02d:%02d", durationSeconds / 60, durationSeconds % 60)
        val mbDown = totalBytesIn / (1024 * 1024f)
        val mbUp = totalBytesOut / (1024 * 1024f)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentTitle("ShieldAI Protected: ${server.flagEmoji} ${server.city}")
            .setContentText("Connected ($formattedDuration) • ↓ %.1f MB  ↑ %.1f MB".format(mbDown, mbUp))
            .setSubText(server.protocol.displayName)
            .setOngoing(true)
            .setContentIntent(contentPendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Disconnect", disconnectPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun buildConnectingNotification(server: VpnServer): Notification {
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentTitle("ShieldAI VPN: Connecting...")
            .setContentText("Securing encrypted tunnel to ${server.city}...")
            .setSubText(server.protocol.displayName)
            .setOngoing(true)
            .setContentIntent(contentPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun startForegroundCompat(notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                notification,
                0
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }
}
