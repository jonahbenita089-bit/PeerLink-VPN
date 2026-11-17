package com.example

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.PeerLinkMainScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.PeerLinkViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: PeerLinkViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainAppContent(
                    activity = this,
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
private fun MainAppContent(
    activity: ComponentActivity,
    viewModel: PeerLinkViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var pendingVpnAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    // Launcher for standard Android VpnService permission dialog
    val vpnPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            pendingVpnAction?.invoke()
        } else {
            Toast.makeText(
                activity,
                "VPN permission is required to establish secure tunnels",
                Toast.LENGTH_LONG
            ).show()
        }
        pendingVpnAction = null
    }

    // Launcher for Android 13+ Notification permission
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* Notification permission result handled silently */ }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasNotificationPermission = ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasNotificationPermission) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    /**
     * Checks VpnService.prepare(context). If an Intent is returned, launches
     * the system VPN dialog; otherwise executes the action immediately.
     */
    fun executeWithVpnPermission(action: () -> Unit) {
        val vpnIntent = VpnService.prepare(activity)
        if (vpnIntent != null) {
            pendingVpnAction = action
            vpnPermissionLauncher.launch(vpnIntent)
        } else {
            action()
        }
    }

    PeerLinkMainScreen(
        uiState = uiState,
        onTabSelected = { tab ->
            viewModel.selectRoleTab(tab)
        },
        onTargetHostIdChange = { newId ->
            viewModel.updateTargetHostId(newId)
        },
        onPowerButtonClick = {
            if (uiState.isConnected) {
                viewModel.disconnect(activity)
            } else {
                executeWithVpnPermission {
                    viewModel.togglePowerButton(activity)
                }
            }
        },
        onStartHostingClick = {
            executeWithVpnPermission {
                viewModel.startHosting(activity)
            }
        },
        onConnectClientClick = {
            executeWithVpnPermission {
                viewModel.connectAsClient(activity)
            }
        },
        onDisconnectClick = {
            viewModel.disconnect(activity)
        },
        onPasteDemoClick = {
            viewModel.pasteDemoHostId()
        },
        onRegenerateHostIdClick = {
            viewModel.regenerateHostId()
        },
        onSettingsClick = {
            viewModel.toggleSettings(true)
        },
        onSettingsDismiss = {
            viewModel.toggleSettings(false)
        },
        onKillSwitchChange = { enabled ->
            viewModel.setKillSwitch(enabled)
        },
        onDnsChange = { dns ->
            viewModel.setSelectedDns(dns)
        },
        onBypassLocalChange = { bypass ->
            viewModel.setBypassLocal(bypass)
        },
        onCopyHostIdSuccess = {
            viewModel.triggerCopyFeedback()
        }
    )
}
