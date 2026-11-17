package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.HostIdGenerator
import com.example.data.PeerLinkRepository
import com.example.data.PeerLinkUiState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the PeerLink VPN application.
 * Exposes reactive StateFlow to Jetpack Compose UI and delegates actions to PeerLinkRepository.
 */
class PeerLinkViewModel : ViewModel() {

    val uiState: StateFlow<PeerLinkUiState> = PeerLinkRepository.uiState

    fun selectRoleTab(tab: String) {
        PeerLinkRepository.selectRoleTab(tab)
    }

    fun updateTargetHostId(input: String) {
        PeerLinkRepository.updateClientTargetHostId(input)
    }

    fun regenerateHostId() {
        PeerLinkRepository.regenerateHostId()
    }

    fun startHosting(context: Context) {
        PeerLinkRepository.startHosting(context)
    }

    fun connectAsClient(context: Context) {
        PeerLinkRepository.connectAsClient(context)
    }

    fun disconnect(context: Context) {
        PeerLinkRepository.disconnect(context)
    }

    /**
     * Primary action triggered by the central glowing power button.
     */
    fun togglePowerButton(context: Context) {
        val state = uiState.value
        if (state.isConnected) {
            disconnect(context)
        } else {
            if (state.activeRoleTab == "host") {
                startHosting(context)
            } else {
                if (HostIdGenerator.isValid(state.clientTargetHostId)) {
                    connectAsClient(context)
                } else {
                    // If empty, supply a demo Host ID for smooth testing
                    if (state.clientTargetHostId.isEmpty()) {
                        PeerLinkRepository.updateClientTargetHostId("X7Y-9PL-2QZ")
                    }
                    connectAsClient(context)
                }
            }
        }
    }

    fun pasteDemoHostId() {
        PeerLinkRepository.updateClientTargetHostId("X7Y-9PL-2QZ")
    }

    fun toggleSettings(show: Boolean) {
        PeerLinkRepository.toggleSettings(show)
    }

    fun setKillSwitch(enabled: Boolean) {
        PeerLinkRepository.setKillSwitch(enabled)
    }

    fun setSelectedDns(dns: String) {
        PeerLinkRepository.setDns(dns)
    }

    fun setBypassLocal(bypass: Boolean) {
        PeerLinkRepository.setBypassLocal(bypass)
    }

    fun triggerCopyFeedback() {
        PeerLinkRepository.triggerCopyFeedback()
    }
}
