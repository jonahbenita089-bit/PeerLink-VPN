package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ConnectionStage
import com.example.data.PeerLinkUiState
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberBorderSubtle
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceHighlight
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.GlowCyan
import com.example.ui.theme.GlowGreen
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Top Bar with Cyber logo, status indicator, and settings icon.
 */
@Composable
fun CyberTopBar(
    isConnected: Boolean,
    role: String,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // App Logo and Brand Title
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(CyberSurfaceVariant)
                    .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "PeerLink Logo",
                    tint = NeonCyan,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "PeerLink",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isConnected) GlowGreen else GlowCyan)
                            .border(
                                1.dp,
                                if (isConnected) NeonGreen.copy(alpha = 0.6f) else NeonCyan.copy(alpha = 0.6f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "VPN",
                            color = if (isConnected) NeonGreen else NeonCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Text(
                    text = if (isConnected) "SECURE TUNNEL ACTIVE" else "P2P ENCRYPTED • ANDROID & iOS",
                    color = if (isConnected) NeonGreen else TextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.8.sp
                )
            }
        }

        // Settings Action Button
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier
                .testTag("settings_button")
                .size(44.dp)
                .clip(CircleShape)
                .background(CyberSurfaceVariant)
                .border(1.dp, CyberBorder, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Large Status Header Text (e.g., "Disconnected", "Hosting Connection", "Authenticating...").
 */
@Composable
fun StatusHeader(
    status: String,
    role: String,
    isConnected: Boolean,
    stage: ConnectionStage,
    modifier: Modifier = Modifier
) {
    val statusColor = when {
        stage == ConnectionStage.AUTHENTICATING || stage == ConnectionStage.ESTABLISHING_TUNNEL -> NeonAmber
        isConnected && role == "host" -> NeonCyan
        isConnected -> NeonGreen
        else -> TextSecondary
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Glowing status indicator pill
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(CyberSurfaceVariant.copy(alpha = 0.7f))
                .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = when {
                    isConnected && role == "host" -> "HOSTING ACTIVE"
                    isConnected -> "CONNECTED"
                    stage != ConnectionStage.IDLE -> "HANDSHAKE IN PROGRESS"
                    else -> "DISCONNECTED"
                },
                color = statusColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.2.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Large status text
        Text(
            text = status,
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Segmented Tab Selector to switch between Host Mode and Client Mode.
 */
@Composable
fun CyberRoleTabSelector(
    activeTab: String,
    isConnected: Boolean,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CyberSurface)
            .border(1.dp, CyberBorder, RoundedCornerShape(14.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Host Mode Tab
        val isHostSelected = activeTab == "host"
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .weight(1f)
                .testTag("tab_host")
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (isHostSelected) CyberSurfaceHighlight else Color.Transparent
                )
                .border(
                    width = if (isHostSelected) 1.dp else 0.dp,
                    color = if (isHostSelected) NeonCyan.copy(alpha = 0.7f) else Color.Transparent,
                    shape = RoundedCornerShape(10.dp)
                )
                .clickable(enabled = !isConnected) {
                    onTabSelected("host")
                }
                .padding(vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Devices,
                    contentDescription = null,
                    tint = if (isHostSelected) NeonCyan else TextMuted,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Host Mode",
                    color = if (isHostSelected) TextPrimary else TextMuted,
                    fontSize = 14.sp,
                    fontWeight = if (isHostSelected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }

        // Client Mode Tab
        val isClientSelected = activeTab == "client"
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .weight(1f)
                .testTag("tab_client")
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (isClientSelected) CyberSurfaceHighlight else Color.Transparent
                )
                .border(
                    width = if (isClientSelected) 1.dp else 0.dp,
                    color = if (isClientSelected) NeonGreen.copy(alpha = 0.7f) else Color.Transparent,
                    shape = RoundedCornerShape(10.dp)
                )
                .clickable(enabled = !isConnected) {
                    onTabSelected("client")
                }
                .padding(vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Link,
                    contentDescription = null,
                    tint = if (isClientSelected) NeonGreen else TextMuted,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Client Mode",
                    color = if (isClientSelected) TextPrimary else TextMuted,
                    fontSize = 14.sp,
                    fontWeight = if (isClientSelected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Host Mode UI Card:
 * Displays generated 9-character Host ID with Copy to Clipboard and Regenerate actions.
 */
@Composable
fun HostModeCard(
    hostId: String,
    isConnected: Boolean,
    onCopyClick: () -> Unit,
    onRegenerateClick: () -> Unit,
    onStartHostingClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CyberSurface)
            .border(1.dp, CyberBorder, RoundedCornerShape(18.dp))
            .padding(18.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lan,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "YOUR HOST ID",
                        color = NeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                }

                if (!isConnected) {
                    IconButton(
                        onClick = onRegenerateClick,
                        modifier = Modifier
                            .testTag("regenerate_host_id_button")
                            .size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Regenerate Host ID",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Code container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CyberBackground)
                    .border(1.dp, CyberBorderSubtle, RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = hostId,
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 3.sp
                    )

                    Button(
                        onClick = onCopyClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberSurfaceVariant,
                            contentColor = NeonCyan
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("copy_host_id_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy to Clipboard",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Copy",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Share this 9-character Host ID with any Android or iOS device. When they connect, their traffic is securely tunneled through your connection.",
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )

            if (!isConnected) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onStartHostingClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonCyan,
                        contentColor = CyberBackground
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("start_hosting_button")
                        .height(48.dp)
                ) {
                    Text(
                        text = "START HOSTING CONNECTION",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 0.8.sp
                    )
                }
            }
        }
    }
}

/**
 * Client Mode UI Card:
 * Input field for entering Host ID, Demo ID button, and Connect button.
 */
@Composable
fun ClientModeCard(
    targetHostId: String,
    isConnected: Boolean,
    errorMessage: String?,
    onHostIdChange: (String) -> Unit,
    onPasteDemoClick: () -> Unit,
    onConnectClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CyberSurface)
            .border(1.dp, CyberBorder, RoundedCornerShape(18.dp))
            .padding(18.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = null,
                        tint = NeonGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "REMOTE HOST ID",
                        color = NeonGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                }

                if (!isConnected) {
                    OutlinedButton(
                        onClick = onPasteDemoClick,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("paste_demo_button")
                    ) {
                        Text(
                            text = "Fill Demo ID",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Text input field
            OutlinedTextField(
                value = targetHostId,
                onValueChange = onHostIdChange,
                enabled = !isConnected,
                placeholder = {
                    Text(
                        text = "XXX-XXX-XXX",
                        color = TextMuted,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        onConnectClick()
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    disabledTextColor = TextSecondary,
                    focusedBorderColor = NeonGreen,
                    unfocusedBorderColor = CyberBorder,
                    disabledBorderColor = CyberBorderSubtle,
                    focusedContainerColor = CyberBackground,
                    unfocusedContainerColor = CyberBackground,
                    disabledContainerColor = CyberSurfaceVariant
                ),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 20.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("host_id_input")
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = errorMessage,
                    color = NeonRed,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Enter the 9-character Host ID provided by any Android or iOS peer to connect and route your traffic through their internet tunnel.",
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )

            if (!isConnected) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        onConnectClick()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonGreen,
                        contentColor = CyberBackground
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("connect_button")
                        .height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CONNECT TO HOST",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 0.8.sp
                    )
                }
            }
        }
    }
}

/**
 * Animated Connection Sequence Card:
 * Displays simulated stages ("Authenticating...", "Establishing secure tunnel...").
 */
@Composable
fun ConnectionSequenceCard(
    stage: ConnectionStage,
    stepDescription: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CyberSurface)
            .border(1.dp, NeonAmber.copy(alpha = 0.7f), RoundedCornerShape(18.dp))
            .padding(18.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    color = NeonAmber,
                    strokeWidth = 2.5.dp,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "ESTABLISHING PEER-TO-PEER TUNNEL",
                    color = NeonAmber,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Step 1: Resolving Peer
            StepItem(
                stepNumber = "01",
                title = "Resolving Peer Address",
                isActive = stage == ConnectionStage.RESOLVING_PEER,
                isDone = stage.ordinal > ConnectionStage.RESOLVING_PEER.ordinal
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Step 2: Authenticating
            StepItem(
                stepNumber = "02",
                title = "Authenticating with Host",
                isActive = stage == ConnectionStage.AUTHENTICATING,
                isDone = stage.ordinal > ConnectionStage.AUTHENTICATING.ordinal
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Step 3: Establishing Tunnel
            StepItem(
                stepNumber = "03",
                title = "Establishing Secure Tunnel",
                isActive = stage == ConnectionStage.ESTABLISHING_TUNNEL,
                isDone = stage.ordinal > ConnectionStage.ESTABLISHING_TUNNEL.ordinal
            )

            if (stepDescription.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = stepDescription,
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun StepItem(
    stepNumber: String,
    title: String,
    isActive: Boolean,
    isDone: Boolean
) {
    val color = when {
        isDone -> NeonGreen
        isActive -> NeonAmber
        else -> TextMuted
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isActive) CyberSurfaceVariant else Color.Transparent)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        if (isDone) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Done",
                tint = NeonGreen,
                modifier = Modifier.size(16.dp)
            )
        } else if (isActive) {
            CircularProgressIndicator(
                color = NeonAmber,
                strokeWidth = 2.dp,
                modifier = Modifier.size(16.dp)
            )
        } else {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .border(1.dp, TextMuted, CircleShape)
            ) {
                Text(
                    text = stepNumber,
                    color = TextMuted,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = title,
            color = color,
            fontSize = 13.sp,
            fontWeight = if (isActive || isDone) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

/**
 * Live Stats Card displayed when connected.
 * Shows: Connected Clients (1 client in host mode), Data Transferred, Live Timer, and Live Speed rates.
 */
@Composable
fun LiveStatsCard(
    uiState: PeerLinkUiState,
    modifier: Modifier = Modifier
) {
    val isHost = uiState.role == "host"
    val accentColor = if (isHost) NeonCyan else NeonGreen

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CyberSurface)
            .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
            .padding(18.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isHost) "LIVE HOSTING METRICS" else "LIVE TUNNEL METRICS",
                        color = accentColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                }

                // Ping or peer count badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(CyberSurfaceVariant)
                        .border(1.dp, CyberBorder, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isHost) "1 CLIENT CONNECTED" else "${uiState.pingMs} MS LATENCY",
                        color = if (isHost) NeonCyan else NeonGreen,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Primary Stats Grid: 2 columns
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Live Timer Card
                StatMetricBox(
                    title = "SESSION TIME",
                    value = uiState.formattedTimer,
                    icon = Icons.Default.HourglassEmpty,
                    iconTint = accentColor,
                    modifier = Modifier.weight(1f)
                )

                // Data Transferred Card
                StatMetricBox(
                    title = "TRANSFERRED",
                    value = uiState.formattedDataTransferred,
                    icon = Icons.Default.Lan,
                    iconTint = accentColor,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Speed Rates: Download & Upload
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CyberBackground)
                    .border(1.dp, CyberBorderSubtle, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Download Speed
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(GlowGreen)
                            .border(1.dp, NeonGreen.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowDownward,
                            contentDescription = "Download",
                            tint = NeonGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "DOWNLOAD",
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = String.format("%.1f Mbps", uiState.downloadSpeedMbps),
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Upload Speed
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(GlowCyan)
                            .border(1.dp, NeonCyan.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowUpward,
                            contentDescription = "Upload",
                            tint = NeonCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "UPLOAD",
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = String.format("%.1f Mbps", uiState.uploadSpeedMbps),
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Subnet connection detail badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isHost) "Gateway: ${uiState.virtualIp}" else "Tunneled IP: ${uiState.virtualIp}",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "ChaCha20-Poly1305",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun StatMetricBox(
    title: String,
    value: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CyberBackground)
            .border(1.dp, CyberBorderSubtle, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                color = TextPrimary,
                fontSize = 18.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

/**
 * Prominent Disconnect Button displayed whenever connected.
 */
@Composable
fun ProminentDisconnectButton(
    role: String,
    onDisconnectClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onDisconnectClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = NeonRed.copy(alpha = 0.15f),
            contentColor = NeonRed
        ),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, NeonRed),
        modifier = modifier
            .fillMaxWidth()
            .testTag("disconnect_button")
            .height(52.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.PowerSettingsNew,
            contentDescription = null,
            tint = NeonRed,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = if (role == "host") "STOP HOSTING CONNECTION" else "DISCONNECT VPN TUNNEL",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            letterSpacing = 1.sp
        )
    }
}
