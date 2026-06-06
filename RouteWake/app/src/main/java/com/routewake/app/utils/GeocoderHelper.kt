package com.routewake.app.utils

import android.content.Context
import android.location.Geocoder
import android.os.Build
import com.routewake.app.model.Place
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

/**
 * Forward / reverse geocoding using Android's built-in [Geocoder].
 * This is a local device feature (backed by the platform geocoder) — no paid
 * APIs and no network keys required.
 */
object GeocoderHelper {

    suspend fun search(context: Context, query: String): List<Place> {
        if (query.isBlank() || !Geocoder.isPresent()) return emptyList()
        val geocoder = Geocoder(context, Locale.getDefault())

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { cont ->
                geocoder.getFromLocationName(query, 5) { addresses ->
                    cont.resume(addresses.map { it.toPlace(query) })
                }
            }
        } else {
            withContext(Dispatchers.IO) {
                @Suppress("DEPRECATION")
                val results = try {
                    geocoder.getFromLocationName(query, 5)
                } catch (_: Exception) {
                    null
                }
                results?.map { it.toPlace(query) } ?: emptyList()
            }
        }
    }

    suspend fun reverse(context: Context, lat: Double, lon: Double): String {
        val fallback = "%.4f, %.4f".format(lat, lon)
        if (!Geocoder.isPresent()) return fallback
        val geocoder = Geocoder(context, Locale.getDefault())

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { cont ->
                geocoder.getFromLocation(lat, lon, 1) { addresses ->
                    cont.resume(addresses.firstOrNull()?.label() ?: fallback)
                }
            }
        } else {
            withContext(Dispatchers.IO) {
                @Suppress("DEPRECATION")
                val results = try {
                    geocoder.getFromLocation(lat, lon, 1)
                } catch (_: Exception) {
                    null
                }
                results?.firstOrNull()?.label() ?: fallback
            }
        }
    }

    private fun android.location.Address.toPlace(query: String): Place =
        Place(name = label().ifBlank { query }, latitude = latitude, longitude = longitude)

    private fun android.location.Address.label(): String {
        featureName?.let { fn ->
            val locality = locality ?: subAdminArea
            if (!fn.equals(locality, ignoreCase = true) && !locality.isNullOrBlank()) {
                return "$fn, $locality"
            }
        }
        return getAddressLine(0)
            ?: locality
            ?: adminArea
            ?: countryName
            ?: ""
    }
}
