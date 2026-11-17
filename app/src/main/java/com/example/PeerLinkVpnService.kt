package com.example

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
import com.example.data.PeerLinkRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer

/**
 * PeerLinkVpnService
 *
 * Implements Android VpnService to establish a virtual network tunnel interface (TUN).
 * Handles the native Android VPN lifecycle, manages the persistent foreground service notification,
 * and configures IP routing, MTU, and DNS through VpnService.Builder.
 *
 * Architecture Notes:
 * In a full production deployment with a real backend relay server:
 * 1. An encrypted Peer-to-Peer relay connection (e.g., using WebRTC DataChannel, WireGuard over UDP,
 *    or a custom WebSocket / QUIC relay proxy) connects the Host and Client.
 * 2. Tunneled IP packets read from the TUN interface (via FileInputStream) are encrypted with
 *    ChaCha20-Poly1305 and forwarded across the relay socket.
 * 3. Incoming packets received from the remote peer socket are decrypted and written back into
 *    the TUN interface (via FileOutputStream).
 *
 * Here, we establish the real Android VpnService TUN interface and mock packet forwarding lifecycle
 * for testing and demonstration while maintaining proper service states.
 */
class PeerLinkVpnService : VpnService() {

    companion object {
        const val TAG = "PeerLinkVpnService"
        const val ACTION_CONNECT = "com.example.action.CONNECT"
        const val ACTION_DISCONNECT = "com.example.action.DISCONNECT"
        const val EXTRA_ROLE = "extra_role"
        const val EXTRA_HOST_ID = "extra_host_id"

        private const val NOTIFICATION_CHANNEL_ID = "peerlink_vpn_channel"
        private const val NOTIFICATION_ID = 1001

        // VPN Tunnel Configuration Constants
        private const val VPN_SESSION_NAME = "PeerLink VPN"
        private const val VPN_MTU = 1500
        private const val CLIENT_VIRTUAL_IP = "10.8.0.2"
        private const val HOST_VIRTUAL_IP = "10.8.0.1"
        private const val SUBNET_PREFIX_LENGTH = 24
        private const val PRIMARY_DNS = "1.1.1.1"
        private const val SECONDARY_DNS = "8.8.8.8"
    }

    private var vpnInterface: ParcelFileDescriptor? = null
    private var tunnelJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    private var currentRole: String = "client"
    private var currentHostId: String = ""

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        Log.d(TAG, "PeerLinkVpnService created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: return START_NOT_STICKY

        when (action) {
            ACTION_CONNECT -> {
                val role = intent.getStringExtra(EXTRA_ROLE) ?: "client"
                val hostId = intent.getStringExtra(EXTRA_HOST_ID) ?: ""
                handleConnect(role, hostId)
            }
            ACTION_DISCONNECT -> {
                handleDisconnect()
            }
        }

        return START_NOT_STICKY
    }

    /**
     * Initializes the VPN interface and launches the foreground notification.
     */
    private fun handleConnect(role: String, hostId: String) {
        currentRole = role
        currentHostId = hostId

        // 1. Start foreground service immediately with persistent notification
        val notification = buildForegroundNotification(role, hostId)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        // 2. Establish VPN TUN interface
        establishVpnInterface(role)

        // 3. Start packet processing loop (Relay Server Integration Hook)
        startPacketRelayLoop()

        Log.i(TAG, "PeerLink VPN connected in [$role] mode for Host ID: $hostId")
    }

    /**
     * Uses VpnService.Builder to establish the virtual network interface.
     */
    private fun establishVpnInterface(role: String) {
        try {
            vpnInterface?.close()

            val builder = Builder()
                .setSession(VPN_SESSION_NAME)
                .setMtu(VPN_MTU)

            if (role == "host") {
                // Host allocates the gateway virtual IP address
                builder.addAddress(HOST_VIRTUAL_IP, SUBNET_PREFIX_LENGTH)
                // In host mode, we can also route client subnet traffic
                builder.addRoute("10.8.0.0", SUBNET_PREFIX_LENGTH)
            } else {
                // Client connects with client virtual IP, routes internet traffic through tunnel
                builder.addAddress(CLIENT_VIRTUAL_IP, SUBNET_PREFIX_LENGTH)
                // Route all traffic through the PeerLink VPN tunnel
                builder.addRoute("0.0.0.0", 0)
                // Add secure DNS servers
                builder.addDnsServer(PRIMARY_DNS)
                builder.addDnsServer(SECONDARY_DNS)
            }

            // Exclude the PeerLink VPN app from its own tunnel to prevent loopbacks
            try {
                builder.addDisallowedApplication(packageName)
            } catch (e: Exception) {
                Log.w(TAG, "Could not add self to disallowed applications: ${e.message}")
            }

            builder.setBlocking(false)
            vpnInterface = builder.establish()

            if (vpnInterface != null) {
                Log.i(TAG, "VpnService TUN interface successfully established: fd=${vpnInterface?.fd}")
            } else {
                Log.e(TAG, "Failed to establish VpnService TUN interface (null descriptor)")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error configuring VpnService.Builder", e)
        }
    }

    /**
     * Packet processing coroutine loop.
     *
     * =========================================================================
     * BACKEND RELAY SERVER API INTEGRATION POINT:
     * =========================================================================
     * To connect to a live backend relay server (e.g. STUN/TURN, WebSockets, or
     * WireGuard protocol over UDP):
     *
     * 1. Initialize Relay Client:
     *    val relaySocket = RelayClient.connect(
     *        relayUrl = "wss://relay.peerlink.network/v1/tunnel",
     *        hostId = currentHostId,
     *        authToken = getAuthToken()
     *    )
     *
     * 2. Outgoing Traffic (App -> Remote Peer):
     *    - Read raw IP packets from `inStream` (FileInputStream of the TUN interface)
     *    - Wrap packet into protocol framing with peer encryption key (e.g., ChaCha20-Poly1305)
     *    - Send to `relaySocket.send(encryptedPacket)`
     *
     * 3. Incoming Traffic (Remote Peer -> App):
     *    - Receive packet bytes from `relaySocket.onPacketReceived`
     *    - Decrypt payload
     *    - Write raw IP packet to `outStream` (FileOutputStream of the TUN interface)
     * =========================================================================
     */
    private fun startPacketRelayLoop() {
        tunnelJob?.cancel()
        val pfd = vpnInterface ?: return

        tunnelJob = serviceScope.launch {
            try {
                val inStream = FileInputStream(pfd.fileDescriptor)
                val outStream = FileOutputStream(pfd.fileDescriptor)
                val packetBuffer = ByteBuffer.allocate(VPN_MTU)

                Log.d(TAG, "Started mock TUN packet relay worker")

                while (isActive) {
                    // In a live integration, non-blocking packet reads would happen here:
                    // val bytesRead = inStream.read(packetBuffer.array())
                    // if (bytesRead > 0) {
                    //     relayClient.forwardToPeer(packetBuffer.array(), 0, bytesRead)
                    // }

                    // Maintain coroutine health and check cancellation
                    kotlinx.coroutines.delay(200)
                }
            } catch (e: IOException) {
                Log.d(TAG, "TUN stream closed: ${e.message}")
            } catch (e: Exception) {
                Log.e(TAG, "Packet relay loop error", e)
            }
        }
    }

    /**
     * Gracefully stops the VPN tunnel and foreground service.
     */
    private fun handleDisconnect() {
        Log.i(TAG, "Stopping PeerLink VPN tunnel")

        tunnelJob?.cancel()
        tunnelJob = null

        try {
            vpnInterface?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing VPN interface", e)
        } finally {
            vpnInterface = null
        }

        // Notify repository to sync UI
        PeerLinkRepository.disconnect(null)

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        handleDisconnect()
        Log.d(TAG, "PeerLinkVpnService destroyed")
    }

    /**
     * Builds the persistent notification required for Android foreground services.
     */
    private fun buildForegroundNotification(role: String, hostId: String): Notification {
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val disconnectIntent = Intent(this, PeerLinkVpnService::class.java).apply {
            action = ACTION_DISCONNECT
        }
        val disconnectPendingIntent = PendingIntent.getService(
            this,
            1,
            disconnectIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentText = if (role == "host") {
            "Hosting internet connection for peers (Host ID: $hostId)"
        } else {
            "Connected to remote Host $hostId via secure tunnel"
        }

        val actionText = if (role == "host") "Stop Hosting" else "Disconnect"

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentTitle("PeerLink VPN is Active")
            .setContentText(contentText)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, actionText, disconnectPendingIntent)
            .build()
    }

    /**
     * Creates the Android notification channel for the foreground service.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "PeerLink VPN Status",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows live connection state of the PeerLink VPN service"
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}
