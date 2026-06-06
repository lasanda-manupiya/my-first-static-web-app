package com.routewake.app.utils

import android.location.Location
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Distance / formatting helpers.
 *
 * [haversineMeters] is a self-contained great-circle implementation (no Google
 * APIs). [distanceBetween] delegates to the platform [Location.distanceBetween]
 * which is generally a touch more accurate; both are provided as the spec
 * mentions either approach.
 */
object DistanceUtils {

    private const val EARTH_RADIUS_M = 6_371_000.0

    fun haversineMeters(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2.0) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_M * c
    }

    /** Uses Android's built-in calculation. Returns distance in meters. */
    fun distanceBetween(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Float {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0]
    }

    /** Human-friendly distance string, e.g. "850 m" or "2.4 km". */
    fun formatDistance(meters: Float): String =
        if (meters >= 1000f) {
            "%.1f km".format(meters / 1000f)
        } else {
            "${meters.toInt()} m"
        }

    /** Human-friendly ETA, e.g. "3 min" or "1 h 5 min". */
    fun formatEta(seconds: Long?): String {
        if (seconds == null) return "--"
        val totalMin = (seconds / 60).toInt()
        return when {
            totalMin <= 0 -> "<1 min"
            totalMin < 60 -> "$totalMin min"
            else -> "${totalMin / 60} h ${totalMin % 60} min"
        }
    }

    /** Speed in m/s rendered as km/h, e.g. "24 km/h". */
    fun formatSpeed(mps: Float): String = "${(mps * 3.6f).toInt()} km/h"
}
