package org.resqnet.sos

import org.junit.Assert.*
import org.junit.Test
import org.resqnet.sos.data.model.SosDeliveryState
import org.resqnet.sos.data.model.SosEntity
import org.resqnet.sos.data.model.SosSeverity

class SosEntityTest {

    @Test
    fun testSosEntityCreationWithValidLocation() {
        val sos = SosEntity(
            messageText = "Trapped in floodwater on second floor",
            peopleCount = 4,
            hasVulnerablePeople = true,
            severity = SosSeverity.CRITICAL,
            latitude = 9.9312,
            longitude = 76.2673,
            locationAccuracyM = 6.5f,
            originDeviceId = "node_test_123"
        )

        assertEquals("Trapped in floodwater on second floor", sos.messageText)
        assertEquals(4, sos.peopleCount)
        assertTrue(sos.hasVulnerablePeople)
        assertEquals(SosSeverity.CRITICAL, sos.severity)
        assertTrue(sos.hasValidLocation)
        assertEquals(0, sos.hopCount)
        assertEquals(SosDeliveryState.SAVED_LOCALLY, sos.deliveryState)
        assertEquals("1.0.0", sos.schemaVersion)
        assertNotNull(sos.messageId)
        assertNotNull(sos.incidentId)
    }

    @Test
    fun testSosEntityWithoutLocation() {
        val sos = SosEntity(
            messageText = "GPS unavailable due to dense canopy",
            peopleCount = 1,
            hasVulnerablePeople = false,
            severity = SosSeverity.HIGH,
            latitude = null,
            longitude = null,
            originDeviceId = "node_test_456"
        )

        assertFalse(sos.hasValidLocation)
        assertNull(sos.latitude)
        assertNull(sos.longitude)
        assertEquals(SosDeliveryState.SAVED_LOCALLY, sos.deliveryState)
    }

    @Test
    fun testDeliveryStateDisplayNameAndInvariants() {
        // Verify states never claim false delivery
        assertEquals("Saved Locally", SosDeliveryState.SAVED_LOCALLY.displayName)
        assertEquals("Queued Offline", SosDeliveryState.QUEUED_OFFLINE.displayName)
        assertEquals("Relayed by Peer", SosDeliveryState.RELAYED_BY_PEER.displayName)
        assertEquals("Uploaded to Server", SosDeliveryState.UPLOADED_TO_BACKEND.displayName)
        assertEquals("Server Acknowledged", SosDeliveryState.ACKNOWLEDGED_BY_SERVER.displayName)
        assertEquals("Delivered to Rescuer", SosDeliveryState.DELIVERED_CONFIRMED.displayName)
        assertEquals("Expired / Undelivered", SosDeliveryState.FAILED_EXPIRED.displayName)
    }
}
