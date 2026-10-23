package org.resqnet.sos

import android.app.Application
import org.resqnet.sos.data.local.SosDatabase
import org.resqnet.sos.data.repository.SosRepository
import org.resqnet.sos.location.LocationHelper
import org.resqnet.sos.telemetry.DeviceStatusManager

class ResQNetSosApp : Application() {

    lateinit var database: SosDatabase
        private set

    lateinit var repository: SosRepository
        private set

    lateinit var locationHelper: LocationHelper
        private set

    lateinit var deviceStatusManager: DeviceStatusManager
        private set

    override fun onCreate() {
        super.onCreate()
        database = SosDatabase.getInstance(this)
        repository = SosRepository(database.sosDao(), this)
        locationHelper = LocationHelper(this)
        deviceStatusManager = DeviceStatusManager(this, locationHelper)
    }
}
