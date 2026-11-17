# PeerLink VPN for iOS

This directory contains the native Apple iOS implementation of **PeerLink VPN**, built with SwiftUI and Apple's **NetworkExtension (`NEPacketTunnelProvider`)** framework.

PeerLink allows peer-to-peer internet sharing and encrypted tunneling between Android and iOS devices using the exact same 9-character Host ID protocol (e.g., `X7Y-9PL-2QZ`).

---

## Architecture Overview

| Component | Android Counterpart | iOS Implementation |
|---|---|---|
| **VPN Service** | `android.net.VpnService` | `NEPacketTunnelProvider` (`PacketTunnelProvider.swift`) |
| **System Manager** | `VpnService.prepare()` | `NETunnelProviderManager` (`VPNManager.swift`) |
| **UI Framework** | Jetpack Compose (M3) | SwiftUI (`ContentView.swift`) |
| **State Pattern** | MVVM + `StateFlow` | MVVM + `ObservableObject` / `@Published` |
| **Virtual Subnet** | `10.8.0.0/24` (MTU 1500) | `10.8.0.0/24` (MTU 1500) |
| **Crypto & Cipher**| ChaCha20-Poly1305 (256-bit) | ChaCha20-Poly1305 (256-bit) |

---

## Opening and Building in Xcode

### 1. Requirements
- macOS Ventura 13.0+ or Sonoma / Sequoia
- Xcode 15+ or 16+
- Apple Developer Account (Personal or Team account to sign NetworkExtension entitlements)

### 2. Creating / Importing into Xcode
1. In Xcode, select **File > New > Project...** -> **iOS App** with SwiftUI interface.
2. Name the project `PeerLinkVPN` with Bundle Identifier `com.aistudio.peerlinkvpn`.
3. Drag the files from `iosApp/PeerLinkVPN/` into your main app target:
   - `PeerLinkApp.swift`
   - `ContentView.swift`
   - `PeerLinkViewModel.swift`
   - `VPNManager.swift`
   - `Theme.swift`
   - `Info.plist`
4. Add a **Network Extension Target**:
   - In Xcode, go to **File > New > Target...** -> Choose **Network Extension**.
   - Name it `PacketTunnel` with Bundle Identifier `com.aistudio.peerlinkvpn.PacketTunnel`.
   - Provider Type: **Packet Tunnel Provider**.
   - Replace the generated file with `iosApp/PacketTunnel/PacketTunnelProvider.swift`.
5. In Xcode project settings under **Signing & Capabilities**:
   - Add Capability: **Network Extensions** -> Check **Packet Tunnel**.
   - Add Capability: **App Groups** -> Check `group.com.aistudio.peerlinkvpn`.
6. Connect an iPhone or run on an iOS Simulator and press **Command + R** to run.

---

## Cross-Platform Peer-to-Peer Interoperability
- **Android Host $\rightarrow$ iOS Client:** Start hosting on an Android device, copy the 9-character Host ID (e.g. `X7Y-9PL-2QZ`), and enter it into the iOS app in Client Mode.
- **iOS Host $\rightarrow$ Android Client:** Start hosting on an iPhone, copy the 9-character Host ID, and enter it into the Android app in Client Mode.
- Both platforms share the same packet framing and virtual subnet (`10.8.0.1` gateway, `10.8.0.2` client).
