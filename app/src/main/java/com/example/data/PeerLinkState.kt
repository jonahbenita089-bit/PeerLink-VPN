package com.example.data

/**
 * Core AppState matching the exact requirement:
 * AppState: { role: 'idle' | 'host' | 'client', isConnected: boolean, hostId: String, connectionTimeSeconds: Int, dataTransferredMB: Float }
 */
data class AppState(
    val role: String = "idle", // 'idle' | 'host' | 'client'
    val isConnected: Boolean = false,
    val hostId: String = "",
    val connectionTimeSeconds: Int = 0,
    val dataTransferredMB: Float = 0.0f
)

/**
 * Granular connection lifecycle steps for the simulated authentication/handshake.
 */
enum class ConnectionStage {
    IDLE,
    RESOLVING_PEER,
    AUTHENTICATING,
    ESTABLISHING_TUNNEL,
    CONNECTED
}

/**
 * Rich UI State for the cyber-themed PeerLink VPN interface.
 */
data class PeerLinkUiState(
    val appState: AppState = AppState(),
    // Convenience delegations for direct reactive access
    val role: String = "idle", // 'idle' | 'host' | 'client'
    val isConnected: Boolean = false,
    val hostId: String = "",
    val connectionTimeSeconds: Int = 0,
    val dataTransferredMB: Float = 0.0f,

    // Client Mode Target Input
    val clientTargetHostId: String = "",
    val connectionStage: ConnectionStage = ConnectionStage.IDLE,
    val statusMessage: String = "Disconnected",
    val stepDescription: String = "",

    // Live Metrics
    val connectedClientsCount: Int = 0,
    val uploadSpeedMbps: Float = 0.0f,
    val downloadSpeedMbps: Float = 0.0f,
    val pingMs: Int = 0,
    val virtualIp: String = "10.8.0.2",
    val gatewayIp: String = "10.8.0.1",

    // UI Navigation & Preferences
    val activeRoleTab: String = "host", // "host" or "client" selector
    val showSettings: Boolean = false,
    val killSwitchEnabled: Boolean = true,
    val selectedDns: String = "Cloudflare (1.1.1.1)",
    val bypassLocalTraffic: Boolean = true,
    val errorMessage: String? = null,
    val copyFeedbackTrigger: Long = 0L
) {
    val formattedTimer: String
        get() {
            val hours = connectionTimeSeconds / 3600
            val minutes = (connectionTimeSeconds % 3600) / 60
            val seconds = connectionTimeSeconds % 60
            return if (hours > 0) {
                String.format("%02d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format("%02d:%02d", minutes, seconds)
            }
        }

    val formattedDataTransferred: String
        get() {
            return if (dataTransferredMB >= 1024f) {
                String.format("%.2f GB", dataTransferredMB / 1024f)
            } else {
                String.format("%.1f MB", dataTransferredMB)
            }
        }
}

/**
 * Utility to generate a 9-character Host ID formatted as XXX-XXX-XXX.
 * Example: "X7Y-9PL-2QZ"
 */
object HostIdGenerator {
    private val CHAR_POOL = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ".toCharArray()

    fun generate(): String {
        val part1 = (1..3).map { CHAR_POOL.random() }.joinToString("")
        val part2 = (1..3).map { CHAR_POOL.random() }.joinToString("")
        val part3 = (1..3).map { CHAR_POOL.random() }.joinToString("")
        return "$part1-$part2-$part3"
    }

    fun cleanInput(raw: String): String {
        val alphanumeric = raw.uppercase().filter { it.isLetterOrDigit() }.take(9)
        return when {
            alphanumeric.length > 6 -> "${alphanumeric.substring(0, 3)}-${alphanumeric.substring(3, 6)}-${alphanumeric.substring(6)}"
            alphanumeric.length > 3 -> "${alphanumeric.substring(0, 3)}-${alphanumeric.substring(3)}"
            else -> alphanumeric
        }
    }

    fun isValid(id: String): Boolean {
        val cleaned = id.filter { it.isLetterOrDigit() }
        return cleaned.length == 9
    }
}
