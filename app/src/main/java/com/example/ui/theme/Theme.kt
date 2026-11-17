package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CyberColorScheme = darkColorScheme(
  primary = NeonCyan,
  onPrimary = CyberBackground,
  primaryContainer = CyberSurfaceVariant,
  onPrimaryContainer = NeonCyan,
  secondary = NeonGreen,
  onSecondary = CyberBackground,
  secondaryContainer = CyberSurfaceVariant,
  onSecondaryContainer = NeonGreen,
  tertiary = NeonAmber,
  onTertiary = CyberBackground,
  background = CyberBackground,
  onBackground = TextPrimary,
  surface = CyberSurface,
  onSurface = TextPrimary,
  surfaceVariant = CyberSurfaceVariant,
  onSurfaceVariant = TextSecondary,
  outline = CyberBorder,
  outlineVariant = CyberBorderSubtle,
  error = NeonRed,
  onError = TextPrimary
)

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = CyberColorScheme,
    typography = Typography,
    content = content
  )
}
