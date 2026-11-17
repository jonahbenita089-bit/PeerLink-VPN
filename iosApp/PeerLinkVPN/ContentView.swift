import SwiftUI

struct ContentView: View {
    @StateObject private var viewModel = PeerLinkViewModel()
    @State private var copiedAlert = false
    @State private var rotationAngle: Double = 0

    var body: some View {
        ZStack {
            // Background Cyber Gradient
            LinearGradient(
                colors: [.cyberBackground, .cyberBackgroundEnd],
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()

            VStack(spacing: 0) {
                // MARK: - Top Bar
                topBarView
                    .padding(.horizontal, 20)
                    .padding(.top, 10)

                ScrollView(showsIndicators: false) {
                    VStack(spacing: 24) {
                        // Status Header Text
                        statusHeaderView
                            .padding(.top, 16)

                        // Central Glowing Power Button
                        powerButtonView

                        // Handshake Progress / Live Stats / Mode Cards
                        VStack(spacing: 16) {
                            if isHandshaking {
                                connectionSequenceView
                            }

                            if viewModel.isConnected {
                                liveStatsView
                                disconnectButtonView
                            }

                            if !viewModel.isConnected && !isHandshaking {
                                roleTabsView

                                if viewModel.activeTab == "host" {
                                    hostModeCardView
                                } else {
                                    clientModeCardView
                                }
                            }
                        }
                        .padding(.horizontal, 20)
                        .padding(.bottom, 32)
                    }
                }
            }
        }
        .sheet(isPresented: $viewModel.showSettings) {
            settingsSheetView
        }
        .alert("Copied!", isPresented: $copiedAlert) {
            Button("OK", role: .cancel) { }
        } message: {
            Text("Host ID \(viewModel.hostId) copied to clipboard.")
        }
    }

    private var isHandshaking: Bool {
        viewModel.connectionStage == .resolvingPeer ||
        viewModel.connectionStage == .authenticating ||
        viewModel.connectionStage == .establishingTunnel
    }

    // MARK: - Top Bar View
    private var topBarView: some View {
        HStack {
            HStack(spacing: 12) {
                ZStack {
                    RoundedRectangle(cornerRadius: 10)
                        .fill(Color.cyberSurfaceVariant)
                        .frame(width: 40, height: 40)
                        .overlay(
                            RoundedRectangle(cornerRadius: 10)
                                .stroke(Color.neonCyan.opacity(0.5), lineWidth: 1)
                        )
                    Image(systemName: "shield.checkered")
                        .font(.system(size: 20))
                        .foregroundColor(.neonCyan)
                }

                VStack(alignment: .leading, spacing: 2) {
                    HStack(spacing: 6) {
                        Text("PeerLink")
                            .font(.system(size: 20, weight: .heavy))
                            .foregroundColor(.textPrimary)

                        Text("iOS")
                            .font(.system(size: 9, weight: .bold, design: .monospaced))
                            .foregroundColor(.neonCyan)
                            .padding(.horizontal, 6)
                            .padding(.vertical, 2)
                            .background(Color.neonCyan.opacity(0.15))
                            .cornerRadius(4)
                            .overlay(
                                RoundedRectangle(cornerRadius: 4)
                                    .stroke(Color.neonCyan.opacity(0.4), lineWidth: 1)
                            )
                    }

                    Text(viewModel.isConnected ? "SECURE TUNNEL ACTIVE" : "P2P ENCRYPTED (CROSS-PLATFORM)")
                        .font(.system(size: 9, weight: .semibold, design: .monospaced))
                        .foregroundColor(viewModel.isConnected ? .neonGreen : .textMuted)
                }
            }

            Spacer()

            Button(action: { viewModel.showSettings = true }) {
                ZStack {
                    Circle()
                        .fill(Color.cyberSurfaceVariant)
                        .frame(width: 44, height: 44)
                        .overlay(Circle().stroke(Color.cyberBorder, lineWidth: 1))
                    Image(systemName: "gearshape.fill")
                        .font(.system(size: 18))
                        .foregroundColor(.textSecondary)
                }
            }
        }
    }

    // MARK: - Status Header
    private var statusHeaderView: some View {
        VStack(spacing: 10) {
            HStack(spacing: 8) {
                Circle()
                    .fill(statusIndicatorColor)
                    .frame(width: 8, height: 8)

                Text(statusBadgeTitle)
                    .font(.system(size: 11, weight: .bold, design: .monospaced))
                    .foregroundColor(statusIndicatorColor)
            }
            .padding(.horizontal, 14)
            .padding(.vertical, 6)
            .background(Color.cyberSurfaceVariant.opacity(0.7))
            .cornerRadius(16)
            .overlay(
                RoundedRectangle(cornerRadius: 16)
                    .stroke(statusIndicatorColor.opacity(0.4), lineWidth: 1)
            )

            Text(viewModel.statusMessage)
                .font(.system(size: 24, weight: .bold))
                .foregroundColor(.textPrimary)
        }
    }

    private var statusIndicatorColor: Color {
        if isHandshaking { return .neonAmber }
        if viewModel.isConnected && viewModel.role == "host" { return .neonCyan }
        if viewModel.isConnected { return .neonGreen }
        return .textSecondary
    }

    private var statusBadgeTitle: String {
        if viewModel.isConnected && viewModel.role == "host" { return "HOSTING ACTIVE" }
        if viewModel.isConnected { return "CONNECTED" }
        if isHandshaking { return "HANDSHAKE IN PROGRESS" }
        return "DISCONNECTED"
    }

    // MARK: - Central Glowing Power Button
    private var powerButtonView: some View {
        VStack(spacing: 12) {
            ZStack {
                // Ambient Glow
                Circle()
                    .fill(
                        RadialGradient(
                            colors: [
                                statusIndicatorColor.opacity(viewModel.isConnected || isHandshaking ? 0.35 : 0.12),
                                .clear
                            ],
                            center: .center,
                            startRadius: 20,
                            endRadius: 90
                        )
                    )
                    .frame(width: 170, height: 170)

                // Outer border
                Circle()
                    .stroke(statusIndicatorColor.opacity(viewModel.isConnected ? 0.8 : 0.3), lineWidth: 2)
                    .frame(width: 156, height: 156)

                // Rotating telemetry ring
                Circle()
                    .trim(from: 0, to: isHandshaking ? 0.45 : 0.25)
                    .stroke(statusIndicatorColor, style: StrokeStyle(lineWidth: 3, lineCap: .round))
                    .frame(width: 156, height: 156)
                    .rotationEffect(.degrees(rotationAngle))
                    .onAppear {
                        withAnimation(.linear(duration: 4).repeatForever(autoreverses: false)) {
                            rotationAngle = 360
                        }
                    }

                // Inner Button Surface
                Button(action: { viewModel.togglePower() }) {
                    ZStack {
                        Circle()
                            .fill(
                                LinearGradient(
                                    colors: [.cyberSurfaceVariant, .cyberSurface, .cyberBackground],
                                    startPoint: .top,
                                    endPoint: .bottom
                                )
                            )
                            .frame(width: 118, height: 118)
                            .overlay(
                                Circle()
                                    .stroke(
                                        LinearGradient(
                                            colors: [statusIndicatorColor.opacity(0.9), statusIndicatorColor.opacity(0.3)],
                                            startPoint: .topLeading,
                                            endPoint: .bottomTrailing
                                        ),
                                        lineWidth: 2
                                    )
                            )

                        Image(systemName: "power")
                            .font(.system(size: 46, weight: .bold))
                            .foregroundColor(statusIndicatorColor)
                    }
                }
                .buttonStyle(PlainButtonStyle())
            }

            Text(powerPromptText)
                .font(.system(size: 11, weight: .bold, design: .monospaced))
                .foregroundColor(viewModel.isConnected ? statusIndicatorColor : .textMuted)
        }
    }

    private var powerPromptText: String {
        if isHandshaking { return "CONNECTING..." }
        if viewModel.isConnected && viewModel.role == "host" { return "TAP TO STOP HOSTING" }
        if viewModel.isConnected { return "TAP TO DISCONNECT" }
        if viewModel.activeTab == "host" { return "TAP TO START HOSTING" }
        return "TAP TO CONNECT"
    }

    // MARK: - Role Tab Selector
    private var roleTabsView: some View {
        HStack(spacing: 6) {
            Button(action: { viewModel.activeTab = "host" }) {
                HStack {
                    Image(systemName: "laptopcomputer.and.iphone")
                    Text("Host Mode")
                }
                .font(.system(size: 14, weight: viewModel.activeTab == "host" ? .bold : .medium))
                .foregroundColor(viewModel.activeTab == "host" ? .textPrimary : .textMuted)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 12)
                .background(viewModel.activeTab == "host" ? Color.cyberSurfaceHighlight : Color.clear)
                .cornerRadius(10)
                .overlay(
                    RoundedRectangle(cornerRadius: 10)
                        .stroke(viewModel.activeTab == "host" ? Color.neonCyan.opacity(0.7) : Color.clear, lineWidth: 1)
                )
            }

            Button(action: { viewModel.activeTab = "client" }) {
                HStack {
                    Image(systemName: "link")
                    Text("Client Mode")
                }
                .font(.system(size: 14, weight: viewModel.activeTab == "client" ? .bold : .medium))
                .foregroundColor(viewModel.activeTab == "client" ? .textPrimary : .textMuted)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 12)
                .background(viewModel.activeTab == "client" ? Color.cyberSurfaceHighlight : Color.clear)
                .cornerRadius(10)
                .overlay(
                    RoundedRectangle(cornerRadius: 10)
                        .stroke(viewModel.activeTab == "client" ? Color.neonGreen.opacity(0.7) : Color.clear, lineWidth: 1)
                )
            }
        }
        .padding(4)
        .background(Color.cyberSurface)
        .cornerRadius(14)
        .overlay(RoundedRectangle(cornerRadius: 14).stroke(Color.cyberBorder, lineWidth: 1))
    }

    // MARK: - Host Mode Card
    private var hostModeCardView: some View {
        VStack(alignment: .leading, spacing: 14) {
            HStack {
                HStack(spacing: 8) {
                    Image(systemName: "antenna.radiowaves.left.and.right")
                        .foregroundColor(.neonCyan)
                    Text("YOUR HOST ID")
                        .font(.system(size: 12, weight: .bold, design: .monospaced))
                        .foregroundColor(.neonCyan)
                }
                Spacer()
                Button(action: { viewModel.regenerateHostId() }) {
                    Image(systemName: "arrow.clockwise")
                        .font(.system(size: 14))
                        .foregroundColor(.textMuted)
                }
            }

            HStack {
                Text(viewModel.hostId)
                    .font(.system(size: 22, weight: .black, design: .monospaced))
                    .foregroundColor(.textPrimary)
                    .tracking(2)

                Spacer()

                Button(action: {
                    UIPasteboard.general.string = viewModel.hostId
                    copiedAlert = true
                }) {
                    HStack(spacing: 6) {
                        Image(systemName: "doc.on.doc")
                        Text("Copy")
                    }
                    .font(.system(size: 12, weight: .bold))
                    .foregroundColor(.neonCyan)
                    .padding(.horizontal, 12)
                    .padding(.vertical, 8)
                    .background(Color.cyberSurfaceVariant)
                    .cornerRadius(8)
                }
            }
            .padding(14)
            .background(Color.cyberBackground)
            .cornerRadius(12)
            .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color.cyberBorderSubtle, lineWidth: 1))

            Text("Share this 9-character Host ID with any Android or iOS device to allow them to tunnel their internet through your connection.")
                .font(.system(size: 12))
                .foregroundColor(.textSecondary)
                .lineSpacing(4)

            Button(action: { viewModel.startHosting() }) {
                Text("START HOSTING CONNECTION")
                    .font(.system(size: 13, weight: .bold))
                    .foregroundColor(.cyberBackground)
                    .frame(maxWidth: .infinity)
                    .frame(height: 48)
                    .background(Color.neonCyan)
                    .cornerRadius(12)
            }
        }
        .padding(18)
        .background(Color.cyberSurface)
        .cornerRadius(18)
        .overlay(RoundedRectangle(cornerRadius: 18).stroke(Color.cyberBorder, lineWidth: 1))
    }

    // MARK: - Client Mode Card
    private var clientModeCardView: some View {
        VStack(alignment: .leading, spacing: 14) {
            HStack {
                HStack(spacing: 8) {
                    Image(systemName: "link")
                        .foregroundColor(.neonGreen)
                    Text("REMOTE HOST ID")
                        .font(.system(size: 12, weight: .bold, design: .monospaced))
                        .foregroundColor(.neonGreen)
                }
                Spacer()
                Button(action: { viewModel.updateTargetHostId("X7Y-9PL-2QZ") }) {
                    Text("Fill Demo ID")
                        .font(.system(size: 11, weight: .semibold))
                        .foregroundColor(.neonCyan)
                        .padding(.horizontal, 8)
                        .padding(.vertical, 4)
                        .background(Color.cyberSurfaceVariant)
                        .cornerRadius(6)
                }
            }

            TextField("XXX-XXX-XXX", text: Binding(
                get: { viewModel.clientTargetHostId },
                set: { viewModel.updateTargetHostId($0) }
            ))
            .font(.system(size: 20, weight: .bold, design: .monospaced))
            .foregroundColor(.textPrimary)
            .padding(14)
            .background(Color.cyberBackground)
            .cornerRadius(12)
            .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color.cyberBorder, lineWidth: 1))
            .autocapitalization(.allCharacters)
            .disableAutocorrection(true)

            if let error = viewModel.errorMessage {
                Text(error)
                    .font(.system(size: 12))
                    .foregroundColor(.neonRed)
            }

            Text("Enter the 9-character Host ID of any Android or iOS device to securely route your internet through their peer tunnel.")
                .font(.system(size: 12))
                .foregroundColor(.textSecondary)
                .lineSpacing(4)

            Button(action: { viewModel.connectAsClient() }) {
                HStack(spacing: 8) {
                    Image(systemName: "link")
                    Text("CONNECT TO HOST")
                }
                .font(.system(size: 13, weight: .bold))
                .foregroundColor(.cyberBackground)
                .frame(maxWidth: .infinity)
                .frame(height: 48)
                .background(Color.neonGreen)
                .cornerRadius(12)
            }
        }
        .padding(18)
        .background(Color.cyberSurface)
        .cornerRadius(18)
        .overlay(RoundedRectangle(cornerRadius: 18).stroke(Color.cyberBorder, lineWidth: 1))
    }

    // MARK: - Connection Sequence Card
    private var connectionSequenceView: some View {
        VStack(spacing: 14) {
            HStack(spacing: 10) {
                ProgressView()
                    .progressViewStyle(CircularProgressViewStyle(tint: .neonAmber))
                Text("ESTABLISHING PEER-TO-PEER TUNNEL")
                    .font(.system(size: 12, weight: .bold, design: .monospaced))
                    .foregroundColor(.neonAmber)
            }

            VStack(spacing: 8) {
                stepRow(number: "01", title: "Resolving Peer Address", isDone: viewModel.connectionStage != .resolvingPeer)
                stepRow(number: "02", title: "Authenticating with Host", isDone: viewModel.connectionStage == .establishingTunnel || viewModel.connectionStage == .connected)
                stepRow(number: "03", title: "Establishing Secure Tunnel", isDone: viewModel.connectionStage == .connected)
            }

            if !viewModel.stepDescription.isEmpty {
                Text(viewModel.stepDescription)
                    .font(.system(size: 11, design: .monospaced))
                    .foregroundColor(.textMuted)
            }
        }
        .padding(18)
        .background(Color.cyberSurface)
        .cornerRadius(18)
        .overlay(RoundedRectangle(cornerRadius: 18).stroke(Color.neonAmber.opacity(0.6), lineWidth: 1))
    }

    private func stepRow(number: String, title: String, isDone: Bool) -> some View {
        HStack {
            Image(systemName: isDone ? "checkmark.circle.fill" : "circle")
                .foregroundColor(isDone ? .neonGreen : .textMuted)
            Text(title)
                .font(.system(size: 13, weight: isDone ? .semibold : .regular))
                .foregroundColor(isDone ? .textPrimary : .textMuted)
            Spacer()
        }
        .padding(8)
    }

    // MARK: - Live Stats Card
    private var liveStatsView: some View {
        let isHost = viewModel.role == "host"
        let accent = isHost ? Color.neonCyan : Color.neonGreen

        return VStack(spacing: 16) {
            HStack {
                HStack(spacing: 8) {
                    Image(systemName: "gauge.with.needle")
                        .foregroundColor(accent)
                    Text(isHost ? "LIVE HOSTING METRICS" : "LIVE TUNNEL METRICS")
                        .font(.system(size: 12, weight: .bold, design: .monospaced))
                        .foregroundColor(accent)
                }
                Spacer()
                Text(isHost ? "1 CLIENT CONNECTED" : "\(viewModel.pingMs) MS LATENCY")
                    .font(.system(size: 10, weight: .bold, design: .monospaced))
                    .foregroundColor(accent)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 4)
                    .background(Color.cyberSurfaceVariant)
                    .cornerRadius(6)
            }

            HStack(spacing: 12) {
                statBox(title: "SESSION TIME", value: viewModel.formattedTimer, icon: "hourglass", tint: accent)
                statBox(title: "TRANSFERRED", value: viewModel.formattedDataTransferred, icon: "arrow.up.arrow.down", tint: accent)
            }

            HStack {
                HStack(spacing: 10) {
                    Image(systemName: "arrow.down.circle.fill")
                        .foregroundColor(.neonGreen)
                        .font(.system(size: 24))
                    VStack(alignment: .leading, spacing: 2) {
                        Text("DOWNLOAD")
                            .font(.system(size: 9, weight: .bold, design: .monospaced))
                            .foregroundColor(.textMuted)
                        Text(String(format: "%.1f Mbps", viewModel.downloadSpeedMbps))
                            .font(.system(size: 14, weight: .bold, design: .monospaced))
                            .foregroundColor(.textPrimary)
                    }
                }
                Spacer()
                HStack(spacing: 10) {
                    Image(systemName: "arrow.up.circle.fill")
                        .foregroundColor(.neonCyan)
                        .font(.system(size: 24))
                    VStack(alignment: .leading, spacing: 2) {
                        Text("UPLOAD")
                            .font(.system(size: 9, weight: .bold, design: .monospaced))
                            .foregroundColor(.textMuted)
                        Text(String(format: "%.1f Mbps", viewModel.uploadSpeedMbps))
                            .font(.system(size: 14, weight: .bold, design: .monospaced))
                            .foregroundColor(.textPrimary)
                    }
                }
            }
            .padding(12)
            .background(Color.cyberBackground)
            .cornerRadius(12)
            .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color.cyberBorderSubtle, lineWidth: 1))

            HStack {
                Text(isHost ? "Gateway: 10.8.0.1" : "Tunneled IP: 10.8.0.2")
                    .font(.system(size: 11, design: .monospaced))
                    .foregroundColor(.textMuted)
                Spacer()
                Text("ChaCha20-Poly1305")
                    .font(.system(size: 11, design: .monospaced))
                    .foregroundColor(.textMuted)
            }
        }
        .padding(18)
        .background(Color.cyberSurface)
        .cornerRadius(18)
        .overlay(RoundedRectangle(cornerRadius: 18).stroke(accent.opacity(0.5), lineWidth: 1))
    }

    private func statBox(title: String, value: String, icon: String, tint: Color) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            HStack(spacing: 6) {
                Image(systemName: icon)
                    .foregroundColor(tint)
                    .font(.system(size: 12))
                Text(title)
                    .font(.system(size: 9, weight: .bold, design: .monospaced))
                    .foregroundColor(.textMuted)
            }
            Text(value)
                .font(.system(size: 18, weight: .heavy, design: .monospaced))
                .foregroundColor(.textPrimary)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(12)
        .background(Color.cyberBackground)
        .cornerRadius(12)
        .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color.cyberBorderSubtle, lineWidth: 1))
    }

    // MARK: - Disconnect Button
    private var disconnectButtonView: some View {
        Button(action: { viewModel.disconnect() }) {
            HStack(spacing: 8) {
                Image(systemName: "power")
                Text(viewModel.role == "host" ? "STOP HOSTING CONNECTION" : "DISCONNECT VPN TUNNEL")
            }
            .font(.system(size: 13, weight: .bold))
            .foregroundColor(.neonRed)
            .frame(maxWidth: .infinity)
            .frame(height: 52)
            .background(Color.neonRed.opacity(0.15))
            .cornerRadius(14)
            .overlay(RoundedRectangle(cornerRadius: 14).stroke(Color.neonRed, lineWidth: 1.5))
        }
    }

    // MARK: - Settings Sheet
    private var settingsSheetView: some View {
        NavigationView {
            ZStack {
                Color.cyberSurface.ignoresSafeArea()

                ScrollView {
                    VStack(alignment: .leading, spacing: 20) {
                        Toggle(isOn: $viewModel.killSwitchEnabled) {
                            VStack(alignment: .leading, spacing: 4) {
                                Text("Kill Switch")
                                    .font(.system(size: 15, weight: .semibold))
                                    .foregroundColor(.textPrimary)
                                Text("Block non-tunnel traffic if the peer connection drops unexpectedly.")
                                    .font(.system(size: 12))
                                    .foregroundColor(.textSecondary)
                            }
                        }
                        .tint(.neonCyan)

                        Divider().background(Color.cyberBorder)

                        Toggle(isOn: $viewModel.bypassLocalTraffic) {
                            VStack(alignment: .leading, spacing: 4) {
                                Text("Bypass Local Network")
                                    .font(.system(size: 15, weight: .semibold))
                                    .foregroundColor(.textPrimary)
                                Text("Permit local LAN traffic (AirPlay, local printers).")
                                    .font(.system(size: 12))
                                    .foregroundColor(.textSecondary)
                            }
                        }
                        .tint(.neonCyan)

                        Divider().background(Color.cyberBorder)

                        VStack(alignment: .leading, spacing: 8) {
                            Text("CROSS-PLATFORM ARCHITECTURE")
                                .font(.system(size: 11, weight: .bold, design: .monospaced))
                                .foregroundColor(.neonGreen)

                            Text("• iOS Implementation: Apple NetworkExtension (NEPacketTunnelProvider)\n• Android Implementation: android.net.VpnService (TUN Builder)\n• Cross-Peer Protocol: ChaCha20-Poly1305 over WebSockets / WebRTC DataChannel\n• Virtual Network: 10.8.0.0/24 subnet with MTU 1500")
                                .font(.system(size: 11, design: .monospaced))
                                .foregroundColor(.textSecondary)
                                .lineSpacing(4)
                                .padding(12)
                                .frame(maxWidth: .infinity, alignment: .leading)
                                .background(Color.cyberBackground)
                                .cornerRadius(10)
                        }
                    }
                    .padding(20)
                }
            }
            .navigationTitle("VPN Settings")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Done") { viewModel.showSettings = false }
                        .foregroundColor(.neonCyan)
                }
            }
        }
    }
}
