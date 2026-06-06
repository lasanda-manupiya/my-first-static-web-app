package com.routewake.app.utils

object Constants {
    /** Selectable alert radii (meters) shown in the UI. */
    val RADIUS_OPTIONS = listOf(100, 200, 500, 1000, 2000, 5000)

    /** GPS update interval bounds. */
    const val LOCATION_INTERVAL_MS = 5_000L
    const val LOCATION_FASTEST_INTERVAL_MS = 5_000L
    const val LOCATION_MIN_DISTANCE_M = 0f

    /** Notifications. */
    const val TRACKING_CHANNEL_ID = "routewake_tracking"
    const val ALARM_CHANNEL_ID = "routewake_alarm"
    const val TRACKING_NOTIFICATION_ID = 1001

    /** Default map start point (London) when no location is known yet. */
    const val DEFAULT_LAT = 52.4862
    const val DEFAULT_LON = -1.8904

    /** Service actions. */
    const val ACTION_START = "com.routewake.app.action.START"
    const val ACTION_STOP = "com.routewake.app.action.STOP"
    const val EXTRA_LAT = "extra_lat"
    const val EXTRA_LON = "extra_lon"
    const val EXTRA_NAME = "extra_name"
    const val EXTRA_RADIUS = "extra_radius"
    const val EXTRA_HIGH_ACCURACY = "extra_high_accuracy"

    /** Snooze duration. */
    const val SNOOZE_MILLIS = 5 * 60 * 1000L

    /** Helper to render a radius as a friendly label, e.g. 1000 -> "1km". */
    fun radiusLabel(meters: Int): String =
        if (meters >= 1000) {
            val km = meters / 1000.0
            if (km % 1.0 == 0.0) "${km.toInt()}km" else "${km}km"
        } else {
            "${meters}m"
        }
}
