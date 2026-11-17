import Foundation
import NetworkExtension

/// VPNManager handles the iOS NetworkExtension system tunnel configuration.
/// Equivalent to Android's VpnService.prepare and startService.
class VPNManager: ObservableObject {
    static let shared = VPNManager()
    
    private let tunnelBundleId = "com.aistudio.peerlinkvpn.PacketTunnel"
    private var providerManager: NETunnelProviderManager?
    
    @Published var isTunnelActive: Bool = false
    
    init() {
        loadManager()
    }
    
    func loadManager(completion: (() -> Void)? = nil) {
        NETunnelProviderManager.loadAllFromPreferences { [weak self] managers, error in
            if let error = error {
                print("PeerLink iOS: Error loading VPN preferences: \(error.localizedDescription)")
                return
            }
            
            if let existing = managers?.first(where: {
                ($0.protocolConfiguration as? NETunnelProviderProtocol)?.providerBundleIdentifier == self?.tunnelBundleId
            }) {
                self?.providerManager = existing
            } else {
                let newManager = NETunnelProviderManager()
                let config = NETunnelProviderProtocol()
                config.providerBundleIdentifier = self?.tunnelBundleId
                config.serverAddress = "PeerLink P2P Tunnel"
                newManager.protocolConfiguration = config
                newManager.localizedDescription = "PeerLink VPN"
                newManager.isEnabled = true
                self?.providerManager = newManager
            }
            
            completion?()
        }
    }
    
    func startTunnel(role: String, hostId: String, completion: @escaping (Bool) -> Void) {
        guard let manager = providerManager else {
            loadManager { [weak self] in
                self?.startTunnel(role: role, hostId: hostId, completion: completion)
            }
            return
        }
        
        manager.isEnabled = true
        manager.saveToPreferences { [weak self] error in
            if let error = error {
                print("PeerLink iOS: Error saving VPN config: \(error.localizedDescription)")
                completion(false)
                return
            }
            
            manager.loadFromPreferences { error in
                let options: [String: NSObject] = [
                    "role": role as NSString,
                    "hostId": hostId as NSString
                ]
                
                do {
                    try manager.connection.startVPNTunnel(options: options)
                    DispatchQueue.main.async {
                        self?.isTunnelActive = true
                    }
                    completion(true)
                } catch {
                    print("PeerLink iOS: Could not start tunnel: \(error.localizedDescription)")
                    completion(false)
                }
            }
        }
    }
    
    func stopTunnel() {
        providerManager?.connection.stopVPNTunnel()
        DispatchQueue.main.async {
            self.isTunnelActive = false
        }
    }
}
