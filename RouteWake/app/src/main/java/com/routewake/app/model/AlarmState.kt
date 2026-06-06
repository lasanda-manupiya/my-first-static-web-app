package com.routewake.app.model

/**
 * Live state of an active location alarm. Held purely in memory (StateFlow) —
 * there is intentionally no database backing this.
 */
data class AlarmState(
    val destination: Place? = null,
    /** Selected alert radius in meters. */
    val radiusMeters: Int = 500,
    /** Whether the foreground tracking service is currently running. */
    val isTracking: Boolean = false,
    /** Whether the alarm is currently ringing. */
    val isRinging: Boolean = false,

    // Live telemetry, updated by the location service.
    val currentLatitude: Double? = null,
    val currentLongitude: Double? = null,
    /** Distance to destination in meters, or null if unknown yet. */
    val distanceMeters: Float? = null,
    /** Current speed in meters/second. */
    val speedMps: Float = 0f
) {
    /** Estimated time to arrival in seconds, or null when speed is too low to estimate. */
    val etaSeconds: Long?
        get() {
            val dist = distanceMeters ?: return null
            if (speedMps < 0.5f) return null // basically stationary
            return (dist / speedMps).toLong()
        }
}
