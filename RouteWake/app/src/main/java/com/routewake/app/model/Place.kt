package com.routewake.app.model

/**
 * A geographic destination the user can set an alarm for.
 *
 * This is a plain in-memory model. When persisted (saved / recent places) it is
 * serialized to a simple delimited string in DataStore — no Room/SQL involved.
 */
data class Place(
    val name: String,
    val latitude: Double,
    val longitude: Double
) {
    /** Encodes the place to a single line usable for DataStore string sets. */
    fun encode(): String = "$name|$latitude|$longitude"

    companion object {
        /** Decodes a place previously produced by [encode]. Returns null if malformed. */
        fun decode(raw: String): Place? {
            val parts = raw.split("|")
            if (parts.size != 3) return null
            val lat = parts[1].toDoubleOrNull() ?: return null
            val lon = parts[2].toDoubleOrNull() ?: return null
            return Place(parts[0], lat, lon)
        }
    }
}
