package org.resqnet.sos.data.model

/**
 * Honest delivery states for ResQNet SOS beacons.
 *
 * DESIGN INVARIANT:
 * Never mark a message as DELIVERED merely because it was placed in a queue,
 * saved locally, or transmitted via Bluetooth advertising.
 */
enum class SosDeliveryState(val displayName: String, val detailedStatus: String) {
    SAVED_LOCALLY(
        displayName = "Saved Locally",
        detailedStatus = "Persisted securely in device storage. Preparing broadcast."
    ),
    QUEUED_OFFLINE(
        displayName = "Queued Offline",
        detailedStatus = "In transmission queue awaiting local peer discovery or gateway."
    ),
    RELAYED_BY_PEER(
        displayName = "Relayed by Peer",
        detailedStatus = "Forwarded to a neighboring handset. Moving through mesh."
    ),
    UPLOADED_TO_BACKEND(
        displayName = "Uploaded to Server",
        detailedStatus = "Reached ResQNet gateway via internet. Awaiting command ack."
    ),
    ACKNOWLEDGED_BY_SERVER(
        displayName = "Server Acknowledged",
        detailedStatus = "Received and verified by incident command server."
    ),
    DELIVERED_CONFIRMED(
        displayName = "Delivered to Rescuer",
        detailedStatus = "Dispatched and confirmed by emergency rescue team."
    ),
    FAILED_EXPIRED(
        displayName = "Expired / Undelivered",
        detailedStatus = "Beacon time-to-live elapsed before reaching a gateway."
    )
}
