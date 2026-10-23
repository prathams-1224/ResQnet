package org.resqnet.sos.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import org.resqnet.sos.data.local.SosDao
import org.resqnet.sos.data.model.SosDeliveryState
import org.resqnet.sos.data.model.SosEntity
import org.resqnet.sos.data.model.SosSeverity
import java.util.UUID

class SosRepository(
    private val sosDao: SosDao,
    context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "org.resqnet.sos_preferences",
        Context.MODE_PRIVATE
    )

    /**
     * Get or generate a persistent anonymous device identifier.
     * Never uses hardware IMEI or MAC address for privacy protection.
     */
    fun getOrCreateDeviceId(): String {
        var deviceId = prefs.getString(KEY_DEVICE_ID, null)
        if (deviceId == null) {
            deviceId = "node_" + UUID.randomUUID().toString().substring(0, 8)
            prefs.edit().putString(KEY_DEVICE_ID, deviceId).apply()
        }
        return deviceId
    }

    /**
     * Save an emergency distress message locally in Room before attempting transmission.
     */
    suspend fun createAndSaveSos(
        messageText: String,
        peopleCount: Int,
        hasVulnerablePeople: Boolean,
        severity: SosSeverity,
        latitude: Double?,
        longitude: Double?,
        accuracyM: Float?,
        locationTimestamp: Long?
    ): SosEntity {
        val now = System.currentTimeMillis()
        val incidentId = UUID.randomUUID().toString()
        val messageId = UUID.randomUUID().toString()
        val deviceId = getOrCreateDeviceId()

        val entity = SosEntity(
            messageId = messageId,
            incidentId = incidentId,
            schemaVersion = "1.0.0",
            createdAt = now,
            expiresAt = now + (24 * 60 * 60 * 1000L), // 24 hour TTL
            messageText = messageText.trim(),
            peopleCount = peopleCount.coerceAtLeast(1),
            hasVulnerablePeople = hasVulnerablePeople,
            severity = severity,
            latitude = latitude,
            longitude = longitude,
            locationAccuracyM = accuracyM,
            locationTimestamp = locationTimestamp,
            originDeviceId = deviceId,
            hopCount = 0,
            deliveryState = SosDeliveryState.SAVED_LOCALLY,
            lastUpdatedAt = now
        )

        // Step 1: Save locally
        sosDao.insertSos(entity)

        // Step 2: Transition to queued for transmission
        val queued = entity.copy(
            deliveryState = SosDeliveryState.QUEUED_OFFLINE,
            lastUpdatedAt = System.currentTimeMillis()
        )
        sosDao.updateSos(queued)

        return queued
    }

    fun getAllSosFlow(): Flow<List<SosEntity>> = sosDao.getAllSosFlow()

    fun getPendingCountFlow(): Flow<Int> = sosDao.getPendingCountFlow()

    suspend fun getSosById(messageId: String): SosEntity? = sosDao.getSosById(messageId)

    suspend fun updateDeliveryState(messageId: String, newState: SosDeliveryState) {
        sosDao.updateDeliveryState(messageId, newState, System.currentTimeMillis())
    }

    companion object {
        private const val KEY_DEVICE_ID = "anonymous_device_id"
    }
}
