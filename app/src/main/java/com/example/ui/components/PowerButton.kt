package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ConnectionStage
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import com.example.ui.theme.TextMuted

/**
 * Large glowing cyber power button with multi-layer animated rings and glowing pulse aura.
 */
@Composable
fun CyberPowerButton(
    isConnected: Boolean,
    stage: ConnectionStage,
    role: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isConnecting = stage == ConnectionStage.RESOLVING_PEER ||
            stage == ConnectionStage.AUTHENTICATING ||
            stage == ConnectionStage.ESTABLISHING_TUNNEL

    val primaryGlowColor = when {
        isConnecting -> NeonAmber
        isConnected && role == "host" -> NeonCyan
        isConnected -> NeonGreen
        else -> NeonCyan
    }

    val infiniteTransition = rememberInfiniteTransition(label = "power_pulse")

    // Breathing pulse scale animation
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = if (isConnecting) 1.15f else if (isConnected) 1.08f else 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isConnecting) 600 else 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    // Rotating cyber ring angle
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isConnecting) 1200 else 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring_rotation"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(170.dp)
                .testTag("central_power_button")
                .clip(CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true, color = primaryGlowColor),
                    onClick = onClick
                )
        ) {
            // Background Canvas: Glowing radial aura & segmented animated cyber arcs
            Canvas(modifier = Modifier.size(170.dp)) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val baseRadius = size.width / 2f

                // Outer ambient glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            primaryGlowColor.copy(alpha = if (isConnected || isConnecting) 0.35f else 0.12f),
                            primaryGlowColor.copy(alpha = 0.05f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = baseRadius * pulseScale
                    )
                )

                // Outer cyber dashed border
                drawCircle(
                    color = primaryGlowColor.copy(alpha = if (isConnected) 0.8f else 0.3f),
                    radius = baseRadius - 6.dp.toPx(),
                    style = Stroke(
                        width = 2.dp.toPx()
                    )
                )

                // Rotating dynamic telemetry arc
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            primaryGlowColor.copy(alpha = 0.1f),
                            primaryGlowColor,
                            primaryGlowColor.copy(alpha = 0.2f)
                        )
                    ),
                    startAngle = rotationAngle,
                    sweepAngle = if (isConnecting) 160f else 90f,
                    useCenter = false,
                    topLeft = Offset(10.dp.toPx(), 10.dp.toPx()),
                    size = androidx.compose.ui.geometry.Size(
                        size.width - 20.dp.toPx(),
                        size.height - 20.dp.toPx()
                    ),
                    style = Stroke(
                        width = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                )
            }

            // Inner button surface
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(118.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                CyberSurfaceVariant,
                                CyberSurface,
                                CyberBackground
                            )
                        )
                    )
                    .border(
                        width = 2.dp,
                        brush = Brush.linearGradient(
                            listOf(
                                primaryGlowColor.copy(alpha = 0.9f),
                                primaryGlowColor.copy(alpha = 0.3f)
                            )
                        ),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Rounded.PowerSettingsNew,
                    contentDescription = if (isConnected) "Disconnect VPN" else "Activate VPN",
                    tint = primaryGlowColor,
                    modifier = Modifier.size(54.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Subtext indication under button
        val promptText = when {
            isConnecting -> "CONNECTING..."
            isConnected && role == "host" -> "TAP TO STOP HOSTING"
            isConnected -> "TAP TO DISCONNECT"
            role == "host" -> "TAP TO START HOSTING"
            else -> "TAP TO CONNECT"
        }

        Text(
            text = promptText,
            color = if (isConnected) primaryGlowColor else TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.8.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}
