package com.routewake.app.storage

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.routewake.app.model.Place
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Stores "recent" and "saved" places as newline-delimited strings in the same
 * DataStore used for settings. Deliberately simple — no Room database.
 */
class PlacesRepository(private val context: Context) {

    private object Keys {
        val RECENT = stringPreferencesKey("recent_places")
        val SAVED = stringPreferencesKey("saved_places")
    }

    private val maxRecent = 8

    val recentPlaces: Flow<List<Place>> =
        context.dataStore.data.map { prefs -> decodeList(prefs[Keys.RECENT]) }

    val savedPlaces: Flow<List<Place>> =
        context.dataStore.data.map { prefs -> decodeList(prefs[Keys.SAVED]) }

    suspend fun addRecent(place: Place) {
        context.dataStore.edit { prefs ->
            val current = decodeList(prefs[Keys.RECENT])
                .filterNot { it.sameLocation(place) }
                .toMutableList()
            current.add(0, place)
            prefs[Keys.RECENT] = encodeList(current.take(maxRecent))
        }
    }

    suspend fun savePlace(place: Place) {
        context.dataStore.edit { prefs ->
            val current = decodeList(prefs[Keys.SAVED])
            if (current.none { it.sameLocation(place) }) {
                prefs[Keys.SAVED] = encodeList(current + place)
            }
        }
    }

    suspend fun removeSaved(place: Place) {
        context.dataStore.edit { prefs ->
            val current = decodeList(prefs[Keys.SAVED]).filterNot { it.sameLocation(place) }
            prefs[Keys.SAVED] = encodeList(current)
        }
    }

    private fun encodeList(places: List<Place>): String =
        places.joinToString("\n") { it.encode() }

    private fun decodeList(raw: String?): List<Place> =
        raw?.split("\n")
            ?.filter { it.isNotBlank() }
            ?.mapNotNull { Place.decode(it) }
            ?: emptyList()

    private fun Place.sameLocation(other: Place): Boolean =
        kotlin.math.abs(latitude - other.latitude) < 1e-5 &&
            kotlin.math.abs(longitude - other.longitude) < 1e-5
}
