package com.example.data

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.PeerLinkVpnService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * PeerLinkRepository serves as the single source of truth for VPN connection state,
 * coordination between UI and PeerLinkVpnService, and background coroutine metrics simulation.
 */
object PeerLinkRepository {

    private const val TAG = "PeerLinkRepository"
    private val scope = CoroutineScope(Dispatchers.Default)

    private val initialHostId = HostIdGenerator.generate()

    private val _uiState = MutableStateFlow(
        PeerLinkUiState(
            appState = AppState(
                role = "idle",
                isConnected = false,
                hostId = initialHostId,
                connectionTimeSeconds = 0,
                dataTransferredMB = 0f
            ),
            role = "idle",
            isConnected = false,
            hostId = initialHostId,
            connectionTimeSeconds = 0,
            dataTransferredMB = 0f,
            activeRoleTab = "host",
            statusMessage = "Disconnected"
        )
    )
    val uiState: StateFlow<PeerLinkUiState> = _uiState.asStateFlow()

    private var metricsJob: Job? = null
    private var connectionSequenceJob: Job? = null

    /**
     * Updates active tab (Host mode or Client mode).
     */
    fun selectRoleTab(tab: String) {
        if (_uiState.value.isConnected) return
        _uiState.update { it.copy(activeRoleTab = tab, errorMessage = null) }
    }

    /**
     * Updates the target Host ID entered by the client.
     */
    fun updateClientTargetHostId(rawInput: String) {
        val formatted = HostIdGenerator.cleanInput(rawInput)
        _uiState.update {
            it.copy(
                clientTargetHostId = formatted,
                errorMessage = null
            )
        }
    }

    /**
     * Regenerates a new 9-character Host ID for this device.
     */
    fun regenerateHostId() {
        if (_uiState.value.isConnected) return
        val newId = HostIdGenerator.generate()
        _uiState.update { current ->
            current.copy(
                hostId = newId,
                appState = current.appState.copy(hostId = newId)
            )
        }
    }

    /**
     * Starts Host Mode: shares internet connection with remote peers.
     */
    fun startHosting(context: Context) {
        if (_uiState.value.isConnected) return

        connectionSequenceJob?.cancel()
        metricsJob?.cancel()

        _uiState.update { current ->
            current.copy(
                role = "host",
                isConnected = true,
                connectionStage = ConnectionStage.CONNECTED,
                statusMessage = "Hosting Connection",
                connectedClientsCount = 1,
                connectionTimeSeconds = 0,
                dataTransferredMB = 0.0f,
                pingMs = 18,
                virtualIp = "10.8.0.1",
                gatewayIp = "0.0.0.0",
                appState = current.appState.copy(
                    role = "host",
                    isConnected = true,
                    connectionTimeSeconds = 0,
                    dataTransferredMB = 0.0f
                ),
                errorMessage = null
            )
        }

        // Start VpnService in HOST mode
        startNativeVpnService(context, role = "host", hostId = _uiState.value.hostId)

        // Launch Coroutine metrics ticker
        startLiveMetricsSimulation(isHost = true)
    }

    /**
     * Connects as Client to a remote Host ID with simulated connection handshake sequence.
     */
    fun connectAsClient(context: Context) {
        val targetId = _uiState.value.clientTargetHostId
        if (!HostIdGenerator.isValid(targetId)) {
            _uiState.update {
                it.copy(errorMessage = "Please enter a valid 9-character Host ID (e.g. X7Y-9PL-2QZ)")
            }
            return
        }

        if (_uiState.value.isConnected) return

        connectionSequenceJob?.cancel()
        metricsJob?.cancel()

        connectionSequenceJob = scope.launch {
            try {
                // Step 1: Resolving Peer
                _uiState.update {
                    it.copy(
                        role = "client",
                        isConnected = false,
                        connectionStage = ConnectionStage.RESOLVING_PEER,
                        statusMessage = "Resolving Peer...",
                        stepDescription = "Locating peer node for Host ID $targetId",
                        errorMessage = null
                    )
                }
                delay(600)

                // Step 2: Authenticating
                _uiState.update {
                    it.copy(
                        connectionStage = ConnectionStage.AUTHENTICATING,
                        statusMessage = "Authenticating...",
                        stepDescription = "Verifying cryptographic credentials with peer"
                    )
                }
                delay(750)

                // Step 3: Establishing secure tunnel
                _uiState.update {
                    it.copy(
                        connectionStage = ConnectionStage.ESTABLISHING_TUNNEL,
                        statusMessage = "Establishing secure tunnel...",
                        stepDescription = "Negotiating ChaCha20 cipher & virtual IP interface"
                    )
                }
                delay(800)

                // Step 4: Connected!
                _uiState.update { current ->
                    current.copy(
                        role = "client",
                        isConnected = true,
                        connectionStage = ConnectionStage.CONNECTED,
                        statusMessage = "Connected to Host",
                        stepDescription = "Tunnel active via P2P relay",
                        connectionTimeSeconds = 0,
                        dataTransferredMB = 0.0f,
                        pingMs = 24,
                        virtualIp = "10.8.0.2",
                        gatewayIp = "10.8.0.1",
                        appState = current.appState.copy(
                            role = "client",
                            isConnected = true,
                            connectionTimeSeconds = 0,
                            dataTransferredMB = 0.0f
                        )
                    )
                }

                // Start native VpnService in client mode
                startNativeVpnService(context, role = "client", hostId = targetId)

                // Start metrics simulation ticker
                startLiveMetricsSimulation(isHost = false)

            } catch (e: Exception) {
                Log.e(TAG, "Connection handshake interrupted", e)
                disconnect(context)
            }
        }
    }

    /**
     * Disconnects active VPN session (Host or Client).
     */
    fun disconnect(context: Context? = null) {
        connectionSequenceJob?.cancel()
        connectionSequenceJob = null

        metricsJob?.cancel()
        metricsJob = null

        _uiState.update { current ->
            current.copy(
                role = "idle",
                isConnected = false,
                connectionStage = ConnectionStage.IDLE,
                statusMessage = "Disconnected",
                stepDescription = "",
                uploadSpeedMbps = 0f,
                downloadSpeedMbps = 0f,
                pingMs = 0,
                connectedClientsCount = 0,
                appState = current.appState.copy(
                    role = "idle",
                    isConnected = false
                )
            )
        }

        context?.let { ctx ->
            stopNativeVpnService(ctx)
        }
    }

    /**
     * Coroutine to simulate live connection timers and realistic data transfer rates.
     */
    private fun startLiveMetricsSimulation(isHost: Boolean) {
        metricsJob?.cancel()
        metricsJob = scope.launch {
            var totalMB = 0.0f
            var seconds = 0

            while (isActive) {
                delay(1000)
                seconds++

                // Realistic network fluctuation
                val downSpeed = if (isHost) {
                    Random.nextDouble(6.0, 18.0).toFloat()
                } else {
                    Random.nextDouble(14.0, 38.5).toFloat()
                }

                val upSpeed = if (isHost) {
                    Random.nextDouble(18.0, 48.0).toFloat()
                } else {
                    Random.nextDouble(3.0, 9.5).toFloat()
                }

                // Convert Mbps to MB transferred in 1 second: (Mbps / 8) * 1 sec
                val secondTransferMB = ((downSpeed + upSpeed) / 8f) * Random.nextDouble(0.7, 1.2).toFloat()
                totalMB += secondTransferMB

                val jitterPing = if (isHost) {
                    Random.nextInt(15, 22)
                } else {
                    Random.nextInt(21, 32)
                }

                _uiState.update { current ->
                    if (!current.isConnected) current
                    else {
                        current.copy(
                            connectionTimeSeconds = seconds,
                            dataTransferredMB = totalMB,
                            downloadSpeedMbps = downSpeed,
                            uploadSpeedMbps = upSpeed,
                            pingMs = jitterPing,
                            appState = current.appState.copy(
                                connectionTimeSeconds = seconds,
                                dataTransferredMB = totalMB
                            )
                        )
                    }
                }
            }
        }
    }

    private fun startNativeVpnService(context: Context, role: String, hostId: String) {
        try {
            val intent = Intent(context, PeerLinkVpnService::class.java).apply {
                action = PeerLinkVpnService.ACTION_CONNECT
                putExtra(PeerLinkVpnService.EXTRA_ROLE, role)
                putExtra(PeerLinkVpnService.EXTRA_HOST_ID, hostId)
            }
            context.startForegroundService(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start PeerLinkVpnService", e)
        }
    }

    private fun stopNativeVpnService(context: Context) {
        try {
            val intent = Intent(context, PeerLinkVpnService::class.java).apply {
                action = PeerLinkVpnService.ACTION_DISCONNECT
            }
            context.startService(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop PeerLinkVpnService", e)
        }
    }

    fun toggleSettings(show: Boolean) {
        _uiState.update { it.copy(showSettings = show) }
    }

    fun setKillSwitch(enabled: Boolean) {
        _uiState.update { it.copy(killSwitchEnabled = enabled) }
    }

    fun setDns(dns: String) {
        _uiState.update { it.copy(selectedDns = dns) }
    }

    fun setBypassLocal(bypass: Boolean) {
        _uiState.update { it.copy(bypassLocalTraffic = bypass) }
    }

    fun triggerCopyFeedback() {
        _uiState.update { it.copy(copyFeedbackTrigger = System.currentTimeMillis()) }
    }
}
