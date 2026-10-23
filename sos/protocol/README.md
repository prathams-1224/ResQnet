# ResQNet Offline Store-and-Forward Mesh Protocol (Specification & Roadmap)

## 1. Overview

This document specifies the communication contract and transmission architecture for the **ResQNet Offline Distress Relay Protocol**, connecting the Citizen Android Application (`sos/mobile`) to neighboring peer handsets and gateway transceivers.

During acute monsoon floods and cyclonic landfalls in Kerala, cellular base stations and terrestrial broadband infrastructure experience widespread power and backhaul failures. The ResQNet offline protocol enables trapped citizens to propagate emergency distress beacons across an ad-hoc peer-to-peer mesh.

---

## 2. Evaluation of Transport Technologies

Before implementing offline mesh relay in Phase 2, the transport layer must be selected based on real-world Android operational constraints:

| Transport Layer | Advantages | Android Limitations & Gotchas | Recommendation |
| :--- | :--- | :--- | :--- |
| **Google Nearby Connections API** | High-throughput, P2P Wi-Fi Hotspot negotiation, official Google Play Services library. | Requires Google Play Services; Wi-Fi Aware/Hotspot switching interrupts user connectivity; **background operations are severely restricted or killed by Android Battery Optimization / Doze mode without persistent foreground notifications**; high battery drain. | Viable for high-bandwidth chunk exchange when app is in foreground or during direct rescuer proximity. |
| **Bluetooth Low Energy (BLE) Advertisements** | Ultra-low power; supported across virtually 100% of Android devices (Android 8.0+); operates without pairing or active connection handshakes. | Payload limit: Legacy BLE advertisement is capped at 31 bytes (Extended Advertising supports up to 255 bytes). Requires compact binary packet encoding (e.g., Protobuf or Bit-Packing). | **Recommended as Primary Beacon Layer** for distress broadcasting and discovery. |
| **Wi-Fi Aware (NAN)** | True zero-infrastructure ad-hoc Wi-Fi without access points; moderate throughput. | Hardware support is fragmented across budget Android handsets common in rural Kerala; requires Android 8.0+. | Secondary fallback for cluster synchronization where hardware permits. |

> [!IMPORTANT]
> **Delivery Truth Invariant**:  
> Under no circumstances may the software claim a message is "Delivered" simply because it was broadcast over Bluetooth or accepted into a peer handset's cache. A distress signal is strictly labeled `RELAYED_BY_PEER` until an authenticated digital receipt or server acknowledgment is returned from incident command.

---

## 3. Protocol Message Binary Wire Format (< 256 bytes)

For BLE advertisement framing, the JSON `SosMessageContract` is serialized into a compact binary frame:

```
 0                   1                   2                   3
 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
| Magic (0x5251) = 'RQ'         | Version (0x01)| Severity (0-3)|
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                      Incident ID (16 Bytes UUID)              |
|                                                               |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                      Message ID (16 Bytes UUID)               |
|                                                               |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                      Timestamp UTC (4 Bytes, Epoch Seconds)   |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                      Latitude (4 Bytes, Fixed-point 1e-5)     |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                      Longitude (4 Bytes, Fixed-point 1e-5)    |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
| Accuracy (1 B)| People (1 B)  | Hop Count (1B)| Max Hops (1B) |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
| Flags (Vulnerable, Medical)   | Reserved / CRC-16 Checksum    |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
```

Total compact beacon size: **56 Bytes** — easily fit into extended BLE advertisement payloads without packet fragmentation.

---

## 4. Next Implementation Step

The immediate next milestone following Phase 1 UI and local persistence is:
**Implementing the Bluetooth LE Beacon Broadcaster and Scanner Service** in `sos/mobile/` to broadcast the 56-byte compact emergency beacon and detect neighboring peer nodes.
