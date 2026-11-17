import Foundation
import Combine

enum ConnectionStage {
    case idle
    case resolvingPeer
    case authenticating
    case establishingTunnel
    case connected
}

class PeerLinkViewModel: ObservableObject {
    // MARK: - Core AppState
    @Published var role: String = "idle" // 'idle' | 'host' | 'client'
    @Published var isConnected: Bool = false
    @Published var hostId: String = ""
    @Published var connectionTimeSeconds: Int = 0
    @Published var dataTransferredMB: Float = 0.0

    // MARK: - Extended UI State
    @Published var activeTab: String = "host" // "host" or "client"
    @Published var clientTargetHostId: String = ""
    @Published var connectionStage: ConnectionStage = .idle
    @Published var statusMessage: String = "Disconnected"
    @Published var stepDescription: String = ""

    @Published var connectedClientsCount: Int = 0
    @Published var downloadSpeedMbps: Float = 0.0
    @Published var uploadSpeedMbps: Float = 0.0
    @Published var pingMs: Int = 0
    @Published var showSettings: Bool = false
    @Published var killSwitchEnabled: Bool = true
    @Published var selectedDns: String = "Cloudflare (1.1.1.1)"
    @Published var bypassLocalTraffic: Bool = true
    @Published var errorMessage: String? = nil

    private var timer: AnyCancellable?
    private var sequenceTask: Task<Void, Never>?

    init() {
        self.hostId = generateHostId()
    }

    // MARK: - Host ID Helper
    func generateHostId() -> String {
        let pool = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ"
        func randomPart() -> String {
            return String((0..<3).compactMap { _ in pool.randomElement() })
        }
        return "\(randomPart())-\(randomPart())-\(randomPart())"
    }

    func regenerateHostId() {
        guard !isConnected else { return }
        self.hostId = generateHostId()
    }

    func updateTargetHostId(_ input: String) {
        let cleaned = input.uppercased().filter { $0.isLetter || $0.isNumber }
        let trimmed = String(cleaned.prefix(9))
        if trimmed.count > 6 {
            let p1 = trimmed.prefix(3)
            let p2 = trimmed.dropFirst(3).prefix(3)
            let p3 = trimmed.suffix(from: trimmed.index(trimmed.startIndex, offsetBy: 6))
            clientTargetHostId = "\(p1)-\(p2)-\(p3)"
        } else if trimmed.count > 3 {
            let p1 = trimmed.prefix(3)
            let p2 = trimmed.dropFirst(3)
            clientTargetHostId = "\(p1)-\(p2)"
        } else {
            clientTargetHostId = trimmed
        }
        errorMessage = nil
    }

    // MARK: - Actions
    func togglePower() {
        if isConnected {
            disconnect()
        } else {
            if activeTab == "host" {
                startHosting()
            } else {
                if clientTargetHostId.isEmpty {
                    clientTargetHostId = "X7Y-9PL-2QZ"
                }
                connectAsClient()
            }
        }
    }

    func startHosting() {
        guard !isConnected else { return }
        sequenceTask?.cancel()
        timer?.cancel()

        role = "host"
        isConnected = true
        connectionStage = .connected
        statusMessage = "Hosting Connection"
        connectedClientsCount = 1
        connectionTimeSeconds = 0
        dataTransferredMB = 0.0
        pingMs = 18
        errorMessage = nil

        VPNManager.shared.startTunnel(role: "host", hostId: hostId) { _ in }
        startMetricsSimulation(isHost: true)
    }

    func connectAsClient() {
        let cleaned = clientTargetHostId.filter { $0.isLetter || $0.isNumber }
        guard cleaned.count == 9 else {
            errorMessage = "Please enter a valid 9-character Host ID (e.g. X7Y-9PL-2QZ)"
            return
        }

        guard !isConnected else { return }
        sequenceTask?.cancel()
        timer?.cancel()

        sequenceTask = Task { @MainActor in
            // Step 1: Resolving
            self.role = "client"
            self.isConnected = false
            self.connectionStage = .resolvingPeer
            self.statusMessage = "Resolving Peer..."
            self.stepDescription = "Locating peer node for Host ID \(self.clientTargetHostId)"
            try? await Task.sleep(nanoseconds: 600_000_000)

            // Step 2: Authenticating
            self.connectionStage = .authenticating
            self.statusMessage = "Authenticating..."
            self.stepDescription = "Verifying cryptographic credentials with peer"
            try? await Task.sleep(nanoseconds: 750_000_000)

            // Step 3: Establishing tunnel
            self.connectionStage = .establishingTunnel
            self.statusMessage = "Establishing secure tunnel..."
            self.stepDescription = "Negotiating ChaCha20 cipher & virtual IP interface"
            try? await Task.sleep(nanoseconds: 800_000_000)

            // Step 4: Connected
            self.role = "client"
            self.isConnected = true
            self.connectionStage = .connected
            self.statusMessage = "Connected to Host"
            self.stepDescription = "Tunnel active via P2P relay"
            self.connectionTimeSeconds = 0
            self.dataTransferredMB = 0.0
            self.pingMs = 24

            VPNManager.shared.startTunnel(role: "client", hostId: self.clientTargetHostId) { _ in }
            self.startMetricsSimulation(isHost: false)
        }
    }

    func disconnect() {
        sequenceTask?.cancel()
        sequenceTask = nil
        timer?.cancel()
        timer = nil

        role = "idle"
        isConnected = false
        connectionStage = .idle
        statusMessage = "Disconnected"
        stepDescription = ""
        downloadSpeedMbps = 0.0
        uploadSpeedMbps = 0.0
        pingMs = 0
        connectedClientsCount = 0

        VPNManager.shared.stopTunnel()
    }

    private func startMetricsSimulation(isHost: Bool) {
        timer = Timer.publish(every: 1.0, on: .main, in: .common)
            .autoconnect()
            .sink { [weak self] _ in
                guard let self = self, self.isConnected else { return }
                self.connectionTimeSeconds += 1

                let down = isHost ? Float.random(in: 6.0...18.0) : Float.random(in: 14.0...38.5)
                let up = isHost ? Float.random(in: 18.0...48.0) : Float.random(in: 3.0...9.5)
                let secondMB = ((down + up) / 8.0) * Float.random(in: 0.7...1.2)

                self.downloadSpeedMbps = down
                self.uploadSpeedMbps = up
                self.dataTransferredMB += secondMB
                self.pingMs = isHost ? Int.random(in: 15...22) : Int.random(in: 21...32)
            }
    }

    var formattedTimer: String {
        let hours = connectionTimeSeconds / 3600
        let minutes = (connectionTimeSeconds % 3600) / 60
        let seconds = connectionTimeSeconds % 60
        if hours > 0 {
            return String(format: "%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            return String(format: "%02d:%02d", minutes, seconds)
        }
    }

    var formattedDataTransferred: String {
        if dataTransferredMB >= 1024.0 {
            return String(format: "%.2f GB", dataTransferredMB / 1024.0)
        } else {
            return String(format: "%.1f MB", dataTransferredMB)
        }
    }
}
