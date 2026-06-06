package com.routewake.app.storage

import com.routewake.app.model.AlarmState
import com.routewake.app.model.Place
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Process-wide, in-memory holder for the currently active alarm.
 *
 * The foreground service (writer) and the UI/ViewModel (reader) both run in the
 * same process, so a singleton [StateFlow] is all we need — no database, no IPC.
 */
object ActiveAlarmStore {

    private val _state = MutableStateFlow(AlarmState())
    val state: StateFlow<AlarmState> = _state.asStateFlow()

    fun startTracking(destination: Place, radiusMeters: Int) {
        _state.update {
            AlarmState(
                destination = destination,
                radiusMeters = radiusMeters,
                isTracking = true,
                isRinging = false
            )
        }
    }

    fun updateLocation(lat: Double, lon: Double, distanceMeters: Float, speedMps: Float) {
        _state.update {
            it.copy(
                currentLatitude = lat,
                currentLongitude = lon,
                distanceMeters = distanceMeters,
                speedMps = speedMps
            )
        }
    }

    fun setRinging(ringing: Boolean) {
        _state.update { it.copy(isRinging = ringing) }
    }

    fun stop() {
        _state.value = AlarmState()
    }
}
