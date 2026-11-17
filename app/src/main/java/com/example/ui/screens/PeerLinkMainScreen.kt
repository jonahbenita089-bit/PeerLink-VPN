package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.data.ConnectionStage
import com.example.data.PeerLinkUiState
import com.example.ui.components.ClientModeCard
import com.example.ui.components.ConnectionSequenceCard
import com.example.ui.components.CyberPowerButton
import com.example.ui.components.CyberRoleTabSelector
import com.example.ui.components.CyberTopBar
import com.example.ui.components.HostModeCard
import com.example.ui.components.LiveStatsCard
import com.example.ui.components.ProminentDisconnectButton
import com.example.ui.components.StatusHeader
import com.example.ui.dialogs.SettingsDialog
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberBackgroundGradientEnd
import kotlinx.coroutines.launch

@Composable
fun PeerLinkMainScreen(
    uiState: PeerLinkUiState,
    onTabSelected: (String) -> Unit,
    onTargetHostIdChange: (String) -> Unit,
    onPowerButtonClick: () -> Unit,
    onStartHostingClick: () -> Unit,
    onConnectClientClick: () -> Unit,
    onDisconnectClick: () -> Unit,
    onPasteDemoClick: () -> Unit,
    onRegenerateHostIdClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onSettingsDismiss: () -> Unit,
    onKillSwitchChange: (Boolean) -> Unit,
    onDnsChange: (String) -> Unit,
    onBypassLocalChange: (Boolean) -> Unit,
    onCopyHostIdSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Listen for copy feedback
    LaunchedEffect(uiState.copyFeedbackTrigger) {
        if (uiState.copyFeedbackTrigger > 0) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("PeerLink Host ID", uiState.hostId)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Host ID copied to clipboard!", Toast.LENGTH_SHORT).show()
            scope.launch {
                snackbarHostState.showSnackbar("Host ID ${uiState.hostId} copied to clipboard")
            }
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = CyberBackground,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            CyberBackground,
                            CyberBackgroundGradientEnd
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Bar: Logo, status & settings icon
                CyberTopBar(
                    isConnected = uiState.isConnected,
                    role = uiState.role,
                    onSettingsClick = onSettingsClick
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Central Status Header (e.g. "Disconnected", "Hosting Connection", "Authenticating...")
                StatusHeader(
                    status = uiState.statusMessage,
                    role = uiState.role,
                    isConnected = uiState.isConnected,
                    stage = uiState.connectionStage,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Large Glowing Central Power Button
                CyberPowerButton(
                    isConnected = uiState.isConnected,
                    stage = uiState.connectionStage,
                    role = uiState.role,
                    onClick = onPowerButtonClick
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Main Content Container: Tabs / Cards / Live Stats
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val isHandshake = uiState.connectionStage == ConnectionStage.RESOLVING_PEER ||
                            uiState.connectionStage == ConnectionStage.AUTHENTICATING ||
                            uiState.connectionStage == ConnectionStage.ESTABLISHING_TUNNEL

                    // 1. If connecting/handshaking: show Animated Connection Sequence
                    AnimatedVisibility(
                        visible = isHandshake,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        ConnectionSequenceCard(
                            stage = uiState.connectionStage,
                            stepDescription = uiState.stepDescription
                        )
                    }

                    // 2. If connected: show Live Stats and Disconnect Button
                    AnimatedVisibility(
                        visible = uiState.isConnected,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column {
                            LiveStatsCard(uiState = uiState)

                            Spacer(modifier = Modifier.height(18.dp))

                            // Prominently displayed Disconnect Button
                            ProminentDisconnectButton(
                                role = uiState.role,
                                onDisconnectClick = onDisconnectClick
                            )
                        }
                    }

                    // 3. If disconnected & not handshaking: show Tab Selector and Host / Client Mode UI
                    AnimatedVisibility(
                        visible = !uiState.isConnected && !isHandshake,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column {
                            // Mode Selector: Host Mode vs Client Mode
                            CyberRoleTabSelector(
                                activeTab = uiState.activeRoleTab,
                                isConnected = uiState.isConnected,
                                onTabSelected = onTabSelected
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            if (uiState.activeRoleTab == "host") {
                                // Host Mode UI
                                HostModeCard(
                                    hostId = uiState.hostId,
                                    isConnected = uiState.isConnected,
                                    onCopyClick = onCopyHostIdSuccess,
                                    onRegenerateClick = onRegenerateHostIdClick,
                                    onStartHostingClick = onStartHostingClick
                                )
                            } else {
                                // Client Mode UI
                                ClientModeCard(
                                    targetHostId = uiState.clientTargetHostId,
                                    isConnected = uiState.isConnected,
                                    errorMessage = uiState.errorMessage,
                                    onHostIdChange = onTargetHostIdChange,
                                    onPasteDemoClick = onPasteDemoClick,
                                    onConnectClick = onConnectClientClick
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Settings Modal Dialog
    if (uiState.showSettings) {
        SettingsDialog(
            killSwitchEnabled = uiState.killSwitchEnabled,
            selectedDns = uiState.selectedDns,
            bypassLocalTraffic = uiState.bypassLocalTraffic,
            onKillSwitchChange = onKillSwitchChange,
            onDnsChange = onDnsChange,
            onBypassLocalChange = onBypassLocalChange,
            onDismiss = onSettingsDismiss
        )
    }
}
